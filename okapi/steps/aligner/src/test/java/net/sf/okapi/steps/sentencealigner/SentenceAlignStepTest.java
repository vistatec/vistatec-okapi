package net.sf.okapi.steps.sentencealigner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.pipeline.EventObserver;
import net.sf.okapi.common.pipeline.Pipeline;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.filters.plaintext.PlainTextFilter;
import net.sf.okapi.filters.tmx.TmxFilter;
import net.sf.okapi.lib.segmentation.SRXDocument;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SentenceAlignStepTest {
	private Pipeline pipeline;
	private SentenceAlignerStep aligner;
	private EventObserver eventObserver;
	private Pipeline tmxPipeline;
	private SentenceAlignerStep tmxAligner;
	private EventObserver tmxEventObserver;

	@Before
	public void setUp() throws Exception {
		// create pipeline
		pipeline = new Pipeline();
		eventObserver = new EventObserver();
		pipeline.addObserver(eventObserver);

		// add filter step
		IFilter filter = new PlainTextFilter();
		pipeline.addStep(new RawDocumentToFilterEventsStep(filter));

		// add aligner step
		aligner = new SentenceAlignerStep();

		Parameters p = new Parameters();
		p.setGenerateTMX(false);
		p.setSegmentTarget(true);
		p.setSegmentSource(true);
		p.setUseCustomTargetRules(false);
		aligner.setParameters(p);
		
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.plaintext.PlainTextFilter");
		aligner.setFilterConfigurationMapper(fcMapper);
		pipeline.addStep(aligner);
	}

	@Before
	public void setUpTmx() throws Exception {
		// create pipeline
		tmxPipeline = new Pipeline();
		tmxEventObserver = new EventObserver();
		tmxPipeline.addObserver(tmxEventObserver);

		// add filter step
		IFilter tmxFilter = new TmxFilter();
		tmxPipeline.addStep(new RawDocumentToFilterEventsStep(tmxFilter));

		// add aligner step
		tmxAligner = new SentenceAlignerStep();

		Parameters p = new Parameters();
		p.setGenerateTMX(false);
		p.setSegmentTarget(true);
		p.setSegmentSource(true);
		p.setUseCustomTargetRules(false);
		p.setOutputOneTOneMatchesOnly(true);
		tmxAligner.setParameters(p);
		
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.tmx.TmxFilter");
		tmxAligner.setFilterConfigurationMapper(fcMapper);
		tmxPipeline.addStep(tmxAligner);
	}
	
	@After
	public void tearDown() throws Exception {
		pipeline.destroy();
		tmxPipeline.destroy();
	}

	@Test
	public void sentenceEnglishEnglishAlign() throws URISyntaxException {
		URL url = SentenceAlignStepTest.class.getResource("/src.txt");
		RawDocument t = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH);
		t.setFilterConfigId("okf_plaintext");
		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.ENGLISH);

		pipeline.startBatch();

		try (RawDocument rd = new RawDocument(this.getClass().getResourceAsStream("/src.txt"), "UTF-8", LocaleId.ENGLISH)) {
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
		URL url = SentenceAlignStepTest.class.getResource("/trgMultimatch.txt");
		RawDocument t = new RawDocument(url.toURI(), "UTF-8", LocaleId.fromString("pt"));
		t.setFilterConfigId("okf_plaintext");

		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.PORTUGUESE);

		pipeline.startBatch();

		try(RawDocument rd = new RawDocument(this.getClass().getResourceAsStream("/srcMultimatch.txt"),
				"UTF-8", LocaleId.ENGLISH)) {
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
				"'all life forms,' which is understood to mean universal destruction.",
				tue.getTextUnit().getAlignedSegments().getSource(0, LocaleId.PORTUGUESE).toString());
		assertEquals(
				"No one is justified " +
				"in making any other statement than this\" (First Darlek Empire letter, Mar. 12, 3035; see " +
				"also DE 11:4).",
				tue.getTextUnit().getAlignedSegments().getSource(1, LocaleId.PORTUGUESE).toString());
		assertEquals(EventType.TEXT_UNIT, tue.getEventType());
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
	}
	
	@Test
	public void sentenceAlignMultimatchCollpasewhitespace() throws URISyntaxException {
		URL url = SentenceAlignStepTest.class.getResource("/trgMultimatch.txt");
		RawDocument t = new RawDocument(url.toURI(), "UTF-8", LocaleId.fromString("pt"));
		t.setFilterConfigId("okf_plaintext");

		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.PORTUGUESE);
		Parameters p = (Parameters)aligner.getParameters();
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
				"'all life forms,' which is understood to mean universal destruction." ,
				tue.getTextUnit().getAlignedSegments().getSource(0, LocaleId.PORTUGUESE).toString());
		assertEquals(
				"No one is justified " +
				"in making any other statement than this\" (First Darlek Empire letter, Mar. 12, 3035; see " +
				"also DE 11:4).",
				tue.getTextUnit().getAlignedSegments().getSource(1, LocaleId.PORTUGUESE).toString());
		assertEquals(EventType.TEXT_UNIT, tue.getEventType());
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
	}
	
	@Test
	public void sentenceAlignOnetoOneOnly() throws URISyntaxException {
		URL url = SentenceAlignStepTest.class.getResource("/one_to_one1.tmx");
		RawDocument in = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH);
		in.setFilterConfigId("okf_tmx");

		tmxAligner.setSourceLocale(LocaleId.ENGLISH);
		tmxAligner.setTargetLocale(LocaleId.FRENCH);
		
		tmxPipeline.startBatch();
		tmxPipeline.process(in);
		tmxPipeline.endBatch();

		// There are no one to one alignments so we see no aligned segments
		List<Event> el = tmxEventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.DOCUMENT_PART, el.remove(0).getEventType());
		Event tue = el.remove(0);
		TextContainer srcCont = tue.getTextUnit().getSource(); 
		assertTrue(srcCont.isEmpty());
		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
		in.close();
	}
	
	@Test
	public void sentenceAlignOnetoOneOnlyTwo() throws URISyntaxException {
		URL url = SentenceAlignStepTest.class.getResource("/one_to_one2.tmx");
		RawDocument in = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH);
		in.setFilterConfigId("okf_tmx");

		tmxAligner.setSourceLocale(LocaleId.ENGLISH);
		tmxAligner.setTargetLocale(LocaleId.FRENCH);
		
		tmxPipeline.startBatch();
		tmxPipeline.process(in);
		tmxPipeline.endBatch();

		// There are no one to one alignments so we see no TU's
		List<Event> el = tmxEventObserver.getResult();
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
	public void testDefaultGCAlignerSegmentation () {
		SRXDocument doc = new SRXDocument();
		InputStream is = SentenceAlignerStep.class.getResourceAsStream("/net/sf/okapi/steps/gcaligner/default.srx");
		doc.loadRules(is);
		ISegmenter seg = doc.compileLanguageRules(LocaleId.ENGLISH, null);

		TextFragment tf = new TextFragment();
		tf.setCodedText("Test 1.2. test 2.");
		TextContainer tc = new TextContainer(tf);
		seg.computeSegments(tc);
		tc.getSegments().create(seg.getRanges());
		assertEquals(2, tc.getParts().size());
	}
	
}
