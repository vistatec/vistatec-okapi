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

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Comment the following to remove the parameter UI $$$
//import net.sf.okapi.ui.filters.openxml.Editor;

/**
 * This tests OpenXMLFilter (including OpenXMLContentFilter) and
 * OpenXMLZipFilterWriter (including OpenXMLContentSkeleton writer)
 * by filtering, automatically translating, and then writing the
 * zip file corresponding to a Word, Excel or Powerpoint 2009 file, 
 * then comparing it to a gold file to make sure nothing has changed.
 * It does this with a specific list of files.
 * 
 * <p>This is done with no translator first, to make sure the same
 * file is created that was filtered in the first place.  Then it
 * is translated into Pig Latin by PigLatinTranslator, translated so
 * codes are expanded by CodePeekTranslator, and then translated to
 * see a view like the translator will see by TagPeekTranslator.
 */

@RunWith(JUnit4.class)
public class OpenXMLRoundTripSequenceTest {
	private XMLFactories factories = new XMLFactoriesForTest();
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private boolean allGood=true;
	private ConditionalParameters cparams; // DWH 6-18-09
	private boolean bSquishy=true; // DWH 7-16-09
	private OpenXMLFilter filter=null;
	private LocaleId locLA = LocaleId.fromString("la");
	private LocaleId locENUS = LocaleId.fromString("en-US"); // don't lower-case the US
//	private LocaleId locENUS = LocaleId.fromString("en-US");

	@Test
	public void runTest () throws Exception {
		cparams = getParametersFromUserInterface();

		ArrayList<String> themfiles = new ArrayList<String>();
		themfiles.add("BoldWorld.docx");
		themfiles.add("sample.docx");
		
//		filter = new OpenXMLFilter(new PigLatinTranslator(), locLA);
		filter = new OpenXMLFilter(new PigLatinTranslator(), locENUS);
		for(String s : themfiles)
		{
			runOneTest(s,filter); // English
		}
		assertTrue("Some Roundtrip files failed.",allGood);
	}

	public void runOneTest (String filename, OpenXMLFilter filter) throws Exception {
		Path sInputPath=null,sOutputPath=null,sGoldPath=null;
		Event event;
		URI uri;
		boolean rtrued2;
		try {	
			filter.setParameters(cparams);

			filter.setOptions(locENUS, "UTF-8", true);

			sInputPath = Paths.get(getClass().getResource("/BoldWorld.docx").toURI()).getParent();
			sOutputPath = sInputPath.resolve("output");
			sGoldPath = sInputPath.resolve("gold");

			uri = sInputPath.resolve(filename).toUri();
			try
			{
				filter.open(new RawDocument(uri,"UTF-8",locENUS),true); // DWH 7-16-09 squishiness
			}
			catch(Exception e)
			{
				throw new OkapiException(e);				
			}
			
			OpenXMLZipFilterWriter writer = new OpenXMLZipFilterWriter(cparams,
					factories.getInputFactory(), factories.getOutputFactory(), factories.getEventFactory());

//			writer.setOptions(locLA, "UTF-8");
			writer.setOptions(locENUS, "UTF-8");

			Path outputPath = sOutputPath.resolve("Tran" + filename);

			writer.setOutput(outputPath.toString());
			
			while ( filter.hasNext() ) {
				event = filter.next();
				if (event!=null)
				{
//					if (event.getEventType()==EventType.START_SUBDOCUMENT) // DWH 4-16-09 was START_DOCUMENT
// 6-27-09				writer.setParameters(filter.getParameters());
					writer.handleEvent(event);
				}
				else
					event = null; // just for debugging
			}
			writer.close();
			Path goldPath = sGoldPath.resolve("Tran" + filename);
			OpenXMLPackageDiffer differ = new OpenXMLPackageDiffer(Files.newInputStream(goldPath),
																   Files.newInputStream(outputPath));

			rtrued2 = differ.isIdentical();
			if (!rtrued2) {
				allGood = false;
				LOGGER.warn("Tran{}{} FAILED", filename);
				for (OpenXMLPackageDiffer.Difference d : differ.getDifferences()) {
					LOGGER.warn("+ {}", d.toString());
				}
			}
			differ.cleanup();
		}
		finally {
			if ( filter != null ) filter.close();
		}
	}
	private ConditionalParameters getParametersFromUserInterface()
	{
		ConditionalParameters parms;
//    Choose the first to get the UI $$$
//		parms = (new Editor()).getParametersFromUI(new ConditionalParameters());
		parms = new ConditionalParameters();
		return parms;
	}
}
