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
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.StartGroup;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static net.sf.okapi.common.IdGenerator.DOCUMENT_PART;
import static net.sf.okapi.common.IdGenerator.TEXT_UNIT;

class ReferentEventsAccumulator implements Accumulator<Event> {

    private final Parameters parameters;
    private final XMLEventFactory eventFactory;
    private final StoryChildElement.StyledTextReferenceElement styledTextReferenceElement;
    private final String parentId;
    private final IdGenerator idGenerator;

    private IdGenerator referentIdGenerator;

    ReferentEventsAccumulator(Parameters parameters, XMLEventFactory eventFactory, StoryChildElement.StyledTextReferenceElement styledTextReferenceElement,
                              String parentId, IdGenerator idGenerator) {
        this.parameters = parameters;
        this.eventFactory = eventFactory;
        this.styledTextReferenceElement = styledTextReferenceElement;
        this.parentId = parentId;
        this.idGenerator = idGenerator;
    }

    @Override
    public List<Event> accumulate() throws XMLStreamException {
        List<Event> events = new ArrayList<>();
        List<Event> referentEvents = new ArrayList<>();

        StartGroup startGroup = createStartGroup();
        events.add(new Event(EventType.START_GROUP, startGroup));

        if (styledTextReferenceElement instanceof StoryChildElement.StyledTextReferenceElement.Table) {

            for (StoryChildElement.StyledTextReferenceElement.Table.Cell cell : ((StoryChildElement.StyledTextReferenceElement.Table) styledTextReferenceElement).getCells()) {
                referentIdGenerator = getReferentIdGenerator(startGroup.getId(), cell.getStartElement().getName().getLocalPart());
                referentEvents.addAll(new ReferentEventsAccumulator(parameters, eventFactory, cell, startGroup.getId(), referentIdGenerator).accumulate());
            }

        } else {

            IdGenerator documentPartIdGenerator = new IdGenerator(startGroup.getId(), DOCUMENT_PART);
            IdGenerator textUnitIdGenerator = new IdGenerator(startGroup.getId(), TEXT_UNIT);

            ListIterator<StoryChildElement> storyChildElementIterator = styledTextReferenceElement.getStoryChildElements().listIterator();

            referentEvents.addAll(new StoryChildElementsEventsAccumulator(parameters, eventFactory, documentPartIdGenerator, textUnitIdGenerator, storyChildElementIterator).accumulate());
        }

        events.addAll(referentEvents);
        events.add(new Event(EventType.END_GROUP, new Ending(startGroup.getId())));

        return events;
    }

    private StartGroup createStartGroup() {
        StartGroup startGroup = new StartGroup(parentId, idGenerator.createId(), true);
        startGroup.setSkeleton(new StyledTextReferenceSkeleton(styledTextReferenceElement));

        return startGroup;
    }

    private IdGenerator getReferentIdGenerator(String parentId, String prefix) {
        if (null != referentIdGenerator) {
            return referentIdGenerator;
        }

        return new IdGenerator(parentId, prefix);
    }
}
