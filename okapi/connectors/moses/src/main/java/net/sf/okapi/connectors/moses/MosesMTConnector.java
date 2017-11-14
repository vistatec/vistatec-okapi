/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.moses;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import net.htmlparser.jericho.CharacterReference;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.BaseConnector;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class MosesMTConnector extends BaseConnector {
	private static final Logger LOGGER = Logger.getLogger(MosesMTConnector.class.getName());
	private Parameters params;
	private XmlRpcClient client;
	private HashMap<String, String> mosesParams;
	private Object[] xmlRpcParams;
	private List<QueryResult> hits;
	private XmlRpcClientConfigImpl config;
	private SimpleTokenizer tokenizer;

	public MosesMTConnector() {
		params = new Parameters();		
		hits = new LinkedList<QueryResult>();
		client = new XmlRpcClient();	
		config = new XmlRpcClientConfigImpl();
		mosesParams = new HashMap<String, String>();
		// The XmlRpcClient.execute method doesn't accept Hashmap (pParams).
		// It's either Object[] or List.
		xmlRpcParams = new Object[] { null };
	}

	
	@Override
	public String getName() {
		return "Moses-MT";
	}

	@Override
	public String getSettingsDisplay() {
		return "Server: " + params.getServerURL();
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}

	@Override
	public void open() {		
		// close any previously opened clients
		close();
		
		tokenizer = new SimpleTokenizer(getTargetLanguage());
		
		try {
			config.setServerURL(new URL(params.getServerURL()));
		} catch (MalformedURLException e) {
			throw new OkapiIOException(String.format("Cannot connect to the following URL: %s",
					params.getServerURL()), e);
		}
		client.setConfig(config);

		// initialize the common parameters we send moses
		mosesParams.put("align", "true");
		mosesParams.put("report-all-factors", "true");
	}

	@Override
	public void close() {
		hits.clear();
	}

	@Override
	public int query(String plainText) {
		return query(new TextFragment(plainText));
	}

	@Override
	public int query(TextFragment text) {
		hits.clear();
		if (!text.hasText(false))
			return 0;

		QueryResult qRes = new QueryResult();
		mosesParams.put("text", mosesPreprocess(text.getText()));
		xmlRpcParams[0] = mosesParams;
		try {
			@SuppressWarnings("rawtypes")
			HashMap mt = (HashMap) client.execute("translate", xmlRpcParams);
			String textTranslation = (String) mt.get("text");

			qRes.weight = getWeight();
			qRes.source = text;
			if (text.hasCode()) {
				qRes.target = new TextFragment(mosesPostProcess(textTranslation));
				for (Code c : text.getClonedCodes()) {
					qRes.target.append(c);
				}
				qRes.target.alignCodeIds(text);
			} else {
				qRes.target = new TextFragment(mosesPostProcess(textTranslation));
			}
			qRes.setCombinedScore(QueryResult.COMBINEDSCORE_UNDEFINED);
			qRes.setQuality(QueryResult.QUALITY_UNDEFINED);
			qRes.setFuzzyScore(95); // TODO: Makes this a parameter
			qRes.origin = getName();
			qRes.matchType = MatchType.MT;
		} catch (XmlRpcException e) {
			throw new OkapiIOException(String.format(
					"Error calling moses server translate for: %s", text.toString()), e);
		}
		hits.add(qRes);
		
		return 1;
	}

	@Override
	public boolean hasNext() {
		return !hits.isEmpty();
	}

	@Override
	public QueryResult next() {
		return hits.remove(0);
	}
	
	private String mosesPreprocess(String text) {		
		String postProcessText = tokenizer.tokenize(text);
		postProcessText = postProcessText.toLowerCase(getTargetLanguage().toJavaLocale());
		return postProcessText;		
	}
	
	private String mosesPostProcess(String text) {
		// unescape any xml/html entities
		String t = CharacterReference.decode(text);
		
		// FIXME: Will only work with English. It will partially work and other 
		// latin based languages (sans the abbreviations)
		t = DeNormalize.processSingleLine(t).replaceAll("\\s+", " ").trim();
		
		// remove unknown markers
		t = t.replaceAll("(\\|UNK)+", "");
		return t;
	}
}
