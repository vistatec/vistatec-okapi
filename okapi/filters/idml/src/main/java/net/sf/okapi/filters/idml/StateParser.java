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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import static net.sf.okapi.filters.idml.ParsingIdioms.SELF;
import static net.sf.okapi.filters.idml.ParsingIdioms.parseSpreadItems;

class StateParser {

    private static final QName ACTIVE = Namespaces.getDefaultNamespace().getQName("Active");

    private final StartElement startElement;
    private final String activeLayerId;
    private final XMLEventReader eventReader;

    StateParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
        this.startElement = startElement;
        this.activeLayerId = activeLayerId;
        this.eventReader = eventReader;
    }

    State parse() throws XMLStreamException {

        State.StateBuilder stateBuilder = new State.StateBuilder()
                .setId(startElement.getAttributeByName(SELF).getValue())
                .setActive(Boolean.parseBoolean(startElement.getAttributeByName(ACTIVE).getValue()))
                .setActiveLayerId(activeLayerId);

        parseSpreadItems(startElement, eventReader, stateBuilder);

        return stateBuilder.build();
    }
}
