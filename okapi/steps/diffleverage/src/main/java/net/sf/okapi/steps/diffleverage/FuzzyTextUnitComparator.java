/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.diffleverage;

import java.util.Comparator;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.search.lucene.analysis.AlphabeticNgramTokenizer;
import net.sf.okapi.lib.search.lucene.scorer.Util;

/**
 * Fuzzily Compare two source {@link TextUnit}s. Text, isReferent() and (optionally) codes must all be same in order to
 * be a match. The fuzzy compare uses n-grams and does a in memory comparison to get a score. Only scores greater than {
 * {@link #threshold} are considered matches.
 * 
 * @author HARGRAVEJE
 * 
 */
public class FuzzyTextUnitComparator implements Comparator<ITextUnit> {
	private static final int NGRAM_SIZE = 3;

	private boolean codeSensitive;
	private int threshold;
	private final AlphabeticNgramTokenizer tokenizer;

	public FuzzyTextUnitComparator(final boolean codeSensitive, final int threshold,
			final LocaleId localeId) {
		this.codeSensitive = codeSensitive;
		setThreshold(threshold);
		tokenizer = Util.createNgramTokenizer(NGRAM_SIZE, localeId);		
	}

	public void setCodeSensitive(final boolean codeSensitive) {
		this.codeSensitive = codeSensitive;
	}

	public boolean isCodeSensitive() {
		return codeSensitive;
	}

	public void setThreshold(final int threshold) {
		this.threshold = threshold;
	}

	public float getThreshold() {
		return threshold;
	}

	@Override
	public int compare(final ITextUnit oldTextUnit, final ITextUnit newTextUnit) {
		if (oldTextUnit.isReferent() && !newTextUnit.isReferent()) {
			return -1; // old is greater than new
			// (not sure what greater means in this case but we have to return something)
		} else if (!oldTextUnit.isReferent() && newTextUnit.isReferent()) {
			return 1; // new is greater than old
			// (not sure what greater means in this case but we have to return something)
		} else {
			// both are either referents or not
			final int result = oldTextUnit.getSource().compareTo(newTextUnit.getSource(),
					codeSensitive);
			if (result == 0) {
				return result;
			} else {
				// do fuzzy compare
				return fuzzyCompare(oldTextUnit, newTextUnit, result);
			}
		}
	}

	private int fuzzyCompare(final ITextUnit oldSource, final ITextUnit newSource,
			int exactCompareResult) {
		float score = Util.calculateNgramDiceCoefficient(
				oldSource.getSource().getUnSegmentedContentCopy().getText(), 
				newSource.getSource().getUnSegmentedContentCopy().getText(), 
				tokenizer);
		if (score >= threshold) {
			return 0;
		}

		return exactCompareResult;
	}
}
