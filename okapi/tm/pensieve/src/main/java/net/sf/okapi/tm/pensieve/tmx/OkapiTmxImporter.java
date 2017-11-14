/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.tm.pensieve.tmx;

import java.io.IOException;
import java.net.URI;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.tm.pensieve.common.PensieveUtil;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;
import net.sf.okapi.tm.pensieve.writer.ITmWriter;

public class OkapiTmxImporter implements ITmxImporter {

    private IFilter tmxFilter;
    private LocaleId sourceLang;

    /**
     * Creates an instance of OkapiTMXHandler
     * @param sourceLang the language to import as the source language.
     * @param tmxFilter the IFilter to use to parse the TMX
     */
    public OkapiTmxImporter(LocaleId sourceLang, IFilter tmxFilter) {
        this.tmxFilter = tmxFilter;
        this.sourceLang = sourceLang;
        if (Util.isNullOrEmpty(sourceLang)) {
            throw new IllegalArgumentException("'sourceLang' must be set");
        }
        if (tmxFilter == null) {
            throw new IllegalArgumentException("'filter' must be set");
        }
    }

    /**
     * Imports TMX to Pensieve
     * @param tmxUri The location of the TMX
     * @param targetLang The target language to index
     * @param tmWriter The TMWriter to use when writing to the TM
     * @throws java.io.IOException if there was a problem with the TMX import
     */
    public void importTmx(URI tmxUri, LocaleId targetLang, ITmWriter tmWriter) throws IOException {
        checkImportTmxParams(tmxUri, targetLang, tmWriter);
        try {
            tmxFilter.open(new RawDocument(tmxUri, null, sourceLang, targetLang));
            while (tmxFilter.hasNext()) {
                Event event = tmxFilter.next();
                indexEvent(targetLang, tmWriter, event);
            }
        } finally {
            tmxFilter.close();
        }
    }

    private void checkImportTmxParams(URI tmxUri,
    	LocaleId targetLang,
    	ITmWriter tmWriter)
    {
        if (Util.isNullOrEmpty(targetLang)) {
            throw new IllegalArgumentException("'targetLang' was not set");
        }
        if (tmxUri == null) {
            throw new IllegalArgumentException("'tmxUri' was not set");
        }
        if (tmWriter == null) {
            throw new IllegalArgumentException("'tmWriter' was not set");
        }
    }

    private void indexEvent(LocaleId targetLang, ITmWriter tmWriter, Event event) throws IOException {
        TranslationUnit tu;
        if (event.getEventType() == EventType.TEXT_UNIT) {
            tu = PensieveUtil.convertToTranslationUnit(sourceLang, targetLang, event.getTextUnit());
            tmWriter.indexTranslationUnit(tu);
        }
    }
    

}
