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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_LEVEL;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.WPML_PARAGRAPH_STYLE;

/**
 * Provides a block property markup component.
 */
class BlockProperties extends MarkupComponent implements Nameable {
    private XMLEventFactory eventFactory;
    private StartElement startElement;
    private EndElement endElement;

    private List<Attribute> attributes = new ArrayList<>();
    private List<BlockProperty> properties = new ArrayList<>();

    BlockProperties(XMLEventFactory eventFactory, StartElement startElement, EndElement endElement, List<BlockProperty> properties) {
        this.eventFactory = eventFactory;
        this.startElement = startElement;
        this.endElement = endElement;

        Iterator iterator = startElement.getAttributes();

        while (iterator.hasNext()) {
            attributes.add((Attribute) iterator.next());
        }

        this.properties.addAll(properties);
    }

    @Override
    public QName getName() {
        return startElement.getName();
    }

    @Override
    public List<XMLEvent> getEvents() {
        List<XMLEvent> events = new ArrayList<>();

        events.add(eventFactory.createStartElement(startElement.getName(), getAttributes().iterator(), startElement.getNamespaces()));

        for (BlockProperty property : properties) {
            events.addAll(property.getEvents());
        }
        events.add(endElement);

        return events;
    }

    List<Attribute> getAttributes() {
        return attributes;
    }

    List<BlockProperty> getProperties() {
        return properties;
    }

    BlockProperty getParagraphStyleProperty() {
        for (BlockProperty property : properties) {
            if (WPML_PARAGRAPH_STYLE.equals(property.getName())) {
                return property;
            }
        }

        return null;
    }

    Attribute getParagraphLevelAttribute() {
        for (Attribute attribute : attributes) {
            if (LOCAL_LEVEL.equals(attribute.getName().getLocalPart())) {
                return attribute;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockProperties that = (BlockProperties) o;

        return Objects.equals(startElement, that.startElement)
                && Objects.equals(endElement, that.endElement)
                && Objects.equals(attributes, that.attributes)
                && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startElement, endElement, attributes, properties);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + properties.size() + ")" + properties;
    }
}
