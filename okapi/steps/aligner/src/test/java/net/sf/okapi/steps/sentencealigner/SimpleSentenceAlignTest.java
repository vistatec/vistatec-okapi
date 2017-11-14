package net.sf.okapi.steps.sentencealigner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipeline.EventObserver;
import net.sf.okapi.common.pipeline.Pipeline;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.filters.plaintext.PlainTextFilter;
import net.sf.okapi.filters.tmx.TmxFilter;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SimpleSentenceAlignTest {
	private Pipeline pipeline;
	private SentenceAlignerStep aligner;
	private EventObserver eventObserver;
	private RawDocumentToFilterEventsStep rawDocumentToFilterEventsStep;

	@Before
	public void setUp() throws Exception {
		// create pipeline
		pipeline = new Pipeline();
		eventObserver = new EventObserver();
		pipeline.addObserver(eventObserver);

		// add filter step
		rawDocumentToFilterEventsStep = new RawDocumentToFilterEventsStep();
		pipeline.addStep(rawDocumentToFilterEventsStep);

		// add aligner step
		aligner = new SentenceAlignerStep();

		Parameters p = new Parameters();
		p.setGenerateTMX(false);
		p.setSegmentTarget(true);
		p.setSegmentSource(true);
		p.setUseCustomTargetRules(false);
		p.setForceSimpleOneToOneAlignment(true);
		aligner.setParameters(p);
		
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.plaintext.PlainTextFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.tmx.TmxFilter");
		aligner.setFilterConfigurationMapper(fcMapper);
		pipeline.addStep(aligner);
	}
	
	@After
	public void tearDown() throws Exception {
		pipeline.destroy();
	}

	@Test
	public void sentenceEnglishEnglishAlign() throws URISyntaxException {
		rawDocumentToFilterEventsStep.setFilter(new PlainTextFilter());
		URL url = SimpleSentenceAlignTest.class.getResource("/src.txt");
		RawDocument t = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH);
		t.setFilterConfigId("okf_plaintext");
		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.ENGLISH);

		pipeline.startBatch();

		try(RawDocument rd = new RawDocument(this.getClass().getResourceAsStream("/src.txt"), "UTF-8",	LocaleId.ENGLISH)) {
			pipeline.process(rd);
			pipeline.endBatch();
			t.close();
		}

		// test we observed the correct events
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());
		Event tue = el.remove(0);
		assertEquals("Mr. Holmes is from the U.K. not the U.S. Is Dr. Watson from there too?", tue
				.getResource().toString());
		assertEquals(EventType.TEXT_UNIT, tue.getEventType());
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
	}

	@Test
	public void sentenceAlignMultimatch() throws URISyntaxException {
		rawDocumentToFilterEventsStep.setFilter(new PlainTextFilter());
		URL url = SimpleSentenceAlignTest.class.getResource("/trgMultimatch.txt");
		RawDocument t = new RawDocument(url.toURI(), "UTF-8", LocaleId.fromString("pt"));
		t.setFilterConfigId("okf_plaintext");

		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.PORTUGUESE);

		pipeline.startBatch();
		try(RawDocument rd = new RawDocument(this.getClass().getResourceAsStream("/srcMultimatch.txt"), "UTF-8", LocaleId.ENGLISH)) {
			pipeline.process(rd);
			pipeline.endBatch();
			t.close();
		}

		// test we observed the correct events
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());
		Event tue = el.remove(0);
		assertEquals("The First Darlek Empire has written: \"The simplest statement we know of is the " +
				"statement of Davross himself, namely, that the members of the empire should destroy " +
				"'all life forms,' which is understood to mean universal destruction. " +
				"No one is justified " +
				"in making any other statement than this\" (First Darlek Empire letter, Mar. 12, 3035; see " +
				"also DE 11:4).",
				tue.getTextUnit().getAlignedSegments().getSource(0, LocaleId.PORTUGUESE).toString());
		assertEquals(EventType.TEXT_UNIT, tue.getEventType());
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
	}
	
	@Test
	public void sentenceAlignMultimatchCollpasewhitespace() throws URISyntaxException {
		rawDocumentToFilterEventsStep.setFilter(new PlainTextFilter());
		URL url = SimpleSentenceAlignTest.class.getResource("/trgMultimatch.txt");
		RawDocument t = new RawDocument(url.toURI(), "UTF-8", LocaleId.fromString("pt"));
		t.setFilterConfigId("okf_plaintext");

		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.PORTUGUESE);
		Parameters p = (Parameters)aligner.getParameters();
		p.setForceSimpleOneToOneAlignment(true);
		p.setCollapseWhitespace(true);
		aligner.setParameters(p);
		
		pipeline.startBatch();

		try(RawDocument rd = new RawDocument(this.getClass().getResourceAsStream("/srcMultimatch.txt"), "UTF-8", LocaleId.ENGLISH)) {
			pipeline.process(rd);
			pipeline.endBatch();
			t.close();
		}

		// test we observed the correct events
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());
		Event tue = el.remove(0);
		assertEquals("The First Darlek Empire has written: \"The simplest statement we know of is the " +
				"statement of Davross himself, namely, that the members of the empire should destroy " +
				"'all life forms,' which is understood to mean universal destruction. " +
				"No one is justified " +
				"in making any other statement than this\" (First Darlek Empire letter, Mar. 12, 3035; see " +
				"also DE 11:4).",
				tue.getTextUnit().getAlignedSegments().getSource(0, LocaleId.PORTUGUESE).toString());
		assertEquals(EventType.TEXT_UNIT, tue.getEventType());
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
	}
	
	@Test
	public void sentenceAlignOnetoOneOnly() throws URISyntaxException {
		rawDocumentToFilterEventsStep.setFilter(new TmxFilter());
		URL url = SimpleSentenceAlignTest.class.getResource("/one_to_one1.tmx");
		RawDocument in = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH);
		in.setFilterConfigId("okf_tmx");

		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.FRENCH);
		
		pipeline.startBatch();
		pipeline.process(in);
		pipeline.endBatch();

		// simple aligner must collapse sentences to align
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.DOCUMENT_PART, el.remove(0).getEventType());
		Event tue = el.remove(0);
		TextContainer srcCont = tue.getTextUnit().getSource(); 
		assertFalse(srcCont.isEmpty());
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
		in.close();
	}
	
	@Test
	public void sentenceAlignOnetoOneOnlyTwo() throws URISyntaxException {
		rawDocumentToFilterEventsStep.setFilter(new TmxFilter());
		URL url = SimpleSentenceAlignTest.class.getResource("/one_to_one2.tmx");
		RawDocument in = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH);
		in.setFilterConfigId("okf_tmx");

		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.FRENCH);
		
		pipeline.startBatch();
		pipeline.process(in);
		pipeline.endBatch();

		// simple aligner must collapse sentences to align
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.DOCUMENT_PART, el.remove(0).getEventType());
		Event tue = el.remove(0);
		TextContainer srcCont = tue.getTextUnit().getSource(); 
		assertFalse(srcCont.isEmpty());
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
		in.close();
	}
}
