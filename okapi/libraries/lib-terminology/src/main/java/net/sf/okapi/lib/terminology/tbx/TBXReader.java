/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.terminology.tbx;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.lib.terminology.ConceptEntry;
import net.sf.okapi.lib.terminology.IGlossaryReader;
import net.sf.okapi.lib.terminology.LangEntry;
import net.sf.okapi.lib.terminology.TermEntry;

public class TBXReader implements IGlossaryReader {

	private ConceptEntry nextEntry;
	private ConceptEntry cent;
	private LangEntry lent;
	private XMLStreamReader reader;

	@Override
	public void open (File file) {
		try {
			open(new FileInputStream(file));
		}
		catch ( Throwable e) {
			throw new OkapiIOException("Error opening the URI.\n" + e.getLocalizedMessage());
		}
	}

	@Override
	public void open (InputStream input) {
		try {
			close();
			XMLInputFactory fact = XMLInputFactory.newInstance();
			fact.setProperty(XMLInputFactory.IS_COALESCING, true);
			
			// security concern. Turn off DTD processing
			// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing			
			fact.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			
			reader = fact.createXMLStreamReader(input);
			
			// Read the first entry
			readNext();
		}
		catch ( Throwable e) {
			throw new OkapiIOException("Error opening the URI.\n" + e.getLocalizedMessage());
		}
	}

	@Override
	public void close () {
		nextEntry = null;
		try {
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
	public boolean hasNext () {
		return (nextEntry != null);
	}

	@Override
	public ConceptEntry next () {
		ConceptEntry currentEntry = nextEntry; // Next entry becomes the current one
		readNext(); // Parse the new next entry
		return currentEntry; // Send the current entry
	}

	private void readNext () {
		try {
			nextEntry = cent = null;
			while ( reader.hasNext() ) {
				int eventType = reader.next();
				switch ( eventType ) {
				case XMLStreamConstants.START_ELEMENT:
					String name = reader.getLocalName();
					if ( "termEntry".equals(name) ) {
						processTermEntry();
						return; // Done for this entry
					}
					break;
				}
			}
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error when reading." + e.getLocalizedMessage(), e);
		}
	}

	private void processTermEntry () throws XMLStreamException {
		cent = new ConceptEntry();
		cent.setId(reader.getAttributeValue(null, "id"));
		String name;
		while ( reader.hasNext() ) {
			int eventType = reader.next();
			switch ( eventType ) {
			case XMLStreamConstants.START_ELEMENT:
				name = reader.getLocalName();
				if ( "langSet".equals(name) ) {
					processLangSet();
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				name = reader.getLocalName();
				if ( "termEntry".equals(name) ) {
					nextEntry = cent; // No error, we can set the real entry
					return; // This termEntry is done
				}
				break;
			}
		}
	}

	private void processLangSet () throws XMLStreamException {
		// Get the language information
		String lang = reader.getAttributeValue(XMLConstants.XML_NS_URI, "lang");
		if ( Util.isEmpty(lang) ) {
			throw new OkapiIOException("Missing or empty xml:lang attribute.");
		}
		// Create the new language entry
		lent = new LangEntry(LocaleId.fromString(lang));

		String name;
		while ( reader.hasNext() ) {
			int eventType = reader.next();
			switch ( eventType ) {
			case XMLStreamConstants.START_ELEMENT:
				name = reader.getLocalName();
				if ( "tig".equals(name) ) {
					processTig();
				}
				else if ( "ntig".equals(name) ) {
					processNtig();
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				name = reader.getLocalName();
				if ( "langSet".equals(name) ) {
					cent.addLangEntry(lent);
					return; // This langSet is done
				}
				break;
			}
		}
	}

	private void processTig () throws XMLStreamException {
		String name;
		while ( reader.hasNext() ) {
			int eventType = reader.next();
			switch ( eventType ) {
			case XMLStreamConstants.START_ELEMENT:
				name = reader.getLocalName();
				if ( "term".equals(name) ) {
					processTerm();
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				name = reader.getLocalName();
				if ( "tig".equals(name) ) {
					return; // This tig is done
				}
				break;
			}
		}
	}

	private void processNtig () throws XMLStreamException {
		String name;
		while ( reader.hasNext() ) {
			int eventType = reader.next();
			switch ( eventType ) {
			case XMLStreamConstants.START_ELEMENT:
				name = reader.getLocalName();
				if ( "termGrp".equals(name) ) {
					processTermGrp();
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				name = reader.getLocalName();
				if ( "ntig".equals(name) ) {
					return; // This ntig is done
				}
				break;
			}
		}
	}

	private void processTermGrp () throws XMLStreamException {
		String name;
		while ( reader.hasNext() ) {
			int eventType = reader.next();
			switch ( eventType ) {
			case XMLStreamConstants.START_ELEMENT:
				name = reader.getLocalName();
				if ( "term".equals(name) ) {
					processTerm();
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				name = reader.getLocalName();
				if ( "termGrp".equals(name) ) {
					return; // This termGrp is done
				}
				break;
			}
		}
	}

	private void processTerm () throws XMLStreamException {
		String id = reader.getAttributeValue(null, "id");
		// We do not read the <hi> element, but just get its content
		StringBuilder tmp = new StringBuilder();
		while ( reader.hasNext() ) {
			int eventType = reader.next();
			switch ( eventType ) {
			case XMLStreamConstants.END_ELEMENT:
				if ( "term".equals(reader.getLocalName()) ) {
					TermEntry term = new TermEntry(tmp.toString());
					term.setId(id);
					lent.addTerm(term);
					return;
				}
				break;
			case XMLStreamConstants.CHARACTERS:
				tmp.append(reader.getText());
				break;
			}
		}
	}

}
