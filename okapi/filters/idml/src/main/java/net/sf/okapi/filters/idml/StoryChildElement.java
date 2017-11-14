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
import java.util.Collections;
import java.util.List;

import static net.sf.okapi.filters.idml.Properties.getEmptyProperties;

class StoryChildElement extends MarkupRange.MarkupRangeElement {

    StoryChildElement(StartElement startElement, List<XMLEvent> events, EndElement endElement) {
        super(startElement, events, endElement);
    }

    static class StoryChildElementBuilder extends MarkupRangeElementBuilder {

        @Override
        public StoryChildElement build() {
            return new StoryChildElement(startElement, innerEvents, endElement);
        }
    }

    /**
     * Represents a styled text - Content, Br, etc.
     */
    static class StyledTextElement extends StoryChildElement {

        protected final StyleDefinitions styleDefinitions;

        StyledTextElement(StartElement startElement, List<XMLEvent> innerEvents, EndElement endElement, StyleDefinitions styleDefinitions) {
            super(startElement, innerEvents, endElement);
            this.styleDefinitions = styleDefinitions;
        }

        StyleDefinitions getStyleDefinitions() {
            return styleDefinitions;
        }

        static class StyledTextElementBuilder extends StoryChildElementBuilder {

            protected StyleDefinitions styleDefinitions;

            StyledTextElementBuilder setStyleDefinitions(StyleDefinitions styleDefinitions) {
                this.styleDefinitions = styleDefinitions;
                return this;
            }

            @Override
            public StoryChildElement build() {
                return new StyledTextElement(startElement, innerEvents, endElement, styleDefinitions);
            }
        }

        static class Content extends StyledTextElement {
            Content(StartElement startElement, List<XMLEvent> innerEvents, EndElement endElement, StyleDefinitions styleDefinitions) {
                super(startElement, innerEvents, endElement, styleDefinitions);
            }

            static class ContentBuilder extends StyledTextElementBuilder {
                @Override
                public StoryChildElement build() {
                    return new Content(startElement, innerEvents, endElement, styleDefinitions);
                }
            }
        }

        static class Break extends StyledTextElement {
            Break(StartElement startElement, List<XMLEvent> innerEvents, EndElement endElement, StyleDefinitions styleDefinitions) {
                super(startElement, innerEvents, endElement, styleDefinitions);
            }

            static class BreakBuilder extends StyledTextElementBuilder {
                @Override
                public StoryChildElement build() {
                    return new Break(startElement, innerEvents, endElement, styleDefinitions);
                }
            }
        }
    }

    /**
     * Represents a styled text reference - Table, Footnote and Note so far.
     */
    static class StyledTextReferenceElement extends StyledTextElement {

        protected final Properties properties;
        protected final List<StoryChildElement> storyChildElements;
        protected final StyleRangeEventsGenerator styleRangeEventsGenerator;
        protected final StoryChildElementsWriter storyChildElementsWriter;

        StyledTextReferenceElement(StartElement startElement, Properties properties, List<StoryChildElement> storyChildElements,
                                   EndElement endElement, StyleDefinitions styleDefinitions,
                                   StyleRangeEventsGenerator styleRangeEventsGenerator, StoryChildElementsWriter storyChildElementsWriter) {
            super(startElement, Collections.<XMLEvent>emptyList(), endElement, styleDefinitions);
            this.properties = properties;
            this.storyChildElements = storyChildElements;
            this.styleRangeEventsGenerator = styleRangeEventsGenerator;
            this.storyChildElementsWriter = storyChildElementsWriter;
        }

        Properties getProperties() {
            return properties;
        }

        List<StoryChildElement> getStoryChildElements() {
            return storyChildElements;
        }

        @Override
        public List<XMLEvent> getEvents() {
            List<XMLEvent> events = new ArrayList<>();

            events.add(getStartElement());
            events.addAll(properties.getEvents());

            events.addAll(storyChildElementsWriter.write(storyChildElements));

            if (!storyChildElements.isEmpty() && storyChildElements.get(storyChildElements.size() - 1) instanceof StyledTextElement) {
                // if story element list is not empty
                // and the last element is styled text element
                events.addAll(styleRangeEventsGenerator.generateCharacterStyleRangeEnd());
                events.addAll(styleRangeEventsGenerator.generateParagraphStyleRangeEnd());
            }

            events.add(getEndElement());

            return events;
        }

        static class StyledTextReferenceElementBuilder extends StyledTextElementBuilder {

            protected Properties properties = getEmptyProperties();
            protected List<StoryChildElement> storyChildElements = new ArrayList<>();
            protected StyleRangeEventsGenerator styleRangeEventsGenerator;
            protected StoryChildElementsWriter storyChildElementsWriter;

            StyledTextReferenceElementBuilder setProperties(Properties properties) {
                this.properties = properties;
                return this;
            }

            StyledTextReferenceElementBuilder addStoryChildElements(List<StoryChildElement> storyChildElements) {
                this.storyChildElements.addAll(storyChildElements);
                return this;
            }

            StyledTextReferenceElementBuilder setStyleRangeEventsGenerator(StyleRangeEventsGenerator styleRangeEventsGenerator) {
                this.styleRangeEventsGenerator = styleRangeEventsGenerator;
                return this;
            }

            StyledTextReferenceElementBuilder setStoryChildElementsWriter(StoryChildElementsWriter storyChildElementsWriter) {
                this.storyChildElementsWriter = storyChildElementsWriter;
                return this;
            }

            @Override
            public StyledTextReferenceElement build() {
                return new StyledTextReferenceElement(startElement, properties, storyChildElements, endElement, styleDefinitions, styleRangeEventsGenerator, storyChildElementsWriter);
            }
        }

        static class HyperlinkTextSource extends StyledTextReferenceElement {
            HyperlinkTextSource(StartElement startElement, Properties properties, List<StoryChildElement> storyChildElements, EndElement endElement, StyleDefinitions styleDefinitions,
                                StyleRangeEventsGenerator styleRangeEventsGenerator, StoryChildElementsWriter storyChildElementsWriter) {
                super(startElement, properties, storyChildElements, endElement, styleDefinitions, styleRangeEventsGenerator, storyChildElementsWriter);
            }

            @Override
            public List<XMLEvent> getEvents() {
                List<XMLEvent> events = new ArrayList<>();

                // TODO: possibly check if a story child element has default styles
                if (1 == storyChildElements.size()) {
                    events.add(getStartElement());
                    events.addAll(storyChildElements.get(0).getEvents());
                    events.add(getEndElement());

                    return events;
                }


                return super.getEvents();
            }

            static class HyperlinkTextSourceBuilder extends StyledTextReferenceElementBuilder {
                @Override
                public HyperlinkTextSource build() {
                    return new HyperlinkTextSource(startElement, properties, storyChildElements, endElement, styleDefinitions, styleRangeEventsGenerator, storyChildElementsWriter);
                }
            }
        }

        static class Note extends StyledTextReferenceElement {
            Note(StartElement startElement, Properties properties, List<StoryChildElement> storyChildElements, EndElement endElement, StyleDefinitions styleDefinitions,
                 StyleRangeEventsGenerator styleRangeEventsGenerator, StoryChildElementsWriter storyChildElementsWriter) {
                super(startElement, properties, storyChildElements, endElement, styleDefinitions, styleRangeEventsGenerator, storyChildElementsWriter);
            }

            static class NoteBuilder extends StyledTextReferenceElementBuilder {
                @Override
                public StyledTextReferenceElement build() {
                    return new Note(startElement, properties, storyChildElements, endElement, styleDefinitions, styleRangeEventsGenerator, storyChildElementsWriter);
                }
            }
        }

        static class Footnote extends StyledTextReferenceElement {
            Footnote(StartElement startElement, Properties properties, List<StoryChildElement> storyChildElements, EndElement endElement, StyleDefinitions styleDefinitions,
                     StyleRangeEventsGenerator styleRangeEventsGenerator, StoryChildElementsWriter storyChildElementsWriter) {
                super(startElement, properties, storyChildElements, endElement, styleDefinitions, styleRangeEventsGenerator, storyChildElementsWriter);
            }

            static class FootnoteBuilder extends StyledTextReferenceElementBuilder {
                @Override
                public StyledTextReferenceElement build() {
                    return new Footnote(startElement, properties, storyChildElements, endElement, styleDefinitions, styleRangeEventsGenerator, storyChildElementsWriter);
                }
            }
        }

        static class Table extends StyledTextReferenceElement {

            private final List<MarkupRangeElement> markupRangeElements;
            private final List<Cell> cells;

            Table(StartElement startElement, Properties properties, List<MarkupRangeElement> markupRangeElements, List<Cell> cells,
                  EndElement endElement, StyleDefinitions styleDefinitions) {
                super(startElement, properties, Collections.<StoryChildElement>emptyList(), endElement, styleDefinitions, null, null);
                this.markupRangeElements = markupRangeElements;
                this.cells = cells;
            }

            List<Cell> getCells() {
                return cells;
            }

            @Override
            public List<XMLEvent> getEvents() {
                List<XMLEvent> events = new ArrayList<>();

                events.add(getStartElement());
                events.addAll(properties.getEvents());

                for (MarkupRangeElement markupRangeElement : markupRangeElements) {
                    events.addAll(markupRangeElement.getEvents());
                }

                for (Cell cell : cells) {
                    events.addAll(cell.getEvents());
                }

                events.add(getEndElement());

                return events;
            }

            static class TableBuilder extends StyledTextReferenceElementBuilder {

                private List<MarkupRangeElement> markupRangeElements = new ArrayList<>();
                private List<Cell> cells = new ArrayList<>();

                TableBuilder addMarkupRangeElement(MarkupRangeElement markupRangeElement) {
                    markupRangeElements.add(markupRangeElement);
                    return this;
                }

                TableBuilder addCell(Cell cell) {
                    cells.add(cell);
                    return this;
                }

                @Override
                public Table build() {
                    return new Table(startElement, properties, markupRangeElements, cells, endElement, styleDefinitions);
                }
            }

            static class Cell extends StyledTextReferenceElement {
                Cell(StartElement startElement, List<StoryChildElement> storyChildElements, EndElement endElement,
                     StyleRangeEventsGenerator styleRangeEventsGenerator, StoryChildElementsWriter storyChildElementsWriter) {
                    super(startElement, Properties.getEmptyProperties(), storyChildElements, endElement, null, styleRangeEventsGenerator, storyChildElementsWriter);
                }

                static class CellBuilder extends StyledTextReferenceElementBuilder {
                    @Override
                    public Cell build() {
                        return new Cell(startElement, storyChildElements, endElement, styleRangeEventsGenerator, storyChildElementsWriter);
                    }
                }
            }
        }
    }
}
