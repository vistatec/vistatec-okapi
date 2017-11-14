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

import net.sf.okapi.common.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.List;

import static net.sf.okapi.filters.openxml.AttributeStripper.RevisionAttributeStripper.stripSectionPropertiesRevisionAttributes;
import static net.sf.okapi.filters.openxml.AttributeStripper.RevisionAttributeStripper.stripTableRowRevisionAttributes;
import static net.sf.okapi.filters.openxml.StartElementContextFactory.createStartElementContext;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isParagraphStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isSectionPropertiesStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isTableRowStartEvent;

/**
 * Simplifies markup within the paragraphs or other rich
 * content structure or a document part, by
 * - Stripping revision ID (rsid*) and powerpoint spelling error information
 * - Merging consecutive runs with equivalent run properties
 * - Merge consecutive text elements
 */
public class ParagraphSimplifier {

	private static final String TAB_REPLACEMENT = "\t";

	private static final Logger LOGGER = LoggerFactory.getLogger(ParagraphSimplifier.class);

	private XMLEventReader xmlReader;
	private XMLEventWriter xmlWriter;
	private XMLEventFactory eventFactory;
	private ConditionalParameters params;
	private StyleDefinitions styleDefinitions;

	public ParagraphSimplifier(XMLEventReader xmlReader, XMLEventWriter xmlWriter, XMLEventFactory eventFactory,
							   ConditionalParameters params, StyleDefinitions styleDefinitions) {
		this.xmlReader = xmlReader;
		this.xmlWriter = xmlWriter;
		this.eventFactory = eventFactory;
		this.params = params;
		this.styleDefinitions = styleDefinitions;
	}

	// TODO: refactor with StyledTextPartHandler (the sectPtr stuff)
	public void process() throws XMLStreamException {
		while (xmlReader.hasNext()) {
			XMLEvent e = xmlReader.nextEvent();
			if (isParagraphStartEvent(e)) {
				Block block = new BlockParser(createStartElementContext(e.asStartElement(), xmlReader, eventFactory, params),
						new IdGenerator(null), styleDefinitions).parse();
				flushEvents(block.getEvents());
			}
			else {
				if (isSectionPropertiesStartEvent(e)) {
					e = stripSectionPropertiesRevisionAttributes(createStartElementContext(e.asStartElement(), null, eventFactory, params));
				} else if (isTableRowStartEvent(e)) {
					e = stripTableRowRevisionAttributes(createStartElementContext(e.asStartElement(), null, eventFactory, params));
				}
				xmlWriter.add(e);
			}
		}
	}

	private void flushEvents(List<XMLEvent> events) throws XMLStreamException {
		for (XMLEvent e : events) {
			try {
				xmlWriter.add(e);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
