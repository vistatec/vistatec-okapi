/*
 * =============================================================================
 *   Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.idml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.Reader;

class PreferencesParser {

    private static final QName XML_PREFERENCE = Namespaces.getDefaultNamespace().getQName("XMLPreference");

    private static final QName DEFAULT_STORY_TAG_NAME = Namespaces.getDefaultNamespace().getQName("DefaultStoryTagName");
    private static final QName DEFAULT_TABLE_TAG_NAME = Namespaces.getDefaultNamespace().getQName("DefaultTableTagName");
    private static final QName DEFAULT_CELL_TAG_NAME = Namespaces.getDefaultNamespace().getQName("DefaultCellTagName");

    private static final QName STORY_PREFERENCE = Namespaces.getDefaultNamespace().getQName("StoryPreference");
    private static final QName STORY_DIRECTION = Namespaces.getDefaultNamespace().getQName("StoryDirection");

    private final Reader reader;
    private final XMLInputFactory inputFactory;

    PreferencesParser(Reader reader, XMLInputFactory inputFactory) {
        this.reader = reader;
        this.inputFactory = inputFactory;
    }

    Preferences parse() throws XMLStreamException, IOException {
        XMLEventReader eventReader = inputFactory.createXMLEventReader(reader);
        Preferences.PreferencesBuilder preferencesBuilder = new Preferences.PreferencesBuilder();

        try {
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (!event.isStartElement()) {
                    continue;
                }

                StartElement element = event.asStartElement();

                if (XML_PREFERENCE.equals(element.getName())) {
                    preferencesBuilder = parseXmlPreference(element, preferencesBuilder);
                    continue;
                }

                if (STORY_PREFERENCE.equals(element.getName())) {
                    preferencesBuilder = parseStoryPreference(element, preferencesBuilder);
                }
            }
        } finally {
            reader.close();
        }

        return preferencesBuilder.build();
    }

    private Preferences.PreferencesBuilder parseXmlPreference(StartElement element, Preferences.PreferencesBuilder preferencesBuilder) {
        return preferencesBuilder.setXmlPreference(new Preferences.XMLPreference(
                element.getAttributeByName(DEFAULT_STORY_TAG_NAME).getValue(),
                element.getAttributeByName(DEFAULT_TABLE_TAG_NAME).getValue(),
                element.getAttributeByName(DEFAULT_CELL_TAG_NAME).getValue()
        ));
    }

    private Preferences.PreferencesBuilder parseStoryPreference(StartElement element, Preferences.PreferencesBuilder preferencesBuilder) {
        return preferencesBuilder.setStoryPreference(new Preferences.StoryPreference(
                element.getAttributeByName(STORY_DIRECTION).getValue()
        ));
    }
}
