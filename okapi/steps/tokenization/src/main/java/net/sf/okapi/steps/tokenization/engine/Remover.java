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

package net.sf.okapi.steps.tokenization.engine;

import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.tokenization.common.AbstractLexer;
import net.sf.okapi.steps.tokenization.common.Lexem;
import net.sf.okapi.steps.tokenization.common.Lexems;
import net.sf.okapi.steps.tokenization.common.LexerRule;
import net.sf.okapi.steps.tokenization.common.Token;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

public class Remover extends AbstractLexer {
	
	@Override
	public boolean lexer_hasNext() {
		
		return false; // Iterator is not used
	}

	@Override
	public void lexer_init() {

	}

	@Override
	public Lexem lexer_next() {

		return null; // Iterator is not used
	}

	@Override
	public void lexer_open(String text, LocaleId language, Tokens tokens) {
		
		 // Iterator is not used
	}

	public Lexems process(String text, LocaleId language, Tokens tokens) {

		if (tokens == null) return null;
				
		for (LexerRule rule : getRules()) {

			if (!checkRule(rule, language)) continue;
			
			List<Integer> inTokenIDs = rule.getInTokenIDs();
			
			for (Token token : tokens) {
				
				if (token.isDeleted()) continue;
				
				if (inTokenIDs.contains(token.getTokenId())) // Remove listed tokens
					token.delete();
			}				
		}
		
		return null;
	}

}
