/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.xliff;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import net.sf.okapi.common.FileCompare;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipelinedriver.BatchItemContext;
import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.filters.its.html5.HTML5Filter;
import net.sf.okapi.filters.openoffice.OpenOfficeFilter;
import net.sf.okapi.filters.properties.PropertiesFilter;
import net.sf.okapi.filters.rainbowkit.RainbowKitFilter;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.rainbowkit.creation.ExtractionStep;
import net.sf.okapi.steps.rainbowkit.postprocess.MergingStep;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExtractThenMergeTest {
	
	private String root;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	
	@Before
	public void setUp() {
		root = TestUtil.getParentDir(this.getClass(), "/test01.properties");
		// Java returns paths with a leading slash even on Windows; this
		// does not represent what will really be passed to the step in practice.
		// Strip the leading slash if we're on Windows with a drive-letter path.
		if (System.getProperty("os.name").startsWith("Windows") && Pattern.matches("^/[A-Z]:.*$", root))
			root = root.substring(1);
	}

	@Test
	public void testSimpleExtractThenMerge ()
		throws URISyntaxException
	{
		// Ensure output is deleted
		assertTrue(Util.deleteDirectory(new File(root+"xlf2Pack")));
		
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations(PropertiesFilter.class.getName());
		fcMapper.addConfigurations(OpenOfficeFilter.class.getName());
		fcMapper.addConfigurations(HTML5Filter.class.getName());
		fcMapper.addConfigurations(RainbowKitFilter.class.getName());
		fcMapper.setCustomConfigurationsDirectory(root);
		fcMapper.updateCustomConfigurations();

		IPipelineDriver pdriver = new PipelineDriver();
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
		pdriver.setOutputDirectory(root);
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		ExtractionStep es = new ExtractionStep();
		pdriver.addStep(es);
		net.sf.okapi.steps.rainbowkit.creation.Parameters ep
			= (net.sf.okapi.steps.rainbowkit.creation.Parameters)es.getParameters();
		ep.setWriterClass("net.sf.okapi.steps.rainbowkit.xliff.XLIFF2PackageWriter");
		ep.setPackageName("xlf2Pack");
		
		String inputPath = root+"/test01.properties";
		String outputPath = inputPath.replace("test01.", "test01.out.");
		URI inputURI = new File(inputPath).toURI();
		URI outputURI = new File(outputPath).toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_properties", outputURI, "UTF-8", locEN, locFR));
		
		inputPath = root+"/test02.html";
		outputPath = inputPath.replace("test02.", "test02.out.");
		inputURI = new File(inputPath).toURI();
		outputURI = new File(outputPath).toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_itshtml5", outputURI, "UTF-8", locEN, locFR));
		
		inputPath = root+"/sub Dir/test01.odt";
		outputPath = inputPath.replace("test01.", "test01.out.");
		inputURI = new File(inputPath).toURI();
		outputURI = new File(outputPath).toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_openoffice", outputURI, "UTF-8", locEN, locFR));
		
		pdriver.processBatch();

		File file = new File(root+"xlf2Pack/work/test01.properties.xlf");
		assertTrue(file.exists());
		file = new File(root+"xlf2Pack/work/test02.html.xlf");
		assertTrue(file.exists());
		file = new File(root+"xlf2Pack/work/sub Dir/test01.odt.xlf");
		assertTrue(file.exists());

		//=== Now merge
		
		deleteOutputDir("xlf2Pack/done", true);
		
		pdriver = new PipelineDriver();
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
		pdriver.setOutputDirectory(root);
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		MergingStep mrgStep = new MergingStep();
		pdriver.addStep(mrgStep);
		
//		Parameters prm = (Parameters)mrgStep.getParameters();
//		prm.setReturnRawDocument(true);
		
		inputURI = new File(root+"xlf2Pack/manifest.rkm").toURI();
		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_rainbowkit@noPrompt",
			null, "UTF-8", locEN, locFR));
		
		pdriver.processBatch();

		file = new File(root+"xlf2Pack/done/test01.out.properties");
		assertTrue(file.exists());
		file = new File(root+"xlf2Pack/done/test02.out.html");
		assertTrue(file.exists());
		file = new File(root+"xlf2Pack/done/sub Dir/test01.out.odt");
		assertTrue(file.exists());
		
		// Compare
		FileCompare fc = new FileCompare();
		fc.filesExactlyTheSame(root+"xlf2Pack/done/test01.out.properties",
			root+"xlf2Pack/original/test01.properties");
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
