/*===========================================================================
  Copyright (C) 2009-2017 by the Okapi Framework contributors
-----------------------------------------------------------------------------
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

package net.sf.okapi.common.filterwriter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Stack;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.ITSLQIAnnotations;
import net.sf.okapi.common.annotation.ITSProvenanceAnnotations;
import net.sf.okapi.common.annotation.TermsAnnotation;
import net.sf.okapi.common.annotation.XLIFFNote;
import net.sf.okapi.common.annotation.XLIFFNoteAnnotation;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Writer for creating XLIFF document.
 */
public class XLIFFWriter implements IFilterWriter {
	
	/**
	 * Name of the Okapi XLIFF extension matchType.
	 */
	public static final String OKP_MATCHTYPE = "matchType";
	/**
	 * Name of the Okapi XLIFF extension engine.
	 */
	public static final String OKP_ENGINE = "engine";

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

	private XMLWriter writer;
	private XLIFFContent xliffCont;
	private ITSContent itsContForUnit;
	private ITSContent itsContForSrcCont;
	private ITSContent itsContForTrgCont;
	private ITSContent itsContForAltTrgCont;
	private String skeletonPath;
	
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private String dataType;
	private String original;
	
	private String fwOutputPath; // Used in IFilterWriter mode
	private String fwConfigId; // Used in IFilterWriter mode
	private String fwInputEncoding; // Used in IFilterWriter mode

	private boolean inFile;
	private boolean hasFile;
	
	private Stack<String> annotatorsRef;
	private boolean needAnnotatorsRef;
	private OutputStream outputStream;

	
	private XLIFFWriterParameters params = new XLIFFWriterParameters();
	
	/**
	 * Creates an XLIFF writer object.
	 */
	public XLIFFWriter () {
		xliffCont = new XLIFFContent();
	}
	
	/**
	 * Creates a new XLIFF document.
	 * @param xliffPath the full path of the document to create.
	 * @param skeletonPath the path for the skeleton, or null for no skeleton.
	 * @param srcLoc the source locale.
	 * @param trgLoc the target locale, or null for no target.
	 * @param dataType the value for the <code>datatype</code> attribute.
	 * @param original the value for the <code>original</code> attribute.
	 * @param message optional comment to put at the top of the document (can be null).
	 */
	public void create (String xliffPath,
		String skeletonPath,
		LocaleId srcLoc,
		LocaleId trgLoc,
		String dataType,
		String original,
		String message)
	{
		if ( writer != null ) {
			close();
		}
		
		this.skeletonPath = skeletonPath;
		this.original = original;
		this.srcLoc = srcLoc;
		
		// some filters like xini do not set the source locale
		if (srcLoc == null) {
		    this.srcLoc = LocaleId.EMPTY;
		}
		
		this.trgLoc = trgLoc;
		this.dataType = dataType;
				
		// Create the output
        if ( outputStream == null ) {               
            writer = new XMLWriter(xliffPath);                         
        } else if ( outputStream != null ) {            
            writer = new XMLWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));            
        }
		
		writer.writeStartDocument();
		writer.writeStartElement("xliff");
		writer.writeAttributeString("version", "1.2");
		writer.writeAttributeString("xmlns", Namespaces.NS_XLIFF12);
		writer.writeAttributeString("xmlns:okp", Namespaces.NS_XLIFFOKAPI);
		if ( params.getIncludeIts() ) {
			writer.writeAttributeString("xmlns:"+Namespaces.ITS_NS_PREFIX, Namespaces.ITS_NS_URI); 
			writer.writeAttributeString("xmlns:"+Namespaces.ITSXLF_NS_PREFIX, Namespaces.ITSXLF_NS_URI); 
			writer.writeAttributeString(Namespaces.ITS_NS_PREFIX+":version", "2.0");
		}
		writeAnnotatorsRefIfNeeded();

		if ( !Util.isEmpty(message) ) {
			writer.writeLineBreak();
			writer.writeComment(message, false);
		}
		writer.writeLineBreak();
		
		// ITS helpers
		itsContForUnit = new ITSContent(xliffCont.getCharsetEncoder(), false, true);
		itsContForSrcCont = new ITSContent(xliffCont.getCharsetEncoder(), false, true);
		itsContForTrgCont = new ITSContent(xliffCont.getCharsetEncoder(), false, true);
		itsContForAltTrgCont = new ITSContent(xliffCont.getCharsetEncoder(), false, true);
		
		annotatorsRef = new Stack<String>();
		annotatorsRef.push(null);
		needAnnotatorsRef = false;
	}

	/**
	 * Writes the end of this the document and close it.
	 * If a &lt;file&gt; element is currently opened, it is closed automatically.
	 */
	public void close () {
		if ( writer != null ) {
			if ( !hasFile ) {
				writeStartFile(original, dataType, skeletonPath, fwConfigId, fwInputEncoding, null);
			}
			if ( inFile ) {
				writeEndFile();
			}
			writer.writeEndElementLineBreak(); // xliff
			writer.writeEndDocument();
			writer.close();
			writer = null;
		}
		fwConfigId = null;
		fwInputEncoding = null;
		skeletonPath = null;
	}

	/**
	 * Writes the start of a &lt;file&gt; element.
	 * <p>each call to this method must have a corresponding call to {@link #writeEndFile()}.
	 * @param original the value for the <code>original</code> attribute. If null: "unknown" is used.
	 * @param dataType the value for the <code>datatype</code> attribute. If null: "x-undefined" is used. 
	 * @param skeletonPath optional external skeleton information, or null.
	 * @see #writeEndFile()
	 */
	public void writeStartFile (String original,
		String dataType,
		String skeletonPath)
	{
		writeStartFile(original, dataType, skeletonPath, null, null, null);
	}
	
	/**
	 * Writes the start of a &lt;file&gt; element.
	 * <p>each call to this method must have a corresponding call to {@link #writeEndFile()}.
	 * @param original the value for the <code>original</code> attribute. If null: "unknown" is used.
	 * @param dataType the value for the <code>datatype</code> attribute. If null: "x-undefined" is used. 
	 * @param skeletonPath optional external skeleton information, or null.
	 * @param extraForHeader optional extra raw valid XLIFF to place in the header, or null.
	 * @see #writeEndFile()
	 */
	public void writeStartFile (String original,
		String dataType,
		String skeletonPath,
		String extraForHeader)
	{
		writeStartFile(original, dataType, skeletonPath, null, null, extraForHeader);
	}
	
	/**
	 * Internal method to write the start of a &lt;file&gt; element.
	 * <p>each call to this method must have a corresponding call to {@link #writeEndFile()}.
	 * @param original the value for the <code>original</code> attribute. If null: "unknown" is used.
	 * @param dataType the value ofr the <code>datatype</code> attribute. If null: "x-undefined" is used. 
	 * @param skeletonPath external skeleton information (can be null).
	 * @param configId the optional filter configuration id used to extract the original (IFilterWriter mode), or null.
	 * @param inputEncoding the optional encoding of the input file (IFilterWriter mode), or null.
	 * @param extraForHeader optional extra raw valid XLIFF to place in the header, or null.
	 * @see #writeEndFile()
	 */
	private void writeStartFile (String original,
		String dataType,
		String skeletonPath,
		String configId,
		String inputEncoding,
		String extraForHeader)
	{
		writer.writeStartElement("file");
		writer.writeAttributeString("original",
			(original!=null) ? original : "unknown");
		writer.writeAttributeString("source-language", srcLoc.toBCP47());
		if ( trgLoc != null ) {
			writer.writeAttributeString("target-language", trgLoc.toBCP47());
		}
		
		if ( dataType == null ) dataType = "x-undefined";
		else if ( dataType.equals("text/html") ) dataType = "html";
		else if ( dataType.equals("text/xml") ) dataType = "xml";
		else if ( !dataType.startsWith("x-")
			&& !dataType.equals("html")
			&& !dataType.equals("xml") )
		{
			dataType = "x-"+dataType;
		}
		writer.writeAttributeString("datatype", dataType);

		if ( !Util.isEmpty(inputEncoding) ) {
			writer.writeAttributeString("okp:inputEncoding", inputEncoding);
		}
		if ( !Util.isEmpty(configId) ) {
			writer.writeAttributeString("okp:configId", configId);
		}
		writeAnnotatorsRefIfNeeded();
		writer.writeLineBreak();
		
		// Write out external skeleton info if available 
		// test of tool element should be written
		if ( !Util.isEmpty(skeletonPath) || !Util.isEmpty(extraForHeader) || 
				(!Util.isEmpty(params.getToolId()) && !Util.isEmpty(params.getToolName())) ) {
			writer.writeStartElement("header");			
			
			if ( !Util.isEmpty(skeletonPath) ) {
				writer.writeStartElement("skl");
				writer.writeStartElement("external-file");
				writer.writeAttributeString("href", skeletonPath);
				writer.writeEndElement(); // external-file
				writer.writeEndElement(); // skl
			}
			
			// see if we have the required attributes for the tool element
			// if we have them then write out "<tool..."
			if (!Util.isEmpty(params.getToolId()) && !Util.isEmpty(params.getToolName())) {
				writer.writeStartElement("tool");
				writer.writeAttributeString("tool-id", params.getToolId());
				writer.writeAttributeString("tool-name", params.getToolName());
				
				if (!Util.isEmpty(params.getToolVersion())) 
					writer.writeAttributeString("tool-version", params.getToolVersion());
				if (!Util.isEmpty(params.getToolCompany())) 
					writer.writeAttributeString("tool-company", params.getToolCompany());
				
				// tool extension point - write out custom namespace snippet
				if ( !Util.isEmpty(params.getToolXmlSnippet()) ) {
					writer.writeRawXML(params.getToolXmlSnippet());
				}
				writer.writeEndElement(); // tool
			}
						
			if ( !Util.isEmpty(extraForHeader) ) {
				writer.writeRawXML(extraForHeader);
			}
			writer.writeEndElementLineBreak(); // header
		}

		inFile = hasFile = true;
		writer.writeStartElement("body");
		writer.writeLineBreak();
	}
	
	
	/**
	 * Writes the end of a &lt;file&gt; element.
	 * This method should be called for each call to {@link #writeStartFile(String, String, String)}.
	 * @see #writeStartFile(String, String, String)
	 */
	public void writeEndFile () {
		writer.writeEndElementLineBreak(); // body
		writer.writeEndElementLineBreak(); // file
		inFile = false;
	}
	/**
	 * Writes the start of a &lt;group&gt; element.
	 * @see #writeEndGroup()
	 */
	public void writeStartGroup (StartGroup resource) {
		if ( !inFile ) {
			writeStartFile(original, dataType, skeletonPath, fwConfigId, fwInputEncoding, null);
		}
		String id = resource.getId();
		String resName = resource.getName();
		String resType = resource.getType();
		writer.writeStartElement("group");
		writer.writeAttributeString("id", id);
		if ( !Util.isEmpty(resName) ) {
			writer.writeAttributeString("resname", resName);
		}
		if ( !Util.isEmpty(resType) ) {
			if ( resType.startsWith("x-") || ( RESTYPEVALUES.contains(";"+resType+";")) ) {
				writer.writeAttributeString("restype", resType);
			}
			else { // Make sure the value is valid
				writer.writeAttributeString("restype", "x-"+resType);
			}
		}
		writeAnnotatorsRefIfNeeded();
		writeSizeProperties(writer, resource);
		writer.writeLineBreak();
	}

	/**
	 * Writes the end of a &lt;group&gt; element.
	 * @see #writeStartGroup(StartGroup)
	 */
	public void writeEndGroup () {
		writer.writeEndElementLineBreak(); // group
	}
	
	/**
	 * Writes a text unit as a &lt;trans-unit&gt; element.
	 * @param tu the text unit to output.
	 */
	public void writeTextUnit (ITextUnit tu) {
		writeTextUnit(tu, null);
	}
	
	/**
	 * Writes a text unit as a &lt;trans-unit&gt; element.
	 * @param tu the text unit to output.
	 * @param phaseName the value of <code>phase-name</code> (can be null).
	 */
	public void writeTextUnit (ITextUnit tu, String phaseName) {
		// Avoid writing out some entries in non-IFilterWriter mode
		if ( fwConfigId == null ) {
			// Check if we need to set the entry as non-translatable
			if ( params.getSetApprovedAsNoTranslate() ) {
				Property prop = tu.getTargetProperty(trgLoc, Property.APPROVED);
				if (( prop != null ) && prop.getValue().equals("yes") ) {
					tu.setIsTranslatable(false);
				}
			}
			// Check if we need to skip non-translatable entries
			if ( !tu.isTranslatable() && !params.getIncludeNoTranslate() ) {
				return;
			}
		}

		if ( !inFile ) {
			writeStartFile(original, dataType, skeletonPath, fwConfigId, fwInputEncoding, null);
		}

		// Push the new values of the annotators based on possible annotation at the TU level
		pushAnnotatorsRef(ITSContent.getAnnotatorsRef(tu));

		writer.writeStartElement("trans-unit");
		writer.writeAttributeString("id", tu.getId());
		String tmp = tu.getName();
		if ( !Util.isEmpty(tmp) ) {
			writer.writeAttributeString("resname", tmp);
		}
		writeSizeProperties(writer, tu);

		// Write the coord attribute if present on the trans-unit itself
		if (tu.hasProperty(Property.COORDINATES) ) {
			writer.writeAttributeString("coord", tu.getProperty(Property.COORDINATES).getValue());
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
		if ( !tu.isTranslatable() ) {
			writer.writeAttributeString("translate", "no");
		}
		else if ( trgLoc != null ) {
			// If translatable and we have an ITS Locale Filter annotation: check it
			GenericAnnotations anns = tu.getAnnotation(GenericAnnotations.class);
			if ( anns != null ) {
				GenericAnnotation ann = anns.getFirstAnnotation(GenericAnnotationType.LOCFILTER);
				if ( ann != null ) {
					String value = ann.getString(GenericAnnotationType.LOCFILTER_VALUE);
					if ( !ITSContent.isExtendedMatch(value, trgLoc.toBCP47()) ) {
						writer.writeAttributeString("translate", "no");
					}
				}
			}
		}
		if ( phaseName != null ) {
			writer.writeAttributeString("phase-name", phaseName);
		}

		if ( trgLoc != null ) {
			if ( tu.hasTargetProperty(trgLoc, Property.APPROVED) ) {
				if ( tu.getTargetProperty(trgLoc, Property.APPROVED).getValue().equals("yes") ) {
					writer.writeAttributeString(Property.APPROVED, "yes");
				}
				// "no" is the default
			}
		}
		
		if ( tu.preserveWhitespaces() ) {
			writer.writeAttributeString("xml:space", "preserve");
		}

		StringBuilder sbITS = new StringBuilder();
		
		// ITS Annotations at the unit level
		if ( params.getIncludeIts() ) {
			writeAnnotatorsRefIfNeeded();
			sbITS.setLength(0);
			itsContForUnit.outputAnnotations(tu.getAnnotation(GenericAnnotations.class), sbITS, false, false, false, trgLoc);
			// Provenance
			itsContForUnit.outputAnnotations(
				(GenericAnnotations)tu.getAnnotation(ITSProvenanceAnnotations.class), sbITS, false, false, false, trgLoc);
			// Write attributes
			writer.appendRawXML(sbITS.toString());
		}
		
		writer.writeLineBreak();

		// Get the source container
		TextContainer tc = tu.getSource();
		boolean srcHasText = tc.hasText(false);

		//--- Write the source
		
		// Push the new values of the annotators based on possible annotation at the source container level
		pushAnnotatorsRef(ITSContent.getAnnotatorsRef(tc));

		writer.writeStartElement("source");
		writer.writeAttributeString("xml:lang", srcLoc.toBCP47());

		// Write the coord attribute if needed
		if ( tc.hasProperty(Property.COORDINATES) ) {
			writer.writeAttributeString("coord", tc.getProperty(Property.COORDINATES).getValue());
		}

		// ITS Annotations at the source container level
		if ( params.getIncludeIts() ) {
			writeAnnotatorsRefIfNeeded();
			sbITS.setLength(0);
			itsContForSrcCont.outputAnnotations(tc.getAnnotation(GenericAnnotations.class), sbITS, false, false, false, null);
			// LQI
			itsContForSrcCont.outputAnnotations(
				(GenericAnnotations)tc.getAnnotation(ITSLQIAnnotations.class), sbITS, false, false, false, null);
			// Provenance
			itsContForSrcCont.outputAnnotations(
				(GenericAnnotations)tc.getAnnotation(ITSProvenanceAnnotations.class), sbITS, false, false, false, null);
			// Write the attributes
			writer.appendRawXML(sbITS.toString());
		}
		
		// Write full source content (always without segments markers
		writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, params.getEscapeGt(), false, params.getPlaceholderMode(),
			params.getIncludeCodeAttrs(), params.getIncludeIts(), trgLoc));
		List<GenericAnnotations> srcStandoff = xliffCont.getStandoff();
		writer.writeEndElementLineBreak(); // source
		
		// Write segmented source (with markers) if needed
		if ( tc.hasBeenSegmented() ) {
			writer.writeStartElement("seg-source");
			writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, params.getEscapeGt(), true, params.getPlaceholderMode(),
				params.getIncludeCodeAttrs(), params.getIncludeIts(), trgLoc));
			// No repeat of the standoff
			writer.writeEndElementLineBreak(); // seg-source
		}

		annotatorsRef.pop();
		
		//--- Write the target
		
		List<GenericAnnotations> trgStandoff = null;
		if ( trgLoc != null ) {
			// At this point tc contains the source
			// Do we have an available target to use instead?
			tc = tu.getTarget(trgLoc);
			boolean outputTarget = true;
			if (params.getUseSourceForTranslated()) {
				// Always override target with source
				tc = tu.getSource();
				outputTarget = true;
			}
			else if (tc == null || tc.isEmpty() || (srcHasText && !tc.hasText(false))) {
				// Override source if the copy option is set
				if (params.getCopySource()) {
					tc = tu.getSource();
				}
				else if (tc == null) {
					// If an empty target TC already exists, leave it -- it may have
					// properties set we need to preserve
					tc = new TextContainer();
				}
			}

			if ( outputTarget ) {
				// Push the new values of the annotators based on possible annotation at the target container level
				pushAnnotatorsRef(ITSContent.getAnnotatorsRef(tc));

				writer.writeStartElement("target");
				writer.writeAttributeString("xml:lang", trgLoc.toBCP47());

				// Write the target state, if present
				if (tc.hasProperty(Property.STATE) ) {
					writer.writeAttributeString(Property.STATE, tc.getProperty(Property.STATE).getValue());
				}

				// Write the target state-qualifier, if present
				if (tc.hasProperty(Property.STATE_QUALIFIER) ) {
					writer.writeAttributeString(Property.STATE_QUALIFIER, tc.getProperty(Property.STATE_QUALIFIER).getValue());
				}

				// Write the coord attribute if needed
				if ( tc.hasProperty(Property.COORDINATES) ) {
					writer.writeAttributeString("coord", tc.getProperty(Property.COORDINATES).getValue());
				}

				// ITS Annotations at the target container level
				if ( params.getIncludeIts() ) {
					writeAnnotatorsRefIfNeeded();
					sbITS.setLength(0);
					itsContForTrgCont.outputAnnotations(tc.getAnnotation(GenericAnnotations.class), sbITS, false, false, false, null);
					// LQI
					itsContForTrgCont.outputAnnotations(
						(GenericAnnotations)tc.getAnnotation(ITSLQIAnnotations.class), sbITS, false, false, false, null);
					// Provenance
					itsContForTrgCont.outputAnnotations(
						(GenericAnnotations)tc.getAnnotation(ITSProvenanceAnnotations.class), sbITS, false, false, false, null);
					// Write attributes
					writer.appendRawXML(sbITS.toString());
				}
				
				// Now tc hold the content to write. Write it with or without marks
				writer.writeRawXML(xliffCont.toSegmentedString(tc, 0, params.getEscapeGt(), tc.hasBeenSegmented(), params.getPlaceholderMode(),
					params.getIncludeCodeAttrs(), params.getIncludeIts(), trgLoc));
				trgStandoff = xliffCont.getStandoff();
				writer.writeEndElementLineBreak(); // target
				annotatorsRef.pop();
			}

			// Possible alternate translations
			if ( params.getIncludeAltTrans() ) {
				// We re-get the target because tc could be coming from the source
				TextContainer altCont = tu.getTarget(trgLoc);
				if ( altCont != null ) {
					// From the target container
					writeAltTranslations(altCont.getAnnotation(AltTranslationsAnnotation.class), null);
					// From the segments
					for ( Segment seg : altCont.getSegments() ) {
						writeAltTranslations(seg.getAnnotation(AltTranslationsAnnotation.class), seg);
					}
				}
			}
		}
		
		// Deprecated Property.NOTE and Property.TRANSNOTE
		if ( tu.hasProperty(Property.NOTE) ) {
			writer.writeStartElement("note");
			writer.writeString(tu.getProperty(Property.NOTE).getValue());
			writer.writeEndElementLineBreak(); // note
		}
		if ( tu.hasProperty(Property.TRANSNOTE) ) {
			writer.writeStartElement("note");
			writer.writeAttributeString("from", "translator");
			writer.writeString(tu.getProperty(Property.TRANSNOTE).getValue());
			writer.writeEndElementLineBreak(); // note
		}
		
		// new XliffNote Annotation support
		writeXliffNotes(tu);
		
		if ( srcStandoff != null ) {
			writer.writeRawXML(writeStandoffLQI(srcStandoff));
		}
		if (( itsContForSrcCont != null ) && itsContForSrcCont.hasStandoff() ) {
			writer.writeRawXML(itsContForSrcCont.writeStandoffLQI());
		}
		if ( trgStandoff != null ) {
			writer.writeRawXML(writeStandoffLQI(trgStandoff));
		}
		if (( itsContForTrgCont != null ) && itsContForTrgCont.hasStandoff() ) {
			writer.writeRawXML(itsContForTrgCont.writeStandoffLQI());
		}
		if (( itsContForUnit != null ) && itsContForUnit.hasStandoff() ) {
			writer.writeRawXML(itsContForUnit.writeStandoffLQI());
		}
		
		// Temporary output for terms annotation
		//TODO: replace this by extended element linked with <mrk>
		TermsAnnotation ann = tu.getSource().getAnnotation(TermsAnnotation.class);
		if (( ann != null ) && ( ann.size() > 0 )) {
			writer.writeStartElement("note");
			writer.writeAttributeString("annotates", "source");
			writer.writeString("Terms:\n"+ann.toString());
			writer.writeEndElementLineBreak(); // note
		}

		writer.writeEndElementLineBreak(); // trans-unit
		annotatorsRef.pop();
	}
	
	private void writeXliffNotes(ITextUnit tu) {
		XLIFFNoteAnnotation notes = tu.getAnnotation(XLIFFNoteAnnotation.class);
		if (notes == null) {
			return;
		}

		for (XLIFFNote n : notes) {
			writer.writeStartElement("note");

			// annotates
			if (n.getAnnotates() != null) {
				writer.writeAttributeString("annotates", n.getAnnotates().toString());
			}

			// from
			if (n.getFrom() != null) {
				writer.writeAttributeString("from", n.getFrom());
			}

			// priority
			if (n.getPriority() != null) {
				writer.writeAttributeString("priority", n.getPriority().toString());
			}

			writer.writeString(n.getNoteText());
			writer.writeEndElementLineBreak(); // note
		}
	}

	/**
	 * Writes possible alternate translations
	 * @param ann the annotation with the alternate translations (can be null)
	 * @param segment the segment where the annotation comes from, or null  if the
	 * annotation comes from the container.
	 */
	private void writeAltTranslations (AltTranslationsAnnotation ann,
		Segment segment)
	{
		if ( ann == null ) {
			return;
		}
		for ( AltTranslation alt : ann ) {
			writer.writeStartElement("alt-trans");
			if ( segment != null ) {
				writer.writeAttributeString("mid", segment.getId());
			}
			if ( alt.getCombinedScore() > 0 ) {
				writer.writeAttributeString("match-quality", String.format("%d", alt.getCombinedScore()));
			}
			if ( !Util.isEmpty(alt.getOrigin()) ) {
				writer.writeAttributeString("origin", alt.getOrigin());
			}
			if ( alt.getType() != MatchType.UKNOWN ) {
				writer.writeAttributeString("okp:"+OKP_MATCHTYPE, alt.getType().toString());
			}
			if ( alt.getEngine() !=  null ) {
				writer.writeAttributeString("okp:"+OKP_ENGINE, alt.getEngine());
			}
			TextContainer cont = alt.getSource();
			if ( !cont.isEmpty() ) {
				writer.writeStartElement("source");
				writer.writeAttributeString("xml:lang", alt.getSourceLocale().toBCP47());
				// Write full source content (always without segments markers)
				writer.writeRawXML(xliffCont.toSegmentedString(cont, 0, params.getEscapeGt(), false, params.getPlaceholderMode(),
					params.getIncludeCodeAttrs(), params.getIncludeIts(), trgLoc));
				writer.writeEndElementLineBreak(); // source
			}
			
			// Push the new values of the annotators based on possible annotation at the target container level
			pushAnnotatorsRef(ITSContent.getAnnotatorsRef(alt.getTarget()));
			writer.writeStartElement("target");
			writer.writeAttributeString("xml:lang", alt.getTargetLocale().toBCP47());
			if ( params.getIncludeIts() ) {
				writeAnnotatorsRefIfNeeded();
				StringBuilder tmp = new StringBuilder();
				itsContForAltTrgCont.outputAnnotations(alt.getTarget().getAnnotation(GenericAnnotations.class), tmp, false, false, false, null);
				writer.appendRawXML(tmp.toString());
			}
			writer.writeRawXML(xliffCont.toSegmentedString(alt.getTarget(), 0, params.getEscapeGt(), false, params.getPlaceholderMode(),
				params.getIncludeCodeAttrs(), params.getIncludeIts(), trgLoc));
			writer.writeEndElementLineBreak(); // target
			annotatorsRef.pop();
			
			writer.writeEndElementLineBreak(); // alt-trans
		}
	}

	@Override
	public void cancel () {
		// Nothing for now
	}

	@Override
	public EncoderManager getEncoderManager () {
		// None
		return null;
	}

	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return null;
	}

	@Override
	public String getName () {
		return getClass().getName();
	}

	@Override
	public XLIFFWriterParameters getParameters () {
		return params;
	}

	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			processStartDocument((StartDocument)event.getResource());
			break;
		case END_DOCUMENT:
			processEndDocument();
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
			// Do nothing
			break;
		}
		return event;
	}

	@Override
	public void setOptions (LocaleId locale,
		String defaultEncoding)
	{
		trgLoc = locale;
		// Ignore encoding as we always use UTF-8
	}

	@Override
	public void setOutput (String path) {
		fwOutputPath = path;
	}

	@Override
	public void setOutput (OutputStream output) {
	      outputStream = output;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (XLIFFWriterParameters)params;
	}

	// Use for IFilterWriter mode
	private void processStartDocument (StartDocument resource) {
		// trgLoc was set before
		// fwOutputPath was set before
		create(fwOutputPath, null, resource.getLocale(), trgLoc, resource.getMimeType(), resource.getName(), null);

		// Additional variables specific to IFilterWriter mode
		fwInputEncoding = resource.getEncoding();
		IParameters params = resource.getFilterParameters();
		if ( params == null ) fwConfigId = null;
		else fwConfigId = params.getPath();
	}

	// Use for IFilterWriter mode
	private void processEndDocument () {
		// All is done in close() (used by both modes)
		close();
	}

	// Use for IFilterWriter mode
	private void processStartSubDocument (StartSubDocument resource) {
		writeStartFile(resource.getName(), resource.getMimeType(), null,
			fwConfigId, fwInputEncoding, null);		
	}

	// Use for IFilterWriter mode
	private void processEndSubDocument (Ending resource) {
		writeEndFile();
	}

	// Use for IFilterWriter mode
	private void processStartGroup (StartGroup resource) {
		writeStartGroup(resource);
	}

	// Use for IFilterWriter mode
	private void processEndGroup (Ending resource) {
		writer.writeEndElementLineBreak(); // group
	}

	// Use for IFilterWriter mode
	private void processTextUnit (ITextUnit tu) {
		writeTextUnit(tu, null);
	}

	private String writeStandoffLQI (List<GenericAnnotations> list) {
		if ( Util.isEmpty(list) ) return "";
		if ( itsContForUnit == null ) {
			itsContForUnit = new ITSContent(xliffCont.getCharsetEncoder(), false, true);
		}
		return itsContForUnit.writeStandoffLQI(list);
	}

	private void writeAnnotatorsRefIfNeeded () {
		if ( needAnnotatorsRef && params.getIncludeIts() ) {
			writer.writeAttributeString(Namespaces.ITS_NS_PREFIX+":annotatorsRef", annotatorsRef.peek());
			needAnnotatorsRef = false;
		}
	}

	/*
	 * Write general size properties (maxwidth, maxheight, minwidth, minheight, size-unit) where they are expected.
	 * @param writer xliff writer
	 * @param resource data source
	 */
	private static void writeSizeProperties(XMLWriter writer, INameable resource) {
		boolean sizeWritten = false;

		if (resource.hasProperty(Property.MAX_WIDTH)) {
			writer.writeAttributeString("maxwidth", resource.getProperty(Property.MAX_WIDTH).getValue());
			sizeWritten = true;
		}
		if (resource.hasProperty(Property.MAX_HEIGHT)) {
			writer.writeAttributeString("maxheight", resource.getProperty(Property.MAX_HEIGHT).getValue());
			sizeWritten = true;
		}

		if (sizeWritten) {
			if (resource.hasProperty(Property.SIZE_UNIT)) {
				writer.writeAttributeString("size-unit", resource.getProperty(Property.SIZE_UNIT).getValue());
			} else {
				//default
				writer.writeAttributeString("size-unit", "pixel");
			}
		}
	}

	/**
	 * Pushes a new level of annotators references and base the new one on the given values.
	 * @param values the set of new values with which to update the new level. Can be null to inherit
	 * all the previous level's values.
	 */
	private void pushAnnotatorsRef (String values) {
		annotatorsRef.push(annotatorsRef.peek());
		setAnnotatorsRef(values);
	}
	
	/**
	 * Updates the current ITS annotators references based on a given set of values.
	 * @param values the annotators references values to use to update the existing ones.
	 * The annotation will be output in the next element that can carry it.
	 */
	public void setAnnotatorsRef (String values) {
		// Push the new values of the annotators based on possible annotation at the TU level
		String oldValues = annotatorsRef.pop();
		String newValues = ITSContent.updateAnnotatorsRef(oldValues, values);
		annotatorsRef.push(newValues);

		// Establish if the updated values have changed (only if it's no needed yet)
		if ( !needAnnotatorsRef ) {
			if ( oldValues == null ) {
				needAnnotatorsRef = (newValues != null);
			}
			else {
				if ( newValues != null ) {
					needAnnotatorsRef = !oldValues.equals(newValues);
				}
				else {
					needAnnotatorsRef = true;
				}
			}
		}
	}

	public XLIFFContent getXLIFFContent() {
		return xliffCont;
	}

}
