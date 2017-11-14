/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.okapi.common.exceptions.OkapiIOException;

/**
 * Helper methods to manipulate strings.
 */
public final class StringUtil {
	
	private static int max(int x1, int x2) {
		return (x1 > x2 ? x1 : x2);
	}

	private static int max(int x1, int x2, int x3, int x4) {
		return max(max(x1, x2), max(x3, x4));
	}

	private static TraceBack next(TraceBack tb, TraceBack[][] tba) {
		TraceBack tb2 = tb;
		return tba[tb2.i][tb2.j];
	}
	
	private static float calculateDiceCoefficient(int intersection, int size1, int size2) {
		return (float) ((2.0f * (float) intersection)) / (float) (size1 + size2) * 100.0f;
	}
	
	/**
	 * Longest Common Subsequence algorithm on {@link CharSequence}s.
	 * 
	 * @param seq1
	 *            {@link CharSequence} one
	 * @param seq2
	 *            {@link CharSequence} two
	 * @return the score based on the length of the common subsequence and the input sequences
	 */
	public static float LcsEditDistance(CharSequence seq1, CharSequence seq2) {
		int matches = 0;
		int d = 1;
		int n = seq1.length(), m = seq2.length();
		int[][] F = new int[n + 1][m + 1]; // Accumulate scores
		TraceBack[][] T = new TraceBack[n + 1][m + 1]; // path traceback
		int s = 0;
		int maxi = n, maxj = m;
		int maxval = Integer.MIN_VALUE;
		TraceBack start;

		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				s = 0;
				if (seq1.charAt(i - 1) == seq2.charAt(j - 1))
					s = 2;

				int val = max(0, F[i - 1][j - 1] + s, F[i - 1][j] - d, F[i][j - 1] - d);
				F[i][j] = val;
				if (val == 0) {
					T[i][j] = null;
				}
				else if (val == F[i - 1][j - 1] + s) {
					T[i][j] = new TraceBack(i - 1, j - 1);
				}
				else if (val == F[i - 1][j] - d) {
					T[i][j] = new TraceBack(i - 1, j);
				}
				else if (val == F[i][j - 1] - d) {
					T[i][j] = new TraceBack(i, j - 1);
				}
				if (val > maxval) {
					maxval = val;
					maxi = i;
					maxj = j;
				}
			}
		}
		start = new TraceBack(maxi, maxj);

		// retrace the optimal path and calculate score
		matches = 0;
		TraceBack tb = start;
		int i = tb.i;
		int j = tb.j;
		while ((tb = next(tb, T)) != null) {
			i = tb.i;
			j = tb.j;
			if (seq1.charAt(i) == seq2.charAt(j)) {
				matches++;
			}
		}
		
		return calculateDiceCoefficient(matches, seq1.length(), seq2.length());
	}

	// String formatting
	
	/**
	 * Returns a title-case representation of a given string. The first character is capitalized, following
	 * characters are in lower case.
	 * @param st the give string.
	 * @return a copy of the given string normalized to the title case. 
	 */
	public static String titleCase(String st) {
		if (Util.isEmpty(st)) 
			return st;
		
		return st.substring(0,1).toUpperCase() + st.substring(1).toLowerCase();
	}
	
	/**
	 * Removes qualifiers (quotation marks etc.) around text in a given string. 
	 * @param st the given string.
	 * @param startQualifier the qualifier to be removed before the given string.
	 * @param endQualifier the qualifier to be removed after the given string.
	 * @return a copy of the given string without qualifiers.
	 */
	public static String removeQualifiers(String st, String startQualifier, String endQualifier) {
	
		if (Util.isEmpty(st)) return st; 
		if (Util.isEmpty(startQualifier)) return st;
		if (Util.isEmpty(endQualifier)) return st;
		
		int startQualifierLen = startQualifier.length();
		int endQualifierLen = endQualifier.length();
		
		if (st.startsWith(startQualifier) && st.endsWith(endQualifier))
			return st.substring(startQualifierLen, Util.getLength(st) - endQualifierLen);
			
		return st;
	}
	
	/**
	 * Return any qualifiers (quotation marks etc.) around text in a given string. 
	 * @param st the given string.
	 * @return the qualifier
	 */
	public static String getQualifier(String st) {
		if (Util.isEmpty(st))
			return "";
		
		if (st.startsWith("\"") && st.endsWith("\""))
			return "\"";
			
		if (st.startsWith("'") && st.endsWith("'"))
			return "'";
		
		return "";
	}
	
	/**
	 * Test if there are qualifiers (quotation marks etc.) around text in a given string. 
	 * @param st the given string.
	 * @return true if the string has qualifiers
	 */
	public static boolean hasQualifiers(String st) {
		if (Util.isEmpty(st))
			return false;
		
		if (st.startsWith("\"") && st.endsWith("\""))
			return true;
			
		if (st.startsWith("'") && st.endsWith("'"))
			return true;
		
		return false;
	}
	
	/**
	 * Removes qualifiers (quotation marks etc.) around text in a given string. 
	 * @param st the given string.
	 * @param qualifier the qualifier to be removed before and after text in the string.
	 * @return a copy of the given string without qualifiers.
	 */
	public static String removeQualifiers(String st, String qualifier) {
	
		if (Util.isEmpty(st) || Util.isEmpty(qualifier))
			return st;
		
		int qualifierLen = qualifier.length();
		
		if (st.startsWith(qualifier) && st.endsWith(qualifier))
			return st.substring(qualifierLen, Util.getLength(st) - qualifierLen);
			
		return st;
	}
	
	/**
	 * Removes quotation marks (double or single) around text in a given string. 
	 * @param st the given string.
	 * @return a copy of the given string without quotation marks.
	 */
	public static String removeAnyQualifiers(String st) {
		if (Util.isEmpty(st)) {
			return st;
		}
		
		String t = removeQualifiers(st, "'");
		if (!Util.isEmpty(t) && Util.getLength(t) >= 2 && hasQualifiers(t)) {
			return removeQualifiers(t, "\""); 
		}
		
		return t;
	}
	
	/**
	 * Removes quotation marks around text in a given string. 
	 * @param st the given string.
	 * @return a copy of the given string without quotation marks.
	 */
	public static String removeQualifiers(String st) {
	
		return removeQualifiers(st, "\""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Converts line breaks in a given string to the Unix standard (\n).
	 * @param string the given string.
	 * @return a copy of the given string, all line breaks are \n.
	 */
	public static String normalizeLineBreaks(String string) {
		
		String res = string;
		
		if (!Util.isEmpty(res)) {
		
			res = res.replaceAll("\r\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			res = res.replace("\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			res = res.replace("\r", "\n");  //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return res;
	}

	private static boolean checkRegex(String regex) {
		
		try {			
			Pattern.compile(regex);		
		} 
		catch (PatternSyntaxException e) {			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Converts shell wildcards (e.g. * and ?) in a given string to its Java regex representation.
	 * @param string the given string.
	 * @return a copy of the given string, all wildcards are converted into a correct Java regular expression. 
	 * The result is checked for being a correct regex pattern. If it is not, then the given original string is returned as
	 * being most likely already a regex pattern.
	 */
	public static String normalizeWildcards(String string) {

		if (Util.isEmpty(string)) return string;
		if (!containsWildcards(string)) return string;
						
		String normalized = string.replaceAll("\\?", ".").replaceAll("\\*", ".*?");
		
		// Make sure we're not normalizing a correct regex thus damaging it
		return checkRegex(normalized) ? normalized : string;
	}
	
	/**
	 * Detects if a given string contains shell wildcard characters (e.g. * and ?).
	 * @param string the given string.
	 * @return true if the string contains the asterisk (*) or question mark (?).
	 */
	public static boolean containsWildcards(String string) {
	
		if (Util.isEmpty(string)) return false;
	
		return string.indexOf('*') != -1 || string.indexOf('?') != -1;
	}
		
	/**
	 * Detects if a given string matches a given pattern (not necessarily a regex), possibly containing wildcards
	 * @param string the given string (no-wildcards)
	 * @param pattern the pattern containing wildcards to match against
	 * @param filenameMode indicates if the given string should be considered a file name
	 * @return true if the given string matches the given pattern
	 */
	public static boolean matchesWildcard(String string, String pattern, boolean filenameMode) {
		if (filenameMode) {
			String filename = Util.getFilename(string, true);
			String patternFilename = Util.getFilename(pattern, true);
			
			String filePath = Util.getDirectoryName(string);
			String patternFilePath = Util.getDirectoryName(pattern);
			
			boolean pathMatches;
			if (Util.isEmpty(patternFilePath)) 
				pathMatches = true; // word/settings/filename.ext matches *.ext
			else
				pathMatches = Pattern.matches(normalizeWildcards(patternFilePath), filePath); // word/settings/filename.ext matches word/*/*.ext
			
			boolean filenameMatches = Pattern.matches(normalizeWildcards(patternFilename), filename);			
			
			return pathMatches && filenameMatches;				
		}
		return Pattern.matches(normalizeWildcards(pattern), string);
	}
	
	/**
	 * Detects if a given string matches a given pattern (not necessarily a regex), possibly containing wildcards
	 * @param string the given string (no-wildcards)
	 * @param pattern the pattern containing wildcards to match against
	 * @return true if the given string matches the given pattern
	 */
	public static boolean matchesWildcard(String string, String pattern) {
		return matchesWildcard(string, pattern, false);
	}

	public static String[] split(String string, String delimRegex, int group) {		
		String delimPlaceholder = "<delimiter>";
		
		string = RegexUtil.replaceAll(string, delimRegex, group, delimPlaceholder);
		return ListUtil.stringAsArray(string, delimPlaceholder);
	}
	
	public static String[] split(String string, String delimRegex) {		
		return split(string, delimRegex, 0);
	}
	
	/**
	 * Returns a number of occurrences of a given substring in a given string.
	 * @param str the given string.
	 * @param substr the given substring being sought.
	 * @return the number of occurrences of the substring in the string.
	 */
	public static int getNumOccurrences(String str, String substr){		
	    String temp = str;
		int count = 0;
		int i = temp.indexOf(substr);
		
		while(i >= 0){
			
		    count++;
		    temp = temp.substring(i + 1);
		    i = temp.indexOf(substr);
		}
		
		return count;
	}

	/**
	 * Checks if a given string contains only whitespace characters.
	 * @param str the given string
	 * @return true if the given string is whitespace
	 */
	public static boolean isWhitespace(String str) {
		if (Util.isEmpty(str)) return false; // "" is neither whitespace
		
		return "".equals(str.trim());
	}
	
	/**
	 * Returns a new string padded with a given character repeated given times.
	 * @param length length of the new string
	 * @param c the character to pad the string
	 * @return the new string
	 */
	public static String getString(int length, char c) {
		if (length < 0) length = 0;
		
		char[] chars = new char[length];
		Arrays.fill(chars, c);
		return new String(chars);
	}

	/**
	 * Pads a range of a given string with a given character. 
	 * @param string the given string
	 * @param startPos start position of the pad range (including)
	 * @param endPos end position of the pad range (excluding)
	 * @param padder the character to pad the range with
	 * @return the given string with the given range padded with the given char
	 */
	public static String padString(String string, int startPos, int endPos, char padder) {
		if (startPos < 0) startPos = 0;		
		char[] chars = string.toCharArray();
		
		for (int i = startPos; i < Math.min(endPos, string.length()); i++) {
			chars[i] = padder;
		}
		
		return new String(chars);
	}

	public static String substring(String string, int start, int end) {
		int len = string.length();
		if (start < 0) return null;
		if (end < 0) return null;
		if (start > len) return null;
		if (end > len) end = len;
		if (start > end) return null;
		return string.substring(start, end);
	}
	
	public static String charsToString(Set<Character> set) {
		char[] chars = new char[set.size()];
		int i = 0;
		for (Character ch : set) {
			chars[i++] = ch;
		}
		return String.valueOf(chars);
	}

	/**
	 * Returns the reversed version of a given string, e.g. "cba" for "abc".
	 * @param str The given string
	 * @return The reversed string
	 */
	public static String mirrorString(String str) {
		StringBuffer buffer = new StringBuffer(str).reverse();
		return buffer.toString();
	}
	
	public static String readString(File path) {
		try (InputStream is = new BufferedInputStream(new FileInputStream(path))) {
			return StreamUtil.streamUtf8AsString(is);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}
	
	public static String readString(URL url) {
		try {
			return StreamUtil.streamUtf8AsString(url.openStream());
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}
	
	public static void writeString(String str, File path) {
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(path))) {
			writeString(str, os);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}
	
	public static void writeString(String str, OutputStream os) {
		try (InputStream is = StreamUtil.stringAsStream(str, "UTF-8")) {
			StreamUtil.copy(is, os);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}
	
	private static final Pattern UNICODE_WHITESPACE_PATTERN = Pattern.compile("\\p{Z}|\\s");	
	public static String removeWhiteSpace(String s) {
		if (s == null) return null;
	    Matcher m = UNICODE_WHITESPACE_PATTERN.matcher(s);
	    return m.replaceAll("");
	}

	/**
	 * chomp single newline combination from end of string
	 * Copied from Apache Commons StrinUtil (http://www.apache.org/licenses/LICENSE-2.0)
	 * @param str the string to chop.
	 * @return the chopped string.
	 */
	public static String chomp(final String str) {
		if (Util.isEmpty(str)) {
			return str;
		}

		if (str.length() == 1) {
			final char ch = str.charAt(0);
			if (ch == '\r' || ch == '\n') {
				return "";
			}
			return str;
		}
		int lastIdx = str.length() - 1;
		final char last = str.charAt(lastIdx);

		if (last == '\n') {
			if (str.charAt(lastIdx - 1) == '\r') {
				lastIdx--;
			}
		} else if (last != '\r') {
			lastIdx++;
		}
		return str.substring(0, lastIdx);
	}
}
