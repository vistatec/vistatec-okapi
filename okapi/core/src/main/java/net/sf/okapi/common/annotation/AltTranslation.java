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

package net.sf.okapi.common.annotation;

import java.util.UUID;

import net.sf.okapi.common.HashCodeUtil;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

/**
 * Stores the data representing an alternate translation.
 * <p>This object is used with the {@link AltTranslationsAnnotation} annotation.
 * <p>Note that the content of the source and target is always un-segmented.
 */
public class AltTranslation implements Comparable<AltTranslation> {
	/**
	 * Origin string indicating that the match is coming from a source input document
	 * (e.g. alt-trans elements in XLIFF). 
	 */
	public static final String ORIGIN_SOURCEDOC = "SourceDoc";

	LocaleId srcLocId;
	LocaleId trgLocId;
	ITextUnit tu;
	MatchType type;
	int combinedScore;
	int fuzzyScore;
	int qualityScore;
	String origin;
	boolean fromOriginal;
	String engine;
	XLIFFTool tool;
	String alttranstype;

	/**
	 * Creates a new AltTranslation object.
	 * 
	 * @param sourceLocId
	 *            the locale of the source.
	 * @param targetLocId
	 *            the locale of the target.
	 * @param originalSource
	 *            the original source content.
	 * @param alternateSource
	 *            the source content corresponding to the alternate translation (can be null).
	 * @param alternateTarget
	 *            the content of alternate translation.
	 * @param type
	 *            the type of alternate translation.
	 * @param combinedScore
	 *            the combined score for this alternate translation (must be between 0 and 100).
	 * @param origin
	 *            an optional identifier for the origin of this alternate translation.
	 */
	public AltTranslation (LocaleId sourceLocId,
		LocaleId targetLocId,
		TextFragment originalSource,
		TextFragment alternateSource,
		TextFragment alternateTarget,
		MatchType type,
		int combinedScore,
		String origin)
	{
		// by default fuzzyScore is set the same as combinedScore which is true in most cases
		this(sourceLocId, targetLocId, originalSource, alternateSource, alternateTarget, type,
				combinedScore, origin, combinedScore, QueryResult.QUALITY_UNDEFINED);
	}

	/**
	 * Creates a new AltTranslation object.
	 * 
	 * @param sourceLocId
	 *            the locale of the source.
	 * @param targetLocId
	 *            the locale of the target.
	 * @param originalSource
	 *            the original source content.
	 * @param alternateSource
	 *            the source content corresponding to the alternate translation (can be null).
	 * @param alternateTarget
	 *            the content of alternate translation.
	 * @param type
	 *            the type of alternate translation.
	 * @param combinedScore
	 *            the combined score for this alternate translation (must be between 0 and 100).
	 *            Combined score is usually a combination of fuzzyScore and qualityScore.
	 * @param origin
	 *            an optional identifier for the origin of this alternate translation.
	 * @param fuzzyScore - fuzzy score (string distance) between the original source and alternate translation source 
	 * @param qualityScore - optional quality score from the TM or MT engine
	 */
	public AltTranslation (LocaleId sourceLocId,
		LocaleId targetLocId,
		TextFragment originalSource,
		TextFragment alternateSource,
		TextFragment alternateTarget,
		MatchType type,
		int combinedScore,
		String origin, 
		int fuzzyScore, 
		int qualityScore)
	{
		this.srcLocId = sourceLocId;
		this.trgLocId = targetLocId;
		this.type = type;
		this.combinedScore = combinedScore;
		this.origin = origin;
		this.qualityScore = qualityScore;
		this.fuzzyScore = fuzzyScore;
		
		// if there is no alttranstype attribute the default is proposal
		this.alttranstype = "proposal";

		tu = new TextUnit(UUID.randomUUID().toString());
		if ( alternateSource != null ) {
			tu.setSourceContent(alternateSource);
		}

		// TODO: copy code-content from original source to alternate target if necessary
		// TODO: the magic should go here
		if ( alternateTarget != null ) {
			tu.setTargetContent(targetLocId, alternateTarget);
		}
	}

	/**
	 * Gets the target content of this entry.
	 * 
	 * @return the target content of this entry.
	 */
	public TextContainer getTarget () {
		return tu.getTarget(trgLocId);
	}

	/**
	 * Sets the target parts of this alternate translation.
	 * 
	 * @param targetLocId
	 *            the target locale.
	 * @param alternateTarget
	 *            the content of the alternate translation.
	 */
	public void setTarget (LocaleId targetLocId,
		TextFragment alternateTarget)
	{
		this.trgLocId = targetLocId;
		tu.setTargetContent(targetLocId, alternateTarget);
	}

	/**
	 * Gets the source locale for this entry.
	 * 
	 * @return the source locale for this entry.
	 */
	public LocaleId getSourceLocale () {
		return srcLocId;
	}

	/**
	 * Gets the target locale for this entry.
	 * 
	 * @return the target locale for this entry.
	 */
	public LocaleId getTargetLocale () {
		return trgLocId;
	}

	/**
	 * Gets the source content of this entry (can be empty)
	 * If the result is empty, it means the source is the same as the source of the object (segment or text container)
	 * is attached to.
	 * @return the source content of this entry (can be empty)
	 */
	public TextContainer getSource () {
		return tu.getSource();
	}

	/**
	 * Gets the combined score for this entry.
	 * @return the combined score for this entry.
	 */
	public int getCombinedScore () {
		return combinedScore;
	}

	/**
	 * Gets the origin for this entry (can be null).
	 * @return the origin for this entry, or null if none is defined.
	 */
	public String getOrigin () {
		return origin;
	}

	/**
	 * Gets the engine for this entry (can be null).
	 * @return the engine information for this entry, or null if none is defined.
	 */
	public String getEngine () {
		return engine;
	}

	/**
	 * Gets the text unit for this entry.
	 * @return the text unit for this entry.
	 */
	public ITextUnit getEntry () {
		return tu;
	}

	/**
	 * Gets the type of this alternate translation. The value is on of the {@link MatchType} values.
	 * @return the type of this alternate translation.
	 */
	public MatchType getType () {
		return type;
	}

	/**
	 * Sets the match type of this alternate translation.
	 * @param type the new match type.
	 */
	public void setType (MatchType type) {
		this.type = type;
	}

	/**
	 * Sets the score of this alternate translation.
	 * @param score the new score.
	 */
	public void setCombinedScore (int score) {
		// some systems set scores higher than 100!!!
		if ((( score < 0 ) && score != QueryResult.COMBINEDSCORE_UNDEFINED )) {
			throw new IllegalArgumentException("Invalid score value.");
		}
		this.combinedScore = score;
	}

	/**
	 * Sets the origin of this alternate translation.
	 * @param origin the new origin.
	 */
	public void setOrigin (String origin) {
		this.origin = origin;
	}
	
	/**
	 * Sets the engine of this alternate translation.
	 * The engine is some additional information about the origin of the entry.
	 * @param engine the new engine.
	 */
	public void setEngine (String engine) {
		this.engine = engine;
	}

	/**
	 * Sets the flag indicating if this alternate translation was provided from the original document
	 * (e.g. as an alt-trans element in XLIFF).
	 * @param fromOriginal true if the match was provided by the original document.
	 */
	public void setFromOriginal (boolean fromOriginal) {
		this.fromOriginal = fromOriginal;
	}
	
	/**
	 * Indicates if this alternate translation was provided from the original document.
	 * @return true if the match was provided by the original document.
	 */
	public boolean getFromOriginal () {
		return this.fromOriginal;
	}
	
	public int getFuzzyScore () {
		return fuzzyScore;
	}

	public void setFuzzyScore (int fuzzyScore) {
		if (( fuzzyScore < 0 ) || ( fuzzyScore > 100 )) {
			throw new IllegalArgumentException("Invalid fuzzyScore value.");
		}
		this.fuzzyScore = fuzzyScore;
	}

	public int getQualityScore () {
		return qualityScore;
	}

	public void setQualityScore (int qualityScore) {
		if ((( qualityScore < 0 ) && qualityScore != QueryResult.QUALITY_UNDEFINED ) || ( qualityScore > 100 )) {
			throw new IllegalArgumentException("Invalid qualityScore value.");
		}
		this.qualityScore = qualityScore;
	}

	public XLIFFTool getTool () {
		return tool;
	}
	public void setTool (XLIFFTool tool) {
		this.tool = tool;
	}

	/**
	 * Indicates if a given match type is considered as exact or not.
	 * @param type the match type to evaluate.
	 * @return true if the given match type is considered exact.
	 */
	private boolean isExact (MatchType type) {
		// EXACT_REPAIRED considered a fuzzy match
		if ( type.ordinal() < MatchType.EXACT_REPAIRED.ordinal() ) {
			return true;
		}
		return false;
	}

	/**
	 * Indicator telling if the result is coming from a machine translation engine or not.
	 * @return true if the result is coming from a machine translation engine, false otherwise.
	 */
	public boolean fromMT () {
		return (type == MatchType.MT);
	}
	
	public String getALttransType() {
		return alttranstype;
	}

	public void setAltTransType(String alttranstype) {
		this.alttranstype = alttranstype;
	}

	/**
	 * This method implements a three way sort on (1) AltTranslationType (2) Score (3)
	 * source string match. AltTranslationType is the primary key, score secondary and source
	 * string tertiary.
	 * @param other the AltTranslation we are comparing against.
	 * @return the comparison result (0 if both object are equal).
	 */
	@Override
	public int compareTo (AltTranslation other) {
		final int EQUAL = 0;

		if ( this == other ) {
			return EQUAL;
		}

		String thisSource = this.getSource().toString();
		String otherSource = other.getSource().toString();
		int comparison;
		
		// only sort by match type if this or other is some kind of exact match
		if ( isExact(this.getType()) || isExact(other.getType()) ) {					
			// compare TmMatchType
			comparison = this.getType().compareTo(other.getType());
			if ( comparison != EQUAL ) {
				return comparison;
			}
		}
		
		// compare score
		comparison = Float.compare(this.combinedScore, other.getCombinedScore());
		if ( comparison != EQUAL ) {
			return comparison * -1; // we want to reverse the normal score sort
		}

		// compare source strings with codes
		comparison = thisSource.compareTo(otherSource);
		if ( comparison != EQUAL ) {
			return comparison;
		}

		// default
		return EQUAL;
	}

	/**
	 * Define equality of state.
	 * @param other the object to compare with.
	 * @return true if the objects are equal, false otherwise.
	 */
	@Override
	public boolean equals (Object other) {
		if ( this == other ) {
			return true;
		}
		if ( !(other instanceof AltTranslation) ) {
			return false;
		}

		AltTranslation otherHit = (AltTranslation) other;
		return ( this.getType() == otherHit.getType() )
				&& (this.getSource().toString().equals(otherHit.getSource().toString()))
				&& (this.getTarget().toString().equals(otherHit.getTarget().toString()));
	}

	/**
	 * A class that overrides equals must also override hashCode.
	 * @return the has code for this object.
	 */
	@Override
	public int hashCode () {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, getType());
		result = HashCodeUtil.hash(result, getSource().toString());
		result = HashCodeUtil.hash(result, getTarget().toString());
		return result;
	}
	
	@Override
	public String toString() {		
		return String.format("%s %d%% %s", type.name(), combinedScore, tu.getSource());
	}

}