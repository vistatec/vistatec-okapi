package net.sf.okapi.filters.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class HtmlFullFileTest {

	private HtmlFilter htmlFilter;
	private String[] testFileList;
	private LocaleId locEN = LocaleId.fromString("en");
	private FileLocation location = FileLocation.fromClass(HtmlFullFileTest.class);

	@Before
	public void setUp() throws Exception {
		htmlFilter = new HtmlFilter();
		testFileList = HtmlUtils.getHtmlTestFiles();
	}

	@After
	public void tearDown() {
		htmlFilter.close();
	}

	@Test
	public void testAllExternalFiles() throws URISyntaxException {
		@SuppressWarnings("unused")
		Event event = null;

		for (String f : testFileList) {
			InputStream htmlStream = location.in("/" + f).asInputStream();
			htmlFilter.open(new RawDocument(htmlStream, "UTF-8", locEN));
			while (htmlFilter.hasNext()) {
				event = htmlFilter.next();
			}
		}
	}

	@Test
	public void testNonwellformed() {
		InputStream htmlStream = location.in("/nonwellformed.specialtest").asInputStream();
		htmlFilter.open(new RawDocument(htmlStream, "UTF-8", locEN));
		while (htmlFilter.hasNext()) {
			@SuppressWarnings("unused")
			Event event = htmlFilter.next();
		}
	}

	@Test
	public void testEncodingShouldBeFound() {
		InputStream htmlStream = location.in("/withEncoding.html").asInputStream();
		htmlFilter.open(new RawDocument(htmlStream, "UTF-8", locEN));
		assertEquals("windows-1252", htmlFilter.getEncoding());
	}

	@Test
	public void testEncodingShouldBeFound2() {
		InputStream htmlStream = location.in("/W3CHTMHLTest1.html").asInputStream();
		htmlFilter.open(new RawDocument(htmlStream, "UTF-8", locEN));
		assertEquals("UTF-8", htmlFilter.getEncoding());
	}

	@Test
	public void testOkapiIntro() {
		InputStream htmlStream = location.in("/okapi_intro_test.html").asInputStream();
		htmlFilter.open(new RawDocument(htmlStream, "windows-1252", locEN));
		boolean foundText = false;
		boolean first = true;
		String lastText = "";
		String firstText = "";
		while (htmlFilter.hasNext()) {
			Event event = htmlFilter.next();
			if (event.getEventType() == EventType.TEXT_UNIT) {
				ITextUnit tu = event.getTextUnit();
				if (first) {
					first = false;
					firstText = tu.getSource().toString();
				}
				foundText = true;
				lastText = tu.getSource().toString();
			}
		}
		assertTrue(foundText);
		assertEquals("Okapi Framework", firstText);
		assertEquals("\u00A0", lastText);
	}

	@Test
	public void testSkippedScriptandStyleElements() {
		InputStream htmlStream = location.in("/testStyleScriptStylesheet.html").asInputStream();
		htmlFilter.open(new RawDocument(htmlStream, "UTF-8", locEN));
		boolean foundText = false;
		boolean first = true;
		String firstText = "";
		while (htmlFilter.hasNext()) {
			Event event = htmlFilter.next();
			if (event.getEventType() == EventType.TEXT_UNIT) {
				ITextUnit tu = event.getTextUnit();
				if (first) {
					first = false;
					firstText = tu.getSource().toString();
				}
				foundText = true;
			}
		}
		assertTrue(foundText);
		assertEquals("First Text", firstText);
	}

	@Test
	public void testOpenTwiceWithString() {
		RawDocument rawDoc = new RawDocument("<b>bolded html</b>", locEN);
		htmlFilter.open(rawDoc);
		htmlFilter.close();
		htmlFilter.open(rawDoc);
		htmlFilter.close();
	}
	
	@Test
	public void testOpenTwiceWithURI() throws URISyntaxException {
		URL url = location.in("/okapi_intro_test.html").asUrl();
		RawDocument rawDoc = new RawDocument(url.toURI(), "windows-1252", locEN);
		htmlFilter.open(rawDoc);
		htmlFilter.close();
		htmlFilter.open(rawDoc);
		htmlFilter.close();
	}

	@Test(expected=NullPointerException.class)
	public void testOpenTwiceWithStream() throws URISyntaxException {
		InputStream htmlStream = location.in("/okapi_intro_test.html").asInputStream();
		RawDocument rawDoc = new RawDocument(htmlStream, "windows-1252", locEN);
		htmlFilter.open(rawDoc);
		htmlFilter.close();
		htmlFilter.open(rawDoc);
		htmlFilter.close();
	}
}
