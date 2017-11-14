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

package net.sf.okapi.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StringUtilTest {

	@Test
	public void testRemoveQualifiers() {	
		assertEquals("qualified text", StringUtil.removeQualifiers("\"qualified text\""));
		assertEquals("qualified text", StringUtil.removeQualifiers("\"qualified text\"", "\""));
		assertEquals("qualified text", StringUtil.removeQualifiers("\'qualified text\'", "\'"));
		assertEquals("'qualified text'", StringUtil.removeQualifiers("\'qualified text\'", "\""));
		assertEquals("[qualified text]", StringUtil.removeQualifiers("((({[qualified text]})))", "((({", "})))"));
		assertEquals("qualified text", StringUtil.removeQualifiers("((({[qualified text]})))", "((({[", "]})))"));
	}
	
	@Test
	public void testTitleCase() {		
		assertEquals("Title case", StringUtil.titleCase("Title case"));
		assertEquals("Title case", StringUtil.titleCase("Title Case"));
		assertEquals("Title case", StringUtil.titleCase("title case"));
		assertEquals("Title case", StringUtil.titleCase("TITLE CASE"));
		assertEquals("Title case", StringUtil.titleCase("tITLE CaSE"));
	}
	
	@Test
	public void testNormalizeLineBreaks() {		
		assertEquals("line1\nline2", StringUtil.normalizeLineBreaks("line1\nline2"));
		assertEquals("line1\nline2\n", StringUtil.normalizeLineBreaks("line1\nline2\n"));
		
		assertEquals("line1\nline2", StringUtil.normalizeLineBreaks("line1\r\nline2"));
		assertEquals("line1\nline2\n", StringUtil.normalizeLineBreaks("line1\r\nline2\r\n"));
		
		assertEquals("line1\nline2", StringUtil.normalizeLineBreaks("line1\rline2"));
		assertEquals("line1\nline2\n", StringUtil.normalizeLineBreaks("line1\rline2\r"));
	}
	
	@Test
	public void testNormalizeWildcards() {		
		assertEquals("en.*?", StringUtil.normalizeWildcards("en*"));
		assertEquals("en.", StringUtil.normalizeWildcards("en?"));
		
		assertEquals("en.*?u.*?", StringUtil.normalizeWildcards("en*u*"));
		assertEquals("en.u.", StringUtil.normalizeWildcards("en?u?"));
		
		assertEquals("en.*?u.", StringUtil.normalizeWildcards("en*u?"));
		assertEquals("en.u.*?", StringUtil.normalizeWildcards("en?u*"));
		
		assertEquals("([\\d\\w-.]+?", StringUtil.normalizeWildcards("([\\d\\w-.]+?"));
	}
	
	@Test
	public void testContainsWildcards() {
	
		assertTrue(StringUtil.containsWildcards("t* has wildcards"));
		assertTrue(StringUtil.containsWildcards("t? has wildcards"));
		assertTrue(StringUtil.containsWildcards("([\\d\\w-.]+?"));
		assertFalse(StringUtil.containsWildcards("no wildcards"));
	}
	
	@Test
	public void testMatchesWildcard() {
		assertTrue(StringUtil.matchesWildcard("filename1.xml", "*.xml"));
		assertTrue(StringUtil.matchesWildcard("filename2.xml", "*.xml"));
		assertTrue(StringUtil.matchesWildcard(".xml", "*.xml"));
		assertFalse(StringUtil.matchesWildcard("filename1.dita", "*.xml")); // false
		assertTrue(StringUtil.matchesWildcard("filename1.dita", "*.*"));
		assertTrue(StringUtil.matchesWildcard("filename.*", "filename.*"));
		assertTrue(StringUtil.matchesWildcard("filename1.dita", "filename?.d?ta"));
		assertTrue(StringUtil.matchesWildcard("filename5.data", "filename?.d?ta"));
		assertFalse(StringUtil.matchesWildcard("filenames5.data", "filename?.d?ta")); // false		
		assertFalse(StringUtil.matchesWildcard("word/settings/filenames5.data", "filename?.d?ta")); // false		
		assertFalse(StringUtil.matchesWildcard("word/settings/filename5.data", "filename?.d?ta")); // false, !!! non-filename mode
		assertTrue(StringUtil.matchesWildcard("word/settings/filename5.data", "filename?.d?ta", true)); // filename mode
		assertTrue(StringUtil.matchesWildcard("word/settings/filename5.data", "word/settings/*.*"));
		assertTrue(StringUtil.matchesWildcard("word/settings/filename5.data", "word/*.*"));
		assertFalse(StringUtil.matchesWildcard("word/settings/filename5.data", "word/*.*", true)); // false
		assertTrue(StringUtil.matchesWildcard("word/settings/filename5.data", "word/settings/*.*", true));
	}
	
	@Test
	public void testSplit() {		
		String[] chunks = StringUtil.split("item1,   item2,item3,\t\nitem4", ",\\p{Space}*");
		
		assertEquals(4, chunks.length);
		assertEquals("item1", chunks[0]);
		assertEquals("item2", chunks[1]);
		assertEquals("item3", chunks[2]);
		assertEquals("item4", chunks[3]);
		
		chunks = StringUtil.split("item1,   item2 item3\t\nitem4", "[^,\\p{Space}+](\\p{Space}+)", 1);
		
		assertEquals(3, chunks.length);
		assertEquals("item1,   item2", chunks[0]);
		assertEquals("item3", chunks[1]);
		assertEquals("item4", chunks[2]);
	}
	
	@Test
	public void testGetNumOccurrences() {		
		assertEquals(3, StringUtil.getNumOccurrences("1 text 2 text 1 text 1 text 2", "1"));
		assertEquals(2, StringUtil.getNumOccurrences("1 text 2 text 1 text 1 text 2", "2"));
	}
	
	@Test
	public void testIsWhitespace() {
		assertTrue(StringUtil.isWhitespace("\t   \b\n\f\r"));
		assertFalse(StringUtil.isWhitespace("\t  text \b\n\f\r"));
	}
	
	@Test
	public void testGetString() {
		assertEquals("aaaaaaaaaa", StringUtil.getString(10, 'a'));
		assertEquals("\u0001\u0001\u0001", StringUtil.getString(3, '\u0001'));
		assertEquals("", StringUtil.getString(0, '\u0001'));
		assertEquals("", StringUtil.getString(-3, '\u0001'));
	}
	
	@Test
	public void testPadString() {
		assertEquals("012aaaa7890123456789", StringUtil.padString("01234567890123456789", 3, 7, 'a'));
		assertEquals("aaaaaaa7890123456789", StringUtil.padString("01234567890123456789", -3, 7, 'a'));
		assertEquals("012345678901aaaaaaaa", StringUtil.padString("01234567890123456789", 12, 70, 'a'));
		assertEquals("01234567890123456789", StringUtil.padString("01234567890123456789", 50, 70, 'a'));
		assertEquals("01234567890123456789", StringUtil.padString("01234567890123456789", -100, -10, 'a'));
	}
	
}

	