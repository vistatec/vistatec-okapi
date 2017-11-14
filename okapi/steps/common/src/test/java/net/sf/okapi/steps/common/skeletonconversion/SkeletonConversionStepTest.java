package net.sf.okapi.steps.common.skeletonconversion;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.steps.EventListBuilderStep;
import net.sf.okapi.lib.extra.steps.EventLogger;
import net.sf.okapi.lib.extra.steps.TuDpLogger;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SkeletonConversionStepTest {

private static final LocaleId ENUS = new LocaleId("en", "us");
	private final FileLocation pathBase = FileLocation.fromClass(this.getClass());
	
	@Test
	public void testDoubleExtraction () throws URISyntaxException, IOException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();				
		
		list.add(new InputDocument(pathBase.in("aa324.html").toString(), null));
		list.add(new InputDocument(pathBase.in("form.html").toString(), null));
		list.add(new InputDocument(pathBase.in("W3CHTMHLTest1.html").toString(), null));
		list.add(new InputDocument(pathBase.in("msg00058.html").toString(), null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		
		SkeletonConversionStep sks = new SkeletonConversionStep();
		
		assertTrue(rtc.executeCompare(new HtmlFilter(), list, "UTF-8", ENUS, ENUS, "skeleton", sks));
	}
	
	@Test
	public void testEvents() throws MalformedURLException {
		EventListBuilderStep elbs1 = new EventListBuilderStep();
		EventListBuilderStep elbs2 = new EventListBuilderStep();
		
		new XPipeline(
				"Test pipeline for SkeletonConversionStepTest",
				new XBatch(
						new XBatchItem(
								pathBase.in("form.html").asUrl(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				elbs1,
				new SkeletonConversionStep(),
				new TuDpLogger(),
				elbs2
		).execute();		
	}
	
	@Test
	public void testEvents2() throws MalformedURLException {
		EventListBuilderStep elbs1 = new EventListBuilderStep();
		EventListBuilderStep elbs2 = new EventListBuilderStep();
		
		new XPipeline(
				"Test pipeline for SkeletonConversionStepTest",
				new XBatch(
						new XBatchItem(
								pathBase.in("msg00058.html").asUrl(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				elbs1,
				new SkeletonConversionStep(),
				new EventLogger(),
				new TuDpLogger(),
				elbs2
		).execute();
	}

}
