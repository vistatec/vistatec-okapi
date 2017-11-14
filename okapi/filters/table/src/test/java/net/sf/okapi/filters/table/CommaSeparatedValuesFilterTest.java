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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.table.csv.CSVSkeletonWriter;
import net.sf.okapi.filters.table.csv.CommaSeparatedValuesFilter;
import net.sf.okapi.filters.table.csv.Parameters;
import net.sf.okapi.lib.extra.filters.AbstractLineFilter;
import net.sf.okapi.lib.extra.filters.WrapMode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(JUnit4.class)
public class CommaSeparatedValuesFilterTest {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private CommaSeparatedValuesFilter filter;
	private FilterTestDriver testDriver;
	private FileLocation root;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	private LocaleId locFRCA = LocaleId.fromString("fr-ca");
	private LocaleId locFRFR = LocaleId.fromString("fr-fr");
	
	@Before
	public void setUp() {
		filter = new CommaSeparatedValuesFilter();
		assertNotNull(filter);
		
		testDriver = new FilterTestDriver();
		assertNotNull(testDriver);
		
		testDriver.setDisplayLevel(0);
		testDriver.setShowSkeleton(true);
		
        root = FileLocation.fromClass(this.getClass());
        Parameters params = (Parameters) filter.getParameters();
        setDefaults(params);
	}

	@Test
	public void testCatkeys () {
		Parameters params = (Parameters) filter.getParameters();
		// col1=source, col3=comment, col4=target
		params.load(root.in("/okf_table@catkeys.fprm").asUrl(), false);
		String snippet = "1\tfrench\tinfo\t1234\n"
			+ "Source 1\tContext 1\tComment 1\tTarget 1\n"
			+ "Source 2\tContext 2\t\tTarget 2\n";
		
		List<Event> events = getEvents(snippet, locEN, locFR);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("Source 1", tu.getSource().toString());
		assertEquals("Target 1", tu.getTarget(locFR).toString());
		assertEquals("Comment 1", tu.getProperty(Property.NOTE).getValue());

		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu);
		assertEquals("Source 2", tu.getSource().toString());
		assertTrue(null==tu.getProperty(Property.NOTE));
		assertEquals("Target 2", tu.getTarget(locFR).toString());
	}
	
	@Test
	public void testThreeColumnsSrcTrgData () {
		String snippet = "\"src\",\"trg\",data\n"
			+ "\"source1\",\"target1\",data1\n"
			+ "\"source2\",\"target2\",data2\n";

		// Set the parameters
		Parameters params = new Parameters();
		params.fieldDelimiter = ",";
		params.textQualifier = "\"";
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		params.sourceColumns = "1";
		params.targetColumns = "2";
		params.targetLanguages = locFRCA.toString();
		params.targetSourceRefs = "1";
		filter.setParameters(params);
		
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFRCA),
			filter.getEncoderManager(), locFRCA);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testThreeColumnsSrcDataWithSubfilter () {
		String snippet = "\"skel\",\"source1<b>bold</b>\",\"source2\",data1";

		// Set the parameters
		Parameters params = new Parameters();
		params.fieldDelimiter = ",";
		params.textQualifier = "\"";
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		params.sourceColumns = "2,3";
		params.targetLanguages = locFRCA.toString();
		params.subfilter = "okf_html";
		filter.setParameters(params);
		
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFRCA),
			filter.getEncoderManager(), locFRCA);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testThreeColumnsExtractAllWithSubfilter() {
		String snippet = "\"source1\",\"source2<b>bold</b>\",data1\n";			

		// Set the parameters
		Parameters params = new Parameters();
		params.fieldDelimiter = ",";
		params.textQualifier = "\"";
		params.sendColumnsMode = Parameters.SEND_COLUMNS_ALL;
		params.targetLanguages = locFRCA.toString();
		params.subfilter = "okf_html";
		filter.setParameters(params);
		
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFRCA),
			filter.getEncoderManager(), locFRCA);
		assertEquals(snippet, result);
	}

	@Test
	public void testSubfilterTuIds() {
	    String snippet = "\"source1\",\"source2<b>bold</b>\",data1\n";

        // Set the parameters
        Parameters params = new Parameters();
        params.fieldDelimiter = ",";
        params.textQualifier = "\"";
        params.sendColumnsMode = Parameters.SEND_COLUMNS_ALL;
        params.subfilter = "okf_html";
        filter.setParameters(params);

        List<ITextUnit> tus = FilterTestDriver.filterTextUnits(getEvents(snippet, locEN, locFRCA));
        assertEquals(3, tus.size());
        Set<String> ids = new HashSet<>();
        for (ITextUnit tu : tus) {
            ids.add(tu.getId());
        }
        // If there was an ID collection, there will be fewer IDs than TUs
        assertEquals(tus.size(), ids.size());
	}

	@Test
	public void testThreeColumnsSrcTrgData_2 () {
		String snippet = "\"src\",\"trg\",data\n"
			+ "\"source1\",         \"target1\",data1\n"
			+ "\"source2\"    ,\"target2\",data2\n";

		// Set the parameters
		Parameters params = new Parameters();
		params.fieldDelimiter = ",";
		params.textQualifier = "\"";
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		params.sourceColumns = "1";
		params.targetColumns = "2";
		params.targetLanguages = locFRCA.toString();
		params.targetSourceRefs = "1";
		filter.setParameters(params);
		
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFRCA),
			filter.getEncoderManager(), locFRCA);  
		assertEquals(snippet, result);
	}
	
	@Test
	public void testThreeColumnsSrcTrgData_3 () {
		
		InputStream input = root.in("/CSVTest_97.txt").asInputStream();
		assertNotNull(input);
		
		String snippet = null;
		
		try {
			snippet = streamAsString(input);
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		// Set the parameters
		Parameters params = new Parameters();
		params.fieldDelimiter = ",";
		params.textQualifier = "\"";
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		params.sourceColumns = "1,4";
		params.targetColumns = "2,5";
		params.targetLanguages = "FR-CA,FR-CA";
		params.targetSourceRefs = "1,4";
		filter.setParameters(params);
				
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFRCA),
			filter.getEncoderManager(), locFRCA);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testFileEvents96_2() {
		
		//Parameters params = (Parameters) filter.getParameters();
			
		InputStream input = root.in("/CSVTest_96_2.txt").asInputStream(); // issue 96
		assertNotNull(input);
	
		// Set the parameters
		Parameters params = new Parameters();
		setDefaults(params);
		params.fieldDelimiter = ",";
		params.textQualifier = "\"";
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		params.sourceColumns = "1";
		params.targetColumns = "2";
		params.targetLanguages = locFRCA.toString();
		params.targetSourceRefs = "1";
		filter.setParameters(params);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "src");
		testEvent(EventType.TEXT_UNIT, "trg");
		testEvent(EventType.TEXT_UNIT, "data");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1", "", "target1", locFRCA, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",data1");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source2", "", "target2", locFRCA, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",data2");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		filter.close();
		
	}
	
	@Test
	public void testFileEvents96_3() {
		
		//Parameters params = (Parameters) filter.getParameters();
			
		InputStream input = root.in("/CSVTest_96_2.txt").asInputStream(); // issue 96
		assertNotNull(input);
	
		// Set the parameters
		Parameters params = new Parameters();
		setDefaults(params);
		params.fieldDelimiter = ",";
		params.textQualifier = "\"";
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		params.sourceColumns = "1";
		params.targetColumns = "2";
		params.targetLanguages = ""; //locFRCA.toString();
		params.targetSourceRefs = "1";
		filter.setParameters(params);
		
		filter.open(new RawDocument(input, "UTF-8", locEN, locFRCA));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "src");
		testEvent(EventType.TEXT_UNIT, "trg");
		testEvent(EventType.TEXT_UNIT, "data");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1", "", "target1", locFRCA, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",data1");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source2", "", "target2", locFRCA, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",data2");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
				filter.close();		
	}	
	
	@Test
	public void testSkeletonWriter () {
		String snippet = "text1, \"text2\", text3, text4";
//		String snippet2 = "text1, \"text2_1, text2_2\", \"text3_1, text3_2\", text4";
		// The writer shouldn't add additional qualifiers not present in the original
		String snippet2 = "text1, \"text2_1, text2_2\", text3_1, text3_2, text4";

		List<Event> list = getEvents(snippet, locEN, locFRCA);
		assertEquals(8, list.size());
		
		Event e;
		ITextUnit tu;
		
		// Replace ["text2"] with [text2_1, text2_2]
		e = list.get(3);
		tu = e.getTextUnit();
		tu.setSource(new TextContainer("text2_1, text2_2"));
		assertEquals("yes", tu.getProperty("qualified").getValue());
		
		// Replace [text3] with [text3_1, text3_2]
		e = list.get(4);
		tu = e.getTextUnit();
		tu.setSource(new TextContainer("text3_1, text3_2"));
		assertNull(tu.getProperty("qualified"));
		
		String result = FilterTestDriver.generateOutput(list, locFRCA,
				new CSVSkeletonWriter(), filter.getEncoderManager());

		assertEquals(snippet2, result);
	}
	
	public static void setDefaults(net.sf.okapi.filters.table.base.Parameters params) {
		
		if (params == null) return;
		
		params.columnNamesLineNum = 1;
		params.valuesStartLineNum = 2;
		params.sourceIdSuffixes = "";
		params.sourceIdSourceRefs = "";
		params.targetColumns = "";
		params.targetSourceRefs = "";
		params.targetLanguages = "";
		params.commentColumns = "";
		params.commentSourceRefs = "";
		params.columnNamesLineNum = 1;
		params.valuesStartLineNum = 2;
		params.sendHeaderMode = Parameters.SEND_HEADER_COLUMN_NAMES_ONLY;
//		params.detectColumnsMode = Parameters.DETECT_COLUMNS_COL_NAMES;
		params.sendColumnsMode = Parameters.SEND_COLUMNS_ALL;
		params.sourceColumns = "";
		params.targetColumns = "";
		params.targetSourceRefs = "";
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
		assertEquals("text/csv", filter.getMimeType());
		assertEquals("okf_table_csv", filter.getName());
		
		// Read lines from a file, check mime types 
		InputStream input = root.in("/csv_test1.txt").asInputStream();
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
						
		assertEquals(params.unescapeSource, true);
		assertEquals(params.trimLeading, true);
		assertEquals(params.trimTrailing, true);
		assertEquals(params.preserveWS, true);
		assertEquals(params.useCodeFinder, false);
		assertEquals(				
				"#v1\ncount.i=2\nrule0=%(([-0+#]?)[-0+#]?)((\\d\\$)?)(([\\d\\*]*)(\\.[\\d\\*]*)?)[dioxXucsfeEgGpn]\n" +
				"rule1=(\\\\r\\\\n)|\\\\a|\\\\b|\\\\f|\\\\n|\\\\r|\\\\t|\\\\v\nsample=\nuseAllRulesWhenTesting.b=false", 
				params.codeFinderRules);
									
		// Check if defaults are set
		params = new Parameters();
		filter.setParameters(params);
		
		params.columnNamesLineNum = 1;
		params.valuesStartLineNum = 1;
		params.detectColumnsMode = 1;
		params.numColumns = 1;
		params.sendHeaderMode = 1;
		params.trimMode = 1;
		params.fieldDelimiter = "1";
		params.textQualifier = "1";
		params.sourceIdColumns = "1";
		params.sourceColumns = "1";
		params.targetColumns = "1";
		params.commentColumns = "1";
		params.preserveWS = true;
		params.useCodeFinder = true;
		
		params = getParameters();
		
		assertEquals(params.fieldDelimiter, "1");
		assertEquals(params.columnNamesLineNum, 1);
		assertEquals(params.numColumns, 1);
		assertEquals(params.sendHeaderMode, 1);
		assertEquals(params.textQualifier, "1");
		assertEquals(params.trimMode, 1);
		assertEquals(params.valuesStartLineNum, 1);
		assertEquals(params.preserveWS, true);
		assertEquals(params.useCodeFinder, true);
		
		// Load filter parameters from a file, check if params have changed
//		URL paramsUrl = root.in("/test_params1.txt").asUrl();
//		assertNotNull(paramsUrl);  
		
		params.load(root.in("/test_params1.txt").asUrl(), false);
		
		assertEquals("2", params.fieldDelimiter);
		assertEquals(params.columnNamesLineNum, 2);
		assertEquals(params.numColumns, 2);
		assertEquals(params.sendHeaderMode, 2);
		assertEquals("2", params.textQualifier);
		assertEquals(params.trimMode, 2);
		assertEquals(params.valuesStartLineNum, 2);
		assertEquals(params.preserveWS, false);
		assertEquals(params.useCodeFinder, false);
		
		// Save filter parameters to a file, load and check if params have changed
		URL paramsUrl = root.in("/test_params2.txt").asUrl();
		assertNotNull(paramsUrl);
		
		params.save(paramsUrl.toURI().getPath());
		
		// Change params before loading them
		params = (Parameters) filter.getParameters();
		params.fieldDelimiter = "3";
		params.columnNamesLineNum = 3;
		params.numColumns = 3;
		params.sendHeaderMode = 3;
		params.textQualifier = "3";
		params.trimMode = 3;
		params.valuesStartLineNum = 3;
		params.preserveWS = true;
		params.useCodeFinder = true;
		
		params.load(paramsUrl, false);
		
		assertEquals(params.fieldDelimiter, "2");
		assertEquals(params.columnNamesLineNum, 2);
		assertEquals(params.numColumns, 2);
		assertEquals(params.sendHeaderMode, 2);
		assertEquals(params.textQualifier, "2");
		assertEquals(params.trimMode, 2);
		assertEquals(params.valuesStartLineNum, 2);
		assertEquals(params.preserveWS, false);
		assertEquals(params.useCodeFinder, false);
		
		// Check if parameters type is controlled
		
		filter.setParameters(new net.sf.okapi.filters.plaintext.base.Parameters());
		InputStream input = root.in("/csv_test1.txt").asInputStream();
		try {
			filter.open(new RawDocument(input, "UTF-8", locEN));
			fail("OkapiBadFilterParametersException should've been trown");
		}
		catch (OkapiBadFilterParametersException e) {
		}
		
		filter.close();
	
		filter.setParameters(new net.sf.okapi.filters.table.csv.Parameters());
		input = root.in("/csv_test1.txt").asInputStream();
		try {
			filter.open(new RawDocument(input, "UTF-8", locEN));
		}
		catch (OkapiBadFilterParametersException e) {
			fail("OkapiBadFilterParametersException should NOT have been trown");
		}
			filter.close();
	}

	@Test
	public void testSkeleton () {
		String st = null;
		String expected = null;
		
		try {
			st = getSkeleton(getFullFileName("/csv_test1.txt"));
		} 
		catch (UnsupportedEncodingException e) {
		}	
		LOGGER.trace(String.format("Skeleton of %s\n---\n", "csv_test1.txt") + st + "\n----------");
		
		try {
			expected = streamAsString(root.in("/csv_test1.txt").asInputStream());			
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
			st = getSkeleton(getFullFileName("/csv_test2.txt"));
		} 
		catch (UnsupportedEncodingException e) {
		}	
		LOGGER.trace(String.format("Skeleton of %s\n---\n", "csv_test2.txt") + st + "\n----------");
		
		try {
			expected = streamAsString(root.in("/csv_test2.txt").asInputStream());			
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
			st = getSkeleton(getFullFileName("/csv_test3.txt"));
		} 
		catch (UnsupportedEncodingException e) {
		}	
		LOGGER.trace(String.format("Skeleton of %s\n---\n", "csv_test3.txt") + st + "\n----------");
		
		try {
			expected = streamAsString(root.in("/csv_test3.txt").asInputStream());			
		} 
		catch (IOException e) {
		}
		assertEquals(expected, st);
	}
	
	@Test
	public void testFileEvents() {
		testDriver.setDisplayLevel(0);
		
		InputStream input = root.in("/csv_test1.txt").asInputStream();
		assertNotNull(input);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "FieldName1");
		testEvent(EventType.TEXT_UNIT, "FieldName2");
		testEvent(EventType.TEXT_UNIT, "FieldName3");
		testEvent(EventType.TEXT_UNIT, "FieldName4");
		testEvent(EventType.TEXT_UNIT, "FieldName5");
		testEvent(EventType.TEXT_UNIT, "FieldName6");
		testEvent(EventType.TEXT_UNIT, "FieldName7");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11");
		testEvent(EventType.TEXT_UNIT, "Value12");
		testEvent(EventType.TEXT_UNIT, "Value13");
		testEvent(EventType.TEXT_UNIT, "Value14");
		testEvent(EventType.TEXT_UNIT, "Value15");
		testEvent(EventType.TEXT_UNIT, "Value16");
		testEvent(EventType.TEXT_UNIT, "Value17");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21");
		testEvent(EventType.TEXT_UNIT, "Value22");
		testEvent(EventType.TEXT_UNIT, "Value23");
		testEvent(EventType.TEXT_UNIT, "Value24");
		testEvent(EventType.TEXT_UNIT, "Value25");
		testEvent(EventType.TEXT_UNIT, "Value26");
		testEvent(EventType.TEXT_UNIT, "Value27");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value31");
		testEvent(EventType.TEXT_UNIT, "Value32");
		testEvent(EventType.TEXT_UNIT, "Value33");
		testEvent(EventType.TEXT_UNIT, "Value34");
		testEvent(EventType.TEXT_UNIT, "Value35");
		testEvent(EventType.TEXT_UNIT, "Value36");
		testEvent(EventType.TEXT_UNIT, "Value37");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
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
	public void testFileEvents106() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv.txt").asInputStream(); // issue 106
		assertNotNull(input);
		
		params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
		params.valuesStartLineNum = 2;
		params.columnNamesLineNum = 1;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_COL_NAMES;
		params.trimLeading = false;
		params.trimTrailing = false;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "id");
		testEvent(EventType.TEXT_UNIT, "value");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "01");
		testEvent(EventType.TEXT_UNIT, "one");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "02");
		testEvent(EventType.TEXT_UNIT, "first,\nsecond\n");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "03");
		testEvent(EventType.TEXT_UNIT, "three");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents106_2() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv2.txt").asInputStream(); // issue 106
		assertNotNull(input);
		
		params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
		params.valuesStartLineNum = 2;
		params.columnNamesLineNum = 1;
		//params.detectColumnsMode = Parameters.DETECT_COLUMNS_COL_NAMES;
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "id");
		testEvent(EventType.TEXT_UNIT, "value");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "01");
		testEvent(EventType.TEXT_UNIT, "one");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "02");
		testEvent(EventType.TEXT_UNIT, "first,\nsecond");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "03");
		testEvent(EventType.TEXT_UNIT, "three");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents2() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv_testg.txt").asInputStream();
		assertNotNull(input);
		
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.removeQualifiers = true;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
			
		// Line 1
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one, two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 2
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 3, 4
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
//		// Line 5
//		testEvent(EventType.START_GROUP, null);
//		testEvent(EventType.TEXT_UNIT, "1");
//		testEvent(EventType.TEXT_UNIT, "one");
//		testEvent(EventType.END_GROUP, null);
//		// Line 6
//		testEvent(EventType.START_GROUP, null);
//		testEvent(EventType.TEXT_UNIT, "two");
//		testEvent(EventType.TEXT_UNIT, "xxx");
//		testEvent(EventType.TEXT_UNIT, "yyy");
//		testEvent(EventType.END_GROUP, null);
		// Line 5, 6
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one,\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 7, 8
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);				
		// Line 9, 10
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 11, 12, 13
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two\n three");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 14, 15, 16, 17
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two\n three");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents2a() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv_testg2.txt").asInputStream();
		assertNotNull(input);
		
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.removeQualifiers = true;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
			
		// Line 1, 2, 3, 4
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		//testEvent(EventType.TEXT_UNIT, "one\n two\n three\n ");
		testEvent(EventType.TEXT_UNIT, "one\n two\n three"); // trim leading
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents3() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv_testg.txt").asInputStream();
		assertNotNull(input);
		
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.removeQualifiers = false;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		// Line 1				
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "\"one, two\"");
		testEvent(EventType.TEXT_UNIT, "\"xxx\"");
		testEvent(EventType.TEXT_UNIT, "\"yyy\"");
		testEvent(EventType.END_GROUP, null);
		// Line 2
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "\"one \"\"two\"\"\"");
		testEvent(EventType.TEXT_UNIT, "\"xxx\"");
		testEvent(EventType.TEXT_UNIT, "\"yyy\"");
		testEvent(EventType.END_GROUP, null);
		// Line 3, 4
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "\"one\n two\"");
		testEvent(EventType.TEXT_UNIT, "\"xxx\"");
		testEvent(EventType.TEXT_UNIT, "\"yyy\"");
		testEvent(EventType.END_GROUP, null);
//		// Line 5
//		testEvent(EventType.START_GROUP, null);
//		testEvent(EventType.TEXT_UNIT, "1");
//		testEvent(EventType.TEXT_UNIT, "\"one");
//		testEvent(EventType.END_GROUP, null);
//		// Line 6
//		testEvent(EventType.START_GROUP, null);
//		testEvent(EventType.TEXT_UNIT, "\"two\"\"");
//		testEvent(EventType.TEXT_UNIT, "\"xxx\"");
//		testEvent(EventType.TEXT_UNIT, "\"yyy\"");
//		testEvent(EventType.END_GROUP, null);
		// Line 5, 6
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "\"one,\n \"\"two\"\"\"");
		testEvent(EventType.TEXT_UNIT, "\"xxx\"");
		testEvent(EventType.TEXT_UNIT, "\"yyy\"");
		testEvent(EventType.END_GROUP, null);
		// Line 7, 8
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "\"one\n \"\"two\"\"\"");
		testEvent(EventType.TEXT_UNIT, "\"xxx\"");
		testEvent(EventType.TEXT_UNIT, "\"yyy\"");
		testEvent(EventType.END_GROUP, null);
		// Line 9, 10
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "\"one\n \"\"two\"\"\"");
		testEvent(EventType.TEXT_UNIT, "\"xxx\"");
		testEvent(EventType.TEXT_UNIT, "\"yyy\"");
		testEvent(EventType.END_GROUP, null);
		// Line 11, 12, 13
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "\"one\n \"\"two\"\"\n \"\"three\"\"\"");
		testEvent(EventType.TEXT_UNIT, "\"xxx\"");
		testEvent(EventType.TEXT_UNIT, "\"yyy\"");
		testEvent(EventType.END_GROUP, null);
		// Line 14, 15, 16, 17
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "\"one\n \"\"two\"\"\n \"\"three\"\"\n \"");
		testEvent(EventType.TEXT_UNIT, "\"xxx\"");
		testEvent(EventType.TEXT_UNIT, "\"yyy\"");
		testEvent(EventType.END_GROUP, null);
				
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents6() {
		
		Parameters params = (Parameters) filter.getParameters();
		params.textQualifier = "#";
		
		InputStream input = root.in("/csv_testg3.txt").asInputStream();
		assertNotNull(input);
		
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.removeQualifiers = true;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
			
		// Line 1
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one, two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 2
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 3, 4
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
//		// Line 5
//		testEvent(EventType.START_GROUP, null);
//		testEvent(EventType.TEXT_UNIT, "1");
//		testEvent(EventType.TEXT_UNIT, "one");
//		testEvent(EventType.END_GROUP, null);
//		// Line 6
//		testEvent(EventType.START_GROUP, null);
//		testEvent(EventType.TEXT_UNIT, "two");
//		testEvent(EventType.TEXT_UNIT, "xxx");
//		testEvent(EventType.TEXT_UNIT, "yyy");
//		testEvent(EventType.END_GROUP, null);
		// Line 5, 6
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one,\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 7, 8
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);				
		// Line 9, 10
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 11, 12, 13
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two\n three");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 14, 15, 16, 17
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two\n three");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents7() {
		
		Parameters params = (Parameters) filter.getParameters();
		params.escapingMode = Parameters.ESCAPING_MODE_BACKSLASH;
		
		InputStream input = root.in("/csv_testg4.txt").asInputStream();
		assertNotNull(input);
		
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.removeQualifiers = true;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
			
		// Line 1
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one, two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 2
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 3, 4
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
//		// Line 5
//		testEvent(EventType.START_GROUP, null);
//		testEvent(EventType.TEXT_UNIT, "1");
//		testEvent(EventType.TEXT_UNIT, "one");
//		testEvent(EventType.END_GROUP, null);
//		// Line 6
//		testEvent(EventType.START_GROUP, null);
//		testEvent(EventType.TEXT_UNIT, "two");
//		testEvent(EventType.TEXT_UNIT, "xxx");
//		testEvent(EventType.TEXT_UNIT, "yyy");
//		testEvent(EventType.END_GROUP, null);
		// Line 5, 6
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one,\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 7, 8
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);				
		// Line 9, 10
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 11, 12, 13
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two\n three");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 14, 15, 16, 17
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two\n three");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents8() {
		
		Parameters params = (Parameters) filter.getParameters();
		params.escapingMode = Parameters.ESCAPING_MODE_BACKSLASH;
		params.textQualifier = "@";
		
		InputStream input = root.in("/csv_testg5.txt").asInputStream();
		assertNotNull(input);
		
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.removeQualifiers = true;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
			
		// Line 1
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one, two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 2
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 3, 4
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
//		// Line 5
//		testEvent(EventType.START_GROUP, null);
//		testEvent(EventType.TEXT_UNIT, "1");
//		testEvent(EventType.TEXT_UNIT, "one");
//		testEvent(EventType.END_GROUP, null);
//		// Line 6
//		testEvent(EventType.START_GROUP, null);
//		testEvent(EventType.TEXT_UNIT, "two");
//		testEvent(EventType.TEXT_UNIT, "xxx");
//		testEvent(EventType.TEXT_UNIT, "yyy");
//		testEvent(EventType.END_GROUP, null);
		// Line 5, 6
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one,\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 7, 8
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);				
		// Line 9, 10
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 11, 12, 13
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two\n three");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		// Line 14, 15, 16, 17
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "one\n two\n three");
		testEvent(EventType.TEXT_UNIT, "xxx");
		testEvent(EventType.TEXT_UNIT, "yyy");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents4() {		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv_testh.txt").asInputStream();
		assertNotNull(input);
		
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.removeQualifiers = false;
		params.trimMode = Parameters.TRIM_NONQUALIFIED_ONLY;
		params.wrapMode = WrapMode.NONE;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		// Line 1, 2				
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "\"\"\"eins\"\"\nzwei\"");
		testEvent(EventType.END_GROUP, null);
				
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents5() {		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv_testh.txt").asInputStream();
		assertNotNull(input);
		
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		params.removeQualifiers = false;
		params.trimMode = Parameters.TRIM_NONE;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		// Line 1, 2				
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "1");
		testEvent(EventType.TEXT_UNIT, "\"\"\"eins\"\"\nzwei\"");
		testEvent(EventType.END_GROUP, null);
				
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents106_3() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv3.txt").asInputStream(); // issue 106
		assertNotNull(input);
		
//		params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
//		params.valuesStartLineNum = 2;
//		params.columnNamesLineNum = 1;
//		params.detectColumnsMode = Parameters.DETECT_COLUMNS_COL_NAMES;
		
		params.trimLeading = true;
		params.trimTrailing = false;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "02");
		testEvent(EventType.TEXT_UNIT, "first,\nsecond\n");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		input = root.in("/csv3.txt").asInputStream(); // issue 106
		assertNotNull(input);
		
		params.trimLeading = true;
		params.trimTrailing = true;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "02");
		testEvent(EventType.TEXT_UNIT, "first,\nsecond");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents106_4() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/csv.txt").asInputStream(); // issue 106
		assertNotNull(input);
		
		URL paramsUrl = root.in("/okf_table@copy-of-csv._106.fprm").asUrl();
		assertNotNull(paramsUrl);  
		
		params.load(paramsUrl, false);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		//testEvent(EventType.TEXT_UNIT, "one");
		testEvent(EventType.TEXT_UNIT, "one", "01", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "01,[#$$self$]");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "first,\nsecond\n", "02", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "02,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "three", "03", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "03,[#$$self$]");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents118() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/test2cols.csv").asInputStream(); // issue 118
		assertNotNull(input);
		
		URL paramsUrl = root.in("/okf_table@2Cols_ID_Text.fprm").asUrl();
		assertNotNull(paramsUrl);  
		
		params.load(paramsUrl, false);
		
// DEBUG		
//		params.valuesStartLineNum = 32;
//		params.sendHeaderMode = 0;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		// Line 1
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "814", null, null, null, null);
		testEvent(EventType.TEXT_UNIT, "text", null, null, null, null);
		testEvent(EventType.END_GROUP, null);
		
		// Lines 2, 3
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "works \"\"text\"\" and \"\"text\"\" text\n", "815", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "815,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 4
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "works \"\"text\"\", and \"\"text\"\" text", "815", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "815,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Lines 5, 6
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "does not work \"\"text\"\", and \"\"text\"\" text\n", "815", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "815,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 7
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "text", "816", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "816,[#$$self$]");
		testEvent(EventType.END_GROUP, null);
		
		// Line 8
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "works \"\"text\"\" and \"\"text\"\"", "817", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "817,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Lines 9, 10
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "works \"\"text\"\" and \"\"text\"\"\n", "817", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "817,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
				
		// Line 11
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "works \"\"text\"\", and \"\"text\"\"", "817", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "817,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Lines 12, 13
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "works \"\"text\"\", and \"\"text\"\"\n", "817", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "817,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 14
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "\"\"text\"\" and text", "818", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "818,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 15
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "text, \"\"text\"\" text", "818", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "818,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 16
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "text \"\"text\"\", text", "818", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "818,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Lines 17, 18
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "text \"\"text\"\", text\n", "818", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "818,\"[#$$self$]\", text");
		testEvent(EventType.END_GROUP, null);
		
		// Line 19
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "text \"\"text, text\"\", text", "819", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "819,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Lines 20, 21
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "text \"\"text, text\"\", text\n", "819", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "819,\"[#$$self$]\", text");
		testEvent(EventType.END_GROUP, null);
		
		// Line 22
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "\"\"text", "820", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "820,[#$$self$], text\"\" text, text");
		testEvent(EventType.END_GROUP, null);
		
		// Line 23
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "\"\"text, text\"\"", "821", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "821,\"[#$$self$]\", text, text");
		testEvent(EventType.END_GROUP, null);
		
		// Line 24
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "text \"\"text", "822", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "822, [#$$self$], text\"\" text, text");
		testEvent(EventType.END_GROUP, null);
		
		// Line 25
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "text \"\"text \"\"text,text\"\", text\"\" text", "823", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "823, \"[#$$self$]\", text");
		testEvent(EventType.END_GROUP, null);
		
		// Line 26
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "X <x>xxxxN</x> xxxxN <X>xxxxN</X>, text4.", "999", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "999,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 27
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "A <b>text1</b> text2 <B>text3</B>, text4.", "999", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "999,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 28
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "A <b>text1</b> text2 <B>text3</B>, text4 ", "999", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "999,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 29
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "text \"\"text, text.\"\", text", "819", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "819,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 30
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "text \"\"text, text \"\", text", "819", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "819,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 31
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Text", "111", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "111,[#$$self$]");
		testEvent(EventType.END_GROUP, null);
		
		// Line 32
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, " text1 \"\"text2\"\" text3, text4, text5.", "222", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "222,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 33
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Text", "333", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "333,\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents118_2() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/test2cols2.csv").asInputStream(); // issue 118
		assertNotNull(input);
		
		URL paramsUrl = root.in("/okf_table@2Cols_ID_Text.fprm").asUrl();
		assertNotNull(paramsUrl);  
		
		params.load(paramsUrl, false);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		
		// Line 1
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "814", null, null, null, null);
		testEvent(EventType.TEXT_UNIT, "text", null, null, null, null);
		testEvent(EventType.END_GROUP, null);
		
		// Line 22
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "\"\"text", "820", null, null, null);
		testEvent(EventType.DOCUMENT_PART, "820,[#$$self$], text\"\" text, text");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents96() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/CSVTest_96.txt").asInputStream(); // issue 96
		assertNotNull(input);
		
		params.loadFromResource("/okf_table@copy-of-csv_96.fprm");
		params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Source");
		testEvent(EventType.TEXT_UNIT, "Target");
		testEvent(EventType.TEXT_UNIT, "Data");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Source text 1", "", "Target text 1", locFRFR, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",third column data1");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Source text 2", "", "Target text 2", locFRFR, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",third column data2");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		input = root.in("/CSVTest_96.txt").asInputStream(); // issue 96
		assertNotNull(input);
		
		params.loadFromResource("/okf_table@copy-of-csv_96.fprm");
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Source text 1", "", "Target text 1", locFRFR, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",third column data1");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Source text 2", "", "Target text 2", locFRFR, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",third column data2");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testFileEvents97() {
		
		Parameters params = (Parameters) filter.getParameters();
		
		InputStream input = root.in("/CSVTest_97.txt").asInputStream(); // issue 97
		assertNotNull(input);
		
		params.loadFromResource("/okf_table@copy-of-csv_97.fprm");
		params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Source");
		testEvent(EventType.TEXT_UNIT, "Target");
		testEvent(EventType.TEXT_UNIT, "Data");
		testEvent(EventType.TEXT_UNIT, "Source");
		testEvent(EventType.TEXT_UNIT, "Target");		
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Source text 1", "", "Target text 1", locFRFR, "");
		testEvent(EventType.TEXT_UNIT, "SourceB1", "", "TargetB1", locFRCA, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",third column data1,\"[#$$self$]\",\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Source text 2", "", "Target text 2", locFRFR, "");
		testEvent(EventType.TEXT_UNIT, "SourceB2", "", "TargetB2", locFRCA, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",third column data2,\"[#$$self$]\",\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
		
		
		input = root.in("/CSVTest_97.txt").asInputStream(); // issue 97
		assertNotNull(input);
		
		params.loadFromResource("/okf_table@copy-of-csv_97.fprm");
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
						
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Source text 1", "", "Target text 1", locFRFR, "");
		testEvent(EventType.TEXT_UNIT, "SourceB1", "", "TargetB1", locFRCA, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",third column data1,\"[#$$self$]\",\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Source text 2", "", "Target text 2", locFRFR, "");
		testEvent(EventType.TEXT_UNIT, "SourceB2", "", "TargetB2", locFRCA, "");
		testEvent(EventType.DOCUMENT_PART, "\"[#$$self$]\",\"[#$$self$]\",third column data2,\"[#$$self$]\",\"[#$$self$]\"");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		
		filter.close();
	}
	
	@Test
	public void testQualifiedValues() {
				
		//_getParameters().detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		//_getParameters().compoundTuDelimiter = "\n";
		
		Parameters params = (Parameters) filter.getParameters();
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		
		InputStream input = root.in("/csv_test3.txt").asInputStream();
		assertNotNull(input);
		
		params.wrapMode = WrapMode.NONE; // !!!
		params.removeQualifiers = true;
		
//		params.valuesStartLineNum = 9;
//		params.sendHeaderMode = 0;

		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		
		// Line 1
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11");
		testEvent(EventType.TEXT_UNIT, "Value12");
		testEvent(EventType.TEXT_UNIT, "Value13");
		testEvent(EventType.TEXT_UNIT, "Value14");		
		testEvent(EventType.END_GROUP, null);
		
		// Line 2
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21");
		testEvent(EventType.TEXT_UNIT, "Value22.1,Value22.2, Value22.3");
		testEvent(EventType.TEXT_UNIT, "Value23");
		testEvent(EventType.TEXT_UNIT, "Value24");		
		testEvent(EventType.END_GROUP, null);
				
		// Line 4-7
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value31");
		testEvent(EventType.TEXT_UNIT, "Value32");
		testEvent(EventType.TEXT_UNIT, "Value33");
		testEvent(EventType.TEXT_UNIT, "Value34.1\nValue34.2\nValue34.3\nValue34.4,Value34.5");
		testEvent(EventType.TEXT_UNIT, "Value35");
		testEvent(EventType.END_GROUP, null);
		
		// Line 9-12
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value41");
		testEvent(EventType.TEXT_UNIT, "Value42");
		testEvent(EventType.TEXT_UNIT, "Value43");
		testEvent(EventType.TEXT_UNIT, "Value44.1\nValue44.2\nValue44.3\nValue44.4");
		testEvent(EventType.TEXT_UNIT, "Value45.1,Value45.2");
		testEvent(EventType.END_GROUP, null);		
		
		// Line 14-18
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value51");
		testEvent(EventType.TEXT_UNIT, "Value52");
		testEvent(EventType.TEXT_UNIT, "Value53");
		testEvent(EventType.TEXT_UNIT, "Value54.1\nValue54.2\nValue54.3\nValue54.4,Value55.1,Value55.2\nValue55.3,Value55.4");
		testEvent(EventType.END_GROUP, null);
		
		// Line 20-25
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value61");
		testEvent(EventType.TEXT_UNIT, "Value62");
		testEvent(EventType.TEXT_UNIT, "Value63");
		testEvent(EventType.TEXT_UNIT, "Value64.1\nValue64.2\nValue64.3\nValue64.4,Value65.1,Value65.2\nValue65.3,Value65.4\n" +
				"Value65.5,Value66");
		testEvent(EventType.END_GROUP, null);		
		// -------------------------------------------------
		
		// Line 27-31
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value71");
		testEvent(EventType.TEXT_UNIT, "Value72 aaa quoted part 1, then quoted part 2 value,Value73,Value74\n" +
				"Value81,Value82 with unclosed quote\nValue91,Value92\nValueA1,ValueA2\nValueB1");
		testEvent(EventType.TEXT_UNIT, "ValueB2,ValueB3");		// If quotation marks are not around field, preserve them 
		testEvent(EventType.TEXT_UNIT, "ValueB4");
		testEvent(EventType.END_GROUP, null);
		
		// Line 32
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "ValueC1");
		testEvent(EventType.TEXT_UNIT, "ValueC2");		 
		testEvent(EventType.TEXT_UNIT, "ValueC3");			
		testEvent(EventType.TEXT_UNIT, "ValueC4");
		testEvent(EventType.TEXT_UNIT, "ValueC5");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		filter.close();
		
		
		// Unwrap lines
		input = root.in("/csv_test3.txt").asInputStream();
		assertNotNull(input);
		
		params.wrapMode = WrapMode.SPACES; // !!!
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		
		// Line 1
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11");
		testEvent(EventType.TEXT_UNIT, "Value12");
		testEvent(EventType.TEXT_UNIT, "Value13");
		testEvent(EventType.TEXT_UNIT, "Value14");		
		testEvent(EventType.END_GROUP, null);
		
		// Line 2
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21");
		testEvent(EventType.TEXT_UNIT, "Value22.1,Value22.2, Value22.3");
		testEvent(EventType.TEXT_UNIT, "Value23");
		testEvent(EventType.TEXT_UNIT, "Value24");		
		testEvent(EventType.END_GROUP, null);
				
		// Line 4-7
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value31");
		testEvent(EventType.TEXT_UNIT, "Value32");
		testEvent(EventType.TEXT_UNIT, "Value33");
		testEvent(EventType.TEXT_UNIT, "Value34.1 Value34.2 Value34.3 Value34.4,Value34.5");
		testEvent(EventType.TEXT_UNIT, "Value35");
		testEvent(EventType.END_GROUP, null);
		
		// Line 9-12
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value41");
		testEvent(EventType.TEXT_UNIT, "Value42");
		testEvent(EventType.TEXT_UNIT, "Value43");
		testEvent(EventType.TEXT_UNIT, "Value44.1 Value44.2 Value44.3 Value44.4");
		testEvent(EventType.TEXT_UNIT, "Value45.1,Value45.2");
		testEvent(EventType.END_GROUP, null);		
		
		// Line 14-18
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value51");
		testEvent(EventType.TEXT_UNIT, "Value52");
		testEvent(EventType.TEXT_UNIT, "Value53");
		testEvent(EventType.TEXT_UNIT, "Value54.1 Value54.2 Value54.3 Value54.4,Value55.1,Value55.2 Value55.3,Value55.4");
		testEvent(EventType.END_GROUP, null);
		
		// Line 20
		// Line 20-25
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value61");
		testEvent(EventType.TEXT_UNIT, "Value62");
		testEvent(EventType.TEXT_UNIT, "Value63");
		testEvent(EventType.TEXT_UNIT, "Value64.1 Value64.2 Value64.3 Value64.4,Value65.1,Value65.2 Value65.3,Value65.4 " +
				"Value65.5,Value66");
		testEvent(EventType.END_GROUP, null);
		// -------------------------------------------------
		
		// Line 27-31
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value71");
		testEvent(EventType.TEXT_UNIT, "Value72 aaa quoted part 1, then quoted part 2 value,Value73,Value74 " +
				"Value81,Value82 with unclosed quote Value91,Value92 ValueA1,ValueA2 ValueB1");
		testEvent(EventType.TEXT_UNIT, "ValueB2,ValueB3");		// If quotation marks are not around field, preserve them 
		testEvent(EventType.TEXT_UNIT, "ValueB4");
		testEvent(EventType.END_GROUP, null);
		
		// Line 32
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "ValueC1");
		testEvent(EventType.TEXT_UNIT, "ValueC2");		 
		testEvent(EventType.TEXT_UNIT, "ValueC3");			
		testEvent(EventType.TEXT_UNIT, "ValueC4");
		testEvent(EventType.TEXT_UNIT, "ValueC5");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		filter.close();
	}

	@Test
	public void testQualifiedValues2() {
				
		//_getParameters().detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		//_getParameters().compoundTuDelimiter = "\n";
		
		Parameters params = (Parameters) filter.getParameters();
		params.detectColumnsMode = Parameters.DETECT_COLUMNS_NONE;
		
		InputStream input = root.in("/csv_test3.txt").asInputStream();
		assertNotNull(input);
		
		params.wrapMode = WrapMode.NONE; // !!!
		params.removeQualifiers = false;
		
//		params.valuesStartLineNum = 9;
//		params.sendHeaderMode = 0;

		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		
		// Line 1
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11");
		testEvent(EventType.TEXT_UNIT, "\"Value12\"");
		testEvent(EventType.TEXT_UNIT, "Value13");
		testEvent(EventType.TEXT_UNIT, "Value14");		
		testEvent(EventType.END_GROUP, null);
		
		// Line 2
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21");
		testEvent(EventType.TEXT_UNIT, "\"Value22.1,Value22.2, Value22.3\"");
		testEvent(EventType.TEXT_UNIT, "Value23");
		testEvent(EventType.TEXT_UNIT, "Value24");		
		testEvent(EventType.END_GROUP, null);
				
		// Line 4-7
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value31");
		testEvent(EventType.TEXT_UNIT, "Value32");
		testEvent(EventType.TEXT_UNIT, "Value33");
		testEvent(EventType.TEXT_UNIT, "\"Value34.1\nValue34.2\nValue34.3\nValue34.4,Value34.5\"");
		testEvent(EventType.TEXT_UNIT, "Value35");
		testEvent(EventType.END_GROUP, null);
		
		// Line 9-12
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value41");
		testEvent(EventType.TEXT_UNIT, "Value42");
		testEvent(EventType.TEXT_UNIT, "Value43");
		testEvent(EventType.TEXT_UNIT, "\"Value44.1\nValue44.2\nValue44.3\nValue44.4\"");
		testEvent(EventType.TEXT_UNIT, "\"Value45.1,Value45.2\"");
		testEvent(EventType.END_GROUP, null);		
		
		// Line 14-18
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value51");
		testEvent(EventType.TEXT_UNIT, "Value52");
		testEvent(EventType.TEXT_UNIT, "Value53");
		testEvent(EventType.TEXT_UNIT, "\"Value54.1\nValue54.2\nValue54.3\nValue54.4,\"\"Value55.1,Value55.2\nValue55.3,Value55.4\"");
		testEvent(EventType.END_GROUP, null);
		
//		// Line 15
//		testEvent(EventType.START_GROUP, null);		
//		testEvent(EventType.TEXT_UNIT, "Value54.2");
//		testEvent(EventType.END_GROUP, null);
//		
//		// Line 16
//		testEvent(EventType.START_GROUP, null);		
//		testEvent(EventType.TEXT_UNIT, "Value54.3");
//		testEvent(EventType.END_GROUP, null);
//		
//		// Line 17-18
//		testEvent(EventType.START_GROUP, null);		
//		testEvent(EventType.TEXT_UNIT, "Value54.4");
//		testEvent(EventType.TEXT_UNIT, "\"Value55.1,Value55.2\nValue55.3,Value55.4\"");
//		testEvent(EventType.END_GROUP, null);
		
		// Line 20-25
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value61");
		testEvent(EventType.TEXT_UNIT, "Value62");
		testEvent(EventType.TEXT_UNIT, "Value63");
		testEvent(EventType.TEXT_UNIT, "\"Value64.1\nValue64.2\nValue64.3\nValue64.4,\"\"Value65.1,Value65.2\nValue65.3,Value65.4\n" +
				"Value65.5,\"\"Value66\"\"\"");
		testEvent(EventType.END_GROUP, null);		
		// -------------------------------------------------
		
		// Line 27-31
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value71");
		testEvent(EventType.TEXT_UNIT, "\"Value72 \"\"aaa \"\"quoted part 1\"\", then \"\"quoted part 2\"\" value\"\"\"\",Value73\"\",Value74\n" +
				"Value81,\"\"Value82 with unclosed quote\nValue91,Value92\n\"\"ValueA1\"\",ValueA2\"\"\"\"\nValueB1\"");
		testEvent(EventType.TEXT_UNIT, "\"Value\"\"B2,Va\"\"lueB3\"");		// If quotation marks are not around field, preserve them 
		testEvent(EventType.TEXT_UNIT, "Va\"\"lueB4\"\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 32
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "ValueC1");
		testEvent(EventType.TEXT_UNIT, "\"\"ValueC2");		 
		testEvent(EventType.TEXT_UNIT, "ValueC3\"\"");			
		testEvent(EventType.TEXT_UNIT, "\"\"ValueC4\"\"");
		testEvent(EventType.TEXT_UNIT, "ValueC5");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		filter.close();
		
		
		// Unwrap lines
		input = root.in("/csv_test3.txt").asInputStream();
		assertNotNull(input);
		
		params.wrapMode = WrapMode.SPACES; // !!!
		filter.open(new RawDocument(input, "UTF-8", locEN));
		
		testEvent(EventType.START_DOCUMENT, null);
		
		// Line 1
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value11");
		testEvent(EventType.TEXT_UNIT, "\"Value12\"");
		testEvent(EventType.TEXT_UNIT, "Value13");
		testEvent(EventType.TEXT_UNIT, "Value14");		
		testEvent(EventType.END_GROUP, null);
		
		// Line 2
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value21");
		testEvent(EventType.TEXT_UNIT, "\"Value22.1,Value22.2, Value22.3\"");
		testEvent(EventType.TEXT_UNIT, "Value23");
		testEvent(EventType.TEXT_UNIT, "Value24");		
		testEvent(EventType.END_GROUP, null);
				
		// Line 4-7
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value31");
		testEvent(EventType.TEXT_UNIT, "Value32");
		testEvent(EventType.TEXT_UNIT, "Value33");
		testEvent(EventType.TEXT_UNIT, "\"Value34.1 Value34.2 Value34.3 Value34.4,Value34.5\"");
		testEvent(EventType.TEXT_UNIT, "Value35");
		testEvent(EventType.END_GROUP, null);
		
		// Line 9-12
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value41");
		testEvent(EventType.TEXT_UNIT, "Value42");
		testEvent(EventType.TEXT_UNIT, "Value43");
		testEvent(EventType.TEXT_UNIT, "\"Value44.1 Value44.2 Value44.3 Value44.4\"");
		testEvent(EventType.TEXT_UNIT, "\"Value45.1,Value45.2\"");
		testEvent(EventType.END_GROUP, null);		
		
		// Line 14-18
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value51");
		testEvent(EventType.TEXT_UNIT, "Value52");
		testEvent(EventType.TEXT_UNIT, "Value53");
		testEvent(EventType.TEXT_UNIT, "\"Value54.1 Value54.2 Value54.3 Value54.4,\"\"Value55.1,Value55.2 Value55.3,Value55.4\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 20
		// Line 20-25
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "Value61");
		testEvent(EventType.TEXT_UNIT, "Value62");
		testEvent(EventType.TEXT_UNIT, "Value63");
		testEvent(EventType.TEXT_UNIT, "\"Value64.1 Value64.2 Value64.3 Value64.4,\"\"Value65.1,Value65.2 Value65.3,Value65.4 " +
				"Value65.5,\"\"Value66\"\"\"");
		testEvent(EventType.END_GROUP, null);
		// -------------------------------------------------
		
		// Line 27-31
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "Value71");
		testEvent(EventType.TEXT_UNIT, "\"Value72 \"\"aaa \"\"quoted part 1\"\", then \"\"quoted part 2\"\" value\"\"\"\",Value73\"\",Value74 " +
				"Value81,\"\"Value82 with unclosed quote Value91,Value92 \"\"ValueA1\"\",ValueA2\"\"\"\" ValueB1\"");
		testEvent(EventType.TEXT_UNIT, "\"Value\"\"B2,Va\"\"lueB3\"");		// If quotation marks are not around field, preserve them 
		testEvent(EventType.TEXT_UNIT, "Va\"\"lueB4\"\"");
		testEvent(EventType.END_GROUP, null);
		
		// Line 32
		testEvent(EventType.START_GROUP, null);		
		testEvent(EventType.TEXT_UNIT, "ValueC1");
		testEvent(EventType.TEXT_UNIT, "\"\"ValueC2");		 
		testEvent(EventType.TEXT_UNIT, "ValueC3\"\"");			
		testEvent(EventType.TEXT_UNIT, "\"\"ValueC4\"\"");
		testEvent(EventType.TEXT_UNIT, "ValueC5");
		testEvent(EventType.END_GROUP, null);
		
		testEvent(EventType.END_DOCUMENT, null);
		filter.close();
	}
	
	@Test
	public void testDoubleExtraction () {
		// Read all files in the data directory
//		URL url = root.in("/csv_test1.txt").asUrl();
//		String root = Util.getDirectoryName(url.getPath());
//		root = Util.getDirectoryName(root) + "/data/";
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		
		list.add(new InputDocument(root.in("/csv_test1.txt").toString(), ""));
		list.add(new InputDocument(root.in("/csv_test2.txt").toString(), ""));
		list.add(new InputDocument(root.in("/csv_testc.txt").toString(), ""));
		list.add(new InputDocument(root.in("/csv_test3.txt").toString(), ""));
		list.add(new InputDocument(root.in("/csv.txt").toString(), ""));
		list.add(new InputDocument(root.in("/csv2.txt").toString(), ""));
		list.add(new InputDocument(root.in("/csv3.txt").toString(), ""));
		list.add(new InputDocument(root.in("/CSVTest_96.txt").toString(), ""));
		list.add(new InputDocument(root.in("/CSVTest_97.txt").toString(), ""));
		list.add(new InputDocument(root.in("/CSVTesting01.csv").toString(), ""));
		list.add(new InputDocument(root.in("/testContent_escaped.csv").toString(), ""));
		list.add(new InputDocument(root.in("/testContent_escaped2.csv").toString(), ""));
		list.add(new InputDocument(root.in("/testContent_escaped4.csv").toString(), ""));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR, "out"));
	}
	
	@Test
	public void testRecordId() {
		IParameters params = filter.getParameters();

		params.load(root.in("/okf_table@record_id.fprm").asUrl(), false);
		
		InputStream input = root.in("/csv_testd.txt").asInputStream();
		assertNotNull(input);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row1", "00001_src1", "target1 row1", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row1", "00001_src2", "target2 row1", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "00001, [#$$self$], [#$$self$], [#$$self$], [#$$self$]");
		testEvent(EventType.END_GROUP, null);
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row2", "00002_src1", "target1 row2", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row2", "00002_src2", "target2 row2", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "00002, [#$$self$], [#$$self$], [#$$self$], [#$$self$]");
		testEvent(EventType.END_GROUP, null);
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row3", "00003_src1", "target1 row3", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row3", "00003_src2", "target2 row3", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "00003, [#$$self$], [#$$self$], [#$$self$], [#$$self$]");
		testEvent(EventType.END_GROUP, null);
		filter.close();
	}
	
	@Test
	public void testSourceId() {
		IParameters params = filter.getParameters();

		params.load(root.in("/okf_table@source_id.fprm").asUrl(), false);
		
		InputStream input = root.in("/csv_teste.txt").asInputStream();
		assertNotNull(input);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row1", "source1 ID row1", "target1 row1", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row1", "source2 ID row1", "target2 row1", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "[#$$self$], [#$$self$], [#$$self$], [#$$self$], source2 ID row1, source1 ID row1");
		testEvent(EventType.END_GROUP, null);
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row2", "source1 ID row2", "target1 row2", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row2", "source2 ID row2", "target2 row2", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "[#$$self$], [#$$self$], [#$$self$], [#$$self$], source2 ID row2, source1 ID row2");
		testEvent(EventType.END_GROUP, null);
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row3", "source1 ID row3", "target1 row3", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row3", "source2 ID row3", "target2 row3", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "[#$$self$], [#$$self$], [#$$self$], [#$$self$], source2 ID row3, source1 ID row3");
		testEvent(EventType.END_GROUP, null);
		filter.close();
	}
	
	@Test
	public void testEmptySourceId() {
		// 1. No suffix
		IParameters params = filter.getParameters();

		params.load(root.in("/okf_table@record_id2.fprm").asUrl(), false);
		
		InputStream input = root.in("/csv_testf.txt").asInputStream();
		assertNotNull(input);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row1", "source1 ID row1", "target1 row1", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row1", "source2 ID row1", "target2 row1", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "00001, [#$$self$], [#$$self$], [#$$self$], source1 ID row1, [#$$self$], source2 ID row1");
		testEvent(EventType.END_GROUP, null);
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row2", "source1 ID row2", "target1 row2", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row2", "00002", "target2 row2", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "00002, [#$$self$], [#$$self$], [#$$self$], source1 ID row2, [#$$self$]");
		testEvent(EventType.END_GROUP, null);
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row3", "00003", "target1 row3", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row3", "source2 ID row3", "target2 row3", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "00003, [#$$self$], [#$$self$], [#$$self$],, [#$$self$], source2 ID row3");
		testEvent(EventType.END_GROUP, null);
		filter.close();
		
		// 2. With suffix
		params.load(root.in("/okf_table@record_id3.fprm").asUrl(), false);

		input = root.in("/csv_testf.txt").asInputStream();
		assertNotNull(input);
		
		filter.open(new RawDocument(input, "UTF-8", locEN));
		testEvent(EventType.START_DOCUMENT, null);		
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row1", "source1 ID row1", "target1 row1", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row1", "source2 ID row1", "target2 row1", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "00001, [#$$self$], [#$$self$], [#$$self$], source1 ID row1, [#$$self$], source2 ID row1");
		testEvent(EventType.END_GROUP, null);
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row2", "source1 ID row2", "target1 row2", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row2", "00002_tu2", "target2 row2", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "00002, [#$$self$], [#$$self$], [#$$self$], source1 ID row2, [#$$self$]");
		testEvent(EventType.END_GROUP, null);
		testEvent(EventType.START_GROUP, null);
		testEvent(EventType.TEXT_UNIT, "source1 row3", "00003_tu1", "target1 row3", locEN, null);
		testEvent(EventType.TEXT_UNIT, "source2 row3", "source2 ID row3", "target2 row3", locEN, null);
		testEvent(EventType.DOCUMENT_PART, "00003, [#$$self$], [#$$self$], [#$$self$],, [#$$self$], source2 ID row3");
		testEvent(EventType.END_GROUP, null);
		filter.close();
	}
	
	@Test
	public void testTabDelimited2Column ()
		throws IOException
	{
		// Sets the configuration
		IParameters params = filter.getParameters();
		params.load(root.in("/okf_table@table_src_tab_trg.fprm").asUrl(), false);
		
		// Get the input file content
		String snippet = streamAsString(root.in("/test_tsv_simple.txt").asInputStream());
		assertNotNull(snippet);
		
		// Get the list of events
		List<Event> events = getEvents(snippet, locEN, locFRFR);

		// Test row with source, tab and target
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("Source1", tu.getSource().getCodedText());
		assertEquals("Target1", tu.getTarget(locFRFR).getCodedText());
		
		// Test row with source tab and no target
		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu);
		assertEquals("Source2", tu.getSource().getCodedText());
		assertFalse(tu.hasTarget(locFRFR));

		// Test row with source column only (no tab)
		tu = FilterTestDriver.getTextUnit(events, 3);
		assertNotNull(tu);
		assertEquals("Source3", tu.getSource().getCodedText());
		assertFalse(tu.hasTarget(locFRFR));
		
		// Test row with tab then target and comment
		tu = FilterTestDriver.getTextUnit(events, 4);
		assertNotNull(tu);
		assertEquals("Source4", tu.getSource().getCodedText());
		assertEquals("Target4", tu.getTarget(locFRFR).getCodedText());
		assertEquals("Comment4", tu.getProperty(Property.NOTE).getValue());
	}
	
	@Test
	public void testTabDelimited2ColumnRoundTrip ()
		throws IOException
	{
		// Sets the configuration
		IParameters params = filter.getParameters();
		params.load(root.in("/okf_table@table_src_tab_trg.fprm").asUrl(), false);
		
		// Get the input file content
		String snippet = streamAsString(root.in("/test_tsv_simple.txt").asInputStream());
		
		// Do a round-trip test
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFRFR),
			filter.getEncoderManager(), locFRFR);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testEmptyLinesInCell() {
		String snippet = "cell1,cell2,\"cell3\n\nis\n\nlong\",cell4\n"
				+ "cell1,\"cell2\n\nis\n\nalso\n\nlong\",cell3,cell4\n";

		// Set the parameters
		Parameters params = new Parameters();
		params.fieldDelimiter = ",";
		params.textQualifier = "\"";
		params.preserveWS = true;
		filter.setParameters(params);
		
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFRCA),
			filter.getEncoderManager(), locFRCA);
		assertEquals(snippet, result);
	}
	
	@Test
	public void testAddTextQualifiers() {
		String snippet = "cell1,cell2,cell3";
		String expected = "cell1,\"cell2, yo!\",cell3";

		// Set the parameters
		Parameters params = new Parameters();
		params.fieldDelimiter = ",";
		params.textQualifier = "\"";
		params.addQualifiers = true;
		filter.setParameters(params);
		
		List<Event> events = getEvents(snippet, locEN, locFRCA);
		ITextUnit tu = (ITextUnit) events.get(2).getResource();
		tu.setTarget(locFRCA, new TextContainer("cell2, yo!"));
		String result = FilterTestDriver.generateOutput(events,
			locFRCA, filter.createSkeletonWriter(), filter.getEncoderManager());
		assertEquals(expected, result);
	}

	@Test
	public void testTrgAtCol4_Issue511 () {
		// See issue https://bitbucket.org/okapiframework/okapi/issues/511
		String snippet = "\"file\"\t\"id\"\t\"src\"\t\"trg\"\n"
			+ "\"f1\"\t\"i1\"\t\"src1\"\t\"\"\n"
			+ "\"f2\"\t\"i2\"\t\"src2\"\t\"ori-trg\"\n";
		String expected = "\"file\"\t\"id\"\t\"src\"\t\"trg\"\n"
			+ "\"f1\"\t\"i1\"\t\"src1\"\t\"trg for 1\"\n"
			+ "\"f2\"\t\"i2\"\t\"src2\"\t\"trg for 2\"\n";
		// Set the parameters
		Parameters params = new Parameters();
		params.load(root.in("/okf_table@tabtest1.fprm").asUrl(), false);
		filter.setParameters(params);
		
		List<Event> events = FilterTestDriver.getEvents(filter, snippet, locEN, locFRCA);
		int i = 1;
		for ( Event event : events ) {
			if ( event.getEventType() != EventType.TEXT_UNIT ) continue;
			ITextUnit tu = event.getTextUnit();
			assertEquals("src"+i, tu.getSource().toString());
			if ( tu.hasTarget(locFRCA) ) {
				if (i == 2)
					assertEquals("ori-trg", tu.getTarget(locFRCA).toString());
			}
			tu.setTarget(locFRCA, new TextContainer("trg for "+i));
			i++;
		}
		
		ISkeletonWriter skelWriter = filter.createSkeletonWriter();
		String result = FilterTestDriver.generateOutput(events,
			locFRCA, skelWriter, filter.getEncoderManager());
		LOGGER.info(result);
		assertEquals(expected, result);
	}
	
	// Helpers
	private String getFullFileName(String fileName) {
//		URL url = root.in("/csv_testb.txt").asUrl();
//		String root = Util.getDirectoryName(url.getPath());
//		root = Util.getDirectoryName(root) + "/data/";
		return root.in(fileName).toString();
	}
	
	private Event testEvent(EventType expectedType, String expectedText) {
		assertNotNull(filter);
		
		Event event = filter.next();		
		assertNotNull(event);
		
		assertTrue(event.getEventType() == expectedType);
		
		switch (event.getEventType()) {
		case TEXT_UNIT:
			IResource res = event.getResource();
			assertTrue(res instanceof ITextUnit);
			// assertEquals(expectedText, ((TextUnit) res).toString());
			assertEquals(expectedText, TextUnitUtil.getSourceText((ITextUnit)res, true));
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
		
		return event;
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
			if (!Util.isEmpty(expName)) {
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

	private ArrayList<Event> getEvents (String snippet,
		LocaleId srcLang,
		LocaleId trgLang)
	{
		return FilterTestDriver.getEvents(filter, snippet, srcLang, trgLang);
	}	
}
