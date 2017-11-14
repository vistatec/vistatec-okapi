/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.lingo24;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.BaseConnector;
import net.sf.okapi.lib.translation.QueryUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Connector for the <a href="https://developer.lingo24.com/premium-machine-translation-api">Lingo24 Premium MT API</a>.
 */
public class Lingo24Connector extends BaseConnector {

    private static final String BASE_URL = "https://api.lingo24.com/mt/v1/translate";
    private static final String BASE_QUERY = "?user_key=%s&source=%s&target=%s";
    private static final String QPARAM = "&q=";

    private Parameters params;
    private JSONParser parser;
    private QueryUtil util;

    public Lingo24Connector () {
        params = new Parameters();
        util = new QueryUtil();
        parser = new JSONParser();
    }

    @Override
    public String getName () {
        return "Lingo24 Premium MT";
    }

    @Override
    public String getSettingsDisplay () {
        return "Lingo24 URL: " + BASE_URL;
    }

    @Override
    public void close () {
        // Nothing to do
    }

    @Override
    public void open () {
        // Nothing to do
    }

    @Override
    public int query (String plainText) {
        return query(new TextFragment(plainText));
    }

    /**
     * Queries the Lingo24 Premium MT API.
     * See &lt;a href="https://developer.lingo24.com/premium-machine-translation-api">Lingo24 Developer Portal&gt;/a&lt; for details.
     * @param fragment the fragment to query.
     * @return the number of translations (1 or 0).
     */
    @Override
    public int query (TextFragment fragment) {
        current = -1;
        try {
            if (!fragment.hasText(false)) {
                return 0;
            }
            if (Util.isEmpty(params.getUserKey())) {
                throw new OkapiException("You must have a Lingo24 API user_key to use this connector.");
            }
            String qtext = util.toCodedHTML(fragment);
            String urlString = BASE_URL + String.format(BASE_QUERY, params.getUserKey(), srcCode, trgCode);
            URL url = new URL(urlString + QPARAM + URLEncoder.encode(qtext, "UTF-8"));

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            int code = conn.getResponseCode();
            if (code != 200) {
                throw new OkapiException(String.format("Error: response code %d\n" + conn.getResponseMessage(), code));
            }

            JSONObject object = (JSONObject)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>)object;

            if (!map.containsKey("errors")) {
                String translation = (String)map.get("translation");

                result = new QueryResult();
                result.weight = getWeight();
                result.source = fragment;
                if (fragment.hasCode()) {
                    result.target = new TextFragment(util.fromCodedHTML(translation, fragment, true),
                            fragment.getClonedCodes());
                } else {
                    result.target = new TextFragment(util.fromCodedHTML(translation, fragment, true));
                }
                result.setFuzzyScore(95);
                result.origin = getName();
                result.matchType = MatchType.MT;
                current = 0;
            } else {
                JSONArray errorsArray = (JSONArray) map.get("errors");
                StringBuilder errorsString = new StringBuilder("");
                for (Object entry : errorsArray) {
                    errorsString.append(entry).append("; ");
                }
                throw new OkapiException(String.format("Response from server with errors: %s", errorsString));
            }
        }
        catch (Throwable e) {
            throw new OkapiException("Error querying the server.\n" + e.getMessage(), e);
        }
        return ((current==0) ? 1 : 0);
    }

    @Override
    public IParameters getParameters () {
        return params;
    }

    @Override
    public void setParameters (IParameters params) {
        this.params = (Parameters) params;
    }


}
