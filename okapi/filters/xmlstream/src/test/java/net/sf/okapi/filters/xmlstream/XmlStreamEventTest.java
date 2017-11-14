/*===========================================================================
  Copyright (C) 2008 Jim Hargrave
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

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.filters.xmlstream.integration.XmlStreamTestUtils;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XmlStreamEventTest {

	private XmlStreamFilter filter;
	private URL parameters;
	private final FileLocation root = FileLocation.fromClass(getClass());

	@Before
	public void setUp() throws Exception {
		filter = new XmlStreamFilter();
		parameters = root.in("/xml_wellformedConfiguration.yml").asUrl();
	}

	@Test
	public void testMetaTagContent() {
		String snippet = "<meta http-equiv=\"keywords\" content=\"one,two,three\"/>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		// Build the input
		GenericSkeleton skel = new GenericSkeleton();
		ITextUnit tu = new TextUnit("tu1", "one,two,three");
		skel.add("content=\"");
		skel.addContentPlaceholder(tu);
		skel.add("\"");
		tu.setIsReferent(true);
		tu.setType("content");
		tu.setSkeleton(skel);
		events.add(new Event(EventType.TEXT_UNIT, tu));

		skel = new GenericSkeleton();
		DocumentPart dp = new DocumentPart("dp1", false);
		skel.add("<meta http-equiv=\"keywords\" ");
		skel.addReference(tu);
		skel.add("/>");
		dp.setSkeleton(skel);
		events.add(new Event(EventType.DOCUMENT_PART, dp));

		addEndEvents(events);

		assertTrue(FilterTestDriver.laxCompareEvents(events,
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}

	@Test
	public void testPWithAttributes() {
		String snippet = "<p title='my title' dir='rtl'>Text of p</p>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		// Build the input
		GenericSkeleton skel = new GenericSkeleton();
		ITextUnit tu2 = new TextUnit("tu2", "my title");
		skel.add("title='");
		skel.addContentPlaceholder(tu2);
		skel.add("'");
		tu2.setIsReferent(true);
		tu2.setType("title");
		tu2.setSkeleton(skel);
		events.add(new Event(EventType.TEXT_UNIT, tu2));

		skel = new GenericSkeleton();
		ITextUnit tu1 = new TextUnit("tu1", "Text of p");
		tu1.setType("paragraph");
		skel.add("<p ");
		skel.addReference(tu2);
		skel.add(" dir='");
		skel.addValuePlaceholder(tu1, "dir", null);
		tu1.setSourceProperty(new Property("dir", "rtl", false));
		skel.add("'>");
		skel.addContentPlaceholder(tu1);
		skel.append("</p>");
		tu1.setSkeleton(skel);
		events.add(new Event(EventType.TEXT_UNIT, tu1));

		addEndEvents(events);
		assertTrue(FilterTestDriver.laxCompareEvents(events,
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}
	
	@Test
	public void testLang() {
		String snippet = "<dummy xml:lang=\"en\"/>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		// Build the input
		GenericSkeleton skel = new GenericSkeleton();
		DocumentPart dp1 = new DocumentPart("dp1", false);
		skel.add("<dummy xml:lang=\"");
		dp1.setSourceProperty(new Property("language", "en", false));
		skel.addValuePlaceholder(dp1, "language", null);		
		skel.add("\"/>");
		dp1.setSkeleton(skel);
		events.add(new Event(EventType.DOCUMENT_PART, dp1));

		addEndEvents(events);

		assertTrue(FilterTestDriver.compareEvents(events,
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}
	
	@Test
	public void testIdOnP() {
		String snippet = "<p id=\"foo\"/>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		// Build the input
		ITextUnit tu1 = new TextUnit("tu1", "");		
		tu1.setName("foo-id");
		tu1.setType("paragraph");
		tu1.setMimeType(MimeTypeMapper.XML_MIME_TYPE);
		tu1.setSourceProperty(new Property("id", "foo", true));
		GenericSkeleton skel = new GenericSkeleton();		
		skel.add("<p id=\"foo\"/>");
		skel.addContentPlaceholder(tu1);
		tu1.setSkeleton(skel);
		events.add(new Event(EventType.TEXT_UNIT, tu1));

		addEndEvents(events);

		assertTrue(FilterTestDriver.compareEvents(events,
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}
	
	// TODO - re-enable test when logic in AbstractBaseFilter is fixed 
	@Test @Ignore
	public void testTextUnitWithoutText() {
		String snippet = "<b>    <font>  </font> </b>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		// Build the input
		GenericSkeleton skel = new GenericSkeleton();
		DocumentPart dp1 = new DocumentPart("dp1", false);
		skel.add(snippet);
		dp1.setSkeleton(skel);
		events.add(new Event(EventType.DOCUMENT_PART, dp1));

		addEndEvents(events);

		assertTrue(FilterTestDriver.compareEvents(events,
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}
	
	@Test
	public void testXmlLang() {
		String snippet = "<yyy xml:lang=\"en\"/>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		// Build the input
		GenericSkeleton skel = new GenericSkeleton();
		DocumentPart dp1 = new DocumentPart("dp1", false);
		skel.add("<yyy xml:lang=\"");
		dp1.setSourceProperty(new Property("language", "en", false));
		skel.addValuePlaceholder(dp1, "language", null);		
		skel.add("\"/>");
		dp1.setSkeleton(skel);
		events.add(new Event(EventType.DOCUMENT_PART, dp1));

		addEndEvents(events);

		assertTrue(FilterTestDriver.compareEvents(events,
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}
	
	@Test
	public void testPWithInlines() {
		String snippet = "<p>Before <b>bold</b> <a href=\"there\"/> after.</p>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		GenericSkeleton skel = new GenericSkeleton();
		DocumentPart dp1 = new DocumentPart("dp1", true);
		skel.add("<a href=\"");
		skel.addValuePlaceholder(dp1, "href", null);
		dp1.setSourceProperty(new Property("href", "there", false));
		skel.add("\"/>");
		dp1.setName("a");
		dp1.setSkeleton(skel);
		events.add(new Event(EventType.DOCUMENT_PART, dp1));

		skel = new GenericSkeleton();
		ITextUnit tu1 = new TextUnit("tu1", "Before ");
		tu1.setType("paragraph");
		TextFragment tf = tu1.getSource().getFirstContent();
		Code code = new Code(TagType.OPENING, "b", "<b>");
		code.setType(Code.TYPE_BOLD);
		tf.append(code);
		tf.append("bold");
		code = new Code(TagType.CLOSING, "b", "</b>");
		code.setType(Code.TYPE_BOLD);
		tf.append(code);
		tf.append(" ");
		code = new Code(TagType.PLACEHOLDER, "a");
		code.setType(Code.TYPE_LINK);
		code.appendReference("dp1");
		tf.append(code);
		tf.append(" after.");
		skel.add("<p>");
		skel.addContentPlaceholder(tu1);
		skel.append("</p>");
		tu1.setSkeleton(skel);
		events.add(new Event(EventType.TEXT_UNIT, tu1));

		addEndEvents(events);

		assertTrue(FilterTestDriver.laxCompareEvents(events,
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}

	@Test
	public void testPWithComment() {
		String snippet = "<p>Before <!--comment--> after.</p>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		GenericSkeleton skel = new GenericSkeleton();
		
		ITextUnit tu1 = new TextUnit("tu1", "Before ");
		tu1.setType("paragraph");
		TextFragment tf = tu1.getSource().getFirstContent();
		Code code = new Code(TagType.PLACEHOLDER, Code.TYPE_COMMENT, "<!--comment-->");
		tf.append(code);
		tf.append(" after.");
		
		skel.append("<p>");
		skel.addContentPlaceholder(tu1);
		skel.append("</p>");
		tu1.setSkeleton(skel);
		
		events.add(new Event(EventType.TEXT_UNIT, tu1));
		
		
		addEndEvents(events);

		assertTrue(FilterTestDriver.laxCompareEvents(events,
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}

	@Test
	public void testPWithProcessingInstruction() {
		String snippet = "<p>Before <?PI?> after.</p>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		GenericSkeleton skel = new GenericSkeleton();
		
		ITextUnit tu1 = new TextUnit("tu1", "Before ");
		tu1.setType("paragraph");
		TextFragment tf = tu1.getSource().getFirstContent();
		Code code = new Code(TagType.PLACEHOLDER, Code.TYPE_XML_PROCESSING_INSTRUCTION, "<?PI?>");		
		tf.append(code);
		tf.append(" after.");
		skel.append("<p>");
		skel.addContentPlaceholder(tu1);
		skel.append("</p>");
		tu1.setSkeleton(skel);
		events.add(new Event(EventType.TEXT_UNIT, tu1));		
		
		addEndEvents(events);

		assertTrue(FilterTestDriver.laxCompareEvents(events,
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}	

	@Test
	public void testPWithInlines2() {
		String snippet = "<p>Before <b>bold</b> <img href=\"there\" alt=\"text\"/> after.</p>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		GenericSkeleton skel = new GenericSkeleton();
		ITextUnit tu2 = new TextUnit("tu2", "text");
		skel.add("alt=\"");
		skel.addContentPlaceholder(tu2);
		skel.add("\"");
		tu2.setIsReferent(true);
		tu2.setType("alt");
		tu2.setSkeleton(skel);
		events.add(new Event(EventType.TEXT_UNIT, tu2));

		skel = new GenericSkeleton();
		DocumentPart dp1 = new DocumentPart("dp1", true);
		skel.add("<img href=\"");
		dp1.setSourceProperty(new Property("href", "there", false));
		skel.addValuePlaceholder(dp1, "href", null);
		skel.add("\" ");
		skel.addReference(tu2);
		skel.add("/>");
		dp1.setIsReferent(true);
		dp1.setType("img");
		dp1.setSkeleton(skel);
		events.add(new Event(EventType.DOCUMENT_PART, dp1));

		skel = new GenericSkeleton();
		ITextUnit tu1 = new TextUnit("tu1", "Before ");
		tu1.setType("paragraph");
		TextFragment tf = tu1.getSource().getFirstContent();
		Code code = new Code(TagType.OPENING, "b", "<b>");
		code.setType(Code.TYPE_BOLD);
		tf.append(code);
		tf.append("bold");
		code = new Code(TagType.CLOSING, "b", "</b>");
		code.setType(Code.TYPE_BOLD);
		tf.append(code);
		tf.append(" ");
		code = new Code(TagType.PLACEHOLDER, "img");
		code.setType(Code.TYPE_IMAGE);
		code.appendReference("dp1");
		tf.append(code);
		tf.append(" after.");
		skel.add("<p>");
		skel.addContentPlaceholder(tu2);
		skel.append("</p>");
		tu1.setSkeleton(skel);
		events.add(new Event(EventType.TEXT_UNIT, tu1));

		addEndEvents(events);

		assertTrue(FilterTestDriver.laxCompareEvents(events,
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}

	@Test
	public void testTableGroups() {
		String snippet = "<table id=\"100\"><tr><td>text</td></tr></table>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		StartGroup g1 = new StartGroup("ssd0", "sg1");
		g1.setSkeleton(new GenericSkeleton("<table id=\"100\">"));
		events.add(new Event(EventType.START_GROUP, g1));

		StartGroup g2 = new StartGroup("sg1", "sg2");
		g2.setSkeleton(new GenericSkeleton("<tr>"));
		events.add(new Event(EventType.START_GROUP, g2));		

		GenericSkeleton skel = new GenericSkeleton();
		ITextUnit tu = new TextUnit("tu1", "text");
		tu.setType("td");
		
		skel.append("<td>");
		skel.addContentPlaceholder(tu);
		skel.append("</td>");
		tu.setSkeleton(skel);
		events.add(new Event(EventType.TEXT_UNIT, tu));
		
		
		Ending e2 = new Ending("eg2");
		e2.setSkeleton(new GenericSkeleton("</tr>"));
		events.add(new Event(EventType.END_GROUP, e2));

		Ending e3 = new Ending("eg3");
		e3.setSkeleton(new GenericSkeleton("</table>"));
		events.add(new Event(EventType.END_GROUP, e3));

		addEndEvents(events);

		assertTrue(FilterTestDriver.laxCompareEvents(events,
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}

	@Test
	public void testGroupInPara() {
		String snippet = "<p>Text before list:" + 
		"<ul>" + 
		"<li>Text of item 1</li>" + 
		"<li>Text of item 2</li>" + 
		"</ul>" + "and text after the list.</p>";
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);

		GenericSkeleton tu3skel = new GenericSkeleton("<p>");
		ITextUnit tu3 = new TextUnit("tu3", "Text before list:");
		tu3.setSkeleton(tu3skel);
		tu3skel.addContentPlaceholder(tu3);
		tu3.setType("paragraph");
				
		// embedded list
		StartGroup g1 = new StartGroup(tu3.getId(), "sg1");
		g1.setIsReferent(true);
		g1.setSkeleton(new GenericSkeleton("<ul>"));		

		TextFragment tf = tu3.getSource().getFirstContent();
		Code c = new Code(TagType.PLACEHOLDER, "ul", TextFragment.makeRefMarker("sg1"));
		c.setReferenceFlag(true);
		tf.append(c);		
		events.add(new Event(EventType.START_GROUP, g1));

		GenericSkeleton tu1skel = new GenericSkeleton();
		ITextUnit tu1 = new TextUnit("tu1", "Text of item 1");		
		tu1.setType("li");		
		tu1skel.append("<li>");
		tu1skel.addContentPlaceholder(tu1);
		tu1skel.append("</li>");
		tu1.setSkeleton(tu1skel);		
		events.add(new Event(EventType.TEXT_UNIT, tu1));
				
		GenericSkeleton tu2skel = new GenericSkeleton();
		ITextUnit tu2 = new TextUnit("tu2", "Text of item 2");		
		tu2.setType("li");
		tu2skel.append("<li>");
		tu2skel.addContentPlaceholder(tu2);
		tu2skel.append("</li>");
		tu2.setSkeleton(tu2skel);
		events.add(new Event(EventType.TEXT_UNIT, tu2));

		Ending e3 = new Ending("eg3");
		e3.setSkeleton(new GenericSkeleton("</ul>"));
		events.add(new Event(EventType.END_GROUP, e3));
				
		tf.append("and text after the list.");				
		tu3skel.append("</p>");		
		events.add(new Event(EventType.TEXT_UNIT, tu3));
		
		addEndEvents(events);

		assertTrue(FilterTestDriver.laxCompareEvents(events, 
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}
	
	@Test
	public void testPreserveWhitespace() {
		String snippet = "<pre>\twhitespace is preserved</pre>"; 
		ArrayList<Event> events = new ArrayList<Event>();

		addStartEvents(events);
		
		GenericSkeleton skel = new GenericSkeleton();		
		
		ITextUnit tu = new TextUnit("tu1", "\twhitespace is preserved");
		tu.setType("pre");
		tu.setPreserveWhitespaces(true);
		skel.append("<pre>");
		skel.addContentPlaceholder(tu);
		skel.append("</pre>");
		tu.setSkeleton(skel);
	
		events.add(new Event(EventType.TEXT_UNIT, tu));
		
		addEndEvents(events);

		assertTrue(FilterTestDriver.laxCompareEvents(events, 
				XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
	}

	@Test
	public void testExcludeByDefault() {
		String snippet = "<xml><test>Exclude this</test><test translate='y'>Include this</test>" +
				 "<test>Exclude this but <inline translate='y'>Include this too</inline></test></xml>";
		URL originalParameters = parameters;
		parameters = root.in("/excludeByDefault.yml").asUrl();
		ArrayList<Event> events = new ArrayList<Event>();
		addStartEvents(events);
		
		GenericSkeleton skel = new GenericSkeleton();		
		DocumentPart dp = new DocumentPart("dp1", false);
		skel.add("<xml><test>Exclude this</test><test translate='y'>");
		dp.setSkeleton(skel);
		events.add(new Event(EventType.DOCUMENT_PART, dp));
		
		ITextUnit tu = new TextUnit("tu1", "Include this");
		events.add(new Event(EventType.TEXT_UNIT, tu));
		
		skel = new GenericSkeleton();
		dp = new DocumentPart("dp2", false);
		skel.add("</test><test>Exclude this but <inline translate='y'>");
		dp.setSkeleton(skel);
		events.add(new Event(EventType.DOCUMENT_PART, dp));
		
		tu = new TextUnit("tu2", "Include this too");
		events.add(new Event(EventType.TEXT_UNIT, tu));
		
		skel = new GenericSkeleton();
		dp = new DocumentPart("dp3", false);
		skel.add("</inline></test></xml>");
		dp.setSkeleton(skel);
		events.add(new Event(EventType.DOCUMENT_PART, dp));
		
		addEndEvents(events);

		try {
			assertTrue(FilterTestDriver.laxCompareEvents(events, 
					XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
		}
		finally {
			parameters = originalParameters;
		}
	}
	
	@Test
	public void testEntitiesInSkeletonParts() throws Exception {
		URL originalParameters = parameters;
		parameters = root.in("/excludeByDefault.yml").asUrl();
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<xml><foo>&lt;foo&gt;and &amp; and &lt;/foo&gt;</foo></xml>\n";
		ArrayList<Event> expectedEvents = new ArrayList<Event>();
		addStartEvents(expectedEvents);
		GenericSkeleton skel = new GenericSkeleton();
		DocumentPart dp = new DocumentPart("dp1", false);
		skel.add(snippet);
		dp.setSkeleton(skel);
		expectedEvents.add(new Event(EventType.DOCUMENT_PART, dp));
		addEndEvents(expectedEvents);
		try {
			assertTrue(FilterTestDriver.laxCompareEvents(expectedEvents, 
					XmlStreamTestUtils.getEvents(snippet, filter, parameters)));
		}
		finally {
			parameters = originalParameters;
		}
	}

	private void addStartEvents(ArrayList<Event> events) {		
		events.add(new Event(EventType.START_DOCUMENT, new StartDocument("sd1")));
	}

	private void addEndEvents(ArrayList<Event> events) {
		events.add(new Event(EventType.END_DOCUMENT, new Ending("ed2")));
	}
}
