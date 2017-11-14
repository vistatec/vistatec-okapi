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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.plaintext.paragraphs.ParaPlainTextFilter;
import net.sf.okapi.filters.plaintext.paragraphs.Parameters;
import net.sf.okapi.lib.extra.filters.AbstractLineFilter;
import net.sf.okapi.lib.extra.filters.WrapMode;

@RunWith(JUnit4.class)
public class ParaPlainTextFilterTest {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final FileLocation location = FileLocation.fromClass(this.getClass());
	
	private ParaPlainTextFilter filter;
	private FilterTestDriver testDriver;
	private Parameters params;
    private LocaleId locEN = LocaleId.fromString("en"); 
    private LocaleId locFR = LocaleId.fromString("fr"); 
//  private String root;
    
	
	@Before
	public void setUp() {
		filter = new ParaPlainTextFilter();
		assertNotNull(filter);

		params = (Parameters) filter.getParameters();
		assertNotNull(params);
		
		testDriver = new FilterTestDriver();
		assertNotNull(testDriver);
		
		testDriver.setDisplayLevel(0);
		testDriver.setShowSkeleton(true);
//        root = TestUtil.getParentDir(this.getClass(), "/crt.txt");
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
	
		// Empty filter parameters, OkapiBadFilterParametersException 
		try {
			filter.setParameters(null);
			InputStream input2 = location.in("/cr.txt").asInputStream();
			filter.open(new RawDocument(input2, "UTF-8", locEN));
			fail("OkapiBadFilterParametersException should've been trown");
		}	
		catch (OkapiBadFilterParametersException e) {
		}
		finally {
			filter.close();
		}		
	}		
		
	@Test
	public void testNameAndMimeType() {
		assertEquals(filter.getMimeType(), "text/plain");
		assertEquals(filter.getName(), "okf_plaintext_paragraphs");
		
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
	public void testFiles() {
		
		params.extractParagraphs = false;
		params.preserveWS = false;
		
		testFile("BOM_MacUTF16withBOM2.txt", false);		
		testFile("cr.txt", false);
		testFile("crlf_start.txt", true);
		testFile("crlf_end.txt", true);
		testFile("crlf.txt", false);
		testFile("crlfcrlf_end.txt", true);
		testFile("crlfcrlf.txt", false);
		testFile("lf.txt", false);
	}
				
	@Test
	public void testFiles2() {
		
		params.extractParagraphs = true;
//		params.reset();
		
		testFile("crlfcrlf_end.txt", true);
	}
	
	@Test
	public void testSkeleton () {
		String st = null;
		String expected = null;
		params.extractParagraphs = false;
		
		try {
			st = getSkeleton(getFullFileName("crlf_start.txt")); // Trailing linebreak
		} 
		catch (UnsupportedEncodingException e) {
		}	
		LOGGER.trace(String.format("Skeleton of %s\n---\n", "crlf_start.txt") + st + "\n----------");
		
		try {
			expected = streamAsString(location.in("/crlf_start.txt").asInputStream());
		} 
		catch (IOException e) {
		}
		assertEquals(expected, st);
	}
	
	@Test
	public void testSkeleton2 () {
		String st = null;
		String expected = null;
		
		try {
			st = getSkeleton(getFullFileName("csv_test1.txt")); // No trailing linebreak
		} 
		catch (UnsupportedEncodingException e) {
		}	
		LOGGER.trace(String.format("Skeleton of %s\n---\n", "csv_test1.txt") + st + "\n----------");
		
		try {
			expected = streamAsString(location.in("/csv_test1.txt").asInputStream());
		} 
		catch (IOException e) {
		}
		assertEquals(expected, st);
	}
	
	@Test
	public void testSkeleton3 () {
		String st = null;
		String expected = null;
		
		try {
			st = getSkeleton(getFullFileName("csv_test2.txt")); // No trailing linebreak
		} 
		catch (UnsupportedEncodingException e) {
		}	
		LOGGER.trace(String.format("Skeleton of %s\n---\n", "csv_test2.txt") + st + "\n----------");
		
		try {
			expected = streamAsString(location.in("/csv_test2.txt").asInputStream());
		} 
		catch (IOException e) {
		}
		assertEquals(expected, st);
	}
	
	@Test
	public void testSkeleton4 () {
		String st = null;
		String expected = null;
		
		try {
			st = getSkeleton(getFullFileName("crlfcrlf.txt")); // No trailing linebreak
		} 
		catch (UnsupportedEncodingException e) {
		}	
		LOGGER.trace(String.format("Skeleton of %s\n---\n", "crlfcrlf.txt") + st + "\n----------");
		
		try {
			expected = streamAsString(location.in("/crlfcrlf.txt").asInputStream());
		} 
		catch (IOException e) {
		}
		assertEquals(expected, st);
	}
	
	@Test
	public void testSkeleton5 () {
		String st = null;
		String expected = null;
		
		try {
			st = getSkeleton(getFullFileName("al2.txt")); // No trailing linebreak
		} 
		catch (UnsupportedEncodingException e) {
		}	
		LOGGER.trace(String.format("Skeleton of %s\n---\n", "al2.txt") + st + "\n----------");
		
		try {
			expected = streamAsString(location.in("/al2.txt").asInputStream());
		} 
		catch (IOException e) {
		}
		assertEquals(expected, st);
	}
	
	@Test
	public void testEvents() {
		String filename = "cr.txt";
		
		testDriver.setDisplayLevel(0);
		testDriver.setShowSkeleton(true);
		
		InputStream input = location.in("/" + filename).asInputStream();
		assertNotNull(input);
		
		LOGGER.trace(filename);
		filter.open(new RawDocument(input, "UTF-8", locEN));
		if (!testDriver.process(filter)) Assert.fail();
		filter.close();
	}
	
/*  TODO: fix these
	@Test
	public void testDoubleExtraction () {
		// Read all files in the data directory
		params.extractParagraphs = true;
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		
		list.add(new InputDocument(root + "cr.txt", ""));
		list.add(new InputDocument(root + "csv_test1.txt", ""));
		list.add(new InputDocument(root + "crlf_start.txt", ""));
		list.add(new InputDocument(root + "crlf_end.txt", ""));
		list.add(new InputDocument(root + "crlf.txt", ""));		
		list.add(new InputDocument(root + "lf.txt", "")); 
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, "fr"));
	}

	@Test
	public void testDoubleExtraction2() {
		// Read all files in the data directory
		params.extractParagraphs = true;
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		
		list.add(new InputDocument(root + "crlfcrlf.txt", ""));
		list.add(new InputDocument(root + "crlfcrlf_end.txt", ""));
		list.add(new InputDocument(root + "crlfcrlfcrlf.txt", ""));
		list.add(new InputDocument(root + "crlfcrlfcrlf_end.txt", ""));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, "fr"));
	}
	
	@Test
	public void testDoubleExtraction3() {
		// Read all files in the data directory
		params.extractParagraphs = true;
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		
		list.add(new InputDocument(root + "lgpl.txt", ""));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, "fr"));
	}
*/

	@Test
	public void testCancel() {
		testDriver.setDisplayLevel(0);
		params.extractParagraphs = false;
		
		InputStream input = location.in("/cr.txt").asInputStream();
		assertNotNull(input);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		testEvent(EventType.TEXT_UNIT, "Line 1");
		testEvent(EventType.TEXT_UNIT, "Line 2");
		filter.cancel();
		testEvent(EventType.CANCELED, null);
		assertFalse(filter.hasNext());
		
		filter.close();		
	}
	
	@Test
	public void testLineNumbers() {
		
		InputStream input = location.in("/test_paragraphs1.txt").asInputStream();
		assertNotNull(input);
		
		Parameters params = (Parameters) filter.getParameters();
		params.extractParagraphs = false;
		params.wrapMode = WrapMode.NONE;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);
		testEvent(EventType.TEXT_UNIT, "Line 1", 1);
		testEvent(EventType.TEXT_UNIT, "Line 2", 2);
		testEvent(EventType.TEXT_UNIT, "Line 3", 4);
		testEvent(EventType.TEXT_UNIT, "Line 4", 5);
		testEvent(EventType.TEXT_UNIT, "Line 5", 6);
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();		
		
		input = location.in("/test_paragraphs1.txt").asInputStream();
		assertNotNull(input);
		
		params.extractParagraphs = true;
		params.wrapMode = WrapMode.NONE;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);
		testEvent(EventType.TEXT_UNIT, "Line 1\nLine 2", 1);
		testEvent(EventType.TEXT_UNIT, "Line 3\nLine 4\nLine 5", 4);
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testParagraphs() {
		
		InputStream input = location.in("/test_paragraphs1.txt").asInputStream();
		assertNotNull(input);
		
		Parameters params = (Parameters) filter.getParameters();
		params.extractParagraphs = false;
		params.wrapMode = WrapMode.NONE;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);
		testEvent(EventType.TEXT_UNIT, "Line 1");
		testEvent(EventType.TEXT_UNIT, "Line 2");
		testEvent(EventType.TEXT_UNIT, "Line 3");
		testEvent(EventType.TEXT_UNIT, "Line 4");
		testEvent(EventType.TEXT_UNIT, "Line 5");
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		input = location.in("/test_paragraphs1.txt").asInputStream();
		assertNotNull(input);
		
		params.extractParagraphs = false;
		params.wrapMode = WrapMode.PLACEHOLDERS;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);
		testEvent(EventType.TEXT_UNIT, "Line 1");
		testEvent(EventType.TEXT_UNIT, "Line 2");
		testEvent(EventType.TEXT_UNIT, "Line 3");
		testEvent(EventType.TEXT_UNIT, "Line 4");
		testEvent(EventType.TEXT_UNIT, "Line 5");
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		input = location.in("/test_paragraphs1.txt").asInputStream();
		assertNotNull(input);
		
		params.extractParagraphs = false;
		params.wrapMode = WrapMode.SPACES;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);
		testEvent(EventType.TEXT_UNIT, "Line 1");
		testEvent(EventType.TEXT_UNIT, "Line 2");
		testEvent(EventType.TEXT_UNIT, "Line 3");
		testEvent(EventType.TEXT_UNIT, "Line 4");
		testEvent(EventType.TEXT_UNIT, "Line 5");
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
				
		//---------------------------------------------------------------------------
		input = location.in("/test_paragraphs1.txt").asInputStream();
		assertNotNull(input);
		
		params.extractParagraphs = true;
		params.wrapMode = WrapMode.NONE;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);
		testEvent(EventType.TEXT_UNIT, "Line 1\nLine 2");
		testEvent(EventType.TEXT_UNIT, "Line 3\nLine 4\nLine 5");
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		input = location.in("/test_paragraphs1.txt").asInputStream();
		assertNotNull(input);
		
		params.extractParagraphs = true;
		params.wrapMode = WrapMode.SPACES;		
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);
		testEvent(EventType.TEXT_UNIT, "Line 1 Line 2");
		testEvent(EventType.TEXT_UNIT, "Line 3 Line 4 Line 5");
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		input = location.in("/test_paragraphs1.txt").asInputStream();
		assertNotNull(input);
		
		params.extractParagraphs = true;
		params.wrapMode = WrapMode.PLACEHOLDERS;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);
		testEvent(EventType.TEXT_UNIT, "Line 1\rLine 2");
		testEvent(EventType.TEXT_UNIT, "Line 3\rLine 4\rLine 5");
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
				
// Helpers
	
	private void testFile(String filename, boolean emptyTail) {
		testDriver.setDisplayLevel(0);
		
		InputStream input = location.in("/" + filename).asInputStream();
		assertNotNull(input);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		testEvent(EventType.TEXT_UNIT, "Line 1");
		testEvent(EventType.TEXT_UNIT, "Line 2");
		testEvent(EventType.TEXT_UNIT, "Line 3");
		testEvent(EventType.TEXT_UNIT, "Line 4");
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		// List events
		input = location.in("/" + filename).asInputStream();
		assertNotNull(input);
		
		LOGGER.trace(filename);
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
			LOGGER.trace(res.toString());
			LOGGER.trace(expectedText);
			assertEquals(expectedText, res.toString());
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
	
	private void testEvent(EventType expectedType, String expectedText, int expectedLineNum) {
		assertNotNull(filter);
		
		Event event = filter.next();		
		assertNotNull(event);
		
		assertTrue(event.getEventType() == expectedType);
		
		switch (event.getEventType()) {
		case TEXT_UNIT:
			IResource res = event.getResource();
			assertTrue(res instanceof ITextUnit);
			assertEquals(expectedText, res.toString());
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
	
	private URI getFullFileName(String fileName) {
		return location.in("/" + fileName).asUri();
	}
	
	private String getSkeleton (URI fileUri) throws UnsupportedEncodingException {
		ByteArrayOutputStream writerBuffer;
										
		try (IFilterWriter writer = filter.createFilterWriter()) {
			// Open the input
			filter.open(new RawDocument(fileUri, "UTF-8", locEN, locFR));

			// Prepare the output
			writer.setOptions(locFR, "UTF-16");
			writerBuffer = new ByteArrayOutputStream();
			writer.setOutput(writerBuffer);

			// Process the document
			Event event;
			while ( filter.hasNext() ) {
				event = filter.next();
				writer.handleEvent(event);
			}
		}
		finally {
			if ( filter != null ) filter.close();
		}
		return new String(writerBuffer.toByteArray(), "UTF-16");
	}
	
	private String streamAsString(InputStream input) throws IOException {
		BufferedReader reader;
		reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

		StringBuilder tmp = new StringBuilder();
		char[] buf = new char[2048];
		int count = 0;
		while (( count = reader.read(buf)) != -1 ) {
			tmp.append(buf, 0, count);
		}
		
        return tmp.toString();
    }
			
}
	