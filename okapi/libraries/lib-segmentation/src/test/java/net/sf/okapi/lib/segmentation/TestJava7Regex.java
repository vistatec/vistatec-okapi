/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package net.sf.okapi.lib.segmentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.TreeSet;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextContainer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestJava7Regex {

	private SRXSegmenter segmenter;
	private SRXDocument doc;
	private ArrayList<Rule> rules;
	
	@Before
	public void startUp() {
		doc = new SRXDocument();
		segmenter = new SRXSegmenter(); 
		rules = new ArrayList<Rule>();
		
		assertTrue(segmenter.useJavaRegex()); // Default
	}

	@Test
	public void testSet() {
		TreeSet<Character> set = new TreeSet<Character>();
		set.add('w');
		set.add('a');
		set.add('x');
		set.add('t');
		set.add('s');		
		
		char[] chars = new char[set.size()];
		int i = 0;
		for (Character ch : set) {
			chars[i++] = ch;
		}
		assertEquals("astwx", String.valueOf(chars));
	}

	@Test
	public void testMetachars() {		
		// http://userguide.icu-project.org/strings/regexp
		// http://docs.oracle.com/javase/tutorial/essential/regex/index.html

		//* \a
		testBreak("before\u0007After", "\\a", "A", "before\u0007", "After");
		testBreak("before\u0007After", "[\\a ]", "A", "before\u0007", "After");
		testBreak("before_After", "[\\a_]", "A", "before_", "After");
		testBreak("before After", "[\\a ]", "A", "before ", "After");

		//* \A
		testBreak("After", "\\AA", "f", "A", "fter");
		testBreak("before\nAfter", "\\Abef", "ore", "bef", "ore\nAfter");

		//* \b
		testBreak("before After", "\\b", "A", "before ", "After");
		testBreak("before After", "\\b", " ", "before", " After");
		testBreak("before\rAfter", "\\b", "\r", "before", "\rAfter");

		// ICU regex specs don't require \b in sets
		testBreak("before After", "\\b", "A", "before ", "After");
		testBreak("before After", "\\b", " ", "before", " After");
		testBreak("before After", "\\b|\\r", "A", "before ", "After");
		testBreak("before\rAfter", "\\b|\\r", "A", "before\r", "After");

		//* \B
		testBreak("before afAter", "\\B", "A", "before af", "Ater");
		testBreak("before after", "\\B", "o", "bef", "ore after");
		testBreak("befo\rreAfter", "\\B", "A", "befo\rre", "After");
		
		// ICU regex specs don't require \B in sets
		testBreak("before afAter", "\\B", "A", "before af", "Ater");
		testBreak("before after", "\\B", "o", "bef", "ore after");
		testBreak("before after", "\\B|\\r", "o", "bef", "ore after");
		testBreak("beforeAfter", "\\Be", "A", "before", "After");
		testBreak("before\rAfter", "\\B|\\r", "A", "before\r", "After");

		//* \cX
		testBreak("before\u0001After", "\\cA", "A", "before\u0001", "After");
		testBreak("before\u0002After", "\\cB", "A", "before\u0002", "After");
		testBreak("before\u0003After", "\\cC", "A", "before\u0003", "After");
		testBreak("before\u0004After", "\\cD", "A", "before\u0004", "After");
		testBreak("before\u0005After", "\\cE", "A", "before\u0005", "After");
		testBreak("before\u0006After", "\\cF", "A", "before\u0006", "After");
		testBreak("before\u0007After", "\\cG", "A", "before\u0007", "After");
		testBreak("before\u0008After", "\\cH", "A", "before\u0008", "After");
		testBreak("before\u0009After", "\\cI", "A", "before\u0009", "After");
		testBreak("before\nAfter", "\\cJ", "A", "before\n", "After");
		testBreak("before\u000BAfter", "\\cK", "A", "before\u000B", "After");
		testBreak("before\u000CAfter", "\\cL", "A", "before\u000C", "After");
		testBreak("before\rAfter", "\\cM", "A", "before\r", "After");
		testBreak("before\u000EAfter", "\\cN", "A", "before\u000E", "After");
		testBreak("before\u000FAfter", "\\cO", "A", "before\u000F", "After");
		testBreak("before\u0010After", "\\cP", "A", "before\u0010", "After");
		testBreak("before\u0011After", "\\cQ", "A", "before\u0011", "After");
		testBreak("before\u0012After", "\\cR", "A", "before\u0012", "After");
		testBreak("before\u0013After", "\\cS", "A", "before\u0013", "After");
		testBreak("before\u0014After", "\\cT", "A", "before\u0014", "After");
		testBreak("before\u0015After", "\\cU", "A", "before\u0015", "After");
		testBreak("before\u0016After", "\\cV", "A", "before\u0016", "After");
		testBreak("before\u0017After", "\\cW", "A", "before\u0017", "After");
		testBreak("before\u0018After", "\\cX", "A", "before\u0018", "After");
		testBreak("before\u0019After", "\\cY", "A", "before\u0019", "After");
		testBreak("before\u001AAfter", "\\cZ", "A", "before\u001A", "After");		
		testBreak("before\u001AAfter", "[\\cZ\\cA]", "A", "before\u001A", "After");
		testBreak("before\u0001After", "[\\cZ\\cA]", "A", "before\u0001", "After");
		
		//* \d
		testBreak("before001After", "\\d", "A", "before001", "After");
		testNoBreak("beforeAfter", "\\d", "A", "before001", "After");
		testNoBreak("beforeAfter", "[\\d]", "A", "before001", "After");
		testBreak("before\u0969After", "\\d", "A", "before\u0969", "After");
		testBreak("before001After", "[\\d]", "A", "before001", "After");
		testBreak("before001After", "[\\d\\r]", "A", "before001", "After");
		testNoBreak("beforeAfter", "[\\d\\r]", "A", "before001", "After");
		testBreak("before\u0969After", "[\\d\\r]", "A", "before\u0969", "After");
		testBreak("before\rAfter", "[\\d\\r]", "A", "before\r", "After");
		
		//* \D
		testBreak("beforeAfter", "\\D", "A", "before", "After");
		testNoBreak("before\u0969After", "\\D", "A", "before", "After");
		testBreak("beforeAfter", "[\\D]", "A", "before", "After");
		testNoBreak("before\u0969After", "[\\D]", "A", "before", "After");
		testBreak("beforeAfter", "[\\D\\r]", "A", "before", "After");
		testNoBreak("before\u0969After", "[\\D\\r]", "A", "before", "After");
		testBreak("before\rAfter", "[\\D\\r]", "A", "before\r", "After");
		
		//* \e
		testBreak("before\u001BAfter", "\\e", "A", "before\u001B", "After");
		testBreak("before\u001BAfter", "[\\e\\r]", "A", "before\u001B", "After");
		
		//* \E
		testBreak("be{4}[foreAfter", "be\\Q{4}[\\Efore", "A", "be{4}[fore", "After");
		testBreak("be{4}[foreAfter", "be[\\Q{[\\E]4\\}\\[fore", "A", "be{4}[fore", "After");
		testBreak("be[4}[foreAfter", "be[\\Q{[\\E]4\\}\\[fore", "A", "be[4}[fore", "After");
		
		//* \f
		testBreak("before\u000CAfter", "\\f", "A", "before\u000C", "After");
		testBreak("before\u000CAfter", "[\\f\\r]", "A", "before\u000C", "After");
		
		//* \G These should be working in Java 7, but they fail
//		testBreak("before before After", "\\Gbefore ", "A", "before before ", "After");
//		testBreak("beforebeforeAfterAfterAfter", "before", "\\GAfter", "beforebefore", "AfterAfterAfter");
//		testBreak("before before After after", "\\G[a-z ]", "A\\G[a-z ]", "before before ", "After after");
//		testBreak("before before After", "\\G[a-z ]", "A", "before before ", "After");
		
		//* \n
		testBreak("before\nAfter", "\\n", "A", "before\n", "After");
		testBreak("before\nAfter", "[\\n\\r]", "A", "before\n", "After");
		testBreak("before\rAfter", "[\\n\\r]", "A", "before\r", "After");
		testBreak("before\nAfter", "\n", "A", "before\n", "After");
		testBreak("before\nAfter", "[\n\r]", "A", "before\n", "After");
		testBreak("before\rAfter", "[\n\r]", "A", "before\r", "After");

		//* \N{UNICODE CHARACTER NAME} *Not supported in Java 7*
//		testBreak("before:After", "\\N{COLON}", "A", "before:", "After");
//		testBreak("before:After", "re\\N{COLON}", "A", "before:", "After");
//		testBreak("before\u00B8After", "\\N{CEDILLA}", "A", "before\u00B8", "After");
//		testBreak("before\u00B8After", "[\\N{COLON}\\N{CEDILLA}]", "A", "before\u00B8", "After");
//		testBreak("before0After", "\\N{DIGIT ZERO}", "A", "before0", "After");
//		testBreak("before\u00E6After", "[\\N{LATIN SMALL LETTER AE}]", "A", "before\u00E6", "After");
		
		//* \p{UNICODE PROPERTY NAME}
		testBreak("beforeиAfter", "\\p{IsCyrillic}", "A", "beforeи", "After");
		testBreak("beforeъиAfter", "\\p{IsCyrillic}", "A", "beforeъи", "After");
		testBreak("beforeъиAfter", "ъ\\p{IsCyrillic}", "A", "beforeъи", "After");
		testBreak("abcцыпаAfter", "abcцы\\p{IsCyrillic}{2}", "A", "abcцыпа", "After");
		testBreak("beforeиAfter", "[\\p{IsCyrillic}]", "A", "beforeи", "After");
		testBreak("beforeиAfter", "[\\p{IsCyrillic}\r]", "A", "beforeи", "After");
		testBreak("before\rAfter", "[\\p{IsCyrillic}\r]", "A", "before\r", "After");
		
		//* \P{UNICODE PROPERTY NAME}
		testBreak("beforeиbAfter", "\\P{IsCyrillic}", "A", "beforeиb", "After");
		testBreak("abcцыdaAfter", "abcцы\\P{IsCyrillic}{2}", "A", "abcцыda", "After");
		testBreak("beforeиbAfter", "[\\P{IsCyrillic}]", "A", "beforeиb", "After");
		testBreak("beforeиbAfter", "[\\P{IsCyrillic}\r]", "A", "beforeиb", "After");
		testBreak("before\rAfter", "[\\P{IsCyrillic}\r]", "A", "before\r", "After");
		
		//* \Q
		testBreak("be{4}[foreAfter", "be\\Q{4}[\\Efore", "A", "be{4}[fore", "After");
		testBreak("be{4}[foreAfter", "be[\\Q{[\\E]4\\}\\[fore", "A", "be{4}[fore", "After");
		testBreak("be[4}[foreAfter", "be[\\Q{[\\E]4\\}\\[fore", "A", "be[4}[fore", "After");
		
		//* \r
		testBreak("before\rAfter", "\\r", "A", "before\r", "After");
		testBreak("before\rAfter", "[\\n\\r]", "A", "before\r", "After");
		testBreak("before\nAfter", "[\\n\\r]", "A", "before\n", "After");
		testBreak("before\rAfter", "\r", "A", "before\r", "After");
		testBreak("before\rAfter", "[\n\r]", "A", "before\r", "After");
		testBreak("before\nAfter", "[\n\r]", "A", "before\n", "After");
		
		//* \s
		testBreak("before After", "\\s", "A", "before ", "After");
		testBreak("before\u3000After", "\\s", "A", "before\u3000", "After");
		testBreak("before\u2009After", "\\s", "A", "before\u2009", "After");
		testBreak("before\tAfter", "\\s", "A", "before\t", "After");
		testBreak("before After", "[\\s\\t]", "A", "before ", "After");
		testBreak("before\tAfter", "[\\s\\t]", "A", "before\t", "After");		
		
		//* \S
		testBreak("   beforeAfter", "\\S", "A", "   before", "After");
		testBreak("   beforeAfter", "[\\S]", "A", "   before", "After");
		testBreak("   beforeAfter", "[\\S\\t]", "A", "   before", "After");
		
		//* \t
		testBreak("before\tAfter", "\\t", "A", "before\t", "After");
		testBreak("before\tAfter", "[\\t\\r]", "A", "before\t", "After");
		testBreak("before\tAfter", "e\\tA", "f", "before\tA", "fter");
		
		//* \u0000
		testBreak("before\u1234After", "\\u1234", "A", "before\u1234", "After");
		
//		//* \Uhhhhhhhh *Not supported in Java 7*
//		testBreak("before" + buildString(0x000000AF) + "After", 
//				"\\U000000AF", "A", "before" + buildString(0x000000AF), "After");
//		testBreak("before" + buildString(0x000000AF) + "After", 
//				"\\U000000af", "A", "before" + buildString(0x000000AF), "After");
//		testBreak("before" + buildString(0x0000FFFF) + "After", 
//				"\\U0000FFFF", "A", "before" + buildString(0x0000FFFF), "After");
//		testBreak("before" + buildString(0x00010000) + "After", 
//				"\\U00010000", "A", "before" + buildString(0x00010000), "After");
//		testBreak("before" + buildString(0x0010AAAF) + "After", 
//				"\\U0010AAAF", "A", "before" + buildString(0x0010AAAF), "After");
//		testBreak("before" + buildString(0x0010AAAF) + "After", 
//				"[\\U0010AAAF\\U00010000]", "A", "before" + buildString(0x0010AAAF), "After");
//		testBreak("before" + buildString(0x00010000) + "After", 
//				"[\\U0010AAAF\\U00010000]", "A", "before" + buildString(0x00010000), "After");
//		testBreak("before" + buildString(0x00010AA0) + "After", 
//				"[\\U0010AAAF\\U00010000-\\U00010AA1]", "A", "before" + buildString(0x00010AA0), "After");
		
		//* \w
		testBreak("beforeAfter", "\\w", "A", "before", "After");
		testBreak("beforeAfter", "[\\w\\r]", "A", "before", "After");
		testBreak("before\rAfter", "[\\w\\r]", "A", "before\r", "After");
		
		//* \W
		testBreak("before After", "\\W", "A", "before ", "After");
		testBreak("before After", "[\\W\\r]", "A", "before ", "After");
		testBreak("before\rAfter", "[\\W\\r]", "A", "before\r", "After");

		//* \x{hhhh}
		testBreak("before\u00AFAfter", "\\x{00AF}", "A", "before\u00AF", "After");
		testBreak("before\u00AFAfter", "\\x{00af}", "A", "before\u00AF", "After");
		testBreak("before\u00AFAfter", "\\x{af}", "A", "before\u00AF", "After");
		testBreak("before\u00AFAfter", "[\\x{00AF}\\x{00AA}]", "A", "before\u00AF", "After");
		testBreak("before\u00AAAfter", "[\\x{00AF}\\x{00AA}]", "A", "before\u00AA", "After");
		testBreak("before" + buildString(0x00FFFF) + "After", 
				"\\x{00FFFF}", "A", "before" + buildString(0x00FFFF), "After");
		testBreak("before" + buildString(0x10FFFF) + "After", 
				"[\\x{10FFFF}\\x{10A000}]", "A", "before" + buildString(0x10FFFF), "After");
		testBreak("before" + buildString(0x10A000) + "After", 
				"[\\x{10FFFF}\\x{10A000}-\\x{10AA00}]", "A", "before" + buildString(0x10A000), "After");

		//* \xhh
		testBreak("before\u00AFAfter", "\\xAF", "A", "before\u00AF", "After");
		testBreak("before\u00AFAfter", "[\\xAF\\xAA]", "A", "before\u00AF", "After");
		testBreak("before\u00AAAfter", "[\\xAF\\xAA]", "A", "before\u00AA", "After");
		
		//* \X = For legacy grmapheme cluster (?>\PM\pM*) or (?>\PM*\pM*) or 
		// For extended grapheme cluster:
		// "(?:(?:\\u000D\\u000A)|(?:[\\u0E40\\u0E41\\u0E42\\u0E43\\u0E44\\u0EC0\\u0EC1\\u0EC2\\u0EC3\\u0EC4\\uAAB5\\uAAB6\\uAAB9\\uAABB\\uAABC]*(?:[\\u1100-\\u115F\\uA960-\\uA97C]+|([\\u1100-\\u115F\\uA960-\\uA97C]*((?:[[\\u1160-\\u11A2\\uD7B0-\\uD7C6][\\uAC00\\uAC1C\\uAC38]][\\u1160-\\u11A2\\uD7B0-\\uD7C6]*|[\\uAC01\\uAC02\\uAC03\\uAC04])[\\u11A8-\\u11F9\\uD7CB-\\uD7FB]*))|[\\u11A8-\\u11F9\\uD7CB-\\uD7FB]+|[^[\\p{Zl}\\p{Zp}\\p{Cc}\\p{Cf}&&[^\\u000D\\u000A\\u200C\\u200D]]\\u000D\\u000A])[[\\p{Mn}\\p{Me}\\u200C\\u200D\\u0488\\u0489\\u20DD\\u20DE\\u20DF\\u20E0\\u20E2\\u20E3\\u20E4\\uA670\\uA671\\uA672\\uFF9E\\uFF9F][\\p{Mc}\\u0E30\\u0E32\\u0E33\\u0E45\\u0EB0\\u0EB2\\u0EB3]]*)|(?s:.))"
		testBreak("beforeg̈After", "(?:(?:\\u000D\\u000A)|(?:[\\u0E40\\u0E41\\u0E42\\u0E43\\u0E44\\u0EC0\\u0EC1\\u0EC2\\u0EC3\\u0EC4\\uAAB5\\uAAB6\\uAAB9\\uAABB\\uAABC]*(?:[\\u1100-\\u115F\\uA960-\\uA97C]+|([\\u1100-\\u115F\\uA960-\\uA97C]*((?:[[\\u1160-\\u11A2\\uD7B0-\\uD7C6][\\uAC00\\uAC1C\\uAC38]][\\u1160-\\u11A2\\uD7B0-\\uD7C6]*|[\\uAC01\\uAC02\\uAC03\\uAC04])[\\u11A8-\\u11F9\\uD7CB-\\uD7FB]*))|[\\u11A8-\\u11F9\\uD7CB-\\uD7FB]+|[^[\\p{Zl}\\p{Zp}\\p{Cc}\\p{Cf}&&[^\\u000D\\u000A\\u200C\\u200D]]\\u000D\\u000A])[[\\p{Mn}\\p{Me}\\u200C\\u200D\\u0488\\u0489\\u20DD\\u20DE\\u20DF\\u20E0\\u20E2\\u20E3\\u20E4\\uA670\\uA671\\uA672\\uFF9E\\uFF9F][\\p{Mc}\\u0E30\\u0E32\\u0E33\\u0E45\\u0EB0\\u0EB2\\u0EB3]]*)|(?s:.))", "A", "beforeg̈", "After");
		testBreak("beforeÁAfter", "(?>\\PM*\\pM*)", "A", "beforeÁ", "After");
		testBreak("beforeÃAfter", "(?>\\PM*\\pM*)", "A", "beforeÃ", "After");
		testBreak("beforeก็After", "(?>\\PM\\pM*)", "A", "beforeก็", "After");
		testBreak("before\u0F3FAfter", "\\u0F3F", "A", "before\u0F3F", "After");
		testBreak("before\u1100\u1161\u11A8After", "(?>\\PM\\pM*)", "A", "before\u1100\u1161\u11A8", "After");
		testBreak("before\u0BA8\u0BBFAfter", "(?>\\PM\\pM*)", "A", "before\u0BA8\u0BBF", "After");
		testBreak("before\u0937\u093FAfter", "(?>\\PM\\pM*)", "A", "before\u0937\u093F", "After");
		testBreak("before\u093FAfter", "(?>\\PM\\pM*)", "A", "before\u093F", "After");
		testBreak("before\u0915\u094D\u0937\u093FAfter", "(?:(?:\\u000D\\u000A)|(?:[\\u0E40\\u0E41\\u0E42\\u0E43\\u0E44\\u0EC0\\u0EC1\\u0EC2\\u0EC3\\u0EC4\\uAAB5\\uAAB6\\uAAB9\\uAABB\\uAABC]*(?:[\\u1100-\\u115F\\uA960-\\uA97C]+|([\\u1100-\\u115F\\uA960-\\uA97C]*((?:[[\\u1160-\\u11A2\\uD7B0-\\uD7C6][\\uAC00\\uAC1C\\uAC38]][\\u1160-\\u11A2\\uD7B0-\\uD7C6]*|[\\uAC01\\uAC02\\uAC03\\uAC04])[\\u11A8-\\u11F9\\uD7CB-\\uD7FB]*))|[\\u11A8-\\u11F9\\uD7CB-\\uD7FB]+|[^[\\p{Zl}\\p{Zp}\\p{Cc}\\p{Cf}&&[^\\u000D\\u000A\\u200C\\u200D]]\\u000D\\u000A])[[\\p{Mn}\\p{Me}\\u200C\\u200D\\u0488\\u0489\\u20DD\\u20DE\\u20DF\\u20E0\\u20E2\\u20E3\\u20E4\\uA670\\uA671\\uA672\\uFF9E\\uFF9F][\\p{Mc}\\u0E30\\u0E32\\u0E33\\u0E45\\u0EB0\\u0EB2\\u0EB3]]*)|(?s:.))", "A", "before\u0915\u094D\u0937\u093F", "After");		
		
		//* \Z
		testBreak("terbefore\nAfter", "Af", "ter\\Z", "terbefore\nAf", "ter");
		testBreak("terbefore\nAfter\n", "Af", "ter\\Z", "terbefore\nAf", "ter\n");
		
		//* \z
		testBreak("terbefore\nAfter", "Af", "ter\\z", "terbefore\nAf", "ter");
		testNoBreak("terbefore\nAfter\n", "Af", "ter\\z", "terbefore\nAf", "ter\n");
		
		//* \n
		testBreak("before beforeAfter", "(before) \\2", "A", "before before", "After");
		testBreak("before before After", "(before) \\2", " A", "before before", " After");
		testBreak("before before After", "(before) \\2 ", "A", "before before ", "After");
		testBreak("before beforeก็After", "(before) \\2(?>\\PM\\pM*)?", "A", "before beforeก็", "After");
		testBreak("before before ก็After", "(before) \\2 (?>\\PM\\pM*)?", "A", "before before ก็", "After");
		testBreak("before before After", "(before) \\2 (?>\\PM\\pM*)?", "A", "before before ", "After");
		testBreak("before beforeAfter", "(before) \\2(?>\\PM\\pM*)?", "A", "before before", "After");
		
		//* \0ooo		
		testBreak("before\nAfter", "\\012", "A", "before\n", "After");
		testBreak("before\u000BAfter", "\\013", "A", "before\u000B", "After");
		testBreak("before\rAfter", "e", "\\015", "before", "\rAfter");
		testBreak("before\u0018After", "\\030", "A", "before\u0018", "After");
		testBreak("before\u001AAfter", "\\032", "A", "before\u001A", "After");
		testBreak("before\u002AAfter", "\\052", "A", "before\u002A", "After");
		testBreak("before\u003FAfter", "\\077", "A", "before\u003F", "After");
		testBreak("before\u0040After", "\\0100", "A", "before\u0040", "After");
		testBreak("before\u00AAAfter", "\\0252", "A", "before\u00AA", "After");
		testBreak("beforePAfter", "e", "\\0120", "before", "PAfter");
		testBreak("before\u00FFAfter", "e", "\\0377", "before", "\u00FFAfter");
		testBreak("before\u00FFAfter", "e", "[\\0377]", "before", "\u00FFAfter");
		testBreak("before\u00FFAfter", "e", "[\\0377\\077]", "before", "\u00FFAfter");
		testBreak("before\u00FFAfter", "e", "[\\0377\\077]", "before", "\u00FFAfter");
		testNoBreak("before\u003FAfter", "e", "\\0477", "before", "\u003FAfter"); // Octal cannot exceed 0377 (0xFF)
		
		//* \[pattern]
		testBreak("beфываfore\nAfter", "[a-z\\p{IsCyrillic}]+", "ore", "beфываf", "ore\nAfter");
		
		//* \.
		testBreak("before\nAfter", ".", "ore", "bef", "ore\nAfter");
		
		//* \^
		testBreak("before\nAfter", "^bef", "ore", "bef", "ore\nAfter");
		
		//* \$
		testBreak("terbefore\nAfter", "Af", "ter$", "terbefore\nAf", "ter");
		
		// \
		testBreak("be{4}[foreAfter", "be\\{4\\}\\[fore", "A", "be{4}[fore", "After");
		
		// \ in sets
		testBreak("be{4}[foreAfter", "be[\\{\\[]4\\}\\[fore", "A", "be{4}[fore", "After");
		testBreak("be[4}[foreAfter", "be[\\{\\[]4\\}\\[fore", "A", "be[4}[fore", "After");
		
		//* Space
		testBreak("before After", " ", "A", "before ", "After");
		
		//* Backspace (Java Char \b, not ICU regex \b)
		testBreak("before\bAfter", "\u0008", "A", "before\b", "After");
		testBreak("before\u0008After", "\b", "A", "before\u0008", "After");
		testBreak("before\bAfter", "\b", "A", "before\b", "After");
		testBreak("before\bAfter", "\b", "A", "before\b", "After");
		testBreak("before\bAfter", "\b", "A", "before\b", "After");
		testBreak("before\bAfter", "e\bA", "f", "before\bA", "fter");
		
		// TODO Test combined rules (several meta-characters in the rule)
		
		testBreak("Sentence 1. Sentence 2.", "\\.", "\\s|<br/?>", "Sentence 1.", " Sentence 2.");
		testBreak("Sentence 1.<br>Sentence 2.", "\\.", "\\s|<br/?>", "Sentence 1.", "<br>Sentence 2.");
		testBreak("Sentence 1.<br/>Sentence 2.", "\\.", "\\s|<br/?>", "Sentence 1.", "<br/>Sentence 2.");
		
		// TODO Test back refs with word boundaries used
	}
	
	@Test
	public void testMetachars2() {
		testBreak("Mr. Holmes is from the U.K. not the U.S. Is Dr. Watson from there too? Yes: both are.", 
				"\\b(St|Gen|Hon|Dr|Mr|Ms|Mrs|Col|Maj|Brig|Sgt|Capt|Cmnd|Sen|Rev|Rep|Revd)\\.", 
				"\\s+\\p{Lu}", 
				//"\\s+",
				"Mr.", " Holmes is from the U.K. not the U.S. Is Dr.", 3);
	}

	// http://java.sun.com/developer/technicalArticles/Intl/Supplementary/
	private String buildString(int codePoint) {
	    if (Character.charCount(codePoint) == 1) {
	        return String.valueOf((char) codePoint);
	    } else {
	        return new String(Character.toChars(codePoint));
	    }
	}
	
	private void testBreak(String text, String bbr, String abr, String beforeBreak,
			String afterBreak) {		
		rules.clear();
		rules.add(new Rule(bbr, abr, true));
		doc.addLanguageRule("default", rules);
		doc.addLanguageMap(new LanguageMap(".*", "default"));
		segmenter.setLanguage(null); // Force rules recompile 
		doc.compileLanguageRules(LocaleId.ENGLISH, segmenter);
		assertEquals(2, segmenter.computeSegments(text));
		TextContainer tc = new TextContainer(text);
		tc.getSegments().create(segmenter.getRanges());
		assertEquals(beforeBreak, tc.getSegments().get(0).toString());
		assertEquals(afterBreak, tc.getSegments().get(1).toString());
	}
	
	private void testBreak(String text, String bbr, String abr, String beforeBreak,
			String afterBreak, int numSeg) {		
		rules.clear();
		rules.add(new Rule(bbr, abr, true));
		doc.addLanguageRule("default", rules);
		doc.addLanguageMap(new LanguageMap(".*", "default"));
		segmenter.setLanguage(null); // Force rules recompile 
		doc.compileLanguageRules(LocaleId.ENGLISH, segmenter);
		assertEquals(numSeg, segmenter.computeSegments(text));
		TextContainer tc = new TextContainer(text);
		tc.getSegments().create(segmenter.getRanges());
		assertEquals(beforeBreak, tc.getSegments().get(0).toString());
		assertEquals(afterBreak, tc.getSegments().get(1).toString());
	}
	
	private void testNoBreak(String text, String bbr, String abr, String beforeBreak,
			String afterBreak) {		
		rules.clear();
		rules.add(new Rule(bbr, abr, true));
		doc.addLanguageRule("default", rules);
		doc.addLanguageMap(new LanguageMap(".*", "default"));
		segmenter.setLanguage(null); // Force rules recompile
		doc.compileLanguageRules(LocaleId.ENGLISH, segmenter);
		assertEquals(1, segmenter.computeSegments(text));
		TextContainer tc = new TextContainer(text);
		tc.getSegments().create(segmenter.getRanges());
		assertEquals(text, tc.getSegments().get(0).toString());
	}
}
