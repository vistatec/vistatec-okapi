package net.sf.okapi.steps.paraaligner;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.pipeline.EventObserver;
import net.sf.okapi.common.pipeline.Pipeline;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.plaintext.PlainTextFilter;
import net.sf.okapi.filters.xml.XMLFilter;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.common.ResourceSimplifierStep;
import net.sf.okapi.steps.sentencealigner.SentenceAlignStepTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ParagraphAlignStepTest {
	private Pipeline pipeline;
	private ParagraphAlignerStep aligner;
	private EventObserver eventObserver;
	private RawDocumentToFilterEventsStep filteringStep;

	@Before
	public void setUp() throws Exception {
		// create pipeline
		pipeline = new Pipeline();
		eventObserver = new EventObserver();
		pipeline.addObserver(eventObserver);

		// add filter step
		IFilter filter = new PlainTextFilter();
		filteringStep = new RawDocumentToFilterEventsStep(filter);
		pipeline.addStep(filteringStep);
		pipeline.addStep(new ResourceSimplifierStep());

		// add aligner step
		aligner = new ParagraphAlignerStep();

		Parameters p = new Parameters();
		aligner.setParameters(p);
		
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.plaintext.PlainTextFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.xml.XMLFilter");
		aligner.setFilterConfigurationMapper(fcMapper);
		pipeline.addStep(aligner);
	}

	@After
	public void tearDown() throws Exception {
		pipeline.destroy();
	}

	@Test
	public void OnetoOneAlign() throws URISyntaxException {
		URL url = SentenceAlignStepTest.class.getResource("/trgParas.txt");
		RawDocument t = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH);
		t.setFilterConfigId("okf_plaintext");
		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.ENGLISH);

		pipeline.startBatch();

		pipeline.process(new RawDocument(this.getClass().getResourceAsStream("/srcParas.txt"), "UTF-8",
				LocaleId.ENGLISH));

		pipeline.endBatch();

		// test we observed the correct events
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.PIPELINE_PARAMETERS, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());
		
		Event tue = el.remove(0);
		assertEquals("This is the first paragraph. It contains two sentences.", tue.getResource().toString());
		tue = el.remove(0);
		assertEquals("This is the second paragraph.", tue.getResource().toString());
		tue = el.remove(0);
		assertEquals("This is the third paragraph.", tue.getResource().toString());
		
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
	}
	
	@Test
	public void alignWithSourceDeletion() throws URISyntaxException {
		URL url = SentenceAlignStepTest.class.getResource("/trgParas.txt");
		RawDocument t = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH);
		t.setFilterConfigId("okf_plaintext");
		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.ENGLISH);

		pipeline.startBatch();

		pipeline.process(new RawDocument(this.getClass().getResourceAsStream("/srcParasMulti.txt"), "UTF-8",
				LocaleId.ENGLISH));

		pipeline.endBatch();

		// test we observed the correct events
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.PIPELINE_PARAMETERS, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());
		
		Event tue = el.remove(0);
//		ITextUnit mergeTu = tue.getTextUnit();
//		assertEquals("This is the first paragraph. It contains two sentences.", tue.getResource().toString());
//		assertEquals("This is the first paragraph. It contains two sentences. This is the second paragraph.", mergeTu.getTarget(LocaleId.ENGLISH).toString());
//		tue = el.remove(0);
		assertEquals("This is the second paragraph.", tue.getResource().toString());
		
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
	}
	
	@Test
	public void OnetoOneXmlAlign() throws URISyntaxException {
		filteringStep.setFilter(new XMLFilter());
		Parameters p = (Parameters)aligner.getParameters();
		p.setUseSkeletonAlignment(true);
		
		URL url = SentenceAlignStepTest.class.getResource("/trgXmlParas.xml");
		RawDocument t = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH);
		t.setFilterConfigId("okf_xml");
		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.ENGLISH);

		pipeline.startBatch();

		pipeline.process(new RawDocument(this.getClass().getResourceAsStream("/srcXmlParas.xml"), "UTF-8",
				LocaleId.ENGLISH));

		pipeline.endBatch();

		// test we observed the correct events
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.PIPELINE_PARAMETERS, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());
		
		Event tue = el.remove(0);
		assertEquals(" This is the first paragraph. It contains two sentences. ", tue.getResource().toString());
		tue = el.remove(0);
		assertEquals(" This is the second paragraph. ", tue.getResource().toString());
		tue = el.remove(0);
		assertEquals(" This is the third paragraph. ", tue.getResource().toString());
		
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
		
		filteringStep.setFilter(new PlainTextFilter());
		p.setUseSkeletonAlignment(false);
	}
	
	@Test
	public void DeleteInsertXmlAlign() throws URISyntaxException {
		Parameters p = (Parameters)aligner.getParameters();
		p.setUseSkeletonAlignment(true);
		filteringStep.setFilter(new XMLFilter());
		
		URL url = SentenceAlignStepTest.class.getResource("/trgXmlDeleteInsertParas.xml");
		RawDocument t = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH);
		t.setFilterConfigId("okf_xml");
		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.ENGLISH);

		pipeline.startBatch();

		pipeline.process(new RawDocument(this.getClass().getResourceAsStream("/srcXmlParas.xml"), "UTF-8",
				LocaleId.ENGLISH));

		pipeline.endBatch();

		// test we observed the correct events
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.PIPELINE_PARAMETERS, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());
		
		Event tue = el.remove(0);
		assertEquals(" This is the first paragraph. It contains two sentences. ", tue.getResource().toString());
		tue = el.remove(0);
		assertEquals(" This is the third paragraph. ", tue.getResource().toString());
		
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
		
		filteringStep.setFilter(new PlainTextFilter());
		p.setUseSkeletonAlignment(false);
	}
}
