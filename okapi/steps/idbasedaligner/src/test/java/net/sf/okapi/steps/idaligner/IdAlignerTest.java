package net.sf.okapi.steps.idaligner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipeline.Pipeline;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IdAlignerTest {
	private Pipeline pipeline;
	private IdBasedAlignerStep aligner;
	private EventObserver eventObserver;
	private RawDocumentToFilterEventsStep rawDocumentToFilterStep;

	@Before
	public void setUp() throws Exception {
		// create pipeline
		pipeline = new Pipeline();

		// add filter step
		rawDocumentToFilterStep = new RawDocumentToFilterEventsStep();
		pipeline.addStep(rawDocumentToFilterStep);

		// add aligner step
		aligner = new IdBasedAlignerStep();

		Parameters p = new Parameters();
		p.setGenerateTMX(false);
		p.setCopyToTarget(true);
		aligner.setParameters(p);
		
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.properties.PropertiesFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.html.HtmlFilter");
		rawDocumentToFilterStep.setFilterConfigurationMapper(fcMapper);
		aligner.setFilterConfigurationMapper(fcMapper);
		pipeline.addStep(aligner);
	}

	@After
	public void tearDown() throws Exception {
		pipeline.destroy();
	}

	@Test
	public void testEnglishEnglishAlignWithTuIds() throws URISyntaxException {
	    aligner.getParameters().setUseTextUnitIds(true);
	    englishEnglishAlign();
	}

	@Test
	public void IdEnglishEnglishAlign() throws URISyntaxException {
	    englishEnglishAlign();
	}

	private void englishEnglishAlign() throws URISyntaxException { 
		URI uri = IdAlignerTest.class
				.getResource("/messages_en-brief.properties").toURI();
		RawDocument t = new RawDocument(uri, "UTF-8", LocaleId.ENGLISH);
		t.setFilterConfigId("okf_properties");
		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.ENGLISH);

		rawDocumentToFilterStep.setFilterConfigurationId("okf_properties");
		
		eventObserver = new EventObserver();
		pipeline.deleteObservers();
		pipeline.addObserver(eventObserver);		
		pipeline.startBatch();

		pipeline.process(new RawDocument(uri, "UTF-8", LocaleId.ENGLISH));

		pipeline.endBatch();

		// test we observed the correct events
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.PIPELINE_PARAMETERS, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());

		Event tue = el.remove(0);
		assertEquals("Cancel", tue.getResource().toString());
		assertEquals("key.Cancel", tue.getTextUnit().getName());

		tue = el.remove(0);
		
		// get first alttranslation - should be our match
		assertNotNull(tue.getTextUnit().getTarget(LocaleId.ENGLISH).getAnnotation(AltTranslationsAnnotation.class));
		assertEquals("Unable to communicate with the <b>server</b>.", getAltTransTarget(tue.getTextUnit(), LocaleId.ENGLISH));
		
		assertEquals("Unable to communicate with the <b>server</b>.", tue.getResource().toString());
		assertEquals("key.server", tue.getTextUnit().getName());

		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
	}

	@Test
	public void IdEnglishEnglishHtmlAlign() throws URISyntaxException {
		URL url = IdAlignerTest.class.getResource("/messages_html_en-brief.properties");
		RawDocument t = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH);
		
		t.setFilterConfigId("okf_properties-html-subfilter");
		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.ENGLISH);

		eventObserver = new EventObserver();
		pipeline.deleteObservers();
		pipeline.addObserver(eventObserver);
		pipeline.startBatch();
		RawDocument s = new RawDocument(this.getClass().getResourceAsStream(
				"/messages_html_en-brief.properties"), "UTF-8", LocaleId.ENGLISH);
		rawDocumentToFilterStep.setFilterConfigurationId("okf_properties-html-subfilter");
		s.setFilterConfigId("okf_properties-html-subfilter");
		pipeline.process(s);

		pipeline.endBatch();

		// test we observed the correct events
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.PIPELINE_PARAMETERS, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.START_SUBFILTER, el.remove(0).getEventType());

		Event tue = el.remove(0);
		assertEquals("<b>Cancel</b>", tue.getResource().toString());
		assertEquals("key.Cancel_1", tue.getTextUnit().getName());
		
		assertEquals(EventType.END_SUBFILTER, el.remove(0).getEventType());
		assertEquals(EventType.DOCUMENT_PART, el.remove(0).getEventType());
		assertEquals(EventType.START_SUBFILTER, el.remove(0).getEventType());
		
		tue = el.remove(0);
		assertEquals("Unable to communicate", tue.getResource().toString());
		assertEquals("key.server_1", tue.getTextUnit().getName());
		
		assertEquals(EventType.DOCUMENT_PART, el.remove(0).getEventType());
		assertEquals(EventType.DOCUMENT_PART, el.remove(0).getEventType());
		
		tue = el.remove(0);
		assertEquals("server", tue.getResource().toString());
		assertEquals("key.server_4", tue.getTextUnit().getName());
	}

	@Test
	public void IdSourceTargetAlign() throws URISyntaxException {
		URL url = IdAlignerTest.class
				.getResource("/messages_de-brief.properties");
		RawDocument t = new RawDocument(url.toURI(), "ASCII", LocaleId.ENGLISH);
		t.setFilterConfigId("okf_properties");
		aligner.setSecondInput(t);
		aligner.setSourceLocale(LocaleId.ENGLISH);
		aligner.setTargetLocale(LocaleId.GERMAN);

		rawDocumentToFilterStep.setFilterConfigurationId("okf_properties");
		
		eventObserver = new EventObserver();
		pipeline.deleteObservers();
		pipeline.addObserver(eventObserver);
		pipeline.startBatch();

		pipeline.process(new RawDocument(this.getClass().getResourceAsStream(
				"/messages_de-brief.properties"), "ASCII", LocaleId.GERMAN));

		pipeline.endBatch();

		// test we observed the correct events
		List<Event> el = eventObserver.getResult();
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.PIPELINE_PARAMETERS, el.remove(0).getEventType());
		assertEquals(EventType.START_DOCUMENT, el.remove(0).getEventType());

		Event tue = el.remove(0);
		
		assertNotNull(tue.getTextUnit().getTarget(LocaleId.GERMAN).getAnnotation(AltTranslationsAnnotation.class));
		assertEquals("Abbrechen", getAltTransTarget(tue.getTextUnit(), LocaleId.GERMAN));
		AltTranslationsAnnotation ata = tue.getTextUnit().getTarget(LocaleId.GERMAN).getAnnotation(AltTranslationsAnnotation.class);
		assertEquals(MatchType.EXACT_UNIQUE_ID, ata.getFirst().getType());
		
		assertTrue(tue.getTextUnit().hasTarget(LocaleId.GERMAN));
		assertEquals("Abbrechen", tue.getTextUnit().getTarget(LocaleId.GERMAN).toString());

		tue = el.remove(0);
		
		assertNotNull(tue.getTextUnit().getTarget(LocaleId.GERMAN).getAnnotation(AltTranslationsAnnotation.class));
		assertEquals("Es konnte keine Verbindung zum <b>Server</b> aufgebaut werden.", getAltTransTarget(tue.getTextUnit(), LocaleId.GERMAN));
		ata = tue.getTextUnit().getTarget(LocaleId.GERMAN).getAnnotation(AltTranslationsAnnotation.class);
		assertEquals(MatchType.EXACT_UNIQUE_ID, ata.getFirst().getType());

		assertTrue(tue.getTextUnit().hasTarget(LocaleId.GERMAN));
		assertEquals("Es konnte keine Verbindung zum <b>Server</b> aufgebaut werden.", tue.getTextUnit().toString());

		assertEquals(EventType.END_DOCUMENT, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
	}
	
	private String getAltTransTarget(ITextUnit tu, LocaleId targetLocale) {
		AltTranslationsAnnotation ata = tu.getTarget(targetLocale).getAnnotation(AltTranslationsAnnotation.class); 
		return ata.getFirst().getTarget().toString();
	}
}
