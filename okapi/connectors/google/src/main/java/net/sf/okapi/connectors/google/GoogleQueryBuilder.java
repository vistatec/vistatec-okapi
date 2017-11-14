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

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Util;

public class GoogleQueryBuilder<T> {
    // "The URL for GET requests, including parameters, must be less than 2K characters."
    // https://cloud.google.com/translate/docs/translating-text#translating_text_1
    private static final int QUERY_LIMIT = 2048;
    private static final String QPARAM = "&q=";

    private StringBuilder sb;
    private String baseUrl;
    private GoogleMTv2Parameters params;
    private final String srcCode, tgtCode;
    private List<String> sourceTexts = new ArrayList<>();
    private List<T> sources = new ArrayList<>();

    public GoogleQueryBuilder(String baseUrl, GoogleMTv2Parameters params, String srcCode, String tgtCode) {
        this.baseUrl = baseUrl;
        this.params = params;
        this.srcCode = srcCode;
        this.tgtCode = tgtCode;
        reset();
    }

    public void reset() {
        sb = new StringBuilder(baseUrl)
            .append("?key=").append(params.getApiKey())
            .append("&source=").append(srcCode)
            .append("&target=").append(tgtCode);
        // "model=nmt" is assumed as the default behavior. If PBMT is explicitly
        // desired, we override it here.
        if (params.getUsePBMT()) {
            sb.append("&model=base");
        }
        sourceTexts.clear();
        sources.clear();
    }

    public boolean hasCapacity(String sourceText) {
        int additionalLen = QPARAM.length() + Util.URLEncodeUTF8(sourceText).length();
        return (sb.length() + additionalLen < QUERY_LIMIT);
    }

    public void addQuery(String sourceText, T source) {
        if (!hasCapacity(sourceText)) {
            throw new IllegalStateException("Query too long to add '" + sourceText + "'");
        }
        sb.append(QPARAM).append(Util.URLEncodeUTF8(sourceText));
        sourceTexts.add(sourceText);
        sources.add(source);
    }

    public List<String> getSourceTexts() {
        return sourceTexts;
    }
    public List<T> getSources() {
        return sources;
    }
    public int getSourceCount() {
        return sourceTexts.size();
    }

    public String getQuery() {
        return sb.toString();
    }
}
