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

import java.util.TreeMap;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.steps.tokenization.common.AbstractLexer;
import net.sf.okapi.steps.tokenization.common.Lexem;
import net.sf.okapi.steps.tokenization.common.Lexems;
import net.sf.okapi.steps.tokenization.common.LexerRule;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;
import com.ibm.icu.util.ULocale;

public class RbbiLexer extends AbstractLexer {

	// Cache for iterators reuse
	private TreeMap <LocaleId, RuleBasedBreakIterator> iterators = new TreeMap <LocaleId, RuleBasedBreakIterator>();

	private RuleBasedBreakIterator iterator = null;
	private int start;
	private int end;
	private String text;
	
	@Override
	public void lexer_init() {
		
	}
	
	@Override
	public boolean lexer_hasNext() {
		
		return end != BreakIterator.DONE;
	}

	@Override
	public Lexem lexer_next() {
		
		end = iterator.next();
		if (end == BreakIterator.DONE) return null;
		
		if (start >= end) return null;
		
		int lexemId = iterator.getRuleStatus();
		Lexem lexem = new Lexem(lexemId, text.substring(start,end), start, end);
		
		start = end; // Prepare for the next iteration
		
		return lexem;
	}

	public static String formatRule(String buffer, String name, String description, String rule, int lexemId) {
		
		buffer = Util.normalizeNewlines(buffer);
		rule = rule.replace("\\", "\\\\");
		
		String part1 = String.format("\\$%s = %s;", name, rule);
		String part2 = String.format("\\$%s {%d};", name, lexemId);
		
		buffer = buffer.replaceFirst("(!!forward;)", String.format("%s$0", part1));
		buffer = buffer.replaceFirst("(!!reverse;)", String.format("%s$0", part2));
		
		return buffer;
	}
	
	@Override
	public void lexer_open(String text, LocaleId language, Tokens tokens) {
		
		if (Util.isEmpty(text)) {
			cancel();
			return;
		}
		this.text = text;
		
		if ( iterators.containsKey(language) ) {
			iterator = iterators.get(language);
		}
		else {
			iterator = (RuleBasedBreakIterator)BreakIterator.getWordInstance(
				ULocale.createCanonical(language.toString()));
			String defaultRules = iterator.toString();
			
			// Collect rules for the language, combine with defaultRules
			String newRules = defaultRules;			
			
			for (LexerRule rule : getRules()) {
				
				boolean isInternal = Util.isEmpty(rule.getPattern());
				
				if (checkRule(rule, language) && !isInternal) {
					
					newRules = formatRule(newRules, rule.getName(), rule.getDescription(), rule.getPattern(), rule.getLexemId());
				}					
			}				
			
			// Recreate iterator for the language(with new rules), store for future reuse
			iterator = new RuleBasedBreakIterator(newRules); 
			iterators.put(language, iterator);
		}

		if ( iterator == null ) return;		
		iterator.setText(text);
		
		// Sets the current iteration position to the beginning of the text
		start = iterator.first();
		end = start;
	}

	public Lexems process(String text, LocaleId language, Tokens tokens) {
		
		return null;
	}

}
