/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.batchconfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.okapi.applications.rainbow.Input;
import net.sf.okapi.applications.rainbow.pipeline.PipelineStorage;
import net.sf.okapi.applications.rainbow.pipeline.PipelineWrapper;
import net.sf.okapi.applications.rainbow.pipeline.StepInfo;
import net.sf.okapi.common.ExecutionContext;
import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipeline.IPipeline;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.plugins.PluginsManager;
import net.sf.okapi.steps.rainbowkit.creation.Parameters;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.rules.TemporaryFolder;

@RunWith(JUnit4.class)
public class BatchConfigurationTest {
    private static final String OKF_CUSTOM_HTML = "okf_custom_html";
    private static final String OKF_CUSTOM_XML = "okf_custom_xml";
    private static final String OKF_OPENXML = "okf_openxml";
    private static final String OKF_PO = "okf_po";
    private static final String OKF_XLIFF = "okf_xliff";
    
    @Rule
	public TemporaryFolder folder = new TemporaryFolder();

    private static FilterConfigurationMapper fcMapper;
	
	@BeforeClass
	public static void setup() throws Exception {
        fcMapper = new FilterConfigurationMapper();
		// Get pre-defined configurations
		DefaultFilters.setMappings(fcMapper, false, true);
	}
	
	@Test
	public void testExtensionMappings() throws Exception {
        // creates a temp folder
        File tmpFolder = folder.newFolder("temp");

        // plugin manager
        PluginsManager pm = new PluginsManager();
        pm.discover(tmpFolder, false);

        // creates a simple PipelineWrapper
        PipelineWrapper wrapper = new PipelineWrapper(
            fcMapper, "", pm, "", "", "", null, new ExecutionContext());

        wrapper.load( Paths.get(
        		getClass().getClassLoader().getResource("bconfTest.pln").toURI() ).toString() );
        
		// just make sure the initial value is false, this should get overriden later
        assertFalse(((Parameters)wrapper.getPipeline().getSteps().get(2).getParameters()).getCreateZip());
        
        // creates a list of Input files
        Input html = new Input();
        html.relativePath = "test.html";
        html.filterConfigId = OKF_CUSTOM_HTML;

        Input empty = new Input();
        empty.relativePath = "test";
        empty.filterConfigId = OKF_CUSTOM_XML;

        ArrayList<Input> inputFiles = new ArrayList<Input>();
        inputFiles.add(html);
        inputFiles.add(empty);

        File batchConfigFile = new File(tmpFolder, "exported.bconf");
        
        // export batch config file
        BatchConfiguration bc = new BatchConfiguration();
        bc.exportConfiguration(batchConfigFile.getAbsolutePath(), wrapper,
            fcMapper, inputFiles);

        // check if the batch config file has been created
        assertTrue(batchConfigFile.exists());
        
        Map<String, String> stepParamOverrides = new HashMap<>();
        stepParamOverrides.put("net.sf.okapi.steps.rainbowkit.creation.ExtractionStep", 
        		"#v1\ncreateZip.b=true");
        
        // de-compose the batch config file
        bc.installConfiguration(batchConfigFile.getAbsolutePath(),
            tmpFolder.getAbsolutePath(), wrapper, stepParamOverrides);

        // read the extension mapping file into a hash table
		BufferedReader fh = new BufferedReader(new FileReader(
                new File(tmpFolder, "extensions-mapping.txt")));
        
		HashMap<String, String> filterConfigByExtension
            = new HashMap<String, String>();
		
		String s;
		while ((s = fh.readLine()) != null) {
			String fields[] = s.split("\t");
			String ext = fields[0];
			String fc = fields[1];
			
			filterConfigByExtension.put(ext, fc);
		}
		fh.close();

        // check if the configured file extensions exist
        assertTrue(
            OKF_CUSTOM_HTML.equals(filterConfigByExtension.get(".html")));
        assertTrue(
            OKF_CUSTOM_XML.equals(filterConfigByExtension.get("")));
        assertTrue(
            OKF_PO.equals(filterConfigByExtension.get(".po")));

        assertTrue(OKF_OPENXML.equals(filterConfigByExtension.get(".docx"))
                && OKF_OPENXML.equals(filterConfigByExtension.get(".docm"))
                && OKF_OPENXML.equals(filterConfigByExtension.get(".pptx"))
                && OKF_OPENXML.equals(filterConfigByExtension.get(".pptm"))
                && OKF_OPENXML.equals(filterConfigByExtension.get(".ppsx"))
                && OKF_OPENXML.equals(filterConfigByExtension.get(".ppsm"))
                && OKF_OPENXML.equals(filterConfigByExtension.get(".potx"))
                && OKF_OPENXML.equals(filterConfigByExtension.get(".potm"))
                && OKF_OPENXML.equals(filterConfigByExtension.get(".xlsx"))
                && OKF_OPENXML.equals(filterConfigByExtension.get(".xlsm"))
                && OKF_OPENXML.equals(filterConfigByExtension.get(".xltx"))
                && OKF_OPENXML.equals(filterConfigByExtension.get(".xltm"))
        );

        assertTrue(
            OKF_OPENXML.equals(filterConfigByExtension.get(".vsdx")));
        assertTrue(
            OKF_XLIFF.equals(filterConfigByExtension.get(".mxliff")));
        
        //check the installed pipeline
        PipelineStorage pipelineStore = new PipelineStorage(wrapper.getAvailableSteps(), 
        		new File(tmpFolder, "pipeline.pln").getAbsolutePath());
        IPipeline pipeline = pipelineStore.read();
        List<IPipelineStep> steps = pipeline.getSteps();
        
        assertEquals(3, steps.size());
        assertEquals("net.sf.okapi.steps.xsltransform.XSLTransformStep", steps.get(0).getClass().getName());
        assertEquals("net.sf.okapi.steps.common.RawDocumentToFilterEventsStep", steps.get(1).getClass().getName());
        assertEquals("net.sf.okapi.steps.rainbowkit.creation.ExtractionStep", steps.get(2).getClass().getName());
        
        Parameters p = (Parameters) steps.get(2).getParameters();
        assertTrue(p.getCreateZip());
	}
}
