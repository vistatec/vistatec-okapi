package net.sf.okapi.steps.tokenization.engine;

import java.util.LinkedHashMap;
import java.util.List;

import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.tokenization.common.AbstractLexer;
import net.sf.okapi.steps.tokenization.common.InputTokenAnnotation;
import net.sf.okapi.steps.tokenization.common.Lexem;
import net.sf.okapi.steps.tokenization.common.Lexems;
import net.sf.okapi.steps.tokenization.common.LexerRule;
import net.sf.okapi.steps.tokenization.common.LexerRules;
import net.sf.okapi.steps.tokenization.common.Token;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

public class LookUpLexer extends AbstractLexer {

	private LexerRules rules;
	private LinkedHashMap<LexerRule, List<String>> dictionaries;
	
	@Override
	protected boolean lexer_hasNext() {

		return false;
	}

	@Override
	protected void lexer_init() {
		
		rules = getRules();
		dictionaries = new LinkedHashMap<LexerRule, List<String>>(); 
		
		for (LexerRule rule : rules) {
			
			if (!checkRule(rule)) continue;
			
			String dictionaryLocation = rule.getPattern();
			List<String> dictionary = ListUtil.loadList(this.getClass(), dictionaryLocation);
			
			dictionaries.put(rule, dictionary);
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
		
		for (LexerRule rule : rules) {
			
			if (!checkRule(rule, language)) continue;
			List<Integer> inTokenIDs = rule.getInTokenIDs();
			
			List<String> dictionary = dictionaries.get(rule);
			if (dictionary == null) continue;
			
			for (Token token : tokens)			
				if (inTokenIDs.contains(token.getTokenId())) {
				
					if (dictionary.contains(token.getValue())) {

						Lexem lexem = new Lexem(rule.getLexemId(), token.getValue(), token.getRange());
						lexem.setAnnotation(new InputTokenAnnotation(token));
				    	lexems.add(lexem);
				    	
				    	if (!rule.getKeepInput())
				    		token.delete(); // Remove replaced token
				    }
				}
		}
		
		return lexems;
	}


}
