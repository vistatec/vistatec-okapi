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

import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

class Story {

    private final XMLEvent startDocumentEvent;
    private final StartElement wrappingStoryStartElement;
    private final StartElement storyStartElement;
    private final List<StoryChildElement> childElements;
    private final EndElement storyEndElement;
    private final EndElement wrappingStoryEndElement;
    private final XMLEvent endDocumentEvent;

    Story(XMLEvent startDocumentEvent, StartElement wrappingStoryStartElement, StartElement storyStartElement,
          List<StoryChildElement> childElements, EndElement storyEndElement, EndElement wrappingStoryEndElement, XMLEvent endDocumentEvent) {
        this.startDocumentEvent = startDocumentEvent;
        this.wrappingStoryStartElement = wrappingStoryStartElement;
        this.storyStartElement = storyStartElement;
        this.childElements = childElements;
        this.storyEndElement = storyEndElement;
        this.wrappingStoryEndElement = wrappingStoryEndElement;
        this.endDocumentEvent = endDocumentEvent;
    }

    XMLEvent getStartDocumentEvent() {
        return startDocumentEvent;
    }

    StartElement getWrappingStoryStartElement() {
        return wrappingStoryStartElement;
    }

    StartElement getStoryStartElement() {
        return storyStartElement;
    }

    List<StoryChildElement> getChildElements() {
        return childElements;
    }

    EndElement getStoryEndElement() {
        return storyEndElement;
    }

    EndElement getWrappingStoryEndElement() {
        return wrappingStoryEndElement;
    }

    XMLEvent getEndDocumentEvent() {
        return endDocumentEvent;
    }

    static class StoryBuilder implements Builder<Story> {

        private XMLEvent startDocumentEvent;
        private StartElement wrappingStoryStartElement;
        private StartElement storyStartElement;
        private List<StoryChildElement> childElements = new ArrayList<>();
        private EndElement storyEndElement;
        private EndElement wrappingStoryEndElement;
        private XMLEvent endDocumentEvent;

        StoryBuilder setStartDocumentEvent(XMLEvent startDocument) {
            this.startDocumentEvent = startDocument;
            return this;
        }

        StoryBuilder setWrappingStoryStartElement(StartElement wrappingStoryStartElement) {
            this.wrappingStoryStartElement = wrappingStoryStartElement;
            return this;
        }

        StoryBuilder setStoryStartElement(StartElement storyStartElement) {
            this.storyStartElement = storyStartElement;
            return this;
        }

        StoryBuilder addChildElements(List<StoryChildElement> storyChildElements) {
            childElements.addAll(storyChildElements);
            return this;
        }

        StoryBuilder setStoryEndElement(EndElement storyEndElement) {
            this.storyEndElement = storyEndElement;
            return this;
        }

        StoryBuilder setWrappingStoryEndElement(EndElement wrappingStoryEndElement) {
            this.wrappingStoryEndElement = wrappingStoryEndElement;
            return this;
        }

        StoryBuilder setEndDocumentEvent(XMLEvent endDocumentEvent) {
            this.endDocumentEvent = endDocumentEvent;
            return this;
        }

        @Override
        public Story build() {
            return new Story(startDocumentEvent, wrappingStoryStartElement, storyStartElement, childElements, storyEndElement, wrappingStoryEndElement, endDocumentEvent);
        }
    }
}
