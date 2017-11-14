/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.tkit.merger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiMergeException;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.AlignmentStatus;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.IAlignedSegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.segmentation.LanguageMap;
import net.sf.okapi.lib.segmentation.Rule;
import net.sf.okapi.lib.segmentation.SRXDocument;
import net.sf.okapi.lib.tkit.merge.ITextUnitMerger;
import net.sf.okapi.lib.tkit.merge.Parameters;
import net.sf.okapi.lib.tkit.merge.TextUnitMerger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TextUnitMergerTest {

	private static final String XLFSTART = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xliff version=\"1.2\">"
			+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
			+ "<body><trans-unit id=\"1\"><source>\n";
	private static final String XLFEND = "\n</trans-unit></body></file></xliff>";
	
	private HtmlFilter htmlFilter;
	private ITextUnitMerger merger;

	@Before
	public void setUp() {
		htmlFilter = new HtmlFilter();
		merger = new TextUnitMerger();
		merger.setTargetLocale(LocaleId.FRENCH);
		merger.setParameters(new Parameters());
	}

	@After
	public void tearDown() {
		htmlFilter.close();
	}
	
	@Test
	public void mergeWithoutCodes() {
		String srcSnippet = "<p>Before bold after.</p>";
		String trgSnippet = "<p>french for what he said.</p>";
		ITextUnit tuFromSkeleton = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, srcSnippet, LocaleId.ENGLISH), 1);
		ITextUnit tuFromTrans = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, trgSnippet, LocaleId.FRENCH), 1);
		ITextUnit mergedTu = merger.mergeTargets(tuFromSkeleton, tuFromTrans);
		assertEquals("french for what he said.", mergedTu.getTarget(LocaleId.FRENCH).toString());
	}
	
	@Test
	public void mergeWithBalancedCodes() {
		String srcSnippet = "<p>Before <b>bold</b> <img href=\"there\" alt=\"text\"/> after.</p>";
		String trgSnippet = "<p>XXXXXX <b>XXXX <img href=\"XXXXX\" alt=\"XXXX\"/> XXXX</b>.</p>";
		ITextUnit tuFromSkeleton = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, srcSnippet, LocaleId.ENGLISH), 2);
		ITextUnit tuFromTrans = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, trgSnippet, LocaleId.FRENCH), 2);
		ITextUnit mergedTu = merger.mergeTargets(tuFromSkeleton, tuFromTrans);
		assertEquals("XXXXXX <b>XXXX [#$dp1] XXXX</b>.", mergedTu.getTarget(LocaleId.FRENCH).toString());
	}
	
	@Test
	public void mergeWithStandaloneCodes() {
		// Made up example - we force b,i to be stand alone codes for testing
		String srcSnippet = "<p>Before <b>bold<i> after.</p>";
		String trgSnippet = "<p>XXXXXX <i>XXXX<b> XXXX.</p>";
		ITextUnit tuFromSkeleton = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, srcSnippet, LocaleId.ENGLISH), 1);
		ITextUnit tuFromTrans = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, trgSnippet, LocaleId.FRENCH), 1);
		ITextUnit mergedTu = merger.mergeTargets(tuFromSkeleton, tuFromTrans);
		assertEquals("XXXXXX <i>XXXX<b> XXXX.", mergedTu.getTarget(LocaleId.FRENCH).toString());
	}
	
	@Test
	public void mergeWithMovedBalancedCodes() {
		String srcSnippet = "<p><i>Before</i> <b>bold</b> after.</p>";
		String trgSnippet = "<p><b>XXXXXX</b> <i>XXXX</i> XXXX.</p>";
		ITextUnit tuFromSkeleton = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, srcSnippet, LocaleId.ENGLISH), 1);
		ITextUnit tuFromTrans = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, trgSnippet, LocaleId.FRENCH), 1);
		ITextUnit mergedTu = merger.mergeTargets(tuFromSkeleton, tuFromTrans);
		assertEquals("<b>XXXXXX</b> <i>XXXX</i> XXXX.", mergedTu.getTarget(LocaleId.FRENCH).toString());
	}
	
	@Test(expected=OkapiMergeException.class)
	public void mergeWithMissingBalancedCodes() {
		String srcSnippet = "<p><i>Before</i> <b>bold</b> after.</p>";
		String trgSnippet = "<p><b>XXXXXX</b> XXXX XXXX.</p>";
		ITextUnit tuFromSkeleton = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, srcSnippet, LocaleId.ENGLISH), 1);
		ITextUnit tuFromTrans = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, trgSnippet, LocaleId.FRENCH), 1);
		
		ITextUnitMerger m = new TextUnitMerger();
		m.setTargetLocale(LocaleId.FRENCH);
		Parameters p = new Parameters();
		p.setThrowCodeException(true);
		m.setParameters(p);
		
		ITextUnit mergedTu = m.mergeTargets(tuFromSkeleton, tuFromTrans);
		assertEquals("<b>XXXXXX</b> XXXX XXXX.", mergedTu.getTarget(LocaleId.FRENCH).toString());
	}
	
	@Test(expected=OkapiMergeException.class)
	public void mergeWithAddedBalancedCodes() {
		String srcSnippet = "<p><i>Before</i> <b>bold</b> after.</p>";
		String trgSnippet = "<p><i>XXXXXX</i> <b>XXXX</b> <u><u>XXXX</u></u>.</p>";
		ITextUnit tuFromSkeleton = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, srcSnippet, LocaleId.ENGLISH), 1);
		ITextUnit tuFromTrans = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, trgSnippet, LocaleId.FRENCH), 1);
		
		ITextUnitMerger m = new TextUnitMerger();
		m.setTargetLocale(LocaleId.FRENCH);
		Parameters p = new Parameters();
		p.setThrowCodeException(true);
		m.setParameters(p);
		
		ITextUnit mergedTu = m.mergeTargets(tuFromSkeleton, tuFromTrans);
		assertEquals("<i>XXXXXX</i> <b>XXXX</b> <u><u>XXXX</u></u>.", mergedTu.getTarget(LocaleId.FRENCH).toString());
	}
	
	@Test
	public void mergeCodesWithSegments() {
		String srcSnippet = "A segment without codes. A segment with <b>codes</b>. <i>And another segment</i>.";
		String trgSnippet = "A segment without codes and segment with <i>codes</i>. <b>And another segment</b>.";
		ITextUnit tuFromSkeleton = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, srcSnippet, LocaleId.ENGLISH), 1);
		tuFromSkeleton.createSourceSegmentation(createSegmenterWithRules(LocaleId.ENGLISH));
		ITextUnit tuFromTrans = FilterTestDriver.getTextUnit(
				FilterTestDriver.getEvents(htmlFilter, trgSnippet, LocaleId.FRENCH), 1);
		tuFromTrans.createSourceSegmentation(createSegmenterWithRules(LocaleId.FRENCH));
		ITextUnit mergedTu = merger.mergeTargets(tuFromSkeleton, tuFromTrans);
		
		assertEquals("A segment without codes and segment with <i>codes</i>. <b>And another segment</b>.", 
				mergedTu.getTarget(LocaleId.FRENCH).toString());
		
		// assume segments are aligned unless otherwise changed
		IAlignedSegments segments = mergedTu.getAlignedSegments();
		assertEquals(AlignmentStatus.ALIGNED, segments.getAlignmentStatus());
		
		assertEquals(1, mergedTu.getSourceSegments().count());
		
		Segment src = segments.getSource(0, LocaleId.FRENCH);
		Segment trg = segments.getCorrespondingTarget(src, LocaleId.FRENCH);
		assertNotNull(trg);
		GenericContent fmt = new GenericContent();
		assertEquals("[A segment without codes. A segment with <b>codes</b>. <i>And another segment</i>.]",
			fmt.printSegmentedContent(mergedTu.getSource(), true, true));
		assertEquals("[A segment without codes and segment with <i>codes</i>. <b>And another segment</b>.]",
				fmt.printSegmentedContent(mergedTu.getTarget(LocaleId.FRENCH), true, true));
	}
	
	@Test
	public void testXLIFF () {
		XLIFFFilter xliffFilter = new XLIFFFilter();
		String srcSnippet = XLFSTART + "<ph id=\"1\" ts=\"z\">C1</ph>Source. Text.</source>\n"
			+ "<seg-source><mrk id=\"s1\" mtype=\"seg\"><ph id=\"1\" ts=\"z\">C1</ph>Source.</mrk> "
			+ "<mrk id=\"s2\" mtype=\"seg\">Text.</mrk></seg-source>\n"
			+ XLFEND;
		String trgSnippet = XLFSTART + "<ph id=\"1\" ts=\"z\">C1</ph>Source. Text.</source>\n"
			+ "<seg-source><mrk id=\"s1\" mtype=\"seg\"><ph id=\"1\" ts=\"z\">C1</ph>Source.</mrk> "
			+ "<mrk id=\"s2\" mtype=\"seg\">Text.</mrk></seg-source>\n"
			+ "<target><mrk id=\"s1\" mtype=\"seg\"><ph id=\"1\" ts=\"z\">fromT</ph>Target1.</mrk> "
			+ "<mrk id=\"s2\" mtype=\"seg\">Target2.</mrk></target>\n"
			+ XLFEND;
		ITextUnit tuFromSkeleton = FilterTestDriver.getTextUnit(
			FilterTestDriver.getEvents(xliffFilter, srcSnippet, LocaleId.ENGLISH, LocaleId.FRENCH), 1);
		ITextUnit tuFromTrans = FilterTestDriver.getTextUnit(
			FilterTestDriver.getEvents(xliffFilter, trgSnippet, LocaleId.ENGLISH, LocaleId.FRENCH), 1);
		assertEquals(true, tuFromTrans.hasTarget(LocaleId.FRENCH));
		ITextUnit mergedTu = merger.mergeTargets(tuFromSkeleton, tuFromTrans);

		TextContainer tc = mergedTu.getTarget(LocaleId.FRENCH);
		assertEquals("fromTTarget1. Target2.", tc.toString());
		Code code = tc.getFirstContent().getCode(0);
		assertEquals("<ph id=\"1\" ts=\"z\">fromT</ph>", code.getOuterData());
		assertEquals(2, tc.getSegments().count());
		
		xliffFilter.close();
	}
	
	private ISegmenter createSegmenterWithRules(LocaleId locId) {
		SRXDocument doc = new SRXDocument();
		LanguageMap langMap = new LanguageMap(".*", "default");
		doc.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<Rule>();
		langRules.add(new Rule("\\.", "\\s", true));
		// Add the rules to the document
		doc.addLanguageRule("default", langRules);
		// Create the segmenter
		return doc.compileLanguageRules(locId, null);
	}

}
