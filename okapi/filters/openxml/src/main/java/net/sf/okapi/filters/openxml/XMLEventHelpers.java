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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

class XMLEventHelpers {
	static final QName ATTR_XML_SPACE = Namespaces.XML.getQName("space");
	static final QName CACHED_PAGE_BREAK = Namespaces.WordProcessingML.getQName("lastRenderedPageBreak");
	static final QName WPML_ID = Namespaces.WordProcessingML.getQName("id");
	static final QName WPML_NAME = Namespaces.WordProcessingML.getQName("name");
	static final QName WPML_VAL = Namespaces.WordProcessingML.getQName("val");
	static final QName WPML_PROPERTY_VANISH = Namespaces.WordProcessingML.getQName("vanish");
	static final QName WPML_RUN_STYLE = Namespaces.WordProcessingML.getQName("rStyle");
	static final QName WPML_PARAGRAPH_STYLE = Namespaces.WordProcessingML.getQName("pStyle");
	static final QName CHART_STRCACHE = Namespaces.Chart.getQName("strCache");
	static final QName DML_HYPERLINK_ACTION = Namespaces.DrawingML.getQName("action");
	static final String LOCAL_LEVEL = "lvl";
	static final String LOCAL_SHEET_VIEW = "sheetView";
	static final String LOCAL_ALIGNMENT = "alignment";
	static final String LOCAL_PRESENTATION = "presentation";
	static final String LOCAL_TABLE = "tbl";
	static final String LOCAL_TABLE_PROPERTIES = "tblPr";
	static final String LOCAL_TABLE_GRID = "tblGrid";
	static final String LOCAL_TABLE_ROW = "tr";
	static final String LOCAL_TEXT_BODY = "txBody";
	static final String LOCAL_TEXT_BODY_PROPERTIES = "bodyPr";
	static final String LOCAL_PARA = "p";
	static final String LOCAL_STRING_ITEM = "si";
	static final String LOCAL_PARAGRAPH_PROPERTIES = "pPr";
	static final String LOCAL_BIDIRECTIONAL = "bidi";
	static final String LOCAL_BIDI_VISUAL = "bidiVisual";
	static final String LOCAL_RTL = "rtl";
	static final String LOCAL_RTL_COL = "rtlCol";
	static final String LOCAL_READING_ORDER = "readingOrder";
	static final String LOCAL_RIGHT_TO_LEFT = "rightToLeft";
	static final String LOCAL_RUN = "r";
	static final String LOCAL_RUN_PROPERTIES = "rPr";
	static final String LOCAL_PROPERTY_LANGUAGE = "lang";
	static final String LOCAL_TEXT = "t";
	static final String LOCAL_VALUE = "v";
	static final String LOCAL_NO_BREAK_HYPHEN = "noBreakHyphen";
	static final String LOCAL_REGULAR_HYPHEN_VALUE = "\u002D";
	static final String LOCAL_SOFT_HYPHEN = "softHyphen";
	static final String LOCAL_TAB = "tab";
	static final String LOCAL_BREAK = "br";
	static final String LOCAL_CARRIAGE_RETURN = "cr";
	static final String LOCAL_SECTION_PROPERTIES = "sectPr";
	static final String LOCAL_TEXTPATH = "textpath";
	static final String LOCAL_DRAWING_PROPERTY = "docPr";
	static final String LOCAL_GRAPHICS_PROPERTY = "cNvPr";
	static final String LOCAL_SIMPLE_FIELD = "fldSimple";
	static final String LOCAL_COMPLEX_FIELD = "fldChar";
	static final String LOCAL_SMART_TAG_PROPERTIES = "smartTagPr";
	static final String LOCAL_FIELD_CODE = "instrText";
	static final QName COMPLEX_FIELD_TYPE =  Namespaces.WordProcessingML.getQName("fldCharType");

	static final String LOCAL_TYPE = "type";
	static final String PREFIX_A = "a";

	private static final String LOCAL_TABLE_COLUMN = "tableColumn";
	private static final String LOCAL_SHEET_FORMULA = "f";
	private static final String LOCAL_TABLE_FORMULA = "formula";
	private static final String LOCAL_CALCULATED_FORMULA = "calculatedColumnFormula";

	enum BooleanAttributeValue {
		INVALID_VALUE(""),

		FALSE_INTEGER("0"),
		TRUE_INTEGER("1"),

		FALSE_STRING("false"),
		TRUE_STRING("true");

		String value;

		BooleanAttributeValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static BooleanAttributeValue fromValue(String value) {
			if (null == value) {
				return INVALID_VALUE;
			}

			for (BooleanAttributeValue attributeValue : values()) {
				if (value.equals(attributeValue.getValue())) {
					return attributeValue;
				}
			}

			return INVALID_VALUE;
		}
	}

	/**
	 * Boolean attribute true values.
	 */
	static final EnumSet<BooleanAttributeValue> BOOLEAN_ATTRIBUTE_TRUE_VALUES = EnumSet.of(
			BooleanAttributeValue.TRUE_INTEGER,
			BooleanAttributeValue.TRUE_STRING
	);

	static final boolean DEFAULT_BOOLEAN_ATTRIBUTE_TRUE_VALUE = Boolean.valueOf(BooleanAttributeValue.TRUE_STRING.getValue());
	static final boolean DEFAULT_BOOLEAN_ATTRIBUTE_FALSE_VALUE = Boolean.valueOf(BooleanAttributeValue.FALSE_STRING.getValue());

	static boolean isSectionPropertiesStartEvent(XMLEvent event) {
		return isStartElement(event, LOCAL_SECTION_PROPERTIES);
	}

	static boolean isSheetViewStartEvent(XMLEvent event) {
		return isStartElement(event, LOCAL_SHEET_VIEW);
	}

	static boolean isSheetViewEndEvent(XMLEvent event) {
		return isEndElement(event, LOCAL_SHEET_VIEW);
	}

	static boolean isAlignmentStartEvent(XMLEvent event) {
		return isStartElement(event, LOCAL_ALIGNMENT);
	}

	static boolean isPresentationStartEvent(XMLEvent event) {
		return isStartElement(event, LOCAL_PRESENTATION);
	}

	static boolean isPresentationEndEvent(XMLEvent event) {
		return isEndElement(event, LOCAL_PRESENTATION);
	}

	static boolean isBlockMarkupStartEvent(XMLEvent event) {
		return isTableStartEvent(event)
				|| isTextBodyStartEvent(event);
	}

	static boolean isBlockMarkupEndEvent(XMLEvent event) {
		return isTableEndEvent(event)
				|| isTextBodyEndEvent(event);
	}

	static boolean isTableStartEvent(XMLEvent event) {
		return isStartElement(event, LOCAL_TABLE);
	}

	static boolean isTableEndEvent(XMLEvent event) {
		return isEndElement(event, LOCAL_TABLE);
	}

	static boolean isTablePropertiesStartEvent(XMLEvent event) {
		return isStartElement(event, LOCAL_TABLE_PROPERTIES);
	}

	static boolean isTableGridStartEvent(XMLEvent event) {
		return isStartElement(event, LOCAL_TABLE_GRID);
	}

	static boolean isTableGridEndEvent(XMLEvent event) {
		return isEndElement(event, LOCAL_TABLE_GRID);
	}

	static boolean isTableRowStartEvent(XMLEvent event) {
		return isStartElement(event, LOCAL_TABLE_ROW);
	}

	static boolean isTextBodyStartEvent(XMLEvent event) {
		return isStartElement(event, LOCAL_TEXT_BODY);
	}

	static boolean isTextBodyEndEvent(XMLEvent event) {
		return isEndElement(event, LOCAL_TEXT_BODY);
	}

	static boolean isTextBodyPropertiesStartEvent(XMLEvent event) {
		return isStartElement(event, LOCAL_TEXT_BODY_PROPERTIES);
	}

	static boolean isParagraphStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_PARA);
	}

	static boolean isStringItemStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_STRING_ITEM);
	}

	static boolean isStringItemEndEvent(XMLEvent e) {
		return isEndElement(e, LOCAL_STRING_ITEM);
	}

	static boolean isParagraphPropertiesStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_PARAGRAPH_PROPERTIES);
	}

	static boolean isRunStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_RUN) &&
			   !Namespaces.Math.containsName(e.asStartElement().getName());
	}

	static boolean isRunContainerStartEvent(XMLEvent e) {
		return e.isStartElement() && RunContainer.RUN_CONTAINER_TYPES.contains(RunContainer.Type.fromValue(e.asStartElement().getName().getLocalPart()));
	}

	static boolean isSimpleFieldStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_SIMPLE_FIELD);
	}

	static boolean isRunPropsStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_RUN_PROPERTIES);
	}

	static boolean isRunContainerPropertiesStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_SMART_TAG_PROPERTIES);
	}

	static boolean isTextStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_TEXT);
	}

	static boolean isTableColumnEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_TABLE_COLUMN);
	}

	static boolean isExcelFormula(XMLEvent e) {
		return isStartElement(e, LOCAL_SHEET_FORMULA) || isStartElement(e, LOCAL_TABLE_FORMULA) || isStartElement(e, LOCAL_CALCULATED_FORMULA);
	}

	static boolean isNoBreakHyphenStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_NO_BREAK_HYPHEN);
	}

	static boolean isSoftHyphenStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_SOFT_HYPHEN);
	}

	static boolean isTabStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_TAB);
	}

	/**
	 * This checks that not only is the element a break element, but also
	 * that it's a true "line" break, not a page or column break.
	 */
	static boolean isLineBreakStartEvent(XMLEvent e) {
		if (isStartElement(e, LOCAL_BREAK) || isStartElement(e, LOCAL_CARRIAGE_RETURN)) {
			StartElement start = e.asStartElement();
			QName typeName = createQName(LOCAL_TYPE, start.getName());
			Attribute a = start.getAttributeByName(typeName);
			if (a == null) {
				return true;
			}
			String type = a.getValue();
			if (!type.equals("page") && !type.equals("column")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the given {@code startElement} represents a page break.
	 *
	 * @param startElement the {@link StartElement} to check
	 * @return if the given {@code startElement} is a {@code <br type="page"/>}
	 */
	static boolean isPageBreak(StartElement startElement) {
		QName name = startElement.getName();
		if (!"br".equals(name.getLocalPart())) {
			return false;
		}
		QName typeName = new QName(name.getNamespaceURI(), "type");
		return "page".equals(getAttributeValue(startElement, typeName));
	}

	static boolean isTextPath(XMLEvent e) {
		return isStartElement(e, LOCAL_TEXTPATH);
	}

	static boolean isGraphicsProperty(XMLEvent e) {
		return isStartElement(e, LOCAL_DRAWING_PROPERTY) ||
			   isStartElement(e, LOCAL_GRAPHICS_PROPERTY);
	}

	static boolean isChartValueStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_VALUE);
	}

	static boolean isComplexCodeStart(XMLEvent e) {
		return isStartElement(e, LOCAL_COMPLEX_FIELD) &&
				"begin".equals(getAttributeValue(e.asStartElement(), COMPLEX_FIELD_TYPE));
	}

	static boolean isComplexCodeSeparate(XMLEvent e) {
		return isStartElement(e, LOCAL_COMPLEX_FIELD) &&
				"separate".equals(getAttributeValue(e.asStartElement(), COMPLEX_FIELD_TYPE));
	}

	static boolean isComplexCodeEnd(XMLEvent e) {
		return isStartElement(e, LOCAL_COMPLEX_FIELD) &&
				"end".equals(getAttributeValue(e.asStartElement(), COMPLEX_FIELD_TYPE));
	}

	static boolean isFieldCodeStartEvent(XMLEvent e) {
		return isStartElement(e, LOCAL_FIELD_CODE);
	}

	static boolean isFieldCodeEndEvent(XMLEvent e) {
		return isEndElement(e, LOCAL_FIELD_CODE);
	}

	static boolean isStartElement(XMLEvent e, String expectedLocalPart) {
		return e.isStartElement() && expectedLocalPart.equals(e.asStartElement().getName().getLocalPart());
	}

	static boolean isEndElement(XMLEvent e, StartElement correspondingStartElement) {
		return e.isEndElement() && e.asEndElement().getName().equals(correspondingStartElement.getName());
	}

	static boolean isEndElement(XMLEvent e, String expectedLocalPart) {
		return e.isEndElement() && expectedLocalPart.equals(e.asEndElement().getName().getLocalPart());
	}

	static boolean hasPreserveWhitespace(StartElement e) {
		return "preserve".equals(getAttributeValue(e, ATTR_XML_SPACE));
	}

	static boolean isWhitespace(XMLEvent e) {
		return (e.isCharacters() && e.asCharacters().getData().trim().isEmpty());
	}

	static String getAttributeValue(StartElement el, QName name) {
		Attribute attr = el.getAttributeByName(name);
		return (attr != null) ? attr.getValue() : null;
	}

	/**
	 * Gets a boolean attribute value.
	 *
	 * @param element      An XML element
	 * @param name         An attribute name
	 * @param defaultValue A default value to return if the attribute does not exist
	 *
	 * @return {@code true}  if the attribute value is in the set of boolean attribute "true" values,
	 *         {@code false} if the attribute value is not in the set of boolean attribute "true" values
	 */
	static boolean getBooleanAttributeValue(StartElement element, QName name, boolean defaultValue) {
		Attribute attribute = element.getAttributeByName(name);

		if (null == attribute) {
			return defaultValue;
		}

		return BOOLEAN_ATTRIBUTE_TRUE_VALUES.contains(BooleanAttributeValue.fromValue(attribute.getValue()));
	}

	static QName createQName(String localPart, QName modelName) {
		return new QName(modelName.getNamespaceURI(), localPart, modelName.getPrefix());
	}

	static List<XMLEvent> gatherEvents(StartElementContext startElementContext) throws XMLStreamException {
		// Gather elements up to the end
		List<XMLEvent> p = new ArrayList<>();
		p.add(startElementContext.getStartElement());
		while (startElementContext.getEventReader().hasNext()) {
			XMLEvent e = startElementContext.getEventReader().nextEvent();
			p.add(e);
			if (isEndElement(e, startElementContext.getStartElement())) {
				return p;
			}
		}
		throw new IllegalStateException("Unterminated start element: " + XMLEventSerializer.serialize(startElementContext.getStartElement()));
	}

	static boolean eventEquals(List<XMLEvent> e1, List<XMLEvent> e2) {
		// XMLEvent doesn't implement equals() in the default implementation,
		// so we compare by hand.
		if (e1.size() != e2.size()) {
			return false;
		}
		for (int i = 0; i < e1.size(); i++) {
			if (!eventEquals(e1.get(i), e2.get(i))) {
				return false;
			}
		}
		return true;
	}

	static boolean eventEquals(XMLEvent e1, XMLEvent e2) {
		if (e1.getEventType() != e2.getEventType()) {
			return false;
		}
		switch (e1.getEventType()) {
		case XMLEvent.START_ELEMENT:
			return startElementEquals(e1.asStartElement(), e2.asStartElement());
		case XMLEvent.END_ELEMENT:
			return e1.asEndElement().getName().equals(e2.asEndElement().getName());
		case XMLEvent.CHARACTERS:
			return e1.asCharacters().getData().equals(e2.asCharacters().getData());
		default:
			return true;
		}
	}

	static boolean startElementEquals(StartElement e1, StartElement e2) {
		return e1.getName().equals(e2.getName()) && attrEquals(e1, e2);
	}

	static boolean attrEquals(StartElement e1, StartElement e2) {
		List<Attribute> a1 = attributesToList(e1);
		List<Attribute> a2 = attributesToList(e2);
		if (a1.size() != a2.size()) return false;
		// This is complicated by StrippableAttribute not supporting equals().
		for (Attribute a : a1) {
			boolean foundMatch = false;
			for (Attribute test : a2) {
				if (attrEquals(a, test)) {
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch) return false;
		}
		return true;
	}

	static List<Attribute> attributesToList(StartElement el) {
		List<Attribute> attrs = new ArrayList<>();
		for (Iterator<?> it = el.getAttributes(); it.hasNext(); ) {
			attrs.add((Attribute)it.next());
		}
		return attrs;
	}

	static boolean attrEquals(Attribute a1, Attribute a2) {
		return Objects.equals(a1.getName(), a2.getName()) &&
			   Objects.equals(a1.getValue(), a2.getValue());
	}

	static void addChunksToList(List<XMLEvent> list, List<Chunk> chunks) {
		for (XMLEvents chunk : chunks) {
			list.addAll(chunk.getEvents());
		}
	}
}
