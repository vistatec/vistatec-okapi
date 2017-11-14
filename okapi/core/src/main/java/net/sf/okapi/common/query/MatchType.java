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

package net.sf.okapi.common.query;

/**
 * Enumeration of the different match types possible for an alternate translation entry.
 * <p>
 * <b>Matches are in ranked order from highest to lowest. Please maintain ranked order when adding new entries.</b>
 */
public enum MatchType {	
	/**
	 * final, approved human translation.
	 */
	ACCEPTED,
	
	/**
	 * Improved translation edited by a human.
	 */
	HUMAN_RECOMMENDED,
	
	/**
	 * EXACT and matches a unique id
	 */
	EXACT_UNIQUE_ID,

	/**
	 * EXACT and comes from the preceding version of the same document 
	 * (i.e., if v4 is leveraged this match must come from v3, not v2 or v1!!).
	 */
	EXACT_PREVIOUS_VERSION,

	/**
	 * EXACT and a small number of segments before and/or after.
	 */
	EXACT_LOCAL_CONTEXT,

	/**
	 * EXACT and comes from a repeated segment in the same document.
	 */
	EXACT_DOCUMENT_CONTEXT,

	/**
	 * EXACT and the structural type of the segment (title, paragraph, list element etc..)
	 */
	EXACT_STRUCTURAL,
	
	/**
	 * Matches text and codes exactly.
	 */
	EXACT,

	/**
	 * EXACT_TEXT_ONLY and matches with a unique id
	 */
	EXACT_TEXT_ONLY_UNIQUE_ID,
	
	/**
	 * EXACT_TEXT_ONLY and comes from a previous version of the same document
	 */
	EXACT_TEXT_ONLY_PREVIOUS_VERSION,
	
	/**
	 * Matches text exactly, but there is a difference in one or more codes and/or whitespace
	 */
	EXACT_TEXT_ONLY,
	
	/**
	 * Matches text and codes exactly, but only after the result 
	 * of some automated repair (i.e., number replacement, code repair, 
	 * capitalization, punctuation etc..)
	 */
	EXACT_REPAIRED,

	/**
	 * Matches FUZZY with a unique id
	 */
	FUZZY_UNIQUE_ID,
	
	/**
	 * FUZZY and comes from a previous version of the same document
	 */
	FUZZY_PREVIOUS_VERSION,
	
	/**
	 * Matches both text and/or codes partially.
	 */
	FUZZY,
	
	/**
	 * Matches both text and/or codes partially and some automated repair 
	 * (i.e., number replacement, code repair, capitalization, punctuation etc..) 
	 * was applied to the target
	 */
	FUZZY_REPAIRED,

	/**
	 * Matches assembled from phrases in the TM or other resource.
	 */
	PHRASE_ASSEMBLED,

	/**
	 * Indicates a translation coming from an MT engine.
	 */
	MT,

	/**
	 * TM concordance or phrase match (usually a word or term only)
	 */
	CONCORDANCE,

	/**
	 * Unknown match type. Used as default value only - should always be updated
	 * to a known match type. A UNKOWN type always sorts below all other
	 * matches. Make sure this type is the last in the list.
	 */
	UKNOWN
}
