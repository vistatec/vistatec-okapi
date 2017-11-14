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
import java.util.Arrays;

import static net.sf.okapi.filters.idml.ParsingIdioms.SELF;
import static net.sf.okapi.filters.idml.ParsingIdioms.VISIBLE;

class DesignMapParser {

    private static final QName DOCUMENT = Namespaces.getDefaultNamespace().getQName("Document");
    private static final QName STORY_LIST = Namespaces.getDefaultNamespace().getQName("StoryList");
    private static final QName ACTIVE_LAYER = Namespaces.getDefaultNamespace().getQName("ActiveLayer");

    private static final QName SRC = Namespaces.getDefaultNamespace().getQName("src");

    private static final QName GRAPHIC = Namespaces.getIdPackageNamespace().getQName("Graphic");
    private static final QName FONTS = Namespaces.getIdPackageNamespace().getQName("Fonts");
    private static final QName STYLES = Namespaces.getIdPackageNamespace().getQName("Styles");
    private static final QName PREFERENCES = Namespaces.getIdPackageNamespace().getQName("Preferences");
    private static final QName TAGS = Namespaces.getIdPackageNamespace().getQName("Tags");

    private static final QName LAYER = Namespaces.getDefaultNamespace().getQName("Layer");

    private static final QName MASTER_SPREAD = Namespaces.getIdPackageNamespace().getQName("MasterSpread");
    private static final QName SPREAD = Namespaces.getIdPackageNamespace().getQName("Spread");

    private static final QName BACKING_STORY = Namespaces.getIdPackageNamespace().getQName("BackingStory");
    private static final QName STORY = Namespaces.getIdPackageNamespace().getQName("Story");

    private final Reader reader;
    private final XMLInputFactory inputFactory;

    DesignMapParser(Reader reader, XMLInputFactory inputFactory) {
        this.reader = reader;
        this.inputFactory = inputFactory;
    }

    DesignMap parse() throws XMLStreamException, IOException {
        XMLEventReader eventReader = inputFactory.createXMLEventReader(reader);
        DesignMap.DesignMapBuilder designMapBuilder = new DesignMap.DesignMapBuilder();

        try {
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (!event.isStartElement()) {
                    continue;
                }

                StartElement element = event.asStartElement();

                if (DOCUMENT.equals(element.getName())) {
                    designMapBuilder = parseDocument(element, designMapBuilder);
                    continue;
                }

                if (GRAPHIC.equals(element.getName())) {
                    designMapBuilder.setGraphicPartName(element.getAttributeByName(SRC).getValue());
                    continue;
                }

                if (FONTS.equals(element.getName())) {
                    designMapBuilder.setFontsPartName(element.getAttributeByName(SRC).getValue());
                    continue;
                }

                if (STYLES.equals(element.getName())) {
                    designMapBuilder.setStylesPartName(element.getAttributeByName(SRC).getValue());
                    continue;
                }

                if (PREFERENCES.equals(element.getName())) {
                    designMapBuilder.setPreferencesPartName(element.getAttributeByName(SRC).getValue());
                    continue;
                }

                if (TAGS.equals(element.getName())) {
                    designMapBuilder.setTagsPartName(element.getAttributeByName(SRC).getValue());
                    continue;
                }

                if (LAYER.equals(element.getName())) {
                    designMapBuilder = parseLayer(element, designMapBuilder);
                    continue;
                }

                if (MASTER_SPREAD.equals(element.getName())) {
                    designMapBuilder.addMasterSpreadPartName(element.getAttributeByName(SRC).getValue());
                    continue;
                }

                if (SPREAD.equals(element.getName())) {
                    designMapBuilder.addSpreadPartName(element.getAttributeByName(SRC).getValue());
                    continue;
                }

                if (BACKING_STORY.equals(element.getName())) {
                    designMapBuilder.setBackingStoryPartName(element.getAttributeByName(SRC).getValue());
                    continue;
                }

                if (STORY.equals(element.getName())) {
                    designMapBuilder.addStoryPartName(element.getAttributeByName(SRC).getValue());
                }
            }
        } finally {
            reader.close();
        }

        return designMapBuilder.build();
    }

    private DesignMap.DesignMapBuilder parseDocument(StartElement element, DesignMap.DesignMapBuilder designMapBuilder) {
        designMapBuilder.setId(element.getAttributeByName(SELF).getValue());
        designMapBuilder.setStoryIds(Arrays.asList(element.getAttributeByName(STORY_LIST).getValue().split(" ")));
        designMapBuilder.setActiveLayerId(element.getAttributeByName(ACTIVE_LAYER).getValue());

        return designMapBuilder;
    }

    private DesignMap.DesignMapBuilder parseLayer(StartElement element, DesignMap.DesignMapBuilder designMapBuilder) {
        return designMapBuilder.addLayer(new Layer(element.getAttributeByName(SELF).getValue(),
                Boolean.parseBoolean(element.getAttributeByName(VISIBLE).getValue())));
    }
}
