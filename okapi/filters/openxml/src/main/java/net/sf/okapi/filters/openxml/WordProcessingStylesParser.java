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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.Reader;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static net.sf.okapi.filters.openxml.StartElementContextFactory.createStartElementContext;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.DEFAULT_BOOLEAN_ATTRIBUTE_FALSE_VALUE;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.WPML_VAL;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.getAttributeValue;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.getBooleanAttributeValue;

/**
 * Provides a word processing styles parser.
 */
class WordProcessingStylesParser implements StyleDefinitionsParser {

    private static final QName RPR_DEFAULT = Namespaces.WordProcessingML.getQName("rPrDefault");
    private static final QName RPR = Namespaces.WordProcessingML.getQName("rPr");
    private static final QName STYLE = Namespaces.WordProcessingML.getQName("style");
    private static final QName STYLE_TYPE = Namespaces.WordProcessingML.getQName("type");
    private static final QName STYLE_ID = Namespaces.WordProcessingML.getQName("styleId");
    private static final QName STYLE_DEFAULT = Namespaces.WordProcessingML.getQName("default");
    private static final QName STYLE_BASED_ON = Namespaces.WordProcessingML.getQName("basedOn");
    private static final QName STYLE_LINK = Namespaces.WordProcessingML.getQName("link");

    private static final EnumSet<StyleType> PARSABLE_STYLE_TYPES = EnumSet.range(StyleType.PARAGRAPH, StyleType.CHARACTER);

    private XMLEventFactory eventFactory;
    private XMLInputFactory inputFactory;
    private Reader reader;
    private ConditionalParameters conditionalParameters;

    public WordProcessingStylesParser(XMLEventFactory eventFactory,
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

        RunProperties documentDefaultRunProperties = null;
        Map<StyleType, String> defaultStylesByStyleTypes = new EnumMap<>(StyleType.class);
        Map<String, StyleType> styleTypesByStyleIds = new HashMap<>();
        Map<String, String> parentStylesByStyleIds = new HashMap<>();
        Map<String, String> linkedStylesByStyleIds = new HashMap<>();
        Map<String, RunProperties> runPropertiesByStyleIds = new HashMap<>();

        try {
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (!event.isStartElement()) {
                    continue;
                }

                StartElement element = event.asStartElement();

                if (RPR_DEFAULT.equals(element.getName())) {
                    documentDefaultRunProperties = parseDefaultRunProperties(element, eventReader);
                    continue;
                }

                if (STYLE.equals(element.getName())) {
                    StyleData styleData = parseStyle(element, eventReader);

                    if (null == styleData) {
                        continue;
                    }

                    if (styleData.shouldBeDefault()) {
                        defaultStylesByStyleTypes.put(styleData.getType(), styleData.getId());
                    }

                    styleTypesByStyleIds.put(styleData.getId(), styleData.getType());
                    parentStylesByStyleIds.put(styleData.getId(), styleData.getParentId());
                    linkedStylesByStyleIds.put(styleData.getId(), styleData.getLinkedStyleId());
                    runPropertiesByStyleIds.put(styleData.getId(), styleData.getRunProperties());
                }
            }
        } finally {
            reader.close();
        }

        if (null == documentDefaultRunProperties) {
            documentDefaultRunProperties = RunProperties.emptyRunProperties();
        }

        return new WordStyleDefinitions(
                documentDefaultRunProperties,
                defaultStylesByStyleTypes,
                styleTypesByStyleIds,
                parentStylesByStyleIds,
                linkedStylesByStyleIds,
                runPropertiesByStyleIds);
    }

    private RunProperties parseDefaultRunProperties(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException {
        RunProperties runProperties = null;

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                break;
            }
            if (!event.isStartElement()) {
                continue;
            }

            StartElement element = event.asStartElement();

            if (RPR.equals(element.getName())) {
                runProperties = parseRunProperties(element, eventReader);
            }
        }

        if (null == runProperties) {
            runProperties = RunProperties.emptyRunProperties();
        }

        return runProperties;
    }

    private RunProperties parseRunProperties(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException {
        StartElementContext startElementContext = createStartElementContext(startElement, eventReader, eventFactory, conditionalParameters);
        return new RunPropertiesParser(startElementContext).parse();
    }

    private StyleData parseStyle(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException {
        String attributeValue = getAttributeValue(startElement, STYLE_TYPE);
        StyleType styleType = null == attributeValue
                ? StyleType.PARAGRAPH
                : StyleType.fromValue(attributeValue);

        if (!PARSABLE_STYLE_TYPES.contains(styleType)) {
            // skip parsing of types which do not contain run properties information
            return null;
        }

        boolean shouldBeDefault = getBooleanAttributeValue(startElement, STYLE_DEFAULT, DEFAULT_BOOLEAN_ATTRIBUTE_FALSE_VALUE);

        String styleId = getAttributeValue(startElement, STYLE_ID);
        String parentStyleId = null;
        String linkedStyleId = null;

        RunProperties runProperties = null;

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                break;
            }
            if (!event.isStartElement()) {
                continue;
            }

            StartElement element = event.asStartElement();

            if (STYLE_BASED_ON.equals(element.getName())) {
                parentStyleId = getAttributeValue(element, WPML_VAL);
                continue;
            }
            if (STYLE_LINK.equals(element.getName())) {
                linkedStyleId = getAttributeValue(element, WPML_VAL);
                continue;
            }
            if (RPR.equals(element.getName())) {
                runProperties = parseRunProperties(element, eventReader);
            }
        }

        if (null == runProperties) {
            runProperties = RunProperties.emptyRunProperties();
        }

        return new StyleData(styleType, shouldBeDefault, styleId, parentStyleId, linkedStyleId, runProperties);
    }

    private static class StyleData {

        private final StyleType type;
        private final boolean shouldBeDefault;
        private final String id;
        private final String parentId;
        private final String linkedStyleId;
        private final RunProperties runProperties;

        public StyleData(StyleType type, boolean shouldBeDefault, String id, String parentId, String linkedStyleId, RunProperties runProperties) {
            this.type = type;
            this.shouldBeDefault = shouldBeDefault;
            this.id = id;
            this.parentId = parentId;
            this.linkedStyleId = linkedStyleId;
            this.runProperties = runProperties;
        }

        public StyleType getType() {
            return type;
        }

        public boolean shouldBeDefault() {
            return shouldBeDefault;
        }

        public String getId() {
            return id;
        }

        public String getParentId() {
            return parentId;
        }

        public String getLinkedStyleId() {
            return linkedStyleId;
        }

        public RunProperties getRunProperties() {
            return runProperties;
        }
    }
}
