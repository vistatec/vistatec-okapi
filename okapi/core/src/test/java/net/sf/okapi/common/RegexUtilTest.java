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

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class RegexUtilTest {

	@Test
	public void testReplaceAll() {		
		assertEquals("e{1@^}ddddd{2@^5}+", RegexUtil.replaceAll("e{1,}ddddd{2,5}+", "\\{.*?(,).*?\\}", 1, "@^"));
		assertEquals("\"@eins\"\"", RegexUtil.replaceAll("\"\"\"eins\"\"", "((\\\"\\\")+)[^\\\"]|[^\\\"]((\\\"\\\")+)", 1, "@"));
		assertEquals("\"\"\"eins@", RegexUtil.replaceAll("\"\"\"eins\"\"", "((\\\"\\\")+)[^\\\"]|[^\\\"]((\\\"\\\")+)", 3, "@"));		
	}
	
	@Test
	public void testCountMatches() {
		assertEquals(3, RegexUtil.countMatches("1 text 2 text 1 text 1 text 2", "1"));
		assertEquals(2, RegexUtil.countMatches("1 text 2 text 1 text 1 text 2", "2"));
	}
	
	@Test
	public void testCountQualifiers() {		
		assertEquals(3, RegexUtil.countLeadingQualifiers("\"text, \"text\", text,\"text\"\"\"", "\""));
		assertEquals(4, RegexUtil.countTrailingQualifiers("\"text, \"text\", text,\"text\"\"\"", "\""));
		assertEquals(3, RegexUtil.countLeadingQualifiers("\"\u0432\u0430\u0432\u044b, " +
				"\"\u044b\u0432\u044b\u0432\u0430\u044b\u0432\u0432\u0430\", \u044b\u0432\u0430\u0430\u0430," +
				"\"\u044b\u0444\u044b\u0432\u044b\"\"\"", 
				"\""));
		assertEquals(4, RegexUtil.countTrailingQualifiers("\"\u0432\u0430\u0432\u044b, " +
				"\"\u044b\u0432\u044b\u0432\u0430\u044b\u0432\u0432\u0430\", \u044b\u0432\u0430\u0430\u0430," +
				"\"\u044b\u0444\u044b\u0432\u044b\"\"\"",
				"\""));
	}

	@Test
	public void testGetQuotedAreas() {
		String st =
			"((abc)d(e\\(((f)ghi)\\Qj\\)k\\Elm)nop\\Qqrs\\E";
		   //0123456789 0123456789 012 345 67890123 45678 9
		   //0          1          2           3
		
		List<Range> quotedAreas = RegexUtil.getQuotedAreas(st);
		assertEquals(2, quotedAreas.size());
		assertEquals(21, quotedAreas.get(0).start);
		assertEquals(24, quotedAreas.get(0).end);
		assertEquals(35, quotedAreas.get(1).start);
		assertEquals(37, quotedAreas.get(1).end);
	}
	
	@Test
	public void testGetGroupAtPos() {
		String st =
			   //0122221133 3345544443 333 322 2222111100
				"((abc)d(e\\(((f)ghi)\\Qj\\)k\\Elm)nop)qr";
			   //0123456789 0123456789 012 345 6789012345
			   //0          1          2           3
		assertEquals(0, RegexUtil.getGroupAtPos(st, 0));
		assertEquals(1, RegexUtil.getGroupAtPos(st, 1));
		assertEquals(2, RegexUtil.getGroupAtPos(st, 2));
		assertEquals(2, RegexUtil.getGroupAtPos(st, 3));
		assertEquals(2, RegexUtil.getGroupAtPos(st, 4));
		assertEquals(2, RegexUtil.getGroupAtPos(st, 5));
		assertEquals(1, RegexUtil.getGroupAtPos(st, 6));
		assertEquals(1, RegexUtil.getGroupAtPos(st, 7));
		assertEquals(3, RegexUtil.getGroupAtPos(st, 8));
		assertEquals(3, RegexUtil.getGroupAtPos(st, 9));
		assertEquals(3, RegexUtil.getGroupAtPos(st, 10)); // escaped
		assertEquals(3, RegexUtil.getGroupAtPos(st, 11));
		assertEquals(4, RegexUtil.getGroupAtPos(st, 12));
		assertEquals(5, RegexUtil.getGroupAtPos(st, 13));
		assertEquals(5, RegexUtil.getGroupAtPos(st, 14));
		assertEquals(4, RegexUtil.getGroupAtPos(st, 15));
		assertEquals(4, RegexUtil.getGroupAtPos(st, 16));
		assertEquals(4, RegexUtil.getGroupAtPos(st, 17));
		assertEquals(4, RegexUtil.getGroupAtPos(st, 18));
		assertEquals(3, RegexUtil.getGroupAtPos(st, 19));
		assertEquals(3, RegexUtil.getGroupAtPos(st, 21));
		assertEquals(3, RegexUtil.getGroupAtPos(st, 22));
		assertEquals(3, RegexUtil.getGroupAtPos(st, 23)); // not escaped, quoted area
		assertEquals(2, RegexUtil.getGroupAtPos(st, 24));
		assertEquals(2, RegexUtil.getGroupAtPos(st, 25));
		assertEquals(2, RegexUtil.getGroupAtPos(st, 26));
		assertEquals(2, RegexUtil.getGroupAtPos(st, 27));
		assertEquals(2, RegexUtil.getGroupAtPos(st, 28));
		assertEquals(2, RegexUtil.getGroupAtPos(st, 29));
		assertEquals(1, RegexUtil.getGroupAtPos(st, 30));
		assertEquals(1, RegexUtil.getGroupAtPos(st, 31));
		assertEquals(1, RegexUtil.getGroupAtPos(st, 32));
		assertEquals(1, RegexUtil.getGroupAtPos(st, 33));
		assertEquals(0, RegexUtil.getGroupAtPos(st, 34));
		assertEquals(0, RegexUtil.getGroupAtPos(st, 35));
	}
	
	@Test
	public void testUpdateGroupReferences() {
		String st1 = "abcdefghijk abcdefghijkghab";
		String regex1 = "((ab)cdef(gh)ijk) \\1\\3\\2";
		
		String st2 = "ablmncdefghijk ablmncdefghijkghlmn";		
		String regex2 = "((ab)(lmn)cdef(gh)ijk) \\1\\3\\2";
		String regex3 = "((ab)(lmn)cdef(gh)ijk) \\1\\4\\3";
		
		assertTrue(Pattern.compile(regex1).matcher(st1).matches());
		assertFalse(Pattern.compile(regex2).matcher(st2).matches());
		assertTrue(Pattern.compile(regex3).matcher(st2).matches());
		
		assertEquals(regex3, RegexUtil.updateGroupReferences(regex2, 3));
		assertTrue(Pattern.compile(RegexUtil.updateGroupReferences(regex2, 
				3)).matcher(st2).matches());
	}
}
