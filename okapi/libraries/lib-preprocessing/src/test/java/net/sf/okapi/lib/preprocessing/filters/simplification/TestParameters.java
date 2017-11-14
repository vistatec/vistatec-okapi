package net.sf.okapi.lib.preprocessing.filters.simplification;

import java.net.URISyntaxException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestParameters {

	private static final LocaleId ENUS = new LocaleId("en", "us");
	
	@Test
	public void testParameters() throws URISyntaxException {
		SimplificationFilter filter = new SimplificationFilter();
		
		Parameters params =	filter.getParameters();
		params.setSimplifyResources(true);
		params.setSimplifyCodes(false);
		
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("/form.html").toURI(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(filter)
		).execute();
	}
	
	@Test(expected = OkapiBadFilterParametersException.class)
	public void testParameters_NullParameters() throws URISyntaxException {
		SimplificationFilter filter = new SimplificationFilter();
		filter.setParameters(null);		
		
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("/form.html").toURI(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(filter)
		).execute();
	}
	
	@Test(expected = OkapiBadFilterParametersException.class)
	public void testParameters_NullConfigId() throws URISyntaxException {
		SimplificationFilter filter = new SimplificationFilter();
		Parameters params =	filter.getParameters();
		params.setFilterConfigId(null);		
		
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("/form.html").toURI(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(filter)
		).execute();
	}
	
	@Test(expected = OkapiBadFilterParametersException.class)
	public void testParameters_EmptyConfigId() throws URISyntaxException {
		SimplificationFilter filter = new SimplificationFilter();
		Parameters params =	filter.getParameters();
		params.setFilterConfigId("");		
		
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("/form.html").toURI(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(filter)
		).execute();
	}
	
	@Test(expected = OkapiBadFilterParametersException.class)
	public void testParameters_NonexistentConfigId() throws URISyntaxException {
		SimplificationFilter filter = new SimplificationFilter();
		Parameters params =	filter.getParameters();
		params.setFilterConfigId("okf_bogus");		
		
		new XPipeline(
				null,
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("/form.html").toURI(),
								"UTF-8",
								ENUS)
						),
						
				new RawDocumentToFilterEventsStep(filter)
		).execute();
	}
}
