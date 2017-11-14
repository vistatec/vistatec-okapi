/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffsplitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.steps.wordcount.WordCounter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Splits a given XLIFF 1.2 input into several XLIFF documenta based on a provided word count.
 * The split occurs anywhere after a trans-unit element (so file elements can be split as well).
 * The word-count is done on the source content of each trans-unit.
 * trans-unit with state-qualifier set to x-numeric, x-alphanumeric and x-punctuation are not
 * counted. And trans-unit with translate='no' are also not counted.
 */
public class XliffWCSplitter {

	static private final QName QNTRANSLATE = new QName("", "translate");
	static private final QName QNSTATE = new QName("", "state");
	static private final QName QNSTATEQUALIFIER = new QName("", "state-qualifier");

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final XMLInputFactory xif;
	private final XMLOutputFactory xof;
	private final XMLEventFactory evfact;

	private XliffWCSplitterParameters params;
	private String outputRoot;
	File partFile;
	private int count;
	private int partCount;
	private XMLEventReader reader = null;
	private XMLEventWriter writer = null;
	private LocaleId srcLoc;
	private StringBuilder content;
	private String state;
	private String stateQualifier;
	private boolean isTranslatable;
	
	private boolean splitASAP;
	private List<XMLEvent> events;
	private boolean storeEvents;
	private Stack<XMLEvent> stack;
	private Stack<Boolean> translate;
	private LinkedHashMap<String, Integer> files;
	
	public XliffWCSplitter (XliffWCSplitterParameters params) {
		this.params = params;
		xif = XMLInputFactory.newInstance();
		xof = XMLOutputFactory.newInstance();
		evfact = XMLEventFactory.newInstance();
		
		// security concern. Turn off DTD processing
		// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);			
	}

	/**
	 * Process a raw document.
	 * @param rawDoc the raw document to process. 
	 */
	public Map<String, Integer> process (RawDocument rawDoc) {
		URI uri = rawDoc.getInputURI();
		if ( uri == null ) {
			throw new InvalidParameterException("This step does not support non-URI raw documents.");
		}
		String outRoot = uri.getPath();
		int p = outRoot.lastIndexOf('.');
		if ( p != -1 ) outRoot = outRoot.substring(0, p);
		return process(rawDoc.getStream(), outRoot, rawDoc.getSourceLocale().toString());
	}
	
	/**
	 * Process an input stream.
	 * @param inputStream the input stream to process.
	 * @param outputRoot path of the output part with the root filename. 
	 */
	public Map<String, Integer> process (InputStream inputStream,
		String outputRoot,
		String srcLang)
	{
		files = new LinkedHashMap<>();
		try {
			this.outputRoot = outputRoot;
			srcLoc = LocaleId.fromBCP47(srcLang);
			partCount = 0;
			events = new ArrayList<>();
			storeEvents = false;
			stack = new Stack<>();
			reader = xif.createXMLEventReader(inputStream);
			startPart();
			readAndwrite();
		}
		catch ( XMLStreamException | IOException e ) {
			logger.error(e.getMessage());
			throw new OkapiIOException(e);
		}
		finally {
			endPart();
			if ( reader != null ) {
				try {
					reader.close();
				}
				catch ( XMLStreamException e ) {
					logger.error(e.getMessage());
				}
				reader = null;
			}
		}
		return files;
	}
	
	private void store (XMLEvent event) {
		if ( !storeEvents ) return;
		events.add(event);
	}
	
	private void startPart ()
		throws XMLStreamException, IOException
	{
		splitASAP = false;
		count = 0;
		translate = new Stack<>();
		translate.push(true);
		partFile = new File(outputRoot+String.format("_PART%03d.xlf", ++partCount));
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(partFile), StandardCharsets.UTF_8);
		writer = xof.createXMLEventWriter(osw);
		// Workaround for TS2014 bug that produces ANSI output if the XLIFF UTF-8 has no BOM
		// We add a BOM at the start. This assume the writer does not do it itself
		osw.append('\ufeff');
	}
	
	private void endPart () {
		if ( writer != null ) {
			try {
				files.put(partFile.getName(), count);
				writer.flush();
				writer.close();
			}
			catch ( XMLStreamException e ) {
				logger.error(e.getMessage());
			}
		}
	}

	private void split ()
		throws XMLStreamException, IOException
	{
		// Close all elements (without popping them)
		int i = stack.size()-1;
		while ( i >= 0 ) {
			StartElement se = stack.get(i).asStartElement();
			writer.add(evfact.createEndElement(se.getName(), se.getNamespaces()));
			i--;
		}
		writer.flush();
		// Close the writer
		endPart();
		
		// Start the next part
		startPart();
		// Create the start elements
		// So it match where we were in the previous file and the closing elements will match too
		i = 0;
		while ( i < stack.size() ) {
			XMLEvent e = stack.get(i);
			writer.add(e);
			i++;
		}
		writer.flush();
	}

	private void readAndwrite ()
		throws XMLStreamException, IOException
	{
		while ( reader.hasNext() ) {
			XMLEvent event = reader.nextEvent();
			writer.add(event);
			store(event);
			if ( event.isStartElement() ) {
				stack.push(event);
				StartElement se = event.asStartElement();
				switch ( se.getName().getLocalPart() ) {
				case "group":
					Attribute attr = se.getAttributeByName(QNTRANSLATE);
					if ( attr == null ) translate.push(translate.peek());
					else translate.push("yes".equals(attr.getValue()));
					break;
				case "trans-unit":
					stack.pop();
					storeEvents = false;
					processTransUnit(se);
					// Did we reached the threshold?
					// If yes, we need to split
					if ( splitASAP ) {
						split();
					}
					break;
				default:
					break;
				}
			}
			else if ( event.isEndElement() ) {
				stack.pop();
			}
		}
	}

	private void processTransUnit (StartElement start)
		throws XMLStreamException
	{
		// No need to keep track of the stack at this level
		boolean inAltTrans = false;
		state = null;
		stateQualifier = null;
		content = new StringBuilder();
		
		Attribute attr = start.getAttributeByName(QNTRANSLATE);
		if ( attr == null ) isTranslatable = translate.peek();
		else isTranslatable = "yes".equals(attr.getValue());
		
		while ( reader.hasNext() ) {
			XMLEvent event = reader.nextEvent();
			writer.add(event);
			if ( event.isEndElement() ) {
				EndElement ee = event.asEndElement();
				String name = ee.getName().getLocalPart();
				switch ( name ) {
				case "trans-unit":
					verifyThreshold();
					return;
				case "alt-trans":
					inAltTrans = false;
					break;
				default:
					break;
				}
			}
			else if ( event.isStartElement() ) {
				StartElement se = event.asStartElement();
				String name = se.getName().getLocalPart();
				switch ( name ) {
				case "source":
					if ( !inAltTrans ) {
						processContent(se);
					}
					break;
				case "target":
					if ( !inAltTrans ) {
						getTargetAttributes(se);
					}
					break;
				case "alt-trans":
					inAltTrans = true;
					// Fall through
				default:
					break;
				}
			}
		}
	}
	
	private void getTargetAttributes (StartElement start) {
		Attribute attr = start.getAttributeByName(QNSTATE);
		if ( attr != null ) state = attr.getValue();
		attr = start.getAttributeByName(QNSTATEQUALIFIER);
		if ( attr != null ) stateQualifier = attr.getValue();
	}
	
	private void processContent (StartElement start)
		throws XMLStreamException
	{
		// No need to keep track of the stack at this level
		while ( reader.hasNext() ) {
			XMLEvent event = reader.nextEvent();
			writer.add(event);
			if ( event.isCharacters() ) {
				content.append(event.asCharacters().getData());
			}
			else if ( event.isEndElement() ) {
				EndElement ee = event.asEndElement();
				String name = ee.getName().getLocalPart();
				switch ( name ) {
				case "source":
					return;
				}
			}
			else if ( event.isStartElement() ) {
				StartElement se = event.asStartElement();
				switch ( se.getName().getLocalPart() ) {
				case "x":
				case "bx":
				case "ex":
				case "ph":
				case "it":
					break;
				case "g":
					break;
				}
			}
		}
	}

	private void verifyThreshold () {
		if ( !isTranslatable ) return;
		if ( "x-numeric".equals(stateQualifier) ) return;
		if ( "x-alphanum".equals(stateQualifier) ) return;
		if ( "x-punctuation".equals(stateQualifier) ) return;
		// Else: accumulate the count
		count += WordCounter.count(content.toString(), srcLoc);
		splitASAP = (count >= params.getThreshold());
	}
	
}
