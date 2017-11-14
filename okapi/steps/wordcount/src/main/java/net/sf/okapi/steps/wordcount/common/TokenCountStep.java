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

package net.sf.okapi.steps.wordcount.common;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.steps.tokenization.Tokenizer;
import net.sf.okapi.steps.tokenization.common.TokensAnnotation;
import net.sf.okapi.steps.tokenization.tokens.Tokens;
import net.sf.okapi.steps.wordcount.WordCounter;

public abstract class TokenCountStep extends BaseCountStep {

	protected abstract String[] getTokenNames();

	/* To be overridden in steps that need more complex logic. */
	protected Tokens filterTokens(Tokens allTokens) {
		return allTokens.getFilteredList(getTokenNames());
	}
	
	protected Tokens getTokens(TextContainer textContainer, LocaleId locale) {
		TokensAnnotation ta = textContainer.getAnnotation(TokensAnnotation.class);
		
		Tokens allTokens = ta != null ? ta.getTokens()
				: Tokenizer.tokenize(textContainer, locale);
		
		return filterTokens(allTokens);
	}
			
	@Override
	protected long count(TextContainer textContainer, LocaleId locale) {
		Tokens tokens = getTokens(textContainer, locale);
		if (GMX.isLogographicScript(locale)) {
			long characterCount = TokenCharacterCountStep.countTokenChars(tokens, locale);
			return WordCounter.countFromLogographicCharacterCount(characterCount, locale);
		}
		return tokens == null ? 0 : tokens.size();
	}

	protected Tokens getTokens(Segment segment, LocaleId locale) {
		TokensAnnotation ta = segment.getAnnotation(TokensAnnotation.class);
		
		Tokens allTokens = ta != null ? ta.getTokens()
				: Tokenizer.tokenize(segment.getContent(), locale);
		
		return filterTokens(allTokens);
	}
	
	@Override
	protected long count(Segment segment, LocaleId locale) {		
		Tokens tokens = getTokens(segment, locale);
		if (GMX.isLogographicScript(locale)) {
			long characterCount = TokenCharacterCountStep.countTokenChars(tokens, locale);
			return WordCounter.countFromLogographicCharacterCount(characterCount, locale);
		}
		return tokens == null ? 0 : tokens.size();		
	}
	
	@Override
	protected long countInTextUnit(ITextUnit textUnit) {
		if (textUnit == null) return 0;
		
		LocaleId srcLocale = getSourceLocale();		
		TextContainer source = textUnit.getSource();
		
		// Individual segments metrics
		long segCount = 0;
		long segmentsCount = 0;
		long textContainerCount = 0;
		
		ISegments segs = source.getSegments();
		if (segs != null) {
			for (Segment seg : segs) {
				segCount = count(seg, srcLocale);
				segmentsCount += segCount;
				saveToMetrics(seg, segCount);
			}
		}
		// TC metrics
		textContainerCount = count(source, srcLocale);
		saveToMetrics(source, textContainerCount);
		
		if (textContainerCount > 0) return textContainerCount;  
		if (segmentsCount > 0) return segmentsCount;
		return 0;
	}
}
