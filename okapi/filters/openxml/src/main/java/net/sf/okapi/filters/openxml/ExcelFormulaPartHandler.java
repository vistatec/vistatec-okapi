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
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.skeleton.GenericSkeleton;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import static net.sf.okapi.filters.openxml.XMLEventHelpers.isExcelFormula;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isTableColumnEvent;

/**
 * Here is a basic logic that was brought from OpenXMLContentFilter(general mechanism left in OpenXMLContentFilter the
 *  removing could be dangerously) which is handling string data in tableN.xml and sheetN.xml files
 * it works with column names and references to the columns in formulas.
 */
class ExcelFormulaPartHandler extends GenericPartHandler {
    private ISkeleton skeleton;
    private String documentId;
    private String subDocumentId;
    private XMLEventReader xmlReader;
    private Map<String, String> sharedStrings;
    private XMLEventFactory eventFactory;

    private static final String NAME = "name";
    private static Pattern FORMULA_PATTERN = Pattern.compile("(.*?[\\[\"]{1})([a-zA-Z0-9 \\*]+?)([\\]\"]{1}.*)");
    private Iterator<Event> filterEventIterator;

    private enum FORMULA_MATCHER_GROUPS {
        NON_TRANSLATABLE(1),
        TRANSLATABLE(2),
        UNPROCESSED(3);

        int value;

        FORMULA_MATCHER_GROUPS(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }
    }

    ExcelFormulaPartHandler(ConditionalParameters params,
                            OpenXMLZipFile zipFile, ZipEntry entry, Map<String, String> sharedStrings) {
        this(params, sharedStrings, entry.getName());
        this.zipFile = zipFile;
        this.entry = entry;
    }

    ExcelFormulaPartHandler(ConditionalParameters params, ISkeleton skeleton, Map<String, String> sharedStrings,
                            ZipEntry entry) {
        this(params, sharedStrings, entry.getName());
        this.skeleton = skeleton;
    }

    private ExcelFormulaPartHandler(ConditionalParameters params, Map<String, String> sharedStrings, String partName) {
        super(params, partName);
        this.sharedStrings = sharedStrings;
        eventFactory = XMLEventFactory.newInstance();
    }

    private void process() throws XMLStreamException, IOException {
        DocumentPart documentPart = new DocumentPart(documentPartIdGenerator.createId(), false);
        documentPart.setSkeleton(new GenericSkeleton(getModifiedContent()));

        filterEvents.add(new Event(EventType.DOCUMENT_PART, documentPart));
        filterEvents.add(new Event(EventType.END_DOCUMENT, new Ending(subDocumentId)));
        filterEventIterator = filterEvents.iterator();
    }


    public String getModifiedContent() throws XMLStreamException, IOException {
        StringWriter sw = new StringWriter();
        XMLEventWriter xmlEventWriter = XMLOutputFactory.newInstance().createXMLEventWriter(sw);
        xmlReader = createXMLReader();

        while (xmlReader.hasNext()) {
            XMLEvent e = xmlReader.nextEvent();
            if (isTableColumnEvent(e)) {
                e = rewriteNameAttribute(e);
            } else if (isExcelFormula(e)) {
                xmlEventWriter.add(e);
                e = xmlReader.nextEvent();
                if (e.isCharacters()) {
                    e = eventFactory.createCharacters(updateTextualReferencesInParsedCharacterData(e.asCharacters()));
                }
            }
            xmlEventWriter.add(e);
        }
        return sw.toString();
    }

    private XMLEventReader createXMLReader() throws XMLStreamException, IOException {
        if (zipFile == null && entry == null && skeleton != null) {
            return XMLInputFactory.newInstance().createXMLEventReader(
                    new InputStreamReader(new BufferedInputStream(new ByteArrayInputStream(skeleton.toString().getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8));
        } else if (skeleton == null && zipFile != null && entry != null) {
            return zipFile.getInputFactory().createXMLEventReader(
                    new InputStreamReader(new BufferedInputStream(zipFile.getInputStream(entry)), StandardCharsets.UTF_8));
        } else {
            throw new OkapiException("XMLReader should be initialized with zip file configuration or with string content");
        }
    }

    private XMLEvent rewriteNameAttribute(XMLEvent e) {
        String value = e.asStartElement().getAttributeByName(new QName(NAME)).getValue();
        String translatedSharedString = sharedStrings.get(value);

        if (translatedSharedString == null) {
            return e;
        } else {
            Attribute attr = eventFactory.createAttribute(NAME, translatedSharedString);
            Iterator attributeIterator = e.asStartElement().getAttributes();
            ArrayList<Attribute> updatedAttributes = new ArrayList<>();
            while (attributeIterator.hasNext()) {
                Attribute attribute = (Attribute) attributeIterator.next();
                if (NAME.equalsIgnoreCase(attribute.getName().toString())) {
                    updatedAttributes.add(attr);
                } else {
                    updatedAttributes.add(attribute);
                }
            }
            return eventFactory.createStartElement(e.asStartElement().getName(), updatedAttributes.iterator(), e.asStartElement().getNamespaces());
        }
    }

    private String updateTextualReferencesInParsedCharacterData(Characters pcdata) {
        StringBuilder result = new StringBuilder();
        String text = pcdata.getData();
        Matcher formulaMatcher = FORMULA_PATTERN.matcher(text);
        String formulaPart = null;

        if (!formulaMatcher.find()) {
            return text;
        }

        do {
            if (formulaMatcher.groupCount() != FORMULA_MATCHER_GROUPS.values().length) {
                break;
            }
            result.append(formulaMatcher.group(FORMULA_MATCHER_GROUPS.NON_TRANSLATABLE.getValue()));
            String textUnitPart = formulaMatcher.group(FORMULA_MATCHER_GROUPS.TRANSLATABLE.getValue());

            if (sharedStrings.get(textUnitPart) == null) {
                result.append(textUnitPart);
            } else {
                result.append(sharedStrings.get(textUnitPart));
            }

            formulaPart = formulaMatcher.group(FORMULA_MATCHER_GROUPS.UNPROCESSED.getValue());
            formulaMatcher.reset(formulaPart);
        } while (formulaMatcher.find());
        
        if (formulaPart != null) {
            result.append(formulaPart);
        }
        return result.toString();
    }

    @Override
    public Event open(String documentId, String subDocumentId, LocaleId srcLang) throws IOException, XMLStreamException {
        this.documentId = documentId;
        this.subDocumentId = subDocumentId;
        process();

        return createStartSubDocumentEvent(documentId, subDocumentId);
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
