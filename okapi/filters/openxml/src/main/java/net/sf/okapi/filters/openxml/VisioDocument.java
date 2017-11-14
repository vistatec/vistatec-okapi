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

import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;

import static java.util.Collections.enumeration;
import static java.util.Collections.list;
import static java.util.Collections.sort;
import static net.sf.okapi.filters.openxml.Relationships.mapRelsToTargets;

class VisioDocument extends DocumentType {

    private static final String MASTERS = Namespaces.VisioDocumentRelationships.getDerivedURI("/masters");
    private static final String PAGES = Namespaces.VisioDocumentRelationships.getDerivedURI("/pages");

    private static final String MASTER = Namespaces.VisioDocumentRelationships.getDerivedURI("/master");
    private static final String PAGE = Namespaces.VisioDocumentRelationships.getDerivedURI("/page");

    private List<String> mastersAndPages;

    VisioDocument(OpenXMLZipFile zipFile, ConditionalParameters params) {
        super(zipFile, params);
    }

    @Override
    boolean isClarifiablePart(String contentType) {
        return false;
    }

    @Override
    boolean isStyledTextPart(String entryName, String type) {
        return false;
    }

    @Override
    void initialize() throws IOException, XMLStreamException {
        mastersAndPages = getMastersAndPages();
    }

    private List<String> getMastersAndPages() throws IOException, XMLStreamException {
        String mainDocumentPart = getZipFile().getMainDocumentTarget();
        Relationships relationships = getZipFile().getRelationshipsForTarget(mainDocumentPart);

        List<Relationships.Rel> masters = relationships.getRelByType(MASTERS);

        if (masters == null || masters.size() != 1) {
            throw new OkapiBadFilterInputException(UNEXPECTED_NUMBER_OF_RELATIONSHIPS);
        }

        List<Relationships.Rel> pages = relationships.getRelByType(PAGES);

        if (pages == null || pages.size() != 1) {
            throw new OkapiBadFilterInputException(UNEXPECTED_NUMBER_OF_RELATIONSHIPS);
        }

        Relationships masterRelationships = getZipFile().getRelationshipsForTarget(masters.get(0).target);
        Relationships pageRelationships = getZipFile().getRelationshipsForTarget(pages.get(0).target);

        List<String> targets = mapRelsToTargets(masterRelationships.getRelByType(MASTER));
        targets.addAll(mapRelsToTargets(pageRelationships.getRelByType(PAGE)));

        return targets;
    }

    @Override
    OpenXMLPartHandler getHandlerForFile(ZipEntry entry, String contentType) {

        if (!isTranslatableType(entry.getName(), contentType)) {
            return new NonTranslatablePartHandler(getZipFile(), entry);
        }

        return new MasterAndPagePartHandler(getParams(), getZipFile(), entry);
    }

    private boolean isTranslatableType(String entryName, String contentType) {
        return entryName.endsWith(".xml") && (isMasterPart(contentType) || isPagePart(contentType));
    }

    @Override
    Enumeration<? extends ZipEntry> getZipFileEntries() throws IOException, XMLStreamException {
        List<? extends ZipEntry> entryList = list(getZipFile().entries());
        sort(entryList, new ZipEntryComparator(mastersAndPages));

        return enumeration(entryList);
    }
}
