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

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.List;

/**
 * Provides a markup component factory.
 */
class MarkupComponentFactory {

    /**
     * Creates a start markup component.
     *
     * @param eventFactory An event factory
     * @param startElement A start element
     *
     * @return A start markup component
     */
    static MarkupComponent createStartMarkupComponent(XMLEventFactory eventFactory, StartElement startElement) {
        return new MarkupComponent.StartMarkupComponent(eventFactory, startElement);
    }

    /**
     * Creates an end markup component.
     *
     * @param endElement An end element
     *
     * @return An end markup component
     */
    static MarkupComponent createEndMarkupComponent(EndElement endElement) {
        return new MarkupComponent.EndMarkupComponent(endElement);
    }

    /**
     * Creates an empty element markup component.
     *
     * @param eventFactory An event factory
     * @param startElement A start element
     * @param endElement   An end element
     *
     * @return A empty element markup component
     */
    static MarkupComponent createEmptyElementMarkupComponent(XMLEventFactory eventFactory, StartElement startElement, EndElement endElement) {
        return new MarkupComponent.EmptyElementMarkupComponent(eventFactory, startElement, endElement);
    }

    /**
     * Creates a general markup component.
     *
     * @param events Events
     *
     * @return A general markup component
     */
    static MarkupComponent createGeneralMarkupComponent(List<XMLEvent> events) {
        return new MarkupComponent.GeneralMarkupComponent(events);
    }
}
