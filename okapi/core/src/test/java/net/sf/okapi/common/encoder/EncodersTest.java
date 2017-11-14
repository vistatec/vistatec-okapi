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

package net.sf.okapi.common.encoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.security.Policy.Parameters;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.filters.DummyFilter;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.resource.Property;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EncodersTest {

	@Test
	public void testXMLEncoder () {
		XMLEncoder enc = new XMLEncoder();
		
		assertEquals("en", enc.toNative(Property.LANGUAGE, "en"));
		assertEquals("windows-1252", enc.toNative(Property.ENCODING, "windows-1252"));
		
		enc.setOptions(null, "us-ascii", "\n");
		assertEquals("&#x20000;", enc.encode("\uD840\uDC00", EncoderContext.TEXT));
		assertEquals("&#x20000;", enc.encode(0x20000, EncoderContext.TEXT));
		assertEquals("abc", enc.encode("abc", EncoderContext.TEXT));
		assertEquals("a", enc.encode('a', EncoderContext.TEXT));
		assertEquals("a", enc.encode((int)'a', EncoderContext.TEXT));

		enc.setOptions(null, "UTF-8", "\n");
		assertEquals(0x20000, enc.encode("\uD840\uDC00", EncoderContext.TEXT).codePointAt(0));
		assertEquals(0x20000, enc.encode(0x20000, EncoderContext.TEXT).codePointAt(0));
		assertEquals("abc", enc.encode("abc", EncoderContext.TEXT));
		assertEquals("a", enc.encode((int)'a', EncoderContext.TEXT));
		assertEquals("\u00a0", enc.encode("\u00a0", EncoderContext.TEXT));
		assertEquals("\u00a0", enc.encode('\u00a0', EncoderContext.TEXT));
		assertEquals("\u00a0", enc.encode(0x00a0, EncoderContext.TEXT));
		assertEquals(">", enc.encode(">", EncoderContext.TEXT));
		assertEquals(">", enc.encode((int)'>', EncoderContext.TEXT));
		assertEquals(">", enc.encode('>', EncoderContext.TEXT));
		
		DummyParameters params = new DummyParameters();
		params.setBoolean(XMLEncoder.ESCAPEGT, true);
		params.setBoolean(XMLEncoder.ESCAPENBSP, true);
		enc.setOptions(params, "UTF-8", "\n");
		assertEquals("&#x00a0;", enc.encode("\u00a0", EncoderContext.TEXT));
		assertEquals("&#x00a0;", enc.encode('\u00a0', EncoderContext.TEXT));
		assertEquals("&#x00a0;", enc.encode(0x00a0, EncoderContext.TEXT));
		assertEquals("&gt;", enc.encode(">", EncoderContext.TEXT));
		assertEquals("&gt;", enc.encode((int)'>', EncoderContext.TEXT));
		assertEquals("&gt;", enc.encode('>', EncoderContext.TEXT));
	}

	@Test
	public void testCDATAFalsePositive() {
		// Make sure we never accidentally produce the sequence "]]>" by writing
		// out an unescaped > when the escapeGT option is not set.
		XMLEncoder enc = new XMLEncoder();
		DummyParameters params = new DummyParameters();
		params.setBoolean(XMLEncoder.ESCAPEGT, false);
		enc.setOptions(params, "UTF-8", "\n");

		assertEquals("]]&gt;", enc.encode("]]>", EncoderContext.TEXT));

		enc = new XMLEncoder();
		StringBuilder sb = new StringBuilder();
		sb.append(enc.encode(']', EncoderContext.TEXT));
		sb.append(enc.encode(']', EncoderContext.TEXT));
		sb.append(enc.encode('>', EncoderContext.TEXT));
		assertEquals("]]&gt;", sb.toString());
	}

	@Test
	public void testXMLEncoderHighSurrogateChars() {
		XMLEncoder enc = new XMLEncoder();
		enc.setOptions(null, "us-ascii", "\n");
		StringBuilder sb = new StringBuilder();
		sb.append(enc.encode('\uD840', EncoderContext.TEXT));
		sb.append(enc.encode('\uDC00', EncoderContext.TEXT));
		assertEquals("&#x20000;", sb.toString());
	}

	@Test
	public void testXMLEncoderQuoteMode() {
		XMLEncoder enc = new XMLEncoder("UTF-8", "\n", true, true, true, QuoteMode.ALL);
		assertEquals("&quot;&apos;", enc.encode("\"'", EncoderContext.TEXT));
		enc = new XMLEncoder("UTF-8", "\n", true, true, true, QuoteMode.UNESCAPED);
		assertEquals("\"'", enc.encode("\"'", EncoderContext.TEXT));
		enc = new XMLEncoder("UTF-8", "\n", true, true, true, QuoteMode.NUMERIC_SINGLE_QUOTES);
		assertEquals("&quot;&#39;", enc.encode("\"'", EncoderContext.TEXT));
		enc = new XMLEncoder("UTF-8", "\n", true, true, true, QuoteMode.DOUBLE_QUOTES_ONLY);
		assertEquals("&quot;'", enc.encode("\"'", EncoderContext.TEXT));
	}

	@Test
	public void testHTMLEncoder () {
		HtmlEncoder enc = new HtmlEncoder();
		
		assertEquals("en", enc.toNative(Property.LANGUAGE, "en"));
		assertEquals("windows-1252", enc.toNative(Property.ENCODING, "windows-1252"));
		
		enc.setOptions(null, "us-ascii", "\n");
		assertEquals("&#x20000;", enc.encode("\uD840\uDC00", EncoderContext.TEXT));
		assertEquals("&#x20000;", enc.encode(0x20000, EncoderContext.TEXT));
		assertEquals("abc", enc.encode("abc", EncoderContext.TEXT));
		assertEquals("a", enc.encode('a', EncoderContext.TEXT));
		assertEquals("a", enc.encode((int)'a', EncoderContext.TEXT));

		enc.setOptions(null, "UTF-8", "\r");
		assertEquals(0x20000, enc.encode("\uD840\uDC00", EncoderContext.TEXT).codePointAt(0));
		assertEquals(0x20000, enc.encode(0x20000, EncoderContext.TEXT).codePointAt(0));
		assertEquals("abc\r", enc.encode("abc\n", EncoderContext.TEXT));
		assertEquals("a", enc.encode('a', EncoderContext.TEXT));
		assertEquals("a", enc.encode((int)'a', EncoderContext.TEXT));
		
		// quoteMode default
		assertEquals("&#39;", enc.encode("'", EncoderContext.TEXT));
		assertEquals("&quot;", enc.encode("\"", EncoderContext.TEXT));
		
		HtmlEncoder enc2 = new HtmlEncoder("UTF-8", "\n", QuoteMode.DOUBLE_QUOTES_ONLY);
		assertEquals("'", enc2.encode("'", EncoderContext.TEXT));
		assertEquals("&quot;", enc2.encode("\"", EncoderContext.TEXT));
		
		HtmlEncoder enc3 = new HtmlEncoder("UTF-8", "\n", QuoteMode.ALL);
		assertEquals("&apos;", enc3.encode("'", EncoderContext.TEXT));
		assertEquals("&quot;", enc3.encode("\"", EncoderContext.TEXT));
		
		HtmlEncoder enc4 = new HtmlEncoder("UTF-8", "\n", QuoteMode.UNESCAPED);
		assertEquals("'", enc4.encode("'", EncoderContext.TEXT));
		assertEquals("\"", enc4.encode("\"", EncoderContext.TEXT));
	}

	@Test
	public void testPropertiesEncoder () {
		PropertiesEncoder enc = new PropertiesEncoder();
		
		assertEquals("en", enc.toNative(Property.LANGUAGE, "en"));
		assertEquals("windows-1252", enc.toNative(Property.ENCODING, "windows-1252"));
		
		enc.setOptions(null, "us-ascii", "\n");
		assertEquals("\\ud840\\udc00", enc.encode("\uD840\uDC00", EncoderContext.TEXT));
		assertEquals("\\ud840\\udc00", enc.encode(0x20000, EncoderContext.TEXT));
		assertEquals("abc", enc.encode("abc", EncoderContext.TEXT));
		assertEquals("a", enc.encode('a', EncoderContext.TEXT));
		assertEquals("a", enc.encode((int)'a', EncoderContext.TEXT));

		enc.setOptions(null, "UTF-8", "\n");
		assertEquals(0x20000, enc.encode("\uD840\uDC00", EncoderContext.TEXT).codePointAt(0));
		assertEquals(0x20000, enc.encode(0x20000, EncoderContext.TEXT).codePointAt(0));
		assertEquals("abc\\n", enc.encode("abc\n", EncoderContext.TEXT));
		assertEquals("a", enc.encode('a', EncoderContext.TEXT));
		assertEquals("a", enc.encode((int)'a', EncoderContext.TEXT));
	}

	@Test
	public void testJSONEncoder () {
		JSONEncoder enc = new JSONEncoder();
		
		assertEquals("en", enc.toNative(Property.LANGUAGE, "en"));
		assertEquals("windows-1252", enc.toNative(Property.ENCODING, "windows-1252"));
		
		enc.setOptions(null, "us-ascii", "\n");
		assertEquals("\\ud840\\udc00", enc.encode("\uD840\uDC00", EncoderContext.TEXT));
		assertEquals("\\ud840\\udc00", enc.encode(0x20000, EncoderContext.TEXT));
		assertEquals("abc", enc.encode("abc", EncoderContext.TEXT));
		assertEquals("a", enc.encode('a', EncoderContext.TEXT));
		assertEquals("a", enc.encode((int)'a', EncoderContext.TEXT));

		enc.setOptions(null, "UTF-8", "\n");
		assertEquals(0x20000, enc.encode("\uD840\uDC00", EncoderContext.TEXT).codePointAt(0));
		assertEquals(0x20000, enc.encode(0x20000, EncoderContext.TEXT).codePointAt(0));
		assertEquals("abc\\n\\\"\\\\ \\b\\f\\t\\r\\/", enc.encode("abc\n\"\\ \b\f\t\r/", EncoderContext.TEXT));
		assertEquals("a", enc.encode('a', EncoderContext.TEXT));
		assertEquals("a", enc.encode((int)'a', EncoderContext.TEXT));
	}

	@Test
	public void testJSONEncoderEscapeForwardSlash() {
		JSONEncoder enc = new JSONEncoder();
		IParameters params = new StringParameters();
		params.setBoolean("escapeForwardSlashes", true);
		enc.setOptions(params, "UTF-8", "\n");
		assertEquals("A\\/B\\\"C\\\\D", enc.encode("A/B\"C\\D", EncoderContext.TEXT));
		params.setBoolean("escapeForwardSlashes", false);
		enc.setOptions(params, "UTF-8", "\n");
		// We always escape double quote and backslash
		assertEquals("A/B\\\"C\\\\D", enc.encode("A/B\"C\\D", EncoderContext.TEXT));
	}

	@Test
	public void testJSONEncoderDontEscapeExtendedChars() {
		JSONEncoder enc = new JSONEncoder();
		enc.setOptions(null, "UTF-8", "\n");
		assertEquals("\ud840\udc00", enc.encode("\ud840\udc00", EncoderContext.TEXT));
		StringBuilder sb = new StringBuilder();
		sb.append(enc.encode('\ud840', EncoderContext.TEXT));
		sb.append(enc.encode('\udc00', EncoderContext.TEXT));
		assertEquals("\ud840\udc00", sb.toString());
	}

	@Test
	public void testJSONEncoderEscapeAlwyasExtendedCharsInAscii() {
		// In ascii, we should always escape since it's not encodable, even when
		// the option isn't set
		JSONEncoder enc = new JSONEncoder();
		enc.setOptions(null,  "us-ascii", "\n");
		assertEquals("\\ud840\\udc00", enc.encode("\ud840\udc00", EncoderContext.TEXT));
		StringBuilder sb = new StringBuilder();
		sb.append(enc.encode('\ud840', EncoderContext.TEXT));
		sb.append(enc.encode('\udc00', EncoderContext.TEXT));
		assertEquals("\\ud840\\udc00", sb.toString());
	}

	@Test
	public void testJSONEncoderEscapeExtendedChars() {
		JSONEncoder enc = new JSONEncoder();
		IParameters params = new StringParameters();
		params.setBoolean("escapeExtendedChars", true);
		enc.setOptions(params, "UTF-8", "\n");
		assertEquals("\\ud840\\udc00", enc.encode("\ud840\udc00", EncoderContext.TEXT));
		StringBuilder sb = new StringBuilder();
		sb.append(enc.encode('\ud840', EncoderContext.TEXT));
		sb.append(enc.encode('\udc00', EncoderContext.TEXT));
		assertEquals("\\ud840\\udc00", sb.toString());
	}

	@Test
	public void testDefaultEncoder () {
		DefaultEncoder enc = new DefaultEncoder();
		
		assertEquals("en", enc.toNative(Property.LANGUAGE, "en"));
		assertEquals("windows-1252", enc.toNative(Property.ENCODING, "windows-1252"));
		
		enc.setOptions(null, "us-ascii", "\r\n");
		assertEquals("\uD840\uDC00", enc.encode("\uD840\uDC00", EncoderContext.TEXT));
		assertEquals("\uD840\uDC00", enc.encode(0x20000, EncoderContext.TEXT));
		assertEquals("abc\r\n", enc.encode("abc\n", EncoderContext.TEXT));
		assertEquals("a", enc.encode('a', EncoderContext.TEXT));
		assertEquals("a", enc.encode((int)'a', EncoderContext.TEXT));

		enc.setOptions(null, "UTF-8", "\r");
		assertEquals(0x20000, enc.encode("\uD840\uDC00", EncoderContext.TEXT).codePointAt(0));
		assertEquals(0x20000, enc.encode(0x20000, EncoderContext.TEXT).codePointAt(0));
		assertEquals("abc\r", enc.encode("abc\n", EncoderContext.TEXT));
		assertEquals("a", enc.encode('a', EncoderContext.TEXT));
		assertEquals("a", enc.encode((int)'a', EncoderContext.TEXT));
	}

	@Test
	public void testDTDEncoder () {
		DTDEncoder enc = new DTDEncoder();
		enc.setOptions(null, "us-ascii", "\n");
		assertEquals("&lt;&amp;&#37;", enc.encode("<&%", EncoderContext.TEXT));
		assertEquals("&#x20000;", enc.encode("\uD840\uDC00", EncoderContext.TEXT));
		assertEquals("&#x20000;", enc.encode(0x20000, EncoderContext.TEXT));
		assertEquals("&#37;", enc.encode('%', EncoderContext.TEXT));
		assertEquals("&#37;", enc.encode((int)'%', EncoderContext.TEXT));
	}

	@Test
	public void changeEncoderTest () {
		try (IFilter filter = new DummyFilter();
			 GenericFilterWriter gfw = new GenericFilterWriter(filter.createSkeletonWriter(),
					 			filter.getEncoderManager())) {
			EncoderManager em1 = filter.getEncoderManager();
			assertNotNull(em1);
			em1.setDefaultOptions(null, "UTF-8", "\n");
			em1.updateEncoder(MimeTypeMapper.XML_MIME_TYPE);
			assertEquals("net.sf.okapi.common.encoder.XMLEncoder", em1.getEncoder().getClass().getName());

			EncoderManager em2 = gfw.getEncoderManager();
			assertSame(em1, em2);

			// Bogus MIME type result in default encoder 
			em2.updateEncoder("bogus");
			assertEquals("net.sf.okapi.common.encoder.DefaultEncoder", em2.getEncoder().getClass().getName());

			// Now change the mapping
			em2.setMapping(MimeTypeMapper.XML_MIME_TYPE, "net.sf.okapi.common.encoder.PropertiesEncoder");
			em2.updateEncoder(MimeTypeMapper.XML_MIME_TYPE);
			assertEquals("net.sf.okapi.common.encoder.PropertiesEncoder", em2.getEncoder().getClass().getName());
			assertEquals("net.sf.okapi.common.encoder.PropertiesEncoder", em1.getEncoder().getClass().getName());
		}
	}
	
}
