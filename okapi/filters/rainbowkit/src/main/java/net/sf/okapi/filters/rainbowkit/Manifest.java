/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.rainbowkit;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.okapi.common.Base64;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.exceptions.OkapiException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implements the writing and reading of a manifest document, commonly used
 * in different types of translation packages.
 */
public class Manifest implements IAnnotation {

	public static final String EXTRACTIONTYPE_NONE = "none"; // Not an extracted file (used for reference files)
	public static final String EXTRACTIONTYPE_XLIFF = "xliff";
	public static final String EXTRACTIONTYPE_XLIFF2 = "xliff2";
	public static final String EXTRACTIONTYPE_PO = "po";
	public static final String EXTRACTIONTYPE_RTF = "rtf";
	public static final String EXTRACTIONTYPE_VERSIFIED_RTF = "versified+rtf";
	public static final String EXTRACTIONTYPE_VERSIFIED = "versified";
	public static final String EXTRACTIONTYPE_XLIFFRTF = "xliff+rtf";
	public static final String EXTRACTIONTYPE_OMEGAT = "omegat";
	public static final String EXTRACTIONTYPE_TRANSIFEX = "transifex";
	public static final String EXTRACTIONTYPE_ONTRAM = "ontram";
	public static final String EXTRACTIONTYPE_TABLE = "table";

	public static final String VERSION = "2";
	public static final String MANIFEST_FILENAME = "manifest";
	public static final String MANIFEST_EXTENSION = ".rkm";

	private static final String TIP_VERSION = "1.3";
	
	private LinkedHashMap<Integer, MergingInfo> docs;
	private String tempPackageRoot;
	private String packageRoot;
	private String packageId;
	private String projectId;
	private LocaleId sourceLoc;
	private LocaleId targetLoc;
	private String inputRoot;
	private String originalSubDir;
	private String sourceSubDir;
	private String targetSubDir;
	private String tmSubDir;
	private String mergeSubDir;
	private String skelSubDir;
	private String originalDir;
	private String tempOriginalDir;
	private String tempSkelDir;
//	private String sourceDir;
	private String tempSourceDir;
//	private String targetDir;
	private String tempTargetDir;
	private String mergeDir;
	private String skelDir;
//	private String tmDir;
	private String tempTmDir;
	private String creatorParams;
	private boolean useApprovedOnly;
	private boolean updateApprovedFlag;
	private String date;
	private boolean generateTIPManifest = false;
	private String libVersion = "";

	public Manifest () {
		docs = new LinkedHashMap<Integer, MergingInfo>();
		tempPackageRoot = "";
		packageRoot = "";
		originalSubDir = "";
		sourceSubDir = "";
		targetSubDir = "";
		mergeSubDir = "";
		tmSubDir = "";
		skelSubDir = "";
		updateFullDirectories();
		useApprovedOnly = false;
		updateApprovedFlag = false;
	}

	public Map<Integer, MergingInfo> getItems () {
		return docs;
	}

	public MergingInfo getItem (int docID) {
		return docs.get(docID);
	}
	
	/**
	 * Gets the library version used to create the manifest.
	 * @return the version of the library used to create this manifest. Can be empty if the manifest
	 * does not have such information, or if the code is not run from the JAR files.
	 */
	public String getLibVersion () {
		return libVersion;
	}
	
	public String getPackageId () {
		return packageId;
	}
	
	public String getProjectId () {
		return projectId;
	}
	
	public LocaleId getSourceLocale () {
		return sourceLoc;
	}
	
	public LocaleId getTargetLocale () {
		return targetLoc;
	}
	
	public String getCreatorParameters () {
		return creatorParams;
	}
	
	public boolean getUseApprovedOnly () {
		return useApprovedOnly;
	}
	
	public void setUseApprovedOnly (boolean value) {
		useApprovedOnly = value;
	}
	
	public boolean getUpdateApprovedFlag () {
		return updateApprovedFlag;
	}
	
	public void setUpdateApprovedFlag (boolean value) {
		updateApprovedFlag = value;
	}
	
	public boolean getGenerateTIPManifest () {
		return generateTIPManifest;
	}
	
	public void setGenerateTIPManifest (boolean generateTIPManifest) {
		this.generateTIPManifest = generateTIPManifest;
	}
	
	/**
	 * Gets the input root (always with the terminal separator).
	 * @return the input root.
	 */
	public String getInputRoot () {
		return inputRoot;
	}
	
	/**
	 * Gets the package root (always with the terminal separator).
	 * @return the package root.
	 */
	public String getPackageRoot () {
		return packageRoot;
	}
	
	/**
	 * Gets the temporary package root (always with the terminal separator).
	 * @return the temporary package root.
	 */
	public String getTempPackageRoot () {
		return tempPackageRoot;
	}
	
	/**
	 * Sets the sub-directories used by the given package.
	 * All defaults to "" (same directory as the directory of the package itself.
	 * @param originalSubDir the sub-directory for the original document.
	 * @param sourceSubDir the sub-directory for the source documents.
	 * @param targetSubDir the sub-directory for the target documents.
	 * @param mergeSubDir the sub-directory for the merged documents.
	 * @param tmSubDir the sub-directory for TM-related data.
	 * @param skelSubDir the sub-directory for the skeleton files (if needed)
	 * @param overwrite true to overwrite existing settings (use null to not overwrite a given sub-directory).
	 * false to use the specified value (if the current is empty).
	 */
	public void setSubDirectories (String originalSubDir,
		String sourceSubDir,
		String targetSubDir,
		String mergeSubDir,
		String tmSubDir,
		String skelSubDir,
		boolean overwrite)
	{
		if (( originalSubDir != null ) && ( overwrite || Util.isEmpty(this.originalSubDir) )) {
			this.originalSubDir = originalSubDir;
		}
		if (( sourceSubDir != null ) && ( overwrite || Util.isEmpty(this.sourceSubDir) )) {
			this.sourceSubDir = sourceSubDir;
		}
		if (( targetSubDir != null ) && ( overwrite || Util.isEmpty(this.targetSubDir) )) {
			this.targetSubDir = targetSubDir;
		}
		if (( mergeSubDir != null ) && ( overwrite || Util.isEmpty(this.mergeSubDir) )) {
			this.mergeSubDir = mergeSubDir;
		}
		if (( tmSubDir != null ) && ( overwrite || Util.isEmpty(this.tmSubDir) )) {
			this.tmSubDir = tmSubDir;
		}
		if (( skelSubDir != null ) && ( overwrite || Util.isEmpty(this.skelSubDir) )) {
			this.skelSubDir = skelSubDir;
		}
		updateFullDirectories();
	}
	
	/**
	 * Gets the temporary directory where to store the original files (always with a terminal separator).
	 * @return the temporary directory where to store the original files.
	 */
	public String getTempOriginalDirectory () {
		return tempOriginalDir;
	}
	
	/**
	 * Gets the temporary directory where to store the skeleton files (always with a terminal separator).
	 * @return the temporary directory where to store the skeleton files.
	 */
	public String getTempSkelDirectory () {
		return tempSkelDir;
	}
	
	/**
	 * Gets the full temporary directory where to store the prepared source files (always with a terminal separator). 
	 * @return the temporary directory where to store the prepared source files.
	 */
	public String getTempSourceDirectory () {
		return tempSourceDir;
	}
	
	/**
	 * Get the temporary directory where to store the prepared target files (always with a terminal separator).
	 * @return the temporary directory where to store the prepared target files.
	 */
	public String getTempTargetDirectory () {
		return tempTargetDir;
	}
	
	/**
	 * Gets the directory where to store the original files (always with a terminal separator).
	 * @return the directory where to store the original files.
	 */
	public String getOriginalDirectory () {
		return originalDir;
	}
	
	/**
	 * Gets the directory where to store the skeleton files if there are any (always with a terminal separator).
	 * @return the directory where to store the skeleton files.
	 */
	public String getSkeletonDirectory () {
		return skelDir;
	}
	
	/**
	 * Get the directory where to output the result of the merging process (always with a terminal separator).
	 * @return the directory where to store the prepared target files.
	 */
	public String getMergeDirectory () {
		return mergeDir;
	}
	
	/**
	 * Gets the temporary directory where to output TM-related information (always with a terminal separator).
	 * @return the temporary directory where to store TM-related information.
	 */
	public String getTempTmDirectory () {
		return tempTmDir;
	}
	
	/**
	 * Gets the date when the manifest was created (saved the first time).
	 * @return A string representation of the creation date,
	 * or null if the manifest file has not been saved yet.
	 */
	public String getDate () {
		return date;
	}
	
	public void setInformation (String packageRoot,
		LocaleId srcLoc,
		LocaleId trgLoc,
		String inputRoot,
		String packageId,
		String projectId,
		String creatorParams,
		String tempPackageRoot)
	{
		this.sourceLoc = srcLoc;
		this.targetLoc = trgLoc;
		this.inputRoot = Util.ensureSeparator(inputRoot, false);
		this.packageRoot = Util.ensureSeparator(packageRoot, false);
		this.tempPackageRoot = Util.ensureSeparator(tempPackageRoot, false);
		this.packageId = packageId;
		this.projectId = projectId;
		updateFullDirectories();
		this.creatorParams = creatorParams;
	}
	
	/**
	 * Adds a document to the manifest.
	 * @param docId Key of the document. Must be unique within the manifest.
	 */
	public void addDocument (int docId,
		String extractionType,
		String relativeInputPath,
		String filterId,
		String filterParameters,
		String inputEncoding,
		String relativeTargetPath,
		String targetEncoding)
	{
		docs.put(docId, new MergingInfo(docId, extractionType, relativeInputPath, filterId,
			filterParameters, inputEncoding, relativeTargetPath, targetEncoding));
	}
	
	/**
	 * Gets the full path of the manifest file.
	 * @return the full path of the manifest file.
	 */
	public String getPath () {
		return packageRoot+MANIFEST_FILENAME+MANIFEST_EXTENSION;
	}

	/**
	 * Saves the manifest file. This method assumes the root is set.
	 * @param dir directory where to save the file, use null for the default.
	 */
	public void save (String dir) {
		if ( generateTIPManifest ) {
			// TIPP manifest to save from the package writer
			return;
		}

		XMLWriter writer = null;
		try {
			String outputPath = getPath();
			if ( dir != null ) {
				outputPath = Util.ensureSeparator(dir, false) + MANIFEST_FILENAME + MANIFEST_EXTENSION;
			}
			writer = new XMLWriter(outputPath);

			writer.writeStartDocument();
			writer.writeComment("=================================================================", true);
			writer.writeComment("PLEASE, DO NOT RENAME, MOVE, MODIFY OR ALTER IN ANY WAY THIS FILE", true);
			writer.writeComment("=================================================================", true);
			writer.writeStartElement("manifest");
			writer.writeAttributeString("version", VERSION);
			writer.writeAttributeString("libVersion", getClass().getPackage().getImplementationVersion());
			writer.writeAttributeString("projectId", projectId);
			writer.writeAttributeString("packageId", packageId);
			writer.writeAttributeString("source", sourceLoc.toString());
			writer.writeAttributeString("target", targetLoc.toString());
			writer.writeAttributeString("originalSubDir", originalSubDir.replace('\\', '/'));
			if ( skelSubDir != null ) {
				writer.writeAttributeString("skeletonSubDir", skelSubDir.replace('\\', '/'));
			}
			writer.writeAttributeString("sourceSubDir", sourceSubDir.replace('\\', '/'));
			writer.writeAttributeString("targetSubDir", targetSubDir.replace('\\', '/'));
			writer.writeAttributeString("mergeSubDir", mergeSubDir.replace('\\', '/'));
			writer.writeAttributeString("tmSubDir", tmSubDir.replace('\\', '/'));
			SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			date = DF.format(new java.util.Date());
			writer.writeAttributeString("date", date);
			writer.writeAttributeString("useApprovedOnly", (useApprovedOnly ? "1" : "0"));
			writer.writeAttributeString("updateApprovedFlag", (updateApprovedFlag ? "1" : "0"));
			writer.writeLineBreak();

			// creatorParams
			writer.writeStartElement("creatorParameters");
			writer.writeString(Base64.encodeString(creatorParams.toString()));
			writer.writeEndElementLineBreak();
			
			// Info for the documents
			for ( MergingInfo item : docs.values() ) {
				writer.writeRawXML(item.writeToXML("doc", true));
				writer.writeLineBreak();
			}

			writer.writeEndElement(); // manifest
			writer.writeEndDocument();
		}
		finally {
			if ( writer != null ) writer.close();
		}
	}

	public void saveTIPManifest (String dir,
		List<String> tms)
	{
		XMLWriter writer = null;
		try {
			String tippManifestPath = getPath();
			if ( dir != null ) {
				tippManifestPath = Util.ensureSeparator(dir, false) + MANIFEST_FILENAME + ".xml";
			}
			else {
				tippManifestPath = tippManifestPath.replace(MANIFEST_EXTENSION, ".xml");
			}
			writer = new XMLWriter(tippManifestPath);

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			String outputDate = df.format(new Date());
			
			writer.writeStartDocument();
			writer.writeComment("EXPERIMENTAL OUTPUT ONLY!", true);
			writer.writeStartElement("TIPManifest");
			writer.writeAttributeString("version", TIP_VERSION);
			writer.writeStartElement("GlobalDescriptor");

			writer.writeElementString("UniquePackageID", packageId);
			
			writer.writeStartElement("PackageCreator");
			
			writer.writeElementString("CreatorName", "unspecified");
			writer.writeLineBreak();
			writer.writeElementString("CreatorID", "urn:unspecified");
			writer.writeLineBreak();
			writer.writeElementString("CreatorUpdate", outputDate);
			writer.writeLineBreak();

			writer.writeStartElement("ContributorTool");
			writer.writeElementString("ToolName", getClass().getName());
			writer.writeLineBreak();
			writer.writeElementString("ToolID", "urn:"+getClass().getName().replace('.', ':'));
			writer.writeLineBreak();
			writer.writeElementString("ToolVersion", "1.0");
			writer.writeLineBreak();
			writer.writeEndElementLineBreak(); // ContributorTool

			writer.writeElementString("Communication", "FTP"); // Just to have something valid
			writer.writeLineBreak();
			
			writer.writeEndElementLineBreak(); // PackageCreator
			
			writer.writeStartElement("OrderAction");
			writer.writeStartElement("OrderTask");
			writer.writeElementString("TaskType", "Translate");
			writer.writeLineBreak();
			writer.writeElementString("SourceLanguage", getSourceLocale().toString());
			writer.writeLineBreak();
			writer.writeElementString("TargetLanguage", getTargetLocale().toString());
			writer.writeLineBreak();
			writer.writeEndElementLineBreak(); // OrderTask
			writer.writeEndElementLineBreak(); // OrderAction
			
			writer.writeEndElementLineBreak(); // GlobalDescriptor
			
			
			writer.writeStartElement("PackageObjects");

			//--- files in input
			writer.writeStartElement("PackageObjectSection");
			writer.writeAttributeString("sectionname", "input");
			int seq = 1;
			for ( MergingInfo item : docs.values() ) {
				writer.writeStartElement("ObjectFile");
				writer.writeAttributeString("localizable", "no"); // Because we send the bilingual folder
				writer.writeAttributeString("sequence", String.valueOf(seq));
				writer.writeElementString("Type", item.getFilterId());
				writer.writeElementString("LocationPath", item.getRelativeInputPath().substring(1));
				//Optional: writer.writeElementString("Description", "todo");
				writer.writeEndElementLineBreak(); // ObjectFile
				seq++;
			}
			writer.writeEndElementLineBreak(); // PackageObjectSection

			//--- files in bilingual
			writer.writeStartElement("PackageObjectSection");
			writer.writeAttributeString("sectionname", "bilingual");
			seq = 1;
			for ( MergingInfo item : docs.values() ) {
				if ( item.getExtractionType().equals(Manifest.EXTRACTIONTYPE_NONE) ) {
					continue; // Skip non-filtered files
				}
				writer.writeStartElement("ObjectFile");
				writer.writeAttributeString("localizable", "yes");
				writer.writeAttributeString("sequence", String.valueOf(seq));
				writer.writeElementString("Type", "XLIFF 2.0");
				writer.writeElementString("LocationPath", item.getRelativeInputPath().substring(1)+".xlf");
				//Optional: writer.writeElementString("Description", "todo");
				writer.writeEndElementLineBreak(); // ObjectFile
				seq++;
			}
			writer.writeEndElementLineBreak(); // PackageObjectSection
			
			//--- files in tm
			if ( !tms.isEmpty() ) {
				writer.writeStartElement("PackageObjectSection");
				writer.writeAttributeString("sectionname", "tm");
				seq = 1;
				for ( String path : tms ) {
					writer.writeStartElement("ObjectFile");
					writer.writeAttributeString("localizable", "no");
					writer.writeAttributeString("sequence", String.valueOf(seq));
					writer.writeElementString("Type", "TMX");
					writer.writeElementString("LocationPath", path);
					writer.writeEndElementLineBreak(); // ObjectFile
					seq++;
				}
				writer.writeEndElementLineBreak(); // PackageObjectSection
			}

			writer.writeEndElementLineBreak(); // PackageObjects
			
			
			writer.writeEndElement(); // TIPManifest
			writer.writeEndDocument();
		}
		finally {
			if ( writer != null ) writer.close();
		}
	}

	public void load (File inputFile) {
		try {
			DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
		    // Not needed in this case: docFac.setNamespaceAware(true);
			Document doc = docFac.newDocumentBuilder().parse(inputFile);
		    
		    NodeList NL = doc.getElementsByTagName("manifest");
		    if ( NL == null ) throw new OkapiException("Invalid manifest file.");
		    Element elem = (Element)NL.item(0);
		    if ( elem == null ) throw new OkapiException("Invalid manifest file.");
		    
		    String tmp = elem.getAttribute("version");
		    if ( Util.isEmpty(tmp) ) throw new OkapiException("Missing version attribute.");

		    libVersion = elem.getAttribute("libVersion");

		    tmp = elem.getAttribute("projectId");
		    if ( Util.isEmpty(tmp) ) throw new OkapiException("Missing projectId attribute.");
		    projectId = tmp;
		    
		    tmp = elem.getAttribute("packageId");
		    if ( Util.isEmpty(tmp) ) throw new OkapiException("Missing packageId attribute.");
		    packageId = tmp;
		    
		    tmp = elem.getAttribute("source");
		    if ( Util.isEmpty(tmp) ) throw new OkapiException("Missing source attribute.");
		    sourceLoc = LocaleId.fromString(tmp);
		    
		    tmp = elem.getAttribute("target");
		    if ( Util.isEmpty(tmp) ) throw new OkapiException("Missing target attribute.");
		    targetLoc = LocaleId.fromString(tmp);

		    date = elem.getAttribute("date");
		    if ( Util.isEmpty(tmp) ) date = "Unknown";

		    tmp = elem.getAttribute("originalSubDir");
		    if ( Util.isEmpty(tmp) ) throw new OkapiException("Missing originalSubDir attribute.");
		    originalSubDir = tmp.replace('/', File.separatorChar);

		    tmp = elem.getAttribute("skeletonSubDir");
		    if ( !Util.isEmpty(tmp) ) skelSubDir = tmp.replace('/', File.separatorChar);
		    else skelSubDir = null;

		    tmp = elem.getAttribute("sourceSubDir");
		    if ( Util.isEmpty(tmp) ) throw new OkapiException("Missing sourceSubDir attribute.");
		    sourceSubDir = tmp.replace('/', File.separatorChar);

		    tmp = elem.getAttribute("targetSubDir");
		    if ( Util.isEmpty(tmp) ) throw new OkapiException("Missing targetSubDir attribute.");
		    targetSubDir = tmp.replace('/', File.separatorChar);

		    tmp = elem.getAttribute("mergeSubDir");
		    if ( Util.isEmpty(tmp) ) throw new OkapiException("Missing mergeSubDir attribute.");
		    mergeSubDir = tmp.replace('/', File.separatorChar);

		    tmp = elem.getAttribute("tmSubDir");
		    if ( Util.isEmpty(tmp) ) tmSubDir = "";
		    else tmSubDir = tmp.replace('/', File.separatorChar);
		    
		    // creatorParameters
		    NL = elem.getElementsByTagName("creatorParameters");
		    if ( NL.getLength() > 0 ) {
		    	creatorParams = Base64.decodeString(Util.getTextContent(NL.item(0)));
		    }
		    else {
		    	creatorParams = "";
		    }
		    
		    tmp = elem.getAttribute("useApprovedOnly");
		    if ( Util.isEmpty(tmp) ) useApprovedOnly = false;
		    else useApprovedOnly = !tmp.equals("0");
		    
		    tmp = elem.getAttribute("updateApprovedFlag");
		    if ( Util.isEmpty(tmp) ) this.updateApprovedFlag = true;
		    else updateApprovedFlag = !tmp.equals("0");
		    
		    // Documents
		    docs.clear();
		    NL = elem.getElementsByTagName("doc");
		    for ( int i=0; i<NL.getLength(); i++ ) {
		    	elem = (Element)NL.item(i);
		    	MergingInfo item = MergingInfo.readFromXML(elem);
		    	docs.put(item.getDocId(), item);
		    }
		    packageRoot = Util.ensureSeparator(Util.getDirectoryName(inputFile.getAbsolutePath()), false);
		    tempPackageRoot = packageRoot;
			updateFullDirectories();
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

	private void updateFullDirectories () {
		originalDir = Util.ensureSeparator(packageRoot + originalSubDir, false);
//		sourceDir = Util.ensureSeparator(packageRoot + sourceSubDir, false);
//		targetDir = Util.ensureSeparator(packageRoot + targetSubDir, false);
		mergeDir = Util.ensureSeparator(packageRoot + mergeSubDir, false);
//		tmDir = Util.ensureSeparator(packageRoot + tmSubDir, false);
		skelDir = Util.ensureSeparator(packageRoot + skelSubDir, false);

		tempOriginalDir = Util.ensureSeparator(tempPackageRoot + originalSubDir, false);
		tempSkelDir = Util.ensureSeparator(tempPackageRoot + skelSubDir, false);
		tempSourceDir = Util.ensureSeparator(tempPackageRoot + sourceSubDir, false);
		tempTargetDir = Util.ensureSeparator(tempPackageRoot + targetSubDir, false);
//		tempMergeDir = Util.ensureSeparator(tempPackageRoot + mergeSubDir, false);
		tempTmDir = Util.ensureSeparator(tempPackageRoot + tmSubDir, false);
	}

	@Override
	public String toString () {
		return projectId + "_" + packageId;
	}

}
