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

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

/**
 *  Proxies requests to other parts of the filter and provides them with the shared strings.
 */
class OpenXMLZipFileProxy extends OpenXMLZipFile {
    Map<String, String> sharedStrings;

    public OpenXMLZipFileProxy(ZipFile zipFile, XMLInputFactory inputFactory, XMLOutputFactory outputFactory,
                               XMLEventFactory eventFactory, String encoding, Map<String, String> sharedStrings) {
        super(zipFile, inputFactory, outputFactory, eventFactory, encoding);
        this.sharedStrings = sharedStrings;
    }


    public DocumentType createDocument(ConditionalParameters params) throws XMLStreamException, IOException {
        initializeContentTypes();
        mainDocumentTarget = getRelationshipTarget();
        DocumentType doc;

        switch (contentTypes.getContentType(mainDocumentTarget)) {
            case ContentTypes.Types.Excel.MAIN_DOCUMENT_TYPE:
                doc = new ExcelDocument(this, params, sharedStrings);
                break;
            default:
                doc = super.createDocument(params);
        }
        doc.initialize();

        return doc;
    }
}
