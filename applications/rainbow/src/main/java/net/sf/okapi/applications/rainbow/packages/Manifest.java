/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.packages;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.exceptions.OkapiException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implements the writing and reading of a manifest document, commonly used
 * in different types of translation packages.
 */
public class Manifest {

	private LinkedHashMap<Integer, ManifestItem> docs;
	private String rootFolder;
	private String packageID;
	private String packageType;
	private String projectID;
	private LocaleId sourceLoc;
	private LocaleId targetLoc;
	private String originalDir;
	private String sourceDir;
	private String targetDir;
	private String doneDir;
	private String readerClass;
	private String date;
	private boolean useApprovedOnly;
	private boolean updateApprovedFlag;

	public Manifest () {
		docs = new LinkedHashMap<Integer, ManifestItem>();
		sourceDir = "";
		targetDir = "";
		originalDir = "";
		doneDir = "";
		useApprovedOnly = false;
		updateApprovedFlag = true;
	}

	public void setReaderClass (String readerClass) {
		this.readerClass = readerClass;
	}
	
	public String getReaderClass () {
		return readerClass;
	}
	
	public Map<Integer, ManifestItem> getItems () {
		return docs;
	}

	public ManifestItem getItem (int docID) {
		return docs.get(docID);
	}
	
	public String getPackageID () {
		return packageID;
	}
	
	public void setPackageID (String value) {
		packageID = value;
	}

	public String getPackageType () {
		return packageType;
	}
	
	public void setPackageType (String value) {
		packageType = value;
	}

	public String getProjectID () {
		return projectID;
	}
	
	public void setProjectID (String value) {
		projectID = value;
	}

	public LocaleId getSourceLanguage () {
		return sourceLoc;
	}
	
	public void setSourceLanguage (LocaleId value) {
		if ( value == null ) throw new NullPointerException();
		sourceLoc = value;
	}

	public LocaleId getTargetLanguage () {
		return targetLoc;
	}
	
	public void setTargetLanguage (LocaleId value) {
		if ( value == null ) throw new NullPointerException();
		targetLoc = value;
	}

	public String getRoot () {
		return rootFolder;
	}
	
	public void setRoot (String value) {
		if ( value == null ) throw new NullPointerException();
		rootFolder = value;
	}

	public String getSourceLocation () {
		return sourceDir;
	}
	
	public void setSourceLocation (String value) {
		if ( value == null ) sourceDir = "";
		else sourceDir = value;
	}

	public String getTargetLocation () {
		return targetDir;
	}
	
	public void setTargetLocation (String value) {
		if ( value == null ) targetDir = "";
		else targetDir = value;
	}

	public String getOriginalLocation () {
		return originalDir;
	}
	
	public void setOriginalLocation (String value) {
		if ( value == null ) originalDir = "";
		else originalDir = value;
	}

	public String getDoneLocation () {
		return doneDir;
	}
	
	public void setDoneLocation (String value) {
		if ( value == null ) doneDir = "";
		else doneDir = value;
	}

	public void setDate (String value) {
		date = value;
	}
	
	public String getDate () {
		return date;
	}
	
	public boolean useApprovedOnly () {
		return useApprovedOnly;
	}
	
	public void setUseApprovedOnly (boolean value) {
		useApprovedOnly = value;
	}
	
	public boolean updateApprovedFlag () {
		return updateApprovedFlag;
	}
	
	public void setUpdateApprovedFlag (boolean value) {
		updateApprovedFlag = value;
	}
	
	/**
	 * Adds a document to the manifest.
	 * @param docID Key of the document. Must be unique within the manifest.
	 * @param relativeInputPath Relative path of the input document.
	 * @param relativeOutputPath Relative path of the output document.
	 */
	public void addDocument (int docID,
		String relativeWorkPath,
		String relativeInputPath,
		String relativeOutputPath,
		String inputEncoding,
		String outputEncoding,
		String filterID,
		String postProcessingType)
	{
		docs.put(docID, new ManifestItem(relativeWorkPath,
			relativeInputPath, relativeOutputPath,
			inputEncoding, outputEncoding, filterID, postProcessingType, true));
	}

	public String getFileToMergePath (int docID) {
		return rootFolder + File.separator
			+ (( targetDir.length() == 0 ) ? "" : (targetDir + File.separator))
			+ docs.get(docID).getRelativeWorkPath();
	}
	
	public String getMergeInputRoot () {
		return rootFolder + File.separator
			+ (( targetDir.length() == 0 ) ? "" : (targetDir + File.separator));
	}

	public String getFileToGeneratePath (int docID) {
		return rootFolder + File.separator
			+ (( doneDir.length() == 0 ) ? "" : (doneDir + File.separator))
			+ docs.get(docID).getRelativeOutputPath();
	}

	/**
	 * Saves the manifest file. This method assumes the root is set.
	 */
	public void Save () {
		XMLWriter writer = null;
		try {
			writer = new XMLWriter(rootFolder + File.separator + "manifest.xml");

			writer.writeStartDocument();
			writer.writeComment("=================================================================", true);
			writer.writeComment("PLEASE, DO NOT RENAME, MOVE, MODIFY OR ALTER IN ANY WAY THIS FILE", true);
			writer.writeComment("=================================================================", true);
			writer.writeStartElement("rainbowManifest");
			writer.writeAttributeString("xmlns:its", "http://www.w3.org/2005/11/its");
			writer.writeAttributeString("its:version", "1.0");
			writer.writeAttributeString("its:translate", "no");
			writer.writeAttributeString("projectID", projectID);
			writer.writeAttributeString("packageID", packageID);
			writer.writeAttributeString("sourceLang", sourceLoc.toBCP47());
			writer.writeAttributeString("targetLang", targetLoc.toBCP47());
			writer.writeAttributeString("packageType", packageType);
			writer.writeAttributeString("readerClass", readerClass);
			writer.writeAttributeString("originalDir", originalDir.replace('\\', '/'));
			writer.writeAttributeString("sourceDir", sourceDir.replace('\\', '/'));
			writer.writeAttributeString("targetDir", targetDir.replace('\\', '/'));
			writer.writeAttributeString("doneDir", doneDir.replace('\\', '/'));
			SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			writer.writeAttributeString("date", DF.format(new java.util.Date()));
			writer.writeAttributeString("useApprovedOnly", (useApprovedOnly ? "yes" : "no"));
			writer.writeAttributeString("updateApprovedFlag", (updateApprovedFlag ? "yes" : "no"));

			Iterator<Integer> iter = docs.keySet().iterator();
			ManifestItem item;
			while ( iter.hasNext() ) {
				int id = iter.next();
				item = docs.get(id);
				writer.writeStartElement("doc");
				writer.writeAttributeString("id", String.valueOf(id));
				writer.writeAttributeString("filter", item.getFilterID());
				writer.writeAttributeString("work", item.getRelativeWorkPath().replace('\\', '/'));
				writer.writeAttributeString("input", item.getRelativeInputPath().replace('\\', '/'));
				writer.writeAttributeString("output", item.getRelativeOutputPath().replace('\\', '/'));
				writer.writeAttributeString("inputEncoding", item.getInputEncoding());
				writer.writeAttributeString("outputEncoding", item.getOutputEncoding());
				writer.writeAttributeString("postProcessing", item.getPostProcessingType());
				writer.writeEndElementLineBreak();
			}

			writer.writeEndElement(); // rainbowManifest
			writer.writeEndDocument();
		}
		finally {
			if ( writer != null ) writer.close();
		}
	}

	public void load (String path) {
		try {
			DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
		    // Not needed in this case: DFac.setNamespaceAware(true);
		    Document doc = docFac.newDocumentBuilder().parse("file:///"+path);
		    
		    NodeList NL = doc.getElementsByTagName("rainbowManifest");
		    if ( NL == null ) throw new OkapiException("Invalid manifest file.");
		    Element elem = (Element)NL.item(0);
		    if ( elem == null ) throw new OkapiException("Invalid manifest file.");
		    
		    String tmp = elem.getAttribute("projectID");
		    if (( tmp == null ) || ( tmp.length() == 0 ))
		    	throw new OkapiException("Missing projectID attribute.");
		    else setProjectID(tmp);
		    
		    tmp = elem.getAttribute("packageID");
		    if (( tmp == null ) || ( tmp.length() == 0 ))
		    	throw new OkapiException("Missing packageID attribute.");
		    else setPackageID(tmp);
		    
		    tmp = elem.getAttribute("packageType");
		    if (( tmp == null ) || ( tmp.length() == 0 ))
		    	throw new OkapiException("Missing packageType attribute.");
		    else setPackageType(tmp);
		    
		    tmp = elem.getAttribute("readerClass");
		    if (( tmp == null ) || ( tmp.length() == 0 ))
		    	throw new OkapiException("Missing readerClass attribute.");
		    else setReaderClass(tmp);
		    
		    tmp = elem.getAttribute("sourceLang");
		    if (( tmp == null ) || ( tmp.length() == 0 ))
		    	throw new OkapiException("Missing sourceLang attribute.");
		    else setSourceLanguage(LocaleId.fromString(tmp));
		    
		    tmp = elem.getAttribute("targetLang");
		    if (( tmp == null ) || ( tmp.length() == 0 ))
		    	throw new OkapiException("Missing targetLang attribute.");
		    else setTargetLanguage(LocaleId.fromString(tmp));

		    tmp = elem.getAttribute("originalDir");
		    setOriginalLocation(tmp.replace('/', File.separatorChar));

		    tmp = elem.getAttribute("sourceDir");
		    setSourceLocation(tmp.replace('/', File.separatorChar));

		    tmp = elem.getAttribute("targetDir");
		    setTargetLocation(tmp.replace('/', File.separatorChar));

		    tmp = elem.getAttribute("doneDir");
		    setDoneLocation(tmp.replace('/', File.separatorChar));
		    
		    tmp = elem.getAttribute("date");
		    setDate(tmp);
		    
		    tmp = elem.getAttribute("useApprovedOnly");
		    if ( tmp != null ) {
		    	setUseApprovedOnly(tmp.equals("yes"));
		    }

		    String inPath, outPath, inEnc, outEnc, filterID, postProcessingType;
		    docs.clear();
		    NL = elem.getElementsByTagName("doc");
		    for ( int i=0; i<NL.getLength(); i++ ) {
		    	elem = (Element)NL.item(i);
		    	tmp = elem.getAttribute("id");
			    if (( tmp == null ) || ( tmp.length() == 0 ))
			    	throw new OkapiException("Missing id attribute.");
			    int id = Integer.valueOf(tmp);
			    
		    	tmp = elem.getAttribute("work");
			    if (( tmp == null ) || ( tmp.length() == 0 ))
			    	throw new OkapiException("Missing work attribute.");
			    
		    	inPath = elem.getAttribute("input");
			    if (( inPath == null ) || ( inPath.length() == 0 ))
			    	throw new OkapiException("Missing input attribute.");
			    
		    	outPath = elem.getAttribute("output");
			    if (( outPath == null ) || ( outPath.length() == 0 ))
			    	throw new OkapiException("Missing output attribute.");
			    
		    	inEnc = elem.getAttribute("inputEncoding");
			    if (( inEnc == null ) || ( inEnc.length() == 0 ))
			    	throw new OkapiException("Missing inputEncoding attribute.");
			    
		    	outEnc = elem.getAttribute("outputEncoding");
			    if (( outEnc == null ) || ( outEnc.length() == 0 ))
			    	throw new OkapiException("Missing outputEncoding attribute.");
			    
			    filterID = elem.getAttribute("filter");
			    if (( filterID == null ) || ( filterID.length() == 0 ))
			    	throw new OkapiException("Missing filter attribute.");
			    
			    postProcessingType = elem.getAttribute("postProcessing");
			    if (( filterID == null ) || ( filterID.length() == 0 )) {
			    	postProcessingType = "default";	
			    }
			    
		    	docs.put(id, new ManifestItem(tmp.replace('/', File.separatorChar),
		    		inPath.replace('/', File.separatorChar),
		    		outPath.replace('/', File.separatorChar),
		    		inEnc, outEnc, filterID, postProcessingType, true));
		    }

		    rootFolder = Util.getDirectoryName(path);
		}
		catch ( SAXException e ) {
			throw new OkapiException(e);
		}
		catch ( ParserConfigurationException e ) {
			throw new OkapiException(e);
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
	}

	/**
	 * Checks the content of the manifest against the package where
	 * it has been found.
	 * @return The number of error found.
	 */
	public int checkPackageContent () {
		int nErrors = 0;
		Iterator<Integer> iter = docs.keySet().iterator();
		int docId;
		ManifestItem mi;
		while ( iter.hasNext() ) {
			docId = iter.next();
			mi = docs.get(docId);
			File F = new File(getFileToMergePath(docId));
			if ( !F.exists() ) {
				nErrors++;
				mi.setExists(false);
			}
		}
		return nErrors;
	}

}
