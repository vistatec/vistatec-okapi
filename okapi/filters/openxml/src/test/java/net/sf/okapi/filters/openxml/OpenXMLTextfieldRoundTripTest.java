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
import static org.junit.Assert.fail;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class OpenXMLTextfieldRoundTripTest {
	private XMLFactories factories = new XMLFactoriesForTest();
	private LocaleId locENUS = LocaleId.fromString("en-us");

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private boolean allGood=true;

	private String fileName;

	@Parameterized.Parameters
	public static Object[][] testdata(){
		return new Object[][]{
				{"Textfield.docx"},
				{"ComplexTextfield.docx"}
		};
	}

	public OpenXMLTextfieldRoundTripTest(String fileName) {
		this.fileName = fileName;
	}

	@Before
	public void before() throws Exception {
		this.allGood = true;
	}

	@Test
	public void runTestsWithTextfield() {
		ConditionalParameters cparams = new ConditionalParameters();
		cparams.tsComplexFieldDefinitionsToExtract.add("FORMTEXT");

 		runOneTest(fileName, true, false, cparams, "textfield");
		assertTrue("Some Roundtrip files failed.",allGood);
	}


	public void runOneTest (String filename, boolean bTranslating, boolean bPeeking, ConditionalParameters cparams) {
		runOneTest(filename, bTranslating, bPeeking, cparams, "");
	}

	public void runOneTest (String filename, boolean bTranslating, boolean bPeeking, ConditionalParameters cparams,
							String goldSubdirPath) {
		runOneTest(filename, bTranslating, bPeeking, cparams, goldSubdirPath, locENUS);
	}

	public void runOneTest (String filename, boolean bTranslating, boolean bPeeking, ConditionalParameters cparams,
						    String goldSubdirPath, LocaleId localeId) {
		Path sInputPath, sOutputPath, sGoldPath;
		Event event;
		URI uri;
		OpenXMLFilter filter = null;
		boolean rtrued2;
		try {
			if (bPeeking)
			{
				if (bTranslating)
					filter = new OpenXMLFilter(new CodePeekTranslator(), localeId);
				else
					filter = new OpenXMLFilter(new TagPeekTranslator(), localeId);
			}
			else if (bTranslating) {
				localeId = LocaleId.fromString("en-US"); // don't lower-case the US
				filter = new OpenXMLFilter(new PigLatinTranslator(), localeId);
			}
			else
				filter = new OpenXMLFilter();
			
			filter.setParameters(cparams);
			filter.setOptions(locENUS, "UTF-8", true);

			sInputPath = Paths.get(getClass().getResource("/BoldWorld.docx").toURI()).getParent();
			sOutputPath = sInputPath.resolve("output");
			sGoldPath = sInputPath.resolve("gold").resolve(goldSubdirPath);

			uri = sInputPath.resolve(filename).toUri();
			
			try
			{
				filter.open(new RawDocument(uri,"UTF-8", localeId));
			}
			catch(Exception e)
			{
				throw new OkapiException(e);				
			}
			
			OpenXMLZipFilterWriter writer = new OpenXMLZipFilterWriter(cparams,
						factories.getInputFactory(), factories.getOutputFactory(), factories.getEventFactory());

			if (bPeeking)
				writer.setOptions(localeId, "UTF-8");
			else if (bTranslating)
				writer.setOptions(localeId, "UTF-8");
//				writer.setOptions(locLA, "UTF-8");
			else
				writer.setOptions(localeId, "UTF-8");

			String writerFilename = bPeeking ? (bTranslating ? "Peek" : "Tag") : (bTranslating ? "Tran" : "Out")+filename;
			writer.setOutput(sOutputPath.resolve(writerFilename).toString());
			
			while ( filter.hasNext() ) {
				event = filter.next();
				if (event!=null)
				{
					writer.handleEvent(event);
				}
				else
					event = null; // just for debugging
			}
			writer.close();
			Path outputPath = sOutputPath.resolve(
					bPeeking ? (bTranslating ? "Peek" : "Tag") : (bTranslating ? "Tran" : "Out") + filename);
			Path goldPath = sGoldPath.resolve(
					bPeeking ? (bTranslating ? "Peek" : "Tag") : (bTranslating ? "Tran" : "Out") + filename);

			OpenXMLPackageDiffer differ = new OpenXMLPackageDiffer(Files.newInputStream(goldPath),
																   Files.newInputStream(outputPath));
			rtrued2 = differ.isIdentical();
			if (!rtrued2) {
				LOGGER.warn("{}{}{}",
							(bPeeking ? (bTranslating ? "Peek" : "Tag") : (bTranslating ? "Tran" : "Out")),
							filename, (rtrued2 ? " SUCCEEDED" : " FAILED"));
				LOGGER.warn("Gold: {}\nOutput: {}", goldPath, outputPath);
				for (OpenXMLPackageDiffer.Difference d : differ.getDifferences()) {
					LOGGER.warn("+ {}", d.toString());
				}
			}
			if (!rtrued2)
				allGood = false;
			differ.cleanup();
		}
		catch ( Throwable e ) {
			LOGGER.warn("Failed to roundtrip file " + filename, e);
			fail("An unexpected exception was thrown on file '"+filename+e.getMessage());
		}
		finally {
			if ( filter != null ) filter.close();
		}
	}

}
