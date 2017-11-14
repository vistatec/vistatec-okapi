/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextFragment;

/**
 * Provides a simple way to compare two lists of tokens using
 * basic fuzzy matching algorithms. 
 */
public class TextMatcher {

	/**
	 * Flag indicating to ignore case differences.
	 */
	public static final int IGNORE_CASE = 0x01;
	/**
	 * Flag indicating to ignore whitespaces differences.
	 */
	public static final int IGNORE_WHITESPACES = 0x02;
	/**
	 * Flag indication to ignore punctuation differences.
	 */
	public static final int IGNORE_PUNCTUATION = 0x04;
	
	private static final int MAXTOKEN = 1024;
	
	private static short[] matP = null; // 'previous' cost array, horizontally
	private static short[] matD = null; // cost array, horizontally

	private BreakIterator breaker1;
	private BreakIterator breaker2;

	/**
	 * Creates a new TextMatcher object.
	 * @param locale1 locale of the first language.
	 * @param locale2 locale of the second language.
	 */
	public TextMatcher (LocaleId locale1,
		LocaleId locale2)
	{
		// Create the first breaker.
		breaker1 = BreakIterator.getWordInstance(locale1.toJavaLocale());
		if ( locale1.equals(locale2) ) {
			// Use the same one if the second language is the same.
			breaker2 = breaker1;
		}
		else {
			// If two different languages: create a second breaker.
			breaker2 = BreakIterator.getWordInstance(locale2.toJavaLocale());
		}
	}
	
	/**
	 * Returns the minimum value between three given values.
	 * @param value1 the first given value.
	 * @param value2 the second given value.
	 * @param value3 the third given value.
	 * @return the minimum value between three given values.
	 */
	protected static short minimum (int value1,
		int value2,
		int value3)
	{
		return (short)Math.min(value1, Math.min(value2, value3));
	}
	
	private static int waikoloa (List<String> tokens1,
		List<String> tokens2)
	{
		int wordsInQuery = tokens1.size();
		int wordsInCandidate = tokens2.size();
		int wordsFound = 0;
		int n;
		for ( String token : tokens1 ) {
			//TODO: fix order and duplicates cases
			if ( (n = tokens2.indexOf(token)) > -1 ) {
				wordsFound++;
				tokens2.set(n, null);
			}
		}
		
		Float f = 100 * ((float)(2*wordsFound) / (wordsInQuery+wordsInCandidate));
		return f.intValue(); 
	}
	
	private static int levenshtein (List<String> tokens1,
		List<String> tokens2)
	{
		int n = tokens1.size();
		int m = tokens2.size();
		if ( n == 0 ) return m;
		if ( m == 0 ) return n;

		// Create the array if needed
		// We use this to avoid re-creating the array each time
		if ( matP == null ) matP = new short[MAXTOKEN+1];
		if ( matD == null ) matD = new short[MAXTOKEN+1];

		// Artificial limitation due to static arrays
		if ( n > MAXTOKEN ) n = MAXTOKEN;
		if ( m > MAXTOKEN ) m = MAXTOKEN;
		short[] swap; // place-holder to assist in swapping p and d
	
		// Indexes into strings tokens and t
		int i; // Iterates through tokens1
		int j; // Iterates through t
	
		Object obj2j = null; // Object for p_aList2
		int cost; // Cost
	
		for ( i=0; i<=n; i++ ) matP[i] = (short)i;
		for ( j=1; j<=m; j++ ) {
			obj2j = tokens2.get(j-1);
			matD[0] = (short)j;
			Object obj1i = null; // Object for tokens1
			// Not used: object s_i2; // Object for list 2 (at i-1)
			for ( i=1; i<=n; i++ ) {
				obj1i = tokens1.get(i-1);
				cost = (obj1i.equals(obj2j) ? (short)0 : (short)1);
				// Minimum of cell to the left+1, to the top+1, diagonally left and up + cost
				matD[i] = minimum(matD[i-1]+1, matP[i]+1, matP[i-1]+cost);
			}
			// Copy current distance counts to 'previous row' distance counts
			swap = matP; matP = matD; matD = swap;
		}
	
		// The last action in the above loop was to switch d and p
		// so now p has actually the most recent cost counts
		int longest = Math.max(n, m);
		return (100*(longest-matP[n]))/longest;
	}

	/**
	 * Compare two textFragment content. 
	 * @param frag1 The base fragment.
	 * @param frag2 the fragment to compare against the base fragment.
	 * @param options Comparison options.
	 * @return A score between 0 (no match) and 100 (exact match).
	 */
	public int compare (TextFragment frag1,
		TextFragment frag2,
		int options)
	{
		String text1 = frag1.getCodedText();
		String text2 = frag2.getCodedText();
		
		// Check if it actually is exactly the same?
		if ( text1.equals(text2) ) return 100;
		// Check if there is only casing differences
		if ( text1.equalsIgnoreCase(text2) ) {
			return (((options & IGNORE_CASE) == IGNORE_CASE) ? 100 : 99);
		}

		// Break down into tokens
		List<String> tokens1;
		List<String> tokens2;
		
		if ( (options & IGNORE_PUNCTUATION) == IGNORE_PUNCTUATION ) {
			text1 = text1.replaceAll("\\p{Punct}", " ");
			text2 = text2.replaceAll("\\p{Punct}", " ");
		}
		
		if ( (options & IGNORE_CASE) == IGNORE_CASE ) {
			tokens1 = tokenize(text1.toLowerCase(), breaker1);
			tokens2 = tokenize(text2.toLowerCase(), breaker2);
		}
		else { // Keep case differences
			tokens1 = tokenize(text1, breaker1);
			tokens2 = tokenize(text2, breaker2);
		}

		int n = levenshtein(tokens1, tokens2);
		if ( n == 100 ) {
			// Differences are hidden tokenization
			return 99;
		}
		else return n;
	}
	
	/**
	 * Creates a list of tokens from a string to use
	 * with the {@link #compareToBaseTokens(String, List, TextFragment)}.
	 * @param plainText the based text.
	 * @return the list of tokens for the given fragment.
	 */
	public List<String> prepareBaseTokens (String plainText) {
		String text = plainText.replaceAll("\\p{Punct}", " ");
		return tokenize(text.toLowerCase(), breaker1);
	}

	/**
	 * Compare a list of tokens to a {@link TextFragment} object.
	 * @param text1 the original plain text. 
	 * @param tokens1 the list of tokens.
	 * @param frag2 the fragment to compare against list of tokens.
	 * @return A score between 0 (no match) and 100 (exact match).
	 */
	public int compareToBaseTokens (String text1,
		List<String> tokens1,
		TextFragment frag2)
	{
		String text2 = frag2.getCodedText();
		// Check if it actually is exactly the same?
		if ( text1.equals(text2) ) return 100;
		// Check if there is only casing differences
		if ( text1.equalsIgnoreCase(text2) ) {
			return 99;
		}

		text2 = text2.replaceAll("\\p{Punct}", " ");
		List<String> tokens2 = tokenize(text2.toLowerCase(), breaker2);

		//int n = levenshtein(tokens1, tokens2);
		int n = waikoloa(tokens1, tokens2);
		if ( n == 100 ) {
			// Differences are hidden tokenization
			return 99;
		}
		else return n;
	}
	
	/**
	 * Breaks the text into words (or equivalents).
	 * @param text The text to break down.
	 * @return The list of the "words" generated.
	 */
	private List<String> tokenize (String text,
		BreakIterator breakerToUse)
	{
		breakerToUse.setText(text);
		ArrayList<String> list = new ArrayList<String>();
		int start = breakerToUse.first();
		for ( int end = breakerToUse.next(); end != BreakIterator.DONE; start=end, end=breakerToUse.next() ) {
			list.add(text.substring(start,end));
		}
		return list;
	}

}
