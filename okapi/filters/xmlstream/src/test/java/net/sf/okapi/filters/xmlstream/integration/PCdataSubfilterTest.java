package net.sf.okapi.filters.xmlstream.integration;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.filters.xmlstream.XmlStreamFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PCdataSubfilterTest {

	private XmlStreamFilter xmlStreamFilter;	
	private final FileLocation root = FileLocation.fromClass(getClass());
	private LocaleId locEN = LocaleId.ENGLISH;
	private FilterConfigurationMapper fcMapper;
	
	@Before
	public void setUp() throws Exception {		       
		xmlStreamFilter = new XmlStreamFilter();				
		fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.html.HtmlFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.xmlstream.XmlStreamFilter");
		fcMapper.setCustomConfigurationsDirectory(root.in("/").toString());
        fcMapper.addCustomConfiguration("okf_xmlstream@pcdata_subfilter");
        fcMapper.updateCustomConfigurations();
        xmlStreamFilter.setFilterConfigurationMapper(fcMapper);
	}
		
	@Test
	public void testPcdataWithoutEscapes() throws URISyntaxException, MalformedURLException {
		xmlStreamFilter.setParametersFromURL(root.in("/okf_xmlstream@pcdata_subfilter.fprm").asUrl());
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/failure.xml").toString(), null));
		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
	}
	
	@Test
	public void testPcdataWithEscapes() throws URISyntaxException, MalformedURLException {
		xmlStreamFilter.setParametersFromURL(root.in("/okf_xmlstream@pcdata_subfilter.fprm").asUrl());
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/success.xml").toString(), null));
		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
	}
	
	@Test
	public void testPcdataHrefReference() throws URISyntaxException, MalformedURLException {
		xmlStreamFilter.setParametersFromURL(root.in("/okf_xmlstream@pcdata_subfilter.fprm").asUrl());
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/test_href_reference.xml").toString(), null));
		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
	}
}