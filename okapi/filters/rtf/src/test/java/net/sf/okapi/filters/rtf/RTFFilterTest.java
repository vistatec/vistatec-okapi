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

package net.sf.okapi.filters.rtf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RTFFilterTest {

	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locFR = LocaleId.FRENCH;
	private GenericContent fmt;

	@Before
	public void setUp () {
		fmt = new GenericContent();
	}
	
	@Test
	public void testBasicProcessing () {
		FilterTestDriver testDriver = new FilterTestDriver();
		try (RTFFilter filter = new RTFFilter()) {
			InputStream input = RTFFilterTest.class.getResourceAsStream("/Test01.rtf");
			filter.open(new RawDocument(input, "windows-1252", locEN, locFR));
			if ( !testDriver.process(filter) ) Assert.fail();
			filter.close();
		}
		catch ( Throwable e ) {
			e.printStackTrace();
			Assert.fail("Exception occured");
		}
	}
	
	@Test
	public void testSimpleTU () {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents("Test01.rtf", locEN, locFR), 1);
		assertNotNull(tu);
		assertEquals("Text (to) translate.", tu.getSource().toString());
		TextContainer tc = tu.getTarget(locFR);
		assertNotNull(tc);
		assertEquals("Texte \u00e0 traduire.", tc.toString());

		tu = FilterTestDriver.getTextUnit(getEvents("Test01.rtf", locEN, locFR), 2);
		assertNotNull(tu);
		assertEquals("[Text with <1>bold</1>.]", fmt.printSegmentedContent(tu.getSource(), true));
		tc = tu.getTarget(locFR);
		assertNotNull(tc);
		assertEquals("[Texte avec du <1>gras</1>.]", fmt.printSegmentedContent(tc, true));
	}
	
	private ArrayList<Event> getEvents (String file, LocaleId srcLoc, LocaleId trgLoc) {
		ArrayList<Event> list = new ArrayList<Event>();
		try (IFilter filter = new RTFFilter()) {
			InputStream input = RTFFilterTest.class.getResourceAsStream("/"+file);
			filter.open(new RawDocument(input, "windows-1252", srcLoc, trgLoc));
			while ( filter.hasNext() ) {
				list.add(filter.next());
			}
		}
		catch ( Throwable e ) {
			e.printStackTrace();
			Assert.fail("Exception occured: "+e.getLocalizedMessage());
		}
		return list;
	}	

}
