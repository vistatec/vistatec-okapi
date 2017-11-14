/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.plaintext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
// import net.sf.okapi.filters.regex.Parameters;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.plaintext.regex.Parameters;
import net.sf.okapi.filters.plaintext.regex.RegexPlainTextFilter;
import net.sf.okapi.lib.extra.filters.AbstractLineFilter;

@RunWith(JUnit4.class)
public class RegexPlainTextFilterTest {
	
	private RegexPlainTextFilter filter;
	private FilterTestDriver testDriver;
    private FileLocation location;
    private LocaleId locEN = LocaleId.fromString("en"); 
    private LocaleId locFR = LocaleId.fromString("fr"); 

	@Before
	public void setUp() {
		filter = new RegexPlainTextFilter();
		assertNotNull(filter);
		
		testDriver = new FilterTestDriver();
		assertNotNull(testDriver);
		
		testDriver.setDisplayLevel(0);
		testDriver.setShowSkeleton(true);
		location = FileLocation.fromClass(this.getClass());
	}

	@Test
	public void testEmptyInput() {
		// Empty input, check exceptions
				
		// Empty stream, OkapiBadFilterInputException expected, no other		
		InputStream input = null;
		try {
			filter.open(new RawDocument(input, "UTF-8", locEN));
			fail("IllegalArgumentException should've been trown");
		}	
		catch (IllegalArgumentException e) {
		}
		finally {
			filter.close();
		}
				
		// Empty URI, OkapiBadFilterInputException expected, no other
		URI uri = null;
		try {
			filter.open(new RawDocument(uri, "UTF-8", locEN));
			fail("IllegalArgumentException should've been trown");
		}	
		catch (IllegalArgumentException e) {
		}
		finally {
			filter.close();
		}
		
		// Empty char seq, OkapiBadFilterInputException expected, no other		
		String st = null;
		try {
			filter.open(new RawDocument(st, locEN, locEN));
			fail("IllegalArgumentException should've been trown");
		}	
		catch (IllegalArgumentException e) {
		}
		finally {
			filter.close();
		}
		
		// Empty raw doc, open(RawDocument), OkapiBadFilterInputException expected, no other		
		try {
			filter.open(null);
			fail("OkapiBadFilterInputException should've been trown");
		}	
		catch (OkapiBadFilterInputException e) {
		}
		finally {
			filter.close();
		}
	
		// Empty raw doc, open(RawDocument, boolean), OkapiBadFilterInputException expected, no other
		try {
			filter.open(null, true);
			fail("OkapiBadFilterInputException should've been trown");
		}	
		catch (OkapiBadFilterInputException e) {
		}
		finally {
			filter.close();
		}
	
		// Empty filter parameters, no exception expected
		try {
			filter.setParameters(null);
			
			InputStream input2 = location.in("/cr.txt").asInputStream();
			filter.open(new RawDocument(input2, "UTF-8", locEN));
		}	
		finally {
			filter.close();
		}		
	}		
	
	@Test
	public void testParameters() throws URISyntaxException {
		// Test if default regex parameters have been loaded
		IParameters rp = filter.getRegexParameters();
				
		assertNotNull(rp);
		assertTrue(rp instanceof net.sf.okapi.filters.regex.Parameters);
		
		net.sf.okapi.filters.regex.Parameters rpp = (net.sf.okapi.filters.regex.Parameters) rp;
		assertNotNull(rpp.getRules());
		assertFalse(rpp.getRules().isEmpty());
				
		// Check if defaults are set
		Parameters params = new Parameters(); 
		filter.setParameters(params);
		
		assertEquals(params.rule, net.sf.okapi.filters.plaintext.regex.Parameters.DEF_RULE);
		assertEquals(params.sourceGroup, net.sf.okapi.filters.plaintext.regex.Parameters.DEF_GROUP);
		assertEquals(params.regexOptions, net.sf.okapi.filters.plaintext.regex.Parameters.DEF_OPTIONS);
		
		// Load filter parameters from a file, check if params have changed
		URL paramsUrl = location.in("/test_params1.txt").asUrl();
		assertNotNull(paramsUrl);  
		
		params.load(paramsUrl, false);
		assertEquals(params.rule, "(.)"); 
		assertEquals(params.sourceGroup, 1);
		assertEquals(params.regexOptions, 8);
		
		// Save filter parameters to a file, load and check if params have changed
		FileLocation paramsOut = location.out("/test_params2.txt");
		assertNotNull(paramsOut);
	
		filter.setRule("(Test (rule))", 2, 0x88);
		
		// Force the creation of the output folder
		paramsOut.makeOutputDir();
		params.save(paramsOut.toString());
		
		// Test the parameters are loaded into the internal regex and compiled
		filter.open(new RawDocument("Line 1/r/nLine2 Test rule", locEN, locEN), true);
		
		testEvent(EventType.START_DOCUMENT, "");
		testEvent(EventType.DOCUMENT_PART, "Line 1/r/nLine2 ");
		testEvent(EventType.TEXT_UNIT, "rule"); 
		
		params.rule = "(a*+)";
		params.sourceGroup = 1;
		params.regexOptions = 40;
		
		params.load(paramsOut.asUrl(), false);
		
		assertEquals(params.rule, "(Test (rule))");
		assertEquals(params.sourceGroup, 2);
		assertEquals(params.regexOptions, 0x88);		
		
		// One more time to make sure that params are saved
		params.rule = "(a*+)";
		params.sourceGroup = 1;
		params.regexOptions = 40;
		
		params.save(paramsOut.toString());
		params.rule = "(Test (rule))";
		params.sourceGroup = 2;
		params.regexOptions = 0x88;
		
		params.load(paramsOut.asUrl(), false);
		
		assertEquals(params.rule, "(a*+)");
		assertEquals(params.sourceGroup, 1);
		assertEquals(params.regexOptions, 40);
	}
	
	@Test
	public void testNameAndMimeType() {
		assertEquals(filter.getMimeType(), "text/plain");
		assertEquals(filter.getName(), "okf_plaintext_regex");
		
		// Read lines from a file, check mime types 
		InputStream input = location.in("/cr.txt").asInputStream();
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		while (filter.hasNext()) {
			Event event = filter.next();
			assertNotNull(event);
			
			IResource res = event.getResource();
			assertNotNull(res);
			
			switch (event.getEventType()) {
			case TEXT_UNIT:
				assertTrue(res instanceof ITextUnit);
				assertEquals(((ITextUnit)res).getMimeType(), filter.getMimeType());
				break;
			case DOCUMENT_PART:
				assertTrue(res instanceof DocumentPart);
				assertEquals(((DocumentPart) res).getMimeType(), null);
				break;
			default:
				break;
			}
		}
		filter.close();
	}
	
	@Test
	public void testEvents() throws IOException {
		
	}
	
	@Test
	public void testFiles() {
		testFile("cr.txt", false);
		testFile("crlf_end.txt", true);
		testFile("crlf.txt", false);
		testFile("crlfcrlf_end.txt", true);
		testFile("crlfcrlf.txt", false);
		testFile("lf.txt", false);
		testFile("mixture.txt", true);
		testFile("u0085.txt", false);
		testFile("u2028.txt", false);
		testFile("u2029.txt", false);		
	}
			
	@Test
	public void testDoubleExtraction () {
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		
		list.add(new InputDocument(location.in("/cr.txt").toString(), ""));
		list.add(new InputDocument(location.in("/crlf_end.txt").toString(), ""));
		list.add(new InputDocument(location.in("/crlf.txt").toString(), ""));
		list.add(new InputDocument(location.in("/crlfcrlf_end.txt").toString(), ""));
		list.add(new InputDocument(location.in("/crlfcrlf.txt").toString(), ""));
		list.add(new InputDocument(location.in("/lf.txt").toString(), ""));
		list.add(new InputDocument(location.in("/mixture.txt").toString(), ""));
		list.add(new InputDocument(location.in("/u0085.txt").toString(), ""));
		list.add(new InputDocument(location.in("/u2028.txt").toString(), ""));
		list.add(new InputDocument(location.in("/u2029.txt").toString(), "")); 
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR));
	}
	
	
	
// Helpers
	
	private void testFile(String filename, boolean emptyTail) {
		testDriver.setDisplayLevel(0);
		
		InputStream input = location.in("/" + filename).asInputStream();
		assertNotNull(input);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		testEvent(EventType.TEXT_UNIT, "Line 1", 1);
		testEvent(EventType.DOCUMENT_PART, null);
		testEvent(EventType.TEXT_UNIT, "Line 2", 2);
		testEvent(EventType.DOCUMENT_PART, null);
		testEvent(EventType.TEXT_UNIT, "Line 3", 3);
		testEvent(EventType.DOCUMENT_PART, null);
		testEvent(EventType.TEXT_UNIT, "Line 4", 4);
		if (emptyTail) testEvent(EventType.DOCUMENT_PART, null);
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		// List events
		input = location.in("/" + filename).asInputStream();
		assertNotNull(input);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		if ( !testDriver.process(filter) ) Assert.fail();
		filter.close();
	}
	
	private void testEvent(EventType expectedType, String expectedText) {
		assertNotNull(filter);
		
		Event event = filter.next();		
		assertNotNull(event);
		
		assertTrue(event.getEventType() == expectedType);
		
		switch (event.getEventType()) {
		case TEXT_UNIT:
			IResource res = event.getResource();
			assertTrue(res instanceof ITextUnit);
			assertEquals(((ITextUnit)res).toString(), expectedText);
			break;
		case DOCUMENT_PART:
			if (expectedText == null) break;
			res = event.getResource();
			assertTrue(res instanceof DocumentPart);
			ISkeleton skel = res.getSkeleton();
			if (skel != null) {
				assertEquals(skel.toString(), expectedText);
			}
			break;
		default:
			break;
		}
	}
	
	private void testEvent(EventType expectedType, String expectedText, int expectedLineNum) {
		assertNotNull(filter);
		
		Event event = filter.next();		
		assertNotNull(event);
		
		assertTrue(event.getEventType() == expectedType);
		
		switch (event.getEventType()) {
		case TEXT_UNIT:
			IResource res = event.getResource();
			assertTrue(res instanceof ITextUnit);
			assertEquals(expectedText, ((ITextUnit)res).toString());
			Property prop = ((ITextUnit)res).getSourceProperty(AbstractLineFilter.LINE_NUMBER);
			assertNotNull(prop);
			String st = prop.getValue();
			assertEquals(expectedLineNum, Integer.parseInt(st));
			break;
		case DOCUMENT_PART:
			if (expectedText == null) break;
			res = event.getResource();
			assertTrue(res instanceof DocumentPart);
			ISkeleton skel = res.getSkeleton();
			if (skel != null) {
				assertEquals(expectedText, skel.toString());
			}
			break;
		default:
			break;
		}
	}
			
}
