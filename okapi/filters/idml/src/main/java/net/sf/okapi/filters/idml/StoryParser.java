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
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import static net.sf.okapi.filters.idml.ParsingIdioms.UNEXPECTED_STRUCTURE;

class StoryParser {

    private static final QName WRAPPING_STORY = Namespaces.getIdPackageNamespace().getQName("Story");
    private static final QName STORY = Namespaces.getDefaultNamespace().getQName("Story");

    private final Parameters parameters;
    private final XMLEventFactory eventFactory;
    private final XMLEventReader eventReader;

    StoryParser(Parameters parameters, XMLEventFactory eventFactory, XMLEventReader eventReader) {
        this.parameters = parameters;
        this.eventFactory = eventFactory;
        this.eventReader = eventReader;
    }

    Story parse() throws XMLStreamException {
        Story.StoryBuilder storyBuilder = new Story.StoryBuilder();

        storyBuilder.setStartDocumentEvent(parseStartDocumentEvent());
        storyBuilder.setWrappingStoryStartElement(parseNextStartElement(WRAPPING_STORY));
        storyBuilder.setStoryStartElement(parseNextStartElement(STORY));

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextTag();

            if (event.isEndElement() && WRAPPING_STORY.equals(event.asEndElement().getName())) {
                storyBuilder.setWrappingStoryEndElement(event.asEndElement());
                break;
            }

            if (event.isEndElement() && STORY.equals(event.asEndElement().getName())) {
                storyBuilder.setStoryEndElement(event.asEndElement());
                continue;
            }

            if (!event.isStartElement()) {
                throw new IllegalStateException(UNEXPECTED_STRUCTURE);
            }

            storyBuilder.addChildElements(new StoryChildElementsParser(event.asStartElement(), parameters, eventFactory, eventReader).parse());
        }

        storyBuilder.setEndDocumentEvent(parseEndDocumentEvent());

        return storyBuilder.build();
    }

    private XMLEvent parseStartDocumentEvent() throws XMLStreamException {
        XMLEvent event = eventReader.nextEvent();

        if (!event.isStartDocument()) {
            throw new IllegalStateException(UNEXPECTED_STRUCTURE);
        }

        return event;
    }

    private StartElement parseNextStartElement(QName nextStartElementName) throws XMLStreamException {
        XMLEvent event = eventReader.nextTag();

        if (!event.isStartElement() || !nextStartElementName.equals(event.asStartElement().getName())) {
            throw new IllegalStateException(UNEXPECTED_STRUCTURE);
        }

        return event.asStartElement();
    }

    private XMLEvent parseEndDocumentEvent() throws XMLStreamException {
        XMLEvent event = eventReader.nextEvent();

        if (!event.isEndDocument()) {
            throw new IllegalStateException(UNEXPECTED_STRUCTURE);
        }

        return event;
    }
}
