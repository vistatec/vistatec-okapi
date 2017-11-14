/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.virtualdb.jdbc.h2;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.virtualdb.IVDocument;
import net.sf.okapi.virtualdb.IVItem;
import net.sf.okapi.virtualdb.IVItem.ItemType;
import net.sf.okapi.virtualdb.IVRepository.OpeningMode;
import net.sf.okapi.virtualdb.KeyAndSegId;
import net.sf.okapi.virtualdb.jdbc.IDBAccess;

public class H2Access implements IDBAccess {

	public static final int ITEMKIND_DOCUMENT = 0;
	public static final int ITEMKIND_SUBDOCUMENT = 1;
	public static final int ITEMKIND_GROUP = 2;
	public static final int ITEMKIND_TEXTUNIT = 3;

	public static final String H2DB_EXT = ".h2.db";
	public static final int VERSION = 100;
	
	public static final String INFO_TBLNAME = "INFO";
	public static final String INFO_KEY = "KEY";
	public static final String INFO_VERSION = "VERSION";
	public static final String INFO_EXTRA1 = "EXTRA1";

//	public static final String DOCS_TBLNAME = "DOCS";
//	public static final String DOCS_KEY = "KEY";
//	public static final String DOCS_XID = "XID";
//	public static final String DOCS_NAME = "NAME";
//	public static final String DOCS_TYPE = "TYPE";

	public static final String ITMS_TBLNAME = "ITMS";
	public static final String ITMS_KEY = "KEY";
	public static final String ITMS_DKEY = "DKEY";
	public static final String ITMS_PARENT = "PARENT";
	public static final String ITMS_FCHILD = "FCHILD";
	public static final String ITMS_PREV = "PREV";
	public static final String ITMS_NEXT = "NEXT";
	public static final String ITMS_KIND = "KIND";
	public static final String ITMS_LEVEL = "LEVEL";
	public static final String ITMS_XID = "XID";
	public static final String ITMS_NAME = "NAME";
	public static final String ITMS_TYPE = "TYPE";

	public static final String TUNS_TBLNAME = "TUNS";
	public static final String TUNS_KEY = "KEY";
	public static final String TUNS_IKEY = "IKEY";
	public static final String TUNS_CTEXT = "CTEXT";
	public static final String TUNS_CODES = "CODES";
	public static final String TUNS_TRGCTEXT = "TRGCTEXT";
	public static final String TUNS_TRGCODES = "TRGCODES";

	// Temporary table
	public static final String SEGS_TBLNAME = "SEGS";
	public static final String SEGS_KEY = "KEY"; // Index for the table
	public static final String SEGS_IKEY = "IKEY"; // Key of the item for this segment
	public static final String SEGS_SID = "SID"; // Id of this segment
	public static final String SEGS_CTEXT = "CTEXT";
	public static final String SEGS_TRGCTEXT = "TRGCTEXT";

	private H2Access self;
	private RepositoryType repoType;
	private String baseDir;
	private Connection conn = null;
	private PreparedStatement pstmItemByKey;
	private PreparedStatement pstmItemById;
	
	// Variables used during import
	private IFilterConfigurationMapper fcMapper;

	/**
	 * Creates the repository in memory.
	 * @param fcMapper the filter configuration mapper to use for importing files. Can be null
	 * if no file is imported during the session.
	 */
	public H2Access (IFilterConfigurationMapper fcMapper) {
		initialize(RepositoryType.INMEMORY, fcMapper);
	}
	
	/**
	 * Creates the repository to work in a given folder.
	 * @param baseDirectory the folder where to work for this repository.
	 * @param fcMapper the filter configuration mapper to use for importing files. Can be null
	 * if no file is imported during the session.
	 */
	public H2Access (String baseDirectory,
		IFilterConfigurationMapper fcMapper)
	{
		initialize(RepositoryType.LOCAL, fcMapper);
		baseDir = baseDirectory;
		if ( !baseDir.endsWith("/") || !baseDir.endsWith("\\") ) {
			baseDir += "/";
		}
	}

	// Not tested
//	public H2Access (URL baseURL,
//		IFilterConfigurationMapper fcMapper)
//	{
//		initialize(RepositoryType.REMOTE, fcMapper);
//	}
	
	private void initialize (RepositoryType repositoryType,
		IFilterConfigurationMapper fcMapper)
	{
		this.repoType = repositoryType;
		this.fcMapper = fcMapper;
		self = this;
		try {
			// Initialize the driver
			Class.forName("org.h2.Driver");
		}
		catch ( ClassNotFoundException e ) {
			throw new OkapiException(e);
		}
	}
	
	/**
	 * Sets the filter configuration mapper.
	 * @param fcMapper the new filter configuration mapper to use.
	 */
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}

	@Override
	public void close () {
		try {
			if ( pstmItemByKey != null ) {
				pstmItemByKey.close();
				pstmItemByKey = null;
			}
			if ( pstmItemById != null ) {
				pstmItemById.close();
				pstmItemById = null;
			}
			if ( conn != null ) {
				conn.close();
				conn = null;
			}
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
	}

	private void initGlobal ()
		throws SQLException
	{
		if ( conn == null ) return;
		pstmItemByKey = conn.prepareStatement("select * from ITMS left join TUNS on ITMS.KEY=TUNS.IKEY WHERE ITMS.KEY=?");
		pstmItemById = conn.prepareStatement("select * from ITMS left join TUNS on ITMS.KEY=TUNS.IKEY where ITMS.XID=? and ITMS.DKEY=?");
	}

	public void open (String name,
		OpeningMode mode)
	{
		// Close existing connection
		close();
		
		if ( name.endsWith(H2DB_EXT) ) {
			name = name.substring(0, name.length()-H2DB_EXT.length());
		}
		// Create the connection string
		String connStr = null;
		switch ( repoType ) {
		case INMEMORY:
			connStr = "jdbc:h2:"+name;
			if ( mode == OpeningMode.MUST_EXIST ) {
				connStr += ";IFEXISTS=TRUE";
			}
			break;
			
		case LOCAL:
			String path = baseDir+name;
			// In OVERWRITE mode we always delete existing database
			if ( mode == OpeningMode.OVERWRITE ) {
				File file = new File(path+H2DB_EXT);
				if ( file.exists() ) {
					deleteFiles(path);
				}
			}
			// In all case we check/create the path
			Util.createDirectories(path);
			connStr = "jdbc:h2:"+baseDir+name;
			if ( mode == OpeningMode.MUST_EXIST ) {
				connStr += ";IFEXISTS=TRUE";
			}
			break;
			
		default:
			throw new OkapiException("Unsupported repository type.");
		}
		
		// Create the connection
		try {
			conn = DriverManager.getConnection(connStr, "sa", "");
			conn.setAutoCommit(true);
			createTables();
			initGlobal();
		}
		catch (SQLException e) {
			throw new OkapiException(e);
		}
	}
	
	//TODO: remove this method
	@Override
	public void open (String name) {
		// Close existing connection
		close();
		// Create the connection string
		String connStr = null;
		switch ( repoType ) {
		case INMEMORY:
			connStr = "jdbc:h2:mem:"+name+";IFEXISTS=TRUE";
			break;
		case LOCAL:
			connStr = "jdbc:h2:"+baseDir+name+";IFEXISTS=TRUE";
			break;
		default:
			throw new OkapiException("Unsupported repository type.");
		}
		// Create the connection
		try {
			conn = DriverManager.getConnection(connStr, "sa", "");
			conn.setAutoCommit(true);
			initGlobal();
		}
		catch (SQLException e) {
			throw new OkapiException(e);
		}
	}

	//TODO: remove this method
	@Override
	public void create (String name) {
		// Close existing connection
		close();
		// Create the connection string
		String connStr = null;
		switch ( repoType ) {
		case INMEMORY:
			connStr = "jdbc:h2:mem:"+name;
			break;
		case LOCAL:
			// Check if a DB exists already
			String path = baseDir+name;
			File file = new File(path+H2DB_EXT);
			if ( file.exists() ) {
				deleteFiles(path);
			}
			else {
				Util.createDirectories(path);
			}
			// New DB
			connStr = "jdbc:h2:"+path;
			break;
		default:
			throw new OkapiException("Unsupported repository type.");
		}
		// Create the connection
		try {
			conn = DriverManager.getConnection(connStr, "sa", "");
			createTables();
			initGlobal();
		}
		catch (SQLException e) {
			throw new OkapiException(e);
		}
	}

	private void deleteFiles (String path) {
		class WildcharFilenameFilter implements FilenameFilter {
			String filename;
			public WildcharFilenameFilter (String filename) {
				this.filename = filename;
			}
			public boolean accept(File dir, String name) {
				return Pattern.matches(filename+"\\..*?\\.db", name);
			}
		}
		String dir = Util.getDirectoryName(path);
		String filename = Util.getFilename(path, false);
		File d = new File(dir);
		File[] list = d.listFiles(new WildcharFilenameFilter(filename));
		if ( list == null ) return;
		for ( File f : list ) {
			f.delete();
		}
	}
	
	@Override
	public void delete () {
		//TODO: delete repository
		throw new UnsupportedOperationException("delete()");
	}

//	@Override
//	public IVDocument getDocument (String docId) {
//		Statement stm = null;
//		IVDocument doc = null;
//		try {
//			stm = conn.createStatement();
//			ResultSet rs = stm.executeQuery(String.format(
//				"SELECT * FROM %s WHERE %s='%s'", DOCS_TBLNAME, DOCS_XID, docId));
//			if ( rs.first() ) {
//				doc = new H2Document(this, rs.getLong(DOCS_KEY), rs.getString(DOCS_XID), rs.getString(DOCS_NAME), rs.getString(DOCS_TYPE));
//			}
//		}
//		catch ( SQLException e ) {
//			throw new OkapiException(e);
//		}
//		finally {
//			try {
//				if ( stm != null ) {
//					stm.close();
//					stm = null;
//				}
//			}
//			catch ( SQLException e ) {
//				throw new OkapiException(e);
//			}
//		}
//		return doc;
//	}

	@Override
	public Iterable<IVDocument> documents () {
		return new Iterable<IVDocument>() {
			@Override
			public Iterator<IVDocument> iterator() {
				return new H2DocumentIterator(self);
			};
		};
	}
	
	/**
	 * Gets the list of the keys to all the documents in this repository.
	 * @return a list of the keys to all the documents in this repository.
	 */
	List<Long> getDocumentsKeys () {
		Statement stm = null;
		try {
			stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(String.format("select KEY from ITMS where KIND=%d", ITEMKIND_DOCUMENT));
			List<Long> list = new ArrayList<Long>();
			while ( rs.next() ) {
				list.add(rs.getLong(1));
			}
			return list;
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error reading keys.\n"+e.getMessage());
		}
		finally {
			try {
				if ( stm != null ) {
					stm.close();
					stm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
	}

	/**
	 * Gets a list of keys for the given document.
	 * @param docKey the key of the document for which to get the items, use -1 for all documents.
	 * @param tuOnly true to get only the text unit items.
	 * @return the list of keys for the given document
	 */
	List<Long> getItemsKeys (long docKey,
		boolean tuOnly)
	{
		Statement stm = null;
		try {
			stm = conn.createStatement();
			// Construct the SQL query
			String query = "select KEY from ITMS";
			if ( docKey == -1 ) {
				if ( tuOnly ) {
					query += String.format(" where KIND=%d", ITEMKIND_TEXTUNIT);
				}
			}
			else {
				query += String.format(" where DKEY=%d", docKey);
				if ( tuOnly ) {
					query += String.format(" and KIND=%d", ITEMKIND_TEXTUNIT);
				}
			}
			// Get the list
			ResultSet rs = stm.executeQuery(query);
			List<Long> list = new ArrayList<Long>();
			while ( rs.next() ) {
				list.add(rs.getLong(1));
			}
			return list;
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error reading keys.\n"+e.getMessage());
		}
		finally {
			try {
				if ( stm != null ) {
					stm.close();
					stm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
	}

	@Override
	public String importDocument (RawDocument rd) {
		H2Importer imp = new H2Importer(this, fcMapper);
		imp.importDocument(rd);
		return null;
	}

	@Override
	public long importDocumentReturnKey (RawDocument rd) {
		H2Importer imp = new H2Importer(this, fcMapper);
		return imp.importDocument(rd);
//TODO: get real key		
//		return rd.getInputURI().getPath().hashCode();
	}

	private void createTables () {
		Statement stm = null;
		try {
			// First: make sure we don't have already the tables
			// We do that be looking if INFO exists
			ResultSet rs = conn.getMetaData().getTables(null, null, INFO_TBLNAME, null);
			if ( rs.next() ) return; // Done

			// Else: Create the tables
			stm = conn.createStatement();
			
			/* Create the table of information
			CREATE TABLE INFO (
				KEY INTEGER IDENTITY PRIMARY KEY,
				VERSION INTEGER,
				EXTRA1 BLOB)
			*/
			stm.execute("CREATE TABLE " + INFO_TBLNAME + " ("
				+ INFO_KEY + " INTEGER PRIMARY KEY,"
				+ INFO_VERSION + " INTEGER,"
				+ INFO_EXTRA1 + " BLOB"
				+ ")");
			
//			/* Create the table of documents
//			CREATE TABLE DOCS (
//				KEY INTEGER IDENTITY PRIMARY KEY,
//				XID VARCHAR,
//				NAME VARCHAR,
//				TYPE VARCHAR)
//			*/
//			stm.execute("CREATE TABLE " + DOCS_TBLNAME + " ("
//				+ DOCS_KEY + " INTEGER IDENTITY PRIMARY KEY,"
//				+ DOCS_XID + " VARCHAR,"
//				+ DOCS_NAME + " VARCHAR,"
//				+ DOCS_TYPE + " VARCHAR"
//				+ ")");
			
			/* Create the table of items
			CREATE TABLE ITMS (
				KEY INTEGER IDENTITY PRIMARY KEY,
				DKEY INTEGER REFERENCES DOCS(KEY),
				PARENT INTEGER,
				FCHILD INTEGER,
				PREV INTEGER,
				NEXT INTEGER,
				KIND INTEGER,
				LEVEL INTEGER,
				XID VARCHAR,
				NAME VARCHAR,
				TYPE VARCHAR)
			*/
			stm.execute("CREATE TABLE " + ITMS_TBLNAME + " ("
				+ ITMS_KEY + " INTEGER IDENTITY PRIMARY KEY,"
				+ ITMS_DKEY + " INTEGER,"
				+ ITMS_PARENT + " INTEGER,"
				+ ITMS_FCHILD + " INTEGER,"
				+ ITMS_PREV + " INTEGER,"
				+ ITMS_NEXT + " INTEGER,"
				+ ITMS_KIND + " INTEGER,"
				+ ITMS_LEVEL + " INTEGER,"
				+ ITMS_XID + " VARCHAR,"
				+ ITMS_NAME + " VARCHAR,"
				+ ITMS_TYPE + " VARCHAR"
				+ ")");
			
			/* Create the table of text units
			CREATE TABLE TUNS (
				KEY INTEGER IDENTITY PRIMARY KEY,
				IKEY INTEGER REFERENCES ITMS(KEY),
				CTEXT VARCHAR,
				CODES VARCHAR,
				TRGCTEXT VARCHAR,
				TRGCODES VARCHAR)
			*/
			stm.execute("CREATE TABLE " + TUNS_TBLNAME + " ("
				+ TUNS_KEY + " INTEGER IDENTITY PRIMARY KEY,"
				+ TUNS_IKEY + " INTEGER REFERENCES "+ITMS_TBLNAME+ "("+ITMS_KEY+") ON DELETE CASCADE,"
				+ TUNS_CTEXT + " VARCHAR,"
				+ TUNS_CODES + " VARCHAR,"
				+ TUNS_TRGCTEXT + " VARCHAR,"
				+ TUNS_TRGCODES + " VARCHAR"
				+ ")");
			
			stm.execute(String.format("INSERT INTO %s (%s,%s) VALUES(%d,%d)",
				INFO_TBLNAME, INFO_KEY, INFO_VERSION, 1, VERSION));
			
		}	
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
		finally {
			try {
				if ( stm != null ) {
					stm.close();
					stm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
	}
	
	void saveDocument (H2Document doc) {
		PreparedStatement pstm = null;
		try {
			/* The things to save in a document in this implementation are:
			 * The navigation pointers for previous next (if another document was added or removed)
			 * ... and that's about it.
			 */
			pstm = conn.prepareStatement(String.format("update ITMS set %s=?, %s=? where %s=?",
				ITMS_PREV, ITMS_NEXT, ITMS_KEY));
			pstm.setLong(1, doc.previous);
			pstm.setLong(2, doc.next);
			pstm.setLong(3, doc.key);
			pstm.execute();
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
		finally {
			try {
				if ( pstm != null ) {
					pstm.close();
					pstm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
	}
	
	protected void saveTextUnit (H2TextUnit htu) {
		PreparedStatement pstm = null;
		try {
			pstm = conn.prepareStatement(String.format("update TUNS set %s=?, %s=? where %s=?",
				TUNS_TRGCTEXT, TUNS_TRGCODES, TUNS_IKEY));
			String[] trgData = targetsToStorage(htu.getTextUnit());
			pstm.setString(1, trgData[0]); // Targets coded-text
			pstm.setString(2, trgData[1]); // Targets codes
			pstm.setLong(3, htu.getKey());
			pstm.execute();
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
		finally {
			try {
				if ( pstm != null ) {
					pstm.close();
					pstm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
	}
	
	IVItem getItemFromExtractionId (H2Document doc,
		String id)
	{
		try {
			// Always left-join with the TUNS table so we get extra text unit info in one call
			// This is ok because most calls are for text units.
			pstmItemById.setString(1, id);
			pstmItemById.setLong(2, doc.key);
			ResultSet rs = pstmItemById.executeQuery();
			// Return null if nothing found
			if ( !rs.first() ) return null;
			IVItem it= fillItem(doc, rs);
			return it;
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error reading an item.\n"+e.getMessage());
		}
	}
	
	IVItem getItemFromItemKey (H2Document doc,
		long itemKey)
	{
		if ( itemKey == -1 ) return null;
		try {
			// Always left-join with the TUNS table so we get extra text unit info in one call
			// This is ok because most calls are for text units.
			pstmItemByKey.setLong(1, itemKey);
			ResultSet rs = pstmItemByKey.executeQuery();
			// Return null if nothing found
			if ( !rs.first() ) return null;
			return fillItem(doc, rs);
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error reading an item.\n"+e.getMessage());
		}
	}

	private IVItem fillItem (H2Document doc,
		ResultSet rs)
	{
		IVItem item = null;
		try {
			switch ( rs.getInt(ITMS_KIND) ) {
			case ITEMKIND_TEXTUNIT:
				H2TextUnit htu = new H2TextUnit(rs.getLong(TUNS_IKEY), doc, rs.getString(ITMS_XID),
					rs.getString(ITMS_NAME), rs.getString(ITMS_TYPE));
				htu.fillPointers(rs.getLong(ITMS_PARENT), rs.getLong(ITMS_PREV), rs.getLong(ITMS_NEXT));
				ITextUnit tu = htu.getTextUnit();
				tu.setSource(TextContainer.splitStorageToContent(rs.getString(TUNS_CTEXT), rs.getString(TUNS_CODES)));
				storageToTargets(tu, rs.getString(TUNS_TRGCTEXT), rs.getString(TUNS_TRGCODES));
				item = htu;
				break;
			case ITEMKIND_GROUP:
				H2Group grp = new H2Group(rs.getLong("ITMS.KEY"), doc, rs.getString(ITMS_XID),
					rs.getString(ITMS_NAME), rs.getString(ITMS_TYPE));
				grp.fillPointers(rs.getLong(ITMS_PARENT), rs.getLong(ITMS_FCHILD),
					rs.getLong(ITMS_PREV), rs.getLong(ITMS_NEXT));
				item = grp;
				break;
			case ITEMKIND_SUBDOCUMENT:
				H2SubDocument sd = new H2SubDocument(rs.getLong("ITMS.KEY"), doc, rs.getString(ITMS_XID),
					rs.getString(ITMS_NAME), rs.getString(ITMS_TYPE));
				sd.fillPointers(rs.getLong(ITMS_PARENT), rs.getLong(ITMS_FCHILD),
					rs.getLong(ITMS_PREV), rs.getLong(ITMS_NEXT));
				item = sd;
				break;
			case ITEMKIND_DOCUMENT:
				H2Document newDoc = new H2Document(this, rs.getLong("ITMS.KEY"), rs.getString(ITMS_XID),
					rs.getString(ITMS_NAME), rs.getString(ITMS_TYPE));
				newDoc.fillPointers(rs.getLong(ITMS_PARENT), rs.getLong(ITMS_FCHILD),
					rs.getLong(ITMS_PREV), rs.getLong(ITMS_NEXT));
				item = newDoc;
				break;
			}
			return item;
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error filling item.\n"+e.getMessage());
		}
	}
	
	@Override
	public IVDocument getDocument (long itemKey) {
		Statement stm = null;
		IVDocument doc = null;
		try {
			stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(String.format(
				"select * from ITMS where KEY=%d", itemKey));
			if ( rs.first() ) {
				doc = new H2Document(this, itemKey, rs.getString(ITMS_XID), rs.getString(ITMS_NAME), rs.getString(ITMS_TYPE));
				((H2Document)doc).fillPointers(rs.getLong(ITMS_PARENT), rs.getLong(ITMS_FCHILD),
					rs.getLong(ITMS_PREV), rs.getLong(ITMS_NEXT));
			}
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
		finally {
			try {
				if ( stm != null ) {
					stm.close();
					stm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
		return doc;
	}

	void completeItemsWriting (LinkedHashMap<Long, H2Navigator> items) {
		PreparedStatement pstm = null;
		try {
			// Create the item entry to save the spot and get a key
			pstm = conn.prepareStatement(String.format("UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=? WHERE %s=?", ITMS_TBLNAME,
				ITMS_PARENT, ITMS_LEVEL, ITMS_FCHILD, ITMS_PREV, ITMS_NEXT, ITMS_KEY));
			for ( H2Navigator item : items.values() ) {
				pstm.setLong(1, item.parent);
				pstm.setLong(2, item.level);
				pstm.setLong(3, item.firstChild);
				pstm.setLong(4, item.previous);
				pstm.setLong(5, item.next);
				pstm.setLong(6, item.key);
				pstm.execute();
			}
		    pstm.close();
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
		finally {
			try {
				if ( pstm != null ) {
					pstm.close();
					pstm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
	}
	
	long writeResourceData (INameable res, ItemType type, long docKey) {
		long itemKey = -1;
		PreparedStatement pstm = null;
		try {
			// Create the item entry to save the spot and get a key
			pstm = conn.prepareStatement(String.format("INSERT INTO %s (%s,%s,%s,%s,%s) VALUES(?,?,?,?,?);", ITMS_TBLNAME,
				ITMS_DKEY, ITMS_KIND, ITMS_XID, ITMS_NAME, ITMS_TYPE));
			pstm.setLong(1, docKey);
			switch ( type ) {
			case DOCUMENT:
				pstm.setShort(2, (short)ITEMKIND_DOCUMENT);
				break;
			case SUB_DOCUMENT:
				pstm.setShort(2, (short)ITEMKIND_SUBDOCUMENT);
				break;
			case GROUP:
				pstm.setShort(2, (short)ITEMKIND_GROUP);
				break;
			default:
				break;
			}
			pstm.setString(3, res.getId());
			pstm.setString(4, res.getName());
			pstm.setString(5, res.getType());
			pstm.execute();
			// Get the Item key
			ResultSet keys = pstm.getGeneratedKeys();
		    if ( keys.first() ) {
		    	itemKey = keys.getLong(1);
		    }
		    pstm.close();
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
		finally {
			try {
				if ( pstm != null ) {
					pstm.close();
					pstm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
		return itemKey;
	}

	@Override
	public void removeDocument (IVDocument vdoc) {
		PreparedStatement pstm = null;
		try {
			H2Document doc = (H2Document)vdoc;

			// The new next sibling of the document's previous sibling is now the document's next sibling
			if ( doc.previous > -1 ) {
				pstm = conn.prepareStatement(String.format("UPDATE %s SET %s=? WHERE %s=?", ITMS_TBLNAME,
					ITMS_NEXT, ITMS_KEY));
				pstm.setLong(1, doc.next);
				pstm.setLong(2, doc.previous);
				pstm.execute();
				pstm.close();
			}
			
			// The new previous sibling of the document's next sibling is now the document's previous sibling
			if ( doc.next > -1 ) {
				pstm = conn.prepareStatement(String.format("UPDATE %s SET %s=? WHERE %s=?", ITMS_TBLNAME,
					ITMS_PREV, ITMS_KEY));
				pstm.setLong(1, doc.previous);
				pstm.setLong(2, doc.next);
				pstm.execute();
				pstm.close();
			}
			
			// Delete all items related the document
			pstm = conn.prepareStatement(String.format("delete from %s where %s=?",
				ITMS_TBLNAME, ITMS_DKEY));
			pstm.setLong(1, doc.key);
			pstm.execute();
			pstm.close();
			
			// Delete the document entry itself
			pstm = conn.prepareStatement(String.format("delete from %s where %s=?",
				ITMS_TBLNAME, ITMS_KEY));
			pstm.setLong(1, doc.key);
			pstm.execute();
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
		finally {
			try {
				if ( pstm != null ) {
					pstm.close();
					pstm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
	}
	
//	long writeSubDocumentData (StartSubDocument ssd) {
//		long itemKey = -1;
//		PreparedStatement pstm = null;
//		try {
//			// Create the item entry to save the spot and get a key
//			pstm = conn.prepareStatement(String.format("INSERT INTO %s (%s,%s,%s,%s,%s) VALUES(?,?,?,?,?);", ITMS_TBLNAME,
//				ITMS_DKEY, ITMS_KIND, ITMS_XID, ITMS_NAME, ITMS_TYPE));
//			pstm.setLong(1, -1L);
//			pstm.setShort(2, (short)ITEMKIND_DOCUMENT);
//			pstm.setString(3, ssd.getId());
//			pstm.setString(4, ssd.getName());
//			pstm.setString(5, ssd.getType());
//			pstm.execute();
//			// Get the Item key
//			ResultSet keys = pstm.getGeneratedKeys();
//		    if ( keys.first() ) {
//		    	itemKey = keys.getLong(1);
//		    }
//		    pstm.close();
//		}
//		catch ( SQLException e ) {
//			throw new OkapiException(e);
//		}
//		finally {
//			try {
//				if ( pstm != null ) {
//					pstm.close();
//					pstm = null;
//				}
//			}
//			catch ( SQLException e ) {
//				throw new OkapiException(e);
//			}
//		}
//		return itemKey;
//	}

	long writeTextUnitData (ITextUnit tu,
		long docKey)
	{
		long itemKey = -1;
		PreparedStatement pstm = null;
		try {
			// Create the item entry to save the spot and get a key
			pstm = conn.prepareStatement(String.format("INSERT INTO %s (%s,%s,%s,%s,%s) VALUES(?,?,?,?,?);", ITMS_TBLNAME,
				ITMS_DKEY, ITMS_KIND, ITMS_XID, ITMS_NAME, ITMS_TYPE));
			pstm.setLong(1, docKey);
			pstm.setShort(2, (short)ITEMKIND_TEXTUNIT);
			pstm.setString(3, tu.getId());
			pstm.setString(4, tu.getName());
			pstm.setString(5, tu.getType());
			pstm.execute();
			// Get the Item key
			ResultSet keys = pstm.getGeneratedKeys();
		    if ( keys.first() ) {
		    	itemKey = keys.getLong(1);
		    }
		    pstm.close();

		    // Create the text unit data entry
			pstm = conn.prepareStatement(String.format("INSERT INTO %s (%s,%s,%s,%s,%s) VALUES(?,?,?,?,?);", TUNS_TBLNAME,
				TUNS_IKEY, TUNS_CTEXT, TUNS_CODES, TUNS_TRGCTEXT, TUNS_TRGCODES));
			String[] srcData = TextContainer.contentToSplitStorage(tu.getSource());
			String[] trgData = targetsToStorage(tu);
			pstm.setLong(1, itemKey);
			pstm.setString(2, srcData[0]); // Source coded-text
			pstm.setString(3, srcData[1]); // Source codes
			pstm.setString(4, trgData[0]); // Targets coded-text
			pstm.setString(5, trgData[1]); // Targets codes
			pstm.execute();
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
		finally {
			try {
				if ( pstm != null ) {
					pstm.close();
					pstm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
		return itemKey;
	}

	private String[] targetsToStorage (ITextUnit tu) {
		String res[] = new String[2];
		StringBuilder tmp0 = new StringBuilder();
		StringBuilder tmp1 = new StringBuilder();
		Iterator<LocaleId> iter = tu.getTargetLocales().iterator();
		while ( iter.hasNext() ) {
			LocaleId loc = iter.next();
			TextContainer tc = tu.getTarget(loc);
			tmp0.append(loc.toString()+"|");
			String[] data = TextContainer.contentToSplitStorage(tc);
			tmp0.append(data[0]);  // Target coded text
			tmp0.append("\u0093");
			tmp1.append(data[1]); // Target codes
			tmp1.append("\u0093");
		}
		res[0] = tmp0.toString();
		res[1] = tmp1.toString();
		return res;
	}

	private void storageToTargets (ITextUnit tu,
		String ctext,
		String codes)
	{
		String[] codesParts = codes.split("\u0093", -2);
		String[] ctextParts = ctext.split("\u0093", -2);
		for ( int i=0; i<ctextParts.length-1; i++ ) {
			int n = ctextParts[i].indexOf('|');
			LocaleId loc = LocaleId.fromString(ctextParts[i].substring(0, n));
			TextContainer tc = TextContainer.splitStorageToContent(ctextParts[i].substring(n+1), codesParts[i]);
			tu.setTarget(loc, tc);
		}
	}

	@Override
	public IVDocument getFirstDocument () {
		List<Long> list = getDocumentsKeys();
		if ( list.size() < 1 ) return null;
		return getDocument(list.get(0));
	}

	@Override
	public void saveExtraData1 (InputStream inputStream) {
		PreparedStatement pstm = null;
		try {
			pstm = conn.prepareStatement(String.format("update %s set %s=? where %s=1",
				INFO_TBLNAME, INFO_EXTRA1, INFO_KEY));
			pstm.setBinaryStream(1, inputStream); 
			pstm.executeUpdate(); 
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
		finally {
			try {
				if ( pstm != null ) {
					pstm.close();
					pstm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
	}

	public InputStream loadExtraData1 () {
		PreparedStatement pstm = null;
		try {
			pstm = conn.prepareStatement(String.format("select %s from %s where %s=1",
				INFO_EXTRA1, INFO_TBLNAME, INFO_KEY));
			ResultSet rs = pstm.executeQuery();
			// Return null if nothing found
			if ( !rs.first() ) return null;
			// Otherwise: return the stream
			Blob blob = rs.getBlob(1);
			if ( blob == null ) return null;
			return blob.getBinaryStream();
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
		finally {
			try {
				if ( pstm != null ) {
					pstm.close();
					pstm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
	}

	public List<List<KeyAndSegId>> getSegmentsWithSameSourceButDifferentTarget (LocaleId trgLoc) {
		List<List<KeyAndSegId>> list = Collections.emptyList();
		Statement stm = null;
		PreparedStatement pstm = null;
		try {
			// Create a temporary table for the segments
			stm = conn.createStatement();
			// Drop the table if it exists already
			stm.execute("DROP TABLE IF EXISTS " + SEGS_TBLNAME);
			// Create the table
			stm.execute("CREATE TEMPORARY TABLE " + SEGS_TBLNAME + " ("
				+ SEGS_KEY + " INTEGER IDENTITY PRIMARY KEY,"
				+ SEGS_IKEY + " INTEGER,"
				+ SEGS_SID + " VARCHAR,"
				+ SEGS_CTEXT + " VARCHAR,"
				+ SEGS_TRGCTEXT + " VARCHAR"
				+ ")");
			
			//--- Fill the temporary table
			
			// Get all text units
			String query = String.format("select ITMS.KEY, %s, %s from ITMS left join TUNS on ITMS.KEY=TUNS.IKEY "
				+ "where ITMS.KIND=%d",
				TUNS_CTEXT, TUNS_TRGCTEXT, ITEMKIND_TEXTUNIT);
			ResultSet rs = stm.executeQuery(query);
			
			// For each text unit, get its segments and fill the temporary table
			pstm = conn.prepareStatement(String.format("INSERT INTO %s (%s,%s,%s,%s) VALUES(?,?,?,?)",
				SEGS_TBLNAME, SEGS_IKEY, SEGS_SID, SEGS_CTEXT, SEGS_TRGCTEXT));
			
			while ( rs.next() ) {
				ITextUnit tu = ((H2TextUnit)getItemFromItemKey(null, rs.getLong(1))).getTextUnit();
				ISegments srcSegs = tu.getSource().getSegments();
				// Get the target or create an empty one, then get the segments
				ISegments trgSegs = tu.createTarget(trgLoc, false, IResource.CREATE_EMPTY).getSegments();
				for ( Segment seg : srcSegs ) {
					Segment trgSeg = trgSegs.get(seg.id);
					pstm.setLong(1, rs.getLong(1));
					pstm.setString(2, seg.id);
					pstm.setString(3, seg.text.toString());
					pstm.setString(4, trgSeg==null ? "" : trgSeg.text.toString());
					pstm.execute();
				}
			}
			rs.close();
			rs = null; // Free as soon as possible

			// Now Get the sorted list of segments
			query = String.format("select %s, %s, %s, %s from %s order by %s DESC, %s",
				SEGS_IKEY, SEGS_SID, SEGS_CTEXT, SEGS_TRGCTEXT, SEGS_TBLNAME, SEGS_CTEXT, SEGS_TRGCTEXT);
			rs = stm.executeQuery(query);
			
			// Process the segments
			list = processSameSourceDifferentTarget(rs, 1, 2, 3, 4);
			
			// Drop the temporary table
			stm.execute("DROP TABLE IF EXISTS " + SEGS_TBLNAME);
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error when looking for groups of items.\n"+e.getMessage());
		}
		finally {
			try {
				if ( pstm != null ) {
					pstm.close();
					pstm = null;
				}
				if ( stm != null ) {
					stm.close();
					stm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
		return list;
	}
	
	
	private List<List<KeyAndSegId>> processSameSourceDifferentTarget (ResultSet rs,
		int itemKeyCol,
		int segIdCol,
		int srcCol,
		int trgCol)
		throws SQLException
	{
		List<List<KeyAndSegId>> list = new ArrayList<List<KeyAndSegId>>();

		// Loop through the results
		long prevKey = -1;
		String prevSegId = null;
		String prevSrc = "";
		String prevTrg = "";
		boolean hasDiff = false;
		List<KeyAndSegId> group = new ArrayList<KeyAndSegId>();
		while ( rs.next() ) {
			// Treat first entry case
			if ( prevKey == -1 ) {
				prevKey = rs.getLong(itemKeyCol);
				if ( segIdCol > 0 ) prevSegId = rs.getString(segIdCol);
				prevSrc = rs.getString(srcCol);
				prevTrg = rs.getString(trgCol);
				continue;
			}
			// Treat the entries after the first one
			// Check if the sources are the same
			if ( prevSrc.equals(rs.getString(srcCol)) ) {
				// We have at least two entries with the same source
				// Add the initial key of this group if not done yet
				if ( prevKey != -2 ) {
					group.add(new KeyAndSegId(prevKey, prevSegId));
					prevKey = -2; // Used now
				}
				// Add the current key
				group.add(new KeyAndSegId(
					rs.getLong(itemKeyCol),
					(segIdCol>0 ? rs.getString(segIdCol) : null))
				);
				// Compare targets if we have not detected already a difference
				if ( !hasDiff ) {
					if ( !prevTrg.equals(rs.getString(trgCol)) ) {
						hasDiff = true;
					}
				}
			}
			else { // New source
				// Add this group of entries with the same sources
				// to the results if they have at least one difference in targets
				if ( hasDiff ) {
					list.add(new ArrayList<KeyAndSegId>(group));
				}
				// Set the new base for the next comparisons
				hasDiff = false;
				group.clear();
				prevKey = rs.getLong(itemKeyCol);
				if ( segIdCol > 0 ) prevSegId = rs.getString(segIdCol);
				prevSrc = rs.getString(srcCol);
				prevTrg = rs.getString(trgCol);
			}
		}
		// Make sure we add the last group if needed
		if ( hasDiff ) {
			list.add(group);
		}
		
		return list;
	}
	
	public List<List<KeyAndSegId>> getSameSourceWithDifferentTarget () {
		List<List<KeyAndSegId>> list = Collections.emptyList();
		Statement stm = null;
		try {
			// Sort all entries by source
			stm = conn.createStatement();
			String query = String.format("select ITMS.KEY, %s, %s from ITMS left join TUNS on ITMS.KEY=TUNS.IKEY "
				+ "where ITMS.KIND=%d order by %s DESC, %s",
				TUNS_CTEXT, TUNS_TRGCTEXT, ITEMKIND_TEXTUNIT, TUNS_CTEXT, TUNS_TRGCTEXT);

			// Get the results
			ResultSet rs = stm.executeQuery(query);
			
			list = processSameSourceDifferentTarget(rs, 1, -1, 2, 3);

//			// Loop through the results
//			long prevKey = -1;
//			String prevSrc = "";
//			String prevTrg = "";
//			boolean hasDiff = false;
//			List<Long> group = new ArrayList<Long>();
//			while ( rs.next() ) {
//				// Treat first entry case
//				if ( prevKey == -1 ) {
//					prevKey = rs.getLong(1);
//					prevSrc = rs.getString(2);
//					prevTrg = rs.getString(3);
//					continue;
//				}
//				// Treat the entries after the first one
//				// Check if the sources are the same
//				String test = rs.getString(2);
//				if ( prevSrc.equals(rs.getString(2)) ) {
//					// We have at least two entries with the same source
//					// Add the initial key of this group if not done yet
//					if ( prevKey != -2 ) {
//						group.add(prevKey);
//						prevKey = -2; // Used now
//					}
//					// Add the current key
//					group.add(rs.getLong(1));
//					// Compare targets if we have not detected already a difference
//					if ( !hasDiff ) {
//						if ( !prevTrg.equals(rs.getString(3)) ) {
//							hasDiff = true;
//						}
//					}
//				}
//				else { // New source
//					// Add this group of entries with the same sources
//					// to the results if they have at least one difference in targets
//					if ( hasDiff ) {
//						list.add(new ArrayList<Long>(group));
//					}
//					// Set the new base for the next comparisons
//					hasDiff = false;
//					group.clear();
//					prevKey = rs.getLong(1);
//					prevSrc = rs.getString(2);
//					prevTrg = rs.getString(3);
//				}
//			}
//			// Make sure we add the last group if needed
//			if ( hasDiff ) {
//				list.add(group);
//			}
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error when looking for groups of items.\n"+e.getMessage());
		}
		finally {
			try {
				if ( stm != null ) {
					stm.close();
					stm = null;
				}
			}
			catch ( SQLException e ) {
				throw new OkapiException(e);
			}
		}
		return list;
	}

}
