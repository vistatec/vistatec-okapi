package net.sf.okapi.steps.scopingreport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.extra.steps.TextUnitLogger;
import net.sf.okapi.lib.reporting.ReportGenerator;
import net.sf.okapi.steps.leveraging.LeveragingStep;
import net.sf.okapi.steps.wordcount.CharacterCountStep;
import net.sf.okapi.steps.wordcount.WordCountStep;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class TestFields {

	private ReportGenerator gen;
	
	@Before
	public void startup() throws URISyntaxException {
		ScopingReportStep srs;
		WordCountStep wcs;
		CharacterCountStep ccs;
		StartDocument sd;
		Ending ed;
		Event sdEvent, edEvent;
		Event sbEvent, ebEvent;
		ITextUnit tu1, tu2, tu3, tu4;
		Event tuEvent1, tuEvent2, tuEvent3, tuEvent4;
		Logger localLogger = LoggerFactory.getLogger(getClass());
		
		String pathBase = Util.getDirectoryName(this.getClass().getResource("").toURI().getPath()) + "/";
		net.sf.okapi.connectors.pensieve.Parameters rparams = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		rparams.setDbDirectory(pathBase + "testtm");
		
		String outputFilePath = pathBase + "out/test_scoping_report6.txt";
		
		sbEvent = new Event(EventType.START_BATCH);
		ebEvent = new Event(EventType.END_BATCH);
		Event siEvent = new Event(EventType.START_BATCH_ITEM);
		Event eiEvent = new Event(EventType.END_BATCH_ITEM);
		
		sd = new StartDocument("sd");		
		sd.setLocale(LocaleId.ENGLISH);
		sdEvent = new Event(EventType.START_DOCUMENT, sd);
		
		ed = new Ending("ed");
		edEvent = new Event(EventType.END_DOCUMENT, ed);
		
		tu1 = new TextUnit("tu1");
		tu1.setSource(new TextContainer("Elephants cannot fly."));
		tuEvent1 = new Event(EventType.TEXT_UNIT, tu1);
		
		tu2 = new TextUnit("tu2");
		tu2.setSource(new TextContainer("Elephants can't fly."));
		tuEvent2 = new Event(EventType.TEXT_UNIT, tu2);
		
		tu3 = new TextUnit("tu3");
		tu3.setSource(new TextContainer("Elephants can fly."));
		tuEvent3 = new Event(EventType.TEXT_UNIT, tu3);
		
		tu4 = new TextUnit("tu4");
		tu4.setSource(new TextContainer("Airplanes can fly."));
		tuEvent4 = new Event(EventType.TEXT_UNIT, tu4);
		
		LeveragingStep ls = new LeveragingStep();
		ls.setSourceLocale(LocaleId.ENGLISH);
		ls.setTargetLocale(LocaleId.FRENCH);
		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
		params.setResourceParameters(rparams.toString());
		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
		params.setThreshold(60);
		params.setFillTarget(true);
		
		wcs = new WordCountStep();
		ccs = new CharacterCountStep();
		srs = new ScopingReportStep();
		srs.setSourceLocale(LocaleId.ENGLISH);
		srs.setTargetLocale(LocaleId.FRENCH);
		Parameters params2 = (Parameters) srs.getParameters();
		params2.setProjectName("test_project");
		params2.setOutputPath(outputFilePath);
		params2.setCustomTemplateURI(this.getClass().getResource("golden_file_template2.txt").toURI().getPath());
		params2.setCountAsNonTranslatable_ExactMatch(true);
		params2.setCountAsNonTranslatable_GMXFuzzyMatch(true);
		sd.setName(params2.getCustomTemplateURI());
		
		wcs.handleEvent(sbEvent);
		wcs.handleEvent(siEvent);
		wcs.handleEvent(sdEvent);
		wcs.handleEvent(tuEvent1);
		wcs.handleEvent(tuEvent2);
		wcs.handleEvent(tuEvent3);
		wcs.handleEvent(tuEvent4);
		wcs.handleEvent(edEvent);
		wcs.handleEvent(eiEvent);
		wcs.handleEvent(ebEvent);
		
		ccs.handleEvent(sbEvent);
		ccs.handleEvent(siEvent);
		ccs.handleEvent(sdEvent);
		ccs.handleEvent(tuEvent1);
		ccs.handleEvent(tuEvent2);
		ccs.handleEvent(tuEvent3);
		ccs.handleEvent(tuEvent4);
		ccs.handleEvent(edEvent);
		ccs.handleEvent(eiEvent);
		ccs.handleEvent(ebEvent);
		
		ls.handleEvent(sbEvent);
		ls.handleEvent(siEvent);
		ls.handleEvent(sdEvent);
		ls.handleEvent(tuEvent1);
		ls.handleEvent(tuEvent2);
		ls.handleEvent(tuEvent3);
		ls.handleEvent(tuEvent4);
		ls.handleEvent(edEvent);
		ls.handleEvent(eiEvent);
		ls.handleEvent(ebEvent);
		
		srs.handleEvent(sbEvent);
		srs.handleEvent(siEvent);
		srs.handleEvent(sdEvent);
		srs.handleEvent(tuEvent1);
		srs.handleEvent(tuEvent2);
		srs.handleEvent(tuEvent3);
		srs.handleEvent(tuEvent4);
		srs.handleEvent(edEvent);
		srs.handleEvent(eiEvent);
		srs.handleEvent(ebEvent);
				
		localLogger.debug(TextUnitLogger.getTuInfo(tu1, LocaleId.ENGLISH));
		localLogger.debug(TextUnitLogger.getTuInfo(tu2, LocaleId.ENGLISH));
		localLogger.debug(TextUnitLogger.getTuInfo(tu3, LocaleId.ENGLISH));
		localLogger.debug(TextUnitLogger.getTuInfo(tu4, LocaleId.ENGLISH));
		
		gen = srs.getReportGenerator();
	}
	
	private long getField(String fieldsName) {
		return Util.strToLong(gen.getField(fieldsName), 0L);
	}
	
	@Test
	public void total_counts_should_be_greater_or_equal_to_the_sum_of_categories_in_every_group() {
		long totalWords = getField(ScopingReportStep.PROJECT_TOTAL_WORD_COUNT);
		long totalCharacters = getField(ScopingReportStep.PROJECT_TOTAL_CHARACTER_COUNT);
		
		// Okapi Words
		long count = 0;
		
		count += getField(ScopingReportStep.PROJECT_EXACT_UNIQUE_ID);
		count += getField(ScopingReportStep.PROJECT_EXACT_PREVIOUS_VERSION);
		count += getField(ScopingReportStep.PROJECT_EXACT_LOCAL_CONTEXT);
		count += getField(ScopingReportStep.PROJECT_EXACT_DOCUMENT_CONTEXT);
		count += getField(ScopingReportStep.PROJECT_EXACT_STRUCTURAL);
		count += getField(ScopingReportStep.PROJECT_EXACT);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY);
		count += getField(ScopingReportStep.PROJECT_EXACT_REPAIRED);
		count += getField(ScopingReportStep.PROJECT_FUZZY_PREVIOUS_VERSION);
		count += getField(ScopingReportStep.PROJECT_FUZZY_UNIQUE_ID);
		count += getField(ScopingReportStep.PROJECT_FUZZY);
		count += getField(ScopingReportStep.PROJECT_FUZZY_REPAIRED);
		count += getField(ScopingReportStep.PROJECT_PHRASE_ASSEMBLED);
		count += getField(ScopingReportStep.PROJECT_MT);
		count += getField(ScopingReportStep.PROJECT_CONCORDANCE);
		
		assertTrue(totalWords >= count);
		
		// Okapi Characters
		count = 0;
		
		count += getField(ScopingReportStep.PROJECT_EXACT_UNIQUE_ID_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_PREVIOUS_VERSION_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_LOCAL_CONTEXT_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_DOCUMENT_CONTEXT_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_STRUCTURAL_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_REPAIRED_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_FUZZY_PREVIOUS_VERSION_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_FUZZY_UNIQUE_ID_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_FUZZY_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_FUZZY_REPAIRED_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_PHRASE_ASSEMBLED_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_MT_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_CONCORDANCE_CHARACTERS);
		
		assertTrue(totalCharacters >= count);
		
		// GMX Words
		count = 0;
		
		count += getField(ScopingReportStep.PROJECT_GMX_PROTECTED_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_EXACT_MATCHED_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_LEVERAGED_MATCHED_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_REPETITION_MATCHED_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_FUZZY_MATCHED_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT);
		
		assertTrue(totalWords >= count);
		
		// GMX Words
		count = 0;
		
		count += getField(ScopingReportStep.PROJECT_GMX_PROTECTED_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_EXACT_MATCHED_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_LEVERAGED_MATCHED_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_REPETITION_MATCHED_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_FUZZY_MATCHED_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_CHARACTER_COUNT);
		
		assertTrue(totalCharacters >= count);
	}
	
	@Test
	public void total_counts_should_be_equal_to_the_sum_of_categories_and_nocategory() {
		long totalWords = getField(ScopingReportStep.PROJECT_TOTAL_WORD_COUNT);
		long totalCharacters = getField(ScopingReportStep.PROJECT_TOTAL_CHARACTER_COUNT);
		
		// Okapi Words
		long count = 0;
		
		count += getField(ScopingReportStep.PROJECT_EXACT_UNIQUE_ID);
		count += getField(ScopingReportStep.PROJECT_EXACT_PREVIOUS_VERSION);
		count += getField(ScopingReportStep.PROJECT_EXACT_LOCAL_CONTEXT);
		count += getField(ScopingReportStep.PROJECT_EXACT_DOCUMENT_CONTEXT);
		count += getField(ScopingReportStep.PROJECT_EXACT_STRUCTURAL);
		count += getField(ScopingReportStep.PROJECT_EXACT);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY);
		count += getField(ScopingReportStep.PROJECT_EXACT_REPAIRED);
		count += getField(ScopingReportStep.PROJECT_FUZZY_PREVIOUS_VERSION);
		count += getField(ScopingReportStep.PROJECT_FUZZY_UNIQUE_ID);
		count += getField(ScopingReportStep.PROJECT_FUZZY);
		count += getField(ScopingReportStep.PROJECT_FUZZY_REPAIRED);
		count += getField(ScopingReportStep.PROJECT_PHRASE_ASSEMBLED);
		count += getField(ScopingReportStep.PROJECT_MT);
		count += getField(ScopingReportStep.PROJECT_CONCORDANCE);
		
		assertTrue(totalWords == count + getField(ScopingReportStep.PROJECT_NOCATEGORY));
		
		// Okapi Characters
		count = 0;
		
		count += getField(ScopingReportStep.PROJECT_EXACT_UNIQUE_ID_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_PREVIOUS_VERSION_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_LOCAL_CONTEXT_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_DOCUMENT_CONTEXT_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_STRUCTURAL_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_EXACT_REPAIRED_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_FUZZY_PREVIOUS_VERSION_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_FUZZY_UNIQUE_ID_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_FUZZY_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_FUZZY_REPAIRED_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_PHRASE_ASSEMBLED_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_MT_CHARACTERS);
		count += getField(ScopingReportStep.PROJECT_CONCORDANCE_CHARACTERS);
		
		assertTrue(totalCharacters == count + getField(ScopingReportStep.PROJECT_NOCATEGORY_CHARACTERS));
		
		// GMX Words
		count = 0;
		
		count += getField(ScopingReportStep.PROJECT_GMX_PROTECTED_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_EXACT_MATCHED_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_LEVERAGED_MATCHED_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_REPETITION_MATCHED_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_FUZZY_MATCHED_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT);
		
		assertTrue(totalWords == count + getField(ScopingReportStep.PROJECT_GMX_NOCATEGORY));
		
		// GMX Characters
		count = 0;
		
		count += getField(ScopingReportStep.PROJECT_GMX_PROTECTED_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_EXACT_MATCHED_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_LEVERAGED_MATCHED_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_REPETITION_MATCHED_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_FUZZY_MATCHED_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT);
		count += getField(ScopingReportStep.PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_CHARACTER_COUNT);
		
		assertTrue(totalCharacters == count + getField(ScopingReportStep.PROJECT_GMX_NOCATEGORY_CHARACTERS));
	}
	
	@Test
	public void total_counts_should_be_equal_to_the_sum_of_translatable_and_nontranslatable() {
		long total = getField(ScopingReportStep.PROJECT_TOTAL_WORD_COUNT);
		
		assertTrue(total == getField(ScopingReportStep.PROJECT_TRANSLATABLE_WORD_COUNT) + 
				getField(ScopingReportStep.PROJECT_NONTRANSLATABLE_WORD_COUNT));
		
		assertTrue(total == getField(ScopingReportStep.PROJECT_GMX_TRANSLATABLE_WORD_COUNT) + 
				getField(ScopingReportStep.PROJECT_GMX_NONTRANSLATABLE_WORD_COUNT));
		
		total = getField(ScopingReportStep.PROJECT_TOTAL_CHARACTER_COUNT);
		
		assertTrue(total == getField(ScopingReportStep.PROJECT_TRANSLATABLE_CHARACTER_COUNT) + 
				getField(ScopingReportStep.PROJECT_NONTRANSLATABLE_CHARACTER_COUNT));
		
		assertTrue(total == getField(ScopingReportStep.PROJECT_GMX_TRANSLATABLE_CHARACTER_COUNT) + 
				getField(ScopingReportStep.PROJECT_GMX_NONTRANSLATABLE_CHARACTER_COUNT));
	}
	
	//------------------------------
	
	private void testValue(String name, String value) {
		assertTrue(gen.getField(name).indexOf(name) == -1); // not [?FIELD_NAME]
		assertEquals(value, gen.getField(name));
	}
	
	private void testValue(String name, long value) {
		assertTrue(gen.getField(name).indexOf(name) == -1); // not [?FIELD_NAME]
		assertEquals(value, getField(name));
	}
	
//	private void testNotEmpty(String name) {
//		assertTrue(!Util.isEmpty(gen.getField(name)));
//		assertTrue(gen.getField(name).indexOf(name) == -1); // not [?FIELD_NAME]
//	}
	
	@Test
	public void testFields() {
		testValue(ScopingReportStep.PROJECT_NAME, "test_project");
		//testNotEmpty(ScopingReportStep.PROJECT_DATE);
		testValue(ScopingReportStep.PROJECT_SOURCE_LOCALE, "en");
		testValue(ScopingReportStep.PROJECT_TARGET_LOCALE, "fr");
		testValue(ScopingReportStep.PROJECT_TOTAL_WORD_COUNT, 12L);
		//testNotEmpty(ScopingReportStep.ITEM_NAME);
		testValue(ScopingReportStep.ITEM_SOURCE_LOCALE, "en");
		testValue(ScopingReportStep.ITEM_TARGET_LOCALE, "fr");
		testValue(ScopingReportStep.ITEM_TOTAL_WORD_COUNT, 12L);
		testValue(ScopingReportStep.PROJECT_GMX_PROTECTED_WORD_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_EXACT_MATCHED_WORD_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_LEVERAGED_MATCHED_WORD_COUNT, 3L);
		testValue(ScopingReportStep.PROJECT_GMX_REPETITION_MATCHED_WORD_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_FUZZY_MATCHED_WORD_COUNT, 6L);
		testValue(ScopingReportStep.PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_NONTRANSLATABLE_WORD_COUNT, 6L);
		testValue(ScopingReportStep.PROJECT_GMX_TRANSLATABLE_WORD_COUNT, 6L);
		testValue(ScopingReportStep.PROJECT_GMX_NOCATEGORY, 3L);
		testValue(ScopingReportStep.ITEM_GMX_PROTECTED_WORD_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_EXACT_MATCHED_WORD_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_LEVERAGED_MATCHED_WORD_COUNT, 3L);
		testValue(ScopingReportStep.ITEM_GMX_REPETITION_MATCHED_WORD_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_FUZZY_MATCHED_WORD_COUNT, 6L);
		testValue(ScopingReportStep.ITEM_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_NONTRANSLATABLE_WORD_COUNT, 6L);
		testValue(ScopingReportStep.ITEM_GMX_TRANSLATABLE_WORD_COUNT, 6L);
		testValue(ScopingReportStep.ITEM_GMX_NOCATEGORY, 3L);
		testValue(ScopingReportStep.PROJECT_EXACT_UNIQUE_ID, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_PREVIOUS_VERSION, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_LOCAL_CONTEXT, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_DOCUMENT_CONTEXT, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_STRUCTURAL, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT, 3L);
		testValue(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_REPAIRED, 0L);
		testValue(ScopingReportStep.PROJECT_FUZZY_PREVIOUS_VERSION, 0L);
		testValue(ScopingReportStep.PROJECT_FUZZY_UNIQUE_ID, 0L);
		testValue(ScopingReportStep.PROJECT_FUZZY, 6L);
		testValue(ScopingReportStep.PROJECT_FUZZY_REPAIRED, 0L);
		testValue(ScopingReportStep.PROJECT_PHRASE_ASSEMBLED, 0L);
		testValue(ScopingReportStep.PROJECT_MT, 0L);
		testValue(ScopingReportStep.PROJECT_CONCORDANCE, 0L);
		testValue(ScopingReportStep.PROJECT_NONTRANSLATABLE_WORD_COUNT, 3L);
		testValue(ScopingReportStep.PROJECT_TRANSLATABLE_WORD_COUNT, 9L);
		testValue(ScopingReportStep.PROJECT_NOCATEGORY, 3L);
		testValue(ScopingReportStep.ITEM_EXACT_UNIQUE_ID, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_PREVIOUS_VERSION, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_LOCAL_CONTEXT, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_DOCUMENT_CONTEXT, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_STRUCTURAL, 0L);
		testValue(ScopingReportStep.ITEM_EXACT, 3L);
		testValue(ScopingReportStep.ITEM_EXACT_TEXT_ONLY_PREVIOUS_VERSION, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_TEXT_ONLY_UNIQUE_ID, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_TEXT_ONLY, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_REPAIRED, 0L);
		testValue(ScopingReportStep.ITEM_FUZZY_PREVIOUS_VERSION, 0L);
		testValue(ScopingReportStep.ITEM_FUZZY_UNIQUE_ID, 0L);
		testValue(ScopingReportStep.ITEM_FUZZY, 6L);
		testValue(ScopingReportStep.ITEM_FUZZY_REPAIRED, 0L);
		testValue(ScopingReportStep.ITEM_PHRASE_ASSEMBLED, 0L);
		testValue(ScopingReportStep.ITEM_MT, 0L);
		testValue(ScopingReportStep.ITEM_CONCORDANCE, 0L);
		testValue(ScopingReportStep.ITEM_NONTRANSLATABLE_WORD_COUNT, 3L);
		testValue(ScopingReportStep.ITEM_TRANSLATABLE_WORD_COUNT, 9L);
		testValue(ScopingReportStep.ITEM_NOCATEGORY, 3L);
		
		testValue(ScopingReportStep.PROJECT_TOTAL_CHARACTER_COUNT, 65L);
		testValue(ScopingReportStep.PROJECT_WHITESPACE_CHARACTER_COUNT, 8L);
		testValue(ScopingReportStep.PROJECT_PUNCTUATION_CHARACTER_COUNT, 4L);
		testValue(ScopingReportStep.PROJECT_OVERALL_CHARACTER_COUNT, 77L);
		testValue(ScopingReportStep.ITEM_TOTAL_CHARACTER_COUNT, 65L);
		testValue(ScopingReportStep.ITEM_WHITESPACE_CHARACTER_COUNT, 8L);
		testValue(ScopingReportStep.ITEM_PUNCTUATION_CHARACTER_COUNT, 4L);
		testValue(ScopingReportStep.ITEM_OVERALL_CHARACTER_COUNT, 77L);
		testValue(ScopingReportStep.PROJECT_GMX_PROTECTED_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_EXACT_MATCHED_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_LEVERAGED_MATCHED_CHARACTER_COUNT, 18L);
		testValue(ScopingReportStep.PROJECT_GMX_REPETITION_MATCHED_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_FUZZY_MATCHED_CHARACTER_COUNT, 32L);
		testValue(ScopingReportStep.PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.PROJECT_GMX_NONTRANSLATABLE_CHARACTER_COUNT, 32L);
		testValue(ScopingReportStep.PROJECT_GMX_TRANSLATABLE_CHARACTER_COUNT, 33L);
		testValue(ScopingReportStep.PROJECT_GMX_NOCATEGORY_CHARACTERS, 15L);
		testValue(ScopingReportStep.ITEM_GMX_PROTECTED_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_EXACT_MATCHED_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_LEVERAGED_MATCHED_CHARACTER_COUNT, 18L);
		testValue(ScopingReportStep.ITEM_GMX_REPETITION_MATCHED_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_FUZZY_MATCHED_CHARACTER_COUNT, 32L);
		testValue(ScopingReportStep.ITEM_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_NUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_MEASUREMENT_ONLY_TEXT_UNIT_CHARACTER_COUNT, 0L);
		testValue(ScopingReportStep.ITEM_GMX_NONTRANSLATABLE_CHARACTER_COUNT, 32L);
		testValue(ScopingReportStep.ITEM_GMX_TRANSLATABLE_CHARACTER_COUNT, 33L);
		testValue(ScopingReportStep.ITEM_GMX_NOCATEGORY_CHARACTERS, 15L);
		testValue(ScopingReportStep.PROJECT_EXACT_UNIQUE_ID_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_PREVIOUS_VERSION_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_LOCAL_CONTEXT_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_DOCUMENT_CONTEXT_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_STRUCTURAL_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_CHARACTERS, 18L);
		testValue(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_TEXT_ONLY_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_EXACT_REPAIRED_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_FUZZY_PREVIOUS_VERSION_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_FUZZY_UNIQUE_ID_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_FUZZY_CHARACTERS, 32L);
		testValue(ScopingReportStep.PROJECT_FUZZY_REPAIRED_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_PHRASE_ASSEMBLED_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_MT_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_CONCORDANCE_CHARACTERS, 0L);
		testValue(ScopingReportStep.PROJECT_NONTRANSLATABLE_CHARACTER_COUNT, 18L);
		testValue(ScopingReportStep.PROJECT_TRANSLATABLE_CHARACTER_COUNT, 47L);
		testValue(ScopingReportStep.PROJECT_NOCATEGORY_CHARACTERS, 15L);
		testValue(ScopingReportStep.ITEM_EXACT_UNIQUE_ID_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_PREVIOUS_VERSION_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_LOCAL_CONTEXT_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_DOCUMENT_CONTEXT_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_STRUCTURAL_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_CHARACTERS, 18L);
		testValue(ScopingReportStep.ITEM_EXACT_TEXT_ONLY_PREVIOUS_VERSION_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_TEXT_ONLY_UNIQUE_ID_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_TEXT_ONLY_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_EXACT_REPAIRED_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_FUZZY_PREVIOUS_VERSION_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_FUZZY_UNIQUE_ID_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_FUZZY_CHARACTERS, 32L);
		testValue(ScopingReportStep.ITEM_FUZZY_REPAIRED_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_PHRASE_ASSEMBLED_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_MT_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_CONCORDANCE_CHARACTERS, 0L);
		testValue(ScopingReportStep.ITEM_NONTRANSLATABLE_CHARACTER_COUNT, 18L);
		testValue(ScopingReportStep.ITEM_TRANSLATABLE_CHARACTER_COUNT, 47L);
		testValue(ScopingReportStep.ITEM_NOCATEGORY_CHARACTERS, 15L);
	}
	
}
