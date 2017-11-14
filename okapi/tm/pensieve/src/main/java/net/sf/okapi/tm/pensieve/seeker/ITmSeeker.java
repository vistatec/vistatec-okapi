/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.tm.pensieve.seeker;

import java.util.List;

import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.tm.pensieve.common.Metadata;
import net.sf.okapi.tm.pensieve.common.TmHit;

/**
 * Used to query the TM.
 * 
 * @author HaslamJD
 * @author HARGRAVEJE
 */
public interface ITmSeeker extends AutoCloseable {

	/**
	 * Get a list of exact matches for a given text fragment, taking inline codes in account.
	 * 
	 * @param query
	 *            the fragment to search for
	 * @param metadata
	 *            the metadata attributes to also match against, null for no metadata.
	 * @return a list of exact matches
	 * @throws net.sf.okapi.common.exceptions.OkapiIOException
	 *             if the search cannot be completed due to I/O problems
	 */
	List<TmHit> searchExact(TextFragment query, Metadata metadata);

	/**
	 * 
	 * Get a list of fuzzy matches for a given text fragment, taking inline codes in account.
	 * 
	 * @param query
	 *            the fragment to search for.
	 * @param threshold
	 *            the minimal score value to return.
	 * @param maxHits
	 *            the max number of hits returned.
	 * @param metadata
	 *            the metadata attributes to also match against, null for no metadata.
	 * @return a list of exact or fuzzy matches.
	 * @throws net.sf.okapi.common.exceptions.OkapiIOException
	 *             if the search cannot be completed do to I/O problems
	 */
	List<TmHit> searchFuzzy(TextFragment query, int threshold, int maxHits, Metadata metadata);

	/**
	 * 
	 * Get a list of concordance matches (without position offsets) for a given text string. Simple condordance does not
	 * allow codes.
	 * 
	 * @param query
	 *            the string to search for.
	 * @param threshold
	 *            the minimal score value to return.
	 * @param maxHits
	 *            the max number of hits returned
	 * @param metadata
	 *            the metadata attributes to also match against, null for no metadata.
	 * @return a list of exact or fuzzy concordance marches.
	 * @throws net.sf.okapi.common.exceptions.OkapiIOException
	 *             if the search cannot be completed do to I/O problems
	 */
	List<TmHit> searchSimpleConcordance(String query, int threshold, int maxHits, Metadata metadata);

	/**
	 * Close the searcher
	 */
	void close();
}
