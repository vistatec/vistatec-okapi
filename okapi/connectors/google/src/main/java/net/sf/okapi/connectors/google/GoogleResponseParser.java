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
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GoogleResponseParser {
    private JSONParser parser = new JSONParser();

    public List<String> parseResponse(Reader r) throws IOException, ParseException {
        return parseArrayResponse(r, "translations", "translatedText");
    }

    public List<String> parseLanguagesResponse(Reader r) throws IOException, ParseException {
        return parseArrayResponse(r, "languages", "language");
    }

    protected List<String> parseArrayResponse(Reader r, String arrayName, String arrayKey)
                                throws IOException, ParseException {
        JSONObject json = (JSONObject)parser.parse(r);
        JSONArray array = requireArray(require(json, "data"), arrayName);
        List<String> values = new ArrayList<>();
        for (Object o : array) {
            if (o instanceof JSONObject) {
                values.add(unescapeTranslation(requireString((JSONObject)o, arrayKey)));
            }
        }
        return values;
    }

    public GoogleMTErrorException parseError(int code, String s, String query) throws IOException, ParseException {
        try {
            JSONObject json = (JSONObject)parser.parse(s);
            json = require(json, "error");
            JSONObject inner = (JSONObject)requireArray(json, "errors").get(0);
            // There may still be more we could be scraping here
            return new GoogleMTErrorException((int)requireLong(json, "code"), requireString(inner, "message"),
                    requireString(inner, "domain"), requireString(inner, "reason"), query);
        }
        catch (Exception e) {
            // For certain types of 400 errors, Google will respond with an HTML page.
            return new GoogleMTErrorException(code, "Google returned non-JSON error: " + s, "", "", query);
        }
    }

    // Google seems to assume the content type is HTML, and returns &, <, >, ", using named entities,
    // and ' as &#39;.  Other characters are returned using JSON's normal unicode escape mechanism.
    private String unescapeTranslation(String text) {
        text = text.replace("&#39;", "'");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&quot;", "\"");
        return text.replace("&amp;", "&");
    }

    private JSONObject require(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null || !(o instanceof JSONObject)) {
            throw new IllegalArgumentException("JSON didn't contain expected object " + key);
        }
        return (JSONObject)o;
    }
    private String requireString(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null || !(o instanceof String)) {
            throw new IllegalArgumentException("JSON didn't contain expected object " + key);
        }
        return (String)o;
    }
    private long requireLong(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null || !(o instanceof Long)) {
            throw new IllegalArgumentException("JSON didn't contain expected object " + key);
        }
        return (long)o;
    }
    private JSONArray requireArray(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null || !(o instanceof JSONArray)) {
            throw new IllegalArgumentException("JSON didn't contain expected array " + key);
        }
        return (JSONArray)o;
    }
}
