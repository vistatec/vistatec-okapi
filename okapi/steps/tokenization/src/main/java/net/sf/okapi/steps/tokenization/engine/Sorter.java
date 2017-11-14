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

import java.util.Collections;
import java.util.Comparator;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.tokenization.common.AbstractLexer;
import net.sf.okapi.steps.tokenization.common.Lexem;
import net.sf.okapi.steps.tokenization.common.Lexems;
import net.sf.okapi.steps.tokenization.common.Token;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

public class Sorter extends AbstractLexer {

	@Override
	protected boolean lexer_hasNext() {
		return false;
	}

	@Override
	protected void lexer_init() {
	}

	@Override
	protected Lexem lexer_next() {
		return null;
	}

	@Override
	protected void lexer_open(String text, LocaleId language, Tokens tokens) {
	}

	private Comparator<Token> rangeComparator = new Comparator<Token>() {
      public int compare(Token token1, Token token2) {

      	int s1 = token1.getLexem().getRange().start;
      	int s2 = token2.getLexem().getRange().start;
      	
      	if (s1 < s2) return -1;        	      	
      	if (s1 > s2) return 1;
      	
      	if (s1 == s2) {
      		
      		int e1 = token1.getLexem().getRange().end;
          	int e2 = token2.getLexem().getRange().end;

          	// Longer tokens go first
      		if (e1 < e2) return 1;
      		if (e1 > e2) return -1;
      	}
      	
      	return 0;
      }    
	};

	public Lexems process(String text, LocaleId language, Tokens tokens) {
		Collections.sort(tokens, rangeComparator);
		return null;
	}

}
