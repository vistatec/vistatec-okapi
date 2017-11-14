/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TextContainerTest {
	
    private GenericContent fmt = new GenericContent();

    @Test
    public void testDefaultConstructor () {
		TextContainer tc = new TextContainer();
		assertFalse(tc.hasBeenSegmented());
		assertTrue(tc.contentIsOneSegment());
		assertEquals("", tc.getCodedText());
		assertEquals("0", tc.getSegments().get(0).id);
    }
    
    @Test
    public void testStringConstructor () {
		TextContainer tc = new TextContainer("");
		assertFalse(tc.hasBeenSegmented());
		assertTrue(tc.contentIsOneSegment());
		assertEquals("", tc.getCodedText());
		assertEquals("0", tc.getSegments().get(0).id);
		tc = new TextContainer("text");
		assertFalse(tc.hasBeenSegmented());
		assertTrue(tc.contentIsOneSegment());
		assertEquals("text", tc.getCodedText());
		assertEquals("0", tc.getSegments().get(0).id);
    }
    
    @Test
    public void testTextFragmentConstructor () {
    	TextFragment tf = new TextFragment("abc");
		TextContainer tc = new TextContainer(tf);
		assertFalse(tc.hasBeenSegmented());
		assertTrue(tc.contentIsOneSegment());
		assertEquals("abc", tc.getCodedText());
		assertEquals("0", tc.getSegments().get(0).id);
		assertSame(tf, tc.getSegments().getFirstContent());
    }
    
    @Test
    public void testTextSegmentConstructor () {
    	Segment seg = new Segment("qwerty", new TextFragment("xyz"));
		TextContainer tc = new TextContainer(seg);
		ISegments segments = tc.getSegments();
		assertFalse(tc.hasBeenSegmented());
		assertTrue(tc.contentIsOneSegment());
		assertEquals("xyz", tc.getFirstContent().toText());
		assertEquals("qwerty", segments.get(0).id);
		assertSame(seg.text, segments.getFirstContent());
    }
    
    @Test
    public void testTextSegmentWithNullsConstructor () {
    	Segment seg = new Segment(null, null);
    	seg.text = null;
		TextContainer tc = new TextContainer(seg);
		ISegments segments = tc.getSegments();
		assertFalse(tc.hasBeenSegmented());
		assertTrue(tc.contentIsOneSegment());
		assertEquals("", tc.getFirstContent().toText());
		assertEquals("0", segments.get(0).id);
		assertSame(seg.text, segments.getFirstContent());
    }
    
	@Test
	public void testSegmentsWithCodePlusOneChar () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "BR", "<br/>");
		tf.append(".");
		TextContainer tc = new TextContainer(tf);
		ISegments segments = tc.getSegments();
		assertEquals(1, segments.count());
		assertEquals("<br/>.", segments.get(0).toString());
	}

	@Test
	public void testCounts () {
        TextContainer tc = new TextContainer();
        tc.append(new Segment("i1", new TextFragment("Hello")));
        tc.append(new TextPart(" the "));
        tc.append(new Segment("i2", new TextFragment("World")));
        assertEquals(3, tc.count());
        assertEquals(2, tc.getSegments().count());
	}
	
	@Test
	public void testSegmentsWithJustCode () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "BR", "<br/>");
		TextContainer tc = new TextContainer(tf);
		ISegments segments = tc.getSegments();
		assertEquals(1, segments.count());
		assertEquals("<br/>", segments.get(0).toString());
	}

	@Test
	public void testTextPartIterator () {
		TextContainer tc = createMultiSegmentContent();
		int i = -1;
		for ( TextPart part : tc ) {
			i++;
			switch ( i ) {
			case 0:
				assertEquals("text1", part.text.toText());
				assertTrue(part.isSegment());
				break;
			case 1:
				assertEquals(" ", part.text.toText());
				assertFalse(part.isSegment());
				break;
			case 2:
				assertEquals("text2", part.text.toText());
				assertTrue(part.isSegment());
				break;
			}
		}
	}
	
	@Test
	public void testSegmentIterator () {
		TextContainer tc = new TextContainer("[s1]");
		for ( Iterator<Segment> iter = tc.getSegments().iterator(); iter.hasNext(); ) {
			Segment seg = iter.next();
			assertEquals("[s1]", seg.text.toText());
		}
		ISegments segments = tc.getSegments();
		segments.append(new TextFragment("[s2]"));
		segments.append(new TextFragment("[s3]"));
		segments.append(new TextFragment("[s4]"));
		StringBuilder tmp = new StringBuilder();
		for ( Iterator<Segment> iter = tc.getSegments().iterator(); iter.hasNext(); ) {
			Segment seg = iter.next();
			tmp.append(seg.text.toText());
		}
		assertEquals("[s1][s2][s3][s4]", tmp.toString());
	}
	
	@Test
	public void testSegmentsWithTwoCodes () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "BR1", "<br1/>");
		tf.append(TagType.PLACEHOLDER, "BR2", "<br2/>");
		TextContainer tc = new TextContainer(tf);
		ISegments segments = tc.getSegments();
		assertEquals(1, segments.count());
		assertEquals("<br1/><br2/>", segments.get(0).toString());
	}
	
	@Test
	public void testHasBeenSegmented () {
		TextContainer tc = new TextContainer("seg1");
		ISegments segments = tc.getSegments();
		assertFalse(tc.hasBeenSegmented());
		segments.append(new Segment("1", new TextFragment("seg2")));
		assertTrue(tc.hasBeenSegmented());
		segments.joinAll();
		assertFalse(tc.hasBeenSegmented());
		segments.append(new TextFragment("seg3"));
		assertTrue(tc.hasBeenSegmented());
		//TODO: createSeg... etc
	}

	@Test
	public void testSegmentsWithOneChar () {
		TextContainer tc = new TextContainer(new TextFragment("z"));
		ISegments segments = tc.getSegments();
		assertEquals(1, segments.count());
		assertEquals("z", segments.get(0).toString());
	}
	
	@Test
	public void testSegmentsEmpty () {
		TextContainer tc = new TextContainer();
		ISegments segments = tc.getSegments();
		assertEquals(1, segments.count());
		assertEquals("0", segments.get(0).id);
	}

    @Test
    public void testCloneDeepCopy(){
    	TextContainer tc = new TextContainer("text");
        Property p1 = new Property("name", "value", true);
        tc.setProperty(p1);
        AltTranslationsAnnotation ann1 = new AltTranslationsAnnotation();
        ann1.add(LocaleId.ENGLISH, LocaleId.FRENCH, null, new TextFragment("src"),
        	new TextFragment("trg"), MatchType.EXACT, 99, "origin");
        tc.setAnnotation(ann1);
		TextContainer tc2 = tc.clone();
		assertEquals(tc.getFirstContent().toText(), tc2.getFirstContent().toText());
		assertNotSame(tc.getFirstContent(), tc2.getFirstContent());
        assertEquals("name property", p1.getValue(), tc2.getProperty("name").getValue());
        assertNotSame("properties should not be the same reference due to clone", p1, tc2.getProperty("name"));
        AltTranslationsAnnotation ann2 = tc2.getAnnotation(AltTranslationsAnnotation.class); 
        assertNotNull(ann2);
        assertEquals(99, ann2.getFirst().getCombinedScore());
        assertEquals("origin", ann2.getLast().getOrigin());
//TODO: check this with everyone        assertNotSame(ann2, ann1);
    }

    @Test
    public void testIsEmpty () {
		TextContainer tc = new TextContainer();
		assertTrue(tc.isEmpty());
		tc.getLastContent().append('z');
		assertFalse(tc.isEmpty());
		tc.setContent(new TextFragment());
		assertTrue(tc.isEmpty());
		tc.setContent(new TextFragment("text"));
		assertFalse(tc.isEmpty());
    }
    
	@Test
	public void testGetFirstSegmentContent () {
		TextContainer tc = new TextContainer("text");
		assertEquals("text", tc.getSegments().getFirstContent().toText());
	}
	
	@Test
	public void testCloningDistinction () {
		TextContainer tc = new TextContainer("text");
		TextContainer tc2 = tc.clone();
		assertNotSame(tc, tc2);
		assertNotSame(tc.getSegments().getFirstContent(), tc2.getSegments().getFirstContent());
		assertEquals(tc.toString(), tc2.toString());
	}

	@Test
	public void testHasTextWithText () {
		TextContainer tc = new TextContainer("text");
		assertTrue(tc.hasText(true, false));
		assertTrue(tc.hasText(true, true));
		assertFalse(tc.hasText(false, true));
		assertFalse(tc.hasText(false, false));
	}

	@Test
	public void testHasTextNoText () {
		TextContainer tc = new TextContainer("");
		assertFalse(tc.hasText(true, false));
		assertFalse(tc.hasText(true, true));
		assertFalse(tc.hasText(false, true));
		assertFalse(tc.hasText(false, false));
	}

	@Test
	public void testHasTextSpaces () {
		TextContainer tc = new TextContainer("  \t");
		// White spaces are not text
		assertFalse(tc.hasText(false, false));
		assertFalse(tc.hasText(true, false));
		// White spaces are text
		assertTrue(tc.hasText(true, true));
		assertFalse(tc.hasText(false, true));
	}

	@Test
	public void testHasTextCodeOnly () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "br", "<br/>");
		TextContainer tc = new TextContainer(tf);
		assertFalse(tc.hasText(false, false));
		assertFalse(tc.hasText(true, false));
		assertFalse(tc.hasText(true, true));
		assertFalse(tc.hasText(false, true));
	}

	@Test
	public void testHasTextSpacesAndCode () {
		TextFragment tf = new TextFragment("  \t");
		tf.append(TagType.PLACEHOLDER, "br", "<br/>");
		TextContainer tc = new TextContainer(tf);
		// White spaces are not text
		assertFalse(tc.hasText(false, false));
		assertFalse(tc.hasText(true, false));
		// White spaces are text
		assertTrue(tc.hasText(true, true));
		assertFalse(tc.hasText(false, true));
	}

	@Test(expected=InvalidPositionException.class)
	public void testCreateSegmentNegativeSpan () {
		TextContainer tc = new TextContainer("text");
		tc.getSegments().create(3, 1); // end is <= start+1
	}
	
	@Test(expected=InvalidPositionException.class)
	public void testCreateSegmentBadRangeOrder () {
		TextContainer tc = new TextContainer("text");
		List<Range> ranges = new ArrayList<Range>();
		ranges.add(new Range(2, 4));
		ranges.add(new Range(0, 2)); // Bad order
		tc.getSegments().create(ranges);
	}
	
	@Test
	public void testCreateSegmentEmptySpan () {
		TextContainer tc = new TextContainer("text");
		ISegments segments = tc.getSegments();
		assertEquals(1, segments.count());
		segments.create(1, 1); // No change because end is <= start+1
		assertEquals(1, segments.count()); // No change
		assertEquals("[text]", fmt.printSegmentedContent(tc, true));
		assertFalse(tc.hasBeenSegmented()); // Not segmented
	}
	
	@Test
	public void testCreateSegmentOneCharSpan () {
		TextContainer tc = new TextContainer("text");
		tc.getSegments().create(1, 2);
		assertEquals("t[e]xt", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.hasBeenSegmented());
	}
	
	@Test
	public void testCreateSegmentAllContent () {
		TextContainer tc = new TextContainer("text");
		assertFalse(tc.hasBeenSegmented());
		tc.getSegments().create(0, -1);
		assertEquals("[text]", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.hasBeenSegmented());
	}

	@Test(expected=InvalidPositionException.class)
	public void testCreateSegmentWithPHCodesStartInMarker () {
		TextFragment tf = new TextFragment("t1");
		tf.append(TagType.PLACEHOLDER, "BR1", "<br1/>");
		tf.append("t2");
		tf.append(TagType.PLACEHOLDER, "BR2", "<br2/>");
		TextContainer tc = new TextContainer(tf);		
		assertEquals("t1<br1/>t2<br2/>", tc.toString());
		// Show throw a InvalidPositionException
		// The start position breaks a marker
		tc.getSegments().create(3, -1);
	}

	@Test
	public void testCreateSegmentOnExistingSegment () {
		TextContainer tc = new TextContainer("seg1 seg2");
		assertFalse(tc.hasBeenSegmented());
		tc.getSegments().create(0, 4);
		assertEquals("[seg1] seg2", fmt.printSegmentedContent(tc, true));
		tc.getSegments().create(0, 9);
		assertEquals("[seg1 seg2]", fmt.printSegmentedContent(tc, true));
	}

	@Test
	public void testCreateSegmentWithPHCodes () {
		TextFragment tf = new TextFragment("t1");
		tf.append(TagType.PLACEHOLDER, "BR1", "<br1/>");
		tf.append("t2");
		tf.append(TagType.PLACEHOLDER, "BR2", "<br2/>");
		TextContainer tc = new TextContainer(tf);
		ISegments segments = tc.getSegments();
		assertEquals("t1<br1/>t2<br2/>", tc.toString());
		segments.create(4, 8); // "t1**t2**"
		assertEquals("t1<1/>[t2<2/>]", fmt.printSegmentedContent(tc, true));
		assertEquals("t2<2/>", fmt.setContent(segments.get(0).text).toString());
	}

	@Test
	public void testCreateSegmentWithPairedCodes () {
		TextFragment tf = new TextFragment("t1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("t2");
		tf.append(TagType.CLOSING, "b", "</b>");
		TextContainer tc = new TextContainer(tf);
		ISegments segments = tc.getSegments();
		assertEquals("t1<b>t2</b>", tc.toString());
		segments.create(2, 8); // "t1**t2**"
		assertEquals("t1[<b>t2</b>]", fmt.printSegmentedContent(tc, true, true));
		assertEquals("<1>t2</1>", fmt.setContent(segments.get(0).text).toString());
	}
	
	@Test
	public void testCreateSegmentWithSplitCodes () {
		TextFragment tf = new TextFragment("t1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("t2");
		tf.append(TagType.CLOSING, "b", "</b>");
		TextContainer tc = new TextContainer(tf);
		ISegments segments = tc.getSegments();
		assertEquals("t1<b>t2</b>", tc.toString());
		segments.create(4, -1);
		assertEquals("t1<b>[t2</b>]", fmt.printSegmentedContent(tc, true, true));
		assertEquals("t2<e1/>", fmt.setContent(segments.get(0).text).toString());
	}
	
	@Test
	public void testCreateMultiSegmentsWithSplitCodes () {
		TextFragment tf = new TextFragment("t1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("t2");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("t3");
		tf.append(TagType.PLACEHOLDER, "br", "<br/>");
		TextContainer tc = new TextContainer(tf);
		ISegments segments = tc.getSegments();
		assertEquals("[t1<1>t2</1>t3<2/>]", fmt.printSegmentedContent(tc, true));
		List<Range> ranges = new ArrayList<Range>();
		ranges.add(new Range(0, 4));
		ranges.add(new Range(4, -1));
		segments.create(ranges);
		assertEquals("[t1<b1/>][t2<e1/>t3<2/>]", fmt.printSegmentedContent(tc, true));
		assertEquals("t1<b1/>", fmt.setContent(segments.get(0).text).toString());
		assertEquals("t2<e1/>t3<2/>", fmt.setContent(segments.get(1).text).toString());
	}

	@Test
	public void testSameFirstAndLastSegments () {
		TextContainer tc = new TextContainer("text");
		ISegments segments = tc.getSegments();
		assertEquals("text", segments.getFirstContent().toText());
		assertSame(segments.getFirstContent(), segments.getLastContent());
		assertFalse(tc.hasBeenSegmented());
	}
	
	@Test
	public void testGetSameSegment () {
		TextContainer tc = createMultiSegmentContent();
		ISegments segments = tc.getSegments();
		assertEquals("text1", segments.getFirstContent().toText());
		assertSame(segments.getFirstContent(), segments.get(0).text);
	}
	
	@Test
	public void testGetLastSegment () {
		TextContainer tc = createMultiSegmentContent();
		ISegments segments = tc.getSegments();
		assertEquals("text2", segments.getLastContent().toText());
		assertSame(segments.getLastContent(), segments.get(1).text);
	}

	@Test
	public void testRemovePart () {
		TextContainer tc = createMultiSegmentContent();
		assertEquals("[text1] [text2]", fmt.printSegmentedContent(tc, true));
		tc.remove(2);
		assertEquals("[text1] ", fmt.printSegmentedContent(tc, true));
		tc.remove(0); // last segment: clear only
		assertEquals("[] ", fmt.printSegmentedContent(tc, true));
	}
	
	@Test
	public void testHasTextHolderIsSpacesSegmentIsText () {
		TextContainer tc = new TextContainer(" text ");
		tc.getSegments().create(1, 5);
		assertEquals(" [text] ", fmt.printSegmentedContent(tc, true));
		assertFalse(tc.hasText(false, false));
		assertTrue(tc.hasText(true, false));
		assertTrue(tc.hasText(true, true));
		assertTrue(tc.hasText(false, true));
	}
	
	@Test
	public void testHasTextHolderIsTextSegmentIsSpaces () {
		TextContainer tc = new TextContainer("T    T");
		tc.getSegments().create(1, 5);
		assertEquals("T[    ]T", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.hasText(false, false));
		assertTrue(tc.hasText(true, false));
		assertTrue(tc.hasText(true, true));
		assertTrue(tc.hasText(false, true));
	}

	@Test
	public void testHasTextHolderIsSpacesSegmentIsSpaces () {
		TextContainer tc = new TextContainer("      ");
		tc.getSegments().create(1, 5);
		assertEquals(" [    ] ", fmt.printSegmentedContent(tc, true));
		assertFalse(tc.hasText(false, false));
		assertFalse(tc.hasText(true, false));
		assertTrue(tc.hasText(true, true));
		assertTrue(tc.hasText(false, true));
	}
	
	@Test
	public void testHasTextOnlySegmentsWithSpaces () {
		TextContainer tc = new TextContainer("        ");
		List<Range> ranges = new ArrayList<Range>();
		ranges.add(new Range(0, 4)); // -> "**    "
		ranges.add(new Range(4, 8));
		tc.getSegments().create(ranges);
		assertEquals("[    ][    ]", fmt.printSegmentedContent(tc, true));
		assertFalse(tc.hasText(false, false));
		assertFalse(tc.hasText(true, false));
		assertTrue(tc.hasText(true, true));
		assertFalse(tc.hasText(false, true));
	}

	@Test
	public void testHasTextWithContentIsSegmentWithText () {
		TextContainer tc = new TextContainer("text");
		tc.getSegments().create(0, 4);
		assertEquals("[text]", fmt.printSegmentedContent(tc, true));
		assertFalse(tc.hasText(false, false));
		assertTrue(tc.hasText(true, false));
		assertTrue(tc.hasText(true, true));
		assertFalse(tc.hasText(false, true));
	}

	@Test
	public void testHasTextWithContentIsSegmentWithSpaces () {
		TextContainer tc = new TextContainer("    ");
		tc.getSegments().create(0, 4);
		assertEquals("[    ]", fmt.printSegmentedContent(tc, true));
		assertFalse(tc.hasText(false, false));
		assertFalse(tc.hasText(true, false));
		assertTrue(tc.hasText(true, true));
		assertFalse(tc.hasText(false, true));
	}

	@Test
	public void testSetContent () {
		TextContainer tc = createMultiSegmentContent();
		assertEquals("[text1] [text2]", fmt.printSegmentedContent(tc, true));
		assertFalse(tc.contentIsOneSegment());
		tc.setContent(new TextFragment("new text"));
		assertEquals("[new text]", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.contentIsOneSegment());
	}
	
	@Test
	public void testGetSetProperties () {
		TextContainer tc = new TextContainer();
		Set<String> list = tc.getPropertyNames();
		assertNotNull(list);
		assertTrue(list.size()==0);
		Property p1 = new Property("name", "value", false);
		tc.setProperty(p1);
		assertTrue(tc.hasProperty("name"));
		Property p2 = tc.getProperty("name");
		assertSame(p1, p2);
		assertEquals("value", p2.getValue());
		assertFalse(p2.isReadOnly());
		list = tc.getPropertyNames();
		assertEquals(1, list.size());
		for ( String name : list ) {
			p1 = tc.getProperty(name);
			assertEquals("value", p1.toString());
			assertSame(p1, p2);
		}
	}

	@Test
	public void testAppendSimpleSegmentToEmpty () {
		TextContainer tc = new TextContainer();
		ISegments segments = tc.getSegments();
		assertFalse(tc.hasBeenSegmented());
		assertEquals(1, segments.count());
		segments.append(new TextFragment("seg"));
		assertTrue(tc.hasBeenSegmented());
		assertEquals(1, segments.count());
		assertEquals("seg", segments.get(0).toString());
	}

	@Test
	public void testAppendSimpleSegmentToEmptyWithOption () {
		TextContainer tc = new TextContainer();
		ISegments segments = tc.getSegments();
		assertFalse(tc.hasBeenSegmented());
		assertEquals(1, segments.count());
		segments.append(new TextFragment("seg"), false);
		assertEquals(2, segments.count());
		assertEquals("", segments.get(0).toString());
		assertEquals("seg", segments.get(1).toString());
	}

	@Test
	public void testAppendSimpleSegmentToInitialEmpty () {
		TextContainer tc = new TextContainer();
		ISegments segments = tc.getSegments();
		assertFalse(tc.hasBeenSegmented());
		segments.append(new TextFragment(), !tc.hasBeenSegmented());
		assertTrue(tc.hasBeenSegmented());
		assertEquals(1, segments.count());
		assertEquals("", segments.get(0).toString());
		segments.append(new TextFragment(), !tc.hasBeenSegmented());
		assertEquals("", segments.get(0).toString());
		assertEquals("", segments.get(1).toString());
	}

	@Test
	public void testAppendSimpleSegmentToNonEmpty () {
		TextContainer tc = new TextContainer("seg1");
		ISegments segments = tc.getSegments();
		assertEquals(1, segments.count());
		segments.append(new TextFragment("seg2"));
		assertEquals(2, segments.count());
		assertEquals("seg1", segments.get(0).toString());
		assertEquals("seg2", segments.get(1).toString());
	}
	
	@Test
	public void testAutoID () {
		TextContainer tc = new TextContainer("seg1");
		ISegments segments = tc.getSegments();
		
		// Same as the one passed
		assertEquals("0", segments.get(0).id);

		segments.append(new Segment("0", new TextFragment("seg2")));
		// "0" is duplicate, so changed to "1"
		assertEquals("1", segments.get(1).id);
		
		segments.append(new Segment("id1", new TextFragment("seg3")));
		// "id1" not duplicate, so unchanged
		assertEquals("id1", segments.get(2).id);
		
		segments.append(new Segment("1", new TextFragment("seg4")));
		// "1" is duplicate, so changed to "2"
		assertEquals("2", segments.get(3).id);
		
		segments.append(new Segment("10", new TextFragment("seg5")));
		// "10" not duplicate, so unchanged
		assertEquals("10", segments.get(4).id);
		
		segments.append(new Segment("id1", new TextFragment("seg6")));
		// "id1" is duplicate, so changed to "11" (auto goes to +1 of highest value)
		assertEquals("11", segments.get(5).id);
	}
	
	@Test
	public void testAppendSeveralSegments () {
		TextContainer tc = createMultiSegmentContent();
		ISegments segments = tc.getSegments();
		assertEquals(2, segments.count());
		assertEquals("text1", segments.get(0).toString());
		assertEquals("text2", segments.get(1).toString());
		assertEquals("[text1] [text2]", fmt.printSegmentedContent(tc, true));
		segments.append(new TextFragment("add1"));
		segments.append(new Segment("segid", new TextFragment("add2")));
		assertEquals("[text1] [text2][add1][add2]", fmt.printSegmentedContent(tc, true));
		assertEquals("0", segments.get(2).id);
		assertEquals("segid", segments.get(3).id);
	}

	@Test
	public void testContentIsOneSegmentDefault () {
		TextContainer tc = new TextContainer();
		assertEquals("[]", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.contentIsOneSegment());
	}
	
	@Test
	public void ContentIsOneSegment1Segment () {
		TextContainer tc = new TextContainer("text");
		assertEquals("[text]", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.contentIsOneSegment());
	}
	
	@Test
	public void ContentIsOneSegmentSpaceInHolder () {
		TextContainer tc = new TextContainer("text ");
		tc.getSegments().create(0, 4);
		assertEquals("[text] ", fmt.printSegmentedContent(tc, true));
		assertFalse(tc.contentIsOneSegment());
	}
	
	@Test
	public void ContentIsOneSegment2Segments () {
		TextContainer tc = new TextContainer("seg1");
		ISegments segments = tc.getSegments();
		segments.append(new Segment("s2", new TextFragment("seg2")));
		assertEquals("[seg1][seg2]", fmt.printSegmentedContent(tc, true));
		assertFalse(tc.contentIsOneSegment());
	}
	
	@Test
	public void testMergingSegments () {
		TextContainer tc = createMultiSegmentContent();
		tc.getSegments().joinAll();
		assertTrue(tc.contentIsOneSegment());
		assertEquals("[text1 text2]", fmt.printSegmentedContent(tc, true));
	}

	@Test
	public void testJoinAllAndGetRanges () {
		TextContainer tc = createMultiSegmentContent();
		// "text1 text2"
		//  01234567890 ranges=(0,5),(6,11)
		assertEquals("[text1] [text2]", fmt.printSegmentedContent(tc, true));
		ArrayList<Range> ranges = new ArrayList<Range>();
		tc.getSegments().joinAll(ranges);
		assertNotNull(ranges);
		assertEquals(2, ranges.size());
		assertEquals(0, ranges.get(0).start);
		assertEquals(5, ranges.get(0).end);
		assertEquals(6, ranges.get(1).start);
		assertEquals(11, ranges.get(1).end);
	}
	
	@Test
	public void testMergingAndResplitting () {
		TextContainer tc = createMultiSegmentContent();
		ISegments segments = tc.getSegments();
		assertEquals("[text1] [text2]", fmt.printSegmentedContent(tc, true));
		ArrayList<Range> ranges = new ArrayList<Range>();
		tc.getSegments().joinAll(ranges);
		assertEquals(1, segments.count());
		assertEquals("[text1 text2]", fmt.printSegmentedContent(tc, true));
		segments.create(ranges);
		assertEquals(2, segments.count());
		assertEquals("[text1] [text2]", fmt.printSegmentedContent(tc, true));
		assertEquals("text1", segments.get(0).toString());
		assertEquals("text2", segments.get(1).toString());
	}
	
	@Test
	public void testGetSegmentFromId () {
		TextContainer tc = createMultiSegmentContent();
		ISegments segments = tc.getSegments();
		assertSame(segments.get(0), segments.get("s1"));
		assertSame(segments.get(1), segments.get("s2"));
		segments.get(1).id = "newId";
		assertSame(segments.get(1), segments.get("newId"));
	}

	@Test
	public void testGetSegmentFromIdAfterReindex () {
		TextContainer tc = createMultiSegmentContent();
		ISegments segments = tc.getSegments();
		assertSame(segments.get(0), segments.get("s1"));
		assertSame(segments.get(1), segments.get("s2"));
		tc.changePart(1); // Change non-segment to a segment
		assertSame(segments.get(0), segments.get("s1"));
		assertSame(segments.get(1), segments.get("0")); // Inserted
		assertSame(segments.get(2), segments.get("s2"));
		assertEquals("text2", segments.get("s2").text.toText());
		assertEquals(" ", segments.get("0").text.toText());
	}

	@Test
	public void testJoinSegmentWithNextOnUnsegmented () {
		TextContainer tc = new TextContainer("text");
		ISegments segments = tc.getSegments();
		segments.get(0).id = "id1"; // Set the ID to non-default
		segments.joinWithNext(0);
		assertEquals("id1", segments.get(0).id);
		assertFalse(tc.hasBeenSegmented());
		assertTrue(tc.contentIsOneSegment());
	}

	@Test
	public void testJoinSegmentWithNext () {
		TextContainer tc = createMultiSegmentContent();
		ISegments segments = tc.getSegments();
		// Make it 3 segments
		segments.append(new TextFragment("seg3"));
		segments.get(1).id = "id2"; // Set the ID to non-default
		assertEquals(3, segments.count());
		assertEquals("[text1] [text2][seg3]", fmt.printSegmentedContent(tc, true));
		// Join second segment with next
		segments.joinWithNext(1);
		assertEquals("[text1] [text2seg3]", fmt.printSegmentedContent(tc, true));
		assertEquals("id2", segments.get(1).id);
		assertFalse(tc.contentIsOneSegment());
		assertTrue(tc.hasBeenSegmented());
		// Second first with next
		segments.joinWithNext(0);
		assertEquals("[text1 text2seg3]", fmt.printSegmentedContent(tc, true));
		assertEquals("s1", segments.get(0).id);
		assertTrue(tc.contentIsOneSegment());
		assertTrue(tc.hasBeenSegmented()); // "manual" segmentation change
	}
	
	@Test
	public void testJoinPartWithNextPartsSimple () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc, true));
		tc.joinWithNext(1, 1);
		assertEquals("[text1<1/>] text2<2/>", fmt.printSegmentedContent(tc, true));
	}

	@Test
	public void testJoinPartWithNextPartsTwoParts () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc, true));
		tc.joinWithNext(0, 2);
		assertEquals("[text1<1/> text2<2/>]", fmt.printSegmentedContent(tc, true));
		tc = createMultiSegmentContentWithCodes();
		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc, true));
		tc.joinWithNext(0, -1); // Same, using -1
		assertEquals("[text1<1/> text2<2/>]", fmt.printSegmentedContent(tc, true));
	}

	@Test
	public void testJoinPartWithNextPartsEnsureSegment () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc, true));
		tc.changePart(0); // Change segment 0 into a non-segment
		assertEquals("text1<1/> [text2<2/>]", fmt.printSegmentedContent(tc, true));
		tc.joinWithNext(0, 2); // Join non-segment with all parts
		assertTrue(tc.contentIsOneSegment()); // Non-segment turned to segment because single
		assertEquals("[text1<1/> text2<2/>]", fmt.printSegmentedContent(tc, true));
	}
	
	@Test
	public void testChangePartSegmentToNonSegment () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc, true));
		tc.changePart(0); // Change segment 0 into a non-segment
		assertEquals("text1<1/> [text2<2/>]", fmt.printSegmentedContent(tc, true));
	}

	@Test
	public void testChangePartNonSegmentToSegment () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		ISegments segments = tc.getSegments();
		
		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc, true));
		tc.changePart(1); // Change non-segment into a segment
		assertEquals("[text1<1/>][ ][text2<2/>]", fmt.printSegmentedContent(tc, true));
		assertEquals("2", segments.get(1).id); // Check auto-id
	}

	@Test
	public void testChangePartOnlySegmentToNonSegment () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc, true));
		tc.changePart(0); // Change segment into a non-segment
		assertEquals("text1<1/> [text2<2/>]", fmt.printSegmentedContent(tc, true));
		tc.changePart(2); // Try to change only segment into non-segment
		// Should not change
		assertEquals("text1<1/> [text2<2/>]", fmt.printSegmentedContent(tc, true));
	}

	@Test
	public void testMergingSegmentsWithCodes () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		ISegments segments = tc.getSegments();
		segments.joinAll();
		assertEquals(1, segments.count());
		TextFragment tf = segments.getFirstContent();
		assertEquals("text1<br/> text2<br/>", tc.toString());
		assertEquals("text1<br/> text2<br/>", tf.toText());
		List<Code> codes = tf.getCodes();
		assertEquals(2, codes.size());
		assertEquals(1, codes.get(0).id);
		assertEquals(2, codes.get(1).id);
	}

	@Test
	public void testMergingAndResplittingWithCodes () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		ISegments segments = tc.getSegments();
		
		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc, true));
		ArrayList<Range> ranges = new ArrayList<Range>();
		segments.joinAll(ranges);
		assertEquals(1, segments.count());
		assertEquals("[text1<1/> text2<2/>]", fmt.printSegmentedContent(tc, true));
		List<Code> codes = segments.getFirstContent().getCodes();
		assertEquals(2, codes.size());
		assertEquals(1, codes.get(0).id);
		assertEquals(2, codes.get(1).id);
		segments.create(ranges);
		assertEquals(2, segments.count());
		assertEquals("text1<br/>", segments.get(0).toString());
		assertEquals("text2<br/>", segments.get(1).toString());
	}
	
	@Test
	public void testSplitPartNewSegmentOnLeft () {
		TextContainer tc = new TextContainer("part1part2");
		ISegments segments = tc.getSegments();
		
		assertEquals("[part1part2]", fmt.printSegmentedContent(tc, true));
		tc.split(0, 0, 5, true);
		assertEquals("[part1][part2]", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.hasBeenSegmented());
		assertFalse(tc.contentIsOneSegment());
		assertEquals("0", segments.get(1).id); // old segment is on the right
		assertEquals("1", segments.get(0).id); // new segment is on the left
	}
	
	@Test
	public void testSplitPartNewNonSegmentOnLeft () {
		TextContainer tc = new TextContainer("part1part2");
		ISegments segments = tc.getSegments();
		
		assertEquals("[part1part2]", fmt.printSegmentedContent(tc, true));
		tc.split(0, 0, 5, false); // Create non-segment
		assertEquals("part1[part2]", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.hasBeenSegmented());
		assertFalse(tc.contentIsOneSegment());
		assertEquals("0", segments.get(0).id); // old segment is on the right
	}
	
	@Test
	public void testSplitPartNewSegmentOnRight () {
		TextContainer tc = new TextContainer("part1part2");
		ISegments segments = tc.getSegments();
		
		assertEquals("[part1part2]", fmt.printSegmentedContent(tc, true));
		tc.split(0, 5, -1, true);
		assertEquals("[part1][part2]", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.hasBeenSegmented());
		assertFalse(tc.contentIsOneSegment());
		assertEquals("0", segments.get(0).id); // old segment is on the left
		assertEquals("1", segments.get(1).id); // new segment is on the right
	}
	
	@Test
	public void testSplitPartNonSegmentOnRight () {
		TextContainer tc = new TextContainer("part1part2");
		ISegments segments = tc.getSegments();
		
		assertEquals("[part1part2]", fmt.printSegmentedContent(tc, true));
		tc.split(0, 5, -1, false); // Create non-segment part
		assertEquals("[part1]part2", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.hasBeenSegmented());
		assertFalse(tc.contentIsOneSegment());
		assertEquals("0", segments.get(0).id); // old segment is on the left
	}
	
	@Test
	public void testSplitPartWithoutSpan () {
		TextContainer tc = new TextContainer("part1part2");
		ISegments segments = tc.getSegments();

		assertEquals("[part1part2]", fmt.printSegmentedContent(tc, true));
		tc.split(0, 5, 5, false); // Ask for non-segment, but that should be ignored
		assertEquals("[part1][part2]", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.hasBeenSegmented());
		assertFalse(tc.contentIsOneSegment());
		assertEquals("0", segments.get(0).id); // new segment is on the left
		assertEquals("1", segments.get(1).id); // new segment is on the right
	}
	
	@Test
	public void testSplitNewSegmentAtMiddle () {
		TextContainer tc = new TextContainer("part1part2part3");
		ISegments segments = tc.getSegments();
		
		assertEquals("[part1part2part3]", fmt.printSegmentedContent(tc, true));
		tc.split(0, 5, 10, true);
		assertEquals("[part1][part2][part3]", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.hasBeenSegmented());
		assertFalse(tc.contentIsOneSegment());
		assertEquals("0", segments.get(0).id); // old segment 0
		assertEquals("1", segments.get(1).id); // new segment 
		assertEquals("2", segments.get(2).id); // second new part (right of segment 0)
	}
	
	@Test
	public void testSplitNewNonSegmentAtMiddle () {
		TextContainer tc = new TextContainer("part1part2part3");
		ISegments segments = tc.getSegments();
		
		assertEquals("[part1part2part3]", fmt.printSegmentedContent(tc, true));
		tc.split(0, 5, 10, false); // Create non-segment
		assertEquals("[part1]part2[part3]", fmt.printSegmentedContent(tc, true));
		assertTrue(tc.hasBeenSegmented());
		assertFalse(tc.contentIsOneSegment());
		assertEquals("0", segments.get(0).id); // old segment 0
		assertFalse(tc.get(1).isSegment());
		assertEquals("1", segments.get(1).id); // last part (right of segment 0)
	}
	
	@Test
	public void testVariousSplitsAndJoins () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		ISegments segments = tc.getSegments();

		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc, true));
		// Make "te" an non-segment
		tc.split(2, 0, 2, false);
		assertEquals("[text1<1/>] te[xt2<2/>]", fmt.printSegmentedContent(tc, true));
		assertEquals("te", tc.get(2).toString());
		// Make <1/> a segment
		tc.split(0, 5, -1, true);
		assertEquals("[text1][<1/>] te[xt2<2/>]", fmt.printSegmentedContent(tc, true));
		assertEquals("2", segments.get(1).id);
		// Join all in one segment after "text1"
		segments.joinWithNext(1);
		assertEquals("[text1][<1/> text2<2/>]", fmt.printSegmentedContent(tc, true));
		assertEquals("0", segments.get(0).id);
		assertEquals("2", segments.get(1).id);
	}

	@Test
	public void testSplitResultingInNoChanges () {
		TextContainer tc = new TextContainer("text");
		ISegments segments = tc.getSegments();
		
		assertEquals("[text]", fmt.printSegmentedContent(tc, true));
		tc.split(0, 0, 0, true); // Span is empty and at the front
		tc.split(0, 4, 4, true); // Span is empty and at the back
		tc.split(0, 4, -1, true); // Span is empty and at the back
		tc.split(0, 0, -1, true); // Span is the whole part
		tc.split(0, 0, 4, true); // Span is the whole part
		assertEquals("[text]", fmt.printSegmentedContent(tc, true));
		assertFalse(tc.hasBeenSegmented());
		assertTrue(tc.contentIsOneSegment());
		assertEquals("0", segments.get(0).id); // old segment 0
	}

	@Test
	public void testCompareTo_OneAndOneSame () {
		TextContainer tc1 = new TextContainer("text");
		TextContainer tc2 = new TextContainer("text");
		assertEquals(0, tc1.compareTo(tc2, true));
		assertEquals(0, tc1.compareTo(tc2, false));
	}
	
	@Test
	public void testCompareTo_OneAndOneDifferentInText () {
		TextContainer tc1 = new TextContainer("text1");
		TextContainer tc2 = new TextContainer("text2");
		assertEquals(-1, tc1.compareTo(tc2, true));
		assertEquals(-1, tc1.compareTo(tc2, false));
		tc1 = new TextContainer("text2"); // First is greater now
		tc2 = new TextContainer("text1");
		assertEquals(1, tc1.compareTo(tc2, true));
		assertEquals(1, tc1.compareTo(tc2, false));
	}

	@Test
	public void testCompareTo_TwoOnTwoSame () {
		TextContainer tc1 = createMultiSegmentContent();
		TextContainer tc2 = createMultiSegmentContent();
		assertEquals(0, tc1.compareTo(tc2, true));
		assertEquals(0, tc1.compareTo(tc2, false));
	}
	
	@Test
	public void testCompareTo_OneOnTwoSameText () {
		// Same text, one non-segmented the other segmented
		TextContainer tc1 = new TextContainer("text1 text2");
		TextContainer tc2 = createMultiSegmentContent();
		assertFalse(0==tc1.compareTo(tc2, true));
		assertFalse(0==tc1.compareTo(tc2, false));
	}

	@Test
	public void testCompareTo_TwoOnTwoDifferenceInCodes () {
		TextContainer tc1 = createMultiSegmentContentWithCodes();
		TextContainer tc2 = createMultiSegmentContentWithCodes();
		tc2.get(0).getContent().getCode(0).data = new StringBuilder("<XYZ/>");
		assertFalse(0==tc1.compareTo(tc2, true)); // Code sensitive
		assertTrue(0==tc1.compareTo(tc2, false));
	}

	@Test
	public void testCompareTo_TwoOnTwoNoDifferenceInCodes () {
		TextContainer tc1 = createMultiSegmentContentWithCodes();
		TextContainer tc2 = createMultiSegmentContentWithCodes();
		assertTrue(0==tc1.compareTo(tc2, true));
		assertTrue(0==tc1.compareTo(tc2, false));
	}

	@Test
	public void testUnwrap_All () {
		TextContainer tc1 = new TextContainer(" \t \n");
		TextContainer tc2 = tc1.clone();
		TextContainer tc3 = tc1.clone();
		TextContainer tc4 = tc1.clone();
		
		tc1.unwrap(true, true);
		assertEquals("[]", fmt.printSegmentedContent(tc1, true));
		
		tc2.unwrap(false, true);
		assertEquals("[ ]", fmt.printSegmentedContent(tc2, true));
		
		tc3.unwrap(true, false);
		assertEquals("[]", fmt.printSegmentedContent(tc3, true));
		
		tc4.unwrap(false, false);
		assertEquals("[ ]", fmt.printSegmentedContent(tc4, true));
	}
	
	@Test
	public void testUnwrap_Simple () {
		TextContainer tc1 = new TextContainer(" a b\tc \n\t");
		TextContainer tc2 = tc1.clone();
		TextContainer tc3 = tc1.clone();
		TextContainer tc4 = tc1.clone();
		
		tc1.unwrap(true, true);
		assertEquals("[a b c]", fmt.printSegmentedContent(tc1, true));
		
		tc2.unwrap(false, true);
		assertEquals("[ a b c ]", fmt.printSegmentedContent(tc2, true));
		
		tc3.unwrap(true, false);
		assertEquals("[a b c]", fmt.printSegmentedContent(tc3, true));

		tc4.unwrap(false, false);
		assertEquals("[ a b c ]", fmt.printSegmentedContent(tc4, true));
	}
	
	@Test
	public void testUnwrap_Parts1SegNoText () {
		TextContainer tc1 = new TextContainer();
		tc1.append(new TextFragment(" \t "));
		tc1.append(new TextFragment(" "));
		tc1.append(new TextFragment("  "));
		tc1.append(new TextFragment(" \n"));
		TextContainer tc2 = tc1.clone();
		TextContainer tc3 = tc1.clone();
		TextContainer tc4 = tc1.clone();
		assertEquals("[ \t ]    \n", fmt.printSegmentedContent(tc1, true));
		
		tc1.unwrap(true, true);
		assertEquals("[]", fmt.printSegmentedContent(tc1, true));
		
		tc2.unwrap(false, true); // Ending deletion is because of collapse is true
		assertEquals("[ ]", fmt.printSegmentedContent(tc2, true));

		tc3.unwrap(true, false); // Ends are trimmed
		assertEquals("[]", fmt.printSegmentedContent(tc3, true));
		
		tc4.unwrap(false, false); // End not trimmed, last part is not collapsed
		assertEquals("[ ] ", fmt.printSegmentedContent(tc4, true));
	}

	@Test
	public void testUnwrap_MixedPartsWithText () {
		TextContainer tc1 = new TextContainer();
		ISegments segments = tc1.getSegments();		
		tc1.append(new TextFragment(" \tt1 "));
		tc1.append(new TextFragment("   "));
		segments.append(new TextFragment("t2"));
		tc1.append(new TextFragment("  "));
		segments.append(new TextFragment(" t3\n\n"));
		tc1.append(new TextFragment("  "));
		TextContainer tc2 = tc1.clone();
		TextContainer tc3 = tc1.clone();
		TextContainer tc4 = tc1.clone();
		assertEquals("[ \tt1 ]   [t2]  [ t3\n\n]  ", fmt.printSegmentedContent(tc1, true));
	
		tc1.unwrap(true, true);
		assertEquals("[t1 ][t2] [t3]", fmt.printSegmentedContent(tc1, true));
		
		tc2.unwrap(false, true);
		assertEquals("[ t1 ][t2] [t3 ]", fmt.printSegmentedContent(tc2, true));

		tc3.unwrap(true, false);
		assertEquals("[t1 ][t2] [t3]", fmt.printSegmentedContent(tc3, true));
		
		tc4.unwrap(false, false);
		assertEquals("[ t1 ][t2] [t3 ]", fmt.printSegmentedContent(tc4, true));
	}
	
	@Test
	public void testUnwrap_MixedPartsWithText2 () {
		TextContainer tc1 = new TextContainer();
		ISegments segments = tc1.getSegments();		
		segments.append(new TextFragment("t1"));
		tc1.append(new TextFragment(" "));
		TextContainer tc2 = tc1.clone();
		TextContainer tc3 = tc1.clone();
		TextContainer tc4 = tc1.clone();
		assertEquals(2, tc1.count());
		assertEquals("[t1] ", fmt.printSegmentedContent(tc1, true));
		
		tc1.unwrap(true, true);
		assertEquals("[t1]", fmt.printSegmentedContent(tc1, true));
		
		tc2.unwrap(false, true);
		assertEquals("[t1] ", fmt.printSegmentedContent(tc2, true));

		tc3.unwrap(true, false);
		assertEquals("[t1]", fmt.printSegmentedContent(tc3, true));
		
		tc4.unwrap(false, false);
		assertEquals("[t1] ", fmt.printSegmentedContent(tc4, true));
	}
	
	@Test
	public void testUnwrap_MixedPartsEmpties () {
		TextContainer tc1 = new TextContainer();
		ISegments segments = tc1.getSegments();
		tc1.append(" ", false);
		assertEquals("[] ", fmt.printSegmentedContent(tc1, true));
		segments.append(new TextFragment(""), false);
		assertEquals("[] []", fmt.printSegmentedContent(tc1, true));
		tc1.append("\n", false);
		assertEquals("[] []\n", fmt.printSegmentedContent(tc1, true));
		segments.append(new TextFragment(""), false);
		assertEquals("[] []\n[]", fmt.printSegmentedContent(tc1, true));
		tc1.append("\n", false);
		assertEquals("[] []\n[]\n", fmt.printSegmentedContent(tc1, true));
		TextContainer tc2 = tc1.clone();
		TextContainer tc3 = tc1.clone();
		TextContainer tc4 = tc1.clone();
		
		tc1.unwrap(true, true);
		assertEquals("[][][]", fmt.printSegmentedContent(tc1, true));
		
		tc2.unwrap(false, true); // Ending is removed by collapsing not by trimming 
		assertEquals("[][][]", fmt.printSegmentedContent(tc2, true));

		tc3.unwrap(true, false);
		assertEquals("[] [] []", fmt.printSegmentedContent(tc3, true));
		
		tc4.unwrap(false, false);
		assertEquals("[] [] [] ", fmt.printSegmentedContent(tc4, true));
	}

	@Test
	public void testUnwrap_MixedParts () {
		TextContainer tc1 = new TextContainer();
		ISegments segments = tc1.getSegments();
		tc1.append(" ", false);
		assertEquals("[] ", fmt.printSegmentedContent(tc1, true));
		segments.append(new TextFragment("  t1 "), false);
		assertEquals("[] [  t1 ]", fmt.printSegmentedContent(tc1, true));
		segments.append(new TextFragment("  t2" ), false);
		assertEquals("[] [  t1 ][  t2]", fmt.printSegmentedContent(tc1, true));
		TextContainer tc2 = tc1.clone();
		TextContainer tc3 = tc1.clone();
		TextContainer tc4 = tc1.clone();
		
		tc1.unwrap(true, true);
		assertEquals("[][t1 ][t2]", fmt.printSegmentedContent(tc1, true));
		
		tc2.unwrap(false, true); // Trimming s done after first segment 
		assertEquals("[][t1 ][t2]", fmt.printSegmentedContent(tc2, true));

		tc3.unwrap(true, false);
		assertEquals("[] [t1 ][t2]", fmt.printSegmentedContent(tc3, true));
		
		tc4.unwrap(false, false);
		assertEquals("[] [t1 ][t2]", fmt.printSegmentedContent(tc4, true));
	}

	@Test
	public void testSegments () {
		String originalText = "[seg1][seg2] [seg3]";
		TextContainer tc = new TextContainer(originalText);
		ISegments segments = tc.getSegments();
		
		// "[seg1][seg2] [seg3]"
		//  0123456789012345678
		assertFalse(tc.hasBeenSegmented());
		List<Range> ranges = new ArrayList<Range>();
		ranges.add(new Range(0, 6));
		ranges.add(new Range(6, 12));
		ranges.add(new Range(13, -1));
		segments.create(ranges);
		assertEquals(4, tc.count());
		assertEquals(3, segments.count());
		assertEquals("[seg1]", segments.get(0).toString());
		assertEquals("[seg2]", segments.get(1).toString());
		assertEquals("[seg3]", segments.get(2).toString());
		// Test merge all
		segments.joinAll();
		assertFalse(tc.hasBeenSegmented());
		assertEquals(originalText, tc.toString());
	}

	@Test
	public void testSegmentRemoval () {
		String originalText = "[seg1][seg2] [seg3]";
		TextContainer tc = new TextContainer(originalText);
		ISegments segments = tc.getSegments();
		// Test segmenting from an array
		List<Range> ranges = new ArrayList<Range>();
		ranges.add(new Range(0, 6));
		ranges.add(new Range(6, 12));
		ranges.add(new Range(13, 19));
		segments.create(ranges);
		assertTrue(tc.hasBeenSegmented());
		assertEquals("[seg2]", segments.get(1).toString());
		tc.remove(segments.getPartIndex(1));
		assertEquals("[seg3]", segments.get(1).toString());
		tc.remove(segments.getPartIndex(0));
		assertEquals("[seg3]", segments.get(0).toString());
		tc.remove(segments.getPartIndex(0));
		assertEquals("", segments.get(0).toString()); // Always 1 segment at least
	}
	
	@Test
	public void testSegmentsFromArray () {
		String originalText = "[seg1][seg2] [seg3]";
		TextContainer tc = new TextContainer(originalText);
		ISegments segments = tc.getSegments();

		// Test segmenting from an array
		List<Range> ranges = new ArrayList<Range>();
		ranges.add(new Range(0, 6));
		ranges.add(new Range(6, 12));
		ranges.add(new Range(13, 19));
		
		segments.create(ranges);
		assertTrue(tc.hasBeenSegmented());
		assertEquals("[seg1]", segments.get(0).toString());
		assertEquals("[seg2]", segments.get(1).toString());
		assertEquals("[seg3]", segments.get(2).toString());
		// Test Merge one-by-one
		assertEquals(3, segments.count());
		segments.joinWithNext(0);
		assertEquals(2, segments.count());
		segments.joinWithNext(0);
		assertEquals(1, segments.count());
		assertTrue(tc.contentIsOneSegment());
		assertTrue(tc.hasBeenSegmented()); // "manual" segmentation changes
		assertEquals(originalText, tc.toString());
		
		// Re-segment again and re-merge out of sequence
		segments.create(ranges);
		assertEquals(3, segments.count());
		assertEquals(3, segments.count());
		segments.joinWithNext(0); // ([seg1])+[seg2]
		segments.joinWithNext(0); // ([seg1]+[seg2])+[seg3]
		assertEquals(originalText, tc.toString());
	}

	@Test
	public void testStorage_WithoutCodes () {
		TextContainer tc1 = createMultiSegmentContent();
		String data = TextContainer.contentToString(tc1);
		TextContainer tc2 = TextContainer.stringToContent(data);
		assertEquals("[text1] [text2]", fmt.printSegmentedContent(tc2, true));
		assertTrue(tc2.hasBeenSegmented());
		assertEquals("text1 text2", tc2.toString());
	}
	
	@Test
	public void testStorage_WithCodes () {
		TextContainer tc1 = createMultiSegmentContentWithCodes();
		String data = TextContainer.contentToString(tc1);
		TextContainer tc2 = TextContainer.stringToContent(data);
		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc2, true));
		assertTrue(tc2.hasBeenSegmented());
		assertEquals("text1<br/> text2<br/>", tc2.toString());
	}

	@Test
	public void testSplitStorage_WithoutCodes () {
		TextContainer tc1 = createMultiSegmentContent();
		String data[] = TextContainer.contentToSplitStorage(tc1);
		TextContainer tc2 = TextContainer.splitStorageToContent(data[0], data[1]);
		assertEquals("[text1] [text2]", fmt.printSegmentedContent(tc2, true));
		assertTrue(tc2.hasBeenSegmented());
		assertEquals("text1 text2", tc2.toString());
	}
	
	@Test
	public void testSplitStorage_WithCodes () {
		TextContainer tc1 = createMultiSegmentContentWithCodes();
		String data[] = TextContainer.contentToSplitStorage(tc1);
		TextContainer tc2 = TextContainer.splitStorageToContent(data[0], data[1]);
		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc2, true));
		assertTrue(tc2.hasBeenSegmented());
		assertEquals("text1<br/> text2<br/>", tc2.toString());
	}

	@Test
	public void testAppendPart_AsPart () {
		TextContainer tc = new TextContainer();
		tc.append(new TextPart("n0"));
		tc.append(new Segment(null, new TextFragment("s1")));
		tc.changePart(0);
		assertEquals("n0[s1]", fmt.printSegmentedContent(tc, true));
	}

	@Test
	public void testAppendPart_AsStringAndTextFragment () {
		TextContainer tc = new TextContainer();
		tc.append("p0");
		tc.append(new TextFragment("p1"));
		tc.changePart(0);
		assertEquals("[p0]p1", fmt.printSegmentedContent(tc, true));
	}

	@Test
	public void testGetPartIndex_Found () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		assertEquals(2, tc.getSegments().count());
		// Segment index 1 is part 2
		assertEquals(2, tc.getSegments().getPartIndex(1));
	}
	
	@Test
	public void testGetPartIndex_NotFound () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		// No segment index 3
		assertEquals(-1, tc.getSegments().getPartIndex(3));
	}
	
	@Test
	public void testSwapSegments () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		ISegments segments = tc.getSegments();
		
		assertEquals("[text1<1/>] [text2<2/>]", fmt.printSegmentedContent(tc, true));
		assertEquals("0", segments.get(0).id);
		assertEquals("1", segments.get(1).id);
		tc.getSegments().swap(0, 1);
		assertEquals("[text2<2/>] [text1<1/>]", fmt.printSegmentedContent(tc, true));
		assertEquals("1", segments.get(0).id);
		assertEquals("0", segments.get(1).id);
	}
	
	@Test
	public void testGetSegmentIndex () {
		TextContainer tc = createMultiSegmentContentWithCodes();
		ISegments segments = tc.getSegments();
		assertEquals("1", segments.get(tc.getSegments().getIndex("1")).id);
	}

	@Test
	public void testInsertSegment () {
		TextContainer tc = createMultiSegmentContent();
		ISegments segs = tc.getSegments();
		segs.insert(1, new Segment("s2", new TextFragment("[new-seg]"))); // s2 exists already
		Segment seg = segs.get(1);
		assertEquals("[new-seg]", seg.toString()); // segment was inserted
		assertFalse("s2".equals(seg.id)); // new segment has new id
		assertEquals("s2", segs.get(2).id); // Previous segment kept its id
	}

	@Test
	public void testSetSegment () {
		TextContainer tc = createMultiSegmentContent();
		ISegments segs = tc.getSegments();
		segs.set(1, new Segment("s1", new TextFragment("[new-seg]"))); // s1 exists already
		Segment seg = segs.get(1);
		assertEquals("[new-seg]", seg.toString()); // segment was inserted
		assertEquals("0", seg.id); // new segment has new id
		assertEquals("[new-seg]", seg.text.toString());
	}

	@Test
	public void testGetFirstSegment () {
		Segment seg = new Segment("qwerty", new TextFragment("xyz"));
		TextContainer tc = new TextContainer(seg);
		assertNotNull(tc.getFirstSegment());
		assertSame(seg, tc.getFirstSegment());
	}
	
	@Test
	public void testGetSegmented () {
		TextContainer tc = new TextContainer();
		assertFalse(tc.hasBeenSegmented());
	}
	
	@Test
	public void testPartsAddition () {
		// Default
		TextContainer tc = new TextContainer();
		tc.append(new TextPart("tp"), true);
		tc.append(new Segment("seg1"), false);
		tc.append(new Segment("seg2"), false);
		
		assertEquals(3, tc.count());
		assertEquals(3, tc.getSegments().count());
		
		// Changing the 1-st part
		tc = new TextContainer();
		tc.append(new TextPart("tp"), true);		
		tc.append(new Segment("seg1"), false);
		tc.append(new Segment("seg2"), false);
		tc.changePart(0);
		
		assertEquals(3, tc.count());
		assertEquals(2, tc.getSegments().count());
		
		// Adding the part last
		tc = new TextContainer();			
		tc.append(new Segment("seg1"), true);
		tc.append(new Segment("seg2"), false);
		tc.insert(0, new TextPart("tp"));
		
		assertEquals(3, tc.count());
		assertEquals(2, tc.getSegments().count());
		
		// Constructor with a text part and segments
		tc = new TextContainer(new TextPart("tp"), new Segment("seg1"), new Segment("seg2"));
		assertEquals(3, tc.count());
		assertEquals(2, tc.getSegments().count());
		
		assertFalse(tc.get(0).isSegment());
		assertTrue(tc.get(1).isSegment());
		assertTrue(tc.get(2).isSegment());
		
		// Constructor with a text part
		tc = new TextContainer(new TextPart("tp"));
		assertEquals(1, tc.count());
		assertEquals(1, tc.getSegments().count());
		assertTrue(tc.get(0).isSegment());
		
		// Constructor with a segment
		tc = new TextContainer(new Segment("seg1"));
		assertEquals(1, tc.count());
		assertEquals(1, tc.getSegments().count());
		assertTrue(tc.get(0).isSegment());
		
		// Constructor with only text parts
		tc = new TextContainer(new TextPart("tp1"), new TextPart("tp2"), new TextPart("tp3"));
		assertEquals(3, tc.count());
		assertEquals(1, tc.getSegments().count());
		assertTrue(tc.get(0).isSegment());
		assertFalse(tc.get(1).isSegment());
		assertFalse(tc.get(2).isSegment());
		
		// Constructor with only segments
		tc = new TextContainer(new Segment("seg1"), new Segment("seg2"), new Segment("seg3"));
		assertEquals(3, tc.count());
		assertEquals(3, tc.getSegments().count());
		assertTrue(tc.get(0).isSegment());
		assertTrue(tc.get(1).isSegment());
		assertTrue(tc.get(2).isSegment());
	}

	private TextContainer createMultiSegmentContent () {
		TextFragment tf = new TextFragment("text1 text2");
		TextContainer tc = new TextContainer(tf);
		List<Range> ranges = new ArrayList<Range>();
		ranges.add(new Range(0, 5, "s1"));
		ranges.add(new Range(6, 11, "s2"));
		tc.getSegments().create(ranges);
		return tc;
	}

	private TextContainer createMultiSegmentContentWithCodes () {
		TextFragment tf = new TextFragment("text1");
		tf.append(TagType.PLACEHOLDER, "br", "<br/>");
		TextContainer tc = new TextContainer(tf);
		tc.append(new TextFragment(" "));
		tf = new TextFragment("text2");
		tf.append(TagType.PLACEHOLDER, "br", "<br/>", 2);
		// Segmented text have continuous code IDs sequence across segments
		// they do not restart at 1 for each segment
//		code.id = 2;
//		tf.balanceMarkers(); // Update lastCodeID
		tc.getSegments().append(tf);
		return tc;
	}
}
