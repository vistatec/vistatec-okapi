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
import net.sf.okapi.common.IdGenerator;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static java.util.Collections.singletonList;

class StoryEventsAccumulator implements Accumulator<Event> {

    private final Story story;
    private final Parameters parameters;
    private final XMLEventFactory eventFactory;
    private final IdGenerator documentPartIdGenerator;
    private final IdGenerator textUnitIdGenerator;

    private List<Event> events;

    StoryEventsAccumulator(Story story, Parameters parameters, XMLEventFactory eventFactory,
                           IdGenerator documentPartIdGenerator, IdGenerator textUnitIdGenerator) {
        this.story = story;
        this.parameters = parameters;
        this.eventFactory = eventFactory;
        this.documentPartIdGenerator = documentPartIdGenerator;
        this.textUnitIdGenerator = textUnitIdGenerator;

        events = new ArrayList<>();
    }

    @Override
    public List<Event> accumulate() throws XMLStreamException {

        DocumentPartEventBuilder documentPartEventBuilder = new DocumentPartEventBuilder(documentPartIdGenerator);

        documentPartEventBuilder.addMarkupRange(singletonList(story.getStartDocumentEvent()));
        documentPartEventBuilder.addMarkupRangeStartElement(story.getWrappingStoryStartElement());
        documentPartEventBuilder.addMarkupRangeStartElement(story.getStoryStartElement());

        events.add(documentPartEventBuilder.build());

        ListIterator<StoryChildElement> storyChildElementIterator = story.getChildElements().listIterator();

        List<Event> storyChildElementEvents = new StoryChildElementsEventsAccumulator(parameters, eventFactory, documentPartIdGenerator, textUnitIdGenerator, storyChildElementIterator).accumulate();
        events.addAll(storyChildElementEvents);

        documentPartEventBuilder = new DocumentPartEventBuilder(documentPartIdGenerator);

        documentPartEventBuilder.addMarkupRangeEndElement(story.getStoryEndElement());
        documentPartEventBuilder.addMarkupRangeEndElement(story.getWrappingStoryEndElement());
        documentPartEventBuilder.addMarkupRange(singletonList(story.getEndDocumentEvent()));

        events.add(documentPartEventBuilder.build());

        return events;
    }
}
