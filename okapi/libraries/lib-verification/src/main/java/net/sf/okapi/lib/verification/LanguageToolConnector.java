/*===========================================================================
  Copyright (C) 2010-2017 by the Okapi Framework contributors
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

package net.sf.okapi.lib.verification;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.query.IQuery;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.connectors.google.GoogleMTv2Connector;
import net.sf.okapi.connectors.google.GoogleMTv2Parameters;

public class LanguageToolConnector {
	
	//private final Logger logger = LoggerFactory.getLogger(getClass());

	private ArrayList<Issue> issues;
	private String lang;
	private String motherTongue;
	private String serverUrl;
	private IQuery mt;
	private boolean bilingualMode;
	private JSONParser jsonParser;
	
	/**
	 * Creates a new LanguageToolConnector object.
	 */
	public LanguageToolConnector () {
		issues = new ArrayList<Issue>();
		jsonParser = new JSONParser();
	}

	public void initialize (LocaleId locId,
		LocaleId motherLocId, 
		String serverUrl,
		boolean translateLTMsg,
		boolean bilingualMode,
		String ltTranslationSource,
		String ltTranslationTarget,
		String ltTranslationServiceKey)
	{
		//TODO: Better mapping to LT language codes
		lang = locId.getLanguage();
		motherTongue = motherLocId.getLanguage();
		// Set the server URL
		if ( !serverUrl.endsWith("/") ) serverUrl += "/";
		this.serverUrl = serverUrl;

		if ( mt != null ) {
			mt.close();
			mt = null;
		}
		this.bilingualMode = bilingualMode;
		if ( translateLTMsg ) {
			mt = new GoogleMTv2Connector();
			GoogleMTv2Parameters prm = (GoogleMTv2Parameters)mt.getParameters();
			prm.setApiKey(ltTranslationServiceKey);
			mt.setLanguages(LocaleId.fromBCP47(ltTranslationSource),
				LocaleId.fromBCP47(ltTranslationTarget));
			mt.open();
		}
	}
	
	public List<Issue> getIssues () {
		return issues;
	}

	protected JSONObject sendRequest(URL url) throws UnsupportedEncodingException, IOException, ParseException {
        URLConnection conn = url.openConnection();
        return (JSONObject) jsonParser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	}

	public int checkSegment (URI docId,
		String subDocId,
		Segment srcSeg,
		Segment trgSeg,
		ITextUnit tu)
	{
		issues.clear();
		if ( !trgSeg.text.hasText() ) return 0;
		String ctext = trgSeg.text.getCodedText();

		// Create the connection and query
		URL url;
		try {
			if ( bilingualMode ) {
			    url = new URL(serverUrl + String.format("v2/check?language=%s&text=%s&srctext=%s&motherTongue=%s", lang,
					URLEncoder.encode(ctext, "UTF-8"),
					URLEncoder.encode(srcSeg.text.getCodedText(), "UTF-8"),
					motherTongue));
			}
			else {
			    url = new URL(serverUrl + String.format("v2/check?language=%s&text=%s", lang,
					URLEncoder.encode(ctext, "UTF-8")));
			}
			// Get and process the results
			JSONObject object = sendRequest(url);
			JSONArray matches = (JSONArray) object.get("matches");
			for (Object match: matches) {
			    JSONObject error = (JSONObject) match;
				String msg = (String) error.get("message");
				if ( mt != null ) {
					if ( mt.query(msg) > 0 ) {
						msg = String.format("%s  (--> %s)", msg, mt.next().target.toText());
					}
				}
				Long start = (Long) error.get("offset");
				Long end = start + (Long) error.get("length");
				Issue issue = new Issue(docId, subDocId, IssueType.LANGUAGETOOL_ERROR, tu.getId(), trgSeg.getId(),
					msg, 0, 0,
					TextFragment.fromFragmentToString(trgSeg.text, start.intValue()),
					TextFragment.fromFragmentToString(trgSeg.text, end.intValue()),
					Issue.DISPSEVERITY_MEDIUM, tu.getName());
				issues.add(issue);
				// Check for ITS issue type
				JSONObject rule = (JSONObject) error.get("rule");
				String itsType = (String) rule.get("issueType");
				if ( itsType != null ) {
					issue.setString(GenericAnnotationType.LQI_TYPE, itsType);
				}
			}
		}
		catch ( Throwable e ) {
			// -99 for srcEnd special marker
			issues.add(new Issue(docId, subDocId, IssueType.LANGUAGETOOL_ERROR, tu.getId(), trgSeg.getId(),
				"Error with LanguageTool server. All LT checks are skipped from this text unit on. "+e.getMessage(),
				0, -99, 0, -1, Issue.DISPSEVERITY_HIGH, tu.getName()));
		}
		
		return issues.size();
	}

//	private IssueType convertITSType (String ltType) {
//		return IssueType.LANGUAGETOOL_ERROR;
//	}
}
