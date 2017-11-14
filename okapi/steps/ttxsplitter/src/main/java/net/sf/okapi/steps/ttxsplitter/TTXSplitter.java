/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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

package net.sf.okapi.steps.ttxsplitter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import net.sf.okapi.common.LocaleId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TTXSplitter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final TTXSplitterParameters params;
	
	private String srcLang;
	private WordCounter counter;
	private long wordCount;
	private long partWC;
	private XMLEventReader reader;
	private int outputState;
	private XMLOutputFactory outFact;
	private XMLEventFactory  evtFact;
	private XMLEventWriter writer;
	private int part;
	private String outputPath;
	private Stack<XMLEvent> topStack;
	private Stack<XMLEvent> stack;
	private int level;
	private ArrayList<XMLEvent> endEvents;
	
	public TTXSplitter (TTXSplitterParameters params) {
		this.params = params;
	}
	
	public void initialize (String srcLang) {
		this.srcLang = srcLang;
		counter = new WordCounter(LocaleId.fromString(srcLang));
	}
	
	public void split (URI inputURI) {
		try {
			outputState = -1;
			process(inputURI);
			if ( wordCount <= params.getPartCount() ) {
				logger.warn("Word count for this document is {} (less than the {} files requested): No split was perfomed.",
					wordCount, params.getPartCount());
				return;
			}
			//TODO: have a param threshold under which we do not split
			
			partWC = wordCount / params.getPartCount();
			logger.info("Word count = {}: {} files of about {} words each.", wordCount, params.getPartCount(), partWC);
			
			// Generate the output files
			outputState = 0;
			part = 0;
			outputPath = new File(inputURI).getAbsolutePath();
			stack = new Stack<>();
			topStack = new Stack<>();
			process(inputURI);
		}
		catch ( Throwable e ) {
			e.printStackTrace();
			logger.error("Could not split the TTX document:\n"+ e.getLocalizedMessage());
		}
	}
	
	private void process (URI inputURI)
		throws XMLStreamException, MalformedURLException, IOException
	{
		try {
			XMLInputFactory inpFact = XMLInputFactory.newInstance();
			inpFact.setProperty(XMLInputFactory.IS_COALESCING, true);
			// security concern. Turn off DTD processing
			// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
			inpFact.setProperty(XMLInputFactory.SUPPORT_DTD, false);			
			
			StreamSource source = new StreamSource(new BufferedInputStream(inputURI.toURL().openStream()));
			reader = inpFact.createXMLEventReader(source);
	
			if ( outputState > -1 ) {
				outFact = XMLOutputFactory.newInstance();
				evtFact = XMLEventFactory.newInstance();
				// Create the closing events
				endEvents = new ArrayList<>();
				endEvents.add(evtFact.createEndElement(new QName("", "Raw"), null));
				endEvents.add(evtFact.createEndElement(new QName("", "Body"), null));
				endEvents.add(evtFact.createEndElement(new QName("", "TRADOStag"), null));
			}
			
			level = 0;
			wordCount = 0;
			boolean inText = false;
			boolean skip = false;
			StringBuilder sb = new StringBuilder();
			String name;
			
			while ( reader.hasNext() ) {
				XMLEvent event = reader.nextEvent();
				switch ( event.getEventType() ) {
				
				case XMLEvent.START_ELEMENT:
					level++;
					StartElement elem = event.asStartElement();
					switch ( elem.getName().getLocalPart() ) {
					case "Tuv":
						String lang = getAttributeValue(elem, "Lang");
						if ( srcLang.equals(lang) ) skip = false;
						else skip = true;
						break;
					case "UserSettings":
						initialize(getAttributeValue(elem, "SourceLanguage"));
						break;
					case "ut":
						if ( writer != null ) writer.add(event);
						skipElement("ut"); level--;
						continue;
					case "Raw":
						inText = true;
						if ( outputState == 0 ) {
							topStack.push(event);
							outputState = 1; // We are done collecting the top events
						}
						if ( outputState > -1 ) {
							startNewOutput();
							continue;
						}
						break;
					case "df":
						if ( writer != null ) stack.push(event);
						break;
					}
					break;
					
				case XMLEvent.END_ELEMENT:
					level--;
					name = event.asEndElement().getName().getLocalPart();
					if ( name.equals("df") ) {
						if ( writer != null ) stack.pop();
					}
					if (( level == 3 ) && inText ) {
						if ( sb.length() > 0 ) {
							wordCount += counter.getWordCount(sb.toString());
							sb.setLength(0);
						}
						// If we went over the part count we need to start the next output file
						if (( wordCount >= partWC ) && ( writer != null ) && ( part < params.getPartCount() )) {
							writer.add(event);
							endOutput();
							startNewOutput();
							continue;
						}
					}
					break;
				
				case XMLEvent.CHARACTERS:
					if ( inText && !skip ) {
						sb.append(event.asCharacters().getData());
					}
				}
				
				if ( outputState == 0 ) {
					topStack.push(event);
				}
				// In all cases: if we have the writer set we output the event
				if ( writer != null ) {
					writer.add(event);
				}
				
			}
		}
		finally {
			close();
		}
	}
	
	private String getAttributeValue (StartElement elem,
		String name)
	{
		Attribute attr = elem.getAttributeByName(new QName("", name));
		if ( attr == null ) return null;
		return attr.getValue();
	}
	
	private void endOutput ()
		throws XMLStreamException
	{
		if ( writer != null ) {
			// Do we have un-closed elements like df
			if ( !stack.isEmpty() ) {
				for ( int i=0; i<stack.size(); i++ ) {
					XMLEvent event = evtFact.createEndElement(new QName("", "df"), null);
					writer.add(event);		
				}
			}
			if ( part < params.getPartCount() ) {
				// Write the ending events (but not for the last file)
				for ( XMLEvent event : endEvents ) {
					writer.add(event);
				}
			}
			// Close
			writer.close();
			writer = null;
			logger.info("Part {} has {} words.", String.format("%03d", part), wordCount);
		}
	}
	
	private void startNewOutput ()
		throws XMLStreamException, IOException
	{
		wordCount = 0;
		part++;
		String mark = String.format("_part%03d", part);
		FileOutputStream fos = new FileOutputStream(outputPath.replace(".ttx", mark+".ttx"));
		writer = outFact.createXMLEventWriter(fos, "UTF-8");
		for ( int i=0; i<topStack.size(); i++ ) {
			writer.add(topStack.elementAt(i));
		}
		for ( int i=0; i<stack.size(); i++ ) {
			writer.add(stack.elementAt(i));
		}
	}
	
	private void close ()
		throws XMLStreamException
	{
		if ( reader != null ) {
			reader.close();
			reader = null;
		}
		endOutput();
	}
	
	/**
	 * Skip over the current element.
	 * @param name the name of the current element.
	 */
	private void skipElement (String name)
		throws XMLStreamException
	{
		while ( reader.hasNext() ) {
			XMLEvent event = reader.nextEvent();
			if ( writer != null ) {
				writer.add(event);
			}
			switch ( event.getEventType() ) {
			case XMLEvent.END_ELEMENT:
				if ( name.equals(event.asEndElement().getName().getLocalPart()) ) {
					return;
				}
			}
		}
	}
	
}
