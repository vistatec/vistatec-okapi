package net.sf.okapi.filters.doxygen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DelimiterTokenizer extends RegexTokenizer implements Iterable<DelimiterTokenizer.Token> {
	
	private MatchRecord lastMatch;
	
	public DelimiterTokenizer(Pattern pattern, String string)
	{
		HashMap<Pattern, Object> p = new HashMap<Pattern, Object>();
		p.put(pattern, null);
		init(p, string);
	}
	
	public DelimiterTokenizer(Map<Pattern, Object> patterns, String string)
	{
		init(patterns, string);
	}
	
	private void init(Map<Pattern, Object> patterns, String string)
	{
		s = string;
		
		if (string != null)
			for (Entry<Pattern, Object> e : patterns.entrySet())
				matchers.put(e.getKey().matcher(string), null);
	}

	private MatchRecord getLastMatch()
	{
		if (lastMatch != null)
			return lastMatch;
		
		Matcher m = getPrefixMatcher();
		
		if (m != null && !firstRun)
			lastMatch = new MatchRecord(m);
		
		return lastMatch;
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
				
				return getPrefixMatcher() != null || getLastMatch() != null;
			}
			
			@Override
			public Token next()
			{
				MatchRecord r = getLastMatch();
				lastMatch = null;
				
				if (!firstRun) currentPrefixMatcher = null;
				Matcher m = getPrefixMatcher();
				
				i = m != null ? m.end() : s.length();
				
				if (firstRun) firstRun = false;
				
				return new Token(r, m);
			}
			
			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public final class Token extends RegexTokenizer.Token
	{
		
		public Token(MatchRecord front, MatchRecord back)
		{
			super(front, back);
		}
		
		public Token(MatchRecord front, Matcher back)
		{
			super(front, back != null ? new MatchRecord(back) : null);
		}

		public String delimiter() { return super.prefix(); }
		
		public Pattern delimiterPattern() { return super.prefixPattern(); }
		
		@Override
		public String suffix() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Pattern suffixPattern() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}
}
