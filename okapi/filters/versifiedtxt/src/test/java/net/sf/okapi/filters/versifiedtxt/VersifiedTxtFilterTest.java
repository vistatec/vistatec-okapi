/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.versifiedtxt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.resource.AlignmentStatus;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VersifiedTxtFilterTest {
	private VersifiedTextFilter filter;
	private String root;
		
	@Before
	public void setUp() {
		filter = new VersifiedTextFilter();
		filter.setOptions(LocaleId.ENGLISH, LocaleId.SPANISH, "UTF-8", true);
		root = TestUtil.getParentDir(this.getClass(), "/part1.txt");
	}
	
	@Test
	public void testDefaultInfo () {		
		assertNotNull(filter.getName());
		assertNotNull(filter.getDisplayName());
		List<FilterConfiguration> list = filter.getConfigurations();
		assertNotNull(list);
		assertTrue(list.size()>0);
	}
	
	@Test
	public void testStartDocument () {
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(root + "part1.txt", null),
			"UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH));
	}

	@Test
	public void testSimpleVerse() {
		String snippet = "|btest\n|v1\nThis is a test.";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("This is a test.", tu.getSource().toString());
		assertEquals("test::1", tu.getName());
	}
	
	@Test
	public void testSimpleBookChapterVerse() {
		String snippet = "|bbook\n|cchapter\n|v1\nThis is a test.";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("This is a test.", tu.getSource().toString());
		assertEquals("book:chapter:1", tu.getName());
	}
	
	@Test
	public void testOutputSimpleBookChapterVerseWithMacLB () {
		String snippet = "|bbook\r|cchapter\r|v0\rTest\r\r|v1\rThis is a test.";
		String expected = snippet;
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), LocaleId.FRENCH);
		assertEquals(expected, result);
	}

	@Test
	public void testOutputSimpleBookChapterVerse() {
		String snippet = "|bbook\n|cchapter\n|v1\nThis is a test.";
		String expected = "|bbook\n|cchapter\n|v1\nThis is a test."; 
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), LocaleId.ENGLISH);
		assertEquals(expected, result);
	}

	@Test
	public void testOutputSimpleBookChapterVerseMultilingual () {
		String snippet = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\n\n\n|v2\nsource2\n<TARGET>\ntarget2";
		String expected = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\n\n\n|v2\nsource2\n<TARGET>\ntarget2";
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), LocaleId.FRENCH);
		assertEquals(expected, result);
	}

	@Test
	public void testOutputSimpleBookChapterVerseMultilingualFillTarget () {
		String snippet = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\n\n\n|v2\nsource2\n<TARGET>\ntarget2";
		String expected = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\nsource\n\n|v2\nsource2\n<TARGET>\ntarget2";
		IParameters p = filter.getParameters();
		p.setBoolean(GenericSkeletonWriter.ALLOWEMPTYOUTPUTTARGET, false);
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), LocaleId.FRENCH);
		assertEquals(expected, result);
	}
	
	@Test
	public void testSimplePlaceholders() {
		String snippet = "|bbook\n|cchapter\n|v1\n{1}This is {2}a test{3}";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("{1}This is {2}a test{3}", tu.getSource().toString());
		assertEquals("book:chapter:1", tu.getName());
	}
	
	@Test
	public void testEmptyVerses() {
		String snippet = "|bbook\n|cchapter\n|v1\n|v2\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("", tu.getSource().toString());
		
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertNotNull(tu);
		assertEquals("", tu.getSource().toString());
	}

	@Test
	public void testBilingual() {		
		String snippet = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\ntarget\n\n|v2\nsource2\n<TARGET>\ntarget2\n\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("source", tu.getSource().toString());		
		assertEquals("target", tu.getTarget(filter.getTrgLoc()).toString());
		
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertNotNull(tu);
		assertEquals("source2", tu.getSource().toString());
		// filter will remove all training newlines on last entry
		assertEquals("target2", tu.getTarget(filter.getTrgLoc()).toString());
	}
	
	@Test
	public void testBilingualWithGenericWriter() {
		String snippet = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\ntarget\n\n|v2\nsource2\n<TARGET>\ntarget2";
		String expected = snippet;
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), LocaleId.SPANISH);
		assertEquals(expected, result);
	}
	
	@Test
	public void testBilingualWithInternalNewlinesWithGenericWriter() {
		String snippet = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\ntarget1\n\ntarget2\n\n|v2\nsource2\n<TARGET>\ntarget2";
		String expected = snippet;
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), LocaleId.SPANISH);
		assertEquals(expected, result);
	}
	
	@Test
	public void testBilingualWithGenericWriterWithMissingNewlines() {
		String snippet = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\ntarget\n|v2\nsource2\n<TARGET>\ntarget2";
		String expected = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\ntarget\n\n|v2\nsource2\n<TARGET>\ntarget2";
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), LocaleId.SPANISH);
		assertEquals(expected, result);
	}
	
	@Test
	public void testBilingualWithEmptyVerses() {
		String snippet = "|bbook\n|cchapter\n|v1\n<TARGET>\n|v2\n<TARGET>\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("", tu.getSource().toString());
		assertEquals("", tu.getTarget(filter.getTrgLoc()).toString());
		
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertNotNull(tu);
		assertEquals("", tu.getSource().toString());
		assertEquals("", tu.getTarget(filter.getTrgLoc()).toString());
	}
	
	@Test
	public void testDoubleExtraction() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root+"part1.txt", null));		
		RoundTripComparison rtc = new RoundTripComparison(false);
		assertTrue(rtc.executeCompare(filter, list, "windows-1252", LocaleId.ENGLISH, LocaleId.ENGLISH));
	}
	
	@Test
	public void testDoubleExtractionBilingual() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root+"bilingual.txt", null));		
		RoundTripComparison rtc = new RoundTripComparison(false);
		assertTrue(rtc.executeCompare(filter, list, "windows-1252", LocaleId.ENGLISH, LocaleId.SPANISH));
	}
	
	@Test
	public void testDoubleExtractionEmptyVerses() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root+"empty_verses.vrsz", null));		
		RoundTripComparison rtc = new RoundTripComparison(false);
		assertTrue(rtc.executeCompare(filter, list, "windows-1252", LocaleId.ENGLISH, LocaleId.SPANISH));
	}
	
	@Test
	public void testTrados() throws URISyntaxException {
		@SuppressWarnings("resource")
		RawDocument rawDoc = new RawDocument(Util.toURI(root+"trados.vrsz"), "windows-1252", LocaleId.ENGLISH, LocaleId.SPANISH);
		filter.open(rawDoc);
		List<Event> events = new LinkedList<Event>();
		while(filter.hasNext()) {
			events.add(filter.next());
		}
		filter.close();
		
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertEquals("gh", tu.getName());
		assertEquals(AlignmentStatus.ALIGNED, tu.getAlignedSegments().getAlignmentStatus(LocaleId.SPANISH));
		assertEquals(2, tu.getSource().getSegments().count());
		assertEquals("\ta record. ", tu.getSource().getFirstSegment().toString());
		assertEquals("\tA RECORD. ", tu.getTarget(LocaleId.SPANISH).getFirstSegment().toString());
		
		tu = FilterTestDriver.getTextUnit(events, 2);
		assertEquals("source", tu.getName());
		assertEquals(AlignmentStatus.ALIGNED, tu.getAlignedSegments().getAlignmentStatus(LocaleId.SPANISH));
		assertEquals(1, tu.getSource().getSegments().count());
		assertEquals("\u00a0Add a Source", tu.getSource().getFirstSegment().toString());
		assertEquals("\u00a0ADD A SOURCE", tu.getTarget(LocaleId.SPANISH).getFirstSegment().toString());

		tu = FilterTestDriver.getTextUnit(events, 3);
		assertEquals("newsource", tu.getName());
		assertEquals(AlignmentStatus.ALIGNED, tu.getAlignedSegments().getAlignmentStatus(LocaleId.SPANISH));
		assertEquals(1, tu.getSource().getSegments().count());
		assertEquals("\u2014Add a New Source", tu.getSource().getFirstSegment().toString());
		assertEquals("\u2014ADD A NEW SOURCE", tu.getTarget(LocaleId.SPANISH).getFirstSegment().toString());

		tu = FilterTestDriver.getTextUnit(events, 4);
		assertEquals("sourcelink", tu.getName());
		assertEquals(AlignmentStatus.ALIGNED, tu.getAlignedSegments().getAlignmentStatus(LocaleId.SPANISH));
		assertEquals(1, tu.getSource().getSegments().count());
		assertEquals("\u2013Create a New Source", tu.getSource().getFirstSegment().toString());
		assertEquals("\u2013CREATE A NEW SOURCE", tu.getTarget(LocaleId.SPANISH).getFirstSegment().toString());

		tu = FilterTestDriver.getTextUnit(events, 5);
		assertEquals("suredetach", tu.getName());
		assertEquals(AlignmentStatus.ALIGNED, tu.getAlignedSegments().getAlignmentStatus(LocaleId.SPANISH));
		assertEquals(1, tu.getSource().getSegments().count());
		assertEquals("\u2003detach this source?\u2002", tu.getSource().getFirstSegment().toString());
		assertEquals("\u2003DETACH THIS SOURCE FROM THIS INDIVIDUAL?\u2002", tu.getTarget(LocaleId.SPANISH).getFirstSegment().toString());
	}
	
	//@Test
	public void testTradosRoundtrip() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root+"trados.vrsz", null));		
		RoundTripComparison rtc = new RoundTripComparison(false);
		assertTrue(rtc.executeCompare(filter, list, "windows-1252", LocaleId.ENGLISH, LocaleId.SPANISH));
	}
	
	@Test
	public void testOpenTwiceWithString() {
		@SuppressWarnings("resource")
		RawDocument rawDoc = new RawDocument("|vtest", LocaleId.ENGLISH);
		filter.open(rawDoc);
		filter.open(rawDoc);
		filter.close();
	}

	@Test(expected=OkapiBadFilterInputException.class)
	public void testMissingVerse() {
		String snippet = "|btest\nThis is a test.";
		FilterTestDriver.getTextUnit(getEvents(snippet), 1);
	}
	
	@Test
	public void testMissingBook() {
		String snippet = "|v1\nThis is a test.";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("This is a test.", tu.getSource().toString());
	}
	
	@Test
	public void testSidWithSpecialTerminator() {
		String snippet = "|v1 (sid)+| \nThis is a test.";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("This is a test.", tu.getSource().toString());
	}
	
	@Test
	public void testSidAndTradosSegmentMarkers() {
		String snippet = "|v1 (SOURCE)+| \n{0>SOURCE<}100{>TARGET<0}";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals(AlignmentStatus.ALIGNED, tu.getAlignedSegments().getAlignmentStatus(LocaleId.SPANISH));
		assertEquals("SOURCE", tu.getSource().getFirstSegment().toString());
		assertEquals("TARGET", tu.getTarget(LocaleId.SPANISH).getFirstSegment().toString());
	}
	
	@Test(expected=OkapiBadFilterInputException.class)
	public void testSidAndBrokenTradosSegmentMarkers() {
		String snippet = "|v1\n{0>SOURCE<}100{>TARGET<0";
		FilterTestDriver.getTextUnit(getEvents(snippet), 1);
	}
	
	private ArrayList<Event> getEvents (String snippet) {
		return FilterTestDriver.getEvents(filter, snippet, LocaleId.ENGLISH, LocaleId.SPANISH);
	}
}
