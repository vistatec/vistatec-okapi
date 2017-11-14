/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.sdlpackage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

public class SdlPackageFilterTests {

	private final LocaleId locENUS = LocaleId.fromString("en-US");
	private final LocaleId locFRCA = LocaleId.fromString("fr-CA");
	private final LocaleId locPLPL = LocaleId.fromString("pl-PL");
	
	private FileLocation root;
	
	@Before
	public void setUp()
		throws URISyntaxException
	{
		root = FileLocation.fromClass(this.getClass());
	}

	@Test
	public void testInformation () {
		try ( SdlPackageFilter filter = new SdlPackageFilter() ) {
			
			assertEquals(SdlPackageFilter.MIME_TYPE, filter.getMimeType());
			assertEquals("okf_sdlpackage", filter.getName());
			assertEquals("SDLPPX and SDLRPX Filter (BETA)", filter.getDisplayName());
			
			List<FilterConfiguration> confs = filter.getConfigurations();
			assertEquals(1, confs.size());
			FilterConfiguration conf = confs.get(0);
			assertEquals(SdlPackageFilter.class.getName(), conf.filterClass);
			assertEquals(filter.getName(), conf.configId);
		}
	}
	
	@Test
	public void testSimpleRead ()
		throws URISyntaxException
	{
		try ( SdlPackageFilter filter = new SdlPackageFilter() ) {
			
			URI uri = root.in("/ts2017-test01.sdlppx").asUri();
			RawDocument rd = new RawDocument(uri, "UTF-8", locENUS, locFRCA);
			filter.setOptions(locENUS, locFRCA, "UTF-8", true);
			filter.open(rd);
			int sdCount = 0;
			int segCount = 0;
			boolean segFound = false;
			String sdFound = null;
			while ( filter.hasNext() ) {
				Event event = filter.next();
				switch ( event.getEventType() ) {
				case START_SUBDOCUMENT:
					//System.out.println("--- sf: "+event.getStartSubDocument().getName());
					sdCount++; // Note that XLIFF file is seen as a sub-document too
					if ( sdCount == 2 ) {
						sdFound = event.getStartSubDocument().getName();
					}
					break;
				case TEXT_UNIT:
					ITextUnit tu = event.getTextUnit();
					ISegments segs = tu.getSource().getSegments();
					for ( Segment seg : segs ) {
						//System.out.println("seg: "+seg.getContent().getCodedText()+"\n");
						segCount++;
						if ( seg.getContent().getCodedText().equals(
							"It has several paragraphs and several sentences.") ) {
							segFound = true;
						}
					}
				default:
					break;
				}
			}
			assertEquals(2, sdCount);
			assertEquals("C:\\Users\\ysavourel\\Documents\\Studio 2017\\Projects\\Project 1\\en-US\\Test.docx", sdFound);
			assertEquals(4, segCount);
			assertTrue(segFound);
		}
	}
	
	@Test
	public void testSimpleReadWrite ()
		throws URISyntaxException
	{
		IFilterWriter writer = null;
		File out = root.in("/ts2017-test01.out.sdlppx").asFile();
		out.delete();
		assertFalse(out.exists());

		// Read and write with target in capital letters
		try ( SdlPackageFilter filter = new SdlPackageFilter() ) {
			URI uri = root.in("/ts2017-test01.sdlppx").asUri();
			RawDocument rd = new RawDocument(uri, "UTF-8", locENUS, locPLPL);
			filter.setOptions(locENUS, locPLPL, "UTF-8", true);
			filter.open(rd);
			// Prepare writer
			writer = filter.createFilterWriter();
			writer.setOutput(out.getAbsolutePath());
			
			while ( filter.hasNext() ) {
				Event event = filter.next();
				if ( event.isTextUnit() ) {
					ITextUnit tu = event.getTextUnit();
					// SDLPPX has empty <target> elements, so we force the copy
					TextContainer tc = tu.createTarget(locPLPL, true, IResource.COPY_ALL);
					ISegments segs = tc.getSegments();
					for ( Segment seg : segs ) {
						TextFragment tf = seg.getContent();
						tf.setCodedText(tf.getCodedText().toUpperCase());
					}
				}
				writer.handleEvent(event);
			}
		}
		finally {
			if ( writer != null ) {
				writer.close();
			}
		}

		// Read and check the output
		try ( SdlPackageFilter filter = new SdlPackageFilter() ) {
			URI uri = root.in("/ts2017-test01.out.sdlppx").asUri(); //new File(base, "ts2017-test01.out.sdlppx").toURI();
			RawDocument rd = new RawDocument(uri, "UTF-8", locENUS, locPLPL);
			filter.setOptions(locENUS, locPLPL, "UTF-8", true);
			filter.open(rd);
			
			while ( filter.hasNext() ) {
				Event event = filter.next();
				if ( event.isTextUnit() ) {
					ITextUnit tu = event.getTextUnit();
					TextContainer tc = tu.getTarget(locPLPL);
					assertNotNull(tc);
					ISegments segs = tc.getSegments();
					for ( Segment seg : segs ) {
						String found = seg.getContent().getCodedText();
						String expected = seg.getContent().getCodedText().toUpperCase();
						assertEquals(expected, found);
					}
				}
			}
		}
	}
}