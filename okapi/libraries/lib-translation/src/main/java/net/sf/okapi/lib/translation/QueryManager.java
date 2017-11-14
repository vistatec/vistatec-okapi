/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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

package net.sf.okapi.lib.translation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.query.IQuery;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

/**
 * Provides a wrapper to manage and query several translation resources at the 
 * same time. For example, a local TM, a remote TM and a Machine Translation server.
 */
public class QueryManager {

	private LinkedHashMap<Integer, ResourceItem> resList;
	private ArrayList<QueryResult> results;
	private int current = -1;
	private int lastId = 0;
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private LinkedHashMap<String, String> attributes;
	private int threshold = 75;
	private int maxHits = 5;
	private int totalSegments;
	private int exactBestMatches;
	private int fuzzyBestMatches;
	private String rootDir;
	private int noQueryThreshold = 101;
	// Options
	private int thresholdToFill = Integer.MAX_VALUE;
	private boolean keepIfNotEmpty = true; // == false for actual option
	private boolean keepIfTargetSameAsSource = true; // == false for actual option
	private boolean downgradeIdenticalBestMatches = false;
	private String targetPrefix = null;
	private int thresholdToPrefix = 99;
	private boolean copySourceOnNoText = false;
	
	/**
	 * Creates a new QueryManager object.
	 */
	public QueryManager () {
		resList = new LinkedHashMap<Integer, ResourceItem>();
		results = new ArrayList<QueryResult>();
		attributes = new LinkedHashMap<String, String>();		
	}
	
	/**
	 * Adds a translation resource to the manager.
	 * @param connector The translation resource connector to add.
	 * @param name Name of the translation resource to add.
	 * @return The ID for the added translation resource. This ID can be
	 * used later to access specifically the added translation resource.
	 */
	public int addResource (IQuery connector, String name) {
		assert(connector!=null);
		ResourceItem ri = new ResourceItem();
		ri.query = connector;
		ri.enabled = true;
		ri.name = name;
		resList.put(++lastId, ri);
		return lastId;
	}
	
	/**
	 * Adds a translation resource to the manager and initializes it with the 
	 * current source and target language of this manager, as well as any
	 * attributes that is set, and the current threshold and maximum hits if it is relevant.
	 * @param connector The translation resource connector to add.
	 * @param resourceName Name of the translation resource to add.
	 * @param params the parameters for this connector.
	 * @return The identifier for the added translation resource. This identifier
	 * can be used later to access specifically the added translation resource.
	 * @throws RuntimeException if an error occurs.
	 */
	public int addAndInitializeResource (IQuery connector,
		String resourceName,
		IParameters params)
	{
		// Add the resource
		int id = addResource(connector, resourceName);
		// Set the parameters and open 
		connector.setNoQueryThreshold(noQueryThreshold);
		connector.setRootDirectory(rootDir); // Before open()
		connector.setParameters(params);
		if (( srcLoc != null ) && ( trgLoc != null )) {
			connector.setLanguages(srcLoc, trgLoc);
		}
		connector.open();
		for ( String name : attributes.keySet() ) {
			connector.setAttribute(name, attributes.get(name));
		}
		if ( connector instanceof ITMQuery ) {
			((ITMQuery)connector).setThreshold(threshold);
			((ITMQuery)connector).setMaximumHits(maxHits);
		}
		return id;
	}
	
	/**
	 * Creates a translation resource and its parameters from their class names,
	 * adds it to the manager and initializes it with the 
	 * current source and target language of this manager, as well as any
	 * attributes that is set, and the current threshold and maximum hits if it is relevant.
	 * @param connectorClass the name of the class for the connector.
	 * @param resourceName the name of the translation resource (can be null).
	 * @param connectorParams connector parameters stored in a string.
	 * @return The identifier for the added translation resource. This identifier
	 * can be used later to access specifically the added translation resource.
	 * @throws RuntimeException if an error occurs.
	 */
	public int addAndInitializeResource (String connectorClass,
		String resourceName,
		String connectorParams)
	{
		IQuery conn;
		try {
			conn = (IQuery)Class.forName(connectorClass).newInstance();
		}
		catch ( InstantiationException e ) {
			throw new OkapiException("Error creating connector.", e);
		}
		catch ( IllegalAccessException e ) {
			throw new OkapiException("Error creating connector.", e);
		}
		catch ( ClassNotFoundException e ) {
			throw new OkapiException("Error creating connector.", e);
		}
		IParameters tmParams = conn.getParameters();
		if ( tmParams != null ) { // Set the parameters only if the connector take some
			tmParams.fromString(connectorParams);
		}
		return addAndInitializeResource(conn, ((resourceName==null) ? conn.getName() : resourceName), tmParams);
	}
	
	/**
	 * Creates a translation resource and its parameters from their class names,
	 * adds it to the manager and initializes it with the 
	 * current source and target language of this manager, as well as any
	 * attributes that is set, and the current threshold and maximum hits if it is relevant.
	 * @param connectorClass the name of the class for the connector.
	 * @param resourceName the name of the translation resource (can be null).
	 * @param loader class loader from which the connector class must be loaded
	 * @param connectorParams connector parameters stored in a string.
	 * @return The identifier for the added translation resource. This identifier
	 * can be used later to access specifically the added translation resource.
	 * @throws RuntimeException if an error occurs.
	 */
	public int addAndInitializeResource (String connectorClass,
		String resourceName,
		ClassLoader loader,
		String connectorParams)
	{
		IQuery conn;
		try {
			conn = (IQuery)Class.forName(connectorClass, true, loader).newInstance();
		}
		catch ( InstantiationException e ) {
			throw new OkapiException("Error creating connector.", e);
		}
		catch ( IllegalAccessException e ) {
			throw new OkapiException("Error creating connector.", e);
		}
		catch ( ClassNotFoundException e ) {
			throw new OkapiException("Error creating connector.", e);
		}
		IParameters tmParams = conn.getParameters();
		if ( tmParams != null ) { // Set the parameters only if the connector take some
			tmParams.fromString(connectorParams);
		}
		return addAndInitializeResource(conn, ((resourceName==null) ? conn.getName() : resourceName), tmParams);
	}
	
	/**
	 * Enables or disables a given translation resource.
	 * @param resourceId ID of the translation resource to enable or disable.
	 * @param enabled True to enable the resource, false to disable it.
	 */
	public void setEnabled (int resourceId,
		boolean enabled)
	{
		resList.get(resourceId).enabled = enabled;
	}
	
	/**
	 * Removes a given translation resource.
	 * @param resourceId ID of the translation resource to remove.
	 */
	public void remove (int resourceId) {
		resList.remove(resourceId);
	}
	
	/**
	 * Gets the IQuery interface for a given translation resource.
	 * @param resourceId ID of the translation resource to lookup.
	 * @return The IQuery interface for the given translation resource, or null
	 * if the ID is not found.
	 */
	public IQuery getInterface (int resourceId) {
		return resList.get(resourceId).query;
	}
	
	/**
	 * Gets the configuration data for a given translation resource.
	 * @param resourceId ID of the translation resource to lookup.
	 * @return A ResourceItem object that contains the configuration data for 
	 * the given translation resource, or null if the ID is not found.
	 */
	public ResourceItem getResource (int resourceId) {
		return resList.get(resourceId);
	}
	
	/**
	 * Gets the name for a given translation resource.
	 * @param resourceId ID of the translation resource to lookup.
	 * @return The name of the given translation resource, or null
	 * if the ID is not found.
	 */
	public String getName (int resourceId) {
		return resList.get(resourceId).name;
	}
	
	/**
	 * Gets the configuration data for all the translation resources in this manager.
	 * @return A map of ID+ResourceItem objects pairs that contains the
	 * configuration data for each translation resource. the map can be empty.
	 */
	public Map<Integer, ResourceItem> getResources () {
		return resList;
	}

	/**
	 * Closes all translation resources in this manager.
	 */
	public void close () {
		for ( ResourceItem ri : resList.values() ) {
			ri.query.close();
		}
	}

	/**
	 * Gets the list of all hit results of the last query.
	 * @return A list of all hit results of the last query.
	 */
	public List<QueryResult> getResults () {
		return results;
	}
	
	/**
	 * Resets the current result to the first one if there is one.
	 */
	public void rewind () {
		if ( results.size() > 0 ) current = 0;
		else current = -1;
	}
	
	/**
	 * Indicates of there is a hit available.
	 * @return True if a hit is available, false if not.
	 */
	public boolean hasNext() {
		if ( current >= results.size() ) {
			current = -1;
		}
		return (current > -1);
	}

	/**
	 * Gets the next hit for the last query.
	 * @return A QueryResult object that holds the source and target text of
	 * the hit, or null if there is no more hit.
	 */
	public QueryResult next () {
		if (( current > -1 ) && ( current < results.size() )) {
			current++;
			return results.get(current-1);
		}
		current = -1;
		return null;
	}

	/**
	 * Queries all enabled translation resources for a given plain text. 
	 * @param plainText The text to query.
	 * @return The number of hits for the given query.
	 */
	public int query (String plainText) {
		results.clear();
		ResourceItem ri;
		for ( int id : resList.keySet() ) {
			ri = resList.get(id);
			if ( !ri.enabled ) continue; // Skip disabled entries
			if ( ri.query.query(plainText) > 0 ) {
				QueryResult res = null;
				while ( ri.query.hasNext() ) {
					res = ri.query.next();
					res.connectorId = id;
					if ( res.getCombinedScore() < threshold ) break; // Weed out MT if needed
					results.add(res);
				}
			}
		}
		
		// remove duplicates based on QueryResult.equals
		// remove duplicates also sorts in ranked order
		results = QueryUtil.removeDuplicates(results); 
		
		if ( results.size() > 0 ) current = 0;		
		return results.size();
	}

	/**
	 * Queries all enabled translation resources for a given text fragment. 
	 * @param text The text to query.
	 * @return The number of hits for the given query.
	 */
	public int query (TextFragment text) {
		results.clear();
		ResourceItem ri;
		for ( int id : resList.keySet() ) {
			ri = resList.get(id);
			if ( !ri.enabled ) continue; // Skip disabled entries
			if ( ri.query.query(text) > 0 ) {
				QueryResult res = null;
				while ( ri.query.hasNext() ) {
					res = ri.query.next();
					if ( res.getCombinedScore() < threshold ) break;
					res.connectorId = id;
					results.add(res);
				}
			}
		}
		
		// Remove duplicates based on QueryResult.equals
		// remove duplicates also sorts in ranked order
		results = QueryUtil.removeDuplicates(results);
		
		if ( results.size() > 0 ) current = 0;		
		return results.size();
	}

	/**
	 * Sets an attribute for this manager and all translation resources in
	 * this manager.
	 * @param name name of the attribute.
	 * @param value Value of the attribute.
	 */
	public void setAttribute (String name,
		String value)
	{
		attributes.put(name, value);
		for ( ResourceItem ri : resList.values() ) {
			ri.query.setAttribute(name, value);
		}
	}
	
	/**
	 * Removes a given attribute from this manager and all translation
	 * resources in this manager.
	 * @param name The name of the attribute to remove.
	 */
	public void removeAttribute (String name) {
		attributes.remove(name);
		for ( ResourceItem ri : resList.values() ) {
			ri.query.removeAttribute(name);
		}
	}
	
	/**
	 * Removes all attributes from this manager and all the translation
	 * resources in this manager.
	 */
	public void clearAttributes () {
		attributes.clear();
		for ( ResourceItem ri : resList.values() ) {
			ri.query.clearAttributes();
		}
	}
	
	/**
	 * Sets the source and target locales for this manager and for all
	 * translation resources in this manager.
	 * @param sourceLocale Code of the source locale to set.
	 * @param targetLocale Code of the target locale to set.
	 */
	public void setLanguages (LocaleId sourceLocale,
		LocaleId targetLocale)
	{
		srcLoc = sourceLocale;
		trgLoc = targetLocale;
		for ( ResourceItem ri : resList.values() ) {
			ri.query.setLanguages(srcLoc, trgLoc);
		}
	}

	/**
	 * Gets the current source locale for this manager.
	 * @return Code of the current source locale for this manager.
	 */
	public LocaleId getSourceLanguage () {
		return srcLoc;
	}

	/**
	 * Gets the current target locale for this manager.
	 * @return Code of the current target locale for this manager.
	 */
	public LocaleId getTargetLanguage () {
		return trgLoc;
	}

	/**
	 * Sets the threshold for this query manager and all the relevant
	 * translation resources it holds.
	 * @param value the threshold value to set.
	 */
	public void setThreshold (int value) {
		threshold = value;
		for ( ResourceItem ri : resList.values() ) {
			if ( ri.query instanceof ITMQuery ) {
				((ITMQuery)ri.query).setThreshold(threshold);
			}
		}
	}
	
	/**
	 * Sets the maximum number of hits to return for this query manager
	 * and all the relevant translation resources it holds.
	 * @param max the maximum value to set.
	 */
	public void setMaximumHits (int max) {
		maxHits = max;
		for ( ResourceItem ri : resList.values() ) {
			if ( ri.query instanceof ITMQuery ) {
				((ITMQuery)ri.query).setMaximumHits(maxHits);
			}
		}
	}

	/**
	 * Sets the root directory for this query manager
	 * and all translation resources it holds.
	 * @param rootDir the root directory.
	 */
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
		for ( ResourceItem ri : resList.values() ) {
			ri.query.setRootDirectory(this.rootDir);
		}
	}

	/**
	 * Sets the no-query threshold for this query manager.
	 * and all the translation resources it holds.
	 * @param noQueryThreshold the value of the no-query threshold (between 0 and 101).
	 * Use 101 to always allow the query.
	 */
	public void setNoQueryThreshold (int noQueryThreshold) {
		this.noQueryThreshold = noQueryThreshold;
		for ( ResourceItem ri : resList.values() ) {
			ri.query.setNoQueryThreshold(this.noQueryThreshold);
		}
	}
	
	/**
	 * Sets the options for performing the leverage.
	 * @param thresholdToFill if the first match has a score equal or above this value,
	 * the target text of the match is placed in the target content. To avoid any filling of
	 * the target: simply use a high value (e.g. <code>Integer.MAX_VALUE</code>).
	 * @param fillIfTargetIsEmpty true to fill the target only if its content is currently empty.
	 * @param fillIfTargetIsSameAsSource true to fill the target if fillIfTargetIsEmpty is true and
	 * the source and target content are the same. 
	 * @param downgradeIdenticalBestMatches true to reduce the score of best matches when
	 * they are identical.
	 * @param targetPrefix A prefix to place at the front of the candidate target if it is 
	 * leveraged into the text unit. Use null to not set a prefix.
	 * @param thresholdToPrefix if a target prefix is defined and the score is equal or below this
	 * threshold the prefix is added. This parameter is ignored if the target prefix is null.
	 * @param copySourceOnNoText true to copy the source content for the target segments that have 
	 * no text (but they may have codes and/or white spaces).
	 */
	public void setOptions (int thresholdToFill,
		boolean fillIfTargetIsEmpty,
		boolean fillIfTargetIsSameAsSource,
		boolean downgradeIdenticalBestMatches,
		String targetPrefix,
		int thresholdToPrefix,
		boolean copySourceOnNoText)
	{
		this.thresholdToFill = thresholdToFill;
		this.keepIfNotEmpty = !fillIfTargetIsEmpty;
		this.keepIfTargetSameAsSource = !fillIfTargetIsSameAsSource;
		this.downgradeIdenticalBestMatches = downgradeIdenticalBestMatches;
		this.targetPrefix = targetPrefix;
		this.thresholdToPrefix = thresholdToPrefix;
		this.copySourceOnNoText = copySourceOnNoText;
	}
		
	/**
	 * Leverages a text unit (segmented or not) based on the current settings.
	 * Any options or attributes needed must be set before calling this method.
	 * @param tu the text unit to leverage.
	 * @see #setAttribute(String, String)
	 * @see #setLanguages(LocaleId, LocaleId)
	 * @see #setMaximumHits(int)
	 * @see #setNoQueryThreshold(int)
	 * @see #setOptions(int, boolean, boolean, boolean, String, int, boolean)
	 * @see #setRootDirectory(String)
	 * @see #setThreshold(int)
	 */
	public void leverage (ITextUnit tu) {
		if ( !tu.isTranslatable() ) {
			return;
		}
		
		totalSegments += tu.getSource().getSegments().count();
		
		// Query each translation resource
		for ( int id : resList.keySet() ) {
			ResourceItem ri = resList.get(id);
			if ( !ri.enabled ) continue; // Skip disabled entries
			ri.query.leverage(tu);
		}
		
		// Sort annotations added across IQuery.leverage calls
		// and fill in best matching target if needed
		AltTranslationsAnnotation altTrans = null;
		AltTranslation bestMatch = null;
		
		for ( LocaleId loc : tu.getTargetLocales() ) {
			
			TextContainer tc = tu.getTarget(loc);
			if ( tc == null ) continue;
			
			// Check for entries without text
			if ( copySourceOnNoText ) {
				for ( Segment srcSeg : tu.getSourceSegments() ) {
					if ( !srcSeg.text.hasText(false) ) {
						Segment trgSeg = tc.getSegments().get(srcSeg.id);
						if ( trgSeg != null ) {
							trgSeg.text = srcSeg.text.clone();
						}
					}
				}
			}
			
			// Check target container first
			altTrans = tc.getAnnotation(AltTranslationsAnnotation.class);
			if ( altTrans != null ) {
				// Sort the results
				altTrans.sort();
				// Down-grade identical best matches if requested
				if ( downgradeIdenticalBestMatches ) {
					altTrans.downgradeIdenticalBestMatches(false, threshold);
				}
				// Check if we do have a best match
				if ( (bestMatch = altTrans.getFirst()) != null ) {
					// Update the statistics
					if ( bestMatch.getCombinedScore() >= 100 ) exactBestMatches++;
					else if ( bestMatch.getCombinedScore() > 0 ) fuzzyBestMatches++;
					// Do we need to fill the target?
					if ( bestMatch.getCombinedScore() >= thresholdToFill ) {
						// Alternate translation content is expected to always be un-segmented: We can use getFirstContent()
						// Check if we need to skip the leveraging
						boolean leverage = true;
						if ( keepIfNotEmpty && !tc.isEmpty() ) {
							if ( keepIfTargetSameAsSource ) {
								if ( tu.getSource().compareTo(tc, true) != 0 ) {
									leverage = false; // Target is different that source
								}
							}
							else {
								leverage = false;
							}
						}
						// If it's OK to leverage do it
						if ( leverage ) {
							// If a prefix is defined and the score equal or below the given threshold: we add it
							if (( targetPrefix != null ) && ( bestMatch.getCombinedScore() <= thresholdToPrefix )) {
								TextFragment tf = new TextFragment(targetPrefix + bestMatch.getTarget().getFirstContent().getCodedText(),
									bestMatch.getTarget().getFirstContent().getClonedCodes());
								tu.setTargetContent(getTargetLanguage(), tf);
							}
							else { // Otherwise we just use the found content
								tu.setTargetContent(getTargetLanguage(), bestMatch.getTarget().getFirstContent());
							}
							// We have leveraged an un-segmented target: do we need to un-segment the source?
							if ( tu.getSource().hasBeenSegmented() ) {
								tu.getSource().joinAll();
							}
						}
					}
				}
			}

			// Then check each target segment
			// A customized leverage method may fill both container and segments, so we have to check both
			for ( Segment ts : tc.getSegments() ) {
				altTrans = ts.getAnnotation(AltTranslationsAnnotation.class);
				if ( altTrans != null ) {
					altTrans.sort();
					// Down-grade identical best matches if requested
					if ( downgradeIdenticalBestMatches ) {
						altTrans.downgradeIdenticalBestMatches(false, threshold);
					}
					// Check if we have a best match
					if ( (bestMatch = altTrans.getFirst()) != null ) {
						// Update the statistics
						if ( bestMatch.getCombinedScore() >= 100 ) exactBestMatches++;
						else if ( bestMatch.getCombinedScore() > 0 ) fuzzyBestMatches++;
						// Do we need to fill the target?
						if ( bestMatch.getCombinedScore() >= thresholdToFill ) {
							// Check condition for overwriting existing target
							Segment ss = tu.getSourceSegment(ts.id, false);
							if ( ss == null ) continue;
							boolean leverage = true;
							if ( keepIfNotEmpty && !ts.text.isEmpty() ) {
								if ( keepIfTargetSameAsSource ) {
									if ( ts.text.compareTo(ss.text, true) != 0 ) {
										leverage = false;
									}
								}
								else {
									leverage = false;
								}
							}
							if ( leverage ) {
								// Alternate translation content is expected to always be un-segmented: We can use getFirstContent()
								// If a prefix is defined and the score equal or below the given threshold: we add it
								if (( targetPrefix != null ) && ( bestMatch.getCombinedScore() <= thresholdToPrefix )) {
									ts.text = new TextFragment(targetPrefix + bestMatch.getTarget().getFirstContent().getCodedText(),
										bestMatch.getTarget().getFirstContent().getClonedCodes());
								}
								else { // Otherwise we just use the found content
									ts.text = bestMatch.getTarget().getFirstContent();	
								}
							}
						}
					}
				}
			}

		}
	}
		
//	/**
//	 * Adjusts the inline codes of a new text fragment based on an original one.
//	 * @param oriSrc the original source text fragment.
//	 * @param newSrc the new source text fragment.
//	 * @param newTrg the new target text fragment (this is the fragment that will be adjusted).
//	 * @param score the score for the match: >=100 means no adjustment is made.
//	 * @param parent the parent text unit (used for error information only)
//	 * @return the newTrg parameter adjusted
//	 */
//	// To unified with TextUnitUtil equivalent method
//	public TextFragment adjustNewFragment (TextFragment oriSrc,
//		TextFragment newSrc,
//		TextFragment newTrg,
//		int score,
//		TextUnit parent)
//	{
//		List<Code> newCodes = newTrg.getCodes();
//		List<Code> oriCodes = oriSrc.getCodes();
//		
//		// If score is 100 or more: no reason to adjust anything: use the target as-it
//		// This allows targets with only code differences to be used as-it
//		boolean needAdjustment = false;
//		if ( score >= 100 ) {
//			// Check if we need to adjust even if it's ann exact match
//			// when we have empty codes in the new target
//			for ( Code code : newCodes ) {
//				if ( !code.hasData() ) {
//					needAdjustment = true;
//					break;
//				}
//			}
//			// Or reference in the original
//			if ( !needAdjustment ) {
//				for ( Code code : oriCodes ) {
//					if ( code.hasReference() ) {
//						needAdjustment = true;
//						break;
//					}
//				}
//			}
//			if ( !needAdjustment ) {
//				return newTrg;
//			}
//		}
//		// If both new and original have no code, return the new fragment
//		if ( !newTrg.hasCode() && !oriSrc.hasCode() ) {
//			return newTrg;
//		}
//		
//		
//		// If the codes of the original sources and the matched one are the same: no need to adjust
//		if ( !needAdjustment && oriCodes.toString().equals(newSrc.getCodes().toString()) ) {
//			return newTrg;
//		}
//
//		// Else: try to adjust
//		int[] oriIndices = new int[oriCodes.size()];
//		for ( int i=0; i<oriIndices.length; i++ ) oriIndices[i] = i;
//		
//		int done = 0;
//		Code newCode, oriCode;
//
//		for ( int i=0; i<newCodes.size(); i++ ) {
//			newCode = newCodes.get(i);
//			newCode.setOuterData(null); // Remove XLIFF outer codes if needed
//
//			// Get the data from the original code (match on id)
//			oriCode = null;
//			for ( int j=0; j<oriIndices.length; j++ ) {
//				if ( oriIndices[j] == -1) continue; // Used already
//				//if (( oriCodes.get(oriIndices[j]).getId() == newCode.getId() ))
//					//TOFIX && ( oriCodes.get(oriIndices[j]).getTagType() == newCode.getTagType() ))
//				if ( oriCodes.get(oriIndices[j]).getTagType() == newCode.getTagType() ) {
//					//oriIndex = oriIndices[j];
//					oriCode = oriCodes.get(oriIndices[j]);
//					oriIndices[j] = -1;
//					done++;
//					break;
//				}
//			}
//			
//			if ( oriCode == null ) { // Not found in original (extra in target)
//				if (( newCode.getData() == null )
//					|| ( newCode.getData().length() == 0 )) {
//					// Leave it like that
//					logger.warn("The extra target code id='{}' does not have corresponding data (item id='{}', name='{}')",
//						newCode.getId(), parent.getId(), (parent.getName()==null ? "" : parent.getName()));
//				}
//				// Else: This is a new code: keep it
//			}
//			else { // A code with same ID existed in the original
//				// Get the data from the original
//				newCode.setData(oriCode.getData());
//				newCode.setOuterData(oriCode.getOuterData());
//				newCode.setReferenceFlag(oriCode.hasReference());
//			}
//		}
//		
//		// If needed, check for missing codes in new fragment
//		if ( oriCodes.size() > done ) {
//			// Any index > -1 in source means it was was deleted in target
//			for ( int i=0; i<oriIndices.length; i++ ) {
//				if ( oriIndices[i] != -1 ) {
//					Code code = oriCodes.get(oriIndices[i]);
//					if ( !code.isDeleteable() ) {
//						logger.warn("The code id='{}' ({}) is missing in target (item id='{}', name='{}')",
//							code.getId(), code.getData(), parent.getId(), (parent.getName()==null ? "" : parent.getName()));
//						logger.info("Source='{}'\nTarget='{}'", oriSrc.toText(), newTrg.toText());
//					}
//				}
//			}
//		}
//		
//		return newTrg;
//	}
	
	/**
	 * Resets the counters used to calculate the number of segments leveraged.
	 * @see #getExactBestMatches()
	 * @see #getTotalSegments()
	 */
	public void resetCounters () {
		totalSegments = 0;
		exactBestMatches = 0;
		fuzzyBestMatches = 0;
	}
	
	/**
	 * Gets the total number of segments processed since the last call to {@link #resetCounters()}.
	 * @return the total number of segment processed.
	 */
	public int getTotalSegments () {
		return totalSegments;
	}
	
	/**
	 * Gets the number of best matches that are exact (100%) since the last call to {@link #resetCounters()}.
	 * @return the number of best matches that are exact.
	 */
	public int getExactBestMatches () {
		return exactBestMatches;
	}

	/**
	 * Gets the number of best matches that are fuzzy (less that 100%, more than 0%) since the last call
	 * to {@link #resetCounters()}.
	 * @return the number of best matches that are fuzzy.
	 */
	public int getFuzzyBestMatches () {
		return fuzzyBestMatches;
	}

}
