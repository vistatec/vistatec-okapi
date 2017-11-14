/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.openxml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Class that rewrites the shared strings table to denormalize
 * shared references, according to the information in
 * {@link SharedStringMap}.
 */
public class SharedStringsDenormalizer {
	private XMLEventFactory factory;
	private SharedStringMap sharedStringMap;
	private List<SharedString> stringData = new ArrayList<SharedString>();

	public SharedStringsDenormalizer(XMLEventFactory factory, SharedStringMap sharedStringMap) {
		this.factory = factory;
		this.sharedStringMap = sharedStringMap;
	}

	private static final QName TABLE = Namespaces.SpreadsheetML.getQName("sst");
	private static final QName ENTRY = Namespaces.SpreadsheetML.getQName("si");
	private static final QName COUNT_ATTR = new QName("count");
	private static final QName UNIQUE_ATTR = new QName("uniqueCount");

	public void process(XMLEventReader reader, XMLEventWriter writer) throws IOException, XMLStreamException {
		readTable(reader);
		writeTable(writer);
		writer.close();
	}

	private void readTable(XMLEventReader reader) throws XMLStreamException {
		SharedString currentSSI = null;
		int currentIndex = 0;
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement()) {
				StartElement el = event.asStartElement();
				if (ENTRY.equals(el.getName())) {
					currentSSI = new SharedString(currentIndex++);
				}
				if (currentSSI != null) {
					currentSSI.addEvent(event);
				}
			}
			else if (event.isEndElement()) {
				EndElement el = event.asEndElement();
				if (currentSSI != null) {
					currentSSI.addEvent(event);
				}
				if (ENTRY.equals(el.getName())) {
					stringData.add(currentSSI);
					currentSSI = null;
				}
			}
			else if (event.isCharacters()) {
				if (currentSSI != null) {
					currentSSI.addEvent(event);
				}
			}
		}
	}

	private void writeTable(XMLEventWriter writer) throws XMLStreamException {
		writer.add(factory.createStartDocument("UTF-8", "1.0", true));
		// Write the start element, with updated count / uniqueCount attributes.
		List<Attribute> attrs = new ArrayList<Attribute>();
		List<SharedStringMap.Entry> entries = sharedStringMap.getEntries();
		String count = String.valueOf(entries.size());
		attrs.add(factory.createAttribute(COUNT_ATTR, count));
		attrs.add(factory.createAttribute(UNIQUE_ATTR, count));
		StartElement sstStart = factory.createStartElement(TABLE, attrs.iterator(),
				Collections.singleton(factory.createNamespace(Namespaces.SpreadsheetML.getURI())).iterator());
		writer.add(sstStart);

		// Write the entries, using the new order
		int newIndex = 0;
		for (SharedStringMap.Entry e : entries) {
			if (newIndex++ != e.getNewIndex()) {
				throw new IllegalStateException("Index mismatch: expected " + e.getNewIndex() + " found " + newIndex);
			}
			stringData.get(e.getOriginalIndex()).writeEvents(writer);
		}

		// Write the tail element
		writer.add(factory.createEndElement(TABLE, null));
		writer.add(factory.createEndDocument());
	}

	static class SharedString {
		private List<XMLEvent> events = new ArrayList<XMLEvent>();
		private int originalIndex;
		SharedString(int originalIndex) {
			this.originalIndex = originalIndex;
		}
		void addEvent(XMLEvent e) {
			events.add(e);
		}
		void writeEvents(XMLEventWriter writer) throws XMLStreamException {
			for (XMLEvent e : events) {
				writer.add(e);
			}
		}
		public int getOriginalIndex() {
			return originalIndex;
		}
	}
}
