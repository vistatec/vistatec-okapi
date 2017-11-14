/*===========================================================================
  Copyright (C) 2011-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.txml;

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
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.query.MatchType;
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
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class TXMLFilter implements IFilter {

	private final static String TARGETLOCALE = "targetlocale";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private boolean hasNext;
	private XMLStreamReader reader;
	private RawDocument input;
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private LinkedList<Event> queue;
	private boolean canceled;
	private GenericSkeleton skel;
	private String lineBreak;
	private ITextUnit tu;
	private IdGenerator otherId;
	private StringBuilder buffer;
	private TXMLSkeletonWriter skelWriter;
	private EncoderManager encoderManager;
	
	public TXMLFilter () {
		params = new Parameters();
	}
	
	@Override
	public void cancel () {
		canceled = true;
	}

	@Override
	public void close () {
		try {
			hasNext = false;
			if ( input != null ) {
				input.close();
				input = null;
			}
			
			if ( reader != null ) {
				reader.close();
				reader = null;
			}
		}
		catch ( XMLStreamException e) {
			throw new OkapiIOException(e);
		}
	}

	@Override
	public String getName () {
		return "okf_txml";
	}

	@Override
	public String getDisplayName () {
		return "TXML Filter";
	}

	@Override
	public String getMimeType () {
		return MimeTypeMapper.XML_MIME_TYPE;
	}

	@Override
	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MimeTypeMapper.XML_MIME_TYPE,
			getClass().getName(),
			"TXML",
			"Wordfast Pro TXML documents",
			null,
			".txml;"));
		list.add(new FilterConfiguration(getName()+"-fillEmptyTargets",
			MimeTypeMapper.XML_MIME_TYPE,
			getClass().getName(),
			"TXML (Fill empty targets in output)",
			"Wordfast Pro TXML documents with empty targets filled on output.",
			"fillEmptyTargets.fprm",
			".txml;"));
		return list;
	}
	
	@Override
	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.XML_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		}
		return encoderManager;
	}
	
	@Override
	public Parameters getParameters () {
		return params;
	}

	@Override
	public boolean hasNext () {
		return hasNext;
	}

	@Override
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
				read();
			}
			if ( queue.peek().getEventType() == EventType.END_DOCUMENT ) {
				hasNext = false;
			}
			return queue.poll();
		}
		catch ( XMLStreamException e ) {
			throw new OkapiIOException(e);
		}
	}

	@Override
	public void open (RawDocument input) {
		open(input, true);
	}
	
	@Override
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		try {
			canceled = false;
			this.input = input;

			XMLInputFactory fact = XMLInputFactory.newInstance();
			fact.setProperty(XMLInputFactory.IS_COALESCING, true);
			
			// security concern. Turn off DTD processing
			// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing			
			fact.setProperty(XMLInputFactory.SUPPORT_DTD, false);

			// Determine encoding based on BOM, if any
			input.setEncoding("UTF-8"); // Default for XML, other should be auto-detected
			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
			detector.detectBom();
			if ( detector.isAutodetected() ) {
				reader = fact.createXMLStreamReader(input.getStream(), detector.getEncoding());
			}
			else {
				reader = fact.createXMLStreamReader(input.getStream());
			}

			String realEnc = reader.getCharacterEncodingScheme();
			String encoding = input.getEncoding();
			if ( realEnc != null ) encoding = realEnc;

			srcLoc = input.getSourceLocale();
			if ( srcLoc == null ) throw new NullPointerException("Source language not set.");
			
			trgLoc = input.getTargetLocale();
			if ( trgLoc == null ) throw new NullPointerException("Target language not set.");

			otherId = new IdGenerator(null, "o");
			hasNext = true;
			queue = new LinkedList<Event>();
			buffer = new StringBuilder();
			
			StartDocument startDoc = new StartDocument(otherId.createId());
			startDoc.setEncoding(encoding, detector.hasUtf8Bom());
			lineBreak = detector.getNewlineType().toString();
			if ( input.getInputURI() != null ) {
				startDoc.setName(input.getInputURI().getPath());
			}
			startDoc.setLocale(srcLoc);
			startDoc.setFilterParameters(getParameters());
			startDoc.setFilterWriter(createFilterWriter());
			startDoc.setType(MimeTypeMapper.XML_MIME_TYPE);
			startDoc.setMimeType(MimeTypeMapper.XML_MIME_TYPE);
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
			skel.append("\"?>"+lineBreak);
			startDoc.setSkeleton(skel);
		}
		catch ( XMLStreamException e) {
			throw new OkapiIOException(e);
		}
	}
	
	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	public ISkeletonWriter createSkeletonWriter() {
		if ( skelWriter == null ) {
			skelWriter = new TXMLSkeletonWriter(params.getAllowEmptyOutputTarget());
		}
		return skelWriter;
	}

	@Override
	public IFilterWriter createFilterWriter () {
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	private void read () throws XMLStreamException {
		skel = new GenericSkeleton();
		buffer.setLength(0);

		while ( reader.hasNext() ) {
			int type = reader.next();
			switch ( type ) {
			case XMLStreamConstants.START_ELEMENT:
				String name = reader.getLocalName();
				if ( "translatable".equals(name) ) {
					processTranslatable();
					return;
				}
				else if ( "txml".equals(name) ){
					processTxml();
				}
				else if ( "localizable".equals(name) ) {
					buildStartElement(true);
					logger.warn("The <localizable> element is not supported yet: it will not be extracted.");
				}
				else {
					buildStartElement(true);
				}
				break;
				
			case XMLStreamConstants.END_ELEMENT:
				buildEndElement(true);
				if ( reader.getLocalName().equals("txml") ) { // End of document
					createDocumentPartIfNeeded();
				}
				break;
				
			case XMLStreamConstants.SPACE: // Non-significant spaces
				skel.append(reader.getText().replace("\n", lineBreak));
				break;

			case XMLStreamConstants.CHARACTERS:
			case XMLStreamConstants.CDATA:
				skel.append(Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0, true, null));
				break;
				
			case XMLStreamConstants.COMMENT:
				skel.append("<!--"+ reader.getText().replace("\n", lineBreak) + "-->");
				break;
				
			case XMLStreamConstants.PROCESSING_INSTRUCTION:
				skel.append("<?"+ reader.getPITarget() + " " + reader.getPIData() + "?>");
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
				Ending ending = new Ending(otherId.createId());
				ending.setSkeleton(skel);
				queue.add(new Event(EventType.END_DOCUMENT, ending));
				return;
			}
		}
	}

	private void processTxml () {
		// Check source language
		String tmp = reader.getAttributeValue(null, "locale");
		if ( !Util.isEmpty(tmp) ) {
			 if ( !srcLoc.equals(tmp) ) {
				 logger.warn("Specified source was '{}' but source language in the file is '{}'.\nUsing '{}'.",
					srcLoc.toString(), tmp, tmp);
				 srcLoc = LocaleId.fromString(tmp);
			 }
		}

		// Check target language
		tmp = reader.getAttributeValue(null, TARGETLOCALE);
		if ( !Util.isEmpty(tmp) ) {
			 if ( !trgLoc.equals(tmp) ) {
				 logger.warn("Specified target was '{}' but target language in the file is '{}'.\nUsing '{}'.",
					trgLoc.toString(), tmp, tmp);
				 trgLoc = LocaleId.fromString(tmp);
			 }
		}
		
		buildStartElement(true);
	}
	
	private void processTranslatable ()
		throws XMLStreamException
	{
		createDocumentPartIfNeeded();
		
		// Initialize variable for this text unit
		tu = new TextUnit(reader.getAttributeValue(null, "blockId")); // Use the blockId value

		buildStartElement(true);
//		skel.addContentPlaceholder(tu);
//		tu.setSkeleton(skel);
//		tu.setPreserveWhitespaces(true);
//		tu.setMimeType(MimeTypeMapper.XML_MIME_TYPE);
//		queue.add(new Event(EventType.TEXT_UNIT, tu));
		
		TextFragment tf;
		String segId = null;
		TextPart ws1 = null;
		Segment srcSeg = null;
		Segment trgSeg = null;
		TextPart ws2 = null;
		TextContainer srcCont = tu.getSource();
		TextContainer trgCont = tu.createTarget(trgLoc, true, IResource.CREATE_EMPTY);
		boolean hasOneTarget = false;
		boolean srcDone = false;
		String tmp;
		boolean gtmt = false;
		boolean modified = false;
		boolean inRevision = false;
		String comment = "";
		boolean firstPartIsComment = false;
		
		while ( reader.hasNext() ) {
			switch ( reader.next() ) {
			case XMLStreamConstants.START_ELEMENT:
				String name = reader.getLocalName();
				if ( "segment".equals(name) ) {
					segId = reader.getAttributeValue(null, "segmentId");
					tmp = reader.getAttributeValue(null, "modified");
					if ( !Util.isEmpty(tmp) ) {
						modified = tmp.equals("true");
					}
					tmp = reader.getAttributeValue(null, "gtmt");
					if ( !Util.isEmpty(tmp) ) {
						gtmt = tmp.equals("true");
					}
//					tmp = reader.getAttributeValue(null, "unconfirmed");
//					if ( !Util.isEmpty(tmp) ) {
//						contAnn.setUnconfirmed(tmp.equals("true"));
//					}
				}
				else if ( !inRevision && "source".equals(name) ) {
					tf = processContent(name);
					srcSeg = new Segment(segId, tf);
					srcDone = true;
				}
				else if ( !inRevision && "target".equals(name) ) {
					tf = processContent(name); // Use the same id as the source
					trgSeg = new Segment(segId, tf);
				}
				else if ( "ws".equals(name) ) {
					if ( srcDone ) ws2 = new TextPart(processContent(name));
					else ws1 = new TextPart(processContent(name));
				}
				else if ( "revisions".equals(name) ) {
					inRevision = true;
				}
				break;
				
			case XMLStreamConstants.END_ELEMENT:
				name = reader.getLocalName();
				if ( "segment".equals(name) ) {
					// Set first WS
					if ( ws1 != null ) {
						srcCont.append(ws1, srcCont.count()==1);
						if ( trgCont != null ) trgCont.append(ws1.clone(), trgCont.count()==1);
					}
					
					// Set content: source, then target if needed
					srcCont.getSegments().append(srcSeg, srcCont.count()==1);
					// Record the WS used to allow proper merging back later
					srcSeg.setAnnotation(new TXMLSegAnnotation(
						(ws1==null ? "" : "b") + (ws2==null ? "" : "a")));
					
					if ( trgSeg != null ) {
						trgCont.getSegments().append(trgSeg, trgCont.count()==1);
						hasOneTarget = true;
						if ( gtmt && !modified ) {
							TextUnitUtil.addAltTranslation(trgSeg, new AltTranslation(srcLoc, trgLoc,
								srcSeg.getContent(), srcSeg.getContent(), trgSeg.getContent(), MatchType.MT, 95, AltTranslation.ORIGIN_SOURCEDOC));
						}
					}
					else { // Empty if no target for this segment
						trgCont.getSegments().append(new TextFragment(), trgCont.count()==1);
					}
					// Adjust the first WS if needed
					if (( ws1 != null ) && ( srcCont.count() == 2 )) {
						srcCont.changePart(0);
						if ( trgCont != null ) trgCont.changePart(0);
					}
					// Set last WS
					if ( ws2 != null ) {
						srcCont.append(ws2, false);
						if ( trgCont != null ) trgCont.append(ws2.clone(), false);
					}
					ws1 = null;
					srcSeg = null;
					trgSeg = null;
					ws2 = null;
					srcDone = false;
					gtmt = false;
					modified = false;
				}
				else if ( "revisions".equals(name) ) {
					inRevision = false;
				}
				else if ( "translatable".equals(name) ) {
					if ( !hasOneTarget ) {
						tu.removeTarget(trgLoc);
					}
					if ( firstPartIsComment ) {
						skel.append(comment);
						// Then check if there is anything else, if not: it's not an extractable element
						if ( tu.getSource().count()==1 && tu.getSource().isEmpty() ) {
							buildEndElement(true);
							tu = null;
							createDocumentPartIfNeeded();
						}
					}

					// If tu is still not null: it wasn't a commented out chunk
					if ( tu != null ) {
						skel.addContentPlaceholder(tu);
						tu.setSkeleton(skel);
						tu.setPreserveWhitespaces(true);
						tu.setMimeType(MimeTypeMapper.XML_MIME_TYPE);
						queue.add(new Event(EventType.TEXT_UNIT, tu));
						buildEndElement(true);
					}
					return;
				}
				break;
				
			case XMLStreamConstants.COMMENT:
				if ( (srcCont.count() == 1) && srcCont.isEmpty() ) {
					// We will need to add a part at the front
					// Keep adding until we have a non-commented segment
					comment += "<!--"+ reader.getText().replace("\n", lineBreak) + "-->";
					firstPartIsComment = true;
				}
				else { // Not a first part comment
					// There was at least one non-commented segment before this commented one
					srcCont.append(new TextPart("<!--"+ reader.getText().replace("\n", lineBreak) + "-->"), true);
				}
				break;
			}
		}
	}
	
	//todo
	private TextFragment processContent (String nameForExit)
		throws XMLStreamException
	{
		TextFragment tf = new TextFragment();
		while ( reader.hasNext() ) {
			switch ( reader.next() ) {
			case XMLStreamConstants.CHARACTERS:
			case XMLStreamConstants.CDATA:
			case XMLStreamConstants.SPACE:
				tf.append(reader.getText());
				break;
			case XMLStreamConstants.START_ELEMENT:
				String name = reader.getLocalName();
				TagType tagType = TagType.PLACEHOLDER;
				String type = "ph";
				int idToUse = -1;
				//String tmp = reader.getAttributeValue(null, "type");
				appendCode(tagType, idToUse, name, type, false, tf);
				break;
			case XMLStreamConstants.END_ELEMENT:
				name = reader.getLocalName();
				if ( name.equals(nameForExit) ) {
					return tf;
				}
			}
		}
		return tf;
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
			if ( TARGETLOCALE.equals(attrName) ) {
				tmp.append(" "+TARGETLOCALE+"=\"");
				skel.append(tmp.toString());
				skel.append(trgLoc.toString());
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

//	private void storeUntilEndElement (String name) throws XMLStreamException {
//		int eventType;
//		while ( reader.hasNext() ) {
//			eventType = reader.next();
//			switch ( eventType ) {
//			case XMLStreamConstants.START_ELEMENT:
//				buildStartElement(true);
//				break;
//			case XMLStreamConstants.END_ELEMENT:
//				if ( name.equals(reader.getLocalName()) ) {
//					buildEndElement(true);
//					reader.next(); // Move forward
//					return;
//				}
//				// Else: just store the end
//				buildEndElement(true);
//				break;
//			case XMLStreamConstants.SPACE:
//			case XMLStreamConstants.CDATA:
//			case XMLStreamConstants.CHARACTERS:
//				//TODO: escape unsupported chars
//				skel.append(Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0, true, null));
//				break;
//			case XMLStreamConstants.COMMENT:
//				//addTargetIfNeeded();
//				skel.append("<!--"+ reader.getText().replace("\n", lineBreak) + "-->");
//				break;
//			case XMLStreamConstants.PROCESSING_INSTRUCTION:
//				skel.append("<?"+ reader.getPITarget() + " " + reader.getPIData() + "?>");
//				break;
//			}
//		}
//	}

	private void createDocumentPartIfNeeded () {
		// Make a document part with skeleton between the previous event and now.
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
	
}
