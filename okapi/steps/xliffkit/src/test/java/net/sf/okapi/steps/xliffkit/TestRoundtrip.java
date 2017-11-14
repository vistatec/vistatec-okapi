package net.sf.okapi.steps.xliffkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.filters.openxml.OpenXMLFilter;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XParameter;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;
import net.sf.okapi.lib.extra.steps.EventListBuilderStep;
import net.sf.okapi.lib.extra.steps.EventLogger;
import net.sf.okapi.lib.extra.steps.TextUnitLogger;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.xliffkit.reader.XLIFFKitReaderStep;
import net.sf.okapi.steps.xliffkit.writer.XLIFFKitWriterStep;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestRoundtrip {
	
	private static final LocaleId ENUS = new LocaleId("en", "us");
	private static final LocaleId FRFR = new LocaleId("fr", "fr");

    @Ignore("OpenXML skeleton is no longer serialiable")
	@Test
	public void testDoubleExtraction() throws MalformedURLException, URISyntaxException {
		String pathBase = new File(this.getClass().getResource("").toURI()).getPath() + "/";
		String src1Path = pathBase + "writer/src1/";
		String inPath = src1Path + "BoldWorld.docx";
		String midPath = pathBase + "testRoundtrip.xliff.kit";
		String outPath = pathBase + "out/";
		
		EventListBuilderStep elb1 = new EventListBuilderStep();
		EventListBuilderStep elb2 = new EventListBuilderStep();
		
		XPipeline pl = new XPipeline(
				"Test pipeline for XLIFFKitWriterStep",
				new XBatch(
						new XBatchItem(
								new File(inPath).toURI().toURL(),
								"UTF-8",
								ENUS,
								FRFR)
						),
				new RawDocumentToFilterEventsStep(new OpenXMLFilter()),
				elb1,
				//new TextUnitLogger(),
				new XPipelineStep(
						new XLIFFKitWriterStep(),								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new XParameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new File(midPath).toURI().toString()))
		);
		assertEquals(3, pl.getSteps().size());
		pl.execute();
		
		List<Event> list1 = elb1.getList();
		assertTrue(new File(midPath).exists());
				
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								new File(midPath).toURI().toURL(),
								"UTF-8",
								Util.getDirectoryName(outPath),
								"UTF-8",
								ENUS,
								ENUS)
						),
				new XLIFFKitReaderStep(),
				elb2,
				new TextUnitLogger(),
				new EventLogger()
		).execute();
		
		List<Event> list2 = elb2.getList();
		
		if ( !FilterTestDriver.compareEvents(list1, list2, true) ) {
			throw new OkapiException("Events are different for " + inPath);
		}
	}
	
	@Test
	public void testDoubleExtraction2() throws MalformedURLException, URISyntaxException {
		String pathBase = new File(this.getClass().getResource("").toURI()).getPath() + "/";
		String src1Path = pathBase + "writer/src1/";
		String inPath = src1Path + "aa324.html";
		String midPath = pathBase + "testRoundtrip.xliff.kit";
		String outPath = pathBase + "out/";
		String outFilePath = outPath + "content/target/en-US.fr-FR/" + "aa324.html";
		
		EventListBuilderStep elb1 = new EventListBuilderStep();
		EventListBuilderStep elb2 = new EventListBuilderStep();
		
		new XPipeline(
				"Test pipeline for XLIFFKitWriterStep",
				new XBatch(
						new XBatchItem(
								new File(inPath).toURI().toURL(),
								"UTF-8",
								ENUS,
								FRFR)
						),
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				elb1,
				new XPipelineStep(
						new XLIFFKitWriterStep(),								
						new XParameter("gMode", true),
						new XParameter("includeOriginal", true),
						new XParameter("message", "This document is a part of the test t-kit, generated from net.sf.okapi.steps.xliffkit.writer.testPackageFormat()"),
						//new XParameter("outputURI", this.getClass().getResource("draft4.xliff.kit").toURI().toString()))
						new XParameter("outputURI", new File(midPath).toURI().toString()))
		).execute();
		
		List<Event> list1 = elb1.getList();
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								new File(midPath).toURI().toURL(),
								"UTF-8",
								Util.getDirectoryName(outPath),
								"UTF-8",
								ENUS,
								ENUS)
						),
				new XLIFFKitReaderStep(),
				elb2
//				,
//				new EventLogger()
		).execute();
		
		List<Event> list2 = elb2.getList();
		
		if ( !FilterTestDriver.compareEvents(list1, list2, true) ) {
			throw new OkapiException("Events are different for " + inPath);
		}
		
		// And a filter roundtrip
		new XPipeline(
				"Test pipeline for XLIFFKitWriterStep",
				new XBatch(
						new XBatchItem(
								new File(inPath).toURI().toURL(),
								"UTF-8",
								ENUS,
								FRFR)
						),
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				elb1
		).execute();
		
		list1 = elb1.getList();
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								new File(outFilePath).toURI().toURL(),
								"UTF-8",
								ENUS,
								FRFR)
						),
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				elb2,
				new TextUnitLogger()
		).execute();
		
		list2 = elb2.getList();
		
		if ( !FilterTestDriver.compareEvents(list1, list2, true) ) {
			throw new OkapiException("Events are different for " + inPath);
		}
	}
}
