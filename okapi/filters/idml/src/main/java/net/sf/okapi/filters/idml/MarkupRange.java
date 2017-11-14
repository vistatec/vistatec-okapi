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
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

class MarkupRange implements Eventive {

    protected final List<XMLEvent> events;

    MarkupRange(List<XMLEvent> events) {
        this.events = events;
    }

    static MarkupRange getMarkupRange(List<XMLEvent> events) {
        return new MarkupRange(events);
    }

    static MarkupRange getMarkupRangeStartElement(StartElement startElement) {
        return new MarkupRangeStartElement(startElement);
    }

    static MarkupRange getMarkupRangeEndElement(EndElement endElement) {
        return new MarkupRangeEndElement(endElement);
    }

    @Override
    public List<XMLEvent> getEvents() {
        return events;
    }

    static class MarkupRangeBuilder implements Builder<MarkupRange> {
        protected List<XMLEvent> events = new ArrayList<>();

        MarkupRangeBuilder addEvents(List<XMLEvent> events) {
            this.events.addAll(events);
            return this;
        }

        @Override
        public MarkupRange build() {
            return new MarkupRange(events);
        }
    }

    static class MarkupRangeStartElement extends MarkupRange implements Nameable {

        private static final int START_ELEMENT_INDEX = 0;

        MarkupRangeStartElement(javax.xml.stream.events.StartElement startElement) {
            super(singletonList((XMLEvent) startElement));
        }

        @Override
        public QName getName() {
            return events.get(START_ELEMENT_INDEX).asStartElement().getName();
        }
    }

    static class MarkupRangeEndElement extends MarkupRange implements Nameable {

        private static final int END_ELEMENT_INDEX = 0;

        MarkupRangeEndElement(javax.xml.stream.events.EndElement endElement) {
            super(singletonList((XMLEvent) endElement));
        }

        @Override
        public QName getName() {
            return events.get(END_ELEMENT_INDEX).asEndElement().getName();
        }
    }

    static class MarkupRangeElement extends MarkupRange implements Nameable {

        private static final int START_ELEMENT_INDEX = 0;

        private int endElementIndex;

        MarkupRangeElement(StartElement startElement, List<XMLEvent> innerEvents, EndElement endElement) {
            super(asList(startElement, innerEvents, endElement));

            endElementIndex = events.size() - 1;
        }

        private static List<XMLEvent> asList(StartElement startElement, List<XMLEvent> innerEvents, EndElement endElement) {
            List<XMLEvent> events = new ArrayList<>(innerEvents.size() + 2);

            events.add(startElement);
            events.addAll(innerEvents);
            events.add(endElement);

            return events;
        }

        StartElement getStartElement() {
            return events.get(START_ELEMENT_INDEX).asStartElement();
        }

        List<XMLEvent> getInnerEvents() {
            return events.subList(START_ELEMENT_INDEX + 1, endElementIndex);
        }

        EndElement getEndElement() {
            return events.get(endElementIndex).asEndElement();
        }

        @Override
        public QName getName() {
            return events.get(START_ELEMENT_INDEX).asStartElement().getName();
        }

        static class MarkupRangeElementBuilder implements Builder<MarkupRangeElement> {

            protected StartElement startElement;
            protected List<XMLEvent> innerEvents = new ArrayList<>();
            protected EndElement endElement;

            MarkupRangeElementBuilder setStartElement(StartElement startElement) {
                this.startElement = startElement;
                return this;
            }

            MarkupRangeElementBuilder addInnerEvent(XMLEvent innerEvent) {
                innerEvents.add(innerEvent);
                return this;
            }

            MarkupRangeElementBuilder addInnerEvents(List<XMLEvent> innerEvents) {
                this.innerEvents.addAll(innerEvents);
                return this;
            }

            MarkupRangeElementBuilder setEndElement(EndElement endElement) {
                this.endElement = endElement;
                return this;
            }

            @Override
            public MarkupRangeElement build() {
                return new MarkupRangeElement(startElement, innerEvents, endElement);
            }
        }
    }
}
