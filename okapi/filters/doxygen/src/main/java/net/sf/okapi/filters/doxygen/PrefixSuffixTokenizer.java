package net.sf.okapi.filters.doxygen;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrefixSuffixTokenizer extends RegexTokenizer implements Iterable<PrefixSuffixTokenizer.Token> {
	
	private Matcher currentSuffixMatcher;
	private boolean switcher = false;
	
	public PrefixSuffixTokenizer(Map<Pattern, Pattern> delimiters, String string)
	{
		s = string;
		
		if (string == null) return;
		
		for (Map.Entry<Pattern, Pattern> e : delimiters.entrySet()) {
			
			Matcher prefixMatcher = e.getKey().matcher(string);
			Matcher suffixMatcher = e.getValue().matcher(string);
			
			matchers.put(prefixMatcher, suffixMatcher);
		}
	}
	
	private Matcher getSuffixMatcher()
	{
		if (currentSuffixMatcher != null)
			return currentSuffixMatcher;
		
		Matcher newMatcher = null;
		
		Matcher p = getPrefixMatcher();
		
		if (p != null && !firstRun) {
			Matcher s = matchers.get(p);
			newMatcher = s.find(p.end()) ? s : null;
		}
		
		currentSuffixMatcher = newMatcher;
		
		return currentSuffixMatcher;
	}
	
	
	@Override
	public Iterator<Token> iterator() {
		return new Iterator<Token>()
		{
			
			@Override
			public boolean hasNext()
			{
				if (s == null || s.length() < 1) return false;
				
				if (firstRun) return true;
				
				return getPrefixMatcher() != null || getSuffixMatcher() != null;
			}
			
			@Override
			public Token next()
			{
				Matcher front;
				Matcher back;
				
				if (switcher) {
					front = getPrefixMatcher();
					back = getSuffixMatcher();
					currentPrefixMatcher = null;
				} else {
					front = getSuffixMatcher();
					back = getPrefixMatcher();
					currentSuffixMatcher = null;
				}
				switcher = !switcher;
				
				i = back != null ? back.end() : s.length();
				
				if (firstRun) firstRun = false;
				
				return new Token(front, back);
			}
			
			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}