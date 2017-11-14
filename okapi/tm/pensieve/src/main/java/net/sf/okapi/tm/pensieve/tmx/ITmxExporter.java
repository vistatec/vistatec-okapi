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

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.tm.pensieve.seeker.ITmSeeker;

/**
 * The TMX interface to Pensieve.
 */
public interface ITmxExporter {


    /**
     * Exports all Pensieve contents matching source and target language to TMX
     * @param sourceLocale The source locale to export
     * @param targetLocale The target locale to export
     * @param tmSeeker The Seeker to use when reading from the TM
     * @param tmxWriter The TMXWriter to use when writing to the TMX file
     * @throws IOException if there was a problem with the TMX export
     */
    void exportTmx(LocaleId sourceLocale, LocaleId targetLocale, ITmSeeker tmSeeker, TMXWriter tmxWriter) throws IOException;

    /**
     * Exports all Pensieve contents matching source languages to TMX
     * @param sourceLocale The source locale to export
     * @param tmSeeker The Seeker to use when reading from the TM
     * @param tmxWriter The TMXWriter to use when writing to the TMX file
     * @throws IOException if there was a problem with the TMX export
     */
    void exportTmx(LocaleId sourceLocale, ITmSeeker tmSeeker, TMXWriter tmxWriter) throws IOException;
}
