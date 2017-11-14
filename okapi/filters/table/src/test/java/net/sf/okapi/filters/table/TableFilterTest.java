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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.filters.table.base.BaseTableFilter;
import net.sf.okapi.filters.table.base.Parameters;
import net.sf.okapi.filters.table.csv.CommaSeparatedValuesFilter;
import net.sf.okapi.filters.table.fwc.FixedWidthColumnsFilter;
import net.sf.okapi.filters.table.tsv.TabSeparatedValuesFilter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class TableFilterTest {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private TableFilter filter;
    private FilterTestDriver testDriver;
    private FileLocation root;
    private LocaleId locEN = LocaleId.fromString("en");
    private LocaleId locFR = LocaleId.fromString("fr");

    @Before
    public void setUp() {
        filter = new TableFilter();
        assertNotNull(filter);
        testDriver = new FilterTestDriver();
        assertNotNull(testDriver);
        testDriver.setDisplayLevel(0);
        testDriver.setShowSkeleton(true);
        root = FileLocation.fromClass(this.getClass());

        Parameters params = (Parameters) filter.getActiveParameters();
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
        } catch (IllegalArgumentException e) {
        } finally {
            filter.close();
        }

        // Empty URI, OkapiBadFilterInputException expected, no other
        URI uri = null;
        try {
            filter.open(new RawDocument(uri, "UTF-8", locEN));
            fail("IllegalArgumentException should've been trown");
        } catch (IllegalArgumentException e) {
        } finally {
            filter.close();
        }

        // Empty char seq, OkapiBadFilterInputException expected, no other
        String st = null;
        try {
            filter.open(new RawDocument(st, locEN, locEN));
            fail("IllegalArgumentException should've been trown");
        } catch (IllegalArgumentException e) {
        } finally {
            filter.close();
        }

        // Empty raw doc, open(RawDocument), OkapiBadFilterInputException
        // expected, no other
        try {
            filter.open(null);
            fail("OkapiBadFilterInputException should've been trown");
        } catch (OkapiBadFilterInputException e) {
        } finally {
            filter.close();
        }

        // Empty raw doc, open(RawDocument, boolean),
        // OkapiBadFilterInputException expected, no other
        try {
            filter.open(null, true);
            fail("OkapiBadFilterInputException should've been trown");
        } catch (OkapiBadFilterInputException e) {
        } finally {
            filter.close();
        }

        // Empty filter parameters, OkapiBadFilterParametersException expected
        filter.setParameters(null);

        InputStream input2 = root.in("/csv_test1.txt").asInputStream();
        try {
            filter.open(new RawDocument(input2, "UTF-8", locEN));
            fail("OkapiBadFilterParametersException should've been trown");
        } catch (OkapiBadFilterParametersException e) {
        } finally {
            filter.close();
        }
    }

    @Test
    public void testNameAndMimeType() {
        assertEquals(filter.getMimeType(), "text/csv");
        assertEquals(filter.getName(), "okf_table");

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
                assertEquals(((ITextUnit) res).getMimeType(), filter.getMimeType());
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
        if (!testDriver.process(filter)) Assert.fail();
        filter.close();
    }

    @Test
    public void testFileEvents2() {
        testDriver.setDisplayLevel(0);

        filter.setConfiguration(TabSeparatedValuesFilter.FILTER_CONFIG);
        InputStream input = root.in("/TSV_test.txt").asInputStream();
        assertNotNull(input);

        net.sf.okapi.filters.table.tsv.Parameters params = (net.sf.okapi.filters.table.tsv.Parameters) filter
                .getActiveParameters();

        params.sendHeaderMode = Parameters.SEND_HEADER_ALL;
        params.valuesStartLineNum = 2;
        params.columnNamesLineNum = 1;
        params.detectColumnsMode = Parameters.DETECT_COLUMNS_COL_NAMES;

        filter.open(new RawDocument(input, "UTF-8", locEN));

        testEvent(EventType.START_DOCUMENT, null);

        testEvent(EventType.START_GROUP, null);
        testEvent(EventType.TEXT_UNIT, "Source");
        testEvent(EventType.TEXT_UNIT, "Target");
        testEvent(EventType.END_GROUP, null);

        testEvent(EventType.START_GROUP, null);
        testEvent(EventType.TEXT_UNIT, "Source text 1");
        testEvent(EventType.TEXT_UNIT, "Target text 1");
        testEvent(EventType.END_GROUP, null);

        testEvent(EventType.START_GROUP, null);
        testEvent(EventType.TEXT_UNIT, "Source text 2");
        testEvent(EventType.TEXT_UNIT, "Target text 2");
        testEvent(EventType.END_GROUP, null);

        testEvent(EventType.END_DOCUMENT, null);

        filter.close();

        // List events
        String filename = "csv_test1.txt";
        input = root.in("/" + filename).asInputStream();
        assertNotNull(input);

        LOGGER.trace(filename);
        filter.open(new RawDocument(input, "UTF-8", locEN));
        if (!testDriver.process(filter)) Assert.fail();
        filter.close();
    }
    
    @Test
    public void testColumnDefinedLocales() throws URISyntaxException {
        testDriver.setDisplayLevel(0);
        
        net.sf.okapi.filters.table.Parameters params = (net.sf.okapi.filters.table.Parameters) filter
                .getParameters();

        URL paramsUrl = root.in("/okf_table@defined_locales.fprm").asUrl();
        assertNotNull(paramsUrl);
       
        params.load(paramsUrl, false);

        filter.setParameters(params);
        InputStream input = root.in("/Locale_defined_TSV_test.txt").asInputStream();
        assertNotNull(input);

        filter.open(new RawDocument(input, "UTF-8", LocaleId.EMPTY, LocaleId.EMPTY));
        
        testEvent(EventType.START_DOCUMENT, null);
        Event e = testEvent(EventType.PIPELINE_PARAMETERS, null);
        assertEquals(LocaleId.ENGLISH.toString(), e.getPipelineParameters().getSourceLocale().toString());
        assertEquals(LocaleId.FRENCH.toString(), e.getPipelineParameters().getTargetLocale().toString());
        
        testEvent(EventType.START_GROUP, null);
        e = testEvent(EventType.TEXT_UNIT, "Source text 1");;
        assertTrue(e.getTextUnit().hasTarget(LocaleId.FRENCH));
        
        testEvent(EventType.DOCUMENT_PART, null);
        testEvent(EventType.END_GROUP, null);

        testEvent(EventType.START_GROUP, null);
        e = testEvent(EventType.TEXT_UNIT, "Source text 2");
        assertTrue(e.getTextUnit().hasTarget(LocaleId.FRENCH));
        
        testEvent(EventType.DOCUMENT_PART, null);
        testEvent(EventType.END_GROUP, null);

        testEvent(EventType.END_DOCUMENT, null);

        filter.close();
    }

    @Test
    public void testColumnDefinedSource() throws URISyntaxException {
        testDriver.setDisplayLevel(0);
        
        net.sf.okapi.filters.table.Parameters params = (net.sf.okapi.filters.table.Parameters) filter
                .getParameters();

        URL paramsUrl = root.in("/okf_table@defined_locales.fprm").asUrl();
        assertNotNull(paramsUrl);
       
        params.load(paramsUrl, false);

        filter.setParameters(params);
        InputStream input = root.in("/Locale_defined_TSV_test.txt").asInputStream();
        assertNotNull(input);

        filter.open(new RawDocument(input, "UTF-8", LocaleId.EMPTY, LocaleId.SPANISH));
        
        testEvent(EventType.START_DOCUMENT, null);
        Event e = testEvent(EventType.PIPELINE_PARAMETERS, null);
        assertEquals(LocaleId.ENGLISH.toString(), e.getPipelineParameters().getSourceLocale().toString());
        assertEquals(LocaleId.SPANISH.toString(), e.getPipelineParameters().getTargetLocale().toString());
        
        testEvent(EventType.START_GROUP, null);
        e = testEvent(EventType.TEXT_UNIT, "Source text 1");;
        assertTrue(e.getTextUnit().hasTarget(LocaleId.SPANISH));
        
        testEvent(EventType.DOCUMENT_PART, null);
        testEvent(EventType.END_GROUP, null);

        testEvent(EventType.START_GROUP, null);
        e = testEvent(EventType.TEXT_UNIT, "Source text 2");
        assertTrue(e.getTextUnit().hasTarget(LocaleId.SPANISH));
        
        testEvent(EventType.DOCUMENT_PART, null);
        testEvent(EventType.END_GROUP, null);

        testEvent(EventType.END_DOCUMENT, null);

        filter.close();
    }

    @Test
    public void testColumnDefinedTarget() throws URISyntaxException {
        testDriver.setDisplayLevel(0);
        
        net.sf.okapi.filters.table.Parameters params = (net.sf.okapi.filters.table.Parameters) filter
                .getParameters();

        URL paramsUrl = root.in("/okf_table@defined_locales.fprm").asUrl();
        assertNotNull(paramsUrl);
       
        params.load(paramsUrl, false);

        filter.setParameters(params);
        InputStream input = root.in("/Locale_defined_TSV_test.txt").asInputStream();
        assertNotNull(input);

        filter.open(new RawDocument(input, "UTF-8", LocaleId.ARABIC, LocaleId.EMPTY));
        
        testEvent(EventType.START_DOCUMENT, null);
        Event e = testEvent(EventType.PIPELINE_PARAMETERS, null);
        assertEquals(LocaleId.ARABIC.toString(), e.getPipelineParameters().getSourceLocale().toString());
        assertEquals(LocaleId.FRENCH.toString(), e.getPipelineParameters().getTargetLocale().toString());
        
        testEvent(EventType.START_GROUP, null);
        e = testEvent(EventType.TEXT_UNIT, "Source text 1");;
        assertTrue(e.getTextUnit().hasTarget(LocaleId.FRENCH));
        
        testEvent(EventType.DOCUMENT_PART, null);
        testEvent(EventType.END_GROUP, null);

        testEvent(EventType.START_GROUP, null);
        e = testEvent(EventType.TEXT_UNIT, "Source text 2");
        assertTrue(e.getTextUnit().hasTarget(LocaleId.FRENCH));
        
        testEvent(EventType.DOCUMENT_PART, null);
        testEvent(EventType.END_GROUP, null);

        testEvent(EventType.END_DOCUMENT, null);

        filter.close();
    }

    @Test
    public void testSynchronization() {

        // ------------------------
        filter.setConfiguration(CommaSeparatedValuesFilter.FILTER_CONFIG);
        IParameters params2 = filter.getActiveParameters();
        assertTrue(params2 instanceof net.sf.okapi.filters.table.csv.Parameters);
        assertTrue(params2 instanceof net.sf.okapi.filters.table.base.Parameters);
        assertFalse(params2 instanceof net.sf.okapi.filters.table.fwc.Parameters);

        filter.setConfiguration(FixedWidthColumnsFilter.FILTER_CONFIG);
        IParameters params3 = filter.getActiveParameters();
        assertTrue(params3 instanceof net.sf.okapi.filters.table.fwc.Parameters);
        assertTrue(params3 instanceof net.sf.okapi.filters.table.base.Parameters);
        assertFalse(params3 instanceof net.sf.okapi.filters.table.csv.Parameters);

        filter.setConfiguration(TabSeparatedValuesFilter.FILTER_CONFIG);
        IParameters params4 = filter.getActiveParameters();
        assertTrue(params4 instanceof net.sf.okapi.filters.table.tsv.Parameters);
        assertTrue(params4 instanceof net.sf.okapi.filters.table.base.Parameters);
        assertFalse(params4 instanceof net.sf.okapi.filters.table.fwc.Parameters);

        filter.setConfiguration(BaseTableFilter.FILTER_CONFIG);
        IParameters params5 = filter.getActiveParameters();
        assertTrue(params5 instanceof net.sf.okapi.filters.table.base.Parameters);
        assertFalse(params5 instanceof net.sf.okapi.filters.table.csv.Parameters);
    }

    @Test
    public void testTrimMode() {

    }

    @Test
    public void testMultilineColNames() {

    }

    @Test
    public void testSkeleton() {
        String st = null;
        String expected = null;

        try {
            st = getSkeleton(getFullFileName("/csv_test1.txt"));
        } catch (UnsupportedEncodingException e) {
        }
        LOGGER.trace(String.format("Skeleton of %s\n---\n", "csv_test1.txt") + st + "\n----------");

        try {
            expected = streamAsString(root.in("/csv_test1.txt").asInputStream());
        } catch (IOException e) {
        }
        assertEquals(expected, st);
    }

    @Test
    public void testSkeleton3() {
        String st = null;
        String expected = null;

        try {
            st = getSkeleton(getFullFileName("/csv_test2.txt"));
        } catch (UnsupportedEncodingException e) {
        }
        LOGGER.trace(String.format("Skeleton of %s\n---\n", "csv_test2.txt") + st + "\n----------");

        try {
            expected = streamAsString(root.in("/csv_test2.txt").asInputStream());
        } catch (IOException e) {
        }
        assertEquals(expected, st);
    }

    /*
     * @Test public void testStartDocument () {
     * assertTrue("Problem in StartDocument",
     * FilterTestDriver.testStartDocument(filter, new
     * InputDocument(root.in("/csv_test1.txt", "").toString(), "UTF-8", locEN, locEN)); }
     */
    @Test
    public void testDoubleExtraction() {
        // Read all files in the data directory
        ArrayList<InputDocument> list = new ArrayList<InputDocument>();

        list.add(new InputDocument(root.in("/csv.txt").toString(), ""));
        list.add(new InputDocument(root.in("/csv_test1.txt").toString(), ""));
        list.add(new InputDocument(root.in("/csv_test2.txt").toString(), ""));
        list.add(new InputDocument(root.in("/test01.catkeys").toString(), ""));

        RoundTripComparison rtc = new RoundTripComparison();
        assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR));
    }

    @Test
    public void testIssue124() {

        IParameters prms = filter.getParameters();
        assertEquals(net.sf.okapi.filters.table.Parameters.class, prms.getClass());

        net.sf.okapi.filters.table.Parameters params = (net.sf.okapi.filters.table.Parameters) filter
                .getParameters();

        URL paramsUrl = root.in("/okf_table@test124.fprm").asUrl();
        assertNotNull(paramsUrl);

        params.load(paramsUrl, false);

        filter.open(new RawDocument("", locEN, locFR));
        assertEquals("net.sf.okapi.filters.table.tsv.Parameters", params.getParametersClassName());

        Event event = filter.next();
        assertNotNull(event);

        assertTrue(event.getEventType() == EventType.START_DOCUMENT);

        StartDocument startDoc = (StartDocument) event.getResource();
        IParameters sdps = startDoc.getFilterParameters();
        assertEquals(net.sf.okapi.filters.table.Parameters.class, sdps.getClass());
    }

    // Helpers
    private String getFullFileName(String fileName) {
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
            assertEquals(expectedText, ((ITextUnit) res).toString());
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

    private String getSkeleton(String fileName) throws UnsupportedEncodingException {
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
            while (filter.hasNext()) {
                event = filter.next();
                writer.handleEvent(event);
            }
        } finally {
            if (filter != null) filter.close();
        }
        return new String(writerBuffer.toByteArray(), "UTF-16");
    }

    private String streamAsString(InputStream input) throws IOException {
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

        StringBuilder tmp = new StringBuilder();
        char[] buf = new char[2048];
        int count = 0;
        while ((count = reader.read(buf)) != -1) {
            tmp.append(buf, 0, count);
        }

        return tmp.toString();
    }
}
