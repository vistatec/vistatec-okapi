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

package net.sf.okapi.filters.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.html.HtmlFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PropertiesFilterTest {
	
	private PropertiesFilter filter;
	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locFR = LocaleId.FRENCH;

	@Before
	public void setUp() {
		filter = new PropertiesFilter();
		IFilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		try (HtmlFilter htmlFilter = new HtmlFilter()) {
			for (FilterConfiguration config : htmlFilter.getConfigurations()) {
				fcMapper.addConfiguration(config);
			}
		}
		filter.setFilterConfigurationMapper(fcMapper);
	}

	@Test
	public void testDefaultInfo () {
		assertNotNull(filter.getParameters());
		assertNotNull(filter.getName());
		List<FilterConfiguration> list = filter.getConfigurations();
		assertNotNull(list);
		assertTrue(list.size()>0);
	}

	@Test
	public void testDoubleExtraction () {
		// Read all files in the data directory
		ArrayList<InputDocument> list = new ArrayList<>();
		FileLocation location = FileLocation.fromClass(getClass());

		list.add(new InputDocument(location.in("/Test01.properties").toString(), null));
		list.add(new InputDocument(location.in("/Test02.properties").toString(), "okf_properties@Test02.fprm"));
		list.add(new InputDocument(location.in("/Test03.properties").toString(), "okf_properties@Test03.fprm"));
		list.add(new InputDocument(location.in("/Test04.properties").toString(), "okf_properties@Test04.fprm"));
		list.add(new InputDocument(location.in("/issue_216.properties").toString(), "issue_216.fprm"));
	
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR, "out"));
	}

	@Test
	public void testStartDocument () {
		FileLocation location = FileLocation.fromClass(getClass()).in("/Test01.properties");
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(location.toString(), null),
			"UTF-8", locEN, locEN));
	}
	
	@Test
	public void testLineBreaks_CR () {
		String snippet = "Key1=Text1\rKey2=Text2\r";
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testMessagePlaceholders () {
		String snippet = "Key1={1}Text1{2}";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
		assertEquals("{1}Text1{2}", tu.getSource().toString());
	}
	
	@Test
	public void testMessagePlaceholdersEscaped () {
		// Message with place holders. They are treated an inline code by default
		String snippet = "Key1={1}Text1{2}";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
		assertEquals("{1}Text1{2}", tu.getSource().toString());
	}

	@Test
	public void testineBreaks_CRLF () {
		String snippet = "Key1=Text1\r\nKey2=Text2\r\n";
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testLineBreaks_LF () {
		String snippet = "Key1=Text1\n\n\nKey2=Text2\n";
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testEntry () {
		String snippet = "Key1=Text1\n# Comment\nKey2=Text2\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertNotNull(tu);
		assertEquals("Text2", tu.getSource().toString());
		assertEquals("Key2", tu.getName());
		assertTrue(tu.hasProperty(Property.NOTE));
		Property prop = tu.getProperty(Property.NOTE);
		assertEquals(" Comment", prop.getValue());
		assertTrue(prop.isReadOnly());
	}
	
	@Test
	public void testSplicedEntry () {
		String snippet = "Key1=Text1\nKey2=Text2 \\\nSecond line";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertNotNull(tu);
		assertEquals("Text2 Second line", tu.getSource().toString());
	}
	
	@Test
	public void testEscapes () {
		String snippet = "Key1=Text with \\u00E3";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("Text with \u00E3", tu.getSource().toString());
	}
	
	@Test
	public void testKeySpecial () {
		String snippet = "\\:\\= : Text1";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("Text1", tu.getSource().toString());
		assertEquals("\\:\\=", tu.getName());
	}
	
	@Test
	public void testLocDirectives_Skip () {
		String snippet = "#_skip\nKey1:Text1\nKey2:Text2";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		// Text1 not extracted because of the directive
		assertEquals("Text2", tu.getSource().toString());
	}
	
	@Test
	public void testLocDirectives_Group () {
		String snippet = "#_bskip\nKey1:Text1\n#_text\nKey2:Text2\nKey2:Text3";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		// Text1 not extracted because of the directive
		assertEquals("Text2", tu.getSource().toString());
		// No next TU because of _bskip
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertNull(tu);
	}
	
	@Test
	public void testSpecialChars () {
		String snippet = "Key1:Text1\\n=lf, \\t=tab, \\w=w, \\r=cr, \\\\=bs\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu); // Convert the \n
		assertEquals("Text1\n=lf, \t=tab, \\w=w, \\r=cr, \\\\=bs", tu.getSource().toString());
	}

	@Test
	public void testSpecialCharsInKey () {
		String snippet = "Key\\ \\:\\\\:Text1\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("Key\\ \\:\\\\", tu.getName());
		assertEquals("Text1", tu.getSource().toString());
	}

	@Test
	public void testSpecialCharsOutput () {
		String snippet = "Key1:Text1\\n=lf, \\t=tab \\w=w, \\r=cr, \\\\=bs\n";
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
	}

	@Test
	public void testWithSubfilter() {
		Parameters p = filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with \\u00E3 more <br> test</b>";
		List<Event> el = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(el, 1);
		p.setSubfilter(null);
		filter.setParameters(p);
		assertNotNull(tu);
		assertEquals("<b>Text with ã more <br> test</b>", tu.getSource().toString());
	}
	
	@Test
	public void testWithSubfilterTwoParas() {
		Parameters p = filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with \\u00E3 more</b> <p> test";
		List<Event> el = getEvents(snippet);
		ITextUnit tu1 = FilterTestDriver.getTextUnit(el, 1);
		ITextUnit tu2 = FilterTestDriver.getTextUnit(el, 2);
		p.setSubfilter(null);
		filter.setParameters(p);
		assertNotNull(tu1);
		assertNotNull(tu2);
		assertEquals("<b>Text with ã more</b>", tu1.getSource().toString());
		assertEquals("test", tu2.getSource().toString());
	}
	
	@Test
	public void testWithSubfilterWithEmbeddedMessagePH() {
		Parameters p = filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with {1} more {2} test</b>";
		List<Event> el = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(el, 1);
		p.setSubfilter(null);
		filter.setParameters(p);
		assertNotNull(tu);
		// the Properties filter code-finder rules are passed to the HTML/XML sub-filters, so {1} and {2} are seen as codes 
		assertEquals(4, tu.getSource().getFirstContent().getCodes().size());
		assertEquals("<b>Text with {1} more {2} test</b>", tu.getSource().toString());
	}
	
	@Test
	public void testWithSubfilterWithHTMLEscapes() {
		Parameters p = filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with &amp;=amp test</b>";
		List<Event> el = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(el, 1);
		p.setSubfilter(null);
		filter.setParameters(p);
		assertNotNull(tu);
		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
		assertEquals("<b>Text with &=amp test</b>", tu.getSource().toString());
	}
	
	@Test
	public void testWithSubfilterOutput () {
		Parameters p = filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with &amp;=amp test</b>\n";
		String result = FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
		p.setSubfilter(null);
		filter.setParameters(p);
	}
	
	@Test
	public void testWithSubfilterOutputEscapeExtended () {
		Parameters p = filter.getParameters();
		p.setSubfilter("okf_html");
		filter.setParameters(p);
		String inSnippet = "key=v\u00c3\u201el\u00c3\u00bc\u00c3\u00a9 w\u00c3\u00aeth <b>html</b>\n";
		String outSnippet = "key=v\\u00c3\\u201el\\u00c3\\u00bc\\u00c3\\u00a9 w\\u00c3\\u00aeth <b>html</b>\n";
		String result = FilterTestDriver.generateOutput(getEvents(inSnippet), filter.getEncoderManager(), locEN);
		assertEquals(outSnippet, result);
	}
	
	@Test
	public void testWithSubfilterOutputDoNotEscapeExtended () {
		Parameters p = filter.getParameters();
		p.setSubfilter("okf_html");
		p.setEscapeExtendedChars(false);		
		filter.setParameters(p);
		String snippet = "key=v\u00c3\u201el\u00c3\u00bc\u00c3\u00a9 w\u00c3\u00aeth <b>html</b>\n";
		String result = FilterTestDriver.generateOutput(getEvents(snippet), filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testHtmlOutput () {
//		Parameters p = filter.getParameters();
//		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with &amp;=amp test</b>";
		String result = FilterTestDriver.generateOutput(getEvents2(snippet),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
//		p.setSubfilter(null);
//		filter.setParameters(p);
	}
	
	@Test
	public void testWithSubfilterWithEmbeddedEscapedMessagePH() {
		Parameters p = filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with \\{1\\} more \\{2\\} test</b>";
		List<Event> el = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(el, 1);
		p.setSubfilter(null);
		filter.setParameters(p);
		assertNotNull(tu);
		// The Properties filter code-finder rules are passed to the HTML/XML sub-filters,
		// But {1} and {2} are escaped, so not seen as codes 
		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
		assertEquals("<b>Text with \\{1\\} more \\{2\\} test</b>", tu.getSource().toString());
	}
	
	@Test
	public void testDoubleExtractionSubFilter() {
		// Read all files in the data directory
		Parameters p = filter.getParameters();
		p.setSubfilter("okf_html");
		ArrayList<InputDocument> list = new ArrayList<>();
		FileLocation location = FileLocation.fromClass(getClass()).in("/Test05.properties");
		list.add(new InputDocument(location.toString(), null));	
		RoundTripComparison rtc = new RoundTripComparison();
		p.setSubfilter(null);
		filter.setParameters(p);
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR));
	}
	
	@Test
	public void testIdGeneration_defaultConfig() throws IOException {
		FileLocation location = FileLocation.fromClass(getClass()).in("/issue_216.properties");
		List<Event> list = getEvents(TestUtil.getFileAsString(location.asFile()));
		assertEquals(5, list.size());
		assertEquals(EventType.START_DOCUMENT, list.get(0).getEventType());
		assertEquals(EventType.TEXT_UNIT, list.get(1).getEventType());
		assertEquals(EventType.TEXT_UNIT, list.get(2).getEventType());
		assertEquals(EventType.TEXT_UNIT, list.get(3).getEventType());
		assertEquals(EventType.END_DOCUMENT, list.get(4).getEventType());
	}
	
	@Test
	public void testIdGeneration_subfiltersConfig() throws IOException {
		FileLocation location = FileLocation.fromClass(getClass());

		Parameters params = new Parameters();
		params.load(location.in("/issue_216.fprm").asUrl(), false);
		filter.setParameters(params);

		List<Event> list = getEvents(TestUtil.getFileAsString(location.in("/issue_216.properties").asFile()));
		assertEquals(29, list.size());
		
		assertEquals(EventType.START_DOCUMENT, list.get(0).getEventType());
		
		assertEquals(EventType.START_SUBFILTER, list.get(1).getEventType());
		assertEquals("someKey1_ssf1", list.get(1).getStartSubfilter().getId());
		assertEquals("sub-filter:someKey1", list.get(1).getStartSubfilter().getName());		
		
		assertEquals(EventType.START_SUBFILTER, list.get(10).getEventType());
		assertEquals("someKey2_ssf2", list.get(10).getStartSubfilter().getId());
		assertEquals("sub-filter:someKey2", list.get(10).getStartSubfilter().getName());
		
		assertEquals(EventType.START_SUBFILTER, list.get(19).getEventType());
		assertEquals("someKey3_ssf3", list.get(19).getStartSubfilter().getId());
		assertEquals("sub-filter:someKey3", list.get(19).getStartSubfilter().getName());
		
		assertEquals(EventType.END_SUBFILTER, list.get(8).getEventType());
		assertEquals(EventType.END_SUBFILTER, list.get(17).getEventType());
		assertEquals(EventType.END_SUBFILTER, list.get(26).getEventType());
		
		assertEquals(EventType.TEXT_UNIT, list.get(3).getEventType());
		assertEquals("someKey1_sf1_tu1", list.get(3).getTextUnit().getId());
		assertEquals("one-id", list.get(3).getTextUnit().getName());
		
		assertEquals(EventType.TEXT_UNIT, list.get(5).getEventType());
		assertEquals("someKey1_sf1_tu2", list.get(5).getTextUnit().getId());
		assertEquals("two-id", list.get(5).getTextUnit().getName());
		
		assertEquals(EventType.TEXT_UNIT, list.get(12).getEventType());
		assertEquals("someKey2_sf2_tu1", list.get(12).getTextUnit().getId());
		assertEquals("someKey2_2", list.get(12).getTextUnit().getName());
		
		assertEquals(EventType.TEXT_UNIT, list.get(14).getEventType());
		assertEquals("someKey2_sf2_tu2", list.get(14).getTextUnit().getId());
		assertEquals("two-id", list.get(14).getTextUnit().getName());
		
		assertEquals(EventType.TEXT_UNIT, list.get(21).getEventType());
		assertEquals("someKey3_sf3_tu1", list.get(21).getTextUnit().getId());
		assertEquals("someKey3_2", list.get(21).getTextUnit().getName());
		
		assertEquals(EventType.TEXT_UNIT, list.get(23).getEventType());
		assertEquals("someKey3_sf3_tu2", list.get(23).getTextUnit().getId());
		assertEquals("someKey3_4", list.get(23).getTextUnit().getName());
		
		assertEquals(EventType.END_DOCUMENT, list.get(28).getEventType());
	}

	private ArrayList<Event> getEvents(String snippet) {
		return FilterTestDriver.expandMultiEvents(FilterTestDriver.getEvents(filter,
							new RawDocument(snippet, locEN), null));
	}
	
	private ArrayList<Event> getEvents2(String snippet) {
		return FilterTestDriver.expandMultiEvents(FilterTestDriver.getEvents(new HtmlFilter(),
				new RawDocument(snippet, locEN), null));
	}
}
