/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.observer.IObservable;
import net.sf.okapi.common.observer.IObserver;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.filters.table.TableFilter;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.beans.v0.TestEvent;
import net.sf.okapi.lib.beans.v0.TestEventBean;
import net.sf.okapi.lib.beans.v0.TestEventBean2;
import net.sf.okapi.lib.beans.v1.EventBean;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XParameter;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;
import net.sf.okapi.lib.extra.steps.EventLogger;
import net.sf.okapi.lib.extra.steps.TuDpSsfLogger;
import net.sf.okapi.lib.persistence.IPersistenceBean;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.beans.FactoryBean;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.leveraging.LeveragingStep;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
@RunWith(JUnit4.class)
public class XLIFFKitWriterTest {
	private final Logger logger = LoggerFactory.getLogger(getClass());

//	private final String IN_NAME1 = "Gate Openerss.htm";
	private final String IN_NAME2 = "TestDocument01.odt";
	private final String IN_NAME3 = "test4.txt";

	private class WriteObserver implements IObserver {
		@Override
		public void update(IObservable o, Object arg) {
			if (arg instanceof IPersistenceBean<?>)
				beans.add((IPersistenceBean<?>) arg);
		}		
	};
	
	WriteObserver writeObserver = new WriteObserver();
	List<IPersistenceBean<?>> beans = new ArrayList<IPersistenceBean<?>>(); 
		
	private XPipeline buildPipeline(String inPath1, String inPath2) throws URISyntaxException {
		
//		XLIFFKitWriterStep step1 = new XLIFFKitWriterStep();
//		// TODO Create outPath parameter, move to constructor
//		// Output files are created in /target/test-classes/net/sf/okapi/steps/xliffkit/writer
//		String outPath = Util.getDirectoryName(this.getClass().getResource(inPath).getPath()) + "/" + inPath + ".xlf";
//		step1.setOutput(outPath);
		//step1.setOptions(LocaleId.FRENCH, "UTF-8");
		
//		LeveragingStep step2 = new LeveragingStep();
//		step2.setsourceLocale(LocaleId.ENGLISH);
//		step2.setTargetLocale(LocaleId.FRENCH);
		
//		TextModificationStep step3 = new TextModificationStep();
//		step3.setTargetLocale(LocaleId.FRENCH);
		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		
		net.sf.okapi.connectors.pensieve.Parameters params = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		params.setDbDirectory(pathBase + "testtm");
		
		return
			new XPipeline(
					"Test pipeline for XLIFFKitWriterStep",
					new XBatch(
//							new BatchItem(
//									this.getClass().getResource(inPath1),
//									"UTF-8",
//									Util.getDirectoryName(this.getClass().getResource(inPath1).getPath()) + 
//											"/" + inPath1 + ".en.fr.xliff.kit",
//									"UTF-8",
//									LocaleId.ENGLISH,
//									LocaleId.FRENCH),
									
							new XBatchItem(
									this.getClass().getResource(inPath2),
									"UTF-8",
									Util.getDirectoryName(new File(this.getClass().getResource(inPath2).toURI()).getPath()) + 
											"/" + inPath2 + ".en.fr.xliff.kit",
									"UTF-8",
									LocaleId.ENGLISH,
									LocaleId.FRENCH)
//							,
//									
//							new BatchItem(
//									this.getClass().getResource(inPath1),
//									"UTF-8",
//									Util.getDirectoryName(this.getClass().getResource(inPath1).getPath()) + 
//										"/" + inPath1 + ".en.zh-cn.xliff.kit",
//									"UTF-16",
//									LocaleId.ENGLISH,
//									LocaleId.CHINA_CHINESE)
							),
									
					new RawDocumentToFilterEventsStep(),
					
					new XPipelineStep(new LeveragingStep(), 
							new XParameter("resourceClassName", net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName()),
							new XParameter("resourceParameters", params.toString(), true),
							new XParameter("threshold", 80),
							new XParameter("fillTarget", true)
					),
//					new PipelineStep(new TextModificationStep(), 
//							new Parameter("type", 0),
//							new Parameter("addPrefix", true),
//							new Parameter("prefix", "{START_"),
//							new Parameter("addSuffix", true),
//							new Parameter("suffix", "_END}"),
//							new Parameter("applyToExistingTarget", false),
//							new Parameter("addName", false),
//							new Parameter("addID", true),
//							new Parameter("markSegments", false)
//					),
					new XPipelineStep(
							new XLIFFKitWriterStep(),								
							new XParameter("gMode", true))
			);
	}
	
	// DEBUG @Test
	public void testOutputFile() throws URISyntaxException {		
		//buildPipeline(IN_NAME3, IN_NAME2).execute();
		buildPipeline(IN_NAME2, IN_NAME3).execute();
	}

	// DEBUG @Test
	public void testTempFile() {				
	}
	
	// DEBUG @Test
	public void testXLIFFFilterEvents() {
		
		XLIFFFilter filter = new XLIFFFilter();
		InputStream input = this.getClass().getResourceAsStream("TestDocument01.odt.xlf");
		filter.open(new RawDocument(input, "UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH));
		
		Event event = null;
		ITextUnit tu = null;

		while (filter.hasNext()) {
			event = filter.next();
			if (event.getEventType() != EventType.TEXT_UNIT) continue;
		
			tu = event.getTextUnit();
			if ("2".equals(tu.getId())) break;
		}
		
		filter.close();
	}

	private static final LocaleId ENUS = new LocaleId("en", "us");
	private static final LocaleId FRFR = new LocaleId("fr", "fr");
	private static final LocaleId DEDE = new LocaleId("de", "de");	
	private static final LocaleId ITIT = new LocaleId("it", "it");
	
	// DEBUG 
	@Test
	public void testPackageFormat() throws URISyntaxException, MalformedURLException {

		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		String src1Path = pathBase + "src1/";
		String src2Path = pathBase + "src2/";
		net.sf.okapi.connectors.pensieve.Parameters params = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		params.setDbDirectory(pathBase + "testtm");
		
		new XPipeline(
				"Test pipeline for XLIFFKitWriterStep",
				new XBatch(
						new XBatchItem(
						new File(src1Path, "test1.xlf").toURI().toURL(),
						"UTF-8",
						ENUS,
						DEDE)
				,		
				
						new XBatchItem(
								new File(src2Path, "test9.odt").toURI().toURL(),
								"UTF-8",
								ENUS,
								DEDE)
						,
								
						new XBatchItem(
								new File(src1Path, "test10.html").toURI().toURL(),
								"UTF-8",
								ENUS,
								DEDE),
								
						new XBatchItem(
								new File(src1Path, "aa324.html").toURI().toURL(),
								"UTF-8",
								ENUS,
								DEDE),
// TODO DOCX is not mapped to any default filter configuration								
//						new BatchItem(
//								new URL("file", null, src1Path + "test11.docx"),
//								"UTF-8",
//								ENUS,
//								DEDE)
//						
						new XBatchItem(
								new File(src1Path, "test11.docx").toURI(),
								"UTF-8",
								"okf_openxml",
								null,
								"UTF-8",
								ENUS,
								DEDE)

						),
								
				new RawDocumentToFilterEventsStep()
				,				
				new EventLogger()
				,
				new XPipelineStep(new LeveragingStep(), 
						new XParameter("resourceClassName", net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName()),
						new XParameter("resourceParameters", params.toString(), true),
						new XParameter("threshold", 80),
						new XParameter("fillTarget", true)
				),
				
				new XPipelineStep(
						new XLIFFKitWriterStep(),								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new Parameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new File(pathBase, "testPackageFormat.xliff.kit").toURI().toString()))
		).execute();
	}
	
	@Test
	public void testXliffEvents() throws MalformedURLException, URISyntaxException {
		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		String src1Path = pathBase + "src1/";
		
		RawDocumentToFilterEventsStep rd2fe = new RawDocumentToFilterEventsStep();
		rd2fe.setFilter(new XLIFFFilter());
		
		new XPipeline(
				"",
				new XBatch(
						new XBatchItem(
								new File(src1Path, "test1.xlf").toURI().toURL(),
								"UTF-8",
								ENUS,
								DEDE)
						),
						
				rd2fe,				
				new EventLogger(),				
				new TuDpSsfLogger()
				
		).execute();
	}
	
	// DEBUG 
	@Test
	public void testPackageFormat2() throws URISyntaxException, MalformedURLException {

		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		String src1Path = pathBase + "src1/";
		String src2Path = pathBase + "src2/";
		
		net.sf.okapi.connectors.pensieve.Parameters params = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		params.setDbDirectory(pathBase + "testtm");
		
		new XPipeline(
				"Test pipeline for XLIFFKitWriterStep",
				new XBatch(
						new XBatchItem(
								new File(src2Path, "test5.txt").toURI().toURL(),
								"UTF-8",
								ENUS,
								FRFR)
						,								
						new XBatchItem(
								new File(src1Path, "test12.html").toURI().toURL(),
								"UTF-8",
								ENUS,
								DEDE)
//						,
// TODO DOCX is not mapped to any default filter configuration								
//						new BatchItem(
//								new URL("file", null, src1Path + "test11.docx"),
//								"UTF-8",
//								ENUS,
//								DEDE)
//						
						),
								
						new RawDocumentToFilterEventsStep()
				,				
				new XPipelineStep(new LeveragingStep(), 
						new XParameter("resourceClassName", net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName()),
						new XParameter("resourceParameters", params.toString(), true),
						new XParameter("threshold", 80),
						new XParameter("fillTarget", true)
				),
				
				new XPipelineStep(
						new XLIFFKitWriterStep(),								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new XParameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new File(pathBase, "testPackageFormat2.xliff.kit").toURI().toString()))
		).execute();
	}
	
	// DEBUG @Test
	public void testPackageFormat3() throws URISyntaxException, MalformedURLException {

		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		String src1Path = pathBase + "src1/";
		String src2Path = pathBase + "src2/";
		
		net.sf.okapi.connectors.pensieve.Parameters params = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		params.setDbDirectory(pathBase + "testtm");
		
		RawDocumentToFilterEventsStep rd2fe = new RawDocumentToFilterEventsStep();
		IFilter filter = new TableFilter();
		rd2fe.setFilter(filter);
		filter.getParameters().load(Util.URItoURL(new File(src1Path, "okf_table@copy-of-csv_97.fprm").toURI()), true);
		
		new XPipeline(
				"Test pipeline for XLIFFKitWriterStep",
				new XBatch(
						new XBatchItem(
								new File(src2Path, "CSVTest_97.txt").toURI(),
								"UTF-8",
								ENUS,
								DEDE)
						),
								
				rd2fe
				,				
				new XPipelineStep(new LeveragingStep(), 
						new XParameter("resourceClassName", net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName()),
						new XParameter("resourceParameters", params.toString(), true),
						new XParameter("threshold", 80),
						new XParameter("fillTarget", true)
				),
				
				new XPipelineStep(
						new XLIFFKitWriterStep(),								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new XParameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new File(pathBase, "testPackageFormat3.xliff.kit").toURI().toString()))
		).execute();
	}
	
	// DEBUG @Test
	public void testPackageFormat4() throws URISyntaxException, MalformedURLException {

		int loops = 1;
		long start = System.currentTimeMillis();
		
		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		String src1Path = pathBase + "src1/";
		String src2Path = pathBase + "src2/";
		net.sf.okapi.connectors.pensieve.Parameters params = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		params.setDbDirectory(pathBase + "testtm");
		
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
								new File(src1Path, "BoldWorld.docx").toURI(),
								"UTF-8",
								"okf_openxml",
								null,
								"UTF-8",
								ENUS,
								DEDE)

						),
								
				new RawDocumentToFilterEventsStep()
				,				
				new XPipelineStep(new LeveragingStep(), 
						new XParameter("resourceClassName", net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName()),
						new XParameter("resourceParameters", params.toString(), true),
						new XParameter("threshold", 80),
						new XParameter("fillTarget", true)
				),
				
				new XPipelineStep(
						new XLIFFKitWriterStep(),								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new XParameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new File(pathBase, "testPackageFormat4.xliff.kit").toURI().toString()))
		).execute();
		log(" Total: " + (System.currentTimeMillis() - start) + " milliseconds.");
	}

	// DEBUG @Test
	public void testPackageFormat5() throws URISyntaxException, MalformedURLException {

		int loops = 1;
		long start = System.currentTimeMillis();
		
		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		String src1Path = pathBase + "src1/";
		String src2Path = pathBase + "src2/";
		net.sf.okapi.connectors.pensieve.Parameters params = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		params.setDbDirectory(pathBase + "testtm");
		
		for(int i = 0; i < loops; i++) {
		new XPipeline(
				"Test pipeline for XLIFFKitWriterStep",
				new XBatch(
						new XBatchItem(
								new File(src1Path, "test5.txt").toURI().toURL(),
								"UTF-8",
								ENUS,
								FRFR)

// TODO DOCX is not mapped to any default filter configuration								
//						new BatchItem(
//								new URL("file", null, src1Path + "test11.docx"),
//								"UTF-8",
//								ENUS,
//								DEDE)
//						
//						new BatchItem(
//								(new URL("file", null, src1Path + "BoldWorld.docx")).toURI(),
//								"UTF-8",
//								"okf_openxml",
//								null,
//								"UTF-8",
//								ENUS,
//								DEDE)

						),
								
				new RawDocumentToFilterEventsStep()
				,				
				new XPipelineStep(new LeveragingStep(), 
						new XParameter("resourceClassName", net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName()),
						new XParameter("resourceParameters", params.toString(), true),
						new XParameter("threshold", 80),
						new XParameter("fillTarget", true)
				),
				
				new XPipelineStep(
						new XLIFFKitWriterStep(),								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new XParameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new File(pathBase, "testPackageFormat5.xliff.kit").toURI().toString()))
		).execute();
		}
		log(" Total: " + (System.currentTimeMillis() - start) + " milliseconds.");
	}

	private void log(String str) {
		logger.debug(str);
	}

	// DEBUG 
	@Test
	public void testPackageFormat6() throws URISyntaxException, MalformedURLException {

		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		String src1Path = pathBase + "src1/";
		String src2Path = pathBase + "src2/";
		net.sf.okapi.connectors.pensieve.Parameters params = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		params.setDbDirectory(pathBase + "testtm");
		
		new XPipeline(
				"Test pipeline for XLIFFKitWriterStep",
				new XBatch(

						new XBatchItem(
								new File(src1Path, "test12.html").toURI().toURL(),
								"UTF-8",
								ENUS,
								DEDE)
//						,
// TODO DOCX is not mapped to any default filter configuration								
//						new BatchItem(
//								new URL("file", null, src1Path + "test11.docx"),
//								"UTF-8",
//								ENUS,
//								DEDE)
//						
						),
								
						new RawDocumentToFilterEventsStep()
				,				
				new XPipelineStep(new LeveragingStep(), 
						new XParameter("resourceClassName", net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName()),
						new XParameter("resourceParameters", params.toString(), true),
						new XParameter("threshold", 80),
						new XParameter("fillTarget", true)
				),
				
				new XPipelineStep(
						new XLIFFKitWriterStep(),								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new XParameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new File(pathBase, "testPackageFormat6.xliff.kit").toURI().toString()))
		).execute();
	}
	
	@Test
	public void testPackageFormat7() throws URISyntaxException, MalformedURLException {

		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		String src1Path = pathBase + "src1/";
		String src2Path = pathBase + "src2/";
		net.sf.okapi.connectors.pensieve.Parameters params = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		params.setDbDirectory(pathBase + "testtm");
		
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
								new File(src1Path, "test11.docx").toURI(),
								"UTF-8",
								"okf_openxml",
								null,
								"UTF-8",
								ENUS,
								DEDE)

						),
								
				new RawDocumentToFilterEventsStep()
				,				
				new EventLogger()
				,
				new XPipelineStep(new LeveragingStep(), 
						new XParameter("resourceClassName", net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName()),
						new XParameter("resourceParameters", params.toString(), true),
						new XParameter("threshold", 80),
						new XParameter("fillTarget", true)
				),
				
				new XPipelineStep(
						new XLIFFKitWriterStep(),								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new Parameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new File(pathBase, "testPackageFormat.xliff.kit").toURI().toString()))
		).execute();
	}
	
	@Test
	public void testPackageFormat8() throws URISyntaxException, MalformedURLException {

		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		String src1Path = pathBase + "src1/";
		String src2Path = pathBase + "src2/";
		net.sf.okapi.connectors.pensieve.Parameters params = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		params.setDbDirectory(pathBase + "testtm");
		
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
								new File(src1Path, "test_UTF-8.txt").toURI().toURL(),
								"UTF-8",
								ENUS,
								FRFR)
						),
								
				new RawDocumentToFilterEventsStep()
				,				
				new EventLogger()
				,
				new XPipelineStep(new LeveragingStep(), 
						new XParameter("resourceClassName", net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName()),
						new XParameter("resourceParameters", params.toString(), true),
						new XParameter("threshold", 80),
						new XParameter("fillTarget", true)
				),
				
				new XPipelineStep(
						new XLIFFKitWriterStep(),								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new Parameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new File(pathBase, "testPackageFormat8.xliff.kit").toURI().toString()))
		).execute();
	}

	
	// DEBUG 	@Test
	public void testReferences() throws MalformedURLException, URISyntaxException {
		XLIFFKitWriterStep writerStep = new XLIFFKitWriterStep();
		
		String pathBase = Util.getDirectoryName(this.getClass().getResource("test2.txt").getPath()) + "/";
		writerStep.setOutputURI(new File(pathBase, "testReferences.xliff.kit").toURI());
		writerStep.setTargetLocale(DEDE);
		net.sf.okapi.steps.xliffkit.writer.Parameters params = 
			(net.sf.okapi.steps.xliffkit.writer.Parameters) writerStep.getParameters();
		
		params.setIncludeSource(false);
		params.setIncludeOriginal(false);
		
		List<Event> events = new ArrayList<Event>();
		
		Event e = new Event(EventType.START_BATCH);
		events.add(e);
		
		StartDocument sd = new StartDocument("sd1");
		sd.setName("test_refs.txt");
		sd.setLocale(ENUS);
		sd.setFilterWriter(new GenericFilterWriter(null, null));
		
		e = new Event(EventType.START_DOCUMENT, sd);
		events.add(e);
//------------------------
	
		
		ITextUnit tu1 = TextUnitUtil.buildTU("source-text1" + (char) 2 + '"' + " : " + '"' + 
			'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		
		ITextUnit tu2 = TextUnitUtil.buildTU("source-text2" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		
		ITextUnit tu3 = TextUnitUtil.buildTU("source-text3" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		
		ITextUnit tu4 = TextUnitUtil.buildTU("source-text4" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		
		ITextUnit tu5 = TextUnitUtil.buildTU("source-text5" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		
		ITextUnit tu6 = TextUnitUtil.buildTU("source-text6" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		
		ITextUnit tu7 = TextUnitUtil.buildTU("source-text7" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");

		tu1.setTarget(FRFR, new TextContainer("french-text1"));
		tu1.setTarget(DEDE, new TextContainer("german-text1"));
		
		GenericSkeleton skel1 = new GenericSkeleton();
		GenericSkeleton skel2 = new GenericSkeleton();
		GenericSkeleton skel3 = new GenericSkeleton();
		GenericSkeleton skel4 = new GenericSkeleton();
		GenericSkeleton skel5 = new GenericSkeleton();
		GenericSkeleton skel6 = new GenericSkeleton();
		GenericSkeleton skel7 = new GenericSkeleton();
		
		tu1.setId("tu1");
		tu1.setSkeleton(skel1);
		
		tu2.setId("tu2");
		tu2.setSkeleton(skel2);
		
		tu3.setId("tu3");
		tu3.setSkeleton(skel3);
		
		tu4.setId("tu4");
		tu4.setSkeleton(skel4);
		
		tu5.setId("tu5");
		tu5.setSkeleton(skel5);
		
		tu6.setId("tu6");
		tu6.setSkeleton(skel6);
		
		tu7.setId("tu7");
		tu7.setSkeleton(skel7);
		
		// 1 -> 3
		// 3 -> 4
		// 5 -> 2
		// 5 -> 7
		// 7 -> 6
		// 2 -> 6 (recursion)
				
		skel1.addContentPlaceholder(tu3);
		skel3.addContentPlaceholder(tu4);
		skel5.addContentPlaceholder(tu2);
		skel5.addContentPlaceholder(tu7);
		skel7.addContentPlaceholder(tu6);
		skel2.addContentPlaceholder(tu6);

		e = new Event(EventType.TEXT_UNIT, tu1);
		events.add(e);
		
		e = new Event(EventType.TEXT_UNIT, tu2);
		events.add(e);
		
		e = new Event(EventType.TEXT_UNIT, tu3);
		events.add(e);
		
		e = new Event(EventType.TEXT_UNIT, tu4);
		events.add(e);
		
		e = new Event(EventType.TEXT_UNIT, tu5);
		events.add(e);
		
		e = new Event(EventType.TEXT_UNIT, tu6);
		events.add(e);
		
		e = new Event(EventType.TEXT_UNIT, tu7);
		events.add(e);

//------------------------		
		e = new Event(EventType.END_DOCUMENT);
		events.add(e);
		
		e = new Event(EventType.END_BATCH);
		events.add(e);
		
		for (Event event : events) {
			writerStep.handleEvent(event);
		}
	}		
	
	// DEBUG 		@Test
	public void testReferences2() throws MalformedURLException, URISyntaxException {
		XLIFFKitWriterStep writerStep = new XLIFFKitWriterStep();
		
		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		writerStep.setOutputURI(new File(pathBase, "testReferences2.xliff.kit").toURI());
		writerStep.setTargetLocale(DEDE);
		net.sf.okapi.steps.xliffkit.writer.Parameters params = 
			(net.sf.okapi.steps.xliffkit.writer.Parameters) writerStep.getParameters();
		
		params.setIncludeSource(false);
		params.setIncludeOriginal(false);
		
		List<Event> events = new ArrayList<Event>();
		
		Event e = new Event(EventType.START_BATCH);
		events.add(e);
		
		StartDocument sd = new StartDocument("sd1");
		sd.setName("test_refs.txt");
		sd.setLocale(ENUS);
		sd.setFilterWriter(new GenericFilterWriter(null, null));
		
		e = new Event(EventType.START_DOCUMENT, sd);
		events.add(e);
//------------------------
	
		
		ITextUnit tu1 = TextUnitUtil.buildTU("source-text1" + (char) 2 + '"' + " : " + '"' + 
			'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		
		ITextUnit tu2 = TextUnitUtil.buildTU("source-text2" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		
		GenericSkeleton skel1 = new GenericSkeleton();
		GenericSkeleton skel2 = new GenericSkeleton();
		
		tu1.setId("tu1");
		tu1.setSkeleton(skel1);
		
		tu2.setId("tu2");
		tu2.setSkeleton(skel2);
		
		// 1 -> 2
		// 2 -> 1

		skel1.addContentPlaceholder(tu2);
		skel2.addContentPlaceholder(tu1);
		
		e = new Event(EventType.TEXT_UNIT, tu1);
		events.add(e);
		
		e = new Event(EventType.TEXT_UNIT, tu2);
		events.add(e);
		
//------------------------		
		e = new Event(EventType.END_DOCUMENT);
		events.add(e);
		
		e = new Event(EventType.END_BATCH);
		events.add(e);
		
		for (Event event : events) {
			writerStep.handleEvent(event);
		}
	}
	
	// DEBUG 		
	@Test
	public void testReferences3() throws MalformedURLException, URISyntaxException {
		XLIFFKitWriterStep writerStep = new XLIFFKitWriterStep();
		IPersistenceSession session = writerStep.getSession();
		
		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		writerStep.setOutputURI(new File(pathBase, "testReferences3.xliff.kit").toURI());
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

	// DEBUG 		
	@Test
	public void testReferences4() throws MalformedURLException, URISyntaxException {
		XLIFFKitWriterStep writerStep = new XLIFFKitWriterStep();
		IPersistenceSession session = writerStep.getSession();
		
		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		writerStep.setOutputURI(new File(pathBase, "testReferences4.xliff.kit").toURI());
		writerStep.setTargetLocale(DEDE);
		net.sf.okapi.steps.xliffkit.writer.Parameters params = 
			(net.sf.okapi.steps.xliffkit.writer.Parameters) writerStep.getParameters();
		
		params.setIncludeSource(false);
		params.setIncludeOriginal(false);
		
		session.registerBean(TestEvent.class, TestEventBean.class);
		
		TestEvent e1 = new TestEvent("e1");
		TestEvent e2 = new TestEvent("e2");
		TestEvent e3 = new TestEvent("e3");
		TestEvent e4 = new TestEvent("e4");
		TestEvent e5 = new TestEvent("e5");
		TestEvent e6 = new TestEvent("e6");
		TestEvent e7 = new TestEvent("e7");
		
		e1.setParent(e3);
		e3.setParent(e4);
		e2.setParent(e6);
		e7.setParent(e6);
		e5.setParent(e2);

		writerStep.handleEvent(new Event(EventType.START_BATCH));
		StartDocument sd = new StartDocument("sd1");
		sd.setName("test_refs4.txt");
		sd.setLocale(ENUS);
		sd.setFilterWriter(new GenericFilterWriter(null, null));
		
		writerStep.handleEvent(new Event(EventType.START_DOCUMENT, sd));
		writerStep.handleEvent(e1);
		writerStep.handleEvent(e2);
		writerStep.handleEvent(e3);
		writerStep.handleEvent(e4);
		writerStep.handleEvent(e5);
		writerStep.handleEvent(e6);
		writerStep.handleEvent(e7);
		writerStep.handleEvent(new Event(EventType.END_DOCUMENT));
		writerStep.handleEvent(new Event(EventType.END_BATCH));
	}
	
	// DEBUG 	
	@Test
	public void testReferences5() throws MalformedURLException, URISyntaxException {
		XLIFFKitWriterStep writerStep = new XLIFFKitWriterStep();
		IPersistenceSession session = writerStep.getSession();
		
		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		writerStep.setOutputURI(new File(pathBase, "testReferences5.xliff.kit").toURI());
		writerStep.setTargetLocale(DEDE);
		net.sf.okapi.steps.xliffkit.writer.Parameters params = 
			(net.sf.okapi.steps.xliffkit.writer.Parameters) writerStep.getParameters();
		
		params.setIncludeSource(false);
		params.setIncludeOriginal(false);
		
		session.registerBean(TestEvent.class, TestEventBean2.class);
		
		TestEvent e1 = new TestEvent("e1");
		TestEvent e2 = new TestEvent("e2");
		e2.setParent(e1);
		e1.setParent(e2);

		writerStep.handleEvent(new Event(EventType.START_BATCH));
		StartDocument sd = new StartDocument("sd1");
		sd.setName("test_refs5.txt");
		sd.setLocale(ENUS);
		sd.setFilterWriter(new GenericFilterWriter(null, null));
		
		writerStep.handleEvent(new Event(EventType.START_DOCUMENT, sd));
		writerStep.handleEvent(e1);
		writerStep.handleEvent(e2);
		writerStep.handleEvent(new Event(EventType.END_DOCUMENT));
		writerStep.handleEvent(new Event(EventType.END_BATCH));
	}
	
	@Test
	public void testReferences6() throws MalformedURLException, URISyntaxException {
		XLIFFKitWriterStep writerStep = new XLIFFKitWriterStep();
		IPersistenceSession session = writerStep.getSession();
		beans.clear();
		writerStep.getSession().addObserver(writeObserver);
		
		String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
		writerStep.setOutputURI(new File(pathBase, "testReferences6.xliff.kit").toURI());
		writerStep.setTargetLocale(DEDE);
		net.sf.okapi.steps.xliffkit.writer.Parameters params = 
			(net.sf.okapi.steps.xliffkit.writer.Parameters) writerStep.getParameters();
		
		params.setIncludeSource(false);
		params.setIncludeOriginal(false);
		
		session.registerBean(TestEvent.class, TestEventBean2.class);
		
		TestEvent e1 = new TestEvent("e1");
		TestEvent e2 = new TestEvent("e2");
		TestEvent e3 = new TestEvent("e3");
		TestEvent e4 = new TestEvent("e4");
		TestEvent e5 = new TestEvent("e5");
		TestEvent e6 = new TestEvent("e6");
		TestEvent e7 = new TestEvent("e7");
		
		e1.setParent(e3);
		e3.setParent(e4);
		e2.setParent(e6);
		e7.setParent(e6);
		e5.setParent(e2);

		writerStep.handleEvent(new Event(EventType.START_BATCH));
		StartDocument sd = new StartDocument("sd1");
		sd.setName("test_refs6.txt");
		sd.setLocale(ENUS);
		sd.setFilterWriter(new GenericFilterWriter(null, null));
		
		writerStep.handleEvent(new Event(EventType.START_DOCUMENT, sd));
		writerStep.handleEvent(e1);
		writerStep.handleEvent(e2);
		writerStep.handleEvent(e3);
		writerStep.handleEvent(e4);
		writerStep.handleEvent(e5);
		writerStep.handleEvent(e6);
		writerStep.handleEvent(e7);
		writerStep.handleEvent(new Event(EventType.END_DOCUMENT));
		writerStep.handleEvent(new Event(EventType.END_BATCH_ITEM)); // Closes the session and doesn't let END_BATCH serialized
		writerStep.handleEvent(new Event(EventType.END_BATCH));
		
		assertEquals(9, beans.size());
		assertTrue(beans.get(0) instanceof EventBean);
		assertTrue(beans.get(1) instanceof TestEventBean2);
		assertTrue(beans.get(2) instanceof TestEventBean2);
		assertTrue(beans.get(3) instanceof TestEventBean2);
		assertTrue(beans.get(4) instanceof TestEventBean2);
		assertTrue(beans.get(5) instanceof TestEventBean2);
		assertTrue(beans.get(6) instanceof TestEventBean2);
		assertTrue(beans.get(7) instanceof TestEventBean2);
		assertTrue(beans.get(8) instanceof EventBean);
		
		TestEventBean2 bean = null;
		FactoryBean parent = null;
		
		// event2
		bean = (TestEventBean2) beans.get(1);
		assertEquals("e1", bean.getId());
		parent = bean.getParent();		
		assertEquals(parent.getClassName(), TestEvent.class.getName());
		assertEquals(0, parent.getReference());
		assertNotNull(parent.getContent());		
		assertTrue(parent.getContent() instanceof TestEventBean2);
		TestEventBean2 c1 = (TestEventBean2) parent.getContent();
		
		bean = (TestEventBean2) parent.getContent();
		assertEquals("e3", bean.getId());
		parent = bean.getParent();
		assertEquals(TestEvent.class.getName(), parent.getClassName());
		assertEquals(0, parent.getReference());
		assertNotNull(parent.getContent());		
		assertTrue(parent.getContent() instanceof TestEventBean2);
		TestEventBean2 c2 = (TestEventBean2) parent.getContent();
		
		bean = (TestEventBean2) parent.getContent();
		assertEquals("e4", bean.getId());
		parent = bean.getParent();
		assertEquals(null, parent.getClassName());
		assertEquals(0, parent.getReference());
		
		// event3
		bean = (TestEventBean2) beans.get(2);
		assertEquals("e2", bean.getId());
		long rid2 = bean.getRefId();
		parent = bean.getParent();
		assertEquals(parent.getClassName(), TestEvent.class.getName());
		assertEquals(0, parent.getReference());
		assertNotNull(parent.getContent());		
		assertTrue(parent.getContent() instanceof TestEventBean2);
		
		bean = (TestEventBean2) parent.getContent();
		assertEquals("e6", bean.getId());
		parent = bean.getParent();
		assertEquals(null, parent.getClassName());
		assertEquals(0, parent.getReference());
		long rid6 = bean.getRefId();
		
		// event4
		bean = (TestEventBean2) beans.get(3);
		assertNull(bean.getId());
		parent = bean.getParent();
		assertEquals(null, parent.getClassName());
		assertEquals(0, parent.getReference());
		assertNull(parent.getContent());
		assertTrue(bean.getRefId() < 0);
		assertTrue(bean.getRefId() == -c1.getRefId());
		
		// event5
		bean = (TestEventBean2) beans.get(4);
		assertNull(bean.getId());
		parent = bean.getParent();
		assertEquals(null, parent.getClassName());
		assertEquals(0, parent.getReference());
		assertNull(parent.getContent());
		assertTrue(bean.getRefId() < 0);
		assertTrue(bean.getRefId() == -c2.getRefId());
		
		// event6
		bean = (TestEventBean2) beans.get(5);
		assertEquals("e5", bean.getId());
		parent = bean.getParent();
		assertEquals(parent.getClassName(), TestEvent.class.getName());
		assertEquals(rid2, parent.getReference());
		assertNull(parent.getContent());		
		
		// event7
		bean = (TestEventBean2) beans.get(6);
		assertNull(bean.getId());
		parent = bean.getParent();
		assertEquals(null, parent.getClassName());
		assertEquals(0, parent.getReference());
		assertNull(parent.getContent());
		assertTrue(bean.getRefId() < 0);
		assertTrue(bean.getRefId() == -rid6);
		
		// event8
		bean = (TestEventBean2) beans.get(7);
		assertEquals("e7", bean.getId());
		parent = bean.getParent();
		assertEquals(parent.getClassName(), TestEvent.class.getName());
		assertEquals(rid6, parent.getReference());
		assertNull(parent.getContent());
	}
	
	@Test
	public void testStaxParser () throws XMLStreamException {		
		XMLInputFactory fact = null;
		XMLStreamReader reader = null;
		 
		logger.info("Factory class: {}", System.getProperty("javax.xml.stream.XMLInputFactory"));
		
		fact = XMLInputFactory.newInstance();
		reader = fact.createXMLStreamReader(this.getClass().getResourceAsStream("."));
		logger.info("Factory class: {}", ClassUtil.getQualifiedClassName(fact));
		logger.info("Reader class: {}", ClassUtil.getQualifiedClassName(reader));
	}
	
	@Test
	public void testStaxParser2 () throws XMLStreamException {		
		XMLInputFactory fact = null;
		XMLStreamReader reader = null;
		 
		logger.info("Factory class: {}", System.getProperty("javax.xml.stream.XMLInputFactory"));
		
		fact = XMLInputFactory.newInstance();
		reader = fact.createXMLStreamReader(this.getClass().getResourceAsStream("."));
		logger.info("Factory class: {}", ClassUtil.getQualifiedClassName(fact));
		logger.info("Reader class: {}", ClassUtil.getQualifiedClassName(reader));
		
//		System.setProperty("javax.xml.stream.XMLInputFactory","com.sun.xml.internal.stream.XMLInputFactoryImpl");
//		fact = XMLInputFactory.newInstance();
//		reader = fact.createXMLStreamReader(this.getClass().getResourceAsStream("/BinUnitTest01.xlf"));
//		logger.info("Factory class: {}", ClassUtil.getQualifiedClassName(fact));
//		logger.info("Reader class: {}", ClassUtil.getQualifiedClassName(reader));
//		
//		System.getProperties().remove("javax.xml.stream.XMLInputFactory");
//		//testSystemProperties();
//		
//		fact = XMLInputFactory.newInstance();
//		reader = fact.createXMLStreamReader(this.getClass().getResourceAsStream("/BinUnitTest01.xlf"));
//		logger.info("Factory class: {}", ClassUtil.getQualifiedClassName(fact));
//		logger.info("Reader class: {}", ClassUtil.getQualifiedClassName(reader));
	}
	
	// DEBUG 
		@Test
		public void testAnnotations() throws URISyntaxException, MalformedURLException {

			String pathBase = Util.getDirectoryName(new File(this.getClass().getResource("test2.txt").toURI()).getPath()) + "/";
			String src1Path = pathBase + "src1/";
			String src2Path = pathBase + "src2/";
			net.sf.okapi.connectors.pensieve.Parameters params = 
				new net.sf.okapi.connectors.pensieve.Parameters();
			params.setDbDirectory(pathBase + "testtm");
			
			XLIFFKitWriterStep ws = new XLIFFKitWriterStep();
			AltTranslationsAnnotation ata = new AltTranslationsAnnotation();
			ws.getSession().setAnnotation(ata);
			ata.add(LocaleId.ENGLISH, LocaleId.GERMAN, new TextFragment("original source"), new TextFragment("alternate source"), new TextFragment("alternate target"), MatchType.EXACT, 100, "tests", 100, 100);
			
			new XPipeline(
					"Test pipeline for XLIFFKitWriterStep",
					new XBatch(								
							new XBatchItem(
									new File(src1Path,  "test12.html").toURI().toURL(),
									"UTF-8",
									ENUS,
									DEDE)
							),
									
							new RawDocumentToFilterEventsStep()
					,				
					new XPipelineStep(new LeveragingStep(), 
							new XParameter("resourceClassName", net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName()),
							new XParameter("resourceParameters", params.toString(), true),
							new XParameter("threshold", 80),
							new XParameter("fillTarget", true)
					),
					
					new XPipelineStep(
							ws,								
							new XParameter("gMode", true),
							new XParameter("includeOriginal", true),
							new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
							new XParameter("outputURI", new File(pathBase, "testPackageFormat7.xliff.kit").toURI().toString()))
			).execute();
		}
}
