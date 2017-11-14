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

class StoryChildElementsEventsAccumulator implements Accumulator<Event> {

    private final Parameters parameters;
    private final XMLEventFactory eventFactory;
    private final IdGenerator documentPartIdGenerator;
    private final IdGenerator textUnitIdGenerator;
    private final ListIterator<StoryChildElement> storyChildElementIterator;

    private List<Event> events;
    private List<Event> referentEvents;

    StoryChildElementsEventsAccumulator(Parameters parameters, XMLEventFactory eventFactory,
                                        IdGenerator documentPartIdGenerator, IdGenerator textUnitIdGenerator,
                                        ListIterator<StoryChildElement> storyChildElementIterator) {
        this.parameters = parameters;
        this.eventFactory = eventFactory;
        this.documentPartIdGenerator = documentPartIdGenerator;
        this.textUnitIdGenerator = textUnitIdGenerator;
        this.storyChildElementIterator = storyChildElementIterator;

        events = new ArrayList<>();
        referentEvents = new ArrayList<>();
    }

    @Override
    public List<Event> accumulate() throws XMLStreamException {
        DocumentPartEventBuilder documentPartEventBuilder = new DocumentPartEventBuilder(documentPartIdGenerator);

        while (storyChildElementIterator.hasNext()) {
            StoryChildElement storyChildElement = storyChildElementIterator.next();

            if (storyChildElement instanceof StoryChildElement.StyledTextElement) {
                documentPartEventBuilder = addMarkupElementsEvent(documentPartEventBuilder);

                storyChildElementIterator.previous();
                addTextUnitElementsEvents(new TextUnitElementsAccumulator(storyChildElementIterator).accumulate());
                continue;
            }

            documentPartEventBuilder.addMarkupRangeElement(storyChildElement);
        }

        addMarkupElementsEvent(documentPartEventBuilder);
        events.addAll(referentEvents);

        return events;
    }

    private DocumentPartEventBuilder addMarkupElementsEvent(DocumentPartEventBuilder documentPartEventBuilder) {
        Event event = documentPartEventBuilder.build();

        if (null != event) {
            events.add(event);

            return  new DocumentPartEventBuilder(documentPartIdGenerator);
        }

        return documentPartEventBuilder;
    }

    private void addTextUnitElementsEvents(List<StoryChildElement> textUnitElements) throws XMLStreamException {
        List<Event> currentEvents = new TextUnitElementsMapper(parameters, eventFactory, documentPartIdGenerator, textUnitIdGenerator).map(textUnitElements);

        // add the very first event as a text unit and others as referent events
        this.events.add(currentEvents.get(0));
        referentEvents.addAll(currentEvents.subList(1, currentEvents.size()));
    }
}
