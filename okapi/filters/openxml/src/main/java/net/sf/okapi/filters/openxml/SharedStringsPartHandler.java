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
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;

import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createGeneralMarkupComponent;
import static net.sf.okapi.filters.openxml.StartElementContextFactory.createStartElementContext;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isStringItemStartEvent;

class SharedStringsPartHandler extends GenericPartHandler {
    private SharedStringMap sharedStringMap;

    private XMLEventFactory eventFactory;
    private StyleDefinitions styleDefinitions;

    private IdGenerator textUnitId;
    private IdGenerator nestedBlockId;

    private XMLEventReader xmlReader;
    private Iterator<Event> filterEventIterator;
    private String docId, subDocId;
    private File rewrittenStringsTable;
    private int sharedStringIndex = 0;

    SharedStringsPartHandler(ConditionalParameters cparams, OpenXMLZipFile zipFile, ZipEntry entry, StyleDefinitions styleDefinitions, SharedStringMap sharedStringMap) {
        this(cparams, zipFile.getEventFactory(), entry.getName(), styleDefinitions);
        this.zipFile = zipFile;
        this.entry = entry;
        this.sharedStringMap = sharedStringMap;
    }

    SharedStringsPartHandler(ConditionalParameters cparams, XMLEventFactory eventFactory, String partName, StyleDefinitions styleDefinitions) {
        super(cparams, partName);
        this.eventFactory = eventFactory;
        this.styleDefinitions = styleDefinitions;

        documentPartIdGenerator = new IdGenerator(partName, IdGenerator.DOCUMENT_PART);
        textUnitId = new IdGenerator(partName, IdGenerator.TEXT_UNIT);
        nestedBlockId = new IdGenerator(null);
        markup = new Block.BlockMarkup();
    }

    /**
     * Open this part and perform any initial processing.  Return the
     * first event for this part.  In this case, it's a START_SUBDOCUMENT
     * event.
     *
     * @param docId    document identifier
     * @param subDocId sub-document identifier
     * @param srcLang  the locale of the source
     * @return Event
     * @throws IOException
     * @throws XMLStreamException
     */
    @Override
    public Event open(String docId, String subDocId, LocaleId srcLang) throws IOException, XMLStreamException {
        this.docId = docId;
        this.subDocId = subDocId;
        /**
         * Process the XML event stream, simplifying as we go.  Non-block content is
         * written as a document part.  Blocks are parsed, then converted into TextUnit structures.
         */
        xmlReader = zipFile.getInputFactory().createXMLEventReader(
                new InputStreamReader(new BufferedInputStream(zipFile.getInputStream(entry)), StandardCharsets.UTF_8));
        return open(docId, subDocId, xmlReader);
    }

    // Package-private for test.  XXX This is an artifact of the overall PartHandler
    // interface needing work.
    Event open(String docId, String subDocId, XMLEventReader xmlReader) throws XMLStreamException, IOException {
        SharedStringsDenormalizer deno = new SharedStringsDenormalizer(zipFile.getEventFactory(), sharedStringMap);
        XMLEventReader reader = zipFile.getInputFactory().createXMLEventReader(
                new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8));
        rewrittenStringsTable = File.createTempFile("sharedStrings", ".xml");
        XMLEventWriter writer = zipFile.getOutputFactory().createXMLEventWriter(
                new OutputStreamWriter(new FileOutputStream(rewrittenStringsTable), StandardCharsets.UTF_8));
        deno.process(reader, writer);

        InputStream is = new BufferedInputStream(new FileInputStream(rewrittenStringsTable));

        this.xmlReader = XMLInputFactory.newInstance().createXMLEventReader(is);
        try {
            process();
        } finally {
            if (xmlReader != null) {
                xmlReader.close();
            }
            rewrittenStringsTable.delete();
        }
        return createStartSubDocumentEvent(docId, subDocId);
    }

    private void process() throws XMLStreamException {
        XMLEvent e = null;
        while (xmlReader.hasNext()) {
            e = xmlReader.nextEvent();
            if (isStringItemStartEvent(e) && sharedStringMap.isStringVisible(sharedStringIndex++)) {
                flushDocumentPart();
                StartElementContext startElementContext = createStartElementContext(e.asStartElement(), xmlReader, eventFactory, params);
                StringItem stringItem = new StringItemParser(startElementContext, nestedBlockId, styleDefinitions).parse();

                StringItemTextUnitMapper mapper = new StringItemTextUnitMapper(stringItem, textUnitId);
                if (mapper.getTextUnits().isEmpty()) {
                    addBlockChunksToDocumentPart(stringItem.getChunks());
                } else {
                    for (ITextUnit tu : mapper.getTextUnits()) {
                        filterEvents.add(new Event(EventType.TEXT_UNIT, tu));
                    }
                }
            } else {
                addEventToDocumentPart(e);
            }
        }
        flushDocumentPart();
        filterEvents.add(new Event(EventType.END_DOCUMENT, new Ending(subDocId)));
        filterEventIterator = filterEvents.iterator();
    }

    private void addMarkupComponentToDocumentPart(MarkupComponent markupComponent) {
        if (!documentPartEvents.isEmpty()) {
            markup.addComponent(createGeneralMarkupComponent(documentPartEvents));
            documentPartEvents = new ArrayList<>();
        }
        markup.addComponent(markupComponent);
    }

    private void addBlockChunksToDocumentPart(List<Chunk> chunks) {
        for (Chunk chunk : chunks) {
            if (chunk instanceof Markup) {
                for (MarkupComponent markupComponent : ((Markup) chunk).getComponents()) {
                    addMarkupComponentToDocumentPart(markupComponent);
                }
                continue;
            }

            documentPartEvents.addAll(chunk.getEvents());
        }
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
