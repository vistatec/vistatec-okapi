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
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.DocumentPart;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createEndMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createGeneralMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createStartMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponentParser.parseEmptyElementMarkupComponent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isAlignmentStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isPresentationEndEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isPresentationStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isSheetViewEndEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isSheetViewStartEvent;

/**
 * Provides a clarifiable part handler.
 */
class ClarifiablePartHandler extends NonTranslatablePartHandler {

    ClarifiablePartHandler(OpenXMLZipFile zipFile, ZipEntry entry) {
        super(zipFile, entry);
    }

    @Override
    public Event open(String documentId, String subDocumentId, LocaleId srcLang) throws IOException, XMLStreamException {
        XMLEventReader xmlEventReader = getZipFile().getInputFactory().createXMLEventReader(
                new InputStreamReader(new BufferedInputStream(getInputStream()), StandardCharsets.UTF_8));

        return open(xmlEventReader);
    }

    public Event open(InputStream is) throws IOException, XMLStreamException {
        XMLEventReader xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(
                new InputStreamReader(new BufferedInputStream(is), StandardCharsets.UTF_8));

        return open(xmlEventReader);
    }

    private Event open(XMLEventReader xmlEventReader) throws XMLStreamException {
        DocumentPart documentPart;

        try {
            documentPart = handlePart(xmlEventReader);
        } finally {
            if (null != xmlEventReader) {
                xmlEventReader.close();
            }
        }

        return new Event(EventType.DOCUMENT_PART, documentPart);
    }

    private InputStream getInputStream() throws IOException, XMLStreamException {
        String modifiedContent = getModifiedContent();

        return null == modifiedContent
                ? getZipFile().getInputStream(getEntry())
                : new ByteArrayInputStream(modifiedContent.getBytes(StandardCharsets.UTF_8));
    }

    private DocumentPart handlePart(XMLEventReader xmlEventReader) throws XMLStreamException {
        MarkupBuilder markupBuilder = new MarkupBuilder();

        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (isPresentationStartEvent(event)
                    || isSheetViewStartEvent(event)) {
                markupBuilder.addMarkupComponent(createStartMarkupComponent(getZipFile().getEventFactory(), event.asStartElement()));
            } else if (isPresentationEndEvent(event)
                    || isSheetViewEndEvent(event)) {
                markupBuilder.addMarkupComponent(createEndMarkupComponent(event.asEndElement()));
            } else if (isAlignmentStartEvent(event)) {
                markupBuilder.addMarkupComponent(parseEmptyElementMarkupComponent(xmlEventReader, getZipFile().getEventFactory(), event.asStartElement()));
            } else {
                markupBuilder.addEvent(event);
            }
        }

        DocumentPart documentPart = new DocumentPart(getEntry().getName(), false);
        documentPart.setSkeleton(new MarkupZipSkeleton(getZipFile().getZip(), getEntry(), markupBuilder.build()));

        return documentPart;
    }

    private static class MarkupBuilder {
        private List<XMLEvent> events = new ArrayList<>();
        private Markup markup = new Block.BlockMarkup();

        void addEvent(XMLEvent event) {
            events.add(event);
        }

        void addMarkupComponent(MarkupComponent markupComponent) {
            flushEvents();
            markup.addComponent(markupComponent);
        }

        private void flushEvents() {
            if (!events.isEmpty()) {
                markup.addComponent(createGeneralMarkupComponent(events));
                events = new ArrayList<>();
            }
        }

        Markup build() {
            flushEvents();
            return markup;
        }
    }
}
