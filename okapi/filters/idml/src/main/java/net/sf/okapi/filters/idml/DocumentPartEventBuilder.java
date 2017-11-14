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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.resource.DocumentPart;

import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.List;

import static net.sf.okapi.filters.idml.MarkupRange.getMarkupRange;
import static net.sf.okapi.filters.idml.MarkupRange.getMarkupRangeEndElement;
import static net.sf.okapi.filters.idml.MarkupRange.getMarkupRangeStartElement;

class DocumentPartEventBuilder implements Builder<Event> {

    private final IdGenerator documentPartIdGenerator;

    private Markup.MarkupBuilder markupBuilder;

    DocumentPartEventBuilder(IdGenerator documentPartIdGenerator) {
        this.documentPartIdGenerator = documentPartIdGenerator;

        markupBuilder = new Markup.MarkupBuilder();
    }

    void addMarkupRange(List<XMLEvent> events) {
        markupBuilder.addMarkupRange(getMarkupRange(events));
    }

    void addMarkupRangeStartElement(StartElement startElement) {
        markupBuilder.addMarkupRange(getMarkupRangeStartElement(startElement));
    }

    void addMarkupRangeEndElement(EndElement endElement) {
        markupBuilder.addMarkupRange(getMarkupRangeEndElement(endElement));
    }

    void addMarkupRangeElement(StoryChildElement storyChildElement) {
        markupBuilder.addMarkupRange(storyChildElement);
    }

    @Override
    public Event build() {
        Markup markup = markupBuilder.build();

        if (markup.getMarkupRanges().isEmpty()) {
            return null;
        }

        DocumentPart documentPart = new DocumentPart(documentPartIdGenerator.createId(), false);
        documentPart.setSkeleton(new MarkupSkeleton(markup));

        return new Event(EventType.DOCUMENT_PART, documentPart);
    }
}
