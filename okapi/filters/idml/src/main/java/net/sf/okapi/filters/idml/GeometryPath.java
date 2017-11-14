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
import java.util.Arrays;
import java.util.List;

class GeometryPath implements Eventive {

    private final StartElement startElement;
    private final StartElement pathPointArrayStartElement;
    private final List<PathPoint> pathPoints;
    private final EndElement pathPointArrayEndElement;
    private final EndElement endElement;

    GeometryPath(StartElement startElement, StartElement pathPointArrayStartElement, List<PathPoint> pathPoints, EndElement pathPointArrayEndElement, EndElement endElement) {
        this.startElement = startElement;
        this.pathPointArrayStartElement = pathPointArrayStartElement;
        this.pathPoints = pathPoints;
        this.pathPointArrayEndElement = pathPointArrayEndElement;
        this.endElement = endElement;
    }

    StartElement getStartElement() {
        return startElement;
    }

    StartElement getPathPointArrayStartElement() {
        return pathPointArrayStartElement;
    }

    List<PathPoint> getPathPoints() {
        return pathPoints;
    }

    EndElement getPathPointArrayEndElement() {
        return pathPointArrayEndElement;
    }

    EndElement getEndElement() {
        return endElement;
    }

    @Override
    public List<XMLEvent> getEvents() {
        List<XMLEvent> events = new ArrayList<>();

        for (PathPoint pathPoint : pathPoints) {
            events.addAll(pathPoint.getEvents());
        }

        return events;
    }

    static class GeometryPathBuilder implements Builder<GeometryPath> {

        private StartElement startElement;
        private StartElement pathPointArrayStartElement;
        private List<PathPoint> pathPoints = new ArrayList<>();
        private EndElement pathPointArrayEndElement;
        private EndElement endElement;

        GeometryPathBuilder setStartElement(StartElement startElement) {
            this.startElement = startElement;
            return this;
        }

        GeometryPathBuilder setPathPointArrayStartElement(StartElement pathPointArrayStartElement) {
            this.pathPointArrayStartElement = pathPointArrayStartElement;
            return this;
        }

        GeometryPathBuilder addPathPoint(PathPoint pathPoint) {
            pathPoints.add(pathPoint);
            return this;
        }

        GeometryPathBuilder setPathPointArrayEndElement(EndElement pathPointArrayEndElement) {
            this.pathPointArrayEndElement = pathPointArrayEndElement;
            return this;
        }

        GeometryPathBuilder setEndElement(EndElement endElement) {
            this.endElement = endElement;
            return this;
        }

        @Override
        public GeometryPath build() {
            return new GeometryPath(startElement, pathPointArrayStartElement, pathPoints, pathPointArrayEndElement, endElement);
        }
    }

    static class PathPoint implements Eventive {

        private final StartElement startElement;
        private final EndElement endElement;

        PathPoint(StartElement startElement, EndElement endElement) {
            this.startElement = startElement;
            this.endElement = endElement;
        }

        StartElement getStartElement() {
            return startElement;
        }

        EndElement getEndElement() {
            return endElement;
        }

        @Override
        public List<XMLEvent> getEvents() {
            return Arrays.asList(startElement, endElement);
        }
    }
}
