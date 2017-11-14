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

package net.sf.okapi.filters.po;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class POWriterTest {
	
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	private POFilter filter;
	private String header = "# \nmsgid \"\"\nmsgstr \"\"\n"
		+ "\"Content-Type: text/plain; charset=UTF-8\\n\"\n"
		+ "\"Content-Transfer-Encoding: 8bit\\n\"\n"
		+ "\"Language: fr\\n\"\n"
		+ "\"Plural-Forms: nplurals=2; plural=(n>1);\\n\"\n\n";
	
	@Before
	public void setUp() {
		filter = new POFilter();
	}

	@Test
	public void testEscapes () {
		String snippet = ""
			+ "msgid \"'\"\\\"\r"
			+ "msgstr \"'\"\\\"\r\r";
		String expected = ""
			+ "msgid \"'\\\"\\\\\"\r"
			+ "msgstr \"'\\\"\\\\\"\r\r";
		String result = rewrite(getEvents(snippet, locEN, locFR), locFR);
		assertEquals(header.replace('\n', '\r')+expected, result);
	}

	@Test
	public void testEscapesAmongAlreadyEscaped () {
		String snippet = ""
			+ "msgid \"' \\\\ \" \\\\\\\"\r"
			+ "msgstr \"' \\\\ \" \\\\\\\"\r\r";
		String expected = ""
			+ "msgid \"' \\\\ \\\" \\\\\\\\\"\r"
			+ "msgstr \"' \\\\ \\\" \\\\\\\\\"\r\r";
		String result = rewrite(getEvents(snippet, locEN, locFR), locFR);
		assertEquals(header.replace('\n', '\r')+expected, result);
	}
		
	@Test
	public void testSrcSimpleOutput () {
		String snippet = ""
			+ "msgid \"Text 1\"\r"
			+ "msgstr \"\"\r\r";
		String result = rewrite(getEvents(snippet, locEN, locFR), locFR);
		assertEquals(header.replace('\n', '\r')+snippet, result);
	}
		
	@Test
	public void testSrcTrgSimpleOutput () {
		String snippet = ""
			+ "msgid \"Text 1\"\r"
			+ "msgstr \"Texte 1\"\r\r";
		String result = rewrite(getEvents(snippet, locEN, locFR), locFR);
		assertEquals(header.replace('\n', '\r')+snippet, result);
	}
	
	@Test
	public void testOutputWithLinesWithWrap () {
		String snippet = ""
			+ "msgid \"\"\n"
			+ "\"line1\\n\"\n"
			+ "\"line2\\n\"\n"
			+ "msgstr \"\"\n"
			+ "\"line1trans\\n\"\n"
			+ "\"line2trans\\n\"\n"
			+ "\"line3trans\"\n\n";
		String result = rewrite(getEvents(snippet, locEN, locFR), locFR);
		assertEquals(header+snippet, result);
	}
		
	@Test
	public void testOutputWithPlural () {
		String snippet = ""
			+ "msgid \"source singular\"\n"
			+ "msgid_plural \"source plural\"\n"
			+ "msgstr[0] \"target singular\"\n"
			+ "msgstr[1] \"target plural\"\n\n";
		String result = rewrite(getEvents(snippet, locEN, locFR), locFR);
		assertEquals(header+snippet, result);
	}
		
	@Test
	public void testOutputWithFuzzyPlural () {
		String snippet = ""
			+ "#, fuzzy\n"
			+ "msgid \"source singular\"\n"
			+ "msgid_plural \"source plural\"\n"
			+ "msgstr[0] \"target singular\"\n"
			+ "msgstr[1] \"target plural\"\n\n";
		String result = rewrite(getEvents(snippet, locEN, locFR), locFR);
		assertEquals(header+snippet, result);
	}
		
	@Test
	public void testOutputWithFuzzy () {
		String snippet = "#, fuzzy\n"
			+ "msgid \"source\"\n"
			+ "msgstr \"target\"\n\n";
		String result = rewrite(getEvents(snippet, locEN, locFR), locFR);
		assertEquals(header+snippet, result);
	}
		
	private ArrayList<Event> getEvents(String snippet,
		LocaleId srcLang,
		LocaleId trgLang)
	{
		return FilterTestDriver.getEvents(filter,  snippet,  srcLang, trgLang);
	}

	private String rewrite (ArrayList<Event> list,
		LocaleId trgLang)
	{
		POWriter writer = new POWriter();
		writer.setOptions(trgLang, "UTF-8");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		writer.setOutput(output);
		for (Event event : list) {
			writer.handleEvent(event);
		}
		writer.close();
		return output.toString();
	}

}
