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

package net.sf.okapi.tm.pensieve.common;

import net.sf.okapi.common.HashCodeUtil;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;

/**
 * Represents a TM Hit. This stores a reference to the TranslationUnit and its
 * score and {@link MatchType}
 * 
 * @author HaslamJD
 * @author HARGRAVEJE
 */
public class TmHit implements Comparable<TmHit> {
	private TranslationUnit tu;
	private float score;
	private MatchType matchType;
	private boolean codeMismatch;
	private int docId;

	/**
	 * Default constructor which sets the MatchType to NONE. 
	 */
	public TmHit() {
		setMatchType(MatchType.UKNOWN);
		setCodeMismatch(false);
	}

	/**
	 * Create a new TmHit.
	 * @param tu
	 * @param matchType
	 * @param score
	 */
	public TmHit(TranslationUnit tu, MatchType matchType, float score) {
		setTu(tu);
		setMatchType(matchType);
		setScore(score);
		setCodeMismatch(false);
	}

	/**
	 * Get the TmHit's score. 
	 * @return the score as a float normalized between 0 and 1.0
	 */
	public float getScore() {
		return score;
	}

	/**
	 * Set the TmHit's score.
	 * @param score
	 */
	public void setScore(Float score) {
		this.score = score;
	}

	/**
	 * Get the TmHit's {@link TranslationUnit}
	 * @return a {@link TranslationUnit}
	 */
	public TranslationUnit getTu() {
		return tu;
	}

	/**
	 * Set the TmHit's {@link TranslationUnit}
	 * @param tu
	 */
	public void setTu(TranslationUnit tu) {
		this.tu = tu;
	}

	/**
	 * Set the Tmhit's {@link MatchType}
	 * @param matchType
	 */
	public void setMatchType(MatchType matchType) {
		this.matchType = matchType;
	}

	/**
	 * Get the Tmhit's {@link MatchType}
	 * @return a {@link MatchType}
	 */
	public MatchType getMatchType() {
		return matchType;
	}

	/**
	 * Set true of the {@link Code}s between the TmHit and query {@link TextFragment} are different.
	 * @param codeMismatch
	 */
	public void setCodeMismatch(boolean codeMismatch) {
		this.codeMismatch = codeMismatch;
	}

	/**
	 * Is there a difference between the {@link Code}s of the TmHit and the query {@link TextFragment}?  
	 * @return true if there is a code difference.
	 */
	public boolean isCodeMismatch() {
		return codeMismatch;
	}

	/**
	 * Set the document id for the TmHit. This is usually the Lucene document id.  
	 * @param docId
	 */
	public void setDocId(int docId) {
		this.docId = docId;
	}

	/**
	 * Get the document id for the TmHit. This is usually the Lucene document id.
	 * @return integer specifying the TmHit's document id.
	 */
	public int getDocId() {
		return docId;
	}

	/**
	 * This method implements a three way sort on (1) MatchType (2) score (3)
	 * source string. MatchType is the primary key, score secondary and source
	 * string tertiary.
	 * 
	 * @param other - the TmHit we are comparing against.
	 */
	public int compareTo(TmHit other) {
		final int EQUAL = 0;

		if (this == other)
			return EQUAL;

		String thisSource = this.tu.getSource().getContent().toText();
		String otherSource = other.tu.getSource().getContent().toText();
		
		// only sort by match type if this or other is some kind of exact match
		int comparison;
		if ( isExact(this.matchType) || isExact(other.matchType) ) {		
			// compare MatchType
			comparison = this.matchType.compareTo(other.getMatchType());
			if (comparison != EQUAL)
				return comparison;
		}

		// compare score
		comparison = Float.compare(this.score, other.getScore());
		if (comparison != EQUAL)
			return comparison * -1;  // we want to reverse the normal score sort

		// compare source strings with codes
		comparison = thisSource.compareTo(otherSource);
		if (comparison != EQUAL)
			return comparison;

		// default
		return EQUAL;
	}

	/**
	 * Define equality of state.
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof TmHit))
			return false;

		TmHit otherHit = (TmHit) other;
		return (this.matchType == otherHit.getMatchType())
				&& (this.tu.getSource().getContent().toText().equals(otherHit
						.getTu().getSource().getContent().toText()))
				&& (this.tu.getTarget().getContent().toText().equals(otherHit
						.getTu().getTarget().getContent().toText()));
	}

	/**
	 * A class that overrides equals must also override hashCode.
	 */
	@Override
	public int hashCode() {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, matchType);
		result = HashCodeUtil.hash(result, tu.getSource().getContent()
				.toText());
		result = HashCodeUtil.hash(result, tu.getTarget().getContent()
				.toText());
		return result;
	}
	
	private boolean isExact (MatchType type) {
		if ( type.ordinal() <= MatchType.EXACT_REPAIRED.ordinal() ) {
			return true;
		}
		return false;
	}
}
