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

package net.sf.okapi.connectors.microsoft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.BaseConnector;
import net.sf.okapi.lib.translation.ITMQuery;
import net.sf.okapi.lib.translation.QueryUtil;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class MicrosoftMTConnector extends BaseConnector implements ITMQuery {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final int RETRIES = 5; // number of times to try to get a response from Microsoft before failing
	private static final int SLEEPPAUSE = 300; // DWH 5-3-2012 how long to wait before trying
	static final int CONNECTION_TIMEOUT = 20 * 1000;
	
	private QueryUtil util = new QueryUtil();
	private Parameters params = new Parameters();
	private int maximumHits = 1;
	private int threshold = -10; // it was returning 0 for good translations
	private Iterator<QueryResult> results = Collections.emptyIterator();
	private MicrosoftMTAPI api;
	private CloseableHttpClient httpClient;

	public MicrosoftMTConnector () {
		// Use a 20s timeout; this is conservative (Microsoft recommends 13s).
		this(HttpClientBuilder.create()
				.setDefaultRequestConfig(RequestConfig.custom()
					.setConnectTimeout(CONNECTION_TIMEOUT)
					.setSocketTimeout(CONNECTION_TIMEOUT)
					.build())
				.build());
	}

	public MicrosoftMTConnector(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
		util = new QueryUtil();
		params = new Parameters();
	}
	
	@Override
	public void close () {
		// Nothing to do
	}

	@Override
	public String getName () {
		return "Microsoft-Translator";
	}

	@Override
	public String getSettingsDisplay () {
		return "Service: " + MicrosoftMTAPIImpl.MSMT_BASE_URL;
	}

	@Override
	public void open () {
		open(null);
	}

	void open(MicrosoftMTAPI api) {
		this.results = Collections.emptyIterator();
		this.api = api != null ? api :
			new MicrosoftMTAPIImpl(getParameters(), httpClient, new TokenProvider(httpClient, getParameters()));
	}

	protected <T> int _query(String queryText, T originalText, QueryResultBuilder<T> resultBuilder) {
		open(api);
		if (queryText.isEmpty()) return 0;
		List<QueryResult> queryResults = new ArrayList<>();
		try {
			for (int tries = 0; tries < RETRIES; tries++) {
				List<TranslationResponse> responses = api.getTranslations(queryText, srcCode, trgCode, maximumHits, threshold);
				if (responses != null) {
					queryResults = resultBuilder.convertResponses(responses, originalText);
					break;
				}
				// TODO handle this in the API?
				try {
					Thread.sleep(SLEEPPAUSE);
				} catch (InterruptedException e) { // the app closed
					throw new OkapiException("Interrupted while trying to contact Microsoft MT");
				}
			}
		}
		catch ( Throwable e) {
			throw new OkapiException("Error querying the MT server.\n" + e.getMessage(), e);
		}
		if (queryResults.size() > 0) {
		    results = queryResults.iterator();
		    return queryResults.size();
		}
	    throw new OkapiException("Could not retrieve results from Microsoft MT after " + RETRIES + " attempts.");
	}

	@Override
	public int query (String plainText) {
		return _query(plainText, plainText, new TextQueryResultBuilder(params, getWeight()));
	}

	@Override
	public int query (TextFragment frag) {
		return _query(util.toCodedHTML(frag), frag, new FragmentQueryResultBuilder(params, getWeight()));
	}

	/**
	 * GetTranslationsArray has two internal limitations:
	 * - Only 10 strings can be translated at once.
	 * - The total number of characters in all strings must be < 10,000 characters. According to
	 *   the docs, this doesn't count the length of the XML.
	 * See: https://msdn.microsoft.com/en-us/library/ff512418.aspx
	 */
	static final int CHAR_COUNT_THRESHOLD = 10000;
	protected <T> List<List<QueryResult>> _batchQuery(List<String> texts, List<T> originalText,
													  QueryResultBuilder<T> qrBuilder) {
		open(api);
		int batchCharCount = 0;
		List<List<QueryResult>> results = new ArrayList<>(texts.size());
		List<String> textsToQuery = new ArrayList<>();
		List<T> originalTextsToQuery = new ArrayList<>();
		for (int end = 0; end < texts.size(); end++) {
			int charCount = texts.get(end).length();
			if (charCount >= CHAR_COUNT_THRESHOLD) {
				// Too big, skip this one
				logger.warn("Segment {} starting with '{}' is too long to query.", end,
							texts.get(end).substring(0, 20));
				continue;
			}
			boolean processBatch = false;
			if (batchCharCount + charCount >= CHAR_COUNT_THRESHOLD) {
				processBatch = true;
			}
			if (textsToQuery.size() >= 10) {
				processBatch = true;
			}
			if (processBatch) {
				results.addAll(_subBatchQuery(textsToQuery, originalTextsToQuery, qrBuilder));
				batchCharCount = 0;
				textsToQuery.clear();
				originalTextsToQuery.clear();
				end--; // Do this one again
			}
			else {
				batchCharCount += charCount;
				textsToQuery.add(texts.get(end));
				originalTextsToQuery.add(originalText.get(end));
			}
		}
		if (textsToQuery.size() > 0) {
			results.addAll(_subBatchQuery(textsToQuery, originalTextsToQuery, qrBuilder));
		}
		return results;
	}

	// Process a single batch that is small enough to be handled in a single API call.
	protected <T> List<List<QueryResult>> _subBatchQuery(List<String> texts, List<T> originalText,
								QueryResultBuilder<T> qrBuilder) {
		GetTranslationsArrayRequest request = new GetTranslationsArrayRequest(texts, srcCode, trgCode,
											maximumHits, params.getCategory());
		List<List<QueryResult>> queryResults = new ArrayList<>();
		// XXX how does this handle skipped entries?
		for (int tries = 0; tries < RETRIES; tries++) {
			List<List<TranslationResponse>> responses = api.getTranslationsArray(request, srcCode, trgCode,
												maximumHits, threshold);
			if (responses != null) {
				for (int i = 0; i < responses.size(); i++) {
					queryResults.add(qrBuilder.convertResponses(responses.get(i), originalText.get(i)));
				}
				break;
			}
			// TODO handle this in the API?
			try {
				Thread.sleep(SLEEPPAUSE);
			} catch (InterruptedException e) { // the app closed
				throw new OkapiException("Interrupted while trying to contact Microsoft MT");
			}
		}
		if (queryResults.size() > 0) {
		    return queryResults;
		}
		logger.error(String.format(
                "Failed to batch translate with Microsoft Translator (%d tries). Skipping query for %s",
                RETRIES, request.toXML()));
		throw new OkapiException("Could not retrieve results from Microsoft MT after " + RETRIES + " attempts.");
	}
	
	/**
	 * Adds or overwrites a translation on the server.
	 * @param source the text of the source.
	 * @param target the new text of the translation.
	 * @param rating the rating to use for this translation.
	 * @return the HTTP response code (200 is success)
	 */
	public int addTranslation (TextFragment source,
		TextFragment target,
		int rating)
	{
		open(api);
		try {
			// Convert the fragment to coded HTML
			String stext = util.toCodedHTML(source);
			String ttext = util.toCodedHTML(target);
			return api.addTranslation(stext, ttext, srcCode, trgCode, rating);
		}
		catch ( Throwable e) {
			throw new OkapiException("Error adding translation to the server.\n" + e.getMessage(), e);
		}
	}
	
	/**
	 * Adds or overwrites a list of translations on the server.
	 * @param sources list of the source fragments. They should be 100 at most. 
	 * @param targets list of the corresponding translations. They must be one for each source
	 * and they must be in the same order.
	 * @param ratings  list of the corresponding ratings. They must be one for each source
	 * and they must be in the same order.
	 * @return the HTTP response code (200 is success)
	 */
	public int addTranslationList (List<TextFragment> sources,
		List<TextFragment> targets,
		List<Integer> ratings)
	{
		open(api);
		try {
			// Checking
			if ( targets.size() != sources.size() ) {
				throw new OkapiException("There should be as many targets as sources.");
			}
			if ( ratings.size() != sources.size() ) {
				throw new OkapiException("There should be as many ratings as sources.");
			}
			if ( sources.size() > 100 ) {
				throw new OkapiException("No more than 100 segments allowed.");
			}

			return api.addTranslationArray(util.toCodedHTML(sources), util.toCodedHTML(targets),
					ratings, srcCode, trgCode);
		}
		catch ( Throwable e) {
			throw new OkapiException("Error adding translations.\n" + e.getMessage(), e);
		}
	}

	@Override
	public void leverage (ITextUnit tu) {
		leverageUsingBatchQuery(tu);
	}
	
	@Override	
	public void batchLeverage(List<ITextUnit> tuList) {
		batchLeverageUsingBatchQuery(tuList);
	}

	@Override
	public List<List<QueryResult>> batchQueryText(List<String> plainTexts) {
		return _batchQuery(plainTexts, plainTexts, new TextQueryResultBuilder(params, getWeight()));
	}

	@Override
	public List<List<QueryResult>> batchQuery (List<TextFragment> fragments) {
		return _batchQuery(util.toCodedHTML(fragments), fragments,
						   new FragmentQueryResultBuilder(params, getWeight()));
	}

	@Override
	protected String toInternalCode (LocaleId locale) {
		String code = locale.toBCP47().toLowerCase();
		if ( code.equals("zh-tw") || code.startsWith("zh-hant") || code.equals("zh-cht") || code.equals("zh-hk")) {
			code = "zh-CHT";
		}
		else if ( code.startsWith("zh") ) { // zh-cn, zh-hans, zh-chs..
			code = "zh-CHS";
		}
		else if ( code.startsWith("sr-cyrl") ) {
			// Preserve script for Serbian in Cyrillic
			// Latin script is the default
			;
		}
		else if ( code.startsWith("pt") || code.equals("es-419") ) {
			; // Preserve region variants for some locales
		}
		else if (locale.getLanguage().equals("in")) {
			// Java uses 'in' to represent the Indonesian locale, while
			// Microsoft expects 'id'. ICU and Locale#toLanguageTag
			// both produce 'id', but our LocaleIds aren't always
			// coming from those sources.
			return "id";
		}
		else { // Use just the language otherwise
			code = locale.getLanguage(); 
		}
		return code;
	}

	@Override
	public Parameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	public boolean hasNext () {
		return results.hasNext();
	}

	@Override
	public QueryResult next () {
		return results.hasNext() ? results.next() : null;
	}

	@Override
	public int getMaximumHits () {
		return maximumHits;
	}

	@Override
	public void setMaximumHits (int maximumHits) {
		this.maximumHits = maximumHits;
	}

	@Override
	public int getThreshold () {
		return threshold;
	}

	@Override
	public void setThreshold (int threshold) {
		this.threshold = threshold;
		this.threshold = -10; // Microsoft is returning confidence of 0
	}

	@Override
	public void setLanguages (LocaleId sourceLocale,
		LocaleId targetLocale)
	{
		super.setLanguages(sourceLocale, targetLocale);
		// srcCode and trgCode are set properly in setLanguage()
		// they must not be reset here
	}
}
