package net.sf.okapi.steps.wordcount.categorized.gmx;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.extra.steps.TextUnitLogger;
import net.sf.okapi.steps.leveraging.LeveragingStep;
import net.sf.okapi.steps.wordcount.common.BaseCountStep;
import net.sf.okapi.steps.wordcount.common.BaseCounter;
import net.sf.okapi.steps.wordcount.common.GMX;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class TestGMXCounts {
	
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
		tu.setSource(new TextContainer("12:00 is 15 minutes after 11:45. You can check at freetime@example.com 8-) for $300"));
		tuEvent = new Event(EventType.TEXT_UNIT, tu);
	}
	
	private void doSDEvent(LocaleId locale) {
		sd.setLocale(locale);
		bcs.handleEvent(sdEvent);
	}
	
	private void doTUEvent(String text) {
		tu.setSource(new TextContainer(text));
		bcs.handleEvent(tuEvent);
	}

	@Test
	public void testGMXAlphanumericOnlyTextUnitWordCountStep () {
		bcs = new GMXAlphanumericOnlyTextUnitWordCountStep();
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		// GMX AlphanumericOnlyTextUnitWordCount only applies to text units that consist solely of
		// alphanumeric words. This is not true for the full example sentence, so this count is zero.
		assertEquals(0, BaseCounter.getCount(tu, GMX.AlphanumericOnlyTextUnitWordCount));
		
		doTUEvent("freetime@example.com 8-)");
		assertEquals(2, BaseCounter.getCount(tu, GMX.AlphanumericOnlyTextUnitWordCount)); // freetime@example.com, 8-)
		
		// Logographic word count
		doSDEvent(LocaleId.JAPANESE);
		doTUEvent("freetime@example.com 8-)");
		assertEquals(6, BaseCounter.getCount(tu, GMX.AlphanumericOnlyTextUnitWordCount)); // 19 characters / 3.0
	}
	
	@Test
	public void testGMXAlphanumericOnlyTextUnitCharacterCountStep () {
		bcs = new GMXAlphanumericOnlyTextUnitCharacterCountStep();
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		// GMX AlphanumericOnlyTextUnitCharacterCount only applies to text units that consist solely of
		// alphanumeric words. This is not true for the full example sentence, so this count is zero.
		assertEquals(0, BaseCounter.getCount(tu, GMX.AlphanumericOnlyTextUnitCharacterCount));
		
		doTUEvent("freetime@example.com 8-)");
		assertEquals(19, BaseCounter.getCount(tu, GMX.AlphanumericOnlyTextUnitCharacterCount)); // freetime@example.com, 8-)
	}
	
	@Test
	public void testGMXExactMatchedWordCountStep () throws URISyntaxException {
		String pathBase = Util.getDirectoryName(this.getClass().getResource("").toURI().getPath()) + "/";
		net.sf.okapi.connectors.pensieve.Parameters rparams = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		rparams.setDbDirectory(pathBase + "testtm");
		tu.setSource(new TextContainer("Elephants cannot fly."));
		
		LeveragingStep ls = new LeveragingStep();
		ls.setSourceLocale(LocaleId.ENGLISH);
		ls.setTargetLocale(LocaleId.FRENCH);
		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
		params.setResourceParameters(rparams.toString());
		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
		params.setThreshold(99);
		params.setFillTarget(true);
		ls.handleEvent(sdEvent);
		ls.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		AltTranslationsAnnotation ata = tu.getTarget(LocaleId.FRENCH).getAnnotation(AltTranslationsAnnotation.class);
		ata.add(new AltTranslation(LocaleId.ENGLISH, LocaleId.FRENCH, tu.getSource().getFirstContent(), null, null, MatchType.EXACT_UNIQUE_ID, 100, null));
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		bcs = new GMXLeveragedMatchedWordCountStep();
		bcs.setSourceLocale(LocaleId.ENGLISH);
		bcs.setTargetLocale(LocaleId.FRENCH);
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		assertEquals(3, BaseCounter.getCount(tu, GMX.LeveragedMatchedWordCount));
	}
	
	@Test
	public void testGMXExactMatchedCharacterCountStep () throws URISyntaxException {
		String pathBase = Util.getDirectoryName(this.getClass().getResource("").toURI().getPath()) + "/";
		net.sf.okapi.connectors.pensieve.Parameters rparams = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		rparams.setDbDirectory(pathBase + "testtm");
		tu.setSource(new TextContainer("Elephants cannot fly."));
		
		LeveragingStep ls = new LeveragingStep();
		ls.setSourceLocale(LocaleId.ENGLISH);
		ls.setTargetLocale(LocaleId.FRENCH);
		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
		params.setResourceParameters(rparams.toString());
		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
		params.setThreshold(99);
		params.setFillTarget(true);
		ls.handleEvent(sdEvent);
		ls.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		AltTranslationsAnnotation ata = tu.getTarget(LocaleId.FRENCH).getAnnotation(AltTranslationsAnnotation.class);
		ata.add(new AltTranslation(LocaleId.ENGLISH, LocaleId.FRENCH, tu.getSource().getFirstContent(), null, null, MatchType.EXACT_UNIQUE_ID, 100, null));
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		bcs = new GMXLeveragedMatchedCharacterCountStep();
		bcs.setSourceLocale(LocaleId.ENGLISH);
		bcs.setTargetLocale(LocaleId.FRENCH);
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		assertEquals(18, BaseCounter.getCount(tu, GMX.LeveragedMatchedCharacterCount));
	}

	@Test
	public void testGMXLeveragedMatchedWordCountStep () throws URISyntaxException {
		String pathBase = Util.getDirectoryName(this.getClass().getResource("").toURI().getPath()) + "/";
		net.sf.okapi.connectors.pensieve.Parameters rparams = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		rparams.setDbDirectory(pathBase + "testtm");
		tu.setSource(new TextContainer("Elephants cannot fly."));
		
		LeveragingStep ls = new LeveragingStep();
		ls.setSourceLocale(LocaleId.ENGLISH);
		ls.setTargetLocale(LocaleId.FRENCH);
		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
		params.setResourceParameters(rparams.toString());
		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
		params.setThreshold(99);
		params.setFillTarget(true);
		ls.handleEvent(sdEvent);
		ls.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		bcs = new GMXLeveragedMatchedWordCountStep();
		bcs.setSourceLocale(LocaleId.ENGLISH);
		bcs.setTargetLocale(LocaleId.FRENCH);
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		assertEquals(3, BaseCounter.getCount(tu, GMX.LeveragedMatchedWordCount)); 
		assertEquals(0, BaseCounter.getCount(tu, GMX.FuzzyMatchedWordCount));
	}
	
	@Test
	public void testGMXLeveragedMatchedCharacterCountStep () throws URISyntaxException {
		String pathBase = Util.getDirectoryName(this.getClass().getResource("").toURI().getPath()) + "/";
		net.sf.okapi.connectors.pensieve.Parameters rparams = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		rparams.setDbDirectory(pathBase + "testtm");
		tu.setSource(new TextContainer("Elephants cannot fly."));
		
		LeveragingStep ls = new LeveragingStep();
		ls.setSourceLocale(LocaleId.ENGLISH);
		ls.setTargetLocale(LocaleId.FRENCH);
		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
		params.setResourceParameters(rparams.toString());
		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
		params.setThreshold(99);
		params.setFillTarget(true);
		ls.handleEvent(sdEvent);
		ls.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		bcs = new GMXLeveragedMatchedCharacterCountStep();
		bcs.setSourceLocale(LocaleId.ENGLISH);
		bcs.setTargetLocale(LocaleId.FRENCH);
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		assertEquals(18, BaseCounter.getCount(tu, GMX.LeveragedMatchedCharacterCount)); 
		assertEquals(0, BaseCounter.getCount(tu, GMX.FuzzyMatchedCharacterCount));
	}
	
	@Test
	public void testGMXFuzzyMatchWordCountStep () throws URISyntaxException {
		String pathBase = Util.getDirectoryName(this.getClass().getResource("").toURI().getPath()) + "/";
		net.sf.okapi.connectors.pensieve.Parameters rparams = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		rparams.setDbDirectory(pathBase + "testtm");
		tu.setSource(new TextContainer("Elephants cannot fly here."));
		
		LeveragingStep ls = new LeveragingStep();
		ls.setSourceLocale(LocaleId.ENGLISH);
		ls.setTargetLocale(LocaleId.FRENCH);
		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
		params.setResourceParameters(rparams.toString());
		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
		params.setThreshold(50);
		params.setFillTarget(true);
		ls.handleEvent(sdEvent);
		ls.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		bcs = new GMXFuzzyMatchWordCountStep();
		bcs.setSourceLocale(LocaleId.ENGLISH);
		bcs.setTargetLocale(LocaleId.FRENCH);
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		assertEquals(4, BaseCounter.getCount(tu, GMX.FuzzyMatchedWordCount));
		assertEquals(0, BaseCounter.getCount(tu, GMX.LeveragedMatchedWordCount));
	}
	
	@Test
	public void testGMXFuzzyMatchCharacterCountStep () throws URISyntaxException {
		String pathBase = Util.getDirectoryName(this.getClass().getResource("").toURI().getPath()) + "/";
		net.sf.okapi.connectors.pensieve.Parameters rparams = 
			new net.sf.okapi.connectors.pensieve.Parameters();
		rparams.setDbDirectory(pathBase + "testtm");
		tu.setSource(new TextContainer("Elephants cannot fly here."));
		
		LeveragingStep ls = new LeveragingStep();
		ls.setSourceLocale(LocaleId.ENGLISH);
		ls.setTargetLocale(LocaleId.FRENCH);
		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
		params.setResourceParameters(rparams.toString());
		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
		params.setThreshold(50);
		params.setFillTarget(true);
		ls.handleEvent(sdEvent);
		ls.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		bcs = new GMXFuzzyMatchCharacterCountStep();
		bcs.setSourceLocale(LocaleId.ENGLISH);
		bcs.setTargetLocale(LocaleId.FRENCH);
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		logger.debug(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
		
		assertEquals(22, BaseCounter.getCount(tu, GMX.FuzzyMatchedCharacterCount));
		assertEquals(0, BaseCounter.getCount(tu, GMX.LeveragedMatchedCharacterCount));
	}
	
	@Test
	public void testGMXMeasurementOnlyTextUnitWordCountStep () {
		bcs = new GMXMeasurementOnlyTextUnitWordCountStep();
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		// GMX MeasurementOnlyTextUnitWordCount only applies to text units that consist solely of
		// measurement words. This is not true for the full example sentence, so this count is zero.
		assertEquals(0, BaseCounter.getCount(tu, GMX.MeasurementOnlyTextUnitWordCount));
		
		doTUEvent("11:45 12:00 12/1/1999 $300");
		assertEquals(4, BaseCounter.getCount(tu, GMX.MeasurementOnlyTextUnitWordCount)); // 11:45, 12:00, 12/1/1999, $300
		
		// Logographic word count
		doSDEvent(LocaleId.JAPANESE);
		doTUEvent("11:45 12:00 12/1/1999 $300");
		assertEquals(6, BaseCounter.getCount(tu, GMX.MeasurementOnlyTextUnitWordCount)); // 18 characters / 3.0
	}
	
	@Test
	public void testGMXMeasurementOnlyTextUnitCharacterCountStep () {
		bcs = new GMXMeasurementOnlyTextUnitCharacterCountStep();
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		// GMX MeasurementOnlyTextUnitWordCount only applies to text units that consist solely of
		// measurement words. This is not true for the full example sentence, so this count is zero.
		assertEquals(0, BaseCounter.getCount(tu, GMX.MeasurementOnlyTextUnitCharacterCount));
		
		doTUEvent("11:45 12:00 12/1/1999 $300");
		assertEquals(18, BaseCounter.getCount(tu, GMX.MeasurementOnlyTextUnitCharacterCount)); // 11:45, 12:00, 12/1/1999, $300
	}
	
	@Test
	public void testGMXNumericOnlyTextUnitWordCountStep () {
		bcs = new GMXNumericOnlyTextUnitWordCountStep();
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		// GMX NumericOnlyTextUnitWordCount only applies to text units that consist solely of
		// measurement words. This is not true for the full example sentence, so this count is zero.
		assertEquals(0, BaseCounter.getCount(tu, GMX.NumericOnlyTextUnitWordCount));
		
		doTUEvent("15 16 3.1415926536");
		assertEquals(3, BaseCounter.getCount(tu, GMX.NumericOnlyTextUnitWordCount)); // 15, 16, 3.1415926536
		
		// Logographic word count
		doSDEvent(LocaleId.JAPANESE);
		doTUEvent("15 16 3.1415926536");
		assertEquals(5, BaseCounter.getCount(tu, GMX.NumericOnlyTextUnitWordCount)); // 15 characters / 3.0
	}
	
	@Test
	public void testGMXNumericOnlyTextUnitCharacterCountStep () {
		bcs = new GMXNumericOnlyTextUnitCharacterCountStep();
		bcs.handleEvent(sdEvent);
		bcs.handleEvent(tuEvent);
		// GMX NumericOnlyTextUnitWordCount only applies to text units that consist solely of
		// measurement words. This is not true for the full example sentence, so this count is zero.
		assertEquals(0, BaseCounter.getCount(tu, GMX.NumericOnlyTextUnitCharacterCount));
		
		doTUEvent("15 16 3.1415926536");
		assertEquals(15, BaseCounter.getCount(tu, GMX.NumericOnlyTextUnitCharacterCount)); // 15, 16, 3.1415926536
	}
	
	@Test
	public void testGMXProtectedWordCountStep () {
		bcs = new GMXProtectedWordCountStep();
		bcs.handleEvent(sdEvent);		
		
		tu.setIsTranslatable(false);
		bcs.handleEvent(tuEvent);
		assertEquals(11, BaseCounter.getCount(tu, GMX.ProtectedWordCount)); // freetime@example.com, 8-), $300 are not words	
		
		tu.setIsTranslatable(true);
		bcs.handleEvent(tuEvent);
		assertEquals(0, BaseCounter.getCount(tu, GMX.ProtectedWordCount)); // 0 - not counted in a translatable TU
	}
	
	@Test
	public void testGMXProtectedCharacterCountStep () {
		bcs = new GMXProtectedCharacterCountStep();
		bcs.handleEvent(sdEvent);		
		
		tu.setIsTranslatable(false);
		bcs.handleEvent(tuEvent);
		assertEquals(62, BaseCounter.getCount(tu, GMX.ProtectedCharacterCount));
		
		tu.setIsTranslatable(true);
		bcs.handleEvent(tuEvent);
		assertEquals(0, BaseCounter.getCount(tu, GMX.ProtectedCharacterCount)); // 0 - not counted in a translatable TU
	}
	
	@Test
	public void testGMXRepetitionMatchedWordCountStep () {
		// Not yet implemented
	}	
}

