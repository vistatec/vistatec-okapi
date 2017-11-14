/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.common.resource;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.GenericContent;

@RunWith(JUnit4.class)
public class AlignedSegmentsTest {
	private static final LocaleId locFR = LocaleId.FRENCH;
	private static final String TU1 = "tu1";
	private GenericContent fmt;

	public AlignedSegmentsTest() {
		fmt = new GenericContent();
	}

	@Test
	public void loopThroughSegments() {

		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();

		Segment srcSeg = as.getSource(0, locFR);
		Segment trgSeg = as.getCorrespondingTarget(srcSeg, locFR);
		assertEquals("Part 1.", srcSeg.text.toString());
		trgSeg = as.getCorrespondingTarget(srcSeg, locFR);
		assertEquals("Trg 1.", trgSeg.text.toString());
	
		srcSeg = as.getSource(1, locFR);
		trgSeg = as.getCorrespondingTarget(srcSeg, locFR);
		assertEquals("Part 2.", srcSeg.text.toString());
		trgSeg = as.getCorrespondingTarget(srcSeg, locFR);
		assertEquals("Trg 2.", trgSeg.text.toString());
	}

	@Test
	public void getSegmentsTest() {
		ITextUnit tu = new TextUnit(TU1);
		tu.setSourceContent(new TextFragment("text"));
		IAlignedSegments as = tu.getAlignedSegments();
		assertNotNull(as);
	}

	@Test
	public void removeSegmentsTest() {
		ITextUnit tu = createSegmentedTU();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment seg = as.getSource(0, locFR);
		assertEquals("0", seg.id);
		as.remove(seg, locFR);
		seg = as.getSource(0, locFR);
		assertEquals("s2", seg.id);
	}

	@Test
	public void removeSegmentsWithTargetTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment seg = as.getSource(0, locFR);
		assertEquals("0", seg.id);
		as.remove(seg, locFR);
		ISegments segs = tu.getTargetSegments(locFR);
		seg = segs.get(0);
		assertEquals("s2", seg.id);
	}

	@Test
	public void insertSourceSegmentTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment seg1 = new Segment("newId");
		as.insert(1, seg1, null, locFR);
		Segment seg2 = as.getSource(1, locFR);
		assertSame(seg1, seg2); // Check insertion
		Segment seg3 = as.getSource(2, locFR);
		assertEquals("s2", seg3.id); // Check old seg(1) was move down
		Segment seg4 = as.getCorrespondingTarget(seg2, locFR);
		assertEquals(seg1.id, seg4.id); // Check target was added
	}

	@Test
	public void insertSourceSegmentChangeIdTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment seg1 = new Segment("s2"); // "s2" id exists already
		as.insert(1, seg1, null, locFR);
		Segment seg2 = as.getSource(1, locFR);
		assertSame(seg1, seg2);
		assertEquals("1", seg2.id); // Id was changed to a valid one
		Segment seg4 = as.getCorrespondingTarget(seg2, locFR);
		assertEquals(seg1.id, seg4.id); // Check target was added with validated
										// id
	}

	@Test
	public void insertSegmentsChangeIdTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment insertSrcSeg = new Segment("s2"); // "s2" id exists already
		Segment insertTrgSeg = new Segment("zzId", new TextFragment("[text]"));
		as.insert(1, insertSrcSeg, insertTrgSeg, locFR);
		Segment srcAtInsertedIndex = as.getSource(1, locFR);
		assertSame("The given source should be inserted at the index (not a copy)", insertSrcSeg, srcAtInsertedIndex);
		assertEquals("1", srcAtInsertedIndex.id); // Id was changed to a valid
													// one
		Segment trgAtInsertedIndex = as.getCorrespondingTarget(srcAtInsertedIndex, locFR);
		assertEquals("[text]", trgAtInsertedIndex.toString());
		assertEquals(insertSrcSeg.id, trgAtInsertedIndex.id); // Check target
																// was added
																// with
																// validated id
	}

	@Test
	public void splitSourceSegmentTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment srcSeg = as.getSource(1, locFR);
		Segment newSrcSeg = as.splitSource(locFR, srcSeg, 5); // "Part ][2."
		assertNotNull(newSrcSeg);
		assertEquals("2.", newSrcSeg.text.toString()); // Check new segment
														// content
		assertEquals("Part ", srcSeg.text.toString()); // Check original segment
														// content
		// Check the target
		Segment newTrgSeg = as.getCorrespondingTarget(newSrcSeg, locFR);
		assertNotNull(newTrgSeg);
		assertTrue(newTrgSeg.text.isEmpty());
		assertEquals("[Part 1.] a [Part ][2.]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[Trg 1.] a [Trg 2.][]", fmt.printSegmentedContent(tu.getTarget(locFR), true));
	}

	@Test
	public void splitTargetSegmentTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment trgSeg = tu.getTargetSegments(locFR).get(1);
		Segment newTrgSeg = as.splitTarget(locFR, trgSeg, 4); // "Trg ][2."
		assertNotNull(newTrgSeg);
		// Check new segment content
		assertEquals("2.", newTrgSeg.text.toString());
		// Check original segment content
		assertEquals("Trg ", trgSeg.text.toString());
		// Check the source
		Segment newSrcSeg = as.getCorrespondingSource(newTrgSeg, locFR);
		assertNotNull(newSrcSeg);
		assertEquals("[Part 1.] a [Part 2.][]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[Trg 1.] a [Trg ][2.]", fmt.printSegmentedContent(tu.getTarget(locFR), true));
	}

	@Test
	public void setSourceTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment newSeg = new Segment("newId", new TextFragment("newText"));
		as.setSegment(1, newSeg, locFR);
		Segment seg = as.getSource(1, locFR);
		assertEquals("newText", seg.toString());
		assertEquals("newId", seg.id);
		seg = as.getCorrespondingTarget(newSeg, locFR);
		assertEquals("newId", seg.id);
	}

	@Test
	public void setTargetTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment newSeg = new Segment("newId", new TextFragment("newText"));
		as.setSegment(1, newSeg, locFR);
		Segment seg = as.getSource(1, locFR);
		assertEquals("newId", seg.id);
		seg = as.getCorrespondingTarget(seg, locFR);
		assertEquals("newText", seg.toString());
	}

	@Test
	public void getSourceTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		assertEquals("Part 2.", as.getSource(1, locFR).text.toString());
	}

	@Test
	public void getCorrespondingTargetTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment srcSeg = as.getSource(1, locFR);
		Segment trgSeg = as.getCorrespondingTarget(srcSeg, locFR);
		assertEquals("Trg 2.", trgSeg.text.toString());
	}

	@Test
	public void getCorrespondingSourceTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment srcSeg = as.getCorrespondingSource(tu.getTargetSegments(locFR).get(0), locFR);
		assertEquals("Part 1.", srcSeg.text.toString());
	}

	@Test
	public void joinWithNextTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		// First: add a new segment
		as.append(new Segment("nId", new TextFragment("newSrcText")),
				new Segment("nId", new TextFragment("newTrgText")), locFR);
		assertEquals("[Part 1.] a [Part 2.][newSrcText]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[Trg 1.] a [Trg 2.][newTrgText]", fmt.printSegmentedContent(tu.getTarget(locFR), true));
		// First join
		Segment srcSeg = as.getSource(1, locFR);
		as.joinWithNext(srcSeg, locFR);
		assertEquals("[Part 1.] a [Part 2.newSrcText]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[Trg 1.] a [Trg 2.newTrgText]", fmt.printSegmentedContent(tu.getTarget(locFR), true));
		// Second join
		srcSeg = as.getSource(0, locFR);
		as.joinWithNext(srcSeg, locFR);
		assertEquals("[Part 1. a Part 2.newSrcText]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[Trg 1. a Trg 2.newTrgText]", fmt.printSegmentedContent(tu.getTarget(locFR), true));
	}

	@Test
	public void joinAllTest() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		as.joinAll(locFR);
		assertEquals("[Part 1. a Part 2.]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[Trg 1. a Trg 2.]", fmt.printSegmentedContent(tu.getTarget(locFR), true));

	}

	@Test
	public void appendSegmentTest1() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment seg = new Segment("nId", new TextFragment("newSrcText"));
		as.append(seg, null, locFR);
		assertEquals("newSrcText", as.getSource(2, locFR).toString());
		assertEquals("nId", as.getCorrespondingTarget(seg, locFR).id);
		assertEquals("[Part 1.] a [Part 2.][newSrcText]", fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals("[Trg 1.] a [Trg 2.][]", fmt.printSegmentedContent(tu.getTarget(locFR), true));
	}

	@Test
	public void appendSegmentTest2() {
		ITextUnit tu = createSegmentedTUAndTarget();
		IAlignedSegments as = tu.getAlignedSegments();
		Segment seg = new Segment("nId", new TextFragment("newSrcText"));
		as.append(seg, new Segment("nId", new TextFragment("newTrgText")), locFR);
		assertEquals("newSrcText", as.getSource(2, locFR).toString());
		assertEquals("nId", as.getCorrespondingTarget(seg, locFR).id);
		assertEquals("newTrgText", as.getCorrespondingTarget(seg, locFR).toString());
		assertEquals("[Part 1.] a [Part 2.][newSrcText]", fmt.printSegmentedContent(tu.getSource(), true));
	}

	@Test
	public void alignWithAlignedPairs() {
		// create aligned pairs
		Segment srcSeg, trgSeg;
		List<AlignedPair> alignedPairs = new LinkedList<AlignedPair>();

		String[] source = { "apSource 1.", "apSource 2.", "apSource 3." };
		String[] target = { "apTarg 1.", "apTarg 2.", "apTarg 3." };

		srcSeg = new Segment("sA", new TextFragment(source[0]));
		trgSeg = new Segment("sAlpha", new TextFragment(target[0]));
		alignedPairs.add(new AlignedPair(srcSeg, trgSeg, locFR));

		srcSeg = new Segment("sB", new TextFragment(source[1]));
		trgSeg = new Segment("sBeta", new TextFragment(target[1]));
		alignedPairs.add(new AlignedPair(srcSeg, trgSeg, locFR));

		srcSeg = new Segment("sC", new TextFragment(source[2]));
		trgSeg = new Segment("sChuppa?", new TextFragment(target[2]));
		alignedPairs.add(new AlignedPair(srcSeg, trgSeg, locFR));

		// create tu
		ITextUnit tu = createSegmentedTUAndTarget();

		// call method
		tu.getAlignedSegments().align(alignedPairs, locFR);

		assertTrue("a new target should be created if none is present for the given " + "locale", tu.hasTarget(locFR));

		// replaces content

		String[] actualSources = { tu.getSource().getSegments().get(0).toString(),
				tu.getSource().getSegments().get(1).toString(),
				tu.getSource().getSegments().get(2).toString() };

		assertArrayEquals("the source segments of the aligned pairs should be " + "used in the source content", source,
				actualSources);

		String[] actualTargets = { tu.getTarget(locFR).getSegments().get(0).toString(),
				tu.getTarget(locFR).getSegments().get(1).toString(),
				tu.getTarget(locFR).getSegments().get(2).toString() };

		assertArrayEquals("the target segments of the aligned pairs should be " + "used in the target content", target,
				actualTargets);

		// target is aligned after
		assertEquals("the target should have a status of ALIGNED after the " + "align() method is called",
				AlignmentStatus.ALIGNED, tu.getTarget(locFR).getSegments().getAlignmentStatus());

		assertEquals("new aligned source", "apSource 1.", tu.getSource().getFirstSegment().toString());

		assertEquals("new aligned target", "apTarg 1.", tu.getTarget(locFR).getFirstSegment().toString());
	}

	@Test
	public void alignCollapseAll() {
		ITextUnit tu = createSegmentedTUAndTarget();
		tu.getAlignedSegments().alignCollapseAll(locFR);

		assertTrue("the target content should be one segment after alignCollapseAll()",
				tu.getTarget(locFR).contentIsOneSegment());

		assertFalse("the target should not be flagged as segmented after it " + "has been collapsed",
				tu.getTarget(locFR).hasBeenSegmented());

		assertEquals("the target should be marked as ALIGNED after alignCollapseAll()", AlignmentStatus.ALIGNED,
				tu.getTarget(locFR).getSegments().getAlignmentStatus());

		// default source should not be collapsed
		assertTrue("the source content should be one segment after alignCollapseAll()",
				tu.getSource().contentIsOneSegment());
	}

	private ITextUnit createSegmentedTU() {
		ITextUnit tu = new TextUnit("id", "Part 1.");
		tu.getSource().getSegments().append(new Segment("s2", new TextFragment("Part 2.")), " a ");
		return tu;
	}

	private ITextUnit createSegmentedTUAndTarget() {
		ITextUnit tu = createSegmentedTU();
		// Add the target segments
		ISegments segs = tu.getTargetSegments(locFR);
		segs.get(0).text.append("Trg 1.");
		segs.get(1).text.append("Trg 2.");

		return tu;
	}
}
