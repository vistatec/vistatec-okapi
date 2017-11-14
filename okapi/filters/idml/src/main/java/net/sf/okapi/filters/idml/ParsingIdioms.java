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
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ParsingIdioms {

    static final String UNEXPECTED_STRUCTURE = "Unexpected structure";

    static final QName SELF = Namespaces.getDefaultNamespace().getQName("Self");
    static final QName VISIBLE = Namespaces.getDefaultNamespace().getQName("Visible");

    static final QName MASTER_SPREAD = Namespaces.getDefaultNamespace().getQName("MasterSpread");
    static final QName SPREAD = Namespaces.getDefaultNamespace().getQName("Spread");

    static final QName ITEM_TRANSFORM = Namespaces.getDefaultNamespace().getQName("ItemTransform");

    static final QName PROPERTIES = Namespaces.getDefaultNamespace().getQName("Properties");

    private static final QName ITEM_LAYER = Namespaces.getDefaultNamespace().getQName("ItemLayer");

    private static final QName TEXT_FRAME = Namespaces.getDefaultNamespace().getQName("TextFrame");
    private static final QName GRAPHIC_LINE = Namespaces.getDefaultNamespace().getQName("GraphicLine");
    private static final QName RECTANGLE = Namespaces.getDefaultNamespace().getQName("Rectangle");
    private static final QName OVAL = Namespaces.getDefaultNamespace().getQName("Oval");
    private static final QName POLYGON = Namespaces.getDefaultNamespace().getQName("Polygon");
    private static final QName GROUP = Namespaces.getDefaultNamespace().getQName("Group");
    private static final QName MULTI_STATE_OBJECT = Namespaces.getDefaultNamespace().getQName("MultiStateObject");
    private static final QName BUTTON = Namespaces.getDefaultNamespace().getQName("Button");
    private static final QName TEXT_BOX = Namespaces.getDefaultNamespace().getQName("TextBox");

    static boolean getBooleanAttributeValue(StartElement element, QName name, boolean defaultValue) {
        Attribute attribute = element.getAttributeByName(name);

        if (null == attribute) {
            return defaultValue;
        }

        return Boolean.parseBoolean(attribute.getValue());
    }

    static String getItemLayerAttributeValue(StartElement startElement, String activeLayerId) {
        Attribute attribute = startElement.getAttributeByName(ITEM_LAYER);

        if (null == attribute) {
            return activeLayerId;
        }

        return attribute.getValue();
    }

    static List<Attribute> getStartElementAttributes(StartElement startElement) {
        List<Attribute> attributes = new ArrayList<>();

        Iterator iterator = startElement.getAttributes();

        while (iterator.hasNext()) {
            attributes.add((Attribute) iterator.next());
        }

        return attributes;
    }

    static StartElement peekNextStartElement(XMLEventReader eventReader) throws XMLStreamException {

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.peek();

            if (event.isStartElement()) {
                return event.asStartElement();
            }

            eventReader.nextEvent();
        }

        throw new IllegalStateException(UNEXPECTED_STRUCTURE);
    }

    /**
     * Parses spread items and fill in the spread holder with them.
     *
     * A spread can contain:
     * - text frame
     * - graphic line
     * - rectangle
     * - oval
     * - polygon
     * - group
     * - multi-state object
     * - button
     * - text box
     *
     * A group or a state can contain all the above but the following:
     * - multi-state object
     * - button
     * - text box
     *
     * @param startElement     A start element
     * @param eventReader      An event reader
     * @param spreadItemHolder A spread item holder
     *
     * @throws XMLStreamException
     */
    static void parseSpreadItems(StartElement startElement, XMLEventReader eventReader, SpreadItemHolder spreadItemHolder) throws XMLStreamException {

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                return;
            }

            if (!event.isStartElement()) {
                continue;
            }

            StartElement element = event.asStartElement();

            if (TEXT_FRAME.equals(element.getName())) {
                spreadItemHolder.addSpreadItem(parseTextFrame(element, spreadItemHolder.getActiveLayerId(), eventReader));
                continue;
            }

            if (GRAPHIC_LINE.equals(element.getName())) {
                spreadItemHolder.addSpreadItem(parseGraphicLine(element, spreadItemHolder.getActiveLayerId(), eventReader));
                continue;
            }

            if (RECTANGLE.equals(element.getName())) {
                spreadItemHolder.addSpreadItem(parseRectangle(element, spreadItemHolder.getActiveLayerId(), eventReader));
                continue;
            }

            if (OVAL.equals(element.getName())) {
                spreadItemHolder.addSpreadItem(parseOval(element, spreadItemHolder.getActiveLayerId(), eventReader));
                continue;
            }

            if (POLYGON.equals(element.getName())) {
                spreadItemHolder.addSpreadItem(parsePolygon(element, spreadItemHolder.getActiveLayerId(), eventReader));
                continue;
            }

            if (GROUP.equals(element.getName())) {
                spreadItemHolder.addSpreadItem(parseGroup(element, spreadItemHolder.getActiveLayerId(), eventReader));
                continue;
            }

            if (MULTI_STATE_OBJECT.equals(element.getName())) {
                spreadItemHolder.addSpreadItem(parseMultiStateObject(element, spreadItemHolder.getActiveLayerId(),  eventReader));
                continue;
            }

            if (BUTTON.equals(element.getName())) {
                spreadItemHolder.addSpreadItem(parseButton(element, spreadItemHolder.getActiveLayerId(),  eventReader));
                continue;
            }

            if (TEXT_BOX.equals(element.getName())) {
                spreadItemHolder.addSpreadItem(parseTextBox(element, spreadItemHolder.getActiveLayerId(),  eventReader));
            }
        }

        throw new IllegalStateException(UNEXPECTED_STRUCTURE);
    }

    private static SpreadItem parseTextFrame(StartElement startElement, String activeLayerId, XMLEventReader eventReader) throws XMLStreamException {
        SpreadItemParser.TextFrameParser textFrameParser = new SpreadItemParser.TextFrameParser(startElement, activeLayerId, eventReader);
        return textFrameParser.parse(new SpreadItem.TextFrame.TextFrameBuilder());
    }

    private static SpreadItem parseGraphicLine(StartElement startElement, String activeLayerId, XMLEventReader eventReader) throws XMLStreamException {
        SpreadItemParser.GraphicLineParser graphicLineParser = new SpreadItemParser.GraphicLineParser(startElement, activeLayerId, eventReader);
        return graphicLineParser.parse(new SpreadItem.GraphicLine.GraphicLineBuilder());
    }

    private static SpreadItem parseRectangle(StartElement startElement, String activeLayerId, XMLEventReader eventReader) throws XMLStreamException {
        SpreadItemParser.RectangleParser rectangleParser = new SpreadItemParser.RectangleParser(startElement, activeLayerId, eventReader);
        return rectangleParser.parse(new SpreadItem.Rectangle.RectangleBuilder());
    }

    private static SpreadItem parseOval(StartElement startElement, String activeLayerId, XMLEventReader eventReader) throws XMLStreamException {
        SpreadItemParser.OvalParser ovalParser = new SpreadItemParser.OvalParser(startElement, activeLayerId, eventReader);
        return ovalParser.parse(new SpreadItem.Oval.OvalBuilder());
    }

    private static SpreadItem parsePolygon(StartElement startElement, String activeLayerId, XMLEventReader eventReader) throws XMLStreamException {
        SpreadItemParser.PolygonParser polygonParser = new SpreadItemParser.PolygonParser(startElement, activeLayerId, eventReader);
        return polygonParser.parse(new SpreadItem.Polygon.PolygonBuilder());
    }

    private static SpreadItem parseGroup(StartElement startElement, String activeLayerId, XMLEventReader eventReader) throws XMLStreamException {
        SpreadItemParser.GroupParser groupParser = new SpreadItemParser.GroupParser(startElement, activeLayerId, eventReader);
        return groupParser.parse(new SpreadItem.Group.GroupBuilder());
    }

    private static SpreadItem parseMultiStateObject(StartElement startElement, String activeLayerId, XMLEventReader eventReader) throws XMLStreamException {
        SpreadItemParser.MultiStateObjectParser multiStateObjectParser = new SpreadItemParser.MultiStateObjectParser(startElement, activeLayerId, eventReader);
        return multiStateObjectParser.parse(new SpreadItem.MultiStateObject.MultiStateObjectBuilder());
    }

    private static SpreadItem parseButton(StartElement startElement, String activeLayerId, XMLEventReader eventReader) throws XMLStreamException {
        SpreadItemParser.ButtonParser buttonParser = new SpreadItemParser.ButtonParser(startElement, activeLayerId, eventReader);
        return buttonParser.parse(new SpreadItem.Button.ButtonBuilder());
    }

    private static SpreadItem parseTextBox(StartElement startElement, String activeLayerId, XMLEventReader eventReader) throws XMLStreamException {
        SpreadItemParser.TextBoxParser textBoxParser = new SpreadItemParser.TextBoxParser(startElement, activeLayerId, eventReader);
        return textBoxParser.parse(new SpreadItem.TextBox.TextBoxBuilder());
    }

    enum StyledStoryChildElement {

        PARAGRAPH_STYLE_RANGE("ParagraphStyleRange"),
        CHARACTER_STYLE_RANGE("CharacterStyleRange"),

        FOOTNOTE("Footnote"),
        GAIJI_OWNED_ITEM_OBJECT("GaijiOwnedItemObject"),
        NOTE("Note"),
        TABLE("Table"),
        TEXT_VARIABLE_INSTANCE("TextVariableInstance"),
        HYPERLINK_TEXT_DESTINATION("HyperlinkTextDestination"),
        CHANGE("Change"),
        HIDDEN_TEXT("HiddenText"),
        XML_ELEMENT("XMLElement"),
        XML_ATTRIBUTE("XMLAttribute"),
        XML_COMMENT("XMLComment"),
        XML_INSTRUCTION("XMLInstruction"),
        DTD("DTD"),
        OVAL("Oval"),
        RECTANGLE("Rectangle"),
        GRAPHIC_LINE("GraphicLine"),
        POLYGON("Polygon"),
        GROUP("Group"),
        TEXT_FRAME("TextFrame"),
        BUTTON("Button"),
        FORM_FIELD("FormField"),
        MULTI_STATE_OBJECT("MultiStateObject"),
        EPS_TEXT("EPSText"),
        HYPERLINK_TEXT_SOURCE("HyperlinkTextSource"),
        PAGE_REFERENCE("PageReference"),
        PARAGRAPH_DESTINATION("ParagraphDestination"),
        CROSS_REFERENCE_SOURCE("CrossReferenceSource"),
        CONTENT("Content"),
        BREAK("Br"),

        UNSUPPORTED("");

        QName name;

        StyledStoryChildElement(String name) {
            this.name = Namespaces.getDefaultNamespace().getQName(name);
        }

        QName getName() {
            return name;
        }

        static StyledStoryChildElement fromName(QName name) {
            if (null == name) {
                return UNSUPPORTED;
            }

            for (StyledStoryChildElement styledStoryChildElement : values()) {
                if (styledStoryChildElement.getName().equals(name)) {
                    return styledStoryChildElement;
                }
            }

            return UNSUPPORTED;
        }
    }
}
