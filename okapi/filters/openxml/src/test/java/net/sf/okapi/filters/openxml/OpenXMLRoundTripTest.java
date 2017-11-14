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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class OpenXMLRoundTripTest {
	private XMLFactories factories = new XMLFactoriesForTest();
	private LocaleId locENUS = LocaleId.fromString("en-us");

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private boolean allGood=true;

	@Before
	public void before() throws Exception {
		this.allGood = true;
	}

	@Test
	public void testHiddenTablesWithFormula() {
		ConditionalParameters cparams = getParametersFromUserInterface();
		cparams.setTranslateExcelHidden(false);
		runOneTest("hidden_table_with_formula.xlsx", true, false, cparams);
		assertTrue("Some Roundtrip files failed.",allGood);
	}

	@Test
	public void testHiddenMergeCells() {
		ConditionalParameters cparams = getParametersFromUserInterface();
		cparams.setTranslateExcelHidden(false);
		runOneTest("HiddenMergeCells.xlsx", true, false, cparams);
		assertTrue("Some Roundtrip files failed.",allGood);
	}

	@Test
	public void testPhoneticRunPropertyForAsianLanguages() {
		ConditionalParameters cparams = getParametersFromUserInterface();
		cparams.setTranslateExcelHidden(false);
		runOneTest("japanese_phonetic_run_property.xlsx", true, false, cparams);
		assertTrue("Some Roundtrip files failed.",allGood);
	}

	@Test
	public void testExternalHyperlinks() {
		ConditionalParameters cparams = getParametersFromUserInterface();
		cparams.setExtractExternalHyperlinks(true);
		runOneTest("external_hyperlink.docx", true, false, cparams);
		runOneTest("external_hyperlink.pptx", true, false, cparams);
		assertTrue("Some Roundtrip files failed.",allGood);
	}

	@Test
	public void testClarifiablePart() throws Exception {
		ConditionalParameters conditionalParameters = getParametersFromUserInterface();

		runOneTest("clarifiable-part-en.pptx", false, false, conditionalParameters, "", LocaleId.ENGLISH);
		runOneTest("clarifiable-part-ar.pptx", false, false, conditionalParameters, "", LocaleId.ARABIC);
		runOneTest("clarifiable-part-en.xlsx", false, false, conditionalParameters, "", LocaleId.ENGLISH);
		runOneTest("clarifiable-part-ar.xlsx", false, false, conditionalParameters, "", LocaleId.ARABIC);
		assertTrue("Some Roundtrip files failed.",allGood);
	}

	@Test
	public void testRevisionsAcceptance() throws Exception {
		ConditionalParameters conditionalParameters = getParametersFromUserInterface();
		conditionalParameters.setAutomaticallyAcceptRevisions(false);

		runOneTest("numbering-revisions.docx", false, false, conditionalParameters);
		runOneTest("table-grid-revisions.docx", false, false, conditionalParameters);
		assertTrue("Some Roundtrip files failed.",allGood);
	}

	@Test
	public void runTestsWithColumnExclusion() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
        params.setTranslateExcelExcludeColumns(true);
        params.tsExcelExcludedColumns = new TreeSet<String>();
        params.tsExcelExcludedColumns.add("1A");

        runOneTest("shared_string_in_two_columns.xlsx", true, false, params);
        assertTrue("Some Roundtrip files failed.", allGood);
	}

	// Slimmed-down version of some of the integration tests -- this checks for idempotency
	// by roundtripping once, then using the output of that to roundtrip again.  The first and
	// second roundtrip outputs should be the same.
	@Test
	public void runTestTwice() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
		runTestTwice("Escapades.docx", params);
	}

	@Test
	public void runTestsExcludeGraphicMetaData() {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateWordExcludeGraphicMetaData(true);
		runTests("exclude_graphic_metadata/", params,
				"textarea.docx",
				"picture.docx");
		assertTrue("Some Roundtrip files failed.", allGood);
	}

	@Test
	public void runTestsWithAggressiveTagStripping() {
		ConditionalParameters params = new ConditionalParameters();
		params.setCleanupAggressively(true);
		runTests("aggressive/", params,
				 "spacing.docx",
				 "vertAlign.docx");
		assertTrue("Some Roundtrip files failed.", allGood);
	}

	@Test
	public void runTestsWithHiddenCellsExposed() {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateExcelHidden(true);
		runTests("hidden_cells/", params, "hidden_cells.xlsx");
		runTests("hidden_cells/", params, "hidden_stuff.xlsx");
		runTests("hidden_cells/", params, "hidden_table.xlsx");
		assertTrue("Some Roundtrip files failed.", allGood);
	}

	@Test
	public void runTestWithStyledTextCell() {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateExcelHidden(true);
		runOneTest("styled_cells.xlsx", true, false, params);
	}

	/**
	 * Runs tests for all given files.
	 *
	 * @param files file names
	 */
	private void runTests(String goldSubDirPath, ConditionalParameters params, String... files) {
		for(String s : files)
		{
			runOneTest(s, true, false, params, goldSubDirPath);  // PigLatin
		}
		assertTrue("Some Roundtrip files failed.", allGood);
	}

	@Test
	public void runTestsAddLineSeparatorCharacter() {
		ConditionalParameters params = new ConditionalParameters();
		params.setAddLineSeparatorCharacter(true);

		List<String> files = new ArrayList<>();
		files.add("Document-with-soft-linebreaks.docx");
		//files.add("Document-with-soft-linebreaks.pptx");
		files.add("PageBreak.docx");

		runTests("lbaschar/", params, files.toArray(new String[0]));
	}

	@Test
	public void testAdditionalDocumentTypes() throws Exception {
		ConditionalParameters conditionalParameters = getParametersFromUserInterface();
		conditionalParameters.setTranslateExcelSheetNames(true);

		runOneTest("macro-2.docm", true, false, conditionalParameters);

		runOneTest("template-2.dotx", true, false, conditionalParameters);
		runOneTest("macro-template-2.dotm", true, false, conditionalParameters);

		runOneTest("macro-2.pptm", true, false, conditionalParameters);

		runOneTest("show-2.ppsx", true, false, conditionalParameters);
		runOneTest("macro-show-2.ppsm", true, false, conditionalParameters);

		runOneTest("template-2.potx", true, false, conditionalParameters);
		runOneTest("macro-template-2.potm", true, false, conditionalParameters);

		runOneTest("macro-2.xlsm", true, false, conditionalParameters);

		runOneTest("template-2.xltx", true, false, conditionalParameters);
		runOneTest("macro-template-2.xltm", true, false, conditionalParameters);

		runOneTest("2-pages.vsdx", true, false, conditionalParameters);
		runOneTest("2-pages.vsdm", true, false, conditionalParameters);

		assertTrue("Some Roundtrip files failed.", allGood);
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
//				filter = new OpenXMLFilter(new PigLatinTranslator(), locLA);
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

	public void runTestTwice (String filename, ConditionalParameters cparams) {
		Path sInputPath, sOutputPath;
		URI uri;
		try {
			sInputPath = Paths.get(getClass().getResource("/BoldWorld.docx").toURI()).getParent();
			sOutputPath = sInputPath.resolve("output");

			uri = sInputPath.resolve(filename).toUri();
			String writerFilename = "1_" + filename;
			Path outputPath1 = sOutputPath.resolve(writerFilename);

			roundTrip(uri, writerFilename, cparams);

			String writerFilename2 = "2_" + filename;
			Path outputPath2 = sOutputPath.resolve(writerFilename2);

			roundTrip(outputPath1.toUri(), writerFilename2, cparams);

			OpenXMLPackageDiffer differ = new OpenXMLPackageDiffer(Files.newInputStream(outputPath1),
																   Files.newInputStream(outputPath2));
			boolean same = differ.isIdentical();
			if (!same) {
				LOGGER.warn("{}{}", filename, (same ? " SUCCEEDED" : " FAILED"));
				for (OpenXMLPackageDiffer.Difference d : differ.getDifferences()) {
					LOGGER.warn("+ {}", d.toString());
				}
			}
			differ.cleanup();
			assertTrue(same);
		}
		catch ( Throwable e ) {
			LOGGER.warn("Failed to roundtrip file " + filename, e);
			fail("An unexpected exception was thrown on file '"+filename+e.getMessage());
		}
	}

	private void roundTrip(URI inputUri, String outputFilename, ConditionalParameters cparams) throws Exception {
		OpenXMLFilter filter = new OpenXMLFilter();
		try {
			filter.setParameters(cparams);
			filter.setOptions(locENUS, "UTF-8", true);

			Path sInputPath = Paths.get(getClass().getResource("/BoldWorld.docx").toURI()).getParent();
			Path sOutputPath = sInputPath.resolve("output");

			try {
				filter.open(new RawDocument(inputUri,"UTF-8", locENUS),true); // DWH 7-16-09 squishiness
			}
			catch(Exception e) {
				throw new OkapiException(e);
			}

			OpenXMLZipFilterWriter writer = new OpenXMLZipFilterWriter(cparams,
					factories.getInputFactory(), factories.getOutputFactory(), factories.getEventFactory());
			writer.setOptions(locENUS, "UTF-8");

			writer.setOutput(sOutputPath.resolve(outputFilename).toString());

			while ( filter.hasNext() ) {
				Event event = filter.next();
				if (event != null) {
					writer.handleEvent(event);
				}
			}
			writer.close();
		}
		finally {
			if (filter != null) filter.close();
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
