package net.sf.okapi.steps.leveraging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.pipelinedriver.BatchItemContext;
import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.steps.common.FilterEventsToRawDocumentStep;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;
import net.sf.okapi.tm.pensieve.common.TranslationUnitVariant;
import net.sf.okapi.tm.pensieve.writer.ITmWriter;
import net.sf.okapi.tm.pensieve.writer.TmWriterFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LeveragingStepTest {
	
	private String root;
	private String tmDir;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	
	public LeveragingStepTest () {
		root = TestUtil.getParentDir(this.getClass(), "/test01.html");
		tmDir = Util.ensureSeparator(Util.getTempDirectory(), true) + "levtestTM";
	}

	@Test
	public void testSimpleStep ()
		throws URISyntaxException
	{
		// Ensure output is deleted
		File outFile = new File(root+"test01.out.html");
		createTM();
		
		IPipelineDriver pdriver = new PipelineDriver();
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations(HtmlFilter.class.getName());
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		
		LeveragingStep levStep = new LeveragingStep();
		Parameters params = (Parameters)levStep.getParameters();
		// Set connector to use
		net.sf.okapi.connectors.pensieve.Parameters tmParams = new net.sf.okapi.connectors.pensieve.Parameters();
		tmParams.fromString(params.getResourceParameters());
		tmParams.setDbDirectory(tmDir);
		params.setResourceParameters(tmParams.toString());
		// Set threshold for fuzzy
		params.setThreshold(90);
		// Set threshold for filling the target
		params.setFillTargetThreshold(90);
		
		pdriver.addStep(levStep);
		
		pdriver.addStep(new FilterEventsToRawDocumentStep());
		
		String inputPath = root+"/test01.html";
		URI inputURI = new File(inputPath).toURI();
		URI outputURI = outFile.toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_html", outputURI, "UTF-8", locEN, locFR));
		
		pdriver.processBatch();

		assertTrue(outFile.exists());
		
		InputDocument doc = new InputDocument(outFile.getPath(), null);
		// Check exact
		ITextUnit tu = FilterTestDriver.getTextUnit(new HtmlFilter(), doc, "UTF-8", locFR, LocaleId.EMPTY, 1);
		assertNotNull(tu);
		assertEquals(TRG_1.toText(), tu.getSource().getFirstContent().toText());
		// Check fuzzy
		tu = FilterTestDriver.getTextUnit(new HtmlFilter(), doc, "UTF-8", locFR, LocaleId.EMPTY, 2);
		assertNotNull(tu);
		assertEquals(TRG_2.toText(), tu.getSource().getFirstContent().toText());
	}
	
	private static final TextFragment TRG_1 = new TextFragment("FR This is an example of text");
	private static final TextFragment TRG_2 = new TextFragment("FR This is an example of TEXT");
	
	private void createTM () {
		Util.deleteDirectory(tmDir, true);
		Util.createDirectories(tmDir+"/");

		ITmWriter tmWriter = TmWriterFactory.createFileBasedTmWriter(tmDir, true);
		TranslationUnitVariant source = new TranslationUnitVariant(locEN, new TextFragment("This is an example of text"));
		TranslationUnitVariant target = new TranslationUnitVariant(locEN, TRG_1);
		TranslationUnit tu = new TranslationUnit(source, target);
		tmWriter.indexTranslationUnit(tu);
		source = new TranslationUnitVariant(locEN, new TextFragment("This is an example of TEXT"));
		target = new TranslationUnitVariant(locEN, TRG_2);
		tu = new TranslationUnit(source, target);
		tmWriter.indexTranslationUnit(tu);
		tmWriter.commit();
	}

}
