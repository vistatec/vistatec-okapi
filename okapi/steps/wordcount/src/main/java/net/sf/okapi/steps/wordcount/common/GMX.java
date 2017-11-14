/*===========================================================================
  Copyright (C) 2009-2015 by the Okapi Framework contributors
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

package net.sf.okapi.steps.wordcount.common;

import java.util.regex.Pattern;

import net.sf.okapi.common.LocaleId;

/**
 * Implementation of the GMX-V specification, v. 2.0
 * 
 * @see Web http://www.xtm-intl.com/manuals/gmx-v/GMX-V-2.0.html
 * @see PDF http://www.etsi.org/deliver/etsi_gs/LIS/001_099/004/02.00.00_60/gs_LIS004v020000p.pdf
 * 
 * @version 0.2 08.26.2015
 */

public class GMX {

	final public static String extNamePrefix = "x-";
	
// 3.2. Word Count Categories
	
	/**
	 * Total word count - an accumulation of the word counts, both translatable and non-translatable, 
	 * from the individual text units that make up the document.
	 */
	final public static String TotalWordCount = "TotalWordCount";
	
	/**
	 * An accumulation of the word count for text that has been marked as 'protected', or otherwise 
	 * not translatable (XLIFF text enclosed in <mrk mtype="protected"> elements). 
	 */
	final public static String ProtectedWordCount = "ProtectedWordCount";

	/**
	 * An accumulation of the word count for text units that have been matched unambiguously with a 
	 * prior translation and thus require no translator input.
	 */
	final public static String ExactMatchedWordCount = "ExactMatchedWordCount";

	/**
	 * An accumulation of the word count for text units that have been matched against a leveraged 
	 * translation memory database.
	 */
	final public static String LeveragedMatchedWordCount = "LeveragedMatchedWordCount";

	/**
	 * An accumulation of the word count for repeating text units that have not been matched in any 
	 * other form. Repetition matching is deemed to take precedence over fuzzy matching.
	 */
	final public static String RepetitionMatchedWordCount = "RepetitionMatchedWordCount";

	/**
	 * An accumulation of the word count for text units that have been fuzzy matched against a 
	 * leveraged translation memory database. 
	 */
	final public static String FuzzyMatchedWordCount = "FuzzyMatchedWordCount";

	/**
	 * An accumulation of the word count for text units that have been identified as containing 
	 * only alphanumeric words. 
	 */
	final public static String AlphanumericOnlyTextUnitWordCount = "AlphanumericOnlyTextUnitWordCount";

	/**
	 * An accumulation of the word count for text units that have been identified as containing 
	 * only numeric words.
	 */
	final public static String NumericOnlyTextUnitWordCount = "NumericOnlyTextUnitWordCount";

	/**
	 * An accumulation of the word count from measurement-only text units.
	 */
	final public static String MeasurementOnlyTextUnitWordCount = "MeasurementOnlyTextUnitWordCount";
	
// 3.3. Auto Text Word Count Categories	

	/**
	 * An accumulation of the word count for simple numeric values, e.g. 10.
	 */
	final public static String SimpleNumericAutoTextWordCount = "SimpleNumericAutoTextWordCount";

	/**
	 * An accumulation of the word count for complex numeric values which include decimal 
	 * and/or thousands separators, e.g. 10,000.00.
	 */
	final public static String ComplexNumericAutoTextWordCount = "ComplexNumericAutoTextWordCount";

	/**
	 * An accumulation of the word count for identifiable measurement values, e.g. 10.50 mm. 
	 * Measurement values take precedent over the above numeric categories. No double counting 
	 * of these categories is allowed.
	 */
	final public static String MeasurementAutoTextWordCount = "MeasurementAutoTextWordCount";

	/**
	 * An accumulation of the word count for identifiable alphanumeric words, e.g. AEG321. 
	 */
	final public static String AlphaNumericAutoTextWordCount = "AlphaNumericAutoTextWordCount";

	/**
	 * An accumulation of the word count for identifiable dates, e.g. 25 June 1992.
	 */
	final public static String DateAutoTextWordCount = "DateAutoTextWordCount";

	/**
	 * An accumulation of the word count for identifiable trade marks, e.g. "Weapons of Mass Destruction...".
	 */
	final public static String TMAutoTextWordCount = "TMAutoTextWordCount";

// 3.4. Character Count Categories	

	/**
	 * An accumulation of the character counts, both translatable and non-translatable, from the 
	 * individual text units that make up the document. This count includes all non white space 
	 * characters in the document (please refer to Section 2.7. White Space Characters for details 
	 * of what constitutes white space characters), excluding inline markup and punctuation characters 
	 * (please refer to Section 2.10. Punctuation Characters for details of what constitutes 
	 * punctuation characters).
	 */
	final public static String TotalCharacterCount = "TotalCharacterCount";

	/**
	 * The total of all punctuation characters in the canonical form of text in the document that 
	 * DO NOT form part of the character count as per section 2.10. Punctuation Characters.
	 */
	final public static String PunctuationCharacterCount = "PunctuationCharacterCount";

	/**
	 * The total of all white space characters in the canonical form of the text units in the document. 
	 * Please refer to section 2.7. White Space Characters for a detailed explanation of how white 
	 * space characters are identified and counted.
	 */
	final public static String WhiteSpaceCharacterCount = "WhiteSpaceCharacterCount";

	/**
	 * The total of all of the three main character counts (TotalCharacterCount +
	 * PunctuationCharacterCount + WhiteSpaceCharacterCount) in the canonical form of the text units in
	 * the document.
	 * 
	 * (Added in GMX-V 2.0)
	 */
	final public static String OverallCharacterCount = "OverallCharacterCount";

	/**
	 * An accumulation of the character count for text that has been marked as 'protected', or 
	 * otherwise not translatable (XLIFF text enclosed in <mrk mtype="protected"> elements).
	 */
	final public static String ProtectedCharacterCount = "ProtectedCharacterCount";

	/**
	 * An accumulation of the character count for text units that have been matched unambiguously 
	 * with a prior translation and require no translator input.
	 */
	final public static String ExactMatchedCharacterCount = "ExactMatchedCharacterCount";

	/**
	 * An accumulation of the character count for text units that have been matched against a 
	 * leveraged translation memory database.
	 */
	final public static String LeveragedMatchedCharacterCount = "LeveragedMatchedCharacterCount";

	/**
	 * An accumulation of the character count for repeating text units that have not been matched 
	 * in any other form. Repetition matching is deemed to take precedence over fuzzy matching.
	 */
	final public static String RepetitionMatchedCharacterCount = "RepetitionMatchedCharacterCount";

	/**
	 * An accumulation of the character count for text units that have a fuzzy match against a 
	 * leveraged translation memory database.
	 */
	final public static String FuzzyMatchedCharacterCount = "FuzzyMatchedCharacterCount";

	/**
	 * An accumulation of the character count for text units that have been identified as 
	 * containing only alphanumeric words.
	 */
	final public static String AlphanumericOnlyTextUnitCharacterCount = "AlphanumericOnlyTextUnitCharacterCount";

	/**
	 * An accumulation of the character count for text units that have been identified as 
	 * containing only numeric words.
	 */
	final public static String NumericOnlyTextUnitCharacterCount = "NumericOnlyTextUnitCharacterCount";

	/**
	 * An accumulation of the character count from measurement-only text units.
	 */
	final public static String MeasurementOnlyTextUnitCharacterCount = "MeasurementOnlyTextUnitCharacterCount";
	
// 3.5. Auto Text Character Count Categories	

	/**
	 * An accumulation of the character count for simple numeric values, e.g. 10. 
	 */
	final public static String SimpleNumericAutoTextCharacterCount = "SimpleNumericAutoTextCharacterCount";

	/**
	 * An accumulation of the character count for complex numeric values which include decimal 
	 * and/or thousands separators, e.g. 10,000.00.
	 */
	final public static String ComplexNumericAutoTextCharacterCount = "ComplexNumericAutoTextCharacterCount";

	/**
	 * An accumulation of the character count for identifiable measurement values, e.g. 10.50 mm. 
	 * Measurement values take precedent over the above numeric categories. No double counting of these categories is allowed.
	 */
	final public static String MeasurementAutoTextCharacterCount = "MeasurementAutoTextCharacterCount";

	/**
	 * An accumulation of the character count for identifiable alphanumeric words, e.g. AEG321.
	 */
	final public static String AlphaNumericAutoTextCharacterCount = "AlphaNumericAutoTextCharacterCount";

	/**
	 * An accumulation of the character count for identifiable dates, e.g. 25 June 1992. 
	 */
	final public static String DateAutoTextCharacterCount = "DateAutoTextCharacterCount";

	/**
	 * An accumulation of the character count for identifiable trade marks, e.g. "Weapons of Mass Destruction...". 
	 */
	final public static String TMAutoTextCharacterCount = "TMAutoTextCharacterCount";

// 3.6. Inline Element Count Categories	

	/**
	 * The actual non-linking inline element count for unqualified (see Section 2.14.2 
	 * Unqualified Text Units) text units. Please refer to Section 2.11. Inline Element Counts 
	 * for a detailed explanation and examples for this category.
	 */
	final public static String TranslatableInlineCount = "TranslatableInlineCount";
	
// 3.7. Linking Inline Element Count Categories	

	/**
	 * The actual linking inline element count for unqualified (see Section 2.14.2 Unqualified 
	 * Text Units) text units. Please refer to Section 2.12. Linking Inline Elements for a 
	 * detailed explanation and examples for this category.
	 */
	final public static String TranslatableLinkingInlineCount = "TranslatableLinkingInlineCount";
	
// 3.8. Text Unit Counts	

	/**
	 * The total number of text units.
	 */
	final public static String TextUnitCount = "TextUnitCount";
	
// 3.9. Other Count Categories	

	/**
	 * The total number of files.
	 */
	final public static String FileCount = "FileCount";

	/**
	 * The total number of pages.
	 */
	final public static String PageCount = "PageCount";

	/**
	 * A count of the total number of screens.
	 */
	final public static String ScreenCount = "ScreenCount";
	
// 3.10. Project Specific Count Categories	

	/**
	 * The word count for text units that are identical within all files within a given project. 
	 * The word count for the primary occurrence is not included in this count, only that of 
	 * subsequent matches.
	 */
	final public static String ProjectRepetionMatchedWordCount = "ProjectRepetionMatchedWordCount";

	/**
	 * The word count for fuzzy matched text units within all files within a given project. The 
	 * word count for the primary occurrence is not included in this count, only that of 
	 * subsequent matches. 
	 */
	final public static String ProjectFuzzyMatchedWordCount = "ProjectFuzzyMatchedWordCount";

	/**
	 * The character count for text that is identical within all files within a given project. 
	 * The character count for the primary occurrence is not included in this count, only that 
	 * of subsequent matches.
	 */
	final public static String ProjectRepetionMatchedCharacterCount = "ProjectRepetionMatchedCharacterCount";

	/**
	 * The character count for fuzzy matched text within all files within a given project. The character 
	 * count for the primary occurrence is not included in this count, only that of subsequent matches. 
	 */
	final public static String ProjectFuzzyMatchedCharacterCount = "ProjectFuzzyMatchedCharacterCount";

    /**
     * Pattern to match the BCP-47 codes of the locales that are considered "logographic" by the
     * GMX-V 2.0 spec. These languages have special requirements for word counts.
     * Note that this is not perfect as some languages use several scripts.
     */
	private static final Pattern LOGOGRAPHICSCRIPTS = Pattern.compile("(zh|ja|ko|th|lo|km|my)(-.*)?", Pattern.CASE_INSENSITIVE);

	/**
	 * Indicates whether or not the language is considered a "logographic" language per
	 * the GMX-V 2.0 spec. If <code>true</code>, word counts for this language are defined as
	 * (character count / {@link #getCharacterCountFactor(LocaleId)}), unless the character
	 * count factor is <code>-1d</code> in which case word counts are not meaningful for
	 * the language.
	 * 
	 * @see web http://www.xtm-intl.com/manuals/gmx-v/GMX-V-2.0.html#LogographicScripts
	 */
	public static boolean isLogographicScript(LocaleId locId) {
		return LOGOGRAPHICSCRIPTS.matcher(locId.toBCP47()).matches();
	}
	
	/**
	 * For "logographic" languages, GMX-V 2.0 defines factors by which the character count
	 * should be divided in order to yield the word count.
	 * <p>
	 * Returns <code>-1d</code> if the language does not have a factor. If this method returns
	 * <code>-1d</code> and {@link #isLogographicScript(LocaleId)} returns <code>true</code>,
	 * then word counts are not meaningful for this language.
	 * 
	 * @see web http://www.xtm-intl.com/manuals/gmx-v/GMX-V-2.0.html#LogographicScripts
	 */
	public static double getCharacterCountFactor(LocaleId language) {
		if (language.sameLanguageAs(LocaleId.CHINA_CHINESE)) {
			// Only comparing language; "China" irrelevant
			return 2.8;
		} else if (language.sameLanguageAs(LocaleId.JAPANESE)) {
			return 3.0;
		} else if (language.sameLanguageAs(LocaleId.KOREAN)) {
			return 3.3;
		} else if (language.sameLanguageAs("th")) { // Thai
			return 6.0;
		}
		return -1d;
	}
}
