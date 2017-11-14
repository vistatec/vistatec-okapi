/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.mosestext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MosesTextFilterWriterTest {
	
	private String root;
	private MosesTextFilter filter;
	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locFR = LocaleId.FRENCH;
	private final String lb = System.getProperty("line.separator");

	public MosesTextFilterWriterTest () throws URISyntaxException {
		filter = new MosesTextFilter();
		URL url = MosesTextFilterWriterTest.class.getResource("/Test01.txt");
		root = Util.getDirectoryName(url.toURI().getPath()) + File.separator;
	}

	@Test
	public void testSimpleOutputFromMosesText () {
		// Read a Moses text and create Moses output (not a round trip!)
		String snippet = "<mrk mtype=\"seg\">Line 1</mrk>\rLine 2\r";
		String expected = "Line 1"+lb+"Line 2"+lb;
		String res = generateMosesOutput(getEvents(snippet));
		assertEquals(expected, res);
	}
	
	@Test
	public void testMultilineOutputFromMosesText () {
		// Read a Moses text and create Moses output (not a round trip!)
		String snippet = "Text 1.\r<mrk mtype=\"seg\">Text 2\rText 3.</mrk>\rText 4\r";
		String expected = "Text 1."+lb+"Text 2<lb/>Text 3."+lb+"Text 4"+lb;
		String res = generateMosesOutput(getEvents(snippet));
		assertEquals(expected, res);
	}
	
	@Test
	public void testOutputFromXLIFF01 () {
		// Read from XLIFF and generate the Moses file (in a string)
		IFilter xlfFilter = new net.sf.okapi.filters.xliff.XLIFFFilter();
		String res = generateMosesOutput(getEventsFromFile(xlfFilter, root+"Test-XLIFF01.xlf"));
		
		// Read the Moses string and compare with the expected result
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(res), 4);
		assertNotNull(tu);
		assertEquals("4", tu.getId());
		assertEquals("Help Authoring Guidelines", tu.getSource().toString());
	}

	@Test
	public void testFileOutputFromXLIFF01 () {
		File outFile = new File(root+"Test-XLIFF01.out.txt");
		outFile.delete();
		
		IFilter xlfFilter = new net.sf.okapi.filters.xliff.XLIFFFilter();
		generateFileOutput(
			getEventsFromFile(xlfFilter, root+"Test-XLIFF01.xlf"),
			outFile);
		
		// Read the Moses file and compare with the expected result
		String res = generateMosesOutput(getEventsFromFile(filter, root+"Test-XLIFF01.out.txt"));
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(res), 1);
		assertNotNull(tu);
		assertEquals("1", tu.getId());
		assertEquals("Okapi Framework <g id=\"1\">Developer's Guide</g> - Help Guidelines", tu.getSource().toString());
		assertEquals("<g id=\"1\">", tu.getSource().getFirstContent().getCode(0).getData());

		tu = FilterTestDriver.getTextUnit(getEvents(res), 4);
		assertNotNull(tu);
		assertEquals("4", tu.getId());
		assertEquals("Help Authoring Guidelines", tu.getSource().toString());
	}
	
	@Test
	public void testOutputFromXLIFF02 () {
		// Read from XLIFF and generate the Moses file (in a string)
		IFilter xlfFilter = new net.sf.okapi.filters.xliff.XLIFFFilter();
		String res = generateMosesOutput(getEventsFromFile(xlfFilter, root+"Test-XLIFF02.xlf"));
		
		// Check the Moses output
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(res), 3);
		assertNotNull(tu);
		assertEquals("3", tu.getId());
		assertEquals("<g id=\"1\">word1</g>, <g id=\"2\">word2</g>, <x id=\"3\"/>word3, <x id=\"4\"/>word5, <x id=\"5\"/>, etc.",
			tu.getSource().toString());
		
		tu = FilterTestDriver.getTextUnit(getEvents(res), 4);
		assertNotNull(tu);
		assertEquals("4", tu.getId());
		assertEquals("<=lt, >=gt, \"=quot, '=apos, \r\r=0xD\n"
			+ "Line 2\n"
			+ "Line 3\n"
			+ "<x id=\"1\"/> end.",
			tu.getSource().toString());
	}

	private String generateMosesOutput (List<Event> list) {
		try {
			IFilterWriter writer = new MosesTextFilterWriter();
			writer.setOptions(locEN, "UTF-8");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			writer.setOutput(baos);
			for ( Event event : list ) {
				writer.handleEvent(event);
			}
			writer.close();
			return baos.toString("UTF-8");
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiException("Error when creating the output");
		}
	}
	
	private void generateFileOutput (List<Event> list,
		File outFile)
	{
		IFilterWriter writer = new MosesTextFilterWriter();
		writer.setOptions(locEN, "UTF-8");
		writer.setOutput(outFile.getAbsolutePath());
		for ( Event event : list ) {
			writer.handleEvent(event);
		}
		writer.close();
	}
	
	private ArrayList<Event> getEventsFromFile (IFilter filter,
		String path)
	{
		ArrayList<Event> list = new ArrayList<Event>();
		filter.open(new RawDocument(new File(path).toURI(), "UTF-8", locEN, locFR));
		while (filter.hasNext()) {
			Event event = filter.next();
			list.add(event);
		}
		filter.close();
		return list;
	}

	private ArrayList<Event> getEvents (String snippet) {
		return FilterTestDriver.getEvents(filter, new RawDocument(snippet, locEN), null);
	}

}
