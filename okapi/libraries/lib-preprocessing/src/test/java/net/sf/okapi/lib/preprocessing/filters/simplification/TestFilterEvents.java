package net.sf.okapi.lib.preprocessing.filters.simplification;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.steps.EventListBuilderStep;
import net.sf.okapi.lib.extra.steps.TuDpLogger;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestFilterEvents {

	private static final LocaleId ENUS = new LocaleId("en", "us");
	
	@Test
	public void listInternalFilterEvents() throws URISyntaxException {
		IFilter filter = new HtmlFilter();
		
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("/form.html").toURI(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(filter),
				new TuDpLogger()
		).execute();
	}
	
	@Test
	public void listTransformedEvents() throws URISyntaxException {
		SimplificationFilter filter = new SimplificationFilter();
		Parameters params = filter.getParameters();
		params.setFilterConfigId("okf_html");
		
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("/form.html").toURI(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(filter),
				new TuDpLogger()
		).execute();
	}
	
	@Test
	public void testInternalFilterEvents() throws URISyntaxException {
		IFilter filter = new HtmlFilter();
		EventListBuilderStep elbs1 = new EventListBuilderStep();		
		
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("/form.html").toURI(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(filter),
				elbs1
		).execute();
		
		List<Event> list = elbs1.getList();
		DocumentPart dp;
		ITextUnit tu;
		
		assertEquals(114, list.size());
		
		dp = list.get(1).getDocumentPart();
		assertEquals("dp2", dp.getId());
		assertEquals("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=[#$$self$@%encoding]\">", dp.getSkeleton().toString());
		
		dp = list.get(93).getDocumentPart();
		assertEquals("dp53", dp.getId());
		assertEquals("<option [#$tu41]>", dp.getSkeleton().toString());
		
		dp = list.get(96).getDocumentPart();
		assertEquals("dp54", dp.getId());
		assertEquals("<option [#$tu43]>", dp.getSkeleton().toString());
		
		dp = list.get(99).getDocumentPart();
		assertEquals("dp55", dp.getId());
		assertEquals("<option [#$tu45]>", dp.getSkeleton().toString());
		
		dp = list.get(102).getDocumentPart();
		assertEquals("dp56", dp.getId());
		assertEquals("<option [#$tu47]>", dp.getSkeleton().toString());
		
		tu = list.get(85).getTextUnit();
		assertEquals("tu34", tu.getId());
		assertEquals("<input type=\"radio\" name=\"FavouriteFare\" [#$tu35] checked=\"checked\" /> Spam <input " +
				"type=\"radio\" name=\"FavouriteFare\" [#$tu36] /> Rhubarb <input type=\"radio\" name=\"FavouriteFare\" " +
				"[#$tu37] /> Honey <input type=\"radio\" name=\"FavouriteFare\" [#$tu38] /> Rum", tu.getSource().toString());
		
		tu = list.get(110).getTextUnit();
		assertEquals("tu50", tu.getId());
		assertEquals("<input type=\"submit\" [#$tu51] name=\"button1\"/>", tu.getSource().toString());
	}
	
	@Test
	public void testTransformedEvents() throws URISyntaxException {
		SimplificationFilter filter = new SimplificationFilter();
		Parameters params = filter.getParameters();
		params.setFilterConfigId("okf_html");
		EventListBuilderStep elbs1 = new EventListBuilderStep();
		
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("/form.html").toURI(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(filter),
				elbs1
		).execute();
		
		List<Event> list = elbs1.getList();
		DocumentPart dp;
		ITextUnit tu;
		
		assertEquals(131, list.size());
		
		dp = list.get(1).getDocumentPart();
		assertEquals("dp2", dp.getId());
		assertEquals("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">", dp.getSkeleton().toString());
		
		dp = list.get(105).getDocumentPart();
		assertEquals("dp53", dp.getId());
		assertEquals("<option value=\"", dp.getSkeleton().toString());
		
		dp = list.get(109).getDocumentPart();
		assertEquals("dp54", dp.getId());
		assertEquals("<option value=\"", dp.getSkeleton().toString());
		
		dp = list.get(113).getDocumentPart();
		assertEquals("dp55", dp.getId());
		assertEquals("<option value=\"", dp.getSkeleton().toString());
		
		dp = list.get(117).getDocumentPart();
		assertEquals("dp56", dp.getId());
		assertEquals("<option value=\"", dp.getSkeleton().toString());
		
		tu = list.get(96).getTextUnit();
		assertEquals("tu34", tu.getId());
		assertEquals("<input type=\"radio\" name=\"FavouriteFare\" " +
				"value=\"spam\" checked=\"checked\" /> Spam <input " +
				"type=\"radio\" name=\"FavouriteFare\" value=\"rhubarb\" " +
				"/> Rhubarb <input type=\"radio\" name=\"FavouriteFare\" " +
				"value=\"honey\" /> Honey <input type=\"radio\" name=\"" +
				"FavouriteFare\" value=\"rum\" /> Rum", tu.getSource().toString());
		
		tu = list.get(127).getTextUnit();
		assertEquals("tu50", tu.getId());
		assertEquals("<input type=\"submit\" value=\"Submit Form\" name=\"button1\"/>", tu.getSource().toString());
	}
}
