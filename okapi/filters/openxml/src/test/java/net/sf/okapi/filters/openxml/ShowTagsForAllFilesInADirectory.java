/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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
package net.sf.okapi.filters.openxml;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JFileChooser;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowTagsForAllFilesInADirectory {
	private XMLFactories factories = new XMLFactoriesForTest();
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private OpenXMLFilter openXMLFilter;
	private String[] testFileList;
	private LocaleId locENUS = LocaleId.fromString("en-us");

	public void doAll() {
		try {
			setUp();
			testAll();
			tearDown();
		}
		catch(Exception e)
		{
			LOGGER.warn(e.getMessage());
		}
	}
	
	public void setUp() throws Exception {
		openXMLFilter = new OpenXMLFilter(new TagPeekTranslator(),locENUS);	
		openXMLFilter.setOptions(locENUS, "UTF-8", true);
    	    testFileList = new String[1]; // timporary

	    JFileChooser chooser = new JFileChooser();
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the JDK.
	    ExampleFileFilter filenamefilter = new ExampleFileFilter();
	    filenamefilter.addExtension("docx");
	    filenamefilter.addExtension("pptx");
	    filenamefilter.addExtension("xlsx");
	    filenamefilter.setDescription("Office 2007 Files");
	    chooser.setFileFilter(filenamefilter);
	    chooser.setMultiSelectionEnabled(true);
	    chooser.setDialogTitle("Select files or click Cancel to skip this test");
	    int returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	File philly[] = chooser.getSelectedFiles();
	    	int plen = philly.length;
	    	testFileList = new String[plen];
	    	for(int i=0;i<plen;i++)
	    		testFileList[i] = philly[i].getAbsolutePath(); // .getName();	    	
	    }
	    else {
	    	testFileList = new String[]{}; // Empty to skip the manual tests
	    }
	}

	public void tearDown() {
		openXMLFilter.close();

	}

	public void testAll() throws URISyntaxException {
		Event event;
		String sOutputPath;
		OpenXMLZipFilterWriter writer;
		int flen;
		//String base=System.getProperty("user.dir").replace('\\','/').toLowerCase();

		for (String f : testFileList) {
                                                      if (f==null)
                                                        continue;
			String ff = f;
			String ff20 = ff.replace(" ","%20").toLowerCase();
			String ff20s = ff20.replace('\\','/');
			String fff = "file:/" + ff20s;// DWH 6-11-09 added file: and lowercase
			flen = f.length()-5;
			sOutputPath = f.substring(0,flen) + ".out" + f.substring(flen);
			writer = new OpenXMLZipFilterWriter(new ConditionalParameters(),
					factories.getInputFactory(), factories.getOutputFactory(), factories.getEventFactory());
			writer.setOptions(locENUS, "UTF-8");
			writer.setOutput(sOutputPath);
			try {
				URI uriFf = new URI(fff);
				openXMLFilter.open(new RawDocument(uriFf,"UTF-8",locENUS),true); // DWH 4-22-09
				while (openXMLFilter.hasNext()) {
					event = openXMLFilter.next();
					if (event!=null)
						writer.handleEvent(event);
					else
						event = null; // just for debugging
				}
				writer.close();
			} catch (Exception e) {
				throw new OkapiException("Error for file: " + f + ": " + e.toString());
			}
		}
	}
}
