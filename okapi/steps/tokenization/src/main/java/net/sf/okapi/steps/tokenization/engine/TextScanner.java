package net.sf.okapi.steps.tokenization.engine;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.tokenization.common.AbstractLexer;
import net.sf.okapi.steps.tokenization.common.Lexem;
import net.sf.okapi.steps.tokenization.common.Lexems;
import net.sf.okapi.steps.tokenization.common.LexerRule;
import net.sf.okapi.steps.tokenization.common.LexerRules;
import net.sf.okapi.steps.tokenization.common.RegexRule;
import net.sf.okapi.steps.tokenization.common.RegexRules;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

public class TextScanner extends AbstractLexer {

	private LinkedList<Lexem> queue;
	private LinkedList<LexerRule> rulesQueue;
	private boolean hasNext;
	private LexerRules rules;
	private LinkedHashMap<LexerRule, Pattern> patterns;
	private String text;
	private LocaleId language;	
		
	@Override
	protected Class<? extends LexerRules> lexer_getRulesClass() {
		return RegexRules.class;
	}
	
	@Override
	protected boolean lexer_hasNext() {
		return hasNext;
	}

	@Override
	protected void lexer_init() {
		queue = new LinkedList<Lexem>();
		rulesQueue = new LinkedList<LexerRule>();
		
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
		if (!queue.isEmpty())
			return queue.poll();
		
		do {
			
			RegexRule rule = null;
			
			if (!rulesQueue.isEmpty())
				rule = (RegexRule) rulesQueue.poll();
						
			//while (rule != null && !rule.supportsLanguage(language) && !rule.isEnabled()) {
			while (rule != null && !checkRule(rule, language)) {
				
				if (rulesQueue.isEmpty())
					rule = null;
				else
					rule = (RegexRule) rulesQueue.poll();
			}
			
			if (rule != null) {
				
				Pattern pattern = patterns.get(rule);
				if (pattern != null) {
				
					// Extract lexems from text					
					Matcher matcher = pattern.matcher(text);
					int groupIndex = rule.getRegexGroup();
					
				    while (matcher.find()) {
				    	
				    	int start = matcher.start(groupIndex);
				    	int end = matcher.end(groupIndex);
				    	
				    	if (start > -1 && end > -1)
				    		queue.add(new Lexem(rule.getLexemId(), matcher.group(groupIndex), start, end));
				    }
					
					return queue.poll();
				}
			}
									
		} while(!rulesQueue.isEmpty() && queue.isEmpty()); // No lexems were extracted (queue is empty), try next rule
		
		hasNext = false;
		return null;
	}

	@Override
	protected void lexer_open(String text, LocaleId language, Tokens tokens) {
		this.text = text;
		this.language = language;
		
		queue.clear();
		rulesQueue.clear();		
		rulesQueue.addAll(rules);
		
		hasNext = true;
	}

	public Lexems process(String text, LocaleId language, Tokens tokens) {
		return null; // Not used
	}

}
