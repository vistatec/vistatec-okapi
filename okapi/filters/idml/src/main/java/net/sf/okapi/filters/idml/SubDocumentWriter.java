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
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static net.sf.okapi.filters.idml.ParsingIdioms.UNEXPECTED_STRUCTURE;

class SubDocumentWriter implements IFilterWriter {

    private static String ERROR_CREATING_SUB_DOCUMENT_WRITER = "Error creating sub document writer";
    private static String ERROR_ADDING_EVENTS_TO_SUB_DOCUMENT_WRITER = "Error adding events to sub document writer";
    private static String ERROR_CLOSING_SUB_DOCUMENT_WRITER = "Error closing sub document writer";

    private final Parameters parameters;
    private final ReferenceableEventsWriter referenceableEventsWriter;
    private final XMLEventWriter writer;

    private final List<ReferenceableEvent> referenceableEvents;
    private final Deque<ReferenceableEvent> parentReferenceableEvents;

    SubDocumentWriter(Parameters parameters, XMLOutputFactory outputFactory, Charset charset, String outputPath,
                      ReferenceableEventsWriter referenceableEventsWriter) {
        this.parameters = parameters;
        this.referenceableEventsWriter = referenceableEventsWriter;

        writer = getWriter(outputPath, charset, outputFactory);

        referenceableEvents = new ArrayList<>();
        parentReferenceableEvents = new ArrayDeque<>();
    }

    private XMLEventWriter getWriter(String outputPath, Charset charset, XMLOutputFactory outputFactory) {
        Util.createDirectories(outputPath);
        try {
            return outputFactory.createXMLEventWriter(new FileOutputStream(outputPath), charset.name());
        } catch (XMLStreamException | FileNotFoundException e) {
            throw new OkapiIOException(ERROR_CREATING_SUB_DOCUMENT_WRITER, e);
        }
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void setOptions(LocaleId locale, String defaultEncoding) {
    }

    @Override
    public void setOutput(String path) {
    }

    @Override
    public void setOutput(OutputStream output) {
    }

    @Override
    public Event handleEvent(Event event) {

        switch (event.getEventType()) {
            case START_GROUP:
                handleStartGroupEvent(event);
                break;
            case END_GROUP:
                handleEndGroupEvent();
                break;
            case TEXT_UNIT:
                handleTextUnitEvent(event);
                break;
            case DOCUMENT_PART:
                handleDocumentPartEvent(event);
                break;
            case END_DOCUMENT:
                handleEndDocumentEvent(event);
                close();
                break;
            case START_DOCUMENT:
            case MULTI_EVENT:
            case START_SUBDOCUMENT:
            case END_SUBDOCUMENT:
            case START_SUBFILTER:
            case END_SUBFILTER:
            default:
                break;
        }

        return event;
    }

    private void handleStartGroupEvent(Event event) {
        ReferenceableEvent parentReferenceableEvent = findParentReferenceableEvent(event.getStartGroup().getParentId());

        ReferenceableEvent referentEvent = new ReferenceableEvent(event.getStartGroup().getId(), event);
        parentReferenceableEvent.addReferentEvent(referentEvent);

        parentReferenceableEvents.push(referentEvent);
    }

    private ReferenceableEvent findParentReferenceableEvent(String parentId) {
        if (!parentReferenceableEvents.isEmpty()) {
            return parentReferenceableEvents.peek();
        }

        ReferenceableEvent referenceableEvent = findReferenceableEvent(referenceableEvents, parentId);

        if (null == referenceableEvent) {
            throw new IllegalStateException(UNEXPECTED_STRUCTURE);
        }

        return referenceableEvent;
    }

    private ReferenceableEvent findReferenceableEvent(List<ReferenceableEvent> referenceableEvents, String eventId) {
        for (ReferenceableEvent referenceableEvent : referenceableEvents) {

            if (referenceableEvent.getEventId().equals(eventId)) {
                return referenceableEvent;
            }

            if (!referenceableEvent.getReferentEvents().isEmpty()) {
                ReferenceableEvent foundReferenceableEvent = findReferenceableEvent(referenceableEvent.getReferentEvents(), eventId);

                if (null != foundReferenceableEvent) {
                    return foundReferenceableEvent;
                }
            }
        }

        return null;
    }

    private void handleEndGroupEvent() {
        parentReferenceableEvents.pop();
    }

    private void handleTextUnitEvent(Event event) {
        ReferenceableEvent referenceableEvent = new ReferenceableEvent(event.getTextUnit().getId(), event);
        addReferenceableEvent(referenceableEvent);
    }

    private void handleDocumentPartEvent(Event event) {
        ReferenceableEvent referenceableEvent = new ReferenceableEvent(event.getDocumentPart().getId(), event);
        addReferenceableEvent(referenceableEvent);
    }

    private void addReferenceableEvent(ReferenceableEvent referenceableEvent) {
        if (!parentReferenceableEvents.isEmpty()) {
            ReferenceableEvent parentReferenceableEvent = parentReferenceableEvents.peek();
            parentReferenceableEvent.addReferentEvent(referenceableEvent);
        } else {
            referenceableEvents.add(referenceableEvent);
        }
    }

    private void handleEndDocumentEvent(Event event) {
        try {
            addEvents(referenceableEventsWriter.write(referenceableEvents));
        } catch (XMLStreamException e) {
            throw new OkapiIOException(ERROR_ADDING_EVENTS_TO_SUB_DOCUMENT_WRITER, e);
        }
    }

    private void addEvents(List<XMLEvent> events) throws XMLStreamException {
        for (XMLEvent event : events) {
            writer.add(event);
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (XMLStreamException e) {
            throw new OkapiIOException(ERROR_CLOSING_SUB_DOCUMENT_WRITER, e);
        }
    }

    @Override
    public IParameters getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(IParameters params) {
    }

    @Override
    public void cancel() {
    }

    @Override
    public EncoderManager getEncoderManager() {
        return null;
    }

    @Override
    public ISkeletonWriter getSkeletonWriter() {
        return null;
    }
}
