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

package net.sf.okapi.steps.rainbowkit.creation;

import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.pipelinedriver.BatchItemContext;
import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.filters.its.html5.HTML5Filter;
import net.sf.okapi.filters.openoffice.OpenOfficeFilter;
import net.sf.okapi.filters.openxml.OpenXMLFilter;
import net.sf.okapi.filters.properties.PropertiesFilter;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.common.createtarget.CreateTargetStep;
import net.sf.okapi.steps.rainbowkit.ontram.OntramPackageWriter;

import org.custommonkey.xmlunit.XMLAssert;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class ExtractionStepTest {
	
	private String root;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	private LocaleId locENUS = LocaleId.fromString("en-us");
	private LocaleId locRURU = LocaleId.fromString("ru-ru");
	
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
	public void stub () {
		assertTrue(true);
	}
	
	@Test
	public void testSimpleStep ()
		throws URISyntaxException
	{
		// Ensure output is deleted
		assertTrue(Util.deleteDirectory(new File(root+"pack1")));
		
		IPipelineDriver pdriver = new PipelineDriver();
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations(PropertiesFilter.class.getName());
		fcMapper.addConfigurations(OpenOfficeFilter.class.getName());
		fcMapper.addConfigurations(HTML5Filter.class.getName());
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		pdriver.addStep(new ExtractionStep());

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

		File file = new File(root+"pack1/work/test01.properties.xlf");
		assertTrue(file.exists());
		file = new File(root+"pack1/work/test02.html.xlf");
		assertTrue(file.exists());
		file = new File(root+"pack1/work/sub Dir/test01.odt.xlf");
		assertTrue(file.exists());
	}

// TODO MW: Unit test for issue #534
//	@Test
//	public void testICMLtoXLIFF2() throws URISyntaxException
//	{
//		// Ensure output is deleted
//		assertTrue(Util.deleteDirectory(new File(root+"pack1")));
//
//		IPipelineDriver pdriver = new PipelineDriver();
//		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
//		fcMapper.addConfigurations(ICMLFilter.class.getName());
//		pdriver.setFilterConfigurationMapper(fcMapper);
//		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
//		pdriver.addStep(new RawDocumentToFilterEventsStep());
//		ExtractionStep extractionStep = new ExtractionStep();
//		Parameters params = new Parameters();
//		params.setWriterClass("net.sf.okapi.steps.rainbowkit.xliff.XLIFF2PackageWriter");
//		extractionStep.setParameters(params);
//		pdriver.addStep(extractionStep);
//
//		String inputPath = root+"/Bullets_Test_EN.icml";
//		String outputPath = inputPath.replace("Bullets_Test_EN.", "Bullets_Test_EN.out.");
//		URI inputURI = new File(inputPath).toURI();
//		URI outputURI = new File(outputPath).toURI();
//		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_icml", outputURI, "UTF-8", locEN, locFR));
//
//		pdriver.processBatch();
//
//		File file = new File(root+"pack1/work/Bullets_Test_EN.icml.xlf");
//		assertTrue(file.exists());
//
//		XLIFFDocument xlf = new XLIFFDocument();
//		xlf.load(file);
//		assertNotNull("There should be a file element in the generated XLIFF file.", xlf.getFileNode("f1"));
//		assertEquals("The XLIFF file should have one unit.", 1, xlf.getUnits().size());
//		Unit unit = xlf.getUnits().get(0);
//		assertEquals("The unit should have three segments.", 3, unit.getSegmentCount());
//	}

//  TODO MW: This test below is running fine, but only with XLIFF2 toolkit 1.1.5-SNAPSHOT.
// 				Need to wait until release of 1.1.5. before activating this test.
//
//	@Test
//	public void testICMLwithCDATAtoXLIFF2() throws URISyntaxException
//	{
//		// NOTE MW: Unit test for issue #527
//
//		// Ensure output is deleted
//		assertTrue(Util.deleteDirectory(new File(root+"pack1")));
//
//		IPipelineDriver pdriver = new PipelineDriver();
//		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
//		fcMapper.addConfigurations(ICMLFilter.class.getName());
//		pdriver.setFilterConfigurationMapper(fcMapper);
//		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
//		pdriver.addStep(new RawDocumentToFilterEventsStep());
//		ExtractionStep extractionStep = new ExtractionStep();
//		Parameters params = new Parameters();
//		params.setWriterClass("net.sf.okapi.steps.rainbowkit.xliff.XLIFF2PackageWriter");
//		extractionStep.setParameters(params);
//		pdriver.addStep(extractionStep);
//
//		String inputPath = root+"/CDATA_Test.icml";
//		String outputPath = inputPath.replace("CDATA_Test.", "CDATA_Test.out.");
//		URI inputURI = new File(inputPath).toURI();
//		URI outputURI = new File(outputPath).toURI();
//		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_icml", outputURI, "UTF-8", locEN, locFR));
//
//		pdriver.processBatch();
//
//		File file = new File(root+"pack1/work/CDATA_Test.icml.xlf");
//		assertTrue(file.exists());
//
//		XLIFFDocument xlf = new XLIFFDocument();
//		xlf.load(file);
//		assertNotNull("There should be a file element in the generated XLIFF file.", xlf.getFileNode("f1"));
//		assertEquals("The XLIFF file should have two units.", 2, xlf.getUnits().size());
//	}

	@DataProvider
	public static Object[][] extendedCodeTypesDataProvider() {
		return new Object[][]{
				{"extended-code-type-support.docx", OpenXMLFilter.class.getName(), "okf_openxml"},
				{"extended-code-type-support.docx_en-US_fr-FR.sdlxliff", XLIFFFilter.class.getName(), "okf_xliff"},
		};
	}

	@Test
	@UseDataProvider("extendedCodeTypesDataProvider")
	public void testExtendedCodeTypes(String filename, String filterClassName, String filterConfigurationId) throws Exception {
		Path rootPath = Paths.get(root);
		Path packPath = rootPath.resolve("pack1");

		assertTrue(Util.deleteDirectory(packPath.toFile()));

		FilterConfigurationMapper filterConfigurationMapper = new FilterConfigurationMapper();
		filterConfigurationMapper.addConfigurations(filterClassName);

		Parameters parameters = new Parameters();
		parameters.setWriterOptions("#v1\nincludeCodeAttrs.b=true");
		IPipelineStep extractionStep = new ExtractionStep();
		extractionStep.setParameters(parameters);

		IPipelineDriver pdriver = new PipelineDriver();
		pdriver.setFilterConfigurationMapper(filterConfigurationMapper);
		pdriver.setRootDirectories(rootPath.toString(), rootPath.toString());
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		pdriver.addStep(extractionStep);

		Path inputPath = rootPath.resolve("code-type").resolve(filename);
		Path outputPath = packPath.resolve("work/code-type").resolve(filename + ".xlf");
		pdriver.addBatchItem(new BatchItemContext(inputPath.toUri(), UTF_8.name(), filterConfigurationId, outputPath.toUri(), UTF_8.name(), locENUS, locFR));

		pdriver.processBatch();
		assertTrue(outputPath.toFile().exists());
		try (Reader out = Files.newBufferedReader(outputPath, UTF_8);
				Reader gold = Files.newBufferedReader(rootPath.resolve("code-type/gold").resolve(filename + ".xlf"), UTF_8)) {
		    XMLAssert.assertXMLEqual(gold, out);
		}
	}

	@Test
	public void testXINICreation ()	throws Exception
	{
		// Ensure output is deleted
		assertTrue(deleteOutputDir("pack2", true));
		
		IPipelineDriver pdriver = new PipelineDriver();
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations(XLIFFFilter.class.getName());
		pdriver.setFilterConfigurationMapper(fcMapper);
		pdriver.setRootDirectories(Util.deleteLastChar(root), 
				Util.deleteLastChar(root)); // Don't include final separator
		pdriver.addStep(new RawDocumentToFilterEventsStep());
		pdriver.addStep(new CreateTargetStep());
		
		ExtractionStep es = new ExtractionStep();
		pdriver.addStep(es);
		Parameters params = (Parameters) es.getParameters();
		params.setWriterClass(OntramPackageWriter.class.getName());
		params.setPackageName("pack2");

		Path rootPath = Paths.get(root);
		Path inputPath = rootPath.resolve("xiniPack/original/test1.xlf");
		Path outputPath = rootPath.resolve("pack2/original/test1.out.xlf");
		pdriver.addBatchItem(new BatchItemContext(inputPath.toUri(), "UTF-8", "okf_xliff", outputPath.toUri(), 
				"UTF-8", locENUS, locRURU));
		
		pdriver.processBatch();

		File file = new File(root+"pack2/xini/contents.xini");
		assertTrue(file.exists());
		
		// Compare with the gold file
		try (Reader goldReader = Files.newBufferedReader(rootPath.resolve("xiniPack/xini/contents.xini"),
		                                                 StandardCharsets.UTF_8)) {
		    String actual = TestUtil.getFileAsString(new File(root+"pack2/xini/contents.xini"))
                                    .replaceFirst("xiniPack/original", "");
		    XMLAssert.assertXMLEqual(goldReader, new StringReader(actual));
		}

		assertTrue(deleteOutputDir("pack2", true));
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
