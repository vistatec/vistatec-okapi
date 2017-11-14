/*===========================================================================
  Copyright (C) 2011-2013 by the Okapi Framework contributors
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

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.filters.txml.TXMLFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TXMLFilterTest {

	private TXMLFilter filter1;
	private GenericContent fmt;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	
	private static final String STARTFILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
		+ "<txml locale=\"EN\" version=\"1.0\" segtype=\"sentence\" createdby=\"WF2.3.0\" datatype=\"regexp\" "
		+ "targetlocale=\"FR\" file_extension=\"html\" editedby=\"WF2.3.0\">\r"
		+ "<skeleton>&lt;html&gt;\r"
		+ "&lt;p&gt;</skeleton>";
	
	public TXMLFilterTest () {
		filter1 = new TXMLFilter();
		fmt = new GenericContent();
	}
	
	@Before
	public void setUp() {
		RoundTripUtils.path = TestUtil.getParentDir(this.getClass(), "/dummy.txt")+"test_txml.json";
	}

	@Test
	public void testSimpleEntry () {
		String snippet = STARTFILE
			+ "<translatable blockId=\"b1\" datatype=\"html\">"
			+ "<segment segmentId=\"s1\" modified=\"true\">"
			+ "<source>Segment one</source><target>Segment un</target>"
			+ "</segment>"
			+ "<segment segmentId=\"2\">"
			+ "<source>segment two</source>"
			+ "</segment>"
			+ "</translatable>"
			+ "</txml>";

		List<Event> events = getEvents(filter1, snippet, locFR);		
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("b1", tu.getId());
		assertEquals("Segment one", tu.getSource().getFirstSegment().toString());
		TextContainer tc = tu.getTarget(locFR);
		assertNotNull(tc);
		assertEquals("Segment un", tc.getFirstSegment().toString());
		
		assertEquals(5, events.size());
		
		assertTrue(events.get(1).isDocumentPart());
		assertTrue(events.get(2).isTextUnit());
		assertTrue(events.get(3).isDocumentPart());
		
		List<Event> simplifiedEvents = roundTripSerilaizedEvents(events, locEN);
		assertEquals(5, simplifiedEvents.size());
	}

	@Test
	public void testRevisedEntry () {
		String snippet = STARTFILE
			+ "<translatable blockId=\"b1\" datatype=\"html\">"
			+ "<segment segmentId=\"s1\" modified=\"true\">"
			+ "<source>Segment one</source><target>Segment un</target>"
			+ "<revisions>"
			+ "<revision id=\"1\" creationid=\"Roberto\" creationdate=\"20130109T162701Z\" type=\"target\">"
			+ "<target>previous translation</target>"
			+ "</revision>"
			+ "</revisions>"
			+ "</segment>"
			+ "<segment segmentId=\"2\">"
			+ "<source>segment two</source>"
			+ "</segment>"
			+ "</translatable>"
			+ "</txml>";

		List<Event> events = getEvents(filter1, snippet, locFR);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("b1", tu.getId());
		assertEquals("Segment one", tu.getSource().getFirstSegment().toString());
		TextContainer tc = tu.getTarget(locFR);
		assertNotNull(tc);
		assertEquals("Segment un", tc.getFirstSegment().toString());
		
		List<Event> simplifiedEvents = roundTripSerilaizedEvents(events);
		tu = FilterTestDriver.getTextUnit(simplifiedEvents, 1);
		assertNotNull(tu);
		tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("b1", tu.getId());
		assertEquals("Segment one", tu.getSource().getFirstSegment().toString());
		tc = tu.getTarget(locFR);
		assertNotNull(tc);
		assertEquals("Segment un", tc.getFirstSegment().toString());
		
		events = getEvents(filter1, snippet, locFR);
		events.get(0).getStartDocument().setMultilingual(false);
		simplifiedEvents = roundTripSerilaizedEvents(events);
		tu = FilterTestDriver.getTextUnit(simplifiedEvents, 1);
		assertNotNull(tu);
		assertEquals("b1", tu.getId());
		assertEquals("Segment one", tu.getSource().getFirstSegment().toString());
		tc = tu.getTarget(locFR);
		assertNotNull(tc);
		assertEquals("Segment un", tc.getFirstSegment().toString());
		
	}

	@Test
	public void testEntryWithCodes () {
		String snippet = STARTFILE
			+ "<translatable blockId=\"b1\" datatype=\"html\">"
			+ "<segment segmentId=\"s1\" modified=\"true\">"
			+ "<source>Segment one</source><target>Segment un</target>"
			+ "</segment>"
			+ "<segment segmentId=\"2\">"
			+ "<source>Segment <ut x='1' type='bold'>&lt;b></ut>TWO<ut x='2' type='bold'>&lt;/b></ut></source>"
			+ "</segment>"
			+ "</translatable>"
			+ "</txml>";

		List<Event> events = getEvents(filter1, snippet, locFR);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		fmt.setContent(tu.getSource().get(1).getContent());
		assertEquals("[Segment one][Segment <1/>TWO<2/>]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("Segment <1/>TWO<2/>", fmt.toString());
		
		List<Event> simplifiedEvents = roundTripSerilaizedEvents(events);
		tu = FilterTestDriver.getTextUnit(simplifiedEvents, 1);
		assertNotNull(tu);
		tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		fmt.setContent(tu.getSource().get(1).getContent());
		assertEquals("[Segment one][Segment <1/>TWO<2/>]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("Segment <1/>TWO<2/>", fmt.toString());
		
		events = getEvents(filter1, snippet, locFR);
		events.get(0).getStartDocument().setMultilingual(false);
		simplifiedEvents = roundTripSerilaizedEvents(events);
		tu = FilterTestDriver.getTextUnit(simplifiedEvents, 1);
		assertNotNull(tu);
		fmt.setContent(tu.getSource().get(1).getContent());
		assertEquals("[Segment one][Segment <1/>TWO<2/>]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("Segment <1/>TWO<2/>", fmt.toString());
		
	}

	@Test
	public void testWS () {
		String snippet = STARTFILE
			+ "<translatable blockId=\"b1\" datatype=\"html\">"
			+ "<segment segmentId=\"s1\">"
			+ "<ws>  </ws>"
			+ "<source>text S</source>"
			+ "<target>text T</target>"
			+ "<ws>  <ut x='1'>&lt;br/></ut> </ws>"
			+ "</segment>"
			+ "<segment segmentId=\"s2\">"
			+ "<source>text S2</source>"
			+ "<ws> \t</ws>"
			+ "</segment>"
			+ "</translatable>"
			+ "</txml>";

		List<Event> events = getEvents(filter1, snippet, locFR);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		
		assertEquals("  [text S]  <1/> [text S2] \t", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("  [text T]  <1/> [] \t", fmt.printSegmentedContent(tu.getTarget(locFR), true));
		
		List<Event> simplifiedEvents = roundTripSerilaizedEvents(events);
		events.get(0).getStartDocument().setMultilingual(false);
		simplifiedEvents = roundTripSerilaizedEvents(events);
		tu = FilterTestDriver.getTextUnit(simplifiedEvents, 1);
		assertNotNull(tu);
		assertEquals("  [text S]  <1/> [text S2] \t", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("  [text T]  <1/> [] \t", fmt.printSegmentedContent(tu.getTarget(locFR), true));
		
		events = getEvents(filter1, snippet, locFR);
		events.get(0).getStartDocument().setMultilingual(false);
		simplifiedEvents = roundTripSerilaizedEvents(events);
		tu = FilterTestDriver.getTextUnit(simplifiedEvents, 1);
		assertNotNull(tu);
		assertEquals("  [text S]  <1/> [text S2] \t", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("  [text T]  <1/> [] \t", fmt.printSegmentedContent(tu.getTarget(locFR), true));
	}

	@Test
	public void testSegments () {
		String snippet = STARTFILE
			+ "<translatable blockId=\"b1\" datatype=\"html\">"
			+ "<segment segmentId=\"s1\">"
			+ "<ws>  </ws>"
			+ "<source>textS1</source>"
			+ "<target>textT1</target>"
			+ "<ws>  <ut x='1'>&lt;br/></ut> </ws>"
			+ "</segment>"
			+ "<segment segmentId=\"s2\">"
			+ "<source>textS2</source>"
			+ "<ws> \t</ws>"
			+ "</segment>"
			+ "<segment segmentId=\"s3\">"
			+ "<ws>{{</ws>"
			+ "<source></source>"
			+ "<ws>}}</ws>"
			+ "</segment>"
			+ "</translatable>"
			+ "</txml>";

		List<Event> events = getEvents(filter1, snippet, locFR);		
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertEquals("  [textS1]  <1/> [textS2] \t{{[]}}", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("  [textT1]  <1/> [] \t{{[]}}", fmt.printSegmentedContent(tu.getTarget(locFR), true));
		
		List<Event> simplifiedEvents = roundTripSerilaizedEvents(events);
		tu = FilterTestDriver.getTextUnit(simplifiedEvents, 1);
		assertNotNull(tu);
		assertEquals("  [textS1]  <1/> [textS2] \t{{[]}}", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("  [textT1]  <1/> [] \t{{[]}}", fmt.printSegmentedContent(tu.getTarget(locFR), true));
		
		events = getEvents(filter1, snippet, locFR);
		events.get(0).getStartDocument().setMultilingual(false);
		simplifiedEvents = roundTripSerilaizedEvents(events);
		tu = FilterTestDriver.getTextUnit(simplifiedEvents, 1);
		assertNotNull(tu);
		assertEquals("  [textS1]  <1/> [textS2] \t{{[]}}", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("  [textT1]  <1/> [] \t{{[]}}", fmt.printSegmentedContent(tu.getTarget(locFR), true));
	}
	
	@Test
	public void testEmptySegments () {
		String snippet = STARTFILE
			+ "<translatable blockId=\"b1\" datatype=\"html\">"
			+ "<segment segmentId=\"s1\">"
			+ "<ws>  </ws>"
			+ "<source></source>"
			+ "<ws>  <ut x='1'>&lt;br/></ut> </ws>"
			+ "</segment>"
			+ "<segment segmentId=\"s2\">"
			+ "<source></source>"
			+ "<ws> \t</ws>"
			+ "</segment>"
			+ "</translatable>"
			+ "</txml>";

		List<Event> events = getEvents(filter1, snippet, locFR);		
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertEquals("  []  <1/> [] \t", fmt.printSegmentedContent(tu.getSource(), true));
		assertNull(tu.getTarget(locFR));
		
		List<Event> simplifiedEvents = roundTripSerilaizedEvents(events);		
		events.get(0).getStartDocument().setMultilingual(false);
		simplifiedEvents = roundTripSerilaizedEvents(events);
		tu = FilterTestDriver.getTextUnit(simplifiedEvents, 1);
		assertNotNull(tu);
		assertEquals("  []  <1/> [] \t", fmt.printSegmentedContent(tu.getSource(), true));
		assertNull(tu.getTarget(locFR));
		
		events = getEvents(filter1, snippet, locFR);
		events.get(0).getStartDocument().setMultilingual(false);
		simplifiedEvents = roundTripSerilaizedEvents(events);
		tu = FilterTestDriver.getTextUnit(simplifiedEvents, 1);
		assertNotNull(tu);
		assertEquals("  []  <1/> [] \t", fmt.printSegmentedContent(tu.getSource(), true));
		assertNull(tu.getTarget(locFR));
	}

	private List<Event> getEvents (IFilter filter,
		String snippet,
		LocaleId trgLocId)
	{
		return FilterTestDriver.getEvents(filter, new RawDocument(snippet, locEN, trgLocId), null);
	}

}
