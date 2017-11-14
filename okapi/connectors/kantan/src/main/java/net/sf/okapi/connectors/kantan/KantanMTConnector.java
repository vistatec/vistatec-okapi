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

package net.sf.okapi.connectors.kantan;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.BaseConnector;
import net.sf.okapi.lib.translation.QueryUtil;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Connector for the <a href="https://www.kantanmt.com/">KantanMT</a> API.  These
 * calls depend on an authorization token and profile ID provided by the user.  The
 * profile ID specifies a particular MT engine within the KantanMT service.  The
 * engine must be started separately (through the KantanMT web interface, or via API)
 * and is assumed to be running by this connector.  As the engine initialiation process
 * takes several minutes, the connector does not perform initialization on its own in
 * the {@link #open} method.
 *
 * @see <a href="http://docs.kantanmt.apiary.io">http://docs.kantanmt.apiary.io</a>
 */
public class KantanMTConnector extends BaseConnector {
    private Logger logger = LoggerFactory.getLogger(KantanMTConnector.class);

    private static final String API_SERVER_URL = "https://www.kantanmt.com/api/xlate";
    private static final String CONNECTOR_ERROR = "KantanMT Connector Error: %s";

    private static final int CONNECTION_TIMEOUT_MS = 10000;
    private static final int SOCKET_TIMEOUT_MS = 0;

    private static final int DEFAULT_FUZZY_SCORE = 95;

    private static final int MAX_SEGMENTS = 900;

    private KantanMTConnectorParameters parameters;
    private JSONParser parser;
    private HttpClient httpClient;
    private List<QueryResult> results = new ArrayList<>();

    public KantanMTConnector() {
        parameters = new KantanMTConnectorParameters();
        parser = new JSONParser();
        httpClient = new DefaultHttpClient();
    }

    protected String getServerURL() {
        return API_SERVER_URL;
    }

    protected int getConnectionTimeout() {
        return CONNECTION_TIMEOUT_MS;
    }

    protected int getSocketTimeout() {
        return SOCKET_TIMEOUT_MS;
    }

    @Override
    public String getName() {
        return "KantanMT";
    }

    @Override
    public String getSettingsDisplay() {
        String profile = (parameters != null) ? parameters.getProfileName() : "";
        if (!Util.isEmpty(profile)) {
            return "Using profile \"" + profile + "\".";
        }
        else {
            return "No profile selected.";
        }
    }

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public IParameters getParameters() {
        return parameters;
    }

    @Override
    public int query(String plainText) {
        return query(new TextFragment(plainText));
    }

    @Override
    public int query(TextFragment tf) {
        results = batchQuery(Collections.singletonList(tf)).get(0);
        if ( results.size() > 0 ) current = 0;
        return results.size();
    }

    @Override
    public boolean hasNext () {
        if ( results == null ) return false;
        if ( current >= results.size() ) {
            current = -1;
        }
        return (current > -1);
    }

    @Override
    public QueryResult next () {
        if ( results == null ) return null;
        if (( current > -1 ) && ( current < results.size() )) {
            current++;
            return results.get(current-1);
        }
        current = -1;
        return null;
    }

    /**
     * Wrapper to hold the original fragment along with its extracted codes, so
     * that we can reinsert them into the translated target.
     */
    private static class RequestInfo {
        private TextFragment originalFragment;
        private String preparedText;
        private QueryUtil queryUtil = new QueryUtil();
        RequestInfo(TextFragment fragment) {
            this.originalFragment = fragment;
            this.preparedText = queryUtil.separateCodesFromText(originalFragment);
        }
        TextFragment generateResult(String translated) {
            return queryUtil.createNewFragmentWithCodes(translated);
        }
    }

    @Override
    public List<List<QueryResult>> batchQuery(List<TextFragment> fragments) {
        List<List<QueryResult>> results = new ArrayList<List<QueryResult>>();

        try {
            if (Util.isEmpty(parameters.getApiToken()) || Util.isEmpty(parameters.getProfileName())) {
                throw new OkapiException("You must specify Profile Name and Authorization Token to use this connector");
            }

            HttpPost post = new HttpPost(getServerURL());
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), getConnectionTimeout());
            HttpConnectionParams.setSoTimeout(httpClient.getParams(), getSocketTimeout());
            post.addHeader("Content-Type", "application/x-www-form-urlencoded");

            List<RequestInfo> requests = new ArrayList<>();
            for (TextFragment fragment : fragments) {
                requests.add(new RequestInfo(fragment));
            }
            List<UrlEncodedFormEntity> forms = fragmentsToPostForms(requests);
            for (UrlEncodedFormEntity form : forms) {
                post.setEntity(form);
                HttpResponse response = httpClient.execute(post);

                final StatusLine status = response.getStatusLine();
                if (status == null) {
                    logger.error("Unable to get response status code from Kantan API");
                    throw new OkapiException(String.format(CONNECTOR_ERROR, "unable to get response status code"));
                }

                int code = status.getStatusCode();
                String content = getResponseContent(response);

                if (content == null || content.length() == 0) {
                    logger.error(String.format("Unable to get response content from Kantan API. Response code %d", code));
                    throw new OkapiException(String.format(CONNECTOR_ERROR, "missing response content, status code: " + code));
                }

                if (code != 200) {
                    logger.error(String.format("Error in communication with a remote server, status code => %d, response body => %s",
                            code, content));
                    String e = code == 401
                            ? "Translation request is not authorized. Please, verify your Kantan profile name and authorization token"
                            : "Remote server responded with " + code + " status code";
                    throw new OkapiException(e);
                }
                processResponse(requests, results, content);
            }
            httpClient.getConnectionManager().shutdown();
        } catch (ParseException e) {
            logger.error("Cannot parse json response from KantanMT", e);
            throw new OkapiException("Cannot parse json response from Kantan MT. " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Error in communication with Kantan MT server", e);
            throw new OkapiException("Error in communication with Kantan MT server: " + e.getMessage(), e);
        }

        return results;
    }

    private void processResponse(List<RequestInfo> requests, List<List<QueryResult>> results, String content) throws ParseException {
        JSONObject object = (JSONObject) parser.parse(content);
        Map<String, Object> map = (Map<String, Object>) object.get("response");
        String type = (String) map.get("type");
        JSONObject bodyObj = (JSONObject) map.get("body");

        if (type.equals("translation")) {
            JSONArray translations = (JSONArray) bodyObj.get("translationData");

            List<QueryResult> qrList;
            for (Object translation : translations) {
                JSONObject transObj = (JSONObject) translation;
                long id = (Long) transObj.get("id");

                if (id >= requests.size() || id < 0) {
                    String m = "source fragment for translation with id '" + id + "' was not found";
                    logger.error(m);
                    throw new OkapiException(m);
                }
                RequestInfo request = requests.get((int) id);
                qrList = new ArrayList<QueryResult>();

                result = new QueryResult();
                result.weight = getWeight();
                result.origin = getName();
                result.source = request.originalFragment;
                result.matchType = MatchType.MT;
                result.setFuzzyScore(DEFAULT_FUZZY_SCORE);

                if (!request.originalFragment.hasText(false)) {
                    result.target = request.originalFragment.clone();
                } else {
                    String translatedText = (String) transObj.get("trg");
                    translatedText = translatedText.replaceAll("&amp;", "&");
                    result.target = request.generateResult(translatedText);
                }
                qrList.add(result);
                results.add(qrList);
            }
        } else if (type.equals("status")) {
            String state = (String) bodyObj.get("state");
            String m = String.format(CONNECTOR_ERROR, "server is in '" + state + "' state");
            throw new OkapiException(m);
        } else {
            String m = String.format("Server returned JSON response with unprocessible type value '%s'", type);
            logger.error(m);
            throw new OkapiException(m);
        }
    }

    private List<UrlEncodedFormEntity> fragmentsToPostForms(List<RequestInfo> requests) {
        List<UrlEncodedFormEntity> forms = new ArrayList<UrlEncodedFormEntity>();

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("auth", parameters.getApiToken()));
        pairs.add(new BasicNameValuePair("profile", parameters.getProfileName()));

        for (int i = 0; i < requests.size(); i++) {
            final RequestInfo request = requests.get(i);
            if (!request.originalFragment.hasText(false)) {
                continue;
            }
            final String text = request.preparedText.replaceAll("&", "%26");

            if (pairs.size() >= MAX_SEGMENTS) {
                forms.add(new UrlEncodedFormEntity(pairs, Consts.UTF_8));
                pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("auth", parameters.getApiToken()));
                pairs.add(new BasicNameValuePair("profile", parameters.getProfileName()));
            }
            pairs.add(new BasicNameValuePair(Integer.toString(i), text));
        }
        forms.add(new UrlEncodedFormEntity(pairs, Consts.UTF_8));

        return forms;
    }

    private String getResponseContent(HttpResponse response) throws IOException {
        InputStream stream = response.getEntity().getContent();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
        String responseBody = br.readLine();
        stream.close();

        return responseBody;
    }
}
