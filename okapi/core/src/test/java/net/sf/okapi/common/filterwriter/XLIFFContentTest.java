/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XLIFFContentTest {

	private XLIFFContent fmt;

	@Before
	public void setUp() throws Exception {
		fmt = new XLIFFContent();
	}
	
	@Test
	public void testSimpleDefault () {
		TextFragment tf = createTextFragment();
		assertEquals(5, tf.getCodes().size());
		assertEquals("t1<bpt id=\"1\">&lt;b1&gt;</bpt><bpt id=\"2\">&lt;b2&gt;</bpt><ph id=\"3\">{\\x1\\}</ph>t2<ept id=\"2\">&lt;/b2&gt;</ept><ept id=\"1\">&lt;/b1&gt;</ept>t3",
			fmt.setContent(tf).toString());
	}
	
	@Test
	public void testSimpleGX () {
		TextFragment tf = createTextFragment();
		assertEquals(5, tf.getCodes().size());
		assertEquals("t1<g id=\"1\"><g id=\"2\"><x id=\"3\"/>t2</g></g>t3",
			fmt.setContent(tf).toString(true));
	}

	@Test
	public void testMisOrderedGX1 () {
		TextFragment tf = createMisOrderedTextFragment1();
		assertEquals(4, tf.getCodes().size());
		assertEquals("t1<bx id=\"1\"/>t2<bx id=\"2\"/>t3<ex id=\"1\"/>t4<ex id=\"2\"/>t5",
			fmt.setContent(tf).toString(true));
	}
	
	@Test
	public void testMisOrderedGX2 () {
		TextFragment tf = createMisOrderedTextFragment2();
		assertEquals(4, tf.getCodes().size());
		assertEquals("<ex id=\"3\"/><g id=\"1\"></g><bx id=\"2\"/>",
			fmt.setContent(tf).toString(true));
	}
	
	@Test
	public void testMisOrderedComplexGX () {
		TextFragment tf = createMisOrderedComplexFragmentUnit();
		assertEquals(8, tf.getCodes().size());
		assertEquals("<bx id=\"1\"/><bx id=\"2\"/><g id=\"3\"></g><ex id=\"1\"/><bx id=\"4\"/><ex id=\"2\"/><ex id=\"4\"/>",
			fmt.setContent(tf).toString(true));
	}
	
	@Test
	public void testMisOrderedComplexBPT () {
		TextFragment tf = createMisOrderedComplexFragmentUnit();
		assertEquals(8, tf.getCodes().size());
		assertEquals("<it id=\"1\" pos=\"open\">&lt;b1&gt;</it><it id=\"2\" pos=\"open\">&lt;b2&gt;</it><bpt id=\"3\">&lt;b2&gt;</bpt><ept id=\"3\">&lt;/b2&gt;</ept><it id=\"1\" pos=\"close\">&lt;/b1&gt;</it><it id=\"4\" pos=\"open\">&lt;b3&gt;</it><it id=\"2\" pos=\"close\">&lt;/b2&gt;</it><it id=\"4\" pos=\"close\">&lt;/b3&gt;</it>",
			fmt.setContent(tf).toString(false));
	}

	@Test
	public void testTextAnalysisAnnotationAndLocNote () {
		GenericAnnotations anns = new GenericAnnotations();
		GenericAnnotation ga = anns.add(GenericAnnotationType.TA);
		ga.setString(GenericAnnotationType.TA_SOURCE, "src");
		TextFragment tf = new TextFragment("Before the span after.");
		//                                  0123456789012345678901
		int diff = tf.annotate(7, 15, GenericAnnotationType.GENERIC, anns);
		tf.annotate(7+(diff/2), 15+diff, GenericAnnotationType.GENERIC,
			new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.LOCNOTE,
				GenericAnnotationType.LOCNOTE_VALUE, "comment",
				GenericAnnotationType.LOCNOTE_TYPE, "alert")));
		assertEquals("Before <mrk its:taSource=\"src\" mtype=\"phrase\">"
			+ "<mrk comment=\"comment\" itsxlf:locNoteType=\"alert\" mtype=\"x-its\">the span</mrk></mrk> after.",
			fmt.setContent(tf).toString(true));
	}

	@Test
	public void testVariousAnnotations () {
		GenericAnnotations anns = new GenericAnnotations();
		anns.add(new GenericAnnotation(GenericAnnotationType.ALLOWEDCHARS,
			GenericAnnotationType.ALLOWEDCHARS_VALUE, "[a-z]"));
		anns.add(new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
			GenericAnnotationType.STORAGESIZE_SIZE, 25,
			GenericAnnotationType.STORAGESIZE_ENCODING, "iso-8859-1",
			GenericAnnotationType.STORAGESIZE_LINEBREAK, "cr"));
		anns.add(new GenericAnnotation(GenericAnnotationType.TERM,
			GenericAnnotationType.TERM_CONFIDENCE, 0.50,
			GenericAnnotationType.TERM_INFO, "REF:myUri"));
		anns.add(new GenericAnnotation(GenericAnnotationType.LQI,
			GenericAnnotationType.LQI_TYPE, "grammar",
			GenericAnnotationType.LQI_COMMENT, "blah",
			GenericAnnotationType.LQI_SEVERITY, 98.5));
		anns.add(new GenericAnnotation(GenericAnnotationType.LANG,
			GenericAnnotationType.LANG_VALUE, "zh"));
		anns.add(new GenericAnnotation(GenericAnnotationType.PRESERVEWS,
			GenericAnnotationType.PRESERVEWS_INFO, "preserve"));
		TextFragment tf = new TextFragment("Before the span after.");
		tf.annotate(7, 15, GenericAnnotationType.GENERIC, anns);
		assertEquals("Before <mrk its:allowedCharacters=\"[a-z]\""
			+ " its:storageSize=\"25\" its:storageEncoding=\"iso-8859-1\" its:storageLinebreak=\"cr\""
			+ " itsxlf:termConfidence=\"0.5\" itsxlf:termInfoRef=\"myUri\""
			+ " xml:lang=\"zh\""
			+ " xml:space=\"preserve\""
			+ " its:locQualityIssueComment=\"blah\" its:locQualityIssueSeverity=\"98.5\" its:locQualityIssueType=\"grammar\""
			+ " mtype=\"term\">the span</mrk> after.",
			fmt.setContent(tf).toString(true));
	}

	@Test
	public void testmultipleLQI () {
		TextFragment tf = new TextFragment("Span 1 Span 2");
		//                                  0123456789012
		// First LQI
		GenericAnnotations anns = new GenericAnnotations();
		anns.setData("id1");
		anns.add(new GenericAnnotation(GenericAnnotationType.LQI,
			GenericAnnotationType.LQI_COMMENT, "comment-1a"));
		anns.add(new GenericAnnotation(GenericAnnotationType.LQI,
			GenericAnnotationType.LQI_COMMENT, "comment-1b"));
		tf.annotate(0, 6, GenericAnnotationType.GENERIC, anns);
		// second LQI (no ID defined)
		anns = new GenericAnnotations();
		anns.add(new GenericAnnotation(GenericAnnotationType.LQI,
			GenericAnnotationType.LQI_COMMENT, "comment-2a"));
		anns.add(new GenericAnnotation(GenericAnnotationType.LQI,
			GenericAnnotationType.LQI_COMMENT, "comment-2b"));
		tf.annotate(11, 17, GenericAnnotationType.GENERIC, anns); // +4 is for first marker
		
		assertEquals("<mrk its:locQualityIssuesRef=\"#VARID\" mtype=\"x-its\">Span 1</mrk> "
			+ "<mrk its:locQualityIssuesRef=\"#VARID\" mtype=\"x-its\">Span 2</mrk>",
			stripVariableID(fmt.setContent(tf).toString(true)));
	}

	@Test
	public void testAnnotationOnOriginalCode () {
		// Original text is with an ITS data category
		TextFragment tf = new TextFragment("Before ");
		Code start = tf.append(TagType.OPENING, "span", "<its:span allowedCharacters='[a-z]'>");
		tf.append("the span");
		Code end = tf.append(TagType.CLOSING, "span", "</its:span>");
		tf.append(" after.");
		// And we have a corresponding annotation
		GenericAnnotations anns = new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.ALLOWEDCHARS,
				GenericAnnotationType.ALLOWEDCHARS_VALUE, "[a-z]"));
		GenericAnnotations.addAnnotations(start, anns);
//TODO: We have to find a better way to attach annotation on span		
		GenericAnnotations.addAnnotations(end, anns);
		// Output
		assertEquals("Before <g id=\"1\"><mrk its:allowedCharacters=\"[a-z]\" mtype=\"x-its\">the span</mrk></g> after.",
			fmt.setContent(tf).toString(1, true, false, true, false, true, LocaleId.FRENCH));
	}

	@Test
	public void testCodeExtendedAttributes () {
		TextFragment tf = new TextFragment("Beginning ");
		Code img = tf.append(TagType.PLACEHOLDER, "image", "<img/>");
		img.setDisplayText("[hint for user]");
		tf.append(" middle ");
		tf.append(TagType.PLACEHOLDER, "other", "<other/>");
		tf.append(" end.");

		assertEquals("Beginning <x id=\"1\" ctype=\"image\" equiv-text=\"[hint for user]\"/> middle <x id=\"2\" ctype=\"x-other\" equiv-text=\"&lt;other/>\"/> end.",
				fmt.setContent(tf).toString(1, true, false, true, true, true, LocaleId.FRENCH));
	}
	
	private TextFragment createTextFragment () {
		TextFragment tf = new TextFragment();
		tf.append("t1");
		tf.append(TagType.OPENING, "b1", "<b1>");
		tf.append(TagType.OPENING, "b2", "<b2>");
		tf.append(TagType.PLACEHOLDER, "x1", "{\\x1\\}");
		tf.append("t2");
		tf.append(TagType.CLOSING, "b2", "</b2>");
		tf.append(TagType.CLOSING, "b1", "</b1>");
		tf.append("t3");
		return tf;
	}
	
	private TextFragment createMisOrderedTextFragment1 () {
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
		return tf;
	}

	private TextFragment createMisOrderedTextFragment2 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.CLOSING, "b1", "</b1>");
		tf.append(TagType.OPENING, "b2", "<b2>");
		tf.append(TagType.CLOSING, "b2", "</b2>");
		tf.append(TagType.OPENING, "b3", "<b3>");
		return tf;
	}

	private TextFragment createMisOrderedComplexFragmentUnit () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "b1", "<b1>");
		tf.append(TagType.OPENING, "b2", "<b2>");
		tf.append(TagType.OPENING, "b2", "<b2>");
		tf.append(TagType.CLOSING, "b2", "</b2>");
		tf.append(TagType.CLOSING, "b1", "</b1>");
		tf.append(TagType.OPENING, "b3", "<b3>");
		tf.append(TagType.CLOSING, "b2", "</b2>");
		tf.append(TagType.CLOSING, "b3", "</b3>");
		return tf;
	}

	private String stripVariableID (String text) {
		text = text.replaceAll("locQualityIssuesRef=\"#(.*?)\"", "locQualityIssuesRef=\"#VARID\""); 
		return text;
	}
	
}
