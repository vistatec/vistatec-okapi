/*===========================================================================
  Copyright (C) 2011-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.archive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.tmx.TmxFilter;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.steps.EventLogger;
import net.sf.okapi.steps.common.FilterEventsToRawDocumentStep;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

@RunWith(JUnit4.class)
public class ArchiveFilterTest {

	private FileLocation root;
	private FilterConfigurationMapper fcMapper;
	private static final LocaleId EN = LocaleId.fromString("en");
	private static final LocaleId ENUS = LocaleId.fromString("en-US");
	private static final LocaleId ESES = new LocaleId("es", "es");
	
	@Before
	public void setUp() throws URISyntaxException {
		fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations(XLIFFFilter.class.getName());
		fcMapper.addConfigurations(TmxFilter.class.getName());
		root = FileLocation.fromClass(this.getClass());
	}

	@Test
	public void testSubFilterOpen ()
		throws ZipException, IOException
	{
		ZipFile zipFile = new ZipFile(root.in("/test1_es.archive").asFile());
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		ZipEntry entry = entries.nextElement();
		IFilter subFilter = new XLIFFFilter();
		subFilter.open(new RawDocument(zipFile.getInputStream(entry), "UTF-8", EN, ESES));
		assertTrue(subFilter.hasNext());
		subFilter.close();
		zipFile.close();
	}
	
	@Test
	public void testFilterOpen ()
		throws ZipException, IOException
	{		
		ZipFile zipFile = new ZipFile(root.in("/test1_es.archive").asFile());
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		ZipEntry entry = entries.nextElement();
		IFilter subFilter = new XLIFFFilter();
		subFilter.open(new RawDocument(zipFile.getInputStream(entry), "UTF-8", EN, ESES));
		assertTrue(subFilter.hasNext());
		subFilter.close();
		zipFile.close();
	}
	
	@Test
	public void testNoTUs ()
		throws MalformedURLException
	{
		// Only document parts are extracted
		boolean ok = true;
		RawDocument input = new RawDocument(root.in("/test2_unknownfiles.archive").asUri(), "UTF-8", ENUS);
		try ( ArchiveFilter filter = new ArchiveFilter() ) {
			filter.open(input);
			while ( filter.hasNext() ) {
				Event event = filter.next();
				switch ( event.getEventType() ) {
				case DOCUMENT_PART:
				case END_DOCUMENT:
				case START_DOCUMENT:
					break;
				default:
					// Unexpected event
					ok = false;
					break;
				}
			}
		}
		assertTrue(ok);
	}
	
	@Test
	public void testMimeType ()
		throws MalformedURLException
	{
		try ( ArchiveFilter filter = new ArchiveFilter() ) {
			assertEquals(ArchiveFilter.MIME_TYPE, filter.getMimeType());
			
			Parameters params = new Parameters();
			params.setFileNames("*.xliff2, *.tmx");
			params.setConfigIds("okf_xliff, okf_tmx");
			filter.setParameters(params);
			assertEquals(ArchiveFilter.MIME_TYPE, filter.getMimeType());
			
			params.setMimeType("application/x-test");
			assertEquals("application/x-test", filter.getMimeType());
		}
	}
	
	@Test
	public void testExtractXLIFFOnly ()
		throws MalformedURLException
	{
		// Extract XLIFF only
		try ( ArchiveFilter filter = new ArchiveFilter() ) {
			Parameters params = (Parameters)filter.getParameters();
			params.setFileNames("*.xlf");
			params.setConfigIds("okf_xliff");
			filter.setFilterConfigurationMapper(fcMapper);
			
			InputDocument doc = new InputDocument(root.in("/test3_es.archive").asFile().getAbsolutePath(), null);
			// Only one TU should be extracted
			ITextUnit tu = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 2);
			assertNull(tu);
			// And it should be this one
			tu = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 1);
			assertEquals("About...", tu.getSource().getCodedText());
		}
	}
	
	@Test
	public void testExtractTMXOnly ()
		throws MalformedURLException
	{
		// Extract XLIFF only
		try ( ArchiveFilter filter = new ArchiveFilter() ) {
			Parameters params = (Parameters)filter.getParameters();
			params.setFileNames("*.tmx");
			params.setConfigIds("okf_tmx");
			filter.setFilterConfigurationMapper(fcMapper);
			
			InputDocument doc = new InputDocument(root.in("/test3_es.archive").asFile().getAbsolutePath(), null);
			// Only one TU should be extracted
			ITextUnit tu = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 2);
			assertNull(tu);
			// And it should be this one
			tu = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 1);
			assertEquals("test en", tu.getSource().getCodedText());
		}
	}
	
	@Test
	public void testExtractXLIFFandTMX ()
		throws MalformedURLException
	{
		// Extract XLIFF only
		try ( ArchiveFilter filter = new ArchiveFilter() ) {
			Parameters params = (Parameters)filter.getParameters();
			params.setFileNames("*.tmx,*.xlf");
			params.setConfigIds("okf_tmx,okf_xliff");
			filter.setFilterConfigurationMapper(fcMapper);
			
			InputDocument doc = new InputDocument(root.in("/test3_es.archive").asFile().getAbsolutePath(), null);
			// Only two TUs should be extracted
			ITextUnit tu = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 3);
			assertNull(tu);
			// And it should be those two
			ITextUnit tu1 = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 1);
			ITextUnit tu2 = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 2);
			int count = 0;
			if ( tu1.getSource().getCodedText().equals("test en") ) count++;
			if ( tu2.getSource().getCodedText().equals("test en") ) count++;
			if ( tu1.getSource().getCodedText().equals("About...") ) count++;
			if ( tu2.getSource().getCodedText().equals("About...") ) count++;
			assertEquals(2, count);
		}
	}
	
	@Test
	public void testNoExtraction ()
		throws MalformedURLException
	{
		// Nothing is extracted as no parameters are specified
		try ( ArchiveFilter filter = new ArchiveFilter() ) {
			Parameters params = new Parameters();
			params.setFileNames("");
			params.setConfigIds("");
			filter.setParameters(params);
			
			InputDocument doc = new InputDocument(root.in("/test3_es.archive").asFile().getAbsolutePath(), null);
			// Only one TU should be extracted
			ITextUnit tu = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 1);
			assertNull(tu);
		}
	}
	
	@Test(expected = OkapiIOException.class)
	public void testMissingFilter ()
		throws MalformedURLException
	{
		// Nothing is extracted as no parameters are specified
		try ( ArchiveFilter filter = new ArchiveFilter() ) {
			Parameters params = new Parameters();
			params.setFileNames("*.txt");
			params.setConfigIds("okf_text");
			filter.setParameters(params);
			filter.setFilterConfigurationMapper(fcMapper); // No okf_text in there
			
			InputDocument doc = new InputDocument(root.in("/test2_unknowfiles.archive").asFile().getAbsolutePath(), null);
			FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 1);
		}
	}
	
	@Test
	public void testWithStream ()
		throws MalformedURLException, FileNotFoundException
	{
		File output = root.in("/out/streamoutput.archive").asFile();
		if ( output.exists() ) {
			output.delete();
		}
		assertFalse(output.exists());
		
		try ( ArchiveFilter filter = new ArchiveFilter() ) {
			Parameters params = new Parameters();
			params.setFileNames("*.xlf, *.tmx");
			params.setConfigIds("okf_xliff, okf_tmx");
			filter.setParameters(params);
			filter.setFilterConfigurationMapper(fcMapper);
	
			// Read and write
			RawDocumentToFilterEventsStep rd2fe = new RawDocumentToFilterEventsStep();
			rd2fe.setFilter(filter);
			new XPipeline(
					"Test pipeline for ArchiveFilterTest",
					new XBatch(
							new XBatchItem(
								new FileInputStream(root.in("/test3_es.archive").asFile()),
								"UTF-8",
								root.out("/streamoutput.archive").asFile().getAbsolutePath(),
								"UTF-8",
								EN,
								ESES)
							),
					rd2fe,	
					new EventLogger(),
					new FilterEventsToRawDocumentStep()
					
			).execute();
			
			// Read the output, just for TMX
			params = (Parameters)filter.getParameters();
			params.setFileNames("*.tmx");
			params.setConfigIds("okf_tmx");
			InputDocument doc = new InputDocument(root.in("/out/streamoutput.archive").asFile().getAbsolutePath(), null);
			// Only one TU should be extracted
			ITextUnit tu = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 2);
			assertNull(tu);
			// And it should be this one
			tu = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 1);
			assertEquals("test en", tu.getSource().getCodedText());
			
			// Read the output, just for XLIFF
			params = (Parameters)filter.getParameters();
			params.setFileNames("*.xlf");
			params.setConfigIds("okf_xliff");
			// Only one TU should be extracted
			tu = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 2);
			assertNull(tu);
			// And it should be this one
			tu = FilterTestDriver.getTextUnit(filter, doc, "UTF-8", EN, ESES, 1);
			assertEquals("About...", tu.getSource().getCodedText());
			
		}
	}
	
	@Test
	public void testDoubelextraction () {
		try ( ArchiveFilter filter = new ArchiveFilter() ) {
			filter.setFilterConfigurationMapper(fcMapper);
			ArrayList<InputDocument> list = new ArrayList<InputDocument>();
			list.add(new InputDocument(root.in("/test1_es.archive").asFile().getAbsolutePath(), null));
			list.add(new InputDocument(root.in("/test2_unknownfiles.archive").asFile().getAbsolutePath(), null));
			list.add(new InputDocument(root.in("/test3_es.archive").asFile().getAbsolutePath(), null));
			RoundTripComparison rtc = new RoundTripComparison();
			assertTrue(rtc.executeCompare(filter, list, "UTF-8", EN, ESES, "outcmp"));
		}
	}
}
