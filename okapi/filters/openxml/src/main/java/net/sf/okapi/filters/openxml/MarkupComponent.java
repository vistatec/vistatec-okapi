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
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_ALIGNMENT;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_PARA;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_PRESENTATION;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_SHEET_VIEW;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_TABLE;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_TEXT_BODY;

/**
 * Provides a markup component.
 */
abstract class MarkupComponent implements XMLEvents {

    static boolean isSheetViewMarkupComponent(MarkupComponent markupComponent) {
        return markupComponent instanceof StartMarkupComponent
                && LOCAL_SHEET_VIEW.equals(((StartMarkupComponent) markupComponent).getName().getLocalPart());
    }

    static boolean isAlignmentEmptyElementMarkupComponent(MarkupComponent markupComponent) {
        return markupComponent instanceof EmptyElementMarkupComponent
                && LOCAL_ALIGNMENT.equals(((EmptyElementMarkupComponent) markupComponent).getName().getLocalPart());
    }

    static boolean isPresentationStartMarkupComponent(MarkupComponent markupComponent) {
        return markupComponent instanceof StartMarkupComponent
                && LOCAL_PRESENTATION.equals(((StartMarkupComponent) markupComponent).getName().getLocalPart());
    }

    static boolean isTableStartMarkupComponent(MarkupComponent markupComponent) {
        return markupComponent instanceof StartMarkupComponent
                && LOCAL_TABLE.equals(((StartMarkupComponent) markupComponent).getName().getLocalPart());
    }

    static boolean isTextBodyStartMarkupComponent(MarkupComponent markupComponent) {
        return markupComponent instanceof StartMarkupComponent
                && LOCAL_TEXT_BODY.equals(((StartMarkupComponent) markupComponent).getName().getLocalPart());
    }

    static boolean isParagraphStartMarkupComponent(MarkupComponent markupComponent) {
        return markupComponent instanceof StartMarkupComponent
                && LOCAL_PARA.equals(((StartMarkupComponent) markupComponent).getName().getLocalPart());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + XMLEventSerializer.serialize(getEvents()) + ")";
    }

    /**
     * Provides a start markup component.
     */
    static class StartMarkupComponent extends MarkupComponent implements Nameable {
        private XMLEventFactory eventFactory;
        private StartElement startElement;
        private List<Attribute> attributes = new ArrayList<>();

        StartMarkupComponent(XMLEventFactory eventFactory, StartElement startElement) {
            this.eventFactory = eventFactory;
            this.startElement = startElement;

            Iterator iterator = startElement.getAttributes();

            while (iterator.hasNext()) {
                attributes.add((Attribute) iterator.next());
            }
        }

        @Override
        public List<XMLEvent> getEvents() {
            return Collections.singletonList((XMLEvent) eventFactory.createStartElement(startElement.getName(), getAttributes().iterator(), startElement.getNamespaces()));
        }

        @Override
        public QName getName() {
            return startElement.getName();
        }

        List<Attribute> getAttributes() {
            return attributes;
        }
    }

    /**
     * Provides an end markup component.
     */
    static class EndMarkupComponent extends MarkupComponent {
        private EndElement endElement;

        EndMarkupComponent(EndElement endElement) {
            this.endElement = endElement;
        }

        @Override
        public List<XMLEvent> getEvents() {
            return Collections.singletonList((XMLEvent) endElement);
        }
    }

    /**
     * Provides an empty element markup component.
     */
    static class EmptyElementMarkupComponent extends MarkupComponent implements Nameable {
        private static final int EMPTY_ELEMENT_EVENTS_SIZE = 2;

        private XMLEventFactory eventFactory;
        private StartElement startElement;
        private EndElement endElement;
        private List<Attribute> attributes = new ArrayList<>();

        EmptyElementMarkupComponent(XMLEventFactory eventFactory, StartElement startElement, EndElement endElement) {
            this.eventFactory = eventFactory;
            this.startElement = startElement;
            this.endElement = endElement;

            Iterator iterator = startElement.getAttributes();

            while (iterator.hasNext()) {
                attributes.add((Attribute) iterator.next());
            }
        }

        @Override
        public List<XMLEvent> getEvents() {
            List<XMLEvent> events = new ArrayList<>(EMPTY_ELEMENT_EVENTS_SIZE);

            events.add(eventFactory.createStartElement(startElement.getName(), getAttributes().iterator(), startElement.getNamespaces()));
            events.add(endElement);

            return events;
        }

        @Override
        public QName getName() {
            return startElement.getName();
        }

        List<Attribute> getAttributes() {
            return attributes;
        }
    }

    /**
     * Provides a general markup component.
     */
    static class GeneralMarkupComponent extends MarkupComponent {
        private List<XMLEvent> events;

        GeneralMarkupComponent(List<XMLEvent> events) {
            this.events = events;
        }

        @Override
        public List<XMLEvent> getEvents() {
            return events;
        }
    }
}
