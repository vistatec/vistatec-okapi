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

package org.w3c.its;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;

import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ITSTest_TestSuite {

	public static final String XML = "xml";
	public static final String HTML = "html";
	
	private String root = TestUtil.getParentDir(this.getClass(), "/input.xml") + "/its2.0/inputdata";
	private FileCompare fc = new FileCompare();
	
	@Test
	public void process () throws URISyntaxException {
		processBatches(root+"/translate", Main.DC_TRANSLATE);
		processBatches(root+"/localizationnote", Main.DC_LOCALIZATIONNOTE);
		processBatches(root+"/terminology", Main.DC_TERMINOLOGY);
		processBatches(root+"/directionality", Main.DC_DIRECTIONALITY);
		processBatches(root+"/languageinformation", Main.DC_LANGUAGEINFORMATION);
		processBatches(root+"/elementswithintext", Main.DC_WITHINTEXT);
		processBatches(root+"/domain", Main.DC_DOMAIN);
		processBatches(root+"/textanalysis", Main.DC_TEXTANALYSIS);
		processBatches(root+"/localefilter", Main.DC_LOCALEFILTER);
		processBatches(root+"/externalresource", Main.DC_EXTERNALRESOURCE);
		processBatches(root+"/targetpointer", Main.DC_TARGETPOINTER);
		processBatches(root+"/idvalue", Main.DC_IDVALUE);
		processBatches(root+"/preservespace", Main.DC_PRESERVESPACE);
		processBatches(root+"/locqualityissue", Main.DC_LOCQUALITYISSUE);
		processBatches(root+"/locqualityrating", Main.DC_LOCQUALITYRATING);
		processBatches(root+"/storagesize", Main.DC_STORAGESIZE);
		processBatches(root+"/mtconfidence", Main.DC_MTCONFIDENCE);
		processBatches(root+"/allowedcharacters", Main.DC_ALLOWEDCHARACTERS);
		processBatches(root+"/provenance", Main.DC_PROVENANCE);
	}
	
	/**
	 * Shortcut to process both xml and html formats
	 * @param base
	 * @param category
	 * @throws URISyntaxException
	 */
	public void processBatches (String base, String category) throws URISyntaxException {
		processBatch(base+"/html", category);
		processBatch(base+"/xml", category);
	}
	
	/**
	 * Process all files in specified folder
	 * @param base
	 * @param category
	 * @throws URISyntaxException
	 */
	public void processBatch (String base, String category) throws URISyntaxException {
		removeOutput(base);
		File f = new File(base);
		if ( ! f.exists() ) return;
		String[] files = Util.getFilteredFiles(base, "");
		for ( String file : files ) {
			if ( file.contains("rules") || file.contains("standoff") ) continue;
			process(base + "/" + file, category);
		}
	}

	private void removeOutput (String baseDir) {
		String outDir = baseDir.replace("/inputdata/", "/output/");
		Util.deleteDirectory(outDir, true);
	}

	private void process (String baseName,
		String dataCategory)
	{
		String input = baseName;
		String output = input.replace("/inputdata/", "/output/");
		int n = output.lastIndexOf('.');
		if ( n > -1 ) output = output.substring(0, n);
		output += "output";
		output += ".txt";
		
		Main.main(new String[]{input, output, "-dc", dataCategory});
		assertTrue(new File(output).exists());
		
		String gold = output.replace("/output/", "/expected/");
		assertTrue(fc.compareFilesPerLines(output, gold, "UTF-8"));
// Just compare for now, until the test cases are stable
//		fc.compareFilesPerLines(output, gold, "UTF-8");
	}
	
}
