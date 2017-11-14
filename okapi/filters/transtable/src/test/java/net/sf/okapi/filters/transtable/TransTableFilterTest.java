/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

package net.sf.okapi.filters.transtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.resource.ITextUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TransTableFilterTest {
	
	private TransTableFilter filter;
	private String root;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	
	@Before
	public void setUp() {
		filter = new TransTableFilter();
		root = TestUtil.getParentDir(this.getClass(),"/test01.xml.txt");
	}

	@Test
	public void testStartDocument () {
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(root+"test01.xml.txt", null),
			"UTF-8", locEN, locEN));
	}
	
	@Test
	public void testMinimalInput () {
		String snippet = "TransTableV1\ten\tfr\n"
			+ "\"okpCtx:tu=1\"\t\"source\"";	
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertNotNull(tu);
		assertEquals("source", tu.getSource().toString());
		assertEquals("1", tu.getId());
	}
		
	@Test
	public void testMinimalSourceTarget () {
		String snippet = "TransTableV1\ten\tfr\n"
			+ "\"okpCtx:tu=1\"\t\"source\"\t\"target\"";	
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertNotNull(tu);
		assertEquals("source", tu.getSource().toString());
		assertEquals("target", tu.getTarget(locFR).toString());
		assertEquals("1", tu.getId());
	}

	@Test
	public void testQuotesInput () {
		String snippet = "\"TransTableV1\"\t\"en\"\t\"fr\"\n"
			+ "okpCtx:tu=1\tsource";	
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertNotNull(tu);
		assertEquals("source", tu.getSource().toString());
		assertEquals("1", tu.getId());
	}
		
	@Test
	public void testUnSegmented () {
		String snippet = "\"TransTableV1\"\t\"en\"\t\"fr\"\n"
			+ "okpCtx:tu=1:s=0\tsource1\n"	
			+ "okpCtx:tu=2\tsource2";	
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertEquals("source1", tu.getSource().toString());
		assertEquals("1", tu.getId());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
		assertEquals("source2", tu.getSource().toString());
		assertEquals("2", tu.getId());
	}
		
	@Test
	public void testSegmented () {
		String snippet = "\"TransTableV1\"\t\"en\"\t\"fr\"\n"
			+ "okpCtx:tu=1:s=0\tsource1\n"	
			+ "okpCtx:tu=2:s=0\tsrc2-seg0\n"	
			+ "okpCtx:tu=2:s=1\tsrc2-seg1\n"	
			+ "okpCtx:tu=2:s=2\tsrc2-seg2\n"	
			+ "okpCtx:tu=3:s=ZZZ\tsrc3-segZZZ\n"	
			+ "okpCtx:tu=4\tsrc4-seg0";	
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertEquals("source1", tu.getSource().toString());
		assertEquals("1", tu.getId());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
		assertEquals("src2-seg0src2-seg1src2-seg2", tu.getSource().toString());
		assertEquals("2", tu.getId());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 3);
		assertEquals("src3-segZZZ", tu.getSource().toString());
		assertEquals("src3-segZZZ", tu.getSourceSegments().get("ZZZ").getContent().toString());
		assertEquals("3", tu.getId());
	}

	@Test
	public void testSegmentedWithTarget () {
		String snippet = "\"TransTableV1\"\t\"en\"\t\"fr\"\n"
			+ "okpCtx:tu=1:s=0\tsource1\ttarget1\n"	
			+ "okpCtx:tu=2:s=0\tsrc2-seg0\n"
			+ "\n  \n\n"
			+ "okpCtx:tu=2:s=1\tsrc2-seg1\ttrg2-seg1\n"	
			+ "okpCtx:tu=2:s=2\tsrc2-seg2\n"
			+ "okpCtx:tu=3:s=ZZZ\tsrc3-segZZZ\n"	
			+ "okpCtx:tu=4\tsrc4-seg0\ttrg4-seg0\n";	
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertEquals("source1", tu.getSource().toString());
		assertEquals("target1", tu.getTarget(locFR).toString());
		assertEquals("1", tu.getId());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
		assertEquals("src2-seg0src2-seg1src2-seg2", tu.getSource().toString());
		assertEquals("trg2-seg1", tu.getTarget(locFR).toString());
		assertEquals("2", tu.getId());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 3);
		assertEquals("src3-segZZZ", tu.getSource().toString());
		assertEquals("src3-segZZZ", tu.getSourceSegments().get("ZZZ").getContent().toString());
		assertEquals("3", tu.getId());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 4);
		assertEquals("src4-seg0", tu.getSource().toString());
		assertEquals("trg4-seg0", tu.getTarget(locFR).toString());
	}
	
//	@Test
//	public void testDoubleExtraction () {
//		// Read all files in the data directory
//		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
//		list.add(new InputDocument(root+"Test01_srt_en.srt", "okf_regex@SRT.fprm"));
//		RoundTripComparison rtc = new RoundTripComparison();
//		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR));
//	}

	private ArrayList<Event> getEvents(String snippet,
		LocaleId srcLang,
		LocaleId trgLang)
	{
		return FilterTestDriver.getEvents(filter, snippet, srcLang, trgLang);
	}

}

