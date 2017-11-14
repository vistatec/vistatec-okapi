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

package net.sf.okapi.filters.idml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class StyleRangeEventsGenerator {

    private static final QName PARAGRAPH_STYLE_RANGE = Namespaces.getDefaultNamespace().getQName("ParagraphStyleRange");
    private static final QName CHARACTER_STYLE_RANGE = Namespaces.getDefaultNamespace().getQName("CharacterStyleRange");

    private final XMLEventFactory eventFactory;

    StyleRangeEventsGenerator(XMLEventFactory eventFactory) {
        this.eventFactory = eventFactory;
    }

    List<XMLEvent> generateParagraphStyleRangeStart(StyleDefinitions styleDefinitions) {
        return generateStyleRangeStart(PARAGRAPH_STYLE_RANGE, styleDefinitions.getParagraphStyleRange());
    }

    List<XMLEvent> generateParagraphStyleRangeEnd() {
        return generateStyleRangeEnd(PARAGRAPH_STYLE_RANGE);
    }

    List<XMLEvent> generateCharacterStyleRangeStart(StyleDefinitions styleDefinitions) {
        return generateStyleRangeStart(CHARACTER_STYLE_RANGE, styleDefinitions.getCharacterStyleRange());
    }

    List<XMLEvent> generateCharacterStyleRangeEnd() {
        return generateStyleRangeEnd(CHARACTER_STYLE_RANGE);
    }

    private List<XMLEvent> generateStyleRangeStart(QName name, StyleRange styleRange) {
        List<XMLEvent> events = new ArrayList<>();

        events.add(eventFactory.createStartElement(name, styleRange.getAttributes().iterator(), null));
        events.addAll(styleRange.getProperties().getEvents());

        return events;
    }

    private List<XMLEvent> generateStyleRangeEnd(QName name) {
        return Collections.singletonList((XMLEvent) eventFactory.createEndElement(name, null));
    }
}
