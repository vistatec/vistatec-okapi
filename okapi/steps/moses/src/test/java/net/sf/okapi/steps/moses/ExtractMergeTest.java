package net.sf.okapi.steps.moses;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileCompare;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.pipelinedriver.BatchItemContext;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.steps.common.FilterEventsToRawDocumentStep;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExtractMergeTest {

	private String root;
	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locJA = LocaleId.JAPANESE;
	private LocaleId locFR = LocaleId.FRENCH;
	private FileCompare fc;

	public ExtractMergeTest () throws URISyntaxException {
		URL url = ExtractMergeTest.class.getResource("/Test-XLIFF01.xlf");
		root = Util.getDirectoryName(url.toURI().getPath()) + File.separator;
		fc = new FileCompare();
	}

	@Test
	public void testExtracThenMerge () throws FileNotFoundException {
		// Make sure output does not exists
		File inFile = new File(root+"/Test-XLIFF01.xlf");
		File out1File = new File(root+"/Test-XLIFF01.xlf.en");
		File gold1File = new File(root+"/Test-XLIFF01.xlf.en_gold");
		File transFile = new File(root+"/Test-XLIFF01.xlf.en_trans");
		File out2File = new File(root+"/Test-XLIFF01.out.xlf");
		
		// Make sure output are deleted
		out1File.delete();
		assertFalse(out1File.exists());
		out2File.delete();
		assertFalse(out2File.exists());
		
		// Set up the extraction pipeline
		PipelineDriver pd = new PipelineDriver();
		pd.addStep(new RawDocumentToFilterEventsStep(new XLIFFFilter()));
		pd.addStep(new ExtractionStep());
		pd.addBatchItem(inFile.toURI(), "UTF-8", "okf_xliff", locEN, locJA);
		// Execute it
		pd.processBatch();

		// Check output
		assertTrue(out1File.exists());
		assertTrue(fc.compareFilesPerLines(
			out1File.getAbsolutePath(), gold1File.getAbsolutePath(), "UTF-8"));

		// Setup the merging pipeline
		pd = new PipelineDriver();
		pd.addStep(new RawDocumentToFilterEventsStep(new XLIFFFilter()));
		pd.addStep(new MergingStep());
		pd.addStep(new FilterEventsToRawDocumentStep());
		// Two parallel inputs: 1=the original file, 2=the Moses translated file
		RawDocument rd1 = new RawDocument(inFile.toURI(), "UTF-8", locEN, locJA, "okf_xliff");
		RawDocument rd2 = new RawDocument(transFile.toURI(), "UTF-8", locJA);
		pd.addBatchItem(new BatchItemContext(rd1, out2File.toURI(), "UTF-8", rd2));
		// Execute it
		pd.processBatch();

		// Check output
		assertTrue(out2File.exists());
	}

	@Test
	public void testExtracThenMergeWithAlt () {
		// Make sure output does not exists
		File inFile = new File(root+"/Test-XLIFF02.xlf");
		File out1File = new File(root+"/Test-XLIFF02.xlf.en");
		File transFile = new File(root+"/Test-XLIFF02.xlf.en_trans");
		File out2File = new File(root+"/Test-XLIFF02.out.xlf");
		
		// Make sure output are deleted
		out1File.delete();
		assertFalse(out1File.exists());
		out2File.delete();
		assertFalse(out2File.exists());
		
		// Set up the extraction pipeline
		PipelineDriver pd = new PipelineDriver();
		pd.addStep(new RawDocumentToFilterEventsStep(new XLIFFFilter()));
		pd.addStep(new ExtractionStep());
		pd.addBatchItem(inFile.toURI(), "UTF-8", "okf_xliff", locEN, locFR);
		// Execute it
		pd.processBatch();

		// Check output
		assertTrue(out1File.exists());

		// Setup the merging pipeline
		pd = new PipelineDriver();
		pd.addStep(new RawDocumentToFilterEventsStep(new XLIFFFilter()));
		MergingStep step = new MergingStep();
		MergingParameters p = (MergingParameters)step.getParameters();
		p.setCopyToTarget(true);
		pd.addStep(step);
		pd.addStep(new FilterEventsToRawDocumentStep());
		// Two parallel inputs: 1=the original file, 2=the Moses translated file
		RawDocument rd1 = new RawDocument(inFile.toURI(), "UTF-8", locEN, locFR, "okf_xliff");
		RawDocument rd2 = new RawDocument(transFile.toURI(), "UTF-8", locFR);
		pd.addBatchItem(new BatchItemContext(rd1, out2File.toURI(), "UTF-8", rd2));
		// Execute it
		pd.processBatch();

		// Check output
		assertTrue(out2File.exists());
		// Check some translations
		// Read the Moses string and compare with the expected result
		List<Event> list = getEventsFromFile(new XLIFFFilter(), out2File.getAbsolutePath(), locFR);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 2);
		assertNotNull(tu);
		assertEquals("2", tu.getId());
		ISegments segs = tu.getTarget(locFR).getSegments();
		assertEquals(2, segs.count());
		for ( Segment seg : segs ) {
			// Copy to the target was set so the target should be translated
			assertTrue(seg.text.toText().startsWith("FR "));
			// Check annotation
			AltTranslationsAnnotation ann = seg.getAnnotation(AltTranslationsAnnotation.class);
			assertNotNull(ann);
			assertTrue(ann.getFirst().getTarget().toString().startsWith("FR "));
		}
	}

	private ArrayList<Event> getEventsFromFile (IFilter filter,
		String path,
		LocaleId trgLoc)
	{
		return FilterTestDriver.getEvents(filter, new RawDocument(new File(path).toURI(), "UTF-8", locEN, trgLoc), null);
	}

}
