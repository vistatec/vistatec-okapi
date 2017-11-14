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
import javax.xml.stream.events.XMLEvent;

import static net.sf.okapi.filters.idml.ParsingIdioms.ITEM_TRANSFORM;
import static net.sf.okapi.filters.idml.ParsingIdioms.SELF;
import static net.sf.okapi.filters.idml.ParsingIdioms.UNEXPECTED_STRUCTURE;
import static net.sf.okapi.filters.idml.ParsingIdioms.VISIBLE;
import static net.sf.okapi.filters.idml.ParsingIdioms.getBooleanAttributeValue;
import static net.sf.okapi.filters.idml.ParsingIdioms.getItemLayerAttributeValue;
import static net.sf.okapi.filters.idml.ParsingIdioms.parseSpreadItems;

class SpreadItemParser {

    private static final QName PROPERTIES = Namespaces.getDefaultNamespace().getQName("Properties");
    private static final QName PATH_GEOMETRY = Namespaces.getDefaultNamespace().getQName("PathGeometry");

    private static final QName PARENT_STORY = Namespaces.getDefaultNamespace().getQName("ParentStory");
    private static final QName PREVIOUS_TEXT_FRAME = Namespaces.getDefaultNamespace().getQName("PreviousTextFrame");
    private static final QName NEXT_TEXT_FRAME = Namespaces.getDefaultNamespace().getQName("NextTextFrame");

    private static final QName TEXT_PATH = Namespaces.getDefaultNamespace().getQName("TextPath");
    private static final QName STATE = Namespaces.getDefaultNamespace().getQName("State");

    protected final StartElement startElement;
    protected final String activeLayerId;
    protected final XMLEventReader eventReader;

    SpreadItemParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
        this.startElement = startElement;
        this.activeLayerId = activeLayerId;
        this.eventReader = eventReader;
    }

    protected SpreadItem.SpreadItemBuilder parseStartElement(SpreadItem.SpreadItemBuilder spreadItemBuilder) {
        spreadItemBuilder.setId(startElement.getAttributeByName(SELF).getValue());
        spreadItemBuilder.setLayerId(getItemLayerAttributeValue(startElement, activeLayerId));
        spreadItemBuilder.setVisible(getBooleanAttributeValue(startElement, VISIBLE, true));
        spreadItemBuilder.setTransformation(startElement.getAttributeByName(ITEM_TRANSFORM).getValue());

        return spreadItemBuilder;
    }

    protected SpreadItem.SpreadItemBuilder parseProperties(SpreadItem.SpreadItemBuilder spreadItemBuilder) throws XMLStreamException {
        XMLEvent event = eventReader.nextTag();

        if (!event.isStartElement() || !PROPERTIES.equals(event.asStartElement().getName())) {
            return spreadItemBuilder;
        }

        while (eventReader.hasNext()) {
            event = eventReader.nextEvent();

            if (event.isEndElement() && PROPERTIES.equals(event.asEndElement().getName())) {
                return spreadItemBuilder;
            }

            if (!event.isStartElement()) {
                continue;
            }

            if (!PATH_GEOMETRY.equals(event.asStartElement().getName())) {
                spreadItemBuilder.addProperty(new PropertyParserRange(event.asStartElement(), eventReader).parse());
                continue;
            }

            spreadItemBuilder.addProperty(parsePathGeometryProperty(event.asStartElement(), eventReader));
        }

        throw new IllegalStateException(UNEXPECTED_STRUCTURE);
    }

    private Property.PathGeometryProperty parsePathGeometryProperty(StartElement startElement, XMLEventReader eventReader) throws XMLStreamException {
        return new PropertyParserRange.PathGeometryPropertyParserRange(startElement, eventReader).parse();
    }

    SpreadItem parse(SpreadItem.SpreadItemBuilder spreadItemBuilder) throws XMLStreamException {
        parseStartElement(spreadItemBuilder);

        return spreadItemBuilder.build();
    }

    static class TextualSpreadItemParser extends SpreadItemParser {

        TextualSpreadItemParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
            super(startElement, activeLayerId, eventReader);
        }

        SpreadItem.TextualSpreadItem parse(SpreadItem.TextualSpreadItem.TextualSpreadItemBuilder textualSpreadItemBuilder) throws XMLStreamException {
            super.parseStartElement(textualSpreadItemBuilder);
            super.parseProperties(textualSpreadItemBuilder);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                    return textualSpreadItemBuilder.build();
                }

                if (!event.isStartElement() || !TEXT_PATH.equals(event.asStartElement().getName())) {
                    continue;
                }

                textualSpreadItemBuilder.addTextPath(parseTextPath(event.asStartElement()));
            }

            throw new IllegalStateException(UNEXPECTED_STRUCTURE);
        }

        private TextPath parseTextPath(StartElement startElement) {
            TextPath.TextPathBuilder textPathBuilder = new TextPath.TextPathBuilder();

            textPathBuilder.setStoryId(startElement.getAttributeByName(PARENT_STORY).getValue());
            textPathBuilder.setPreviousTextFrameId(startElement.getAttributeByName(PREVIOUS_TEXT_FRAME).getValue());
            textPathBuilder.setNextTextFrameId(startElement.getAttributeByName(NEXT_TEXT_FRAME).getValue());

            return textPathBuilder.build();
        }
    }

    static class TextFrameParser extends TextualSpreadItemParser {

        TextFrameParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
            super(startElement, activeLayerId, eventReader);
        }

        SpreadItem.TextFrame parse(SpreadItem.TextFrame.TextFrameBuilder textFrameBuilder) throws XMLStreamException {
            textFrameBuilder.setStoryId(startElement.getAttributeByName(PARENT_STORY).getValue());
            textFrameBuilder.setPreviousTextFrameId(startElement.getAttributeByName(PREVIOUS_TEXT_FRAME).getValue());
            textFrameBuilder.setNextTextFrameId(startElement.getAttributeByName(NEXT_TEXT_FRAME).getValue());

            return (SpreadItem.TextFrame) super.parse(textFrameBuilder);
        }
    }

    static class GraphicLineParser extends TextualSpreadItemParser {

        GraphicLineParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
            super(startElement, activeLayerId, eventReader);
        }

        SpreadItem.GraphicLine parse(SpreadItem.GraphicLine.GraphicLineBuilder graphicLineBuilder) throws XMLStreamException {
            return (SpreadItem.GraphicLine) super.parse(graphicLineBuilder);
        }
    }

    static class RectangleParser extends TextualSpreadItemParser {

        RectangleParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
            super(startElement, activeLayerId, eventReader);
        }

        SpreadItem.Rectangle parse(SpreadItem.Rectangle.RectangleBuilder rectangleBuilder) throws XMLStreamException {
            return (SpreadItem.Rectangle) super.parse(rectangleBuilder);
        }
    }

    static class OvalParser extends TextualSpreadItemParser {

        OvalParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
            super(startElement, activeLayerId, eventReader);
        }

        SpreadItem.Oval parse(SpreadItem.Oval.OvalBuilder ovalBuilder) throws XMLStreamException {
            return (SpreadItem.Oval) super.parse(ovalBuilder);
        }
    }

    static class PolygonParser extends TextualSpreadItemParser {

        PolygonParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
            super(startElement, activeLayerId, eventReader);
        }

        SpreadItem.Polygon parse(SpreadItem.Polygon.PolygonBuilder polygonBuilder) throws XMLStreamException {
            return (SpreadItem.Polygon) super.parse(polygonBuilder);
        }
    }

    static class MultiStateObjectParser extends SpreadItemParser {

        MultiStateObjectParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
            super(startElement, activeLayerId, eventReader);
        }

        SpreadItem.MultiStateObject parse(SpreadItem.MultiStateObject.MultiStateObjectBuilder multiStateObjectBuilder) throws XMLStreamException {
            super.parseStartElement(multiStateObjectBuilder);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                    return multiStateObjectBuilder.build();
                }

                if (!event.isStartElement() || !event.asStartElement().getName().equals(STATE)) {
                    continue;
                }

                multiStateObjectBuilder.addState(parseState(event.asStartElement(), activeLayerId, eventReader));
            }

            throw new IllegalStateException(UNEXPECTED_STRUCTURE);
        }

        private State parseState(StartElement startElement, String activeLayerId, XMLEventReader eventReader) throws XMLStreamException {
            return new StateParser(startElement, activeLayerId, eventReader).parse();
        }
    }

    static class ButtonParser extends MultiStateObjectParser {

        ButtonParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
            super(startElement, activeLayerId, eventReader);
        }

        SpreadItem.Button parse(SpreadItem.Button.ButtonBuilder buttonBuilder) throws XMLStreamException {
            return (SpreadItem.Button) super.parse(buttonBuilder);
        }
    }

    static class GroupParser extends SpreadItemParser {

        GroupParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
            super(startElement, activeLayerId, eventReader);
        }

        SpreadItem.Group parse(SpreadItem.Group.GroupBuilder groupBuilder) throws XMLStreamException {
            super.parseStartElement(groupBuilder);
            parseSpreadItems(startElement, eventReader, groupBuilder);

            return (SpreadItem.Group) super.parse(groupBuilder);
        }
    }

    static class TextBoxParser extends SpreadItemParser {

        TextBoxParser(StartElement startElement, String activeLayerId, XMLEventReader eventReader) {
            super(startElement, activeLayerId, eventReader);
        }

        SpreadItem.TextBox parse(SpreadItem.TextBox.TextBoxBuilder textBoxBuilder) throws XMLStreamException {
            super.parseStartElement(textBoxBuilder);
            parseSpreadItems(startElement, eventReader, textBoxBuilder);

            return (SpreadItem.TextBox) super.parse(textBoxBuilder);
        }
    }
}
