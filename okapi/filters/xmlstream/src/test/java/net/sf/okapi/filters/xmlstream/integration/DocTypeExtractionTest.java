package net.sf.okapi.filters.xmlstream.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.filters.xmlstream.XmlStreamFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DocTypeExtractionTest {

	private XmlStreamFilter xmlStreamFilter;
	private LocaleId locEN = LocaleId.ENGLISH;
	private final FileLocation root = FileLocation.fromClass(getClass());

	@Before
	public void setUp() throws Exception {
		xmlStreamFilter = new XmlStreamFilter();
	}

	@Test
	public void testDoubleExtraction() throws URISyntaxException,
			MalformedURLException {
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/doctype.xml").toString(), null));
		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN,
				locEN));
	}

	@Test
	public void testEvents() throws URISyntaxException,
			MalformedURLException {
		String snippet = "<!DOCTYPE foo [<!ELEMENT foo (content*)>]>" +
								"<foo>" +
								"<content id=\"1\">content1</content>" +
								"</foo>";
		
		ArrayList<Event> events = FilterTestDriver.getEvents(xmlStreamFilter, snippet, LocaleId.ENGLISH);		
		String docType = events.get(1).getDocumentPart().toString();
		assertEquals("<!DOCTYPE foo [<!ELEMENT foo (content*)>]><foo>", docType);
	}
}