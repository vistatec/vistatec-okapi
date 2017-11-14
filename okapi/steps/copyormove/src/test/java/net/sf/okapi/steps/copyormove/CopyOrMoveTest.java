/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.copyormove;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import net.sf.okapi.common.FileCompare;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipelinedriver.BatchItemContext;
import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CopyOrMoveTest {

	private String root;
	private CopyOrMoveStep step;
	private IPipelineDriver pdriver;
	private Parameters params;
	
	public CopyOrMoveTest() {
	}

	@Before
	public void setUp() {
		root = TestUtil.getParentDir(this.getClass(), "/test_folder/from_complex");
		step = new CopyOrMoveStep();
		resetFiles();
		Util.deleteDirectory(root + "to_empty/", true);
		params = (Parameters) step.getParameters();
		pdriver = new PipelineDriver();
		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root));
		pdriver.addStep(step);
	}

	@Test
	public void testBasicCopy() throws FileNotFoundException {
		params.setCopyOption("overwrite");
		params.setMove(false);
		addFiles(pdriver, root + "/to_empty", true);
		pdriver.processBatch();
		
		FileCompare fc = new FileCompare();
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test00.txt", root + "/to_empty/test00.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test01.txt", root + "/to_empty/test01.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test02.txt", root + "/to_empty/test02.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test03.txt", root + "/to_empty/test03.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test04.txt", root + "/to_empty/test04.txt", "UTF-8"));
	}
	
	@Test
	public void testBasicMove() throws FileNotFoundException {
		params.setCopyOption("overwrite");
		params.setMove(true);
		addFiles(pdriver, root + "/to_empty", true);
		pdriver.processBatch();
		
		resetFiles();
		
		FileCompare fc = new FileCompare();
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test00.txt", root + "/to_empty/test00.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test01.txt", root + "/to_empty/test01.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test02.txt", root + "/to_empty/test02.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test03.txt", root + "/to_empty/test03.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test04.txt", root + "/to_empty/test04.txt", "UTF-8"));
	}

	@Test
	public void testStructuredCopy() throws FileNotFoundException {
		params.setCopyOption("overwrite");
		params.setMove(false);
		addFiles(pdriver, root + "/to_empty", false);
		pdriver.processBatch();
		
		FileCompare fc = new FileCompare();
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/test00.txt", root + "/to_empty/test00.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir01/subdir11/test01.txt", root + "/to_empty/subdir01/subdir11/test01.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir01/test02.txt", root + "/to_empty/subdir01/test02.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir02/test03.txt", root + "/to_empty/subdir02/test03.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir02/test04.txt", root + "/to_empty/subdir02/test04.txt", "UTF-8"));
	}

	@Test
	public void testStructuredMove() throws FileNotFoundException {
		params.setCopyOption("overwrite");
		params.setMove(true);
		addFiles(pdriver, root + "/to_empty", false);
		pdriver.processBatch();
			
		resetFiles();
		
		FileCompare fc = new FileCompare();
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/test00.txt", root + "/to_empty/test00.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir01/subdir11/test01.txt", root + "/to_empty/subdir01/subdir11/test01.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir01/test02.txt", root + "/to_empty/subdir01/test02.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir02/test03.txt", root + "/to_empty/subdir02/test03.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir02/test04.txt", root + "/to_empty/subdir02/test04.txt", "UTF-8"));
	}

	@Test
	public void testOverwrite() throws FileNotFoundException {
		params.setCopyOption("overwrite");
		params.setMove(false);
		addFiles(pdriver, root + "/to_empty", true);
		resetTargetFiles(true);
		pdriver.processBatch();
		
		FileCompare fc = new FileCompare();
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test00.txt", root + "/to_empty/test00.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test01.txt", root + "/to_empty/test01.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test02.txt", root + "/to_empty/test02.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test03.txt", root + "/to_empty/test03.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test04.txt", root + "/to_empty/test04.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_flat/test05.txt", root + "/to_empty/test05.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_flat/test06.txt", root + "/to_empty/test06.txt", "UTF-8"));
	}
	
	@Test
	public void testBackup() throws FileNotFoundException {
		params.setCopyOption("backup");
		params.setMove(false);
		addFiles(pdriver, root + "/to_empty", true);
		resetTargetFiles(true);
		pdriver.processBatch();
		
		FileCompare fc = new FileCompare();
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test00.txt", root + "/to_empty/test00.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test01.txt", root + "/to_empty/test01.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test02.txt", root + "/to_empty/test02.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test03.txt", root + "/to_empty/test03.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_flat/test04.txt", root + "/to_empty/test04.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_flat/test05.txt", root + "/to_empty/test05.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_flat/test06.txt", root + "/to_empty/test06.txt", "UTF-8"));
	}
	
	@Test
	public void testSkip() throws FileNotFoundException {
		params.setCopyOption("skip");
		params.setMove(false);
		addFiles(pdriver, root + "/to_empty", true);
		resetTargetFiles(true);
		pdriver.processBatch();
		
		FileCompare fc = new FileCompare();
		assertFalse(fc.compareFilesPerLines(root + "/gold_flat/test00.txt", root + "/to_empty/test00.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_flat/test01.txt", root + "/to_empty/test01.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_flat/test02.txt", root + "/to_empty/test02.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_flat/test03.txt", root + "/to_empty/test03.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_flat/test04.txt", root + "/to_empty/test04.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_flat/test05.txt", root + "/to_empty/test05.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_flat/test06.txt", root + "/to_empty/test06.txt", "UTF-8"));
	}

	@Test
	public void testStructuredOverwrite() throws FileNotFoundException {
		params.setCopyOption("overwrite");
		params.setMove(false);
		addFiles(pdriver, root + "/to_empty", false);
		resetTargetFiles(false);
		pdriver.processBatch();
		
		FileCompare fc = new FileCompare();
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/test00.txt", root + "/to_empty/test00.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir01/subdir11/test01.txt", root + "/to_empty/subdir01/subdir11/test01.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir01/test02.txt", root + "/to_empty/subdir01/test02.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir02/test03.txt", root + "/to_empty/subdir02/test03.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir02/test04.txt", root + "/to_empty/subdir02/test04.txt", "UTF-8"));
		//FIXME missing file: assertFalse(fc.compareFilesPerLines(root + "/gold_complex/subdir01/subdir11/test05.txt", root + "/to_empty/subdir01/subdir11/test05.txt", "UTF-8"));
		//FIXME missing file: assertFalse(fc.compareFilesPerLines(root + "/gold_complex/test06.txt", root + "/to_empty/test06.txt", "UTF-8"));
	}
	
	@Test
	public void testStructuredBackup() throws FileNotFoundException {
		params.setCopyOption("backup");
		params.setMove(false);
		addFiles(pdriver, root + "/to_empty", false);
		resetTargetFiles(false);
		pdriver.processBatch();
		
		FileCompare fc = new FileCompare();
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/test00.txt", root + "/to_empty/test00.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir01/subdir11/test01.txt", root + "/to_empty/subdir01/subdir11/test01.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir01/test02.txt", root + "/to_empty/subdir01/test02.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir02/test03.txt", root + "/to_empty/subdir02/test03.txt", "UTF-8"));
		assertTrue(fc.compareFilesPerLines(root + "/gold_complex/subdir02/test04.txt", root + "/to_empty/subdir02/test04.txt", "UTF-8"));
		//FIXME missing file: assertFalse(fc.compareFilesPerLines(root + "/gold_complex/subdir01/subdir11/test05.txt", root + "/to_empty/subdir01/subdir11/test05.txt", "UTF-8"));
		//FIXME missing file: assertFalse(fc.compareFilesPerLines(root + "/gold_complex/test06.txt", root + "/to_empty/test06.txt", "UTF-8"));
	}
	
	@Test
	public void testStructuredSkip() throws FileNotFoundException {
		params.setCopyOption("skip");
		params.setMove(false);
		addFiles(pdriver, root + "/to_empty", false);
		resetTargetFiles(false);
		pdriver.processBatch();
		
		FileCompare fc = new FileCompare();
		assertFalse(fc.compareFilesPerLines(root + "/gold_complex/test00.txt", root + "/to_empty/test00.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_complex/subdir01/subdir11/test01.txt", root + "/to_empty/subdir01/subdir11/test01.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_complex/subdir01/test02.txt", root + "/to_empty/subdir01/test02.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_complex/subdir02/test03.txt", root + "/to_empty/subdir02/test03.txt", "UTF-8"));
		assertFalse(fc.compareFilesPerLines(root + "/gold_complex/subdir02/test04.txt", root + "/to_empty/subdir02/test04.txt", "UTF-8"));
		//FIXME missing file: assertFalse(fc.compareFilesPerLines(root + "/gold_complex/subdir01/subdir11/test05.txt", root + "/to_empty/subdir01/subdir11/test05.txt", "UTF-8"));
		//FIXME missing file: assertFalse(fc.compareFilesPerLines(root + "/gold_complex/test06.txt", root + "/to_empty/test06.txt", "UTF-8"));
	}

	
	//
	// Helper methods
	//
	private void addFiles(IPipelineDriver pdriver, String output, boolean isFlat) {
		if (isFlat) {
			pdriver.addBatchItem(new BatchItemContext(new RawDocument(new File(root + "/from_flat/test00.txt").toURI(),
					"UTF-8", LocaleId.ENGLISH), new File(output + "/test00.txt").toURI(), null));
			pdriver.addBatchItem(new BatchItemContext(new RawDocument(new File(root + "/from_flat/test01.txt").toURI(),
					"UTF-8", LocaleId.ENGLISH), new File(output + "/test01.txt").toURI(), null));
			pdriver.addBatchItem(new BatchItemContext(new RawDocument(new File(root + "/from_flat/test02.txt").toURI(),
					"UTF-8", LocaleId.ENGLISH), new File(output + "/test02.txt").toURI(), null));
			pdriver.addBatchItem(new BatchItemContext(new RawDocument(new File(root + "/from_flat/test03.txt").toURI(),
					"UTF-8", LocaleId.ENGLISH), new File(output + "/test03.txt").toURI(), null));
			pdriver.addBatchItem(new BatchItemContext(new RawDocument(new File(root + "/from_flat/test04.txt").toURI(),
					"UTF-8", LocaleId.ENGLISH), new File(output + "/test04.txt").toURI(), null));
		} else {
			pdriver.addBatchItem(new BatchItemContext(new RawDocument(new File(root + "/from_complex/test00.txt").toURI(),
					"UTF-8", LocaleId.ENGLISH), new File(output + "/test00.txt").toURI(), null));
			pdriver.addBatchItem(new BatchItemContext(new RawDocument(new File(root + "/from_complex/subdir01/subdir11/test01.txt").toURI(),
					"UTF-8", LocaleId.ENGLISH), new File(output + "/subdir01/subdir11/test01.txt").toURI(), null));
			pdriver.addBatchItem(new BatchItemContext(new RawDocument(new File(root + "/from_complex/subdir01/test02.txt").toURI(),
					"UTF-8", LocaleId.ENGLISH), new File(output + "/subdir01/test02.txt").toURI(), null));
			pdriver.addBatchItem(new BatchItemContext(new RawDocument(new File(root + "/from_complex/subdir02/test03.txt").toURI(),
					"UTF-8", LocaleId.ENGLISH), new File(output + "/subdir02/test03.txt").toURI(), null));
			pdriver.addBatchItem(new BatchItemContext(new RawDocument(new File(root + "/from_complex/subdir02/test04.txt").toURI(),
					"UTF-8", LocaleId.ENGLISH), new File(output + "/subdir02/test04.txt").toURI(), null));
		}
	}
	
	private void resetFiles() {
		Util.deleteDirectory(root + "from_flat/", true);
		StreamUtil.copy(root + "/gold_flat/test00.txt", root + "/from_flat/test00.txt", false);
		StreamUtil.copy(root + "/gold_flat/test01.txt", root + "/from_flat/test01.txt", false);
		StreamUtil.copy(root + "/gold_flat/test02.txt", root + "/from_flat/test02.txt", false);
		StreamUtil.copy(root + "/gold_flat/test03.txt", root + "/from_flat/test03.txt", false);
		StreamUtil.copy(root + "/gold_flat/test04.txt", root + "/from_flat/test04.txt", false);
		
		Util.deleteDirectory(root + "from_complex/", true);
		StreamUtil.copy(root + "/gold_complex/test00.txt", root + "/from_complex/test00.txt" , false);
		StreamUtil.copy(root + "/gold_complex/subdir01/subdir11/test01.txt", root + "/from_complex/subdir01/subdir11/test01.txt" , false);
		StreamUtil.copy(root + "/gold_complex/subdir01/test02.txt", root + "/from_complex/subdir01/test02.txt" , false);
		StreamUtil.copy(root + "/gold_complex/subdir02/test03.txt", root + "/from_complex/subdir02/test03.txt" , false);
		StreamUtil.copy(root + "/gold_complex/subdir02/test04.txt", root + "/from_complex/subdir02/test04.txt" , false);
	}
	
	private void resetTargetFiles(boolean isFlat) {
		if (isFlat) {
			Util.deleteDirectory(root + "to_empty/", true);
			StreamUtil.copy(root + "/gold_for_options_flat/test00.txt", root + "/to_empty/test00.txt", false);
			StreamUtil.copy(root + "/gold_for_options_flat/test01.txt", root + "/to_empty/test01.txt", false);
			StreamUtil.copy(root + "/gold_for_options_flat/test02.txt", root + "/to_empty/test02.txt", false);
			StreamUtil.copy(root + "/gold_for_options_flat/test03.txt", root + "/to_empty/test03.txt", false);
			StreamUtil.copy(root + "/gold_for_options_flat/test04.txt", root + "/to_empty/test04.txt", false);
			StreamUtil.copy(root + "/gold_for_options_flat/test05.txt", root + "/to_empty/test05.txt", false);
			StreamUtil.copy(root + "/gold_for_options_flat/test06.txt", root + "/to_empty/test06.txt", false);
		} else {
			Util.deleteDirectory(root + "to_empty/", true);
			StreamUtil.copy(root + "/gold_for_options_complex/test00.txt", root + "/to_empty/test00.txt", false);
			StreamUtil.copy(root + "/gold_for_options_complex/subdir01/subdir11/test01.txt", root + "/to_empty/subdir01/subdir11/test01.txt", false);
			StreamUtil.copy(root + "/gold_for_options_complex/subdir01/test02.txt", root + "/to_empty/subdir01/test02.txt", false);
			StreamUtil.copy(root + "/gold_for_options_complex/subdir02/test03.txt", root + "/to_empty/subdir02/test03.txt", false);
			StreamUtil.copy(root + "/gold_for_options_complex/subdir02/test04.txt", root + "/to_empty/subdir02/test04.txt", false);
			StreamUtil.copy(root + "/gold_for_options_complex/subdir02/test04.txt", root + "/to_empty/subdir02/test04.txt", false);
			StreamUtil.copy(root + "/gold_for_options_complex/subdir01/subdir11/test05.txt", root + "/to_empty/subdir01/subdir11/test05.txt", false);
			StreamUtil.copy(root + "/gold_for_options_complex/test06.txt", root + "/to_empty/test06.txt", false);
		}
	}
}
