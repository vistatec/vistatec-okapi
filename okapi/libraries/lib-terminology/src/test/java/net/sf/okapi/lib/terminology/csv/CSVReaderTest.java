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

package net.sf.okapi.lib.terminology.csv;

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
public class CSVReaderTest {

	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locFR = LocaleId.FRENCH;
	private FileLocation location;
	
	public CSVReaderTest () throws URISyntaxException {
		location = FileLocation.fromClass(CSVReaderTest.class);
	}
	
	@Test
	public void testSimpleCSV () {
		String snippet = "source 1,target 1\n"
			+ ",target 2\n"
			+ "target1,\n"
			+ ",\n"
			+ "  source 2  ,  target 2  \n"
			+ "\"source 3\",\"target 3\"\n"
			+ "\" source 4 \",\" target 4 \"\n"
			+ "\"source 5\",\"target 5,target 6\"\n"
			+ "\"source 5\",\"target 5,\"\"target 6\"\",target 7\"\n"
			+ "line without delimiter\n"
			+ "   \n";

		List<ConceptEntry> list = getConcepts(snippet, null, locEN, locFR);
		assertNotNull(list);
		assertEquals(6, list.size());
		
		ConceptEntry cent = list.get(0);
		assertEquals("source 1", cent.getEntries(locEN).getTerm(0).getText());
		assertEquals("target 1", cent.getEntries(locFR).getTerm(0).getText());
		cent = list.get(1);
		assertEquals("source 2", cent.getEntries(locEN).getTerm(0).getText());
		assertEquals("target 2", cent.getEntries(locFR).getTerm(0).getText());
		cent = list.get(2);
		assertEquals("source 3", cent.getEntries(locEN).getTerm(0).getText());
		assertEquals("target 3", cent.getEntries(locFR).getTerm(0).getText());
		cent = list.get(3);
		assertEquals(" source 4 ", cent.getEntries(locEN).getTerm(0).getText());
		assertEquals(" target 4 ", cent.getEntries(locFR).getTerm(0).getText());
		cent = list.get(4);
		assertEquals("source 5", cent.getEntries(locEN).getTerm(0).getText());
		assertEquals("target 5,target 6", cent.getEntries(locFR).getTerm(0).getText());
		cent = list.get(5);
		assertEquals("source 5", cent.getEntries(locEN).getTerm(0).getText());
		assertEquals("target 5,\"target 6\",target 7", cent.getEntries(locFR).getTerm(0).getText());
	}

	 @Test(expected=RuntimeException.class)
  	 public void testInvalidCSV () {
		String snippet = "source 1,target 1\n"
			+ ",target 2\n"
			+ "target1,\n"
			+ ",\n"
			+ "  source 2  ,  target 2  \n"
			+ "\"source 3\",\"target 3\"\n"
			+ "\" source 4 \",\" target 4 \"\n"
			+ "\"source 5\",\"target 5,target 6\"\n"
			+ "\"source 5\",\"target 5,\"\"target 6\"\",target 7\"\n"
			+ "sou\"rce 6,target 6\n"
			+ "line without delimiter\n"
			+ "   \n";

		List<ConceptEntry> list = getConcepts(snippet, null, locEN, locFR);
		assertNotNull(list);
		assertEquals(6, list.size());
		
		ConceptEntry cent = list.get(6);
		assertEquals("sou\"rce 6", cent.getEntries(locEN).getTerm(0).getText());
		assertEquals("target 6", cent.getEntries(locFR).getTerm(0).getText());
	}
	

	@Test
	public void testFromFiles () {
		File file = location.in("/test01.csv").asFile();
		List<ConceptEntry> list = getConcepts(null, file, locEN, locFR);
		assertEquals(4, list.size());
		assertEquals("target 3", list.get(2).getEntries(locFR).getTerm(0).getText());
	}

	// Use either snippet or file
	List<ConceptEntry> getConcepts (String snippet,
		File file,
		LocaleId srcLoc,
		LocaleId trgLoc)
	{
		try {
			ArrayList<ConceptEntry> list = new ArrayList<ConceptEntry>();
			
			IGlossaryReader csv = new CSVReader(srcLoc, trgLoc);
			
			if ( file == null ) {
				InputStream is = new ByteArrayInputStream(snippet.getBytes("UTF-8"));
				csv.open(is);
			}
			else {
				csv.open(file);
			}
			while ( csv.hasNext() ) {
				list.add(csv.next());
			}
			csv.close();
			return list;
		}
		catch ( Throwable e ) {
			throw new OkapiException(e.getMessage());
		}
	}
	
}
