/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.lib.segmentation;


import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.InlineAnnotation;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class SRXSegmenterTest {

	private GenericContent fmt = new GenericContent();
	
	@Before
	public void setUp() {
	}

	@Test
	public void testDefaultOptions () {
		SRXSegmenter seg = new SRXSegmenter();
		// Check default options
		assertFalse(seg.cascade());
		assertTrue(seg.segmentSubFlows());
		assertFalse(seg.includeStartCodes());
		assertTrue(seg.includeEndCodes());
		assertFalse(seg.includeIsolatedCodes());
		assertFalse(seg.oneSegmentIncludesAll());
		assertFalse(seg.trimLeadingWhitespaces());
		assertFalse(seg.trimTrailingWhitespaces());
		assertFalse(seg.treatIsolatedCodesAsWhitespace());
	}		
	
	@Test
	public void testChangedOptions () {
		SRXSegmenter seg = new SRXSegmenter();
		// Check changing options
		seg.setOptions(false, true, false, true, true, true, true, true, false, true);
		assertFalse(seg.segmentSubFlows());
		assertTrue(seg.includeStartCodes());
		assertFalse(seg.includeEndCodes());
		assertTrue(seg.includeIsolatedCodes());
		assertTrue(seg.oneSegmentIncludesAll());
		assertTrue(seg.trimLeadingWhitespaces());
		assertTrue(seg.trimTrailingWhitespaces());
		assertTrue(seg.useJavaRegex());
		assertTrue(seg.treatIsolatedCodesAsWhitespace());
	}
	
	@Test
	public void testSimpleSegmentationDefault () {
		ISegmenter seg = createSegmenterWithRules(LocaleId.fromString("en"));
		TextContainer tc = new TextContainer("Part 1.  Part 2. ");
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(3, n);
		segments.create(seg.getRanges());
		assertEquals(3, segments.count());
		assertEquals("Part 1.", segments.get(0).toString());
		assertEquals("  Part 2.", segments.get(1).toString());
		assertEquals(" ", segments.get(2).toString());
	}
	
	@Test
	public void testNegativeRules () {
		ISegmenter seg = createSegmenterWithRules2(LocaleId.fromString("en"));
		TextContainer tc = new TextContainer("Part A. Part B.");
		ISegments segments = tc.getSegments();
		seg.computeSegments(tc);
		segments.create(seg.getRanges());
		assertEquals(4, segments.count());
		assertEquals("Part A", segments.get(0).toString());
		assertEquals(".", segments.get(1).toString());
		assertEquals(" Part B", segments.get(2).toString());
		assertEquals(".", segments.get(3).toString());
	}
	
	@Test
	public void testSimpleSegmentationNewLines () {
		ISegmenter seg = createSegmenterWithNewLineRules(LocaleId.fromString("en"));
		TextFragment tf = new TextFragment();
		TextContainer tc = new TextContainer(tf);
		tf.append("\n");
		tf.append(new Code(TagType.PLACEHOLDER, "x", "Part 1."));
		tf.append("\n");
		tf.append(new Code(TagType.PLACEHOLDER, "x", "Part 2."));
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(3, n);
		segments.create(seg.getRanges());
		assertEquals(3, segments.count());
		assertEquals("\n", segments.get(0).toString());
		assertEquals("Part 1.\n", segments.get(1).toString());
		assertEquals("Part 2.", segments.get(2).toString());
	}
	
	@Test
	// Issue 456
	public void testEmptyBeforeRule () {
		ISegmenter seg = createSegmenterWithEmptyBeforeRule(LocaleId.fromString("en"));
		TextContainer tc = new TextContainer("01.07.2014.");
		ISegments segments = tc.getSegments();
		seg.computeSegments(tc);
		segments.create(seg.getRanges());
		assertEquals(1, segments.count());
		assertEquals("01.07.2014.", segments.get(0).toString());
	}
	
	@Test
	public void testGermanAbbrNoBreakFullSrx() {
		ISegmenter seg;
		SRXDocument doc = new SRXDocument();
		FileLocation location = FileLocation.fromClass(SRXSegmenterTest.class).in("/language_tools_german.srx");
		doc.loadRules(location.asInputStream());
		seg = doc.compileLanguageRules(LocaleId.GERMAN, null);
		
		ITextUnit tu = new TextUnit("temp", "Aus denen er schöpfen konnte d. h. natürlich.");
		tu.createSourceSegmentation(seg);
		ISegments segments = tu.getSource().getSegments();
		
		assertEquals(1, segments.count());
		assertEquals("Aus denen er schöpfen konnte d. h. natürlich.", segments.get(0).toString());
	}
	
	// originally Failed with ICU rules, passed with java regex rules (java regex now default)
	@Test
	public void testGermanAbbrNoBreakConflictingRules() {
		ISegmenter seg = createSegmenterWithGermanNoBreak(LocaleId.GERMAN);	
		TextContainer tc = new TextContainer("Aus denen er schöpfen konnte d. h. natürlich.");
		ISegments segments = tc.getSegments();
		seg.computeSegments(tc);
		segments.create(seg.getRanges());
		assertEquals(1, segments.count());
		assertEquals("Aus denen er schöpfen konnte d. h. natürlich.", segments.get(0).toString());
	}
	
	private ISegmenter createSegmenterWithNewLineRules (LocaleId locId) {
		SRXDocument doc = new SRXDocument();
		LanguageMap langMap = new LanguageMap(".*", "default");
		doc.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<Rule>();
		langRules.add(new Rule("\\n", "", true));
		// Add the ruls to the document
		doc.addLanguageRule("default", langRules);
		// Create the segmenter
		return doc.compileLanguageRules(locId, null);
	}
	
	@Test
	public void testSimpleSegmentationTrimLeading () {
		SRXSegmenter seg = (SRXSegmenter)createSegmenterWithRules(LocaleId.fromString("en"));
		seg.setOptions(seg.segmentSubFlows(), seg.includeStartCodes(), seg.includeEndCodes(), seg.includeIsolatedCodes(),
			seg.oneSegmentIncludesAll(), true, false, seg.useJavaRegex(), false, false);
		TextContainer tc = new TextContainer(" Part 1.  Part 2. ");
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(2, n);
		segments.create(seg.getRanges());
		assertEquals(2, segments.count());
		assertEquals("Part 1.", segments.get(0).toString());
		assertEquals("Part 2.", segments.get(1).toString());
	}
	
	@Test
	public void testSimpleSegmentationTrimTrailing () {
		SRXSegmenter seg = (SRXSegmenter)createSegmenterWithRules(LocaleId.fromString("en"));
		seg.setOptions(seg.segmentSubFlows(), seg.includeStartCodes(), seg.includeEndCodes(), seg.includeIsolatedCodes(),
				seg.oneSegmentIncludesAll(), false, true, seg.useJavaRegex(), false, false);
		TextContainer tc = new TextContainer(" Part 1.  Part 2. ");
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(2, n);
		segments.create(seg.getRanges());
		assertEquals(2, segments.count());
		assertEquals(" Part 1.", segments.get(0).toString());
		assertEquals("  Part 2.", segments.get(1).toString());
	}
	
	@Test
	public void testSimpleSegmentationOneIsAll () {
		SRXSegmenter seg = (SRXSegmenter)createSegmenterWithRules(LocaleId.fromString("en"));
		seg.setOptions(seg.segmentSubFlows(), seg.includeStartCodes(), seg.includeEndCodes(), seg.includeIsolatedCodes(),
				true, true, true, seg.useJavaRegex(), false, false);
		TextContainer tc = new TextContainer(" Part 1  ");
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(1, n);
		segments.create(seg.getRanges());
		assertEquals(1, segments.count());
		assertEquals(" Part 1  ", segments.get(0).toString());
	}
	
	@Test
	public void testTUSegmentation () {
		ITextUnit tu = createMultiTargetSegmentedTextUnit();
		// Check default source segmentation
		ISegments segs = tu.getSource().getSegments();
		assertEquals(3, segs.count());
		assertEquals("Part 1.", segs.get(0).toString());
		assertEquals(" Part 2.", segs.get(1).toString());
		assertEquals(" Part 3.", segs.get(2).toString());
		// Default should be like for German
		segs = tu.getSourceSegments();
		assertEquals(" Part 2.", segs.get(1).toString());
	}

	@Test
	public void testCodedSegmentationDefault1 () {
		SRXSegmenter seg = (SRXSegmenter)createSegmenterWithRules(LocaleId.fromString("en"));
		seg.setOptions(seg.segmentSubFlows(), false, true, false,
				false, false, false, seg.useJavaRegex(), false, false);
		// start = false, end = true, isolated = false		
		TextContainer tc = createCodedText();
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(5, n);
		segments.create(seg.getRanges());
		assertEquals(5, segments.count());
		assertEquals("PH after.", segments.get(0).toString());
		assertEquals("<br/><b> End after.</b>", segments.get(1).toString());
		assertEquals(" Start after.", segments.get(2).toString());
		assertEquals("<i> Text.</i>", segments.get(3).toString());
		assertEquals("  ", segments.get(4).toString());
	}
	
	@Test
	public void testCodedSegmentationNotDefault1 () {
		SRXSegmenter seg = (SRXSegmenter)createSegmenterWithRules(LocaleId.fromString("en"));
		seg.setOptions(seg.segmentSubFlows(), true, false, true,
				false, false, false, seg.useJavaRegex(), false, false);
			// start = true, end = false, isolated = true
		TextContainer tc = createCodedText();
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(5, n);
		segments.create(seg.getRanges());
		assertEquals(5, segments.count());
		assertEquals("PH after.<br/><b>", segments.get(0).toString());
		assertEquals(" End after.", segments.get(1).toString());
		assertEquals("</b> Start after.<i>", segments.get(2).toString());
		assertEquals(" Text.", segments.get(3).toString());
		assertEquals("</i>  ", segments.get(4).toString());				
	}
	
	@Test
	public void testCodedSegmentationDefault2 () {
		SRXSegmenter seg = (SRXSegmenter)createSegmenterWithRules(LocaleId.fromString("en"));
		seg.setOptions(seg.segmentSubFlows(), false, true, false,
				false, false, false, seg.useJavaRegex(), false, false);
			// start = false, end = true, isolated = false
		TextContainer tc = createCodedText2();
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(5, n);
		segments.create(seg.getRanges());
		assertEquals(5, segments.count());
		assertEquals("PH after.", segments.get(0).toString());
		assertEquals("<br/><br/><b><i> End after.</i></b>", segments.get(1).toString());
		assertEquals(" Start after.", segments.get(2).toString());
		assertEquals("<u><i> Text.</i></u>", segments.get(3).toString());
		assertEquals("  ", segments.get(4).toString());
	}

	@Test
	public void testCodedSegmentationNotDefault2 () {
		SRXSegmenter seg = (SRXSegmenter)createSegmenterWithRules(LocaleId.fromString("en"));
		seg.setOptions(seg.segmentSubFlows(), true, false, true,
				false, false, false, seg.useJavaRegex(), false, false);
			// start = true, end = false, isolated = true
		TextContainer tc = createCodedText2();
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(5, n);
		segments.create(seg.getRanges());
		assertEquals(5, segments.count());
		assertEquals("PH after.<br/><br/><b><i>", segments.get(0).toString());
		assertEquals(" End after.", segments.get(1).toString());
		assertEquals("</i></b> Start after.<u><i>", segments.get(2).toString());
		assertEquals(" Text.", segments.get(3).toString());
		assertEquals("</i></u>  ", segments.get(4).toString());
	}
	
	
	@Test
	public void testTUSegmentationRemoval () {
		ITextUnit tu = createMultiTargetSegmentedTextUnit();
		// Removing the target does not change the source associated with it
		tu.removeTarget(LocaleId.FRENCH);
		ISegments segs = tu.getSource().getSegments();
		assertEquals(3, segs.count());
		assertEquals(" Part 2.", segs.get(1).toString());
	}
	
	@Test
	public void testTUSegmentationRemovalAll () {
		ITextUnit tu = createMultiTargetSegmentedTextUnit();
		tu.removeAllSegmentations();
		// Nothing is segmented now
		ISegments segs = tu.getSource().getSegments();
		assertEquals(1, segs.count());
		assertEquals("Part 1. Part 2. Part 3.", tu.getSource().getLastContent().toText());
	}
	
	@Test
	public void testICUSpecificPatterns () {
		SRXDocument doc = new SRXDocument();
		LanguageMap langMap = new LanguageMap(".*", "default");
		doc.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<Rule>();
		langRules.add(new Rule("\\w", "\\s", true));
		langRules.add(new Rule("\\d", "\\s", true));
		langRules.add(new Rule("\\u301c", "\\s", true));
		langRules.add(new Rule("z", "\\x{0608}", true));
		doc.addLanguageRule("default", langRules);
		// Create the segmenter
		ISegmenter segmenter = doc.compileLanguageRules(LocaleId.ENGLISH, null);
		
		assertEquals(2, segmenter.computeSegments("e ")); // e + space
		assertEquals(2, segmenter.computeSegments("e\u00a0")); // e + nbsp
		assertEquals(2, segmenter.computeSegments("e\u1680")); // e + Ogham space
		assertEquals(2, segmenter.computeSegments("\u0104 ")); // A-ogonek + space
		assertEquals(2, segmenter.computeSegments("\u0104\u00a0")); // A-ogonek + nbsp
		assertEquals(2, segmenter.computeSegments("\u0104\u1680")); // A-ogonek + Ogham space

		assertEquals(2, segmenter.computeSegments("1 ")); // 1 + space
		assertEquals(2, segmenter.computeSegments("\u0b66 ")); // Oryia zero + space
		assertEquals(2, segmenter.computeSegments("\uff19 ")); // Full-width 9 + space
		assertEquals(2, segmenter.computeSegments("1\u1680")); // 1 + Ogham space
		assertEquals(2, segmenter.computeSegments("\u0b66\u1680")); // Oryia zero + Ogham space
		assertEquals(2, segmenter.computeSegments("\uff19\u1680")); // Full-width 9 + Ogham space
		
		assertEquals(2, segmenter.computeSegments("\u301c\u1680")); // wave-dash + Ogham space

		assertEquals(2, segmenter.computeSegments("z\u0608")); // z + Arabic ray

		assertEquals(1, segmenter.computeSegments("\u20ac\u1680")); // Euro + Ogham space -> no break
	}
	
	@Test
	public void testWithWithoutTrailingWhitespace() {
		ISegmenter seg = createSegmenterWithRules(LocaleId.ENGLISH);		
		
		ITextUnit tu = new TextUnit("1", "This sentence should not be split.");
		seg.computeSegments(tu.getSource());
		tu.getSource().getSegments().create(seg.getRanges());
		assertEquals("[This sentence should not be split.]", fmt.printSegmentedContent(tu.getSource(), true));
		
		tu = new TextUnit("1", "This sentence should not be split. ");
		seg.computeSegments(tu.getSource());
		tu.getSource().getSegments().create(seg.getRanges());
		assertEquals("[This sentence should not be split.][ ]", fmt.printSegmentedContent(tu.getSource(), true));
	}
	
	@Test
	// ABCDEFGHIJK
	// 012345678901234567890
	// 0         1         2
	// ABC11DE22FG3344HIJ55K
	public void testStoreCodePositions() {
		SRXSegmenter seg = new SRXSegmenter();
		TextFragment tf = new TextFragment();
		tf.append("ABC");
		tf.append(new Code(TagType.PLACEHOLDER, "11"));
		tf.append("DE");
		tf.append(new Code(TagType.PLACEHOLDER, "22"));
		tf.append("FG");
		tf.append(new Code(TagType.PLACEHOLDER, "33"));
		tf.append(new Code(TagType.PLACEHOLDER, "44"));
		tf.append("HIJ");
		tf.append(new Code(TagType.PLACEHOLDER, "55"));
		tf.append("K");
		
		String text = tf.getCodedText();		
		List<Integer> codePositions = seg.storeCodePositions(text);
		assertEquals(5, codePositions.size());
		
		assertEquals(3, (int)codePositions.get(0));
		assertEquals(5, (int)codePositions.get(1));
		assertEquals(7, (int)codePositions.get(2));
		assertEquals(7, (int)codePositions.get(3));
		assertEquals(10, (int)codePositions.get(4));

		List<Integer> originalCodePositions = seg.storeOriginalCodePositions(text);

		assertEquals(1, seg.recalcPos(text, 1, codePositions, originalCodePositions));
		assertEquals(2, seg.recalcPos(text, 2, codePositions, originalCodePositions));
		assertEquals(3, seg.recalcPos(text, 3, codePositions, originalCodePositions));
		assertEquals(6, seg.recalcPos(text, 4, codePositions, originalCodePositions));
		assertEquals(7, seg.recalcPos(text, 5, codePositions, originalCodePositions));
		assertEquals(10, seg.recalcPos(text, 6, codePositions, originalCodePositions));
		assertEquals(11, seg.recalcPos(text, 7, codePositions, originalCodePositions));
		assertEquals(16, seg.recalcPos(text, 8, codePositions, originalCodePositions));
		assertEquals(17, seg.recalcPos(text, 9, codePositions, originalCodePositions));
		assertEquals(18, seg.recalcPos(text, 10, codePositions, originalCodePositions));
		assertEquals(21, seg.recalcPos(text, 11, codePositions, originalCodePositions));
	}

	@Test
	public void testMRK () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "span", "<span>");
		tf.append("Sentence one. Sentence two.");
		tf.append(TagType.CLOSING, "span", "</span>");
		assertEquals("<span>Sentence one. Sentence two.</span>", tf.toText());
		
		// ##Sentence one. Sentence two.##
		// 0123456789012345678901234567890
		tf.annotate(3, 28, "anno", new InlineAnnotation("metadata")); // keep 1 char between annotation and span (to ensure separate codes)
		assertEquals("<1>S<2>entence one. Sentence two</2>.</1>", fmt.setContent(tf).toString());
		
		ISegmenter seg = createSegmenterWithRules(LocaleId.ENGLISH);
		TextContainer tc = new TextContainer(tf);
		assertEquals(2, seg.computeSegments(tc));
		tc.getSegments().create(seg.getRanges());
		assertEquals("[<span>Sentence one.][ Sentence two.</span>]", fmt.printSegmentedContent(tc, true, true));
		assertEquals("[<b1/>S<b2/>entence one.][ Sentence two<e2/>.<e1/>]", fmt.printSegmentedContent(tc, true, false));
	}

	@Test
	public void testTreatIsolatedCodesAsWhitespace () {
		ISegmenter seg = createSegmenterWithRules(LocaleId.fromString("en"));
		seg.setTreatIsolatedCodesAsWhitespace(true);
		TextFragment tf = new TextFragment();
		TextContainer tc = new TextContainer(tf);
		tf.append("Hello.");
		tf.append(new Code(TagType.PLACEHOLDER, "x", "x"));
		tf.append("To the.");
		tf.append(new Code(TagType.PLACEHOLDER, "x", "x"));
		tf.append("World.");
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(3, n);
		segments.create(seg.getRanges());
		assertEquals(3, segments.count());
		assertEquals("Hello.", segments.get(0).toString());
		assertEquals("xTo the.", segments.get(1).toString());
		assertEquals("xWorld.", segments.get(2).toString());
	}

	@Test
	public void testDontTreatNonIsolatedCodesAsWhitespace () {
		ISegmenter seg = createSegmenterWithRules(LocaleId.fromString("en"));
		seg.setTreatIsolatedCodesAsWhitespace(true);
		TextFragment tf = new TextFragment();
		TextContainer tc = new TextContainer(tf);
		tf.append("Hello.");
		tf.append(new Code(TagType.OPENING, "x", "x"));
		tf.append("To the.");
		tf.append(new Code(TagType.CLOSING, "x", "x"));
		tf.append("World.");
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(1, n);
		segments.create(seg.getRanges());
		assertEquals(1, segments.count());
		assertEquals("Hello.xTo the.xWorld.", segments.get(0).toString());
	}

	@Test(expected = SegmentationRuleException.class)
	public void computeSegmentsThrowsSegmentationRuleException() {
		ISegmenter segmenter = new SRXSegmenter();
		segmenter.computeSegments("");
	}

	private static TextFragment getTextFragment(String type) {
		TextFragment tf = new TextFragment();

		switch (type) {
			case "with-codes-seven":
				tf.append(TagType.OPENING, "x", "<x>");
				tf.append("One.");
				tf.append(TagType.CLOSING, "x", "</x>");
				tf.append(TagType.OPENING, "x", "<x>");
				tf.append("Two.{{ Three. Four. ");
				tf.append(TagType.CLOSING, "x", "</x>");
				tf.append("Five.");
				tf.append(TagType.PLACEHOLDER, "y", "<y/>");
				tf.append("}}Six.");
				tf.append(TagType.PLACEHOLDER, "y", "<y/>");
				tf.append(TagType.OPENING, "x", "<x>");
				tf.append("Seven. ");
				tf.append(TagType.CLOSING, "x", "</x>");
				break;
			case "empty-3":
				tf.append("   ");
				break;
			case "one":
				// break is intentionally omitted
			default:
				tf.append("One.");
		}

		return tf;
	}

	@DataProvider
	public static Object[][] computeSegmentsWithTreatIsolatedCodesAsWhitespaceDataProvider() {
		return new Object[][]{
				{"\\b", false, true, false, false, false, false,
						getTextFragment("with-codes-seven"),
						new String[]{
								"<x>One.</x><x>Two.",
								"{{ Three. Four. </x>Five.<y/>}}",
								"Six.<y/><x>Seven. </x>"
						}
				},
				{"\\.", false, true, false, false, false, false,
						getTextFragment("with-codes-seven"),
						new String[]{
								"<x>One.</x>",
								"<x>Two.",
								"{{ Three. Four. </x>Five.<y/>}}",
								"Six.<y/>",
								"<x>Seven.",
								" </x>"
						}
				},
				{"\\.", true, false, false, false, false, true,
						getTextFragment("with-codes-seven"),
						new String[]{
								"One.</x>",
								"Two.",
								"{{ Three. Four. </x>Five.<y/>}}",
								"Six.",
								"Seven.",
								" </x>"
						}
				},
				{"\\.", true, false, true, true, false, false,
						getTextFragment("with-codes-seven"),
						new String[]{
								"<x>One.</x><x>",
								"Two.",
								"{{ Three. Four. </x>Five.<y/>}}",
								"Six.",
								"<y/><x>Seven.",
								" </x>"
						}
				},
				{"\\.", true, false, true, true, false, true,
						getTextFragment("with-codes-seven"),
						new String[]{
								"<x>One.</x><x>",
								"Two.",
								"{{ Three. Four. </x>Five.<y/>}}",
								"Six.",
								"<x>Seven.",
								" </x>"
						}
				},
				{"\\.", true, false, true, true, false, true,
						getTextFragment("one"),
						new String[]{
								"One."
						}
				},
				{"  ", false, false, false, false, true, false,
						getTextFragment("empty-3"),
						new String[]{
								"   "
						}
				},
		};
	}

	@Test
	@UseDataProvider("computeSegmentsWithTreatIsolatedCodesAsWhitespaceDataProvider")
	public void computeSegmentsWithTreatIsolatedCodesAsWhitespace(String patternBeforeBrakePoint,
																  boolean useJavaRegex,
																  boolean includeIsolatedCodes,
																  boolean includeStartCodes,
																  boolean oneSegmentIncludesAll,
																  boolean trimLeadingWS,
																  boolean trimCodes,
																  TextFragment textFragment,
																  String[] expectedSegments) {
		ISegmenter seg = createSegmenterWithEmptyAfterRule(
				LocaleId.fromString("en"),
				patternBeforeBrakePoint,
				useJavaRegex);

		seg.setTreatIsolatedCodesAsWhitespace(true);

		seg.setIncludeStartCodes(includeStartCodes);
		seg.setIncludeIsolatedCodes(includeIsolatedCodes);
		seg.setOneSegmentIncludesAll(oneSegmentIncludesAll);
		seg.setTrimLeadingWS(trimLeadingWS);
		seg.setTrimCodes(trimCodes);

		TextContainer tc = new TextContainer(textFragment);

		ISegments segments = tc.getSegments();
		seg.computeSegments(tc);
		segments.create(seg.getRanges());

		assertThat(segments.count(), is(expectedSegments.length));

		// compute segments one more time to fulfil the coverage
		seg.computeSegments(tc);

		for (int i = 0; i < expectedSegments.length; i++) {
			assertThat(segments.get(i).toString(), equalTo(expectedSegments[i]));
		}
	}

	private ISegmenter createSegmenterWithRules (LocaleId locId) {
		SRXDocument doc = new SRXDocument();
		LanguageMap langMap = new LanguageMap(".*", "default");
		doc.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<Rule>();
		langRules.add(new Rule("\\.", "\\s", true));
		// Add the ruls to the document
		doc.addLanguageRule("default", langRules);
		// Create the segmenter
		return doc.compileLanguageRules(locId, null);
	}
	
	private ISegmenter createSegmenterWithRules2 (LocaleId locId) {
		SRXDocument doc = new SRXDocument();
		LanguageMap langMap = new LanguageMap(".*", "default");
		doc.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<Rule>();
		langRules.add(new Rule("[a-zA-Z ]", "[a-zA-Z ]", false));
		langRules.add(new Rule(".", "", true));
		// Add the ruls to the document
		doc.addLanguageRule("default", langRules);
		// Create the segmenter
		return doc.compileLanguageRules(locId, null);
	}
	
	private ISegmenter createSegmenterWithEmptyBeforeRule(LocaleId locId) {
		SRXDocument doc = new SRXDocument();
		LanguageMap langMap = new LanguageMap(".*", "default");
		doc.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<Rule>();
		langRules.add(new Rule("", "[a-zA-Z0-9]", false));
		langRules.add(new Rule("\\.", "", true));
		// Add the rules to the document
		doc.addLanguageRule("default", langRules);
		doc.setCascade(true);
		// Create the segmenter
		return doc.compileLanguageRules(locId, null);
	}

	private ISegmenter createSegmenterWithEmptyAfterRule(LocaleId locId,
														 String patternBeforeBrakePoint,
														 boolean useJavaRegex) {
		SRXDocument doc = new SRXDocument();
		LanguageMap langMap = new LanguageMap(".*", "default");
		doc.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<>();
		langRules.add(new Rule(patternBeforeBrakePoint, "", true));
		// Add the rules to the document
		doc.addLanguageRule("default", langRules);
		doc.setCascade(true);
		doc.setMaskRule("\\{\\{.*\\}\\}");
		// Create the segmenter
		return doc.compileLanguageRules(locId, null);
	}

	private ISegmenter createSegmenterWithGermanNoBreak(LocaleId locId) {
		SRXDocument doc = new SRXDocument();
		LanguageMap langMap = new LanguageMap("de", "German");
		doc.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<Rule>();
		langRules.add(new Rule("\\b\\p{L}\\.", "\\s+", false));
		langRules.add(new Rule("[\\.]", "\\s+", true));		
		// Add the rules to the document
		doc.addLanguageRule("German", langRules);
		doc.setCascade(true);
		// Create the segmenter
		return doc.compileLanguageRules(locId, null);
	}
	
	private ITextUnit createMultiTargetSegmentedTextUnit () {
		ISegmenter segmenter = createSegmenterWithRules(LocaleId.fromString("en"));
		// Create the source and segment it
		ITextUnit tu = new TextUnit("id1", "Part 1. Part 2. Part 3.");
		tu.createSourceSegmentation(segmenter);
		// Create the German target
		TextContainer tc1 = tu.setTarget(LocaleId.GERMAN, new TextContainer("DE_Part 1. DE_Part 2. DE_Part 3."));
		// Create same segmentation
		segmenter.computeSegments(tc1);
		tc1.getSegments().create(segmenter.getRanges());
		// Create the French target
		TextContainer tc2 = tu.setTarget(LocaleId.FRENCH, new TextContainer("FR_Part 1 and part 2. FR_Part 3."));
		// Create same segmentation
		segmenter.computeSegments(tc2);
		tc2.getSegments().create(segmenter.getRanges());
		return tu;
	}

	private TextContainer createCodedText () {
		// "PH after.<br/> End after.</b> Start after.<i> Text.</i>  "
		TextFragment tf = new TextFragment();
		tf.append("PH after.");
		tf.append(TagType.PLACEHOLDER, "break", "<br/>");
		tf.append(TagType.OPENING, "bold", "<b>");
		tf.append(" End after.");
		tf.append(TagType.CLOSING, "bold", "</b>");
		tf.append(" Start after.");
		tf.append(TagType.OPENING, "italics", "<i>");
		tf.append(" Text.");
		tf.append(TagType.CLOSING, "italics", "</i>");
		tf.append("  ");
		return new TextContainer(tf);
	}

	private TextContainer createCodedText2 () {
		// "PH after.<br/><br/><b><i> End after.</i></b> Start after.<u><i> Text.</i></u>  "
		TextFragment tf = new TextFragment();
		tf.append("PH after.");
		tf.append(TagType.PLACEHOLDER, "break", "<br/>");
		tf.append(TagType.PLACEHOLDER, "break", "<br/>");
		tf.append(TagType.OPENING, "bold", "<b>");
		tf.append(TagType.OPENING, "italics", "<i>");
		tf.append(" End after.");
		tf.append(TagType.CLOSING, "italics", "</i>");
		tf.append(TagType.CLOSING, "bold", "</b>");
		tf.append(" Start after.");
		tf.append(TagType.OPENING, "under", "<u>");
		tf.append(TagType.OPENING, "italics", "<i>");
		tf.append(" Text.");
		tf.append(TagType.CLOSING, "italics", "</i>");
		tf.append(TagType.CLOSING, "under", "</u>");
		tf.append("  ");
		return new TextContainer(tf);
	}
}
