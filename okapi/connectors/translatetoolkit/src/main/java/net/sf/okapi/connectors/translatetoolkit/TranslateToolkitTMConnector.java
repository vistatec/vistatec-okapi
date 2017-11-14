/*===========================================================================
  Copyright (C) 2009-2014 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.translatetoolkit;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.query.IQuery;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.BaseConnector;
import net.sf.okapi.lib.translation.ITMQuery;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Implement {@link IQuery} for the amaGama TM hosted for Translate-Toolkit.
 * <p>Initial URL was: http://amagama.locamotion.org:80/tmserver/
 * <p>Around May-20-2014 it changed to: https://amagama-live.translatehouse.org/api/v1/
 */
public class TranslateToolkitTMConnector extends BaseConnector implements ITMQuery {

	private Parameters params;
	private String baseURL;
	private List<QueryResult> results;
	private int current = -1;
	private int maxHits = 25;
	private int threshold = 60;
	private JSONParser parser;
	
	public TranslateToolkitTMConnector () {
		params = new Parameters();
	}
	
	@Override
	public String getName () {
		return "Translate Toolkit TM";
	}

	@Override
	public String getSettingsDisplay () {
		return "Server: "+params.getUrl();
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
		baseURL = params.getUrl();
		if ( !baseURL.endsWith("/") ) baseURL += "/";
		parser = new JSONParser();
	}

	@Override
	public int query (String plainText) {
		return query(new TextFragment(plainText));
	}

	@Override
	public int query (TextFragment text) {
		// Otherwise, treat the codes depending on the mode
		String plain;
		if ( text.hasCode() && params.getSupportCodes() ) {
			plain = GenericContent.fromFragmentToLetterCoded(text, true);
		}
		else {
			plain = text.getCodedText();
		}
		
		results = new ArrayList<QueryResult>();
		current = -1;
		if ( Util.isEmpty(plain) ) {
			return 0;
		}
		
		try {
			URL url = new URL(baseURL + srcCode + "/" + trgCode + "/unit/"
				+ URLEncoder.encode(plain, "UTF-8").replace("+", "%20"));
			URLConnection conn = url.openConnection();

			// Get the response
	        JSONArray array = (JSONArray)parser.parse(
	        	new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
			QueryResult qr;
	        for ( int i=0; i<array.size(); i++ ) {
	        	if ( i >= maxHits ) break; // Stop at maxHits
	        	@SuppressWarnings("unchecked")
	        	Map<String, Object> map = (Map<String, Object>)array.get(i);
	        	qr = new QueryResult();
	        	qr.weight = getWeight();
	        	qr.setFuzzyScore(((Double)map.get("quality")).intValue());
	        	if ( qr.getFuzzyScore() < threshold ) break; // Done
	        	
	        	if ( text.hasCode() && params.getSupportCodes() ) {
	        		qr.source = GenericContent.fromLetterCodedToFragment((String)map.get("source"), null, false, true);
	        		qr.target = GenericContent.fromLetterCodedToFragment((String)map.get("target"), null, false, true);
	        	}
	        	else {
	        		qr.source = new TextFragment((String)map.get("source"));
	        		qr.target = new TextFragment((String)map.get("target"));
	        	}

	        	// Set match type
				if ( qr.getFuzzyScore() >= 100 ) qr.matchType = MatchType.EXACT;
				else if ( qr.getFuzzyScore() > 0 ) qr.matchType = MatchType.FUZZY;
	        	results.add(qr);
	        }
			if ( results.size() > 0 ) current = 0;
			return results.size();
		}
		catch ( MalformedURLException e ) {
			throw new OkapiException("Error when querying.", e);
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiException("Error when querying.", e);
		}
		catch ( IOException e ) {
			throw new OkapiException("Error when querying.", e);
		}
		catch ( ParseException e ) {
			throw new OkapiException("Error when parsing JSON results.", e);
		}
	}
	
	@Override
	public List<List<QueryResult>> batchQuery (List<TextFragment> fragments) {
		throw new OkapiNotImplementedException();
	}

	@Override
	public void clearAttributes () {
		// Not used with this connector
	}

	@Override
	public void removeAttribute (String name) {
		// Not used with this connector
	}

	@Override
	public void setAttribute (String name,
		String value)
	{
		// Not used with this connector
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
	public int getMaximumHits () {
		return maxHits;
	}

	@Override
	public int getThreshold () {
		return threshold;
	}
	
	@Override
	protected String toInternalCode (LocaleId standardCode) {
		return standardCode.toPOSIXLocaleId();
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	public void setRootDirectory (String rootDir) {
		// Not used
	}
}
