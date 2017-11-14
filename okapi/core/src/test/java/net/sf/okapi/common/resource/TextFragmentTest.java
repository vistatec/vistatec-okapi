/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TextFragmentTest {

	private GenericContent fmt;
	
	@Before
	public void setUp () throws Exception {
		fmt = new GenericContent();
	}
	
	@Test
	public void testConstructors () {
		TextFragment tf1 = new TextFragment();
		assertTrue(tf1.isEmpty());
		assertNotNull(tf1.toText());
		assertNotNull(tf1.getCodedText());
		tf1 = new TextFragment("text");
		assertFalse(tf1.isEmpty());
		TextFragment tf2 = new TextFragment(tf1);
		assertEquals(tf1.toText(), tf2.toText());
		assertNotSame(tf1, tf2);
	}
	
	@Test
	public void testAppend () {
		TextFragment tf1 = new TextFragment();
		tf1.append('c');
		assertEquals("c", tf1.toText());
		tf1 = new TextFragment();
		tf1.append("string");
		assertEquals("string", tf1.toText());
		tf1.append('c');
		assertEquals("stringc", tf1.toText());
		TextFragment tf2 = new TextFragment();
		tf2.append(tf1);
		assertEquals("stringc", tf2.toText());
		assertNotSame(tf1, tf2);
		assertFalse(tf1.hasCode());
		
		tf1 = new TextFragment("string");
		tf1.append(TagType.PLACEHOLDER, "br", "<br/>");
		String s1 = tf1.getCodedText();
		s1 = s1.toUpperCase();
		assertEquals("string<br/>", tf1.toText());
		tf1.setCodedText(s1);
		assertEquals("STRING<br/>", tf1.toText());
		
		// Test with in-line codes
		tf1 = new TextFragment();
		tf1.append(TagType.PLACEHOLDER, "br", "<br/>");
		assertTrue(tf1.hasCode());
		Code code = tf1.getCode(0);
		assertEquals("<br/>", code.getData());
		assertEquals("<br/>", tf1.toText()); 
	}	

	@Test
	public void testAppend2 () {
		TextFragment tf = new TextFragment();
		
		tf.append(new Code(TagType.OPENING, "x", "[1]"));		
		tf.append("AAAA");
		tf.append(new Code(TagType.CLOSING, "x", "[/1]"));
		assertEquals("<1>AAAA</1>", 
			(new GenericContent(tf)).toString());
		
		tf.append(new Code(TagType.OPENING, "x", "[2]"));
		tf.append(new Code(TagType.PLACEHOLDER, "x", "[3/]"));
		tf.append(new Code(TagType.CLOSING, "x", "[/2]"));
		assertEquals("<1>AAAA</1><2><3/></2>", 
			(new GenericContent(tf)).toString());
		
		tf.append(new Code(TagType.OPENING, "x", "[4]"));
		tf.append("BBBB");
		tf.append(new Code(TagType.CLOSING, "x", "[/4]"));
		assertEquals("<1>AAAA</1><2><3/></2><4>BBBB</4>", 
			(new GenericContent(tf)).toString());
		
		tf.append(new Code(TagType.OPENING, "x", "[5]"));
		tf.append(new Code(TagType.PLACEHOLDER, "x", "[6/]"));
		tf.append(new Code(TagType.CLOSING, "x", "[/5]"));
		assertEquals("<1>AAAA</1><2><3/></2><4>BBBB</4><5><6/></5>", 
			(new GenericContent(tf)).toString());
		
		TextFragment tf2 = new TextFragment();
		
		tf2.append(new Code(TagType.OPENING, "x", "[7]"));
		tf2.append(tf);
		tf2.append(new Code(TagType.CLOSING, "x", "[/7]"));
		
//		assertEquals("<1><2>AAAA</1><b3/><4/></2><5>BBBB<e3/><b6/><7/></5><e8/>", 
//			(new GenericContent(tf2)).toString());
		assertEquals("[7][1]AAAA[/1][2][3/][/2][4]BBBB[/4][5][6/][/5][/7]", 
			(new GenericContent(tf2)).toString(true));
		// FIXME Expected: 
		assertEquals("<1><2>AAAA</2><3><4/></3><5>BBBB</5><6><7/></6></1>", 
			(new GenericContent(tf2)).toString());
	}
	
	@Test
	public void testAppendWithDifferentCodeIDs () {
		TextFragment tf1 = new TextFragment();
		tf1.append(TagType.PLACEHOLDER, "a", "[A/]");
		assertEquals(1, tf1.getCode(0).getId());
		
		TextFragment tf2 = new TextFragment();
		tf2.append(TagType.PLACEHOLDER, "b", "[B/]");
		assertEquals(1, tf2.getCode(0).getId());
		
		tf1.append(' ');
		tf1.append(tf2);
		assertEquals("[A/] [B/]", tf1.toText());
		assertEquals(1, tf1.getCode(0).getId());
		assertEquals(2, tf1.getCode(1).getId());
	}

	@Test
	public void testInsertWithCodes_12_123_12345 () {
		TextFragment tf1 = new TextFragment();
		tf1.append(TagType.PLACEHOLDER, "a", "a");
		tf1.append(TagType.PLACEHOLDER, "b", "b");
		
		TextFragment tf2 = new TextFragment();
		tf2.append(TagType.PLACEHOLDER, "c", "c");
		tf2.append(TagType.PLACEHOLDER, "d", "d");
		tf2.append(TagType.PLACEHOLDER, "e", "e");
		
		tf1.insert(-1, tf2);
		assertEquals("abcde", tf1.toText());
		assertEquals("<1/><2/><3/><4/><5/>", fmt.setContent(tf1).toString());
	}
	
	@Test
	public void testInsertWithCodes_145_134_145689 () {
		TextFragment tf1 = new TextFragment();
		tf1.append(TagType.PLACEHOLDER, "a", "a");
		tf1.append(TagType.PLACEHOLDER, "b", "b", 4);
		tf1.append(TagType.PLACEHOLDER, "c", "c", 5);
		
		TextFragment tf2 = new TextFragment();
		tf2.append(TagType.PLACEHOLDER, "d", "d");
		tf2.append(TagType.PLACEHOLDER, "e", "e", 3);
		tf2.append(TagType.PLACEHOLDER, "f", "f", 4);
		
		tf1.insert(-1, tf2);
		assertEquals("abcdef", tf1.toText());
		assertEquals("<1/><4/><5/><6/><8/><9/>", fmt.setContent(tf1).toString());
	}
	
	@Test
	public void testInsertWithCodes_12_345_12345 () {
		TextFragment tf1 = new TextFragment();
		tf1.append(TagType.PLACEHOLDER, "a", "a");
		tf1.append(TagType.PLACEHOLDER, "b", "b");

		TextFragment tf2 = new TextFragment();
		tf2.append(TagType.PLACEHOLDER, "c", "c", 3);
		tf2.append(TagType.PLACEHOLDER, "d", "d", 4);
		tf2.append(TagType.PLACEHOLDER, "e", "e", 5);
		
		tf1.insert(-1, tf2);
		assertEquals("abcde", tf1.toText());
		assertEquals("<1/><2/><3/><4/><5/>", fmt.setContent(tf1).toString());
	}

	@Test
	public void testInsertWithCodes_13_M3M_13768 () {
		TextFragment tf1 = new TextFragment();
		tf1.append(TagType.PLACEHOLDER, "a", "a");
		tf1.append(TagType.PLACEHOLDER, "b", "b", 3);

		TextFragment tf2 = new TextFragment();
		tf2.append(TagType.PLACEHOLDER, "C", "C", -2);
		tf2.append(TagType.PLACEHOLDER, "d", "d", 3);
		tf2.append(TagType.PLACEHOLDER, "E", "E", -3);
		
		tf1.insert(-1, tf2);
		assertEquals("abCdE", tf1.toText());
		assertEquals("<1/><3/><7/><6/><8/>", fmt.setContent(tf1).toString());
	}
	
	@Test
	public void testInsert () {
		TextFragment tf1 = new TextFragment();
		tf1.insert(0, new TextFragment("[ins1]"));
		assertEquals("[ins1]", tf1.toText());
		tf1.insert(4, new TextFragment("ertion"));
		assertEquals("[insertion1]", tf1.toText());
		tf1.insert(0, new TextFragment("<"));
		assertEquals("<[insertion1]", tf1.toText());
		tf1.insert(13, new TextFragment(">"));
		assertEquals("<[insertion1]>", tf1.toText());
		tf1.insert(-1, new TextFragment("$"));
		assertEquals("<[insertion1]>$", tf1.toText());
		// Test with in-line codes
		tf1 = new TextFragment();
		tf1.insert(0, new TextFragment("abc"));
		TextFragment tf2 = new TextFragment();
		tf2.append(TagType.PLACEHOLDER, "br", "<br/>");
		tf1.insert(1, tf2);
		Code code = tf1.getCode(0);
		assertEquals("<br/>", code.getData());
		assertEquals("a<br/>bc", fmt.setContent(tf1).toString(true));
		tf2 = new TextFragment();
		tf2.append(TagType.OPENING, "b", "<b>");
		tf1.insert(4, tf2);
		tf2 = new TextFragment();
		tf2.append(TagType.CLOSING, "b", "</b>");
		tf1.insert(7, tf2);
		tf2 = new TextFragment();
		tf2.append(TagType.PLACEHOLDER, "x", "<x/>");
		tf1.insert(-1, tf2);
		assertEquals("a<br/>b<b>c</b><x/>", tf1.toText());
	}

	@Test
	public void testAppendWithSameID () {
		// Two separate fragments with one code of same id
		TextFragment tf1 = new TextFragment();
		tf1.append(TagType.OPENING, "b", "[b]");
		tf1.append("T1.");
		TextFragment tf2 = new TextFragment(" T2");
		tf2.append(TagType.PLACEHOLDER, "br", "[br/]");
		tf2.append(TagType.CLOSING, "b", "[/b]");

		// First codes of each have same id
		assertEquals(tf1.getCodes().get(0).id, tf2.getCodes().get(0).id);
		// Second code is 2
		assertEquals(2, tf2.getCodes().get(1).id);
		
		// Now group into a single fragment
		tf1.append(tf2); // Effectively calling insert()
		
		List<Code> codes = tf1.getCodes();
		assertEquals(1, codes.get(0).id); // opening
//TOFIX		assertEquals(2, codes.get(1).id); // placeholder
		assertEquals(1, codes.get(2).id); // closing (balanced)
	}

	@Test
	public void testRemove () {
		TextFragment tf1 = makeFragment1();
		assertEquals("[b]A[br/]B[/b]C", fmt.setContent(tf1).toString(true));
		tf1.remove(2, 3); // xxAxxBxxC -> xxxxBxxC
		tf1.remove(4, 5); // xxxxBxxC -> xxxxxxC
		tf1.remove(6, 7); // xxxxxxC -> xxxxxx
		assertFalse(tf1.hasText(true));
		assertEquals(3*2, tf1.getCodedText().length());
		assertEquals("[b][br/][/b]", tf1.toText());

		tf1 = makeFragment1();
		tf1.remove(0, 2); // xxAxxBxxC -> AxxBxxC
		tf1.remove(1, 3); // AxxBxxC -> ABxxC
		tf1.remove(2, 4); // ABxxC -> ABC
		assertFalse(tf1.hasCode());
		assertEquals(3, tf1.getCodedText().length());
		assertEquals("ABC", tf1.toText());
	}
	
	@Test
	public void testInlines () {
		TextFragment tf1 = makeFragment1();
		assertTrue(tf1.hasCode());
		assertEquals("[b]A[br/]B[/b]C", tf1.toText());
		assertEquals("[b]", tf1.getCode(0).getData());
		assertEquals("[br/]", tf1.getCode(1).getData());
		assertEquals("[/b]", tf1.getCode(2).getData());
		assertEquals("<1>A<2/>B</1>C", fmt.setContent(tf1).toString(false));
		tf1.remove(0, 2);
		//TODO: assertEquals("A<2/>B<1/>C", display.setContent(tf1).toString(false));
		assertEquals("A[br/]B[/b]C", tf1.toText());
		TextFragment tf2 = new TextFragment();
		tf2.append(TagType.OPENING, "b", "[b]");
		tf1.insert(0, tf2);
		//TODO: assertEquals("<1/>A<2/>B</1>C", display.setContent(tf1).toString(false));
		
		Code code1 = new Code(TagType.PLACEHOLDER, "type", "data");
		code1.setReferenceFlag(true);
		code1.setId(100);
		code1.setOuterData("outer");
		assertEquals("type", code1.getType());
		assertEquals("data", code1.getData());
		assertEquals("outer", code1.getOuterData());
		assertEquals(100, code1.getId());
		assertEquals(TagType.PLACEHOLDER, code1.getTagType());

		tf1 = new TextFragment();
		Code code2 = tf1.append(code1).getLastCode();
		Code code3 = tf1.getCode(0);
		assertSame(code1, code2);
		assertSame(code2, code3);
		code1 = null;
		assertEquals("type", code2.getType());
		assertEquals("data", code2.getData());
		assertEquals("outer", code2.getOuterData());
		assertEquals(100, code2.getId());
		assertEquals(TagType.PLACEHOLDER, code2.getTagType());
		
		Code code4 = code2.clone();
		assertNotSame(code4, code2);
		assertEquals("type", code4.getType());
		assertEquals("data", code4.getData());
		assertEquals("outer", code4.getOuterData());
		assertEquals(100, code4.getId());
		assertEquals(TagType.PLACEHOLDER, code4.getTagType());
		
		code1 = new Code(TagType.PLACEHOLDER, "t", "d");
		assertFalse(code1.hasReference());
		assertFalse(code1.isCloneable());
		assertFalse(code1.isDeleteable());
		code2 = code1.clone();
		assertFalse(code2.hasReference());
		assertFalse(code2.isCloneable());
		assertFalse(code2.isDeleteable());
		
		code1.setReferenceFlag(true);
		code1.setCloneable(true);
		code1.setDeleteable(true);
		assertTrue(code1.hasReference());
		assertTrue(code1.isCloneable());
		assertTrue(code1.isDeleteable());
		
		code1.setReferenceFlag(false);
		assertFalse(code1.hasReference());
		assertTrue(code1.isCloneable());
		assertTrue(code1.isDeleteable());
		
		code1.setCloneable(false);
		assertFalse(code1.hasReference());
		assertFalse(code1.isCloneable());
		assertTrue(code1.isDeleteable());

		code1.setDeleteable(false);
		assertFalse(code1.hasReference());
		assertFalse(code1.isCloneable());
		assertFalse(code1.isDeleteable());

		code1.setReferenceFlag(true);
		code1.setDeleteable(true);
		assertTrue(code1.hasReference());
		assertFalse(code1.isCloneable());
		assertTrue(code1.isDeleteable());
		
		tf1 = new TextFragment();
		tf1.append(code1);
		String codesStorage1 = Code.codesToString(tf1.getCodes());
		String textStorage1 = tf1.getCodedText();
		assertNotNull(codesStorage1);
		assertNotNull(textStorage1);
		tf2 = new TextFragment();
		tf2.setCodedText(textStorage1, Code.stringToCodes(codesStorage1));
		assertEquals(tf1.toText(), tf2.toText());
		String codesStorage2 = Code.codesToString(tf2.getCodes());
		String textStorage2 = tf2.getCodedText();
		assertEquals(codesStorage1, codesStorage2);
		assertEquals(textStorage1, textStorage2);
	}
	
	@Test
	public void testCodesWithOriginalId() {
		Code code1 = new Code(TagType.PLACEHOLDER, "type", "data");
		code1.setReferenceFlag(true);
		code1.setId(100);
		code1.setOriginalId("xxidxx");
		code1.setOuterData("outer");
		assertEquals("type", code1.getType());
		assertEquals("data", code1.getData());
		assertEquals("outer", code1.getOuterData());
		assertEquals(100, code1.getId());
		assertEquals("xxidxx", code1.getOriginalId());
		assertEquals(TagType.PLACEHOLDER, code1.getTagType());
		
		TextFragment tf1 = new TextFragment();
		tf1.append(code1);
		String codesStorage1 = Code.codesToString(tf1.getCodes());
		String textStorage1 = tf1.getCodedText();
		assertNotNull(codesStorage1);
		assertNotNull(textStorage1);
		TextFragment tf2 = new TextFragment();
		tf2.setCodedText(textStorage1, Code.stringToCodes(codesStorage1));
		assertEquals(tf1.toText(), tf2.toText());
		String codesStorage2 = Code.codesToString(tf2.getCodes());
		String textStorage2 = tf2.getCodedText();
		assertEquals(codesStorage1, codesStorage2);
		assertEquals(textStorage1, textStorage2);
	}
	
	@Test
	public void testCodedText () {
		TextFragment tf1 = makeFragment1();
		assertEquals((2*3)+3, tf1.getCodedText().length()); // 2 per code + 3 chars
		assertEquals(2, tf1.getCodedText(3, 5).length()); // code length for <br/>
		
		String codedText = tf1.getCodedText();
		List<Code> codes = tf1.getCodes();
		TextFragment tf2 = new TextFragment();
		tf2.setCodedText(codedText, codes);
		assertEquals(tf1.toText(), tf2.toText());
		assertEquals(fmt.setContent(tf1).toString(false), fmt.setContent(tf2).toString(false));
		assertNotSame(tf1, tf2);

		codes = null;
		codes = tf1.getCodes(0, 5); // xxAxxBxxC
		assertNotNull(codes);
		assertEquals(2, codes.size());
		assertEquals("[b]", codes.get(0).getData());
		assertEquals("[br/]", codes.get(1).getData());
	}

	@Test
	public void testHasText () {
		TextFragment tf1 = new TextFragment();
		assertFalse(tf1.hasText(true));
		assertFalse(tf1.hasText(false));
		tf1.append(TagType.PLACEHOLDER, "br", "<br/>");
		assertFalse(tf1.hasText(true));
		assertFalse(tf1.hasText(false));
		tf1.append('\t');
		assertTrue(tf1.hasText(true));
		assertFalse(tf1.hasText(false));
		tf1 = new TextFragment();
		tf1.append(TagType.PLACEHOLDER, "br", "<br/>");
		tf1.append('c');
		assertTrue(tf1.hasText(true));
		assertTrue(tf1.hasText(false));
	}
	
	@Test
	public void testGetText () {
		TextFragment frag = new TextFragment("watch out for ");
    	frag.append(TagType.OPENING, "b", "<b>");
    	frag.append("the killer");
    	frag.append(TagType.CLOSING, "b", "</b>");
    	frag.append(" rabbit");
    	assertEquals("watch out for the killer rabbit", frag.getText());
	}
	
	@Test
	public void testHasCode () {
		TextFragment tf1 = new TextFragment();
		assertFalse(tf1.hasCode());
		tf1.append('\t');
		assertFalse(tf1.hasCode());
		tf1.append('c');
		assertFalse(tf1.hasCode());
		tf1.append(TagType.PLACEHOLDER, "br", "<br/>");
		assertTrue(tf1.hasCode());
	}

	@Test
	public void testSubSequenceSimple () {
		TextFragment tf = new TextFragment("abc");
		assertEquals("a", tf.subSequence(0, 1).toText());
		assertEquals("b", tf.subSequence(1, 2).toText());
		assertEquals("c", tf.subSequence(2, 3).toText());
		assertEquals("bc", tf.subSequence(1, 3).toText());
		assertEquals("abc", tf.subSequence(0, -1).toText());
		TextFragment res = tf.subSequence(1, -1);
		assertEquals("bc", res.toText());
	}
	
	@Test
	public void testSubSequenceWithCodes () {
		TextFragment tf = makeFragment1();
		// [b]A[br/]B[/b]C == xxAxxBxxC
		//                    012345678
		assertEquals("[b]A", tf.subSequence(0, 3).toText());
		TextFragment res = tf.subSequence(3, -1);
		assertEquals("[br/]B[/b]C", res.toText());
		assertEquals(2, res.getCodes().size());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testSubSequenceError () { 
		TextFragment tf = new TextFragment("abc");
		tf.subSequence(3, 4);
	}

	@Test
	public void testCharAt () {
		TextFragment tf = new TextFragment("abc");
		assertEquals('a', tf.charAt(0));
		assertEquals('c', tf.charAt(2));
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testCharAtError () { 
		TextFragment tf = new TextFragment("");
		tf.charAt(1);
	}
	
	@Test
	public void testLengthSimple () {
		TextFragment tf = new TextFragment();
		assertEquals(0, tf.length());
		tf.append("abc");
		assertEquals(3, tf.length());
		assertEquals(tf.length(), tf.toText().length());
	}

	@Test
	public void testLengthWithCodes () {
		TextFragment tf = makeFragment1();
		// [b]A[br/]B[/b]C == xxAxxAxxC
		assertEquals(9, tf.length());
		assertEquals(15, tf.toText().length());
	}
	
	@Test
	public void testCharSequence () {
		TextFragment tf = new TextFragment("ABC");
		Pattern pat = Pattern.compile("[bB]");
		// Cast to make sure toString is not tested instead of CharSequence
		assertTrue(pat.matcher((CharSequence)tf).find());
		assertNotNull(new StringBuilder((CharSequence)tf)); 
	}
	
	@Test
	public void testToStringNoCodes () {
		TextFragment tf = new TextFragment("abc");
		assertEquals(tf.toText(), tf.toString()); // No codes so they are the same
	}

	@Test
	public void testToStringWithCodes () {
		TextFragment tf = makeFragment1();
		// [b]A[br/]B[/b]C == xxAxxAxxC
		assertFalse(tf.toText().equals(tf.toString())); // Codes so they are not the same
		assertEquals(tf.charAt(3), tf.toString().charAt(3));
	}

	@Test
	public void testAppendableSeparated () {
		TextFragment tf = new TextFragment();
		StringBuilder csq1 = new StringBuilder("bc");
		StringBuilder csq2 = new StringBuilder("[d]");
		tf.append('a');
		tf.append(csq1);
		tf.append(csq2, 1, 2);
		assertEquals("abcd", tf.toText());
	}
	
	@Test
	public void testAppendableTFInTF () {
		TextFragment tfA = new TextFragment("a");
		TextFragment tf1 = makeFragment1();
		TextFragment tfB = new TextFragment("b");
		assertEquals("a[b]A[br/]B[/b]Cb", tfA.append(tf1).append(tfB).toText());
	}
	
	@Test
	public void testAppendableTFInTFNested () {
		TextFragment tfA = new TextFragment("a");
		TextFragment tf1 = makeFragment1();
		TextFragment tfB = new TextFragment("b");
		assertEquals("a[b]A[br/]B[/b]Cb", tfA.append(tf1.append(tfB)).toText());
	}
	
	@Test
	public void testAppendableTFInSB () {
		TextFragment tf1 = makeFragment1();
		StringBuilder sb = new StringBuilder("xyz");
		TextFragment tfB = new TextFragment("b");
		sb.append(tf1.append(tfB));
		tf1.setCodedText(sb.toString());
		assertEquals("xyz[b]A[br/]B[/b]Cb", tf1.toText());
	}
	
	@Test
	public void testAppendableSelf () {
		TextFragment tf = makeFragment1();
		assertEquals("[b]A[br/]B[/b]C[b]A[br/]B[/b]C", tf.append(tf).toText());
		Code c1 = tf.codes.get(1);
		Code c2 = tf.codes.get(4);
		assertEquals(c1.toString(), c2.toString());
		assertFalse(c1==c2);
	}
	
	@Test
	public void testAppendableTogether () throws IOException {
		TextFragment tf = new TextFragment();
		StringBuilder csq1 = new StringBuilder("bc");
		StringBuilder csq2 = new StringBuilder("[d]");
		tf.append('a').append(csq1).append(csq2, 1, 2);
		assertEquals("abcd", tf.toText());
	}
	
	@Test
	public void testAppendableNull () throws IOException {
		TextFragment tf = new TextFragment();
		StringBuilder csq1 = null;
		tf.append('a').append(csq1).append(csq1, 1, 2);
		assertEquals("anullu", tf.toText());
	}
	
	@Test
	public void testTextCodesChanges () {
		TextFragment tf1 = new TextFragment("<b>New file:</b> %s");

		// Change the codes
		int diff = tf1.changeToCode(0, 3, TagType.OPENING, "b");
		diff += tf1.changeToCode(12+diff, 16+diff, TagType.CLOSING, "b");
		List<Code> list1 = tf1.getCodes();
		assertEquals("<b>", list1.get(0).getData());
		assertEquals("</b>", list1.get(1).getData());
		assertEquals("<b>New file:</b> %s", tf1.toText());
		assertEquals("<1>New file:</1> %s", fmt.setContent(tf1).toString(false));

		// Add an annotation: "%s" (use diff because %s is after both added codes) 
		tf1.annotate(17+diff, 19+diff, "protected", null);
		assertEquals("<b>New file:</b> %s", tf1.toText());
		list1 = tf1.getCodes();
		assertTrue(list1.get(2).hasAnnotation());
		assertTrue(list1.get(2).hasAnnotation("protected"));
		assertEquals("<b>New file:</b> %s", fmt.setContent(tf1).toString(true));
		assertEquals("<1>New file:</1> <2>%s</2>", fmt.setContent(tf1).toString(false));
		
		// Test if we can rebuild the annotation from the storage string
		String codesStorage1 = Code.codesToString(tf1.getCodes());
		String textStorage1 = tf1.getCodedText();
		assertNotNull(codesStorage1);
		assertNotNull(textStorage1);
		TextFragment tf2 = new TextFragment();
		tf2.setCodedText(textStorage1, Code.stringToCodes(codesStorage1));
		assertEquals(tf1.toText(), tf2.toText());
		List<Code> list2 = tf2.getCodes();
		assertTrue(list1.get(2).hasAnnotation());
		assertTrue(list1.get(2).hasAnnotation("protected"));
		assertEquals("<b>New file:</b> %s", fmt.setContent(tf2).toString(true));
		assertEquals("<1>New file:</1> <2>%s</2>", fmt.setContent(tf2).toString(false));

		// Add an annotation for "New" (don't use diff, correct manually: xxNew file:</b>)
		tf1.annotate(2, 5, "term", new InlineAnnotation("Nouveau"));
		assertEquals("<b>New file:</b> %s", fmt.setContent(tf1).toString(true));
		assertEquals("<1><3>New</3> file:</1> <2>%s</2>", fmt.setContent(tf1).toString(false));

		// Test start/end annotation and cloning
		InlineAnnotation annot1 = list1.get(4).getAnnotation("term"); // Opening
		InlineAnnotation annot2 = list1.get(5).getAnnotation("term"); // Closing
		assertSame(annot1, annot2);
		annot1.setData("new data"); // Check that changing in one, affects both
		assertEquals("new data", annot2.toString());
		annot2.setData("Nouveau"); // Check changing back
		assertEquals("Nouveau", annot1.toString());
		assertEquals(list1.get(4).getAnnotation("term").toString(), annot2.toString());
		// Check cloning
		Code c1 = list1.get(4);
		Code c2 = c1.clone();
		annot1 = c1.getAnnotation("term");
		annot2 = c2.getAnnotation("term");
		assertNotSame(annot1, annot2);
		
		// Test if we can rebuild the annotation from the storage string
		tf2 = new TextFragment();
		tf2.setCodedText(tf1.getCodedText(),
			Code.stringToCodes(Code.codesToString(tf1.getCodes())));
		assertEquals(tf1.toText(), tf2.toText());
		list2 = tf2.getCodes();
		assertTrue(list2.get(2).hasAnnotation());
		assertTrue(list2.get(2).hasAnnotation("protected"));
		assertTrue(list2.get(4).hasAnnotation("term"));
		InlineAnnotation annotation = list2.get(4).getAnnotation("term");
		assertEquals("Nouveau", annotation.getData());
		// Test annotation change
		annotation.setData("Neue");
		// Get the codes of tf1
		list1 = tf1.getCodes();
		// Check if the same annotation is now changed like in tf2:
		// It should not as tf2 is a clone.
		assertEquals("Nouveau", list1.get(4).getAnnotation("term").getData());
		assertEquals("Neue", list2.get(4).getAnnotation("term").getData());
		// Checks same annotation object after reading from string storage
		//TODO: Fix the storage issue!!! 
		//annot1 = list2.get(4).getAnnotation("term"); // Opening
		//annot2 = list2.get(5).getAnnotation("term"); // Closing
		//assertSame(annot1, annot2);
		
		// Test re-use of codes for adding annotations
		// Add annotations for "yyNewyy file:" xxyyNewyy file:xx
		tf1.annotate(2, 15, "mt", new InlineAnnotation("MT1"));
		tf1.annotate(2, 15, "term", new InlineAnnotation("TERM2"));
		// The added annotations should have used <1></1>
		assertEquals("<1><3>New</3> file:</1> <2>%s</2>", fmt.setContent(tf1).toString(false));
		list1 = tf1.getCodes();
		assertEquals("MT1", list1.get(0).getAnnotation("mt").getData());
		assertEquals("TERM2", list1.get(0).getAnnotation("term").getData());

		// Test spans
		List<AnnotatedSpan> spans = tf1.getAnnotatedSpans("term");
		assertEquals(2, spans.size());
		assertEquals("New file:", fmt.setContent(spans.get(0).span).toString(true));		
		assertEquals("<3>New</3> file:", fmt.setContent(spans.get(0).span).toString(false));		
		assertEquals("New", fmt.setContent(spans.get(1).span).toString(true));		
		assertEquals("New", fmt.setContent(spans.get(1).span).toString(false));
		assertEquals(2, spans.get(0).range.start);
		assertEquals(15, spans.get(0).range.end);
		assertEquals(4, spans.get(1).range.start);
		assertEquals(7, spans.get(1).range.end);
		
		// Test clearing the annotations
		assertTrue(tf1.hasAnnotation());
		// Clear annotations on <b>
		list1.get(0).removeAnnotations();
		assertTrue(tf1.hasAnnotation()); // Has still other annotations
		assertEquals("<1><3>New</3> file:</1> <2>%s</2>", fmt.setContent(tf1).toString(false));
		assertFalse(list1.get(0).hasAnnotation());
		// Clear annotation on <3>
		int n = list1.size();
		list1.get(4).removeAnnotations();
		// Should be same number of codes: clearing the code does not remove it
		assertEquals(n, list1.size());
		assertEquals("<1><3>New</3> file:</1> <2>%s</2>", fmt.setContent(tf1).toString(false));
		// Clear on the whole text
		tf1.removeAnnotations();
		assertFalse(tf1.hasAnnotation());
		assertEquals("<b>New file:</b> %s", fmt.setContent(tf1).toString(true));
		// Codes with annotations only should be removed by removeAnnotations()
		assertEquals("<1>New file:</1> %s", fmt.setContent(tf1).toString(false));
		
		// Check annotate behavior
		tf1 = new TextFragment("w1 ");
		tf1.append(TagType.OPENING, "b", "<b>");
		tf1.append("w2 w3");
		tf1.append(TagType.CLOSING, "b", "</b>");
		tf1.append(" w4 ");
		tf1.append(TagType.OPENING, "i", "<i>");
		tf1.append("w5 w6");
		tf1.append(TagType.CLOSING, "i", "</i>");
		tf1.append(" w7");
		assertEquals("w1 <1>w2 w3</1> w4 <2>w5 w6</2> w7", fmt.setContent(tf1).toString(false));
		// Annotate "<1>[w2 w3]</1>"
		tf1.annotate(5, 10, "a1", null);
		// Should be the same as annotation uses <1> and </1>
		assertTrue(tf1.hasAnnotation("a1"));
		assertEquals("w1 <1>w2 w3</1> w4 <2>w5 w6</2> w7", fmt.setContent(tf1).toString(false));
		// Annotate "[<1>w2 w3</1>]"
		tf1.annotate(3, 12, "a2", null);
		// Should be the same as annotation uses <1> and </1>
		assertTrue(tf1.hasAnnotation("a2"));
		//TODO: Re-use existing annotation markers, don't add new ones
		//assertEquals("w1 <1>w2 w3</1> w4 <2>w5 w6</2> w7", fmt.setContent(tf1).toString(false));
		// Annotate "<1>[w2] w3</1>]"
		tf1.annotate(5, 7, "a3", null);
		assertTrue(tf1.hasAnnotation("a3"));
		//TODO: Re-use existing annotation markers, don't add new ones
		//assertEquals("w1 <1><3>w2</3> w3</1> w4 <2>w5 w6</2> w7", fmt.setContent(tf1).toString(false));
		// Annotate "<1>w2 [w3]</1>]" (w1 xxyyw2yy w3xx)
		tf1.annotate(12, 14, "a4", null);
		assertTrue(tf1.hasAnnotation("a4"));
		//TODO: Re-use existing annotation markers, don't add new ones
		//assertEquals("w1 <1><3>w2</3> <4>w3</4></1> w4 <2>w5 w6</2> w7", fmt.setContent(tf1).toString(false));

		// Clear all annotations
		tf1.removeAnnotations();
		assertFalse(tf1.hasAnnotation());
		//TODO: Re-use existing annotation markers, don't add new ones
		//assertEquals("w1 <1>w2 w3</1> w4 <2>w5 w6</2> w7", fmt.setContent(tf1).toString(false));
		
		// Annotate "[w1 <1>w2] w3</1>"
		tf1.annotate(0, 7, "a5", null);
		spans = tf1.getAnnotatedSpans("a5");
		//TODO: Re-use existing annotation markers, don't add new ones
		//assertEquals(2, spans.size());
		//assertEquals("<3>w1 </3><1><4>w2</4> w3</1> w4 <2>w5 w6</2> w7", fmt.setContent(tf1).toString(false));

	}
	
	@Test(expected = InvalidPositionException.class)
    public void testGetCodedTextWithBadRange () {
		TextFragment tf = makeFragment1();
		tf.getCodedText(1, 3); // 1 is middle of first code
    }
	
	@Test
    public void testCompareToSameFragment () {
		TextFragment tf1 = new TextFragment("text of the fragment");
		TextFragment tf2 = new TextFragment("text of the fragment");
		assertEquals(0, tf1.compareTo(tf2));
		assertEquals(0, tf1.compareTo(tf2, true));
    }
	
	@Test
    public void testCompareToDifferentFragment () {
		TextFragment tf1 = new TextFragment("text of the fragment");
		TextFragment tf2 = new TextFragment("text Of The Fragment");
		assertFalse(0==tf1.compareTo(tf2));
		assertFalse(0==tf1.compareTo(tf2, true));
    }

	@Test
    public void testCompareToSameFragmentWithSameCodes () {
		TextFragment tf1 = makeFragment1();
		TextFragment tf2 = makeFragment1();
		assertEquals(0, tf1.compareTo(tf2, true));
    }
	
	@Test
    public void testCompareToWithNoCodesAndCodes () {
		TextFragment tf1 = makeFragment1();
		TextFragment tf2 = new TextFragment("ABC");
		assertEquals(0, tf1.compareTo(tf2, false));
		assertTrue(0!=tf1.compareTo(tf2, true));
    }
	
	@Test
	public void testCloneCodes () {
		TextFragment tf = makeFragment3();
		List<Code> list1 = tf.getCodes();
		List<Code> list2 = tf.getClonedCodes();
		assertEquals(list1.size(), list2.size());
		for ( int i=0; i<list1.size(); i++ ) {
			Code c1 = list1.get(i);
			Code c2 = list2.get(i);
			assertNotSame(c1, c2);
			assertEquals(c1.getId(), c2.getId());
			assertEquals(c1.getData(), c2.getData());
			assertEquals(c1.type, c2.getType());
		}
		
	}
	
	@Test
    public void testCompareToSameFragmentWithDifferentCodes () {
		TextFragment tf1 = makeFragment1();
		tf1.getCodes().get(0).setData("[zzz]");
		TextFragment tf2 = makeFragment1();
		assertTrue(0==tf1.compareTo(tf2, false));
		assertFalse(0==tf1.compareTo(tf2, true));
    }

	@Test
    public void testCompareWithSamePrefix() {
		TextFragment tf1 = new TextFragment("Message for ID name200");
		TextFragment tf2 = new TextFragment("Message for ID name200 (a nonmatch during diff)");
		assertFalse(0==tf1.compareTo(tf2));
		assertFalse(0==tf1.compareTo(tf2, true));
    }
	
	@Test
    public void testSynchronizeCodeIdentifiers () {
		TextFragment tf1 = makeFragment1();
		TextFragment tf2 = makeFragment2();
		tf2.alignCodeIds(tf1);
		FilterTestDriver.checkCodeData(tf1, tf2);
		assertEquals("<1>A<2/>B</1>C", fmt.setContent(tf1).toString(false));
		assertEquals("<2/>A<1>B</1>C", fmt.setContent(tf2).toString(false));
    }
	
	@Test
    public void testSynchronizeCodeIdentifiersComplex () {
		TextFragment tf1 = makeFragment1();
		TextFragment tf2 = makeFragment3();
		tf2.alignCodeIds(tf1);
		FilterTestDriver.checkCodeData(tf1, tf2);
		assertEquals("<1>A<2/>B</1>C", fmt.setContent(tf1).toString(false));
		assertEquals("<3><2/>A</3>B<1>C</1>D<4/>", fmt.setContent(tf2).toString(false));
    }
	
	@Test
    public void testSynchronizeCodeIdentifiersMoreComplex () {
		TextFragment tf1 = makeFragment1();
		TextFragment tf2 = makeFragment4();
		tf2.alignCodeIds(tf1);
		FilterTestDriver.checkCodeData(tf1, tf2);
		assertEquals("<1>A<2/>B</1>C", fmt.setContent(tf1).toString(false));
		assertEquals("<2/>A<3>B</3>C", fmt.setContent(tf2).toString(false));
    }
	
	@Test
    public void testSynchronizeCodeIdentifiersMoreComplex2 () {
		TextFragment tf1 = makeFragment1();
		TextFragment tf2 = makeFragment3();
		tf2.alignCodeIds(tf1);
		FilterTestDriver.checkCodeData(tf1, tf2);
		assertEquals("<1>A<2/>B</1>C", fmt.setContent(tf1).toString(false));
		assertEquals("<3><2/>A</3>B<1>C</1>D<4/>", fmt.setContent(tf2).toString(false));
    }

	@Test
    public void testSynchronizeCodeIdentifiersPlaceholderOnly () {
		TextFragment tf1 = makeFragment5();
		TextFragment tf2 = makeFragment6();
		tf2.alignCodeIds(tf1);
		FilterTestDriver.checkCodeData(tf1, tf2);
		assertEquals("<1/>A<2/>B<3/>C", fmt.setContent(tf1).toString(false));
		assertEquals("<2/>A<1/>B<3/>C<4/>", fmt.setContent(tf2).toString(false));
    }
	
	@Test
	public void testIndexOfNonWSEmpty () {
		assertEquals(-1, TextFragment.indexOfLastNonWhitespace("", -1, 0, true, true, true, true));
		assertEquals(-1, TextFragment.indexOfFirstNonWhitespace("", 0, -1, true, true, true, true));
	}

	@Test
	public void testIndexOfFirstNonWSSimple () {
		String text = " 12 4   ";
		assertEquals(1, TextFragment.indexOfFirstNonWhitespace(text, 0, -1, true, true, true, true));
		assertEquals(1, TextFragment.indexOfFirstNonWhitespace(text, 0, -1, true, true, true, true));
		assertEquals(1, TextFragment.indexOfFirstNonWhitespace(text, 1, -1, true, true, true, true));
		assertEquals(2, TextFragment.indexOfFirstNonWhitespace(text, 2, -1, true, true, true, true));
		assertEquals(4, TextFragment.indexOfFirstNonWhitespace(text, 3, -1, true, true, true, true));
		assertEquals(-1, TextFragment.indexOfFirstNonWhitespace(text, 5, -1, true, true, true, true));
		assertEquals(-1, TextFragment.indexOfFirstNonWhitespace(text, 5, 6, true, true, true, true));
	}

	@Test
	public void testIndexOfLastNonWSSimple () {
		String text = "  23 5  8";
		assertEquals(8, TextFragment.indexOfLastNonWhitespace(text, -1, 5, true, true, true, true));
		assertEquals(8, TextFragment.indexOfLastNonWhitespace(text, 8, 0, true, true, true, true));
		assertEquals(5, TextFragment.indexOfLastNonWhitespace(text, 7, 0, true, true, true, true));
		assertEquals(3, TextFragment.indexOfLastNonWhitespace(text, 4, 0, true, true, true, true));
		assertEquals(3, TextFragment.indexOfLastNonWhitespace(text, 3, 0, true, true, true, true));
		assertEquals(2, TextFragment.indexOfLastNonWhitespace(text, 2, 0, true, true, true, true));
		assertEquals(-1, TextFragment.indexOfLastNonWhitespace(text, 7, 6, true, true, true, true));
		assertEquals(-1, TextFragment.indexOfLastNonWhitespace(text, 1, 0, true, true, true, true));
	}
	
	@Test
	public void textIndexOfFirstNonWSEmpty () {
		TextFragment tf = new TextFragment("  ");
		tf.append(TagType.PLACEHOLDER, "x", "<x/>");
		tf.append(" ");
		tf.append(TagType.PLACEHOLDER, "x", "<x/>");
		tf.append(" ");
		String text = tf.getCodedText();
		// "  XX XX "
		// "01234567"
		assertEquals(-1, TextFragment.indexOfFirstNonWhitespace(text, 0, 0, true, true, true, true));
		assertEquals(-1, TextFragment.indexOfFirstNonWhitespace(text, 0, -1, true, true, true, true));
		// WS are not WS
		assertEquals(4, TextFragment.indexOfFirstNonWhitespace(text, 2, 0, true, true, true, false));
	}
	
	@Test
	public void testIndexOfLastNonWSEmpty () {
		TextFragment tf = new TextFragment("  ");
		tf.append(TagType.PLACEHOLDER, "x", "<x/>");
		tf.append(" ");
		tf.append(TagType.PLACEHOLDER, "x", "<x/>");
		tf.append(" ");
		String text = tf.getCodedText();
		// "  XX XX "
		// "01234567"
		assertEquals(-1, TextFragment.indexOfLastNonWhitespace(text, -1, 0, true, true, true, true));
		assertEquals(-1, TextFragment.indexOfLastNonWhitespace(text, 6, 0, true, true, true, true));
		assertEquals(-1, TextFragment.indexOfLastNonWhitespace(text, 7, 0, true, true, true, true));
		// WS are not WS
		assertEquals(7, TextFragment.indexOfLastNonWhitespace(text, -1, 0, true, true, true, false));
	}
	
	@Test
	public void testIndexOfFirstNonWSWithCodes () {
		TextFragment tf = new TextFragment("  ab");
		tf.append(TagType.PLACEHOLDER, "x", "<x/>");
		tf.append("c");
		tf.append(TagType.PLACEHOLDER, "x", "<x/>");
		tf.append(" ");
		tf.append(TagType.PLACEHOLDER, "x", "<x/>");
		String text = tf.getCodedText();
		// "  abXXcXX XX"
		// "012345678901"
		assertEquals(2, TextFragment.indexOfFirstNonWhitespace(text, 0, -1, true, true, true, true));
		assertEquals(2, TextFragment.indexOfFirstNonWhitespace(text, 0, -1, true, true, true, true));
		assertEquals(6, TextFragment.indexOfFirstNonWhitespace(text, 4, -1, true, true, true, true));
		assertEquals(-1, TextFragment.indexOfFirstNonWhitespace(text, 7, -1, true, true, true, true));
		// WS are not WS
		assertEquals(9, TextFragment.indexOfFirstNonWhitespace(text, 7, -1, true, true, true, false));
		// Placeholder codes are not WS
		assertEquals(7, TextFragment.indexOfFirstNonWhitespace(text, 7, -1, true, true, false, true));
		assertEquals(10, TextFragment.indexOfFirstNonWhitespace(text, 9, -1, true, true, false, true));
	}
	
	@Test
	public void testIndexOfLastNonWSWithCodes () {
		TextFragment tf = new TextFragment("  ab");
		tf.append(TagType.PLACEHOLDER, "x", "<x/>");
		tf.append("c");
		tf.append(TagType.PLACEHOLDER, "x", "<x/>");
		tf.append(" d");
		String text = tf.getCodedText();
		// "  abXXcXX d"
		// "01234567890"
		assertEquals(10, TextFragment.indexOfLastNonWhitespace(text, -1, 0, true, true, true, true));
		assertEquals(6, TextFragment.indexOfLastNonWhitespace(text, 9, 0, true, true, true, true));
		assertEquals(3, TextFragment.indexOfLastNonWhitespace(text, 5, 0, true, true, true, true));
		// WS are not WS
		assertEquals(9, TextFragment.indexOfLastNonWhitespace(text, 9, 0, true, true, true, false));
		// Placeholder codes are not WS
		assertEquals(8, TextFragment.indexOfLastNonWhitespace(text, 9, 0, true, true, false, true));
	}
	
	@Test
	public void testRemoveCode() {
		TextFragment f = makeFragment1();
		Code c = f.getCode(1);
		f.removeCode(c);
		assertEquals("[b]AB[/b]C", f.toText());
	}
	
	@Test
	public void testRenumberCodes() {
		
		// 11A1122AA2233B3344BB44		
		// 0123456789012345678901
		// 0         1         2
		
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "(1", "[(1]", 1);
		tf.append("A");		
		tf.append(TagType.CLOSING, "1)", "[1)]", 1);
		tf.append(TagType.OPENING, "(2", "[(2]", 2);
		tf.append("AA");
		tf.append(TagType.CLOSING, "2)", "[2)]", 2);
		tf.append(TagType.OPENING, "(3", "[(3]", 3);
		tf.append("B");		
		tf.append(TagType.CLOSING, "3)", "[3)]", 3);
		tf.append(TagType.OPENING, "(4", "[(4]", 4);
		tf.append("BB");
		tf.append(TagType.CLOSING, "4)", "[4)]", 4);
		tf.changeToCode(7, 9, TagType.PLACEHOLDER, "x");
		tf.changeToCode(18, 20, TagType.PLACEHOLDER, "x");
		
		GenericContent fmt = new GenericContent();
		fmt.setContent(tf);
		assertEquals("<b1/>A<e1/><b2/><5/><e2/><b3/>B<e3/><b4/><6/><e4/>", 
				fmt.toString());
		
		tf.renumberCodes();
		assertEquals("[(1]A[1)][(2]AA[2)][(3]B[3)][(4]BB[4)]", tf.toText());
		assertEquals("<b1/>A<e1/><b2/><3/><e2/><b4/>B<e4/><b5/><6/><e5/>", 
				//                                     |             |
				fmt.toString());
		assertEquals(10, tf.codes.size());
		
		assertEquals(1, tf.codes.get(0).id);
		assertEquals(1, tf.codes.get(1).id);
		assertEquals(2, tf.codes.get(2).id);
		assertEquals(3, tf.codes.get(3).id);
		assertEquals(2, tf.codes.get(4).id);
		assertEquals(4, tf.codes.get(5).id);
		assertEquals(4, tf.codes.get(6).id);
		assertEquals(5, tf.codes.get(7).id);
		assertEquals(5, tf.codes.get(8).id);
		assertEquals(6, tf.codes.get(9).id);
		
		fmt = new GenericContent();
		fmt.setContent(tf);
		assertEquals("<b1/>A<e1/><b2/><3/><e2/><b4/>B<e4/><b5/><6/><e5/>", fmt.toString());
		assertEquals("<b1/>A<e1/><b2/><x3/><e2/><b4/>B<e4/><b5/><x6/><e5/>", GenericContent.fromFragmentToLetterCoded(tf, true));
		
		tf = new TextFragment();
		tf.append(TagType.OPENING, "(4", "[(4]", 4);
		tf.append("A");		
		tf.append(TagType.CLOSING, "4)", "[4)]", 4);
		tf.append(TagType.OPENING, "(2", "[(2]", 2);
		tf.append("AA");
		tf.append(TagType.CLOSING, "2)", "[2)]", 2);
		tf.append(TagType.OPENING, "(1", "[(1]", 1);
		tf.append("B");		
		tf.append(TagType.CLOSING, "1)", "[1)]", 1);
		tf.append(TagType.OPENING, "(3", "[(3]", 3);
		tf.append("BB");
		tf.append(TagType.CLOSING, "3)", "[3)]", 3);
		tf.changeToCode(7, 9, TagType.PLACEHOLDER, "x");
		tf.changeToCode(18, 20, TagType.PLACEHOLDER, "x");
		
		fmt = new GenericContent();
		fmt.setContent(tf);
		assertEquals("<b4/>A<e4/><b2/><5/><e2/><b1/>B<e1/><b3/><6/><e3/>", 
				fmt.toString());
		
		tf.renumberCodes();
		assertEquals("[(4]A[4)][(2]AA[2)][(1]B[1)][(3]BB[3)]", tf.toText());
		assertEquals("<b1/>A<e1/><b2/><3/><e2/><b4/>B<e4/><b5/><6/><e5/>", 
				//                                     |             |
				fmt.toString());
		assertEquals(10, tf.codes.size());
		
		assertEquals(1, tf.codes.get(0).id);
		assertEquals(1, tf.codes.get(1).id);
		assertEquals(2, tf.codes.get(2).id);
		assertEquals(3, tf.codes.get(3).id);
		assertEquals(2, tf.codes.get(4).id);
		assertEquals(4, tf.codes.get(5).id);
		assertEquals(4, tf.codes.get(6).id);
		assertEquals(5, tf.codes.get(7).id);
		assertEquals(5, tf.codes.get(8).id);
		assertEquals(6, tf.codes.get(9).id);
		
		fmt = new GenericContent();
		fmt.setContent(tf);
		assertEquals("<b1/>A<e1/><b2/><3/><e2/><b4/>B<e4/><b5/><6/><e5/>", fmt.toString());
		assertEquals("<b1/>A<e1/><b2/><x3/><e2/><b4/>B<e4/><b5/><x6/><e5/>", GenericContent.fromFragmentToLetterCoded(tf, true));
	}

	@Test
	public void testManyNestedCodes1() {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("Content");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "b", "</b>");
		assertEquals("<b><b><b><b><b><b><b><b><b><b><b>Content</b></b></b></b></b></b></b></b></b></b></b>", tf.toText());
		assertEquals("<1><2><3><4><5><6><7><8><9><10><11>Content</11></10></9></8></7></6></5></4></3></2></1>", fmt.setContent(tf).toString());
		String ct = tf.getCodedText();
		assertEquals(TextFragment.MARKER_OPENING, ct.charAt(0));
		assertEquals(TextFragment.MARKER_OPENING, ct.charAt(2));
		assertEquals(TextFragment.MARKER_OPENING, ct.charAt(4));
		assertEquals(TextFragment.MARKER_OPENING, ct.charAt(6));
		assertEquals(TextFragment.MARKER_OPENING, ct.charAt(8));
		assertEquals(TextFragment.MARKER_OPENING, ct.charAt(10));
		assertEquals(TextFragment.MARKER_OPENING, ct.charAt(12));
		assertEquals(TextFragment.MARKER_OPENING, ct.charAt(14));
		assertEquals(TextFragment.MARKER_OPENING, ct.charAt(16));
		assertEquals(TextFragment.MARKER_OPENING, ct.charAt(18));
		assertEquals(TextFragment.MARKER_OPENING, ct.charAt(20));
		// Content goes here
		assertEquals(TextFragment.MARKER_CLOSING, ct.charAt(29));
		assertEquals(TextFragment.MARKER_CLOSING, ct.charAt(31));
		assertEquals(TextFragment.MARKER_CLOSING, ct.charAt(33));
		assertEquals(TextFragment.MARKER_CLOSING, ct.charAt(35));
		assertEquals(TextFragment.MARKER_CLOSING, ct.charAt(37));
		assertEquals(TextFragment.MARKER_CLOSING, ct.charAt(39));
		assertEquals(TextFragment.MARKER_CLOSING, ct.charAt(41));
		assertEquals(TextFragment.MARKER_CLOSING, ct.charAt(43));
		assertEquals(TextFragment.MARKER_CLOSING, ct.charAt(45));
		assertEquals(TextFragment.MARKER_CLOSING, ct.charAt(47));
		assertEquals(TextFragment.MARKER_CLOSING, ct.charAt(49));
	}

	@Test
	public void testManyNestedCodes2 () {
		TextFragment tf = new TextFragment();
		tf.append("Content"); // the deepest child
		for ( int i=0; i<10; i++ ) {
			TextFragment tf2 = new TextFragment();
			tf2.append(TagType.OPENING, "b"+i, "<b>");
			tf2.append(tf);
			tf2.append(TagType.CLOSING, "b"+i, "</b>");
			tf = tf2;
		}
		assertEquals("<1><2><3><4><5><6><7><8><9><10>Content</10></9></8></7></6></5></4></3></2></1>", fmt.setContent(tf).toString());
	}	

	@Test
	public void testManyNestedCodes3 () {
		TextFragment tf = new TextFragment();
		tf.append("Content"); // the deepest child
		for ( int i=0; i<10; i++ ) {
			TextFragment tf2 = new TextFragment();
			tf2.append(TagType.OPENING, "b", "<b>", 10-i);
			tf.insert(0, tf2);
			tf2 = new TextFragment();
			tf2.append(TagType.CLOSING, "b", "</b>", 10-i);
			tf.insert(-1, tf2);
		}
		assertEquals("<1><2><3><4><5><6><7><8><9><10>Content</10></9></8></7></6></5></4></3></2></1>", fmt.setContent(tf).toString());
	}	

	@Test
	public void testNormalizeCodeIds () {
		TextFragment tf = new TextFragment();
		Code code = new Code(TagType.OPENING, "x");
		code.setId(8);
		tf.append(code);
		code = new Code(TagType.CLOSING, "x");
		code.setId(8);
		tf.append(code);
		code = new Code(TagType.OPENING, "x");
		code.setId(5);
		tf.append(code);
		code = new Code(TagType.PLACEHOLDER, "x");
		code.setId(2);
		tf.append(code);
		code = new Code(TagType.CLOSING, "x");
		code.setId(5);
		tf.append(code);
		
		assertEquals("<8></8><5><2/></5>", fmt.setContent(tf).toString());
		assertEquals(3, tf.renumberCodes(1, false));
		assertEquals("<3></3><2><1/></2>", fmt.setContent(tf).toString());
		assertEquals(9, tf.renumberCodes(7, false));
		assertEquals("<9></9><8><7/></8>", fmt.setContent(tf).toString());
		assertEquals(3, tf.renumberCodes(1));
		assertEquals("<1></1><2><3/></2>", fmt.setContent(tf).toString());		
		assertEquals(89, tf.renumberCodes(87));
		assertEquals("<87></87><88><89/></88>", fmt.setContent(tf).toString());
	}

	@Test
	public void testBalanceMarkers() throws Exception {
		TextFragment tf = new TextFragment();

		Code code = new Code(TagType.OPENING, "tag");
		code.setId(1);
		tf.append(code);

		code = new Code(TagType.OPENING, "tag");
		code.setId(1);
		tf.append(code);

		code = new Code(TagType.CLOSING, "tag");
		code.setId(1);
		tf.append(code);

		code = new Code(TagType.CLOSING, "tag");
		code.setId(1);
		tf.append(code);

		tf.balanceMarkers();

		assertThat(tf.getCodedText(), equalTo("\uE101\uE110\uE101\uE111\uE102\uE112\uE102\uE113"));
	}

	@Test(expected = OkapiIllegalFilterOperationException.class)
	public void testTooManyCodes() {
		TextFragment tf = new TextFragment();
		for (int i = 0; i < 7920; i++) {
				tf.append(new Code(TagType.PLACEHOLDER, "x"));
		}
		tf.balanceMarkers();
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
	 * Makes a fragment <code>[br/]A[b]B[/b]C<code>
	 * @return the new fragment.
	 */
	private TextFragment makeFragment2 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "br", "[br/]");
		tf.append("A");
		tf.append(TagType.OPENING, "b", "[b]");
		tf.append("B");
		tf.append(TagType.CLOSING, "b", "[/b]");
		tf.append("C");
		return tf;
	}

	/**
	 * Makes a fragment <code>[u][br/]A[/u]B[b]C[/b]D[br/]<code>
	 * @return the new fragment.
	 */
	private TextFragment makeFragment3 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "u", "[u]");
		tf.append(TagType.PLACEHOLDER, "br", "[br/]");
		tf.append("A");
		tf.append(TagType.CLOSING, "u", "[/u]");
		tf.append("B");
		tf.append(TagType.OPENING, "b", "[b]");
		tf.append("C");
		tf.append(TagType.CLOSING, "b", "[/b]");
		tf.append("D");
		tf.append(TagType.PLACEHOLDER, "br", "[br/]");
		return tf;
	}

	/**
	 * Makes a fragment <code>[br/]A[u]B[/u]C<code>
	 * @return the new fragment.
	 */
	private TextFragment makeFragment4 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "br", "[br/]");
		tf.append("A");
		tf.append(TagType.OPENING, "u", "[u]");
		tf.append("B");
		tf.append(TagType.CLOSING, "u", "[/u]");
		tf.append("C");
		return tf;
	}

	
	/**
	 * Makes a fragment <code>{1}A{2}B{3}C<code>
	 * @return the new fragment.
	 */
	private TextFragment makeFragment5 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "1", "{1}");
		tf.append("A");
		tf.append(TagType.PLACEHOLDER, "2", "{2}");
		tf.append("B");
		tf.append(TagType.PLACEHOLDER, "3", "{3}");
		tf.append("C");
		return tf;
	}
	
	/**
	 * Makes a fragment <code>{2}A{1}B{3}C<code>
	 * @return the new fragment.
	 */
	private TextFragment makeFragment6 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "2", "{2}");
		tf.append("A");
		tf.append(TagType.PLACEHOLDER, "1", "{1}");
		tf.append("B");
		tf.append(TagType.PLACEHOLDER, "3", "{3}");
		tf.append("C");
		tf.append(TagType.PLACEHOLDER, "4", "{4}");
		return tf;
	}
}
