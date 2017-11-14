/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.tkit.step;

import static net.sf.okapi.lib.tkit.step.MergerUtil.getTextUnitEvents;
import static net.sf.okapi.lib.tkit.step.MergerUtil.writeXliffAndSkeleton;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.steps.common.RawDocumentWriterStep;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CombinedXliffMergerStepTest {
	private HtmlFilter htmlFilter;
	private String root;
	private CombinedXliffMergerStep merger;
	private RawDocumentWriterStep writer;

	@Before
	public void setUp() {
		htmlFilter = new HtmlFilter();
		merger = new CombinedXliffMergerStep();
		writer = new RawDocumentWriterStep();
		root = TestUtil.getParentDir(this.getClass(), "/dummy.txt");
	}

	@After
	public void tearDown() {
		htmlFilter.close();
		merger.destroy();
		writer.destroy();
	}

	@SuppressWarnings("resource")
	@Test
	public void simpleMerge() throws FileNotFoundException {
		String input = "simple.html";
		// Serialize the source file
		writeXliffAndSkeleton(FilterTestDriver.getEvents(
					htmlFilter, 
					new RawDocument(Util.toURI(root+input), "UTF-8", LocaleId.ENGLISH), null), 
				root, root+input+".xlf");

		IFilterConfigurationMapper fcm = new FilterConfigurationMapper();
        DefaultFilters.setMappings(fcm, true, true);        
        merger.setFilterConfigurationMapper(fcm);
		merger.setOutputEncoding("UTF-8");
		merger.setSecondInput(new RawDocument(Util.toURI(root+input+".skl"),"UTF-8", LocaleId.ENGLISH));
		RawDocument rd = new RawDocument(Util.toURI(root+input),"UTF-8", LocaleId.ENGLISH);
		merger.setThirdInput(rd);
		rd.setFilterConfigId("okf_html");
		List<LocaleId> ts = new LinkedList<LocaleId>();
		ts.add(LocaleId.FRENCH);
		merger.setTargetLocales(ts);
		
		Event e = merger.handleEvent(new Event(EventType.RAW_DOCUMENT, 
						new RawDocument(Util.toURI(root+input+".xlf"), "UTF-8", LocaleId.ENGLISH, LocaleId.ENGLISH)));		
		writer.setOutputURI(Util.toURI(root+input+".merged"));
		writer.handleEvent(e);
		writer.destroy();
		
		RawDocument ord = new RawDocument(Util.toURI(root+input), "UTF-8", LocaleId.ENGLISH);
		RawDocument trd = new RawDocument(Util.toURI(root+input+".merged"), "UTF-8", LocaleId.ENGLISH);
		List<Event> o = getTextUnitEvents(htmlFilter, ord);
		List<Event> t = getTextUnitEvents(htmlFilter, trd);
		assertTrue(o.size() == t.size());
		assertTrue(FilterTestDriver.compareEvents(o, t, false));
	}

	@SuppressWarnings("resource")
	@Test
	public void simpleMergeNoSkeleton() throws FileNotFoundException {
		String input = "simple.html";
		// Serialize the source file
		writeXliffAndSkeleton(FilterTestDriver.getEvents(
					htmlFilter, 
					new RawDocument(Util.toURI(root+input), "UTF-8", LocaleId.ENGLISH), null), 
				root, root+input+"_no_skeleton.xlf", false);

		IFilterConfigurationMapper fcm = new FilterConfigurationMapper();
        DefaultFilters.setMappings(fcm, true, true);        
        merger.setFilterConfigurationMapper(fcm);
		merger.setOutputEncoding("UTF-8");
		merger.setSecondInput(null);
		RawDocument rd = new RawDocument(Util.toURI(root+input),"UTF-8", LocaleId.ENGLISH);
		merger.setThirdInput(rd);
		rd.setFilterConfigId("okf_html");
		
		List<LocaleId> ts = new LinkedList<LocaleId>();
		ts.add(LocaleId.FRENCH);
		merger.setTargetLocales(ts);
		Event e = merger.handleEvent(new Event(EventType.RAW_DOCUMENT, 
						new RawDocument(Util.toURI(root+input+"_no_skeleton.xlf"), "UTF-8", LocaleId.ENGLISH, LocaleId.ENGLISH)));
		
		writer.setOutputURI(Util.toURI(root+input+"_no_skeleton.merged"));
		writer.handleEvent(e);
		writer.destroy();
		
		RawDocument ord = new RawDocument(Util.toURI(root+input), "UTF-8", LocaleId.ENGLISH);
		RawDocument trd = new RawDocument(Util.toURI(root+input+"_no_skeleton.merged"), "UTF-8", LocaleId.ENGLISH);
		List<Event> o = getTextUnitEvents(htmlFilter, ord);
		List<Event> t = getTextUnitEvents(htmlFilter, trd);
		assertTrue(o.size() == t.size());
		assertTrue(FilterTestDriver.compareEvents(o, t, false));
	}	
}
