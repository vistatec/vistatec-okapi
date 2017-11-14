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

package net.sf.okapi.filters.ttx;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class TTXFilter implements IFilter {

	public final static String DFSTART_TYPE = "x-df-s";
	public final static String DFEND_TYPE = "x-df-e";
	
	private final static String MATCHPERCENT = "MatchPercent";
	private final static String ORIGIN = "Origin";
	
	// Characters no considered as 'text' in TTX (for un-segmented entries)
	private final static String TTXNOTEXTCHARS = "\u00a0~`!@#$%^&*()_+=-{[}]|\\:;\"'<,>.?/\u2022\u2013";
	
	private final static String TARGETLANGUAGE_ATTR = "TargetLanguage";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private boolean hasNext;
	private XMLStreamReader reader;
	private RawDocument input;
	private String docName;
	private int tuId;
	private IdGenerator otherId;
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private String srcLangCode;
	private String trgLangCode;
	private String trgDefFont;
	private LinkedList<Event> queue;
	private boolean canceled;
	private GenericSkeleton skel;
	private ITextUnit tu;
	private Parameters params;
	//private boolean sourceDone;
	//private boolean targetDone;
	private String encoding;
	private String lineBreak;
	private boolean hasUTF8BOM;
	private StringBuilder buffer;
//	private boolean useDF;
	private boolean insideContent;
	private TTXSkeletonWriter skelWriter;
	private EncoderManager encoderManager;
	private boolean includeUnsegmentedParts;

	private int numberOfOpenedInternalTags;

	public TTXFilter () {
		params = new Parameters();
	}
	
	public void cancel () {
		canceled = true;
	}

	public void close () {
		if ( input != null ) {
			input.close();
			input = null;
		}
		try {
			if ( reader != null ) {
				reader.close();
				reader = null;
			}			
			hasNext = false;
		}
		catch ( XMLStreamException e) {
			throw new OkapiIOException(e);
		}
	}

	public String getName () {
		return "okf_ttx";
	}

	public String getDisplayName () {
		return "TTX Filter";
	}

	public String getMimeType () {
		return MimeTypeMapper.TTX_MIME_TYPE;
	}

	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MimeTypeMapper.TTX_MIME_TYPE,
			getClass().getName(),
			"TTX",
			"Configuration for Trados TTX documents.",
			null,
			".ttx;"));
		return list;
	}
	
	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.TTX_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		}
		return encoderManager;
	}
	
	public Parameters getParameters () {
		return params;
	}

	public boolean hasNext () {
		return hasNext;
	}

	public Event next () {
		try {
			// Check for cancellation first
			if ( canceled ) {
				queue.clear();
				queue.add(new Event(EventType.CANCELED));
				hasNext = false;
			}
			
			// Parse next if nothing in the queue
			if ( queue.isEmpty() ) {
				if ( !read() ) {
					Ending ending = new Ending(otherId.createId());
					ending.setSkeleton(skel);
					queue.add(new Event(EventType.END_DOCUMENT, ending));
				}
			}
			
			// Return the head of the queue
			if ( queue.peek().getEventType() == EventType.END_DOCUMENT ) {
				hasNext = false;
			}
			return queue.poll();
		}
		catch ( XMLStreamException e ) {
			throw new OkapiIOException(e);
		}
	}

	public void open (RawDocument input) {
		open(input, true);
	}
	
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		try {
			canceled = false;
			this.input = input;

			XMLInputFactory fact = XMLInputFactory.newInstance();
			fact.setProperty(XMLInputFactory.IS_COALESCING, true);
			
			//fact.setXMLResolver(new DefaultXMLResolver());
			//TODO: Resolve the re-construction of the DTD, for now just skip it
			// security concern. Turn off DTD processing
			// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing			
			fact.setProperty(XMLInputFactory.SUPPORT_DTD, false);

			// Determine encoding based on BOM, if any
			input.setEncoding("UTF-8"); // Default for XML, other should be auto-detected
			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
			detector.detectBom();

			if ( params.getSegmentMode() == Parameters.MODE_AUTO ) {
				String enc = input.getEncoding();
				if ( detector.isAutodetected() ) {
					enc = detector.getEncoding();
				}
				// If we detect a segment we assume we should extract pre-segmented content only
				includeUnsegmentedParts = !doesFileContainSegment(input.getInputURI(), enc);
			}
			else {
				includeUnsegmentedParts = (params.getSegmentMode() == Parameters.MODE_ALL);
			}
			
			if ( detector.isAutodetected() ) {
				reader = fact.createXMLStreamReader(input.getStream(), detector.getEncoding());
			}
			else {
				reader = fact.createXMLStreamReader(input.getStream());
			}

			String realEnc = reader.getCharacterEncodingScheme();
			if ( realEnc != null ) encoding = realEnc;
			else encoding = input.getEncoding();

			// Set the language codes for the skeleton writer
			if ( skelWriter == null ) {
				skelWriter = new TTXSkeletonWriter();
			}

			srcLoc = input.getSourceLocale();
			if ( srcLoc == null ) throw new NullPointerException("Source language not set.");
			srcLangCode = srcLoc.toString().toUpperCase();
			skelWriter.setSourceLanguageCode(srcLangCode);
			
			trgLoc = input.getTargetLocale();
			if ( trgLoc == null ) throw new NullPointerException("Target language not set.");
			trgLangCode = trgLoc.toString().toUpperCase(); // Default to create new entries
			skelWriter.setTargetLanguageCode(trgLangCode);
			
			hasUTF8BOM = detector.hasUtf8Bom();
			lineBreak = detector.getNewlineType().toString();
			if ( input.getInputURI() != null ) {
				docName = input.getInputURI().getPath();
			}

			insideContent = false;
			tuId = 0;
			otherId = new IdGenerator(null, "o");
			// Set the start event
			hasNext = true;
			queue = new LinkedList<Event>();
			buffer = new StringBuilder();
			trgDefFont = null;

//			useDF = false;
//			// By default, for now, use DF for CJK only
//			if ( trgLoc.sameLanguageAs("ko")
//				|| trgLoc.sameLanguageAs("zh")
//				|| trgLoc.sameLanguageAs("ja") ) {
//				useDF = true;
//			}
			
			StartDocument startDoc = new StartDocument(otherId.createId());
			startDoc.setName(docName);
			startDoc.setEncoding(encoding, hasUTF8BOM);
			startDoc.setLocale(srcLoc);
			startDoc.setFilterParameters(getParameters());
			startDoc.setFilterWriter(createFilterWriter());
			startDoc.setType(MimeTypeMapper.TTX_MIME_TYPE);
			startDoc.setMimeType(MimeTypeMapper.TTX_MIME_TYPE);
			startDoc.setMultilingual(true);
			startDoc.setLineBreak(lineBreak);
			queue.add(new Event(EventType.START_DOCUMENT, startDoc));
			
			// load simplifier rules and send as an event
			if (!Util.isEmpty(((Parameters)getParameters()).getSimplifierRules())) {			
				Event cs = FilterUtil.createCodeSimplifierEvent(((Parameters)getParameters()).getSimplifierRules());
				queue.add(cs);
			}	

			// The XML declaration is not reported by the parser, so we need to
			// create it as a document part when starting
			skel = new GenericSkeleton();
			startDoc.setProperty(new Property(Property.ENCODING, encoding, false));
			skel.append("<?xml version=\"1.0\" encoding=\"");
			skel.addValuePlaceholder(startDoc, Property.ENCODING, LocaleId.EMPTY);
			skel.append("\"?>");
			startDoc.setSkeleton(skel);
			// Set encoder manager and update active encoder for the internal skeleton writer
			skelWriter.processStartDocument(trgLoc, encoding, null, encoderManager, startDoc);
		}
		catch ( XMLStreamException e) {
			throw new OkapiIOException(e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException(e);
		}
	}
	
	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	public ISkeletonWriter createSkeletonWriter() {
		if ( skelWriter == null ) {
			skelWriter = new TTXSkeletonWriter();
		}
		return skelWriter;
	}

	public IFilterWriter createFilterWriter () {
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	private boolean whitespacesOnly (String text) {
		for ( int i=0; i<text.length(); i++ ) {
			if ( !Character.isWhitespace(text.charAt(i)) ) return false;
		}
		return true;
	}
	
	private boolean read () throws XMLStreamException {
		try {
			skel = new GenericSkeleton();
			buffer.setLength(0);
			while ( true ) {
				switch ( reader.getEventType() ) {
				case XMLStreamConstants.START_ELEMENT:
					String name = reader.getLocalName();
					if ( "Tu".equals(name) || "ut".equals(name) || "df".equals(name) ) {
						if ( processTextUnit(name) ) return true;
						// We may return on an end-tag (e.g. Raw), so check for it
						if ( reader.getEventType() == XMLStreamConstants.START_ELEMENT ) { 
							buildStartElement(true);
							// The element at the exit may be different than at the call
							// so we refresh the name here to store the correct ending
							name = reader.getLocalName(); 
							storeUntilEndElement(name);
						}
						continue; // reader.next() was called
					}
					else if ( "UserSettings".equals(name) ){
						processUserSettings();
					}
					else if ( "Raw".equals(name) ) {
						insideContent = true;
						buildStartElement(true);
					}
					else {
						buildStartElement(true);
					}
					break;
					
				case XMLStreamConstants.END_ELEMENT:
					buildEndElement(true);
					break;
					
				case XMLStreamConstants.SPACE: // Non-significant spaces
					skel.append(reader.getText().replace("\n", lineBreak));
					break;
	
				case XMLStreamConstants.CHARACTERS:
				case XMLStreamConstants.CDATA:
					if ( insideContent && !whitespacesOnly(reader.getText()) ) {
						if ( processTextUnit(null) ) return true;
						continue; // next() was called
					}
					else {
						skel.append(Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0, true, null));
					}
					break;
					
				case XMLStreamConstants.COMMENT:
					skel.append("<!--"+ reader.getText().replace("\n", lineBreak) + "-->");
					break;
					
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					skel.append("<?"+ reader.getPITarget() + " " + reader.getPIData() + "?>");
					break;
					
				case XMLStreamConstants.DTD:
					//TODO: Reconstruct the DTD declaration
					// but how? nothing is available to do that
					break;
					
				case XMLStreamConstants.ENTITY_REFERENCE:
				case XMLStreamConstants.ENTITY_DECLARATION:
				case XMLStreamConstants.NAMESPACE:
				case XMLStreamConstants.NOTATION_DECLARATION:
				case XMLStreamConstants.ATTRIBUTE:
					break;
				case XMLStreamConstants.START_DOCUMENT:
					break;
				case XMLStreamConstants.END_DOCUMENT:
					break;
				}
				
				if ( reader.hasNext() ) reader.next();
				else return false;
			}
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error reading TTX.\n"+e.getMessage(), e);
		}
	}

	/* A text unit starts with either non-whitespace text, internal ut, df, or Tu.
	 * It ends with end of Raw, external ut, or end of df not corresponding to 
	 * a df included in the text unit. Tuv elements are segments.
	 */
	// Returns true if it is a text unit we need to return now
	private boolean processTextUnit (String startTag) {
		try {
			// Send any previous tag as document part
			createDocumentPartIfNeeded();

			// Initialize variable for this text unit
			boolean inTarget = false;
			tu = new TextUnit(null); // No id yet
			TextContainer srcCont = tu.getSource();
			ArrayList<TextFragment> trgFragments = new ArrayList<TextFragment>();
			ArrayList<AltTranslation> altTranslations = new ArrayList<AltTranslation>();
			TextFragment srcSegFrag = null;
			TextFragment trgSegFrag = null;
			AltTranslation altTrans = null;
			TextFragment inter = new TextFragment();
			TextFragment current = inter;
			boolean returnValueAfterTextUnitDone = true;
			StringBuilder movedCodes = new StringBuilder();

			String tmp;
			String disp;
			String name;
			boolean moveToNext = false;
			int dfCount = 0;
			boolean changeFirst = false;
			boolean done = false;
			boolean inTU = false;
			
			String lastDFOpen = "";
			String crumbs = ""; // to keep track of open.close of df elements
			boolean hasOriginalSeg = false; // True if the content has TTX segments

			numberOfOpenedInternalTags = 0;

			while ( !done ) {
				// Move to next event if required 
				if ( moveToNext ) reader.next();
				else moveToNext = true;
				
				// Process the event
				switch ( reader.getEventType() ) {
				
				case XMLStreamConstants.CHARACTERS:
				case XMLStreamConstants.CDATA:
				case XMLStreamConstants.SPACE:
					current.append(reader.getText());
					break;
					
				case XMLStreamConstants.START_ELEMENT:
					name = reader.getLocalName();
					if ( !inTU && name.equals("ut") ) {
						if ( !isInline(name) && !isConvertibleToInline() ) { // Non-inline ut
							done = true;
							returnValueAfterTextUnitDone = false;
							continue;
						}
					}
					else if ( name.equals("Tu") ) { // New segment
						// Start new segment
						inTU = true;
						inTarget = false;
						srcSegFrag = new TextFragment();
						trgSegFrag = null;
						altTrans = null;
						if ( !inter.isEmpty() ) { // Deal with previous text span
							if ( includeUnsegmentedParts && hasText(inter.getCodedText()) ) {
								// Unsegmented section contain text: make it a text unit
								addSegment(inter, srcCont, trgFragments, altTranslations, dfCount, crumbs, movedCodes);
							}
							else {
								changeFirst = srcCont.isEmpty();
								srcCont.append(inter);
							}
							inter = null;
						}
						current = srcSegFrag;
						// Get Tu info
						tmp = reader.getAttributeValue(null, MATCHPERCENT);
						String origin = reader.getAttributeValue(null, ORIGIN);
						if (( tmp != null ) || ( origin != null )) {
							int value = 0;
							if ( tmp != null ) {
								try {
									value = Integer.valueOf(tmp);
								}
								catch ( Throwable e ) {
									logger.warn("Unexpected value in {} attribute ({})", MATCHPERCENT, tmp);
								}
							}
							if ( value > 0 ) {
								MatchType matchType = MatchType.FUZZY;
								if (( value > 100 ) && (( origin != null ) && origin.equalsIgnoreCase("xtranslate") )) {
									// case of the "XU" tags
									matchType = MatchType.EXACT_LOCAL_CONTEXT;
								}
								else if ( value > 99 ) {
									matchType = MatchType.EXACT;
								}
								altTrans = new AltTranslation(srcLoc, trgLoc, null, null, null, matchType,
									value, ((origin==null) ? AltTranslation.ORIGIN_SOURCEDOC : origin), value, QueryResult.QUALITY_UNDEFINED);
							}
						}
						continue;
					}
					else if ( name.equals("Tuv") ) { // New language content
						hasOriginalSeg = true;
						tmp = reader.getAttributeValue(null, "Lang");
						if ( tmp != null ) {
							inTarget = trgLoc.equals(tmp);
						}
						else { // Just in case we don't have Lang
							logger.warn("Attribute Lang is missing in Tuv (after text unit '{}')", tuId);
							inTarget = !inTarget;
						}
						if ( inTarget ) {
							// Get start on target
							trgSegFrag = new TextFragment();
							current = trgSegFrag;
						}
						// Else: source is already set
						continue;
					}
					else if ( name.equals("df") ) {
						// We have to use placeholder for df because they don't match ut nesting order
						dfCount++;
						crumbs += "o";
						Code code = current.append(TagType.PLACEHOLDER, DFSTART_TYPE, "", -1);
						lastDFOpen = buildStartElement(false);
						code.setOuterData(lastDFOpen);
						continue;
					}
					// Inline to include in this segment
					TagType tagType = TagType.PLACEHOLDER;
					String type = "ph";
					int idToUse = -1;
					disp = reader.getAttributeValue(null, "DisplayText");
					tmp = reader.getAttributeValue(null, "Type");
					if ( tmp != null ) {
						if ( tmp.equals("start") ) {
							String leftEdge = reader.getAttributeValue(null, "LeftEdge");
							if (( leftEdge != null ) && leftEdge.equals("split") ) {
								// Closing part of a split opening tag: treat it as placeholder
							}
							else { // Normal start tag
								tagType = TagType.OPENING;
								type = ((disp != null) ? disp : "Xpt");
								numberOfOpenedInternalTags++;
							}
						}
						else if ( tmp.equals("end") ) {
							tagType = TagType.CLOSING;
							type = ((disp != null) ? disp : "Xpt");
							if (0 < numberOfOpenedInternalTags) {
								numberOfOpenedInternalTags--;
							}
						}
					}
					appendCode(tagType, idToUse, name, type, false, current);
					break;

				case XMLStreamConstants.END_ELEMENT:
					name = reader.getLocalName();
					if ( name.equals("Raw") ) { // End of document
						done = true;
					}
					else if ( name.equals("Body") ) { // End of document
						done = true;
					}
					else if ( name.equals("df") ) {
						// We have to use placeholder for df because they don't match ut nesting order
						dfCount--;
						crumbs += "c";
						Code code = current.append(TagType.PLACEHOLDER, DFEND_TYPE, "", -1); //(inTarget ? ++trgId : ++srcId));
						code.setOuterData(buildEndElement(false));
						continue;
					}
					// Possible end of segment
					if ( done || name.equals("Tu") ) {
						if ( srcSegFrag != null ) { // Add the segment if we have one
							srcCont.getSegments().append(srcSegFrag);
							// Change first part to non-segment if needed
							if ( changeFirst ) {
								srcCont.changePart(0);
								changeFirst = false;
							}

							// If the target is not there, we copy the source instead
							// TTX should not have source-only TU
							// why??? trgFragments.add((trgSegFrag==null) ? srcSegFrag.clone() : trgSegFrag);
							
							trgFragments.add((trgSegFrag==null) ? new TextFragment() : trgSegFrag);
							// Set the alt-trans target if we had an alt-trans
							if ( altTrans != null ) {
								// Use target or source if target is not available (rare case)
								// Falling back to source is not great but it's how the TTX file is, and this allows to preserve score, etc.
								altTrans.setTarget(trgLoc, trgFragments.get(trgFragments.size()-1));
							}
							altTranslations.add(altTrans);
							srcSegFrag = null;
							trgSegFrag = null;
							altTrans = null;
							inter = new TextFragment();
							current = inter; // Start storing inter-segment part
							// A Tu stops the current segment, but not the text unit
						}
						else if (( inter != null ) && !inter.isEmpty() ) { // If no source segment: only content
							if ( includeUnsegmentedParts && hasText(inter.getCodedText()) ) {
								// Unsegmented section contain text: make it a text unit
								addSegment(inter, srcCont, trgFragments, altTranslations, dfCount, crumbs, movedCodes);
							}
							else {
								srcCont.append(current);
								srcSegFrag = null;
								trgSegFrag = null;
							}
							inter = new TextFragment();
							current = inter; // Start storing inter-segment part
						}
						inTU = false;
						continue; // Stop here
					}
					break;
				}
			}

			// Check if we had only non-segmented text
			if (( inter != null) && !inter.isEmpty() ) {
				String ctext = inter.getCodedText();
				if ( includeUnsegmentedParts && hasText(ctext) ) {
					// Unsegmented section contain text: make it a text unit

					// Move leading whitespace characters to outside
					int n = TextFragment.indexOfFirstNonWhitespace(ctext, 0, -1, false, false, false, true);
					if ( n > 0 ) {
						if ( srcCont.isEmpty() ) {
							// Move to the skeleton if we are at the first segment
							skel.add(ctext.substring(0, n));
						}
						else { // Move to a part before the segment
							srcCont.append(new TextPart(ctext.substring(0, n)));
						}
						ctext = ctext.substring(n);
						inter.setCodedText(ctext);
					}

					// Put the text after in a segment
					addSegment(inter, srcCont, trgFragments, altTranslations, dfCount, crumbs, movedCodes);
					inter = null;
				}
				else {
					srcCont.append(inter);
				}
			}
			
			// Check if this it is worth sending as text unit
			boolean changeToSkel = !hasText(srcCont); // Use special hasText()
			// And make sure text fragments un-segments are skeleton if they are not to be extracted
			// This last check is because we always have at least one segment in a TC.
			if ( !hasOriginalSeg && !includeUnsegmentedParts ) {
				changeToSkel = true;
			}
			if ( changeToSkel ) { 
				// No text-type characters
				if ( skelWriter == null ) {
					skelWriter = new TTXSkeletonWriter();
				}
				skelWriter.checkForFilterInternalUse(lineBreak);
				// Not really a text unit: convert to skeleton
				// Use the skeleton writer processFragment() to get the output
				// so any outer data is generated.
				if ( srcCont.contentIsOneSegment() ) {
					skel.append(skelWriter.processFragment(srcCont.getFirstContent(), EncoderContext.SKELETON));
				}
				else { // Merge all if there is more than one segment
					skel.append(skelWriter.processFragment(srcCont.getUnSegmentedContentCopy(), EncoderContext.SKELETON));
				}
				tu = null;
				return false; // No return from filter
			}
			
			// Else genuine text unit, finalize and send
			String toMoveAfter = "";
			if ( srcCont.hasBeenSegmented() ) {

				// Renumber the source on the whole content, then re-segment
				TextContainer tc = tu.getSource();
				List<Range> ranges = tc.getSegments().getRanges();
				tc.joinAll();
				tc.getFirstContent().renumberCodes();
				tc.getSegments().create(ranges, true);
				
				// Create the target content using the source as the base
				TextContainer cont = srcCont.clone();
				int i = 0;
				for ( Segment seg : cont.getSegments() ) {
					seg.text = trgFragments.get(i);
					i++;
				}
				tu.setTarget(trgLoc, cont);
				
				// Renumber the target like the source
				tc = tu.getTarget(trgLoc);
				ranges = tc.getSegments().getRanges();
				tc.joinAll();
				tc.getFirstContent().renumberCodes();
				tc.getSegments().create(ranges, true);
				// We expect the code to be aligned in the TTX, so no need to re-align them
				
				// Set the annotations
				i = 0;
				for ( Segment seg : tc.getSegments() ) {
					AltTranslation altTmp = altTranslations.get(i);
					if ( altTmp != null ) {
						AltTranslationsAnnotation ann = new AltTranslationsAnnotation();
						ann.add(altTmp);
						seg.setAnnotation(ann);
					}
					i++;
				}
			}
			else { // We assume pre-segmented entry don't have the overlapping DF problem.
				// If they are un-segmented they may, and we try to fix it here:
				if (( dfCount < 0 ) || crumbs.startsWith("c") ) { // Extra </df> in content
					// Process the last segment
					toMoveAfter = moveDFCodesToString(srcCont.getSegments().getLastContent(), dfCount, crumbs);
				}
			}
			
			tu.setId(String.valueOf(++tuId));
			skel.addContentPlaceholder(tu); // Used by the TTXFilterWriter
			tu.setSkeleton(skel);
			tu.setPreserveWhitespaces(true);
			tu.setMimeType(MimeTypeMapper.TTX_MIME_TYPE);
			queue.add(new Event(EventType.TEXT_UNIT, tu));
			
			// For next event (as document part if not empty)
			skel = new GenericSkeleton();
			if ( movedCodes.length() > 0 ) {
				skel.add(movedCodes.toString());
			}
			if ( !Util.isEmpty(toMoveAfter) ) {
				skel.add(toMoveAfter);
			}
			
			return returnValueAfterTextUnitDone;
		}
		catch ( XMLStreamException e) {
			throw new OkapiIOException("Error processing top-level ut element.\n"+e.getMessage(), e);
		}
	}

	private void addSegment (TextFragment frag,
		TextContainer srcCont,
		ArrayList<TextFragment> trgSegments,
		ArrayList<AltTranslation> altTranslations,
		int dfCount,
		String crumbs,
		StringBuilder movedCodes)
	{
		// For next event (as document part if not empty)
		if (( dfCount < 0 ) || crumbs.startsWith("c") ) { // Extra </df> in content
			movedCodes.append(moveDFCodesToString(frag, dfCount, crumbs));
		}
		
		srcCont.append(new Segment(null, frag));
		trgSegments.add(new TextFragment());
		altTranslations.add(null);
	}
	
	/**
	 * Moves the trailing DF codes to a string until dfCount is 0.
	 * The container must not be segmented yet.
	 * @param tc the container to update.
	 * @param dfCount the df counter (-N:more closes, +N:more open)
	 * @param crumbs the track of open and close for df.
	 * @return the string with removed codes.
	 */
	private String moveDFCodesToString (TextFragment tf,
		int dfCount,
		String crumbs)
	{
		String tmp = "";
		StringBuilder ctext = new StringBuilder(tf.getCodedText());
		List<Code> codes = tf.getCodes();

		// If starts with close tags: move them until we reach an open
		// Calculate the number of close tags at the front to move after the TU.
		int count = 0;
		for ( int i=0; i<crumbs.length(); i++ ) {
			if ( crumbs.charAt(i) == 'c' ) count++;
			else break;
		}
		// Do the moving
		if ( count > 0 ) {
			for ( int i=0; i<ctext.length(); i++ ) {
				if ( TextFragment.isMarker(ctext.charAt(i)) ) {
					Code code = codes.get(TextFragment.toIndex(ctext.charAt(i+1)));
					if ( code.getType().equals(DFEND_TYPE) ) {
						// Copy the code data at the front of the skeleton string
						tmp += code.getOuterData(); // Same order as in TU
						// Remove the code from the fragment
						tf.remove(i, i+2);
						// Get the coded text again 
						ctext.setLength(0);
						ctext.append(tf.getCodedText());
						dfCount++;
						if ( --count == 0 ) break; // Done
					}
				}
			}
			tf.renumberCodes();
		}
		
		if ( dfCount >= 0 ) return tmp;
		
		for ( int i=ctext.length()-1; i>=0; i-- ) {
			if ( TextFragment.isMarker(ctext.charAt(i)) ) {
				Code code = codes.get(TextFragment.toIndex(ctext.charAt(i+1)));
				if (( code.getType() != null ) && code.getType().equals(DFEND_TYPE) ) {
					// Copy the code data at the front of the skeleton string
					tmp = code.getOuterData() + tmp;
					// Remove the code from the fragment
					tf.remove(i, i+2);
					// Get the coded text again 
					ctext.setLength(0);
					ctext.append(tf.getCodedText());
					if ( ++dfCount == 0 ) break;
				}
			}
		}
		return tmp;
	}
//Backup code Dec-26-2010 8pm
//	private String moveDFCodesToString (TextContainer tc,
//		int dfCount,
//		String crumbs)
//	{
//		if ( dfCount >= 0 ) return "";
//		String tmp = "";
//		TextFragment tf = tc.get(0).text; 
//		StringBuilder ctext = new StringBuilder(tf.getCodedText());
//		List<Code> codes = tc.getFirstContent().getCodes();
//		
//		for ( int i=ctext.length()-1; i>=0; i-- ) {
//			if ( TextFragment.isMarker(ctext.charAt(i)) ) {
//				Code code = codes.get(TextFragment.toIndex(ctext.charAt(i+1)));
//				if (( code.getType() != null ) && code.getType().equals(DFEND_TYPE) ) {
//					// Copy the code data at the front of the skeleton string
//					tmp = code.getOuterData() + tmp;
//					// Remove the code from the fragment
//					tf.remove(i, i+2);
//					// Get the coded text again 
//					ctext.setLength(0);
//					ctext.append(tf.getCodedText());
//					if ( ++dfCount == 0 ) break;
//				}
//			}
//		}
//		return tmp;
//	}
	
	private boolean hasText (TextContainer tc) {
		for ( TextPart part : tc ) {
			if ( hasText(part.getContent().getCodedText()) ) {
				return true;
			}
		}
		return false;
	}

	private boolean hasText (String codedText) {
		for ( int i=0; i<codedText.length(); i++ ) {
			if ( TextFragment.isMarker(codedText.charAt(i)) ) {
				i++; // Skip index
				continue;
			}
			// Not a marker: test the type of character
			if ( !Character.isWhitespace(codedText.charAt(i)) ) {
				// Extra TTX-no-text specific checks
				if ( TTXNOTEXTCHARS.indexOf(codedText.charAt(i)) == -1 ) {
					// Not a non-white-space that is not a TTX-no-text: that's text
					return true;
				}
			}
		}
		return false;
	}

	private String buildStartElement (boolean store) {
		StringBuilder tmp = new StringBuilder();
		String prefix = reader.getPrefix();
		if (( prefix == null ) || ( prefix.length()==0 )) {
			tmp.append("<"+reader.getLocalName());
		}
		else {
			tmp.append("<"+prefix+":"+reader.getLocalName());
		}

		int count = reader.getNamespaceCount();
		for ( int i=0; i<count; i++ ) {
			prefix = reader.getNamespacePrefix(i);
			tmp.append(String.format(" xmlns%s=\"%s\"",
				((prefix!=null) ? ":"+prefix : ""),
				reader.getNamespaceURI(i)));
		}
		String attrName;
		
		count = reader.getAttributeCount();
		for ( int i=0; i<count; i++ ) {
			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
			prefix = reader.getAttributePrefix(i);
			attrName = String.format("%s%s",
				(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
				reader.getAttributeLocalName(i));
			// Test for target language place-holder
			if ( TARGETLANGUAGE_ATTR.equals(attrName) ) {
				tmp.append(" "+TARGETLANGUAGE_ATTR+"=\"");
				skel.append(tmp.toString());
				//TODO: replace direct write by property: skel.addValuePlaceholder(referent, TARGETLANGUAGE_ATTR, locId);
				skel.append(trgLangCode);
				tmp.setLength(0);
				tmp.append("\"");
			}
			else {
				tmp.append(String.format(" %s=\"%s\"", attrName,
					Util.escapeToXML(reader.getAttributeValue(i).replace("\n", lineBreak), 3, true, null)));
			}
		}
		tmp.append(">");
		if ( store ) skel.append(tmp.toString());
		return tmp.toString();
	}
	
	private String buildEndElement (boolean store) {
		StringBuilder tmp = new StringBuilder();
		String prefix = reader.getPrefix();
		if (( prefix != null ) && ( prefix.length()>0 )) {
			tmp.append("</"+prefix+":"+reader.getLocalName()+">");
		}
		else {
			tmp.append("</"+reader.getLocalName()+">");
		}
		if ( store ) skel.append(tmp.toString());
		return tmp.toString();
	}

	private void processUserSettings () {
		 if ( skelWriter == null ) {
			 skelWriter = new TTXSkeletonWriter();
		 }
		// Check source language
		String tmp = reader.getAttributeValue(null, "SourceLanguage");
		if ( !Util.isEmpty(tmp) ) {
			 if ( !srcLoc.equals(tmp) ) {
				 logger.warn("Specified source was '{}' but source language in the file is '{}'.\nUsing '{}'.",
					srcLoc.toString(), tmp, tmp);
				 srcLoc = LocaleId.fromString(tmp);
				 srcLangCode = tmp;
				 skelWriter.setSourceLanguageCode(srcLangCode);
			 }
		}

		// Check target language
		tmp = reader.getAttributeValue(null, TARGETLANGUAGE_ATTR);
		if ( !Util.isEmpty(tmp) ) {
			 if ( !trgLoc.equals(tmp) ) {
				 logger.warn("Specified target was '{}' but target language in the file is '{}'.\nUsing '{}'.",
					trgLoc.toString(), tmp, tmp);
				 trgLoc = LocaleId.fromString(tmp);
				 trgLangCode = tmp;
				 skelWriter.setTargetLanguageCode(trgLangCode);
			 }
		}
		if ( tmp != null ) {
			//TODO: set property for TargetLanguage
		}

		trgDefFont = reader.getAttributeValue(null, "TargetDefaultFont");
		if ( Util.isEmpty(trgDefFont) ) {
			trgDefFont = "Arial"; // Default
		}

		buildStartElement(true);
	}

	// Case of a UT element outside a TUV, that is an un-segmented/translate code.
//	private void processTopSpecialElement (String tagName) {
//		try {
//			boolean isInline = isInline(tagName);
//			if ( isInline ) {
//				// It's internal, and not in a TU/TUV yet
//				processNewTU();
//				// reader.next() has been called already 
//			}
//			else {
//				if ( tagName.equals("ut") ) { // UT that should not be inline
//					// Keep copying into the skeleton until end of element
//					storeStartElement();
//					storeUntilEndElement("ut"); // Includes the closing tag
//				}
//				else { // DF external
//					storeStartElement();
//					reader.next();
//				}
//			}
//		}
//		catch ( XMLStreamException e) {
//			throw new OkapiIOException("Error processing top-level ut element.", e);
//		}
//	}
	
	private void storeUntilEndElement (String name) throws XMLStreamException {
		int eventType;
		while ( reader.hasNext() ) {
			eventType = reader.next();
			switch ( eventType ) {
			case XMLStreamConstants.START_ELEMENT:
				buildStartElement(true);
				break;
			case XMLStreamConstants.END_ELEMENT:
				if ( name.equals(reader.getLocalName()) ) {
					buildEndElement(true);
					reader.next(); // Move forward
					return;
				}
				// Else: just store the end
				buildEndElement(true);
				break;
			case XMLStreamConstants.SPACE:
			case XMLStreamConstants.CDATA:
			case XMLStreamConstants.CHARACTERS:
				//TODO: escape unsupported chars
				skel.append(Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0, true, null));
				break;
			case XMLStreamConstants.COMMENT:
				//addTargetIfNeeded();
				skel.append("<!--"+ reader.getText().replace("\n", lineBreak) + "-->");
				break;
			case XMLStreamConstants.PROCESSING_INSTRUCTION:
				skel.append("<?"+ reader.getPITarget() + " " + reader.getPIData() + "?>");
				break;
			}
		}
	}

	private boolean isInline (String tagName) {
		if ( tagName.equals("df") ) {
			return true;
		}
		String tmp = reader.getAttributeValue(null, "Style");
		if ( tmp != null ) {
			return  !"external".equals(tmp);
		}
		else {
			// If no Style attribute: check for Class as some are indicator of external type.
			tmp = reader.getAttributeValue(null, "Class");
			if ( tmp != null ) {
				return !"procinstr".equals(tmp);
			}
		}
		return true; // Default is internal
	}

	private boolean isConvertibleToInline() {
		String type = reader.getAttributeValue(null, "Type");

		return null == type && 0 < numberOfOpenedInternalTags;
	}

	private void createDocumentPartIfNeeded () {
		// Make a document part with skeleton between the previous event and now.
		// Spaces can go with Tu to reduce the number of events.
		// This allows to have only the Tu skeleton parts with the TextUnit event
		if ( !skel.isEmpty(true) ) {
			DocumentPart dp = new DocumentPart(otherId.createId(), false, skel);
			queue.add(new Event(EventType.DOCUMENT_PART, dp));
			skel = new GenericSkeleton(); // And create a new skeleton for the next event
		}
	}
	
	/**
	 * Appends a code, using the content of the node. Do not use for <g>-type tags.
	 * @param tagType The type of in-line code.
	 * @param id the id of the code to add.
	 * @param tagName the tag name of the in-line element to process.
	 * @param type the type of code (bpt and ept must use the same one so they can match!) 
	 * @param store true if we need to store the data in the skeleton.
	 */
	private void appendCode (TagType tagType,
		int id,
		String tagName,
		String type,
		boolean store,
		TextFragment content)
	{
		try {
			int endStack = 1;
			StringBuilder innerCode = new StringBuilder();
			StringBuilder outerCode = null;
			outerCode = new StringBuilder();
			outerCode.append("<"+tagName);
			int count = reader.getAttributeCount();
			String prefix;
			for ( int i=0; i<count; i++ ) {
				if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
				prefix = reader.getAttributePrefix(i); 
				outerCode.append(String.format(" %s%s=\"%s\"",
					(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
					reader.getAttributeLocalName(i),
					Util.escapeToXML(reader.getAttributeValue(i).replace("\n", lineBreak), 3, true, null)));
			}
			outerCode.append(">");
			
			int eventType;
			while ( reader.hasNext() ) {
				eventType = reader.next();
				switch ( eventType ) {
				case XMLStreamConstants.START_ELEMENT:
					if ( store ) buildStartElement(store);
					StringBuilder tmpg = new StringBuilder();
					if ( tagName.equals(reader.getLocalName()) ) {
						endStack++; // Take embedded elements into account 
					}
					prefix = reader.getPrefix();
					if (( prefix == null ) || ( prefix.length()==0 )) {
						tmpg.append("<"+reader.getLocalName());
					}
					else {
						tmpg.append("<"+prefix+":"+reader.getLocalName());
					}
					count = reader.getNamespaceCount();
					for ( int i=0; i<count; i++ ) {
						prefix = reader.getNamespacePrefix(i);
						tmpg.append(String.format(" xmlns%s=\"%s\"",
							((prefix!=null) ? ":"+prefix : ""),
							reader.getNamespaceURI(i)));
					}
					count = reader.getAttributeCount();
					for ( int i=0; i<count; i++ ) {
						if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
						prefix = reader.getAttributePrefix(i); 
						tmpg.append(String.format(" %s%s=\"%s\"",
							(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
							reader.getAttributeLocalName(i),
							Util.escapeToXML(reader.getAttributeValue(i).replace("\n", lineBreak), 3, true, null)));
					}
					tmpg.append(">");
					innerCode.append(tmpg.toString());
					outerCode.append(tmpg.toString());
					break;
					
				case XMLStreamConstants.END_ELEMENT:
					if ( store ) buildEndElement(store);
					if ( tagName.equals(reader.getLocalName()) ) {
						if ( --endStack == 0 ) {
							Code code = content.append(tagType, type, innerCode.toString(), id);
							outerCode.append("</"+tagName+">");
							code.setOuterData(outerCode.toString());
							return;
						}
						// Else: fall thru
					}
					// Else store the close tag in the outer code
					prefix = reader.getPrefix();
					if (( prefix == null ) || ( prefix.length()==0 )) {
						innerCode.append("</"+reader.getLocalName()+">");
						outerCode.append("</"+reader.getLocalName()+">");
					}
					else {
						innerCode.append("</"+prefix+":"+reader.getLocalName()+">");
						outerCode.append("</"+prefix+":"+reader.getLocalName()+">");
					}
					break;

				case XMLStreamConstants.CHARACTERS:
				case XMLStreamConstants.CDATA:
				case XMLStreamConstants.SPACE:
					innerCode.append(reader.getText());//TODO: escape unsupported chars
					outerCode.append(Util.escapeToXML(reader.getText(), 0, true, null));
					if ( store ) //TODO: escape unsupported chars
						skel.append(Util.escapeToXML(reader.getText(), 0, true, null));
					break;
				}
			}
		}
		catch ( XMLStreamException e) {
			throw new OkapiIOException(e);
		}
	}

	private boolean doesFileContainSegment (URI inputURI,
		String encoding)
		throws IOException
	{
		if ( inputURI == null ) {
			throw new OkapiIOException("Cannot use the auto-detection of segments with strean or strings.");
		}
		// Just a basic search for "<Tu" (comments not taken into account as they are likely not there in TTX
		BufferedReader reader = null;
		try {
			int count = 0;
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputURI.getPath()), encoding));
			String line = reader.readLine();
			while ( line != null ) {
				count++;
				if ( line.indexOf("<Tuv ") > -1 ) {
					return true;
				}
				line = reader.readLine();
				if ( count > 5000 ) break; // Most likely not segmented
			}
		}
		finally {
			if ( reader != null ) {
				reader.close();
			}
		}
		return false;
	}
	
}
