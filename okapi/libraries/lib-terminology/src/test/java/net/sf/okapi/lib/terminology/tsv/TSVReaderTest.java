/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.terminology.tsv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.lib.terminology.ConceptEntry;
import net.sf.okapi.lib.terminology.IGlossaryReader;

@RunWith(JUnit4.class)
public class TSVReaderTest {

	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locFR = LocaleId.FRENCH;
	private FileLocation location;
	
	public TSVReaderTest () throws URISyntaxException {
		location = FileLocation.fromClass(TSVReaderTest.class);
	}
	
	@Test
	public void testSimpleTSV () {
		String snippet = "source 1\ttarget 1\n"
			+ "source 2\ttarget 2\tmore\n"
			+ "line without tab\n"
			+ "   \n"
			+ "source 3\ttarget 3\n";

		List<ConceptEntry> list = getConcepts(snippet, null, locEN, locFR);
		assertNotNull(list);
		assertEquals(3, list.size());
		
		ConceptEntry cent = list.get(0);
		assertEquals("source 1", cent.getEntries(locEN).getTerm(0).getText());
		assertEquals("target 1", cent.getEntries(locFR).getTerm(0).getText());
		cent = list.get(2);
		assertEquals("source 3", cent.getEntries(locEN).getTerm(0).getText());
		assertEquals("target 3", cent.getEntries(locFR).getTerm(0).getText());
	}


	@Test
	public void testFromFiles () {
		File file = location.in("/test01.tsv").asFile();
		List<ConceptEntry> list = getConcepts(null, file, locEN, locFR);
		assertEquals(3, list.size());
		assertEquals("target 3", list.get(2).getEntries(locFR).getTerm(0).getText());
	}

	@Test
	public void testEncoding () {
		File file = location.in("/test02_utf16be.tsv").asFile();
		List<ConceptEntry> list = getConcepts(null, file, locEN, locFR);
		assertEquals(2, list.size());
		assertEquals("\u00e9\u00df\u00d1\uffe6 target 2", list.get(1).getEntries(locFR).getTerm(0).getText());
	}

	// Use either snippet or file
	List<ConceptEntry> getConcepts (String snippet,
		File file,
		LocaleId srcLoc,
		LocaleId trgLoc)
	{
		try {
			ArrayList<ConceptEntry> list = new ArrayList<ConceptEntry>();
			
			IGlossaryReader tsv = new TSVReader(srcLoc, trgLoc);
			
			if ( file == null ) {
				InputStream is = new ByteArrayInputStream(snippet.getBytes("UTF-8"));
				tsv.open(is);
			}
			else {
				tsv.open(file);
			}
			while ( tsv.hasNext() ) {
				list.add(tsv.next());
			}
			tsv.close();
			return list;
		}
		catch ( Throwable e ) {
			e.printStackTrace();
			throw new OkapiException(e.getMessage());
		}
	}
	
}
