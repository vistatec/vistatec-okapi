/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.google;

import java.io.IOException;
import java.util.List;

import org.json.simple.parser.ParseException;

public interface GoogleMTAPI {
    /**
     * Make a single call to Google to translate the provided list of source texts.  The source texts must
     * obey Google protocol limits of <= 2048 total characters per request.
     * @throws IOException
     * @throws ParseException
     */
    <T> List<TranslationResponse> translate(GoogleQueryBuilder<T> qb)
                throws IOException, ParseException;

    /**
     * Perform translation of oversized (> 2048 characters) segments.
     * @return the translation response
     * @throws IOException
     * @throws ParseException
     */
    <T> TranslationResponse translateSingleSegment(GoogleQueryBuilder<T> qb, String sourceText)
                throws IOException, ParseException;

    List<String> getLanguages() throws IOException, ParseException;
}
