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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.skeleton.ZipSkeleton;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.Collections.enumeration;
import static java.util.Collections.list;
import static java.util.Collections.sort;
import static net.sf.okapi.filters.idml.OrderingIdioms.getOrderedStoryPartNames;
import static net.sf.okapi.filters.idml.OrderingIdioms.getOrderedPasteboardItems;
import static net.sf.okapi.filters.idml.OrderingIdioms.getOrderedStoryIds;
import static net.sf.okapi.filters.idml.ParsingIdioms.MASTER_SPREAD;
import static net.sf.okapi.filters.idml.ParsingIdioms.SPREAD;

class Document {

    static final String MIME_TYPE = "application/vnd.adobe.indesign-idml-package";

    private final Parameters parameters;
    private final XMLInputFactory inputFactory;
    private final XMLEventFactory eventFactory;

    private ZipFile zipFile;
    private String startDocumentId;
    private String encoding;

    private List<String> nonTranslatableSubDocuments;
    private Enumeration<? extends ZipEntry> zipFileEntries;
    private int currentSubDocumentId;

    Document(Parameters parameters, XMLInputFactory inputFactory, XMLEventFactory eventFactory) {
        this.parameters = parameters;
        this.inputFactory = inputFactory;
        this.eventFactory = eventFactory;
    }

    Event open(String startDocumentId, URI uri, LocaleId sourceLocale, String encoding, IFilterWriter filterWriter) throws XMLStreamException, IOException {
        zipFile = new ZipFile(new File(uri.getPath()), ZipFile.OPEN_READ);

        if (!MIME_TYPE.equals(getMimeType())) {
            throw new OkapiBadFilterInputException("IDML filter tried to initialise a file that is not supported.");
        }

        this.encoding = encoding;
        this.startDocumentId = startDocumentId;

        DesignMap designMap = new DesignMapParser(getPartReader(PartNames.DESIGN_MAP), inputFactory).parse();
        Preferences preferences = new PreferencesParser(getPartReader(designMap.getPreferencesPartName()), inputFactory).parse();

        //TODO: parse styles

        List<PasteboardItem> pasteboardItems = new ArrayList<>();
        List<PasteboardItem> invisiblePasteboardItems = new ArrayList<>();

        List<Spread> masterSpreads = getSpreads(designMap.getMasterSpreadPartNames(), designMap.getActiveLayerId(), MASTER_SPREAD);
        List<PasteboardItem> masterSpreadPasteboardItems = getOrderedPasteboardItems(masterSpreads, preferences.getStoryPreference().getStoryDirection());

        if (parameters.getExtractMasterSpreads()) {
            pasteboardItems.addAll(masterSpreadPasteboardItems);
        } else {
            invisiblePasteboardItems.addAll(masterSpreadPasteboardItems);
        }

        List<Spread> spreads = getSpreads(designMap.getSpreadPartNames(), designMap.getActiveLayerId(), SPREAD);
        pasteboardItems.addAll(getOrderedPasteboardItems(spreads, preferences.getStoryPreference().getStoryDirection()));

        List<PasteboardItem> visiblePasteboardItems = getVisiblePasteboardItems(designMap, pasteboardItems);
        invisiblePasteboardItems.addAll(getInvisiblePasteboardItems(pasteboardItems, visiblePasteboardItems));

        List<String> storyIds = getOrderedStoryIds(visiblePasteboardItems);
        List<String> storyPartNames = getOrderedStoryPartNames(designMap.getStoryPartNames(), storyIds);

        List<String> invisibleStoryIds = getOrderedStoryIds(invisiblePasteboardItems);
        List<String> invisibleStoryPartNames = getOrderedStoryPartNames(designMap.getStoryPartNames(), invisibleStoryIds);

        nonTranslatableSubDocuments = PartNames.getPartNames(designMap, invisibleStoryPartNames);

        zipFileEntries = getZipFileEntries(designMap, storyPartNames);
        currentSubDocumentId = 0;

        return getStartDocumentEvent(uri, sourceLocale, filterWriter);
    }

    private String getMimeType() throws IOException {
        InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipFile.getEntry(PartNames.MIME_TYPE)));
        OutputStream outputStream = new ByteArrayOutputStream(MIME_TYPE.length());

        int result;

        while (-1 != (result = inputStream.read())) {
            outputStream.write((byte) result);
        }

        return outputStream.toString();
    }

    private List<Spread> getSpreads(List<String> spreadPartNames, String activeLayerId, QName spreadName) throws IOException, XMLStreamException {
        List<Spread> spreads = new ArrayList<>();

        for (String spreadPartName : spreadPartNames) {
            Spread spread = new SpreadParser(getPartReader(spreadPartName), inputFactory, activeLayerId).parse(spreadName);
            spreads.add(spread);
        }

        return spreads;
    }

    private Reader getPartReader(String partName) throws IOException {
        ZipEntry entry = zipFile.getEntry(partName);

        if (null == entry) {
            throw new OkapiBadFilterInputException("File is missing " + partName);
        }

        return new InputStreamReader(zipFile.getInputStream(entry), encoding);
    }

    private List<PasteboardItem> getVisiblePasteboardItems(DesignMap designMap, List<PasteboardItem> pasteboardItems) {
        return new PasteboardItem.VisibilityFilter(designMap.getLayers(), parameters.getExtractHiddenLayers()).filterVisible(pasteboardItems);
    }

    private List<PasteboardItem> getInvisiblePasteboardItems(List<PasteboardItem> pasteboardItems, List<PasteboardItem> visiblePasteboardItems) {
        List<PasteboardItem> invisiblePasteboardItems = new ArrayList<>(pasteboardItems);
        invisiblePasteboardItems.removeAll(visiblePasteboardItems);

        return invisiblePasteboardItems;
    }

    private Enumeration<? extends ZipEntry> getZipFileEntries(DesignMap designMap, List<String> storyPartNames) throws IOException, XMLStreamException {
        List<? extends ZipEntry> entryList = list(zipFile.entries());
        sort(entryList, new ZipEntryComparator(PartNames.getPartNames(designMap, storyPartNames)));

        return enumeration(entryList);
    }

    private Event getStartDocumentEvent(URI uri, LocaleId sourceLocale, IFilterWriter filterWriter) {

        StartDocument startDoc = new StartDocument(startDocumentId);
        startDoc.setName(uri.getPath());
        startDoc.setLocale(sourceLocale);
        startDoc.setMimeType(MIME_TYPE);
        startDoc.setFilterWriter(filterWriter);
        startDoc.setFilterParameters(parameters);
        startDoc.setLineBreak("\n");
        startDoc.setEncoding(encoding, false);  // IDML files don't have UTF8BOM
        ZipSkeleton skel = new ZipSkeleton(zipFile, null);

        return new Event(EventType.START_DOCUMENT, startDoc, skel);
    }

    boolean hasNextSubDocument() {
        return zipFileEntries.hasMoreElements();
    }

    SubDocument nextSubDocument() {
        ZipEntry zipEntry = zipFileEntries.nextElement();

        if (!isTranslatableSubDocument(zipEntry.getName())) {
            return new NonTranslatableSubDocument(zipFile, zipEntry, startDocumentId, String.valueOf(++currentSubDocumentId));
        }

        return new StorySubDocument(zipFile, zipEntry, startDocumentId, String.valueOf(++currentSubDocumentId), parameters, inputFactory, eventFactory);
    }

    boolean isTranslatableSubDocument(String partName) {
        if (nonTranslatableSubDocuments.contains(partName)) {
            return false;
        }

        return true;
    }

    void close() throws IOException {
        zipFile.close();
    }

    private static class PartNames {

        private static final String MIME_TYPE = "mimetype";
        private static final String DESIGN_MAP = "designmap.xml";
        private static final String CONTAINER = "META-INF/container.xml";
        private static final String METADATA = "META-INF/metadata.xml";

        static List<String> getPartNames(DesignMap designMap, List<String> storyPartNames) {
            List<String> partNames = new ArrayList<>(Arrays.asList(MIME_TYPE, DESIGN_MAP, CONTAINER, METADATA));

            partNames.add(designMap.getGraphicPartName());
            partNames.add(designMap.getFontsPartName());
            partNames.add(designMap.getStylesPartName());
            partNames.add(designMap.getPreferencesPartName());
            partNames.add(designMap.getTagsPartName());
            partNames.addAll(designMap.getMasterSpreadPartNames());
            partNames.addAll(designMap.getSpreadPartNames());
            partNames.add(designMap.getBackingStoryPartName());
            partNames.addAll(storyPartNames);

            return partNames;
        }
    }

    private class ZipEntryComparator implements Comparator<ZipEntry> {

        private List<String> partNames;

        ZipEntryComparator(List<String> partNames) {
            this.partNames = partNames;
        }

        @Override
        public int compare(ZipEntry o1, ZipEntry o2) {
            int index1 = partNames.indexOf(o1.getName());
            int index2 = partNames.indexOf(o2.getName());

            if (index1 == -1) {
                index1 = Integer.MAX_VALUE;
            }

            if (index2 == -1) {
                index2 = Integer.MAX_VALUE;
            }

            return Integer.compare(index1, index2);
        }
    }
}
