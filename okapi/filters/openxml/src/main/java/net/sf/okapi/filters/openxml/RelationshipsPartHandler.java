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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.zip.ZipEntry;

import static net.sf.okapi.filters.openxml.XMLEventSerializer.serialize;

class RelationshipsPartHandler extends GenericPartHandler {
    private static final QName RELATIONSHIP_TAG_NAME = new QName(Namespaces.Relationships.getURI(), "Relationship");
    private static final QName RELATIONSHIP_TYPE = new QName("Type");
    private static final QName RELATIONSHIP_TARGET_MODE = new QName("TargetMode");
    private static final QName RELATIONSHIP_TARGET = new QName("Target");
    private final IdGenerator textUnitIdGenerator;
    private String subDocumentId;
    private XMLEventReader xmlEventReader;
    private Iterator<Event> filterEventIterator;

    private static final String HYPERLINK_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";
    private static final String TARGET_MODE_EXTERNAL = "External";

    public RelationshipsPartHandler(ConditionalParameters params, OpenXMLZipFile zipFile, ZipEntry entry) {
        super(params, entry.getName());
        this.zipFile = zipFile;
        this.entry = entry;
        this.partName = entry.getName();
        textUnitIdGenerator = new IdGenerator(entry.getName(), IdGenerator.TEXT_UNIT);
        markup = new Markup();
    }

    @Override
    public Event open(String documentId, String subDocumentId, LocaleId srcLang) throws IOException, XMLStreamException {
        this.subDocumentId = subDocumentId;
        /**
         * Process the XML event stream, simplifying as we go.  Non-block content is
         * written as a document part.  Blocks are parsed, then converted into TextUnit structures.
         */
        xmlEventReader = zipFile.getInputFactory().createXMLEventReader(
                new InputStreamReader(new BufferedInputStream(zipFile.getInputStream(entry)), StandardCharsets.UTF_8));

        handlePart();
        return createStartSubDocumentEvent(documentId, subDocumentId);
    }

    private void handlePart() throws XMLStreamException {
        while(xmlEventReader.hasNext()) {
            XMLEvent e = xmlEventReader.nextEvent();

            if (!e.isStartElement() || !e.asStartElement().getName().equals(RELATIONSHIP_TAG_NAME)) {
                filterEvents.add(new Event(EventType.DOCUMENT_PART, createDocumentPart(e)));
                continue;
            }

            Attribute type = e.asStartElement().getAttributeByName(RELATIONSHIP_TYPE);
            Attribute targetMode = e.asStartElement().getAttributeByName(RELATIONSHIP_TARGET_MODE);
            Attribute target = e.asStartElement().getAttributeByName(RELATIONSHIP_TARGET);

            if (null == type || targetMode == null || target == null
                    || !HYPERLINK_TYPE.equals(type.getValue()) || !TARGET_MODE_EXTERNAL.equals(targetMode.getValue())
                    || target.getValue().isEmpty()) {
                filterEvents.add(new Event(EventType.DOCUMENT_PART, createDocumentPart(e)));
                continue;
            }


            TextUnit textUnit = createTextUnit(e);
            filterEvents.add(new Event(EventType.TEXT_UNIT, textUnit));
        }

        filterEvents.add(new Event(EventType.END_DOCUMENT, new Ending(subDocumentId)));
        filterEventIterator = filterEvents.iterator();
    }

    private DocumentPart createDocumentPart(XMLEvent e) {
        return new DocumentPart(documentPartIdGenerator.createId(), false , new GenericSkeleton(serialize(e)));
    }

    private TextUnit createTextUnit(XMLEvent event) throws XMLStreamException {
        String serializedEvent = serialize(event);
        String attributeName = !RELATIONSHIP_TARGET.getPrefix().isEmpty() ?
                RELATIONSHIP_TARGET.getPrefix() + ":" + RELATIONSHIP_TARGET.getLocalPart() :
                RELATIONSHIP_TARGET.getLocalPart();
        String attributeValue = event.asStartElement().getAttributeByName(RELATIONSHIP_TARGET).getValue();
        String[] eventParts = serializedEvent.split(attributeName + "=\"" + attributeValue + "\"");
        GenericSkeleton skel = new GenericSkeleton();
        skel.append(eventParts[0]);
        skel.append(attributeName + "=\"");
        TextUnit textUnit = new TextUnit(textUnitIdGenerator.createId());
        skel.addContentPlaceholder(textUnit);
        skel.append("\"" + eventParts[1]);
        textUnit.setSkeleton(skel);
        textUnit.setSourceContent(new TextFragment(attributeValue));
        return textUnit;
    }

    @Override
    public boolean hasNext() {
        return filterEventIterator.hasNext();
    }

    @Override
    public Event next() {
        return filterEventIterator.next();
    }

    @Override
    public void close() {

    }

    @Override
    public void logEvent(Event e) {

    }
}
