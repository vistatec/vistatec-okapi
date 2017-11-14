/*===========================================================================
  Copyright (C) 2008-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xliff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.RegexUtil;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.annotation.ITSLQIAnnotations;
import net.sf.okapi.common.annotation.ITSProvenanceAnnotations;
import net.sf.okapi.common.annotation.XLIFFNoteAnnotation;
import net.sf.okapi.common.annotation.XLIFFTool;
import net.sf.okapi.common.annotation.XLIFFToolAnnotation;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.filterwriter.ITSContent;
import net.sf.okapi.common.filterwriter.XLIFFContent;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.filters.xliff.its.ITSStandoffManager;
import net.sf.okapi.lib.segmentation.LanguageMap;
import net.sf.okapi.lib.segmentation.Rule;
import net.sf.okapi.lib.segmentation.SRXDocument;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class XLIFFFilterTest {

	private XLIFFFilter filter;
	private XLIFFFilter outSegFilter;
	private XLIFFFilter outNoSegFilter;
	private XLIFFFilter noInSegFilter;
	private XLIFFFilter altTransFilter;
	private GenericContent fmt;
	private XLIFFContent xcnt = new XLIFFContent();
	private FileLocation location;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	private LocaleId locES = LocaleId.fromString("es");
	private LocaleId locDE = LocaleId.fromString("de");

	@Before
	public void setUp () {
		filter = new XLIFFFilter();
		fmt = new GenericContent();
		location = FileLocation.fromClass(XLIFFFilterTest.class);
		outSegFilter = new XLIFFFilter();
		Parameters params = outSegFilter.getParameters();
		params.setOutputSegmentationType(Parameters.SEGMENTATIONTYPE_SEGMENTED);
		outNoSegFilter = new XLIFFFilter();
		params = outNoSegFilter.getParameters();
		params.setOutputSegmentationType(Parameters.SEGMENTATIONTYPE_NOTSEGMENTED);
		noInSegFilter = new XLIFFFilter();
		params = noInSegFilter.getParameters();
		params.setIgnoreInputSegmentation(true);
		altTransFilter = new XLIFFFilter();
		params = altTransFilter.getParameters();
		params.setAddAltTrans(true);
		params.setEditAltTrans(true);
	}

	InputDocument inputDocumentToTest(String fileName, String paramFile) {
		return new InputDocument(location.in("/" + fileName).toString(), paramFile);
	}

	@Test
	public void corruptCodeIdsAfterJoinAll() {
		// This xliff snippet is well formed but invalid (Unmatched <bpt>/<ept> in <source>.)
		// this was generated from the XLIFFWriter from a valid XLIFF file, but was
		// run through the CodeSimplifier producing many Textparts.
		// Left here as an example of silent failure by the XLIFFFilter, XLIFFWriter and the code id balancer
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source><mrk mid=\"0\" mtype=\"seg\">X<it id=\"163521559\" ctype=\"x-g\" pos=\"open\"></it> "
			+ "<it id=\"251566206\" ctype=\"x-g\" pos=\"open\"></it>X x.</mrk> <mrk mid=\"1\" mtype=\"seg\">Y y.</mrk>"
			+ "<it id=\"1\" ctype=\"x-g\" pos=\"close\"></it><mrk mid=\"2\" mtype=\"seg\"> <it id=\"251566207\" ctype=\"x-g\" pos=\"open\">"
			+ "</it>X x.</mrk> <mrk mid=\"3\" mtype=\"seg\">Y <bpt id=\"251566199\" ctype=\"x-g\"></bpt>X<ept id=\"251566199\"></ept>"
			+ " Y <bpt id=\"251566201\" ctype=\"x-g\"></bpt>X<ept id=\"251566201\"></ept> Y <bpt id=\"251566203\" ctype=\"x-g\">"
			+ "</bpt>X<ept id=\"251566203\"></ept> Y <bpt id=\"251566205\" ctype=\"x-g\"></bpt>O<ept id=\"251566205\"></ept>.</mrk>"
			+ "<ept id=\"251566207\"></ept><ept id=\"163521559\"></ept></source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
				
		// get last code in last part
		List<TextPart> parts = tu.getSource().getParts();
		int s = parts.size();
		Code c = parts.get(s-1).text.getCode(1);
		
		// before joinAll
		assertEquals(163521559, c.getId());
		
		tu.getSource().joinAll();
		
		// get last code
		parts = tu.getSource().getParts();		
		c = parts.get(0).text.getCode(13);
		
		// after joinAll last code id is wrong
		// because the xliff is invalid, but 
		// there is no warning or error
		assertFalse("163521559".equals(c.getId()));
	}

	@Test
	@Ignore("Is this test still valid?")
	public void disabled_testMisOrderedCodes () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source><g id=\"1\"><g id=\"2\"></g><bx id=\"3\"/></g><ex id=\"3\"/></source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("[<1><2></2><b3/></1><e3/>]", fmt.printSegmentedContent(tu.getSource(), true));
	}

	@Test
	public void testSegmentedTarget () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1. t2</source>"
			+ "<target xml:lang=\"fr\"><mrk mid=\"i1\" mtype=\"seg\">t1.</mrk>\n<mrk mid=\"i2\" mtype=\"seg\">t2</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getTarget(locFR);
		ISegments segments = cont.getSegments();
		assertEquals("[t1.] [t2]", fmt.printSegmentedContent(cont, true));
		assertEquals(2, segments.count());
		assertEquals("t1.", segments.get(0).text.toText());
		assertEquals("i2", segments.get(1).id);
		assertEquals("t2", segments.get(1).text.toText());
		assertEquals("i2", segments.get(1).id);
	}

	@Test
	public void testSegmentedContent () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>s1. s2</source>"
			+ "<seg-source><mrk mid=\"i1\" mtype=\"seg\">s1.</mrk>\n<mrk mid=\"i2\" mtype=\"seg\">s2</mrk></seg-source>"
			+ "<target xml:lang=\"fr\"><mrk mid=\"i1\" mtype=\"seg\">t1.</mrk>\n<mrk mid=\"i2\" mtype=\"seg\">t2</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		ISegments srcSegs = tu.getSourceSegments();
		assertEquals(2, srcSegs.count());
		TextContainer cont = tu.getSource();
		assertEquals("[s1.] [s2]", fmt.printSegmentedContent(cont, true));
		
		cont = tu.getTarget(locFR);
		ISegments segments = cont.getSegments();
		assertEquals("[t1.] [t2]", fmt.printSegmentedContent(cont, true));
		assertEquals(2, segments.count());
		assertEquals("t1.", segments.get(0).text.toText());
		assertEquals("i1", segments.get(0).id);
		assertEquals("t2", segments.get(1).text.toText());
		assertEquals("i2", segments.get(1).id);
	}

	@Test
	public void testSegmentIDs () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\" xml:space=\"preserve\">"
			+ "<source>  s1.</source>"
			+ "<seg-source>  <mrk mid=\"0\" mtype=\"seg\">s1.</mrk></seg-source>"
			+ "<target xml:lang=\"fr\">  <mrk mid=\"0\" mtype=\"seg\">t1.</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		ISegments srcSegs = tu.getSourceSegments();
		assertEquals(1, srcSegs.count());
		TextContainer cont = tu.getSource();
		assertEquals("  [s1.]", fmt.printSegmentedContent(cont, true));
		
		cont = tu.getTarget(locFR);
		ISegments segments = cont.getSegments();
		assertEquals("  [t1.]", fmt.printSegmentedContent(cont, true));
		assertEquals(1, segments.count());
		assertEquals("t1.", segments.get(0).text.toText());
		assertEquals("0", segments.get(0).id);
	}

	@Test
	public void testWSBetweenSegments () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>  s1.\ns2.</source>"
			+ "<seg-source>  <mrk mid=\"1\" mtype=\"seg\">s1.</mrk>\n<mrk mid=\"2\" mtype=\"seg\">s2.</mrk></seg-source>"
			+ "<target>  <mrk mid=\"1\" mtype=\"seg\">t1.</mrk>\n<mrk mid=\"2\" mtype=\"seg\">t2.</mrk></target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"2\" xml:space=\"preserve\">"
			+ "<source>  s1.\ns2.</source>"
			+ "<seg-source>  <mrk mid=\"1\" mtype=\"seg\">s1.</mrk>\n<mrk mid=\"2\" mtype=\"seg\">s2.</mrk></seg-source>"
			+ "<target>  <mrk mid=\"1\" mtype=\"seg\">t1.</mrk>\n<mrk mid=\"2\" mtype=\"seg\">t2.</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		ISegments srcSegs = tu.getSourceSegments();
		assertEquals(2, srcSegs.count());
		TextContainer cont = tu.getSource();
		assertEquals("[s1.] [s2.]", fmt.printSegmentedContent(cont, true));
		cont = tu.getTarget(locFR);
		assertEquals("[t1.] [t2.]", fmt.printSegmentedContent(cont, true));
		
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		srcSegs = tu.getSourceSegments();
		assertEquals(2, srcSegs.count());
		cont = tu.getSource();
		assertEquals("  [s1.]\n[s2.]", fmt.printSegmentedContent(cont, true));
		cont = tu.getTarget(locFR);
		assertEquals("  [t1.]\n[t2.]", fmt.printSegmentedContent(cont, true));
		
	}
	
	@Test
	public void testSegmentedSourceWithOuterCodes () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source><g id='1'><g id='2'>s1. <g id='3'>s2</g></g></g></source>"
			+ "<seg-source><g id='1'><g id='2'><mrk mid=\"i1\" mtype=\"seg\">s1.</mrk> <g id='3'><mrk mid=\"i2\" mtype=\"seg\">s2</mrk></g></g></g></seg-source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		ISegments srcSegs = tu.getSourceSegments();
		assertEquals(2, srcSegs.count());
		TextContainer cont = tu.getSource();
		assertTrue(cont.get(0).text.getCode(0).getTagType()==TagType.OPENING);
		assertTrue(cont.get(0).text.getCode(1).getTagType()==TagType.OPENING);
		assertTrue(cont.get(2).text.getCode(0).getTagType()==TagType.OPENING);
		assertTrue(cont.get(4).text.getCode(0).getTagType()==TagType.CLOSING);
		assertTrue(cont.get(4).text.getCode(1).getTagType()==TagType.CLOSING);
		assertTrue(cont.get(4).text.getCode(2).getTagType()==TagType.CLOSING);
		assertEquals("<b1/><b2/>[s1.] <b3/>[s2]<e3/><e2/><e1/>", fmt.printSegmentedContent(cont, true));
	}

	@Test
	public void testIgnoredSegmentedTarget () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1. t2</source>"
			+ "<target xml:lang=\"fr\"><mrk mid=\"i1\" mtype=\"seg\">t1.</mrk> <mrk mid=\"i2\" mtype=\"seg\">t2</mrk></target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"2\">"
			+ "<source>t1. t2</source>"
			+ "<target xml:lang=\"fr\">t1. t2</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, noInSegFilter), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getTarget(locFR);
		ISegments segments = cont.getSegments();
		assertEquals("[t1. t2]", fmt.printSegmentedContent(cont, true));
		assertEquals(1, segments.count());
		assertEquals("t1. t2", segments.get(0).text.toText());
		assertEquals("0", segments.get(0).id);
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, noInSegFilter), 2);
		assertNotNull(tu);
		assertEquals("t1. t2", segments.get(0).text.toText());
	}
	
	@Test
	public void testOutputOfResegmentedContent () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">\n"
			+ "<source><g id=\"1\">Sentence 1. Sentence 2</g></source>\n"
			+ "</trans-unit>\n"
			+ "</body>"
			+ "</file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">\n"
			+ "<source><g id=\"1\">Sentence 1. Sentence 2</g></source>\n"
//TODO: THIS IS INVALID!!! NEED TO FIX IT!	
// See https://code.google.com/p/okapi/issues/detail?id=202
			+ "<seg-source><mrk mid=\"0\" mtype=\"seg\"><g id=\"1\">Sentence 1.</mrk><mrk mid=\"1\" mtype=\"seg\"> Sentence 2</g></mrk></seg-source>\n"
			+ "<target xml:lang=\"fr\"><mrk mid=\"0\" mtype=\"seg\"><g id=\"1\">Sentence 1.</mrk><mrk mid=\"1\" mtype=\"seg\"> Sentence 2</g></mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body>"
			+ "</file></xliff>";

		// Create SRX document
		SRXDocument doc = new SRXDocument();
		LanguageMap langMap = new LanguageMap(".*", "default");
		doc.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<Rule>();
		langRules.add(new Rule("\\.", "\\s", true));
		// Add the ruls to the document
		doc.addLanguageRule("default", langRules);
		// Create the segmenter
		ISegmenter seg = doc.compileLanguageRules(locFR, null);
		
		// Get the events and segment the source
		List<Event> events = getEvents(snippet, outSegFilter);
		for ( Event event : events ) {
			if ( event.isTextUnit() ) {
				ITextUnit tu = event.getTextUnit();
				seg.computeSegments(tu.getSource());
				tu.getSource().getSegments().create(seg.getRanges());
				assertEquals("[<b1/>Sentence 1.][ Sentence 2<e1/>]",
					fmt.printSegmentedContent(tu.getSource(), true, false));
			}
		}
		// Output the result
		assertEquals(expected, FilterTestDriver.generateOutput(events,
				locFR, outSegFilter.createSkeletonWriter(), outSegFilter.getEncoderManager()));
	}

	@Test
	public void testGroupIds () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<group id=\"_1\">"
			+ " <group>" // no id
			+ "  <group id=\"g1\">" // 2 will be a duplicate
			+ "  </group>"
			+ " </group>"
			+ "</group>"
			+ "</body>"
			+ "</file></xliff>";
		StartGroup sg = FilterTestDriver.getGroup(getEvents(snippet, noInSegFilter),  1);
		assertNotNull(sg);
		assertEquals("_1", sg.getId());
		sg = FilterTestDriver.getGroup(getEvents(snippet, noInSegFilter),  2);
		assertNotNull(sg);
		assertEquals("g1", sg.getId());
		sg = FilterTestDriver.getGroup(getEvents(snippet, noInSegFilter),  3);
		assertNotNull(sg);
		assertEquals("g2", sg.getId());
	}

	@DataProvider
	public static Object[][] CDATAEntryDataProvider() {
		return new Object[][]{
				{
						false,
						"[t1. t2 & .t3]",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
								+ System.lineSeparator()
								+ "<xliff xmlns:okp=\"okapi-framework:xliff-extensions\" version=\"1.2\">"
								+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
								+ "<body>"
								+ "<trans-unit id=\"1\">"
								+ "<source>t1. t2 &amp; .t3</source>"
								+ "<target xml:lang=\"fr\">t1. t2 &amp; .t3</target>"
								+ "<alt-trans match-quality=\"50\" origin=\"orig\" okp:matchType=\"FUZZY\" okp:engine=\"abc\">"
								+ "<source>alt source <bpt id=\"1\">{</bpt>t1  t2 &amp; <ept id=\"1\">}</ept></source>"
								+ "<target>alt target <mrk mtype=\"term\"><bpt id=\"1\">{</bpt>t1  t2 &amp; <ept id=\"1\">}</ept></mrk></target>"
								+ "</alt-trans>"
								+ "</trans-unit>"
								+ "</body>"
								+ "</file>"
								+ "</xliff>"
				},
				{
						true,
						"[t1.<1> t2 & </1>.t3]",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
								+ System.lineSeparator()
								+ "<xliff xmlns:okp=\"okapi-framework:xliff-extensions\" version=\"1.2\">"
								+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
								+ "<body>"
								+ "<trans-unit id=\"1\">"
								+ "<source>t1.<![CDATA[ t2 & ]]>.t3</source>"
								+ "<target xml:lang=\"fr\">t1.<![CDATA[ t2 & ]]>.t3</target>"
								+ "<alt-trans match-quality=\"50\" origin=\"orig\" okp:matchType=\"FUZZY\" okp:engine=\"abc\">"
								+ "<source>alt source <bpt id=\"1\">{</bpt>t1 <![CDATA[ t2 & ]]><ept id=\"1\">}</ept></source>"
								+ "<target>alt target <mrk mtype=\"term\"><bpt id=\"1\">{</bpt>t1 <![CDATA[ t2 & ]]><ept id=\"1\">}</ept></mrk></target>"
								+ "</alt-trans>"
								+ "</trans-unit>"
								+ "</body>"
								+ "</file>"
								+ "</xliff>"
				},
		};
	}

	@Test
	@UseDataProvider("CDATAEntryDataProvider")
	public void testCDATAEntry(boolean inlineCdata, String expectedContent, String expectedOutput) {
		String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns:okp=\"okapi-framework:xliff-extensions\" version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1.<![CDATA[ t2 & ]]>.t3</source>"
			+ "<target xml:lang=\"fr\">t1.<![CDATA[ t2 & ]]>.t3</target>"
			+ "<alt-trans match-quality=\"50\" origin=\"orig\" okp:matchType=\"FUZZY\" okp:engine=\"abc\">"
			+ "<source>alt source <bpt id=\"1\">{</bpt>t1 <![CDATA[ t2 & ]]><ept id=\"1\">}</ept></source>"
			+ "<target>alt target <mrk mtype=\"term\"><bpt id=\"1\">{</bpt>t1 <![CDATA[ t2 & ]]><ept id=\"1\">}</ept></mrk></target>"
			+ "</alt-trans>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";

		Parameters parameters = new Parameters();
		parameters.setInlineCdata(inlineCdata);
		filter.setParameters(parameters);

		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(input), 1);

		assertNotNull(tu);
		assertEquals(expectedContent, fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals(expectedContent, fmt.printSegmentedContent(tu.getTarget(locFR), true));

		assertEquals(expectedOutput, FilterTestDriver.generateOutput(getEvents(input),
				locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testSegmentedEntry () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1. t2</source>"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">t1.</mrk> <mrk mid=\"2\" mtype=\"seg\">t2</mrk></seg-source>"
			+ "<target xml:lang=\"fr\">t1. t2</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertNotNull(tu);
		assertEquals("[t1.] [t2]", fmt.printSegmentedContent(cont, true));
		assertEquals(2, segments.count());
		assertEquals("t1.", segments.get(0).text.toText());
		assertEquals("t2", segments.get(1).text.toText());
	}

	@Test
	public void testSegmentedSource1 () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1. t2</source>"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">t1.</mrk> <mrk mid=\"2\" mtype=\"seg\">t2</mrk></seg-source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertNotNull(tu);
		assertEquals("[t1.] [t2]", fmt.printSegmentedContent(cont, true));
		assertEquals(2, segments.count());
		assertEquals("t1.", segments.get(0).text.toText());
		assertEquals("t2", segments.get(1).text.toText());
	}

	@Test
	public void testSegmentedWithEmptyTarget () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1. t2</source>"
			+ "<target><mrk mid=\"1\" mtype=\"seg\"></mrk> <mrk mid=\"2\" mtype=\"seg\"></mrk></target>"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">t1.</mrk> <mrk mid=\"2\" mtype=\"seg\">t2</mrk></seg-source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		TextContainer cont = tu.getTarget(locFR);
		assertEquals("[t1.] [t2]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[] []", fmt.printSegmentedContent(cont, true));
	}

	@Test
	public void testEmptyTarget () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1</source>"
			+ "<target></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		TextContainer cont = tu.getTarget(locFR);
		assertEquals("[t1]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[]", fmt.printSegmentedContent(cont, true));
	}

	@Test
	public void testStorageSizeAndAllowedChars () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\" xmlns:i='http://www.w3.org/2005/11/its'>"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source i:storageSize='123' i:allowedCharacters='[a-z]'>t1</source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		GenericAnnotation ga = tu.getSource().getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.ALLOWEDCHARS);
		assertEquals("[a-z]", ga.getString(GenericAnnotationType.ALLOWEDCHARS_VALUE));
		ga = tu.getSource().getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.STORAGESIZE);
		assertEquals(123, (int)ga.getInteger(GenericAnnotationType.STORAGESIZE_SIZE));
		assertEquals("UTF-8", ga.getString(GenericAnnotationType.STORAGESIZE_ENCODING));
		assertEquals("lf", ga.getString(GenericAnnotationType.STORAGESIZE_LINEBREAK));
	}
	
	@Test
	public void testMtConfidence () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" its:version=\"2.0\" version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit its:annotatorsRef=\"mt-confidence|ABC\" id=\"1\">"
			+ "<source>source</source>\n"
			+ "<target its:mtConfidence='0.678' its:annotatorsRef=\"mt-confidence|ABC\">target</target>\n"
			+ "</trans-unit>"
			+ "<trans-unit id=\"1\">"
			+ "<source>source 2</source>\n"
			+ "<target>target 2</target>\n"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		
		List<Event> events = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);

		GenericAnnotation ga = tu.getTarget(locFR).getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.MTCONFIDENCE);
		assertEquals(0.678, ga.getDouble(GenericAnnotationType.MTCONFIDENCE_VALUE), 0.0);
		assertEquals("ABC", ITSContent.getAnnotatorRef("mt-confidence", tu));
		assertEquals("ABC", ga.getString(GenericAnnotationType.ANNOTATORREF));
		
		// Add target-level confidence
		tu = FilterTestDriver.getTextUnit(events, 2);
		TextContainer cont = tu.getTarget(locFR);
		cont.append(" + new text");
		GenericAnnotations.addAnnotations(cont,
			new GenericAnnotations(
				new GenericAnnotation(GenericAnnotationType.MTCONFIDENCE,
					GenericAnnotationType.MTCONFIDENCE_VALUE, 0.789,
					GenericAnnotationType.ANNOTATORREF, "NewOne")));
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" its:version=\"2.0\" version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit its:annotatorsRef=\"mt-confidence|ABC\" id=\"1\">"
			+ "<source>source</source>\n"
			+ "<target its:mtConfidence=\"0.678\" its:annotatorsRef=\"mt-confidence|ABC\">target</target>\n"
			+ "</trans-unit>"
			+ "<trans-unit id=\"1\">"
			+ "<source>source 2</source>\n"
			+ "<target its:mtConfidence=\"0.789\" its:annotatorsRef=\"mt-confidence|NewOne\">target 2 + new text</target>\n"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(events,
			locFR, altTransFilter.createSkeletonWriter(), altTransFilter.getEncoderManager()));
	}
	
	@Test
	public void testMtConfidenceInline () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\" its:annotatorsRef=\"mt-confidence|XYZ\">\n"
			+ "<source>t1. t2</source>\n"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">t1.</mrk> <mrk mid=\"2\" mtype=\"seg\">t2</mrk></seg-source>\n"
			+ "<target xml:lang=\"fr\">"
			+ "<mrk mid=\"1\" mtype=\"seg\" its:mtConfidence=\"0.678\">tt1.</mrk> "
			+ "<mrk mid=\"2\" mtype=\"seg\" its:mtConfidence=\"0.679\" its:annotatorsRef=\"mt-confidence|QAZ\">tt2</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file></xliff>";
		
		List<Event> events = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		ISegments segs = tu.getTarget(locFR).getSegments();
		
		assertEquals("mt-confidence|XYZ", ITSContent.getAnnotatorsRef(tu));
		
		GenericAnnotation ga = segs.get(0).getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.MTCONFIDENCE);
		assertEquals(0.678, ga.getDouble(GenericAnnotationType.MTCONFIDENCE_VALUE), 0.0);
		assertEquals("XYZ", ga.getString(GenericAnnotationType.ANNOTATORREF));
		
		ga = segs.get(1).getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.MTCONFIDENCE);
		assertEquals(0.679, ga.getDouble(GenericAnnotationType.MTCONFIDENCE_VALUE), 0.0);
		assertEquals("QAZ", ga.getString(GenericAnnotationType.ANNOTATORREF));

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\" its:annotatorsRef=\"mt-confidence|XYZ\">\n"
			+ "<source>t1. t2</source>\n"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">t1.</mrk> <mrk mid=\"2\" mtype=\"seg\">t2</mrk></seg-source>\n"
			+ "<target xml:lang=\"fr\">"
			+ "<mrk mid=\"1\" mtype=\"seg\" its:mtConfidence=\"0.678\" its:annotatorsRef=\"mt-confidence|XYZ\">tt1.</mrk> "
			+ "<mrk mid=\"2\" mtype=\"seg\" its:mtConfidence=\"0.679\" its:annotatorsRef=\"mt-confidence|QAZ\">tt2</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(events,
			locFR, altTransFilter.createSkeletonWriter(), altTransFilter.getEncoderManager()));
	}
	
	@Test
	public void testMtConfidenceAltTrans () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\" xmlns:its='http://www.w3.org/2005/11/its'>"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\" its:annotatorsRef=\"mt-confidence|XYZ\">"
			+ "<source>text</source>"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">text</mrk></seg-source>"
			+ "<target xml:lang=\"fr\">"
			+ "<mrk mid=\"1\" mtype=\"seg\">tr-text</mrk></target>\n"
			+ "<alt-trans mid=\"1\">"
			+ "<source>text</source>"
			+ "<target>atr-text</target>"
			+ "</alt-trans>\n"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		List<Event> events = getEvents(snippet, altTransFilter);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertEquals("mt-confidence|XYZ", ITSContent.getAnnotatorsRef(tu));
		ISegments segs = tu.getTarget(locFR).getSegments();
		Segment seg = segs.get(0);
		AltTranslationsAnnotation anns = seg.getAnnotation(AltTranslationsAnnotation.class);
		assertNotNull(anns);
		AltTranslation alt = anns.getFirst();
		ITextUnit atu = alt.getEntry();
		TextContainer tc = atu.getTarget(locFR);
		assertEquals("atr-text", tc.getFirstContent().toString());

		// Modify the alt-trans
		tc.getFirstContent().append(" +++");
		GenericAnnotations gas = new GenericAnnotations(new GenericAnnotation(
			GenericAnnotationType.MTCONFIDENCE,
			GenericAnnotationType.MTCONFIDENCE_VALUE, 98.12,
			GenericAnnotationType.ANNOTATORREF, "abc"));
		GenericAnnotations.addAnnotations(tc, gas);
		
		// Add a new alt-trans
		anns.add(locEN, locFR, new TextFragment("text"), null, new TextFragment("tr2-text"), MatchType.EXACT, 100, null);
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\" its:annotatorsRef=\"mt-confidence|XYZ\">"
			+ "<source>text</source>"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">text</mrk></seg-source>"
			+ "<target xml:lang=\"fr\">"
			+ "<mrk mid=\"1\" mtype=\"seg\">tr-text</mrk></target>\n"
			+ "<alt-trans mid=\"1\">\n"
			+ "<source xml:lang=\"en\">text</source>\n"
			+ "<target xml:lang=\"fr\" its:mtConfidence=\"98.12\" its:annotatorsRef=\"mt-confidence|abc\">atr-text +++</target>\n"
			+ "</alt-trans>\n"
			+ "<alt-trans mid=\"1\" match-quality=\"100\" xmlns:okp=\"okapi-framework:xliff-extensions\" okp:matchType=\"EXACT\">\n"
			+ "<target xml:lang=\"fr\">tr2-text</target>\n"
			+ "</alt-trans>\n"
			+ "\n" // Vestige from extraction
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(events,
			locFR, altTransFilter.createSkeletonWriter(), altTransFilter.getEncoderManager()));
	}
	
	@Test
	public void testLQR () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\" xmlns:i='http://www.w3.org/2005/11/its'>"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>source</source>"
			+ "<target i:locQualityRatingScore=\"50\" i:locQualityRatingScoreThreshold=\"95\" i:locQualityRatingProfileRef=\"http://example.org/qaModel/v13\">target</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"2\">"
			+ "<source>source</source>"
			+ "<target i:locQualityRatingVote=\"60\" i:locQualityRatingVoteThreshold=\"90\" i:locQualityRatingProfileRef=\"http://example.org/qaModel/v14\">target</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"3\">"
			+ "<source>source</source>"
			+ "<target>before <mrk mtype=\"x-its\" i:locQualityRatingScore=\"10\" i:locQualityRatingScoreThreshold=\"80\" i:locQualityRatingProfileRef=\"http://example.org/qaModel/v10\">target</mrk> after</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		GenericAnnotation ga = tu.getTarget(locFR).getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.LQR);
		assertEquals(50.0, ga.getDouble(GenericAnnotationType.LQR_SCORE), 0.0);
		assertEquals(95.0, ga.getDouble(GenericAnnotationType.LQR_SCORETHRESHOLD), 0.0);
		assertEquals("http://example.org/qaModel/v13", ga.getString(GenericAnnotationType.LQR_PROFILEREF));
		
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		ga = tu.getTarget(locFR).getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.LQR);
		assertEquals(60, (int)ga.getInteger(GenericAnnotationType.LQR_VOTE));
		assertEquals(90, (int)ga.getInteger(GenericAnnotationType.LQR_VOTETHRESHOLD));
		assertEquals("http://example.org/qaModel/v14", ga.getString(GenericAnnotationType.LQR_PROFILEREF));
		
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 3);
		List<Code> codes = tu.getTarget(locFR).getFirstContent().getCodes();
		assertEquals(2, codes.size());
		Code code = codes.get(0);
		assertTrue(code.hasOnlyAnnotation());
		ga = code.getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.LQR);
		assertEquals(10.0, ga.getDouble(GenericAnnotationType.LQR_SCORE), 0.0);
		assertEquals(80.0, ga.getDouble(GenericAnnotationType.LQR_SCORETHRESHOLD), 0.0);
		assertEquals("http://example.org/qaModel/v10", ga.getString(GenericAnnotationType.LQR_PROFILEREF));
	}
	
	@Test
	public void testLQRInline () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\" xmlns:its='http://www.w3.org/2005/11/its'>"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\" its:annotatorsRef=\"mt-confidence|XYZ\">"
			+ "<source>t1. t2</source>"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">t1.</mrk> <mrk mid=\"2\" mtype=\"seg\">t2</mrk></seg-source>"
			+ "<target xml:lang=\"fr\">"
			+ "<mrk mid=\"1\" mtype=\"seg\" its:locQualityRatingScore=\"50.1234\" its:locQualityRatingScoreThreshold=\"95\" its:locQualityRatingProfileRef=\"http://example.org/qaModel/v13\">tt1.</mrk> "
			+ "<mrk mid=\"2\" mtype=\"seg\" its:locQualityRatingVote=\"60\" its:locQualityRatingVoteThreshold=\"90\" its:locQualityRatingProfileRef=\"http://example.org/qaModel/v14\">tt2</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		ISegments segs = tu.getTarget(locFR).getSegments();
		
		GenericAnnotation ga = segs.get(0).getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.LQR);
		assertEquals(50.1234, ga.getDouble(GenericAnnotationType.LQR_SCORE), 0.0);
		assertEquals(95.0, ga.getDouble(GenericAnnotationType.LQR_SCORETHRESHOLD), 0.0);
		assertEquals("http://example.org/qaModel/v13", ga.getString(GenericAnnotationType.LQR_PROFILEREF));
		
		ga = segs.get(1).getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.LQR);
		assertEquals(new Integer(60), ga.getInteger(GenericAnnotationType.LQR_VOTE));
		assertEquals(90, (int)ga.getInteger(GenericAnnotationType.LQR_VOTETHRESHOLD));
		assertEquals("http://example.org/qaModel/v14", ga.getString(GenericAnnotationType.LQR_PROFILEREF));
	}

	@Test
	public void testLangAndSpaceInline () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\" xmlns:its='http://www.w3.org/2005/11/its'>"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>e1 <mrk mtype=\"x-its\" xml:lang=\"hu\" xml:space=\"preserve\">hu</mrk></source>"
			+ "<target>f1 <mrk mtype=\"x-its\" xml:lang=\"hu\" xml:space=\"default\">hu</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		Code code = tu.getSource().getFirstContent().getCodes().get(0);
		assertEquals("hu", code.getGenericAnnotationString(
			GenericAnnotationType.LANG, GenericAnnotationType.LANG_VALUE));
		assertEquals("preserve", code.getGenericAnnotationString(
			GenericAnnotationType.PRESERVEWS, GenericAnnotationType.PRESERVEWS_INFO));
		code = tu.getTarget(locFR).getFirstContent().getCodes().get(0);
		assertEquals("hu", code.getGenericAnnotationString(
			GenericAnnotationType.LANG, GenericAnnotationType.LANG_VALUE));
		assertEquals("default", code.getGenericAnnotationString(
			GenericAnnotationType.PRESERVEWS, GenericAnnotationType.PRESERVEWS_INFO));
	}

	@Test
	public void testNoTarget () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1</source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("[t1]", fmt.printSegmentedContent(tu.getSource(), true));
		TextContainer cont = tu.getTarget(locFR);
		assertNull(cont);
	}

	@Test
	public void testCREntity () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>\r\r&#13; {<ph id='1'>#13;  </ph>}</source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		String str = tu.getSource().toString();
		assertEquals("\r {#13;  }", str);
	}

	@Test
	public void testUnbalancedIT () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1<it id='1' pos='open'>[b]</it>t2</source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		List<Code> codes = tu.getSource().getFirstContent().getCodes();
		assertEquals("[b]", codes.get(0).toString());
		String str = tu.getSource().getCodedText();
		assertEquals(TextFragment.MARKER_ISOLATED, str.charAt(2));
		assertEquals(TagType.OPENING, codes.get(0).getTagType());
	}

	@Test
	public void testBalancedIT () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source><it id='1' pos='open'>[b]</it>T<it id='2' pos='close'>[/b]</it></source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		List<Code> codes = tu.getSource().getFirstContent().getCodes();
		assertEquals("[b]", codes.get(0).toString());
		assertEquals("[/b]", codes.get(1).toString());
		String str = tu.getSource().getCodedText();
		assertEquals(TextFragment.MARKER_OPENING, str.charAt(0));
		assertEquals(TextFragment.MARKER_CLOSING, str.charAt(3));
		assertEquals(TagType.OPENING, codes.get(0).getTagType());
		assertEquals(TagType.CLOSING, codes.get(1).getTagType());
	}

	@Test
	public void testCREntityOutput () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>\r\r&#13; {<ph id='1'>#13;  </ph>}</source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>&#13; {<ph id=\"1\">#13;  </ph>}</source>"
			+ "<target xml:lang=\"fr\">&#13; {<ph id=\"1\">#13;  </ph>}</target>\r"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testMtConfidenceOutput () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit its:annotatorsRef=\"mt-confidence|ABC\" id=\"1\">"
			+ "<source>source</source>"
			+ "<target its:mtConfidence=\"0.678\">target</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit its:annotatorsRef=\"mt-confidence|ABC\" id=\"1\">"
			+ "<source>source</source>"
			+ "<target its:mtConfidence=\"0.678\" its:annotatorsRef=\"mt-confidence|ABC\">target</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}
	
	
	@Test
	public void testSegmentedEntryWithDifferences () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1withWarning\">"
			+ "<source>t1. x t2</source>" // Extra x in source
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">t1.</mrk> <mrk mid=\"2\" mtype=\"seg\">t2</mrk></seg-source>"
			+ "<target xml:lang=\"fr\">t1. t2</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals(1, tu.getSource().getSegments().count());
		assertEquals("[t1. x t2]", fmt.printSegmentedContent(tu.getSource(), true));
	}

	@Test
	public void testSegmentedEntryOutput () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\"><!--comment-->"
			+ "<source>t1. t2</source>"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">t1.</mrk> <mrk mid=\"2\" mtype=\"seg\">t2</mrk></seg-source>\r"
			+ "<target xml:lang=\"fr\"><mrk mid=\"1\" mtype=\"seg\">tt1.</mrk> <mrk mid=\"2\" mtype=\"seg\">tt2</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\"><!--comment-->"
			+ "<source>t1. t2</source>"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">t1.</mrk> <mrk mid=\"2\" mtype=\"seg\">t2</mrk></seg-source>\r"
			+ "<target xml:lang=\"fr\"><mrk mid=\"1\" mtype=\"seg\">tt1.</mrk> <mrk mid=\"2\" mtype=\"seg\">tt2</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("[t1.] [t2]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[tt1.] [tt2]", fmt.printSegmentedContent(tu.getTarget(locFR), true));
		
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testSegSourceWithoutMrkOutput () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\"><!--comment-->"
			+ "<source>text src</source>"
			+ "<seg-source>text src</seg-source>\r"
			+ "<target xml:lang=\"fr\">text trg</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\"><!--comment-->"
			+ "<source>text src</source>"
			+ "<seg-source><mrk mid=\"0\" mtype=\"seg\">text src</mrk></seg-source>\r"
			+ "<target xml:lang=\"fr\"><mrk mid=\"0\" mtype=\"seg\">text trg</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("[text src]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[text trg]", fmt.printSegmentedContent(tu.getTarget(locFR), true));
		
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testSegmentedNoTargetEntryOutput () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "\r<body>"
			+ "<trans-unit id=\"1\" xml:space=\"preserve\">"
			+ "<source>t1.   t2</source>"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">t1.</mrk>   <mrk mid=\"2\" mtype=\"seg\">t2</mrk></seg-source>"
			+ "</trans-unit></body></file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "\r<body>"
			+ "<trans-unit id=\"1\" xml:space=\"preserve\">"
			+ "<source>t1.   t2</source>"
			+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">t1.</mrk>   <mrk mid=\"2\" mtype=\"seg\">t2</mrk></seg-source>"
			+ "<target xml:lang=\"fr\"><mrk mid=\"1\" mtype=\"seg\">t1.</mrk>   <mrk mid=\"2\" mtype=\"seg\">t2</mrk></target>"
			+ "\r</trans-unit></body></file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "\r<body>"
			+ "<trans-unit id=\"1\" xml:space=\"preserve\">"
			+ "<source>t1.   t2</source>"
			+ "<target xml:lang=\"fr\">t1.   t2</target>"
			+ "\r</trans-unit></body></file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			locFR, outNoSegFilter.createSkeletonWriter(), outNoSegFilter.getEncoderManager()));
	}

	@Test
	public void testSpecialAttributeValues () {
		// Test even on invalid attributes
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"13\">"
			+ "<source>S1<ph ts=\"&lt;&quot;&gt;&apos;\" id=\"1\" x=\"&lt;&quot;&gt;&apos;\">code</ph></source>"
			+ "<target>T1<ph ts=\"&lt;&quot;&gt;&apos;\" id=\"1\" x=\"&lt;&quot;&gt;&apos;\">code</ph></target>"
			+ "</trans-unit></body>"
			+ "</file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"13\">"
			+ "<source>S1<ph ts=\"&lt;&quot;>'\" id=\"1\" x=\"&lt;&quot;>'\">code</ph></source>"
			+ "<target>T1<ph ts=\"&lt;&quot;>'\" id=\"1\" x=\"&lt;&quot;>'\">code</ph></target>"
			+ "</trans-unit></body>"
			+ "</file></xliff>";
		
		//--This section tests the codesToString--
		ArrayList<Event> events = getEvents(snippet);
		for ( Event ev : events ) {
			if ( ev.getResource() instanceof ITextUnit ) {
				ITextUnit tu = ev.getTextUnit();
				TextContainer tc = tu.getSource();
				for ( Iterator<TextPart> it=tc.iterator(); it.hasNext(); ) {
				    TextPart tp = it.next();  
				    TextFragment tf = tp.getContent();
				    List<Code> oriCodes = tf.getCodes();
				    String codeStr = Code.codesToString(oriCodes);
				    // Compare the codes
				    List<Code> newCodes = Code.stringToCodes(codeStr);
				    assertEquals(oriCodes.size(), newCodes.size());
				    for ( int i=0; i<oriCodes.size(); i++ ) {
				    	Code oriCode = oriCodes.get(i);
				    	Code newCode = newCodes.get(i);
				    	assertEquals(oriCode.getData(), newCode.getData());
				    	assertEquals(oriCode.getId(), newCode.getId());
				    	assertEquals(oriCode.getTagType(), newCode.getTagType());
				    	assertEquals(oriCode.getOuterData(), newCode.getOuterData());
				    }
				}
			}
		}
		
		assertEquals(expected, FilterTestDriver.generateOutput(events,
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"13\">"
			+ "<source>S1<ph ts=\"&lt;&quot;>'\" id=\"1\" x=\"&lt;&quot;>'\">code</ph></source>"
			+ "<seg-source><mrk mid=\"0\" mtype=\"seg\">S1<ph ts=\"&lt;&quot;>'\" id=\"1\" x=\"&lt;&quot;>'\">code</ph></mrk></seg-source>\r"
			+ "<target><mrk mid=\"0\" mtype=\"seg\">T1<ph ts=\"&lt;&quot;>'\" id=\"1\" x=\"&lt;&quot;>'\">code</ph></mrk></target>"
			+ "</trans-unit></body>"
			+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			locFR, outSegFilter.createSkeletonWriter(), outSegFilter.getEncoderManager()));
	}

	@Test
	public void testMrk () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createTUWithMrk(), 1);
		assertNotNull(tu);
		assertTrue(tu.getSource().getFirstContent().hasCode());
		assertEquals("t1t2", tu.getSource().toString()); // mrk has empty data (native data is in outerdata)
	}

	@Test
	public void testOutputMrk () {
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\"><source>t1<mrk mtype=\"x-abc\">t2</mrk></source>"
			+ "<target xml:lang=\"fr\">t1<mrk mtype=\"x-abc\">t2</mrk></target>\r"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(createTUWithMrk(),
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testAlTrans () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createTUWithAltTrans(), 1);
		assertNotNull(tu);
		assertEquals("t1", tu.getSource().toString());
		AltTranslationsAnnotation annot = tu.getTarget(locFR).getAnnotation(AltTranslationsAnnotation.class);
		assertNotNull(annot);
		assertEquals("alt source {t1}", annot.getFirst().getEntry().getSource().toString());
		assertEquals("alt target {t1}", annot.getFirst().getEntry().getTarget(locFR).toString());
		assertTrue(annot.getFirst().getFromOriginal());
		assertEquals(MatchType.FUZZY, annot.getFirst().getType());
		assertEquals(50, annot.getFirst().getCombinedScore());
		assertEquals("orig", annot.getFirst().getOrigin());
		assertEquals("abc", annot.getFirst().getEngine());
	}

	@Test
	public void testAltTransWithEmptyTarget () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xliff xmlns:okp=\"okapi-framework:xliff-extensions\" version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\"><source>t1</source>"
			+ "<target>translated t1</target>"
			+ "<alt-trans match-quality=\"100\" origin=\"orig\">"
			+ "<source xml:lang='en'>alt source</source>"
			+ "<target xml:lang='fr'></target>"
			+ "</alt-trans>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		AltTranslationsAnnotation annot = tu.getTarget(locFR).getAnnotation(AltTranslationsAnnotation.class);
		assertNotNull(annot);
		TextContainer tc = annot.getFirst().getTarget();
		assertNotNull(tc);
		assertTrue(tc.isEmpty());
	}

	@Test
	public void testOutputAlTrans () {
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r<xliff xmlns:okp=\"okapi-framework:xliff-extensions\" version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\"><source>t1</source>"
			+ "<target>translated t1</target>"
			+ "<alt-trans match-quality=\"50\" origin=\"orig\" okp:matchType=\"FUZZY\" okp:engine=\"abc\">"
			+ "<source>alt source <bpt id=\"1\">{</bpt>t1<ept id=\"1\">}</ept></source>"
			+ "<target>alt target <mrk mtype=\"term\"><bpt id=\"1\">{</bpt>t1<ept id=\"1\">}</ept></mrk></target>"
			+ "</alt-trans>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(createTUWithAltTrans(),
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testEmptyTargetInAltTrans () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xliff xmlns:okp=\"okapi-framework:xliff-extensions\" version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\"><source>t1</source>"
			+ "<target>translated t1</target>"
			+ "<alt-trans match-quality=\"50\" origin=\"orig\">"
			+ "<source>alt source <bpt id=\"1\">{</bpt>t1<ept id=\"1\">}</ept></source>"
			+ "<target/>"
			+ "</alt-trans>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		AltTranslationsAnnotation annot = tu.getTarget(locFR).getAnnotation(AltTranslationsAnnotation.class);
		assertNotNull(annot);
		AltTranslation at = annot.getFirst();
		TextContainer tc = at.getTarget();
		assertNotNull(tc);
		assertTrue(tc.isEmpty());
	}

	/**
	 * Test parsing, adding, and resolving <tool> elements to an XLIFF doc.
	 */
	@Test
	public void testTool() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\">\n"
			+ "	<file original=\"test\" source-language=\"en\" target-language=\"fr\" datatype=\"xml\">\n"
			+ "		<header>\n"
			+ "			<tool tool-id=\"Okapi\" tool-name=\"Okapi\"></tool>\n"
			+ "		</header>\n"
			+ "		<body>\n"
			+ "			<trans-unit id=\"1\">\n"
			+ "				<source xml:lang=\"en-us\">Test tool 1</source>\n"
			+ "				<target xml:lang=\"fr-fr\">Test tool 1</target>\n"
			+ "				<alt-trans match-quality=\"100\" origin=\"orig\" tool-id=\"Okapi\">\n"
			+ "					<source xml:lang=\"en-us\">Test tool 1</source>\n"
			+ "					<target xml:lang=\"fr-fr\">Test tool 1</target>\n"
			+ "				</alt-trans>\n"
			+ "			</trans-unit>\n"
			+ "		</body>\n"
			+ "	</file>\n"
			+ "</xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" version=\"1.2\">\n"
			+ "	<file original=\"test\" source-language=\"en\" target-language=\"fr\" datatype=\"xml\">\n"
			+ "		<header>\n"
			+ "			<tool tool-id=\"Okapi\" tool-name=\"Okapi\"></tool>\n"
			+ "			<tool tool-id=\"okp\" tool-name=\"okp\"></tool>\n"
			+ "		</header>\n"
			+ "		<body>\n"
			+ "			<trans-unit id=\"1\">\n"
			+ "				<source xml:lang=\"en-us\">Test tool 1</source>\n"
			+ "				<target xml:lang=\"fr-fr\">Test tool 1</target>\n"
			+ "				<alt-trans match-quality=\"100\" tool-id=\"okp\" origin=\"orig\" xmlns:okp=\"okapi-framework:xliff-extensions\" okp:matchType=\"EXACT\">\n"
			+ "					<source xml:lang=\"en\">Test tool 1</source>\n"
			+ "					<target xml:lang=\"fr\">Test tool 1</target>\n"
			+ "				</alt-trans>\n"
			+ "				<alt-trans match-quality=\"100\" origin=\"orig\" tool-id=\"Okapi\">\n"
			+ "					<source xml:lang=\"en-us\">Test tool 1</source>\n"
			+ "					<target xml:lang=\"fr-fr\">Test tool 1</target>\n"
			+ "				</alt-trans>\n"
			+ "			</trans-unit>\n"
			+ "		</body>\n"
			+ "	</file>\n"
			+ "</xliff>";
		expected = expected.replaceAll("\\s*", "");

		// testFilter closes testDoc
		RawDocument testDoc = new RawDocument(snippet, locEN, locFR);

		Parameters params = new Parameters();
		params.setAddAltTrans(true);

		XLIFFFilter testFilter = new XLIFFFilter();
		testFilter.setParameters(params);
		testFilter.open(testDoc);

		StartSubDocument fileElement;
		List<Event> events = new ArrayList<Event>();
		while (testFilter.hasNext()) {
			Event event = testFilter.next();
			events.add(event);
			if (event.isStartSubDocument()) {
				fileElement = (StartSubDocument) event.getResource();

				XLIFFToolAnnotation toolAnn = fileElement.getAnnotation(XLIFFToolAnnotation.class);
				assertNotNull(toolAnn);

				XLIFFTool toolElement = toolAnn.get("Okapi");
				assertNotNull(toolElement);

				toolAnn.add(new XLIFFTool("okp", "okp"), fileElement);

			} else if (event.isTextUnit()) {
				ITextUnit tu = event.getTextUnit();
				TextContainer target = tu.getTarget(locFR);

				AltTranslationsAnnotation altTrans = target.getAnnotation(AltTranslationsAnnotation.class);
				assertNotNull(altTrans);

				Iterator<AltTranslation> iterAltTrans = altTrans.iterator();
				while (iterAltTrans.hasNext()) {
					AltTranslation alt = iterAltTrans.next();
					XLIFFTool refTool = alt.getTool();
					assertNotNull(refTool);
					assertTrue("Okapi".equals(refTool.getName()));
				}

				AltTranslation newAltTrans = new AltTranslation(locEN, locFR, null,
					tu.getSource().getUnSegmentedContentCopy(),
					tu.getTarget(locFR).getUnSegmentedContentCopy(),
					MatchType.EXACT, 100, "orig");
				newAltTrans.setTool(new XLIFFTool("okp", "okp"));
				altTrans.add(newAltTrans);
			}
		}
		String result = FilterTestDriver.generateOutput(events, locFR, testFilter.createSkeletonWriter(), testFilter.getEncoderManager());
		result = result.replaceAll("\\s*", "");
		assertEquals(expected, result);
		testFilter.close();
	}
	
	@Test
	public void testOutputOverrideTargetlanguage () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "\r<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source xml:lang=\"en\">s1</source>"
			+ "<target xml:lang=\"fr\">t1</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"2\">"
			+ "<source xml:lang=\"en\">s2</source>"
			+ "<target>t2</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"3\">"
			+ "<source xml:lang=\"en\">s3</source>"
			+ "</trans-unit></body></file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"de\" datatype=\"x-test\" original=\"file.ext\">"
			+ "\r<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source xml:lang=\"en\">s1</source>"
			+ "<target xml:lang=\"de\">t1</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"2\">"
			+ "<source xml:lang=\"en\">s2</source>"
			+ "<target>t2</target>" // xml:lang is not added if not present
			+ "</trans-unit>"
			+ "<trans-unit id=\"3\">"
			+ "<source xml:lang=\"en\">s3</source>" // Line-break added
			+ "<target xml:lang=\"de\">s3</target>\r"
			+ "</trans-unit></body></file></xliff>";
		
		XLIFFFilter overrideTrgFilter = new XLIFFFilter();
		Parameters p = overrideTrgFilter.getParameters();
		p.setOverrideTargetLanguage(true);
		
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet, overrideTrgFilter, locDE),
			locDE, overrideTrgFilter.createSkeletonWriter(), overrideTrgFilter.getEncoderManager()));
	}

	@Test
	public void testMixedAlTrans () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createTUWithMixedAltTrans(), 1);
		assertNotNull(tu);
		assertEquals("t1 inter t2", tu.getSource().toString());
		assertEquals("[t1] inter [t2]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[] inter []", fmt.printSegmentedContent(tu.getTarget(locFR), true));
		AltTranslationsAnnotation annot = tu.getTarget(locFR).getAnnotation(AltTranslationsAnnotation.class);
		assertNotNull(annot);
		assertEquals("", annot.getFirst().getEntry().getSource().toString()); // No source
		assertEquals("TRG for t1 inter t2", annot.getFirst().getEntry().getTarget(locFR).toString());
		ISegments segs = tu.getTarget(locFR).getSegments();
		annot = segs.get(0).getAnnotation(AltTranslationsAnnotation.class);
		assertNull(annot);
		annot = segs.get(1).getAnnotation(AltTranslationsAnnotation.class);
		assertEquals("", annot.getFirst().getEntry().getSource().toString()); // No source
		assertEquals("TRG for t2", annot.getFirst().getEntry().getTarget(locFR).toString());
		assertNotNull(annot);
	}

	@Test
	public void testAlTransData () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createTUWithAltTransData(), 1);
		assertNotNull(tu);
		AltTranslationsAnnotation annot = tu.getTarget(locFR).getAnnotation(AltTranslationsAnnotation.class);
		int n = 0;
		for ( AltTranslation at : annot ) {
			n++;
			switch ( n ) {
			case 1:
				assertEquals("alt-trans best target", at.getTarget().toString());
				assertEquals(100, at.getCombinedScore());
				assertEquals(MatchType.EXACT_UNIQUE_ID, at.getType());
				assertEquals(AltTranslation.ORIGIN_SOURCEDOC, at.getOrigin());
				break;
			case 2:
				assertEquals("alt-trans local context", at.getTarget().toString());
				assertEquals(100, at.getCombinedScore());
				assertEquals(MatchType.EXACT_LOCAL_CONTEXT, at.getType());
				assertEquals("qwe", at.getOrigin());
				break;
			case 3:
				assertEquals("alt-trans target 2", at.getTarget().toString());
				assertEquals(101, at.getCombinedScore());
				assertEquals(MatchType.EXACT, at.getType());
				assertEquals("xyz", at.getOrigin());
				break;
			case 4:
				assertEquals("alt-trans target 3", at.getTarget().toString());
				assertEquals(0, at.getCombinedScore());
				assertEquals(MatchType.UKNOWN, at.getType());
				assertEquals(AltTranslation.ORIGIN_SOURCEDOC, at.getOrigin());
				break;
			case 5:
				assertEquals("alt-trans target 4", at.getTarget().toString());
				assertEquals(0, at.getCombinedScore());
				assertEquals(MatchType.UKNOWN, at.getType());
				assertEquals(AltTranslation.ORIGIN_SOURCEDOC, at.getOrigin());
				break;
			}
		}
	}

	@Test
	public void testOutputBPTTypeTransUnit () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"1\" resname=\"13\"><source><g id=\"1\">S1</g>, <g id=\"2\">S2</g></source>"
			+ "<target><g id=\"2\">T2</g>, <g id=\"1\">T1</g></target></trans-unit></body>"
			+ "</file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\" target-language=\"fr\">"
			+ "<body><trans-unit id=\"1\" resname=\"13\"><source><g id=\"1\">S1</g>, <g id=\"2\">S2</g></source>"
			+ "<target><g id=\"2\">T2</g>, <g id=\"1\">T1</g></target></trans-unit></body>"
			+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			locEN, filter.createSkeletonWriter(), filter.getEncoderManager()));
		
	}

	@Test
	public void testAddedCloneCode () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-abc\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"1\">"
			+ "<source>s1 <g id='1'>s2 s3</g> s4.</source>"
			+ "<target>t1 <g id='1'>t2</g> t3 <g id='1'>t4</g>.</target>" // Clone of id='1'
			+ "</trans-unit></body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("t1 <1>t2</1> t3 <1>t4</1>.", fmt.printSegmentedContent(tu.getTarget(locFR), false, false));
	}
	
	@Test
	public void testApprovedTU () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createApprovedTU(), 1);
		assertNotNull(tu);
		assertEquals("t1", tu.getSource().getFirstContent().toText());
		assertEquals("translated t1", tu.getTarget(locFR).getFirstContent().toText());
		Property prop = tu.getTargetProperty(locFR, Property.APPROVED);
		assertNotNull(prop);
		assertEquals("yes", prop.getValue());
	}
	
	@Test
	public void testApprovedOutput () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>en</source><target>fr</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"2\" approved=\"no\">"
			+ "<source>en</source><target>fr</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"3\" approved=\"yes\">"
			+ "<source>en</source><target>fr</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		List<Event> list = getEvents(snippet);

		String expectedNoChange = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>en</source><target>fr</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"2\" approved=\"no\">"
			+ "<source>en</source><target>fr</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"3\" approved=\"yes\">"
			+ "<source>en</source><target>fr</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		assertEquals(expectedNoChange, FilterTestDriver.generateOutput(list,
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Add a property
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		Property prop = tu.getTargetProperty(locFR, Property.APPROVED);
		assertTrue(prop==null);
		prop = tu.createTargetProperty(locFR, Property.APPROVED, false, IResource.CREATE_EMPTY);
		prop.setValue("no");
		
		// Change value
		tu = FilterTestDriver.getTextUnit(list, 2);
		prop = tu.getTargetProperty(locFR, Property.APPROVED);
		assertNotNull(prop);
		assertEquals("no", prop.getValue());
		prop.setValue("yes");
		
		// Remove
		tu = FilterTestDriver.getTextUnit(list, 3);
		prop = tu.getTargetProperty(locFR, Property.APPROVED);
		assertNotNull(prop);
		assertEquals("yes", prop.getValue());
		tu.removeTargetProperty(locFR, Property.APPROVED);

		String expectedChanges = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\" approved=\"no\">" // add
			+ "<source>en</source><target>fr</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"2\" approved=\"yes\">" // change value
			+ "<source>en</source><target>fr</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"3\">" // remove
			+ "<source>en</source><target>fr</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		assertEquals(expectedChanges, FilterTestDriver.generateOutput(list,
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	
	@Test
	public void testTargetStateCoordOutput () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>en</source><target state=\"abc\" coord=\"1;2;3;4\">fr</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		List<Event> list = getEvents(snippet);

		// Check the initial value
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		Property prop1 = tu.getTargetProperty(locFR, "state");
		assertNotNull(prop1);
		assertEquals("abc", prop1.getValue());
		Property prop2 = tu.getTargetProperty(locFR, Property.COORDINATES);
		assertNotNull(prop2);
		assertEquals("1;2;3;4", prop2.getValue());
		// Verify the output (no change)
		assertEquals(snippet, FilterTestDriver.generateOutput(list,
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Change the value but this property is read-only
		prop1.setValue("xyz");
		prop2.setValue("4;3;2;1");

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
//TODO:			+ "<source>en</source><target state=\"xyz\" coord=\"4;3;2;1\">fr</target>"
			+ "<source>en</source><target state=\"abc\" coord=\"1;2;3;4\">fr</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(list,
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}


	@Test
	public void testDefaultInfo () {
		assertNotNull(filter.getParameters());
		assertNotNull(filter.getName());
		List<FilterConfiguration> list = filter.getConfigurations();
		assertNotNull(list);
		assertTrue(list.size()>0);
	}

	@Test
	public void testStartDocument () {
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			inputDocumentToTest("JMP-11-Test01.xlf", null),
			"UTF-8", locEN, locEN));
	}
	
	@Test
	public void testStartDocumentFromList () {
		StartDocument sd = FilterTestDriver.getStartDocument(createSimpleXLIFF());
		assertNotNull(sd);
		assertNotNull(sd.getEncoding());
		assertNotNull(sd.getType());
		assertNotNull(sd.getMimeType());
		assertNotNull(sd.getLocale());
		assertEquals("\r", sd.getLineBreak());
	}
	
	@Test
	public void testStartSubDocumentFromList () {
		StartDocument sd = FilterTestDriver.getStartDocument(createSimpleXLIFF());
		StartSubDocument subd = FilterTestDriver.getStartSubDocument(createSimpleXLIFF(), 1);
		assertNotNull(subd);
		assertNotNull(subd.getId());
		assertEquals(sd.getId(), subd.getParentId());
		assertEquals("file.ext", subd.getName());
		Property prop = subd.getProperty("build-num");
		assertNotNull(prop);
		assertEquals("13", prop.getValue());
	}
	
	@Test
	public void testSimpleTransUnit () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createSimpleXLIFF(), 1);
		assertNotNull(tu);
		assertEquals("Hello World!", tu.getSource().toString());
		assertEquals("13", tu.getName());
		Property prop = tu.getProperty("extradata");
		assertNotNull(prop);
		assertEquals("xd", prop.getValue());
	}

	@Test
	public void testWithNamespaces () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createInputWithNamespace(), 1);
		assertNotNull(tu);
		assertEquals("t1", tu.getSource().toString());
		assertEquals("translated t1", tu.getTarget(locFR).toString());
	}
	
	@Test
	public void testBilingualTransUnit () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createBilingualXLIFF(), 1);
		assertNotNull(tu);
		assertEquals("S1, S2", tu.getSource().toString());
		fmt.setContent(tu.getSource().getFirstContent());
		assertEquals("<1>S1</1>, <2>S2</2>", fmt.toString());
		assertTrue(tu.hasTarget(locFR));
		assertEquals("T2, T1", tu.getTarget(locFR).toString());
		fmt.setContent(tu.getTarget(locFR).getFirstContent());
		assertEquals("<2>T2</2>, <1>T1</1>", fmt.toString());
	}
	
	@Test
	public void testBilingualTransUnitWithEmptyLocales () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createBilingualXLIFFWithLocales(LocaleId.EMPTY, LocaleId.EMPTY), 1);
		assertNotNull(tu);
		assertEquals("S1, S2", tu.getSource().toString());
		fmt.setContent(tu.getSource().getFirstContent());
		assertEquals("<1>S1</1>, <2>S2</2>", fmt.toString());
		assertTrue(tu.hasTarget(locFR));
		assertEquals("T2, T1", tu.getTarget(locFR).toString());
		fmt.setContent(tu.getTarget(locFR).getFirstContent());
		assertEquals("<2>T2</2>, <1>T1</1>", fmt.toString());
		
		tu = FilterTestDriver.getTextUnit(createBilingualXLIFFWithLocales(LocaleId.EMPTY, LocaleId.EMPTY), 2);
		assertNotNull(tu);
		assertEquals("S1, S2", tu.getSource().toString());
		fmt.setContent(tu.getSource().getFirstContent());
		assertEquals("<1>S1</1>, <2>S2</2>", fmt.toString());
		assertTrue(tu.hasTarget(LocaleId.SPANISH));
		assertEquals("T2, T1", tu.getTarget(LocaleId.SPANISH).toString());
		fmt.setContent(tu.getTarget(LocaleId.SPANISH).getFirstContent());
		assertEquals("<2>T2</2>, <1>T1</1>", fmt.toString());
		
		tu = FilterTestDriver.getTextUnit(createBilingualXLIFFWithLocales(LocaleId.ENGLISH, LocaleId.EMPTY), 1);
		assertNotNull(tu);
		assertEquals("S1, S2", tu.getSource().toString());
		fmt.setContent(tu.getSource().getFirstContent());
		assertEquals("<1>S1</1>, <2>S2</2>", fmt.toString());
		assertTrue(tu.hasTarget(locFR));
		assertEquals("T2, T1", tu.getTarget(locFR).toString());
		fmt.setContent(tu.getTarget(locFR).getFirstContent());
		assertEquals("<2>T2</2>, <1>T1</1>", fmt.toString());
		
		tu = FilterTestDriver.getTextUnit(createBilingualXLIFFWithLocales(LocaleId.ENGLISH, LocaleId.EMPTY), 2);
		assertNotNull(tu);
		assertEquals("S1, S2", tu.getSource().toString());
		fmt.setContent(tu.getSource().getFirstContent());
		assertEquals("<1>S1</1>, <2>S2</2>", fmt.toString());
		assertTrue(tu.hasTarget(LocaleId.SPANISH));
		assertEquals("T2, T1", tu.getTarget(LocaleId.SPANISH).toString());
		fmt.setContent(tu.getTarget(LocaleId.SPANISH).getFirstContent());
		assertEquals("<2>T2</2>, <1>T1</1>", fmt.toString());
		
		tu = FilterTestDriver.getTextUnit(createBilingualXLIFFWithLocales(LocaleId.EMPTY, LocaleId.FRENCH), 1);
		assertNotNull(tu);
		assertEquals("S1, S2", tu.getSource().toString());
		fmt.setContent(tu.getSource().getFirstContent());
		assertEquals("<1>S1</1>, <2>S2</2>", fmt.toString());
		assertTrue(tu.hasTarget(locFR));
		assertEquals("T2, T1", tu.getTarget(locFR).toString());
		fmt.setContent(tu.getTarget(locFR).getFirstContent());
		assertEquals("<2>T2</2>, <1>T1</1>", fmt.toString());
		
		tu = FilterTestDriver.getTextUnit(createBilingualXLIFFWithLocales(LocaleId.EMPTY, LocaleId.SPANISH), 2);
		assertNotNull(tu);
		assertEquals("S1, S2", tu.getSource().toString());
		fmt.setContent(tu.getSource().getFirstContent());
		assertEquals("<1>S1</1>, <2>S2</2>", fmt.toString());
		assertTrue(tu.hasTarget(LocaleId.SPANISH));
		assertEquals("T2, T1", tu.getTarget(LocaleId.SPANISH).toString());
		fmt.setContent(tu.getTarget(LocaleId.SPANISH).getFirstContent());
		assertEquals("<2>T2</2>, <1>T1</1>", fmt.toString());
	}

	@Test
	public void testBilingualInlines () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createBilingualXLIFF(), 1);
		assertNotNull(tu);
		assertTrue(tu.hasTarget(locFR));
		TextFragment src = tu.getSource().getFirstContent();
		TextFragment trg = tu.getTarget(locFR).getFirstContent();
		assertEquals(4, src.getCodes().size());
		assertEquals(src.getCodes().size(), trg.getCodes().size());
		FilterTestDriver.checkCodeData(src, trg);
	}
	
	@Test
	public void testBPTTypeTransUnit () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createBPTTypeXLIFF(), 1);
		assertNotNull(tu);
		fmt.setContent(tu.getSource().getFirstContent());
		assertEquals("<1>S1</1>, <2>S2</2>", fmt.toString());
		assertTrue(tu.hasTarget(locFR));
		fmt.setContent(tu.getTarget(locFR).getFirstContent());
		assertEquals("<2>T2</2>, <1>T1</1>", fmt.toString());
	}

	@Test
	public void testBPTAndSUBTypeTransUnit () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createBPTAndSUBTypeXLIFF(), 1);
		assertNotNull(tu);
		fmt.setContent(tu.getSource().getFirstContent());
		//assertEquals("<1>S1</1>, <2>S2</2>", fmt.toString());
		assertEquals("<b1/>text<"+(Integer.MAX_VALUE-1)+">S1</"+(Integer.MAX_VALUE-1)+">, <2>S2</2>", fmt.toString());
	}

	@Test
	public void testBPTWithSUB () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createBPTAndSUBTypeXLIFF(), 1);
		assertNotNull(tu);
		Code code = tu.getSource().getFirstContent().getCode(0);
		assertEquals("a", code.getData());
		assertEquals("<bpt id=\"1\">a<sub>", code.getOuterData());
		code = tu.getSource().getFirstContent().getCode(1);
		assertEquals("", code.getData());
		assertEquals("</sub></bpt>", code.getOuterData());
		assertEquals("<it id=\"1\" pos=\"open\">a</it>text<bpt id=\""+(Integer.MAX_VALUE-1)+"\"></bpt>S1"
			+ "<ept id=\""+(Integer.MAX_VALUE-1)+"\">/a</ept>, <bpt id=\"2\">b</bpt>S2<ept id=\"2\">/b</ept>",
			xcnt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testPreserveSpaces () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createTUWithSpaces(), 1);
		assertNotNull(tu);
		fmt.setContent(tu.getSource().getFirstContent());
		assertTrue(tu.preserveWhitespaces());
		assertEquals("t1  t2 t3\t\t<1/>  t4", fmt.toString());
	}

	@Test
	public void testUnwrapSpaces () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createTUWithSpaces(), 2);
		assertNotNull(tu);
		fmt.setContent(tu.getSource().getFirstContent());
		assertFalse(tu.preserveWhitespaces());
		assertEquals("t1 t2 t3 <1/> t4", fmt.toString());
	}

	@Test
	public void testPreserveSpacesInSegmentedTU () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createSegmentedTUWithSpaces(), 1);
		assertNotNull(tu);
		assertEquals("[t1  t2]  [t3  t4]", fmt.printSegmentedContent(tu.getSource(), true));
		//TODO: XLIFF filter needs to get segmented targets too
		assertEquals("[tt1  tt2  tt3  tt4]", fmt.printSegmentedContent(tu.getTarget(locFR), true));
	}

	@Test
	public void testUnwrapSpacesInSegmentedTU () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createSegmentedTUWithSpaces(), 2);
		assertNotNull(tu);
		assertEquals("[t1 t2] [t3 t4]", fmt.printSegmentedContent(tu.getSource(), true));
		//TODO: XLIFF filter needs to get segmented targets too
		assertEquals("[tt1 tt2 tt3 tt4]", fmt.printSegmentedContent(tu.getTarget(locFR), true));
	}

	@Test
	public void testComplexSUB () {
		List<Event> events = createComplexSUBTypeXLIFF();
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
// Can't get the codes right because of... not using outer-codes?
//		assertEquals("t1 <ph id=\"10\">startCode<sub>[nested<ph id=\"20\">ph-in-sub</ph>still in sub]</sub>endCode</ph> t2",
//			xcnt.setContent(tu.getSource().getFirstContent()).toString());
		TextFragment tf = tu.getSource().getFirstContent();
		assertEquals("startCode", tf.getCode(0).getData());
		assertEquals("<ph id=\"10\">startCode<sub>", tf.getCode(0).getOuterData());
		assertEquals(10, tf.getCode(0).getId());
		assertEquals("<ph id=\"10\">startCode<sub>", tf.getCode(0).getOuterData());
		assertEquals("ph-in-sub", tf.getCode(1).getData());
		assertEquals("<ph id=\"20\">ph-in-sub</ph>", tf.getCode(1).getOuterData());
		assertEquals(Integer.MAX_VALUE-1, tf.getCode(1).getId());
		assertEquals("</sub>endCode</ph>", tf.getCode(2).getOuterData());

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r<xliff version=\"1.2\">\r"
				+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\" target-language=\"fr\">"
				+ "<body><trans-unit id=\"13\"><source>t1 <ph id=\"10\">startCode<sub>[nested<ph id=\"20\">ph-in-sub</ph>still in sub]</sub>endCode</ph> t2</source>"
				+ "<target xml:lang=\"fr\">t1 <ph id=\"10\">startCode<sub>[nested<ph id=\"20\">ph-in-sub</ph>still in sub]</sub>endCode</ph> t2</target>"
				+ "\r</trans-unit></body>"
				+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(events,
			locEN, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testComplexSUBInTarget () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createComplexSUBTypeXLIFF(), 1);
		assertNotNull(tu);
		tu.createTarget(locFR, true, IResource.COPY_ALL);
		Code code = tu.getTarget(locFR).getFirstContent().getCode(0);
		assertEquals("startCode", code.getData());
		assertEquals("<ph id=\"10\">startCode<sub>", code.getOuterData());
	}
	
	@Test
	public void testSimpleSub () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"tu1\">"
			+ "<source>t1<ph id='111'>c1<sub xid='-1213'>subtext</sub>c2</ph>t2</source>"
			+ "</trans-unit></body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		TextFragment tf = tu.getSource().getFirstContent();
		assertEquals("c1", tf.getCode(0).getData());
		assertEquals(111, tf.getCode(0).getId());
		assertEquals("c2", tf.getCode(1).getData());
		assertEquals("</sub>c2</ph>", tf.getCode(1).getOuterData());
		assertEquals(Integer.MAX_VALUE-1, tf.getCode(1).getId());
		assertEquals("t1<ph id=\"111\">c1</ph>subtext<ph id=\""+(Integer.MAX_VALUE-1)+"\">c2</ph>t2",
			xcnt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testMultiLevelsSub () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"tu1\"><source>"
			+ "(t1<ph id='1'>(c&amp;1<sub>(t2<ph id='2'>(c2<sub>(t3<ph id='5'>(c3)</ph>t3)</sub>c&lt;2)</ph>t2)</sub>c1)</ph>t1)"
			+ " text<x id='4'/><x id='3'/></source>"
			+ "</trans-unit></body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
//		assertEquals("(t1<1/>(t2<2/>(t3<3/>t3)<4/>t2)<4/>t1) text<4/><5/>",
//			fmt.printSegmentedContent(tu.getSource(), false));
//		assertEquals("(t1<ph id=\"1\">(c1<sub>(t2<ph id=\"2\">(c2<sub>(t3<ph id=\"5\">(c3)</ph>t3)</sub>c2)</ph>t2)</sub>c1)</ph>t1) text<x id=\"4\"/><x id=\"3\"/>",
//			fmt.printSegmentedContent(tu.getSource(), false, true));
		TextFragment tf = tu.getSource().getFirstContent();
		assertEquals("(c&1", tf.getCode(0).getData());
		assertEquals("<ph id=\"1\">(c&amp;1<sub>", tf.getCode(0).getOuterData());
		assertEquals("(c2", tf.getCode(1).getData());
		assertEquals("(c3)", tf.getCode(2).getData());
		assertEquals("c<2)", tf.getCode(3).getData());
		assertEquals("</sub>c&lt;2)</ph>", tf.getCode(3).getOuterData());
		assertEquals("c1)", tf.getCode(4).getData());
	}

	@Test
	public void testSegmentationWithEmptyTarget () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createSegmentedTUEmptyTarget(), 1);
		assertNotNull(tu);
		assertEquals("<1/>[t1]", fmt.printSegmentedContent(tu.getSource(), true));
		TextContainer trgCont = tu.getTarget(locFR);
		assertNotNull(trgCont);
		assertEquals("<1/>[]", fmt.printSegmentedContent(trgCont, true));
	}

	@Test
	public void testOutputSegmentationWithEmptyTarget () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\" target-language=\"fr\">"
			+ "<body>"
			+ "<trans-unit id=\"1\" xml:space=\"preserve\"><source><ph id=\"1\">code</ph>t1</source>"
			+ "<seg-source><ph id=\"1\">code</ph><mrk mid=\"s1\" mtype=\"seg\">t1</mrk></seg-source>\r"
			+ "<target xml:lang=\"fr\"><ph id=\"1\">code</ph><mrk mid=\"s1\" mtype=\"seg\"></mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\" target-language=\"fr\">"
			+ "<body>"
			+ "<trans-unit id=\"1\" xml:space=\"preserve\"><source><ph id=\"1\">code</ph>t1</source>"
			+ "<seg-source><ph id=\"1\">code</ph><mrk mid=\"s1\" mtype=\"seg\">t1</mrk></seg-source>\r"
			+ "<target xml:lang=\"fr\"><ph id=\"1\">code</ph><mrk mid=\"s1\" mtype=\"seg\"></mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			locEN, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testNotes () {
		ITextUnit tu = FilterTestDriver.getTextUnit(createDecoratedXLIFF(), 1);
		assertNotNull(tu);
		XLIFFNoteAnnotation notes = tu.getAnnotation(XLIFFNoteAnnotation.class);
		assertNotNull(notes);
		assertEquals("note 1", notes.getNote(0).getNoteText());
		assertEquals("note 2", notes.getNote(1).getNoteText());
		
		notes = tu.getSource().getAnnotation(XLIFFNoteAnnotation.class);
		assertNotNull(notes);
		assertEquals("note src 1", notes.getNote(0).getNoteText());
		assertEquals("note src 2", notes.getNote(1).getNoteText());
		
		notes = tu.getTarget(locFR).getAnnotation(XLIFFNoteAnnotation.class);
		assertNotNull(notes);
		assertEquals("note trg", notes.getNote(0).getNoteText());
	}

	@Test
	public void testDoubleExtractionFR () {
		// Read all files in the data directory
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(inputDocumentToTest("JMP-11-Test01.xlf", null));
//		list.add(inputDocumentToTest("lqiTest.xlf", null)); // Issue 388
		list.add(inputDocumentToTest("Manual-12-AltTrans.xlf", null));
		list.add(inputDocumentToTest("PAS-10-Test01.xlf", null));
		list.add(inputDocumentToTest("RB-11-Test01.xlf", null));
		list.add(inputDocumentToTest("RB-12-Test02.xlf", null));
		list.add(inputDocumentToTest("SF-12-Test03.xlf", null));
		list.add(inputDocumentToTest("NSTest01.xlf", null));
		list.add(inputDocumentToTest("BinUnitTest01.xlf", null));
		list.add(inputDocumentToTest("MQ-12-Test01.xlf", null));
		list.add(inputDocumentToTest("sdlxliff/okapi_intro_test15977.sdlxliff", null));		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR));
	}

	@Test
	public void testDoubleExtractionDE () {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(inputDocumentToTest("Typo3Draft.xlf", null));
		list.add(inputDocumentToTest("Xslt-Test01.xlf", null));
		list.add(inputDocumentToTest("TS09-12-Test01.xlf", null));
		list.add(inputDocumentToTest("segmented.xlf", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locDE));
	}

	@Test
	public void testDoubleExtractionFromDEDEtoENUS () {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(inputDocumentToTest("OnTramTest01.xlf", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.fromString("de-de"), LocaleId.fromString("en-us")));
	}

	@Test
	public void testDoubleExtractionES () {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(inputDocumentToTest("SF-12-Test01.xlf", null));
		list.add(inputDocumentToTest("SF-12-Test02.xlf", null));
		list.add(inputDocumentToTest("test1_es.xlf", null));
		list.add(inputDocumentToTest("test2_es.xlf", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locES, "out"));
	}

	@Test
	public void testDoubleExtractionWithCustomElements () {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(inputDocumentToTest("addingElements.xlf", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.fromBCP47("enm"), locEN));
	}
	
	@Test
	public void testDoubleExtractionWithMultiAltTrans() {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(inputDocumentToTest("alttrans.xlf", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.fromBCP47("en-us"), locFR));
	}
	
	@Test
	public void testEmptyCodes () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\" build-num=\"13\">"
			+ "<body><trans-unit id=\"1\" resname=\"13\" extradata=\"xd\"><source>code=<x id=\"1\"/></source></trans-unit></body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("code=", tu.getSource().toString());
		tu.setTarget(LocaleId.FRENCH, tu.getSource());
		assertEquals("code=", tu.getTarget(LocaleId.FRENCH).toString());
		assertNotNull(tu);
	}

	@Test
	public void testDecimalAltTransValues() throws Exception {
		RawDocument rd = new RawDocument(location.in("/altTrans-100.xlf").asUri(),
										 "UTF-8", locEN);
		rd.setTargetLocale(locES);
		List<ITextUnit> tus = FilterTestDriver.filterTextUnits(
				FilterTestDriver.getEvents(altTransFilter, rd, altTransFilter.getParameters()));
		assertEquals(1, tus.size());
		ITextUnit tu = tus.get(0);
		AltTranslationsAnnotation anns = tu.getTarget(locES).getAnnotation(AltTranslationsAnnotation.class);
		assertNotNull(anns);
		Iterator<AltTranslation> it = anns.iterator();
		assertTrue(it.hasNext());
		assertEquals(100, it.next().getCombinedScore());
		assertTrue(it.hasNext());
		assertEquals(100, it.next().getCombinedScore());
		assertTrue(it.hasNext());
		assertEquals(100, it.next().getCombinedScore());
		assertTrue(it.hasNext());
		assertEquals(100, it.next().getCombinedScore());
	}

	@Test
	public void testTranslateOnTU () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\"><body>"
			+ "<trans-unit id=\"1\"><source>t1</source></trans-unit>"
			+ "<trans-unit id=\"2\" translate=\"no\"><source>t2</source></trans-unit>"
			+ "<trans-unit id=\"3\" translate=\"yes\"><source>t3</source></trans-unit>"
			+ "</body></file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("t1", tu.getSource().toString());
		assertTrue(tu.isTranslatable());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertEquals("t2", tu.getSource().toString());
		assertFalse(tu.isTranslatable());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 3);
		assertEquals("t3", tu.getSource().toString());
		assertTrue(tu.isTranslatable());
	}

	@Test
	public void testTranslateOnGroup() {
		String snippet = "<?xml version='1.0'?>\r"
				+ "<xliff version='1.2'>\r"
				+ "<file source-language='en' datatype='plaintext' original='file.ext'><body>"
				+ "<group id='g1'>" // t
				+ " <trans-unit id='1'><source>t1</source></trans-unit>"
				+ " <group id='g2' translate='no'>" // t+nt
				+ "  <trans-unit id='2'><source>nt2</source></trans-unit>"
				+ "  <group id='g3' translate='yes'>" // t+nt+t
				+ "   <trans-unit id='3'><source>t3</source></trans-unit>"
				+ "  </group>" // t+nt
				+ "  <trans-unit id='4'><source>nt4</source></trans-unit>"
				+ " </group>" // t
				+ " <trans-unit id='5'><source>t5</source></trans-unit>"
				+ "</group>" // t (default)
				+ "<group id='g4' translate='no'>" // nt
				+ "<trans-unit id='6'><source>nt6</source></trans-unit>"
				+ "<trans-unit id='7' translate='yes'><source>t7</source></trans-unit>"
				+ "<bin-unit id='img1' mime-type='image' translate='yes'>" // nt+t
				+ "<bin-source><external-file href='image1.png'/>"
				+ "</bin-source>"
				+ "<trans-unit id='img1-1'>"
				+ "<source>t8</source>"
				+ "</trans-unit>"
				+ "</bin-unit>"
				+ "</group>"
				+ "</body></file></xliff>";
		List<Event> events = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertEquals("t1", tu.getSource().toString());
		assertTrue(tu.isTranslatable());
		tu = FilterTestDriver.getTextUnit(events, 2);
		assertEquals("nt2", tu.getSource().toString());
		assertFalse(tu.isTranslatable());
		tu = FilterTestDriver.getTextUnit(events, 3);
		assertEquals("t3", tu.getSource().toString());
		assertTrue(tu.isTranslatable());
		tu = FilterTestDriver.getTextUnit(events, 4);
		assertEquals("nt4", tu.getSource().toString());
		assertFalse(tu.isTranslatable());
		tu = FilterTestDriver.getTextUnit(events, 5);
		assertEquals("t5", tu.getSource().toString());
		assertTrue(tu.isTranslatable());
		tu = FilterTestDriver.getTextUnit(events, 6);
		assertEquals("nt6", tu.getSource().toString());
		assertFalse(tu.isTranslatable());
		tu = FilterTestDriver.getTextUnit(events, 7);
		assertEquals("t7", tu.getSource().toString());
		assertTrue(tu.isTranslatable());
		tu = FilterTestDriver.getTextUnit(events, 8);
		assertEquals("t8", tu.getSource().toString());
		assertTrue(tu.isTranslatable());
		
		StartGroup st = FilterTestDriver.getGroup(events, 1);
		assertEquals("g1", st.getId());
		assertTrue(st.isTranslatable());
		st = FilterTestDriver.getGroup(events, 2);
		assertEquals("g2", st.getId());
		assertFalse(st.isTranslatable());
		st = FilterTestDriver.getGroup(events, 3);
		assertEquals("g3", st.getId());
		assertTrue(st.isTranslatable());
		st = FilterTestDriver.getGroup(events, 4);
		assertEquals("g4", st.getId());
		assertFalse(st.isTranslatable());
		st = FilterTestDriver.getGroup(events, 5);
		assertEquals("img1", st.getId());
		assertTrue(st.isTranslatable());
	}

	@Test
	public void testProtectedOnMRK () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\"><body>"
			+ "<trans-unit id=\"1\"><source>A <mrk mtype='protected'>code</mrk> Z</source></trans-unit>"
			+ "</body></file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("A <1/> Z", fmt.setContent(tu.getSource().getFirstContent()).toString());
		assertEquals("<mrk mtype=\"protected\">code</mrk>", tu.getSource().getFirstContent().getCode(0).getOuterData());
	}

	@Test
	public void testITSAnnotations () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\" xmlns:its=\"http://www.w3.org/2005/11/its\" its:version=\"2.0\" "
			+ "xmlns:itsxlf=\""+Namespaces.ITSXLF_NS_URI+"\">\n"
			+ "<file source-language=\"en\" target-language='fr' datatype=\"plaintext\" original=\"file.ext\"><body>"
			+ "<trans-unit id=\"1\">\n"
			+ "<source its:locQualityIssueComment='c2' its:allowedCharacters='[a-z0-9]'"
			+ " its:storageSize='10' its:storageEncoding='us-ascii' its:lineBreakType='cr'>t1 "
			+ "<mrk mtype='x-its' its:locQualityIssueComment='c3'>t2</mrk>"
			+ "</source>"
			+ "<target its:locQualityIssueComment='c4'>trg1</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"2\" its:localeFilterList=\"*-JP\" its:localeFilterType=\"exclude\">\n"
			+ "<source><mrk its:localeFilterList=\"*\" mtype=\"x-its\">abc</mrk></source>"
			+ "</trans-unit>"
			+ "</body></file></xliff>";
		
		List<Event> events = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		// Source level
		GenericAnnotations anns = tu.getSource().getAnnotation(GenericAnnotations.class);
		assertNotNull(anns);
		assertEquals("[a-z0-9]", anns.getFirstAnnotation(GenericAnnotationType.ALLOWEDCHARS).getString(GenericAnnotationType.ALLOWEDCHARS_VALUE));
		assertEquals(10, (int)anns.getFirstAnnotation(GenericAnnotationType.STORAGESIZE).getInteger(GenericAnnotationType.STORAGESIZE_SIZE));
		assertEquals("us-ascii", anns.getFirstAnnotation(GenericAnnotationType.STORAGESIZE).getString(GenericAnnotationType.STORAGESIZE_ENCODING));
		assertEquals("cr", anns.getFirstAnnotation(GenericAnnotationType.STORAGESIZE).getString(GenericAnnotationType.STORAGESIZE_LINEBREAK));
		assertEquals("c2", anns.getFirstAnnotation(GenericAnnotationType.LQI).getString(GenericAnnotationType.LQI_COMMENT));
		// inline level in source
		List<Code> codes = tu.getSource().getFirstContent().getCodes();
		assertEquals(2, codes.size());
		Code code = codes.get(0);
		assertTrue(code.hasOnlyAnnotation());
		assertEquals("c3", code.getGenericAnnotationString(GenericAnnotationType.LQI, GenericAnnotationType.LQI_COMMENT));
		// Target-level
		anns = tu.getTarget(LocaleId.FRENCH).getAnnotation(GenericAnnotations.class);
		assertNotNull(anns);
		assertEquals("c4", anns.getFirstAnnotation(GenericAnnotationType.LQI).getString(GenericAnnotationType.LQI_COMMENT));
		
		tu = FilterTestDriver.getTextUnit(events, 2);
		anns = tu.getAnnotation(GenericAnnotations.class);
		assertNotNull(anns);
		assertEquals("!*-JP", anns.getFirstAnnotation(GenericAnnotationType.LOCFILTER).getString(GenericAnnotationType.LOCFILTER_VALUE));
		
		code = tu.getSource().getFirstContent().getCode(0);
		assertEquals("*", code.getGenericAnnotationString(GenericAnnotationType.LOCFILTER, GenericAnnotationType.LOCFILTER_VALUE));
	}

	@Test
	public void testITSAnnotatorsRef () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\" xmlns:its=\"http://www.w3.org/2005/11/its\" its:version=\"2.0\" "
			+ "xmlns:itsxlf=\""+Namespaces.ITSXLF_NS_URI+"\" its:annotatorsRef='text-analysis|uri1'>\n"
			+ "<file its:annotatorsRef='mt-confidence|uri1 translate|uri2' source-language=\"en\" target-language='fr' datatype=\"plaintext\" original=\"file.ext\"><body>"
			+ "<trans-unit id=\"1\" its:annotatorsRef='mt-confidence|uri1 translate|uri3'>\n"
			+ "<source its:locQualityIssueComment='c2' its:allowedCharacters='[a-z0-9]'"
			+ " its:storageSize='10' its:storageEncoding='us-ascii' its:lineBreakType='cr'>t1 "
			+ "<mrk mtype='x-its' its:locQualityIssueComment='c3' its:annotatorsRef='mt-confidence|uri2'>t2</mrk>"
			+ "</source>"
			+ "<target its:locQualityIssueComment='c4'>trg1</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"2\" its:annotatorsRef='locale-filter|uri4' its:localeFilterList=\"*-JP\" its:localeFilterType=\"exclude\">\n"
			+ "<source><mrk its:localeFilterList=\"*\" mtype=\"x-its\">abc</mrk></source>"
			+ "</trans-unit>"
			+ "</body></file></xliff>";
		
		List<Event> events = getEvents(snippet);
		int tuCount = 0;
		for ( Event event : events ) {
			switch ( event.getEventType() ) {
			case START_SUBDOCUMENT:
				assertEquals("mt-confidence|uri1 text-analysis|uri1 translate|uri2",
					ITSContent.getAnnotatorsRef(event.getStartSubDocument()));
				assertEquals("uri1",
					ITSContent.getAnnotatorRef("text-analysis", event.getStartSubDocument()));
				break;
			case TEXT_UNIT:
				tuCount++;
				if ( tuCount == 1 ) { 
					assertEquals("mt-confidence|uri1 text-analysis|uri1 translate|uri3",
						ITSContent.getAnnotatorsRef(event.getTextUnit()));
					Code code = event.getTextUnit().getSource().getFirstContent().getCode(0);
					String value = code.getGenericAnnotationString(GenericAnnotationType.ANNOT,
						GenericAnnotationType.ANNOT_VALUE);
					assertEquals("mt-confidence|uri2 text-analysis|uri1 translate|uri3", value);
					assertEquals("uri3", ITSContent.getAnnotatorRef("translate", value));
				}
				else if ( tuCount == 2 ) { 
					assertEquals("locale-filter|uri4 mt-confidence|uri1 text-analysis|uri1 translate|uri2",
						ITSContent.getAnnotatorsRef(event.getTextUnit()));
				}
				break;
			default:
				break;
			}
		}
		
	}

	@Test
	public void testLQIAnnotations () {
		// In source mrk of trans-unit 1; test RawDocument URI.
		ITextUnit tu = FilterTestDriver.getTextUnit(filter,
			inputDocumentToTest("lqiTest.xlf", null), "UTF-8", locEN, locFR, 1);
		assertNotNull(tu);
		Code code = tu.getSource().getFirstContent().getCode(0);
		GenericAnnotations anns = code.getGenericAnnotations();
		assertNotNull(anns);
		List<GenericAnnotation> list = anns.getAnnotations(GenericAnnotationType.LQI);
		assertEquals(2, list.size());
		assertEquals("comment1", list.get(0).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals("comment2", list.get(1).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals("misspelling", list.get(1).getString(GenericAnnotationType.LQI_TYPE));

		// In source mrk of trans-unit 1; test RawDocument InputStream.
		tu = FilterTestDriver.getTextUnitFromInputStream(filter,
			inputDocumentToTest("lqiTest.xlf", null), "UTF-8", locEN, locFR, 1);
		assertNotNull(tu);
		code = tu.getSource().getFirstContent().getCode(0);
		anns = code.getGenericAnnotations();
		assertNotNull(anns);
		list = anns.getAnnotations(GenericAnnotationType.LQI);
		assertEquals(2, list.size());
		assertEquals("comment1", list.get(0).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals("comment2", list.get(1).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals("misspelling", list.get(1).getString(GenericAnnotationType.LQI_TYPE));

		// In source mrk of trans-unit 2 (external); test RawDocument URI
		tu = FilterTestDriver.getTextUnit(filter,
			inputDocumentToTest("lqiTest.xlf", null), "UTF-8", locEN, locFR, 2);
		assertNotNull(tu);
		code = tu.getSource().getFirstContent().getCode(0);
		anns = code.getGenericAnnotations();
		assertNotNull(anns);
		list = anns.getAnnotations(GenericAnnotationType.LQI);
		assertEquals(2, list.size());
		assertEquals("comment3", list.get(0).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals("comment4", list.get(1).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals("misspelling", list.get(1).getString(GenericAnnotationType.LQI_TYPE));

		// In source mrk of trans-unit 3 (external); different resource, same fragment Id as trans-unit 2
		tu = FilterTestDriver.getTextUnit(filter,
			inputDocumentToTest("lqiTest.xlf", null), "UTF-8", locEN, locFR, 3);
		assertNotNull(tu);
		code = tu.getSource().getFirstContent().getCode(0);
		anns = code.getGenericAnnotations();
		assertNotNull(anns);
		list = anns.getAnnotations(GenericAnnotationType.LQI);
		assertEquals(2, list.size());
		assertEquals("comment5", list.get(0).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals("comment6", list.get(1).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals("mistranslation", list.get(1).getString(GenericAnnotationType.LQI_TYPE));

		// In source mrk of trans-unit 4 (external); same fragment Id as trans-unit 1, but w/resource
		tu = FilterTestDriver.getTextUnit(filter,
			inputDocumentToTest("lqiTest.xlf", null), "UTF-8", locEN, locFR, 4);
		assertNotNull(tu);
		code = tu.getSource().getFirstContent().getCode(0);
		anns = code.getGenericAnnotations();
		assertNotNull(anns);
		list = anns.getAnnotations(GenericAnnotationType.LQI);
		assertEquals(2, list.size());
		assertEquals("comment7", list.get(0).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals("comment8", list.get(1).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals("mistranslation", list.get(1).getString(GenericAnnotationType.LQI_TYPE));
	}

	@Test
	public void testXLIFFITSLQIMapping() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\""
			+ " xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\""
			+ " version=\"1.2\" its:version=\"2.0\">\n"
			+ "  <file original=\"test2\" source-language=\"en\" target-language=\"fr\" datatype=\"xml\">\n"
			+ "    <body>\n"
			+ "      <trans-unit id=\"1\">\n"
			+ "        <source xml:lang=\"en-us\">test</source>\n"
			+ "        <target xml:lang=\"fr\">test</target>\n"
			+ "      </trans-unit>\n"
			+ "    </body>\n"
			+ "  </file>\n"
			+ "</xliff>";
		String expectedOutput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\""
			+ " xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\""
			+ " version=\"1.2\" its:version=\"2.0\">\n"
			+ "  <file original=\"test2\" source-language=\"en\" target-language=\"fr\" datatype=\"xml\">\n"
			+ "    <body>\n"
			+ "      <trans-unit id=\"1\"%s>\n"
			+ "        %s"
			+ "        %s"
			+ "      </trans-unit>\n"
			+ "    </body>\n"
			+ "  </file>%s\n"
			+ "</xliff>";
		List<Event> events = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);

		// Test adding LQI annotation
		ITSLQIAnnotations anns = new ITSLQIAnnotations();
		GenericAnnotation ann = new GenericAnnotation(GenericAnnotationType.LQI,
			GenericAnnotationType.LQI_COMMENT, "test",
			GenericAnnotationType.LQI_TYPE, "mistranslation");
		anns.add(ann);
		anns.setData("test1");
		tu.setAnnotation(anns);
		tu.setProperty(new Property(Property.ITS_LQI, " locQualityIssuesRef=\"#test1\""));
		assertEquals(String.format(expectedOutput,
			" locQualityIssuesRef=\"#test1\"",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n",
			"<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"test1\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"test\" locQualityIssueType=\"mistranslation\"/>\n"
			+ "</its:locQualityIssues>\n"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test editing the comment field of the LQI annotation
		ann.setString(GenericAnnotationType.LQI_COMMENT, "test edit");
		assertEquals(String.format(expectedOutput,
			" locQualityIssuesRef=\"#test1\"",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n",
			"<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"test1\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"test edit\" locQualityIssueType=\"mistranslation\"/>\n"
			+ "</its:locQualityIssues>\n"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test having LQI as attributes
		anns.setData(null);
		assertEquals(String.format(expectedOutput,
			" its:locQualityIssueComment=\"test edit\" its:locQualityIssueType=\"mistranslation\"",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n", ""),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
		anns.setData("test1");
		tu.setProperty(new Property(Property.ITS_LQI, " locQualityIssuesRef=\"#test1\""));

		// Test removing LQI annotations
		tu.setAnnotation(new ITSLQIAnnotations());
		tu.setProperty(new Property(Property.ITS_LQI, ""));
		assertEquals(String.format(expectedOutput, "",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n", ""),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test adding LQI annotation to source field
		tu.getSource().setAnnotation(anns);
		tu.getSource().setProperty(new Property(Property.ITS_LQI, " locQualityIssuesRef=\"#test1\""));
		assertEquals(String.format(expectedOutput, "",
			"<source xml:lang=\"en-us\" locQualityIssuesRef=\"#test1\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n",
			"<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"test1\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"test edit\" locQualityIssueType=\"mistranslation\"/>\n"
			+ "</its:locQualityIssues>\n"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test editing LQI annotation in source field
		ann.setString(GenericAnnotationType.LQI_COMMENT, "test source edit");
		assertEquals(String.format(expectedOutput, "",
			"<source xml:lang=\"en-us\" locQualityIssuesRef=\"#test1\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n",
			"<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"test1\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"test source edit\" locQualityIssueType=\"mistranslation\"/>\n"
			+ "</its:locQualityIssues>\n"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test removing LQI annotation from source field
		tu.getSource().setAnnotation(new ITSLQIAnnotations());
		tu.getSource().setProperty(new Property(Property.ITS_LQI, ""));
		assertEquals(String.format(expectedOutput, "",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n", ""),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test adding LQI annotation to target field
		tu.getTarget(locFR).setAnnotation(anns);
		tu.getTarget(locFR).setProperty(new Property(Property.ITS_LQI, " locQualityIssuesRef=\"#test1\""));
		assertEquals(String.format(expectedOutput, "",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\" locQualityIssuesRef=\"#test1\">test</target>\n",
			"<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"test1\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"test source edit\" locQualityIssueType=\"mistranslation\"/>\n"
			+ "</its:locQualityIssues>\n"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test editing LQI annotation in target field
		ann.setString(GenericAnnotationType.LQI_COMMENT, "test target edit");
		assertEquals(String.format(expectedOutput, "",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\" locQualityIssuesRef=\"#test1\">test</target>\n",
			"<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"test1\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"test target edit\" locQualityIssueType=\"mistranslation\"/>\n"
			+ "</its:locQualityIssues>\n"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test removing LQI annotation from target field
		tu.getTarget(locFR).setAnnotation(new ITSLQIAnnotations());
		tu.getTarget(locFR).setProperty(new Property(Property.ITS_LQI, ""));
		assertEquals(String.format(expectedOutput, "",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n", ""),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}
	
	@Test
	public void testXLIFFITSProvenance() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\""
			+ " xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\""
			+ " version=\"1.2\" its:version=\"2.0\">\n"
			+ "  <file original=\"test3\" source-language=\"en\" target-language=\"fr\" datatype=\"xml\">\n"
			+ "    <body>\n"
			+ "      <trans-unit id=\"1\">\n"
			+ "        <source xml:lang=\"en-us\">test</source>\n"
			+ "        <target xml:lang=\"fr\">test</target>\n"
			+ "      </trans-unit>\n"
			+ "    </body>\n"
			+ "  </file>\n"
			+ "</xliff>";
		String expectedOutput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\""
			+ " xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\""
			+ " version=\"1.2\" its:version=\"2.0\">\n"
			+ "  <file original=\"test3\" source-language=\"en\" target-language=\"fr\" datatype=\"xml\">\n"
			+ "    <body>\n"
			+ "      <trans-unit id=\"1\"%s>\n"
			+ "        %s"
			+ "        %s"
			+ "      </trans-unit>\n"
			+ "    </body>\n"
			+ "  </file>%s\n"
			+ "</xliff>";
		List<Event> events = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);

		// Test adding Prov annotation
		ITSProvenanceAnnotations anns = new ITSProvenanceAnnotations();
		GenericAnnotation ann = new GenericAnnotation(GenericAnnotationType.PROV,
			GenericAnnotationType.PROV_ORG, "okapi",
			GenericAnnotationType.PROV_TOOL, "okapi");
		anns.add(ann);
		anns.setData("test1");
		tu.setAnnotation(anns);
		tu.setProperty(new Property(Property.ITS_PROV, " provenanceRecordsRef=\"#test1\""));
		assertEquals(String.format(expectedOutput,
			" provenanceRecordsRef=\"#test1\"",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n",
			"<its:provenanceRecords xmlns:its=\""+Namespaces.ITS_NS_URI+"\" version=\"2.0\" xml:id=\"test1\">"
			+ "<its:provenanceRecord org=\"okapi\" tool=\"okapi\"/>"
			+ "</its:provenanceRecords>"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test editing the comment field of the Prov annotation
		ann.setString(GenericAnnotationType.PROV_TOOL, "okapi framework");
		assertEquals(String.format(expectedOutput,
			" provenanceRecordsRef=\"#test1\"",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n",
			"<its:provenanceRecords xmlns:its=\""+Namespaces.ITS_NS_URI+"\" version=\"2.0\" xml:id=\"test1\">"
			+ "<its:provenanceRecord org=\"okapi\" tool=\"okapi framework\"/>"
			+ "</its:provenanceRecords>"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test having Prov as attributes
		anns.setData(null);
		assertEquals(String.format(expectedOutput,
			" its:org=\"okapi\" its:tool=\"okapi framework\"",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n",
			""),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test removing Prov annotations
		tu.setAnnotation(new ITSProvenanceAnnotations());
		tu.setProperty(new Property(Property.ITS_PROV, ""));
		assertEquals(String.format(expectedOutput, "",
			"<source xml:lang=\"en-us\">test</source>\n",
			"<target xml:lang=\"fr\">test</target>\n", ""),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testXLIFFITSProvenanceFile() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\""
			+ " xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\""
			+ " version=\"1.2\" its:version=\"2.0\">\n"
			+ "  <file original=\"test4\" source-language=\"en\" target-language=\"fr\" datatype=\"xml\">\n"
			+ "    <body>\n"
			+ "      <trans-unit id=\"1\">\n"
			+ "        <source xml:lang=\"en-us\">test</source>\n"
			+ "        <target xml:lang=\"fr\">test</target>\n"
			+ "      </trans-unit>\n"
			+ "    </body>\n"
			+ "  </file>\n"
			+ "</xliff>";
		String expectedOutput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\""
			+ " xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\""
			+ " version=\"1.2\" its:version=\"2.0\">\n"
			+ "  <file original=\"test4\" source-language=\"en\" target-language=\"fr\" datatype=\"xml\"%s>\n"
			+ "    <body>\n"
			+ "      <trans-unit id=\"1\">\n"
			+ "        <source xml:lang=\"en-us\">test</source>\n"
			+ "        <target xml:lang=\"fr\">test</target>\n"
			+ "      </trans-unit>\n"
			+ "    </body>\n"
			+ "  </file>%s\n"
			+ "</xliff>";
		List<Event> events = getEvents(snippet);
		StartSubDocument startSubDoc = FilterTestDriver.getStartSubDocument(events, 1);
		assertNotNull(startSubDoc);

		// Test adding Prov annotation
		ITSProvenanceAnnotations anns = new ITSProvenanceAnnotations();
		GenericAnnotation ann = new GenericAnnotation(GenericAnnotationType.PROV,
			GenericAnnotationType.PROV_ORG, "okapi",
			GenericAnnotationType.PROV_TOOL, "okapi");
		anns.add(ann);
		anns.setData("test1");
		startSubDoc.setAnnotation(anns);
		startSubDoc.setProperty(new Property(Property.ITS_PROV, " provenanceRecordsRef=\"#test1\""));
		assertEquals(String.format(expectedOutput,
			" provenanceRecordsRef=\"#test1\"",
			"<its:provenanceRecords xmlns:its=\"" + Namespaces.ITS_NS_URI + "\" version=\"2.0\" xml:id=\"test1\">"
			+ "<its:provenanceRecord org=\"okapi\" tool=\"okapi\"/>"
			+ "</its:provenanceRecords>"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test editing the comment field of the Prov annotation
		ann.setString(GenericAnnotationType.PROV_TOOL, "okapi framework");
		assertEquals(String.format(expectedOutput,
			" provenanceRecordsRef=\"#test1\"",
			"<its:provenanceRecords xmlns:its=\"" + Namespaces.ITS_NS_URI + "\" version=\"2.0\" xml:id=\"test1\">"
			+ "<its:provenanceRecord org=\"okapi\" tool=\"okapi framework\"/>"
			+ "</its:provenanceRecords>"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test removing Prov annotations
		startSubDoc.setAnnotation(new ITSProvenanceAnnotations());
		startSubDoc.getProperty(Property.ITS_PROV).setValue("");
		assertEquals(String.format(expectedOutput, "", ""),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testXLIFFITSProvenanceGroup() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\""
			+ " xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\""
			+ " version=\"1.2\" its:version=\"2.0\">\n"
			+ "  <file original=\"test\" source-language=\"en\" target-language=\"fr\" datatype=\"xml\">\n"
			+ "    <body>\n"
			+ "      <group>\n"
			+ "        <trans-unit id=\"1\">\n"
			+ "          <source xml:lang=\"en-us\">test</source>\n"
			+ "          <target xml:lang=\"fr\">test</target>\n"
			+ "        </trans-unit>\n"
			+ "      </group>\n"
			+ "    </body>\n"
			+ "  </file>\n"
			+ "</xliff>";
		String expectedOutput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\""
			+ " xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\""
			+ " version=\"1.2\" its:version=\"2.0\">\n"
			+ "  <file original=\"test\" source-language=\"en\" target-language=\"fr\" datatype=\"xml\">\n"
			+ "    <body>\n"
			+ "      <group%s>\n"
			+ "        <trans-unit id=\"1\">\n"
			+ "          <source xml:lang=\"en-us\">test</source>\n"
			+ "          <target xml:lang=\"fr\">test</target>\n"
			+ "        </trans-unit>\n"
			+ "      </group>\n"
			+ "    </body>\n"
			+ "  </file>%s\n"
			+ "</xliff>";
		List<Event> events = getEvents(snippet);
		StartGroup group = FilterTestDriver.getGroup(events, 1);
		assertNotNull(group);

		// Test adding Prov annotation
		ITSProvenanceAnnotations anns = new ITSProvenanceAnnotations();
		GenericAnnotation ann = new GenericAnnotation(GenericAnnotationType.PROV,
			GenericAnnotationType.PROV_ORG, "okapi",
			GenericAnnotationType.PROV_TOOL, "okapi");
		anns.add(ann);
		anns.setData("test");
		group.setAnnotation(anns);
		group.setProperty(new Property(Property.ITS_PROV, " provenanceRecordsRef=\"#test\""));
		assertEquals(String.format(expectedOutput,
			" provenanceRecordsRef=\"#test\"",
			"<its:provenanceRecords xmlns:its=\"" + Namespaces.ITS_NS_URI + "\" version=\"2.0\" xml:id=\"test\">"
			+ "<its:provenanceRecord org=\"okapi\" tool=\"okapi\"/>"
			+ "</its:provenanceRecords>"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test editing the comment field of the Prov annotation
		ann.setString(GenericAnnotationType.PROV_TOOL, "okapi framework");
		assertEquals(String.format(expectedOutput,
			" provenanceRecordsRef=\"#test\"",
			"<its:provenanceRecords xmlns:its=\"" + Namespaces.ITS_NS_URI + "\" version=\"2.0\" xml:id=\"test\">"
			+ "<its:provenanceRecord org=\"okapi\" tool=\"okapi framework\"/>"
			+ "</its:provenanceRecords>"),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));

		// Test removing Prov annotations
		group.setAnnotation(new ITSProvenanceAnnotations());
		group.setProperty(new Property(Property.ITS_PROV, ""));
		assertEquals(String.format(expectedOutput, "", ""),
			FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testITSStandoffManager() throws XMLStreamException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader reader = inputFactory.createXMLEventReader(
			new ByteArrayInputStream(("<?xml version=\"1.0\"?>"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">"
			+ "<file original=\"test\" source-language=\"en\" target-language=\"fr\" datatype=\"xml\">\n"
			+ "    <body>\n"
			+ "      <group>\n"
			+ "        <trans-unit id=\"1\" locQualityIssuesRef=\"#lqi1\" provenanceRecordsRef=\"#prov1\">\n"
			+ "          <source xml:lang=\"en-us\">test</source>\n"
			+ "          <target xml:lang=\"fr\">test</target>\n"
			+ "        </trans-unit>\n"
			+ "      </group>\n"
			+ "    </body>\n"
			+ "</file>"
			+ "<its:locQualityIssues xml:id=\"lqi1\">"
			+ "    <its:locQualityIssue locQualityIssueType=\"non-conformance\" locQualityIssueSeverity=\"100\"/>"
			+ "    <its:locQualityIssue locQualityIssueType=\"mistranslation\" locQualityIssueSeverity=\"70\"/>"
			+ "</its:locQualityIssues>"
			+ "<its:provenanceRecords xml:id=\"prov1\">"
			+ "    <its:provenanceRecord org=\"okapi\" tool=\"okapi\"/>"
			+ "</its:provenanceRecords>"
			+ "</xliff>").getBytes()));
		ITSStandoffManager itsManager = new ITSStandoffManager();
		itsManager.parseXLIFF(reader, "", "UTF-8");
		assertTrue(itsManager.getStoredLQIRefs().size() == 1);
		assertTrue(itsManager.getStoredProvRefs().size() == 1);

		GenericAnnotations anns = new GenericAnnotations();
		itsManager.addLQIAnnotation(anns, "#lqi1");
		itsManager.addProvAnnotation(anns, "#prov1");
		List<GenericAnnotation> lqiList = anns.getAnnotations(GenericAnnotationType.LQI);

		for (GenericAnnotation ann : lqiList) {
			String lqiType = ann.getString(GenericAnnotationType.LQI_TYPE);
			assertTrue(lqiType.equals("non-conformance") || lqiType.equals("mistranslation"));
			if (lqiType.equals("non-conformance")) {
				assertTrue(100 == ann.getDouble(GenericAnnotationType.LQI_SEVERITY));
			} else {
				assertTrue(70 == ann.getDouble(GenericAnnotationType.LQI_SEVERITY));
			}
		}
		List<GenericAnnotation> provList = anns.getAnnotations(GenericAnnotationType.PROV);
		for (GenericAnnotation ann : provList) {
			assertEquals("okapi", ann.getString(GenericAnnotationType.PROV_ORG));
			assertEquals("okapi", ann.getString(GenericAnnotationType.PROV_TOOL));
		}
	}

	@Test
	public void testLQIAndProvModifications1 () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source its:locQualityIssueComment=\"lqi1-comment\" its:person=\"Jim\" its:revOrg=\"abc\" its:toolRef=\"uri\">text1</source>\n"
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"2\">\n"
			+ "<source>text2</source>\n"
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"3\">\n"
			+ "<source its:locQualityIssueComment=\"lqi3-comment\">text3</source>\n"
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file>\n"
			+ "</xliff>";
		ArrayList<Event> events = getEvents(snippet);
		
		for ( Event event : events ) {
			if ( event.isTextUnit() ) {
				if ( event.getTextUnit().getId().equals("1") ) {
					// Check the original annotation is there
					TextContainer tc = event.getTextUnit().getSource();
					ITSLQIAnnotations anns = tc.getAnnotation(ITSLQIAnnotations.class);
					assertNotNull(anns);
					assertEquals("lqi1-comment",
						anns.getFirstAnnotation(GenericAnnotationType.LQI).getString(GenericAnnotationType.LQI_COMMENT));
					// Add a new LQ issue
					GenericAnnotation ann = new GenericAnnotation(GenericAnnotationType.LQI,
						GenericAnnotationType.LQI_COMMENT, "lqi2-comment");
					anns.setData("myID");
					tc.setProperty(new Property(Property.ITS_LQI,
						" its:locQualityIssuesRef=\"#myID\""));
					anns.add(ann);
					// Provenance
					ITSProvenanceAnnotations panns = tc.getAnnotation(ITSProvenanceAnnotations.class);
					assertEquals("Jim",
						panns.getFirstAnnotation(GenericAnnotationType.PROV).getString(GenericAnnotationType.PROV_PERSON));
					assertEquals(GenericAnnotationType.REF_PREFIX+"uri",
						panns.getFirstAnnotation(GenericAnnotationType.PROV).getString(GenericAnnotationType.PROV_TOOL));
					assertEquals("abc",
						panns.getFirstAnnotation(GenericAnnotationType.PROV).getString(GenericAnnotationType.PROV_REVORG));
				}
				else if ( event.getTextUnit().getId().equals("2") ) {
					TextContainer tc = event.getTextUnit().getSource();
					// Add two LQ issues (original has none)
					ITSLQIAnnotations.addAnnotations(tc,
						new GenericAnnotation(GenericAnnotationType.LQI, GenericAnnotationType.LQI_COMMENT, "issue1"));
					ITSLQIAnnotations.addAnnotations(tc,
						new GenericAnnotation(GenericAnnotationType.LQI, GenericAnnotationType.LQI_COMMENT, "issue2"));
				}
				else if ( event.getTextUnit().getId().equals("3") ) {
					TextContainer tc = event.getTextUnit().getSource();
					ITSLQIAnnotations anns = tc.getAnnotation(ITSLQIAnnotations.class);
					GenericAnnotation ga = anns.getFirstAnnotation(GenericAnnotationType.LQI);
					assertEquals("lqi3-comment", ga.getString(GenericAnnotationType.LQI_COMMENT));
					// Update a single existing LQI
					ga.setString(GenericAnnotationType.LQI_COMMENT, "newComment3");
					ga.setDouble(GenericAnnotationType.LQI_SEVERITY, 1.234);
				}
			}
		}
		// Check the output
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			// Existing LQI entry is updated
			+ "<source its:locQualityIssuesRef=\"#VARID\" its:person=\"Jim\" its:toolRef=\"uri\" its:revOrg=\"abc\">text1</source>\n"
			+ "<target xml:lang=\"fr\">text1</target>\n"
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"2\">\n"
			+ "<source its:locQualityIssuesRef=\"#VARID\">text2</source>\n"
			+ "<target xml:lang=\"fr\">text2</target>\n"
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"3\">\n"
			+ "<source its:locQualityIssueComment=\"newComment3\" its:locQualityIssueSeverity=\"1.234\">text3</source>\n"
			+ "<target xml:lang=\"fr\">text3</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file>"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"VARID\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"issue1\"/>\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"issue2\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"VARID\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"lqi1-comment\"/>\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"lqi2-comment\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "\n" //TODO: This should be removed
			+ "</xliff>";
		assertEquals(expected, stripVariableID(
			FilterTestDriver.generateOutput(events,
				locFR, filter.createSkeletonWriter(), filter.getEncoderManager())));
	}

	@Test
	public void testAddLQIModifications2 () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source its:locQualityIssueComment=\"lqi1-comment\">text1</source>\n"
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"2\">\n"
			+ "<source its:locQualityIssuesRef=\"#AAA\">text2</source>\n"
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file>\n"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"AAA\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"tu2-issue1\"/>\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"tu2-issue2\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "</xliff>";
		ArrayList<Event> events = getEvents(snippet);
		
		for ( Event event : events ) {
			if ( event.isTextUnit() ) {
				if ( event.getTextUnit().getId().equals("2") ) {
					TextContainer tc = event.getTextUnit().getSource();
					ITSLQIAnnotations anns = tc.getAnnotation(ITSLQIAnnotations.class);
					List<GenericAnnotation> list = anns.getAnnotations(GenericAnnotationType.LQI);
					assertEquals("tu2-issue1", list.get(0).getString(GenericAnnotationType.LQI_COMMENT));
					assertEquals("tu2-issue2", list.get(1).getString(GenericAnnotationType.LQI_COMMENT));
					// Replace the two issues by a single new one
					anns.remove(list.get(0));
					anns.remove(list.get(1));
					anns.add(new GenericAnnotation(GenericAnnotationType.LQI,
						GenericAnnotationType.LQI_COMMENT, "newComment2"));
				}
			}
		}
		// Check the output
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source its:locQualityIssueComment=\"lqi1-comment\">text1</source>\n"
			+ "<target xml:lang=\"fr\">text1</target>\n"
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"2\">\n"
			+ "<source its:locQualityIssuesRef=\"#VARID\">text2</source>\n"
			+ "<target xml:lang=\"fr\">text2</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file>"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"VARID\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"newComment2\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "\n"
			+ "</xliff>";
		assertEquals(expected, stripVariableID(
			FilterTestDriver.generateOutput(events,
				locFR, filter.createSkeletonWriter(), filter.getEncoderManager())));
	}

	@Test
	public void testLQIRemoval () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\" its:locQualityIssueComment=\"tuLqi\">\n"
			+ "<source its:locQualityIssueComment=\"lqi1-comment\">text1</source>\n"
			+ "<target xml:lang=\"fr\" its:locQualityIssueComment=\"lqi1-TRG\">text1</target>\n"
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"2\">\n"
			+ "<source its:locQualityIssuesRef=\"#lqi2\">text2</source>\n"
			+ "<target xml:lang=\"fr\"><mrk mtype=\"x-its\" its:locQualityIssueComment=\"lqimrk\">text2</mrk></target>\n"			
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file>\n"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"lqi2\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"tu2-issue1\"/>\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"tu2-issue2\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "</xliff>";
		ArrayList<Event> events = getEvents(snippet);
		
		for ( Event event : events ) {
			if ( event.isTextUnit() ) {
				if ( event.getTextUnit().getId().equals("1") ) {
					Iterator<IAnnotation> iter = event.getTextUnit().getAnnotations().iterator();
					while ( iter.hasNext() ) {
						iter.next();
						iter.remove();
					}
					event.getTextUnit().getSource().getAnnotations().clear();
					event.getTextUnit().getTarget(locFR).getAnnotations().clear();
				}
				else if ( event.getTextUnit().getId().equals("2") ) {
					event.getTextUnit().getSource().getAnnotations().clear();
					TextContainer tc = event.getTextUnit().getTarget(locFR);
					List<Code> codes = tc.getFirstContent().getCodes();
					codes.get(0).getGenericAnnotations().clear();
					codes.get(1).getGenericAnnotations().clear();
				}
			}
		}
		// Check the output
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source>text1</source>\n"
			+ "<target xml:lang=\"fr\">text1</target>\n"
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"2\">\n"
			+ "<source>text2</source>\n"
			//TODO: improvement: don't output the mrk if there is no annotation.
			+ "<target xml:lang=\"fr\"><mrk mtype=\"x-its\">text2</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file>\n"
			+ "</xliff>";
		assertEquals(expected, stripVariableID(
			FilterTestDriver.generateOutput(events,
				locFR, filter.createSkeletonWriter(), filter.getEncoderManager())));
	}

	@Test
	public void testLocNoteModification () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source>en1</source>\n"
			+ "<target xml:lang=\"fr\"><mrk comment='c1'>A</mrk>"
				+ "<mrk mtype=\"abbrev\" comment='c2'>B</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body></file></xliff>";
		ArrayList<Event> events = getEvents(snippet);
		
		for ( Event event : events ) {
			if ( event.isTextUnit() ) {
				if ( event.getTextUnit().getId().equals("1") ) {
					TextFragment tf = event.getTextUnit().getTarget(locFR).getFirstContent();
					List<Code> codes = tf.getCodes();
					// Modify the value
					GenericAnnotation ga = codes.get(0).getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.LOCNOTE);
					assertEquals("c1", ga.getString(GenericAnnotationType.LOCNOTE_VALUE));
					ga.setString(GenericAnnotationType.LOCNOTE_VALUE, "z1");
					// Delete the LOCNOTE annotation, leave the other info
					ga = codes.get(2).getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.LOCNOTE);
					assertEquals("c2", ga.getString(GenericAnnotationType.LOCNOTE_VALUE));
					codes.get(2).getGenericAnnotations().remove(ga);
					// Add a new note
					tf.append("C"); // -> ##A####B##C
					tf.annotate(10, 11, GenericAnnotationType.GENERIC,
						new GenericAnnotations(
							new GenericAnnotation(GenericAnnotationType.LOCNOTE,
								GenericAnnotationType.LOCNOTE_VALUE, "nc1",
								GenericAnnotationType.LOCNOTE_TYPE, "description")));
					assertEquals("<1>A</1><2>B</2><3>C</3>", fmt.setContent(tf).toString());
				}
			}
		}
		// Check the output
//TODO: itsxlf namespace is not declared		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source>en1</source>\n"
			+ "<target xml:lang=\"fr\"><mrk comment=\"z1\">A</mrk>"
				+ "<mrk mtype=\"abbrev\">B</mrk>"
				+ "<mrk comment=\"nc1\" itsxlf:locNoteType=\"description\" mtype=\"x-its\">C</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body></file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(events,
				locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}

	@Test
	public void testXmlLangModification () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source>en1</source>\n"
			+ "<target xml:lang=\"fr\"><mrk xml:lang=\"es\" mtype='x-its'>A</mrk>"
				+ "<mrk mtype=\"abbrev\" xml:lang=\"de\">B</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body></file></xliff>";
		ArrayList<Event> events = getEvents(snippet);
		
		for ( Event event : events ) {
			if ( event.isTextUnit() ) {
				if ( event.getTextUnit().getId().equals("1") ) {
					TextFragment tf = event.getTextUnit().getTarget(locFR).getFirstContent();
					List<Code> codes = tf.getCodes();
					// Modify the value
					GenericAnnotation ga = codes.get(0).getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.LANG);
					assertEquals("es", ga.getString(GenericAnnotationType.LANG_VALUE));
					ga.setString(GenericAnnotationType.LANG_VALUE, "pl");
					// Delete the LANG annotation, leave the other info
					ga = codes.get(2).getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.LANG);
					assertEquals("de", ga.getString(GenericAnnotationType.LANG_VALUE));
					codes.get(2).getGenericAnnotations().remove(ga);
					// Add a new language info
					tf.append("C"); // -> ##A####B##C
					tf.annotate(10, 11, GenericAnnotationType.GENERIC,
						new GenericAnnotations(
							new GenericAnnotation(GenericAnnotationType.LANG,
								GenericAnnotationType.LANG_VALUE, "ja")));
					assertEquals("<1>A</1><2>B</2><3>C</3>", fmt.setContent(tf).toString());
				}
			}
		}
		// Check the output
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source>en1</source>\n"
			+ "<target xml:lang=\"fr\"><mrk mtype=\"x-its\" xml:lang=\"pl\">A</mrk>"
				+ "<mrk mtype=\"abbrev\">B</mrk>"
				+ "<mrk xml:lang=\"ja\" mtype=\"x-its\">C</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body></file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(events,
				locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}
	
	@Test
	public void testAllowedCharsModification () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source its:allowedCharacters=\"[a-z]\">en</source>\n"
			+ "<target xml:lang=\"fr\"><mrk its:allowedCharacters=\"[A-Z]\" mtype='x-its'>A</mrk>"
				+ "<mrk mtype=\"abbrev\" its:allowedCharacters=\"[BC]\">B</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body></file></xliff>";
		ArrayList<Event> events = getEvents(snippet);
		
		for ( Event event : events ) {
			if ( event.isTextUnit() ) {
				if ( event.getTextUnit().getId().equals("1") ) {
					TextFragment tf = event.getTextUnit().getTarget(locFR).getFirstContent();
					List<Code> codes = tf.getCodes();
					// Modify the value
					GenericAnnotation ga = codes.get(0).getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.ALLOWEDCHARS);
					assertEquals("[A-Z]", ga.getString(GenericAnnotationType.ALLOWEDCHARS_VALUE));
					ga.setString(GenericAnnotationType.ALLOWEDCHARS_VALUE, "[abc]");
					// Delete the annotation, leave the other info
					ga = codes.get(2).getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.ALLOWEDCHARS);
					assertEquals("[BC]", ga.getString(GenericAnnotationType.ALLOWEDCHARS_VALUE));
					codes.get(2).getGenericAnnotations().remove(ga);
					// Add a new language info
					tf.append("C"); // -> ##A####B##C
					tf.annotate(10, 11, GenericAnnotationType.GENERIC,
						new GenericAnnotations(
							new GenericAnnotation(GenericAnnotationType.ALLOWEDCHARS,
								GenericAnnotationType.ALLOWEDCHARS_VALUE, "[x-z]")));
					assertEquals("<1>A</1><2>B</2><3>C</3>", fmt.setContent(tf).toString());
				}
			}
		}
		// Check the output
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source its:allowedCharacters=\"[a-z]\">en</source>\n"
			+ "<target xml:lang=\"fr\"><mrk mtype=\"x-its\" its:allowedCharacters=\"[abc]\">A</mrk>"
				+ "<mrk mtype=\"abbrev\">B</mrk>"
				+ "<mrk its:allowedCharacters=\"[x-z]\" mtype=\"x-its\">C</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body></file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(events,
				locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}
	
	@Test
	public void testStorageSizeModification () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source its:storageSize=\"100\">en</source>\n"
			+ "<target xml:lang=\"fr\"><mrk its:storageSize=\"1\" mtype='x-its'>A</mrk>"
				+ "<mrk mtype=\"abbrev\" its:storageSize=\"2\">B</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body></file></xliff>";
		ArrayList<Event> events = getEvents(snippet);
		
		for ( Event event : events ) {
			if ( event.isTextUnit() ) {
				if ( event.getTextUnit().getId().equals("1") ) {
					TextFragment tf = event.getTextUnit().getTarget(locFR).getFirstContent();
					List<Code> codes = tf.getCodes();
					// Modify the value
					GenericAnnotation ga = codes.get(0).getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.STORAGESIZE);
					assertEquals(1, (int)ga.getInteger(GenericAnnotationType.STORAGESIZE_SIZE));
					ga.setInteger(GenericAnnotationType.STORAGESIZE_SIZE, 11);
					// Delete the annotation, leave the other info
					ga = codes.get(2).getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.STORAGESIZE);
					assertEquals(2, (int)ga.getInteger(GenericAnnotationType.STORAGESIZE_SIZE));
					codes.get(2).getGenericAnnotations().remove(ga);
					// Add a new language info
					tf.append("C"); // -> ##A####B##C
					tf.annotate(10, 11, GenericAnnotationType.GENERIC,
						new GenericAnnotations(
							new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
								GenericAnnotationType.STORAGESIZE_SIZE, 33)));
					assertEquals("<1>A</1><2>B</2><3>C</3>", fmt.setContent(tf).toString());
				}
			}
		}
		// Check the output
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.2\" its:version=\"2.0\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source its:storageSize=\"100\">en</source>\n"
			+ "<target xml:lang=\"fr\"><mrk mtype=\"x-its\" its:storageSize=\"11\">A</mrk>"
				+ "<mrk mtype=\"abbrev\">B</mrk>"
				+ "<mrk its:storageSize=\"33\" mtype=\"x-its\">C</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body></file></xliff>";
		assertEquals(expected, FilterTestDriver.generateOutput(events,
				locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}
	
	@Test
	public void testAddAltTrans () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns:okp=\"okapi-framework:xliff-extensions\" version=\"1.2\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\"><source>t1</source>\n"
			+ "<target>translated t1</target>\n"
			+ "<alt-trans match-quality=\"50\" origin=\"orig\" okp:matchType=\"FUZZY\" okp:engine=\"abc\">\n"
			+ "<source>alt source t1</source>\n"
			+ "<target>alt target t1</target>\n"
			+ "</alt-trans>\n"
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file></xliff>";
		
		ArrayList<Event> events = getEvents(snippet);
		for ( Event event : events ) {
			if ( event.isTextUnit() ) {
				AltTranslationsAnnotation anns = event.getTextUnit().getTarget(locFR).getAnnotation(AltTranslationsAnnotation.class);
				assertNotNull(anns);
				TextFragment oriSrcTf = new TextFragment("t1");
				TextFragment altTrgTf = new TextFragment("second alt target t1");
				anns.add(new AltTranslation(locEN, locFR, oriSrcTf, null, altTrgTf, MatchType.UKNOWN, 0, "WBT"));
			}
		}
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns:okp=\"okapi-framework:xliff-extensions\" version=\"1.2\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\"><source>t1</source>\n"
			+ "<target>translated t1</target>\n"
			// Added entry is placed in front of the existing one because 
			// the marker is generated immediately after we read the main target/source
			// Ideally we should place the marker after existing alt-trans entries
			+ "<alt-trans origin=\"WBT\">\n"
			+ "<target xml:lang=\"fr\">second alt target t1</target>\n"
			+ "</alt-trans>\n"
			+ "<alt-trans match-quality=\"50\" origin=\"orig\" okp:matchType=\"FUZZY\" okp:engine=\"abc\">\n"
			+ "<source>alt source t1</source>\n"
			+ "<target>alt target t1</target>\n"
			+ "</alt-trans>\n"
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file></xliff>";

		try (XLIFFFilter filterAllowingNewAlt = new XLIFFFilter()) {
			filterAllowingNewAlt.getParameters().setAddAltTrans(true);

			assertEquals(expected,
				FilterTestDriver.generateOutput(
					events, locFR, filterAllowingNewAlt.createSkeletonWriter(), filterAllowingNewAlt.getEncoderManager()));
		}
	}
	
	@Test
	public void testAllowEmptyTargets() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<xliff version=\"1.2\">"
				+ "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\">"
				+ "<body><trans-unit id=\"1\" resname=\"13\"><source>Original</source>"
				+ "<target></target></trans-unit></body>"
				+ "</file></xliff>";
		
			String expected1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<xliff version=\"1.2\">"
				+ "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\" target-language=\"fr\">"
				+ "<body><trans-unit id=\"1\" resname=\"13\"><source>Original</source>"
				+ "<target>Original</target></trans-unit></body>"
				+ "</file></xliff>";
			
			String expected2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<xliff version=\"1.2\">"
					+ "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\" target-language=\"fr\">"
					+ "<body><trans-unit id=\"1\" resname=\"13\"><source>Original</source>"
					+ "<target></target></trans-unit></body>"
					+ "</file></xliff>";
			
			Parameters params = (Parameters)filter.getParameters();
			
			params.setAllowEmptyTargets(false);
			assertEquals(expected1, FilterTestDriver.generateOutput(getEvents(snippet),
				locEN, filter.createSkeletonWriter(), filter.getEncoderManager()));
			
			params.setAllowEmptyTargets(true);
			assertEquals(expected2, FilterTestDriver.generateOutput(getEvents(snippet),
				locEN, filter.createSkeletonWriter(), filter.getEncoderManager()));
			
			params.setAllowEmptyTargets(false);
			assertEquals(expected1, FilterTestDriver.generateOutput(getEvents(snippet),
				locEN, filter.createSkeletonWriter(), filter.getEncoderManager()));	
	}
	
	@Test
	public void testEmptyTargetCondition() {
			// XLIFFSkeletonWriter:371
		
			TextContainer trgCont = null;
			assertTrue(trgCont == null || trgCont.isEmpty());
			
			trgCont = new TextContainer();
			assertTrue(trgCont == null || trgCont.isEmpty());				
			
			trgCont = new TextContainer("text");
			assertFalse(trgCont == null || trgCont.isEmpty());
	}
	
	@Test
	public void testAlwaysUseSegSource() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<xliff version=\"1.2\">"
				+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
				+ "<body>"
				+ "<trans-unit id=\"1\">"
				+ "<source>s1.s2.</source>"
				+ "<seg-source>  <mrk mid=\"1\" mtype=\"seg\">s1.</mrk>\n<mrk mid=\"2\" mtype=\"seg\">s2.</mrk></seg-source>"
				+ "<target>  <mrk mid=\"1\" mtype=\"seg\">t1.</mrk>\n<mrk mid=\"2\" mtype=\"seg\">t2.</mrk></target>"
				+ "</trans-unit>"
				+ "<trans-unit id=\"2\" xml:space=\"preserve\">"
				+ "<source>s1.s2.</source>"
				+ "<seg-source>  <mrk mid=\"1\" mtype=\"seg\">s1.</mrk>\n<mrk mid=\"2\" mtype=\"seg\">s2.</mrk></seg-source>"
				+ "<target>  <mrk mid=\"1\" mtype=\"seg\">t1.</mrk>\n<mrk mid=\"2\" mtype=\"seg\">t2.</mrk></target>"
				+ "</trans-unit>" + "</body>" + "</file></xliff>";

		XLIFFFilter f = new XLIFFFilter();
		Parameters p = (Parameters)f.getParameters();
		p.setAlwaysUseSegSource(true);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, f), 1);
		assertNotNull(tu);
		ISegments srcSegs = tu.getSourceSegments();
		assertEquals(2, srcSegs.count());
		TextContainer cont = tu.getSource();
		assertEquals("[s1.] [s2.]", fmt.printSegmentedContent(cont, true));
		cont = tu.getTarget(locFR);
		assertEquals("[t1.] [t2.]", fmt.printSegmentedContent(cont, true));

		tu = FilterTestDriver.getTextUnit(getEvents(snippet, f), 2);
		srcSegs = tu.getSourceSegments();
		assertEquals(2, srcSegs.count());
		cont = tu.getSource();
		assertEquals("  [s1.]\n[s2.]", fmt.printSegmentedContent(cont, true));
		cont = tu.getTarget(locFR);
		assertEquals("  [t1.]\n[t2.]", fmt.printSegmentedContent(cont, true));
	}

	@Test
	public void testElementsInHeader () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<header>\n"
			+ "<phase-group>\n"
			+ "<phase phase-name=\"T1\" process-name=\"P1\"></phase>\n"
			+ "<phase phase-name=\"T2\" process-name=\"P2\"></phase>\n"
			+ "</phase-group>\n"
			+ "<note>note</note>"
			+ "<my:elem xmlns:my=\"myNS\"><my:note>data</my:note></my:elem>\n"
			+ "</header>\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source>text</source>\n"
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file></xliff>";
		
		List<Event> events = getEvents(snippet);

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\">\n"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">\n"
			+ "<header>"
			+ "<phase-group>"
			+ "<phase phase-name=\"T1\" process-name=\"P1\"></phase>"
			+ "<phase phase-name=\"T2\" process-name=\"P2\"></phase>"
			+ "</phase-group>\n\n"
			+ "<note>note</note>"
			+ "<my:elem xmlns:my=\"myNS\"><my:note>data</my:note></my:elem>\n"
			+ "</header>\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source>text</source>\n"
			+ "<target xml:lang=\"fr\">text</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n"
			+ "</file></xliff>";
		
		assertEquals(expected, FilterTestDriver.generateOutput(events,
			locFR, altTransFilter.createSkeletonWriter(), altTransFilter.getEncoderManager()));
	}
	
	@Test
	public void testSdlTagDefs () {
		RawDocument rd = new RawDocument(location.in("/sdlxliff/okapi_intro_test15977.sdlxliff").asUri(),
			"UTF-8", locEN, locFR);
		List<Event> events = FilterTestDriver.getEvents(filter, rd, null);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		Code code = tu.getSource().getFirstContent().getCode(0);
		
		tu = FilterTestDriver.getTextUnit(events, 15);
		assertNotNull(tu);
		code = tu.getSource().getFirstContent().getCode(0);
		assertEquals("x-link;", code.getType());
		assertEquals(TextFragment.TagType.OPENING, code.getTagType());
		assertEquals("<a href=\"index.html\">", code.getData());
		
		code = tu.getSource().getParts().get(2).text.getCode(0);
		assertEquals("x-link;", code.getType());
		assertEquals(TextFragment.TagType.CLOSING, code.getTagType());
		assertEquals("</a>", code.getData());
		
		tu = FilterTestDriver.getTextUnit(events, 67);
		assertNotNull(tu);
	}
	
	@Test
	public void testSdlTagDefsWithSubs() {
		RawDocument rd = new RawDocument(location.in("/sdlxliff/burlington_ufo_center16000.sdlxliff").asUri(),
			"UTF-8", locEN, locFR);
		List<Event> events = FilterTestDriver.getEvents(filter, rd, null);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 10);
		assertNotNull(tu);
		Code code = tu.getSource().getFirstContent().getCode(1);		
		assertNotNull(tu);		
		assertEquals("", code.getType());
		assertEquals(TextFragment.TagType.PLACEHOLDER, code.getTagType());		
	}
	
	@Test
	public void testDoubleExtractionSdlXliff () throws IOException {		
		final LocaleId esEs = new LocaleId("es", "ES");
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(inputDocumentToTest("sdlxliff/okapi_intro_test15977.sdlxliff", "sdl.fprm"));
		RoundTripComparison rtc = new RoundTripComparison();
		XLIFFFilter f = new XLIFFFilter();
		Parameters p = new Parameters();
		p.fromString(StreamUtil.streamUtf8AsString(XLIFFFilterTest.class.getResourceAsStream("sdl.fprm")));
		f.setParameters(p); // params includes line "sdlSegConfValue=Translated"
		assertTrue(rtc.executeCompare(f, list, "UTF-8", locEN, esEs));
		
		// test for updated SDL status
		IPipelineStep s = new BasePipelineStep() {

			@Override
			public String getName() {
				return "Convert SDL Status";
			}

			@Override
			protected Event handleTextUnit(Event event) {
				ITextUnit tu = event.getTextUnit();
				TextContainer tc = tu.getTarget(esEs);
				if (tc != null) { 
					Property locked = tc.getProperty(SdlXliffSkeletonWriter.PROP_SDL_LOCKED);
					if (locked != null) locked.setValue("true");
					// Remove properties added during parsing to force config 'sdlSegConfValue' to be used for conf
					tc.removeProperty(Property.STATE);
					tc.removeProperty(SdlXliffSkeletonWriter.PROP_ORIG_SDL_SEG_CONF);
				}
				return super.handleTextUnit(event);
			}

			@Override
			public String getDescription() {
				return "Convert SDL Status";
			}
			
			 
			
		};
		String outPath = rtc.executeFirstExtractionToFile(
				inputDocumentToTest("sdlxliff/okapi_intro_test15977.sdlxliff", "sdl.fprm"), "sdl_status_out", s);
		FileInputStream fis = new FileInputStream(outPath);
		String o = StreamUtil.streamUtf8AsString(fis);		
		assertTrue(RegexUtil.contains(o, RegexUtil.getPattern("conf=\"Translated\"")));
		assertTrue(!RegexUtil.contains(o, RegexUtil.getPattern("conf=\"Draft\"")));
		
		assertTrue(RegexUtil.contains(o, RegexUtil.getPattern("locked=\"true\"")));
		assertTrue(!RegexUtil.contains(o, RegexUtil.getPattern("locked=\"false\"")));
		fis.close();
	}
	
	
	@Test
	public void testDoubleExtractionSdlXliffAll() throws URISyntaxException {
		final LocaleId esEs = new LocaleId("es", "ES");
		final LocaleId enUs = new LocaleId("en", "US");		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		for (String file : getSdlXliffFiles()) {
			list.add(inputDocumentToTest("sdlxliff/" + file, "sdl.fprm"));
		}
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", enUs, esEs));
	}

	@Test
	public void testPreserveSpaceByDefaultInSdlXliff() throws Exception {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff xmlns:sdl=\"http://sdl.com/FileTypes/SdlXliff/1.0\" version=\"1.2\" sdl:version=\"1.0\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1.\nt2</source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		// Use the real params, not the test params
		filter.getParameters().load(getClass().getResourceAsStream("/net/sf/okapi/filters/xliff/sdl.fprm"), false);
		filter.getParameters().setSkipNoMrkSegSource(false);
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("t1.\nt2", fmt.printSegmentedContent(tu.getSource(), false));
	}

	@Test
	public void testIssue597SdlXliffConfStateMapping() throws Exception {
		filter.getParameters().load(getClass().getResourceAsStream("/net/sf/okapi/filters/xliff/sdl.fprm"), false);

		List<Event> events = createSimpleSDLXLIFF(SdlXliffConfLevel.DRAFT.getConfValue());
		ITextUnit tu = null;
		for (Event event : events) {
			if (event.getEventType().equals(EventType.TEXT_UNIT)) {
				tu = event.getTextUnit();
				break;
			}
		}
		assertNotNull(tu);
		assertTrue(tu.hasTarget(locFR));

		TextContainer tc = tu.getTarget(locFR);
		// State property of text container set based on conf value of <sdl:seg> tag
		assertTrue(tc.hasProperty(Property.STATE));
		assertEquals(SdlXliffConfLevel.DRAFT.getStateValue(), tc.getProperty(Property.STATE).getValue());
		// Keep track of initial conf value
		assertTrue(tc.hasProperty(SdlXliffSkeletonWriter.PROP_ORIG_SDL_SEG_CONF));
		assertEquals(SdlXliffConfLevel.DRAFT.getConfValue(), tc.getProperty(SdlXliffSkeletonWriter.PROP_ORIG_SDL_SEG_CONF).getValue());

		// Change the state property of the text container before using the writer
		tc.setProperty(new Property(Property.STATE, SdlXliffConfLevel.APPROVED_SIGN_OFF.getStateValue(), false));

		// Conf value of <sdl:seg> tag should be updated to reflect the new text container state
		String out = FilterTestDriver.generateOutput(events, locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false);
		assertEquals(getExpectedSDLXLIFF(SdlXliffConfLevel.APPROVED_SIGN_OFF.getConfValue()), out);
	}

	@Test
	public void testIssue597SdlXliffInvalidInitialConf() throws Exception {
		filter.getParameters().load(getClass().getResourceAsStream("/net/sf/okapi/filters/xliff/sdl.fprm"), false);

		List<Event> events = createSimpleSDLXLIFF("foobar"); // Invalid initial conf value
		ITextUnit tu = null;
		for (Event event : events) {
			if (event.getEventType().equals(EventType.TEXT_UNIT)) {
				tu = event.getTextUnit();
				break;
			}
		}
		assertNotNull(tu);
		assertTrue(tu.hasTarget(locFR));

		TextContainer tc = tu.getTarget(locFR);
		// State for text container should be set even though initial conf value was invalid
		assertTrue(tc.hasProperty(Property.STATE));
		assertEquals("x-foobar", tc.getProperty(Property.STATE).getValue());
		// Keep track of initial conf value
		assertTrue(tc.hasProperty(SdlXliffSkeletonWriter.PROP_ORIG_SDL_SEG_CONF));
		assertEquals("foobar", tc.getProperty(SdlXliffSkeletonWriter.PROP_ORIG_SDL_SEG_CONF).getValue());

		// Change the state property of the text container to be valid
		tc.setProperty(new Property(Property.STATE, SdlXliffConfLevel.APPROVED_SIGN_OFF.getStateValue(), false));

		// Conf value of <sdl:seg> tag should be updated to reflect the new text container state
		String out = FilterTestDriver.generateOutput(events, locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false);
		assertEquals(getExpectedSDLXLIFF(SdlXliffConfLevel.APPROVED_SIGN_OFF.getConfValue()), out);
	}

	@Test
	public void testIssue597SdlXliffInvalidUpdatedState() throws Exception {
		filter.getParameters().load(getClass().getResourceAsStream("/net/sf/okapi/filters/xliff/sdl.fprm"), false);

		List<Event> events = createSimpleSDLXLIFF(SdlXliffConfLevel.DRAFT.getConfValue());
		ITextUnit tu = null;
		for (Event event : events) {
			if (event.getEventType().equals(EventType.TEXT_UNIT)) {
				tu = event.getTextUnit();
				break;
			}
		}
		assertNotNull(tu);
		assertTrue(tu.hasTarget(locFR));

		TextContainer tc = tu.getTarget(locFR);
		assertTrue(tc.hasProperty(Property.STATE));
		assertEquals(SdlXliffConfLevel.DRAFT.getStateValue(), tc.getProperty(Property.STATE).getValue());
		assertTrue(tc.hasProperty(SdlXliffSkeletonWriter.PROP_ORIG_SDL_SEG_CONF));
		assertEquals(SdlXliffConfLevel.DRAFT.getConfValue(), tc.getProperty(SdlXliffSkeletonWriter.PROP_ORIG_SDL_SEG_CONF).getValue());

		// Change the state property of the text container to be invalid
		tc.setProperty(new Property(Property.STATE, "foobar", false));

		// The initial conf value was "Draft", so that will be used for the output since state property is invalid
		String out1 = FilterTestDriver.generateOutput(events, locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false);
		assertEquals(getExpectedSDLXLIFF(SdlXliffConfLevel.DRAFT.getConfValue()), out1);

		// Remove the state property
		tc.removeProperty(Property.STATE);

		// "Draft" will again be used for the output since state property is missing
		String out2 = FilterTestDriver.generateOutput(events, locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false);
		assertEquals(getExpectedSDLXLIFF(SdlXliffConfLevel.DRAFT.getConfValue()), out2);
	}

	@Test
	public void testIssue597SdlXliffRemoveStateAndOriginalConf() throws Exception {
		filter.getParameters().load(getClass().getResourceAsStream("/net/sf/okapi/filters/xliff/sdl.fprm"), false);

		List<Event> events = createSimpleSDLXLIFF(SdlXliffConfLevel.DRAFT.getConfValue());
		ITextUnit tu = null;
		for (Event event : events) {
			if (event.getEventType().equals(EventType.TEXT_UNIT)) {
				tu = event.getTextUnit();
				break;
			}
		}
		assertNotNull(tu);
		assertTrue(tu.hasTarget(locFR));

		TextContainer tc = tu.getTarget(locFR);
		assertTrue(tc.hasProperty(Property.STATE));
		assertEquals(SdlXliffConfLevel.DRAFT.getStateValue(), tc.getProperty(Property.STATE).getValue());
		assertTrue(tc.hasProperty(SdlXliffSkeletonWriter.PROP_ORIG_SDL_SEG_CONF));
		assertEquals(SdlXliffConfLevel.DRAFT.getConfValue(), tc.getProperty(SdlXliffSkeletonWriter.PROP_ORIG_SDL_SEG_CONF).getValue());

		// Remove the state and original conf properties
		tc.removeProperty(Property.STATE);
		tc.removeProperty(SdlXliffSkeletonWriter.PROP_ORIG_SDL_SEG_CONF);

		// The config specifies Translated as 'sdlSegConfValue', so that will be used since both properties were removed
		String out = FilterTestDriver.generateOutput(events, locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false);
		assertEquals(getExpectedSDLXLIFF(SdlXliffConfLevel.TRANSLATED.getConfValue()), out);
	}

	/**
	 * This test makes sure that we get no {@code state} if the {@code <sdl:seg>} has no {@code
	 * conf} attribute.
	 */
	@Test
	public void testIssue597SdlXliffNoConf() throws Exception {
		filter.getParameters()
				.load(getClass().getResourceAsStream("/net/sf/okapi/filters/xliff/sdl.fprm"),
						false);

		List<Event> events = createSimpleSDLXLIFF(null);
		ITextUnit tu = null;
		for (Event event : events) {
			if (event.getEventType().equals(EventType.TEXT_UNIT)) {
				tu = event.getTextUnit();
				break;
			}
		}
		assertNotNull(tu);
		assertTrue(tu.hasTarget(locFR));

		TextContainer tc = tu.getTarget(locFR);
		assertThat(tc.getPropertyNames()).doesNotContain("state");
		assertThat(tc.getPropertyNames())
				.doesNotContain(SdlXliffSkeletonWriter.PROP_ORIG_SDL_SEG_CONF);

		// we expect the value from Parameters#getSdlSegConfValue
		String out1 = FilterTestDriver.generateOutput(events, locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false);
		assertThat(out1).isXmlEqualTo(getExpectedSDLXLIFF("Translated"));
	}

	@Test
	public void testSegmentedAddedPh() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1. t2</source>"
			+ "<target xml:lang=\"fr\"><mrk mid=\"i1\" mtype=\"seg\">t1.<ph id=\"-1\">&lt;br/></ph></mrk>\n<mrk mid=\"i2\" mtype=\"seg\">t2</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getTarget(locFR);
		ISegments segments = cont.getSegments();
		assertEquals("[t1.<br/>] [t2]", fmt.printSegmentedContent(cont, true, true));
		assertEquals(2, segments.count());
		assertEquals("t1.<br/>", segments.get(0).text.toText());
		assertEquals("i2", segments.get(1).id);
		assertEquals("t2", segments.get(1).text.toText());
		assertEquals("i2", segments.get(1).id);
	}

	@Test
	public void testSegmentedAddedBptEpt() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1. t2</source>"
			+ "<target xml:lang=\"fr\"><mrk mid=\"i1\" mtype=\"seg\"><bpt id=\"-1\">&lt;b></bpt>t1.<ept id=\"-1\">&lt;/b></ept></mrk>\n<mrk mid=\"i2\" mtype=\"seg\">t2</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getTarget(locFR);
		ISegments segments = cont.getSegments();
		assertEquals("[<b>t1.</b>] [t2]", fmt.printSegmentedContent(cont, true, true));
		assertEquals(2, segments.count());
		assertEquals("<b>t1.</b>", segments.get(0).text.toText());
		assertEquals("i2", segments.get(1).id);
		assertEquals("t2", segments.get(1).text.toText());
		assertEquals("i2", segments.get(1).id);
	}

	@Test
	public void testSegmentedAddedIt() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1. t2</source>"
			+ "<target xml:lang=\"fr\"><mrk mid=\"i1\" mtype=\"seg\"><it id=\"-1\" pos=\"open\">&lt;b></it>t1.</mrk>\n<mrk mid=\"i2\" mtype=\"seg\">t2<it id=\"-1\" pos=\"close\">&lt;/b></it></mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getTarget(locFR);
		ISegments segments = cont.getSegments();
		assertEquals("[<b>t1.] [t2</b>]", fmt.printSegmentedContent(cont, true, true));
		assertEquals(2, segments.count());
		assertEquals("<b>t1.", segments.get(0).text.toText());
		assertEquals("i2", segments.get(1).id);
		assertEquals("t2</b>", segments.get(1).text.toText());
		assertEquals("i2", segments.get(1).id);
	}

	@Test
	public void testSegmentedAddedItAndPh() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1. t2</source>"
			+ "<target xml:lang=\"fr\"><mrk mid=\"i1\" mtype=\"seg\"><it id=\"-1\" pos=\"open\">&lt;b></it>t1.<ph id=\"-2\">&lt;br/></ph></mrk>\n<mrk mid=\"i2\" mtype=\"seg\">t2<it id=\"-1\" pos=\"close\">&lt;/b></it></mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getTarget(locFR);
		ISegments segments = cont.getSegments();
		assertEquals("[<b>t1.<br/>] [t2</b>]", fmt.printSegmentedContent(cont, true, true));
		assertEquals(2, segments.count());
		assertEquals("<b>t1.<br/>", segments.get(0).text.toText());
		assertEquals("i2", segments.get(1).id);
		assertEquals("t2</b>", segments.get(1).text.toText());
		assertEquals("i2", segments.get(1).id);
	}

	@Test
	public void testSegmentedAddedBptEptAndPh() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1. t2</source>"
			+ "<target xml:lang=\"fr\"><mrk mid=\"i1\" mtype=\"seg\"><bpt id=\"-1\">&lt;b></bpt>t1.<ph id=\"-2\">&lt;br/></ph><ept id=\"-1\">&lt;/b></ept></mrk>\n<mrk mid=\"i2\" mtype=\"seg\">t2</mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getTarget(locFR);
		ISegments segments = cont.getSegments();
		assertEquals("[<b>t1.<br/></b>] [t2]", fmt.printSegmentedContent(cont, true, true));
		assertEquals(2, segments.count());
		assertEquals("<b>t1.<br/></b>", segments.get(0).text.toText());
		assertEquals("i2", segments.get(1).id);
		assertEquals("t2", segments.get(1).text.toText());
		assertEquals("i2", segments.get(1).id);
	}
	
	private String[] getSdlXliffFiles() throws URISyntaxException {
		// read all files in the test xml directory
		File dir = location.in("/sdlxliff").asFile();

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".sdlxliff");
			}
		};
		return dir.list(filter);
	}

	@Test
	public void testIssue424() throws URISyntaxException {
	    URI uri = location.in("/lqiExtensions.xlf").asUri();
	    RawDocument rd = new RawDocument(uri, "UTF-8", locEN);
	    rd.setTargetLocale(locFR);
	    List<Event> tuEvents = FilterTestDriver.getTextUnitEvents(filter, rd);
	    assertEquals(1, tuEvents.size());
	    ITextUnit tu = tuEvents.get(0).getTextUnit();
	    TextContainer target = tu.getTarget(locFR);
	    assertNotNull(target);
	    ITSLQIAnnotations lqiAnns = tu.getAnnotation(ITSLQIAnnotations.class);
	    assertNotNull(lqiAnns);
	    List<GenericAnnotation> anns = lqiAnns.getAnnotations(GenericAnnotationType.LQI);
	    assertNotNull(anns);
	    assertEquals(1, anns.size());
	    GenericAnnotation ann = anns.get(0);
	    assertEquals("mistranslation", ann.getString("lqiType"));
	}

	@Test
	public void testPreserveSpaceByDefaultNoDeclaration() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>A\nB  C</source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		testPreserveSpaceByDefault(snippet);
	}

	@Test
	public void testPreserveSpaceByDefaultOnTransUnit() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit xml:space=\"default\" id=\"1\">"
			+ "<source>A\nB  C</source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		testPreserveSpaceByDefault(snippet);
	}

	//TODO(mihai):
	@Test @Ignore
	public void disabled_testModifyXLIFFNote() {
	    String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	            + "<xliff version=\"1.2\">"
	            + "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
	            + "<body>"
	            + "<trans-unit xml:space=\"default\" id=\"1\">"
	            + "<source>Test</source>"
	            + "<note from=\"note &amp;\" priority=\"5\">Note &amp; Content</note>"
	            + "</trans-unit>"
	            + "</body>"
	            + "</file></xliff>";
	    List<Event> events = getEvents(snippet);
	    ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
        assertNotNull(tu);
        Property note = tu.getProperty(Property.NOTE);
        assertNotNull(note);
        assertEquals("Note & Content", note.getValue());
        note.setValue("Modified < Note");
        String output = FilterTestDriver.generateOutput(events,
                locFR, outSegFilter.createSkeletonWriter(), outSegFilter.getEncoderManager());
        String expected =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<xliff version=\"1.2\">" +
                "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">" +
                "<body><trans-unit xml:space=\"default\" id=\"1\"><source>Test</source>" +
                "<seg-source><mrk mid=\"0\" mtype=\"seg\">Test</mrk></seg-source>\n" +
                "<target xml:lang=\"fr\"><mrk mid=\"0\" mtype=\"seg\">Test</mrk></target>\n" +
                "<note from=\"note &amp;\" priority=\"5\">Modified &lt; Note</note></trans-unit></body></file></xliff>";
        assertEquals(expected, output);
	}

	@Test
	public void testAddXLIFFNote() {
	    String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xliff version=\"1.2\">\n"
                + "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\" build-num=\"13\">"
                + "<body><trans-unit id=\"1\"><source>Hello World!</source>"
                + "<note>Test note</note></trans-unit></body>"
                + "</file></xliff>";
	    List<Event> events = getEvents(snippet);
        ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
        assertNotNull(tu);
        Property note = new Property(Property.NOTE, "Test note");
        tu.setProperty(note);
        String output = FilterTestDriver.generateOutput(events,
                locFR, outSegFilter.createSkeletonWriter(), outSegFilter.getEncoderManager());
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xliff version=\"1.2\">\n"
                + "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\" build-num=\"13\" target-language=\"fr\">"
                + "<body><trans-unit id=\"1\"><source>Hello World!</source>"
                + "<seg-source><mrk mid=\"0\" mtype=\"seg\">Hello World!</mrk></seg-source>\n"
                + "<target xml:lang=\"fr\"><mrk mid=\"0\" mtype=\"seg\">Hello World!</mrk></target>\n"
                + "<note>Test note</note></trans-unit></body>"
                + "</file></xliff>";
        assertEquals(expected, output);
	}

	@Test
	public void testOutputSkipTargetInExtention () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:mq=\"MQXliff\" version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">\n"
			+ "<source>source</source>\n"
			+ "<target>target</target>\n"
			+ "<mq:historical-unit>\n<mq:testEmpty/>\n"
			+ "<source xml:space=\"preserve\" mq:segpart=\"2723\">bad source</source>\n"
			+ "<target xml:space=\"preserve\">bad target</target>\n"
			+ "</mq:historical-unit>\n"			
			+ "</trans-unit>\n"
			+ "</body>"
			+ "</file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:mq=\"MQXliff\" version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">\n"
			+ "<source>source</source>\n"
			+ "<target>target</target>\n"
			+ "<mq:historical-unit>\n<mq:testEmpty></mq:testEmpty>\n"
			+ "<source xml:space=\"preserve\" mq:segpart=\"2723\">bad source</source>\n"
			+ "<target xml:space=\"preserve\">bad target</target>\n"
			+ "</mq:historical-unit>\n"			
			+ "</trans-unit>\n"
			+ "</body>"
			+ "</file></xliff>";

		// Get the events and segment the source
		List<Event> events = getEvents(snippet, filter);
		// Output the result
		assertEquals(expected, FilterTestDriver.generateOutput(events,
			locFR, filter.createSkeletonWriter(), filter.getEncoderManager()));
	}
	
	@Test
    public void testHandleInvalidXmlCharacters () {
        assertTrue("Problem in HandleInvalidXmlCharacters", FilterTestDriver.testStartDocument(filter,
            inputDocumentToTest("invalid_xml_entity.xlf", null),
            "UTF-8", locEN, locEN));
        assertTrue(true);
    }

	@Test
	public void testIssue466NoMrk() {
		RawDocument rd;
		List<Event> events;

		filter.getParameters().load(getClass().getResourceAsStream("/net/sf/okapi/filters/xliff/sdl.fprm"), false);

		filter.getParameters().setSkipNoMrkSegSource(false);
		rd = new RawDocument(location.in("/sdlxliff/no_mrk.sdlxliff").asUri(), "UTF-8", locEN, locFR);
		events = FilterTestDriver.getEvents(filter, rd, null);
		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
		assertEquals(1, textUnits.size());
		// <seg-source> contains 1159 <x> elements (<source> and <target> have markers inserted)
		assertEquals(1159, RegexUtil.countMatches(textUnits.get(0).getSkeleton().toString(), "<x id="));

		filter.getParameters().setSkipNoMrkSegSource(true);
		rd = new RawDocument(location.in("/sdlxliff/no_mrk.sdlxliff").asUri(), "UTF-8", locEN, locFR);
		events = FilterTestDriver.getEvents(filter, rd, null);
		assertEquals(0, FilterTestDriver.filterTextUnits(events).size());
		// <seg-source>, <source>, and <target> have 1159 <x> elements each
		assertEquals(1159 * 3, RegexUtil.countMatches(FilterTestDriver.generateOutput(events, locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false), "<x id="));
	}

	@Test
	public void testIssue466MixedMrk() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:sdl=\"http://sdl.com/FileTypes/SdlXliff/1.0\" sdl:version=\"1.0\" version=\"1.2\">"
				+ "<file datatype=\"x-sdlfilterframework2\" original=\"E:\\test.docx\" source-language=\"en-US\" target-language=\"fr-FR\">"
				+ "<body>"
				// Does not contain mrk
				+ "<trans-unit id=\"100\">"
				+ "<source><x id=\"6848\"/>Foobar</source>"
				+ "<seg-source><x id=\"6848\"/>Foobar</seg-source>"
				+ "<target><x id=\"6848\"/>Foobar</target>"
				+ "</trans-unit>"
				// Contains mrk
				+ "<trans-unit id=\"200\">"
				+ "<source>Hello world!</source>"
				+ "<seg-source><mrk mid=\"0\" mtype=\"seg\">Hello world!</mrk></seg-source>"
				+ "<target><mrk mid=\"0\" mtype=\"seg\">Hello world!</mrk></target>"
				+ "</trans-unit>"
				+ "</body>"
				+ "</file></xliff>";

		filter.getParameters().load(getClass().getResourceAsStream("/net/sf/okapi/filters/xliff/sdl.fprm"), false);

		filter.getParameters().setSkipNoMrkSegSource(false);
		assertEquals(2, FilterTestDriver.filterTextUnits(getEvents(snippet)).size());

		filter.getParameters().setSkipNoMrkSegSource(true);
		List<Event> events = getEvents(snippet);
		assertEquals(1, FilterTestDriver.filterTextUnits(events).size());
		assertEquals("200", FilterTestDriver.filterTextUnits(events).get(0).getId());

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:sdl=\"http://sdl.com/FileTypes/SdlXliff/1.0\" sdl:version=\"1.0\" version=\"1.2\">"
				+ "<file datatype=\"x-sdlfilterframework2\" original=\"E:\\test.docx\" source-language=\"en-US\" target-language=\"fr-FR\">"
				+ "<body>"
				// Should not contain mrk
				+ "<trans-unit id=\"100\">"
				+ "<source><x id=\"6848\"/>Foobar</source>"
				+ "<seg-source><x id=\"6848\"/>Foobar</seg-source>"
				+ "<target><x id=\"6848\"/>Foobar</target>"
				+ "</trans-unit>"
				// Should contain mrk
				+ "<trans-unit id=\"200\">"
				+ "<source>Hello world!</source>"
				+ "<seg-source><mrk mid=\"0\" mtype=\"seg\">Hello world!</mrk></seg-source>"
				+ "<target><mrk mid=\"0\" mtype=\"seg\">Hello world!</mrk></target>"
				+ "</trans-unit>"
				+ "</body>"
				+ "</file></xliff>";

		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet), locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false));
	}

	@Test
	public void testIssue466PreserveCRLF() {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:sdl=\"http://sdl.com/FileTypes/SdlXliff/1.0\" sdl:version=\"1.0\" version=\"1.2\">"
				+ "<file datatype=\"x-sdlfilterframework2\" original=\"E:\\test.docx\" source-language=\"en-US\" target-language=\"fr-FR\">"
				+ "<body>"
				+ "<trans-unit id=\"100\">"
				+ "<source><g id=\"g1\">\r\n<g id=\"g2\"><x id=\"x1\"/>\r\n</g>\r\n</g></source>" // Using \r\n newlines
				+ "<seg-source><g id=\"g1\">\r\n<g id=\"g2\"><x id=\"x1\"/>\r\n</g>\r\n</g></seg-source>"
				+ "<target><g id=\"g1\">\r\n<g id=\"g2\"><x id=\"x1\"/>\r\n</g>\r\n</g></target>"
				+ "</trans-unit>"
				+ "</body>"
				+ "</file></xliff>";

		filter.getParameters().load(getClass().getResourceAsStream("/net/sf/okapi/filters/xliff/sdl.fprm"), false);
		List<Event> events = getEvents(snippet);
		assertEquals(0, FilterTestDriver.filterTextUnits(events).size());

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:sdl=\"http://sdl.com/FileTypes/SdlXliff/1.0\" sdl:version=\"1.0\" version=\"1.2\">"
				+ "<file datatype=\"x-sdlfilterframework2\" original=\"E:\\test.docx\" source-language=\"en-US\" target-language=\"fr-FR\">"
				+ "<body>"
				+ "<trans-unit id=\"100\">"
				+ "<source><g id=\"g1\">\r\n<g id=\"g2\"><x id=\"x1\"/>\r\n</g>\r\n</g></source>" // \r\n should be preserved
				+ "<seg-source><g id=\"g1\">\r\n<g id=\"g2\"><x id=\"x1\"/>\r\n</g>\r\n</g></seg-source>"
				+ "<target><g id=\"g1\">\r\n<g id=\"g2\"><x id=\"x1\"/>\r\n</g>\r\n</g></target>"
				+ "</trans-unit>"
				+ "</body>"
				+ "</file></xliff>";

		assertEquals(expected, FilterTestDriver.generateOutput(events, locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false));
	}

	@Test
	public void testPreserveCDATASkeletonInHeader() {
		List<Event> events = getEvents(getCDATASkeleton());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+System.lineSeparator() 
				+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:m=\"http://www.memsource.com/mxlf/2.0\" "
				+ "version=\"1.2\" m:version=\"2.3\" m:level=\"1\">"
				+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
				+ "<header><m:in-ctx-preview-skel><![CDATA[<p>This is CDATA</p>]]></m:in-ctx-preview-skel></header>"
				+ "<body></body></file></xliff>";
		String actual = FilterTestDriver.generateOutput(events, locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false);
		assertThat(actual).isXmlEqualTo(expected);
	}

	@Test
	public void testPreserveCDATAInNotSegmentTransUnitContent() {
		List<Event> events = getEvents(getCDATASkeletonInTransUnit());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+System.lineSeparator()
				+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:foo=\"http://okapiframework.org\" version=\"1.2\">"
				+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
				+ "<header></header><body><trans-unit id=\"1\"><source>source</source><target>target</target>"
				+ "<foo:extra-data><![CDATA[<p>This is CDATA</p>]]></foo:extra-data></trans-unit></body></file></xliff>";
		String actual = FilterTestDriver.generateOutput(events, locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false);
		assertThat(actual).isXmlEqualTo(expected);
	}
	
	@Test
	public void testPreserveCDATAInBody(){
		
		List<Event> events = getEvents(getCDATASkeletonInBody());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator()
				+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:m=\"http://www.memsource.com/mxlf/2.0\" "
				+ "version=\"1.2\" m:version=\"2.3\" m:level=\"1\">"
				+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
				+ "<header></header>" 
				+ "<body><m:in-ctx-preview-metadata><m:mark id=\"1\"><m:open-mark-content>"
				+ "<![CDATA[</span><a id=\"_xlfxrw3h3nzh\"/><span class=\"T2\">]]></m:open-mark-content>"
				+ "</m:mark></m:in-ctx-preview-metadata></body></file></xliff>";
		String actual = FilterTestDriver.generateOutput(events, locFR,
				filter.createSkeletonWriter(), filter.getEncoderManager(), false);
		assertThat(actual).isXmlEqualTo(expected);
	}

	private void testPreserveSpaceByDefault(String snippet) {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("A B C", fmt.printSegmentedContent(tu.getSource(), false));
		XLIFFFilter preserveFilter = new XLIFFFilter();
		preserveFilter.getParameters().setPreserveSpaceByDefault(true);
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, preserveFilter), 1);
		assertEquals("A\nB  C", fmt.printSegmentedContent(tu.getSource(), false));
	}

	private ArrayList<Event> createSimpleXLIFF () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\" build-num=\"13\">"
			+ "<my:elem xmlns:my=\"myNS\"><my:elem2>data</my:elem2></my:elem>\n"
			+ "<body><trans-unit id=\"1\" resname=\"13\" extradata=\"xd\"><source>Hello World!</source></trans-unit></body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}
	
	private ArrayList<Event> createBilingualXLIFF () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"plaintext\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"13\"><source><g id='1'>S1</g>, <g id='2'>S2</g></source>"
			+ "<target><g id='2'>T2</g>, <g id='1'>T1</g></target></trans-unit></body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}
	
	private ArrayList<Event> createBilingualXLIFFWithLocales (LocaleId src, LocaleId trg) {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"plaintext\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"13\"><source><g id='1'>S1</g>, <g id='2'>S2</g></source>"
			+ "<target><g id='2'>T2</g>, <g id='1'>T1</g></target></trans-unit></body>"
			+ "</file>"
			+ "<file source-language=\"en\" target-language=\"es\" datatype=\"plaintext\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"13\"><source><g id='1'>S1</g>, <g id='2'>S2</g></source>"
			+ "<target><g id='2'>T2</g>, <g id='1'>T1</g></target></trans-unit></body>"
			+ "</file>"
			+ "</xliff>";
		return getEvents(snippet, src, trg);
	}
	
	private ArrayList<Event> createBPTTypeXLIFF () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"13\"><source><bpt id='1'>a</bpt>S1<ept id='1'>/a</ept>, <bpt id='2'>b</bpt>S2<ept id='2'>/b</ept></source>"
			+ "<target><bpt id='2'>b</bpt>T2<ept id='2'>/b</ept>, <bpt id='1'>a</bpt>T1<ept id='1'>/a</ept></target></trans-unit></body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}

	private ArrayList<Event> createDecoratedXLIFF () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"13\">"
			+ "<source>text src</source>"
			+ "<target>text trg</target>"
			+ "<note>note 1</note>"
			+ "<note annotates='general'>note 2</note>"
			+ "<note annotates='source'>note src 1</note>"
			+ "<note annotates='target'>note trg</note>"
			+ "<note annotates='source'>note src 2</note>"
			+ "</trans-unit></body></file></xliff>";
		return getEvents(snippet);
	}
	
	private ArrayList<Event> createBPTAndSUBTypeXLIFF () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"13\"><source><bpt id=\"1\">a<sub>text</sub></bpt>S1<ept id=\"1\">/a</ept>, <bpt id=\"2\">b</bpt>S2<ept id=\"2\">/b</ept></source>"
			+ "</trans-unit></body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}
	
	private ArrayList<Event> createComplexSUBTypeXLIFF () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"13\"><source>t1 <ph id=\"10\">startCode<sub>[nested<ph id=\"20\">ph-in-sub</ph>still in sub]</sub>endCode</ph> t2</source>"
			+ "</trans-unit></body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}
	
	private ArrayList<Event> createTUWithSpaces () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\" xml:space=\"preserve\"><source>t1  t2 t3\t\t<ph id='1'>X</ph>  t4</source></trans-unit>"
			+ "<trans-unit id=\"2\"><source>t1  t2 t3\t\t<ph id='1'>X</ph>  t4</source></trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}
	
	private ArrayList<Event> createSegmentedTUWithSpaces () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\" xml:space='preserve'><source>t1  t2  t3  t4</source>"
			+ "<seg-source><mrk mid='1' mtype='seg'>t1  t2</mrk>  <mrk mid='2' mtype='seg'>t3  t4</mrk></seg-source>"
			+ "<target xml:lang='fr'>tt1  tt2  tt3  tt4</target>"
			+ "</trans-unit>"
			+ "<trans-unit id=\"2\"><source>t1  t2  t3  t4</source>"
			+ "<seg-source><mrk mid='1' mtype='seg'>t1  t2</mrk>  <mrk mid='2' mtype='seg'>t3  t4</mrk></seg-source>"
			+ "<target xml:lang='fr'>tt1  tt2  tt3  tt4</target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}

	private ArrayList<Event> createSegmentedTUEmptyTarget () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\" xml:space='preserve'><source><ph id='1'>code</ph>t1</source>"
			+ "<seg-source><ph id='1'>code</ph><mrk mid='s1' mtype='seg'>t1</mrk></seg-source>"
			+ "<target xml:lang='fr'><ph id='1'>code</ph><mrk mid='s1' mtype='seg'></mrk></target>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}

	private ArrayList<Event> createApprovedTU () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\" approved=\"yes\"><source>t1</source>"
			+ "<target>translated t1</target></trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}
	
	private ArrayList<Event> createInputWithNamespace () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<x:xliff version=\"1.2\" xmlns:x=\"'urn:oasis:names:tc:xliff:document:1.2'\">\r"
			+ "<x:file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<x:body>"
			+ "<x:trans-unit id=\"1\" approved=\"yes\"><x:source>t1</x:source>"
			+ "<x:target>translated t1</x:target></x:trans-unit>"
			+ "</x:body>"
			+ "</x:file></x:xliff>";
		return getEvents(snippet);
	}
	
	private ArrayList<Event> createTUWithMrk () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\"><source>t1<mrk mtype=\"x-abc\">t2</mrk></source>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}
	
	private ArrayList<Event> createTUWithAltTrans () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\">\r"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\"><source>t1</source>"
			+ "<target>translated t1</target>"
			+ "<alt-trans match-quality=\"50\" origin=\"orig\" okp:matchType=\"FUZZY\" okp:engine=\"abc\">"
			+ "<source>alt source <bpt id=\"1\">{</bpt>t1<ept id=\"1\">}</ept></source>"
			+ "<target>alt target <mrk mtype=\"term\"><bpt id=\"1\">{</bpt>t1<ept id=\"1\">}</ept></mrk></target>"
			+ "</alt-trans>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}
	
	private ArrayList<Event> createTUWithMixedAltTrans () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\">\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>t1 inter t2</source>"
			+ "<seg-source><mrk mid=\"s1\" mtype=\"seg\">t1</mrk> inter <mrk mid=\"s2\" mtype=\"seg\">t2</mrk></seg-source>"
			+ "<alt-trans>"
			+ "<target>TRG for t1 inter t2</target>"
			+ "</alt-trans>"
			+ "<alt-trans mid=\"s2\">"
			+ "<target>TRG for t2</target>"
			+ "</alt-trans>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}

	private ArrayList<Event> createTUWithAltTransData () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<xliff version=\"1.2\""
			+ " xmlns='"+Namespaces.NS_XLIFF12+"' xmlns:okp='"+Namespaces.NS_XLIFFOKAPI+"'>\r"
			+ "<file source-language=\"en\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body>"
			+ "<trans-unit id=\"1\">"
			+ "<source>source</source>"
			+ "<alt-trans match-quality='101' origin='xyz'>"
			+ "<source>alt-trans source 2</source>"
			+ "<target>alt-trans target 2</target></alt-trans>"
			+ "<alt-trans>"
			+ "<target>alt-trans target 3</target></alt-trans>"
			+ "<alt-trans match-quality='exact'>" // not a common match-quality -> not supported
			+ "<target>alt-trans target 4</target></alt-trans>"
			+ "<alt-trans match-quality='100%' okp:matchType='EXACT_UNIQUE_ID'>"
			+ "<target>alt-trans best target</target></alt-trans>"
			+ "<alt-trans match-quality='100%' okp:matchType='EXACT_LOCAL_CONTEXT' origin='qwe'>"
			+ "<target>alt-trans local context</target></alt-trans>"
			+ "</trans-unit>"
			+ "</body>"
			+ "</file></xliff>";
		return getEvents(snippet);
	}

	private ArrayList<Event> createSimpleSDLXLIFF(String conf) {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<xliff xmlns:sdl=\"http://sdl.com/FileTypes/SdlXliff/1.0\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" version=\"1.2\" sdl:version=\"1.0\">"
				+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
				+ "<body>"
				+ "<trans-unit id=\"1\" xml:space=\"preserve\">"
				+ "<source>source text</source>"
				+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">source</mrk> <mrk mid=\"2\" mtype=\"seg\">text</mrk></seg-source>"
				+ "<target><mrk mid=\"1\" mtype=\"seg\">source</mrk> <mrk mid=\"2\" mtype=\"seg\">text</mrk></target>"
				+ "<sdl:seg-defs>"
				+ "<sdl:seg";
		if (conf != null) {
			snippet += " conf=\"" + conf + "\"";
		}
		snippet += " id=\"1\" locked=\"false\" origin=\"not-translated\"/>"
				+ "</sdl:seg-defs>"
				+ "</trans-unit>"
				+ "</body>"
				+ "</file></xliff>";
		return getEvents(snippet);
	}
	private String getExpectedSDLXLIFF(String conf) {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ System.getProperty("line.separator")
				+ "<xliff xmlns:sdl=\"http://sdl.com/FileTypes/SdlXliff/1.0\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" version=\"1.2\" sdl:version=\"1.0\">"
				+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
				+ "<body>"
				+ "<trans-unit id=\"1\" xml:space=\"preserve\">"
				+ "<source>source text</source>"
				+ "<seg-source><mrk mid=\"1\" mtype=\"seg\">source</mrk> <mrk mid=\"2\" mtype=\"seg\">text</mrk></seg-source>"
				+ "<target><mrk mid=\"1\" mtype=\"seg\">source</mrk> <mrk mid=\"2\" mtype=\"seg\">text</mrk></target>"
				+ "<sdl:seg-defs>"
				+ "<sdl:seg";
		if (conf != null) {
			snippet += " conf=\"" + conf + "\"";
		}
		snippet += " id=\"1\" locked=\"false\" origin=\"not-translated\"></sdl:seg>"
				+ "</sdl:seg-defs>"
				+ "</trans-unit>"
				+ "</body>"
				+ "</file></xliff>";
		return snippet;
	}

	private String getCDATASkeleton() {
	    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	            + "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:m=\"http://www.memsource.com/mxlf/2.0\" version=\"1.2\" m:version=\"2.3\" m:level=\"1\">"
                + "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
	            + "<header>"
                + "<m:in-ctx-preview-skel><![CDATA[<p>This is CDATA</p>]]></m:in-ctx-preview-skel>"
	            + "</header>"
                + "<body/></file></xliff>";
	}

	private String getCDATASkeletonInTransUnit() {
	    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	            + "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:foo=\"http://okapiframework.org\" version=\"1.2\">"
                + "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
	            + "<header>"
	            + "</header>"
                + "<body>"
                + "<trans-unit id=\"1\">"
                + "<source>source</source><target>target</target>"
                + "<foo:extra-data><![CDATA[<p>This is CDATA</p>]]></foo:extra-data>"
                + "</trans-unit>"
                + "</body>"
                + "</file></xliff>";
	}

	private String getCDATASkeletonInBody() {
	    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	            + "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:m=\"http://www.memsource.com/mxlf/2.0\" version=\"1.2\" m:version=\"2.3\" m:level=\"1\">"
                + "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
	            + "<header/>"
                + "<body>"
	            + "<m:in-ctx-preview-metadata>"
                + "<m:mark id=\"1\">"
	            + "<m:open-mark-content><![CDATA[</span><a id=\"_xlfxrw3h3nzh\"/><span class=\"T2\">]]></m:open-mark-content>"
                + "</m:mark></m:in-ctx-preview-metadata></body></file></xliff>";
	}
	
	private ArrayList<Event> getEvents(String snippet) {
		return getEvents(snippet, filter);
	}
	
	private ArrayList<Event> getEvents(String snippet, LocaleId srcLocale, LocaleId trgLocale) {
		return FilterTestDriver.getEvents(filter, snippet, srcLocale, trgLocale);
	}
	
	private ArrayList<Event> getEvents (String snippet,
		XLIFFFilter filterToUse)
	{
		return FilterTestDriver.getEvents(filterToUse, snippet, locEN, locFR);
	}
	
	private ArrayList<Event> getEvents(String snippet,
		XLIFFFilter filterToUse,
		LocaleId trgToUse)
	{
		return FilterTestDriver.getEvents(filterToUse, snippet, locEN, trgToUse);
	}
	
	private String stripVariableID (String text) {
		text = text.replaceAll("locQualityIssuesRef=\"#(.*?)\"", "locQualityIssuesRef=\"#VARID\""); 
		text = text.replaceAll("xml:id=\"(.*?)\"", "xml:id=\"VARID\"");
		return text;
	}

}
