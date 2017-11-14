/*===========================================================================
  Copyright (C) 2009-2017 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.mymemory;

import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.BaseConnector;
import net.sf.okapi.lib.translation.ITMQuery;
import net.sf.okapi.lib.translation.QueryUtil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MyMemoryTMConnector extends BaseConnector implements ITMQuery {

	private static final String BASE_URL = "https://mymemory.translated.net/api/get";
	private static final String BASE_QUERY = "?q=%s&langpair=%s|%s";
	
	private List<QueryResult> results;
	private int current = -1;
	private int maxHits = 25;
	private int threshold = 75;
	private Parameters params;
	private QueryUtil qutil;
	private JSONParser parser;
	private String ipAddress;

	public MyMemoryTMConnector () {
		params = new Parameters();
		qutil = new QueryUtil();
		parser = new JSONParser();
		results = new ArrayList<QueryResult>();
	}

	@Override
	public String getName () {
		return "MyMemory.net";
	}

	@Override
	public String getSettingsDisplay () {
		return String.format("Server: %s\nAllow MT: %s", BASE_URL,
			((params.getUseMT()==1) ? "Yes" : "No"));
	}
	
	@Override
	public void close () {
		// Nothing to do
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
	public void open () {
		if ( params.getSendIP() ) {
			try {
				InetAddress thisIp = InetAddress.getLocalHost();
				ipAddress = thisIp.getHostAddress();
			}
			catch ( UnknownHostException e ) {
				ipAddress = null;
			}
		}
	}

	@Override
	public int query (TextFragment frag) {
		results.clear();
		current = -1;
		if ( !frag.hasText(false) ) return 0;
		try {
			String text = qutil.separateCodesFromText(frag);

			// Create the connection and query
			String urlString = BASE_URL + String.format(BASE_QUERY, URLEncoder.encode(text, "UTF-8"), srcCode, trgCode);
			if ( ipAddress != null ) {
				urlString += ("&ip=" + ipAddress);
			}
			
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();

			// Get the response
			JSONObject object = (JSONObject)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)object;
	    	JSONArray matches = (JSONArray)map.get("matches");
	    	int count = 0;
	    	
	    	for ( int i=0; i<matches.size(); i++ ) {
	    		if ( ++count > maxHits ) break; // Maximum reached
	    		
	    		@SuppressWarnings("unchecked")
	    		Map<String, Object> details = (Map<String, Object>)matches.get(i);
	    		QueryResult res = new QueryResult();
	    		
	    		// Check origin
	    		String from = (String)details.get("last-updated-by");
	    		if ( from == null ) {
	    			from = (String)details.get("created-by");
	    			if ( from == null) from = "";
	    		}
	    		if ( from.equals("MT!") ) {
	    			if ( params.getUseMT() != 1 ) {
	    				count--;
	    				continue; // Skip MT results
	    			}
					res.matchType = MatchType.MT;
					//TODO: should we set the standard MT score? (or use myMemory)?
					//res.score = 95; // Standard score for MT
				}
	    		//TODO: Need to take quality into account
	    		//else {
    			// Check the score
	    		Double match;
	    		Object m = details.get("match");
	    		if ( m instanceof Long ) {
	    			long tmpl = (long)m;
	    			match = (double)(tmpl*100);
	    		}
	    		else { // Assume it's a Double
	    			match = ((Double)m)*100;
	    		}
	    			
    			// To workaround bug in score calculation
    			// Score > 100 should be treated as 100 per Alberto's info.
    			int score = match.intValue() > 100 ? 100 : match.intValue();
	    		//}
	    		// Take presence of codes into account (unsupported)
				if ( qutil.hasCode() ) score--;

	    		// Stop if we reach the threshold (we assume things are sorted)
	    		if ( score < getThreshold() ) break;
				
				// Set various data
	    		res.weight = getWeight();
	    		res.origin = getName();
	    		res.setFuzzyScore(score);

	    		// Set source and target text
				if ( qutil.hasCode() ) {
					res.source = qutil.createNewFragmentWithCodes((String)details.get("segment"));
					res.target = qutil.createNewFragmentWithCodes((String)details.get("translation"));
				}
				else {
					res.source = new TextFragment((String)details.get("segment"));
					res.target = new TextFragment((String)details.get("translation"));
				}
	    		
	    		results.add(res);
	    	}
			current = 0;
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error querying the server.\n" + e.getMessage(), e);
		}
		if ( results.size() > 0 ) current = 0;
		return results.size();
	}

	@Override
	public int query (String plainText) {
		return query(new TextFragment(plainText));
	}
	
	@Override
	public List<List<QueryResult>> batchQuery (List<TextFragment> fragments) {
		throw new OkapiNotImplementedException();
	}

	@Override
	public void removeAttribute (String name) {
		//TODO: use domain
	}

	@Override
	public void clearAttributes () {
		//TODO: use domain
	}

	@Override
	public void setAttribute (String name,
		String value)
	{
		//TODO: use domain
	}

	@Override
	protected String toInternalCode (LocaleId locale) {
		// The expected language code is language-Region with region mandatory
		String lang = locale.getLanguage();
		String reg = locale.getRegion();
		
		//TODO: Use a lookup table and a more complete one
		if ( lang.equals("en") ) reg = "us";
		else if ( lang.equals("pt") ) reg = "br";
		else if ( lang.equals("el") ) reg = "gr";
		else if ( lang.equals("he") ) reg = "il";
		else if ( lang.equals("ja") ) reg = "jp";
		else if ( lang.equals("ko") ) reg = "kr";
		else if ( lang.equals("ms") ) reg = "my";
		else if ( lang.equals("sl") ) reg = "si";
		else if ( lang.equals("sq") ) reg = "al";
		else if ( lang.equals("sv") ) reg = "se";
		else if ( lang.equals("vi") ) reg = "vn";
		else if ( lang.equals("zh") ) {
			if ( reg != null ) reg = "cn";
		}
		else {
			reg = lang;
		}
		return lang+"-"+reg;
	}

	/**
	 * Sets the maximum number of hits to return.
	 */
	@Override
	public void setMaximumHits (int max) {
		if ( max < 1 ) maxHits = 1;
		else maxHits = max;
	}

	@Override
	public void setThreshold (int threshold) {
		this.threshold = threshold;
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
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		params = (Parameters)params;
	}

	@Override
	public void setRootDirectory (String rootDir) {
		// Not used
	}	

}
