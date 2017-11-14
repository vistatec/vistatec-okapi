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

package net.sf.okapi.applications.rainbow.packages.xliff;

import java.io.File;

import net.sf.okapi.applications.rainbow.packages.BaseWriter;
import net.sf.okapi.applications.rainbow.packages.ManifestItem;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.XLIFFContent;
import net.sf.okapi.common.filterwriter.XLIFFWriter;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Implements IWriter for generic XLIFF translation packages.
 */
public class Writer extends BaseWriter {
	
	private static final String EXTENSION = ".xlf";
	
	private static final String RESTYPEVALUES = 
		";auto3state;autocheckbox;autoradiobutton;bedit;bitmap;button;caption;cell;"
		+ "checkbox;checkboxmenuitem;checkedlistbox;colorchooser;combobox;comboboxexitem;"
		+ "comboboxitem;component;contextmenu;ctext;cursor;datetimepicker;defpushbutton;"
		+ "dialog;dlginit;edit;file;filechooser;fn;font;footer;frame;grid;groupbox;"
		+ "header;heading;hedit;hscrollbar;icon;iedit;keywords;label;linklabel;list;"
		+ "listbox;listitem;ltext;menu;menubar;menuitem;menuseparator;message;monthcalendar;"
		+ "numericupdown;panel;popupmenu;pushbox;pushbutton;radio;radiobuttonmenuitem;rcdata;"
		+ "row;rtext;scrollpane;separator;shortcut;spinner;splitter;state3;statusbar;string;"
		+ "tabcontrol;table;textbox;togglebutton;toolbar;tooltip;trackbar;tree;uri;userbutton;"
		+ "usercontrol;var;versioninfo;vscrollbar;window;";

	protected Options options;

	private XMLWriter writer = null;
	private XLIFFContent xliffCont;
	private boolean useSourceForTranslated = false;
	private boolean inFile;
	private LocaleId srcLang;
	private String docMimeType;

	public Writer () {
		super();
		options = new Options();
		xliffCont = new XLIFFContent();
	}
	
	public String getPackageType () {
		return "xliff";
	}
	
	public String getReaderClass () {
		//TODO: Use dynamic name
		return "net.sf.okapi.applications.rainbow.packages.xliff.Reader";
	}
	
	@Override
	public void writeStartPackage () {
		// Set source and target if they are not set yet
		// This allow other package types to be derived from this one.
		String tmp = manifest.getSourceLocation();
		if (( tmp == null ) || ( tmp.length() == 0 )) {
			manifest.setSourceLocation("work");
		}
		tmp = manifest.getTargetLocation();
		if (( tmp == null ) || ( tmp.length() == 0 )) {
			manifest.setTargetLocation("work");
		}
		tmp = manifest.getOriginalLocation();
		if (( tmp == null ) || ( tmp.length() == 0 )) {
			manifest.setOriginalLocation("original");
		}
		if (( tmp == null ) || ( tmp.length() == 0 )) {
			manifest.setDoneLocation("done");
		}
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
		
		// OmegaT specific options
		if ( manifest.getPackageType().equals("omegat") ) {
			// OmegaT does not support sub-folder, so we flatten the structure
			// and make sure identical filename do not clash
			relativeWorkPath = String.format("%d.%s", docID,
				Util.getFilename(relativeSourcePath, true));
			
			// Do not export items with translate='no'
			options.setIncludeNoTranslate(false);
			
			// Make sure to copy the source on empty target
			options.setCopySource(true);
			
			// No alt-trans in the XLIFF
			options.setIncludeAltTrans(false);
			
			// If translated found: replace the target text by the source.
			// Trusting the target will be gotten from the TMX from original
			// This to allow editing of pre-translated items in XLIFF editors
			// that use directly the <target> element.
			useSourceForTranslated = true;
		}

		relativeWorkPath += EXTENSION;
		super.createOutput(docID, relativeSourcePath, relativeTargetPath,
			sourceEncoding, targetEncoding, filtersettings, filterParams);
        close();
		writer = new XMLWriter(manifest.getRoot() + File.separator
			+ ((manifest.getSourceLocation().length() == 0 ) ? "" : (manifest.getSourceLocation() + File.separator))
			+ relativeWorkPath);
	}

	public void close () {
		if ( writer != null ) {
			writer.close();
			writer = null;
		}
	}

	public String getName() {
		return getClass().getName();
	}

	public EncoderManager getEncoderManager () {
		return null;
	}
	
	public IParameters getParameters () {
		return options;
	}

	public void setParameters (IParameters params) {
		options = (Options)params;
	}

	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			processStartDocument((StartDocument)event.getResource());
			break;
		case END_DOCUMENT:
			processEndDocument();
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
		default:
			// DO nothing
			break;
		}
		return event;
	}

	private void processStartDocument (StartDocument resource) {
		srcLang = resource.getLocale();
		writer.writeStartDocument();
		writer.writeStartElement("xliff");
		writer.writeAttributeString("version", "1.2");
		writer.writeAttributeString("xmlns", "urn:oasis:names:tc:xliff:document:1.2");
		writer.writeAttributeString("xmlns:okp", "okapi-framework:xliff-extensions"); 
		docMimeType = resource.getMimeType();
		if (( options.getMessage() != null ) && ( options.getMessage().length() > 0 )) {
			writer.writeComment(options.getMessage(), true);
		}
	}

	private void processEndDocument () {
		if ( inFile ) writeEndFile();
		writer.writeEndElementLineBreak(); // xliff
		writer.writeEndDocument();
		close();

		manifest.addDocument(docID, relativeWorkPath, relativeSourcePath,
			relativeTargetPath, sourceEncoding, targetEncoding, filterID,
			ManifestItem.POSPROCESSING_TYPE_DEFAULT);
	}

	private void processStartSubDocument (StartSubDocument resource) {
		writeStartFile(resource.getName(), resource.getMimeType());		
	}
	
	private void writeStartFile (String original,
		String contentType)
	{
		writer.writeLineBreak();
		writer.writeStartElement("file");
		writer.writeAttributeString("original",
			(original!=null) ? original : "unknown");
		writer.writeAttributeString("source-language", srcLang.toBCP47());
		writer.writeAttributeString("target-language", trgLoc.toBCP47());
		
		if ( contentType == null ) contentType = "x-undefined";
		else if ( contentType.equals("text/html") ) contentType = "html";
		else if ( contentType.equals("text/xml") ) contentType = "xml";
		// TODO: other standard XLIFF content types
		else contentType = "x-"+contentType;
		writer.writeAttributeString("datatype",contentType);
		writer.writeLineBreak();
		
		inFile = true;

//		writer.writeStartElement("header");
//		writer.writeEndElement(); // header
		
		writer.writeStartElement("body");
		writer.writeLineBreak();
	}
	
	private void processEndSubDocument (Ending resource) {
		writeEndFile();
	}
	
	private void writeEndFile () {
		writer.writeEndElementLineBreak(); // body
		writer.writeEndElementLineBreak(); // file
		inFile = false;
	}
	
	private void processStartGroup (StartGroup resource) {
		if ( !inFile ) writeStartFile(relativeSourcePath, docMimeType);
		writer.writeStartElement("group");
		writer.writeAttributeString("id", resource.getId());
		String tmp = resource.getName();
		if (( tmp != null ) && ( tmp.length() != 0 )) {
			writer.writeAttributeString("resname", tmp);
		}
		tmp = resource.getType();
		if ( !Util.isEmpty(tmp) ) {
			if ( tmp.startsWith("x-") || ( RESTYPEVALUES.contains(";"+tmp+";")) ) {
				writer.writeAttributeString("restype", tmp);
			}
			else { // Make sure the value is valid
				writer.writeAttributeString("restype", "x-"+tmp);
			}
		}
		writer.writeLineBreak();
	}
	
	private void processEndGroup (Ending resource) {
		writer.writeEndElementLineBreak(); // group
	}
	
	private void processTextUnit (ITextUnit tu) {
		// Check if we need to set the entry as non-translatable
		if ( options.getSetApprovedAsNoTranslate() ) {
			Property prop = tu.getTargetProperty(trgLoc, Property.APPROVED);
			if (( prop != null ) && prop.getValue().equals("yes") ) {
				tu.setIsTranslatable(false);
			}
		}
		// Check if we need to skip non-translatable entries
		if ( !options.getIncludeNoTranslate() && !tu.isTranslatable() ) {
			return;
		}

		if ( !inFile ) writeStartFile(relativeSourcePath, docMimeType);

		writer.writeStartElement("trans-unit");
		writer.writeAttributeString("id", String.valueOf(tu.getId()));
		String tmp = tu.getName();
		if (( tmp != null ) && ( tmp.length() != 0 )) {
			writer.writeAttributeString("resname", tmp);
		}
		tmp = tu.getType();
		if ( !Util.isEmpty(tmp) ) {
			if ( tmp.startsWith("x-") || ( RESTYPEVALUES.contains(";"+tmp+";")) ) {
				writer.writeAttributeString("restype", tmp);
			}
			else { // Make sure the value is valid
				writer.writeAttributeString("restype", "x-"+tmp);
			}
		}
		if ( !tu.isTranslatable() )
			writer.writeAttributeString("translate", "no");

		if ( tu.hasTargetProperty(trgLoc, Property.APPROVED) ) {
			if ( tu.getTargetProperty(trgLoc, Property.APPROVED).getValue().equals("yes") ) {
				writer.writeAttributeString(Property.APPROVED, "yes");
			}
			// "no" is the default
		}
		
		if ( tu.preserveWhitespaces() )
			writer.writeAttributeString("xml:space", "preserve");
		writer.writeLineBreak();

		// Get the source container
		TextContainer tc = tu.getSource();
		boolean srcHasText = tc.hasText(false);

		//--- Write the source
		
		writer.writeStartElement("source");
		writer.writeAttributeString("xml:lang", manifest.getSourceLanguage().toBCP47());
		// Write full source content (always without segments markers
		writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, false, false,
			options.getGMode(), false, true, manifest.getTargetLanguage()));
		writer.writeEndElementLineBreak(); // source
		// Write segmented source (with markers) if needed
		if ( tc.hasBeenSegmented() ) {
			writer.writeStartElement("seg-source");
			writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, false, true,
				options.getGMode(), false, true, manifest.getTargetLanguage()));
			writer.writeEndElementLineBreak(); // seg-source
		}

		//--- Write the target
		
		writer.writeStartElement("target");
		writer.writeAttributeString("xml:lang", manifest.getTargetLanguage().toBCP47());
		
		// At this point tc contains the source
		// Do we have an available target to use instead?
		tc = tu.getTarget(trgLoc);
		if ( useSourceForTranslated || ( tc == null ) || ( tc.isEmpty() )
			|| ( srcHasText && !tc.hasText(false) )) {
			tc = tu.getSource(); // Fall back to source
			if ( !options.getCopySource() ) tc.clear(); // Clear target if requested
		}
		
		// Write out TMX entries
		super.writeTMXEntries(tu);
		
		// Now tc hold the content to write. Write it with or without marks
		writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, false, tc.hasBeenSegmented(),
			options.getGMode(), false, true, manifest.getTargetLanguage()));
		writer.writeEndElementLineBreak(); // target
		
		// Note
		if ( tu.hasProperty(Property.NOTE) ) {
			writer.writeStartElement("note");
			writer.writeString(tu.getProperty(Property.NOTE).getValue());
			writer.writeEndElementLineBreak(); // note
		}
		
		// Alt-trans
		if ( options.getIncludeAltTrans() ) {
			tc = tu.getTarget(trgLoc);
			if ( tc != null ) {
				writeAlternates(tc.getAnnotation(AltTranslationsAnnotation.class), null);
				for ( Segment seg : tc.getSegments() ) {
					writeAlternates(seg.getAnnotation(AltTranslationsAnnotation.class), seg);
				}
			}
		}

		writer.writeEndElementLineBreak(); // trans-unit
	}
	
	/**
	 * Writes an <at-trans> element.
	 * @param atAnn the annotation to write out.
	 * @param seg Segment to which the annotation is attached, or null if the
	 * annotation is attached to a text container.
	 */
	private void writeAlternates (AltTranslationsAnnotation atAnn,
		Segment seg)
	{
		if ( atAnn == null ) {
			return;
		}
		for ( AltTranslation at : atAnn ) {
			writer.writeStartElement("alt-trans");
			if ( seg != null ) {
				writer.writeAttributeString("mid", seg.id);
			}
			if ( at.getCombinedScore() > 0 ) {
				writer.writeAttributeString("match-quality", at.getCombinedScore()+"%");
			}
			if ( !Util.isEmpty(at.getOrigin()) ) {
				writer.writeAttributeString("origin", at.getOrigin());
			}
			if ( at.getType() != MatchType.UKNOWN ) {
				writer.writeAttributeString("okp:"+XLIFFWriter.OKP_MATCHTYPE, at.getType().toString());
			}
			writer.writeLineBreak();
			// alt-trans source
			TextContainer cont = at.getSource();
			if ( !cont.isEmpty() ) {
				writer.writeStartElement("source");
				writer.writeAttributeString("xml:lang", at.getSourceLocale().toBCP47());
				writer.writeRawXML(xliffCont.toSegmentedString(cont, 0, false, false, options.getGMode(), false, true, manifest.getTargetLanguage()));
				writer.writeEndElementLineBreak(); // alt-trans
			}
			// alt-trans target
			writer.writeStartElement("target");
			writer.writeAttributeString("xml:lang", at.getTargetLocale().toBCP47());
			cont = at.getTarget();
			writer.writeRawXML(xliffCont.toSegmentedString(cont, 0, false, false, options.getGMode(), false, true, manifest.getTargetLanguage()));
			writer.writeEndElementLineBreak(); // target
			// End of alt-trans
			writer.writeEndElementLineBreak();
		}
		
	}

	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return null;
	}

}
