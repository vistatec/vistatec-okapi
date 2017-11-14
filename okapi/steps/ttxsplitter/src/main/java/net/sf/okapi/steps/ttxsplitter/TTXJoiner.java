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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TTXJoiner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private TTXJoinerParameters params;
	private XMLInputFactory inpFact;
	private XMLOutputFactory outFact;
	private XMLEventFactory evtFact;
	private XMLEventWriter writer;
	private XMLEventReader reader;
	private int outputState;
	private String outputPath;
	private ArrayList<XMLEvent> endEvents;
	
	public TTXJoiner (TTXJoinerParameters params) {
		this.params = params;
	}
	
	public void process (List<URI> inputList) {
		try {
			inpFact = XMLInputFactory.newInstance();
			inpFact.setProperty(XMLInputFactory.IS_COALESCING, true);
			// security concern. Turn off DTD processing
			// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
			inpFact.setProperty(XMLInputFactory.SUPPORT_DTD, false);			
			
			outFact = XMLOutputFactory.newInstance();
			evtFact = XMLEventFactory.newInstance();
			// Create the closing events
			endEvents = new ArrayList<>();
			endEvents.add(evtFact.createEndElement(new QName("", "Raw"), null));
			endEvents.add(evtFact.createEndElement(new QName("", "Body"), null));
			endEvents.add(evtFact.createEndElement(new QName("", "TRADOStag"), null));
			
			// First sort the list to be sure we get all files group and in order
			Collections.sort(inputList);
			
			outputState = -1;
			outputPath = "";
			
			for ( URI uri : inputList ) {
				processFile(uri);
			}
		}
		catch ( Throwable e ) {
			e.printStackTrace();
			throw new OkapiException("Error while joining files:\n"+ e.getLocalizedMessage());
		}
		finally {
			try {
				outputState = 2;
				closeOutput();
			}
			catch ( XMLStreamException e ) {
				e.printStackTrace();
			}
		}
	}
	
	private void processFile (URI uri)
		throws XMLStreamException, IOException
	{
		try {
			String path = new File(uri).getAbsolutePath();
			// Extract the marker
			String fname = Util.getFilename(path, false);
			int p = fname.lastIndexOf('_');
			if ( p == -1 ) {
				logger.warn("File {} will be ignored: No '_' for the part marker.", fname);
				return;
			}
			String marker = fname.substring(p);
			if ( !marker.startsWith("_part") ) {
				logger.warn("File {} will be ignored: No part marker.", fname);
				return;
			}
			if ( marker.length() != 8 ) { // "_partNNN"
				logger.warn("File {} will be ignored: Invalid part marker.", fname);
				return;
			}
			fname = fname.substring(0, p);
			String newPath = Util.getDirectoryName(path) + File.separatorChar + fname 
				+ params.getSuffix() + Util.getExtension(path);
			
			// We have the output path for this file
			// Is it for a new set?
			if ( !newPath.equals(outputPath) ) {
				// It is for a new set of files
				// We close this set
				if ( outputState > -1 ) outputState = 2;
				closeOutput();
				outputPath = newPath;
				// And create the new output
				startOutput(outputPath);
			}

			// Open the file to read
			StreamSource source = new StreamSource(new BufferedInputStream(uri.toURL().openStream()));
			reader = inpFact.createXMLEventReader(source);
			// Write and write it
			boolean beforeRaw = true;
			String name;
			boolean done = false;
			while ( reader.hasNext() && !done ) {
				XMLEvent event = reader.nextEvent();
				switch ( event.getEventType() ) {
				case XMLEvent.START_ELEMENT:
					name = event.asStartElement().getName().getLocalPart();
					if ( name.equals("Raw") ) {
						if ( outputState == 0 ) {
							writer.add(event);
						}
						beforeRaw = false;
						continue;
					}
					break;
				case XMLEvent.END_ELEMENT:
					name = event.asEndElement().getName().getLocalPart();
					if ( name.equals("Raw") ) {
						// We are done for this file
						done = true;
						continue;
					}
					break;
				}
				// Write the event
				if ( outputState == 1 ) {
					if ( beforeRaw ) continue;
				}
				writer.add(event);
			}
			if ( outputState == 0 ) outputState = 1;
		}
		finally {
			if ( reader != null ) reader.close();
		}
	}
	
	private void closeOutput ()
		throws XMLStreamException
	{
		if ( writer != null ) {
			if ( outputState == 2 ) {
				// Last close for this set
				for ( XMLEvent event : endEvents ) {
					writer.add(event);
				}
			}
			// Close the writer
			writer.close();
			writer = null;
		}
	}
	
	private void startOutput (String outputPath)
		throws XMLStreamException, IOException
	{
		if ( outputState == -1 ) {
			outputState = 0;
		}
		else if ( outputState == 0 ) {
			outputState = 1;
		}
		FileOutputStream fos = new FileOutputStream(outputPath);
		writer = outFact.createXMLEventWriter(fos, "UTF-8");
	}
	
}
