
/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.pipelinedriver.BatchItemContext;
import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.openxml.OpenXMLFilter;
import net.sf.okapi.filters.rainbowkit.RainbowKitFilter;
import net.sf.okapi.lib.xliff2.core.Part.GetTarget;
import net.sf.okapi.lib.xliff2.core.Segment;
import net.sf.okapi.lib.xliff2.core.Unit;
import net.sf.okapi.lib.xliff2.processor.DefaultEventHandler;
import net.sf.okapi.lib.xliff2.processor.XLIFFProcessor;
import net.sf.okapi.lib.xliff2.reader.Event;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.rainbowkit.creation.ExtractionStep;
import net.sf.okapi.steps.rainbowkit.creation.Parameters;
import net.sf.okapi.steps.rainbowkit.postprocess.MergingStep;

public class Main {
	
	static LocaleId locEN = LocaleId.ENGLISH;
	static LocaleId locFR = LocaleId.fromString("fr");
	static String root;

	public static void main (String[] args)
		throws URISyntaxException
	{
		URL inputUrl = Main.class.getResource("myDoc.docx");
		File inputFile = new File(inputUrl.toURI());
		root = inputFile.getParent();
		
		// Extract and XLIFF2 t-kit in a 'pack1' sub-directory in the directory of the input file
		extract(inputFile);
		
		// Make some change in the extracted file
		File xliffFile = new File(root + File.separator 
			+ "pack1" + File.separator + "work" + File.separator 
			+ inputFile.getName() + ".xlf");
		modifyXLIFF(xliffFile);
		
		// Merge the XLIFF2 file back
		// Result goes to the 'done' sub-directory of the 'pack1' directory
		File manifestFile = new File(root + File.separator + "pack1" + File.separator + "manifest.rkm");
		merge(manifestFile);
	}

	private static void extract (File inputFile) {
		try {
			// Create the pipeline driver
			IPipelineDriver driver = createDriver(root);
			// Add the extraction step
			driver.addStep(new RawDocumentToFilterEventsStep());
			// Create and set up the t-kit creation step
			IPipelineStep extStep = new ExtractionStep();
			Parameters params = (Parameters)extStep.getParameters();
			params.setWriterClass("net.sf.okapi.steps.rainbowkit.xliff.XLIFF2PackageWriter");
			// Add the t-kit creation step
			driver.addStep(extStep);
			
			// Add the input file to the driver
			RawDocument rawDoc = new RawDocument(inputFile.toURI(), "UTF-8", locEN, locFR, "okf_openxml");
			// Set the output information (it goes in the manifest)
			String path = inputFile.getAbsolutePath();
			String outputPath = Util.getDirectoryName(path) + File.separator 
				+ Util.getFilename(path, false) + ".out" + Util.getExtension(path);
			File outputFile = new File(outputPath);
			// Create the batch item to process and add it to the driver
			BatchItemContext item = new BatchItemContext(rawDoc, outputFile.toURI(), "UTF-8");
			driver.addBatchItem(item);
			// Run the pipeline
			driver.processBatch();
		}
		catch ( Throwable e ) {
			e.printStackTrace();
		}
	}

	private static void modifyXLIFF (File file) {
		try {
			// Create a processor that add some text at the end 
			// of each non-empty segment
			XLIFFProcessor proc = new XLIFFProcessor();
			proc.add(new DefaultEventHandler() {
				@Override
				public Event handleUnit (Event event) {
					Unit unit = event.getUnit();
					for ( Segment segment : unit.getSegments() ) {
						if ( segment.getSource().isEmpty() ) continue;
						segment.getTarget(GetTarget.CLONE_SOURCE).append(" blah blah...");
					}
					return event;
				}
			});
			// Run the processor (read and write)
			File tmpFile = new File(file.getAbsolutePath()+".tmp");
			proc.run(file, tmpFile);
			file.delete();
			tmpFile.renameTo(file);
		}
		catch ( Throwable e ) {
			e.printStackTrace();
		}
	}
	
	private static void merge (File manifestFile) {
		try {
			IPipelineDriver driver = createDriver(root);
			// Add the extraction step
			driver.addStep(new RawDocumentToFilterEventsStep());
			// Add the t-kit merging step
			driver.addStep(new MergingStep());
			
			// Add the input file (manifest file) to the driver
			RawDocument rawDoc = new RawDocument(manifestFile.toURI(), "UTF-8", locEN, locFR, "okf_rainbowkit-noprompt");
			driver.addBatchItem(rawDoc);
			// Run the pipeline
			driver.processBatch();
		}
		catch ( Throwable e ) {
			e.printStackTrace();
		}
	}

	static IPipelineDriver createDriver (String root) {
		// Create the pipeline driver
		IPipelineDriver driver = new PipelineDriver();
		// Create a filter configuration map
		IFilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations(OpenXMLFilter.class.getName());
		fcMapper.addConfigurations(RainbowKitFilter.class.getName());
		// Set the filter configuration map to use with the driver
		driver.setFilterConfigurationMapper(fcMapper);
		// Set the root folder for the driver's context
		driver.setRootDirectories(root, root);
		driver.setOutputDirectory(root);
		return driver;
	}

}
