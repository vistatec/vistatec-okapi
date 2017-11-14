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
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.skeleton.ZipSkeleton;

import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createGeneralMarkupComponent;

abstract class GenericPartHandler implements OpenXMLPartHandler {
    protected OpenXMLZipFile zipFile;
    protected ZipEntry entry;
    protected ConditionalParameters params;
    protected String partName;
    protected List<Event> filterEvents = new ArrayList<>();
    protected List<XMLEvent> documentPartEvents = new ArrayList<>();
    protected Markup markup;
    protected IdGenerator documentPartIdGenerator;

    GenericPartHandler(ConditionalParameters params, String partName) {
        this.params = params;
        this.partName = partName;
        documentPartIdGenerator = new IdGenerator(partName, IdGenerator.DOCUMENT_PART);
    }

    protected Event createStartSubDocumentEvent(String documentId, String subDocumentId) {
        StartSubDocument sd = new StartSubDocument(documentId, subDocumentId);
        sd.setName(partName);
        if (zipFile != null) { // XXX This null check is a hack for testing
            ZipSkeleton zs = new ZipSkeleton(zipFile.getZip(), entry);
            sd.setSkeleton(zs);
        }
        ConditionalParameters clonedParams = params.clone();
        sd.setFilterParameters(clonedParams);
        return new Event(EventType.START_SUBDOCUMENT, sd);
    }

    protected void addEventToDocumentPart(XMLEvent e) {
        documentPartEvents.add(e);
    }

    protected void flushDocumentPart() {
        if (!documentPartEvents.isEmpty()) {
            markup.addComponent(createGeneralMarkupComponent(documentPartEvents));
            documentPartEvents = new ArrayList<>();
        }

        if (!markup.getComponents().isEmpty()) {
            DocumentPart documentPart = new DocumentPart(documentPartIdGenerator.createId(), false);
            documentPart.setSkeleton(new MarkupSkeleton(markup));
            markup = new Block.BlockMarkup();

            filterEvents.add(new Event(EventType.DOCUMENT_PART, documentPart));
        }
    }
}
