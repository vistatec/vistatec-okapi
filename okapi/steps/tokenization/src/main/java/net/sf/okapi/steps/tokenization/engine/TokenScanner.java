package net.sf.okapi.steps.tokenization.engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
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

public class TokenScanner extends AbstractLexer {

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
				
				// if (token.isDeleted()) continue;
			
				if (inTokenIDs.contains(token.getTokenId())) {
					
					Range r = token.getRange();
					Matcher matcher = pattern.matcher(token.getValue());
					int groupIndex = rule.getRegexGroup();
					
				    while (matcher.find()) {
				    	
				    	int start = matcher.start(groupIndex);
				    	int end = matcher.end(groupIndex);
				    	
				    	if (start > -1 && end > -1) {
				    		
				    		Lexem lexem = new Lexem(rule.getLexemId(), matcher.group(groupIndex), 
					    			r.start + start, r.start + end);
				    		lexem.setAnnotation(new InputTokenAnnotation(token));
				    		lexem.setImmutable(true);
				    		lexems.add(lexem);
				    		
					    	if (!rule.getKeepInput())
					    		token.delete(); // Delete the original token, other rules are still able to extract parts of it 				    	
				    	}				    		
				    }
				}
			}				
		}
		
		return lexems;
	}
}
