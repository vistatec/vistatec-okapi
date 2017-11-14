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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.tokenization.common.AbstractLexer;
import net.sf.okapi.steps.tokenization.common.InputTokenAnnotation;
import net.sf.okapi.steps.tokenization.common.Lexem;
import net.sf.okapi.steps.tokenization.common.Lexems;
import net.sf.okapi.steps.tokenization.common.LexerRule;
import net.sf.okapi.steps.tokenization.common.LexerRules;
import net.sf.okapi.steps.tokenization.common.RegexRule;
import net.sf.okapi.steps.tokenization.common.RegexRules;
import net.sf.okapi.steps.tokenization.common.Token;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

public class TokenMatcher extends AbstractLexer {
	 
	private LexerRules rules;
	private LinkedHashMap<LexerRule, Pattern> patterns;  
	  
	@Override
		protected Class<? extends LexerRules> lexer_getRulesClass() {

			return RegexRules.class;
		}
	 
	@Override
	protected boolean lexer_hasNext() {

		return false;
	}

	@Override
	protected void lexer_init() {
		
//		patterns = new HashMap<LexerRule, Pattern>();
//		rules = getRules();
//		
//		for (LexerRule rule : rules) {
//			
//			Pattern pattern = Pattern.compile(rule.getPattern());
//			patterns.put(rule, pattern);
//		}
		
		patterns = new LinkedHashMap<LexerRule, Pattern>();
		rules = getRules();
		
		for (LexerRule item : rules) {
			
			RegexRule rule = (RegexRule) item;
			
			Pattern pattern = null;
			if (rule.getPattern() != null)
				pattern = Pattern.compile(rule.getPattern(), rule.getRegexOptions());
			
			patterns.put(rule, pattern);
		}
	}

	@Override
	protected Lexem lexer_next() {

		return null;
	}

	@Override
	protected void lexer_open(String text, LocaleId language, Tokens tokens) {

	}

	public Lexems process(String text, LocaleId language, Tokens tokens) {
		
		Lexems lexems = new Lexems();
		
		for (LexerRule item : rules) {
			
			RegexRule rule = (RegexRule) item;
			
			if (!checkRule(rule, language)) continue;
			List<Integer> inTokenIDs = rule.getInTokenIDs();
			
			Pattern pattern = patterns.get(rule);
			if (pattern == null) continue;
			
			for (Token token : tokens) {
				
				//if (token.isDeleted()) continue;
				
				if (inTokenIDs.contains(token.getTokenId())) {
					
					Matcher matcher = pattern.matcher(token.getValue());
					
				    if (matcher.matches()) {
				    	
				    	Lexem lexem = new Lexem(rule.getLexemId(), token.getValue(), token.getRange());
				    	lexem.setAnnotation(new InputTokenAnnotation(token));
				    	lexems.add(lexem);
				    	
				    	if (!rule.getKeepInput())
				    		token.delete(); // Remove replaced token				    	
				    }
				}
			}				
		}
		
		return lexems;
	}

}
