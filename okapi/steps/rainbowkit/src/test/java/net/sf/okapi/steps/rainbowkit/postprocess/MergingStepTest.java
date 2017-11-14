/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.steps.rainbowkit.postprocess;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import net.sf.okapi.common.FileCompare;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipelinedriver.BatchItemContext;
import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.filters.openoffice.OpenOfficeFilter;
import net.sf.okapi.filters.properties.PropertiesFilter;
import net.sf.okapi.filters.rainbowkit.RainbowKitFilter;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.rainbowkit.creation.ExtractionStep;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MergingStepTest {
	
	private String root;
	private FilterConfigurationMapper fcMapper;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	
	@Before
	public void setUp() {
		root = TestUtil.getParentDir(this.getClass(), "/test01.properties");
		fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations(PropertiesFilter.class.getName());
		fcMapper.addConfigurations(OpenOfficeFilter.class.getName());
		fcMapper.addConfigurations(RainbowKitFilter.class.getName());
		fcMapper.addConfigurations(XLIFFFilter.class.getName());
		fcMapper.setCustomConfigurationsDirectory(root);
		fcMapper.updateCustomConfigurations();
	}

	@Test
	public void textXLIFFMerging ()
		throws URISyntaxException
	{
		// Call in the same test because they use the same files and concurent test would not work
		testXLIFFMerging(false);
		testXLIFFMerging(true);
	}
	
	private void testXLIFFMerging (boolean returnRawDoc)
		throws URISyntaxException
	{
		deleteOutputDir("xliffPack/done", true);
		
		IPipelineDriver pdriver = new PipelineDriver();
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		MergingStep mrgStep = new MergingStep();
		pdriver.addStep(mrgStep);
		
		Parameters prm = (Parameters)mrgStep.getParameters();
		prm.setReturnRawDocument(returnRawDoc);
		
		URI inputURI = new File(root+"xliffPack/manifest.rkm").toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_rainbowkit@noPrompt", null, "UTF-8", locEN, locFR));
		
		pdriver.processBatch();
		
		assertTrue(mrgStep.getErrorCount()==0);

		File file = new File(root+"xliffPack/done/test01.out.properties");
		assertTrue(file.exists());
		file = new File(root+"xliffPack/done/sub Dir/test01.out.odt");
		assertTrue(file.exists());
	}
	
	@Test
	public void testXLIFFExtractThenMerge ()
		throws URISyntaxException, FileNotFoundException
	{
		// Extract
		// Start by clearing the output
		deleteOutputDir("pack1", true);
		// Create the pipeline
		IPipelineDriver pdriver = new PipelineDriver();
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
		// Add the steps
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		pdriver.addStep(new ExtractionStep());
		// Add the input file
		String inputPath = root+"/test01.properties";
		String outputPath = inputPath.replace("test01.", "test01.out.");
		URI inputURI = new File(inputPath).toURI();
		URI outputURI = new File(outputPath).toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_properties", outputURI, "UTF-8", locEN, locFR));
		// Process
		pdriver.processBatch();
		// Check we have an output
		File file = new File(root+"pack1/work/test01.properties.xlf");
		assertTrue(file.exists());
		
		// Now try to merge
		pdriver.clearSteps();
		pdriver.clearItems();
		// Add the steps
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		MergingStep mrgStep = new MergingStep();
		pdriver.addStep(mrgStep);
		// Set the input file
		inputURI = new File(root+"pack1/manifest.rkm").toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_rainbowkit@noPrompt", null, "UTF-8", locEN, locFR));
		// Process
		pdriver.processBatch();

		assertTrue(mrgStep.getErrorCount()==0);
		// Check we have a file
		file = new File(root+"pack1/done/target/test-classes/test01.out.properties");
		assertTrue(file.exists());
		// Compare original and merged.
		FileCompare fc = new FileCompare();
		assertTrue(fc.compareFilesPerLines(file.getAbsolutePath(), inputPath, "us-ascii"));
		
	}
	
	@Test
	public void testXINIMerging ()
		throws URISyntaxException
	{
		deleteOutputDir("xiniPack/translated", true);
		
		IPipelineDriver pdriver = new PipelineDriver();
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		MergingStep mrgStep = new MergingStep();
		pdriver.addStep(mrgStep);
		
		Parameters prm = (Parameters)mrgStep.getParameters();
		prm.setReturnRawDocument(true);
		
		URI inputURI = new File(root+"xiniPack/manifest.rkm").toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_rainbowkit@noPrompt", null, "UTF-8", locEN, locFR));
		
		pdriver.processBatch();

		assertTrue(mrgStep.getErrorCount()==0);
		File file = new File(root+"xiniPack/translated/test1.out.xlf");
		assertTrue(file.exists());
	}
	
	@Test
	public void testXINIMergingWithOutputPath ()
		throws URISyntaxException
	{
		deleteOutputDir("xiniPack/translated", true);
		
		IPipelineDriver pdriver = new PipelineDriver();
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		MergingStep mrgStep = new MergingStep();
		pdriver.addStep(mrgStep);
		
		Parameters prm = (Parameters)mrgStep.getParameters();
		prm.setReturnRawDocument(true);
		prm.setOverrideOutputPath(root+"output");
		
		URI inputURI = new File(root+"xiniPack/manifest.rkm").toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_rainbowkit@noPrompt", null, "UTF-8", locEN, locFR));
		
		pdriver.processBatch();

		assertTrue(mrgStep.getErrorCount()==0);
		File file = new File(root+"output/test1.out.xlf");
		assertTrue(file.exists());
	}
	
	@Test
	public void testPOMerging ()
		throws URISyntaxException
	{
		deleteOutputDir("poPack/done", true);
		
		IPipelineDriver pdriver = new PipelineDriver();
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root),Util.deleteLastChar(root)); // Don't include final separator
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		MergingStep mrgStep = new MergingStep();
		pdriver.addStep(mrgStep);
		
		URI inputURI = new File(root+"poPack/manifest.rkm").toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_rainbowkit@noPrompt", null, "UTF-8", locEN, locFR));
		
		pdriver.processBatch();

		assertTrue(mrgStep.getErrorCount()==0);
		File file = new File(root+"poPack/done/test01.out.properties");
		assertTrue(file.exists());
		file = new File(root+"poPack/done/sub Dir/test01.out.odt");
		assertTrue(file.exists());
		
	}
	
	@Test
	public void testOmegaTMerging ()
		throws URISyntaxException
	{
		deleteOutputDir("omegatPack/done", true);
		
		IPipelineDriver pdriver = new PipelineDriver();
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		MergingStep mrgStep = new MergingStep();
		pdriver.addStep(mrgStep);
		
		URI inputURI = new File(root+"omegatPack/manifest.rkm").toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_rainbowkit@noPrompt", null, "UTF-8", locEN, locFR));
		
		pdriver.processBatch();

		assertTrue(mrgStep.getErrorCount()==0);
		File file = new File(root+"omegatPack/done/test01.out.properties");
		assertTrue(file.exists());
		file = new File(root+"omegatPack/done/sub Dir/test01.out.odt");
		assertTrue(file.exists());
		
	}

    public boolean deleteOutputDir (String dirname, boolean relative) {
    	File d;
    	if ( relative ) d = new File(root + File.separator + dirname);
    	else d = new File(dirname);
    	if ( d.isDirectory() ) {
    		String[] children = d.list();
    		for ( int i=0; i<children.length; i++ ) {
    			boolean success = deleteOutputDir(d.getAbsolutePath() + File.separator + children[i], false);
    			if ( !success ) {
    				return false;
    			}
    		}
    	}
    	if ( d.exists() ) return d.delete();
    	else return true;
    }
    

}
