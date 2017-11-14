/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class QueryUtilTest {

	private QueryUtil qu;
	private GenericContent fmt;
	
	@Before
	public void setUp() {
		qu = new QueryUtil();
		fmt = new GenericContent();
	}

	@Test
	public void testEmptyFragment () {
		TextFragment tf = new TextFragment();
		assertEquals("", qu.separateCodesFromText(tf));
		assertEquals("", qu.createNewFragmentWithCodes("").toText());
	}
	
	@Test
	public void testFragmentWithoutCodes () {
		TextFragment tf = new TextFragment("text");
		assertEquals("text", qu.separateCodesFromText(tf));
		assertEquals("new", qu.createNewFragmentWithCodes("new").toText());
	}
	
	@Test
	public void testFragmentWithCodes () {
		TextFragment tf = makeFragment();
		assertEquals("a & < > \" \' <b>bold</b> t <br/> z <i> q", tf.toText());
		assertEquals("a & < > \" \' bold t  z  q", qu.separateCodesFromText(tf));
		assertEquals("new<b></b><br/><i>", qu.createNewFragmentWithCodes("new").toText());
	}
	
	@Test
	public void testToHTML () {
		TextFragment tf = makeFragment();
		String htmlText = qu.toCodedHTML(tf);
		assertEquals("a &amp; &lt; > \" \' <u id='1'>bold</u> t <br id='p2'/> z <br id='b3'/> q", htmlText);
	}
	
	@Test
	public void testFromSameHTML () {
		TextFragment tf = makeFragment();
		String htmlText = qu.toCodedHTML(tf);
		String codedText = qu.fromCodedHTML(htmlText, tf, true);
		TextFragment resFrag = new TextFragment(codedText, tf.getCodes());
		assertTrue(resFrag.compareTo(tf, false) == 0);
		assertTrue(resFrag.compareTo(tf, true) == 0);
	}
	
	@Test
	public void testFromModifiedHTML () {
		TextFragment tf = makeFragment();
		String htmlText = qu.toCodedHTML(tf);
		String codedText = qu.fromCodedHTML(htmlText, tf, true);
		codedText = codedText.toUpperCase();
		TextFragment resFrag = new TextFragment(codedText, tf.getCodes());
		assertEquals("A & < > \" \' <b>BOLD</b> T <br/> Z <i> Q", resFrag.toText());
	}
	
	@Test
	public void testNewTextFragmentFromSameHTML () {
		TextFragment tf = makeFragment();
		String htmlText = qu.toCodedHTML(tf);
		TextFragment resFrag = qu.fromCodedHTMLToFragment(htmlText, null);
		assertTrue(resFrag.compareTo(tf, false) == 0);
	}
	
	@Test
	public void testSimpleHTMLWithCorrection () {
		TextFragment tf = makeFragment();
		String htmlText = qu.toCodedHTML(tf);
		assertEquals("a &amp; &lt; > \" \' <u id='1'>bold</u> t <br id='p2'/> z <br id='b3'/> q", htmlText);
		// Send something with missing codes (faking translation results)
		htmlText = "a <u id='1'>b</u> c";
		String codedText = qu.fromCodedHTML(htmlText, tf, true);
		TextFragment resFrag = new TextFragment(codedText, tf.getCodes());
		assertEquals("a <1>b</1> c<2/><b3/>", fmt.setContent(resFrag).toString());
		assertEquals("a <b>b</b> c<br/><i>", resFrag.toText());
	}
	
	@Test
	public void testFromSameHTMLComplex () {
		TextFragment tf = makeComplexFragment();
		String htmlText = qu.toCodedHTML(tf);
		String codedText = qu.fromCodedHTML(htmlText, tf, true);
		TextFragment resFrag = new TextFragment(codedText, tf.getCodes());
		assertEquals("t1<1><2>bs1</2></1>t2<3>b1</3>", fmt.setContent(resFrag).toString());
	}
	
	@Test
	public void testFromHTMLComplexWithMovedCodes () {
		TextFragment tf = makeComplexFragment();
		String htmlText = qu.toCodedHTML(tf);
		// What we send:
		assertEquals("t1<u id='1'><u id='2'>bs1</u></u>t2<u id='3'>b1</u>", htmlText);
		// What we get back from the translation:
		htmlText = "t1<u id='2'><u id='3'><u id='1'>t2</u></u>t3</u>";
		String codedText = qu.fromCodedHTML(htmlText, tf, true);
		TextFragment resFrag = new TextFragment(codedText, tf.getCodes());
		assertEquals("t1<2><3><1>t2</1></3>t3</2>", fmt.setContent(resFrag).toString());
		assertEquals("t1<u><b><b>t2</b></b>t3</u>", resFrag.toText());
	}
	
	@Test
	public void testFromHTMLComplexWithMovedCodesWithCorrection () {
		TextFragment tf = makeComplexFragment();
		String htmlText = qu.toCodedHTML(tf);
		// What we send:
		assertEquals("t1<u id='1'><u id='2'>bs1</u></u>t2<u id='3'>b1</u>", htmlText);
		// What we get back from the translation:
		htmlText = "t1<u id='2'><u id='1'>t2</u>t3</u>";
		String codedText = qu.fromCodedHTML(htmlText, tf, true);
		TextFragment resFrag = new TextFragment(codedText, tf.getCodes());
		assertEquals("t1<2><1>t2</1>t3</2><3></3>", fmt.setContent(resFrag).toString());
		assertEquals("t1<u><b>t2</b>t3</u><b></b>", resFrag.toText());
	}
	
	@Test
	public void testWithEscapes () {
		TextFragment tf = makeFragment();
		String htmlText = qu.toCodedHTML(tf);
		htmlText = htmlText + "&aacute;&amp;&#39;&#x0152;&apos;";
		String codedText = qu.fromCodedHTML(htmlText, tf, true);
		TextFragment resFrag = new TextFragment(codedText, tf.getCodes());
		assertEquals("a & < > \" \' <b>bold</b> t <br/> z <i> q\u00e1&'\u0152'", resFrag.toText());
	}
	
	private TextFragment makeFragment () {
		TextFragment tf = new TextFragment("a & < > \" \' ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("bold");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(" t ");
		tf.append(TagType.PLACEHOLDER, null, "<br/>");
		tf.append(" z ");
		tf.append(TagType.OPENING, "i", "<i>");
		tf.append(" q");
		return tf;
	}

	private TextFragment makeComplexFragment () {
		TextFragment tf = new TextFragment("t1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "s", "<u>");
		tf.append("bs1");
		tf.append(TagType.CLOSING, "s", "</u>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("t2");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("b1");
		tf.append(TagType.CLOSING, "b", "</b>");
		return tf;
	}

}
