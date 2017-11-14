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

import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class Property extends MarkupRange.MarkupRangeElement {

    Property(StartElement startElement, List<XMLEvent> innerEvents, EndElement endElement) {
        super(startElement, innerEvents, endElement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        Property that = (Property) o;

        return Objects.equals(getStartElement(), that.getStartElement())
                && Objects.equals(getInnerEvents(), that.getInnerEvents())
                && Objects.equals(getEndElement(), that.getEndElement());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStartElement(), getInnerEvents(), getEndElement());
    }

    static class PropertyBuilder extends MarkupRangeElementBuilder {

        @Override
        public Property build() {
            return new Property(startElement, innerEvents, endElement);
        }
    }

    static class PathGeometryProperty extends Property {

        private final List<GeometryPath> geometryPaths;

        PathGeometryProperty(StartElement startElement, List<GeometryPath> geometryPaths, EndElement endElement) {
            super(startElement, Collections.<XMLEvent>emptyList(), endElement);
            this.geometryPaths = geometryPaths;
        }

        @Override
        List<XMLEvent> getInnerEvents() {
            List<XMLEvent> events = new ArrayList<>();

            for (GeometryPath geometryPath : geometryPaths) {
                events.addAll(geometryPath.getEvents());
            }

            return events;
        }

        List<GeometryPath> getGeometryPaths() {
            return geometryPaths;
        }

        static class PathGeometryPropertyBuilder extends PropertyBuilder {

            private List<GeometryPath> geometryPaths = new ArrayList<>();

            PathGeometryPropertyBuilder addGeometryPath(GeometryPath geometryPath) {
                geometryPaths.add(geometryPath);
                return this;
            }

            @Override
            public PathGeometryProperty build() {
                return new PathGeometryProperty(startElement, geometryPaths, endElement);
            }
        }
    }
}
