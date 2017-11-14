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

package net.sf.okapi.filters.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;
import net.sf.okapi.common.filters.FilterTestDriver;
import static net.sf.okapi.common.filters.FilterTestDriver.*;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.filters.table.base.BaseTableFilter;
import net.sf.okapi.filters.table.fwc.FixedWidthColumnsFilter;
import net.sf.okapi.filters.table.fwc.Parameters;
import net.sf.okapi.lib.extra.filters.AbstractLineFilter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class FixedWidthColumnsFilterTest {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private FixedWidthColumnsFilter filter;
	private FilterTestDriver testDriver;
    private FileLocation root;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	private LocaleId locIT = LocaleId.fromString("it");
	private LocaleId locGESW = LocaleId.fromString("ge-sw");
	
	@Before
	public void setUp() {
		filter = new FixedWidthColumnsFilter();
		assertNotNull(filter);
		
		testDriver = new FilterTestDriver();
		assertNotNull(testDriver);
		
		testDriver.setDisplayLevel(0);
		testDriver.setShowSkeleton(true);
		
        root = FileLocation.fromClass(this.getClass());
        
        Parameters params = (Parameters) filter.getParameters();
        CommaSeparatedValuesFilterTest.setDefaults(params);
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
	
		// Empty filter parameters, OkapiBadFilterParametersException expected		
			filter.setParameters(null);
			
			InputStream input2 = root.in("/csv_test6.txt").asInputStream();
		try {
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
		assertEquals(filter.getMimeType(), "text/csv");
		assertEquals(filter.getName(), "okf_table_fwc");
		
		// Read lines from a file, check mime types 
		InputStream input = root.in("/csv_test6.txt").asInputStream();
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
	public void testParameters() throws URISyntaxException {
		
		// Check if PlainTextFilter params are set for inherited fields
		Parameters params = (Parameters) filter.getParameters();
								
		//assertEquals(params.columnWidths, "");
		assertEquals(params.columnStartPositions, "");
		assertEquals(params.columnEndPositions, "");
					
		// Check if defaults are set
		params = new Parameters();
		filter.setParameters(params);
		
		//params.columnWidths = "";
		params.columnStartPositions = "";
		params.columnEndPositions = "";
		
		params = getParameters();
				
		//assertEquals("", params.columnWidths);
		assertEquals("", params.columnStartPositions);
		assertEquals("", params.columnEndPositions);
		
		// Load filter parameters from a file, check if params have changed
		URL paramsUrl = root.in("/test_params3.txt").asUrl();
		assertNotNull(paramsUrl);
		params.load(paramsUrl, false);

		//assertEquals("19, 30, 21, 16, 15, 21, 20", params.columnWidths);
		assertEquals("1, 20, 50, 71, 87, 102, 123, 144", params.columnStartPositions);
		assertEquals("11, 32, 62, 83, 97, 112, 133, 151", params.columnEndPositions);

		// Save filter parameters to a file, load and check if params have changed
		paramsUrl = root.in("/test_params2.txt").asUrl();
		assertNotNull(paramsUrl);

		params.save(paramsUrl.toURI().getPath());

		// Change params before loading them
		params = (Parameters) filter.getParameters();

		//params.columnWidths = "1, 23, 30";
		params.columnStartPositions = "1, 23, 30";
		params.columnEndPositions = "10, 21, 40";

		params.load(paramsUrl, false);
		// assertEquals("19, 30, 21, 16, 15, 21, 20", params.columnWidths);
		assertEquals("1, 20, 50, 71, 87, 102, 123, 144", params.columnStartPositions);
		assertEquals("11, 32, 62, 83, 97, 112, 133, 151", params.columnEndPositions);
		
		InputStream input = root.in("/csv_test6.txt").asInputStream();
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		
		// Check if parameters type is controlled
		
		filter.setParameters(new net.sf.okapi.filters.plaintext.base.Parameters());
		input = root.in("/csv_test6.txt").asInputStream();
		try {
			filter.open(new RawDocument(input, "UTF-8", locEN));
			fail("OkapiBadFilterParametersException should've been trown");
		}
		catch (OkapiBadFilterParametersException e) {
		}
		
		filter.close();
	
		filter.setParameters(new net.sf.okapi.filters.table.fwc.Parameters());
		input = root.in("/csv_test6.txt").asInputStream();
		try {
			filter.open(new RawDocument(input, "UTF-8", locEN));
		}
		catch (OkapiBadFilterParametersException e) {
			fail("OkapiBadFilterParametersException should NOT have been trown");
		}
			filter.close();
	}
	
	@Test
	public void testListedColumns() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv_testb.txt").asInputStream();
		assertNotNull(input);
		
		params.columnNamesLineNum = 0;
		params.valuesStartLineNum = 1;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		//params.columnWidths = "19, 30, 21, 16, 15, 21, 20, 10";
		params.columnStartPositions = "1, 20, 50, 71, 87, 102, 123, 144";
		params.columnEndPositions = "11, 32, 62, 83, 97, 112, 133, 151";
		
		params.sourceColumns = "4, 6";
		params.sourceIdSuffixes = "_name, _descr";
		params.targetColumns = "     2,7   ";
		params.targetLanguages = "ge-sw, it";
		params.targetSourceRefs = "6, 4";
		params.sourceIdColumns = "1, 3";
		params.sourceIdSourceRefs = "4, 6";
		params.commentColumns = "5";
		params.commentSourceRefs = "4";
		params.recordIdColumn = 8;
				
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value24", "Value21", "Value27", locIT, "Value25");
		testEvent(EventType.TEXT_UNIT, "Value26", "Value23", "Value22", locGESW, "");
		testEvent(EventType.DOCUMENT_PART, "Value21            [#$$self$]                       " +
				"Value23              [#$$self$]         Value25        [#$$self$]              " +
				"[#$$self$]              recID1");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value34", "Value31", "Value37", locIT, "Value35");
		testEvent(EventType.TEXT_UNIT, "Value36", "recID2_descr", "Value32", locGESW, "");
		testEvent(EventType.DOCUMENT_PART, "Value31            [#$$self$]                       " +
				"                     [#$$self$]         Value35        [#$$self$]              " +
				"[#$$self$]              recID2");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testListedColumns2() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv_testa.txt").asInputStream();
		assertNotNull(input);
		
		params.columnNamesLineNum = 1;
		params.valuesStartLineNum = 2;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		//params.columnWidths = "19, 30, 21, 16, 15, 21, 20, 10";
		params.columnStartPositions = "1, 20, 50, 71, 87, 102, 123, 144";
		params.columnEndPositions = "11, 32, 62, 83, 97, 112, 133, 151";
		
		params.sourceColumns = "4, 6";
		params.sourceIdSuffixes = "_name, _descr";
		params.targetColumns = "     2,7   ";
		params.targetLanguages = "ge-sw, it";
		params.targetSourceRefs = "6, 4";
		params.sourceIdColumns = "1, 3";
		params.sourceIdSourceRefs = "4, 6";
		params.commentColumns = "5";
		params.commentSourceRefs = "4";
		params.recordIdColumn = 8;
				
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "SID1"); 
		testEvent(EventType.TEXT_UNIT, "Target2");
		testEvent(EventType.TEXT_UNIT, "SID2");
		testEvent(EventType.TEXT_UNIT, "Source1");
		testEvent(EventType.TEXT_UNIT, "Source2");
		testEvent(EventType.TEXT_UNIT, "Target1");
		testEvent(EventType.TEXT_UNIT, "Key");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value14", "Value11", "Value17", locIT, "");
		testEvent(EventType.TEXT_UNIT, "",        "Value13", "Value12", locGESW, "");
		testEvent(EventType.DOCUMENT_PART, "Value11            [#$$self$]          " +
				"             Value13              [#$$self$]                      " +
				"            [#$$self$]           [#$$self$]");		
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value24", "Value28_name", "Value27", locIT, "Value25");
		testEvent(EventType.TEXT_UNIT, "Value26", "Value23", "Value22", locGESW, "");		
		testEvent(EventType.DOCUMENT_PART, "                   [#$$self$]           " +
				"            Value23              [#$$self$]         Value25        " +
				"[#$$self$]              [#$$self$]              Value28");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value34", "Value31", "", locIT, "");
		//No 2-nd TU because the line 4 is shorter, no src2 
		testEvent(EventType.DOCUMENT_PART, "Value31            Value32              " +
				"         Value33              [#$$self$]        ");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value44", "Value41", "Value47", locIT, "Value45");
		testEvent(EventType.TEXT_UNIT, "Value46", "Value48_descr", "Value42", locGESW, "");
		testEvent(EventType.DOCUMENT_PART, "Value41            [#$$self$]           " +
				"                                 [#$$self$]         Value45        " +
				"[#$$self$]              [#$$self$]              Value48");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testListedColumns3() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv_testa.txt").asInputStream();
		assertNotNull(input);
		
		params.columnNamesLineNum = 1;
		params.valuesStartLineNum = 2;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.sendHeaderMode = Parameters.SEND_HEADER_NONE;
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		//params.columnWidths = "19, 30, 21, 16, 15, 21, 20, 10";
		params.columnStartPositions = "1, 20, 50, 71, 87, 102, 123, 144";
		params.columnEndPositions = "11, 32, 62, 83, 97, 112, 133, 151";
		
		params.sourceColumns = "4, 6";
		params.sourceIdSuffixes = "_name, _descr";
		params.targetColumns = "     2,7   ";
		params.targetLanguages = "ge-sw, it";
		params.targetSourceRefs = "6, 4";
		params.sourceIdColumns = "1, 3";
		params.sourceIdSourceRefs = "4, 6";
		params.commentColumns = "5";
		params.commentSourceRefs = "4";
		params.recordIdColumn = 8;
		
		String snippet = null;
		
		try {
			snippet = streamAsString(input);
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		LOGGER.trace(snippet);
		
		String result = generateOutput(getEvents(filter, snippet, locEN, locGESW),
			filter.getEncoderManager(), locGESW);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testListedColumns4() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/fwc_test4.txt").asInputStream();
		assertNotNull(input);
		
		params.columnNamesLineNum = 1;
		params.valuesStartLineNum = 2;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.sendHeaderMode = Parameters.SEND_HEADER_NONE;
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		//params.columnWidths = "19, 30, 21, 16, 15, 21, 20, 10";
		params.columnStartPositions = "1, 20";
		params.columnEndPositions = "11, 32";
		
		params.sourceColumns = "2";
		params.sourceIdSuffixes = "";
		params.targetColumns = "1";
		params.targetLanguages = "ge-sw";
		params.targetSourceRefs = "2";
		params.sourceIdColumns = "";
		params.sourceIdSourceRefs = "";
		params.commentColumns = "";
		params.commentSourceRefs = "";
		params.recordIdColumn = 0;
		
		String snippet = null;
		
		try {
			snippet = streamAsString(input);
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		LOGGER.trace(snippet);
		
		String result = generateOutput(getEvents(filter, snippet, locEN, locGESW),
			filter.getEncoderManager(), locGESW);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testListedColumns5() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/fwc_test5.txt").asInputStream();
		assertNotNull(input);
		
		params.columnNamesLineNum = 1;
		params.valuesStartLineNum = 2;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.sendHeaderMode = Parameters.SEND_HEADER_NONE;
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		//params.columnWidths = "19, 30, 21, 16, 15, 21, 20, 10";
		params.columnStartPositions = "1, 20";
		params.columnEndPositions = "11, 32";
		
		params.sourceColumns = "1";
		params.sourceIdSuffixes = "";
		params.targetColumns = "2";
		params.targetLanguages = "ge-sw";
		params.targetSourceRefs = "1";
		params.sourceIdColumns = "";
		params.sourceIdSourceRefs = "";
		params.commentColumns = "";
		params.commentSourceRefs = "";
		params.recordIdColumn = 0;
		
		String snippet = null;
		
		try {
			snippet = streamAsString(input);
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		LOGGER.trace(snippet);
		
		String result = generateOutput(getEvents(filter, snippet, locEN, locGESW),
			filter.getEncoderManager(), locGESW);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testFileEvents() {
		testDriver.setDisplayLevel(0);
						
		// Load filter parameters from a file, check if params have changed
		URL paramsUrl = root.in("/test_params3.txt").asUrl();
		assertNotNull(paramsUrl);  
		
		Parameters params = (Parameters) filter.getParameters();
		
		params.load(paramsUrl, false);
		InputStream input = root.in("/csv_test6.txt").asInputStream();
		assertNotNull(input);
		
		CommaSeparatedValuesFilterTest.setDefaults(params);
		params.valuesStartLineNum = 2;
		params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "FieldName1", 1, 0, 1);
		testEvent(EventType.TEXT_UNIT, "Field Name 2", 1, 0, 2);
		testEvent(EventType.TEXT_UNIT, "Field Name 3", 1, 0, 3);
		testEvent(EventType.TEXT_UNIT, "\"FieldName4\"", 1, 0, 4);	// Quotes remain part of the value
		testEvent(EventType.TEXT_UNIT, "FieldName5", 1, 0, 5);
		testEvent(EventType.TEXT_UNIT, "FieldName6", 1, 0, 6);
		testEvent(EventType.TEXT_UNIT, "FieldName7", 1, 0, 7);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11", 2, 1, 1);
		testEvent(EventType.TEXT_UNIT, "Value12", 2, 1, 2);
		testEvent(EventType.TEXT_UNIT, "Value13", 2, 1, 3);
		testEvent(EventType.TEXT_UNIT, "Value14", 2, 1, 4);
		testEvent(EventType.TEXT_UNIT, "Value17", 2, 1, 7);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21", 3, 2, 1);
		testEvent(EventType.TEXT_UNIT, "Value22", 3, 2, 2);
		testEvent(EventType.TEXT_UNIT, "Value23", 3, 2, 3);
		testEvent(EventType.TEXT_UNIT, "Value24", 3, 2, 4);
		testEvent(EventType.TEXT_UNIT, "Value25", 3, 2, 5);
		testEvent(EventType.TEXT_UNIT, "Value26", 3, 2, 6);
		testEvent(EventType.TEXT_UNIT, "Value27", 3, 2, 7);			
		testEvent(EventType.TEXT_UNIT, "Value28", 3, 2, 8);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value31", 4, 3, 1);
		testEvent(EventType.TEXT_UNIT, "Value32", 4, 3, 2);
		testEvent(EventType.TEXT_UNIT, "Value33", 4, 3, 3);
		testEvent(EventType.TEXT_UNIT, "Value34", 4, 3, 4);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value41", 5, 4, 1);
		testEvent(EventType.TEXT_UNIT, "Value42", 5, 4, 2);
		testEvent(EventType.TEXT_UNIT, "Value43", 5, 4, 3);
		testEvent(EventType.TEXT_UNIT, "Value44", 5, 4, 4);
		testEvent(EventType.TEXT_UNIT, "Value45", 5, 4, 5);
		testEvent(EventType.TEXT_UNIT, "Value46", 5, 4, 6);
		testEvent(EventType.TEXT_UNIT, "Value47", 5, 4, 7);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		params.valuesStartLineNum = 3;
		params.sendHeaderMode = Parameters.SEND_HEADER_NONE;
		
		input = root.in("/csv_test6.txt").asInputStream();
		filter.open(new RawDocument(input, "UTF-8", locEN));
				
		testEvent(EventType.START_DOCUMENT, null);
					
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21", 3, 1, 1);
		testEvent(EventType.TEXT_UNIT, "Value22", 3, 1, 2);
		testEvent(EventType.TEXT_UNIT, "Value23", 3, 1, 3);
		testEvent(EventType.TEXT_UNIT, "Value24", 3, 1, 4);
		testEvent(EventType.TEXT_UNIT, "Value25", 3, 1, 5);
		testEvent(EventType.TEXT_UNIT, "Value26", 3, 1, 6);
		testEvent(EventType.TEXT_UNIT, "Value27", 3, 1, 7);
		testEvent(EventType.TEXT_UNIT, "Value28", 3, 1, 8);
		testEvent(EventType.END_GROUP, null);
				
		
		filter.close();
		
		params.valuesStartLineNum = 3;
		params.sendHeaderMode = Parameters.SEND_HEADER_NONE;
		
		input = root.in("/csv_test6.txt").asInputStream();
		filter.open(new RawDocument(input, "UTF-8", locEN));
				
		testEvent(EventType.START_DOCUMENT, null);
					
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21", 3, 1, 1, 10);
		testEvent(EventType.TEXT_UNIT, "Value22", 3, 1, 2, 12);
		testEvent(EventType.TEXT_UNIT, "Value23", 3, 1, 3, 12);
		testEvent(EventType.TEXT_UNIT, "Value24", 3, 1, 4, 12);
		testEvent(EventType.TEXT_UNIT, "Value25", 3, 1, 5, 10);
		testEvent(EventType.TEXT_UNIT, "Value26", 3, 1, 6, 10);
		testEvent(EventType.TEXT_UNIT, "Value27", 3, 1, 7, 10);			
		testEvent(EventType.TEXT_UNIT, "Value28", 3, 1, 8, 7);			
		testEvent(EventType.END_GROUP, null);
		
		filter.close();
		
		params.valuesStartLineNum = 3;
		params.sendHeaderMode = Parameters.SEND_HEADER_COLUMN_NAMES_ONLY;
		
		input = root.in("/csv_test6.txt").asInputStream();
		filter.open(new RawDocument(input, "UTF-8", locEN));
				
		testEvent(EventType.START_DOCUMENT, null);
					
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "FieldName1", 1, 0, 1);
		testEvent(EventType.TEXT_UNIT, "Field Name 2", 1, 0, 2);
		testEvent(EventType.TEXT_UNIT, "Field Name 3", 1, 0, 3);
		testEvent(EventType.TEXT_UNIT, "\"FieldName4\"", 1, 0, 4);	// Quotes remain part of the value
		testEvent(EventType.TEXT_UNIT, "FieldName5", 1, 0, 5);
		testEvent(EventType.TEXT_UNIT, "FieldName6", 1, 0, 6);
		testEvent(EventType.TEXT_UNIT, "FieldName7", 1, 0, 7);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21", 3, 1, 1);
		testEvent(EventType.TEXT_UNIT, "Value22", 3, 1, 2);
		testEvent(EventType.TEXT_UNIT, "Value23", 3, 1, 3);
		testEvent(EventType.TEXT_UNIT, "Value24", 3, 1, 4);
		testEvent(EventType.TEXT_UNIT, "Value25", 3, 1, 5);
		testEvent(EventType.TEXT_UNIT, "Value26", 3, 1, 6);
		testEvent(EventType.TEXT_UNIT, "Value27", 3, 1, 7);			
		testEvent(EventType.TEXT_UNIT, "Value28", 3, 1, 8);
		testEvent(EventType.END_GROUP, null);
		
		filter.close();
		
		params.valuesStartLineNum = 1;
		//params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_FIXED_NUMBER;
		params.numColumns = 3;
		
		input = root.in("/csv_test6.txt").asInputStream();
		filter.open(new RawDocument(input, "UTF-8", locEN));
				
		testEvent(EventType.START_DOCUMENT, null);
					
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "FieldName1", 1, 1, 1);
		testEvent(EventType.TEXT_UNIT, "Field Name 2", 1, 1, 2);
		testEvent(EventType.TEXT_UNIT, "Field Name 3", 1, 1, 3);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11", 2, 2, 1);
		testEvent(EventType.TEXT_UNIT, "Value12", 2, 2, 2);
		testEvent(EventType.TEXT_UNIT, "Value13", 2, 2, 3);
		testEvent(EventType.END_GROUP, null);
		
		filter.close();
		
		params.numColumns = 10;
		
		input = root.in("/csv_test6.txt").asInputStream();
		filter.open(new RawDocument(input, "UTF-8", locEN));
				
		testEvent(EventType.START_DOCUMENT, null);
					
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "FieldName1", 1, 1, 1);
		testEvent(EventType.TEXT_UNIT, "Field Name 2", 1, 1, 2);
		testEvent(EventType.TEXT_UNIT, "Field Name 3", 1, 1, 3);
		testEvent(EventType.TEXT_UNIT, "\"FieldName4\"", 1, 1, 4);	// Quotes remain part of the value
		testEvent(EventType.TEXT_UNIT, "FieldName5", 1, 1, 5);
		testEvent(EventType.TEXT_UNIT, "FieldName6", 1, 1, 6);
		testEvent(EventType.TEXT_UNIT, "FieldName7", 1, 1, 7);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11", 2, 2, 1);
		testEvent(EventType.TEXT_UNIT, "Value12", 2, 2, 2);
		testEvent(EventType.TEXT_UNIT, "Value13", 2, 2, 3);
		testEvent(EventType.TEXT_UNIT, "Value14", 2, 2, 4);
		testEvent(EventType.TEXT_UNIT, "Value17", 2, 2, 7);			// Value28 is ignored
		testEvent(EventType.END_GROUP, null);
		
		filter.close();
		
		// List events		
		String filename = "csv_test1.txt";
		input = root.in("/" + filename).asInputStream();
		assertNotNull(input);
		
		LOGGER.trace(filename);
		filter.open(new RawDocument(input, "UTF-8", locEN));
		if ( !testDriver.process(filter) ) Assert.fail();
		filter.close();
	}
		
	@Test
	public void testHeader() {
		
		// Load filter parameters from a file, check if params have changed
		URL paramsUrl = root.in("/test_params3.txt").asUrl();
		assertNotNull(paramsUrl);  
		
		Parameters params = (Parameters) filter.getParameters();
		params.load(paramsUrl, false);
		InputStream input = root.in("/csv_test8.txt").asInputStream();
		assertNotNull(input);
		
		CommaSeparatedValuesFilterTest.setDefaults(params);
		params.valuesStartLineNum = 7;
		params.columnNamesLineNum = 4;
		params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_COL_NAMES;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		
		testEvent(EventType.TEXT_UNIT, "Test table", 1, 0, true);
		
		testEvent(EventType.TEXT_UNIT, "Contains column names in the 4-th line, a table caption in the 1-st line, and 4 lines of description. This is the 1-st header row.", 
				2, 0, true);
		
		testEvent(EventType.TEXT_UNIT, "This is the 2-nd header row. This table also delimits the number of columns by their names (5 columns only are extracted here)", 
				3, 0, true);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "FieldName1", 4, 0, 1, true);
		testEvent(EventType.TEXT_UNIT, "Field Name 2", 4, 0, 2, true);
		testEvent(EventType.TEXT_UNIT, "Field Name 3", 4, 0, 3, true);
		testEvent(EventType.TEXT_UNIT, "FieldName4", 4, 0, 4, true);	
		testEvent(EventType.TEXT_UNIT, "FieldName5", 4, 0, 5, true);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.TEXT_UNIT, "This is the 4-th header row.",	5, 0, true);
		testEvent(EventType.TEXT_UNIT, "This is the 5-th header row. Data start right after here.", 6, 0, true);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11", 7, 1, 1);
		testEvent(EventType.TEXT_UNIT, "Value12", 7, 1, 2);
		testEvent(EventType.TEXT_UNIT, "Value13", 7, 1, 3);
		testEvent(EventType.TEXT_UNIT, "Value14", 7, 1, 4);
		testEvent(EventType.TEXT_UNIT, "Value15", 7, 1, 5);				
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21", 8, 2, 1);
		testEvent(EventType.TEXT_UNIT, "Value22", 8, 2, 2);
		testEvent(EventType.TEXT_UNIT, "Value23", 8, 2, 3);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value31", 9, 3, 1);
		testEvent(EventType.TEXT_UNIT, "Value32", 9, 3, 2);
		testEvent(EventType.TEXT_UNIT, "Value33", 9, 3, 3);
		testEvent(EventType.TEXT_UNIT, "Value34", 9, 3, 4);
		testEvent(EventType.TEXT_UNIT, "Value35", 9, 3, 5);				
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
				
		input = root.in("/csv_test8.txt").asInputStream();
		assertNotNull(input);
		
		params.valuesStartLineNum = 7;
		params.columnNamesLineNum = 4;
		params.sendHeaderMode = Parameters.SEND_HEADER_NONE;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_COL_NAMES;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
				
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11", 7, 1, 1);
		testEvent(EventType.TEXT_UNIT, "Value12", 7, 1, 2);
		testEvent(EventType.TEXT_UNIT, "Value13", 7, 1, 3);
		testEvent(EventType.TEXT_UNIT, "Value14", 7, 1, 4);
		testEvent(EventType.TEXT_UNIT, "Value15", 7, 1, 5);				
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21", 8, 2, 1);
		testEvent(EventType.TEXT_UNIT, "Value22", 8, 2, 2);
		testEvent(EventType.TEXT_UNIT, "Value23", 8, 2, 3);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value31", 9, 3, 1);
		testEvent(EventType.TEXT_UNIT, "Value32", 9, 3, 2);
		testEvent(EventType.TEXT_UNIT, "Value33", 9, 3, 3);
		testEvent(EventType.TEXT_UNIT, "Value34", 9, 3, 4);
		testEvent(EventType.TEXT_UNIT, "Value35", 9, 3, 5);				
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		input = root.in("/csv_test8.txt").asInputStream();
		assertNotNull(input);
		
		params.valuesStartLineNum = 7;
		params.columnNamesLineNum = 4;
		params.sendHeaderMode = Parameters.SEND_HEADER_COLUMN_NAMES_ONLY;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_COL_NAMES;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "FieldName1", 4, 0, 1, true);
		testEvent(EventType.TEXT_UNIT, "Field Name 2", 4, 0, 2, true);
		testEvent(EventType.TEXT_UNIT, "Field Name 3", 4, 0, 3, true);
		testEvent(EventType.TEXT_UNIT, "FieldName4", 4, 0, 4, true);	
		testEvent(EventType.TEXT_UNIT, "FieldName5", 4, 0, 5, true);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11", 7, 1, 1);
		testEvent(EventType.TEXT_UNIT, "Value12", 7, 1, 2);
		testEvent(EventType.TEXT_UNIT, "Value13", 7, 1, 3);
		testEvent(EventType.TEXT_UNIT, "Value14", 7, 1, 4);
		testEvent(EventType.TEXT_UNIT, "Value15", 7, 1, 5);				
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21", 8, 2, 1);
		testEvent(EventType.TEXT_UNIT, "Value22", 8, 2, 2);
		testEvent(EventType.TEXT_UNIT, "Value23", 8, 2, 3);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value31", 9, 3, 1);
		testEvent(EventType.TEXT_UNIT, "Value32", 9, 3, 2);
		testEvent(EventType.TEXT_UNIT, "Value33", 9, 3, 3);
		testEvent(EventType.TEXT_UNIT, "Value34", 9, 3, 4);
		testEvent(EventType.TEXT_UNIT, "Value35", 9, 3, 5);				
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		List<String> list = filter.getColumnNames();
		assertNotNull(list);
		assertEquals(5, list.size());
		
		assertEquals("FieldName1", list.get(0));
		assertEquals("Field Name 3", list.get(2));
		assertEquals("FieldName5", list.get(4));
		
		input = root.in("/csv_test8.txt").asInputStream();
		assertNotNull(input);
		
		params.valuesStartLineNum = 7;
		params.columnNamesLineNum = 0;
		params.sendHeaderMode = Parameters.SEND_HEADER_COLUMN_NAMES_ONLY;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_COL_NAMES;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11", 7, 1, 1);
		testEvent(EventType.TEXT_UNIT, "Value12", 7, 1, 2);
		testEvent(EventType.TEXT_UNIT, "Value13", 7, 1, 3);
		testEvent(EventType.TEXT_UNIT, "Value14", 7, 1, 4);
		testEvent(EventType.TEXT_UNIT, "Value15", 7, 1, 5);				
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21", 8, 2, 1);
		testEvent(EventType.TEXT_UNIT, "Value22", 8, 2, 2);
		testEvent(EventType.TEXT_UNIT, "Value23", 8, 2, 3);
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value31", 9, 3, 1);
		testEvent(EventType.TEXT_UNIT, "Value32", 9, 3, 2);
		testEvent(EventType.TEXT_UNIT, "Value33", 9, 3, 3);
		testEvent(EventType.TEXT_UNIT, "Value34", 9, 3, 4);
		testEvent(EventType.TEXT_UNIT, "Value35", 9, 3, 5);				
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		list = filter.getColumnNames();
		assertNotNull(list);
		assertEquals(0, list.size());
		
	}
	
	@Test
	public void testSkeleton () {
		String st = null;
		String expected = null;
		
		try {
			st = getSkeleton(getFullFileName("/csv_test6.txt"));
		} 
		catch (UnsupportedEncodingException e) {
		}	
		LOGGER.trace(String.format("Skeleton of %s\n---\n", "csv_test6.txt") + st + "\n----------");
		
		try {
			expected = streamAsString(root.in("/csv_test6.txt").asInputStream());			
		} 
		catch (IOException e) {
		}
		assertEquals(expected, st);
	}
	
	@Test
	public void testSkeleton2 () {
		String st = null;
		String expected = null;
		
		Parameters params = (Parameters) filter.getParameters();
		
		params.columnNamesLineNum = 0;
		params.valuesStartLineNum = 1;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
		//params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		params.sendColumnsMode = Parameters.SEND_COLUMNS_ALL;
		//params.columnWidths = "19, 30, 21, 16, 15, 21, 20, 10";
		params.columnStartPositions	 = " 1, 20, 50, 71, 87, 102, 123, 144";
		params.columnEndPositions	 = "11, 32, 62, 83, 97, 112, 133, 151";
		
		params.sourceColumns = "4, 6";
		params.sourceIdSuffixes = "_name, _descr";
		params.targetColumns = "     2,7   ";
		params.targetLanguages = "ge-sw, it";
		params.targetSourceRefs = "6, 4";
		params.sourceIdColumns = "1, 3";
		params.sourceIdSourceRefs = "4, 6";
		params.commentColumns = "5";
		params.commentSourceRefs = "4";
		params.recordIdColumn = 8;
		
		try {
			st = getSkeleton(getFullFileName("/csv_testb.txt"));
		} 
		catch (UnsupportedEncodingException e) {
		}	
		LOGGER.trace(String.format("Skeleton of %s\n---\n", "csv_testb.txt") + st + "\n----------");
		
		try {
			expected = streamAsString(root.in("/csv_testb.txt").asInputStream());			
		} 
		catch (IOException e) {
		}
		assertEquals(expected, st);
	}
	
	@Test
	public void testSkeleton3 () {
		String st = null;
		String expected = null;
		
		Parameters params = (Parameters) filter.getParameters();
		
		params.columnNamesLineNum = 0;
		params.valuesStartLineNum = 1;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
				
		params.columnStartPositions	 = " 1, 20, 50, 71, 87, 102, 123, 144";
		params.columnEndPositions	 = "11, 32, 62, 83, 97, 112, 133, 151";
		
		params.sourceColumns = "4, 6";
		params.sourceIdSuffixes = "_name, _descr";
		params.targetColumns = "     2,7   ";
		params.targetLanguages = "ge-sw, it";
		params.targetSourceRefs = "6, 4";
		params.sourceIdColumns = "1, 3";
		params.sourceIdSourceRefs = "4, 6";
		params.commentColumns = "5";
		params.commentSourceRefs = "4";
		params.recordIdColumn = 8;
		
		try {
			st = getSkeleton(getFullFileName("/csv_testb.txt"));
		} 
		catch (UnsupportedEncodingException e) {
		}	
		LOGGER.trace(String.format("Skeleton of %s\n---\n", "csv_testb.txt") + st + "\n----------");
		
		try {
			expected = streamAsString(root.in("/csv_testb.txt").asInputStream());			
		} 
		catch (IOException e) {
		}
		assertEquals(expected, st);
	}
	@Test
	public void testDoubleExtraction () {
		// Read all files in the data directory
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/csv_test6.txt").toString(), ""));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR));
	}

	// Helpers
	private String getFullFileName(String fileName) {
		return root.in(fileName).toString();
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
			
			assertEquals(expectedText, ((ITextUnit)res).toString());
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

	private void testEvent(EventType expectedType, String expectedText, int expectedLineNum, int expRow, int expCol) {
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
			assertEquals(expectedLineNum, new Integer(st).intValue());
						
			prop = ((ITextUnit)res).getSourceProperty(BaseTableFilter.ROW_NUMBER);
			assertNotNull(prop);
			
			st = prop.getValue();
			assertEquals(expRow, new Integer(st).intValue());
			
			prop = ((ITextUnit)res).getSourceProperty(BaseTableFilter.COLUMN_NUMBER);
			assertNotNull(prop);
			
			st = prop.getValue();
			assertEquals(expCol, new Integer(st).intValue());
						
			
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

	private void testEvent(EventType expectedType, String expectedText, int expectedLineNum, int expRow, 
			int expCol, int expWidth) {
		
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
			assertEquals(expectedLineNum, new Integer(st).intValue());
						
			prop = ((ITextUnit)res).getSourceProperty(BaseTableFilter.ROW_NUMBER);
			assertNotNull(prop);
			
			st = prop.getValue();
			assertEquals(expRow, new Integer(st).intValue());
			
			prop = ((ITextUnit)res).getSourceProperty(BaseTableFilter.COLUMN_NUMBER);
			assertNotNull(prop);
			
			st = prop.getValue();
			assertEquals(expCol, new Integer(st).intValue());
						
			prop = ((ITextUnit)res).getSourceProperty(FixedWidthColumnsFilter.COLUMN_WIDTH);
			assertNotNull(prop);
			
			st = prop.getValue();
			assertEquals(expWidth, new Integer(st).intValue());
			
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
	
	private void testEvent(EventType expectedType, String expectedText, int expectedLineNum, int expRow, 
			boolean isHeader) {
		
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
			assertEquals(expectedLineNum, new Integer(st).intValue());
						
			prop = ((ITextUnit)res).getSourceProperty(BaseTableFilter.ROW_NUMBER);
			assertNotNull(prop);
			
			st = prop.getValue();
			assertEquals(expRow, new Integer(st).intValue());
												
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
	
	private void testEvent(EventType expectedType,
		String source,
		String expName,
		String target,
		LocaleId language,
		String comment)
	{
		assertNotNull(filter);
		Event event = filter.next();		
		assertNotNull(event);
		assertTrue(event.getEventType() == expectedType);
		
		switch (event.getEventType()) {
		case TEXT_UNIT:
			IResource res = event.getResource();
			assertTrue(res instanceof ITextUnit);
			ITextUnit tu = (ITextUnit)res;
			assertEquals(source, tu.toString());
			Property prop = tu.getSourceProperty(AbstractLineFilter.LINE_NUMBER);
			assertNotNull(prop);
			if ( !Util.isEmpty(expName) ) {
				assertEquals(expName, tu.getName());
			}
			if ( !Util.isEmpty(target) && !Util.isNullOrEmpty(language) ) {
				TextContainer trg = tu.getTarget(language);
				assertNotNull(trg);
				assertEquals(target, trg.toString());
			}
			if ( !Util.isEmpty(comment) ) {
				prop = tu.getProperty(Property.NOTE);
				assertNotNull(prop);
				assertEquals(comment, prop.toString());
			}
			break;

		default:
			break;
		}
			
	}
	
	private void testEvent(EventType expectedType, String expectedText, int expectedLineNum, int expRow, 
			int expCol, boolean isHeader) {
		
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
			assertEquals(expectedLineNum, new Integer(st).intValue());
						
			prop = ((ITextUnit)res).getSourceProperty(BaseTableFilter.ROW_NUMBER);
			assertNotNull(prop);
			
			st = prop.getValue();
			assertEquals(expRow, new Integer(st).intValue());
			
			prop = ((ITextUnit)res).getSourceProperty(BaseTableFilter.COLUMN_NUMBER);
			assertNotNull(prop);
			
			st = prop.getValue();
			assertEquals(expCol, new Integer(st).intValue());
			
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
	
	private Parameters getParameters() {
		IParameters punk = filter.getParameters();
		
		if (punk instanceof Parameters)
			return (Parameters) punk;
		else
			return null;
	}

	private String getSkeleton (String fileName) throws UnsupportedEncodingException {
		ByteArrayOutputStream writerBuffer;
										
		try (IFilterWriter writer = filter.createFilterWriter()) {
			// Open the input
			filter.open(new RawDocument((new File(fileName)).toURI(), "UTF-8", locEN, locFR));

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
		BufferedReader reader = null;
		reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

		StringBuilder tmp = new StringBuilder();
		char[] buf = new char[2048];
		int count = 0;
		while (( count = reader.read(buf)) != -1 ) {
			tmp.append(buf, 0, count);
		}
		
        return tmp.toString();
    }

	@Test
	public void testSkelRefs() {
		GenericSkeleton skel = new GenericSkeleton();
		GenericSkeleton newSkel = skel;
		skel = null;
		assertNotNull(newSkel);
	}
}
