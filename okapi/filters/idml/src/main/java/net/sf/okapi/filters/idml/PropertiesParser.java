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

import static net.sf.okapi.filters.idml.ParsingIdioms.UNEXPECTED_STRUCTURE;

class PropertiesParser {

    private final StartElement startElement;
    private final XMLEventReader eventReader;

    PropertiesParser(StartElement startElement, XMLEventReader eventReader) {
        this.startElement = startElement;
        this.eventReader = eventReader;
    }

    Properties parse() throws XMLStreamException {
        Properties.PropertiesBuilder propertiesBuilder = new Properties.PropertiesBuilder();

        propertiesBuilder.setStartElement(startElement);

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextTag();

            if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                propertiesBuilder.setEndElement(event.asEndElement());

                return propertiesBuilder.build();
            }

            if (!event.isStartElement()) {
                throw new IllegalStateException(UNEXPECTED_STRUCTURE);
            }

            propertiesBuilder.addProperty(new PropertyParserRange(event.asStartElement(), eventReader).parse());
        }

        throw new IllegalStateException(UNEXPECTED_STRUCTURE);
    }
}
