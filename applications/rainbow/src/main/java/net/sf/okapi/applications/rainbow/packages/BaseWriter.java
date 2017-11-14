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
============================================================================*/

package net.sf.okapi.applications.rainbow.packages;

import java.io.File;
import java.io.OutputStream;

import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

public abstract class BaseWriter implements IWriter {

	protected Manifest manifest;
	protected int docID;
	protected String inputRoot;
	protected String relativeWorkPath;
	protected String relativeSourcePath;
	protected String relativeTargetPath;
	protected String sourceEncoding;
	protected String targetEncoding;
	protected String filterID;
	protected TMXWriter tmxWriterApproved;
	protected String tmxPathApproved;
	protected TMXWriter tmxWriterUnApproved;
	protected String tmxPathUnApproved;
	protected TMXWriter tmxWriterAlternate;
	protected String tmxPathAlternate;
	protected TMXWriter tmxWriterLeverage;
	protected String tmxPathLeverage;
	protected LocaleId trgLoc;
	protected String encoding;
	protected String outputPath;
	protected boolean preSegmented;
	protected String creationTool;
	protected EncoderManager encoderManager;
	
	public BaseWriter () {
		manifest = new Manifest();
		manifest.setReaderClass(getReaderClass());
	}
	
	public void cancel () {
		//TODO: implement cancel()
	}
	
	public void setInformation (LocaleId sourceLocale,
		LocaleId targetLocale,
		String projectID,
		String outputFolder,
		String packageID,
		String sourceRoot,
		boolean preSegmented,
		String creationTool)
	{
		manifest.setSourceLanguage(sourceLocale);
		trgLoc = targetLocale;
		manifest.setTargetLanguage(trgLoc);
		manifest.setProjectID(projectID);
		manifest.setRoot(outputFolder);
		manifest.setPackageID(packageID);
		manifest.setPackageType(getPackageType());
		this.inputRoot = sourceRoot;
		this.preSegmented = preSegmented;
		this.creationTool = creationTool;
	}

	public void setEncoderManager (EncoderManager encoderManager) {
		this.encoderManager = encoderManager;
	}
	
	public void writeStartPackage () {
		// Create the root directory
		Util.createDirectories(manifest.getRoot());
		
		String tmp = manifest.getSourceLocation();
		if (( tmp != null ) && ( tmp.length() > 0 )) {
			Util.createDirectories(manifest.getRoot() + File.separator + tmp + File.separator);
		}
		
		tmp = manifest.getTargetLocation();
		if (( tmp != null ) && ( tmp.length() > 0 )) {
			Util.createDirectories(manifest.getRoot() + File.separator + tmp + File.separator);
		}

		tmp = manifest.getOriginalLocation();
		if (( tmp != null ) && ( tmp.length() > 0 )) {
			Util.createDirectories(manifest.getRoot() + File.separator + tmp + File.separator);
		}

		// No need to create the folder structure for the 'done' folder
		// It will be done when merging
		
		// Create the reference TMX (approved pre-translations found in the source files)
		if ( tmxPathApproved == null ) {
			tmxPathApproved = manifest.getRoot() + File.separator + "approved.tmx";
		}
		tmxWriterApproved = new TMXWriter(tmxPathApproved);
		tmxWriterApproved.writeStartDocument(manifest.getSourceLanguage(),
			manifest.getTargetLanguage(), creationTool, null, null, null, null);

		// Create the reference TMX (un-approved pre-translations found in the source files)
		if ( tmxPathUnApproved == null ) {
			tmxPathUnApproved = manifest.getRoot() + File.separator + "unapproved.tmx";
		}
		tmxWriterUnApproved = new TMXWriter(tmxPathUnApproved);
		tmxWriterUnApproved.writeStartDocument(manifest.getSourceLanguage(),
			manifest.getTargetLanguage(), creationTool, null, null, null, null);

		// Create the reference TMX (alternate found in the source files)
		if ( tmxPathAlternate == null ) {
			tmxPathAlternate = manifest.getRoot() + File.separator + "alternate.tmx";
		}
		tmxWriterAlternate = new TMXWriter(tmxPathAlternate);
		tmxWriterAlternate.writeStartDocument(manifest.getSourceLanguage(),
			manifest.getTargetLanguage(), creationTool, null, null, null, null);
		// Make sure we output only alt-translation annotation coming from the source itself
		tmxWriterAlternate.setAltTranslationOption(AltTranslation.ORIGIN_SOURCEDOC);

		// Create the reference TMX (pre-translation TM)
		if ( tmxPathLeverage == null ) {
			tmxPathLeverage = manifest.getRoot() + File.separator + "leverage.tmx";
		}
		tmxWriterLeverage = new TMXWriter(tmxPathLeverage);
		tmxWriterLeverage.writeStartDocument(manifest.getSourceLanguage(),
			manifest.getTargetLanguage(), creationTool, null, null, null, null);
	}

	public void writeEndPackage (boolean createZip) {
		// Save the manifest
		if ( manifest != null ) {
			manifest.Save();
		}

		tmxWriterApproved.writeEndDocument();
		tmxWriterApproved.close();
		if ( tmxWriterApproved.getItemCount() == 0 ) {
			File file = new File(tmxPathApproved);
			file.delete();
		}

		tmxWriterUnApproved.writeEndDocument();
		tmxWriterUnApproved.close();
		if ( tmxWriterUnApproved.getItemCount() == 0 ) {
			File file = new File(tmxPathUnApproved);
			file.delete();
		}

		tmxWriterAlternate.writeEndDocument();
		tmxWriterAlternate.close();
		if ( tmxWriterAlternate.getItemCount() == 0 ) {
			File file = new File(tmxPathAlternate);
			file.delete();
		}

		tmxWriterLeverage.writeEndDocument();
		tmxWriterLeverage.close();
		if ( tmxWriterLeverage.getItemCount() == 0 ) {
			File file = new File(tmxPathLeverage);
			file.delete();
		}

		// Zip the package if needed
		if ( createZip && ( manifest != null )) {
			//Compression.zipDirectory(manifest.getRoot(), manifest.getRoot() + ".zip");
			FileUtil.zipDirectory(manifest.getRoot()+"/", null);
		}
	}
	
	public void createOutput (int docID,
		String relativeSourcePath,
		String relativeTargetPath,
		String sourceEncoding,
		String targetEncoding,
		String filterID,
		IParameters filterParams)
	{
		if ( relativeSourcePath == null ) throw new NullPointerException();
		if ( relativeTargetPath == null ) throw new NullPointerException();
		if ( sourceEncoding == null ) throw new NullPointerException();
		if ( targetEncoding == null ) throw new NullPointerException();
		if ( filterID == null ) throw new NullPointerException();

		this.docID = docID;
		this.relativeSourcePath = relativeSourcePath;
		this.relativeTargetPath = relativeTargetPath;
		this.sourceEncoding = sourceEncoding;
		this.targetEncoding = targetEncoding;
		this.filterID = filterID;
		
		// If needed copy the original input to the package
		String subFolder = manifest.getOriginalLocation();
		if (( subFolder == null ) || ( subFolder.length() == 0 )) return;
				
		String inputPath = inputRoot + File.separator + relativeSourcePath;
		String docPrefix = String.format("%d.", docID);
			
		String destination = manifest.getRoot() + File.separator + subFolder
			+ File.separator + docPrefix + "ori"; // docPrefix has a dot
		StreamUtil.copy(inputPath, destination, false);
			
		String paramsCopy = manifest.getRoot() + File.separator + subFolder
			+ File.separator + docPrefix + "fprm";
		if ( filterParams != null ) {
			filterParams.save(paramsCopy);
		}
		
		// Set the options for the actual writer
		String outputPath = manifest.getRoot() + File.separator
			+ ((manifest.getSourceLocation().length() == 0 ) ? "" : (manifest.getSourceLocation() + File.separator)) 
			+ relativeWorkPath;
		setOptions(trgLoc, targetEncoding);
		setOutput(outputPath);
	}

	public void createCopies (int docID,
		String relativeSourcePath)
	{
		if ( relativeSourcePath == null ) throw new NullPointerException();

		String inputPath = inputRoot + File.separator + relativeSourcePath;
		String outputPath = manifest.getRoot() + File.separator
			+ ((manifest.getSourceLocation().length() == 0 ) ? "" : (manifest.getSourceLocation() + File.separator)) 
			+ relativeSourcePath;

		// If needed copy the original input to the package
		String subFolder = manifest.getOriginalLocation();
		if (( subFolder != null ) && ( subFolder.length() > 0 )) {
			String docPrefix = String.format("%d.", docID);
			String destination = manifest.getRoot() + File.separator + subFolder
				+ File.separator + docPrefix + "ori"; // docPrefix has a dot
			StreamUtil.copy(inputPath, destination, false);
		}
			
		// Copy to the work folder
		Util.createDirectories(outputPath);
		StreamUtil.copy(inputPath, outputPath, false);
	}

	public void setOptions (LocaleId locale,
		String defaultEncoding)
	{
		trgLoc = locale;
		encoding = defaultEncoding;
	}
	
	public void setOutput (String path) {
		outputPath = path;
	}

	public void setOutput (OutputStream output) {
		throw new UnsupportedOperationException();
	}

	public void writeTMXEntries (ITextUnit tu) {
		// Check if we have a target
		TextContainer tc = tu.getTarget(trgLoc);
		if (( tc == null ) || ( tc.isEmpty() )) {
			return; // No target
		}
		if ( tu.getSource().isEmpty() || ( tu.getSource().hasText(false) && !tc.hasText(false) )) {
			return; // Target has code and/or spaces only
		}

		// Process translation(s) in the container itself
		boolean done = false;
		if ( tu.hasTargetProperty(trgLoc, Property.APPROVED) ) {
			if ( tu.getTargetProperty(trgLoc, Property.APPROVED).getValue().equals("yes") ) {
				// Write existing translation that was approved
				tmxWriterApproved.writeItem(tu, null);
				done = true;
			}
		}
		if ( !done ) {
			// Write existing translation not yet approved
			tmxWriterUnApproved.writeItem(tu, null);
		}

		// Process translations in the AltTranslationsAnnotation annotation
		// alternates from the input file or leveraged entries
		TextContainer altCont = tu.getTarget(trgLoc);

		// From the segments
		ISegments srcSegs = tu.getSource().getSegments();
		for ( Segment seg : altCont.getSegments() ) {
			Segment srcSeg = srcSegs.get(seg.id);
			if ( srcSeg == null ) continue;
			writeAltTranslations(seg.getAnnotation(AltTranslationsAnnotation.class), srcSeg.text);
		}
		
		// From the target container
		TextFragment srcOriginal;
		if ( tu.getSource().contentIsOneSegment() ) {
			srcOriginal = tu.getSource().getFirstContent();
		}
		else {
			srcOriginal = tu.getSource().getUnSegmentedContentCopy();
		}
		writeAltTranslations(altCont.getAnnotation(AltTranslationsAnnotation.class), srcOriginal);
		
		// Make sure to call this too (for derived writers)
		writeScoredItem(tu);
	}

	private void writeAltTranslations (AltTranslationsAnnotation ann,
		TextFragment srcOriginal)
	{
		if ( ann == null ) {
			return;
		}
		for ( AltTranslation alt : ann ) {
			if ( alt.getFromOriginal() ) {
				// If it's coming from the original it's a true alternate (e.g. XLIFF one)
				tmxWriterAlternate.writeAlternate(alt, srcOriginal);
			}
			else {
				// Otherwise the translation is from a leveraging step
				tmxWriterLeverage.writeAlternate(alt, srcOriginal);
			}
		}
	}

	@Override
	public void writeScoredItem (ITextUnit item) {
		// Not used. Alternate writing is done in writeTMXEntries
		// But some derived writer may use this
	}

}
