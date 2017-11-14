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
import java.util.Objects;

class Properties implements Eventive {

    private final StartElement startElement;
    private final List<Property> properties;
    private final EndElement endElement;

    Properties(StartElement startElement, List<Property> properties, EndElement endElement) {
        this.startElement = startElement;
        this.properties = properties;
        this.endElement = endElement;
    }

    static Properties getEmptyProperties() {
        return new EmptyProperties();
    }

    static Property.PathGeometryProperty getPathGeometryProperty(List<Property> properties) {

        for (Property property : properties) {
            if (property instanceof Property.PathGeometryProperty) {
                return (Property.PathGeometryProperty) property;
            }
        }

        return null;
    }

    @Override
    public List<XMLEvent> getEvents() {
        List<XMLEvent> events = new ArrayList<>();

        events.add(startElement);

        for (Property property : properties) {
            events.addAll(property.getEvents());
        }

        events.add(endElement);

        return events;
    }

    StartElement getStartElement() {
        return startElement;
    }

    List<Property> getProperties() {
        return properties;
    }

    EndElement getEndElement() {
        return endElement;
    }

    boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        Properties that = (Properties) o;

        return Objects.equals(getProperties(), that.getProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProperties());
    }

    static class PropertiesBuilder implements Builder<Properties> {
        private StartElement startElement;
        private List<Property> properties = new ArrayList<>();
        private EndElement endElement;

        PropertiesBuilder setStartElement(StartElement startElement) {
            this.startElement = startElement;
            return this;
        }

        PropertiesBuilder addProperty(Property property) {
            properties.add(property);
            return this;
        }

        PropertiesBuilder addProperties(List<Property> properties) {
            this.properties.addAll(properties);
            return this;
        }

        PropertiesBuilder setEndElement(EndElement endElement) {
            this.endElement = endElement;
            return this;
        }

        @Override
        public Properties build() {
            return new Properties(startElement, properties, endElement);
        }
    }

    static class EmptyProperties extends Properties {

        EmptyProperties() {
            super(null, Collections.<Property>emptyList(), null);
        }

        @Override
        public List<XMLEvent> getEvents() {
            return Collections.emptyList();
        }
    }
}
