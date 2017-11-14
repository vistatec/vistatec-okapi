/*===========================================================================
  Copyright (C) 2016 by the Okapi Framework contributors
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

package net.sf.okapi.filters.pdf;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.resource.ITextUnit;

@RunWith(JUnit4.class)
public class PdfFilterTest {

	private PdfFilter filter;
	private LocaleId locEN = LocaleId.fromString("en");

	@Before
	public void setUp() {
		filter = new PdfFilter();
	}

	@Test
	public void testDefaultInfo() {
		assertNotNull(filter.getParameters());
		assertNotNull(filter.getName());
		List<FilterConfiguration> list = filter.getConfigurations();
		assertNotNull(list);
		assertTrue(list.size() > 0);
	}

	@Test
	public void testStartDocument() throws URISyntaxException {
		URL url = PdfFilterTest.class.getResource("/OmegaT_documentation_en.PDF");
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
				new InputDocument(url.toURI().getPath(), null), "UTF-8", locEN, locEN));
	}
	
	@Test
	public void firstTextUnit() throws URISyntaxException {
		URL url = PdfFilterTest.class.getResource("/PALC_2011_LT.pdf");
		PdfFilter f = new PdfFilter();
		Parameters p = f.getParameters();
		p.setLineSeparator("");
		p.setParagraphSeparator("\n\n");
		f.setParameters(p);
		ITextUnit first = FilterTestDriver.getTextUnit(f, new InputDocument(url.toURI().getPath(), null), "UTF-8", locEN, locEN, 1);
		assertEquals("Translation Quality Checking in LanguageTool", first.getSource().getFirstContent().toString());
		f.close();
		
		url = PdfFilterTest.class.getResource("/OmegaT_documentation_en.PDF");
		first = FilterTestDriver.getTextUnit(filter, new InputDocument(url.toURI().getPath(), null), "UTF-8", locEN, locEN, 1);
		assertEquals("OmegaT 3.1 - User's Guide Vito Smolej", first.getSource().getFirstContent().toString());
		
		url = PdfFilterTest.class.getResource("/TAUS-QualityDashboard-September.pdf");
		first = FilterTestDriver.getTextUnit(filter, new InputDocument(url.toURI().getPath(), null), "UTF-8", locEN, locEN, 2);
		assertEquals("TAUS Quality Dashboard", first.getSource().getFirstContent().toString());
	}
	
	@Test
	public void firstParagraphTextUnit() throws URISyntaxException {
		URL url = PdfFilterTest.class.getResource("/PALC_2011_LT.pdf");
		PdfFilter f = new PdfFilter();
		Parameters p = f.getParameters();
		p.setLineSeparator("\n");
		p.setParagraphSeparator("\n");
		f.setParameters(p);
		ITextUnit first = FilterTestDriver.getTextUnit(f, new InputDocument(url.toURI().getPath(), null), "UTF-8", locEN, locEN, 3);
		assertTrue(first.getSource().getFirstContent().toString().startsWith("Abstract: In large computer-aided translation"));
		f.close();
		
		url = PdfFilterTest.class.getResource("/OmegaT_documentation_en.PDF");
		first = FilterTestDriver.getTextUnit(filter, new InputDocument(url.toURI().getPath(), null), "UTF-8", locEN, locEN, 5);
		assertTrue(first.getSource().getFirstContent().toString().startsWith("This document is the official user's guide to OmegaT"));
		
		url = PdfFilterTest.class.getResource("/TAUS-QualityDashboard-September.pdf");
		first = FilterTestDriver.getTextUnit(filter, new InputDocument(url.toURI().getPath(), null), "UTF-8", locEN, locEN, 6);
		assertTrue(first.getSource().getFirstContent().toString().startsWith("This document describes how the TAUS Dynamic Quality Framework"));
	}
}
