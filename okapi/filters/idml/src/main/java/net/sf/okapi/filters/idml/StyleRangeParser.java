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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import static net.sf.okapi.filters.idml.ParsingIdioms.PROPERTIES;
import static net.sf.okapi.filters.idml.ParsingIdioms.UNEXPECTED_STRUCTURE;
import static net.sf.okapi.filters.idml.ParsingIdioms.getStartElementAttributes;
import static net.sf.okapi.filters.idml.ParsingIdioms.peekNextStartElement;
import static net.sf.okapi.filters.idml.Properties.getEmptyProperties;

class StyleRangeParser {

    private final StartElement startElement;
    private final XMLEventReader eventReader;

    StyleRangeParser(StartElement startElement, XMLEventReader eventReader) {
        this.startElement = startElement;
        this.eventReader = eventReader;
    }

    StyleRange parse() throws XMLStreamException {
        StyleRange.StyleRangeBuilder styleRangeBuilder = new StyleRange.StyleRangeBuilder();

        styleRangeBuilder.setAttributes(getStartElementAttributes(startElement));

        XMLEvent nextEvent = eventReader.peek();

        if (nextEvent.isEndElement()) {
            if (!nextEvent.asEndElement().getName().equals(startElement.getName())) {
                throw new IllegalStateException(UNEXPECTED_STRUCTURE);
            }

            styleRangeBuilder.setProperties(getEmptyProperties());

            return styleRangeBuilder.build();
        }

        StartElement nextStartElement = peekNextStartElement(eventReader);

        if (!PROPERTIES.equals(nextStartElement.getName())) {
            styleRangeBuilder.setProperties(getEmptyProperties());

            return styleRangeBuilder.build();
        }

        styleRangeBuilder.setProperties(new PropertiesParser(eventReader.nextTag().asStartElement(), eventReader).parse());

        return styleRangeBuilder.build();
    }
}
