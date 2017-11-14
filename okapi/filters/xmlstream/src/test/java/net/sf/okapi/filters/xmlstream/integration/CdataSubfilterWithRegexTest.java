package net.sf.okapi.filters.xmlstream.integration;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.filters.xmlstream.XmlStreamFilter;

@RunWith(JUnit4.class)
public class CdataSubfilterWithRegexTest {

	private XmlStreamFilter xmlStreamFilter;	
	private LocaleId locEN = LocaleId.ENGLISH;
	private FilterConfigurationMapper fcMapper;
	private final FileLocation root = FileLocation.fromClass(getClass());
	
	@Before
	public void setUp() throws Exception {		       
		xmlStreamFilter = new XmlStreamFilter();			
		fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations(net.sf.okapi.filters.html.HtmlFilter.class.getName());
		fcMapper.addConfigurations(net.sf.okapi.filters.xmlstream.XmlStreamFilter.class.getName());
		fcMapper.setCustomConfigurationsDirectory(root.in("/").toString());
        fcMapper.addCustomConfiguration("okf_html@spaces_freemarker_regex");
        fcMapper.addCustomConfiguration("okf_html@spaces_freemarker_no_regex");
        fcMapper.updateCustomConfigurations();
        xmlStreamFilter.setFilterConfigurationMapper(fcMapper);
	}
	
	@Test
	public void testDoubleExtractionWithRegex() throws URISyntaxException, MalformedURLException {
		xmlStreamFilter.setParametersFromURL(root.in("/okf_xmlstream@freemarker.fprm").asUrl());
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/xml-freemarker.xml").toString(), null));
		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
	}
	
	@Test
	public void testDoubleExtractionWithoutRegex() throws URISyntaxException, MalformedURLException {
		xmlStreamFilter.setParametersFromURL(root.in("/okf_xmlstream@freemarker_no_regex.fprm").asUrl());
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/xml-freemarker.xml").toString(), null));
		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
	}

	@Test
	public void testDoubleExtractionWithoutSubfilter() throws URISyntaxException, MalformedURLException {
		xmlStreamFilter.setParametersFromURL(root.in("/okf_html@spaces_freemarker_regex.fprm").asUrl());
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/freemarker.html").toString(), null));
		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
	}
}
