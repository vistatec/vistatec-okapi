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
============================================================================*/

package net.sf.okapi.filters.tmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.xml.XMLConstants;
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
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.PipelineParameters;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class TmxFilter implements IFilter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private boolean hasNext;
	private XMLStreamReader reader;	
	private String docName;
	private int tuId;
	private IdGenerator otherId;
	private LocaleId srcLang;
	private LocaleId trgLang;
	private LinkedList<Event> queue;	
	private boolean canceled;
	private GenericSkeleton skel;	
	private String encoding;	
	private Parameters params;
	private Stack<Boolean> preserveSpaces;
	private String lineBreak;
	private boolean hasUTF8BOM;
	private boolean skipUtWarning;
	private int headerSegType = -1;
	private RawDocument input;
	private StartDocument startDoc;
	
	public enum TuvXmlLang {UNDEFINED,SOURCE,TARGET,OTHER}

	private TuvXmlLang tuvTrgType = TuvXmlLang.UNDEFINED;
	private HashMap<String,String> rulesMap = new HashMap<String,String>();
	private Stack<String> elemStack=new Stack<String>();
	private EncoderManager encoderManager;
	
	private boolean skipInvalidTu = false; 						//--allows processing of slightly invalid tmx files but skips any invalid <tu>s
	
	public static final int SEGTYPE_SENTENCE = 0;
	public static final int SEGTYPE_PARA = 1;
	public static final int SEGTYPE_OR_SENTENCE = 2;
	public static final int SEGTYPE_OR_PARA = 3;
	
	public TmxFilter () {
		params = new Parameters();
		
		rulesMap.put("<seg>", "<bpt><ept><it><ph><hi><ut>");
		rulesMap.put("<sub>", "<bpt><ept><it><ph><hi><ut>");
		rulesMap.put("<hi>", "<bpt><ept><it><ph><hi><ut>");
		rulesMap.put("<bpt>","<sub>");
		rulesMap.put("<ept>","<sub>");
		rulesMap.put("<it>","<sub>");
		rulesMap.put("<ph>","<sub>");		
	}
	
	public void cancel() {
		canceled = true;
	}

	public void close() {
		if (input != null) {
			input.close();
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

	public String getName() {
		return "okf_tmx";
	}
	
	public String getDisplayName () {
		return "TMX Filter";
	}

	public String getMimeType () {
		return MimeTypeMapper.TMX_MIME_TYPE;
	}

	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MimeTypeMapper.TMX_MIME_TYPE,
			getClass().getName(),
			"TMX",
			"Configuration for Translation Memory eXchange (TMX) documents.",
			null,
			".tmx;"));
		return list;
	}
	
	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.TMX_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		}
		return encoderManager;
	}

	public Parameters getParameters () {
		return params;
	}
	
	public boolean hasNext() {
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
			this.input = input;
			
			canceled = false;			
			
			XMLInputFactory fact = XMLInputFactory.newInstance();
			fact.setProperty(XMLInputFactory.IS_COALESCING, true);
//Removed for Java 1.6     		fact.setProperty(XMLInputFactory2.P_REPORT_PROLOG_WHITESPACE, true);
			
			//fact.setXMLResolver(new DefaultXMLResolver());
			//TODO: Resolve the re-construction of the DTD, for now just skip it
			fact.setProperty(XMLInputFactory.SUPPORT_DTD, false);			

			// Determine encoding based on BOM, if any
			input.setEncoding("UTF-8"); // Default for XML, other should be auto-detected
			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
			detector.detectBom();
			if ( detector.isAutodetected() ) {
				input.setEncoding(detector.getEncoding());
				reader = fact.createXMLStreamReader(input.getStream(), detector.getEncoding());
			}
			else {
				reader = fact.createXMLStreamReader(input.getStream());
			}
			
			String realEnc = reader.getCharacterEncodingScheme();
			if ( realEnc != null ) encoding = realEnc;
			else encoding = input.getEncoding();
			
			srcLang = input.getSourceLocale();
			if ( srcLang == null ) throw new NullPointerException("Source language not set.");
			trgLang = input.getTargetLocale();
			if ( trgLang == null ) throw new NullPointerException("Target language not set.");
			hasUTF8BOM = detector.hasUtf8Bom();
			lineBreak = detector.getNewlineType().toString();
			if ( input.getInputURI() != null ) {
				docName = input.getInputURI().getPath();
			}

			preserveSpaces = new Stack<Boolean>();
			preserveSpaces.push(false);
			tuId = 0;
			otherId = new IdGenerator(null, "d");			
			hasNext=true;
			queue = new LinkedList<Event>();
			skipUtWarning = false;
			tuvTrgType = TuvXmlLang.UNDEFINED;
			
			//--attempt encoding detection--
			//if(reader.getEncoding()!=null){
			//	encoding = reader.getEncoding();
			//}
			
			startDoc = new StartDocument(otherId.createId());
			startDoc.setName(docName);
			startDoc.setEncoding(encoding, hasUTF8BOM); //TODO: UTF8 BOM detection
			startDoc.setLocale(srcLang);
			startDoc.setFilterParameters(getParameters());
			startDoc.setFilterWriter(createFilterWriter());
			startDoc.setType(MimeTypeMapper.TMX_MIME_TYPE);
			startDoc.setMimeType(MimeTypeMapper.TMX_MIME_TYPE);
			startDoc.setMultilingual(true);
			startDoc.setLineBreak(lineBreak);			
			// delay sending of the StartDocument event until we process header 
			
			// The XML declaration is not reported by the parser, so we need to
			// create it as a document part when starting			
			skel = new GenericSkeleton();
			startDoc.setProperty(new Property(Property.ENCODING, encoding, false));
			skel.append("<?xml version=\"1.0\" encoding=\"");
			skel.addValuePlaceholder(startDoc, Property.ENCODING, LocaleId.EMPTY);
			skel.append("\"?>");
			startDoc.setSkeleton(skel);
		}
		catch ( XMLStreamException e) {
			throw new OkapiIOException(e);
		}
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	public void setParameters(IParameters params) {
		this.params = (Parameters)params;
	}

	public ISkeletonWriter createSkeletonWriter() {
		return new GenericSkeletonWriter();
	}

	public IFilterWriter createFilterWriter () {
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	private boolean read () throws XMLStreamException {
		skel = new GenericSkeleton();
		int eventType;
		
		while ( reader.hasNext() ) {
			eventType = reader.next();
			switch ( eventType ) {
			case XMLStreamConstants.START_ELEMENT:
				if (reader.getLocalName().equals("tu")){
					
					// Make a document part with skeleton between the previous event and now.
					// Spaces can go with trans-unit to reduce the number of events.
					// This allows to have only the trans-unit skeleton parts with the TextUnit event
					if ( !skel.isEmpty(false) ) {
						DocumentPart dp = new DocumentPart(otherId.createId(), false, skel);
						skel = new GenericSkeleton(); // And create a new skeleton for the next event
						queue.add(new Event(EventType.DOCUMENT_PART, dp));
					}
					
					return processTranslationUnit();
				} else if (reader.getLocalName().equals("header")) {
				    PipelineParameters pp = null;
				    String hSegType = reader.getAttributeValue(null, "segtype");
                    String hSrcLang = reader.getAttributeValue(null, "srclang");

					// set the source locale in case it is not set in RawDocument
					if (srcLang.equals(LocaleId.EMPTY)) {
						if (hSrcLang == null) {
							throw new OkapiBadFilterInputException("Header element is missing the srclang attribute.");
						}
						// we don't support *all* for the source locale
						// FIXME: We could solve this by sending multiple events using each target as the source.
						if ("*all*".equals(hSrcLang)) {
							throw new OkapiBadFilterInputException("TmxFilter does not support \"*all*\". Please define a source locale.");
						}
						
						srcLang = LocaleId.fromString(hSrcLang);
						
						// event to notify pipeline steps of the source locale change
						pp = new PipelineParameters(startDoc, input, null, null);
						pp.setSourceLocale(srcLang);
					}
					
					if (hSegType != null){
						if (hSegType.equals("sentence")){
							headerSegType = SEGTYPE_SENTENCE;	
						} else {
							headerSegType = SEGTYPE_PARA;
						}
					} else {
						headerSegType = -1;
					}
					
					processHeader();			
					// we can add the startDoc event now as we have any (possible) prop/notes from header
			        queue.add(new Event(EventType.START_DOCUMENT, startDoc));			        			        

                    // must add this event after startDoc
                    if (pp != null) {
                        queue.add(new Event(EventType.PIPELINE_PARAMETERS, pp));
                    }
                    
                    // load simplifier rules and send as an event
	        		if (!Util.isEmpty(((Parameters)getParameters()).getSimplifierRules())) {			
	        			Event cs = FilterUtil.createCodeSimplifierEvent(((Parameters)getParameters()).getSimplifierRules());
	        			queue.add(cs);
	        		}
        		
				} else {
					storeStartElement();
					if (!params.getConsolidateDpSkeleton()) {
						DocumentPart dp = new DocumentPart(otherId.createId(), false, skel);
						skel = new GenericSkeleton();
						queue.add(new Event(EventType.DOCUMENT_PART, dp));
					}					
				}
				break;
			
			case XMLStreamConstants.END_ELEMENT:
				storeEndElement();
				if (!params.getConsolidateDpSkeleton()) {
					DocumentPart dp = new DocumentPart(otherId.createId(), false, skel);
					skel = new GenericSkeleton();
					queue.add(new Event(EventType.DOCUMENT_PART, dp));
				}
				break;				
			
			case XMLStreamConstants.SPACE:
			case XMLStreamConstants.CDATA:
				skel.append(reader.getText().replace("\n", lineBreak));
				break;				
			case XMLStreamConstants.CHARACTERS: //TODO: Check if it's ok to not check for unsupported chars
				skel.append(Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0, params.getEscapeGT(), null));
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
				break;
			case XMLStreamConstants.ATTRIBUTE:
				break;
			case XMLStreamConstants.START_DOCUMENT:
				break;
			case XMLStreamConstants.END_DOCUMENT:
				break;				
			}
		}
		return false;
	}	

	private void storeStartElement () {
		String prefix = reader.getPrefix();
		if (( prefix == null ) || ( prefix.length()==0 )) {
			skel.append("<"+reader.getLocalName());
		}
		else {
			skel.append("<"+prefix+":"+reader.getLocalName());
		}

		int count = reader.getNamespaceCount();
		for ( int i=0; i<count; i++ ) {
			TmxUtils.copyXMLNSToSkeleton(skel, reader.getNamespacePrefix(i), 
					 reader.getNamespaceURI(i));
		}
		count = reader.getAttributeCount();
		for ( int i=0; i<count; i++ ) {
			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
			TmxUtils.copyAttributeToSkeleton(skel, reader, i, prefix, params.getEscapeGT());
		}
		skel.append(">");
	}
	
/*	private void storeTuStartElement () {
		
		String prefix = reader.getPrefix();
		if (( prefix == null ) || ( prefix.length()==0 )) {
			skel.append("<"+reader.getLocalName());
		}
		else {
			skel.append("<"+prefix+":"+reader.getLocalName());
		}

		int count = reader.getNamespaceCount();
		for ( int i=0; i<count; i++ ) {
			prefix = reader.getNamespacePrefix(i);
			skel.append(String.format(" xmlns%s=\"%s\"",
				((prefix.length()>0) ? ":"+prefix : ""),
				reader.getNamespaceURI(i)));
		}
		count = reader.getAttributeCount();
		for ( int i=0; i<count; i++ ) {
			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
			prefix = reader.getAttributePrefix(i); 
			skel.append(String.format(" %s%s=\"%s\"",
				(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
				reader.getAttributeLocalName(i),
				reader.getAttributeValue(i)));
			
			//--set the properties depending on the tuvTrgType--
			if(tuvTrgType == TuvXmlLang.UNDEFINED){
				tu.setProperty(new Property(reader.getAttributeLocalName(i),reader.getAttributeValue(i), true));				
			}else if(tuvTrgType == TuvXmlLang.SOURCE){
				tu.setSourceProperty(new Property(reader.getAttributeLocalName(i), reader.getAttributeValue(i), true));
			}else if(tuvTrgType == TuvXmlLang.TARGET || params.getProcessAllTargets()){
				tu.setTargetProperty(currentLang, new Property(reader.getAttributeLocalName(i),reader.getAttributeValue(i), true));
			}			
		}
		skel.append(">");
	}*/	
	
	private void storeEndElement () {
		String ns = reader.getPrefix();
		if (( ns == null ) || ( ns.length()==0 )) {
			skel.append("</"+reader.getLocalName()+">");
		}
		else {
			skel.append("</"+ns+":"+reader.getLocalName()+">");
		}
	}
	

	/**
	 * Processes notes or properties skeletizing start, content, and end element  
	 * @param tmxTu The TmxTu helper for the current tu.
	 * @return true for success and false for failure.
	 */	
	private boolean processTuDocumentPart(TmxTu tmxTu){
		
		String propName = "";							//used for <prop> elements to get the value of type to be used as prop name
		String startElement = reader.getLocalName();	//prop or note
		
		if(tuvTrgType == TuvXmlLang.UNDEFINED){
			//determine the property name and add skel to TmxTu
			propName = tmxTu.parseStartElement(reader,startElement, params.getEscapeGT());
		}else if (tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.getProcessAllTargets()){
			//determine the property name and add skel to TmxTuv
			propName = tmxTu.curTuv.parseStartElement(reader, tuvTrgType, params.getProcessAllTargets(), 
													  params.getEscapeGT(), startElement);
		}
		
		try {
			while(reader.hasNext()){
				int eventType = reader.next();
				switch ( eventType ) {
				case XMLStreamConstants.CHARACTERS:
					 //TODO: Check if it's ok to not check for unsupported chars
					//--append skel and set the properties depending on the tuvTrgType--
					if(tuvTrgType == TuvXmlLang.UNDEFINED){
						tmxTu.appendToSkel(Util.escapeToXML(reader.getText(), 0, params.getEscapeGT(), null));
						tmxTu.addProp(new Property(propName, reader.getText(), true));
					}else if(tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.getProcessAllTargets()){
						tmxTu.curTuv.skelBefore.append(Util.escapeToXML(reader.getText(), 0, params.getEscapeGT(), null));
						tmxTu.curTuv.setProperty(new Property(propName, reader.getText(), true));
					}else if(tuvTrgType == TuvXmlLang.OTHER){
						tmxTu.curTuv.skelBefore.append(Util.escapeToXML(reader.getText(), 0, params.getEscapeGT(), null));
					}
					break;
				case XMLStreamConstants.END_ELEMENT:
					if(reader.getLocalName().equalsIgnoreCase(startElement)){
						//--append skel depending on the tuvTrgType--
						if(tuvTrgType == TuvXmlLang.UNDEFINED){
							tmxTu.parseEndElement(reader, true);
						}else if (tuvTrgType == TuvXmlLang.UNDEFINED || tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.getProcessAllTargets()){
							tmxTu.curTuv.parseEndElement(reader,true);
						}
						return true;
					}
					break;
				}
			}
			return false;
		} catch (XMLStreamException e) {
			throw new OkapiIOException(e);
		}
	}	
	
    private void processHeader() {
        // store the header element we have already parsed
        storeStartElement();
        if (!params.getConsolidateDpSkeleton()) {
            DocumentPart dp = new DocumentPart(otherId.createId(), false, skel);
            skel = new GenericSkeleton();
            queue.add(new Event(EventType.DOCUMENT_PART, dp));
        }           

        // prop or note name
        String name = null;
        // prop or note content
        StringBuilder value = new StringBuilder();
        try {
            while (reader.hasNext()) {
                int eventType = reader.next();                
                switch (eventType) {
                case XMLStreamConstants.COMMENT:
                    skel.append("<!--" + reader.getText().replace("\n", lineBreak) + "-->");
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    skel.append("<?" + reader.getPITarget() + " " + reader.getPIData() + "?>");
                    break;
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CDATA:
                    skel.append(reader.getText().replace("\n", lineBreak));
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if (name != null) {
                        value.append(reader.getText().replace("\n", lineBreak));
                    }
                    // TODO: Check if it's ok to not check for unsupported chars                    
                    skel.append(Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0,
                            params.getEscapeGT(), null));
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    String curLocalName = reader.getLocalName();
                    if (curLocalName.equalsIgnoreCase("note")) {
                        name = "note";
                    } else if (curLocalName.equalsIgnoreCase("prop")) {
                        name = reader.getAttributeValue(null, "type");
                    }
                    
                    storeStartElement();
                    if (!params.getConsolidateDpSkeleton()) {
                        DocumentPart dp = new DocumentPart(otherId.createId(), false, skel);
                        skel = new GenericSkeleton();
                        queue.add(new Event(EventType.DOCUMENT_PART, dp));
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                   curLocalName = reader.getLocalName();
                   if (curLocalName.equalsIgnoreCase("note")
                            || curLocalName.equalsIgnoreCase("prop")) {                       
                        startDoc.setProperty(new Property(name, value.toString(), true));
                        name = null;
                        value.setLength(0);
                    }

                    storeEndElement();
                    if (!params.getConsolidateDpSkeleton()) {
                        DocumentPart dp = new DocumentPart(otherId.createId(), false, skel);
                        skel = new GenericSkeleton();
                        queue.add(new Event(EventType.DOCUMENT_PART, dp));
                    }
                    
                    // done break out of loop
                    if (curLocalName.equalsIgnoreCase("header")) {
                        return;
                    }
                    break;
                }
            }
        } catch (XMLStreamException e) {
            throw new OkapiIOException(e);
        }
    }
	
	/**
	 * Process a segment <seg>*</seg>, appending the skeleton to skel and adding the properties to nameable and reference to tu 
	 */			
	private boolean processSeg(TmxTu tmxTu){
		
		int id = 0;
		//Stack<Integer> idStack = new Stack<Integer>();
		//idStack.push(id);		
		
		String curLocalName;
		
		//--determine which container to use--
		TextFragment tf;
		if(tuvTrgType == TuvXmlLang.SOURCE){
			//tc = pTu.getSource();
			tf = tmxTu.curTuv.tc.getFirstContent();
		}else if(tuvTrgType == TuvXmlLang.TARGET || params.getProcessAllTargets()){
			//tc = pTu.setTarget(currentLang, new TextContainer());
			tf = tmxTu.curTuv.tc.getFirstContent();
		}else{
			tf=null;
		}
		
		//storeTuStartElement();							//store the <seg> element with it's properties
		tmxTu.curTuv.parseStartElement(reader, tuvTrgType, params.getProcessAllTargets(), params.getEscapeGT());
		
		try {
			while(reader.hasNext()){					//loop through the <seg> content
				int eventType;
				eventType = reader.next();
				
				//--if invalid skip to end </seg>--
				if ( skipInvalidTu){
					if (eventType == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equalsIgnoreCase("seg")){
						return false;
					}else{
						continue;
					}
				}
				
				switch ( eventType ) {
				case XMLStreamConstants.CHARACTERS:
				case XMLStreamConstants.CDATA:
				case XMLStreamConstants.SPACE:
					if(tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.getProcessAllTargets()){
						//TODO: Check if it's ok to not check for unsupported chars
						tf.append(reader.getText());	//add to source or target container
					}else{ 			
						//TODO: Check if it's ok to not check for unsupported chars
						tmxTu.curTuv.appendToSkel(Util.escapeToXML(reader.getText(), 0, params.getEscapeGT(), null));
					}
					break;
					
				case XMLStreamConstants.START_ELEMENT:		

					curLocalName = reader.getLocalName().toLowerCase();
					
					//--skip TUs with invalid content--
					if(!isValidElement(elemStack.peek(), curLocalName, params.getExitOnInvalid())){
						skipInvalidTu = true;
						break;
					}
					
					if(curLocalName.equals("ut") && !skipUtWarning){
						logger.warn("<ut> is been deprecated in tmx 1.4.");
						skipUtWarning=true;
					}
					elemStack.push(curLocalName);
										
					if(tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.getProcessAllTargets()){
					    // if this is an element that has an x or i id attribute get it here, otherwise id is set to -1					    
					    switch (curLocalName) {
					        case "hi":
					        case "ph":
					        case "it":
					        case "bpt":
					        case "ept":
					        case "ut": 
					            id = generateCodeId(getAttribute("x"), getAttribute("i"));					            
					            break;					           
					    }
					    
						if ( curLocalName.equals("hi") ) {
							String typeAttr = getAttribute("type");
							String xAttr = getAttribute("x");
							String type = (typeAttr!=null) ? typeAttr : "hi";
							String codeStr = "<hi";
							if(xAttr != null)
								codeStr += " x=\""+ xAttr + "\"";
							if(typeAttr != null)
								codeStr += " type=\""+ typeAttr + "\"";
							codeStr +=">";			
							// FIXME: Code balance does not work if type is set
							// to type value, must set to "hi" to match end tag
							//tf.append(TagType.OPENING, type, codeStr);
							tf.append(TagType.OPENING, "hi", codeStr, id);
						}
						else if (curLocalName.equals("ph") || curLocalName.equals("ut") ) {
							appendCode(TagType.PLACEHOLDER, id, curLocalName, curLocalName, tf);
						}
						else if ( curLocalName.equals("it") ) {
							// Get the pos attribute to detect the type of isolated code (begin or end)
							String pos = getAttribute("pos");
							if ( pos == null ) {
								logger.error("Attribute 'pos' is missing. Will map the <it> code to <ph>.");
								appendCode(TagType.PLACEHOLDER, id, curLocalName, curLocalName, tf);
							}
							else if ( pos.equals("begin") ) {
								appendCode(TagType.OPENING, id, curLocalName, curLocalName, tf);
							}
							else if ( pos.equals("end") ) {
								appendCode(TagType.CLOSING, id, curLocalName, curLocalName, tf);
							}
							else {
								logger.error("Invalide 'pos' value ('+pos+'). Will map the <it> code to <ph>.");
								appendCode(TagType.PLACEHOLDER, id, curLocalName, curLocalName, tf);
							}
						}
						else if ( curLocalName.equals("bpt") ) {
							appendCode(TagType.OPENING, id, curLocalName,"Xpt", tf);
						}
						else if ( curLocalName.equals("ept") ) {
							appendCode(TagType.CLOSING, id, curLocalName,"Xpt", tf);
						}
						break;
					}else{
						tmxTu.curTuv.parseStartElement(reader, tuvTrgType, params.getProcessAllTargets(), params.getEscapeGT());
					}
					break;
					
				case XMLStreamConstants.END_ELEMENT:
					
					curLocalName = reader.getLocalName();		//current element
					elemStack.pop();						//pop one element
					
					if(curLocalName.equalsIgnoreCase("seg")){	//end of seg
										
						tmxTu.curTuv.finishedSegSection=true;
						tmxTu.curTuv.parseEndElement(reader);

						return true;
					}else{
						if(tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.getProcessAllTargets()){
							if(curLocalName.equals("hi")){
								tf.append(TagType.CLOSING, "hi","</hi>");	
							}						
						}else{
							tmxTu.curTuv.parseEndElement(reader, true);
							break;
						}
					}
				}
			}
			// check if we need to log a warning about missing inline code ids
			List<Code> codes = tf.getCodes();
			// first check for the single code case, we never log a warning since
			// code alignment is trivial
			if (codes.size() <= 1) {
		        return false;
			} else {
			    // check each code if any have an id value of -1 log warning
			    for (Code c : codes) {
			        if (c.getId() == -1) {			            
			            logger.warn("The id attributes x and i are missing for {}. "
			                    + "An id will be auto-generated and may not match "
			                    + "the same code in the translated segments.", c.getOuterData());                        
	                }                    
                }                
			}
			
			return false;
		} catch (XMLStreamException e) {
			throw new OkapiIOException(e);
		}
	}
	
	/**
	 * Process an entire tu element
	 * @return FilterEvent
	 */		
	private boolean processTranslationUnit(){
		
		LocaleId currentLang;
		TmxTu tmxTu = new TmxTu(srcLang, trgLang, lineBreak, params.getSegType(), params.getPropValueSep());	//create the TmxTu helper
		tmxTu.parseStartElement(reader, params.getEscapeGT());		//add to TmxTu skelBefore

		//--determine the tu segtype after processing attributes--
		if (tmxTu.segType > 1){
			Property segTypeProp = tmxTu.getProp("segtype"); 
			if ( segTypeProp != null ) {
				if (segTypeProp.getValue().equals("sentence")){
					tmxTu.segType = SEGTYPE_SENTENCE;
				} else {
					tmxTu.segType = SEGTYPE_PARA;
				}
			}

			//--if tu segtype not specified try the header--
			if (tmxTu.segType > 1 && headerSegType != -1 ) {
				if (headerSegType == SEGTYPE_SENTENCE){
					tmxTu.segType = SEGTYPE_SENTENCE;
				} else if (headerSegType == SEGTYPE_PARA){
					tmxTu.segType = SEGTYPE_PARA;
				}
			}
			
			//--if unrecognized value or missing property use specified default
			if (tmxTu.segType == SEGTYPE_OR_SENTENCE){
				tmxTu.segType = SEGTYPE_SENTENCE;
			}else if (tmxTu.segType == SEGTYPE_OR_PARA){
				tmxTu.segType = SEGTYPE_PARA;
			} 			
		}
		
		String curLocalName;
		
		try {
			while(reader.hasNext()){
				
				int eventType = reader.next();
				
				//-- if invalid skip to end </tu>
				if ( skipInvalidTu ){
					if (eventType == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equalsIgnoreCase("tu")){
						//--reset--
						tuvTrgType = TuvXmlLang.UNDEFINED;
						skipInvalidTu = false;
						elemStack.clear();
						Property p = tmxTu.getProp("tuid");
						if (p != null){
							logger.warn("Skipping invalid <tu> element with tuid: {}.", p.getValue());
						}else{
							logger.warn("Skipping invalid <tu> element.");
						}
						return true;
					}else{
						continue;
					}
				}
				
				switch ( eventType ) {
				
				case XMLStreamConstants.COMMENT:
					//appends to either TmxTu or TmxTuv depending on tuvTrgType and skelBefore or skelAfter depending on flags
					tmxTu.smartAppendToSkel(tuvTrgType, "<!--"+ reader.getText().replace("\n", lineBreak) + "-->");
					break;
				case XMLStreamConstants.CHARACTERS: 
					//TODO: Check if it's ok to not check for unsupported chars
					//appends to either TmxTu or TmxTuv depending on tuvTrgType and skelBefore or skelAfter depending on flags					
					tmxTu.smartAppendToSkel(tuvTrgType, Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0, params.getEscapeGT(), null));					
					break;
				case XMLStreamConstants.START_ELEMENT:

					curLocalName = reader.getLocalName(); 
					if(curLocalName.equalsIgnoreCase("note") || curLocalName.equalsIgnoreCase("prop")){
						//Todo: handle true/false
						processTuDocumentPart(tmxTu);
					}else if(reader.getLocalName().equals("tuv")){
						
						currentLang = getXmlLangFromCurTuv();
						tuvTrgType = getTuvTrgType(currentLang);
						
						TmxTuv tmxTuv = tmxTu.addTmxTuv(currentLang,tuvTrgType, params.getPropValueSep());
						tmxTuv.parseStartElement(reader,tuvTrgType, params.getProcessAllTargets(), params.getEscapeGT());
						
					}else if(reader.getLocalName().equals("seg")){
						elemStack.push("seg");
						processSeg(tmxTu);
					}
					break;
					
				case XMLStreamConstants.END_ELEMENT:

					curLocalName = reader.getLocalName(); 
					if(curLocalName.equalsIgnoreCase("tu")){
						
						tmxTu.parseEndElement(reader);
						
						tuId = tmxTu.addPrimaryTextUnitEvent(tuId, params.getProcessAllTargets(), queue);
						tuId = tmxTu.addDuplicateTextUnitEvents(tuId, params.getProcessAllTargets(), queue);
						
						//--reset--
						tuvTrgType = TuvXmlLang.UNDEFINED;
						return true;
						
					}else if(curLocalName.equals("tuv")){

						tmxTu.curTuv.parseEndElement(reader);

					}else{
						//--TMX RULE: Entering here would mean content other than <note>, <prop>, or <tuv> inside the <tu> which is invalid.
						if (params.getExitOnInvalid()) {
							throw new OkapiBadFilterInputException("Only <note>, <prop>, and <tuv> elements are allowed inside <tu>");
						} else{
							logger.warn("Only <note>, <prop>, and <tuv> elements are allowed inside <tu>");
							skipInvalidTu = true;
							break;
						}
					} 	
					break;
				}
			}
		} catch (XMLStreamException e) {
			throw new OkapiIOException(e);
		}
		return false;		
	}
	
	
	/**
	 * Appends a code, using the content of the node. Do not use for <g>-type tags.
	 * @param type The type of in-line code.
	 * @param id The id of the code to add.
	 * @param tagName The tag name of the in-line element to process.
	 * @param type The tag name of the in-line element to process. 
	 * @param fragment The object where to put the code.
	 * Do not save if this parameter is null.
	 */
	private void appendCode (TagType tagType,
		int id,
		String tagName,
		String type,
		TextFragment fragment)
	{
		
		String localName;
		
		try {
			StringBuilder innerCode = new StringBuilder();
			StringBuilder outerCode = null;
			outerCode = new StringBuilder();
			outerCode.append("<"+tagName);
			int count = reader.getAttributeCount();
			String prefix;
			for ( int i=0; i<count; i++ ) {
				if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
				TmxUtils.copyAttributeToBuffer(outerCode, reader, i, lineBreak, params.getEscapeGT());
			}
			outerCode.append(">");
			
			int eventType;
			while ( reader.hasNext() ) {
				eventType = reader.next();
				switch ( eventType ) {
				case XMLStreamConstants.START_ELEMENT:
					
					localName = reader.getLocalName().toLowerCase();
					if(!isValidElement(elemStack.peek(),localName, true)){
						//--throws OkapiBadFilterInputException if not valid--
					}
					
					if(localName.equals("ut") && !skipUtWarning){
						logger.warn("<ut> is been deprecated in tmx 1.4.");
						skipUtWarning=true;
					}
					elemStack.push(localName);

					//--warn about subflow--
					if("sub".equals(reader.getLocalName())){
						logger.warn("A <sub> element was detected. It will be included in its parent code as <sub> is currently not supported.");
					}
					
					prefix = reader.getPrefix();
					StringBuilder tmpg = new StringBuilder();
					if (( prefix == null ) || ( prefix.length()==0 )) {
						tmpg.append("<"+reader.getLocalName());
					}
					else {
						tmpg.append("<"+prefix+":"+reader.getLocalName());
					}
					count = reader.getNamespaceCount();
					for ( int i=0; i<count; i++ ) {
						TmxUtils.copyXMLNSToBuffer(tmpg, reader.getNamespacePrefix(i), 
												   reader.getNamespaceURI(i));
					}
					count = reader.getAttributeCount();
					for ( int i=0; i<count; i++ ) {
						if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
						TmxUtils.copyAttributeToBuffer(tmpg, reader, i, lineBreak, params.getEscapeGT());
					}
					tmpg.append(">");
				
					innerCode.append(tmpg.toString());
					outerCode.append(tmpg.toString());
					
					break;
					
				case XMLStreamConstants.END_ELEMENT:
					
					elemStack.pop();
					
					//--completed the original placeholder/code and back up to the <seg> level--
					if ( tagName.equals(reader.getLocalName()) && ((elemStack.peek().equals("seg"))|| (elemStack.peek().equals("hi")) )) {

						Code code = fragment.append(tagType, type, innerCode.toString(), id);
						outerCode.append("</"+tagName+">");
						code.setOuterData(outerCode.toString());
						return;							
					}else{
						
						String ns = reader.getPrefix();
						if (( ns == null ) || ( ns.length()==0 )) {
							innerCode.append("</"+reader.getLocalName()+">");
							outerCode.append("</"+reader.getLocalName()+">");
						}
						else {
							innerCode.append("</"+ns+":"+reader.getLocalName()+">");
							outerCode.append("</"+ns+":"+reader.getLocalName()+">");
						}						
					}
					break;

				case XMLStreamConstants.CHARACTERS:
				case XMLStreamConstants.CDATA:
				case XMLStreamConstants.SPACE:

					innerCode.append(reader.getText());//TODO: escape unsupported chars
					outerCode.append(Util.escapeToXML(reader.getText(), 0, params.getEscapeGT(), null));
					break;
				}
			}
		}
		catch ( XMLStreamException e) {
			throw new OkapiIOException(e);
		}
	}	

	/**
	 * Gets the TuvXmlLang based on current language and source and specified target lang
	 * @return 	TuvXmlLang.SOURCE, TuvXmlLang.TARGET, and TuvXmlLang.OTHER
	 */		
	private TuvXmlLang getTuvTrgType(LocaleId lang){
		if ( lang.equals(srcLang) ) {
			return TuvXmlLang.SOURCE; 
		}
		else if ( lang.equals(trgLang) ) {
			return TuvXmlLang.TARGET;
		}
		else { 
			return TuvXmlLang.OTHER;
		}
	}
	
	
	/**
	 * Gets the value of the xml:lang or lang attribute from the current <tuv> element
	 * @return the language value
	 * @throws OkapiBadFilterInputException if xml:Lang or lang is missing
	 */		
	private LocaleId getXmlLangFromCurTuv(){
		String tmp = reader.getAttributeValue(XMLConstants.XML_NS_URI, "lang");
		if ( tmp != null ) {
			return LocaleId.fromString(tmp);
		}
		// If xml:lang not found, fall back to lang (old TMX versions)
		int count = reader.getAttributeCount();
		for ( int i=0; i<count; i++ ) {
			if ( reader.getAttributeLocalName(i).equals("lang")){
				return LocaleId.fromString(reader.getAttributeValue(i));
			}
		}
		throw new OkapiBadFilterInputException("The required xml:lang or lang attribute is missing in <tuv>. The file is not valid TMX.");
	}

	private String getAttribute (String localName) {
		int count = reader.getAttributeCount();
		for ( int i=0; i<count; i++ ) {
			if ( reader.getAttributeLocalName(i).equals(localName) ) {
				return reader.getAttributeValue(i);
			}
		}
		return null;
	}
	
	private boolean isValidElement(String curElem, String newElem, boolean throwException){
		String rules = rulesMap.get("<"+curElem+">");

		if(rules!=null && rules.contains("<"+newElem+">")){
			return true;
		}else{
			if(throwException){
				throw new OkapiBadFilterInputException("<"+newElem+"> not allowed in <"+curElem+">. Only "+rules+" allowed.");
			}else{
				logger.warn("<{}> not allowed in <{}>. Only {} allowed.", newElem, curElem, rules);
				return false;		
			}
		}
	}
	
	private int generateCodeId(String x, String i) {
	    int id = -1;
	    
	    // look at x first as this is the primary way to match target codes
	    if (!Util.isEmpty(x)) {
	        try {
	            id = Integer.valueOf(x);
	        } catch (NumberFormatException e) {
	           // case where id is not an integer
	            id = x.hashCode();
	        }
	        return id;
	    }

	    // There is no x, now try the i value
	    if (!Util.isEmpty(i)) {
            try {
                id = Integer.valueOf(i);
            } catch (NumberFormatException e) {
               // case where id is not an integer
                id = i.hashCode();
            }
	    }
        
        return id;
	}	
}
