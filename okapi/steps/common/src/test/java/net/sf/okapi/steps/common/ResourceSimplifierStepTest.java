/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.steps.common;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.steps.DocumentPartLogger;
import net.sf.okapi.lib.extra.steps.EventListBuilderStep;
import net.sf.okapi.lib.extra.steps.EventLogger;
import net.sf.okapi.lib.extra.steps.TextUnitLogger;
import net.sf.okapi.lib.extra.steps.TuDpSsfLogger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class ResourceSimplifierStepTest {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static final LocaleId ENUS = new LocaleId("en", "us");
	private final FileLocation pathBase = FileLocation.fromClass(this.getClass());
	
	@Test
	public void testDoubleExtraction () throws URISyntaxException, IOException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();				
		
		list.add(new InputDocument(pathBase.in("aa324.html").toString(), null));
		list.add(new InputDocument(pathBase.in("form.html").toString(), null));
		list.add(new InputDocument(pathBase.in("W3CHTMHLTest1.html").toString(), null));
		list.add(new InputDocument(pathBase.in("msg00058.html").toString(), null));
		list.add(new InputDocument(pathBase.in("ugly_big.htm").toString(), null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		ResourceSimplifierStep rss = new ResourceSimplifierStep();
		
		assertTrue(rtc.executeCompare(new HtmlFilter(), list, "UTF-8", ENUS, ENUS, "skeleton", rss));
	}
	
	@Test
	public void testEvents() throws MalformedURLException {
		EventListBuilderStep elbs1 = new EventListBuilderStep();
		EventListBuilderStep elbs2 = new EventListBuilderStep();
		
		new XPipeline(
				"Test pipeline for ResourceSimplifierStepTest",
				new XBatch(
						new XBatchItem(
								pathBase.in("aa324.html").asUrl(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				elbs1,
				new ResourceSimplifierStep(),
				//new EventLogger(),
				new DocumentPartLogger(),
				elbs2
		).execute();
		
	}
	
	@Test
	public void testEvents2() throws MalformedURLException {
		EventListBuilderStep elbs1 = new EventListBuilderStep();
		EventListBuilderStep elbs2 = new EventListBuilderStep();
		
		new XPipeline(
				"Test pipeline for ResourceSimplifierStepTest",
				new XBatch(
						new XBatchItem(
								pathBase.in("msg00058.html").asUrl(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
//				elbs1,
				//new ResourceSimplifierStep(),
				new EventLogger(),
				elbs2
		).execute();
		
		for (Event event : elbs2.getList()) {
			if (event.isTextUnit()) {
				logger.debug(TextUnitLogger.getTuInfo(event.getTextUnit(), ENUS));
			}
			else if (event.isDocumentPart()) {
				logger.debug(DocumentPartLogger.getDpInfo(event.getDocumentPart(), ENUS));
			}
		}		
	}

	@Test
	public void testEvents3() throws MalformedURLException {
		EventListBuilderStep elbs1 = new EventListBuilderStep();
		EventListBuilderStep elbs2 = new EventListBuilderStep();
		
		new XPipeline(
				"Test pipeline for ResourceSimplifierStepTest",
				new XBatch(
						new XBatchItem(
								pathBase.in("form.html").asUrl(),
								"UTF-8",
								ENUS)

						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				elbs1,
				new ResourceSimplifierStep(),
				//new EventLogger(),
				new DocumentPartLogger(),
				elbs2
		).execute();
	}
	
	@Test
	public void testTuDpSsfEvents() throws URISyntaxException {
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								pathBase.in("form.html").asUri(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				new TuDpSsfLogger()
		).execute();
	}
	
	@Test
	public void testTuDpSsfEvents_simplified() throws URISyntaxException {
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								pathBase.in("form.html").asUri(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				new ResourceSimplifierStep(),
				new TuDpSsfLogger()
		).execute();
	}
	
	@Test
	public void testTuDpSsfEvents2() throws URISyntaxException {
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								pathBase.in("aa324.html").asUri(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				new TuDpSsfLogger()
		).execute();
	}
	
	@Test
	public void testTuDpSsfEvents2_simplified() throws URISyntaxException {
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								pathBase.in("aa324.html").asUri(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				new ResourceSimplifierStep(),
				new TuDpSsfLogger()
		).execute();
	}
	
	@Test
	public void testTuDpSsfEvents3() throws URISyntaxException {
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								pathBase.in("aa324_out.html").asUri(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				new TuDpSsfLogger()
		).execute();
	}
	
	@Test
	public void testTuDpSsfEvents3_simplified() throws URISyntaxException {
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								pathBase.in("aa324_out.html").asUri(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				new ResourceSimplifierStep(),
				new TuDpSsfLogger()
		).execute();
	}
}
