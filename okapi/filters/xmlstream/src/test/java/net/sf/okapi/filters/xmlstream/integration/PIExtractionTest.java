package net.sf.okapi.filters.xmlstream.integration;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.filters.xmlstream.XmlStreamFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PIExtractionTest {

	private XmlStreamFilter xmlStreamFilter;	
	private final FileLocation root = FileLocation.fromClass(getClass());
	private LocaleId locEN = LocaleId.ENGLISH;
	
	@Before
	public void setUp() throws Exception {
		xmlStreamFilter = new XmlStreamFilter();	
		xmlStreamFilter.setParametersFromURL(XmlStreamFilter.class.getResource("dita.yml"));
	}
	
	@Test
	public void testDoubleExtraction() throws URISyntaxException, MalformedURLException {
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/PI-Problem.xml").toString(), null));
		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
	}
	
	@Test
	public void testDoubleExtraction2() throws URISyntaxException, MalformedURLException {
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/PI-Problem2.dita").toString(), null));
		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
	}
}