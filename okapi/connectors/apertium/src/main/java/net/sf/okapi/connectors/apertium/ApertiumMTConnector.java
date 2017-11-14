/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.apertium;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.BaseConnector;
import net.sf.okapi.lib.translation.QueryUtil;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ApertiumMTConnector extends BaseConnector {

	private Parameters params;
	private JSONParser parser;
	private QueryUtil util;
	
	public ApertiumMTConnector () {
		params = new Parameters();
		util = new QueryUtil();
		parser = new JSONParser();
	}
	
	@Override
	public String getName () {
		return "Apertium MT";
	}

	@Override
	public String getSettingsDisplay () {
		return String.format("Server: %s\n%s", params.getServer(),
			(Util.isEmpty(params.getApiKey()) ? "Without API key" : "With API key"));
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
	 * Queries the Apertium API.
	 * See http://wiki.apertium.org/wiki/Apertium_web_service for details.
	 * @param fragment the fragment to query.
	 * @return the number of translations (1 or 0).
	 */
	@Override
	public int query (TextFragment fragment) {
		result = null;
		current = -1;
		try {
			// Check if there is actually text to translate
			if ( !fragment.hasText(false) ) return 0;
			// Convert the fragment to coded HTML
			String qtext = util.toCodedHTML(fragment);
			// Create the connection and query
			URL url;
			if ( Util.isEmpty(params.getApiKey()) ) {
				url = new URL(params.getServer() + String.format("?format=html&markUnknown=no&q=%s&langpair=%s|%s",
					URLEncoder.encode(qtext, "UTF-8"), srcCode, trgCode));
			}
			else {
				url = new URL(params.getServer() + String.format("?key=%s&format=html&markUnknown=no&q=%s&langpair=%s|%s",
					URLEncoder.encode(params.getApiKey(), "UTF-8"), URLEncoder.encode(qtext, "UTF-8"), srcCode, trgCode));
			}
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(params.getTimeout()*1000);

			// Get the response
			JSONObject object = (JSONObject)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)object;
	    	@SuppressWarnings("unchecked")
	    	Map<String, Object> data = (Map<String, Object>)map.get("responseData");
	    	String res = (String)data.get("translatedText");
	    	if ( res == null ) { // Probably an unsupported pair
		    	long code = (Long)map.get("responseStatus");
	    		res = (String)map.get("responseDetails");
	    		throw new OkapiException(String.format("Error code %d: %s.", code, res));
	    	}
	    	// Remove extra \n if needed
	    	if ( res.endsWith("\n") && !qtext.endsWith("\n")) {
	    		res = res.substring(0, res.length()-1);
	    	}

	    	result = new QueryResult();
	    	result.weight = getWeight();
			result.source = fragment;
			if ( fragment.hasCode() ) {
				result.target = new TextFragment(util.fromCodedHTML(res, fragment, true),
					fragment.getClonedCodes());
			}
			else {
				result.target = new TextFragment(util.fromCodedHTML(res, fragment, true));
			}

			result.setFuzzyScore(95); // Arbitrary score for MT
			result.origin = getName();
			result.matchType = MatchType.MT;
			current = 0;
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error querying the server." + e.getMessage(), e);
		}
		return ((current==0) ? 1 : 0);
	}
	
	@Override
	protected String toInternalCode (LocaleId standardCode) {
		String lang = standardCode.getLanguage();
		String reg = standardCode.getRegion();
		if ( reg != null ) {
			// Temporary fix for the Aranese case (until we get real LocaleID)
			if ( reg.equals("aran") ) lang += "_aran";
			// Temporary fix for the Brazilian Portuguese case (until we get real LocaleID)
			if ( reg.equals("br") ) lang += "_BR";
		}
		return lang;
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

}
