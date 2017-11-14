package net.sf.okapi.filters.xmlstream.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
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
import net.sf.okapi.filters.xmlstream.XmlStreamFilter;

@RunWith(JUnit4.class)
public class PropertyXmlExtractionComparisionTest {

	private XmlStreamFilter xmlStreamFilter;
	private String[] propertyXmlFileList;
	// private String propertyXmlRoot;
	private LocaleId locEN = LocaleId.fromString("en");
	private final FileLocation root = FileLocation.fromClass(getClass());

	@Before
	public void setUp() throws Exception {
		xmlStreamFilter = new XmlStreamFilter();
		xmlStreamFilter.setParametersFromURL(XmlStreamFilter.class
				.getResource("javaPropertiesXml.yml"));

		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.html.HtmlFilter");
		xmlStreamFilter.setFilterConfigurationMapper(fcMapper);

		propertyXmlFileList = XmlStreamTestUtils.getTestFiles("/", ".xml");

		// Path propertyXmlUrl = root.in("/about.xml").asPath();
		// propertyXmlRoot = Util.getDirectoryName(propertyXmlUrl.toURI().getPath()) + File.separator;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStartDocument() {
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(xmlStreamFilter,
				new InputDocument(root.in("/about.xml").toString(), null), "UTF-8", locEN, locEN));
	}

	@Test
	public void testOpenTwice() throws URISyntaxException {
		File file = root.in("/about.xml").asFile();
		RawDocument rawDoc = new RawDocument(file.toURI(), "UTF-8", locEN);
		xmlStreamFilter.open(rawDoc);
		xmlStreamFilter.close();
		xmlStreamFilter.open(rawDoc);
		xmlStreamFilter.close();
	}

	@Test
	public void testDoubleExtractionSingle() throws URISyntaxException, MalformedURLException {
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/about.xml").toString(), null));
		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
	}

	@Test
	public void testDoubleExtraction() throws URISyntaxException {
		RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		for (String f : propertyXmlFileList) {
			list.add(new InputDocument(root.in("/" + f).toString(), null));
		}
		assertTrue(rtc.executeCompare(xmlStreamFilter, list, "UTF-8", locEN, locEN));
	}

	@Test
	public void testAsSnippetWithCdata() throws URISyntaxException, IOException {
		InputStream s = root.in("/simple_cdata.xml").asInputStream();
		String snippet = XmlStreamTestUtils.convertStreamToString(s).trim();
		assertEquals(snippet, XmlStreamTestUtils.generateOutput(
				XmlStreamTestUtils.getEvents(snippet, xmlStreamFilter,
						XmlStreamFilter.class.getResource("javaPropertiesXml.yml")), snippet, locEN, xmlStreamFilter));
	}
	
	@Test
	public void testAsSnippetNoCdata() throws URISyntaxException, IOException {
		InputStream s = root.in("/test_drive.xml").asInputStream();
		String snippet = XmlStreamTestUtils.convertStreamToString(s).trim();
		assertEquals(snippet, XmlStreamTestUtils.generateOutput(
				XmlStreamTestUtils.getEvents(snippet, xmlStreamFilter,
						XmlStreamFilter.class.getResource("javaPropertiesXml.yml")), snippet, locEN, xmlStreamFilter));
	}

	@Test @Ignore
	public void testReconstructFile() {
		GenericSkeletonWriter writer = new GenericSkeletonWriter();
		StringBuilder tmp = new StringBuilder();

		// Open the document to process
		xmlStreamFilter.open(new RawDocument(root.in("/about.xml").asUri(),
				"UTF-8", new LocaleId("en")));

		// process the input document
		while (xmlStreamFilter.hasNext()) {
			Event event = xmlStreamFilter.next();
			switch (event.getEventType()) {
			case START_DOCUMENT:
				writer.processStartDocument(LocaleId.SPANISH, "utf-8", null,
						xmlStreamFilter.getEncoderManager(), (StartDocument) event.getResource());
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
