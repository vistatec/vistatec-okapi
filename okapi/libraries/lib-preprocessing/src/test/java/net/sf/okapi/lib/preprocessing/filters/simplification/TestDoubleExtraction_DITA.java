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
public class TestDoubleExtraction_DITA {

	private static final LocaleId ENUS = new LocaleId("en", "us");
	
	@Test
	public void testDoubleExtraction_DITA() throws URISyntaxException, IOException {
		SimplificationFilter filter = new SimplificationFilter();
		
		Parameters params =	filter.getParameters();
		params.setFilterConfigId("okf_xmlstream-dita");
		params.setSimplifyResources(true);
		params.setSimplifyCodes(false);
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();				
		String pathBase = Util.getDirectoryName(this.getClass().getResource("/aa324.html").toURI().getPath()) + "/";
		
		list.add(new InputDocument(pathBase + "bookmap-readme.dita", null));
		list.add(new InputDocument(pathBase + "changingtheoil.dita", null));
		list.add(new InputDocument(pathBase + "closeprograms.dita", null));
		list.add(new InputDocument(pathBase + "configuredatabase.dita", null));
		list.add(new InputDocument(pathBase + "configurestorage.dita", null));
		list.add(new InputDocument(pathBase + "configurewebserver.dita", null));
		list.add(new InputDocument(pathBase + "configuring.dita", null));
		list.add(new InputDocument(pathBase + "databasetrouble.dita", null));
		list.add(new InputDocument(pathBase + "drivetrouble.dita", null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", ENUS, ENUS, "out"));
	}
	
	@Test
	public void testDoubleExtraction_DefaultConfig() throws URISyntaxException, IOException {
		SimplificationFilter filter = (SimplificationFilter) FilterUtil.createFilter("okf_simplification");
		Parameters params =	filter.getParameters();
		params.setFilterConfigId("okf_xmlstream-dita");
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();				
		String pathBase = Util.getDirectoryName(this.getClass().getResource("/aa324.html").toURI().getPath()) + "/";
		
		list.add(new InputDocument(pathBase + "bookmap-readme.dita", null));
		list.add(new InputDocument(pathBase + "changingtheoil.dita", null));
		list.add(new InputDocument(pathBase + "closeprograms.dita", null));
		list.add(new InputDocument(pathBase + "configuredatabase.dita", null));
		list.add(new InputDocument(pathBase + "configurestorage.dita", null));
		list.add(new InputDocument(pathBase + "configurewebserver.dita", null));
		list.add(new InputDocument(pathBase + "configuring.dita", null));
		list.add(new InputDocument(pathBase + "databasetrouble.dita", null));
		list.add(new InputDocument(pathBase + "drivetrouble.dita", null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", ENUS, ENUS, "out"));
	}
	
	@Test
	public void testDoubleExtraction_ResourcesConfig() throws URISyntaxException, IOException {
		SimplificationFilter filter = (SimplificationFilter) FilterUtil.createFilter("okf_simplification-xmlResources");
		Parameters params =	filter.getParameters();
		params.setFilterConfigId("okf_xmlstream-dita");
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();				
		String pathBase = Util.getDirectoryName(this.getClass().getResource("/aa324.html").toURI().getPath()) + "/";
		
		list.add(new InputDocument(pathBase + "bookmap-readme.dita", null));
		list.add(new InputDocument(pathBase + "changingtheoil.dita", null));
		list.add(new InputDocument(pathBase + "closeprograms.dita", null));
		list.add(new InputDocument(pathBase + "configuredatabase.dita", null));
		list.add(new InputDocument(pathBase + "configurestorage.dita", null));
		list.add(new InputDocument(pathBase + "configurewebserver.dita", null));
		list.add(new InputDocument(pathBase + "configuring.dita", null));
		list.add(new InputDocument(pathBase + "databasetrouble.dita", null));
		list.add(new InputDocument(pathBase + "drivetrouble.dita", null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", ENUS, ENUS, "out"));
	}
	
	@Test
	public void testDoubleExtraction_CodesConfig() throws URISyntaxException, IOException {
		SimplificationFilter filter = (SimplificationFilter) FilterUtil.createFilter("okf_simplification-xmlCodes");
		Parameters params =	filter.getParameters();
		params.setFilterConfigId("okf_xmlstream-dita");
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();				
		String pathBase = Util.getDirectoryName(this.getClass().getResource("/aa324.html").toURI().getPath()) + "/";
		
		list.add(new InputDocument(pathBase + "bookmap-readme.dita", null));
		list.add(new InputDocument(pathBase + "changingtheoil.dita", null));
		list.add(new InputDocument(pathBase + "closeprograms.dita", null));
		list.add(new InputDocument(pathBase + "configuredatabase.dita", null));
		list.add(new InputDocument(pathBase + "configurestorage.dita", null));
		list.add(new InputDocument(pathBase + "configurewebserver.dita", null));
		list.add(new InputDocument(pathBase + "configuring.dita", null));
		list.add(new InputDocument(pathBase + "databasetrouble.dita", null));
		list.add(new InputDocument(pathBase + "drivetrouble.dita", null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", ENUS, ENUS, "out"));
	}
}
