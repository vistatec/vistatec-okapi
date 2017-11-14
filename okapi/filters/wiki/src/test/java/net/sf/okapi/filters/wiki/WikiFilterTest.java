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

package net.sf.okapi.filters.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;

@RunWith(JUnit4.class)
public class WikiFilterTest {
	
	private WikiFilter filter;
	private FileLocation root;
		
	@Before
	public void setUp() {
		filter = new WikiFilter();
		filter.setOptions(LocaleId.ENGLISH, LocaleId.SPANISH, "UTF-8", true);
		root = FileLocation.fromClass(this.getClass());
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
			new InputDocument(root.in("/dokuwiki.txt").toString(), null),
			"UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH));
	}

	@Test
	public void testSimpleLine() {
		String snippet = "This is a test.";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("This is a test.", tu.getSource().toString());
	}
	
	@Test
	public void testMultipleLines() {
		String snippet = "This is \na \ntest.";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("This is a test.", tu.getSource().toString());
	}
	
	@Test
	public void testHeader() {
		String snippet = "=== This is a test. ===";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("This is a test.", tu.getSource().toString());
	}
	
	@Test
	public void testTable() {
		String snippet = "^ Table header 1 ^ Table header 2 |\n"
					+ "| Table cell 1 | Table cell 2 |";
		ArrayList<Event> events = getEvents(snippet);
		ITextUnit tu1 = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu1);
		assertEquals("Table header 1", tu1.getSource().toString());
		ITextUnit tu2 = FilterTestDriver.getTextUnit(events, 4);
		assertNotNull(tu2);
		assertEquals("Table cell 2", tu2.getSource().toString());
	}
	
	@Test
	public void testImageCaption() {
		String snippet = "This is a test. {{image.jpg|This is a caption.}}";
		ArrayList<Event> events = getEvents(snippet);
		ITextUnit tu1 = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu1);
		assertEquals("This is a caption.", tu1.getSource().toString());
		ITextUnit tu2 = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu2);
		assertEquals("This is a test. {{image.jpg|[#$tu2]}}", tu2.getSource().toString());
		assertEquals("This is a test. ", tu2.getSource().getCodedText());
	}
	
	@Test
	public void testSimilarHtmlTags() {
		// Actual tag here makes markup malformed, cuts off TU.
		String snippet = "This is <file> a test.";
		ArrayList<Event> events = getEvents(snippet);
		ITextUnit tu1 = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu1);
		assertEquals("This is", tu1.getSource().toString());
		// Slightly different tag should not be interpreted as markup.
		snippet = "This is <files> a test.";
		events = getEvents(snippet);
		tu1 = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu1);
		assertEquals("This is <files> a test.", tu1.getSource().toString());
	}
	
	@Test
	public void testComplexSeparatingWhitespace() {
		String snippet = "{{image.jpg}}\n \nThis is text.";
		ArrayList<Event> events = getEvents(snippet);
		ITextUnit tu1 = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu1);
		assertEquals("{{image.jpg}}", tu1.getSource().toString());
		ITextUnit tu2 = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu2);
		assertEquals("This is text.", tu2.getSource().toString());
	}
	
	@Test
	public void testDoubleExtraction() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/dokuwiki.txt").toString(), null));		
		RoundTripComparison rtc = new RoundTripComparison(false);
		assertTrue(rtc.executeCompare(filter, list, "utf-8", LocaleId.ENGLISH, LocaleId.ENGLISH));
	}
	
	@Test
	public void testOpenTwiceWithString() {
		RawDocument rawDoc = new RawDocument("This is a test.", LocaleId.ENGLISH);
		filter.open(rawDoc);
		filter.open(rawDoc);
		filter.close();
	}
	
	private ArrayList<Event> getEvents(String snippet)
	{
		return FilterTestDriver.getEvents(filter, snippet, LocaleId.ENGLISH, LocaleId.SPANISH);
	}
}
