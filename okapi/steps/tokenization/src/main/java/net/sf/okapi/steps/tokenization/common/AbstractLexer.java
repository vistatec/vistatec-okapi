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

package net.sf.okapi.steps.tokenization.common;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.lib.extra.Component;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLexer extends Component implements ILexer {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private boolean cancelled = false;
	//private int lexerId = 0;
	private LexerRules rules;
		
	protected abstract void lexer_init();
	protected abstract boolean lexer_hasNext();
	protected abstract Lexem lexer_next();
	protected abstract void lexer_open(String text, LocaleId language, Tokens tokens);
	
	public AbstractLexer() {
		
		super();
		
		Class<? extends LexerRules> rulesClass = lexer_getRulesClass();
		if (rulesClass == null) return;
		
		try {
			rules = rulesClass.newInstance();
			
		} catch (InstantiationException e) {

			logger.debug("Lexer rules instantialion falied: {}", e.getMessage());
			
		} catch (IllegalAccessException e) {
			
			logger.debug("Lexer rules instantialion falied: {}", e.getMessage());
		}
	}
	
	public void init() {
	
		lexer_init();
	}
	
	public void cancel() {
		
		cancelled = true;
	}

	public void close() {				
	}

	public LexerRules getRules() {

		Class<? extends LexerRules> classRef = lexer_getRulesClass();
		
		return (classRef != null) ? classRef.cast(rules): null;
		//return rules;
	}
	
	public boolean hasNext() {
		
		return !cancelled && lexer_hasNext();
	}
	
	public Lexem next() {
		
		if (cancelled) return null;
		
		return lexer_next();
	}
	
	public void open(String text, LocaleId language, Tokens tokens) {
		
		cancelled = false;
		
		lexer_open(text, language, tokens);
	}

	public void setRules(LexerRules rules) {
		
		this.rules = rules;
	}
	
	protected boolean checkRule(LexerRule rule, LocaleId language) {
		return (( rule != null ) && rule.supportsLanguage(language) && rule.isEnabled());
	}
	
	protected boolean checkRule(LexerRule rule) {
		
		return rule != null && rule.isEnabled();
	}

	protected Class<? extends LexerRules> lexer_getRulesClass() {
		
		return LexerRules.class;
	}
}
