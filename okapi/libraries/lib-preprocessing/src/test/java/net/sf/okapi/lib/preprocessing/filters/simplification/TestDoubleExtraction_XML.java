package net.sf.okapi.lib.preprocessing.filters.simplification;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestDoubleExtraction_XML {

	private static final LocaleId ENUS = new LocaleId("en", "us");
	
	@Test
	public void testDoubleExtraction_XML() throws URISyntaxException, IOException {
		SimplificationFilter filter = new SimplificationFilter();
		
		Parameters params =	filter.getParameters();
		params.setSimplifyResources(true);
		params.setSimplifyCodes(false);
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();				
		String pathBase = Util.getDirectoryName(this.getClass().getResource("/aa324.html").toURI().getPath()) + "/";
		
		list.add(new InputDocument(pathBase + "about.xml", null));
		list.add(new InputDocument(pathBase + "failure.xml", null));
		list.add(new InputDocument(pathBase + "PI-Problem.xml", null));
		list.add(new InputDocument(pathBase + "simple_cdata.xml", null));
		list.add(new InputDocument(pathBase + "subfilter-simple.xml", null));
		list.add(new InputDocument(pathBase + "success.xml", null));
		list.add(new InputDocument(pathBase + "test_drive.xml", null));
		list.add(new InputDocument(pathBase + "test_href_reference.xml", null));
		list.add(new InputDocument(pathBase + "translate-attr-subfilter.xml", null));
		list.add(new InputDocument(pathBase + "xml-freemarker.xml", null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", ENUS, ENUS, "out"));
	}
	
	@Test
	public void testDoubleExtraction_DefaultConfig() throws URISyntaxException, IOException {
		IFilter filter = (SimplificationFilter) FilterUtil.createFilter("okf_simplification");
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();				
		String pathBase = Util.getDirectoryName(this.getClass().getResource("/aa324.html").toURI().getPath()) + "/";
		
		list.add(new InputDocument(pathBase + "about.xml", null));
		list.add(new InputDocument(pathBase + "failure.xml", null));
		list.add(new InputDocument(pathBase + "PI-Problem.xml", null));
		list.add(new InputDocument(pathBase + "simple_cdata.xml", null));
		list.add(new InputDocument(pathBase + "subfilter-simple.xml", null));
		list.add(new InputDocument(pathBase + "success.xml", null));
		list.add(new InputDocument(pathBase + "test_drive.xml", null));
		list.add(new InputDocument(pathBase + "test_href_reference.xml", null));
		list.add(new InputDocument(pathBase + "translate-attr-subfilter.xml", null));
		list.add(new InputDocument(pathBase + "xml-freemarker.xml", null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", ENUS, ENUS, "out"));
	}
	
	@Test
	public void testDoubleExtraction_ResourcesConfig() throws URISyntaxException, IOException {
		IFilter filter = (SimplificationFilter) FilterUtil.createFilter("okf_simplification-xmlResources");
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();				
		String pathBase = Util.getDirectoryName(this.getClass().getResource("/aa324.html").toURI().getPath()) + "/";
		
		list.add(new InputDocument(pathBase + "about.xml", null));
		list.add(new InputDocument(pathBase + "failure.xml", null));
		list.add(new InputDocument(pathBase + "PI-Problem.xml", null));
		list.add(new InputDocument(pathBase + "simple_cdata.xml", null));
		list.add(new InputDocument(pathBase + "subfilter-simple.xml", null));
		list.add(new InputDocument(pathBase + "success.xml", null));
		list.add(new InputDocument(pathBase + "test_drive.xml", null));
		list.add(new InputDocument(pathBase + "test_href_reference.xml", null));
		list.add(new InputDocument(pathBase + "translate-attr-subfilter.xml", null));
		list.add(new InputDocument(pathBase + "xml-freemarker.xml", null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", ENUS, ENUS, "out"));
	}
	
	@Test
	public void testDoubleExtraction_CodesConfig() throws URISyntaxException, IOException {
		IFilter filter = (SimplificationFilter) FilterUtil.createFilter("okf_simplification-xmlCodes");
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();				
		String pathBase = Util.getDirectoryName(this.getClass().getResource("/aa324.html").toURI().getPath()) + "/";
		
		list.add(new InputDocument(pathBase + "about.xml", null));
		list.add(new InputDocument(pathBase + "failure.xml", null));
		list.add(new InputDocument(pathBase + "PI-Problem.xml", null));
		list.add(new InputDocument(pathBase + "simple_cdata.xml", null));
		list.add(new InputDocument(pathBase + "subfilter-simple.xml", null));
		list.add(new InputDocument(pathBase + "success.xml", null));
		list.add(new InputDocument(pathBase + "test_drive.xml", null));
		list.add(new InputDocument(pathBase + "test_href_reference.xml", null));
		list.add(new InputDocument(pathBase + "translate-attr-subfilter.xml", null));
		list.add(new InputDocument(pathBase + "xml-freemarker.xml", null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", ENUS, ENUS, "out"));
	}
}
