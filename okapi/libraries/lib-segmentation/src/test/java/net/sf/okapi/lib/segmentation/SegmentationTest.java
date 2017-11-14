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

package net.sf.okapi.lib.segmentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SegmentationTest {

	private ISegmenter segmenter;
	private ISegmenter segmenterTrim;
	private LocaleId locEN = LocaleId.fromString("en");
	private GenericContent fmt = new GenericContent();
	private LocaleId locFR = LocaleId.FRENCH;
	private LocaleId locAR = LocaleId.ARABIC;
	
	@Before
	public void setUp() {
		SRXDocument doc1 = new SRXDocument();
		LanguageMap langMap = new LanguageMap(".*", "default");
		doc1.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<Rule>();
		langRules.add(new Rule("\\.", "\\s", true));
		langRules.add(new Rule("\\|", "", true));
		// Add the ruls to the document
		doc1.addLanguageRule("default", langRules);
		// Create the segmenter
		segmenter = doc1.compileLanguageRules(locEN, null);

		SRXDocument doc2 = new SRXDocument();
		doc2.addLanguageMap(langMap);
		doc2.addLanguageRule("default", langRules);
		doc2.setTrimLeadingWhitespaces(true);
		doc2.setTrimTrailingWhitespaces(true);
		// Create the segmenter
		segmenterTrim = doc2.compileLanguageRules(locEN, null);
	}

	@Test
	public void testGetSegmentCount () {
		TextContainer tc = createSegmentedContainer();
		assertEquals(2, tc.getSegments().count());
	}
	
	@Test
	public void testGetSegments () {
		TextContainer tc = createSegmentedContainer();
		ISegments segments = tc.getSegments();
		assertEquals("<s>Part 1.</s>", segments.get(0).toString());
		assertEquals(" Part 2.", segments.get(1).toString());
		assertEquals("[<1>Part 1.</1>] Outside[ Part 2.]", fmt.printSegmentedContent(tc, true));
	}
	
	@Test
	public void testMergeOneSegment () {
		TextContainer tc = createSegmentedContainer();
		ISegments segments = tc.getSegments();
		assertEquals("[<1>Part 1.</1>] Outside[ Part 2.]", fmt.printSegmentedContent(tc, true));
		tc.changePart(2);
		assertEquals(1, segments.count());
		assertEquals("<s>Part 1.</s>", segments.get(0).toString());
		assertEquals("[<1>Part 1.</1>] Outside Part 2.", fmt.printSegmentedContent(tc, true));
	}

	@Test
	public void testChangeTwoSegmentsToParts () {
		TextContainer tc = createSegmentedContainer();
		tc.changePart(2); // Segment to non-segment
		tc.changePart(0); // try segment to non-segment (but here it's the last segment, so no change)
		assertEquals(1, tc.getSegments().count());
		assertFalse(tc.contentIsOneSegment());
		assertEquals("[<1>Part 1.</1>] Outside Part 2.", fmt.printSegmentedContent(tc, true));
		assertEquals("<s>Part 1.</s> Outside Part 2.", tc.toString());
	}

	
	@Test
	public void testJoinTwoSegmentsIntoOne () {
		TextContainer tc = createSegmentedContainer();
		ISegments segments = tc.getSegments();
		segments.joinWithNext(0);
		assertEquals(1, segments.count());
		assertTrue(tc.contentIsOneSegment());
		assertEquals("[<1>Part 1.</1> Outside Part 2.]", fmt.printSegmentedContent(tc, true));
		assertEquals("<s>Part 1.</s> Outside Part 2.", tc.toString());
	}

	@Test
	public void testMergeAllSegments () {
		TextContainer tc = createSegmentedContainer();
		ISegments segments = tc.getSegments();
		segments.joinAll();
		assertEquals(1, segments.count());
		assertTrue(tc.contentIsOneSegment());
		assertEquals("[<1>Part 1.</1> Outside Part 2.]", fmt.printSegmentedContent(tc, true));
		assertEquals("<s>Part 1.</s> Outside Part 2.", tc.toString());
	}
	
	@Test
	public void testCreateSegment () {
		TextContainer tc = createSegmentedContainer();
		ISegments segments = tc.getSegments();
		// "**Part 1.** Outside Part2."
		//  01234567890123456789012345"
		segments.create(11, 19);
		assertEquals(1, segments.count());
		assertEquals(3, tc.count());
		assertEquals(" Outside", segments.get(0).toString());
	}
	
	@Test
	public void testAppendSegment () {
		TextContainer tc = createSegmentedContainer();
		ISegments segments = tc.getSegments();
		segments.append(new TextFragment(" Added Part."));
		assertEquals(3, segments.count());
		assertEquals(" Added Part.", segments.get(2).toString());
	}

	@Test
	public void testSegmentationSimple1 () {
		TextContainer tc = createSegmentedContainer("a. z", segmenter);
		ISegments segments = tc.getSegments();
		assertEquals(2, segments.count());
		assertEquals("a.", segments.get(0).toString());
		assertEquals(" z", segments.get(1).toString());
		tc = createSegmentedContainer("a. z", segmenterTrim);
		segments = tc.getSegments();
		assertEquals(2, segments.count());
		assertEquals("a.", segments.get(0).toString());
		assertEquals("z", segments.get(1).toString());
	}
	
	@Test
	public void testSegmentationSimpleWithLeadingTrainlingWS () {
		TextContainer tc = createSegmentedContainer(" a.  ", segmenter);
		ISegments segments = tc.getSegments();
		assertEquals(2, segments.count());
		assertEquals(" a.", segments.get(0).toString());
		assertEquals("  ", segments.get(1).toString());
		// 1 segment only because the last one is only made of whitespaces
		tc = createSegmentedContainer("a. ", segmenterTrim);
		segments = tc.getSegments();
		assertEquals(1, segments.count());
		assertEquals("a.", segments.get(0).toString());
	}
	
	@Test
	public void testSegmentationWithEmpty () {
		TextContainer tc = createSegmentedContainer(" a. | b.", segmenter);
		ISegments segments = tc.getSegments();
		assertEquals(3, segments.count());
		assertEquals(" a.", segments.get(0).toString());
		assertEquals(" |", segments.get(1).toString());
		assertEquals(" b.", segments.get(2).toString());
		// 1 segment only because the last one is only made of whitespaces
		tc = createSegmentedContainer(" a. |  b.", segmenterTrim);
		segments = tc.getSegments();
		assertEquals(3, segments.count());
		assertEquals("a.", segments.get(0).toString());
		assertEquals("|", segments.get(1).toString());
		assertEquals("b.", segments.get(2).toString());
	}

	@Test
	public void testTrimOptionsSetting () {
		SRXDocument srxDoc = new SRXDocument();
		srxDoc.setTrimLeadingWhitespaces(true);
		srxDoc.setTrimTrailingWhitespaces(true);
		ISegmenter segter = srxDoc.compileLanguageRules(LocaleId.ENGLISH, null);
		segter.computeSegments(" a ");
		List<Range> list = segter.getRanges();
		assertEquals(1, list.get(0).start);
		assertEquals(2, list.get(0).end);

		// Output tmp.srx at the root level (not in the package tree)
		String tmpPath = FileLocation.fromClass(SegmentationTest.class).out("/tmp.srx").toString();
		srxDoc.saveRules(tmpPath, true, true);

		srxDoc.resetAll();
		segter = srxDoc.compileLanguageRules(LocaleId.ENGLISH, null);
		segter.computeSegments(" a ");
		list = segter.getRanges();
		assertEquals(0, list.get(0).start);
		assertEquals(3, list.get(0).end);

		srxDoc.loadRules(tmpPath);
		segter = srxDoc.compileLanguageRules(LocaleId.ENGLISH, null);
		segter.computeSegments(" a ");
		list = segter.getRanges();
		assertEquals(1, list.get(0).start);
		assertEquals(2, list.get(0).end);
	}

	@Test
	public void testTrimOptionsSettingFromFile () throws URISyntaxException {
		SRXDocument srxDoc = new SRXDocument();
		// Trim options are 'true' in this file (inverse of default)
		FileLocation in = FileLocation.fromClass(SegmentationTest.class).in("/defaultSegmentation.srx");
		srxDoc.loadRules(in.toString());
		ISegmenter segter = srxDoc.compileLanguageRules(LocaleId.ENGLISH, null);
		segter.computeSegments(" a ");
		// Trim options worked
		List<Range> list = segter.getRanges();
		assertEquals(1, list.get(0).start);
		assertEquals(2, list.get(0).end);
	}
	
	@Test
	public void testTUCreateSourceSegmentation () {
		ITextUnit tu = new TextUnit("tuid");
		tu.setSource(createSimpleContent());
		tu.createSourceSegmentation(segmenter);
		assertEquals("[<1>Part 1.</1>][ Part 2.]", fmt.printSegmentedContent(tu.getSource(), true));
	}
	
	@Test
	public void testTUCreateSourceSegmentationOverwrite () {
		ITextUnit tu = new TextUnit("tuid");
		tu.setSource(createSegmentedContainer()); // hard-coded
		assertEquals("[<1>Part 1.</1>] Outside[ Part 2.]", fmt.printSegmentedContent(tu.getSource(), true));
		tu.createSourceSegmentation(segmenter); // From the segmenter
		assertEquals("[<1>Part 1.</1>][ Outside Part 2.]", fmt.printSegmentedContent(tu.getSource(), true));
	}
	
	@Test
	public void testTUSourceSegmentationInTarget () {
		ITextUnit tu = new TextUnit("tuid");
		tu.setSource(createSimpleContent());
		tu.createSourceSegmentation(segmenter);
		assertEquals("[<1>Part 1.</1>][ Part 2.]", fmt.printSegmentedContent(tu.getSource(), true));
		// Creates the target and translate it
		TextContainer tc = tu.createTarget(locFR, true, IResource.COPY_ALL);
		for ( Segment seg : tc.getSegments() ) {
			seg.text.setCodedText(seg.text.getCodedText().toUpperCase() + " FR");
		}
		assertEquals("[<1>PART 1.</1> FR][ PART 2. FR]", fmt.printSegmentedContent(tc, true));
	}
	
	@Test
	public void testCreateTargetSegmentation () {
		ITextUnit tu = new TextUnit("tuid");
		tu.setSource(createSimpleContent());
		tu.createTarget(locFR, true, IResource.COPY_ALL);
		// Segment both with the same segmenter
		tu.createSourceSegmentation(segmenter);
		tu.createTargetSegmentation(segmenter, locFR);
		// We should get the same result
		assertEquals("[<1>Part 1.</1>][ Part 2.]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[<1>Part 1.</1>][ Part 2.]", fmt.printSegmentedContent(tu.getTarget(locFR), true));
	}

	@Test
	public void testCreateSegmentationWithCodes () {
		ITextUnit tu = new TextUnit("tuid");
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "s", "<s>");
		tf.append("Part 1");
		tf.append(TagType.CLOSING, "s", "</s>");
		tf.append(". Part 2.");
		tu.setSource(new TextContainer(tf));
		// Segment
		tu.createSourceSegmentation(segmenter);
		// We should get the same result
		assertEquals("[<1>Part 1</1>.][ Part 2.]", fmt.printSegmentedContent(tu.getSource(), true));
	}
	
	@Test
	public void testSegmentationWithEmptyString() {
		TextContainer tc = createSegmentedContainer("", segmenter);
		ISegments segments = tc.getSegments();
		// FIXME: Why is this 1 - why not 0?
		assertEquals(1, segments.count());
	}
	
	private TextContainer createSimpleContent () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "s", "<s>");
		tf.append("Part 1.");
		tf.append(TagType.CLOSING, "s", "</s>");
		tf.append(" Part 2.");
		return new TextContainer(tf);
	}

	private TextContainer createSegmentedContainer () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "s", "<s>");
		tf.append("Part 1.");
		tf.append(TagType.CLOSING, "s", "</s>");
		tf.append(" Part 2.");
		TextContainer tc = new TextContainer(tf);
		segmenter.computeSegments(tc);
		tc.getSegments().create(segmenter.getRanges());
		// Insert in holder between the two segments
		tc.insert(1, new TextPart(new TextFragment(" Outside")));
		return tc;
	}

	private TextContainer createSegmentedContainer (String text,
		ISegmenter segmenter)
	{
		TextContainer tc = new TextContainer(text);
		segmenter.computeSegments(tc);
		tc.getSegments().create(segmenter.getRanges());
		return tc;
	}

}
