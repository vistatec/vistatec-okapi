package net.sf.okapi.filters.doxygen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RegexTokenizer {

	protected String s;
	protected int i = 0;
	
	protected boolean firstRun = true;
	protected IdentityHashMap<Matcher, Matcher> matchers = new IdentityHashMap<Matcher, Matcher>();
	protected Matcher currentPrefixMatcher = null;
	
	private HashMap<Matcher, HashSet<Tuple>> history = new HashMap<Matcher, HashSet<Tuple>>();
	
	protected Matcher getPrefixMatcher()
	{
		if (currentPrefixMatcher != null)
			return currentPrefixMatcher;
		
		Matcher newMatcher;
		
		while (true) {
			
			newMatcher = null;
		
			int location = s.length();
			
			for (Map.Entry<Matcher, Matcher> e : matchers.entrySet()) {
				Matcher m = e.getKey();
				if (m.find(i) && m.start() < location) {
					newMatcher = m;
					location = m.start();
				}
			}
			if (isDuplicate(newMatcher) && i < s.length()) i++;
			else break;
		}
		
		currentPrefixMatcher = newMatcher;
		cache(newMatcher);
		
		return newMatcher;
	}
	
	
	protected void cache(Matcher m) {
		
		if (m == null) return;
		
		// Only need to cache zero-length matches to prevent infinite loops.
		if (m.start() != m.end()) return;
		
		HashSet<Tuple> h = history.get(m);
		
		if (h == null) {
			h = new HashSet<Tuple>();
			history.put(m, h);
		}
		
		h.add(new Tuple(m.start(), m.end()));
	}

	protected boolean isDuplicate(Matcher m) {
		
		if (m == null || m.end() > i) return false;
		
		HashSet<Tuple> h = history.get(m);
		
		return h != null && h.contains(new Tuple(m.start(), m.end()));
	}
	
	public final class Tuple
	{
		private final int x;
		private final int y;
		
		public Tuple(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tuple other = (Tuple) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

		private RegexTokenizer getOuterType() {
			return RegexTokenizer.this;
		}
	}
	
	public class Token
	{
		private final MatchRecord f;
		private final MatchRecord b;
		
		public Token(MatchRecord front, MatchRecord back)
		{
			f = front;
			b = back;
		}
		
		public Token(Matcher front, Matcher back)
		{
			f = front != null ? new MatchRecord(front) : null;
			b = back != null ? new MatchRecord(back) : null;
		}
		
		public String toString()
		{
			int start = f != null ? f.result.end() : 0;
			
			if (start >= s.length()) return "";
			
			int end = b != null ? b.result.start() : s.length();
			
			return s.substring(start, end);
		}
		
		public String prefix()
		{
			return f != null ? f.result.group() : null;
		}
		
		public Pattern prefixPattern()
		{
			return f != null ? f.pattern : null;
		}
		
		public String suffix()
		{
			return b != null ? b.result.group() : null;
		}
		
		public Pattern suffixPattern()
		{
			return b != null ? b.pattern : null;
		}
	}
	
	public final class MatchRecord
	{
		public final MatchResult result;
		public final Pattern pattern;
		
		public MatchRecord(Matcher m)
		{
			result = m.toMatchResult();
			pattern = m.pattern();
		}
	}
}
