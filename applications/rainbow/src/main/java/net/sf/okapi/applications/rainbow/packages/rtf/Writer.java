/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.packages.rtf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;

import net.sf.okapi.applications.rainbow.packages.BaseWriter;
import net.sf.okapi.applications.rainbow.packages.ManifestItem;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.layerprovider.ILayerProvider;
import net.sf.okapi.common.layerprovider.LayerProvider;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

public class Writer extends BaseWriter {
	
	private static final String   EXTENSION = ".rtf";

	private ISkeletonWriter skelWriter;
	private ILayerProvider layer;
	private PrintWriter writer;
	
	public void setSkeletonWriter (ISkeletonWriter skelWriter) {
		if ( skelWriter == null ) {
			throw new InvalidParameterException("You cannot use the RTF writer with no skeleton writer.\n"
				+ "The filter you are trying to use may be incompatible with an RTF output.");
		}
		this.skelWriter = skelWriter;
		// Keep 2 copies of the referents for RTF: source and target
		if ( this.skelWriter instanceof GenericSkeletonWriter ) {
			((GenericSkeletonWriter)this.skelWriter).setReferentCopies(2);
		}
	}
	
	@Override
	public String getPackageType () {
		return "rtf";
	}
	
	@Override
	public String getReaderClass () {
		return "none"; // RTF use a non-reader-driven post-processing mode
	}
	
	@Override
	public void writeStartPackage () {
		manifest.setSourceLocation("work");
		manifest.setTargetLocation("work");
		manifest.setOriginalLocation("original");
		manifest.setDoneLocation("done");
		super.writeStartPackage();
	}

	@Override
	public void createOutput (int docID,
		String relativeSourcePath,
		String relativeTargetPath,
		String sourceEncoding,
		String targetEncoding,
		String filtersettings,
		IParameters filterParams,
		EncoderManager encoderManager)
	{
		this.encoderManager = encoderManager;
		relativeWorkPath = relativeSourcePath;
		relativeWorkPath += EXTENSION;

		super.createOutput(docID, relativeSourcePath, relativeTargetPath,
			sourceEncoding, targetEncoding, filtersettings, filterParams);

		String path = manifest.getRoot() + File.separator
			+ ((manifest.getSourceLocation().length() == 0 ) ? "" : (manifest.getSourceLocation() + File.separator)) 
			+ relativeWorkPath;
		Util.createDirectories(path);
	}

	@Override
	public void close () {
		// Do not nullify the skelWriter as it is not created in this class
		if ( skelWriter != null ) {
			skelWriter.close();
		}
		if ( writer != null ) {
			writer.close();
			writer = null;
		}
	}

	@Override
	public String getName () {
		return getClass().getName();
	}

	@Override
	public EncoderManager getEncoderManager () {
		return encoderManager;
	}
	
	@Override
	public IParameters getParameters () {
		return null;
	}

	@Override
	public void setOptions (LocaleId locale,
		String defaultEncoding)
	{
		//TODO: Fix encoding so we use a windows-enabled one (especially for UTF-16)
		super.setOptions(locale, defaultEncoding);
		layer = new LayerProvider();
		layer.setOptions(null, encoding, null);
	}

	@Override
	public void setParameters (IParameters params) {
		// Nothing to do
	}

	@Override
	public Event handleEvent (Event event) {
		try {
			switch ( event.getEventType() ) {
			case START_DOCUMENT:
				processStartDocument((StartDocument)event.getResource());
				break;
			case END_DOCUMENT:
				processEndDocument((Ending)event.getResource());
				close();
				break;
			case START_SUBDOCUMENT:
				processStartSubDocument((StartSubDocument)event.getResource());
				break;
			case END_SUBDOCUMENT:
				processEndSubDocument((Ending)event.getResource());
				break;
			case START_GROUP:
			case START_SUBFILTER:
				processStartGroup((StartGroup)event.getResource());
				break;
			case END_GROUP:
			case END_SUBFILTER:
				processEndGroup((Ending)event.getResource());
				break;
			case TEXT_UNIT:
				processTextUnit(event.getTextUnit());
				break;
			case DOCUMENT_PART:
				processDocumentPart((DocumentPart)event.getResource());
				break;
			default:
				break;
			}
		}
		catch ( FileNotFoundException e ) {
			throw new OkapiException(e);
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiException(e);
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
		return event;
	}

	public IParameters getOptions () {
		return null;
	}

	public void setOptions (IParameters options) {
		// Nothing to do
	}

	private void processStartDocument (StartDocument resource) {
		try {
			Util.createDirectories(outputPath);
			writer = new PrintWriter(outputPath, encoding);
		}
		catch ( FileNotFoundException e ) {
			throw new OkapiException(e);
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiException(e);
		}

		//TODO: change codepage
		writer.write("{\\rtf1\\ansi\\ansicpg" + "1252" + "\\uc1\\deff1 \n"+
			"{\\fonttbl \n"+
			"{\\f1 \\fmodern\\fcharset0\\fprq1 Courier New;}\n"+
			"{\\f2 \\fswiss\\fcharset0\\fprq2 Arial;}\n"+
			"{\\f3 \\froman\\fcharset0\\fprq2 Times New Roman;}}\n"+
			"{\\colortbl \\red0\\green0\\blue0;\\red0\\green0\\blue0;\\red0\\green0\\blue255;"+
			"\\red0\\green255\\blue255;\\red0\\green255\\blue0;\\red255\\green0\\blue255;"+
			"\\red255\\green0\\blue0;\\red255\\green255\\blue0;\\red255\\green255\\blue255;"+
			"\\red0\\green0\\blue128;\\red0\\green128\\blue128;\\red0\\green128\\blue0;"+
			"\\red128\\green0\\blue128;\\red128\\green0\\blue0;\\red128\\green128\\blue0;"+
			"\\red128\\green128\\blue128;\\red192\\green192\\blue192;}\n"+
			"{\\stylesheet \n"+
			"{\\s0 \\sb80\\slmult1\\widctlpar\\fs20\\f1 \\snext0 Normal;}\n"+
			"{\\cs1 \\additive \\v\\cf12\\sub\\f1 tw4winMark;}\n"+
			"{\\cs2 \\additive \\cf4\\fs40\\f1 tw4winError;}\n"+
			"{\\cs3 \\additive \\f1\\cf11 tw4winPopup;}\n"+
			"{\\cs4 \\additive \\f1\\cf10 tw4winJump;}\n"+
			"{\\cs5 \\additive \\cf15\\f1\\lang1024\\noproof tw4winExternal;}\n"+
			"{\\cs6 \\additive \\cf6\\f1\\lang1024\\noproof tw4winInternal;}\n"+
			"{\\cs7 \\additive \\cf2 tw4winTerm;}\n"+
			"{\\cs8 \\additive \\cf13\\f1\\lang1024\\noproof DO_NOT_TRANSLATE;}\n"+
			"{\\cs9 \\additive Default Paragraph Font;}"+
			"{\\cs15 \\additive \\v\\f1\\cf12\\sub tw4winMark;}"+
			"}\n"+
			"\\paperw11907\\paperh16840\\viewkind4\\viewscale100\\pard\\plain\\s0\\sb80\\slmult1\\widctlpar\\fs20\\f1 \n"+
			Util.RTF_STARTCODE);

		// Write the skeleton
		writer.write(skelWriter.processStartDocument(trgLoc, encoding, layer, encoderManager, resource));
	}

	private void processEndDocument (Ending resource) { 
		writer.write(skelWriter.processEndDocument(resource));
		writer.write(Util.RTF_ENDCODE+"}\n");
		writer.close();
		if ( manifest != null ) {
			manifest.addDocument(docID, relativeWorkPath, relativeSourcePath,
				relativeTargetPath, sourceEncoding, targetEncoding, filterID,
				ManifestItem.POSPROCESSING_TYPE_RTF);
		}
	}

	private void processStartSubDocument (StartSubDocument resource) throws IOException {
		writer.write(skelWriter.processStartSubDocument(resource));
	}

	private void processEndSubDocument (Ending resource) throws IOException {
		writer.write(skelWriter.processEndSubDocument(resource));
	}

	private void processStartGroup (StartGroup resource) throws IOException {
		writer.write(skelWriter.processStartGroup(resource));
	}

	private void processEndGroup (Ending resource) throws IOException {
		writer.write(skelWriter.processEndGroup(resource));
	}

	private void processTextUnit (ITextUnit tu) {
		// Write out TMX entries
		super.writeTMXEntries(tu);
		// Write skeleton and its content
		writer.write(skelWriter.processTextUnit(tu));
	}

	private void processDocumentPart (DocumentPart resource) throws IOException {
		writer.write(skelWriter.processDocumentPart(resource));
	}

	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return skelWriter;
	}

}
