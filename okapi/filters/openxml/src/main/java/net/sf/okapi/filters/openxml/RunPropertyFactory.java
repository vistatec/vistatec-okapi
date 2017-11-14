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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static net.sf.okapi.filters.openxml.Namespaces.SpreadsheetML;
import static net.sf.okapi.filters.openxml.Namespaces.WordProcessingML;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.WPML_RUN_STYLE;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.gatherEvents;

/**
 * Provides a run property factory.
 */
class RunPropertyFactory {

    private final static int DEFAULT_EVENTS_SIZE = 2;

    /**
     * WordprocessingML toggle property names.
     */
    private static final EnumSet<WpmlTogglePropertyName> WPML_TOGGLE_PROPERTY_NAMES = EnumSet.range(
            WpmlTogglePropertyName.BOLD, WpmlTogglePropertyName.VANISH);

    /**
     * SpreadsheetML toggle property names.
     */
    private static final EnumSet<SmlPropertyName> SML_PROPERTY_NAMES = EnumSet.range(
            SmlPropertyName.BOLD, SmlPropertyName.UNDERLINE);

    private static final Set<String> DRAWINGML_HYPERLINK_NAMES =
            new HashSet<>(asList("hlinkClick", "hlinkMouseOver"));

    /**
     * Creates a run property.
     *
     * @param startElementContext   Contains XML event factory, XML event reader and StartElement
     *
     * @return A created run property
     *
     * @throws XMLStreamException
     */
    static RunProperty createRunProperty(StartElementContext startElementContext) throws XMLStreamException {
        if (RunFonts.RUN_FONTS.equals(startElementContext.getStartElement().getName())) {
            return new RunProperty.FontsRunProperty(RunFonts.createRunFonts(startElementContext));
        } else if (WPML_RUN_STYLE.equals(startElementContext.getStartElement().getName())) {
            return new RunProperty.RunStyleProperty(gatherEvents(startElementContext));
        } else if (WPML_TOGGLE_PROPERTY_NAMES.contains(
                WpmlTogglePropertyName.fromValue(startElementContext.getStartElement().getName()))) {
            return new RunProperty.WpmlToggleRunProperty(gatherEvents(startElementContext));
        } else if (SML_PROPERTY_NAMES.contains(
                SmlPropertyName.fromValue(startElementContext.getStartElement().getName()))) {
            return new RunProperty.SmlRunProperty(gatherEvents(startElementContext));
        } else if (DRAWINGML_HYPERLINK_NAMES.contains(startElementContext.getStartElement().getName().getLocalPart())) {
            return new RunProperty.HyperlinkRunProperty(gatherEvents(startElementContext));
        } else {
            return new RunProperty.GenericRunProperty(gatherEvents(startElementContext));
        }
    }

    /**
     * Creates a run property.
     *
     * @param attribute An attribute
     *
     * @return A created run property
     */
    static RunProperty createRunProperty(Attribute attribute) {
        return createRunProperty(attribute.getName(), attribute.getValue());
    }

    /**
     * Creates a run property.
     *
     * @param name A name
     * @param name A value
     *
     * @return A created run property
     */
    static RunProperty createRunProperty(QName name, String value) {
        return new RunProperty.AttributeRunProperty(name, value);
    }

    /**
     * Creates a run property.
     *
     * @param creationalParameters Creational parameters
     * @param localName            A local name
     * @param attributes           Attributes
     *
     * @return A created run property
     */
    static RunProperty createRunProperty(CreationalParameters creationalParameters, String localName, Map<String, String> attributes) {
        List<XMLEvent> events = new ArrayList<>(DEFAULT_EVENTS_SIZE);

        List<Attribute> attributeList = new ArrayList<>(attributes.size());

        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            attributeList.add(creationalParameters.getEventFactory().createAttribute(
                    creationalParameters.getPrefix(), creationalParameters.getNamespaceUri(), attribute.getKey(), attribute.getValue()));
        }

        events.add(creationalParameters.getEventFactory().createStartElement(
                creationalParameters.getPrefix(), creationalParameters.getNamespaceUri(), localName, attributeList.iterator(), null));
        events.add(creationalParameters.getEventFactory().createEndElement(
                creationalParameters.getPrefix(), creationalParameters.getNamespaceUri(), localName));

        return new RunProperty.GenericRunProperty(events);
    }

    /**
     * Provides toggle property names from the WordprocessingML.
     */
    enum WpmlTogglePropertyName {
        UNSUPPORTED(""),

        BOLD("b"),
        COMPLEX_SCRIPT_BOLD("bCs"),
        CAPS("caps"),
        EMBOSS("emboss"),
        ITALICS("i"),
        COMPLEX_SCRIPT_ITALICS("iCs"),
        IMPRINT("imprint"),
        OUTLINE("outline"),
        SHADOW("shadow"),
        SMALL_CAPS("smallCaps"),
        STRIKE_THROUGH("strike"),
        VANISH("vanish");

        QName value;

        WpmlTogglePropertyName(String value) {
            this.value = WordProcessingML.getQName(value);
        }

        QName getValue() {
            return value;
        }

        static WpmlTogglePropertyName fromValue(QName value) {
            if (null == value) {
                return UNSUPPORTED;
            }

            for (WpmlTogglePropertyName propertyName : values()) {
                if (propertyName.getValue().equals(value)) {
                    return propertyName;
                }
            }

            return UNSUPPORTED;
        }
    }

    /**
     * Provides toggle property names from the SpreadsheetML.
     */
    enum SmlPropertyName {
        UNSUPPORTED(""),

        // boolean properties (defaulting to true)
        BOLD("b", "true"),
        ITALICS("i", "true"),
        SHADOW("shadow", "true"),
        STRIKE_THROUGH("strike", "true"),

        // other properties with specific default
        UNDERLINE("u", "single");

        QName value;

        String defaultValue;

        SmlPropertyName(String value) {
            this.value = SpreadsheetML.getQName(value);
        }

        SmlPropertyName(String value, String defaultValue) {
            this.value = SpreadsheetML.getQName(value);
            this.defaultValue = defaultValue;
        }

        QName getValue() {
            return value;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        static SmlPropertyName fromValue(QName value) {
            if (null == value) {
                return UNSUPPORTED;
            }

            for (SmlPropertyName propertyName : values()) {
                if (propertyName.getValue().equals(value)) {
                    return propertyName;
                }
            }

            return UNSUPPORTED;
        }
    }
}
