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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.exceptions.OkapiException;

public class GoogleMTAPIImpl implements GoogleMTAPI {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleMTAPIImpl.class);

    private final String baseUrl;
    private GoogleMTv2Parameters params;
    private GoogleResponseParser parser = new GoogleResponseParser();

    public GoogleMTAPIImpl(String baseUrl, GoogleMTv2Parameters params) {
        this.baseUrl = baseUrl;
        this.params = params;
    }

    @Override
    public List<String> getLanguages() throws IOException, ParseException {
        URL url = new URL(baseUrl + "/languages?key=" + params.getApiKey());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        int code = conn.getResponseCode();
        if ( code == 200 ) {
            return parser.parseLanguagesResponse(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        }
        else {
            String errorBody = StreamUtil.streamUtf8AsString(conn.getErrorStream());
            throw parser.parseError(code, errorBody, null);
        }
    }

    @Override
    public <T> List<TranslationResponse> translate(GoogleQueryBuilder<T> qb) throws IOException, ParseException  {
        URL url = new URL(qb.getQuery());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        int code = conn.getResponseCode();
        if ( code == 200 ) {
            List<String> translatedTexts = 
                    parser.parseResponse(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            List<TranslationResponse> responses = new ArrayList<>();
            if (qb.getSourceCount() != translatedTexts.size()) {
                LOG.error("Received {} translations for {} sources in query {}", translatedTexts.size(),
                          qb.getSourceCount(), qb.getQuery());
                throw new OkapiException("API returned incorrect number of translations (expected " +
                          qb.getSourceCount() + ", got " + translatedTexts.size());
            }
            for (int i = 0; i < qb.getSourceCount(); i++) {
                responses.add(new TranslationResponse(qb.getSourceTexts().get(i), translatedTexts.get(i)));
            }
            return responses;
        }
        else {
            String errorBody = StreamUtil.streamUtf8AsString(conn.getErrorStream());
            throw parser.parseError(code, errorBody, qb.getQuery());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TranslationResponse translateSingleSegment(GoogleQueryBuilder<T> qb, String sourceText)
                                            throws IOException, ParseException {
        LOG.debug("Using POST query for source '{}...' of length {}", sourceText.substring(0, 32), sourceText.length());
        URL url = new URL(qb.getQuery());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject json = new JSONObject();
        json.put("q", sourceText);
        try (OutputStreamWriter w = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
            w.write(json.toJSONString());
        }
        GoogleResponseParser parser = new GoogleResponseParser();
        int code = conn.getResponseCode();
        if ( code == 200 ) {
            List<String> translatedTexts =
                    parser.parseResponse(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            if (translatedTexts.size() != 1) {
                LOG.error("Received {} translations for {} sources in POST query {} with body '{}'", translatedTexts.size(),
                          1, qb.getQuery(), sourceText);
                throw new OkapiException("API returned incorrect number of translations (expected 1, got " +
                              translatedTexts.size());
            }
            return new TranslationResponse(sourceText, translatedTexts.get(0));
        }
        else {
            String errorBody = StreamUtil.streamUtf8AsString(conn.getErrorStream());
            throw parser.parseError(code, errorBody, qb.toString());
        }
    }


}
