/*===========================================================================
Copyright (C) 2008-2009 by the Okapi Framework contributors
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
import java.util.Iterator;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.tm.pensieve.common.PensieveUtil;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;
import net.sf.okapi.tm.pensieve.seeker.ITmSeeker;
import net.sf.okapi.tm.pensieve.seeker.PensieveSeeker;

/**
 * Used to interact with the Okapi Standards for TMX. For example, the property names and default fields stored.
 */
public class OkapiTmxExporter implements ITmxExporter {

    /**
     * Exports all target locales in Pensieve to TMX
     * @param sourceLocale The source locale
     * @param tmSeeker The TMSeeker to use when reading from the TM
     * @param tmxWriter The TMXWriter to use when writing out the TMX
     */
    public void exportTmx(LocaleId sourceLocale, ITmSeeker tmSeeker, TMXWriter tmxWriter) throws IOException {
        exportTmx(sourceLocale, null, tmSeeker, tmxWriter);
    }

    /**
     * Exports only a specific target locale Pensieve to TMX
     * @param sourceLang The source language of desired translation
     * @param targetLang The target language of desired translation (or null for all target languages)
     * @param tmSeeker The TMSeeker to use when reading from the TM
     * @param tmxWriter The TMXWriter to use when writing out the TMX
     */
    public void exportTmx(LocaleId sourceLang,
    	LocaleId targetLang,
    	ITmSeeker tmSeeker,
    	TMXWriter tmxWriter)
    throws IOException {
        checkExportTmxParams(sourceLang, tmSeeker, tmxWriter);
        try {
            tmxWriter.writeStartDocument(sourceLang, targetLang, "pensieve", "0.0.1", "sentence", "pensieve", "unknown");
            //TODO might eat up too much memory for large TMs
            Iterator<TranslationUnit> iterator = ((PensieveSeeker) tmSeeker).iterator();
            while (iterator.hasNext()) {
                TranslationUnit tu = iterator.next();
                if (isWriteTextUnit(sourceLang, targetLang, tu)) {
                    tmxWriter.writeTUFull(PensieveUtil.convertToTextUnit(tu));
                }
            }
            tmxWriter.writeEndDocument();
        } finally {
            tmxWriter.close();
        }
    }

    private static boolean isWriteTextUnit(LocaleId sourceLang,
    	LocaleId targetLang,
    	TranslationUnit tu)
    {
        return sourceLang.equals(tu.getSource().getLanguage()) && (targetLang == null || targetLang.equals(tu.getTarget().getLanguage()));
    }

    private void checkExportTmxParams(LocaleId sourceLang,
    	ITmSeeker tmSeeker,
    	TMXWriter tmxWriter)
    {
        if (sourceLang == null) {
            throw new IllegalArgumentException("'sourceLang' was not set");
        }

        if (tmSeeker == null) {
            throw new IllegalArgumentException("'tmSeeker' was not set");
        }

        if (tmxWriter == null) {
            throw new IllegalArgumentException("'tmxWriter' was not set");
        }
    }
}
