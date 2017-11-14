package net.sf.okapi.lib.search.lucene.scorer;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.lib.search.lucene.analysis.AlphabeticNgramTokenizer;

public class Util {
	/**
	 * Calculate Dice's Coefficient
	 * 
	 * @param intersection
	 *            number of tokens in common between input 1 and input 2
	 * @param size1
	 *            token size of first input
	 * @param size2
	 *            token size of second input
	 * @return Dice's Coefficient as a float
	 */
	public static float calculateDiceCoefficient(int intersection, int size1, int size2) {
		return (float) ((2.0f * (float) intersection)) / (float) (size1 + size2) * 100.0f;
	}

	/**
	 * Calculate Dice's Coefficient for two strings with tokens as ngrams.
	 * 
	 * @param originalSource
	 *            first string to compare
	 * @param newSource
	 *            second string to compare
	 * @param tokenizer
	 *            ngram tokenizer
	 * @return Dice's Coefficient as a float
	 */
	public static float calculateNgramDiceCoefficient(String originalSource, String newSource,
			AlphabeticNgramTokenizer tokenizer) {
		Set<String> originalSourceTokens = new HashSet<String>();
		Set<String> newSourceTokens = new HashSet<String>();

		try {
			// get old source string tokens
			tokenizer.reset(new StringReader(originalSource));
			while (tokenizer.incrementToken()) {
				originalSourceTokens.add(tokenizer.getTermAttribute().toString());
			}

			// get the new source tokens
			tokenizer.reset(new StringReader(newSource));
			while (tokenizer.incrementToken()) {
				newSourceTokens.add(tokenizer.getTermAttribute().toString());
			}
		} catch (IOException e) {
			throw new OkapiException("Error tokenizing source TextUnits", e);
		}

		// now calculate dice coefficient to get fuzzy score
		int originalSize = originalSourceTokens.size();
		int newSize = newSourceTokens.size();
		originalSourceTokens.retainAll(newSourceTokens);
		int intersection = originalSourceTokens.size();
		return (float) ((2.0f * (float) intersection)) / (float) (originalSize + newSize) * 100.0f;
	}

	/**
	 * Create a {@link AlphabeticNgramTokenizer}
	 * 
	 * @param ngramSize
	 *            size of ngram in characters
	 * @param localeId
	 *            {@link LocaleId} of the content being tokenized
	 * @return a {@link AlphabeticNgramTokenizer}
	 */
	public static AlphabeticNgramTokenizer createNgramTokenizer(int ngramSize, LocaleId localeId) {
		return new AlphabeticNgramTokenizer(new StringReader(""), ngramSize, localeId
				.toJavaLocale());
	}
}
