package net.sf.okapi.steps.wordcount.categorized.okapi;

import static org.junit.Assert.assertEquals;
import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.extra.steps.TextUnitLogger;
import net.sf.okapi.steps.wordcount.common.BaseCountStep;
import net.sf.okapi.steps.wordcount.common.BaseCounter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class TestOkapiCounts {

	private BaseCountStep bcs;
	private StartDocument sd;
	private Event sdEvent;
	private ITextUnit tu;
	private Event tuEvent;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Before
	public void startup() {
		sd = new StartDocument("sd");
		sd.setLocale(LocaleId.ENGLISH);
		sdEvent = new Event(EventType.START_DOCUMENT, sd);

		tu = new TextUnit("tu");
		tu.setSource(new TextContainer("Elephants cannot fly."));
		tuEvent = new Event(EventType.TEXT_UNIT, tu);
		tu.setTarget(LocaleId.FRENCH, new TextContainer(
				"Les éléphants ne peuvent pas voler."));
		TextContainer target = tu.getTarget(LocaleId.FRENCH);
		target.setAnnotation(new AltTranslationsAnnotation());
	}

	@Test
	public void testConcordanceWordCountStep() {
		// Not yet implemented
	}

	@Test
	public void testExactDocumentContextMatchWordCountStep() {
		testCount(ExactDocumentContextMatchWordCountStep.class,
				MatchType.EXACT_DOCUMENT_CONTEXT, false);
	}
	
	@Test
	public void testExactDocumentContextMatchCharacterCountStep() {
		testCount(ExactDocumentContextMatchCharacterCountStep.class,
				MatchType.EXACT_DOCUMENT_CONTEXT, true);
	}

	private void testCount(Class<? extends BaseCountStep> cls,
			MatchType matchType, boolean isCharacters) {
		AltTranslationsAnnotation ata = tu.getTarget(LocaleId.FRENCH)
				.getAnnotation(AltTranslationsAnnotation.class);
		ata.add(new AltTranslation(LocaleId.ENGLISH, LocaleId.FRENCH, tu
				.getSource().getFirstContent(), null, null, matchType, 100,
				null));
		try {
			bcs = ClassUtil.instantiateClass(cls);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		bcs.setSourceLocale(LocaleId.ENGLISH);
		bcs.setTargetLocale(LocaleId.FRENCH);
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));

		String metric = matchType.name() + (isCharacters ? "_CHARACTERS" : "");
		long count = isCharacters ? 18 : 3;
		assertEquals(count, BaseCounter.getCount(tu, metric)); //
	}

	@Test
	public void testExactLocalContextMatchWordCountStep() {
		testCount(ExactLocalContextMatchWordCountStep.class,
				MatchType.EXACT_LOCAL_CONTEXT, false);
	}
	
	@Test
	public void testExactLocalContextMatchCharacterCountStep() {
		testCount(ExactLocalContextMatchCharacterCountStep.class,
				MatchType.EXACT_LOCAL_CONTEXT, true);
	}

	@Test
	public void testExactMatchWordCountStep() {
		testCount(ExactMatchWordCountStep.class, MatchType.EXACT, false);
	}
	
	@Test
	public void testExactMatchCharacterCountStep() {
		testCount(ExactMatchCharacterCountStep.class, MatchType.EXACT, true);
	}

	@Test
	public void testExactPreviousVersionMatchWordCountStep() {
		testCount(ExactPreviousVersionMatchWordCountStep.class,
				MatchType.EXACT_PREVIOUS_VERSION, false);
	}
	
	@Test
	public void testExactPreviousVersionMatchCharacterCountStep() {
		testCount(ExactPreviousVersionMatchCharacterCountStep.class,
				MatchType.EXACT_PREVIOUS_VERSION, true);
	}

	@Test
	public void testExactRepairedWordCountStep() {
		testCount(ExactRepairedWordCountStep.class, MatchType.EXACT_REPAIRED, false);
	}
	
	@Test
	public void testExactRepairedCharacterCountStep() {
		testCount(ExactRepairedCharacterCountStep.class, MatchType.EXACT_REPAIRED, true);
	}

	@Test
	public void testExactStructuralMatchWordCountStep() {
		testCount(ExactStructuralMatchWordCountStep.class,
				MatchType.EXACT_STRUCTURAL, false);
	}
	
	@Test
	public void testExactStructuralMatchCharacterCountStep() {
		testCount(ExactStructuralMatchCharacterCountStep.class,
				MatchType.EXACT_STRUCTURAL, true);
	}

	@Test
	public void testExactTextOnlyPreviousVersionMatchWordCountStep() {
		testCount(ExactTextOnlyPreviousVersionMatchWordCountStep.class,
				MatchType.EXACT_TEXT_ONLY_PREVIOUS_VERSION, false);
	}
	
	@Test
	public void testExactTextOnlyPreviousVersionMatchCharacterCountStep() {
		testCount(ExactTextOnlyPreviousVersionMatchCharacterCountStep.class,
				MatchType.EXACT_TEXT_ONLY_PREVIOUS_VERSION, true);
	}

	@Test
	public void testExactTextOnlyUniqueIdMatchWordCountStep() {
		testCount(ExactTextOnlyUniqueIdMatchWordCountStep.class,
				MatchType.EXACT_TEXT_ONLY_UNIQUE_ID, false);
	}
	
	@Test
	public void testExactTextOnlyUniqueIdMatchCharacterCountStep() {
		testCount(ExactTextOnlyUniqueIdMatchCharacterCountStep.class,
				MatchType.EXACT_TEXT_ONLY_UNIQUE_ID, true);
	}

	@Test
	public void testExactTextOnlyWordCountStep() {
		testCount(ExactTextOnlyWordCountStep.class,
				MatchType.EXACT_TEXT_ONLY, false);
	}
	
	@Test
	public void testExactTextOnlyCharacterCountStep() {
		testCount(ExactTextOnlyCharacterCountStep.class,
				MatchType.EXACT_TEXT_ONLY, true);
	}

	@Test
	public void testExactUniqueIdMatchWordCountStep() {
		testCount(ExactUniqueIdMatchWordCountStep.class,
				MatchType.EXACT_UNIQUE_ID, false);
	}
	
	@Test
	public void testExactUniqueIdMatchCharacterCountStep() {
		testCount(ExactUniqueIdMatchCharacterCountStep.class,
				MatchType.EXACT_UNIQUE_ID, true);
	}

	@Test
	public void testFuzzyMatchWordCountStep() {
		testCount(FuzzyMatchWordCountStep.class, MatchType.FUZZY, false);
	}
	
	@Test
	public void testFuzzyMatchCharacterCountStep() {
		testCount(FuzzyMatchCharacterCountStep.class, MatchType.FUZZY, true);
	}

	@Test
	public void testFuzzyPreviousVersionMatchWordCountStep() {
		testCount(FuzzyPreviousVersionMatchWordCountStep.class,
				MatchType.FUZZY_PREVIOUS_VERSION, false);
	}
	
	@Test
	public void testFuzzyPreviousVersionMatchCharacterCountStep() {
		testCount(FuzzyPreviousVersionMatchCharacterCountStep.class,
				MatchType.FUZZY_PREVIOUS_VERSION, true);
	}

	@Test
	public void testFuzzyRepairedWordCountStep() {
		testCount(FuzzyRepairedWordCountStep.class,
				MatchType.FUZZY_REPAIRED, false);
	}
	
	@Test
	public void testFuzzyRepairedCharacterCountStep() {
		testCount(FuzzyRepairedCharacterCountStep.class,
				MatchType.FUZZY_REPAIRED, true);
	}

	@Test
	public void testFuzzyUniqueIdMatchWordCountStep() {
		testCount(FuzzyUniqueIdMatchWordCountStep.class,
				MatchType.FUZZY_UNIQUE_ID, false);
	}
	
	@Test
	public void testFuzzyUniqueIdMatchCharacterCountStep() {
		testCount(FuzzyUniqueIdMatchCharacterCountStep.class,
				MatchType.FUZZY_UNIQUE_ID, true);
	}

	@Test
	public void testMTWordCountStep() {
		testCount(MTWordCountStep.class, MatchType.MT, false);
	}
	
	@Test
	public void testMTCharacterCountStep() {
		testCount(MTCharacterCountStep.class, MatchType.MT, true);
	}

	@Test
	public void testPhraseAssembledWordCountStep() {
		// Not yet implemented
	}
}
