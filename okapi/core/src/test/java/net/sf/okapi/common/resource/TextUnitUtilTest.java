/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TextUnitUtilTest {

	private GenericContent fmt = new GenericContent();
	private LocaleId locTrg = LocaleId.fromString("trg");

	@Test
	public void testAdjustTargetFragment () {
		TextFragment toTransSrc = makeFragment1();
		TextFragment proposalTrg = makeFragment1Bis("trg");
		assertEquals("{B}A{/B}B{BR/}C trg", proposalTrg.toText());
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(toTransSrc, proposalTrg, true, true, null, null);
		assertEquals("[b]A[/b]B[br/]C trg", proposalTrg.toText());
	}
	
	@Test
	public void testAdjustIncompleteTargetFragmentAutoAdded () {
		TextFragment toTransSrc = makeFragment1();
		TextFragment proposalTrg = makeFragment1Bis("trg");
		proposalTrg.remove(6, 8); // "xxAxxBxxC trg"
		assertEquals("{B}A{/B}BC trg", proposalTrg.toText());
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(toTransSrc, proposalTrg, true, true, null, null);
		assertEquals("[b]A[/b]BC trg[br/]", proposalTrg.toText());
	}
	
	@Test
	public void testAdjustIncompleteTargetFragmentNoAddition () {
		TextFragment toTransSrc = makeFragment1();
		TextFragment proposalTrg = makeFragment1Bis("with warning");
		proposalTrg.remove(6, 8); // "xxAxxBxxC with warning"
		assertEquals("{B}A{/B}BC with warning", proposalTrg.toText());
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(toTransSrc, proposalTrg, true, false, null, null);
		assertEquals("[b]A[/b]BC with warning", proposalTrg.toText());
	}
	
	@Test
	public void testAdjustNoCodes () {
		ITextUnit tu = new TextUnit("1", "src");
		TextFragment newSrc = new TextFragment("src");
		TextFragment newTrg = new TextFragment("trg");
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(tu.getSource().getSegments().getFirstContent(), newTrg, true, false, newSrc, tu);
		assertEquals(locTrg, newTrg.toText());
	}
	
	@Test
	public void testAdjustSameMarkers () {
		ITextUnit tu = createTextUnit1();
		TextFragment tf = new TextFragment("T ");
		tf.append(TagType.OPENING, "b", "<T>");
		tf.append("BOLD");
		tf.append(TagType.CLOSING, "b", "</T>");
		tf.append(" T ");
		tf.append(TagType.PLACEHOLDER, "br", "<PH/>");
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(tu.getSource().getSegments().getFirstContent(), tf, true, false, null, tu);
		assertEquals("T <b>BOLD</b> T <br/>", tf.toText());
		fmt.setContent(tf);
		assertEquals("T <1>BOLD</1> T <2/>", fmt.toString());
	}

	@Test
	public void testAdjustExtraMarkers () {
		ITextUnit tu = createTextUnit1();
		TextFragment tf = new TextFragment("T ");
		tf.append(TagType.OPENING, "b", "<T>");
		tf.append("BOLD");
		tf.append(TagType.CLOSING, "b", "</T>");
		tf.append(" T ");
		tf.append(TagType.PLACEHOLDER, "br", "<PH/>");
		tf.append(TagType.PLACEHOLDER, "extra", "<EXTRA/>");
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(tu.getSource().getSegments().getFirstContent(), tf, true, false, null, tu);
		assertEquals("T <b>BOLD</b> T <br/><EXTRA/>", tf.toText());
		fmt.setContent(tf);
		assertEquals("T <1>BOLD</1> T <2/><3/>", fmt.toString());
	}
	
	@Test
	public void testAdjustMissingMarker () {
		ITextUnit tu = createTextUnit1();
		TextFragment tf = new TextFragment("T ");
		tf.append(TagType.OPENING, "b", "<T>");
		tf.append("BOLD");
		tf.append(" T ");
		tf.append(TagType.PLACEHOLDER, "br", "<PH/>");
		tf.append(TagType.PLACEHOLDER, "extra", "<EXTRA/>");
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(tu.getSource().getSegments().getFirstContent(), tf, true, false, null, tu);
		assertEquals("T <b>BOLD T <br/><EXTRA/>", tf.toText());
		fmt.setContent(tf);
		assertEquals("T <b1/>BOLD T <2/><3/>", fmt.toString());
	}
	
	@Test
	public void testAdjustDifferentTextSameMarkers () {
		ITextUnit tu = createTextUnit1();
		TextFragment tf = new TextFragment("U ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("BOLD");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(" U ");
		tf.append(TagType.PLACEHOLDER, "br", "<br/>");
		// Fuzzy match but codes are the same
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(tu.getSource().getFirstContent(), tf, true, false, null, tu);
		assertEquals("U <b>BOLD</b> U <br/>", tf.toText());
		assertEquals("U <1>BOLD</1> U <2/>", fmt.setContent(tf).toString());
	}

	@Test
	public void testMovedCodes () {
		TextFragment oriFrag = new TextFragment("s1 ");
		oriFrag.append(TagType.PLACEHOLDER, "c1", "[c1]");
		oriFrag.append(" s2 ");
		oriFrag.append(TagType.OPENING, "c2", "[c2>]");
		oriFrag.append(" s3 ");
		oriFrag.append(TagType.CLOSING, "c2", "[<c2]");
		TextFragment trgFrag = GenericContent.fromLetterCodedToFragment("<g2>t3</g2> t1 <x1/> t2", null, false, true);
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(oriFrag, trgFrag, true, false, null, null);
		assertEquals("[c2>]t3[<c2] t1 [c1] t2", fmt.setContent(trgFrag).toString(true));
	}
	
	@Test
	public void testCodesWithSameId () {
		TextFragment oriFrag = new TextFragment("s1 ");
		oriFrag.append(TagType.PLACEHOLDER, "c1", "[c1]");
		oriFrag.append(" s2 ");
		oriFrag.append(TagType.PLACEHOLDER, "c1", "[c1]");
		oriFrag.append(" s3 ");		
		TextFragment trgFrag = GenericContent.fromLetterCodedToFragment("<x1/>t3<x1/> t1 t2", null, false, true);
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(oriFrag, trgFrag, true, false, null, null);
		assertEquals("[c1]t3[c1] t1 t2", fmt.setContent(trgFrag).toString(true));
	} 
	
	@Test
	public void testAddMissingCodes() {
		TextFragment oriFrag;
		TextFragment trgFrag;
		
		oriFrag = GenericContent.fromLetterCodedToFragment("src<x1/>", null, false, true);
		trgFrag = GenericContent.fromLetterCodedToFragment("trg", null, false, true);
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(oriFrag, trgFrag, true, true, null, null);
		assertEquals("trg<1/>", fmt.setContent(trgFrag).toString());
		
		oriFrag = GenericContent.fromLetterCodedToFragment("src<x1/>", null, false, true);
		trgFrag = GenericContent.fromLetterCodedToFragment("trg<x2/>", null, false, true);
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(oriFrag, trgFrag, true, true, null, null);
		assertEquals("trg<2/><1/>", fmt.setContent(trgFrag).toString());
		
		oriFrag = GenericContent.fromLetterCodedToFragment("src<x1/><x2/>", null, false, true);
		trgFrag = GenericContent.fromLetterCodedToFragment("trg<x2/>", null, false, true);
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(oriFrag, trgFrag, true, true, null, null);
		assertEquals("trg<2/><1/>", fmt.setContent(trgFrag).toString());
		
		oriFrag = GenericContent.fromLetterCodedToFragment("<x1/>src<x2/>", null, false, true);
		trgFrag = GenericContent.fromLetterCodedToFragment("trg<x2/>", null, false, true);
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(oriFrag, trgFrag, true, true, null, null);
		assertEquals("<1/>trg<2/>", fmt.setContent(trgFrag).toString());
		
		oriFrag = GenericContent.fromLetterCodedToFragment("<x1/> src", null, false, true);
		trgFrag = GenericContent.fromLetterCodedToFragment("trg", null, false, true);
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(oriFrag, trgFrag, true, true, null, null);
		assertEquals("<1/>trg", fmt.setContent(trgFrag).toString());
		
		oriFrag = GenericContent.fromLetterCodedToFragment("<x1/> src<x2/>", null, false, true);
		trgFrag = GenericContent.fromLetterCodedToFragment("trg", null, false, true);
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(oriFrag, trgFrag, true, true, null, null);
		assertEquals("<1/>trg<2/>", fmt.setContent(trgFrag).toString());
		
		oriFrag = GenericContent.fromLetterCodedToFragment("<x1/> src<x2/>", null, false, true);
		trgFrag = GenericContent.fromLetterCodedToFragment("<x2/>trg", null, false, true);
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(oriFrag, trgFrag, true, true, null, null);
		assertEquals("<1/><2/>trg", fmt.setContent(trgFrag).toString());
	}
	
	@Test
	public void testUtils() {
		String st = "12345678";
		assertEquals("45678", Util.trimStart(st, "123"));
		assertEquals("12345", Util.trimEnd(st, "678"));
		assertEquals("12345678", Util.trimEnd(st, "9"));

		st = "     ";
		assertEquals("", Util.trimStart(st, " "));
		assertEquals("", Util.trimEnd(st, " "));

		st = "  1234   ";
		TextFragment tf = new TextFragment(st);
		TextUnitUtil.trimLeading(tf, null);
		assertEquals("1234   ", tf.toText());
		TextUnitUtil.trimTrailing(tf, null);
		assertEquals("1234", tf.toText());

		st = "     ";
		tf = new TextFragment(st);
		TextUnitUtil.trimLeading(tf, null);
		assertEquals("", tf.toText());
		TextUnitUtil.trimTrailing(tf, null);
		assertEquals("", tf.toText());

		st = "     ";
		tf = new TextFragment(st);
		TextUnitUtil.trimTrailing(tf, null);
		assertEquals("", tf.toText());

		TextFragment tc = new TextFragment("test");

		Code c = new Code(TagType.PLACEHOLDER, "code");
		tc.append(c);

		tc.append(" string");
		TextFragment tcc = new TextFragment();
		Code c2 = new Code(TagType.PLACEHOLDER, "code");
		tcc.append("   ");
		tcc.append(c2);
		tcc.append("    123456  ");

		GenericSkeleton skel = new GenericSkeleton();
		TextUnitUtil.trimLeading(tcc, skel);

		ITextUnit tu1 = new TextUnit("tu1");
		tu1.setSourceContent(tcc);
		assertEquals("    123456  ", tu1.toString());
		assertEquals("   ", skel.toString());

		// --------------------
		TextFragment tcc2 = new TextFragment("    123456  ");
		Code c3 = new Code(TagType.PLACEHOLDER, "code");
		tcc2.append(c3);

		GenericSkeleton skel2 = new GenericSkeleton();
		TextUnitUtil.trimTrailing(tcc2, skel2);

		tu1.setSourceContent(tcc2);		
		assertEquals("    123456  ", tu1.toString());
		assertEquals("", skel2.toString());

		// --------------------
		TextFragment tcc4 = new TextFragment("    123456  ");
		Code c4 = new Code(TagType.PLACEHOLDER, "code");
		tcc4.append(c4);

		char ch = TextUnitUtil.getLastChar(tcc4);
		assertEquals('6', ch);

		// --------------------
		TextFragment tcc5 = new TextFragment("    123456  ");

		TextUnitUtil.deleteLastChar(tcc5);
		assertEquals("    12345  ", tcc5.getCodedText());

		// --------------------
		TextFragment tcc6 = new TextFragment("123456_    ");

		assertTrue(TextUnitUtil.endsWith(tcc6, "_"));
		assertTrue(TextUnitUtil.endsWith(tcc6, "6_"));
		assertFalse(TextUnitUtil.endsWith(tcc6, "  "));

		TextFragment tcc7 = new TextFragment("123456<splicer>    ");
		assertTrue(TextUnitUtil.endsWith(tcc7, "<splicer>"));
		assertTrue(TextUnitUtil.endsWith(tcc7, "6<splicer>"));
		assertFalse(TextUnitUtil.endsWith(tcc7, "  "));
	}

	@Test
	public void testGetText() {

		// Using real fragment (not just coded text string, to have hasCode() working properly
		TextFragment tf = new TextFragment("ab");
		tf.append(TagType.OPENING, "type1", "z");
		tf.append("cde");
		tf.append(TagType.PLACEHOLDER, "type2", "z");
		tf.append("fgh");
		tf.append(TagType.PLACEHOLDER, "type3", "z");
		tf.append("ijklm");
		tf.append(TagType.CLOSING, "type1", "z");

		assertEquals("abcdefghijklm", TextUnitUtil.getText(tf));

		ArrayList<Integer> positions = new ArrayList<Integer>();
		assertEquals("abcdefghijklm", TextUnitUtil.getText(tf, positions));

		assertEquals(4, positions.size());

		assertEquals(2, (int) positions.get(0));
		assertEquals(7, (int) positions.get(1));
		assertEquals(12, (int) positions.get(2));
		assertEquals(19, (int) positions.get(3));

		tf = new TextFragment("ab");
		tf.append(TagType.OPENING, "type1", "z");
		tf.append("cde");
		tf.append(TagType.PLACEHOLDER, "type2", "z");
		tf.append("fgh");
		tf.append(TagType.PLACEHOLDER, "type3", "z");
		tf.append("ijklm");
		tf.append(TagType.CLOSING, "type1", "z");
		tf.append("n");

		assertEquals("abcdefghijklmn", TextUnitUtil.getText(tf));

		positions = new ArrayList<Integer>();
		assertEquals("abcdefghijklmn", TextUnitUtil.getText(tf, positions));

		assertEquals(4, positions.size());

		assertEquals(2, (int) positions.get(0));
		assertEquals(7, (int) positions.get(1));
		assertEquals(12, (int) positions.get(2));
		assertEquals(19, (int) positions.get(3));

		String st = "abcdefghijklmn";
		assertEquals(st, TextUnitUtil.getText(new TextFragment(st)));
		
		//-------------
		tf = new TextFragment("abcde");
		tf.append(TagType.PLACEHOLDER, "iso", "z");
		tf.append(TagType.PLACEHOLDER, "iso", "z");
		tf.append("fghijklm");
		tf.append(TagType.PLACEHOLDER, "iso", "z");
		tf.append(TagType.PLACEHOLDER, "iso", "z");
		tf.append("n");

		assertEquals("abcdefghijklmn", TextUnitUtil.getText(tf));

		positions = new ArrayList<Integer>();
		assertEquals("abcdefghijklmn", TextUnitUtil.getText(tf, positions));

		assertEquals(4, positions.size());

		assertEquals(5, (int) positions.get(0));
		assertEquals(7, (int) positions.get(1));
		assertEquals(17, (int) positions.get(2));
		assertEquals(19, (int) positions.get(3));

		st = "abcdefghijklmn";
		assertEquals(st, TextUnitUtil.getText(new TextFragment(st)));
	}
	
	@Test
	public void testGetCodeMarkers() {
		TextFragment tf1 = new TextFragment();
		
		Code code0 = new Code(TagType.OPENING, "x", "code0");
		code0.setId(1);
		tf1.append(code0);
		
		tf1.append("The plan of ");
		
		Code code1 = new Code(TagType.CLOSING, "x", "code1");
		code1.setId(1);
		tf1.append(code1);
		
		Code code2 = new Code(TagType.OPENING, "x", "code2");
		code2.setId(2);
		tf1.append(code2);
		
		Code code3 = new Code(TagType.PLACEHOLDER, "x", "code3");
		code3.setId(3);
		tf1.append(code3);
		
		Code code4 = new Code(TagType.CLOSING, "x", "code4");
		code4.setId(2);
		tf1.append(code4);
		
		Code code5 = new Code(TagType.OPENING, "x", "code5");
		code5.setId(4);
		tf1.append(code5);
		
		tf1.append("happiness");
		
		Code code6 = new Code(TagType.CLOSING, "x", "code6");
		code6.setId(4);
		tf1.append(code6);
		
		Code code7 = new Code(TagType.OPENING, "x", "code7");
		code7.setId(5);
		tf1.append(code7);
		
		Code code8 = new Code(TagType.PLACEHOLDER, "x", "code8");
		code8.setId(6);
		tf1.append(code8);
		
		Code code9 = new Code(TagType.CLOSING, "x", "code9");
		code9.setId(5);
		tf1.append(code9);
		
		Segment seg1 = new Segment("0", tf1);
		TextContainer tc = new TextContainer(seg1);
		assertEquals(1, tc.count());
		
		TextFragment tf = tc.getUnSegmentedContentCopy();		
		assertFalse(tc.hasBeenSegmented());
				
		assertEquals(1, tf.getCode(0).getId()); // original opening code1
		assertEquals(1, tf.getCode(1).getId()); // original closing code1
		assertEquals(2, tf.getCode(2).getId()); // original opening code2
		assertEquals(3, tf.getCode(3).getId()); // original placeholder code3
		assertEquals(2, tf.getCode(4).getId()); // original closing code2
		assertEquals(4, tf.getCode(5).getId()); // original opening code4
		assertEquals(4, tf.getCode(6).getId()); // original closing code4
		assertEquals(5, tf.getCode(7).getId()); // original opening code5
		assertEquals(6, tf.getCode(8).getId()); // original placeholder code6
		assertEquals(5, tf.getCode(9).getId()); // original closing code5
		
		assertEquals("{0}The plan of {1}{2}{3}{4}{5}happiness{6}{7}{8}{9}", TextUnitUtil.printMarkerIndexes(tf));
		assertEquals("<1>The plan of </1><2><3/></2><4>happiness</4><5><6/></5>", TextUnitUtil.printMarkers(tf));
		assertEquals("code0The plan of code1code2code3code4code5happinesscode6code7code8code9", tf.toText());
	}
	
	@Test
	public void testGetCodeMarkers2() {
		TextFragment tf1 = new TextFragment();
		
		tf1.append("The plan of ");
		
		Code code1 = new Code(TagType.CLOSING, "x", "code1");
		code1.setId(1);
		tf1.append(code1);
		
		Code code2 = new Code(TagType.OPENING, "x", "code2");
		code2.setId(2);
		tf1.append(code2);
		
		Code code3 = new Code(TagType.PLACEHOLDER, "x", "code3");
		code3.setId(3);
		tf1.append(code3);
		
		Code code4 = new Code(TagType.CLOSING, "x", "code4");
		code4.setId(2);
		tf1.append(code4);
		
		Code code5 = new Code(TagType.OPENING, "x", "code5");
		code5.setId(4);
		tf1.append(code5);
		
		tf1.append("happiness");
		
		Code code6 = new Code(TagType.CLOSING, "x", "code6");
		code6.setId(4);
		tf1.append(code6);
		
		Code code7 = new Code(TagType.OPENING, "x", "code7");
		code7.setId(5);
		tf1.append(code7);
		
		Code code8 = new Code(TagType.PLACEHOLDER, "x", "code8");
		code8.setId(6);
		tf1.append(code8);
		
		Code code9 = new Code(TagType.CLOSING, "x", "code9");
		code9.setId(5);
		tf1.append(code9);
		
		Segment seg1 = new Segment("0", tf1);
		TextContainer tc = new TextContainer(seg1);
		assertEquals(1, tc.count());
		
		TextFragment tf = tc.getUnSegmentedContentCopy();		
		assertFalse(tc.hasBeenSegmented());
				
		assertEquals(7, tf.getCode(0).getId()); // original opening code1
		assertEquals(2, tf.getCode(1).getId()); // original opening code2
		assertEquals(3, tf.getCode(2).getId()); // original placeholder code3
		assertEquals(2, tf.getCode(3).getId()); // original closing code2
		assertEquals(4, tf.getCode(4).getId()); // original opening code4
		assertEquals(4, tf.getCode(5).getId()); // original closing code4
		assertEquals(5, tf.getCode(6).getId()); // original opening code5
		assertEquals(6, tf.getCode(7).getId()); // original placeholder code6
		assertEquals(5, tf.getCode(8).getId()); // original closing code5
		
		assertEquals("The plan of {0}{1}{2}{3}{4}happiness{5}{6}{7}{8}", TextUnitUtil.printMarkerIndexes(tf));
		assertEquals("The plan of <e7/><2><3/></2><4>happiness</4><5><6/></5>", TextUnitUtil.printMarkers(tf));
		assertEquals("The plan of code1code2code3code4code5happinesscode6code7code8code9", tf.toText());
	}

	@Test
	public void testRemoveQualifiers() {

		ITextUnit tu = TextUnitUtil.buildTU("\"qualified text\"");
		TextUnitUtil.removeQualifiers(tu, "\"");
		assertEquals("qualified text", tu.getSource().toString());

		tu.setSourceContent(new TextFragment("((({[qualified text]})))"));
		assertEquals("((({[qualified text]})))", tu.getSource().toString());
		TextUnitUtil.removeQualifiers(tu, "((({", "})))");
		assertEquals("[qualified text]", tu.getSource().toString());

		GenericSkeleton tuSkel = (GenericSkeleton) tu.getSkeleton();
		assertNotNull(tuSkel);
		List<GenericSkeletonPart> parts = tuSkel.getParts();
		assertEquals(5, parts.size());

		String tuRef = TextFragment.makeRefMarker("$self$");

		assertEquals("\"", parts.get(0).toString());
		assertEquals("((({", parts.get(1).toString());
		assertEquals(tuRef, parts.get(2).toString());
		assertEquals("})))", parts.get(3).toString());
		assertEquals("\"", parts.get(4).toString());
	}
	
	@Test
	public void testSimplifyCodes() {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		
		TextUnit tu = new TextUnit("tu1");
		tu.setSourceContent(tf);
		
		assertEquals("<x1/>T1<x2/>", tu.getSource().toString());		
		TextUnitUtil.simplifyCodes(tu, null, true);		
		assertEquals("T1", tu.getSource().toString());
		
		GenericSkeleton tuSkel = (GenericSkeleton) tu.getSkeleton();
		assertNotNull(tuSkel);
		List<GenericSkeletonPart> parts = tuSkel.getParts();
		assertEquals(3, parts.size());
		
		assertEquals("<x1/>", parts.get(0).toString());		
		String tuRef = TextFragment.makeRefMarker("$self$");
		assertEquals(tuRef, parts.get(1).toString());
		assertEquals("<x2/>", parts.get(2).toString());
	}
	
	@Test
	public void testSimplifyCodes53() {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "a", "</a>");
		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x/>");
		tf.append(TagType.CLOSING, "b", "</b>");
		
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("happiness");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x/>");
		tf.append(TagType.CLOSING, "d", "</d>");
		
		TextUnit tu = new TextUnit("tu1");
		tu.setSourceContent(tf);
		
		assertEquals("<a>The plan of</a><b><x/></b><c>happiness</c><d><x/></d>", tu.getSource().toString());
		assertEquals(10, tf.getCodes().size());
		assertEquals(10, tu.getSource().getUnSegmentedContentCopy().getCodes().size());
		TextUnitUtil.simplifyCodes(tu, null, true);		
		assertEquals("<a>The plan of</a><b><x/></b><c>happiness</c><d><x/></d>", tu.getSource().toString());
		assertEquals(4, tu.getSource().getUnSegmentedContentCopy().getCodes().size());
		assertEquals("<a>", tu.getSource().getUnSegmentedContentCopy().getCode(0).getData());
		assertEquals("</a><b><x/></b>", tu.getSource().getUnSegmentedContentCopy().getCode(1).getData());
		assertEquals("<c>", tu.getSource().getUnSegmentedContentCopy().getCode(2).getData());
		assertEquals("</c><d><x/></d>", tu.getSource().getUnSegmentedContentCopy().getCode(3).getData());
		
		GenericSkeleton tuSkel = (GenericSkeleton) tu.getSkeleton();
		assertNull(tuSkel);
//		List<GenericSkeletonPart> parts = tuSkel.getParts();
//		assertEquals(1, parts.size());
//		
//		String tuRef = TextFragment.makeRefMarker("$self$");
//		assertEquals(tuRef, parts.get(0).toString());
	}
	
	@Test
	public void testSimplifyCodes53_segmented() {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "a", "</a>");
		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x/>");
		tf.append(TagType.CLOSING, "b", "</b>");
		
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("happiness");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x/>");
		tf.append(TagType.CLOSING, "d", "</d>");
		
		TextUnit tu = new TextUnit("tu1");
		tu.setSourceContent(tf);
		tu.getSource().setHasBeenSegmentedFlag(true);
		
		assertEquals("<a>The plan of</a><b><x/></b><c>happiness</c><d><x/></d>", tu.getSource().toString());
		assertEquals(10, tf.getCodes().size());
		assertEquals(10, tu.getSource().getUnSegmentedContentCopy().getCodes().size());
		TextUnitUtil.simplifyCodes(tu, null, true);		
		assertEquals("<a>The plan of</a><b><x/></b><c>happiness</c><d><x/></d>", tu.getSource().toString());
		assertEquals(4, tu.getSource().getUnSegmentedContentCopy().getCodes().size());
		assertEquals("<a>", tu.getSource().getUnSegmentedContentCopy().getCode(0).getData());
		assertEquals("</a><b><x/></b>", tu.getSource().getUnSegmentedContentCopy().getCode(1).getData());
		assertEquals("<c>", tu.getSource().getUnSegmentedContentCopy().getCode(2).getData());
		assertEquals("</c><d><x/></d>", tu.getSource().getUnSegmentedContentCopy().getCode(3).getData());
		
		GenericSkeleton tuSkel = (GenericSkeleton) tu.getSkeleton();
		assertNull(tuSkel);
	}
	
	@Test
	public void testSimplifyCodes54() {
		TextFragment tf = new TextFragment();
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "a", "</a>");
		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x/>");
		tf.append(TagType.CLOSING, "b", "</b>");
		
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("happiness");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x/>");
		tf.append(TagType.CLOSING, "d", "</d>");
		
		TextUnit tu = new TextUnit("tu1");
		tu.setSourceContent(tf);
		
		assertEquals("The plan of</a><b><x/></b><c>happiness</c><d><x/></d>", tu.getSource().toString());
		assertEquals(9, tf.getCodes().size());
		assertEquals(9, tu.getSource().getUnSegmentedContentCopy().getCodes().size());
		TextUnitUtil.simplifyCodes(tu, null, true);		
		assertEquals("The plan of</a><b><x/></b><c>happiness</c><d><x/></d>", tu.getSource().toString());
		assertEquals(2, tu.getSource().getUnSegmentedContentCopy().getCodes().size());
		assertEquals("</a><b><x/></b><c>", tu.getSource().getUnSegmentedContentCopy().getCode(0).getData());
		assertEquals("</c><d><x/></d>", tu.getSource().getUnSegmentedContentCopy().getCode(1).getData());
		
		GenericSkeleton tuSkel = (GenericSkeleton) tu.getSkeleton();
		assertNull(tuSkel);
	}
	
	@Test
	public void testSimplifyCodes55() {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x/>");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "b", "</b>");
				
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append("happiness");
		
		TextUnit tu = new TextUnit("tu1");
		tu.setSourceContent(tf);
		
		assertEquals("<a><x/></a><b>The plan of</b><c><x/></c><d>happiness", tu.getSource().toString());
		assertEquals(9, tf.getCodes().size());
		assertEquals(9, tu.getSource().getUnSegmentedContentCopy().getCodes().size());
		TextUnitUtil.simplifyCodes(tu, null, true);		
		assertEquals("<a><x/></a><b>The plan of</b><c><x/></c><d>happiness", tu.getSource().toString());
		assertEquals(2, tu.getSource().getUnSegmentedContentCopy().getCodes().size());
		assertEquals("<a><x/></a><b>", tu.getSource().getUnSegmentedContentCopy().getCode(0).getData());
		assertEquals("</b><c><x/></c><d>", tu.getSource().getUnSegmentedContentCopy().getCode(1).getData());
		
		GenericSkeleton tuSkel = (GenericSkeleton) tu.getSkeleton();
		assertNull(tuSkel);
	}
	
	@Test
	public void testSimplifyCodes56() {
		TextFragment tf = new TextFragment();
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "b", "</b>");
				
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append("happiness");
		
		TextUnit tu = new TextUnit("tu1");
		tu.setSourceContent(tf);
		
		assertEquals("The plan of</b><c><x/></c><d>happiness", tu.getSource().toString());
		assertEquals(5, tf.getCodes().size());
		assertEquals(5, tu.getSource().getUnSegmentedContentCopy().getCodes().size());
		TextUnitUtil.simplifyCodes(tu, null, true);		
		assertEquals("The plan of</b><c><x/></c><d>happiness", tu.getSource().toString());
		assertEquals(1, tu.getSource().getUnSegmentedContentCopy().getCodes().size());
		assertEquals("</b><c><x/></c><d>", tu.getSource().getUnSegmentedContentCopy().getCode(0).getData());
		
		GenericSkeleton tuSkel = (GenericSkeleton) tu.getSkeleton();
		assertNull(tuSkel);
	}
	
	@Test
	public void testSimplifyCodes_segmentedTC() {
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", new TextFragment("[seg 1]")));
		tc.append(new TextPart("[text part 1]"));
		tc.append(new Segment("s2", new TextFragment("[seg 2]")));
		tc.append(new TextPart("[text part 2]"));
		tc.append(new TextPart("[text part 3]"));
		tc.append(new Segment("s3", new TextFragment("[seg 3]")));
		tc.append(new Segment("s4", new TextFragment("[seg 4]")));
		
		TextFragment[] res = TextUnitUtil.simplifyCodes(tc, null, false);
		assertEquals("[seg 1][text part 1][seg 2][text part 2][text part 3][seg 3][seg 4]", tc.toString());
		assertNull(res);
	}
	
	@Test
	public void testSimplifyCodes_segmentedTC2() {
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", new TextFragment("[seg 1]")));
		
		TextFragment[] res = TextUnitUtil.simplifyCodes(tc, null, false);		
		assertEquals("[seg 1]", tc.toString());
		assertNull(res);
	}
	
	@Test
	public void testSimplifyCodes_segmentedTC3() {
		TextFragment tf1 = new TextFragment();		
		tf1.append(TagType.PLACEHOLDER, "x11", "<x11/>");
		tf1.append(TagType.PLACEHOLDER, "x12", "<x12/>");
		tf1.append("[seg 1]");
		tf1.append(TagType.PLACEHOLDER, "x13", "<x13/>");
		tf1.append(TagType.PLACEHOLDER, "x14", "<x14/>");
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		
		TextFragment[] res = TextUnitUtil.simplifyCodes(tc, null, true);		
		assertEquals("[seg 1]", tc.toString());
		assertNotNull(res);
		assertEquals("<x11/><x12/>", res[0].toText());
		assertEquals("<x13/><x14/>", res[1].toText());
	}
	
	@Test
	public void testSimplifyCodes_segmentedTC4() {
		TextFragment tf1 = new TextFragment();		
		tf1.append(TagType.PLACEHOLDER, "x11", "<x11/>");
		tf1.append(TagType.PLACEHOLDER, "x12", "<x12/>");
		tf1.append("   [seg 1]");
		tf1.append(TagType.PLACEHOLDER, "x13", "<x13/>");
		tf1.append(TagType.PLACEHOLDER, "x14", "<x14/>");
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		
		TextFragment[] res = TextUnitUtil.simplifyCodes(tc, null, true);		
		assertEquals("[seg 1]", tc.toString());
		assertNotNull(res);
		assertEquals("<x11/><x12/>   ", res[0].toText());
		assertEquals("<x13/><x14/>", res[1].toText());
	}
	
	@Test
	public void testSimplifyCodes_segmentedTC5() {
		TextFragment tf1 = new TextFragment();		
		tf1.append(TagType.PLACEHOLDER, "x11", "<x11/>");
		tf1.append(TagType.PLACEHOLDER, "x12", "<x12/>");
		tf1.append("abc[seg 1]");
		tf1.append(TagType.PLACEHOLDER, "x13", "<x13/>");
		tf1.append(TagType.PLACEHOLDER, "x14", "<x14/>");
		
		TextFragment tf2 = new TextFragment();		
		tf2.append(TagType.PLACEHOLDER, "x21", "<x21/>");
		tf2.append(TagType.PLACEHOLDER, "x22", "<x22/>");
		tf2.append("123[seg 2]");
		tf2.append(TagType.PLACEHOLDER, "x23", "<x23/>");
		tf2.append(TagType.PLACEHOLDER, "x24", "<x24/>");
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		tc.append(new Segment("s2", tf2));
		
		TextFragment[] res = TextUnitUtil.simplifyCodes(tc, null, true);		
		assertEquals("abc[seg 1]<x13/><x14/><x21/><x22/>123[seg 2]", tc.toString());
		assertNotNull(res);
		assertEquals("<x11/><x12/>", res[0].toText());
		assertEquals("<x23/><x24/>", res[1].toText());
		
		assertEquals(4, tc.count());
		assertTrue(tc.get(0).isSegment());
		assertTrue(!tc.get(1).isSegment());
		assertTrue(!tc.get(2).isSegment());
		assertTrue(tc.get(3).isSegment());
		
		assertEquals("abc[seg 1]", tc.get(0).toString());
		assertEquals("<x13/><x14/>", tc.get(1).toString());
		assertEquals("<x21/><x22/>", tc.get(2).toString());
		assertEquals("123[seg 2]", tc.get(3).toString());
		
		ISegments segs = tc.getSegments();
		assertEquals(2, segs.count());
	}
	
	@Test
	public void testSimplifyCodes_segmentedTC5a() {
		TextFragment tf1 = new TextFragment();		
		tf1.append(TagType.PLACEHOLDER, "x11", "<x11/>");
		tf1.append(TagType.PLACEHOLDER, "x12", "<x12/>");
		tf1.append("   [seg 1]");
		tf1.append(TagType.PLACEHOLDER, "x13", "<x13/>");
		tf1.append(TagType.PLACEHOLDER, "x14", "<x14/>");
		
		TextFragment tf2 = new TextFragment();		
		tf2.append(TagType.PLACEHOLDER, "x21", "<x21/>");
		tf2.append(TagType.PLACEHOLDER, "x22", "<x22/>");
		tf2.append("   [seg 2]");
		tf2.append(TagType.PLACEHOLDER, "x23", "<x23/>");
		tf2.append(TagType.PLACEHOLDER, "x24", "<x24/>");
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		tc.append(new Segment("s2", tf2));
		
		TextFragment[] res = TextUnitUtil.simplifyCodes(tc, null, true);		
		assertEquals("[seg 1]<x13/><x14/><x21/><x22/>   [seg 2]", tc.toString());
		assertNotNull(res);
		assertEquals("<x11/><x12/>   ", res[0].toText());
		assertEquals("<x23/><x24/>", res[1].toText());
		
		assertEquals(5, tc.count());
		assertTrue(tc.get(0).isSegment());
		assertTrue(!tc.get(1).isSegment());
		assertTrue(!tc.get(2).isSegment());
		assertTrue(!tc.get(3).isSegment());
		assertTrue(tc.get(4).isSegment());
		
		assertEquals("[seg 1]", tc.get(0).toString());
		assertEquals("<x13/><x14/>", tc.get(1).toString());
		assertEquals("<x21/><x22/>", tc.get(2).toString());
		assertEquals("   ", tc.get(3).toString());
		assertEquals("[seg 2]", tc.get(4).toString());
		
		ISegments segs = tc.getSegments();
		assertEquals(2, segs.count());
	}
	
	@Test
	public void testSimplifyCodes_segmentedTC6() {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "a", "</a>");
		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x4/>");
		tf.append(TagType.CLOSING, "b", "</b>");
		
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("happiness");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x5/>");
		tf.append(TagType.CLOSING, "d", "</d>");
		tf.append(TagType.PLACEHOLDER, "x6", "<x6/>");
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf));
		
		TextFragment[] res = TextUnitUtil.simplifyCodes(tc, null, true);		
		assertEquals("<a><x3/>The plan of</a><b><x4/></b><c>happiness</c><d><x5/></d>", tc.toString());
		assertNotNull(res);
		assertEquals("<x1/><x2/>", res[0].toText());
		assertEquals("<x6/>", res[1].toText());
		
		assertEquals(1, tc.count());
		assertTrue(tc.get(0).isSegment());
		
		assertEquals("<a><x3/>The plan of</a><b><x4/></b><c>happiness</c><d><x5/></d>", tc.get(0).toString());
		
		ISegments segs = tc.getSegments();
		assertEquals(1, segs.count());
	}
	
	@Test
	public void testSimplifyCodes_segmentedTC7() {
		TextContainer tc = new TextContainer();		
		TextFragment tf;
		Code code;
		
		tf = new TextFragment();
		code = new Code(TagType.OPENING, "x", 
				"<w:r><w:rPr><w:sz w:val=\"24\"/><w:szCs w:val=\"24\"/></w:rPr>" +
				"<w:t xml:space=\"preserve\">");
		code.setId(1);
		tf.append(code);
		tf.append("KELLER, J.,");
		tc.append(new Segment("0", tf));
		
		tf = new TextFragment();
		tf.append(" TVRDÝ, L.");
		tc.append(new Segment("1", tf));
		
		tf = new TextFragment();
		code = new Code(TagType.CLOSING, "x", 
				" </w:t></w:r>");
		code.setId(1);
		tf.append(code);
		code = new Code(TagType.OPENING, "x", 
				"<w:r><w:rPr><w:i/><w:sz w:val=\"24\"/><w:szCs w:val=\"24\"/>" +
				"</w:rPr><w:t>");
		code.setId(2);
		tf.append(code);
		tf.append("Vzdělanostní společnost?");
		tc.append(new Segment("2", tf));
		
		tf = new TextFragment();
		tf.append(" Chrám, výtah a pojišťovna");
		code = new Code(TagType.CLOSING, "x", 
				"</w:t></w:r>");
		code.setId(2);
		tf.append(code);
		code = new Code(TagType.OPENING, "x", 
				"<w:r><w:rPr><w:sz w:val=\"24\"/><w:szCs w:val=\"24\"/></w:rPr><w:t>.");
		code.setId(3);
		tf.append(code);
		tc.append(new Segment("3", tf));
		
		tf = new TextFragment();
		tf.append(" Praha: Sociologické nakladatelství, 2008.");
		tc.append(new Segment("4", tf));
		
		tf = new TextFragment();
		tf.append(" 183 s. ISBN 978-86429-78-6.");
		code = new Code(TagType.CLOSING, "x", 
				"</w:t></w:r>");
		code.setId(3);
		tf.append(code);		
		tc.append(new Segment("5", tf));
		
		assertEquals("<w:r><w:rPr><w:sz w:val=\"24\"/><w:szCs w:val=\"24\"/>" +
				"</w:rPr><w:t xml:space=\"preserve\">KELLER, J., TVRDÝ, L. " +
				"</w:t></w:r><w:r><w:rPr><w:i/><w:sz w:val=\"24\"/><w:szCs " +
				"w:val=\"24\"/></w:rPr><w:t>Vzdělanostní společnost? Chrám, výtah " +
				"a pojišťovna</w:t></w:r><w:r><w:rPr><w:sz w:val=\"24\"/><w:szCs " +
				"w:val=\"24\"/></w:rPr><w:t>. Praha: Sociologické nakladatelství, " +
				"2008. 183 s. ISBN 978-86429-78-6.</w:t></w:r>", 
				tc.toString());
		
		assertEquals(6, tc.getSegments().count());
		assertEquals("0", tc.getSegments().get(0).getId());
		assertEquals("1", tc.getSegments().get(1).getId());
		assertEquals("2", tc.getSegments().get(2).getId());
		assertEquals("3", tc.getSegments().get(3).getId());
		assertEquals("4", tc.getSegments().get(4).getId());
		assertEquals("5", tc.getSegments().get(5).getId());
		
		tf = tc.getSegments().get(0).getContent();
		assertEquals(1, tf.getCodes().size());		
		code = tf.getCode(0);
		assertEquals(1, code.getId());
		assertEquals(TagType.OPENING, code.getTagType());
		
		tf = tc.getSegments().get(1).getContent();
		assertEquals(0, tf.getCodes().size());
		
		tf = tc.getSegments().get(2).getContent();
		assertEquals(2, tf.getCodes().size());
		
		code = tf.getCode(0);
		assertEquals(1, code.getId());
		assertEquals(TagType.CLOSING, code.getTagType());		
		
		code = tf.getCode(1);
		assertEquals(2, code.getId());
		assertEquals(TagType.OPENING, code.getTagType());
		
		tf = tc.getSegments().get(3).getContent();
		assertEquals(2, tf.getCodes().size());
		
		code = tf.getCode(0);
		assertEquals(2, code.getId());
		assertEquals(TagType.CLOSING, code.getTagType());		
		
		code = tf.getCode(1);
		assertEquals(3, code.getId());
		assertEquals(TagType.OPENING, code.getTagType());
		
		tf = tc.getSegments().get(4).getContent();
		assertEquals(0, tf.getCodes().size());
		
		tf = tc.getSegments().get(5).getContent();
		assertEquals(1, tf.getCodes().size());
		
		code = tf.getCode(0);
		assertEquals(3, code.getId());
		assertEquals(TagType.CLOSING, code.getTagType());		
		
		TextFragment[] res = TextUnitUtil.simplifyCodes(tc, null, false);
		assertEquals("<w:r><w:rPr><w:sz w:val=\"24\"/><w:szCs w:val=\"24\"/>" +
				"</w:rPr><w:t xml:space=\"preserve\">KELLER, J., TVRDÝ, L. " +
				"</w:t></w:r><w:r><w:rPr><w:i/><w:sz w:val=\"24\"/><w:szCs " +
				"w:val=\"24\"/></w:rPr><w:t>Vzdělanostní společnost? Chrám, výtah " +
				"a pojišťovna</w:t></w:r><w:r><w:rPr><w:sz w:val=\"24\"/><w:szCs " +
				"w:val=\"24\"/></w:rPr><w:t>. Praha: Sociologické nakladatelství, " +
				"2008. 183 s. ISBN 978-86429-78-6.</w:t></w:r>", 
				tc.toString());
		assertNull(res);
	}
	
	@Test
	public void testSegmentId() {
		TextFragment tf = new TextFragment();
		tf.append(new Code(TagType.OPENING, "x", "code1"));
		tf.append("seg");
		tf.append(new Code(TagType.CLOSING, "x", "code2"));
		
		Segment seg = new Segment("0", tf);
		TextContainer tc = new TextContainer(seg);
		assertEquals(1, tc.count());
		assertEquals("0", tc.getSegments().get(0).getId());
		
		TextUnitUtil.simplifyCodes(tc, null, false);
		
		assertEquals(3, tc.count());
		assertEquals("0", tc.getSegments().get(0).getId());
	}
	
	@Test
	public void testSegmentId2() {
		TextFragment tf1 = new TextFragment();
		tf1.append(new Code(TagType.OPENING, "x1", "code1"));
		tf1.append("seg1");
		tf1.append(new Code(TagType.CLOSING, "x1", "code2"));
		
		TextFragment tf2 = new TextFragment();
		tf2.append(new Code(TagType.OPENING, "x2", "code3"));
		tf2.append("seg2");
		tf2.append(new Code(TagType.CLOSING, "x2", "code4"));
		
		Segment seg1 = new Segment("1", tf1);
		Segment seg2 = new Segment("5", tf2);
		TextContainer tc = new TextContainer(seg1);
		tc.append(seg2);
		assertEquals(2, tc.count());
		
		TextUnitUtil.simplifyCodes(tc, null, false);
		
		assertEquals(6, tc.count());
		assertEquals("1", tc.getSegments().get(0).getId());		
		assertEquals("seg1", tc.getSegments().get(0).getContent().toString());
		assertEquals("5", tc.getSegments().get(1).getId());
		assertEquals("seg2", tc.getSegments().get(1).getContent().toString());
	}
	
	@Test
	public void testSegmentId3() {
		TextFragment tf1 = new TextFragment();
		tf1.append(new Code(TagType.OPENING, "x1", "code1"));
		tf1.append("seg1");
		tf1.append(new Code(TagType.CLOSING, "x1", "code2"));
		
		TextFragment tf2 = new TextFragment();
		tf2.append(new Code(TagType.OPENING, "x2", "code3"));
		tf2.append("seg2");
		tf2.append(new Code(TagType.CLOSING, "x2", "code4"));
		
		Segment seg1 = new Segment("1", tf1);
		Segment seg2 = new Segment("5", tf2);
		TextContainer tc = new TextContainer(seg1);
		tc.append(seg2);
		assertEquals(2, tc.count());
		
		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("[#$1@%$seg_start$]code1seg1code2[#$1@%$seg_end$][#$5@%$seg_start$]code3seg2code4[#$5@%$seg_end$]", tf.toText());		
		tc.clear();
		assertEquals("(2: seg_start 1) (10: seg_end 1) (14: seg_start 5) (22: seg_end 5)", 
				TextUnitUtil.restoreSegmentation(tc, tf));
	}
	
	private static final String SEG_START = "$seg_start$";
	private static final String SEG_END = "$seg_end$";
	
	@Test
	public void testSegmentId4() {
		TextFragment tf1 = new TextFragment();
		Code code1 = new Code(TagType.OPENING, "x", "code1");
		code1.setReferenceFlag(true);
		tf1.append(code1);
		tf1.append("seg1");
		Code code2 = new Code(TagType.CLOSING, "x", "code2");
		tf1.append(code2);
		
		TextFragment tf2 = new TextFragment();
		tf2.append(new Code(TagType.OPENING, "x", "code3"));
		tf2.append("seg2");
		tf2.append(new Code(TagType.CLOSING, "x", "code4"));
		
		Segment seg1 = new Segment("1", tf1);
		Segment seg2 = new Segment("5", tf2);
		TextContainer tc = new TextContainer(seg1);
		tc.append(seg2);
		assertEquals(2, tc.count());
		
		TextFragment tf = tc.getUnSegmentedContentCopy();
		assertEquals("code1seg1code2code3seg2code4", tf.toText());
		assertTrue(tc.hasBeenSegmented());
				
		assertEquals(1, tf.getCode(0).getId()); // original opening code1
		assertEquals(1, tf.getCode(1).getId()); // original closing code2
		assertEquals(1, tf.getCode(2).getId()); // original opening code3
		assertEquals(1, tf.getCode(3).getId()); // original closing code4
		
		tf = new TextFragment();
		
		tf.append(new Code(TagType.OPENING, "seg", TextFragment.makeRefMarker(seg1.getId(), SEG_START)));
		tf.append(seg1.getContent());
		tf.append(new Code(TagType.CLOSING, "seg", TextFragment.makeRefMarker(seg1.getId(), SEG_END)));
		
		assertEquals(4, tf.getCodes().size()); // 2 original + 2 seg markers
		
		assertEquals(1, tf.getCode(0).getId()); // seg open marker
		assertEquals(2, tf.getCode(1).getId()); // original opening code1
		assertEquals(2, tf.getCode(2).getId()); // original closing code2
		assertEquals(1, tf.getCode(3).getId()); // seg close marker
		
		tf.append(new Code(TagType.OPENING, "seg", TextFragment.makeRefMarker(seg2.getId(), SEG_START)));
		tf.append(seg2.getContent());
		tf.append(new Code(TagType.CLOSING, "seg", TextFragment.makeRefMarker(seg2.getId(), SEG_END)));
		
		assertEquals(8, tf.getCodes().size()); // 4 original + 4 seg markers
		
		assertEquals(1, tf.getCode(0).getId()); // seg open marker
		assertEquals(2, tf.getCode(1).getId()); // original opening code1
		assertEquals(2, tf.getCode(2).getId()); // original closing code2
		assertEquals(1, tf.getCode(3).getId()); // seg close marker
		assertEquals(3, tf.getCode(4).getId()); // seg open marker
		assertEquals(4, tf.getCode(5).getId()); // original opening code3
		assertEquals(4, tf.getCode(6).getId()); // original closing code4
		assertEquals(3, tf.getCode(7).getId()); // seg close marker
		
		tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("[#$1@%$seg_start$]code1seg1code2[#$1@%$seg_end$][#$5@%$seg_start$]code3seg2code4[#$5@%$seg_end$]", tf.toText());		
		tc.clear();
		assertEquals("(2: seg_start 1) (10: seg_end 1) (14: seg_start 5) (22: seg_end 5)", 
				TextUnitUtil.restoreSegmentation(tc, tf));
	}
	
	@Test
	public void testSegmentId5() {
		TextFragment tf1 = new TextFragment();
		
		Code code0 = new Code(TagType.OPENING, "x", "code0");
		code0.setId(1);
		tf1.append(code0);
		
		tf1.append("The plan of ");
		
		Code code1 = new Code(TagType.CLOSING, "x", "code1");
		code1.setId(1);
		tf1.append(code1);
		
		Code code2 = new Code(TagType.OPENING, "x", "code2");
		code2.setId(2);
		tf1.append(code2);
		
		Code code3 = new Code(TagType.PLACEHOLDER, "x", "code3");
		code3.setId(3);
		tf1.append(code3);
		
		Code code4 = new Code(TagType.CLOSING, "x", "code4");
		code4.setId(2);
		tf1.append(code4);
		
		Code code5 = new Code(TagType.OPENING, "x", "code5");
		code5.setId(4);
		tf1.append(code5);
		
		tf1.append("happiness");
		
		Code code6 = new Code(TagType.CLOSING, "x", "code6");
		code6.setId(4);
		tf1.append(code6);
		
		Code code7 = new Code(TagType.OPENING, "x", "code7");
		code7.setId(5);
		tf1.append(code7);
		
		Code code8 = new Code(TagType.PLACEHOLDER, "x", "code8");
		code8.setId(6);
		tf1.append(code8);
		
		Code code9 = new Code(TagType.CLOSING, "x", "code9");
		code9.setId(5);
		tf1.append(code9);
		
		Segment seg1 = new Segment("0", tf1);
		TextContainer tc = new TextContainer(seg1);
		assertEquals(1, tc.count());
		
		TextFragment tf = tc.getUnSegmentedContentCopy();
		assertEquals("<1>The plan of </1><2><3/></2><4>happiness</4><5><6/></5>", TextUnitUtil.printMarkers(tf));
		assertFalse(tc.hasBeenSegmented());
				
		assertEquals(1, tf.getCode(0).getId()); // original opening code1
		assertEquals(1, tf.getCode(1).getId()); // original closing code1
		assertEquals(2, tf.getCode(2).getId()); // original opening code2
		assertEquals(3, tf.getCode(3).getId()); // original placeholder code3
		assertEquals(2, tf.getCode(4).getId()); // original closing code2
		assertEquals(4, tf.getCode(5).getId()); // original opening code4
		assertEquals(4, tf.getCode(6).getId()); // original closing code4
		assertEquals(5, tf.getCode(7).getId()); // original opening code5
		assertEquals(6, tf.getCode(8).getId()); // original placeholder code6
		assertEquals(5, tf.getCode(9).getId()); // original closing code5
		
		tf = new TextFragment();
				
		tf.append(new Code(TagType.OPENING, "seg", TextFragment.makeRefMarker(seg1.getId(), SEG_START)));
		tf.append(seg1.getContent());
		tf.append(new Code(TagType.CLOSING, "seg", TextFragment.makeRefMarker(seg1.getId(), SEG_END)));
		assertEquals("<1><2>The plan of </2><3><4/></3><5>happiness</5><6><7/></6></1>", TextUnitUtil.printMarkers(tf));
		
		assertEquals(12, tf.getCodes().size()); // 10 original + 2 seg markers
	
		
		assertEquals(1, tf.getCode(0).getId()); // seg open marker
		assertEquals(2, tf.getCode(1).getId()); // original opening code1
		assertEquals(2, tf.getCode(2).getId()); // original closing code1
		assertEquals(3, tf.getCode(3).getId()); // original opening code2
		assertEquals(4, tf.getCode(4).getId()); // original placeholder code3
		assertEquals(3, tf.getCode(5).getId()); // original closing code2
		assertEquals(5, tf.getCode(6).getId()); // original opening code4
		assertEquals(5, tf.getCode(7).getId()); // original closing code4
		assertEquals(6, tf.getCode(8).getId()); // original opening code5
		assertEquals(7, tf.getCode(9).getId()); // original placeholder code6
		assertEquals(6, tf.getCode(10).getId()); // original closing code5
		assertEquals(1, tf.getCode(11).getId()); // seg close marker
				
		tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("[#$0@%$seg_start$]code0The plan of code1code2code3code4code5happinesscode6code7code8code9[#$0@%$seg_end$]", tf.toText());		
		tc.clear();
		assertEquals("(2: seg_start 0) (43: seg_end 0)", 
				TextUnitUtil.restoreSegmentation(tc, tf));
	}
	
	@Test
	public void testSegmentId6() {
		TextFragment tf1 = new TextFragment();
		
		Code code0 = new Code(TagType.OPENING, "x", "code0");
		code0.setId(1);
		tf1.append(code0);
		
		tf1.append("The plan of ");
		
		Code code1 = new Code(TagType.CLOSING, "x", "code1");
		code1.setId(1);
		tf1.append(code1);
		
		Code code2 = new Code(TagType.OPENING, "x", "code2");
		code2.setId(2);
		tf1.append(code2);
		
		Code code3 = new Code(TagType.PLACEHOLDER, "x", "code3");
		code3.setId(3);
		tf1.append(code3);
		
		Code code4 = new Code(TagType.CLOSING, "x", "code4");
		code4.setId(2);
		tf1.append(code4);
		
		Code code5 = new Code(TagType.OPENING, "x", "code5");
		code5.setId(4);
		tf1.append(code5);
		
		tf1.append("happiness");
		
		Code code6 = new Code(TagType.CLOSING, "x", "code6");
		code6.setId(4);
		tf1.append(code6);
		
		Code code7 = new Code(TagType.OPENING, "x", "code7");
		code7.setId(5);
		tf1.append(code7);
		
		Code code8 = new Code(TagType.PLACEHOLDER, "x", "code8");
		code8.setId(6);
		tf1.append(code8);
		
		Code code9 = new Code(TagType.CLOSING, "x", "code9");
		code9.setId(5);
		tf1.append(code9);
		
		Segment seg1 = new Segment("0", tf1);
		TextContainer tc = new TextContainer(seg1);
		assertEquals(1, tc.count());
		
		TextFragment tf = tc.getUnSegmentedContentCopy();
		assertEquals("<1>The plan of </1><2><3/></2><4>happiness</4><5><6/></5>", TextUnitUtil.printMarkers(tf));
		assertFalse(tc.hasBeenSegmented());
				
		assertEquals(1, tf.getCode(0).getId()); // original opening code1
		assertEquals(1, tf.getCode(1).getId()); // original closing code1
		assertEquals(2, tf.getCode(2).getId()); // original opening code2
		assertEquals(3, tf.getCode(3).getId()); // original placeholder code3
		assertEquals(2, tf.getCode(4).getId()); // original closing code2
		assertEquals(4, tf.getCode(5).getId()); // original opening code4
		assertEquals(4, tf.getCode(6).getId()); // original closing code4
		assertEquals(5, tf.getCode(7).getId()); // original opening code5
		assertEquals(6, tf.getCode(8).getId()); // original placeholder code6
		assertEquals(5, tf.getCode(9).getId()); // original closing code5
		
		tf = new TextFragment(seg1.getContent());		
		int markerId = tf.getLastCodeId() + 1;
		
		Code scode = new Code(TagType.OPENING, "seg", TextFragment.makeRefMarker(seg1.getId(), SEG_START));
		scode.setId(markerId);
		TextFragment stf = new TextFragment();
		stf.append(scode);		
		tf.insert(0, stf, true);
		
		Code ecode = new Code(TagType.CLOSING, "seg", TextFragment.makeRefMarker(seg1.getId(), SEG_END));
		ecode.setId(markerId);
		TextFragment etf = new TextFragment();
		etf.append(ecode);
		tf.insert(-1, etf, true);
		
		assertEquals("{10}{0}The plan of {1}{2}{3}{4}{5}happiness{6}{7}{8}{9}{11}", TextUnitUtil.printMarkerIndexes(tf));
		assertEquals("<7><1>The plan of </1><2><3/></2><4>happiness</4><5><6/></5></7>", TextUnitUtil.printMarkers(tf));
		
		assertEquals(12, tf.getCodes().size()); // 10 original + 2 seg markers
	
		assertEquals(1, tf.getCode(0).getId());  // original opening code1
		assertEquals(1, tf.getCode(1).getId());  // original closing code1
		assertEquals(2, tf.getCode(2).getId());  // original opening code2
		assertEquals(3, tf.getCode(3).getId());  // original placeholder code3
		assertEquals(2, tf.getCode(4).getId());  // original closing code2
		assertEquals(4, tf.getCode(5).getId());  // original opening code4
		assertEquals(4, tf.getCode(6).getId());  // original closing code4
		assertEquals(5, tf.getCode(7).getId());  // original opening code5
		assertEquals(6, tf.getCode(8).getId());  // original placeholder code6
		assertEquals(5, tf.getCode(9).getId());  // original closing code5
		assertEquals(7, tf.getCode(10).getId()); // seg open marker
		assertEquals(7, tf.getCode(11).getId()); // seg close marker
				
		tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("[#$0@%$seg_start$]code0The plan of code1code2code3code4code5happinesscode6code7code8code9[#$0@%$seg_end$]", tf.toText());		
		tc.clear();
		assertEquals("(2: seg_start 0) (43: seg_end 0)", 
				TextUnitUtil.restoreSegmentation(tc, tf));
	}
	
	@Test
	public void testSegmentId7() {
		TextFragment tf1 = new TextFragment();
		
		Code code0 = new Code(TagType.OPENING, "x", "code0");
		code0.setId(1);
		code0.setReferenceFlag(true);
		tf1.append(code0);
		
		tf1.append("The plan of ");
		
		Code code1 = new Code(TagType.CLOSING, "x", "code1");
		code1.setId(1);
		tf1.append(code1);
		
		Code code2 = new Code(TagType.OPENING, "x", "code2");
		code2.setId(2);
		tf1.append(code2);
		
		Code code3 = new Code(TagType.PLACEHOLDER, "x", "code3");
		code3.setId(3);
		tf1.append(code3);
		
		Code code4 = new Code(TagType.CLOSING, "x", "code4");
		code4.setId(2);
		tf1.append(code4);
		
		Code code5 = new Code(TagType.OPENING, "x", "code5");
		code5.setId(4);
		code5.setReferenceFlag(true);
		tf1.append(code5);
		
		tf1.append("happiness");
		
		Code code6 = new Code(TagType.CLOSING, "x", "code6");
		code6.setId(4);
		tf1.append(code6);
		
		Code code7 = new Code(TagType.OPENING, "x", "code7");
		code7.setId(5);
		tf1.append(code7);
		
		Code code8 = new Code(TagType.PLACEHOLDER, "x", "code8");
		code8.setId(6);
		tf1.append(code8);
		
		Code code9 = new Code(TagType.CLOSING, "x", "code9");
		code9.setId(5);
		tf1.append(code9);
		
		Segment seg1 = new Segment("0", tf1);
		TextContainer tc = new TextContainer(seg1);
		assertEquals(1, tc.count());
		
		TextFragment tf = tc.getUnSegmentedContentCopy();
		assertEquals("<1>The plan of </1><2><3/></2><4>happiness</4><5><6/></5>", TextUnitUtil.printMarkers(tf));
		assertFalse(tc.hasBeenSegmented());
				
		assertEquals(1, tf.getCode(0).getId()); // original opening code1
		assertEquals(1, tf.getCode(1).getId()); // original closing code1
		assertEquals(2, tf.getCode(2).getId()); // original opening code2
		assertEquals(3, tf.getCode(3).getId()); // original placeholder code3
		assertEquals(2, tf.getCode(4).getId()); // original closing code2
		assertEquals(4, tf.getCode(5).getId()); // original opening code4
		assertEquals(4, tf.getCode(6).getId()); // original closing code4
		assertEquals(5, tf.getCode(7).getId()); // original opening code5
		assertEquals(6, tf.getCode(8).getId()); // original placeholder code6
		assertEquals(5, tf.getCode(9).getId()); // original closing code5
		
		tf = new TextFragment();
		int markerId = 1;	

		Code code = new Code(TagType.OPENING, "seg", TextFragment.makeRefMarker(seg1.getId(), SEG_START));
		code.setId(markerId);		
		tf.append(code); // No re-balancing is happening as code Id <> -1
		
		TextFragment tf2 = seg1.getContent().clone();
		tf2.renumberCodes(markerId + 1);
		tf.insert(-1, tf2, true);
		
		code = new Code(TagType.CLOSING, "seg", TextFragment.makeRefMarker(seg1.getId(), SEG_START));
		code.setId(markerId);
		tf.append(code); // No re-balancing is happening as code Id <> -1
		
        assertEquals("<1><2>The plan of </2><3><4/></3><5>happiness</5><6><7/></6></1>", TextUnitUtil.printMarkers(tf));
		
		assertEquals(12, tf.getCodes().size()); // 10 original + 2 seg markers
	
		assertEquals(1, tf.getCode(0).getId()); // seg open marker
		assertEquals(2, tf.getCode(1).getId()); // original opening code1
		assertEquals(2, tf.getCode(2).getId()); // original closing code1
		assertEquals(3, tf.getCode(3).getId()); // original opening code2
		assertEquals(4, tf.getCode(4).getId()); // original placeholder code3
		assertEquals(3, tf.getCode(5).getId()); // original closing code2
		assertEquals(5, tf.getCode(6).getId()); // original opening code4
		assertEquals(5, tf.getCode(7).getId()); // original closing code4
		assertEquals(6, tf.getCode(8).getId()); // original opening code5
		assertEquals(7, tf.getCode(9).getId()); // original placeholder code6
		assertEquals(6, tf.getCode(10).getId()); // original closing code5
		assertEquals(1, tf.getCode(11).getId()); // seg close marker
				
		tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("[#$0@%$seg_start$]code0The plan of code1code2code3code4code5happinesscode6code7code8code9[#$0@%$seg_end$]", tf.toText());		
		tc.clear();
		assertEquals("(2: seg_start 0) (43: seg_end 0)", 
				TextUnitUtil.restoreSegmentation(tc, tf));
		assertEquals(1, tc.count());
	}
	
	@Test
	public void testSegmentId8() {
		TextFragment tf1 = new TextFragment();
		
		Code code0 = new Code(TagType.OPENING, "x", "code0");
		code0.setId(1);
		tf1.append(code0);
		
		tf1.append("The plan of ");
		
		Code code1 = new Code(TagType.CLOSING, "x", "code1");
		code1.setId(1);
		tf1.append(code1);
		
		Code code2 = new Code(TagType.OPENING, "x", "code2");
		code2.setId(2);
		tf1.append(code2);
		
		Code code3 = new Code(TagType.PLACEHOLDER, "x", "code3");
		code3.setId(3);
		tf1.append(code3);
		
		Code code4 = new Code(TagType.CLOSING, "x", "code4");
		code4.setId(2);
		tf1.append(code4);
		
		Code code5 = new Code(TagType.OPENING, "x", "code5");
		code5.setId(4);
		tf1.append(code5);
		
		tf1.append("happiness");
		
		Code code6 = new Code(TagType.CLOSING, "x", "code6");
		code6.setId(4);
		tf1.append(code6);
		
		Code code7 = new Code(TagType.OPENING, "x", "code7");
		code7.setId(5);
		tf1.append(code7);
		
		Code code8 = new Code(TagType.PLACEHOLDER, "x", "code8");
		code8.setId(6);
		tf1.append(code8);
		
		Code code9 = new Code(TagType.CLOSING, "x", "code9");
		code9.setId(5);
		tf1.append(code9);
		
		Segment seg1 = new Segment("0", tf1);
		TextContainer tc = new TextContainer(seg1);
		assertEquals(1, tc.count());
		
		assertEquals("<1>The plan of </1><2><3/></2><4>happiness</4><5><6/></5>", 
				fmt.setContent(tc.getUnSegmentedContentCopy()).toString());
		TextFragment[] res = TextUnitUtil.simplifyCodes(tc, null, true);
//		assertEquals("<1>The plan of </1><2>happiness</2>", 
//				fmt.setContent(tc.getUnSegmentedContentCopy()).toString());
		
		assertEquals("<2>The plan of </2><5>happiness</5>", 
				fmt.setContent(tc.getUnSegmentedContentCopy()).toString());
		
		assertEquals(1, tc.count());
		assertNull(res);
	}
	
	@Test
	public void testStoreSegmentation () {		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", new TextFragment("[seg 1]")));
		tc.append(new TextPart("[text part 1]"));
		tc.append(new Segment("s2", new TextFragment("[seg 2]")));
		tc.append(new TextPart("[text part 2]"));
		tc.append(new TextPart("[text part 3]"));
		tc.append(new Segment("s3", new TextFragment("[seg 3]")));
		
		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("<1>[seg 1]</1><2/>[text part 1]<3/><4>[seg 2]</4><5/>[text part 2]<6/><7/>[text part 3]<8/><9>[seg 3]</9>", 
				fmt.setContent(tf).toString());
		
		List<Code> codes = tf.getCodes();
		assertEquals(12, codes.size());
		
		assertEquals("[#$s1@%$seg_start$]", codes.get(0).toString()); // <1/>
		assertEquals("[#$s1@%$seg_end$]", codes.get(1).toString()); // <2/>
		
		assertEquals("$tp_start$", codes.get(2).toString()); // <3/>
		assertEquals("$tp_end$", codes.get(3).toString()); // <4/>
		
		assertEquals("[#$s2@%$seg_start$]", codes.get(4).toString()); // <5/>
		assertEquals("[#$s2@%$seg_end$]", codes.get(5).toString()); // <6/>
		
		assertEquals("$tp_start$", codes.get(6).toString()); // <7/>
		assertEquals("$tp_end$", codes.get(7).toString()); // <8/>
		
		assertEquals("$tp_start$", codes.get(8).toString()); // <9/>
		assertEquals("$tp_end$", codes.get(9).toString()); // <10/>
		
		assertEquals("[#$s3@%$seg_start$]", codes.get(10).toString()); // <11/>
		assertEquals("[#$s3@%$seg_end$]", codes.get(11).toString()); // <12/>		
	}

	
	
	@Test
	public void testTreeSet() {
		TreeSet<Integer> set = new TreeSet<Integer>();
		set.add(5);
		set.add(1);
		set.add(5);
		set.add(3);
		set.add(9);
		
		assertEquals(4, set.size()); // 5 is repeated
		assertEquals("[1, 3, 5, 9]", set.toString());
	}
	
	@Test
	public void testTreeMap() {
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();
		map.put(5, "");
		map.put(1, "");
		map.put(5, "");
		map.put(3, "");
		map.put(9, "");
		
		assertEquals(4, map.size()); // 5 is repeated
	}
	
	@Test
	public void testHashtableSort() {
		Hashtable<String, String> h = new Hashtable<String, String>();
	    h.put("a", "b");	    
	    h.put("c", "d");
	    h.put("e", "f");
	    h.put("a", "bb");
	    List<String> v = new ArrayList<String>(h.keySet());
	    Collections.sort(v);
	}
	
	@Test
	public void testRestoreSegmentation () {		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", new TextFragment("[seg 1]")));
		tc.append(new TextPart("[text part 1]"));
		tc.append(new Segment("s2", new TextFragment("[seg 2]")));
		tc.append(new TextPart("[text part 2]"));
		tc.append(new TextPart("[text part 3]"));
		tc.append(new Segment("s3", new TextFragment("[seg 3]")));
		tc.append(new Segment("s4", new TextFragment("[seg 4]")));
		
		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("<1>[seg 1]</1><2/>[text part 1]<3/><4>[seg 2]</4><5/>[text part 2]<6/><7/>[text part 3]<8/><9>[seg 3]</9><10>[seg 4]</10>", 
				fmt.setContent(tf).toString());

		TextUnitUtil.restoreSegmentation(tc, tf);
		assertEquals("(2: seg_start s1) (9: seg_end s1) (13: tp_start) (26: tp_end) (30: seg_start s2) (37: seg_end s2) " +
				"(41: tp_start) (54: tp_end) (58: tp_start) (71: tp_end) (75: seg_start s3) (82: seg_end s3) " +
				"(86: seg_start s4) (93: seg_end s4)", TextUnitUtil.testMarkers());
		
		assertEquals("[seg 1][text part 1][seg 2][text part 2][text part 3][seg 3][seg 4]", tc.toString());
		
		
		Iterator<TextPart> it = tc.iterator();
		
		TextPart part = null; 
		
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals("[seg 1]", part.toString());
		}
				
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("[text part 1]", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals("[seg 2]", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("[text part 2]", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("[text part 3]", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals("[seg 3]", part.toString());
		}		
		
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals("[seg 4]", part.toString());
		}
	}
	
	@Test
	public void testRestoreSegmentation2 () {		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", new TextFragment("[seg 1]")));
		tc.append(new TextPart("[text part 1]"));
		tc.append(new Segment("s2", new TextFragment("[seg 2]")));
		tc.append(new TextPart("[text part 2]"));
		tc.append(new TextPart("[text part 3]"));
		tc.append(new Segment("s3", new TextFragment("[seg 3]")));
		tc.append(new TextPart("[text part 4]"));
		
		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("<1>[seg 1]</1><2/>[text part 1]<3/><4>[seg 2]</4><5/>[text part 2]<6/><7/>[text part 3]<8/><9>[seg 3]</9><10/>[text part 4]<11/>", 
				fmt.setContent(tf).toString());

		TextUnitUtil.restoreSegmentation(tc, tf);
		assertEquals("[seg 1][text part 1][seg 2][text part 2][text part 3][seg 3][text part 4]", tc.toString());
		
		Iterator<TextPart> it = tc.iterator();		
		TextPart part = null; 
		
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals("[seg 1]", part.toString());
		}
				
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("[text part 1]", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals("[seg 2]", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("[text part 2]", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("[text part 3]", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals("[seg 3]", part.toString());
		}		
		
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("[text part 4]", part.toString());
		}
	}
	
	@Test
	public void testRestoreSegmentation3 () {		
		TextFragment tf1 = new TextFragment();		
		tf1.append(TagType.PLACEHOLDER, "x11", "<x11/>");
		tf1.append(TagType.PLACEHOLDER, "x12", "<x12/>");
		tf1.append(TagType.PLACEHOLDER, "x13", "<x13/>");
		tf1.append(TagType.PLACEHOLDER, "x14", "<x14/>");
		
		TextFragment tf2 = new TextFragment();		
		tf2.append(TagType.PLACEHOLDER, "x21", "<x21/>");
		tf2.append(TagType.PLACEHOLDER, "x22", "<x22/>");
		tf2.append(TagType.PLACEHOLDER, "x23", "<x23/>");
		tf2.append(TagType.PLACEHOLDER, "x24", "<x24/>");
		
		TextFragment tf3 = new TextFragment();		
		tf3.append(TagType.PLACEHOLDER, "x31", "<x31/>");
		tf3.append(TagType.PLACEHOLDER, "x32", "<x32/>");
		tf3.append(TagType.PLACEHOLDER, "x33", "<x33/>");
		tf3.append(TagType.PLACEHOLDER, "x34", "<x34/>");
				
		assertEquals("<1/><2/><3/><4/>", fmt.setContent(tf1).toString());
		assertEquals("<1/><2/><3/><4/>", fmt.setContent(tf2).toString());
		assertEquals("<1/><2/><3/><4/>", fmt.setContent(tf3).toString());
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		tc.append(new TextPart("[text part 1]"));
		tc.append(new Segment("s2", tf2));
		tc.append(new TextPart("[text part 2]"));
		tc.append(new TextPart("[text part 3]"));
		tc.append(new Segment("s3", tf3));
		tc.append(new TextPart("[text part 4]"));
		
		//String saveTc = tc.toString();
		
		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("<1><2/><3/><4/><5/></1><6/>[text part 1]<7/><8><9/><10/><11/><12/></8><13/>[text part 2]<14/><15/>[text part 3]<16/><17><18/><19/><20/><21/></17><22/>[text part 4]<23/>",
			fmt.setContent(tf).toString());
		TextUnitUtil.simplifyCodes(tf, null, false);
//		assertEquals("<1/>[text part 1]<2/>[text part 2]<3/>[text part 3]<4/>[text part 4]<5/>", 
//				fmt.setContent(tf).toString());
		
		assertEquals("<1/>[text part 1]<8/>[text part 2]<14/>[text part 3]<17/>[text part 4]<23/>", 
				fmt.setContent(tf).toString());
		
		List<Code> codes = tf.getCodes();
		assertEquals("[#$s1@%$seg_start$]<x11/><x12/><x13/><x14/>[#$s1@%$seg_end$]$tp_start$", codes.get(0).toString()); // <1/>
		
		assertEquals("$tp_end$[#$s2@%$seg_start$]<x21/><x22/><x23/><x24/>[#$s2@%$seg_end$]$tp_start$", codes.get(1).toString()); // <2/>
		
		assertEquals("$tp_end$$tp_start$", codes.get(2).toString()); // <3/>
		
		assertEquals("$tp_end$[#$s3@%$seg_start$]<x31/><x32/><x33/><x34/>[#$s3@%$seg_end$]$tp_start$", codes.get(3).toString()); // <4/>
		
		assertEquals("$tp_end$", codes.get(4).toString()); // <5/>
				
		TextUnitUtil.restoreSegmentation(tc, tf);
		assertEquals("<x11/><x12/><x13/><x14/>[text part 1]<x21/><x22/><x23/><x24/>[text part 2][text part 3]<x31/><x32/><x33/><x34/>[text part 4]", tc.toString());
		
		Iterator<TextPart> it = tc.iterator();		
		TextPart part = null;
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals("<x11/><x12/><x13/><x14/>", part.toString());
		}
				
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("[text part 1]", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals("<x21/><x22/><x23/><x24/>", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("[text part 2]", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("[text part 3]", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals("<x31/><x32/><x33/><x34/>", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("[text part 4]", part.toString());
		}
	}
	
	@Test
	public void testRestoreSegmentation3_2 () {		
		TextFragment tf1 = new TextFragment();		
		tf1.append(TagType.PLACEHOLDER, "x11", "<x11/>");
		tf1.append(TagType.PLACEHOLDER, "x12", "<x12/>");
		tf1.append("[seg 1]");
		tf1.append(TagType.PLACEHOLDER, "x13", "<x13/>");
		tf1.append(TagType.PLACEHOLDER, "x14", "<x14/>");
		
		TextFragment tf2 = new TextFragment();
		tf2.append("[seg 2]");
		tf2.append(TagType.PLACEHOLDER, "x21", "<x21/>");
		tf2.append(TagType.PLACEHOLDER, "x22", "<x22/>");
		tf2.append(TagType.PLACEHOLDER, "x23", "<x23/>");
		tf2.append(TagType.PLACEHOLDER, "x24", "<x24/>");
		
		TextFragment tf3 = new TextFragment();		
		tf3.append(TagType.PLACEHOLDER, "x31", "<x31/>");
		tf3.append(TagType.PLACEHOLDER, "x32", "<x32/>");
		tf3.append(TagType.PLACEHOLDER, "x33", "<x33/>");
		tf3.append("[seg 3]");
		tf3.append(TagType.PLACEHOLDER, "x34", "<x34/>");
				
		assertEquals("<1/><2/>[seg 1]<3/><4/>", fmt.setContent(tf1).toString());		
		assertEquals("[seg 2]<1/><2/><3/><4/>", fmt.setContent(tf2).toString());
		assertEquals("<1/><2/><3/>[seg 3]<4/>", fmt.setContent(tf3).toString());
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		tc.append(new TextPart("[text part 1]"));
		tc.append(new Segment("s2", tf2));
		tc.append(new TextPart("[text part 2]"));
		tc.append(new TextPart("[text part 3]"));
		tc.append(new Segment("s3", tf3));
		tc.append(new TextPart("[text part 4]"));
		
		//String saveTc = tc.toString();
		
		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("<1/><2/><3/><4/><5>[seg 1]</5><6/><7/><8/><9/><10/>[text part 1]<11/><12>[seg 2]</12><13/><14/><15/><16/><17/><18/><19/>[text part 2]<20/><21/>[text part 3]<22/><23/><24/><25/><26/><27/><28>[seg 3]</28><29/><30/><31/><32/>[text part 4]<33/>",
			fmt.setContent(tf).toString());
		assertEquals("$tp_start$<x11/><x12/>$tp_end$[#$s1@%$seg_start$][seg 1][#$s1@%$seg_end$]$tp_start$<x13/><x14/>$tp_end$$tp_start$[text part 1]$tp_end$[#$s2@%$seg_start$][seg 2][#$s2@%$seg_end$]$tp_start$<x21/><x22/><x23/><x24/>$tp_end$$tp_start$[text part 2]$tp_end$$tp_start$[text part 3]$tp_end$$tp_start$<x31/><x32/><x33/>$tp_end$[#$s3@%$seg_start$][seg 3][#$s3@%$seg_end$]$tp_start$<x34/>$tp_end$$tp_start$[text part 4]$tp_end$", 
			tf.toText());
				
		TextUnitUtil.simplifyCodes(tf, null, false);
//		assertEquals("<1>[seg 1]</1>[text part 1]<2>[seg 2]</2>[text part 2]<3/>[text part 3]<4>[seg 3]</4>[text part 4]<5/>", 
//				fmt.setContent(tf).toString());				

		assertEquals("<5>[seg 1]</5>[text part 1]<12>[seg 2]</12>[text part 2]<20/>[text part 3]<28>[seg 3]</28>[text part 4]<33/>", 
				fmt.setContent(tf).toString());
		
		// Codes after simplification
		List<Code> codes = tf.getCodes();
		assertEquals(8, codes.size());
		
		assertEquals("$tp_start$<x11/><x12/>$tp_end$[#$s1@%$seg_start$]", codes.get(0).toString()); // <1/>
		
		assertEquals("[#$s1@%$seg_end$]$tp_start$<x13/><x14/>$tp_end$$tp_start$", codes.get(1).toString()); // <2/>
		
		assertEquals("$tp_end$[#$s2@%$seg_start$]", codes.get(2).toString()); // <3/>
		
		assertEquals("[#$s2@%$seg_end$]$tp_start$<x21/><x22/><x23/><x24/>$tp_end$$tp_start$", codes.get(3).toString()); // <4/>
		
		assertEquals("$tp_end$$tp_start$", codes.get(4).toString()); // <5/>
		
		assertEquals("$tp_end$$tp_start$<x31/><x32/><x33/>$tp_end$[#$s3@%$seg_start$]", codes.get(5).toString()); // <6/>
		
		assertEquals("[#$s3@%$seg_end$]$tp_start$<x34/>$tp_end$$tp_start$", codes.get(6).toString()); // <7/>
		
		assertEquals("$tp_end$", codes.get(7).toString()); // <8/>
				
		TextUnitUtil.restoreSegmentation(tc, tf);
		assertEquals("(0-10: tp_start) (0-22: tp_end) (2: seg_start s1) (9: seg_end s1) (9-27: tp_start) (9-39: tp_end) (11: tp_start) (24: tp_end) (26: seg_start s2) (33: seg_end s2) (33-27: tp_start) (33-51: tp_end) (35: tp_start) (48: tp_end) (50: tp_start) (63: tp_end) (63-18: tp_start) (63-36: tp_end) (65: seg_start s3) (72: seg_end s3) (72-27: tp_start) (72-33: tp_end) (74: tp_start) (87: tp_end)", 
				TextUnitUtil.testMarkers());
		
		assertEquals("<x11/><x12/>[seg 1]<x13/><x14/>[text part 1][seg 2]<x21/><x22/><x23/><x24/>[text part 2]" +
				"[text part 3]<x31/><x32/><x33/>[seg 3]<x34/>[text part 4]", tc.toString());
		
		assertEquals(12, tc.count());
		
		assertTrue(!tc.get(0).isSegment());
		assertTrue(tc.get(1).isSegment());
		assertTrue(!tc.get(2).isSegment());
		assertTrue(!tc.get(3).isSegment());
		assertTrue(tc.get(4).isSegment());
		assertTrue(!tc.get(5).isSegment());
		assertTrue(!tc.get(6).isSegment());
		assertTrue(!tc.get(7).isSegment());
		assertTrue(!tc.get(8).isSegment());
		assertTrue(tc.get(9).isSegment());
		assertTrue(!tc.get(10).isSegment());
		assertTrue(!tc.get(11).isSegment());
		
		assertEquals("<x11/><x12/>", tc.get(0).toString());
		assertEquals("[seg 1]", tc.get(1).toString());
		assertEquals("<x13/><x14/>", tc.get(2).toString());
		assertEquals("[text part 1]", tc.get(3).toString());
		assertEquals("[seg 2]", tc.get(4).toString());
		assertEquals("<x21/><x22/><x23/><x24/>", tc.get(5).toString());
		assertEquals("[text part 2]", tc.get(6).toString());
		assertEquals("[text part 3]", tc.get(7).toString());
		assertEquals("<x31/><x32/><x33/>", tc.get(8).toString());
		assertEquals("[seg 3]", tc.get(9).toString());
		assertEquals("<x34/>", tc.get(10).toString());
		assertEquals("[text part 4]", tc.get(11).toString());
	}
	
	@Test
	public void testRestoreSegmentation3_3 () {		
		TextFragment tf1 = new TextFragment();		
		tf1.append(TagType.PLACEHOLDER, "x11", "<x11/>");
		tf1.append(TagType.PLACEHOLDER, "x12", "<x12/>");
		tf1.append("[seg 1]");
		tf1.append(TagType.PLACEHOLDER, "x13", "<x13/>");
		tf1.append(TagType.PLACEHOLDER, "x14", "<x14/>");
		
		TextFragment tf2 = new TextFragment();
		tf2.append("[seg 2]");
		tf2.append(TagType.PLACEHOLDER, "x21", "<x21/>");
		tf2.append(TagType.PLACEHOLDER, "x22", "<x22/>");
		tf2.append(TagType.PLACEHOLDER, "x23", "<x23/>");
		tf2.append(TagType.PLACEHOLDER, "x24", "<x24/>");
		
		TextFragment tf3 = new TextFragment();		
		tf3.append(TagType.PLACEHOLDER, "x31", "<x31/>");
		tf3.append(TagType.PLACEHOLDER, "x32", "<x32/>");
		tf3.append(TagType.PLACEHOLDER, "x33", "<x33/>");
		tf3.append("[seg 3]");
		tf3.append(TagType.PLACEHOLDER, "x34", "<x34/>");
				
		assertEquals("<1/><2/>[seg 1]<3/><4/>", fmt.setContent(tf1).toString());		
		assertEquals("[seg 2]<1/><2/><3/><4/>", fmt.setContent(tf2).toString());
		assertEquals("<1/><2/><3/>[seg 3]<4/>", fmt.setContent(tf3).toString());
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		tc.append(new TextPart("[text part 1]"));
		tc.append(new Segment("s2", tf2));
		tc.append(new TextPart("[text part 2]"));
		tc.append(new TextPart("[text part 3]"));
		tc.append(new Segment("s3", tf3));
		tc.append(new TextPart("[text part 4]"));
		
		//String saveTc = tc.toString();
		
		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("<1/><2/><3/><4/><5>[seg 1]</5><6/><7/><8/><9/><10/>[text part 1]<11/><12>[seg 2]</12><13/><14/><15/><16/><17/><18/><19/>[text part 2]<20/><21/>[text part 3]<22/><23/><24/><25/><26/><27/><28>[seg 3]</28><29/><30/><31/><32/>[text part 4]<33/>",
				fmt.setContent(tf).toString());
		assertEquals("$tp_start$<x11/><x12/>$tp_end$[#$s1@%$seg_start$][seg 1][#$s1@%$seg_end$]$tp_start$<x13/><x14/>$tp_end$$tp_start$[text part 1]$tp_end$[#$s2@%$seg_start$][seg 2][#$s2@%$seg_end$]$tp_start$<x21/><x22/><x23/><x24/>$tp_end$$tp_start$[text part 2]$tp_end$$tp_start$[text part 3]$tp_end$$tp_start$<x31/><x32/><x33/>$tp_end$[#$s3@%$seg_start$][seg 3][#$s3@%$seg_end$]$tp_start$<x34/>$tp_end$$tp_start$[text part 4]$tp_end$", 
				tf.toText());
				
		TextFragment[] res = TextUnitUtil.simplifyCodes(tf, null, true);		
//		assertEquals("<1>[seg 1]</1>[text part 1]<2>[seg 2]</2>[text part 2]<3/>[text part 3]<4>[seg 3]</4>[text part 4]", 
//				fmt.setContent(tf).toString());
		
		assertEquals("<5>[seg 1]</5>[text part 1]<12>[seg 2]</12>[text part 2]<20/>[text part 3]<28>[seg 3]</28>[text part 4]", 
				fmt.setContent(tf).toString());
		
		assertEquals("$tp_start$<x11/><x12/>$tp_end$[#$s1@%$seg_start$][seg 1][#$s1@%$seg_end$]$tp_start$<x13/><x14/>$tp_end$$tp_start$[text part 1]$tp_end$[#$s2@%$seg_start$][seg 2][#$s2@%$seg_end$]$tp_start$<x21/><x22/><x23/><x24/>$tp_end$$tp_start$[text part 2]$tp_end$$tp_start$[text part 3]$tp_end$$tp_start$<x31/><x32/><x33/>$tp_end$[#$s3@%$seg_start$][seg 3][#$s3@%$seg_end$]$tp_start$<x34/>$tp_end$$tp_start$[text part 4]", 
				tf.toText());
		assertNotNull(res);
		assertNull(res[0]);
		assertEquals("$tp_end$", res[1].toText());

		// Codes after simplification
		List<Code> codes = tf.getCodes();
		assertEquals(7, codes.size());
		
		assertEquals("$tp_start$<x11/><x12/>$tp_end$[#$s1@%$seg_start$]", codes.get(0).toString()); // <1/>
				
		assertEquals("[#$s1@%$seg_end$]$tp_start$<x13/><x14/>$tp_end$$tp_start$", codes.get(1).toString()); // <2/>
		
		assertEquals("$tp_end$[#$s2@%$seg_start$]", codes.get(2).toString()); // <3/>
		
		assertEquals("[#$s2@%$seg_end$]$tp_start$<x21/><x22/><x23/><x24/>$tp_end$$tp_start$", codes.get(3).toString()); // <4/>
		
		assertEquals("$tp_end$$tp_start$", codes.get(4).toString()); // <5/>
		
		assertEquals("$tp_end$$tp_start$<x31/><x32/><x33/>$tp_end$[#$s3@%$seg_start$]", codes.get(5).toString()); // <6/>
		
		TextUnitUtil.restoreSegmentation(tc, tf);
		assertEquals("(0-10: tp_start) (0-22: tp_end) (2: seg_start s1) (9: seg_end s1) (9-27: tp_start) (9-39: tp_end) (11: tp_start) (24: tp_end) (26: seg_start s2) (33: seg_end s2) (33-27: tp_start) (33-51: tp_end) (35: tp_start) (48: tp_end) (50: tp_start) (63: tp_end) (63-18: tp_start) (63-36: tp_end) (65: seg_start s3) (72: seg_end s3) (72-27: tp_start) (72-33: tp_end) (74: tp_start)", 
				TextUnitUtil.testMarkers());
		
		assertEquals("<x11/><x12/>[seg 1]<x13/><x14/>[text part 1][seg 2]<x21/><x22/><x23/><x24/>[text part 2][text part 3]<x31/><x32/><x33/>[seg 3]<x34/>", tc.toString());
		
		TextPart part = null;
		assertEquals(11, tc.count());
		
		part = tc.get(0);
		assertFalse(part.isSegment());
		assertEquals("<x11/><x12/>", part.toString());
				
		part = tc.get(1);
		assertTrue(part.isSegment());
		assertEquals("[seg 1]", part.toString());
	
		part = tc.get(2);
		assertFalse(part.isSegment());
		assertEquals("<x13/><x14/>", part.toString());
	
		part = tc.get(3);
		assertFalse(part.isSegment());
		assertEquals("[text part 1]", part.toString());
		
		part = tc.get(4);
		assertTrue(part.isSegment());
		assertEquals("[seg 2]", part.toString());
	
		part = tc.get(5);
		assertFalse(part.isSegment());
		assertEquals("<x21/><x22/><x23/><x24/>", part.toString());
	
		part = tc.get(6);
		assertFalse(part.isSegment());
		assertEquals("[text part 2]", part.toString());
		
		part = tc.get(7);
		assertFalse(part.isSegment());
		assertEquals("[text part 3]", part.toString());
	
		part = tc.get(8);
		assertFalse(part.isSegment());
		assertEquals("<x31/><x32/><x33/>", part.toString());
		
		part = tc.get(9);
		assertTrue(part.isSegment());
		assertEquals("[seg 3]", part.toString());
		
		part = tc.get(10);
		assertFalse(part.isSegment());
		assertEquals("<x34/>", part.toString());
	}
	
	@Test
	public void testRestoreSegmentation3_4 () {		
		TextFragment tf1 = new TextFragment();		
		tf1.append(TagType.PLACEHOLDER, "x11", "<x11/>");
		tf1.append(TagType.PLACEHOLDER, "x12", "<x12/>");
		tf1.append("[seg 1]");
		tf1.append(TagType.PLACEHOLDER, "x13", "<x13/>");
		tf1.append(TagType.PLACEHOLDER, "x14", "<x14/>");
		
		TextFragment tf2 = new TextFragment();
		tf2.append("[seg 2]");
		tf2.append(TagType.PLACEHOLDER, "x21", "<x21/>");
		tf2.append(TagType.PLACEHOLDER, "x22", "<x22/>");
		tf2.append(TagType.PLACEHOLDER, "x23", "<x23/>");
		tf2.append(TagType.PLACEHOLDER, "x24", "<x24/>");
		
		TextFragment tf3 = new TextFragment();		
		tf3.append(TagType.PLACEHOLDER, "x31", "<x31/>");
		tf3.append(TagType.PLACEHOLDER, "x32", "<x32/>");
		tf3.append(TagType.PLACEHOLDER, "x33", "<x33/>");
		tf3.append("[seg 3]");
		tf3.append(TagType.PLACEHOLDER, "x34", "<x34/>");
				
		assertEquals("<1/><2/>[seg 1]<3/><4/>", fmt.setContent(tf1).toString());		
		assertEquals("[seg 2]<1/><2/><3/><4/>", fmt.setContent(tf2).toString());
		assertEquals("<1/><2/><3/>[seg 3]<4/>", fmt.setContent(tf3).toString());
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		tc.append(new TextPart("[text part 1]"));
		tc.append(new Segment("s2", tf2));
		tc.append(new TextPart("[text part 2]"));
		tc.append(new TextPart("[text part 3]"));
		tc.append(new Segment("s3", tf3));
		tc.append(new TextPart("[text part 4]"));
				
		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("<1/><2/><3/><4/><5>[seg 1]</5><6/><7/><8/><9/><10/>[text part 1]<11/><12>[seg 2]</12><13/><14/><15/><16/><17/><18/><19/>[text part 2]<20/><21/>[text part 3]<22/><23/><24/><25/><26/><27/><28>[seg 3]</28><29/><30/><31/><32/>[text part 4]<33/>",
			fmt.setContent(tf).toString());
		assertEquals("$tp_start$<x11/><x12/>$tp_end$[#$s1@%$seg_start$][seg 1][#$s1@%$seg_end$]$tp_start$<x13/><x14/>$tp_end$$tp_start$[text part 1]$tp_end$[#$s2@%$seg_start$][seg 2][#$s2@%$seg_end$]$tp_start$<x21/><x22/><x23/><x24/>$tp_end$$tp_start$[text part 2]$tp_end$$tp_start$[text part 3]$tp_end$$tp_start$<x31/><x32/><x33/>$tp_end$[#$s3@%$seg_start$][seg 3][#$s3@%$seg_end$]$tp_start$<x34/>$tp_end$$tp_start$[text part 4]$tp_end$", 
				tf.toText());
				
		TextFragment[] res = TextUnitUtil.simplifyCodes(tc, null, true);		
		assertEquals("<1/><2/><3/><4/><5>[seg 1]</5><6/><7/><8/><9/><10/>[text part 1]<11/><12>[seg 2]</12><13/><14/><15/><16/><17/><18/><19/>[text part 2]<20/><21/>[text part 3]<22/><23/><24/><25/><26/><27/><28>[seg 3]</28><29/><30/><31/><32/>[text part 4]<33/>",
			fmt.setContent(tf).toString());
		assertEquals("$tp_start$<x11/><x12/>$tp_end$[#$s1@%$seg_start$][seg 1][#$s1@%$seg_end$]$tp_start$<x13/><x14/>$tp_end$$tp_start$[text part 1]$tp_end$[#$s2@%$seg_start$][seg 2][#$s2@%$seg_end$]$tp_start$<x21/><x22/><x23/><x24/>$tp_end$$tp_start$[text part 2]$tp_end$$tp_start$[text part 3]$tp_end$$tp_start$<x31/><x32/><x33/>$tp_end$[#$s3@%$seg_start$][seg 3][#$s3@%$seg_end$]$tp_start$<x34/>$tp_end$$tp_start$[text part 4]$tp_end$", 
				tf.toText());
		assertNotNull(res);
		assertEquals("<x11/><x12/>", res[0].toText());
		assertEquals("<x34/>[text part 4]", res[1].toText());
		
		TextPart part = null;
		assertEquals(9, tc.count());
		
		part = tc.get(0);
		assertTrue(part.isSegment());
		assertEquals("[seg 1]", part.toString());
		
		part = tc.get(1);
		assertFalse(part.isSegment());
		assertEquals("<x13/><x14/>", part.toString());
		
		part = tc.get(2);
		assertFalse(part.isSegment());
		assertEquals("[text part 1]", part.toString());
	
		part = tc.get(3);
		assertTrue(part.isSegment());
		assertEquals("[seg 2]", part.toString());
	
		part = tc.get(4);
		assertFalse(part.isSegment());
		assertEquals("<x21/><x22/><x23/><x24/>", part.toString());
		
		part = tc.get(5);
		assertFalse(part.isSegment());
		assertEquals("[text part 2]", part.toString());
	
		part = tc.get(6);
		assertFalse(part.isSegment());
		assertEquals("[text part 3]", part.toString());
	
		part = tc.get(7);
		assertFalse(part.isSegment());
		assertEquals("<x31/><x32/><x33/>", part.toString());
		
		part = tc.get(8);
		assertTrue(part.isSegment());
		assertEquals("[seg 3]", part.toString());
	}
	
	@Test
	public void testExtractSegMarkers() {
		TextFragment st = new TextFragment();
		st.append(TagType.PLACEHOLDER, Code.TYPE_REFERENCE, "$tp_end$");
		st.append(TagType.PLACEHOLDER, Code.TYPE_REFERENCE, "[#$s3@%$seg_start$]");
		st.append(TagType.PLACEHOLDER, Code.TYPE_REFERENCE, "<x31/>");
		st.append(TagType.PLACEHOLDER, Code.TYPE_REFERENCE, "<x32/>");
		st.append(TagType.PLACEHOLDER, Code.TYPE_REFERENCE, "<x33/>");
		st.append(TagType.PLACEHOLDER, Code.TYPE_REFERENCE, "<x34/>");
		st.append(TagType.PLACEHOLDER, Code.TYPE_REFERENCE, "[#$s3@%$seg_end$]");
		st.append(TagType.PLACEHOLDER, Code.TYPE_REFERENCE, "$tp_start$");
		
		TextFragment res;
		TextFragment tf = new TextFragment(); 
		res = TextUnitUtil.extractSegMarkers(tf, st, false);
		
		assertEquals("<1/><2/><3/><4/>", fmt.setContent(tf).toString());
		assertEquals("$tp_end$[#$s3@%$seg_start$]<x31/><x32/><x33/><x34/>[#$s3@%$seg_end$]$tp_start$", res.toText());
		
		List<Code> codes = tf.getCodes();
		assertEquals(4, codes.size());
		
		assertEquals("$tp_end$", codes.get(0).toString()); // <1/>
		assertEquals("[#$s3@%$seg_start$]", codes.get(1).toString()); // <2/>
		assertEquals("[#$s3@%$seg_end$]", codes.get(2).toString()); // <3/>
		assertEquals("$tp_start$", codes.get(3).toString()); // <4/>
		
		tf = new TextFragment();
		res = TextUnitUtil.extractSegMarkers(tf, st, true);
		
		assertEquals("<1/><2/><3/><4/>", fmt.setContent(tf).toString());
		assertEquals("<x31/><x32/><x33/><x34/>", res.toText());
		
		codes = tf.getCodes();
		assertEquals(4, codes.size());
		
		assertEquals("$tp_end$", codes.get(0).toString()); // <1/>
		assertEquals("[#$s3@%$seg_start$]", codes.get(1).toString()); // <2/>
		assertEquals("[#$s3@%$seg_end$]", codes.get(2).toString()); // <3/>
		assertEquals("$tp_start$", codes.get(3).toString()); // <4/>
	}
	
	@Test
	public void testRestoreSegmentation4 () {		
		TextFragment tf1 = new TextFragment();		
		tf1.append(TagType.PLACEHOLDER, "x11", "<x11/>");
		tf1.append(TagType.PLACEHOLDER, "x12", "<x12/>");
		tf1.append(TagType.PLACEHOLDER, "x13", "<x13/>");
		tf1.append(TagType.PLACEHOLDER, "x14", "<x14/>");
		
		TextFragment tf2 = new TextFragment();		
		tf2.append(TagType.PLACEHOLDER, "x21", "<x21/>");
		tf2.append(TagType.PLACEHOLDER, "x22", "<x22/>");
		tf2.append(TagType.PLACEHOLDER, "x23", "<x23/>");
		tf2.append(TagType.PLACEHOLDER, "x24", "<x24/>");
		
		TextFragment tf3 = new TextFragment();		
		tf3.append(TagType.PLACEHOLDER, "x31", "<x31/>");
		tf3.append(TagType.PLACEHOLDER, "x32", "<x32/>");
		tf3.append(TagType.PLACEHOLDER, "x33", "<x33/>");
		tf3.append(TagType.PLACEHOLDER, "x34", "<x34/>");
				
		assertEquals("<1/><2/><3/><4/>", fmt.setContent(tf1).toString());		
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		tc.append(new TextPart("[text part 1]"));
		tc.append(new Segment("s2", tf2));
		tc.append(new TextPart("[text part 2]"));
		tc.append(new TextPart("[text part 3]"));
		tc.append(new Segment("s3", tf3));
		tc.append(new TextPart("[text part 4]"));
				
		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
		assertEquals("<1><2/><3/><4/><5/></1><6/>[text part 1]<7/><8><9/><10/><11/><12/></8><13/>[text part 2]<14/><15/>[text part 3]<16/><17><18/><19/><20/><21/></17><22/>[text part 4]<23/>",
			fmt.setContent(tf).toString());
		TextUnitUtil.restoreSegmentation(tc, tf);
		assertEquals("<x11/><x12/><x13/><x14/>[text part 1]<x21/><x22/><x23/><x24/>[text part 2]" +
				"[text part 3]<x31/><x32/><x33/><x34/>[text part 4]", tc.toString());
		
	}
	
	@Test
	public void testRestoreSegmentation5 () {
		TextFragment tf1 = new TextFragment();		
		tf1.append(TagType.PLACEHOLDER, "x1", "[#$0@%$seg_start$]");
		tf1.append("T1");
		tf1.append(TagType.PLACEHOLDER, "x2", "[#$0@%$seg_end$]$tp_start$ $tp_end$[#$1@%$seg_start$]<br /> <br />");
		tf1.append(" T2");
		tf1.append(TagType.PLACEHOLDER, "x3", "[#$1@%$seg_end$]");
		
		TextContainer tc = new TextContainer();
		TextUnitUtil.restoreSegmentation(tc, tf1);
		assertEquals("T1 <br /> <br /> T2", tc.toString());
		assertEquals(4, tc.count());
		Segment s1 = tc.getSegments().get(0);
		Segment s2 = tc.getSegments().get(1);
		assertEquals("T1", s1.getContent().toText());
		assertEquals(" T2", s2.getContent().toText());
		
		Iterator<TextPart> it = tc.iterator();		
		TextPart part = null;
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals("T1", part.toString());
			assertEquals("0", ((Segment) part).getId());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals(" ", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertFalse(part.isSegment());
			assertEquals("<br /> <br />", part.toString());
		}
		
		if (it.hasNext()) {
			part = it.next();
			assertTrue(part.isSegment());
			assertEquals(" T2", part.toString());
			assertEquals("1", ((Segment) part).getId());
		}
	}
	
	@Test
	public void testRestoreSegmentation6 () {
		TextFragment tf1 = new TextFragment();		
		tf1.append(TagType.PLACEHOLDER, "x1", 
				"[#$0@%$seg_start$]   [#$0@%$seg_end$]$tp_start$  $tp_end$$tp_start$ $tp_end$$tp_start$  $tp_end$[#$1@%$seg_start$]");
		tf1.append("T1");
		tf1.append(TagType.PLACEHOLDER, "x2", "[#$1@%$seg_end$]");
		
		TextContainer tc = new TextContainer();
		TextUnitUtil.restoreSegmentation(tc, tf1);
		assertEquals("        T1", tc.toString());
		
		assertEquals(5, tc.count());
		assertEquals(2, tc.getSegments().count());
		
		Segment s1 = tc.getSegments().get(0);
		Segment s2 = tc.getSegments().get(1);
		assertEquals("   ", s1.getContent().toText());
		assertEquals("T1", s2.getContent().toText());
	}
	
	@Test
	public void testRestoreSegmentation7 () {
		TextFragment tf1 = new TextFragment();
		
		tf1.append(TagType.PLACEHOLDER, "x1", 
				"[<br1> <br2> [#$0@%$seg_end$][#$1@%$seg_start$][#$1@%$seg_end$]$tp_start$ T1 $tp_end$$tp_start$ T2 $tp_end$[#$2@%$seg_start$]<br /> <br />]");
		
		TextContainer tc = new TextContainer();
		TextUnitUtil.restoreSegmentation(tc, tf1);
		assertEquals("[<br1> <br2>  T1  T2 <br /> <br />]", tc.toString());
		
		assertEquals(5, tc.count());
		
		TextPart tp1 = tc.get(0);
		assertEquals("[<br1> <br2> ", tp1.getContent().toText()); // non-open segment closed with seg_end at pos 0
		
		TextPart tp2 = tc.get(1);
		assertEquals("", tp2.getContent().toText());
		
		TextPart tp4 = tc.get(2);
		assertEquals(" T1 ", tp4.getContent().toText());
		
		TextPart tp5 = tc.get(3);
		assertEquals(" T2 ", tp5.getContent().toText());
		
		TextPart tp6 = tc.get(4);
		assertEquals("<br /> <br />]", tp6.getContent().toText());
		
		assertEquals(1, tc.getSegments().count());		
		Segment s1 = tc.getSegments().get(0);
		assertEquals("", s1.getContent().toText());
	}
	
	@Test
	public void testTrimSegments() {
		TextFragment tf1 = new TextFragment();		
		tf1.append(" [seg 1]");
		
		TextFragment tf2 = new TextFragment();		
		tf2.append("  [seg 2]   ");
		
		TextFragment tf3 = new TextFragment();		
		tf3.append("    [seg 3]     ");
		
		TextFragment tf4 = new TextFragment();		
		tf4.append("[seg 4]");
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		tc.append(new Segment("s2", tf2));
		tc.append(new Segment("s3", tf3));
		tc.append(new Segment("s4", tf4));
		
		assertEquals(" [seg 1]  [seg 2]       [seg 3]     [seg 4]", tc.toString());
		assertEquals(4, tc.count());
		
		assertTrue(tc.get(0).isSegment());
		assertTrue(tc.get(1).isSegment());
		assertTrue(tc.get(2).isSegment());
		assertTrue(tc.get(3).isSegment());
		
		assertEquals(" [seg 1]", tc.get(0).toString());
		assertEquals("  [seg 2]   ", tc.get(1).toString());
		assertEquals("    [seg 3]     ", tc.get(2).toString());
		assertEquals("[seg 4]", tc.get(3).toString());
		
		TextUnitUtil.trimSegments(tc);
		assertEquals(9, tc.count());
		
		assertTrue(!tc.get(0).isSegment());
		assertTrue(tc.get(1).isSegment());
		assertTrue(!tc.get(2).isSegment());
		assertTrue(tc.get(3).isSegment());
		assertTrue(!tc.get(4).isSegment());
		assertTrue(!tc.get(5).isSegment());
		assertTrue(tc.get(6).isSegment());
		assertTrue(!tc.get(7).isSegment());
		assertTrue(tc.get(8).isSegment());
		
		assertEquals(" ", tc.get(0).toString());
		assertEquals("[seg 1]", tc.get(1).toString());
		assertEquals("  ", tc.get(2).toString());
		assertEquals("[seg 2]", tc.get(3).toString());
		assertEquals("   ", tc.get(4).toString());
		assertEquals("    ", tc.get(5).toString());
		assertEquals("[seg 3]", tc.get(6).toString());
		assertEquals("     ", tc.get(7).toString());
		assertEquals("[seg 4]", tc.get(8).toString());
	}
	
	@Test
	public void testTrimSegments_leading() {
		TextFragment tf1 = new TextFragment();		
		tf1.append(" [seg 1]");
		
		TextFragment tf2 = new TextFragment();		
		tf2.append("  [seg 2]   ");
		
		TextFragment tf3 = new TextFragment();		
		tf3.append("    [seg 3]     ");
		
		TextFragment tf4 = new TextFragment();		
		tf4.append("[seg 4]");
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		tc.append(new Segment("s2", tf2));
		tc.append(new Segment("s3", tf3));
		tc.append(new Segment("s4", tf4));
		
		assertEquals(" [seg 1]  [seg 2]       [seg 3]     [seg 4]", tc.toString());
		assertEquals(4, tc.count());
		
		assertTrue(tc.get(0).isSegment());
		assertTrue(tc.get(1).isSegment());
		assertTrue(tc.get(2).isSegment());
		assertTrue(tc.get(3).isSegment());
		
		assertEquals(" [seg 1]", tc.get(0).toString());
		assertEquals("  [seg 2]   ", tc.get(1).toString());
		assertEquals("    [seg 3]     ", tc.get(2).toString());
		assertEquals("[seg 4]", tc.get(3).toString());
		
		TextUnitUtil.trimSegments(tc, true, false);
		assertEquals(7, tc.count());
		
		assertTrue(!tc.get(0).isSegment());
		assertTrue(tc.get(1).isSegment());
		assertTrue(!tc.get(2).isSegment());
		assertTrue(tc.get(3).isSegment());
		assertTrue(!tc.get(4).isSegment());
		assertTrue(tc.get(5).isSegment());
		assertTrue(tc.get(6).isSegment());
		
		assertEquals(" ", tc.get(0).toString());
		assertEquals("[seg 1]", tc.get(1).toString());
		assertEquals("  ", tc.get(2).toString());
		assertEquals("[seg 2]   ", tc.get(3).toString());
		assertEquals("    ", tc.get(4).toString());
		assertEquals("[seg 3]     ", tc.get(5).toString());
		assertEquals("[seg 4]", tc.get(6).toString());
	}
	
	@Test
	public void testTrimSegments_trailing() {
		TextFragment tf1 = new TextFragment();		
		tf1.append(" [seg 1]");
		
		TextFragment tf2 = new TextFragment();		
		tf2.append("  [seg 2]   ");
		
		TextFragment tf3 = new TextFragment();		
		tf3.append("    [seg 3]     ");
		
		TextFragment tf4 = new TextFragment();		
		tf4.append("[seg 4]");
		
		TextContainer tc = new TextContainer();
		tc.append(new Segment("s1", tf1));
		tc.append(new Segment("s2", tf2));
		tc.append(new Segment("s3", tf3));
		tc.append(new Segment("s4", tf4));
		
		assertEquals(" [seg 1]  [seg 2]       [seg 3]     [seg 4]", tc.toString());
		assertEquals(4, tc.count());
		
		assertTrue(tc.get(0).isSegment());
		assertTrue(tc.get(1).isSegment());
		assertTrue(tc.get(2).isSegment());
		assertTrue(tc.get(3).isSegment());
		
		assertEquals(" [seg 1]", tc.get(0).toString());
		assertEquals("  [seg 2]   ", tc.get(1).toString());
		assertEquals("    [seg 3]     ", tc.get(2).toString());
		assertEquals("[seg 4]", tc.get(3).toString());
		
		TextUnitUtil.trimSegments(tc, false, true);
		
		assertEquals(6, tc.count());
		
		assertTrue(tc.get(0).isSegment());
		assertTrue(tc.get(1).isSegment());
		assertTrue(!tc.get(2).isSegment());
		assertTrue(tc.get(3).isSegment());
		assertTrue(!tc.get(4).isSegment());
		assertTrue(tc.get(5).isSegment());
		
		assertEquals(" [seg 1]", tc.get(0).toString());
		assertEquals("  [seg 2]", tc.get(1).toString());
		assertEquals("   ", tc.get(2).toString());
		assertEquals("    [seg 3]", tc.get(3).toString());
		assertEquals("     ", tc.get(4).toString());
		assertEquals("[seg 4]", tc.get(5).toString());
	}
	
	@Test
	public void testRemoveCodes () {
		TextFragment tf = makeFragment1();
		assertEquals("ABC", TextUnitUtil.removeCodes(tf.getCodedText()));
		tf = makeFragment1Bis("extra");
		assertEquals("ABC extra", TextUnitUtil.removeCodes(tf.getCodedText()));
		tf = createTextUnit1().getSource().getFirstContent();
		assertEquals("t bold t ", TextUnitUtil.removeCodes(tf.getCodedText()));
	}
	
	/**
	 * Makes a fragment <code>[b]A[br/]B[/b]C<code>
	 * @return the new fragment.
	 */
	private TextFragment makeFragment1 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "b", "[b]");
		tf.append("A");
		tf.append(TagType.PLACEHOLDER, "br", "[br/]");
		tf.append("B");
		tf.append(TagType.CLOSING, "b", "[/b]");
		tf.append("C");
		return tf;
	}

	/**
	 * Makes a fragment <code>{B}A{/B}B{BR/}C extra<code>
	 * @return the new fragment.
	 */
	private TextFragment makeFragment1Bis (String extra) {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "b", "{B}");
		tf.append("A");
		tf.append(TagType.CLOSING, "b", "{/B}");
		tf.append("B");
		tf.append(TagType.PLACEHOLDER, "br", "{BR/}");
		tf.append("C "+extra);
		return tf;
	}

	private ITextUnit createTextUnit1 () {
		ITextUnit tu = new TextUnit("1", "t ");
		TextFragment tf = tu.getSource().getSegments().getFirstContent();
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("bold");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(" t ");
		tf.append(TagType.PLACEHOLDER, "br", "<br/>");
		return tu;
	}

}
