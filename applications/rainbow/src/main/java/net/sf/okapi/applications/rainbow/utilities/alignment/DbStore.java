/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.utilities.alignment;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.TextContainer;

public class DbStore {

	//TODO
	public static final int  GROUP_PK       = 0;
	public static final int  GROUP_NAME     = 0;
	public static final int  GROUP_TYPE     = 0;
	public static final int  GROUP_PREWS    = 0;
	public static final int  GROUP_TRANS    = 0;

	public static final String TBLNAME_SOURCE    = "Source";

	public static final int  SOURCE_KEY          = 0;
	public static final String SOURCE_NKEY       = "Key";
	public static final int  SOURCE_GKEY         = 1;
	public static final String SOURCE_NTMP       = "Tmp";
	public static final int  SOURCE_TMP          = 2;
	public static final String SOURCE_NGKEY      = "GKey";
	public static final int  SOURCE_XKEY         = 3;
	public static final String SOURCE_NXKEY      = "XKey";
	public static final int  SOURCE_SEGKEY       = 4;
	public static final String SOURCE_NSEGKEY    = "SegKey";
	public static final int  SOURCE_NAME         = 5;
	public static final String SOURCE_NNAME      = "Name";
	public static final int  SOURCE_TYPE         = 6;
	public static final String SOURCE_NTYPE      = "Type";
	public static final int  SOURCE_TEXT         = 7;
	public static final String SOURCE_NTEXT      = "Text";
	public static final int  SOURCE_CODES        = 8;
	public static final String SOURCE_NCODES     = "Codes";
	public static final int  SOURCE_PREWS        = 9;
	public static final String SOURCE_NPREWS     = "PreWS";
	public static final int  SOURCE_TRANS        = 10;
	public static final String SOURCE_NTRANS     = "Trans";
	
	//TODO
	public static final int  TARGET_PK      = 0;
	public static final int  TARGET_XKEY    = 0;
	public static final int  TARGET_SKEY    = 0;
	public static final int  TARGET_TEXT    = 0;
	public static final int  TARGET_CODES   = 0;
	
	private static final String DATAFILE_EXT = ".h2.db";

	private Connection  conn = null;

	public DbStore () {
		try {
			// Initialize the driver
			Class.forName("org.h2.Driver");
		}
		catch ( ClassNotFoundException e ) {
			throw new OkapiException(e);
		}
	}
	
	public void close () {
		try {
			if ( conn != null ) {
				conn.close();
				conn = null;
			}
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
	}
	
	public void create (String folder,
		String dbName,
		boolean deleteExistingDB)
	{
		Statement stm = null;
		try {
			close();
			String path = folder+File.separatorChar+dbName;
			if ( (new File(path+DATAFILE_EXT)).exists() ) {
				if ( !deleteExistingDB ) return;
				// Else: delete the directory content
				Util.deleteDirectory(folder, false);
			}
			else Util.createDirectories(path);
			
			// Open the connection, this creates the DB if none exists
			conn = DriverManager.getConnection("jdbc:h2:"+path, "sa", "");
	
			// Create the source table
			stm = conn.createStatement();
			stm.execute("CREATE TABLE " + TBLNAME_SOURCE + " ("
				+ SOURCE_NKEY + " INTEGER IDENTITY PRIMARY KEY,"
				+ SOURCE_NTMP + " INTEGER,"
				+ SOURCE_NGKEY + " INTEGER,"
				+ SOURCE_NXKEY + " VARCHAR,"
				+ SOURCE_NSEGKEY + " INTEGER,"
				+ SOURCE_NNAME + " VARCHAR,"
				+ SOURCE_NTYPE + " VARCHAR,"
				+ SOURCE_NTEXT + " VARCHAR,"
				+ SOURCE_NCODES + " VARCHAR,"
				+ SOURCE_NPREWS + " BOOLEAN,"
				+ SOURCE_NTRANS + " BOOLEAN"
				+ ")");
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
	
	public void open (String path) {
		try {
			close();
			if ( !(new File(path+DATAFILE_EXT)).exists() ) return;
			conn = DriverManager.getConnection("jdbc:h2:"+path, "sa", "");
		}
		catch ( SQLException e ) {
			throw new OkapiException(e);
		}
	}
	
	public int getTextUnitCount () {
		Statement stm = null;
		try {
			stm = conn.createStatement();
			String query;
			query = "SELECT COUNT(" + SOURCE_NKEY + ") FROM " + TBLNAME_SOURCE;
			ResultSet result = stm.executeQuery(query);
			if ( !result.first() ) return 0;
			return result.getInt(1);
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
	
	public void addSourceEntry (TextContainer tc,
		int gKey,
		String tuId,
		String tuName,
		String tuType)
	{
		PreparedStatement pstm = null;
		try {
			//TODO: make this pstm class-level
			pstm = conn.prepareStatement(String.format("INSERT INTO %s (%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?);",
				TBLNAME_SOURCE, SOURCE_NTMP, SOURCE_NGKEY, SOURCE_NXKEY, SOURCE_NSEGKEY, SOURCE_NNAME,
				SOURCE_NTYPE, SOURCE_NTEXT, SOURCE_NCODES));
			
			// Store the main content
			pstm.setInt(1, 0);
			pstm.setInt(2, gKey);
			pstm.setString(3, tuId);
			pstm.setInt(4, 0); // SegKey is 0 for the main entry
			pstm.setString(5, tuName);
			pstm.setString(6, tuType);
			pstm.setString(7, TextContainer.contentToString(tc));
			pstm.setString(8, ""); // Not used any more
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

	public TextContainer findEntry (String name) {
		PreparedStatement pstm = null;
		try {
			pstm = conn.prepareStatement(String.format("SELECT %s,%s FROM %s WHERE %s=? ORDER BY %s",
				SOURCE_NTEXT, SOURCE_NCODES, TBLNAME_SOURCE, SOURCE_NNAME, SOURCE_NSEGKEY));
			pstm.setString(1, name);
			ResultSet result = pstm.executeQuery();
			if ( !result.first() ) return null;
			
			// Build the segment
			TextContainer tc = TextContainer.stringToContent(result.getString(1));
			return tc;
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
	
}
