/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common.query;

import java.util.List;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;

/**
 * Provides the methods common to all query engines of translation resources 
 * that can be queried for translating text units. 
 */
public interface IQuery extends AutoCloseable {

	/**
	 * Gets the name of the connector.
	 * @return the name of the connector.
	 */
	public String getName ();
	
	/**
	 * Gets a display representation of the current settings for this connector.
	 * This can be a display of some of the parameters for example, or some explanations
	 * about default non-modifiable settings.
	 * @return a display representation of the current settings.
	 */
	public String getSettingsDisplay ();

	/**
	 * Sets the source and target languages for this query engine.
	 * @param sourceLocale Code of the source locale.
	 * @param targetLocale Code of the target locale.
	 */
	public void setLanguages (LocaleId sourceLocale,
		LocaleId targetLocale);
	
	/**
	 * Gets the current source language for this query engine.
	 * @return Code of the source language.
	 */
	public LocaleId getSourceLanguage ();
	
	/**
	 * Gets the current target language for this query engine.
	 * @return Code of the target language.
	 */
	public LocaleId getTargetLanguage ();
	
	/**
	 * Sets an attribute for this query engine.
	 * @param name name of the attribute.
	 * @param value Value of the attribute.
	 */
	public void setAttribute (String name,
		String value);
	
	/**
	 * Removes a given attribute from this query engine.
	 * @param name The name of the attribute to remove.
	 */
	public void removeAttribute (String name);

	/**
	 * Removes all attributes from this query engine.
	 */
	public void clearAttributes ();
	
	/**
	 * Sets the parameters for opening and querying this connector.
	 * @param params the parameters to set.
	 */
	public void setParameters (IParameters params);
	
	/**
	 * Gets the current parameters of this connector.
	 * @return the current parameters of this connector
	 * or null if no parameters are used.
	 */
	public IParameters getParameters ();
	
	/**
	 * Opens this query engine.
	 */
	public void open ();
	
	/**
	 * Closes this connector.
	 */
	public void close ();
	
	/**
	 * Starts a query for a give plain text. 
	 * @param plainText text to query.
	 * @return The number of hits for the given query.
	 */
	public int query (String plainText);
	
	/**
	 * Starts a query for a given text.
	 * @param text The text to query.
	 * @return The number of hits for the given query.
	 */
	public int query (TextFragment text);
	
	/**
	 * Leverages a text unit (segmented or not) based on the current settings.
	 * Any options or attributes needed must be set before calling this method.
	 * @param tu the text unit to leverage.
	 */
	public void leverage(ITextUnit tu);
	
	/**
	 * Leverages a batch of text units (segmented or not) based on the current settings.
	 * Any options or attributes needed must be set before calling this method.
	 * @param tus of the text unit to leverage.
	 */
	public void batchLeverage(List<ITextUnit> tus);

	/**
	 * Starts a batch query for a given list of plain texts.
	 * <br>
	 * <b>Note that batchQueryText does not use hasNext and next methods. Callers must use the returned list directly.</b>
	 * @param plainTexts list of the plain texts to translate.
	 * @return a list of lists of query result. Each list corresponds to a source text (in the same order)
	 */
	public List<List<QueryResult>> batchQueryText(List<String> plainTexts);

	/**
	 * Starts a batch query for a given list of {@link TextFragment}s.
	 * Some {@link IQuery} implementations are significantly faster when 
	 * a using batch query.
	 * <br>
	 * <b>Note that batchQuery does not use hasNext and next methods. Callers must use the returned list directly.</b>
	 * @param fragments list of the text fragments to translate.
	 * @return a list of lists of query result. Each list corresponds to a source text (in the same order)
	 */
	public List<List<QueryResult>> batchQuery (List<TextFragment> fragments);

	/**
	 * Indicates of there is a hit available.
	 * @return True if a hit is available, false if not.
	 */
	public boolean hasNext ();

	/**
	 * Gets the next hit for the last query.
	 * @return A QueryResult object that holds the source and target text of
	 * the hit, or null if there is no more hit. 
	 */
	public QueryResult next ();

	/**
	 * Sets the root directory that may be used to replace the available ${rootDir} in the
	 * parameters of this object.
	 * @param rootDir the root directory.
	 */
	public void setRootDirectory (String rootDir);
	
	/**
	 * Set the relative weight of this {@link IQuery} connector as compared to 
	 * other connectors. Used to set {@link QueryResult#weight}.    
	 * @param weight the new relative weight.
	 */
	public void setWeight(int weight);
	
	/**
	 * Get the weight for this connector.
	 * @return the weight for this connector
	 */
	public int getWeight();
	
	/**
	 * Sets the threshold for when to avoid executing a query.
	 * <p>If the entry to leverage has already one candidate with a score equals or above this value,
	 * no query is performed. Use 101 to always allow the query.
	 * @param noQueryThreshold no-query threshold (a value between 0 and 101).
	 */
	public void setNoQueryThreshold (int noQueryThreshold);
	
	/**
	 * Gets the threshold for when to avoid a query.
	 * @return the current no-query threshold.
	 */
	public int getNoQueryThreshold ();

}
