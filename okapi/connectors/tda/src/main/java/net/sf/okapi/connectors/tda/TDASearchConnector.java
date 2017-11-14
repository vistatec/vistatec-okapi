/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.tda;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.BaseConnector;
import net.sf.okapi.lib.translation.ITMQuery;
import net.sf.okapi.lib.translation.QueryUtil;
import net.sf.okapi.lib.translation.TextMatcher;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TDASearchConnector extends BaseConnector implements ITMQuery {

	// Language code to TDA code, except for reg=lang cases (fr-fr)
	private static final String[][] LANGSMAP = {
		{"ar", "ar-ar"}, // Yes, ar-ar (Argentina) is Arabic default in TDA, TODO: or use ar-sa?
		{"cs", "cs-cz"},
		{"cy", "cy-gb"},
		{"da", "da-dk"},
		{"el", "el-gr"},
		{"en", "en-us"},
		{"et", "et-ee"},
		{"fa", "fa-ir"},
		{"he", "he-il"},
		{"ko", "ko-kr"},
		{"nb", "nb-no"},
		{"nn", "nn-no"},
		{"sl", "sl-si"},
		{"sv", "sv-se"},
		{"uk", "uk-ua"},
		{"vi", "vi-vn"},
		{"zh", "zh-cn"}
	};
	
	private JSONParser parser;
	private Parameters params;
	private String baseURL;
	private String authKey;
	private int current = -1;
	private int maxHits = 20;
	private List<QueryResult> results;
	private TextMatcher matcher;
	private ScoreComparer scorComp = new ScoreComparer();
	private int threshold = 60;
	private QueryUtil qutil;

	class ScoreComparer implements Comparator<QueryResult> {
		public int compare(QueryResult arg0, QueryResult arg1) {
			return (arg0.getFuzzyScore()>arg1.getFuzzyScore() ? -1 : (arg0.getFuzzyScore()==arg1.getFuzzyScore() ? 0 : 1));
		}
	}
	
	public TDASearchConnector () {
		parser = new JSONParser();
		params = new Parameters();
		qutil = new QueryUtil();
	}
	
	@Override
	public void close () {
		authKey = null;
	}

	@Override
	public String getName () {
		return "TDA-Search";
	}

	@Override
	public String getSettingsDisplay () {
		String tmp = "Server: " + (Util.isEmpty(params.getServer())
			? "<To be specified>"
			: params.getServer());
		return tmp + "\nUser: " + (Util.isEmpty(params.getUsername())
			? "<To be specified>"
			: params.getUsername());
	}

	@Override
	public void open () {
		baseURL = params.getServer();
		if ( !baseURL.endsWith("/") ) baseURL += "/";
		authKey = null;
	}

	@Override
	public int query (String plainText) {
		results = new ArrayList<QueryResult>();
		current = -1;
		if ( Util.isEmpty(plainText) ) return 0;
		return doTDAQuery(plainText, plainText);
	}
	
	@Override
	public int query (TextFragment frag) {
		results = new ArrayList<QueryResult>();
		current = -1;
		if ( !frag.hasText(false) ) return 0;
		return doTDAQuery(qutil.separateCodesFromText(frag), frag.toText());
	}
	
	@Override
	public List<List<QueryResult>> batchQuery (List<TextFragment> fragments) {
		throw new OkapiNotImplementedException();
	}
	
	private int doTDAQuery (String query,
		String original)
	{
		try {
			// Login if needed
			loginIfNeeded();
			// Prepare the plain text
			query = query.replaceAll("\\p{Po}", "");

			// Create the connection and query
			URL url = new URL(baseURL + String.format("segment.json?limit=%d&source_lang=%s&target_lang=%s",
				maxHits, srcCode, trgCode) + "&auth_auth_key="+authKey
				+ (params.getIndustry()>0 ? "&industry="+String.valueOf(params.getIndustry()) : "")
				+ (params.getContentType()>0 ? "&content_type="+String.valueOf(params.getContentType()) : "")
				+ "&q=" + URLEncoder.encode(query, "UTF-8"));
			URLConnection conn = url.openConnection();

			// Get the response
			JSONObject object = (JSONObject)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)object;
	    	JSONArray array = (JSONArray)map.get("segment");
	    	
			// We keep our own hit count as TDA 'limit' may return more than the value asked
			int count = ((array.size() > maxHits) ? maxHits : array.size());
	    	for ( int i=0; i<count; i++ ) {
	    		@SuppressWarnings("unchecked")
	    		Map<String, Object> entry = (Map<String, Object>)array.get(i);
	    		QueryResult result = new QueryResult();
	    		result.weight = getWeight();
	    		result.source = new TextFragment((String)entry.get("source"));
	    		result.target = new TextFragment((String)entry.get("target"));
	    		result.origin = "TDA";
	    		@SuppressWarnings("unchecked")
	    		String tmp = (String)((Map<String, Object>)entry.get("provider")).get("name");
	    		if ( !Util.isEmpty(tmp) ) result.origin += ("/" + tmp);
	    		result.setFuzzyScore(90); //TODO: re-score the data to get meaningful hits
	    		results.add(result);
	    	}

			// Adjust scores
			//TODO: re-order and re-filter results
	    	//TODO: fixup based on original text, not pre-processed one
			fixupResults(original);
			
	    	current = (( results.size() > 0 ) ? 0 : -1);
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error querying the server." + e.getMessage(), e);
		}
		return ((current==0) ? 1 : 0);
	}

	@Override
	public IParameters getParameters () {
		return this.params;
	}
	
	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}
	
	@Override
	protected String toInternalCode (LocaleId locale) {
		String code = locale.toBCP47(); 
		String lang = locale.getLanguage();
		if ( locale.getRegion() == null ) {
			// TDA langs have all a region code: Try to add it here.
			boolean found = false;
			for ( int i=0; i<LANGSMAP.length; i++ ) {
				if ( lang.equals(LANGSMAP[i][0]) ) {
					code = LANGSMAP[i][1];
					found = true;
					break;
				}
			}
			if ( !found ) { // Default: region code is same as lang code: fr-fr
				code = lang+"-"+lang;
			}
		}
		return code;
	}
	
	private void loginIfNeeded () {
		if ( authKey != null ) return;
		try {
			// Create the connection and query
			URL url = new URL(baseURL + "auth_key.json?action=login");
			URLConnection conn = url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			// Write out the content string to the stream.
			String content = String.format("auth_username=%s&auth_password=%s&app_key=%s",
				URLEncoder.encode(params.getUsername(), "UTF-8"),
				URLEncoder.encode(params.getPassword(), "UTF-8"),
				params.getAppKey());
			out.writeBytes(content);
			out.flush ();
			out.close ();
			
			// Read the response
			JSONObject object = (JSONObject)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)object;
	    	@SuppressWarnings("unchecked")
	    	Map<String, Object> data = (Map<String, Object>)map.get("auth_key");
	    	authKey = (String)data.get("id");
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error while login to the server." + e.getMessage(), e);
		}
	}

	@Override
	public int getMaximumHits () {
		return maxHits;
	}

	@Override
	public int getThreshold () {
		return threshold;
	}

	@Override
	public void setMaximumHits (int max) {
		maxHits = max;
	}

	@Override
	public void setThreshold (int threshold) {
		this.threshold = threshold;
	}

	@Override
	public void clearAttributes () {
		// TODO Auto-generated method stub
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

	@Override
	public void removeAttribute (String name) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setAttribute (String name,
		String value)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void setLanguages (LocaleId sourceLocale,
		LocaleId targetLocale)
	{
		super.setLanguages(sourceLocale, targetLocale);
		matcher = new TextMatcher(sourceLocale, sourceLocale);
	}

	@Override
	public void setRootDirectory (String rootDir) {
		// Not used
	}
	
	/**
	 * Re-calculates the scores, re-orders and filters the results based on
	 * more meaning full comparisons.
	 * @param plainText the original text query.
	 */
	private void fixupResults (String plainText) {
		if ( results.size() == 0 ) return;
		List<String> tokens = matcher.prepareBaseTokens(plainText);
		// Loop through the results
		for ( Iterator<QueryResult> iter = results.iterator(); iter.hasNext(); ) {
			QueryResult qr = iter.next();
			// Compute the adjusted score
			qr.setFuzzyScore(matcher.compareToBaseTokens(plainText, tokens, qr.source));
			// Remove the item if lower than the threshold 
			if ( qr.getFuzzyScore() < threshold ) {
				iter.remove();
			}
			else { // Set match type
				if ( qr.getFuzzyScore() >= 100 ) qr.matchType = MatchType.EXACT;
				else if ( qr.getFuzzyScore() > 0 ) qr.matchType = MatchType.FUZZY;
			}
		}
		// Re-order the list from the 
		Collections.sort(results, scorComp);
	}

}
