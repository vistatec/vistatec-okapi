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

import net.sf.okapi.common.ISkeleton;

import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static net.sf.okapi.filters.idml.ParsingIdioms.UNEXPECTED_STRUCTURE;

class ReferenceableEventsWriter {

    private final ReferenceableEventsMerger referenceableEventMerger;
    private final StyleRangeEventsGenerator styleRangeEventsGenerator;

    private StoryChildElementsWriter storyChildElementsWriter;
    private List<XMLEvent> events;

    ReferenceableEventsWriter(ReferenceableEventsMerger referenceableEventMerger, StyleRangeEventsGenerator styleRangeEventsGenerator) {
        this.referenceableEventMerger = referenceableEventMerger;
        this.styleRangeEventsGenerator = styleRangeEventsGenerator;
    }

    List<XMLEvent> write(List<ReferenceableEvent> referenceableEvents) {
        events = new ArrayList<>();
        storyChildElementsWriter = new StoryChildElementsWriter(styleRangeEventsGenerator);

        ReferenceableEvent lastStyledReferenceableEvent = getLastStyledReferenceableEvent(referenceableEvents);

        for (ReferenceableEvent referenceableEvent : referenceableEvents) {

            writeReferenceableEvent(referenceableEvent);

            if (referenceableEvent == lastStyledReferenceableEvent) {
                events.addAll(styleRangeEventsGenerator.generateCharacterStyleRangeEnd());
                events.addAll(styleRangeEventsGenerator.generateParagraphStyleRangeEnd());
            }
        }

        return events;
    }

    private ReferenceableEvent getLastStyledReferenceableEvent(List<ReferenceableEvent> referenceableEvents) {
        ListIterator<ReferenceableEvent> listIterator = referenceableEvents.listIterator(referenceableEvents.size());

        while (listIterator.hasPrevious()) {
            ReferenceableEvent referenceableEvent = listIterator.previous();

            if (isStyledReferenceableEvent(referenceableEvent)) {
                return referenceableEvent;
            }
        }

        return null;
    }

    private boolean isStyledReferenceableEvent(ReferenceableEvent referenceableEvent) {
        switch (referenceableEvent.getEvent().getEventType()) {
            case TEXT_UNIT:
                return true;
            case DOCUMENT_PART:
                return referenceableEvent.getEvent().getDocumentPart().getSkeleton() instanceof StyledTextSkeleton;
            default:
                throw new IllegalStateException(UNEXPECTED_STRUCTURE);
        }
    }

    private void writeReferenceableEvent(ReferenceableEvent referenceableEvent) {

        switch (referenceableEvent.getEvent().getEventType()) {
            case TEXT_UNIT:
                writeTextUnitReferenceableEvent(referenceableEvent);
                break;
            case DOCUMENT_PART:
                writeDocumentPartReferenceableEvent(referenceableEvent);
            default:
        }
    }

    private void writeTextUnitReferenceableEvent(ReferenceableEvent referenceableEvent) {
        referenceableEventMerger.merge(referenceableEvent);

        StyledTextSkeleton skeleton = (StyledTextSkeleton) referenceableEvent.getEvent().getTextUnit().getSkeleton();
        events.addAll(storyChildElementsWriter.write(skeleton.getStoryChildElements()));
    }

    private void writeDocumentPartReferenceableEvent(ReferenceableEvent referenceableEvent) {

        ISkeleton skeleton = referenceableEvent.getEvent().getDocumentPart().getSkeleton();

        if (skeleton instanceof MarkupSkeleton) {
            Markup markup = ((MarkupSkeleton) skeleton).getMarkup();
            events.addAll(markup.getEvents());

            return;

        } else if (skeleton instanceof StyledTextSkeleton) {
            events.addAll(storyChildElementsWriter.write(((StyledTextSkeleton) skeleton).getStoryChildElements()));

            return;
        }

        throw new IllegalStateException(UNEXPECTED_STRUCTURE);
    }
}
