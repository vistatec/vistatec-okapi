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

package net.sf.okapi.steps.wordcount;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.IWithAnnotations;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.steps.tokenization.Tokenizer;
import net.sf.okapi.steps.tokenization.tokens.Tokens;
import net.sf.okapi.steps.wordcount.common.BaseCounter;
import net.sf.okapi.steps.wordcount.common.GMX;
import net.sf.okapi.steps.wordcount.common.Metrics;
import net.sf.okapi.steps.wordcount.common.MetricsAnnotation;
import net.sf.okapi.steps.wordcount.common.StructureParameters;

/**
 * Word Count engine. Contains static methods to calculate number of words in a given text fragment. 
 * 
 * @version 0.1 07.07.2009
 */

public class WordCounter extends BaseCounter {
	
	private static final WordCounter counter = new WordCounter();

	private static StructureParameters params;
	
	protected static void loadParameters() {
		
		if (params != null) return; // Already loaded
		
		params = new StructureParameters();
		if (params == null) return;
		
		params.loadFromResource("word_counter.tprm");
	}
	
	@Override
	protected long doCountImpl(String text, LocaleId language) {
		if (GMX.isLogographicScript(language)) {
			return countLogographicScript(text, language);
		}
		
		Tokens tokens = Tokenizer.tokenize(text, language, getTokenName());		
		if (tokens == null) return 0;
		
		return tokens.size();
	}

	/**
	 * For "logographic" languages, GMX-V 2.0 defines factors by which the character count
	 * should be divided in order to yield the word count. This method calculates that
	 * word count.
	 * <p>
	 * The result will be <code>0</code> if the language is logographic but does not have
	 * a character count factor defined ({@link GMX#getCharacterCountFactor(LocaleId)} returns
	 * <code>-1d</code>). In this case word counts are not meaningful for the supplied language.
	 * <p>
	 * This method will throw {@link IllegalArgumentException} if the supplied language is
	 * not a logographic script ({@link GMX#isLogographicScript(LocaleId)} returns
	 * <code>false</code>).
	 * 
	 * @see web http://www.xtm-intl.com/manuals/gmx-v/GMX-V-2.0.html#LogographicScripts
	 */
	public static long countLogographicScript(Object text, LocaleId language) {
		long characterCount = CharacterCounter.count(text, language);
		return countFromLogographicCharacterCount(characterCount, language);
	}
	
	/**
	 * For "logographic" languages, GMX-V 2.0 defines factors by which the character count
	 * should be divided in order to yield the word count. This method calculates that
	 * word count.
	 * <p>
	 * The result will be <code>0</code> if the language is logographic but does not have
	 * a character count factor defined ({@link GMX#getCharacterCountFactor(LocaleId)} returns
	 * <code>-1d</code>). In this case word counts are not meaningful for the supplied language.
	 * <p>
	 * This method will throw {@link IllegalArgumentException} if the supplied language is
	 * not a logographic script ({@link GMX#isLogographicScript(LocaleId)} returns
	 * <code>false</code>).
	 * 
	 * @see web http://www.xtm-intl.com/manuals/gmx-v/GMX-V-2.0.html#LogographicScripts
	 */
	public static long countFromLogographicCharacterCount(long characterCount, LocaleId language) {
		if (!GMX.isLogographicScript(language)) {
			throw new IllegalArgumentException(language.toString() + " is not a logographic script");
		}
		double charCountFactor = GMX.getCharacterCountFactor(language);
		if (charCountFactor == -1d) {
			return 0L;
		}
		return Math.round(characterCount / charCountFactor);
	}

	/**
	 * Counts words in the source part of a given text unit.
	 * @param textUnit the given text unit
	 * @param language the language of the source
	 * @return number of words
	 */	
	public static long count(ITextUnit textUnit, LocaleId language) {
		return counter.doCount(textUnit, language);		
	}
	
	/**
	 * Counts words in a given text container.
	 * @param textContainer the given text container
	 * @param language the language of the text
	 * @return number of words
	 */
	public static long count(TextContainer textContainer, LocaleId language) {
		return counter.doCount(textContainer, language);		
	}
	
	/**
	 * Counts words in a given segment.
	 * @param segment the given segment
	 * @param language the language of the text
	 * @return number of words
	 */
	public static long count(Segment segment, LocaleId language) {
		return counter.doCount(segment, language);		
	}

	/**
	 * Counts words in a given text fragment.
	 * @param textFragment the given text fragment
	 * @param language the language of the text
	 * @return number of words
	 */
	public static long count(TextFragment textFragment, LocaleId language) {
		return counter.doCount(textFragment, language);		
	}
	
	/**
	 * Counts words in a given string.
	 * @param string the given string
	 * @param language the language of the text
	 * @return number of words
	 */
	public static long count(String string, LocaleId language) {
		return counter.doCount(string, language);		
	}
	
	@Override
	protected String getMetricNameForRetrieval() {
		return GMX.TotalWordCount;
	}
	
	/**
	 * Returns the word count information stored by WordCountStep in annotations of a given resource. 
	 * @param tu the given resource
	 * @return number of words (0 if no word count information found)
	 */
	public static long getCount(IWithAnnotations res) {
		return counter.doGetCount(res);
	}
	
	/**
	 * Returns the word count information stored by WordCountStep in the source part of a given text unit. 
	 * @param tu the given text unit
	 * @return number of words (0 if no word count information found)
	 */
	public static long getCount(ITextUnit tu) {
		return counter.doGetCount(tu);
	}
	
	/**
	 * Returns the word count information stored by WordCountStep in the given text container. 
	 * @param tc the given text container
	 * @return number of words (0 if no word count information found)
	 */
	public static long getCount(TextContainer tc) {
		return counter.doGetCount(tc);
	}
	
	/**
	 * Returns the word count information stored by WordCountStep in a given segment of the source part of a given text unit.
	 * @param tu the given tu
	 * @param segIndex index of the segment in the source
	 * @return number of words (0 if no word count information found)
	 */
	public static long getCount(ITextUnit tu, int segIndex) {
		ISegments segments = tu.getSource().getSegments();
		return getCount(segments.get(segIndex));		
	}
	
	/**
	 * Returns the word count information stored by WordCountStep in a given segment of the source part of a given text unit.
	 * @param segment the given segment
	 * @return number of words (0 if no word count information found)
	 */
	public static long getCount(Segment segment) {
		return counter.doGetCount(segment);
	}
	
	public static String getTokenName() {		
		loadParameters();
		
		if (params == null) return "";
		return params.getTokenName();
	}
	
	public static void setCount(IWithAnnotations res, long count) {
		MetricsAnnotation ma = res.getAnnotation(MetricsAnnotation.class);
		
		if (ma == null) {			
			ma = new MetricsAnnotation();
			res.setAnnotation(ma);
		}
		
		Metrics m = ma.getMetrics();		
		m.setMetric(GMX.TotalWordCount, count);
	}
}
