/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.ttx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.resource.TextPart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.filterwriter.XLIFFContent;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment.TagType;

@RunWith(JUnit4.class)
public class TTXFilterTest {

	private TTXFilter filterIncUnSeg;
	private TTXFilter filterNoUnSeg;
	private GenericContent fmt;
	private FileLocation root;
	private LocaleId locENUS = LocaleId.fromString("en-us");
	private LocaleId locESEM = LocaleId.fromString("es-em");
	private LocaleId locFRFR = LocaleId.fromString("fr-fr");
//	private LocaleId locKOKR = LocaleId.fromString("ko-kr");
	
	private static final String STARTFILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		+ "<TRADOStag Version=\"2.0\"><FrontMatter>\n"
		+ "<ToolSettings CreationDate=\"20070508T094743Z\" CreationTool=\"TRADOS TagEditor\" CreationToolVersion=\"7.0.0.615\"></ToolSettings>\n"
		+ "<UserSettings DataType=\"STF\" O-Encoding=\"UTF-8\" SettingsName=\"\" SettingsPath=\"\" SourceLanguage=\"EN-US\" TargetLanguage=\"ES-EM\" SourceDocumentPath=\"abc.rtf\" SettingsRelativePath=\"\" PlugInInfo=\"\"></UserSettings>\n"
		+ "</FrontMatter><Body><Raw>\n";

	private static final String STARTFILENOLB = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		+ "<TRADOStag Version=\"2.0\"><FrontMatter>\n"
		+ "<ToolSettings CreationDate=\"20070508T094743Z\" CreationTool=\"TRADOS TagEditor\" CreationToolVersion=\"7.0.0.615\"></ToolSettings>\n"
		+ "<UserSettings DataType=\"STF\" O-Encoding=\"UTF-8\" SettingsName=\"\" SettingsPath=\"\" SourceLanguage=\"EN-US\" TargetLanguage=\"ES-EM\" SourceDocumentPath=\"abc.rtf\" SettingsRelativePath=\"\" PlugInInfo=\"\"></UserSettings>\n"
		+ "</FrontMatter><Body><Raw>";

//	private static final String STARTFILEKO = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//		+ "<TRADOStag Version=\"2.0\"><FrontMatter>\n"
//		+ "<ToolSettings CreationDate=\"20070508T094743Z\" CreationTool=\"TRADOS TagEditor\" CreationToolVersion=\"7.0.0.615\"></ToolSettings>\n"
//		+ "<UserSettings DataType=\"STF\" O-Encoding=\"UTF-8\" SettingsName=\"\" SettingsPath=\"\" SourceLanguage=\"EN-US\" TargetLanguage=\"KO-KR\" TargetDefaultFont=\"\ubd7e\" SourceDocumentPath=\"abc.rtf\" SettingsRelativePath=\"\" PlugInInfo=\"\"></UserSettings>\n"
//		+ "</FrontMatter><Body><Raw>\n";

	public TTXFilterTest () {
		fmt = new GenericContent();
		root = FileLocation.fromClass(this.getClass());
		filterIncUnSeg = new TTXFilter();
		Parameters prm1 = (Parameters)filterIncUnSeg.getParameters();
		prm1.setSegmentMode(Parameters.MODE_ALL);
		filterNoUnSeg = new TTXFilter();
		Parameters prm2 = (Parameters)filterNoUnSeg.getParameters();
		prm2.setSegmentMode(Parameters.MODE_EXISTINGSEGMENTS);
	}

	@Test
	public void testSegmentedSurroundedByInternalCodes () {
		String snippet = STARTFILENOLB
			+ "<ut Type=\"start\">bc</ut>"
			+ "<Tu MatchPercent=\"100\">"
			+ "<Tuv Lang=\"EN-US\">en1</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">es1</Tuv>"
			+ "</Tu>"
			+ "<ut Type=\"end\">ec</ut>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertEquals(1, segments.count());
		assertEquals("en1", segments.get(0).toString());
		cont = tu.getTarget(locESEM);
		segments = cont.getSegments();
		assertEquals(1, segments.count());
		assertEquals("<b1/>[es1]<e1/>", fmt.printSegmentedContent(cont, true));
	}

//TODO: fix complex case	@Test
	public void testSegmentedComplex () {
		String snippet = STARTFILENOLB
			+ "<ut Type=\"start\" Style=\"external\" RightEdge=\"angle\" DisplayText=\"paragraph\">&lt;paragraph &gt;</ut>"
			+ "<df Font=\"Wingdings 3\">" // ph 1 <df>
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">&lt;cf &gt;</ut>" // start 2 <cf>
			+ "</df>" // ph 3 </df>
			+ "<ut DisplayText=\"symbol\">&lt;symbol /&gt;</ut>" // ph 4
			+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>" // end 2 </cf> 
			+ "<df Font=\"Myriad Pro\">" // ph 5 <df>
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">&lt;cf &gt;</ut>" // start 6 <cf>
			+ " <Tu><Tuv Lang=\"EN-US\"><df Font=\"Arial\">" // ph 7 <df>
			+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>" // end 6 </cf>
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">&lt;cf &gt;</ut>" // start 8 <cf>
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"indenthere\">&lt;indenthere/&gt;</ut>" // start 9 <indent>
			+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>" // end 8 </cf>
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">&lt;cf &gt;</ut>" // start 9 <cf>
			+ "Src 1 "
			+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>"
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">&lt;cf &gt;</ut>"
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"linebreak\">&lt;linebreak/&gt;</ut>"
			+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>"
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">&lt;cf &gt;</ut>"
			+ "Src 2."
			+ "</df>"
			+ "</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">"
			+ "<df Font=\"Arial\">"
			+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>"
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">&lt;cf &gt;</ut>"
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"indenthere\">&lt;indenthere/&gt;</ut>"
			+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>"
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">&lt;cf &gt;</ut>"
			+ "Trg 1 "
			+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>"
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">&lt;cf &gt;</ut>"
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"linebreak\">&lt;linebreak/&gt;</ut>"
			+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>"
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">&lt;cf &gt;</ut>"
			+ "Trg 2."
			+ "</df>"
			+ "</Tuv></Tu>"
			+ "</df>"
			+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>"
			+ "<ut Type=\"end\" Style=\"external\" LeftEdge=\"angle\" DisplayText=\"paragraph\">&lt;/paragraph&gt;</ut>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertEquals(1, segments.count());
		assertEquals("Src 1", fmt.setContent(segments.get(0).text).toString());
	}

	@Test
	public void testNotSegmentedWithDFAndCodes () {
		String snippet = STARTFILENOLB
			+ "<df Size=\"12\">"
			+ "<ut Type=\"start\" Style=\"external\">{P}</ut><ut Type=\"start\">{i}</ut>"
			+ "src text" 
			+ "</df><ut Type=\"end\">{/i}</ut>"
			+ "<ut Type=\"end\" Style=\"external\">{/P}</ut>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertEquals(1, segments.count());
		// </df> moved outside
		assertEquals("[<1>src text</1>]", fmt.printSegmentedContent(cont, true));
	}

	@Test
	public void testNotSegmentedWithDF () {
		String snippet = STARTFILENOLB
			+ "<df Size=\"12\">"
			+ "<ut Type=\"start\" Style=\"external\">{P}</ut>"
			+ "src text" 
			+ "</df>"
			+ "<ut Type=\"end\" Style=\"external\">{/P}</ut>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertEquals(1, segments.count());
		// </df> moved outside
		assertEquals("[src text]", fmt.printSegmentedContent(cont, true));
	}

	@Test
	public void testOutputNotSegmentedWithDF_ForcingOutSeg () {
		String snippet = STARTFILENOLB
			+ "<df Size=\"12\">"
			+ "<ut Type=\"start\" Style=\"external\">{P}</ut>"
			+ "src text" 
			+ "</df>"
			+ "<ut Type=\"end\" Style=\"external\">{/P}</ut>"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<df Size=\"12\">"
			+ "<ut Type=\"start\" Style=\"external\">{P}</ut>"
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">src text</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">src text</Tuv>"
			+ "</Tu>"
			+ "</df>"
			+ "<ut Type=\"end\" Style=\"external\">{/P}</ut>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testNotSegmentedWithLeadingWS () {
		String snippet = STARTFILENOLB
			+ "<ut Type=\"start\" Style=\"external\">bc</ut>"
			+ "\n   text" 
			+ "<ut Type=\"end\" Style=\"external\">ec</ut>"
			+ "</Raw></Body></TRADOStag>";

		// With inclusion
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertEquals(1, segments.count());
		assertEquals("[text]", fmt.printSegmentedContent(cont, true));
		ISkeleton skl = tu.getSkeleton();
		assertNotNull(skl);
		
		// Without inclusion
		tu = FilterTestDriver.getTextUnit(getEvents(filterNoUnSeg, snippet, locESEM), 1);
		assertNull(tu);
	}

	@Test
	public void testOutputNotSegmentedWithLeadingWS () {
		String snippet = STARTFILENOLB
			+ "<ut Type=\"start\" Style=\"external\">bc</ut>"
			+ "\n   text" 
			+ "<ut Type=\"end\" Style=\"external\">ec</ut>"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<ut Type=\"start\" Style=\"external\">bc</ut>"
			+ "\n   "
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">text</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">text</Tuv>"
			+ "</Tu>"
			+ "<ut Type=\"end\" Style=\"external\">ec</ut>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testSegmentedSurroundedByDF () {
		String snippet = STARTFILENOLB
			+ "<df Size=\"16\">"
			+ "<ut Type=\"start\" Style=\"external\">bc</ut>"
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">en1</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">es1</Tuv>"
			+ "</Tu>"
			+ "</df>" // This is the potential issue
			+ "<ut Type=\"end\" Style=\"external\">ec</ut>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertEquals(1, segments.count());
		assertEquals("en1", segments.get(0).toString());
		segments = tu.getTarget(locESEM).getSegments();
		assertEquals(1, segments.count());
		assertEquals("es1", segments.get(0).toString());
		assertNull(segments.get(0).getAnnotation(AltTranslationsAnnotation.class));
	}

	@Test
	public void testSegmentedAndNot () {
		String snippet = STARTFILENOLB
			+ "<df Size=\"16\">"
			+ "<ut Type=\"start\" Style=\"external\">bc</ut>"
			+ " text "
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">en1</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">es1</Tuv>"
			+ "</Tu>"
			+ "</df>" // This is the potential issue
			+ "<ut Type=\"end\" Style=\"external\">ec</ut>"
			+ "</Raw></Body></TRADOStag>";
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertEquals(2, segments.count());
		assertEquals(" text ", segments.get(0).toString());
		assertEquals("en1", segments.get(1).toString());
		segments = tu.getTarget(locESEM).getSegments();
		assertEquals(2, segments.count());
		assertEquals("", segments.get(0).toString());
		assertEquals("es1", segments.get(1).toString());
		assertNull(segments.get(1).getAnnotation(AltTranslationsAnnotation.class));

		// Without un-segmented text
		tu = FilterTestDriver.getTextUnit(getEvents(filterNoUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		cont = tu.getSource();
		segments = cont.getSegments();
		assertEquals(1, segments.count());
		assertEquals("en1", segments.get(0).toString());
		segments = tu.getTarget(locESEM).getSegments();
		assertEquals(1, segments.count());
		assertEquals("es1", segments.get(0).toString());
		assertNull(segments.get(0).getAnnotation(AltTranslationsAnnotation.class));
	}

	@Test
	public void testOutputSegmentedSurroundedByDF () {
		String snippet = STARTFILENOLB
			+ "<df Size=\"16\">"
			+ "<ut Type=\"start\" Style=\"external\">bc</ut>"
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">en1</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">es1</Tuv>"
			+ "</Tu>"
			+ "</df>" // This is the potential issue
			+ "<ut Type=\"end\" Style=\"external\">ec</ut>"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<df Size=\"16\">"
			+ "<ut Type=\"start\" Style=\"external\">bc</ut>"
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">en1</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">es1</Tuv>"
			+ "</Tu>"
			+ "</df>"
			+ "<ut Type=\"end\" Style=\"external\">ec</ut>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testBasicWithEscapes () {
		String snippet = STARTFILENOLB
			+ "&lt;=lt, &amp;=amp, &gt;=gt, &quot;=quot."
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("<=lt, &=amp, >=gt, \"=quot.", cont.toString());
	}

	@Test
	public void testOutputBasicWithEscapes () {
		String snippet = STARTFILENOLB
			+ "&lt;=lt, &amp;=amp, &gt;=gt, &quot;=quot."
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">&lt;=lt, &amp;=amp, >=gt, &quot;=quot.</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">&lt;=lt, &amp;=amp, >=gt, &quot;=quot.</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testBasicNoExtractableData () {
		String snippet = STARTFILE
			+ " <ut Style=\"external\">some &amp; code</ut>\n\n  <!-- comments-->"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNull(tu);
	}

	@Test
	public void testOutputNoExtractableData () {
		String snippet = STARTFILE
			+ " <ut Style=\"external\">some &amp; code</ut>\n\n  <!-- comments-->"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testBasicNoTU () {
		String snippet = STARTFILE
			+ "text"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("\ntext", cont.toString());
		assertTrue(tu.hasTarget(locESEM));
		assertTrue(tu.getTarget(locESEM).isEmpty());
	}

//always seg now	@Test
//	public void testOutputBasicNoTU () {
//		String snippet = STARTFILENOLB
//			+ "text"
//			+ "</Raw></Body></TRADOStag>";
//		String expected = STARTFILENOLB
//			+ "text"
//			+ "</Raw></Body></TRADOStag>";
//		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterSegNotForced, snippet, locESEM),
//			locESEM, filterSegNotForced.createSkeletonWriter(), filterSegNotForced.getEncoderManager()));
//	}

	@Test
	public void testOutputBasicNoTUWithSegmentation () {
		String snippet = STARTFILENOLB
			+ "text"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">text</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">text</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testOutputEscapesInSkeleton () {
		String snippet = STARTFILENOLB
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"![if !IE]&gt;&lt;p&gt;Text &lt;b&gt;&lt;u&gt;non-validating&lt;/u&gt; downlevel-revealed conditional comment&lt;/b&gt; etc &lt;/p&gt;&lt;![\">code</ut>"
			+ "text"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">"
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"![if !IE]&gt;&lt;p&gt;Text &lt;b&gt;&lt;u&gt;non-validating&lt;/u&gt; downlevel-revealed conditional comment&lt;/b&gt; etc &lt;/p&gt;&lt;![\">code</ut>"
			+ "text"
			+ "</Tuv><Tuv Lang=\"ES-EM\">"
			+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"![if !IE]&gt;&lt;p&gt;Text &lt;b&gt;&lt;u&gt;non-validating&lt;/u&gt; downlevel-revealed conditional comment&lt;/b&gt; etc &lt;/p&gt;&lt;![\">code</ut>"
			+ "text"
			+ "</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testBasicNoTUWithDF () {
		String snippet = STARTFILENOLB
			+ "<df Size=\"16\">text</df>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("text", cont.toString());
		assertTrue(tu.hasTarget(locESEM));
		assertEquals("", tu.getTarget(locESEM).toString());
	}

	@Test
	public void testOutputBasicNoTUWithDFWithSegementation () {
		String snippet = STARTFILENOLB
			+ "<df Size=\"16\">text</df>"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\"><df Size=\"16\">text</df></Tuv>"
			+ "<Tuv Lang=\"ES-EM\"><df Size=\"16\">text</df></Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testVariousTags () {
		String snippet = STARTFILENOLB
			+ "<ut Type=\"start\" Style=\"external\">&lt;p&gt;</ut>paragraph <df Italic=\"on\">"
			+ "<ut Type=\"start\">&lt;i&gt;</ut>text</df><ut Type=\"end\">&lt;/i&gt;</ut>"
			+ "<ut Type=\"end\" Style=\"external\">&lt;/ul&gt;</ut>"
			+ "<ut Type=\"start\" Style=\"external\">&lt;P&gt;</ut>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("paragraph <1/><2>text<3/></2>", fmt.printSegmentedContent(cont, false));
		assertEquals("[paragraph <1/><2>text<3/></2>]", fmt.printSegmentedContent(cont, true));
	}
	
//always segment	@Test
//	public void testOutputVariousTags () {
//		String snippet = STARTFILENOLB
//			+ "<ut Type=\"start\" Style=\"external\">&lt;p&gt;</ut>paragraph <df Italic=\"on\">"
//			+ "<ut Type=\"start\">&lt;i&gt;</ut>text</df><ut Type=\"end\">&lt;/i&gt;</ut>"
//			+ "<ut Type=\"end\" Style=\"external\">&lt;/ul&gt;</ut>"
//			+ "<ut Type=\"start\" Style=\"external\">&lt;P&gt;</ut>"
//			+ "</Raw></Body></TRADOStag>";
//		String expected = STARTFILENOLB
//			+ "<ut Type=\"start\" Style=\"external\">&lt;p&gt;</ut>paragraph <df Italic=\"on\">"
//			+ "<ut Type=\"start\">&lt;i&gt;</ut>text</df><ut Type=\"end\">&lt;/i&gt;</ut>"
//			+ "<ut Type=\"end\" Style=\"external\">&lt;/ul&gt;</ut>"
//			+ "<ut Type=\"start\" Style=\"external\">&lt;P&gt;</ut>"
//			+ "</Raw></Body></TRADOStag>";
//		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterSegNotForced, snippet, locESEM),
//			locESEM, filterSegNotForced.createSkeletonWriter(), filterSegNotForced.getEncoderManager()));
//	}
	
	@Test
	public void testVariousTagsWithSegmentation () {
		String snippet = STARTFILENOLB
			+ "<ut Type=\"start\" Style=\"external\">&lt;p&gt;</ut>"
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">paragraph <df Italic=\"on\">"
			+ "<ut Type=\"start\">&lt;i&gt;</ut>text</df><ut Type=\"end\">&lt;/i&gt;</ut>"
			+ "</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">paragraph <df Italic=\"on\">"
			+ "<ut Type=\"start\">&lt;i&gt;</ut>text</df><ut Type=\"end\">&lt;/i&gt;</ut>"
			+ "</Tuv></Tu> "
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\"><ut Type=\"start\">&lt;i&gt;</ut>text<ut Type=\"end\">&lt;/i&gt;</ut></Tuv>"
			+ "<Tuv Lang=\"ES-EM\"><ut Type=\"start\">&lt;i&gt;</ut>text<ut Type=\"end\">&lt;/i&gt;</ut></Tuv>"
			+ "</Tu>"
			+ "<ut Type=\"end\" Style=\"external\">&lt;/ul&gt;</ut>"
			+ "<ut Type=\"start\" Style=\"external\">&lt;P&gt;</ut>"
			+ "</Raw></Body></TRADOStag>";

		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer tc = tu.getSource();
		assertEquals(2, tc.getSegments().count());
		
		assertEquals(4, tc.get(0).getContent().getCodes().size());
		Code code = tc.get(0).text.getCode(0);
		assertEquals(1, code.getId());
		assertEquals(TTXFilter.DFSTART_TYPE, code.getType());

		code = tc.get(0).text.getCode(2);
		assertEquals(3, code.getId());
		assertEquals(TTXFilter.DFEND_TYPE, code.getType());

		// Second segment (3rd part)
		assertEquals(2, tc.get(2).getContent().getCodes().size());
		code = tc.get(2).text.getCode(0);
		assertEquals("<i>", code.getData());
		assertEquals(TagType.OPENING, code.getTagType());
		// First ID in the second segment should be the continuation of the first segment
		assertEquals(4, code.getId());
	}
	
	@Test
	public void testOutputVariousTagsWithSegmentation () {
		String snippet = STARTFILENOLB
			+ "<ut Type=\"start\" Style=\"external\">&lt;p&gt;</ut>paragraph <df Italic=\"on\">"
			+ "<ut Type=\"start\">&lt;i&gt;</ut>text</df><ut Type=\"end\">&lt;/i&gt;</ut>"
			+ "<ut Type=\"end\" Style=\"external\">&lt;/ul&gt;</ut>"
			+ "<ut Type=\"start\" Style=\"external\">&lt;P&gt;</ut>"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<ut Type=\"start\" Style=\"external\">&lt;p&gt;</ut>"
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">paragraph <df Italic=\"on\">"
			+ "<ut Type=\"start\">&lt;i&gt;</ut>text</df><ut Type=\"end\">&lt;/i&gt;</ut>"
			+ "</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">paragraph <df Italic=\"on\">"
			+ "<ut Type=\"start\">&lt;i&gt;</ut>text</df><ut Type=\"end\">&lt;/i&gt;</ut>"
			+ "</Tuv></Tu>"
			+ "<ut Type=\"end\" Style=\"external\">&lt;/ul&gt;</ut>"
			+ "<ut Type=\"start\" Style=\"external\">&lt;P&gt;</ut>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}
	
// TODO: Do we need to support such tags sequences? <df><ut-start/></df><ut-end/>
//	@Test
//	public void testBadlyNestedTags () {
//		String snippet = STARTFILENOLB
//			+ "<ut Type=\"start\" Style=\"external\">&lt;p&gt;</ut>"
//			+ "Before <df Size=\"8\"><ut Type=\"start\">&lt;FONT face=Arial size=2&gt;</ut>After"
//			+ "<ut Type=\"start\" Style=\"external\">&lt;p style=&quot;background-color: #e0e0e0&quot;&gt;</ut>"
//			+ "Next </df><ut Type=\"end\">&lt;/FONT&gt;</ut>Last."
//			+ "</Raw></Body></TRADOStag>";
//		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
//		assertNotNull(tu);
//		TextContainer cont = tu.getSource();
//		assertEquals("[Before <1/><b2/>After]", fmt.printSegmentedContent(cont, true));
//		assertEquals("Before <FONT face=Arial size=2>After", cont.getFirstContent().toText());
//		tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 2);
//		assertNotNull(tu);
//		cont = tu.getSource();
//		assertEquals("Next <e2/>Last.", fmt.setContent(cont.getFirstContent()).toString());
//		assertEquals("Next </FONT>Last.", cont.getFirstContent().toText());
//	}

	@Test
	public void testWithMixedSegmentation () {
		String snippet = STARTFILENOLB
			+ "<Tu MatchPercent=\"50\"><Tuv Lang=\"EN-US\">text</Tuv></Tu>"
			+ " more text"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("[text][ more text]", fmt.printSegmentedContent(cont, true));
		assertTrue(tu.hasTarget(locESEM));
		cont = tu.getTarget(locESEM);
		assertNotNull(cont);
		assertEquals("[][]", fmt.printSegmentedContent(cont, true));
		ISegments segs = cont.getSegments();
		// Annotation present because of 50% but no target
		AltTranslationsAnnotation ann = segs.get(0).getAnnotation(AltTranslationsAnnotation.class);
		assertNotNull(ann);
		assertEquals(1, ann.size());
		assertEquals("", ann.getFirst().getTarget().toString());
	}

	@Test
	public void testOutputWithMixedSegmentation () {
		String snippet = STARTFILENOLB
			+ "<Tu MatchPercent=\"50\"><Tuv Lang=\"EN-US\">text</Tuv></Tu>"
			+ " more text"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<Tu MatchPercent=\"50\"><Tuv Lang=\"EN-US\">text</Tuv><Tuv Lang=\"ES-EM\">text</Tuv></Tu>"
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\"> more text</Tuv><Tuv Lang=\"ES-EM\"> more text</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testTUInfo () {
		String snippet = STARTFILENOLB
			+ "<Tu Origin=\"abc\" MatchPercent=\"50\"><Tuv Lang=\"EN-US\">en</Tuv><Tuv Lang=\"ES-EM\">es</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getTarget(locESEM);
		assertNotNull(cont);
		ISegments segs = cont.getSegments();
		AltTranslationsAnnotation ann = segs.get(0).getAnnotation(AltTranslationsAnnotation.class);
		assertNotNull(ann);
		assertEquals(1, ann.size());
		assertEquals("es", ann.getFirst().getTarget().toString());
		assertEquals("abc", ann.getFirst().getOrigin());
		assertEquals(50, ann.getFirst().getCombinedScore());
		assertEquals(MatchType.FUZZY, ann.getFirst().getType());
	}

	@Test
	public void testOutputTUInfo () {
		String snippet = STARTFILENOLB
			+ "<Tu Origin=\"abc\" MatchPercent=\"50\"><Tuv Lang=\"EN-US\">en</Tuv><Tuv Lang=\"ES-EM\">es</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<Tu Origin=\"abc\" MatchPercent=\"50\"><Tuv Lang=\"EN-US\">en</Tuv><Tuv Lang=\"ES-EM\">es</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testTUInfoXU () {
		String snippet = STARTFILENOLB
			+ "<Tu Origin=\"xtranslate\" MatchPercent=\"101\"><Tuv Lang=\"EN-US\">en</Tuv><Tuv Lang=\"ES-EM\">es</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getTarget(locESEM);
		assertNotNull(cont);
		ISegments segs = cont.getSegments();
		AltTranslationsAnnotation ann = segs.get(0).getAnnotation(AltTranslationsAnnotation.class);
		assertNotNull(ann);
		assertEquals(1, ann.size());
		assertEquals("es", ann.getFirst().getTarget().toString());
		assertEquals("xtranslate", ann.getFirst().getOrigin());
		assertEquals(101, ann.getFirst().getCombinedScore());
		assertEquals(MatchType.EXACT_LOCAL_CONTEXT, ann.getFirst().getType());
	}

	@Test
	public void testStartingExtraDF () {
		String snippet = STARTFILE
			+ "<df Font=\"z\"><ut Type=\"start\" Style=\"external\">[Z]</ut>"
			+ "Text </df><df Font=\"z\" Bold=\"on\"><ut Type=\"start\">[b]</ut>bold</df>"
			+ "<df Font=\"z\"><ut Type=\"end\">[/b]</ut> after</df>"
			+ "<ut Type=\"end\" Style=\"external\">[/Z]</ut>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("[Text <1/><2>bold<3/><4/></2> after<5/>]", fmt.printSegmentedContent(cont, true).toString());
		XLIFFContent xfmt = new XLIFFContent();
		assertEquals("Text <ph id=\"1\"></ph><bpt id=\"2\">[b]</bpt>bold<ph id=\"3\"></ph><ph id=\"4\"></ph><ept id=\"2\">[/b]</ept> after<ph id=\"5\"></ph>",
			xfmt.setContent(cont.getFirstContent()).toString());
		assertTrue(tu.hasTarget(locESEM));
		assertTrue(tu.getTarget(locESEM).isEmpty());
	}

//always segment	@Test
//	public void testOutputStartingExtraDF () {
//		String snippet = STARTFILENOLB
//			+ "<df Font=\"z\"><ut Type=\"start\" Style=\"external\">[Z]</ut>"
//			+ "Text </df><df Font=\"z\" Bold=\"on\"><ut Type=\"start\">[b]</ut>bold</df>"
//			+ "<df Font=\"z\"><ut Type=\"end\">[/b]</ut> after</df>"
//			+ "<ut Type=\"end\" Style=\"external\">[/Z]</ut>"
//			+ "</Raw></Body></TRADOStag>";
//		String expected = STARTFILENOLB
//			+ "<df Font=\"z\"><ut Type=\"start\" Style=\"external\">[Z]</ut>"
//			+ "Text <df Font=\"z\" Bold=\"on\"><ut Type=\"start\">[b]</ut>bold</df>"
//			+ "<df Font=\"z\"><ut Type=\"end\">[/b]</ut> after</df>"
//			+ "</df>" // closing tag was moved
//			+ "<ut Type=\"end\" Style=\"external\">[/Z]</ut>"
//			+ "</Raw></Body></TRADOStag>";
//		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterSegNotForced, snippet, locESEM),
//			locESEM, filterSegNotForced.createSkeletonWriter(), filterSegNotForced.getEncoderManager()));
//	}

	@Test
	public void testOutputStartingExtraDFWithSegmentation () {
		String snippet = STARTFILENOLB
			+ "<df Font=\"z\"><ut Type=\"start\" Style=\"external\">[Z]</ut>"
			+ "Text </df><df Font=\"z\" Bold=\"on\"><ut Type=\"start\">[b]</ut>bold</df>"
			+ "<df Font=\"z\"><ut Type=\"end\">[/b]</ut> after</df>"
			+ "<ut Type=\"end\" Style=\"external\">[/Z]</ut>"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<df Font=\"z\"><ut Type=\"start\" Style=\"external\">[Z]</ut>"
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">Text <df Font=\"z\" Bold=\"on\"><ut Type=\"start\">[b]</ut>bold</df>"
			+ "<df Font=\"z\"><ut Type=\"end\">[/b]</ut> after</df></Tuv>"
			+ "<Tuv Lang=\"ES-EM\">Text <df Font=\"z\" Bold=\"on\"><ut Type=\"start\">[b]</ut>bold</df>"
			+ "<df Font=\"z\"><ut Type=\"end\">[/b]</ut> after</df></Tuv>"
			+ "</Tu></df>"
			+ "<ut Type=\"end\" Style=\"external\">[/Z]</ut>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testWithPINoTU () {
		String snippet = STARTFILE
			+ "<ut Class=\"procinstr\">pi</ut>text"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("text", cont.toString());
		assertTrue(tu.hasTarget(locESEM));
		assertEquals("", tu.getTarget(locESEM).toString());
	}

	@Test
	public void testNoTUEndsWithUT () {
		String snippet = STARTFILE
			+ "text<ut Class=\"procinstr\">pi</ut>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("text", cont.toString());
		assertTrue(tu.hasTarget(locESEM));
		assertEquals("", tu.getTarget(locESEM).toString());
	}

	@Test
	public void testNoTUContentWithSplitStart () {
		String snippet = STARTFILENOLB
			+ "before <ut Type=\"start\" RightEdge=\"split\">[ulink={</ut>text1<ut Type=\"start\" LeftEdge=\"split\">}]</ut>text2<ut Type=\"end\">[/ulink]</ut> after"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("before [ulink={text1}]text2[/ulink] after", cont.toString());
		assertEquals("before <1>text1<2/>text2</1> after", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testNoTUContentWithUT () {
		String snippet = STARTFILENOLB
			+ "before <ut Type=\"start\">[</ut>in<ut Type=\"end\">]</ut> after"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("before [in] after", cont.toString());
		assertTrue(tu.hasTarget(locESEM));
		assertEquals("", tu.getTarget(locESEM).toString());
	}

//always segment	@Test
//	public void testOutputNoTUContentWithUT () {
//		String snippet = STARTFILENOLB
//			+ "before <ut Type=\"start\">[</ut>in<ut Type=\"end\">]</ut> after"
//			+ "</Raw></Body></TRADOStag>";
//		String expected = STARTFILENOLB
//			+ "before <ut Type=\"start\">[</ut>in<ut Type=\"end\">]</ut> after"
//			+ "</Raw></Body></TRADOStag>";
//		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterSegNotForced, snippet, locESEM),
//			locESEM, filterSegNotForced.createSkeletonWriter(), filterSegNotForced.getEncoderManager()));
//	}

	@Test
	public void testOutputNoTUContentWithUTWithSegmentation () {
		String snippet = STARTFILENOLB
			+ "before <ut Type=\"start\">[</ut>in<ut Type=\"end\">]</ut> after"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">before <ut Type=\"start\">[</ut>in<ut Type=\"end\">]</ut> after</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">before <ut Type=\"start\">[</ut>in<ut Type=\"end\">]</ut> after</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testPartiallySegmentedEntryNothingTranslatable () {
		String snippet = STARTFILENOLB
			+ "{}[]#$%@! <Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">Inside</Tuv></Tu> \t\n-_=+"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		assertEquals("{}[]#$%@! [Inside] \t\n-_=+", fmt.printSegmentedContent(tu.getSource(), true));
	}

	@Test
	public void testPartiallySegmentedEntry () {
		String snippet = STARTFILENOLB
			+ "Outside1 <Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">Inside</Tuv></Tu> Outside2"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		assertEquals("[Outside1 ][Inside][ Outside2]", fmt.printSegmentedContent(tu.getSource(), true));
	}

	@Test
	public void testPartiallySegmentedEntryAfter () {
		String snippet = STARTFILENOLB
			+ "<df Font=\"z\"><ut Type=\"start\" Style=\"external\">[z]</ut>"
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">Src1</Tuv><Tuv Lang=\"ES-EM\">Trg1</Tuv></Tu> Src2"
			+ "</df><ut Type=\"end\" Style=\"external\">[/z]</ut>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		assertEquals("[Src1] [Src2]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[Trg1] []", fmt.printSegmentedContent(tu.getTarget(locESEM), true));
	}

	@Test
	public void testOutputPartiallySegmentedEntryAfter () {
		String snippet = STARTFILENOLB
			+ "<df Font=\"z\"><ut Type=\"start\" Style=\"external\">[z]</ut>"
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">Src1</Tuv><Tuv Lang=\"ES-EM\">Trg1</Tuv></Tu> Src2"
			+ "</df><ut Type=\"end\" Style=\"external\">[/z]</ut>"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<df Font=\"z\"><ut Type=\"start\" Style=\"external\">[z]</ut>"
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">Src1</Tuv><Tuv Lang=\"ES-EM\">Trg1</Tuv></Tu>"
			+ " <Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">Src2</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">Src2</Tuv></Tu>"
			+ "</df><ut Type=\"end\" Style=\"external\">[/z]</ut>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testLargePartiallySegmentedEntry () {
		String snippet = STARTFILENOLB
			+ "Out1<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">In2</Tuv></Tu>"
			+ "Out3<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">In4</Tuv></Tu>"
			+ "Out5"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertEquals("[Out1][In2][Out3][In4][Out5]", fmt.printSegmentedContent(tu.getSource(), true));
	}
	
	@Test
	public void testForExternalDF () {
		String snippet = STARTFILENOLB
			+ "<df Font=\"Arial\"><ut Style=\"external\">code</ut>text<ut Style=\"external\">code</ut></df>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("text", cont.toString());
	}

//always segment	@Test
//	public void testOutputForExternalDF () {
//		String snippet = STARTFILENOLB
//			+ "<df Font=\"Arial\"><ut Style=\"external\">code</ut>text<ut Style=\"external\">code</ut></df>"
//			+ "</Raw></Body></TRADOStag>";
//		String expected = STARTFILENOLB
//			+ "<df Font=\"Arial\"><ut Style=\"external\">code</ut>text<ut Style=\"external\">code</ut></df>"
//			+ "</Raw></Body></TRADOStag>";
//		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterSegNotForced, snippet, locESEM),
//			locESEM, filterSegNotForced.createSkeletonWriter(), filterSegNotForced.getEncoderManager()));
//	}

	@Test
	public void testOutputForExternalDFwithSegmentation () {
		String snippet = STARTFILENOLB
			+ "<df Font=\"Arial\"><ut Style=\"external\">code</ut>text<ut Style=\"external\">code</ut></df>"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<df Font=\"Arial\"><ut Style=\"external\">code</ut>"
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">text</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">text</Tuv></Tu>"
			+ "<ut Style=\"external\">code</ut></df>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testForTwoTUs () {
		String snippet = STARTFILENOLB
			+ "text1<ut Style=\"external\">code</ut>text2"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("text1", cont.toString());
		tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 2);
		assertNotNull(tu);
		cont = tu.getSource();
		assertEquals("text2", cont.toString());
	}

	@Test
	public void testForOneTU () {
		String snippet = STARTFILENOLB
				+ "<ut Type=\"start\" Style=\"external\" RightEdge=\"angle\" DisplayText=\"li\">&lt;li&gt;</ut>"
				+ "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"a\">&lt;a&gt;</ut>"
				+ "text1"
				+ "<ut Style=\"external\" DisplayText=\"br\">&lt;br /&gt;</ut>"
				+ "text2"
				+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"a\">&lt;/a&gt;</ut>"
				+ "<ut Type=\"end\" Style=\"external\" LeftEdge=\"angle\" DisplayText=\"li\">&lt;/li&gt;</ut>"
				+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		assertEquals("<a>text1<br />text2</a>", cont.toString());
	}

	/**
	 * Covers appearing of <ut Type="end"></ut> elements without previously mentioned <ut Type="start"></ut> ones.
	 *
	 * In addition, covers segment starting and stopping by <Tu> elements.
	 */
	@Test
	public void testForOneTUWithTextParts () {
		String snippet = STARTFILENOLB
				+ "<ut Type=\"start\" Style=\"external\" RightEdge=\"angle\" DisplayText=\"p\">&lt;p&gt;</ut>"
				+ "<Tu Origin=\"manual\" MatchPercent=\"0\">"
				+ "<Tuv Lang=\"en\">This <ut DisplayText=\"a\">&lt;a href=http://www.htmlparser.net/&gt;</ut>anchor element<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"a\">&lt;/a&gt;</ut> demonstrates that a tag ending in <df Font=\"Courier New\"><ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"code\">&lt;code&gt;</ut>/&gt;</df><ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"code\">&lt;/code&gt;</ut> is not considered an empty element tag if it has a name that requires an end tag.</Tuv>"
				+ "<Tuv Lang=\"es-EM\">This <ut DisplayText=\"a\">&lt;a href=http://www.htmlparser.net/&gt;</ut>anchor element<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"a\">&lt;/a&gt;</ut> demonstrates that a tag ending in <df Font=\"Courier New\"><ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"code\">&lt;code&gt;</ut>/&gt;</df><ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"code\">&lt;/code&gt;</ut> is not considered an empty element tag if it has a name that requires an end tag.</Tuv>"
				+ "</Tu>"
				+ "  "
				+ "<Tu Origin=\"manual\" MatchPercent=\"0\">"
				+ "<Tuv Lang=\"en\">In this case the final &apos;/&apos; is included in the href attribute value instead of being interpreted as the end of the tag.</Tuv>"
				+ "<Tuv Lang=\"es-EM\">In this case the final &apos;/&apos; is included in the href attribute value instead of being interpreted as the end of the tag.</Tuv>"
				+ "</Tu>"
				+ "\n"
				+ "<ut Style=\"external\" DisplayText=\"p\">&lt;p style=&quot;background-color: #e0e0e0&quot;/&gt;</ut>"
				+ "<Tu Origin=\"manual\" MatchPercent=\"0\">"
				+ "<Tuv Lang=\"en\">The same goes for tags that have an optional end tag like this paragraph, which has a grey background despite the fact that the p element is syntactically an empty element tag.</Tuv>"
				+ "<Tuv Lang=\"es-EM\">The same goes for tags that have an optional end tag like this paragraph, which has a grey background despite the fact that the p element is syntactically an empty element tag.</Tuv>"
				+ "</Tu>"
				+ "<ut Type=\"end\" Style=\"external\" LeftEdge=\"angle\" DisplayText=\"p\">&lt;/p&gt;</ut>"
				+ "</Raw></Body></TRADOStag>";

		List<Event> events = getEvents(filterIncUnSeg, snippet, locESEM);

		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);

		List<TextPart> parts = tu.getSource().getParts();

		assertEquals(4, parts.size());
		assertEquals("This <a href=http://www.htmlparser.net/>anchor element</a> demonstrates that a tag ending in <code>/></code> is not considered an empty element tag if it has a name that requires an end tag.", parts.get(0).toString());
		assertEquals("  ", parts.get(1).toString());
		assertEquals("In this case the final '/' is included in the href attribute value instead of being interpreted as the end of the tag.", parts.get(2).toString());
		assertEquals("\n", parts.get(3).toString());
	}

//always segment	@Test
//	public void testOutputForTwoTUs () {
//		String snippet = STARTFILENOLB
//			+ "text1<ut Style=\"external\">code</ut>text2"
//			+ "</Raw></Body></TRADOStag>";
//		String expected = STARTFILENOLB
//			+ "text1<ut Style=\"external\">code</ut>text2"
//			+ "</Raw></Body></TRADOStag>";
//		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterSegNotForced, snippet, locESEM),
//			locESEM, filterSegNotForced.createSkeletonWriter(), filterSegNotForced.getEncoderManager()));
//	}

	@Test
	public void testOutputForTwoTUsWithSegmentation () {
		String snippet = STARTFILENOLB
			+ "text1<ut Style=\"external\">code</ut>text2"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">text1</Tuv><Tuv Lang=\"ES-EM\">text1</Tuv></Tu>"
			+ "<ut Style=\"external\">code</ut>"
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">text2</Tuv><Tuv Lang=\"ES-EM\">text2</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM), locESEM,
			filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

//always segment	@Test
//	public void testOutputWithPINoTU () {
//		String snippet = STARTFILENOLB
//			+ "<ut Class=\"procinstr\">pi</ut>text"
//			+ "</Raw></Body></TRADOStag>";
//		String expected = STARTFILENOLB
//			+ "<ut Class=\"procinstr\">pi</ut>text"
//			+ "</Raw></Body></TRADOStag>";
//		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterSegNotForced, snippet, locESEM),
//			locESEM, filterSegNotForced.createSkeletonWriter(), filterSegNotForced.getEncoderManager()));
//	}

	@Test
	public void testOutputWithPINoTUWithSegmentation () {
		String snippet = STARTFILENOLB
			+ "<ut Class=\"procinstr\">pi</ut>text"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILENOLB
			+ "<ut Class=\"procinstr\">pi</ut><Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">text</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">text</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testBasicNoUT () {
		String snippet = STARTFILE
			+ "<Tu>"
			+ "<Tuv Lang=\"EN-US\">text en</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">text es</Tuv>"
			+ "</Tu>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertEquals(1, segments.count());
		segments.joinAll();
		assertEquals("text en", cont.toString());
		cont = tu.getTarget(locESEM);
		segments = cont.getSegments();
		assertEquals(1, segments.count());
		segments.joinAll();
		assertEquals("text es", cont.toString());
	}

	@Test
	public void testBasicTwoSegInOneTextUnit () {
		String snippet = STARTFILENOLB
			+ "<Tu><Tuv Lang=\"EN-US\">text1 en</Tuv><Tuv Lang=\"ES-EM\">text1 es</Tuv></Tu>"
			+ "  <Tu><Tuv Lang=\"EN-US\">text2 en</Tuv><Tuv Lang=\"ES-EM\">text2 es</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertEquals(2, segments.count());
		assertEquals("text1 en", segments.get(0).text.toText());
		assertEquals("text2 en", segments.get(1).text.toText());
		assertEquals("[text1 en]  [text2 en]", fmt.printSegmentedContent(cont, true));
		cont = tu.getTarget(locESEM);
		segments = cont.getSegments();
		assertEquals(2, segments.count());
		assertEquals("text1 es", segments.get(0).text.toText());
		assertEquals("text2 es", segments.get(1).text.toText());
		assertEquals("[text1 es]  [text2 es]", fmt.printSegmentedContent(cont, true));

		tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 2);
		assertNull(tu);
	}

	@Test
	public void testOutputBasicTwoSegInOneTextUnit () {
		String snippet = STARTFILENOLB
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">text1 en</Tuv><Tuv Lang=\"ES-EM\">text1 es</Tuv></Tu>"
			+ "  <Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">text2 en</Tuv><Tuv Lang=\"ES-EM\">text2 es</Tuv></Tu>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testBasicWithUT () {
		String snippet = STARTFILE
			+ "<Tu>"
			+ "<Tuv Lang=\"EN-US\">text <ut DisplayText=\"br\">&lt;br/&gt;</ut>en <ut Type=\"start\">&lt;b></ut>bold<ut Type=\"end\">&lt;/b></ut>.</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">TEXT <ut DisplayText=\"br\">&lt;br/&gt;</ut>ES <ut Type=\"start\">&lt;b></ut>BOLD<ut Type=\"end\">&lt;/b></ut>.</Tuv>"
			+ "</Tu>"
			+ "</Raw></Body></TRADOStag>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(filterIncUnSeg, snippet, locESEM), 1);
		assertNotNull(tu);
		TextContainer cont = tu.getSource();
		ISegments segments = cont.getSegments();
		assertEquals(1, segments.count());
		assertEquals("text <br/>en <b>bold</b>.", segments.get(0).text.toText());
		assertEquals("text <1/>en <2>bold</2>.", fmt.setContent(segments.get(0).text).toString());
		cont = tu.getTarget(locESEM);
		segments = cont.getSegments();
		assertEquals(1, segments.count());
		assertEquals("TEXT <br/>ES <b>BOLD</b>.", segments.get(0).text.toText());
		assertEquals("TEXT <1/>ES <2>BOLD</2>.", fmt.setContent(segments.get(0).text).toString());
	}

	@Test
	public void testOutputSimple () {
		String snippet = STARTFILE
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">text en >=gt</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">text es >=gt</Tuv>"
			+ "</Tu>"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILE
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">text en >=gt</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">text es >=gt</Tuv>"
			+ "</Tu>"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testOutputSimpleGTEscaped () {
		String snippet = STARTFILE
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">text en >=gt</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">text es >=gt</Tuv>"
			+ "</Tu>"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILE
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">text en &gt;=gt</Tuv>"
			+ "<Tuv Lang=\"ES-EM\">text es &gt;=gt</Tuv>"
			+ "</Tu>"
			+ "</Raw></Body></TRADOStag>";
		((Parameters)(filterIncUnSeg.getParameters())).setEscapeGT(true);
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

	@Test
	public void testOutputTwoTU () {
		String snippet = STARTFILE
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">text1 en</Tuv><Tuv Lang=\"ES-EM\">text1 es</Tuv></Tu>\n"
			+ "  <ut Style=\"external\">some code</ut>  "
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">text2 en</Tuv><Tuv Lang=\"ES-EM\">text2 es</Tuv></Tu>\n"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILE
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">text1 en</Tuv><Tuv Lang=\"ES-EM\">text1 es</Tuv></Tu>\n"
			+ "  <ut Style=\"external\">some code</ut>  "
			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">text2 en</Tuv><Tuv Lang=\"ES-EM\">text2 es</Tuv></Tu>\n"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}
	
	@Test
	public void testOutputWithOriginalWithoutTraget () {
		String snippet = STARTFILE
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">text1 en</Tuv></Tu>\n"
			+ "  <ut Style=\"external\">some code</ut>  "
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">text2 en</Tuv></Tu>\n"
			+ "</Raw></Body></TRADOStag>";
		String expected = STARTFILE
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">text1 en</Tuv><Tuv Lang=\"ES-EM\">text1 en</Tuv></Tu>\n"
			+ "  <ut Style=\"external\">some code</ut>  "
			+ "<Tu MatchPercent=\"0\">"
			+ "<Tuv Lang=\"EN-US\">text2 en</Tuv><Tuv Lang=\"ES-EM\">text2 en</Tuv></Tu>\n"
			+ "</Raw></Body></TRADOStag>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filterIncUnSeg, snippet, locESEM),
			locESEM, filterIncUnSeg.createSkeletonWriter(), filterIncUnSeg.getEncoderManager()));
	}

//	@Test
//	public void testOutputWithOriginalWithoutTargetKO () {
//		String snippet = STARTFILEKO
//			+ "<Tu><Tuv Lang=\"EN-US\">text1 en</Tuv></Tu>\n"
//			+ "  <ut Style=\"external\">some code</ut>  "
//			+ "<Tu><Tuv Lang=\"EN-US\">text2 en</Tuv></Tu>\n"
//			+ "</Raw></Body></TRADOStag>";
//		String expected = STARTFILEKO
//			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">text1 en</Tuv><Tuv Lang=\"KO-KR\"><df Font=\"\ubd7e\">text1 en</df></Tuv></Tu>\n"
//			+ "  <ut Style=\"external\">some code</ut>  "
//			+ "<Tu MatchPercent=\"0\"><Tuv Lang=\"EN-US\">text2 en</Tuv><Tuv Lang=\"KO-KR\"><df Font=\"\ubd7e\">text2 en</df></Tuv></Tu>\n"
//			+ "</Raw></Body></TRADOStag>";
//		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(filter2, snippet, locKOKR), locKOKR,
//			filter2.createSkeletonWriter()));
//	}

	@Test
	public void testDoubleExtraction () {
		// Read all files in the data directory
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/Test01.html.ttx").toString(), null));
		list.add(new InputDocument(root.in("/Test02_allseg.html.ttx").toString(), null));
		RoundTripComparison rtc = new RoundTripComparison();
		// Use non-forced segmentation output
		assertTrue(rtc.executeCompare(filterIncUnSeg, list, "UTF-8", locENUS, locFRFR));
	}
	
	@Test
	public void textDoubleExtractionOriginalAllSegmented () {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/Test02_allseg.html.ttx").toString(), null));
		RoundTripComparison rtc = new RoundTripComparison();
		// Use the output with forced segmentation, since all is segmented 
		// in the input file we should not add any.
		assertTrue(rtc.executeCompare(filterIncUnSeg, list, "UTF-8", locENUS, locFRFR));
	}

	// Disable this test for SVN commit
//	@Test
	public void __LOCALTEST_ONLY_testDoubleExtractionPrivateFiles () {
		// Read all files in the data directory
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		RoundTripComparison rtc = new RoundTripComparison();

		list.clear();
		list.add(new InputDocument(root.in("/private/set01/file01.docx.ttx").toString(), null));
		assertTrue(rtc.executeCompare(filterIncUnSeg, list, "UTF-8", locENUS, locFRFR));

//		list.clear();
//		list.add(new InputDocument(root+"private/set01/file01.xml.ttx", null));
//		list.add(new InputDocument(root+"private/set01/file02.xml.ttx", null));
//		list.add(new InputDocument(root+"private/set01/file03.xml.ttx", null));
//		assertTrue(rtc.executeCompare(filterSegNotForced, list, "UTF-8", locENUS, locFRFR));

//		list.clear();
//		list.add(new InputDocument(root+"private/set02/file01.xml.ttx", null));
//		list.add(new InputDocument(root+"private/set02/file02.xml.ttx", null));
//		list.add(new InputDocument(root+"private/set02/file03.xml.ttx", null));
//		list.add(new InputDocument(root+"private/set02/file04.xml.ttx", null));
//		list.add(new InputDocument(root+"private/set02/file05.xml.ttx", null));
//		LocaleId locFRCA = LocaleId.fromString("FR-CA");
//		assertTrue(rtc.executeCompare(filterSegNotForced, list, "UTF-8", locENUS, locFRCA));

//		list.clear();
//		list.add(new InputDocument(root+"private/set03/file01.xml.ttx", null));
//		list.add(new InputDocument(root+"private/set03/file02.xml.ttx", null));
//		LocaleId locDEDE = LocaleId.fromString("DE-DE");
//		LocaleId locENGB = LocaleId.fromString("EN-GB");
//		assertTrue(rtc.executeCompare(filterSegNotForced, list, "UTF-8", locDEDE, locENGB));

//		list.clear();
//		list.add(new InputDocument(root+"private/set03/file03.xml.ttx", null));
//		list.add(new InputDocument(root+"private/set03/file04.xml.ttx", null));
//		LocaleId locZHCN = LocaleId.fromString("ZH-CN");
//		assertTrue(rtc.executeCompare(filterSegNotForced, list, "UTF-8", locDEDE, locZHCN));

//		list.clear();
//		list.add(new InputDocument(root+"private/set03/file05.xml.ttx", null));
//		list.add(new InputDocument(root+"private/set03/file06.xml.ttx", null));
//		LocaleId locNLNL = LocaleId.fromString("NL-NL");
//		assertTrue(rtc.executeCompare(filterSegNotForced, list, "UTF-8", locDEDE, locNLNL));

//		list.clear();
//		list.add(new InputDocument(root+"private/set04/file01.mif.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set04/file02.mif.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set04/file03.mif.rtf.ttx", null));
//		assertTrue(rtc.executeCompare(filterSegNotForced, list, "UTF-8", locENGB, locFRFR));

//		list.clear();
//		list.add(new InputDocument(root+"private/set05/file01.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file02.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file03.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file04.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file05.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file06.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file07.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file08.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file09.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file10.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file11.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file12.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file13.rtf.ttx", null));
//		list.add(new InputDocument(root+"private/set05/file14.rtf.ttx", null));
//		LocaleId locPL = LocaleId.fromString("PL");
//		assertTrue(rtc.executeCompare(filterSegNotForced, list, "UTF-8", locENGB, locPL));
	}

	private ArrayList<Event> getEvents(IFilter filter, String snippet, LocaleId trgLocId) {
		return FilterTestDriver.getEvents(filter, snippet, locENUS, trgLocId);
	}

}
