/*
 * =============================================================================
 *   Copyright (C) 2010-2017 by the Okapi Framework contributors
 * -----------------------------------------------------------------------------
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =============================================================================
 */

package net.sf.okapi.filters.openxml;

import static java.util.Collections.singletonList;
import static net.sf.okapi.filters.openxml.RunProperties.emptyRunProperties;
import static net.sf.okapi.filters.openxml.RunPropertyFactory.createRunProperty;
import static net.sf.okapi.filters.openxml.StartElementContextFactory.createStartElementContext;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Provides a PresentationML notes styles parser.
 */
class PresentationNotesStylesParser implements StyleDefinitionsParser {

    private static final String UNEXPECTED_STRUCTURE = "Unexpected structure";

    private static final QName NOTES_STYLE = Namespaces.PresentationML.getQName("notesStyle");
    private static final QName DEFAULT_RPR = Namespaces.DrawingML.getQName("defRPr");

    // level QNames
    private static final List<QName> paragraphLevelNames = Arrays.asList(
            Namespaces.DrawingML.getQName("lvl1pPr"),
            Namespaces.DrawingML.getQName("lvl2pPr"),
            Namespaces.DrawingML.getQName("lvl3pPr"),
            Namespaces.DrawingML.getQName("lvl4pPr"),
            Namespaces.DrawingML.getQName("lvl5pPr"),
            Namespaces.DrawingML.getQName("lvl6pPr"),
            Namespaces.DrawingML.getQName("lvl7pPr"),
            Namespaces.DrawingML.getQName("lvl8pPr"),
            Namespaces.DrawingML.getQName("lvl9pPr"));

    private static final int INVALID_PARAGRAPH_LEVEL_ID = -1;

    static final String DEFAULT_PARAGRAPH_LEVEL_ID = "0";

    private XMLEventFactory eventFactory;
    private XMLInputFactory inputFactory;
    private Reader reader;
    private ConditionalParameters conditionalParameters;

    PresentationNotesStylesParser(
            XMLEventFactory eventFactory,
            XMLInputFactory inputFactory,
            Reader reader,
            ConditionalParameters conditionalParameters) {
        this.eventFactory = eventFactory;
        this.inputFactory = inputFactory;
        this.reader = reader;
        this.conditionalParameters = conditionalParameters;
    }

    @Override
    public StyleDefinitions parse() throws XMLStreamException, IOException {
        XMLEventReader eventReader = inputFactory.createXMLEventReader(reader);

        Map<String, RunProperties> runPropertiesByParagraphLevelIds = null;

        try {
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (!event.isStartElement()) {
                    continue;
                }

                StartElement element = event.asStartElement();

                if (NOTES_STYLE.equals(element.getName())) {
                    runPropertiesByParagraphLevelIds = parseNotesStyle(element, eventReader);
                    break;
                }
            }
        } finally {
            reader.close();
        }

        return new PresentationNotesStyleDefinitions(getExtraDocumentDefaultRunProperties(), runPropertiesByParagraphLevelIds);
    }

    private RunProperties getExtraDocumentDefaultRunProperties() {
        return new RunProperties.DefaultRunProperties(null, null, singletonList(createRunProperty(new QName(null, "baseline"), "0")));
    }

    private Map<String, RunProperties> parseNotesStyle(StartElement startElement, XMLEventReader reader) throws XMLStreamException {

        Map<String, RunProperties> runPropertiesByParagraphLevelIds = new HashMap<>(paragraphLevelNames.size());

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                if (runPropertiesByParagraphLevelIds.isEmpty()) {
                    runPropertiesByParagraphLevelIds.put(DEFAULT_PARAGRAPH_LEVEL_ID, emptyRunProperties());
                }

                return runPropertiesByParagraphLevelIds;
            }

            if (!event.isStartElement()) {
                continue;
            }

            StartElement element = event.asStartElement();

            int paragraphLevelId = getParagraphLevelIdByName(element.getName());

            if (INVALID_PARAGRAPH_LEVEL_ID == paragraphLevelId) {
                continue;
            }

            runPropertiesByParagraphLevelIds.put(String.valueOf(paragraphLevelId), parseParagraphLevelRunProperties(element, reader));
        }

        throw new IllegalStateException(UNEXPECTED_STRUCTURE);
    }

    private int getParagraphLevelIdByName(QName name) {
        for (int id = 0; id < paragraphLevelNames.size(); id++) {
            QName paragraphLevel = paragraphLevelNames.get(id);

            if (name.equals(paragraphLevel)) {
                return id;
            }
        }

        return INVALID_PARAGRAPH_LEVEL_ID;
    }

    private RunProperties parseParagraphLevelRunProperties(StartElement startElement, XMLEventReader reader) throws XMLStreamException {
        RunProperties runProperties = null;

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                if (null == runProperties) {
                    return emptyRunProperties();
                }

                return runProperties;
            }

            if (!event.isStartElement()) {
                continue;
            }

            StartElement element = event.asStartElement();

            if (DEFAULT_RPR.equals(element.getName())) {
                runProperties = parseDefaultRunProperties(element, reader);
            }
        }

        throw new IllegalStateException(UNEXPECTED_STRUCTURE);
    }

    private RunProperties parseDefaultRunProperties(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException {
        StartElementContext startElementContext = createStartElementContext(startElement, eventReader, eventFactory, conditionalParameters);

        return new RunPropertiesParser(startElementContext).parse();
    }
}