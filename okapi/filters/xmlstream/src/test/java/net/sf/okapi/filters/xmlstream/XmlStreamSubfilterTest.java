/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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


package net.sf.okapi.filters.xmlstream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.filters.xmlstream.integration.XmlStreamTestUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XmlStreamSubfilterTest {
	
	private static LocaleId locEN = LocaleId.fromString("en");
	private XmlStreamFilter filter;
	private final FileLocation root = FileLocation.fromClass(getClass());
	
	@Before
	public void setUp() throws Exception {
		filter = new XmlStreamFilter();
	}
	
	@Test
	public void testSimple() throws Exception {
		URL configUrl = root.in("/subfilter-simple.yml").asUrl();
		URL inputUrl = root.in("/subfilter-simple.xml").asUrl();
		RawDocument rd = new RawDocument(inputUrl.toURI(), "UTF-8", locEN);
		List<Event> events = getEvents(filter, rd, configUrl);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("Translate me.", tu.getSource().toString());
		
		// Make sure only one TU was produced
		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNull(tu);
	}
	
	@Test
	public void testNestedTextunits() throws Exception {
		URL configUrl = root.in("/subfilter-simple.yml").asUrl();
		List<Event> events;
		String xml = "<xml><x1>foo</x1>bar</xml>";
		RawDocument rd = new RawDocument(xml, locEN);
		events = getEvents(filter, rd, configUrl);
		assertEquals("foo", FilterTestDriver.getTextUnit(events, 1).getSource().toString());
		assertEquals("bar", FilterTestDriver.getTextUnit(events, 2).getSource().toString());
	}
	
	@Test
	public void testTranslateAttributeSubfilter() throws Exception {
		URL configUrl = root.in("/translate-attr-subfilter.yml").asUrl();
		URL inputUrl = root.in("/translate-attr-subfilter.xml").asUrl();
		RawDocument rd = new RawDocument(inputUrl.toURI(), "UTF-8", locEN);
		List<Event> events = getEvents(filter, rd, configUrl);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("Translate me.", tu.getSource().toString());
		
		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu);
		assertEquals("<a>Translate me 4.<a>", tu.getSource().toString());
		
		tu = FilterTestDriver.getTextUnit(events, 3);
		assertNull(tu);
	}
	
	@Test
	public void testCdataSubfilter() throws Exception {
		URL configUrl = root.in("/cdataAsHTML.yml").asUrl();
		URL inputUrl = root.in("/simple_cdata.xml").asUrl();
		RawDocument rd = new RawDocument(inputUrl.toURI(), "UTF-8", locEN);
		List<Event> events = getEvents(filter, rd, configUrl);
		assertEquals("About", FilterTestDriver.getTextUnit(events, 1).getSource().toString());
		assertEquals("Testing", FilterTestDriver.getTextUnit(events, 2).getSource().toString());
		assertEquals("<b>Test</b> with some <u>HTML</u> <i>tags</i>.", 
					 FilterTestDriver.getTextUnit(events, 3).getSource().toString());
		// Make sure there's no "bonus" segment containing the placeholder that
		// references the CDATA
		assertNull(FilterTestDriver.getTextUnit(events, 4));
	}
	
	@Test
	public void testCdataSubfilterEmptyElement() throws Exception {
		URL configUrl = root.in("/cdataAsHTML.yml").asUrl();
		URL inputUrl = root.in("/empty_element.xml").asUrl();
		RawDocument rd = new RawDocument(inputUrl.toURI(), "UTF-8", locEN);
		List<Event> events = getEvents(filter, rd, configUrl);	
		assertEquals("foobar", 
					 FilterTestDriver.getTextUnit(events, 1).getSource().toString());
		// Make sure there's no "bonus" segment containing the placeholder that
		// references the CDATA
		assertNull(FilterTestDriver.getTextUnit(events, 2));
	}

	// Test for Issue #339: interaction between subfiltering and
	// GROUP rules.
	@Test
	public void testCdataMerging() throws Exception {
		URL configUrl = root.in("/cdataWithGroup.yml").asUrl();
		URL inputUrl = root.in("/cdataWithGroup.xml").asUrl();
		RawDocument rd = new RawDocument(inputUrl.toURI(), "UTF-8", locEN);
		List<Event> events = getEvents(filter, rd, configUrl);
		assertEquals("Test", FilterTestDriver.getTextUnit(events, 1).getSource().toString());

		// Test to make sure the skeleton all ends up in the right place
		ArrayList<Event> expectedEvents = new ArrayList<Event>();
		addStartEvents(expectedEvents);
		addDocumentPart(expectedEvents, "dp1", "<Solution>");
		addStartGroup(expectedEvents, "sg1", "<RESOLUTION>");
		expectedEvents.add(new Event(EventType.START_SUBFILTER));
		addTextUnit(expectedEvents, "sg1_tu1", "sd1_1", "Test", "<p>[#$$self$]");
		addDocumentPart(expectedEvents, "sg1_dp1", "</p>");
		expectedEvents.add(new Event(EventType.END_SUBFILTER));
		addEndGroup(expectedEvents, "sg1", "<![CDATA[[#$sg1_ssf1]]]></RESOLUTION>");
		addDocumentPart(expectedEvents, "dp3", "</Solution>");
		addDocumentPart(expectedEvents, "dp4", "\n");
		addEndEvents(expectedEvents);
		Iterator<Event> expectedIt = expectedEvents.iterator();
		for (Event e : events) {
			Event ee = expectedIt.next();
			if (!FilterTestDriver.laxCompareEvent(ee, e)) {
				fail("Event mismatch: expected " + ee + " but found " + e);
			}
		}
	}
	
	@Test
	public void issue375() throws Exception {
		// doesn't group back nested translation
		URL configUrl = root.in("/Issue375.yml").asUrl();
		String snippet = 
			"<SOLUTIONS>\n" +
			"<![CDATA[Attachments in <img width=\"13\" height=\"15\" src=\"mail-p_attach_all_01.png\" alt=\"paper clip\" /> end tag]]>\n" +
			"</SOLUTIONS>";
		RawDocument rd = new RawDocument(snippet, locEN);
		List<Event> events = getEvents(filter, rd, configUrl);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("paper clip", tu.getSource().toString());
		
		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu);
		assertEquals("Attachments in [#$sd1_sf1_dp1] end tag", tu.getSource().toString());
		
		tu = FilterTestDriver.getTextUnit(events, 3);
		assertNull(tu);
		
		assertEquals(snippet, XmlStreamTestUtils.generateOutput(
				XmlStreamTestUtils.getEvents(snippet, filter, configUrl), snippet, locEN,
				filter));
	}

	@Test
	public void testSubfiltersProduceDistinctTextUnitIds() throws Exception {
		URL configUrl = root.in("/cdataAsHTML.yml").asUrl();
		RawDocument rd = new RawDocument(root.in("/multi_cdata.xml").asInputStream(), "UTF-8", locEN);
		List<ITextUnit> tus = FilterTestDriver.filterTextUnits(getEvents(filter, rd, configUrl));
		assertEquals(2, tus.size());
		assertNotEquals(tus.get(0).getId(), tus.get(1).getId());
	}

	@Test
	public void testJsonSubfilterEvents() {
		final String SNIPPET = "<json>{ \"key\" : \"value\" }</json>";
		FilterConfigurationMapper mapper = new FilterConfigurationMapper();
		mapper.addConfigurations("net.sf.okapi.filters.json.JSONFilter");
		IFilter filter = new XmlStreamFilter();
		filter.setFilterConfigurationMapper(mapper);
		URL configUrl = root.in("/subfilter-json.yml").asUrl();
		RawDocument rd = new RawDocument(SNIPPET, LocaleId.ENGLISH);
		List<Event> events = FilterTestDriver.getEvents(filter, rd, new Parameters(configUrl));
		List<ITextUnit> tus = FilterTestDriver.filterTextUnits(events);
		assertEquals(1, tus.size());
		assertEquals("value", tus.get(0).getSource().toString());
		String output = FilterTestDriver.generateOutput(filter, events, locEN, StandardCharsets.UTF_8);
		assertEquals(SNIPPET, output);
	}

	@Test
	public void testJsonSubfilterWithHtmlEvents() {
		// XML, containing pcdata JSON, containing HTML.  A feast!
		final String SNIPPET = "<json>{ \"key\" : \"&lt;p&gt;value&lt;\\/p&gt;\" }</json>";

		FilterConfigurationMapper mapper = new FilterConfigurationMapper();
		mapper.addConfigurations("net.sf.okapi.filters.html.HtmlFilter");
		net.sf.okapi.filters.json.Parameters jsonParams = new net.sf.okapi.filters.json.Parameters();
		jsonParams.setSubfilter("okf_html");
		mapper.addConfiguration(new FilterConfiguration("okf_json@html", "application/json",
					"net.sf.okapi.filters.json.JSONFilter", "JSON+HTML", "", null, jsonParams, ".json"));
		IFilter filter = new XmlStreamFilter();
		filter.setFilterConfigurationMapper(mapper);
		URL configUrl = root.in("/subfilter-json-html.yml").asUrl();
		RawDocument rd = new RawDocument(SNIPPET, LocaleId.ENGLISH);
		List<Event> events = FilterTestDriver.getEvents(filter, rd, new Parameters(configUrl));
		List<ITextUnit> tus = FilterTestDriver.filterTextUnits(events);
		assertEquals(1, tus.size());
		assertEquals("value", tus.get(0).getSource().toString());
		String output = FilterTestDriver.generateOutput(filter, events, locEN, StandardCharsets.UTF_8);
		assertEquals(SNIPPET, output);
	}

	private void addStartEvents(ArrayList<Event> events) {		
		events.add(new Event(EventType.START_DOCUMENT, new StartDocument("sd1")));
	}

	private void addEndEvents(ArrayList<Event> events) {
		events.add(new Event(EventType.END_DOCUMENT, new Ending("ed2")));
	}
	
	private void addDocumentPart(ArrayList<Event> events, String id, String skeleton) {
		events.add(new Event(EventType.DOCUMENT_PART, 
				new DocumentPart(id, false, new GenericSkeleton(skeleton))));
	}

	private void addStartGroup(ArrayList<Event> events, String id, String skeleton) {
		StartGroup sg = new StartGroup(null, id);
		sg.setSkeleton(new GenericSkeleton(skeleton));
		events.add(new Event(EventType.START_GROUP, sg)); 
	}
	
	private void addEndGroup(ArrayList<Event> events, String id, String skeleton) {
		Ending eg = new Ending(id);
		eg.setSkeleton(new GenericSkeleton(skeleton));
		events.add(new Event(EventType.END_GROUP, eg));
	}
	
	private void addTextUnit(ArrayList<Event> events, String id, String name, String text, String skeleton) {
		TextUnit tu = new TextUnit(id, text, false);
		tu.setName(name);
		tu.setType("paragraph");
		tu.setSkeleton(new GenericSkeleton(skeleton));
		events.add(new Event(EventType.TEXT_UNIT, tu));
	}
	
	private ArrayList<Event> getEvents(XmlStreamFilter filter, RawDocument doc, URL params) {    
        FilterConfigurationMapper mapper = new FilterConfigurationMapper();
        mapper.addConfigurations("net.sf.okapi.filters.html.HtmlFilter");
        filter.setFilterConfigurationMapper(mapper);
        return FilterTestDriver.getEvents(filter, doc, new Parameters(params));
    }   
}
