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

package net.sf.okapi.filters.php;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PHPContentFilterTest {
	
	private PHPContentFilter filter;
	private String root;
	private GenericContent fmt;
	private LocaleId locEN = LocaleId.fromString("en");

	@Before
	public void setUp() {
		filter = new PHPContentFilter();
		root = TestUtil.getParentDir(this.getClass(), "/test01.phpcnt");
		fmt = new GenericContent();
	}

//	@Test
//	public void testDefine () {
//		String snippet = "define('myconst', 'text');";
//		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
//		assertTrue(tu!=null);
//		assertEquals("text", tu.getSource().toString());
//	}
	
//	@Test
//	public void testArrayDeclarations () {
//		String snippet = "$arr=array('key1' => 'text1', 'key2' => 'text2');";
//		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
//		assertTrue(tu!=null);
//		assertEquals("text2", tu.getSource().toString());
//	}

//	@Test
//	public void testArrayDeclarationsNoKeys () {
//		String snippet = "$arr=array('text1', 'text2');";
//		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
//		assertTrue(tu!=null);
//		assertEquals("text2", tu.getSource().toString());
//	}

//	@Test
//	public void testArrayDeclarationsMixed () {
//		String snippet = "$arr=array('text1', 'key2' => 'text2');";
//		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
//		assertTrue(tu!=null);
//		assertEquals("text1", tu.getSource().toString());
//	}

//	@Test
//	public void testRequireOnceFunction () {
//		String snippet = "require_once('file.php'); $a='text';";
//		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
//		assertTrue(tu!=null);
//		assertEquals("text", tu.getSource().toString());
//	}
	
//	@Test
//	public void testRequireFunction () {
//		String snippet = "require('file.php'); $a='text';";
//		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
//		assertTrue(tu!=null);
//		assertEquals("text", tu.getSource().toString());
//	}
	
//	@Test
//	public void testIncludeFunction () {
//		String snippet = "include('file.php'); $a='text';";
//		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
//		assertTrue(tu!=null);
//		assertEquals("text", tu.getSource().toString());
//	}
	
//	@Test //TODO later: case where array index is composite
//	public void testArrayIndexWithVariable () {
//		String snippet = "$a[$b.'skip'] = 'text';";
//		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
//		assertTrue(tu!=null);
//		assertEquals("text", tu.getSource().toString());
//	}
	
	@Test
	public void testDefaultInfo () {
		assertNotNull(filter.getParameters());
		assertNotNull(filter.getName());
		List<FilterConfiguration> list = filter.getConfigurations();
		assertNotNull(list);
		assertTrue(list.size()>0);
	}
	
	@Test
	public void testEntityReferences () {
		String snippet = "$a='&aacute;&#xC1;&#225;&#x00c1;';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("\u00e1\u00c1\u00e1\u00c1", tu.getSource().toString());
	}
	
	@Test
	public void testReferencesLooklike () {
		String snippet = "$a='& &; &#; &aacute';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("& &; &#; &aacute", tu.getSource().toString());
	}
	
	@Test
	public void testConcatSQStrings () {
		String snippet = "$a='t1' \r. 't2';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("t1' \r. 't2", tu.getSource().toString());
		List<Code> codes = tu.getSource().getFirstContent().getCodes();
		assertEquals(1, codes.size());
		assertEquals("' \r. '", codes.get(0).toString());
		assertEquals("x-singlequoted", tu.getType());
	}
	
	@Test
	public void testConcatDQStringsWithCodesAndVariable () {
		String snippet = "$a=\"t1<b>\".$_CONFIG[\"site\"].\"</b>t2\";";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("t1<b>\".$_CONFIG[\"site\"].\"</b>t2", tu.getSource().toString());
		List<Code> codes = tu.getSource().getFirstContent().getCodes();
		assertEquals(3, codes.size());
		assertEquals("<b>", codes.get(0).toString());
		assertEquals(1, codes.get(0).getId());
		assertEquals("\".$_CONFIG[\"site\"].\"", codes.get(1).toString());
//TOFIX		assertEquals(2, codes.get(1).getId());
		assertEquals("</b>", codes.get(2).toString());
//will be ok when above is fixed		assertEquals(3, codes.get(2).getId());
	}
	
	@Test
	public void testCommaCaseWithConcat () {
		String snippet = "$a=test('t1', 't2 '.\"and t3\");";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("t1", tu.getSource().toString());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertTrue(tu!=null);
		assertEquals("t2 '.\"and t3", tu.getSource().toString());
		assertEquals("t2 <1/>and t3", fmt.setContent(tu.getSource().getFirstContent()).toString());
		assertEquals("x-mixed", tu.getType());
	}
	
	@Test
	public void testConcatWithVariable () {
		String snippet = "$a='t1' \r.$b.' t2';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("t1' \r.$b.' t2", tu.getSource().toString());
		List<Code> codes = tu.getSource().getFirstContent().getCodes();
		assertEquals(1, codes.size());
		assertEquals("' \r.$b.'", codes.get(0).toString());
	}
	
	@Test
	public void testConcatMultipleStrings () {
		String snippet = "$a='t1' \r.$b.' t2' . $c.\" t3 \"";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("t1' \r.$b.' t2' . $c.\" t3 ", tu.getSource().toString());
		assertEquals("t1<1/> t2<2/> t3 ", fmt.setContent(tu.getSource().getFirstContent()).toString());
		assertEquals("x-mixed", tu.getType());
	}
	
	@Test
	public void testConcatWithEndings () {
		String snippet = "$a= $z.'t1' \r.$b.' t2' . $c.\" t3 \".$d;";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("t1' \r.$b.' t2' . $c.\" t3 ", tu.getSource().toString());
		assertEquals("t1<1/> t2<2/> t3 ", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}
	
	@Test
	public void testConcatSGAndDQStrings () {
		String snippet = "$a='t1' . \"t2\";";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("t1' . \"t2", tu.getSource().toString());
		assertEquals("x-mixed", tu.getType());
	}
	
	@Test
	public void testEntryWithCodes () {
		String snippet = "$a='{$abc}=text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("{$abc}=text", tu.getSource().toString());
		List<Code> codes = tu.getSource().getFirstContent().getCodes();
		assertNotNull(codes);
		assertEquals(1, codes.size());
		assertEquals("{$abc}", codes.get(0).toString());
	}
	
	@Test
	public void testSimpleHTMLCodes () {
		String snippet = "$a='t<a>t</a>t<a attr=\"val\"/>t';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("t<a>t</a>t<a attr=\"val\"/>t", tu.getSource().toString());
		assertEquals("t<1/>t<2/>t<3/>t", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}
	
	@Test
	public void testParitalStartingHTMLCodes () {
		String snippet = "$a='c attr=\"val\"> text <br/>';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("c attr=\"val\"> text <br/>", tu.getSource().toString());
		assertEquals("c attr=\"val\"> text <1/>", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}
	
	@Test
	public void testParitalClosingHTMLCodes () {
		String snippet = "$a='<br/> text <a href=\"...';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("<br/> text <a href=\"...", tu.getSource().toString());
		assertEquals("<1/> text <a href=\"...", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}
	
	@Test
	public void testSpecialHTMLCodes () {
		String snippet = "$a='<!DOCTYPE...> t <?pi attr=\"val\"?> t';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("<!DOCTYPE...> t <?pi attr=\"val\"?> t", tu.getSource().toString());
		assertEquals("<1/> t <2/> t", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}
	
	@Test
	public void testEscapeCodes () {
		String snippet = "$a='\\n t \\r t \\n\\r t \\v t \\a';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("\\n t \\r t \\n\\r t \\v t \\a", tu.getSource().toString());
		assertEquals("<1/> t <2/> t <3/><4/> t <5/> t <6/>", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}
	
	@Test
	public void testLinefeedCodes () {
		String snippet = "$a='\\n\\n';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		// No extraction because no text
		assertTrue(tu==null);
	}
	
	@Test
	public void testOutputLinefeedCodes () {
		String snippet = "$a='\\n\\n';";
		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testVariableCodes () {
		String snippet = "$a=\"t [var1] t {var2} t {$var3} t\";";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("t [var1] t {var2} t {$var3} t", tu.getSource().toString());
		assertEquals("t <1/> t <2/> t <3/> t", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}
	
	@Test
	public void testCommentsSingleLine () {
		String snippet = "// $a='abc';\n$b=\"def\";";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("def", tu.getSource().toString());
	}
	
	@Test
	public void testCommentsMultiline () {
		String snippet = "/* $a='abc';\nstuff // etc. * / \n */$b=\"def\";";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("def", tu.getSource().toString());
	}
	
	@Test
	public void testEmptyComment () {
		String snippet = "/**/$a='abc';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("abc", tu.getSource().toString());
	}
	
	@Test
	public void testCommentsWithApos () {
		String snippet = "/** Felix's Favorites */\n$cnt['glob']['type'] = 'Felix\\'s Favorites';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("Felix\\'s Favorites", tu.getSource().toString());
	}
	
	@Test
	public void testSkipDirective () {
		String snippet = "//_skip\n $a='skip';\n$b='text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}
	
	@Test
	public void testSkipDirectiveOnConcat () {
		String snippet = "//_skip\n $a='skip' . $x . 'skip';\n$b='text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}
	
	@Test
	public void testTextInBSkipDirective () {
		String snippet = "//_bskip\n $a='skip';\n//_text\n$b='text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}
	
	@Test
	public void testESkipDirective () {
		String snippet = "//_bskip\n $a='skip';\n//_eskip\n$b='text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}
	
	@Test
	public void testDirectiveInMultilineComment () {
		String snippet = "/*_skip*/ $a='skip'; $b='text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}
	
	@Test
	public void testBTextDirective () {
		String snippet = "/*_bskip*/ $a='skip'; /*_btext*/ $b='text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}
	
	@Test
	public void testETextDirective () {
		String snippet = "/*_bskip*/ $a='skip'; /*_btext*/ $b='textB'; /*_etext*/\n"
			+"$c='skip'; /*_eskip*/ $d='textD'";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("textB", tu.getSource().toString());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertTrue(tu!=null);
		assertEquals("textD", tu.getSource().toString());
	}
	
	@Test
	public void testSkipOutsideDirective () {
		String snippet = "$a='skip'; /*_btext*/ $b='textB';";
		Parameters params = (Parameters)filter.getParameters();
		params.setUseDirectives(true);
		params.setExtractOutsideDirectives(false);
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, params), 1);
		assertTrue(tu!=null);
		assertEquals("textB", tu.getSource().toString());
	}
	
	@Test
	public void testDisabledDirectives () {
		String snippet = "/*_skip*/ $a='textA'; $b='textB';";
		Parameters params = (Parameters)filter.getParameters();
		params.setUseDirectives(false);
		params.setExtractOutsideDirectives(false);
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, params), 1);
		assertTrue(tu!=null);
		assertEquals("textA", tu.getSource().toString());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, params), 2);
		assertTrue(tu!=null);
		assertEquals("textB", tu.getSource().toString());
	}
	
	@Test
	public void testDirectiveScope () {
		String snippet = "/*_skip*/ $a['key1']='skip'; $a['key2']='text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}

	@Test
	public void testSingleQuotedString () {
		String snippet = "$a='\\\\text\\'';\n$b='\\'\"text\"';";
		// Check first TU
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("\\\\text\\'", tu.getSource().toString());
	}
	
	@Test
	public void testDoubleQuotedString () {
		String snippet = "$a=\"text\\\"\";\n$b=\"'text\\\"\";";
		// Check second TU
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertTrue(tu!=null);
		assertEquals("'text\\\"", tu.getSource().toString());
	}
	
	@Test
	public void testHeredocString () {
		String snippet = "$a=<<<EOT\ntext\nEOT \n\nEOT;";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text\nEOT \n", tu.getSource().toString());
		assertEquals("x-heredoc", tu.getType());
	}
	
	@Test
	public void testQuotedHeredocString () {
		String snippet = "$a=<<<\"EOT\"\ntext\nEOT \n\nEOT;";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text\nEOT \n", tu.getSource().toString());
		assertEquals("x-heredoc", tu.getType());
	}
	
	@Test
	public void testQuotedNowdocString () {
		String snippet = "$a=<<<'EOT'\ntext\nEOT \n\nEOT;";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text\nEOT \n", tu.getSource().toString());
		assertEquals("x-nowdoc", tu.getType());
	}
	
	@Test
	public void testSemiColumnHeredocString () {
		String snippet = "$a=<<<EOT\ntext\nEOT \n;\nEOT;";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text\nEOT \n;", tu.getSource().toString());
	}
	
	@Test
	public void testMultipleLinesHeredocString () {
		String snippet = "$a=<<<EOT\ntext\nEOT \n EOT \n\nEOT;\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text\nEOT \n EOT \n", tu.getSource().toString());
	}
	
	@Test
	public void testEmptyHeredocStringAndOutput () {
		String snippet = "$a=<<<EOT\n\nEOT;";
		// Should be empty so no TU
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu==null);
		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testWhiteHeredocStringAndOutput () {
		String snippet = "$a=<<<EOT\n  \t  \nEOT;";
		// No text so no TU
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu==null);
		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputSimple () {
		String snippet = "$a='abc';\n$b=\"def\";";
		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testLineBreakType () {
		String snippet = "$a='abc';\r\n$b=\"def\";\r\n";
		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputWithNoStrings () {
		String snippet = "echo $a=$b; and other dummy code";
		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputHeredoc () {
		String snippet = "$a=<<<EOT\ntext\nEOT \n EOT \n\nEOT;\n";
		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputMix () {
		String snippet = "$a=<<<EOT\ntext\nEOT \n EOT \n\nEOT;\n"
			+ "$b=\"abc\"\n// 'comments'\n$c = 'def';\n"
			+ "/* $c=\"abc\" */";
		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testSQIndex () {
		String snippet = "$a['skip']; $arr2[  'skip' ] = 'text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}

	@Test
	public void testnoStringIndex () {
		String snippet = "$a[2] = 'text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}

	@Test
	public void testDQIndex () {
		String snippet = "$a[\"skip\"]; $arr2[  \"skip\" ] = 'text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}

	@Test
	public void testHeredocIndex () {
		String snippet = "$a[ <<<key\nskip\nkey\n] = 'text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}

	@Test
	public void testQuotedHeredocIndex () {
		String snippet = "$a[ <<<\"key\"\nskip\nkey\n] = 'text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}

	@Test
	public void testNowdocIndex () {
		String snippet = "$a[ <<<'key'\nskip\nkey\n] = 'text';";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertTrue(tu!=null);
		assertEquals("text", tu.getSource().toString());
	}

	@Test
	public void testOutputArrayKeys () {
		String snippet = "$arr1[\"foo\"]; $arr2[  'foo' ] = 'text';";
		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@Test
	public void testFilteringOfHtmlLikeTags() throws Exception {
		String[][] snippets = new String[][]{
				{"'Some value, which is not tag > 15°.'", "Some value, which is not tag > 15°."},
				{"'Some value, which is not tag < 15°.'", "Some value, which is not tag < 15°."},
				{"'<Some value, which is tag > 15°.'", " 15°."},
				{"'</Some value, which is tag > 15°.'", " 15°."}
		};

		for (String[] snippet : snippets) {
			ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet[0]), 1);
			assertTrue(tu != null);
			assertEquals(snippet[1], tu.getSource().getFirstContent().getText());
		}
	}

	@Test
	public void testDoubleExtraction () throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root+"test01.phpcnt", null));

		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locEN));
	}

	private ArrayList<Event> getEvents(String snippet) {
		return getEvents(snippet, null);
	}
	
	private ArrayList<Event> getEvents(String snippet, Parameters params) {
		if ( params == null ) filter.getParameters().reset();
		return FilterTestDriver.getEvents(filter, snippet, params, locEN, null);
	}

}
