/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of helper function for working with regular expressions.
 */
public class RegexUtil {
	
	private static final Pattern QUOTED_AREA = Pattern.compile("\\\\Q(.+?)\\\\E");
	private static final Pattern BACKREF_PATTERN = Pattern.compile("\\\\([1-9][0-9]*)");
	
	private static Map<String, Pattern> patternCache = new ConcurrentHashMap<String, Pattern>();

	public static Pattern getPattern(String regex) {		
		Pattern pattern = patternCache.get(regex);
		if (pattern == null) {
			
			pattern = Pattern.compile(regex);
			patternCache.put(regex, pattern);
		}
		
		return pattern;
	}
	
	public static String find(String searchIn, String findWhatRegex, int group) {
		return find(searchIn, getPattern(findWhatRegex), group);
	}
	
	public static String find(String searchIn, Pattern findWhatPattern, int group) {
		Matcher matcher = findWhatPattern.matcher(searchIn);
		if (matcher.find())
			return matcher.group(group);
		else				
			return "";
	}
	
	public static List<String> findAll(String searchIn, String findWhatRegex, int group) {
		return findAll(searchIn, getPattern(findWhatRegex), group);
	}
	
	public static List<String> findAll(String searchIn, Pattern findWhatPattern, int group) {
		List<String> found = new LinkedList<String>();
		Matcher matcher = findWhatPattern.matcher(searchIn);
		while (matcher.find()) {
			found.add(matcher.group(group));
		}
		return found;
	}
	
	public static String replaceAll(String string, Pattern pattern, int group, String replacement) {	
	    Matcher matcher = pattern.matcher(string);
	    
	    // Replace all occurrences of pattern in input
	    StringBuilder buf = new StringBuilder();
	    
	    int start = 0;
	    int end = 0;
	    
	    while (matcher.find()) {	    
	        start = matcher.start(group);
	        if (start != -1) { // The group might not present in the match
	        	buf.append(string.substring(end, start));	        	
	        	if (replacement.contains("*")) {
	        		// Reference to the group itself
	        		buf.append(replacement.replaceAll("\\*", matcher.group(group)));
	        	}
	        	else {
	        		buf.append(replacement);
	        	}		        
		        end = matcher.end(group);
	        }	        
	    }
	    
	    buf.append(string.substring(end));
	    return buf.toString();
	}
	
	public static String replaceAll(String string, String regex, int group, String replacement) {		
		return replaceAll(string, getPattern(regex), group, replacement);
	}
	
	public static int countMatches(String string, String regex) {				
	    return countMatches(string, regex, 0);
	}
	
	public static int countMatches(String string, String regex, int matchLen) {	
		Pattern pattern = getPattern(regex);
	    Matcher matcher = pattern.matcher(string);
	    
	    int count = 0;
	    
	    while (matcher.find())
	    	if (matchLen == 0)
	    		count++;
	    	else
	    		count += string.substring(matcher.start(0), matcher.end(0)).length() / matchLen;
	    
	    return count;
	}
	
	public static int countLeadingQualifiers(String string, String qualifier) {		
		return countMatches(string, qualifier + "+\\b", qualifier.length());
	}
	
	public static int countTrailingQualifiers(String string, String qualifier) {		
		return countMatches(string, "\\b" + qualifier + "+", qualifier.length());
	}

	/**
	 * Escapes a given string for regex.
	 * @param str the given string
	 * @return escaped string
	 */
	public static String escape(String str) {
		str = str.replace("\\", "\\\\");
		str = str.replace("[", "\\[");
		str = str.replace("]", "\\]");
		str = str.replace("\"", "\\\"");
		str = str.replace("^", "\\^");
		str = str.replace("$", "\\$");
		str = str.replace(".", "\\.");
		str = str.replace("|", "\\|");
		str = str.replace("?", "\\?");
		str = str.replace("*", "\\*");
		str = str.replace("+", "\\+");
		str = str.replace("(", "\\(");
		str = str.replace(")", "\\)");
		str = str.replace("{", "\\{");
		str = str.replace("}", "\\}");		
        
        return str;
	}

	public static boolean matches(String st, Pattern pattern) {
		Matcher matcher = pattern.matcher(st);
		return matcher.matches();
	}
	
	public static boolean contains(String st, Pattern pattern) {
		Matcher matcher = pattern.matcher(st);
		return matcher.find();
	}
	
	public static List<Range> getQuotedAreas(String regex) {
		List<Range> quotedAreas = new ArrayList<Range>();
		// Determine areas between \Q and \E
		Matcher m = QUOTED_AREA.matcher(regex);
		while(m.find()) {
			quotedAreas.add(new Range(m.start(1), m.end(1) - 1));
		}
		return quotedAreas;
	}
	
	private static boolean isQuotedArea(int pos, List<Range> quotedAreas) {
		for (Range area : quotedAreas) {
			if (area.contains(pos))	return true;
		}
		return false;
	}
	
	public static int getGroupAtPos(String regex, int position) {
		int group = 0;
		int maxGroup = 0;
		boolean ignoreNext = false;
		List<Range> quotedAreas = getQuotedAreas(regex);
		
		String searchSt = regex.substring(0, position);
		for (int i = 0; i < searchSt.length(); i++) {
			if (ignoreNext) {
				ignoreNext = false;
				continue;
			}
			char ch = regex.charAt(i);
			if (ch == '\\') {
				ignoreNext = !isQuotedArea(i, quotedAreas);				
			}				
			else {				
				if (ch == '(' && !ignoreNext) {
					group = ++maxGroup;
				}
				// Group numbers are assigned based on left parenthesis
				else if (ch == ')' && !ignoreNext) {
					group--;
				} 
			}				
		}
		return group;
	}
	
	/**
	 * Adjust values in back references to capturing groups (like \1) of a given regex.
	 * This method needs to be called when a new group is added to a regex,
	 * and the regex contains back references to existing groups.
	 * Values in the references having number equal or greater than groupNum, should
	 * be increased by 1.
	 * @param regex the given regex containing back references to capturing groups. 
	 * @param groupNum the number of the new group.
	 * @return the given regex with updated references.
	 */
	public static String updateGroupReferences(String regex, int groupNum) {
		Matcher m = BACKREF_PATTERN.matcher(regex);
		List<String> values = new ArrayList<String>();
		
		// Collect group values in a length-sorted list
		while(m.find()) {
			String n = m.group(1);
			values.add(n);				
		}
		
		// Sorted by string length, not by int value
		Collections.sort(values, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1.length() < o2.length()) return 1;
				if (o1.length() > o2.length()) return -1;
				return 0;
			}				
		});
		
		// Replace group numbers starting from the longest to shortest to not replace 
		// parts of longer values
		for (String value : values) {
			int oldValue = Integer.valueOf(value);
			if (oldValue < groupNum - 1) continue;
			
			int newValue = oldValue + 1;
			regex = regex.replace("\\" + value, String.format("\\%d", newValue));
		}
		
		return regex;
	}
}
