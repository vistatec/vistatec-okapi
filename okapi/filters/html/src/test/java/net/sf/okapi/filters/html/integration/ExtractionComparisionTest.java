package net.sf.okapi.filters.html.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.filters.html.HtmlUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExtractionComparisionTest {

	private HtmlFilter htmlFilter;
	private String[] testFileList;
	private LocaleId locEN = LocaleId.fromString("en");
	private FileLocation location = FileLocation.fromClass(ExtractionComparisionTest.class);

	@Before
	public void setUp() throws Exception {
		htmlFilter = new HtmlFilter();
		testFileList = HtmlUtils.getHtmlTestFiles();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStartDocument() {
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(htmlFilter,
				new InputDocument(location.in("/324.html").toString(), null),
				"UTF-8", locEN, locEN));
	}
	

	@Test
	public void testOpenTwice () throws URISyntaxException {
		RawDocument rawDoc = new RawDocument(location.in("/324.html").asUri(), "windows-1252", locEN);
		htmlFilter.open(rawDoc);
		htmlFilter.close();
		htmlFilter.open(rawDoc);
		htmlFilter.close();
	}
	
	@Test
	public void testDoubleExtractionSingle() throws URISyntaxException, MalformedURLException {
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		//list.add(new InputDocument(root + "Carnation Chiropractic Center Inc.htm", null));
		list.add(new InputDocument(location.in("/test.html").toString(), null));
		assertTrue(rtc.executeCompare(htmlFilter, list, "UTF-8", locEN, locEN));
	}
	
	@Test
	public void testDoubleExtraction() throws URISyntaxException {
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		for (String f : testFileList) {
			list.add(new InputDocument(location.in("/" + f).toString(), null));
		}
		assertTrue(rtc.executeCompare(htmlFilter, list, "UTF-8", locEN, locEN));
	}
	
	@Test
	public void testDoubleExtraction2() throws URISyntaxException {
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(location.in("/home_big.html").toString(), null));
		assertTrue(rtc.executeCompare(htmlFilter, list, "UTF-8", locEN, locEN));
	}
	
	//@Test
	public void disabled_testReconstructFile() {
		GenericSkeletonWriter writer = new GenericSkeletonWriter();
		StringBuilder tmp = new StringBuilder();

		// Open the document to process
		htmlFilter.open(new RawDocument(location.in("/324.html").asUri(),
				"UTF-8", new LocaleId("en")));

		// process the input document
		while (htmlFilter.hasNext()) {
			Event event = htmlFilter.next();
			switch (event.getEventType()) {
			case START_DOCUMENT:
				writer.processStartDocument(LocaleId.SPANISH, "utf-8", null,
						htmlFilter.getEncoderManager(), (StartDocument) event.getResource());
				break;
			case TEXT_UNIT:
				ITextUnit tu = event.getTextUnit();
				tmp.append(writer.processTextUnit(tu));
				break;
			case DOCUMENT_PART:
				DocumentPart dp = (DocumentPart) event.getResource();
				tmp.append(writer.processDocumentPart(dp));
				break;
			case START_GROUP:
			case START_SUBFILTER:
				StartGroup startGroup = (StartGroup) event.getResource();
				tmp.append(writer.processStartGroup(startGroup));
				break;
			case END_GROUP:
			case END_SUBFILTER:
				Ending ending = (Ending) event.getResource();
				tmp.append(writer.processEndGroup(ending));
				break;
			default:
				break;
			}
		}
		writer.close();
	}
}
