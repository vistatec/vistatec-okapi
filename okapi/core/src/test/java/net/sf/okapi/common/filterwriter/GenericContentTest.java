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

package net.sf.okapi.common.filterwriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GenericContentTest {

	private GenericContent fmt;
	
	@Before
	public void setUp() throws Exception {
		fmt = new GenericContent();
	}
	
	@Test
	public void testSimple_Default () {
		TextFragment tf = createTextFragment();
		assertEquals(5, tf.getCodes().size());
		String gtext = fmt.setContent(tf).toString();
		assertEquals("t1<1><2><3/>t2</2></1>t3", gtext);
		// Reconstruct it
		TextFragment tf2 = tf.clone();
		GenericContent.updateFragment(gtext, tf2, false);
		assertEquals("t1<1><2><3/>t2</2></1>t3", fmt.setContent(tf2).toString());
	}
	
//	@Test
//	public void testFromNumericCodedToFragment1 () {
//		TextFragment tf = createTextFragment();
//		String gtext = fmt.setContent(tf).toString();
//		assertEquals("t1<1><2><3/>t2</2></1>t3", gtext);
//		// Reconstruct it
//		TextFragment tf2 = fmt.fromNumericCodedToFragment(gtext, tf.getCodes(), false);
//		assertEquals("t1<1><2><3/>t2</2></1>t3", fmt.setContent(tf2).toString());
//	}
	
//	@Test
//	public void testFromNumericCodedToFragment2 () {
//		TextFragment tf = createTextFragment();
//		StringBuilder tmp = new StringBuilder(fmt.setContent(tf).toString());
//		assertEquals("t1<1><2><3/>t2</2></1>t3", tmp.toString());
//		// Reconstruct it (with lost of codes)
//		tmp.delete(2, 5); // Removes <1>
//		TextFragment tf2 = fmt.fromNumericCodedToFragment(tmp.toString(), tf.getCodes(), true);
//		assertEquals("t1<2><3/>t2</2><e1>t3", fmt.setContent(tf2).toString());
//	}
	
	@Test
	public void testSimple_WithOption () {
		TextFragment tf = createTextFragment();
		assertEquals(5, tf.getCodes().size());
		fmt.setContent(tf);
		assertEquals("t1<b1><b2><x1/>t2</b2></b1>t3", fmt.toString(true));
		assertEquals("t1<1><2><3/>t2</2></1>t3", fmt.toString(false));
	}
	
	@Test
	public void testMisOrderedCodes () {
		TextFragment tf = new TextFragment();
		tf.append("t1");
		tf.append(TagType.OPENING, "b1", "<b1>");
		tf.append("t2");
		tf.append(TagType.OPENING, "b2", "<b2>");
		tf.append("t3");
		tf.append(TagType.CLOSING, "b1", "</b1>");
		tf.append("t4");
		tf.append(TagType.CLOSING, "b2", "</b2>");
		tf.append("t5");
		fmt.setContent(tf);
		// Not real XML so mis-ordering is OK
		assertEquals("t1<b1>t2<b2>t3</b1>t4</b2>t5", fmt.toString(true));
		String gtext = fmt.toString(false);
		assertEquals("t1<b1/>t2<b2/>t3<e1/>t4<e2/>t5", gtext);
		// Reconstruct it
		TextFragment tf2 = tf.clone();
		GenericContent.updateFragment(gtext, tf2, false);
		assertEquals("t1<b1>t2<b2>t3</b1>t4</b2>t5", fmt.setContent(tf2).toString(true));
		assertEquals("t1<b1/>t2<b2/>t3<e1/>t4<e2/>t5", fmt.setContent(tf2).toString());
	}
	
	@Test
	public void testReOrderingCodes () {
		TextFragment tf = new TextFragment();
		tf.append("t1");
		tf.append(TagType.OPENING, "b1", "<b1>");
		tf.append("t2");
		tf.append(TagType.OPENING, "b2", "<b2>");
		tf.append("t3");
		tf.append(TagType.CLOSING, "b1", "</b1>");
		tf.append("t4");
		tf.append(TagType.CLOSING, "b2", "</b2>");
		tf.append("t5");
		fmt.setContent(tf);
		// Not real XML so mis-ordering is OK
		assertEquals("t1<b1>t2<b2>t3</b1>t4</b2>t5", fmt.toString(true));
		assertEquals("t1<b1/>t2<b2/>t3<e1/>t4<e2/>t5", fmt.toString(false));
		// Reconstruct it in a different order
//TODO
//		TextFragment tf2 = tf.clone();
//		fmt.updateFragment("t1<b1/>t2<b2/>t4<e2/>t5 t3<e1/>", tf2, false);
//		assertEquals("t1<1>t2<2>t4</2>t5 t3</1>", fmt.setContent(tf2).toString());
	}

	@Test
	public void testLetterCodedToFragment () {
		String ori = "t1<g1>t2</g1><g2><x3/>t3<g4>t4</g4>t5</g2>t6<x5/>t<b6/>t<g7>t</g7>t<e6/><x8/><x9/><x10/>";
		TextFragment tf1 = GenericContent.fromLetterCodedToFragment(ori, null, false, true);
		assertNotNull(tf1);
		assertEquals(15, tf1.getCodes().size());
		assertEquals("t1<1>t2</1><2><3/>t3<4>t4</4>t5</2>t6<5/>t<6>t<7>t</7>t</6><8/><9/><10/>", fmt.setContent(tf1).toString());
		
		TextFragment tf = createTextFragment();
		tf1 = GenericContent.fromLetterCodedToFragment(ori, tf, false, true);
		assertEquals(tf, tf1);
		assertEquals("t1<1>t2</1><2><3/>t3<4>t4</4>t5</2>t6<5/>t<6>t<7>t</7>t</6><8/><9/><10/>", fmt.setContent(tf1).toString());
	}

	@Test
	public void testFragmentToLetterCoded () {
		TextFragment tf1 = createTextFragment();
		String res = GenericContent.fromFragmentToLetterCoded(tf1, true);
		assertEquals("t1<g1><g2><x3/>t2</g2></g1>t3", res);
		// Try round trip
		TextFragment tf2 = GenericContent.fromLetterCodedToFragment(res, null, false, true);
		assertEquals(fmt.setContent(tf1).toString(), fmt.setContent(tf2).toString());
	}

	@Test
	public void testDataTransfer () {
		TextFragment tf = new TextFragment();
		tf.append("t1");
		tf.append(TagType.OPENING, "b1", "[b1]");
		tf.append(TagType.OPENING, "b2", "[b2]");
		tf.append(TagType.PLACEHOLDER, "x1", "[x/]");
		tf.append("t2");
		tf.append(TagType.CLOSING, "b2", "[/b2]");
		tf.append(TagType.CLOSING, "b1", "[/b1]");
		tf.append("t3");
		assertEquals("t1[b1][b2][x/]t2[/b2][/b1]t3", tf.toText());
		
		String res = GenericContent.fromFragmentToLetterCoded(tf, true);
		TextFragment tf2 = GenericContent.fromLetterCodedToFragment(res, tf, true, true);
		assertEquals("t1[b1][b2][x/]t2[/b2][/b1]t3", tf2.toText());

		TextFragment tf3 = GenericContent.fromLetterCodedToFragment(res, tf, false, true);
		assertEquals("t1<g1><g2><x3/>t2</g2></g1>t3", tf3.toText());
	}
	
	@Test
	public void testUpdate () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "[x/]");
		tf.append("A");
		tf.append(TagType.OPENING, "b1", "[b1]");
		tf.append("B");
		tf.append(TagType.CLOSING, "b1", "[/b1]");
		
		TextFragment tf2 = new TextFragment("", tf.getClonedCodes());
		GenericContent.updateFragment("<1/>ZZ<2>QQ</2>", tf2, false);
		assertEquals("[x/]ZZ[b1]QQ[/b1]", tf2.toText());
	}
	
	private TextFragment createTextFragment () {
		TextFragment tf = new TextFragment();
		tf.append("t1");
		tf.append(TagType.OPENING, "b1", "<b1>");
		tf.append(TagType.OPENING, "b2", "<b2>");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("t2");
		tf.append(TagType.CLOSING, "b2", "</b2>");
		tf.append(TagType.CLOSING, "b1", "</b1>");
		tf.append("t3");
		return tf;
	}


	@Test
	public void testEncodeLetterCodesPlainOpenCloseTags() {
		testEncodeLetterCodeTags("<g1><g458></g458></g1>", "<gg1><gg458></gg458></gg1>");
	}

	@Test
	public void testDecodeLetterCodesOpenCloseTags() {
		testDecodeLetterCodeTags("<gg1><gg458></gg458></gg1>", "<g1><g458></g458></g1>");
	}

	@Test
	public void testRoundTripLetterCodesOpenCloseTags() {
		testRoundTripWithInlineCodes("<g1><g458></g458>", "</g1>");
	}

	@Test
	public void testEncodeLetterCodesIsolatedTags() {
		testEncodeLetterCodeTags("<x2/><b34/><e65432/>", "<xx2/><bb34/><ee65432/>");
	}

	@Test
	public void testDecodeLetterCodesIsolatedTags() {
		testDecodeLetterCodeTags("<xx2/><bb34/><ee65432/>", "<x2/><b34/><e65432/>");
	}

	@Test
	public void testRoundTripLetterCodesIsolatedTags() {
		testRoundTripWithInlineCodes("<x2/><b34/><e65432/>", "<b3/><x1/>");
	}

	@Test
	public void testEncodeLetterCodesPreEscapedTags() {
		testEncodeLetterCodeTags("<gg1></gg2><gggg1><xx3/><bb4/><ee5/><xxxx3/><bbbb4/><eeee5/>",
				                 "<ggg1></ggg2><ggggg1><xxx3/><bbb4/><eee5/><xxxxx3/><bbbbb4/><eeeee5/>");
	}

	@Test
	public void testDecodeLetterCodesPreEscapedTags() {
		testDecodeLetterCodeTags("<ggg1></ggg2><ggggg1><xxx3/><bbb4/><eee5/><xxxxx3/><bbbbb4/><eeeee5/>",
				                 "<gg1></gg2><gggg1><xx3/><bb4/><ee5/><xxxx3/><bbbb4/><eeee5/>");
	}

	@Test
	public void testRoundTripLetterCodesPreEscapedTags() {
		testRoundTripWithInlineCodes("<gg1></gg2><gggg1><xx3/><bb4/><ee5/>", "<xxxx3/><bbbb4/><eeee5/>");
	}

	@Test
	public void testEncodeLetterCodesNonDigitTags() {
		String nonMatchingCodes = "<g></g><ggg></ggg><x/><xx/><b/><bbbb/><e/><ee/>";
		testEncodeLetterCodeTags(nonMatchingCodes, nonMatchingCodes);
	}

	@Test
	public void testDecodeLetterCodesNonDigitTags() {
		String nonMatchingCodes = "<g></g><ggg></ggg><x/><xx/><b/><bbbb/><e/><ee/>";
		testDecodeLetterCodeTags(nonMatchingCodes, nonMatchingCodes);
	}

	@Test
	public void testRoundTripLetterCodesNonDigitTags() {
		testRoundTripWithInlineCodes("<g></g><ggg></ggg><x/>", "<xx/><b/><bbbb/><e/><ee/>");
	}

	@Test
	public void testEncodeLetterCodesInappropriateClosing() {
		String nonMatchingClosingTags = "<x><xx><b><bb><e><ee><x2><b34><e65432> </x></b></e></x2></b34></e65432> <xx3><bb4><ee5> <g/><ggg/>";
		testEncodeLetterCodeTags(nonMatchingClosingTags, nonMatchingClosingTags);
	}

	@Test
	public void testDecodeLetterCodesInappropriateClosing() {
		String nonMatchingClosingTags = "<x><xx><b><bb><e><ee><x2><b34><e65432> </x></b></e></x2></b34></e65432> <xx3><bb4><ee5> <g/><ggg/>";
		testDecodeLetterCodeTags(nonMatchingClosingTags, nonMatchingClosingTags);
	}

	@Test
	public void testRoundTripLetterCodesInappropriateClosing() {
		testRoundTripWithInlineCodes("<x><xx><b><bb><e><ee><x2><b34><e65432>", " </x></b></e></x2></b34></e65432> <xx3><bb4><ee5> <g/><ggg/>");
	}

	@Test
	public void testEncodeLetterCodesInappropriateLetters() {
		String inappropriateLettersTags = "<a><a1><z23><y1/></y1>"
				+ " <ba1><ab1><bab1></ba1></ab1></bab1><ba1/><ab1/><bab1/>"
				+ " <ex1/><be3/><xb4/><bxb2>"
				+ " <ggagg2><gygg3/><xxxs7/><bubb43/><evee98/>";
		testEncodeLetterCodeTags(inappropriateLettersTags, inappropriateLettersTags);
	}

	@Test
	public void testDecodeLetterCodesInappropriateLetters() {
		String inappropriateLettersTags = "<a><a1><z23><y1/></y1>"
				+ " <ba1><ab1><bab1></ba1></ab1></bab1><ba1/><ab1/><bab1/>"
				+ " <ex1/><be3/><xb4/><bxb2>"
				+ " <ggagg2><gygg3/><xxxs7/><bubb43/><evee98/>";
		testDecodeLetterCodeTags(inappropriateLettersTags, inappropriateLettersTags);
	}

	@Test
	public void testRoundTripLetterCodesInappropriateLetters() {
		testRoundTripWithInlineCodes("<a><a1><z23><y1/></y1> <ba1><ab1><bab1></ba1></ab1></bab1><ba1/><ab1/><bab1/>",
				" <ex1/><be3/><xb4/><bxb2> <ggagg2><gygg3/><xxxs7/><bubb43/><evee98/>");
	}


	private void testEncodeLetterCodeTags(String originalText,
			String encodedText) {
		TextFragment tf = new TextFragment(originalText);
		String letterCoded = GenericContent.fromFragmentToLetterCoded(tf, true);
		assertEquals("tags that would match letter code tags should have their first letter prepended",
				encodedText, letterCoded);
	}

	private void testDecodeLetterCodeTags(String codedText, String decodedText) {
		TextFragment tf = GenericContent.fromLetterCodedToFragment(codedText, null, false, true);
		assertEquals("tags that have been encoded should be decoded to their original state",
				decodedText, tf.toText());
	}

	private void testRoundTripWithInlineCodes(String firstPart,
			String secondPart) {
		TextFragment tf = createTextFragmentWithCodes(firstPart, secondPart);
		String before = tf.toText();
		String letterCoded = GenericContent.fromFragmentToLetterCoded(tf, true);
		TextFragment decoded = GenericContent.fromLetterCodedToFragment(letterCoded, tf, true, true);
		String after = decoded.toText();
		assertEquals("fragment to letter-coded round trip should not change text fragment", before, after);
	}

	private TextFragment createTextFragmentWithCodes (String firstPart, String secondPart) {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a-tag", "[a]");
		tf.append(TagType.OPENING, "c-tag", "[c]");
		tf.append(firstPart);
		tf.append(TagType.PLACEHOLDER, "d-tag", "[d/]");
		tf.append(secondPart);
		tf.append(TagType.CLOSING, "c-tag", "[/c]");
		tf.append(TagType.CLOSING, "a-tag", "[/a]");
		return tf;
	}

}
