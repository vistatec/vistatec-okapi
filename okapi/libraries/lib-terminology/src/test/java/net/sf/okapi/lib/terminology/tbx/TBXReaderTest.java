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

package net.sf.okapi.lib.terminology.tbx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import net.sf.okapi.lib.terminology.LangEntry;
import net.sf.okapi.lib.terminology.TermEntry;

@RunWith(JUnit4.class)
public class TBXReaderTest {

	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locFR = LocaleId.FRENCH;
	private LocaleId locHU = LocaleId.fromString("hu");
	private LocaleId locTR = LocaleId.fromString("tr");
	private FileLocation location;
	
	public TBXReaderTest () throws URISyntaxException {
        location = FileLocation.fromClass(TBXReaderTest.class);
	}
	
	@Test
	public void testSimpleTBX () {
		String snippet = "<?xml version='1.0'?>"
			+ "<!DOCTYPE martif SYSTEM \"TBXcoreStructV02.dtd\">"
			+ "<martif type=\"TBX\" xml:lang=\"en\"><martifHeader><fileDesc><sourceDesc>"
			+ "<p>From an Oracle corporation termbase</p>"
			+ "</sourceDesc></fileDesc>"
			+ "<encodingDesc><p type=\"XCSURI\">http://www.lisa.org/fileadmin/standards/tbx/TBXXCSV02.XCS</p></encodingDesc>"
			+ "</martifHeader><text><body>"
			
			+ "<termEntry id=\"eid1\">"
			+ "<descrip type=\"subjectField\">manufacturing</descrip>"
			+ "<descrip type=\"definition\">def text</descrip>"
			+ "<langSet xml:lang=\"en\">"
			+ "<tig>"
			+ "<term id=\"eid1-en1\">en text</term>"
			+ "<termNote type=\"partOfSpeech\">noun-en</termNote>"
			+ "</tig></langSet>"
			+ "<langSet xml:lang=\"hu\">"
			+ "<tig>"
			+ "<term id=\"eid1-hu1\">hu <hi>special</hi> text</term>"
			+ "<termNote type=\"partOfSpeech\">noun-hu</termNote>"
			+ "</tig></langSet>"
			+ "</termEntry>"
			
			+ "<termEntry id=\"ent2\">"
			+ "<langSet xml:lang=\"en\">"
			+ "<ntig><termGrp>"
			+ "<term id=\"ent2-1\">en text2</term>"
			+ "</termGrp></ntig></langSet>"
			+ "<langSet xml:lang=\"fr\">"
			+ "<tig>"
			+ "<term id=\"ent2-2\">fr text2</term>"
			+ "</tig></langSet>"
			+ "</termEntry>"

			+ "</body></text></martif>";

		List<ConceptEntry> list = getConcepts(snippet, null);
		assertNotNull(list);
		assertEquals(2, list.size());
		
		ConceptEntry gent = list.get(0);
		assertEquals("eid1", gent.getId());
		assertTrue(gent.hasLocale(locEN));
		LangEntry lent = gent.getEntries(locEN);
		TermEntry tent = lent.getTerm(0);
		assertEquals("eid1-en1", tent.getId());
		assertEquals("en text", tent.getText());
		
		assertTrue(gent.hasLocale(locHU));
		lent = gent.getEntries(locHU);
		tent = lent.getTerm(0);
		assertEquals("eid1-hu1", tent.getId());
		assertEquals("hu special text", tent.getText());

		gent = list.get(1);
		assertTrue(gent.hasLocale(locFR));
		lent = gent.getEntries(locFR);
		tent = lent.getTerm(0);
		assertEquals("ent2-2", tent.getId());
		assertEquals("fr text2", tent.getText());
	}

	@Test
	public void testNoTerms () {
		String snippet = "<?xml version='1.0'?>"
			+ "<!DOCTYPE martif SYSTEM \"TBXcoreStructV02.dtd\">"
			+ "<martif type=\"TBX\" xml:lang=\"en\"><martifHeader><fileDesc><sourceDesc>"
			+ "<p>From an Oracle corporation termbase</p>"
			+ "</sourceDesc></fileDesc>"
			+ "<encodingDesc><p type=\"XCSURI\">http://www.lisa.org/fileadmin/standards/tbx/TBXXCSV02.XCS</p></encodingDesc>"
			+ "</martifHeader><text><body>"
			+ "</body></text></martif>";

		List<ConceptEntry> list = getConcepts(snippet, null);
		assertNotNull(list);
		assertEquals(0, list.size());
	}

	@Test
	public void testEncoding () {
		File file = location.in("/test02_win1254.tbx").asFile();
		List<ConceptEntry> list = getConcepts(null, file);
		assertEquals(1, list.size());
		ConceptEntry cent = list.get(0);
		assertEquals("id1", cent.getId());
		assertEquals("term with: \u00e9\u00e1 and \u0130\u0131", cent.getEntries(locEN).getTerm(0).getText());
		assertEquals("tr term with: \u00e9\u00e1 and \u0130\u0131", cent.getEntries(locTR).getTerm(0).getText());
	}
	
	@Test
	public void testFromFiles () {
		File file = location.in("/test01.tbx").asFile();
		List<ConceptEntry> list = getConcepts(null, file);
		assertEquals(1, list.size());
		assertEquals("eid-Oracle-67", list.get(0).getId());

		file = location.in("/sdl_tbx.tbx").asFile();
		list = getConcepts(null, file);
		assertEquals(223, list.size());
		assertEquals("c228", list.get(list.size()-1).getId());

		file = location.in("/ibm_tbx.tbx").asFile();
		list = getConcepts(null, file);
		assertEquals(5, list.size());
		assertEquals("c5", list.get(list.size()-1).getId());
		
		file = location.in("/maryland.tbx").asFile();
		list = getConcepts(null, file);
		assertEquals(1, list.size());
		assertEquals("eid-VocCod-211.01", list.get(list.size()-1).getId());
		
		file = location.in("/medtronic_TBX.tbx").asFile();
		list = getConcepts(null, file);
		assertEquals(3, list.size());
		assertEquals("c7333", list.get(list.size()-1).getId());
		
		file = location.in("/oracle_TBX.tbx").asFile();
		list = getConcepts(null, file);
		assertEquals(2, list.size());
		assertEquals("c2", list.get(list.size()-1).getId());
	}

	// Comment out for SVN
//	@Test
//	public void testBigFile () {
//		File file = new File(root+"MicrosoftTermCollection_FR.tbx");
//		List<GlossaryEntry> list = getEntries(null, file);
//		assertNotNull(list);
//		GlossaryEntry gent = list.get(list.size()-1);
//		assertEquals(17133, list.size());
//		assertEquals("27766_1436100", gent.getId());
//	}

	// Use either snippet or file
	List<ConceptEntry> getConcepts (String snippet, File file) {
		try {
			ArrayList<ConceptEntry> list = new ArrayList<ConceptEntry>();
			
			//IGlossaryReader tbx = new TBXJaxbReader();
			IGlossaryReader tbx = new TBXReader();
			
			if ( file == null ) {
				InputStream is = new ByteArrayInputStream(snippet.getBytes("UTF-8"));
				tbx.open(is);
			}
			else {
				tbx.open(file);
			}
			while ( tbx.hasNext() ) {
				list.add(tbx.next());
			}
			tbx.close();
			return list;
		}
		catch ( Throwable e ) {
			e.printStackTrace();
			throw new OkapiException(e.getMessage());
		}
	}
	
}
