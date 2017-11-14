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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;

import static net.sf.okapi.filters.openxml.AttributeStripper.RevisionAttributeStripper.stripSectionPropertiesRevisionAttributes;
import static net.sf.okapi.filters.openxml.AttributeStripper.RevisionAttributeStripper.stripTableRowRevisionAttributes;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement.SECTION_PROPERTIES_CHANGE;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement.TABLE_CELL_PROPERTIES_CHANGE;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement.TABLE_GRID_CHANGE;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement.TABLE_PROPERTIES_CHANGE;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement.TABLE_PROPERTIES_EXCEPTIONS_CHANGE;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement.TABLE_ROW_PROPERTIES_CHANGE;
import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createEndMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createGeneralMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createStartMarkupComponent;
import static net.sf.okapi.filters.openxml.StartElementContextFactory.createStartElementContext;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isBlockMarkupEndEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isBlockMarkupStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isParagraphStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isSectionPropertiesStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isTableGridEndEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isTableGridStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isTablePropertiesStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isTableRowStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isTextBodyPropertiesStartEvent;

/**
 * Part handler for styled text (Word document parts, PPTX slides) that
 * follow the styled run model.
 */
class StyledTextPartHandler extends GenericPartHandler {
	private XMLEventFactory eventFactory;
	private StyleDefinitions styleDefinitions;

	private ElementSkipper tablePropertiesChangeElementSkipper;
	private ElementSkipper noElementSkipper;
	private ElementSkipper revisionPropertyChangeElementSkipper;

	private XMLEventReader xmlReader;

	private Iterator<Event> filterEventIterator;
	private String documentId;
	private String subDocumentId;
	private LocaleId sourceLanguage;

	private IdGenerator nestedBlockId;
	private IdGenerator textUnitId;

	StyledTextPartHandler(ConditionalParameters cparams, OpenXMLZipFile zipFile, ZipEntry entry, StyleDefinitions styleDefinitions) {
		this(cparams, zipFile.getEventFactory(), entry.getName(), styleDefinitions);
		this.zipFile = zipFile;
		this.entry = entry;
		textUnitId = new IdGenerator(partName, IdGenerator.TEXT_UNIT);
		markup = new Block.BlockMarkup();
	}

	StyledTextPartHandler(ConditionalParameters cparams, XMLEventFactory eventFactory, String partName, StyleDefinitions styleDefinitions) {
		super(cparams, partName);
		this.eventFactory = eventFactory;
		this.styleDefinitions = styleDefinitions;

		tablePropertiesChangeElementSkipper = ElementSkipperFactory.createGeneralElementSkipper(cparams, TABLE_PROPERTIES_CHANGE);
		noElementSkipper = ElementSkipperFactory.createGeneralElementSkipper(cparams);
		revisionPropertyChangeElementSkipper = ElementSkipperFactory.createGeneralElementSkipper(
				cparams,
				SECTION_PROPERTIES_CHANGE,
				TABLE_GRID_CHANGE,
				TABLE_PROPERTIES_EXCEPTIONS_CHANGE,
				TABLE_ROW_PROPERTIES_CHANGE,
				TABLE_CELL_PROPERTIES_CHANGE);

		nestedBlockId = new IdGenerator(null);
		textUnitId = new IdGenerator(partName, IdGenerator.TEXT_UNIT);
		markup = new Block.BlockMarkup();
	}

	/**
	 * Open this part and perform any initial processing.  Return the
	 * first event for this part.  In this case, it's a START_SUBDOCUMENT
	 * event.
	 *
	 * @param documentId document identifier
	 * @param subDocumentId sub-document identifier
	 * @param sourceLanguage the locale of the source
	 *
	 * @return Event
	 *
	 * @throws IOException
	 * @throws XMLStreamException
     */
	@Override
	public Event open(String documentId, String subDocumentId, LocaleId sourceLanguage) throws IOException,
			XMLStreamException {
		this.documentId = documentId;
		this.subDocumentId = subDocumentId;
		this.sourceLanguage = sourceLanguage;
		/**
		 * Process the XML event stream, simplifying as we go.  Non-block content is
		 * written as a document part.  Blocks are parsed, then converted into TextUnit structures.
		 */
		xmlReader = zipFile.getInputFactory().createXMLEventReader(
				new InputStreamReader(new BufferedInputStream(zipFile.getInputStream(entry)), StandardCharsets.UTF_8));
		return open(documentId, subDocumentId, xmlReader);
	}

	// Package-private for test.  XXX This is an artifact of the overall PartHandler
	// interface needing work.
	Event open(String documentId, String subDocumentId, XMLEventReader xmlReader) throws XMLStreamException {
		this.xmlReader = xmlReader;
		try {
			process();
		}
		finally {
			if (xmlReader != null) {
				xmlReader.close();
			}
		}
		return createStartSubDocumentEvent(documentId, subDocumentId);
	}

	private void process() throws XMLStreamException {
		StartElement parentStartElement = null;

		while (xmlReader.hasNext()) {
			XMLEvent e = xmlReader.nextEvent();
			preHandleNextEvent(e);

			if (isParagraphStartEvent(e)) {
				flushDocumentPart();
				StartElementContext startElementContext =
						createStartElementContext(e.asStartElement(), xmlReader, eventFactory, params, sourceLanguage);
				Block block = new BlockParser(startElementContext, nestedBlockId, styleDefinitions).parse();
				if (block.isHidden()) {
					documentPartEvents.addAll(block.getEvents());
					continue;
				}

				BlockTextUnitMapper mapper = new BlockTextUnitMapper(block, textUnitId);
				if (mapper.getTextUnits().isEmpty() || !isCurrentBlockTranslatable()) {
					addBlockChunksToDocumentPart(block.getChunks());
				}
				else {
					for (ITextUnit tu : mapper.getTextUnits()) {
						filterEvents.add(new Event(EventType.TEXT_UNIT, tu));
					}
				}
			}
			else if (isBlockMarkupStartEvent(e)) {
				addMarkupComponentToDocumentPart(createStartMarkupComponent(eventFactory, e.asStartElement()));
			}
			else if (isTablePropertiesStartEvent(e)) {
				StartElementContext startElementContext = createStartElementContext(e.asStartElement(), xmlReader, eventFactory, params);
				addMarkupComponentToDocumentPart(MarkupComponentParser.parseBlockProperties(startElementContext, tablePropertiesChangeElementSkipper));
			}
			else if (isTextBodyPropertiesStartEvent(e)) {
				StartElementContext startElementContext = createStartElementContext(e.asStartElement(), xmlReader, eventFactory, params);
				addMarkupComponentToDocumentPart(MarkupComponentParser.parseBlockProperties(startElementContext, noElementSkipper));
			}
			else if (isBlockMarkupEndEvent(e)) {
				addMarkupComponentToDocumentPart(createEndMarkupComponent(e.asEndElement()));
			}
			else if (e.isStartElement() && revisionPropertyChangeElementSkipper.canSkip(e.asStartElement(), parentStartElement)) {
				StartElementContext startElementContext = createStartElementContext(e.asStartElement(), parentStartElement, xmlReader, eventFactory, params, null);
				revisionPropertyChangeElementSkipper.skip(startElementContext);
			}
			else {
				if (isSectionPropertiesStartEvent(e)) {
					e = stripSectionPropertiesRevisionAttributes(createStartElementContext(e.asStartElement(), null, eventFactory, params));
				} else if (isTableRowStartEvent(e)) {
					e = stripTableRowRevisionAttributes(createStartElementContext(e.asStartElement(), null, eventFactory, params));
				}
				addEventToDocumentPart(e);
			}

			if (isTableGridStartEvent(e)) {
				parentStartElement = e.asStartElement();
			} else if (isTableGridEndEvent(e)) {
				parentStartElement = null;
			}
		}
		flushDocumentPart();
		filterEvents.add(new Event(EventType.END_DOCUMENT, new Ending(subDocumentId)));
		filterEventIterator = filterEvents.iterator();
	}

	protected void preHandleNextEvent(XMLEvent e) {
		// can be overridden
	}

	protected boolean isCurrentBlockTranslatable() {
		// can be overridden
		// here blocks are always translatable
		return true;
	}

	private void addMarkupComponentToDocumentPart(MarkupComponent markupComponent) {
		if (!documentPartEvents.isEmpty()) {
			markup.addComponent(createGeneralMarkupComponent(documentPartEvents));
			documentPartEvents = new ArrayList<>();
		}
		markup.addComponent(markupComponent);
	}

	private void addBlockChunksToDocumentPart(List<Chunk> chunks) {
		for (Chunk chunk : chunks) {
			if (chunk instanceof Markup) {
				for (MarkupComponent markupComponent : ((Markup) chunk).getComponents()) {
					addMarkupComponentToDocumentPart(markupComponent);
				}
				continue;
			}

			documentPartEvents.addAll(chunk.getEvents());
		}
	}

	@Override
	public boolean hasNext() {
		return filterEventIterator.hasNext();
	}

	@Override
	public Event next() {
		return filterEventIterator.next();
	}

	@Override
	public void close() {
	}

	@Override
	public void logEvent(Event e) {
	}
}
