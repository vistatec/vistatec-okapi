/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.steps.tokenization.common.Token;
import net.sf.okapi.steps.tokenization.tokens.Tokens;
import net.sf.okapi.steps.wordcount.CharacterCounter;

public abstract class TokenCharacterCountStep extends TokenCountStep {

	@Override
	protected long count(Segment segment, LocaleId locale) {
		return countTokenChars(getTokens(segment, locale), locale);
	}
	
	@Override
	protected long count(TextContainer textContainer, LocaleId locale) {
		return countTokenChars(getTokens(textContainer, locale), locale);
	}
	
	/**
	 * Return the total character count (calculated per {@link GMX#TotalCharacterCount})
	 * of all supplied tokens.
	 */
	public static long countTokenChars(Tokens tokens, LocaleId locale) {
		if (tokens == null || locale == null) {
			return 0L;
		}
		long total = 0L;
		for (Token t : tokens) {
			total += CharacterCounter.count(t.getValue(), locale);
		}
		return total;
	}
}
