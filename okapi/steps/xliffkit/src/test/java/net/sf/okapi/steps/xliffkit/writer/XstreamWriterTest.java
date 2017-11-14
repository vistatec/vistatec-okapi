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

package net.sf.okapi.steps.xliffkit.writer;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.lib.beans.sessions.OkapiXstreamSession;
import net.sf.okapi.lib.beans.v0.TestEvent;
import net.sf.okapi.lib.beans.v0.TestEventBean;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XParameter;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;
import net.sf.okapi.lib.persistence.PersistenceSession;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
@RunWith(JUnit4.class)
public class XstreamWriterTest {

	private static final LocaleId ENUS = new LocaleId("en", "us");
	private static final LocaleId FRFR = new LocaleId("fr", "fr");
	private static final LocaleId DEDE = new LocaleId("de", "de");	
	private static final LocaleId ITIT = new LocaleId("it", "it");
	
	@Test
	public void test() {
		
	}
	
	// DEBUG @Test
	public void testPackageFormat4xstream() throws URISyntaxException, MalformedURLException {

		int loops = 1;
		long start = System.currentTimeMillis();
		
		String pathBase = Util.getDirectoryName(this.getClass().getResource("test2.txt").getPath()) + "/";
		String src1Path = pathBase + "src1/";		
		String src2Path = pathBase + "src2/";
		
		XLIFFKitWriterStep writerStep = new XLIFFKitWriterStep();
		PersistenceSession session = new OkapiXstreamSession();
		writerStep.setSession(session);
		writerStep.setResourcesFileExt(".xml");
		
		new XPipeline(
				"Test pipeline for XLIFFKitWriterStep",
				new XBatch(

// TODO DOCX is not mapped to any default filter configuration								
//						new BatchItem(
//								new URL("file", null, src1Path + "test11.docx"),
//								"UTF-8",
//								ENUS,
//								DEDE)
//						
						new XBatchItem(
								(new URL("file", null, src1Path + "BoldWorld.docx")).toURI(),
								"UTF-8",
								"okf_openxml",
								null,
								"UTF-8",
								ENUS,
								DEDE)
						),
								
				new RawDocumentToFilterEventsStep()
				,				
				
				new XPipelineStep(
						writerStep,								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new XParameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new URL("file", null, pathBase + "testPackageFormat4.xstream.kit").toURI().toString()))
		).execute();
		log(" Total: " + (System.currentTimeMillis() - start) + " milliseconds.");
	}
	
	private void log(String str) {
		Logger localLogger = LoggerFactory.getLogger(getClass()); // loggers are cached
		localLogger.debug(str);
	}
	
	// DEBUG @Test
	public void testPackageFormat5xstream() throws URISyntaxException, MalformedURLException {

		int loops = 10;
		long start = System.currentTimeMillis();
		
		String pathBase = Util.getDirectoryName(this.getClass().getResource("test2.txt").getPath()) + "/";
		String src1Path = pathBase + "src1/";		
		String src2Path = pathBase + "src2/";
		
		XLIFFKitWriterStep writerStep = new XLIFFKitWriterStep();
		PersistenceSession session = new OkapiXstreamSession();
		writerStep.setSession(session);
		writerStep.setResourcesFileExt(".xml");
		for(int i = 0; i < loops; i++) {
		new XPipeline(
				"Test pipeline for XLIFFKitWriterStep",
				new XBatch(
						new XBatchItem(
								new URL("file", null, src1Path + "test5.txt"),
								"UTF-8",
								ENUS,
								FRFR)

						),
								
				new RawDocumentToFilterEventsStep()
				,								
				new XPipelineStep(
						writerStep,								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new XParameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new URL("file", null, pathBase + "testPackageFormat5.xstream.kit").toURI().toString()))
		).execute();
		}
		log(" Total: " + (System.currentTimeMillis() - start) + " milliseconds.");
	}

	
	// DEBUG 
	@Test
	public void testReferences3xstream() throws MalformedURLException, URISyntaxException {
		XLIFFKitWriterStep writerStep = new XLIFFKitWriterStep();
		PersistenceSession session = new OkapiXstreamSession();
		writerStep.setSession(session);
		writerStep.setResourcesFileExt(".xml");
		
		String pathBase = Util.getDirectoryName(this.getClass().getResource("test2.txt").getPath()) + "/";
		writerStep.setOutputURI(new URL("file", null, pathBase + "testReferences3.xstream.kit").toURI());
		writerStep.setTargetLocale(DEDE);
		net.sf.okapi.steps.xliffkit.writer.Parameters params = 
			(net.sf.okapi.steps.xliffkit.writer.Parameters) writerStep.getParameters();
		
		params.setIncludeSource(false);
		params.setIncludeOriginal(false);
		
		
		session.registerBean(TestEvent.class, TestEventBean.class);
		
		TestEvent e1 = new TestEvent("e1");
		TestEvent e2 = new TestEvent("e2");
		e2.setParent(e1);
		e1.setParent(e2);

		writerStep.handleEvent(new Event(EventType.START_BATCH));
		StartDocument sd = new StartDocument("sd1");
		sd.setName("test_refs3.txt");
		sd.setLocale(ENUS);
		sd.setFilterWriter(new GenericFilterWriter(null, null));
		
		writerStep.handleEvent(new Event(EventType.START_DOCUMENT, sd));
		writerStep.handleEvent(e1);
		writerStep.handleEvent(e2);
		writerStep.handleEvent(new Event(EventType.END_DOCUMENT));
		writerStep.handleEvent(new Event(EventType.END_BATCH));
	}
}
