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
import java.net.URI;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.tm.pensieve.writer.ITmWriter;

public interface ITmxImporter {

    /**
     * Imports TMX to Pensieve
     * @param tmxUri The location of the TMX
     * @param targetLocale The target locale to index
     * @param tmWriter The TMWriter to use when writing to the TM
     * @throws java.io.IOException if there was a problem with the TMX import
     */
    void importTmx(URI tmxUri, LocaleId targetLocale, ITmWriter tmWriter) throws IOException;
    
}
