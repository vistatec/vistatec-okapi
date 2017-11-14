package net.sf.okapi.lib.preprocessing.filters.simplification;


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.steps.EventLogger;
import net.sf.okapi.lib.extra.steps.TuDpLogger;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestSubfilter_Original {

	private IFilter filter;
	private LocaleId locEN = LocaleId.ENGLISH;
	
	@Before
	public void startUp() throws URISyntaxException {
		filter = FilterUtil.createFilter(
				this.getClass().getResource("/subfilters/okf_xmlstream@microcustom2.fprm"));
	}
	
	@Test
	public void testEvents() throws URISyntaxException {
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("/subfilters/import8971089963360986920.xml").toURI(),
								"UTF-8",
								locEN)
						),
						
				new RawDocumentToFilterEventsStep(filter),
				new EventLogger()
		).execute();
	}
	
	@Test
	public void testTuDpEvents() throws URISyntaxException {
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("/subfilters/import8971089963360986920.xml").toURI(),
								"UTF-8",
								locEN)
						),
						
				new RawDocumentToFilterEventsStep(filter),
				new TuDpLogger()
		).execute();
	}
	
	@Test
	public void testDoubleExtraction() throws URISyntaxException, IOException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		
		list.add(new InputDocument(this.getClass().getResource("/subfilters/import8971089963360986920.xml").toURI().getPath(), null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locEN, "out"));
	}
	
}
