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

package net.sf.okapi.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.okapi.common.Util.SUPPORTED_OS;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@RunWith(JUnit4.class)
public class UtilTest {
	
	private CharsetEncoder chsEnc;
	private DocumentBuilderFactory docBuilderFact;
	
	@Before
	public void setUp() throws Exception {
		docBuilderFact = DocumentBuilderFactory.newInstance();
		docBuilderFact.setValidating(false);
		chsEnc = Charset.forName("iso-8859-1").newEncoder();
	}

	@After
	public void tearDown() {
	}

    @Test
    public void isEmptyNull(){
        assertTrue("null should be empty", Util.isEmpty((String)null));
    }

    @Test
    public void isEmptyEmpty(){
        assertTrue("Empty should be empty", Util.isEmpty(""));
    }

    @Test
    public void isEmptyNotEmpty(){
        assertFalse("Not empty should be not empty", Util.isEmpty("not"));
    }

    @Test
    public void isEmptyNotEmptyWhitespace(){
        assertFalse("Space should not be empty", Util.isEmpty(" "));
    }

    @Test
    public void isEmptyIgnoreWSNotEmptyWhitespace(){
        assertTrue("Space should be empty", Util.isEmpty(" ", true));
    }

    @Test
    public void isEmptyIgnoreWSEmpty(){
        assertTrue("Empty should be empty", Util.isEmpty("", true));
    }

    @Test
    public void isEmptyIgnoreWSNull(){
        assertTrue("Null should be empty", Util.isEmpty(null, true));
    }

    @Test
    public void isEmptyIgnoreWSNotEmpty(){
        assertFalse("Not empty should be not empty", Util.isEmpty("s", true));
    }

    @Test
    public void isEmptyIgnoreWSNotEmptyImmutable(){
        String tmp = "s ";
        Util.isEmpty(tmp, true);
        assertEquals("tmp after method call", "s ", tmp);
    }

	@Test
	public void testTrimStart () {
		assertEquals("textz \t ", Util.trimStart(" \t ztextz \t ", " \tz"));
		assertEquals("", Util.trimStart(" \t ", " \tz"));
		assertNull(Util.trimStart(null, " \tz"));
		assertEquals("", Util.trimStart("", " \tz"));
	}

	@Test
	public void testTrimEnd () {
		assertEquals(" \t ztext", Util.trimEnd(" \t ztextz \t ", " \tz"));
		assertEquals("", Util.trimEnd(" \t ", " \tz"));
		assertNull(Util.trimEnd(null, " \tz"));
		assertEquals("", Util.trimEnd("", " \tz"));
	}

	@Test
	public void testGetDirectoryName_BSlash () {
		String in = "C:\\test\\file";
		assertEquals("C:\\test", Util.getDirectoryName(in));
	}

	@Test
	public void testGetDirectoryName_MixedCases () {
		String in = "/home/test\\file";
		assertEquals("/home/test", Util.getDirectoryName(in));
	}

	@Test
	public void testGetDirectoryName_Slash () {
		String in = "/home/test/file";
		assertEquals("/home/test", Util.getDirectoryName(in));
	}

	@Test
	public void testGetDirectoryName_DirBSlash () {
		String in = "C:\\test\\";
		assertEquals("C:\\test", Util.getDirectoryName(in));
	}

	@Test
	public void testGetDirectoryName_DirSlash () {
		String in = "/home/test/";
		assertEquals("/home/test", Util.getDirectoryName(in));
	}

	@Test
	public void testGetDirectoryName_Filename () {
		String in = "myFile.ext";
		assertEquals("", Util.getDirectoryName(in));
	}

	@Test
	public void testGetDirectoryName_URL () {
		String in = "/C:/test/file.ext";
		assertEquals("/C:/test", Util.getDirectoryName(in));
	}

	@Test
	public void testEscapeToXML_Quote0 () {
		String in = "&<>'\"";
		assertEquals("&amp;&lt;>'\"", Util.escapeToXML(in, 0, false, null));
	}

	@Test
	public void testEscapeToXML_Quote1 () {
		String in = "&<>'\"";
		assertEquals("&amp;&lt;>&apos;&quot;", Util.escapeToXML(in, 1, false, null));
	}

	@Test
	public void testEscapeToXML_Quote2 () {
		String in = "&<>'\"";
		assertEquals("&amp;&lt;>&#39;&quot;", Util.escapeToXML(in, 2, false, null));
	}

	@Test
	public void testEscapeToXML_Quote3 () {
		String in = "&<>'\"";
		assertEquals("&amp;&lt;>'&quot;", Util.escapeToXML(in, 3, false, null));
	}

	@Test
	public void testEscapeToXML_GT () {
		String in = "&<>'\"";
		assertEquals("&amp;&lt;&gt;'&quot;", Util.escapeToXML(in, 3, true, null));
	}

	@Test
	public void testEscapeToXML_ExtCharsWithNull () {
		String in = "\u00d0\u0440Z\uD840\uDC00";
		assertEquals("\u00d0\u0440Z\uD840\uDC00", Util.escapeToXML(in, 0, false, null));
	}

	@Test
	public void testEscapeToXML_ExtCharsWithLatin1 () {
		String in = "\u00d0\u0440Z\uD840\uDC00";
		assertEquals("\u00d0&#x0440;Z&#x20000;", Util.escapeToXML(in, 0, false, chsEnc));
	}

	@Test
	public void testGetExtension () {
		String in = "myFile.abc.ext";
		assertEquals(".ext", Util.getExtension(in));
	}

	@Test
	public void testGetExtension_Alone () {
		String in = ".ext";
		assertEquals(".ext", Util.getExtension(in));
	}

	@Test
	public void testGetExtension_None () {
		String in = "myFile";
		assertEquals("", Util.getExtension(in));
	}

	@Test
	public void testGetExtension_Dot () {
		String in = "myFile.";
		assertEquals(".", Util.getExtension(in));
	}
	
	@Test
	public void testGetPercentage () {
		assertEquals(45, Util.getPercentage(450, 1000));
	}

	@Test
	public void testGetPercentage_WithZero () {
		assertEquals(1, Util.getPercentage(10, 0));
	}

	@Test
	public void testIsSameLanguage_DoNotIgnoreRegion () {
		assertTrue(Util.isSameLanguage("en", "en", false));
		assertTrue(Util.isSameLanguage("en", "EN", false));
		assertTrue(Util.isSameLanguage("En", "eN", false));
		assertFalse(Util.isSameLanguage("en", "fr", false));
		assertFalse(Util.isSameLanguage("en", "en-us", false));
		assertFalse(Util.isSameLanguage("en-us", "en", false));
		assertFalse(Util.isSameLanguage("abc-xyz", "abc-QWE", false));
		assertFalse(Util.isSameLanguage("abc-xyz", "iop-QWE", false));
	}

	@Test
	public void testIsSameLanguage_IgnoreRegion () {
		assertTrue(Util.isSameLanguage("en", "en", true));
		assertTrue(Util.isSameLanguage("en", "EN", true));
		assertTrue(Util.isSameLanguage("En", "eN", true));
		assertFalse(Util.isSameLanguage("en", "fr", true));
		assertTrue(Util.isSameLanguage("en", "en-us", true));
		assertTrue(Util.isSameLanguage("en-us", "en", true));
		assertTrue(Util.isSameLanguage("abc-xyz", "abc-QWE", true));
		assertFalse(Util.isSameLanguage("abc-xyz", "iop-QWE", true));
	}

	@Test
	public void testGetTextContent_Simple () {
		Document doc = createXMLdocument("<d>\n\nText</d>");
		Element elem = doc.getDocumentElement();
		assertEquals("\n\nText", Util.getTextContent(elem));
	}
	
	@Test
	public void testGetTextContent_WithComment () {
		Document doc = createXMLdocument("<d><!--comment-->Text</d>");
		Element elem = doc.getDocumentElement();
		assertEquals("Text", Util.getTextContent(elem));
	}
	
	@Test
	public void testGetTextContent_Empty () {
		Document doc = createXMLdocument("<d/>");
		Element elem = doc.getDocumentElement();
		assertEquals("", Util.getTextContent(elem));
	}
	
	@Test
	public void testMin () {
		assertEquals(-10, Util.min(10, 20, 30, -10, 0, 5));		
		assertEquals(-100, Util.min(-99, -98, -100, 1000));
		assertEquals(10, Util.min(10, 20, 30, 40, 15));
		assertEquals(0, Util.min());
	}
	
	@Test
	public void testToURI () {
		if (Util.getOS() == SUPPORTED_OS.WINDOWS) {
			// Note: Use a folder that is unlikely to exists on any machine
			// As the system may look at existing folders to determine if the path is a file or a directory
			assertEquals("/C:/gremlins.begone", Util.toURI("C:\\gremlins.begone").getPath());
			assertEquals("/C:/gremlins.begone", Util.toURI("file:///C:/gremlins.begone").getPath());
			assertEquals("/C:/gremlins.begone", Util.toURI("/C:/gremlins.begone").getPath());
			assertEquals("/C:/gremlins.begone", Util.toURI("file:/C:/gremlins.begone").getPath());
		} else {
			assertEquals("/test", Util.toURI("/test").getPath());
			assertEquals("/test", Util.toURI("file:///test").getPath());
			assertEquals("/test", Util.toURI("//test").getPath());
			assertEquals("/test", Util.toURI("file:/test").getPath());
		}
	}
	
	@Test
	public void testEmptyURI() throws URISyntaxException {
		// IllegalArgumentException expected
		try{
			assertEquals("", Util.toURI(null).toString());
		}
		catch (IllegalArgumentException e) {
		}
		try{
			assertEquals("", Util.toURI(null).getPath());
		}
		catch (IllegalArgumentException e) {
		}
		try{
			assertEquals("", Util.makeURIFromPath(null));
		}
		catch (IllegalArgumentException e) {
		}
		try{
			assertEquals("", Util.toURI("").toString());
		}
		catch (IllegalArgumentException e) {
		}
		try{
			assertEquals("", Util.toURI("").getPath());
		}
		catch (IllegalArgumentException e) {
		}
		try{
			assertEquals("", Util.makeURIFromPath(""));
		}
		catch (IllegalArgumentException e) {
			return;
		}
		fail();		
	}	

	@Test
	public void testEnsureSeparator() {
		assertNull(Util.ensureSeparator(null, false));
		assertEquals("", Util.ensureSeparator("", false));
		assertEquals("/C:/test/", Util.ensureSeparator("/C:/test/", false));
		assertEquals("/C:/test" + File.separator, Util.ensureSeparator("/C:/test" + File.separator, false));
		assertEquals("/C:/test/", Util.ensureSeparator("/C:/test" + File.separator, true));
		assertEquals("/C:/test" + File.separator, Util.ensureSeparator("/C:/test", false));
		assertEquals("/C:/test/", Util.ensureSeparator("/C:/test", true));
	}
	
	@Test
	public void testFixFilename() {
		assertEquals("", Util.fixFilename(null));
		assertEquals("", Util.fixFilename(null, null));
		assertEquals("abs:def", Util.fixFilename("abs:def", null));
		assertEquals("a_bc_de_fgh_ijk{l};mn_op_qr (s) t_uvw!x_y[z]", Util.fixFilename("a*bc:de<fgh>ijk{l};mn?op\\qr (s) t|uvw!x/y[z]"));
	}
	
	@Test
	public void testFixPath() {
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext", Util.fixPath("/C:/dir1////dir2/dir3/dir4/filename.ext", false));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext", Util.fixPath("/C:/dir1/dir2/////dir3/dir4/filename.ext", false));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext", Util.fixPath("/C:/dir1/dir2\\/dir3/dir4/filename.ext", false));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext", Util.fixPath("/C:/dir1/dir2//\\\\\\\\\\dir3/dir4/filename.ext", false));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext", Util.fixPath("/C:/dir1/dir2/dir3/dir4/\\filename.ext", false));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext", Util.fixPath("/C:/dir1//dir2/\\//\\\\dir3/dir4/filename.ext", false));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext", Util.fixPath("/C:/dir1//dir2\\/\\\\/\\//\\\\dir3/dir4/filename.ext", false));
		
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext".replace("/", File.separator), Util.fixPath("/C:/dir1////dir2/dir3/dir4/filename.ext"));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext".replace("/", File.separator), Util.fixPath("/C:/dir1/dir2/////dir3/dir4/filename.ext"));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext".replace("/", File.separator), Util.fixPath("/C:/dir1/dir2\\/dir3/dir4/filename.ext"));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext".replace("/", File.separator), Util.fixPath("/C:/dir1/dir2//\\\\\\\\\\dir3/dir4/filename.ext"));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext".replace("/", File.separator), Util.fixPath("/C:/dir1/dir2/dir3/dir4/\\filename.ext"));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext".replace("/", File.separator), Util.fixPath("/C:/dir1//dir2/\\//\\\\dir3/dir4/filename.ext"));
		assertEquals("/C:/dir1/dir2/dir3/dir4/filename.ext".replace("/", File.separator), Util.fixPath("/C:/dir1//dir2\\/\\\\/\\//\\\\dir3/dir4/filename.ext"));
	}
	
// Unused
//	@Test
//	public void generateRandomId() {
//		String one = Util.generateRandomId(5);
//		String two = Util.generateRandomId(10);
//		assertTrue(one.length() == 5);
//		assertTrue(two.length() == 10);
//		
//		one = Util.generateRandomId(5);
//		two = Util.generateRandomId(5);
//		assertFalse(one.equals(two));
//	}
	
	@Test
	public void testMapGetValue() {
		Map<String, Long> map = new HashMap<String, Long>();
		map.put("one", 1L);
		map.put("two", 2L);
		assertEquals((Long) 1L, Util.getValue(map, "one", 0L));
		assertEquals((Long) 2L, Util.getValue(map, "two", 0L));
		assertEquals((Long) 0L, Util.getValue(map, "three", 0L));
		assertEquals((Long) 5L, Util.getValue(map, "three", 5L));
	}

	@Test
	public void testFormatDouble () {
		assertEquals("1.2", Util.formatDouble(1.2));
		assertEquals("1.01", Util.formatDouble(1.01));
		assertEquals("100", Util.formatDouble(100.0));
		assertEquals("0.1", Util.formatDouble(0.1));
	}
	
	@Test
	public void testNormalizRange() {
		int nv = Util.normalizeRange(0, 100, 50);
		assertEquals(50, nv);
		nv = Util.normalizeRange(-5, 5, 0);
		assertEquals(50, nv);
		nv = Util.normalizeRange(-5, 5, 5);
		assertEquals(100, nv);
		nv = Util.normalizeRange(-5, 5, -5);
		assertEquals(0, nv);
		nv = Util.normalizeRange(-100, 100, -30);
		assertEquals(35, nv);
		nv = Util.normalizeRange(-100, 100, 30);
		assertEquals(65, nv);
		nv = Util.normalizeRange(-100, 100, 0);
		assertEquals(50, nv);
		nv = Util.normalizeRange(-100, 100, -100);
		assertEquals(0, nv);
		nv = Util.normalizeRange(-100, 100, 100);
		assertEquals(100, nv);
	}
	
	@Test
	public void testBuildPath() {
		assertEquals("home/user/dir".replace("/", File.separator), Util.buildPath("home/user", "dir"));
		assertEquals("home/user/dir".replace("/", File.separator), Util.buildPath("home/user", "/dir"));
		assertEquals("home/user/dir".replace("/", File.separator), Util.buildPath("home/user/", "/dir"));
		
		assertEquals("/home/user/dir".replace("/", File.separator), Util.buildPath("/home/user", "dir"));
		assertEquals("/home/user/dir".replace("/", File.separator), Util.buildPath("/home/user", "/dir"));
		assertEquals("/home/user/dir".replace("/", File.separator), Util.buildPath("/home/user/", "/dir"));
		
		assertEquals("/C:/home/user/dir/filename.ext".replace("/", File.separator), Util.buildPath("/C:/home/user", "dir", "filename.ext"));
		assertEquals("/C:/home/user/dir/filename.ext".replace("/", File.separator), Util.buildPath("/C:/home/user", "/dir", "filename.ext"));
		assertEquals("/C:/home/user/dir/filename.ext".replace("/", File.separator), Util.buildPath("/C:/home/user/", "/dir", "filename.ext"));
		
		assertEquals("file:///C:/home/user/dir/filename.ext", Util.buildPath("file:///C:/home/user", "dir", "filename.ext"));
		assertEquals("file:///C:/home/user/dir/filename.ext", Util.buildPath("file:///C:/home/user", "/dir", "filename.ext"));
		assertEquals("file:///C:/home/user/dir/filename.ext", Util.buildPath("file:///C:/home/user/", "/dir", "filename.ext"));
	}

	@Test
	public void testisValidInXML () {
		// Invalid
		assertFalse(Util.isValidInXML('\u0000'));
		assertFalse(Util.isValidInXML('\u0001'));
		assertFalse(Util.isValidInXML('\u0019'));
		assertFalse(Util.isValidInXML('\uDFFF'));
		assertFalse(Util.isValidInXML('\uFFFF'));
		assertFalse(Util.isValidInXML('\uD800'));
		// Valid
		assertTrue(Util.isValidInXML('\u0020'));
		assertTrue(Util.isValidInXML('\u0021'));
		assertTrue(Util.isValidInXML('\uD7FF'));
		assertTrue(Util.isValidInXML('\uE000'));
		assertTrue(Util.isValidInXML('\uE111'));
		assertTrue(Util.isValidInXML('\uFFFD'));
		assertTrue(Util.isValidInXML('\t'));
		assertTrue(Util.isValidInXML('\n'));
		assertTrue(Util.isValidInXML('\r'));
		assertTrue(Util.isValidInXML('a'));
	}
	
	private Document createXMLdocument (String data) {
		InputSource input = new InputSource(new StringReader(data));
		Document doc = null;
		try {
			doc = docBuilderFact.newDocumentBuilder().parse(input);
		}
		catch ( SAXException e ) {
			e.printStackTrace();
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
		catch ( ParserConfigurationException e ) {
			e.printStackTrace();
		}
		assertNotNull(doc);
		return doc;
	}

}
