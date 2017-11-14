/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.wordcount;

import static org.junit.Assert.assertEquals;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestWordCountStep {

	@Test
	public void testTextUnitCounts() {
		testTextUnitCounts(LocaleId.ENGLISH,
				new String[] { "The number of words in this segment is 9.",
						"The number of words in this second segment is 10.",
						"And the number of words in this third segment is 11." },
				new long[] { 9, 10, 11, 30 });
	}
	
	@Test
	public void testTextUnitCountsCharFactor() {
		testTextUnitCounts(LocaleId.JAPANESE,
				new String[] { "\u65E5\u672C\u8A9E",
						"\u65E5\u672C\u8A9E\u65E5\u672C\u8A9E",
						"\u65E5\u672C\u8A9E\u65E5\u672C\u8A9E\u65E5\u672C\u8A9E" },
				new long[] { 1, 2, 3, 6 });
		testTextUnitCounts(LocaleId.KOREAN,
				new String[] { "\uD55C\uAD6D\uC5B4",
						"\uD55C\uAD6D\uC5B4\uD55C\uAD6D\uC5B4",
						"\uD55C\uAD6D\uC5B4\uD55C\uAD6D\uC5B4\uD55C\uAD6D\uC5B4" },
				new long[] { 1, 2, 3, 5 });
		testTextUnitCounts(LocaleId.CHINA_CHINESE,
				new String[] { "\u4F60\u597D\u5417",
						"\u4F60\u597D\u5417\u4F60\u597D\u5417",
						"\u4F60\u597D\u5417\u4F60\u597D\u5417\u4F60\u597D\u5417" },
				new long[] { 1, 2, 3, 6 });
		testTextUnitCounts(LocaleId.fromString("th"),
				new String[] { "\u0E20\u0E32\u0E29\u0E32\u0E44\u0E17\u0E22",
						"\u0E20\u0E32\u0E29\u0E32\u0E44\u0E17\u0E22" +
						"\u0E20\u0E32\u0E29\u0E32\u0E44\u0E17\u0E22",
						"\u0E20\u0E32\u0E29\u0E32\u0E44\u0E17\u0E22" +
						"\u0E20\u0E32\u0E29\u0E32\u0E44\u0E17\u0E22" +
						"\u0E20\u0E32\u0E29\u0E32\u0E44\u0E17\u0E22" },
				new long[] { 1, 2, 4, 7 });
		// Laotian, Khmer, and Burmese do not have character count factors defined,
		// so word counts cannot be determined (thus they are 0).
		testTextUnitCounts(LocaleId.fromString("lo"),
				new String[] { "\u0E9E\u0EB2\u0EAA\u0EB2\u0EA5\u0EB2\u0EA7",
						"\u0E9E\u0EB2\u0EAA\u0EB2\u0EA5\u0EB2\u0EA7" +
						"\u0E9E\u0EB2\u0EAA\u0EB2\u0EA5\u0EB2\u0EA7",
						"\u0E9E\u0EB2\u0EAA\u0EB2\u0EA5\u0EB2\u0EA7" +
						"\u0E9E\u0EB2\u0EAA\u0EB2\u0EA5\u0EB2\u0EA7" +
						"\u0E9E\u0EB2\u0EAA\u0EB2\u0EA5\u0EB2\u0EA7" },
				new long[] { 0, 0, 0, 0 });
		testTextUnitCounts(LocaleId.fromString("km"),
				new String[] { "\u1797\u17B6\u179F\u17B6\u1781\u17D2\u1798\u17C2\u179A",
						"\u1797\u17B6\u179F\u17B6\u1781\u17D2\u1798\u17C2\u179A" +
						"\u1797\u17B6\u179F\u17B6\u1781\u17D2\u1798\u17C2\u179A",
						"\u1797\u17B6\u179F\u17B6\u1781\u17D2\u1798\u17C2\u179A" +
						"\u1797\u17B6\u179F\u17B6\u1781\u17D2\u1798\u17C2\u179A" +
						"\u1797\u17B6\u179F\u17B6\u1781\u17D2\u1798\u17C2\u179A" },
				new long[] { 0, 0, 0, 0 });
		testTextUnitCounts(LocaleId.fromString("my"),
				new String[] { "\u1019\u103C\u1014\u103A\u1019\u102C\u1018\u102C\u101E\u102C",
						"\u1019\u103C\u1014\u103A\u1019\u102C\u1018\u102C\u101E\u102C" +
						"\u1019\u103C\u1014\u103A\u1019\u102C\u1018\u102C\u101E\u102C",
						"\u1019\u103C\u1014\u103A\u1019\u102C\u1018\u102C\u101E\u102C" +
						"\u1019\u103C\u1014\u103A\u1019\u102C\u1018\u102C\u101E\u102C" +
						"\u1019\u103C\u1014\u103A\u1019\u102C\u1018\u102C\u101E\u102C" },
				new long[] { 0, 0, 0, 0 });
	}
	
	private void testTextUnitCounts(LocaleId locale, String[] frags, long[] counts) {
		ITextUnit tu = new TextUnit("tu");
		TextContainer tc = tu.getSource();
		ISegments segments = tc.getSegments();
		segments.append(new TextFragment(frags[0]));
		segments.append(new TextFragment(frags[1]));
		segments.append(new TextFragment(frags[2]));
		
		WordCountStep step = new WordCountStep();
		StartDocument sd = new StartDocument("sd");
		sd.setLocale(locale);
		step.handleEvent(new Event(EventType.START_DOCUMENT, sd));
		step.handleEvent(new Event(EventType.TEXT_UNIT, tu));
		
		assertEquals(counts[0], WordCounter.getCount(tu, 0));
		assertEquals(counts[1], WordCounter.getCount(tu, 1));
		assertEquals(counts[2], WordCounter.getCount(tu, 2));
		assertEquals(counts[3], WordCounter.getCount(tu));
	}
}
