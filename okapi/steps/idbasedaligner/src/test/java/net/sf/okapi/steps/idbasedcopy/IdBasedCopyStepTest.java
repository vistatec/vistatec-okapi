package net.sf.okapi.steps.idbasedcopy;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipelinedriver.BatchItemContext;
import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.po.POFilter;
import net.sf.okapi.filters.properties.PropertiesFilter;
import net.sf.okapi.steps.common.FilterEventsToRawDocumentStep;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IdBasedCopyStepTest {
	
	private String root;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	
	@Before
	public void setUp() {
		root = TestUtil.getParentDir(this.getClass(), "/destination1.properties");
	}

	@Test
	public void stub () {
		assertTrue(true);
	}
	
	@Test
	public void testCopy ()
		throws URISyntaxException
	{
		IPipelineDriver pdriver = new PipelineDriver();
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations(PropertiesFilter.class.getName());
		fcMapper.addConfigurations(POFilter.class.getName());
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		pdriver.addStep(new IdBasedCopyStep());
		pdriver.addStep(new FilterEventsToRawDocumentStep());

		// Add the properties files
		String input1Path = root+"destination1.properties";
		String input2Path = root+"reference1.properties";
		String propOutputPath = input1Path.replace("1.", "1.out.");
		URI input1URI = new File(input1Path).toURI();
		URI input2URI = new File(input2Path).toURI();
		URI output1URI = new File(propOutputPath).toURI();
		BatchItemContext bic1 = new BatchItemContext(input1URI, "UTF-8", "okf_properties", output1URI, "UTF-8", locEN, locFR);
		RawDocument rd2 = new RawDocument(input2URI, "UTF-8", locFR);
		rd2.setFilterConfigId("okf_properties");
		bic1.add(rd2, null, null);
		pdriver.addBatchItem(bic1);
		
		// Add the PO files
		input1Path = root+"destination1.po";
		input2Path = root+"reference1.po";
		String poOutputPath = input1Path.replace("1.", "1.out.");
		input1URI = new File(input1Path).toURI();
		input2URI = new File(input2Path).toURI();
		output1URI = new File(poOutputPath).toURI();
		bic1 = new BatchItemContext(input1URI, "UTF-8", "okf_po", output1URI, "UTF-8", locEN, locFR);
		rd2 = new RawDocument(input2URI, "UTF-8", locEN, locFR);
		rd2.setFilterConfigId("okf_po");
		bic1.add(rd2, null, null);
		pdriver.addBatchItem(bic1);

		// Make sure the output is deleted
		File file = new File(propOutputPath);
		file.delete();
		file = new File(poOutputPath);
		file.delete();
		
		pdriver.processBatch();

		//TODO: need to test content too
		file = new File(propOutputPath);
		assertTrue(file.exists());

		file = new File(poOutputPath);
		assertTrue(file.exists());
	}

}
