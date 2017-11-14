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

package net.sf.okapi.lib.tkit.roundtrip;

import static net.sf.okapi.lib.tkit.roundtrip.RoundTripUtils.roundTripSerilaizedEvents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.filters.properties.Parameters;
import net.sf.okapi.filters.properties.PropertiesFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PropertiesFilterTest {
	
	private PropertiesFilter filter;
	private LocaleId locEN = LocaleId.ENGLISH;

	@Before
	public void setUp() {
		filter = new PropertiesFilter();
		IFilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		DefaultFilters.setMappings(fcMapper, true, true);
		filter.setFilterConfigurationMapper(fcMapper);
		RoundTripUtils.path = TestUtil.getParentDir(this.getClass(), "/dummy.txt")+"test_properties.json";
	}
		
	@Test
	public void testLineBreaks_CR () {
		String snippet = "Key1=Text1\rKey2=Text2\r";
		String result = FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents(snippet)),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testMessagePlaceholders () {
		String snippet = "Key1={1}Text1{2}";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu);
		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
		assertEquals("{1}Text1{2}", tu.getSource().toString());
	}
	
	@Test
	public void testMessagePlaceholdersEscaped () {
		// Message with place holders. They are treated an inline code by default
		String snippet = "Key1={1}Text1{2}";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu);
		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
		assertEquals("{1}Text1{2}", tu.getSource().toString());
	}

	@Test
	public void testineBreaks_CRLF () {
		String snippet = "Key1=Text1\r\nKey2=Text2\r\n";
		String result = FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents(snippet)),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testLineBreaks_LF () {
		String snippet = "Key1=Text1\n\n\nKey2=Text2\n";
		String result = FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents(snippet)),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testEntry () {
		String snippet = "Key1=Text1\n# Comment\nKey2=Text2\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 2);
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
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 2);
		assertNotNull(tu);
		assertEquals("Text2 Second line", tu.getSource().toString());
	}
	
	@Test
	public void testEscapes () {
		String snippet = "Key1=Text with \\u00E3";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu);
		assertEquals("Text with \u00E3", tu.getSource().toString());
	}
	
	@Test
	public void testKeySpecial () {
		String snippet = "\\:\\= : Text1";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu);
		assertEquals("Text1", tu.getSource().toString());
		assertEquals("\\:\\=", tu.getName());
	}
	
	@Test
	public void testLocDirectives_Skip () {
		String snippet = "#_skip\nKey1:Text1\nKey2:Text2";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu);
		// Text1 not extracted because of the directive
		assertEquals("Text2", tu.getSource().toString());
	}
	
	@Test
	public void testLocDirectives_Group () {
		String snippet = "#_bskip\nKey1:Text1\n#_text\nKey2:Text2\nKey2:Text3";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu);
		// Text1 not extracted because of the directive
		assertEquals("Text2", tu.getSource().toString());
		// No next TU because of _bskip
		tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 2);
		assertNull(tu);
	}
	
	@Test
	public void testSpecialChars () {
		String snippet = "Key1:Text1\\n=lf, \\t=tab, \\w=w, \\r=cr, \\\\=bs\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu); // Convert the \n
		assertEquals("Text1\n=lf, \t=tab, \\w=w, \\r=cr, \\\\=bs", tu.getSource().toString());
	}

	@Test
	public void testSpecialCharsInKey () {
		String snippet = "Key\\ \\:\\\\:Text1\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu);
		assertEquals("Key\\ \\:\\\\", tu.getName());
		assertEquals("Text1", tu.getSource().toString());
	}

	@Test
	public void testSpecialCharsOutput () {
		String snippet = "Key1:Text1\\n=lf, \\t=tab \\w=w, \\r=cr, \\\\=bs\n";
		String result = FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents(snippet)),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testWithSubfilter() {
		Parameters p = (Parameters)filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with \\u00E3 more <br> test</b>";

		List<Event> el = roundTripSerilaizedEvents(getEvents(snippet));
		ITextUnit tu = FilterTestDriver.getTextUnit(el, 1);
		p.setSubfilter(null);
		filter.setParameters(p);
		assertNotNull(tu);
		assertEquals("<b>Text with ã more <br> test</b>", tu.getSource().toString());
	}
	
	@Test
	public void testWithSubfilterTwoParas() {
		Parameters p = (Parameters)filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with \\u00E3 more</b> <p> test";
		List<Event> el = roundTripSerilaizedEvents(getEvents(snippet));
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
		Parameters p = (Parameters)filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with {1} more {2} test</b>";
		List<Event> el = roundTripSerilaizedEvents(getEvents(snippet));
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
		Parameters p = (Parameters)filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with &amp;=amp test</b>";
		List<Event> el = roundTripSerilaizedEvents(getEvents(snippet));
		ITextUnit tu = FilterTestDriver.getTextUnit(el, 1);
		p.setSubfilter(null);
		filter.setParameters(p);
		assertNotNull(tu);
		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
		assertEquals("<b>Text with &=amp test</b>", tu.getSource().toString());
	}
	
	@Test
	public void testWithSubfilterOutput () {
		Parameters p = (Parameters)filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with &amp;=amp test</b>\n";
		String result = FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents(snippet)),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
		p.setSubfilter(null);
		filter.setParameters(p);
	}
	
	@Test
	public void testWithSubfilterOutputEscapeExtended () throws URISyntaxException, IOException {
		Parameters p = (Parameters)filter.getParameters();
		p.setSubfilter("okf_html");
		filter.setParameters(p);
		String inSnippet = "key=v\u00c3\u201el\u00c3\u00bc\u00c3\u00a9 w\u00c3\u00aeth <b>html</b>\n";
		String outSnippet = "key=v\\u00c3\\u201el\\u00c3\\u00bc\\u00c3\\u00a9 w\\u00c3\\u00aeth <b>html</b>\n";
		String result = FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents(inSnippet)), filter.getEncoderManager(), locEN);
		assertEquals(outSnippet, result);
	}
	
	@Test
	public void testWithSubfilterOutputDoNotEscapeExtended () throws URISyntaxException, IOException {
		Parameters p = (Parameters)filter.getParameters();
		p.setSubfilter("okf_html");
		p.setEscapeExtendedChars(false);		
		filter.setParameters(p);
		String snippet = "key=v\u00c3\u201el\u00c3\u00bc\u00c3\u00a9 w\u00c3\u00aeth <b>html</b>\n";
		String result = FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents(snippet)), filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testHtmlOutput () {
//		Parameters p = (Parameters)filter.getParameters();
//		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with &amp;=amp test</b>";
		String result = FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents2(snippet)),
			filter.getEncoderManager(), locEN);
		assertEquals(snippet, result);
//		p.setSubfilter(null);
//		filter.setParameters(p);
	}
	
	@Test
	public void testWithSubfilterWithEmbeddedEscapedMessagePH() {
		Parameters p = (Parameters)filter.getParameters();
		p.setSubfilter("okf_html");
		String snippet = "Key1=<b>Text with \\{1\\} more \\{2\\} test</b>";
		List<Event> el = roundTripSerilaizedEvents(getEvents(snippet));
		ITextUnit tu = FilterTestDriver.getTextUnit(el, 1);
		p.setSubfilter(null);
		filter.setParameters(p);
		assertNotNull(tu);
		// The Properties filter code-finder rules are passed to the HTML/XML sub-filters,
		// But {1} and {2} are escaped, so not seen as codes 
		assertEquals(2, tu.getSource().getFirstContent().getCodes().size());
		assertEquals("<b>Text with \\{1\\} more \\{2\\} test</b>", tu.getSource().toString());
	}
	
	private List<Event> getEvents(String snippet) {
		return FilterTestDriver.expandMultiEvents(
				FilterTestDriver.getEvents(filter, new RawDocument(snippet, locEN), null));
	}
	
	private List<Event> getEvents2(String snippet) {
		return FilterTestDriver.expandMultiEvents(
				FilterTestDriver.getEvents(new HtmlFilter(), new RawDocument(snippet, locEN), null));
	}

}
