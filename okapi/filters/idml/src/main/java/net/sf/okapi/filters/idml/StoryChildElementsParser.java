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

import net.sf.okapi.filters.idml.ParsingIdioms.StyledStoryChildElement;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.sf.okapi.filters.idml.ParsingIdioms.PROPERTIES;
import static net.sf.okapi.filters.idml.ParsingIdioms.UNEXPECTED_STRUCTURE;
import static net.sf.okapi.filters.idml.StyleRange.getDefaultCharacterStyleRange;
import static net.sf.okapi.filters.idml.StyleRange.getDefaultParagraphStyleRange;

class StoryChildElementsParser {

    private static final QName CHANGE_TYPE = Namespaces.getDefaultNamespace().getQName("ChangeType");
    private static final String DELETED_TEXT = "DeletedText";

    private static final QName ROW = Namespaces.getDefaultNamespace().getQName("Row");
    private static final QName COLUMN = Namespaces.getDefaultNamespace().getQName("Column");
    private static final QName CELL = Namespaces.getDefaultNamespace().getQName("Cell");

    private static final EnumSet<StyledStoryChildElement> PARAGRAPH_STYLE_RANGE_STYLED_ELEMENTS = EnumSet.complementOf(
            EnumSet.of(StyledStoryChildElement.UNSUPPORTED, StyledStoryChildElement.PARAGRAPH_STYLE_RANGE));

    private final StartElement startElement;
    private final Parameters parameters;
    private final XMLEventFactory eventFactory;
    private final XMLEventReader eventReader;

    private StyleRange currentParagraphStyleRange;
    private StyleRange currentCharacterStyleRange;

    StoryChildElementsParser(StartElement startElement, Parameters parameters, XMLEventFactory eventFactory, XMLEventReader eventReader) {
        this.startElement = startElement;
        this.parameters = parameters;
        this.eventFactory = eventFactory;
        this.eventReader = eventReader;
    }

    List<StoryChildElement> parse() throws XMLStreamException {

        if (!StyledStoryChildElement.PARAGRAPH_STYLE_RANGE.getName().equals(startElement.getName())) {

            if (PARAGRAPH_STYLE_RANGE_STYLED_ELEMENTS.contains(StyledStoryChildElement.fromName(startElement.getName()))) {
                return parseAsFromParagraphStyleRange(startElement);
            }

            return parseFromUnstyledRange(startElement);
        }

        return parseFromParagraphStyleRange();
    }

    private List<StoryChildElement> parseWithStyleDefinitions(StyleDefinitions styleDefinitions) throws XMLStreamException {
        currentParagraphStyleRange = styleDefinitions.getParagraphStyleRange();
        currentCharacterStyleRange = styleDefinitions.getCharacterStyleRange();

        return parse();
    }

    private List<StoryChildElement> parseFromUnstyledRange(StartElement startElement) throws XMLStreamException {
        StoryChildElement.StoryChildElementBuilder storyChildElementBuilder = new StoryChildElement.StoryChildElementBuilder();

        return singletonList(new StoryChildElementParser(startElement, eventReader).parse(storyChildElementBuilder));
    }

    private List<StoryChildElement> parseAsFromParagraphStyleRange(StartElement startElement) throws XMLStreamException {

        StyleRange paragraphStyleRange = null == currentParagraphStyleRange
                ? getDefaultParagraphStyleRange(eventFactory)
                : currentParagraphStyleRange;

        if (!StyledStoryChildElement.CHARACTER_STYLE_RANGE.getName().equals(startElement.getName())) {
            return parseAsFromCharacterStyleRange(startElement, paragraphStyleRange);
        }

        return parseFromCharacterStyleRange(startElement, paragraphStyleRange, null);
    }

    private List<StoryChildElement> parseFromParagraphStyleRange() throws XMLStreamException {
        List<StoryChildElement> storyChildElements = new ArrayList<>();

        StyleRange paragraphStyleRange = new StyleRangeParser(startElement, eventReader).parse();
        currentParagraphStyleRange = paragraphStyleRange;

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextTag();

            if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                return storyChildElements;
            }

            if (!event.isStartElement()) {
                throw new IllegalStateException(UNEXPECTED_STRUCTURE);
            }

            if (!StyledStoryChildElement.CHARACTER_STYLE_RANGE.getName().equals(event.asStartElement().getName())) {
                storyChildElements.addAll(parseAsFromCharacterStyleRange(event.asStartElement(), paragraphStyleRange));
                continue;
            }

            storyChildElements.addAll(parseFromCharacterStyleRange(event.asStartElement(), paragraphStyleRange, null));
        }

        throw new IllegalStateException(UNEXPECTED_STRUCTURE);
    }

    private List<StoryChildElement> parseAsFromCharacterStyleRange(StartElement startElement, StyleRange paragraphStyleRange) throws XMLStreamException {

        StyleRange characterStyleRange = null == currentCharacterStyleRange
                ? getDefaultCharacterStyleRange(eventFactory)
                : currentCharacterStyleRange;

        StyleDefinitions styleDefinitions = new StyleDefinitions(paragraphStyleRange, characterStyleRange);

        if (parameters.getUntagXmlStructures()) {
            if (StyledStoryChildElement.XML_ATTRIBUTE.getName().equals(startElement.getName())
                    || StyledStoryChildElement.XML_COMMENT.getName().equals(startElement.getName())
                    || StyledStoryChildElement.XML_INSTRUCTION.getName().equals(startElement.getName())) {

                skipRange(startElement);

                return emptyList();
            }

            if (StyledStoryChildElement.XML_ELEMENT.getName().equals(startElement.getName())) {
                return parseFromXmlElementRange(startElement, styleDefinitions);
            }
        }

        if (StyledStoryChildElement.HYPERLINK_TEXT_SOURCE.getName().equals(startElement.getName())) {
            return singletonList(parseHyperlinkTextSource(startElement, styleDefinitions));
        }

        if (StyledStoryChildElement.FOOTNOTE.getName().equals(startElement.getName())) {
            return singletonList(parseFootnote(startElement, styleDefinitions));
        }

        if (StyledStoryChildElement.NOTE.getName().equals(startElement.getName())) {
            return singletonList(parseNote(startElement, styleDefinitions));
        }

        if (StyledStoryChildElement.TABLE.getName().equals(startElement.getName())) {
            return singletonList(parseTable(startElement, styleDefinitions));
        }

        if (StyledStoryChildElement.CHANGE.getName().equals(startElement.getName())) {
            return parseFromChangedRange(startElement, styleDefinitions);
        }

        if (StyledStoryChildElement.CONTENT.getName().equals(startElement.getName())) {
            return singletonList(parseContent(startElement, styleDefinitions));
        }

        if (StyledStoryChildElement.BREAK.getName().equals(startElement.getName())) {
            return singletonList(parseBreak(startElement, styleDefinitions));
        }

        return singletonList(parseFromStyledRange(startElement, styleDefinitions));
    }

    private List<StoryChildElement> parseFromCharacterStyleRange(StartElement startElement,
                                                                 StyleRange paragraphStyleRange, StyleRange defaultCharacterStyleRange) throws XMLStreamException {
        StyleRange characterStyleRange = null == defaultCharacterStyleRange
                ? new StyleRangeParser(startElement, eventReader).parse()
                : defaultCharacterStyleRange;

        currentCharacterStyleRange = characterStyleRange;

        StyleDefinitions styleDefinitions = new StyleDefinitions(paragraphStyleRange, characterStyleRange);

        List<StoryChildElement> storyChildElements = new ArrayList<>();

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextTag();

            if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                return storyChildElements;
            }

            if (!event.isStartElement()) {
                throw new IllegalStateException(UNEXPECTED_STRUCTURE);
            }

            if (parameters.getUntagXmlStructures()) {
                if (StyledStoryChildElement.XML_ATTRIBUTE.getName().equals(event.asStartElement().getName())
                        || StyledStoryChildElement.XML_COMMENT.getName().equals(event.asStartElement().getName())
                        || StyledStoryChildElement.XML_INSTRUCTION.getName().equals(event.asStartElement().getName())) {

                    skipRange(event.asStartElement());
                    continue;
                }

                if (StyledStoryChildElement.XML_ELEMENT.getName().equals(event.asStartElement().getName())) {
                    storyChildElements.addAll(parseFromXmlElementRange(event.asStartElement(), styleDefinitions));
                    continue;
                }
            }

            if (StyledStoryChildElement.HYPERLINK_TEXT_SOURCE.getName().equals(event.asStartElement().getName())) {
                storyChildElements.add(parseHyperlinkTextSource(event.asStartElement(), styleDefinitions));
                continue;
            }

            if (StyledStoryChildElement.FOOTNOTE.getName().equals(event.asStartElement().getName())) {
                storyChildElements.add(parseFootnote(event.asStartElement(), styleDefinitions));
                continue;
            }

            if (StyledStoryChildElement.NOTE.getName().equals(event.asStartElement().getName())) {
                storyChildElements.add(parseNote(event.asStartElement(), styleDefinitions));
                continue;
            }

            if (StyledStoryChildElement.TABLE.getName().equals(event.asStartElement().getName())) {
                storyChildElements.add(parseTable(event.asStartElement(), styleDefinitions));
                continue;
            }

            if (StyledStoryChildElement.CHANGE.getName().equals(event.asStartElement().getName())) {
                storyChildElements.addAll(parseFromChangedRange(event.asStartElement(), styleDefinitions));
                continue;
            }

            if (StyledStoryChildElement.CONTENT.getName().equals(event.asStartElement().getName())) {
                storyChildElements.add(parseContent(event.asStartElement(), styleDefinitions));
                continue;
            }

            if (StyledStoryChildElement.BREAK.getName().equals(event.asStartElement().getName())) {
                storyChildElements.add(parseBreak(event.asStartElement(), styleDefinitions));
                continue;
            }

            storyChildElements.add(parseFromStyledRange(event.asStartElement(), styleDefinitions));
        }

        return storyChildElements;
    }

    private List<StoryChildElement> parseFromXmlElementRange(StartElement startElement, StyleDefinitions styleDefinitions) throws XMLStreamException {
        List<StoryChildElement> storyChildElements = new ArrayList<>();

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextTag();

            if (event.isEndElement() && startElement.getName().equals(event.asEndElement().getName())) {
                break;
            }

            if (!event.isStartElement()) {
                throw new IllegalStateException(UNEXPECTED_STRUCTURE);
            }

            storyChildElements.addAll(new StoryChildElementsParser(event.asStartElement(), parameters, eventFactory, eventReader).parseWithStyleDefinitions(styleDefinitions));
        }

        return storyChildElements;
    }

    private StoryChildElement parseHyperlinkTextSource(StartElement startElement, StyleDefinitions styleDefinitions) throws XMLStreamException {
        StoryChildElement.StyledTextReferenceElement.HyperlinkTextSource.HyperlinkTextSourceBuilder hyperlinkTextSourceBuilder = new StoryChildElement.StyledTextReferenceElement.HyperlinkTextSource.HyperlinkTextSourceBuilder();

        return new StyledTextReferenceElementParser(startElement, styleDefinitions, parameters, eventFactory, eventReader).parse(hyperlinkTextSourceBuilder);
    }

    private StoryChildElement parseFootnote(StartElement startElement, StyleDefinitions styleDefinitions) throws XMLStreamException {
        StoryChildElement.StyledTextReferenceElement.Footnote.FootnoteBuilder footnoteBuilder = new StoryChildElement.StyledTextReferenceElement.Footnote.FootnoteBuilder();

        return new StyledTextReferenceElementParser(startElement, styleDefinitions, parameters, eventFactory, eventReader).parse(footnoteBuilder);
    }

    private StoryChildElement parseNote(StartElement startElement, StyleDefinitions styleDefinitions) throws XMLStreamException {
        StoryChildElement.StyledTextReferenceElement.Note.NoteBuilder noteBuilder = new StoryChildElement.StyledTextReferenceElement.Note.NoteBuilder();

        return new StyledTextReferenceElementParser(startElement, styleDefinitions, parameters, eventFactory, eventReader).parse(noteBuilder);
    }

    private StoryChildElement parseTable(StartElement startElement, StyleDefinitions styleDefinitions) throws XMLStreamException {
        return new TableParser(startElement, styleDefinitions, parameters, eventFactory, eventReader).parse();
    }

    private List<StoryChildElement> parseFromChangedRange(StartElement startElement, StyleDefinitions styleDefinitions) throws XMLStreamException {
        String changeTypeValue = startElement.getAttributeByName(CHANGE_TYPE).getValue();

        if (DELETED_TEXT.equals(changeTypeValue)) {
            skipRange(startElement);
            return emptyList();
        }

        return acceptChanges(startElement, styleDefinitions);
    }

    private void skipRange(StartElement startElement) throws XMLStreamException {
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                return;
            }
        }

        throw new IllegalStateException(UNEXPECTED_STRUCTURE);
    }

    private List<StoryChildElement> acceptChanges(StartElement startElement, StyleDefinitions styleDefinitions) throws XMLStreamException {
        return parseFromCharacterStyleRange(startElement, styleDefinitions.getParagraphStyleRange(), styleDefinitions.getCharacterStyleRange());
    }

    private StoryChildElement parseContent(StartElement startElement, StyleDefinitions styleDefinitions) throws XMLStreamException {
        StoryChildElement.StyledTextElement.Content.ContentBuilder contentBuilder = new StoryChildElement.StyledTextElement.Content.ContentBuilder();
        contentBuilder.setStyleDefinitions(styleDefinitions);

        return new StoryChildElementParser(startElement, eventReader).parse(contentBuilder);
    }

    private StoryChildElement parseBreak(StartElement startElement, StyleDefinitions styleDefinitions) throws XMLStreamException {
        StoryChildElement.StyledTextElement.Break.BreakBuilder breakBuilder = new StoryChildElement.StyledTextElement.Break.BreakBuilder();
        breakBuilder.setStyleDefinitions(styleDefinitions);

        return new StoryChildElementParser(startElement, eventReader).parse(breakBuilder);
    }

    private StoryChildElement parseFromStyledRange(StartElement startElement, StyleDefinitions styleDefinitions) throws XMLStreamException {
        StoryChildElement.StyledTextElement.StyledTextElementBuilder styledTextElementBuilder = new StoryChildElement.StyledTextElement.StyledTextElementBuilder();
        styledTextElementBuilder.setStyleDefinitions(styleDefinitions);

        return new StoryChildElementParser(startElement, eventReader).parse(styledTextElementBuilder);
    }

    private static class StoryChildElementParser extends MarkupRangeElementParser {

        StoryChildElementParser(StartElement startElement, XMLEventReader eventReader) {
            super(startElement, eventReader);
        }

        StoryChildElement parse(StoryChildElement.StoryChildElementBuilder storyChildElementBuilder) throws XMLStreamException {
            return (StoryChildElement) super.parse(storyChildElementBuilder);
        }
    }

    private static class StyledTextReferenceElementParser {

        private final StartElement startElement;
        private final StyleDefinitions styleDefinitions;
        private final Parameters parameters;
        private final XMLEventFactory eventFactory;
        private final XMLEventReader eventReader;

        private StyledTextReferenceElementParser(StartElement startElement, StyleDefinitions styleDefinitions, Parameters parameters, XMLEventFactory eventFactory, XMLEventReader eventReader) {
            this.startElement = startElement;
            this.styleDefinitions = styleDefinitions;
            this.parameters = parameters;
            this.eventFactory = eventFactory;
            this.eventReader = eventReader;
        }

        StoryChildElement.StyledTextReferenceElement parse(StoryChildElement.StyledTextReferenceElement.StyledTextReferenceElementBuilder styledTextReferenceElementBuilder) throws XMLStreamException {

            styledTextReferenceElementBuilder.setStyleDefinitions(styleDefinitions);
            styledTextReferenceElementBuilder.setStartElement(startElement);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextTag();

                if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                    StyleRangeEventsGenerator styleRangeEventsGenerator = new StyleRangeEventsGenerator(eventFactory);

                    styledTextReferenceElementBuilder.setStyleRangeEventsGenerator(styleRangeEventsGenerator)
                            .setStoryChildElementsWriter(new StoryChildElementsWriter(styleRangeEventsGenerator))
                            .setEndElement(event.asEndElement());

                    return styledTextReferenceElementBuilder.build();
                }

                if (!event.isStartElement()) {
                    throw new IllegalStateException(UNEXPECTED_STRUCTURE);
                }

                if (PROPERTIES.equals(event.asStartElement().getName())) {
                    styledTextReferenceElementBuilder.setProperties(new PropertiesParser(event.asStartElement(), eventReader).parse());
                    continue;
                }

                styledTextReferenceElementBuilder.addStoryChildElements(new StoryChildElementsParser(event.asStartElement(), parameters, eventFactory, eventReader).parse());
            }

            throw new IllegalStateException(UNEXPECTED_STRUCTURE);
        }
    }

    private static class TableParser {

        private final StartElement startElement;
        private final StyleDefinitions styleDefinitions;
        private final Parameters parameters;
        private final XMLEventFactory eventFactory;
        private final XMLEventReader eventReader;

        private TableParser(StartElement startElement, StyleDefinitions styleDefinitions, Parameters parameters, XMLEventFactory eventFactory, XMLEventReader eventReader) {
            this.startElement = startElement;
            this.styleDefinitions = styleDefinitions;
            this.parameters = parameters;
            this.eventFactory = eventFactory;
            this.eventReader = eventReader;
        }

        StoryChildElement.StyledTextReferenceElement.Table parse() throws XMLStreamException {
            StoryChildElement.StyledTextReferenceElement.Table.TableBuilder tableBuilder = new StoryChildElement.StyledTextReferenceElement.Table.TableBuilder();

            tableBuilder.setStyleDefinitions(styleDefinitions);
            tableBuilder.setStartElement(startElement);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextTag();

                if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                    tableBuilder.setEndElement(event.asEndElement());

                    return tableBuilder.build();
                }

                if (!event.isStartElement()) {
                    throw new IllegalStateException(UNEXPECTED_STRUCTURE);
                }

                if (PROPERTIES.equals(event.asStartElement().getName())) {
                    tableBuilder.setProperties(new PropertiesParser(event.asStartElement(), eventReader).parse());
                    continue;
                }

                if (ROW.equals(event.asStartElement().getName()) || COLUMN.equals(event.asStartElement().getName())) {
                    tableBuilder.addMarkupRangeElement(new MarkupRangeElementParser(event.asStartElement(), eventReader).parse(new MarkupRange.MarkupRangeElement.MarkupRangeElementBuilder()));
                    continue;
                }

                if (!CELL.equals(event.asStartElement().getName())) {
                    throw new IllegalStateException(UNEXPECTED_STRUCTURE);
                }

                StoryChildElement.StyledTextReferenceElement.Table.Cell.CellBuilder cellBuilder = new StoryChildElement.StyledTextReferenceElement.Table.Cell.CellBuilder();
                tableBuilder.addCell((StoryChildElement.StyledTextReferenceElement.Table.Cell) new StyledTextReferenceElementParser(event.asStartElement(), null, parameters, eventFactory, eventReader).parse(cellBuilder));
            }

            throw new IllegalStateException(UNEXPECTED_STRUCTURE);
        }
    }
}
