/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package net.sf.okapi.filters.mif.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.mif.MIFFilter;
import net.sf.okapi.filters.mif.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MIFFilterTest {

	private final static String STARTMIF = "<MIFFile 9.00><TextFlow <Para ";
	private final static String ENDMIF = ">> # End of MIFFile";
	
	private LocaleId locEN = LocaleId.fromString("en");

	private String root;
	private MIFFilter filter;
	private GenericContent fmt = new GenericContent();
	
	@Before
	public void setUp() throws URISyntaxException {
		filter = new MIFFilter();
		URL url = MIFFilterTest.class.getResource("/Test01.mif");
		root = Util.getDirectoryName(url.toURI().getPath()) + File.separator;
	}

	@Test
	public void testDefaultInfo () {
		//Not using parameters yet: assertNotNull(filter.getParameters());
		assertNotNull(filter.getName());
		List<FilterConfiguration> list = filter.getConfigurations();
		assertNotNull(list);
		assertTrue(list.size()>0);
	}

	@Test
	public void testStartDocument () {
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(root+"Test01.mif", null),
			null, locEN, locEN));
	}

	@Test
	public void testSimpleText () {
		List<Event> list = getEventsFromFile("Test01.mif", null);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 194);
		assertNotNull(tu);
		assertEquals("Line 1\nLine 2", fmt.setContent(tu.getSource().getFirstContent()).toString());
		
		tu = FilterTestDriver.getTextUnit(list, 195);
		assertNotNull(tu);
		assertEquals("\u00e0=agrave", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testExtractIndexMarkers () {
		Parameters params = getCommonParameters();
		
		// Extract index markers
		List<Event> list = getEventsFromFile("TestMarkers.mif", params);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("Text of marker", fmt.setContent(tu.getSource().getFirstContent()).toString());
		assertEquals("x-index", tu.getType());
		
		// Do not extract index markers
		params.setExtractIndexMarkers(false);
		list = getEventsFromFile("TestMarkers.mif", params);
		tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("<1/>Text with index about some subject.", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testExtractLinks () {
		Parameters params = getCommonParameters();

		// Do not extract links
		List<Event> list = getEventsFromFile("TestMarkers.mif", params);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 5);
		assertNotNull(tu);
		assertEquals("text with a link to <1/>http://okapi.opentag.org/", fmt.setContent(tu.getSource().getFirstContent()).toString());
		
		// Do extract links
		params.setExtractLinks(true);
		list = getEventsFromFile("TestMarkers.mif", params);
		tu = FilterTestDriver.getTextUnit(list, 5);
		assertNotNull(tu);
		assertEquals("http://okapi.opentag.com/", fmt.setContent(tu.getSource().getFirstContent()).toString());
		assertEquals("link", tu.getType());
	}

	@Test
	public void testBodyOnlyNoVariables () {
		Parameters params = getCommonParameters();

		List<Event> list = getEventsFromFile("Test01.mif", params);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("Line 1\nLine 2", fmt.setContent(tu.getSource().getFirstContent()).toString());
		
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertNotNull(tu);
		assertEquals("\u00e0=agrave", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testParagraphLinesProcessing() {
		Parameters params = getCommonParameters();

		List<Event> list = getEventsFromFile("TestParaLines.mif", params);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);

		assertNotNull(tu);
		assertEquals("The 1st para line. The 2nd.", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testSimpleEntry () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <String `text \\\\ and &'>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("text \\ and &", fmt.setContent(tu.getSource().getFirstContent()).toString());
		
		String expected = STARTMIF
			+ "<Unique 12345><ParaLine <String `text \\\\ and &'>> # end of ParaLine\n"
			+ ENDMIF;
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testNoTextEntry () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <TextRectID 9> >"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu==null);
		String expected = STARTMIF
			+ "<Unique 12345><ParaLine <TextRectID 9> > # end of ParaLine\n"
			+ ENDMIF;
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@Test
	public void testTwoPartsEntry () {
		String snippet = STARTMIF
			+ "<Unique 12345>#EOU\n<ParaLine \n <String `Part 1'>#EOS\n>#EOPL\n<ParaLine \n <String ` and part 2'>#EOS\n>#EOPL\n"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("Part 1 and part 2", fmt.setContent(tu.getSource().getFirstContent()).toString());
		
		String expected = STARTMIF
			+ "<Unique 12345>#EOU\n<ParaLine \n <String `Part 1 and part 2'>#EOS\n#EOPL\n> # end of ParaLine\n"
			+ ENDMIF;
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@Test
	public void testEmptyString () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <String `Text 1'><AFrame 1><Char ThinSpace><String `'><AFrame 2><String ` end'>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("Text 1<1/>\u2009<2/> end", fmt.setContent(tu.getSource().getFirstContent()).toString());
		Code code = tu.getSource().getFirstContent().getCode(0);
		assertEquals("'><AFrame 1><String `", code.getData());
		code = tu.getSource().getFirstContent().getCode(1);
		assertEquals("'><AFrame 2><String `", code.getData());
	}
	
	@Test
	public void testEmptyStringInFront () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <String `'><Font 1><String `Text'>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("Text", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}
	
	@Test
	public void testTrimFontInFront () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <Font 1><String `Text'>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("Text", fmt.setContent(tu.getSource().getFirstContent()).toString());
		
		String expected = STARTMIF
			+ "<Unique 12345><ParaLine <String `'><Font 1><String `Text'>> # end of ParaLine\n"
			+ ENDMIF;
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testTabs () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <String ` '><Var 1><Char Tab><Char Tab>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu==null); // No text to extract

		String expected = STARTMIF
			+ "<Unique 12345><ParaLine <String ` '><Var 1><String `'><Char Tab><String `'><Char Tab><String `'>> # end of ParaLine\n"
			+ ENDMIF;
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@Test
	public void testTabsAndCodes () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <Char Tab><Font 1><Var 1><Font 2><Char Tab>><ParaLine <Font 3>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu==null);
		DocumentPart dp = FilterTestDriver.getDocumentPart(getEvents(snippet), 2);
		assertEquals("<TextFlow <Para <Unique 12345><ParaLine <String `'><Char Tab><String `'><Font 1><Var 1><Font 2><String `'><Char Tab><String `'><Font 3>> # end of ParaLine\n>", dp.getSkeleton().toString());
	}

	@Test
	public void testDummyBeforeChar () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <String `Text 1'><Dummy <InDummy 2>><Char ThinSpace><String `Text 2'>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("Text 1<1/>\u2009Text 2", fmt.setContent(tu.getSource().getFirstContent()).toString());
		Code code = tu.getSource().getFirstContent().getCode(0);
		assertEquals("'><Dummy <InDummy 2>><String `", code.getData());
	}

	@Test
	public void testCodeAtTheFront () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <Font 1><String `text 1'><Font 2><String `text 2'>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("<1/>text 1<2/>text 2", fmt.setContent(tu.getSource().getFirstContent()).toString());
		assertEquals("'><Font 1><String `", tu.getSource().getFirstContent().getCode(0).getData());
		assertEquals("'><Font 2><String `", tu.getSource().getFirstContent().getCode(1).getData());
	}

	@Test
	public void testCharOnly () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <Dummy 1><Char Tab><Dummy 2>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu==null);
	}

	@Test
	public void testEndsInCharAndCode () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <String `aaa'><Dummy 1><Char Tab><Dummy 2>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("aaa<1/>\t", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testDummyCharString () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <AFrame 1><Char Tab><String `aaa'>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("<1/>\taaa", fmt.setContent(tu.getSource().getFirstContent()).toString());
		assertEquals("'><AFrame 1><String `", tu.getSource().getFirstContent().getCodes().get(0).getData());
		assertEquals("<TextFlow <Para <Unique 12345><ParaLine <String `[#$$self$]'>> # end of ParaLine\n>", tu.getSkeleton().toString());
	}

	@Test
	public void testEmptyFTag () {
		String snippet = STARTMIF
			+ "<Unique 12345><ParaLine <AFrame 1><String `Text 1'><Char ThinSpace><AFrame 2><String `text 2'><AFrame 3>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("<1/>Text 1\u2009<2/>text 2", fmt.setContent(tu.getSource().getFirstContent()).toString());
		assertEquals("'><AFrame 1><String `", tu.getSource().getFirstContent().getCode(0).getData());
		assertEquals("'><AFrame 2><String `", tu.getSource().getFirstContent().getCode(1).getData());
	}

	@Test
	public void testSoftHyphen () {
		String snippet = STARTMIF
			+ "<Unique 123><ParaLine <TextRectID 20><String `How'><Char SoftHyphen>>"
			+ "<ParaLine <String `ever.'>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("However.", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testNormalFont () {
		String snippet = STARTMIF
			+ "<Unique 123><ParaLine <Font <FTag `'><FLanguage NoLanguage><FLocked No>> # end of Font\n<String `Text'>>"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("Text", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}
	
	@Test
	public void testEmptyParaLine () {
		String snippet = STARTMIF
			+ "<Unique 123>\n <ParaLine > # end of ParaLine\n"
			+ ENDMIF;
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu==null);
		DocumentPart dp = FilterTestDriver.getDocumentPart(getEvents(snippet), 2);
		assertEquals("<TextFlow <Para <Unique 123>\n <ParaLine > # end of ParaLine\n>", dp.getSkeleton().toString());
	}

	@Test
	public void testSlashCodes () {
		String snippet = "<MIFFile 10.0> # Generated by FrameMaker 10.0.0.388\n"
			+ "<VariableFormats\n" 
			+ "<VariableFormat\n" 
			+ "<VariableName `Running H/F 4'>\n"
			+ "<VariableDef `<zBold\\><$paranum[LBN.LabNumber]\\><Default Z Font\\>\\x14 \\x05 \\x0b <$paratext[LBT.LabTitle]\\>'>\n"
			+ "> # end of VariableFormat\n"
			+ "> # end of VariableFormats\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("<zBold><1/><Default Z Font>\u2003<3/><4/><2/>",
			fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testSlashCodesOutput () {
		String snippet = "<MIFFile 9.00> # Generated by FrameMaker 10.0.0.388\n"
			+ "<VariableFormats\n" 
			+ "<VariableFormat\n" 
			+ "<VariableName `Running H/F 4'>\n"
			+ "<VariableDef `<zBold\\><$paranum[LBN.LabNumber]\\><Default Z Font\\>\\x14 \\x05 \\x0b <$paratext[LBT.LabTitle]\\>'>\n"
			+ "> # end of VariableFormat\n"
			+ "> # end of VariableFormats\n";
		String expected = "<MIFFile 9.00> # Generated by FrameMaker 10.0.0.388\n"
			+ "<VariableFormats\n" 
			+ "<VariableFormat\n" 
			+ "<VariableName `Running H/F 4'>\n"
			+ "<VariableDef `<zBold\\><$paranum[LBN.LabNumber]\\><Default Z Font\\>\\x14 \\x05 \\x0b <$paratext[LBT.LabTitle]\\>'>\n"
			+ "> # end of VariableFormat\n"
			+ "> # end of VariableFormats\n";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@Test
	public void testOutput () {
		rewriteFile("TestMarkers.mif");
		rewriteFile("Test03.mif");
		rewriteFile("Test01.mif");
		rewriteFile("Test02-v9.mif");

		rewriteFile("Test03_mif7.mif");
		rewriteFile("Test01-v7.mif");
	}

	@Test
	public void testOutputThenCompare () {
		rewriteThenCompareFile("Test01.mif");
		rewriteThenCompareFile("TestMarkers.mif");
		rewriteThenCompareFile("Test03.mif");
		// FIXME: fails
	    //rewriteThenCompareFile("Test03_mif7.mif");
		rewriteThenCompareFile("Test04.mif");
		rewriteThenCompareFile("Test02-v9.mif");
		rewriteThenCompareFile("Test01.mif");
		rewriteThenCompareFile("JATest.mif");
		rewriteThenCompareFile("TestFootnote.mif");
		rewriteThenCompareFile("Test01-v7.mif");
	}
 	
	@Test
	public void testV10IsUsingV9Encoding () {
		List<Event> eventsV9 = getEventsFromFile("TestEncoding-v9.mif", null);
		List<Event> eventsV10 = getEventsFromFile("TestEncoding-v10.mif", null);

		assertTrue("Content of both files should be the same.", eventsV9.size() == eventsV10.size());
		
		for (int i = 0; i < eventsV9.size(); i++) {
			Event eventV9 = eventsV9.get(i);
			Event eventV10 = eventsV10.get(i);
			if ( eventV9.getEventType() != eventV10.getEventType() ) {
				// Trigger assert and allow easy debug
				assertTrue("Content of both files should be the same.", false);
			}
			if ( eventV9.getEventType() == EventType.TEXT_UNIT ) {
				ITextUnit tu1 = eventV9.getTextUnit();
				ITextUnit tu2 = eventV10.getTextUnit();
				assertEquals(tu1.getSource().getFirstContent().getText(),
					tu2.getSource().getFirstContent().getText());
			}
		}
	}

    @Test
    public void testFM2015() {
        String snippet = "<MIFFile 2015><TextFlow <Para <Unique 12345><ParaLine <String `Part 1'>>" + ENDMIF;
        ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
        assertNotNull(tu);
    }

	private void rewriteFile (String fileName) {
		filter.open(new RawDocument(Util.toURI(root+fileName), null, locEN));
		IFilterWriter writer = filter.createFilterWriter();
		writer.setOptions(locEN, null);
		writer.setOutput(root+fileName+".rewrite.mif");
		while ( filter.hasNext() ) {
			writer.handleEvent(filter.next());
		}
		writer.close();
		filter.close();
	}

	private void rewriteThenCompareFile (String fileName) {
		// Rewrite the file
		filter.open(new RawDocument(Util.toURI(root+fileName), null, locEN));
		IFilterWriter writer = filter.createFilterWriter();
		writer.setOptions(locEN, null);
		File outFile = new File(root+fileName+".rewrite.mif");
		outFile.delete();
		writer.setOutput(outFile.getAbsolutePath());
		// Store while rewriting
		ArrayList<Event> list = new ArrayList<Event>();
		while ( filter.hasNext() ) {
			Event event = filter.next();
			list.add(event);
			writer.handleEvent(event);
		}
		writer.close();
		filter.close();
		
		// Read from the rewritten file
		int i = 0;
		filter.open(new RawDocument(outFile.toURI(), null, locEN));
		while ( filter.hasNext() ) {
			Event event1 = list.get(i++);
			Event event2 = filter.next();
			if ( event1.getEventType() != event2.getEventType() ) {
				// Trigger assert and allow easy debug
				assertTrue(false);
			}
			if ( event1.getEventType() == EventType.TEXT_UNIT ) {
				ITextUnit tu1 = event1.getTextUnit();
				ITextUnit tu2 = event2.getTextUnit();
				assertEquals(tu1.getSource().getFirstContent().getText(),
					tu2.getSource().getFirstContent().getText());
			}
		}
		filter.close();
		
	}

	private Parameters getCommonParameters() {
		Parameters params = new Parameters();

		params.setExtractHiddenPages(false);
		params.setExtractMasterPages(false);
		params.setExtractReferencePages(false);
		params.setExtractVariables(false);

		return params;
	}

	private List<Event> getEventsFromFile (String filename,
		Parameters params)
	{
		return FilterTestDriver.getEvents(filter,  new RawDocument(Util.toURI(root+filename), null, locEN), params);
	}

	private ArrayList<Event> getEvents(String snippet) {
		return FilterTestDriver.getEvents(filter, snippet, locEN);
	}
}
