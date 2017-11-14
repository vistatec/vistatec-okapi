/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xliff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filterwriter.XLIFFWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XLIFFFilterCtypeTest {

	private XLIFFFilter filter;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");

	@Before
	public void setUp () {
		filter = new XLIFFFilter();
	}

	@Test
	public void testKeepCtypeG() throws Exception {
		ensureResultContainsCtype("<g id=\"1\" ctype=\"bold\">t1</g>", "bold");
	}

	@Test
	public void testKeepCtypeBx() throws Exception {
		String actual = ensureResultContainsCtype("<bx id=\"1\" ctype=\"bold\"/>t1<ex id=\"1\"/>", "bold");
		// we expect a g element as output because we use the "placeholderMode" of XLIFFWriter
		assertTrue("<g id='1' is missing: " + actual, actual.contains("<g id=\"1\""));
	}

	@Test
	public void testKeepCtypeBxRid() throws Exception {
		String actual = ensureResultContainsCtype("<bx id=\"1\" ctype=\"bold\" rid=\"99\"/>t1<ex id=\"2\" rid=\"99\"/>", "bold");
		// we expect a g element as output because we use the "placeholderMode" of XLIFFWriter
		assertTrue("<g id='1' is missing: " + actual, actual.contains("<g id=\"1\""));
	}

	@Test
	public void testKeepCtypeBpt() throws Exception {
		String actual = ensureResultContainsCtype("<bpt id=\"1\" ctype=\"bold\"/>t1<ept id=\"1\"/>", "bold");
		// we expect a g element as output because we use the "placeholderMode" of XLIFFWriter
		assertTrue("<g id='1' is missing: " + actual, actual.contains("<g id=\"1\""));
	}

	@Test
	public void testKeepCtypeBptRid() throws Exception {
		String actual = ensureResultContainsCtype("<bpt id=\"1\" ctype=\"bold\" rid=\"99\"/>t1<ept id=\"2\" rid=\"99\"/>", "bold");
		// we expect a g element as output because we use the "placeholderMode" of XLIFFWriter
		assertTrue("<g id='1' is missing: " + actual, actual.contains("<g id=\"1\""));
	}

	@Test
	public void testKeepCtypeX() throws Exception {
		// bold not a default value for x-tags
		// see http://docs.oasis-open.org/xliff/v1.2/os/xliff-core.html#ctype
		ensureResultContainsCtype("<x id=\"1\" ctype=\"lb\"/>t1", "lb");
	}

	@Test
	public void testKeepCtypeXBoldAsXBold() throws Exception {
		// bold not a default value for x-tags
		// see http://docs.oasis-open.org/xliff/v1.2/os/xliff-core.html#ctype
		ensureResultContainsCtype("<x id=\"1\" ctype=\"bold\"/>t1", "x-bold");
	}

	@Test
	public void testTargetIsSegmentedIdsAreNumbers() throws Exception {
		String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r<xliff version=\"1.2\">\r"
				+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
				+ "<body>"
				+ "<trans-unit id=\"55b0705f-c181-4e97-8d54-a574d16f6308\">"
				+ "<source><g id=\"1\"><g id=\"2\">One or two sentences </g></g></source>"
				+ "<seg-source><g id=\"1\"><g id=\"2\"><mrk mtype=\"seg\" mid=\"274\">One or two sentences</mrk> </g></g></seg-source>"
				+ "<target><g id=\"1\"><g id=\"2\"><mrk mtype=\"seg\" mid=\"274\">One or two sentences</mrk> </g></g></target></trans-unit>"
				+ "</body></file></xliff>";

		filter.getParameters().setBalanceCodes(false);

		String actual = filterAndWrite(input);
		assertThat(actual).contains("id=\"1\"");
		assertThat(actual).contains("id=\"2\"");
	}

	@Test
	public void testTargetIsSegmentedIdsAreStrings() throws Exception {
		String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
				+ "<xliff version=\"1.2\">\r"
				+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
				+ "<body>"
				+ "<trans-unit id=\"55b0705f-c181-4e97-8d54-a574d16f6308\">"
				+ "<source><g id=\"pt1819\"><g id=\"pt1820\">One or two sentences </g></g></source>"
				+ "<seg-source><g id=\"pt1819\"><g id=\"pt1820\"><mrk mtype=\"seg\" mid=\"274\">One or two sentences</mrk> </g></g></seg-source>"
				+ "<target><g id=\"pt1819\"><g id=\"pt1820\"><mrk mtype=\"seg\" mid=\"274\">One or two sentences</mrk> </g></g></target>"
				+ "</trans-unit></body></file></xliff>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
				+ "<xliff its:version=\"2.0\" version=\"1.2\"\n"
				+ "    xmlns=\"urn:oasis:names:tc:xliff:document:1.2\"\n"
				+ "    xmlns:its=\"http://www.w3.org/2005/11/its\"\n"
				+ "    xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" xmlns:okp=\"okapi-framework:xliff-extensions\">\n"
				+ "    <file datatype=\"x-test\" okp:inputEncoding=\"UTF-8\"\n"
				+ "        original=\"file.ext\" source-language=\"en\" target-language=\"fr\">"
				+ "<body>"
				+ "<trans-unit id=\"55b0705f-c181-4e97-8d54-a574d16f6308\">"
				+ "<source xml:lang=\"en\"><bx ctype=\"x-g\" id=\"1\"/><bx ctype=\"x-g\" id=\"2\"/>One or two sentences <ex id=\"2\"/><ex id=\"1\"/></source>"
				+ "<seg-source><bx ctype=\"x-g\" id=\"1\"/><bx ctype=\"x-g\" id=\"2\"/><mrk mtype=\"seg\" mid=\"274\">One or two sentences</mrk> <ex id=\"2\"/><ex id=\"1\"/></seg-source>"
				+ "<target xml:lang=\"fr\"><bx ctype=\"x-g\" id=\"1\"/><bx ctype=\"x-g\" id=\"2\"/><mrk mtype=\"seg\" mid=\"274\">One or two sentences</mrk> <ex id=\"2\"/><ex id=\"1\"/></target>"
				+ "</trans-unit></body></file></xliff>";

		String actual = filterAndWrite(input);
		assertThat(actual).isXmlEqualTo(expected);
	}

	private String ensureResultContainsCtype(String content, String expectedCtype) throws IOException {
		String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r<xliff version=\"1.2\">\r"
				+ "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
				+ "<body>"
				+ "<trans-unit id=\"1\">"
				+ "<source>" + content + "</source>"
				+ "<target>" + content + "</target>"
				+ "</trans-unit>"
				+ "</body>"
				+ "</file></xliff>";

		String actual = filterAndWrite(input);
		assertTrue("Result does not contain ctype " + expectedCtype + ": " +
				actual, actual.contains("ctype=\"" + expectedCtype + "\""));
		return actual;
	}

	private String filterAndWrite(String input) throws IOException {
		ArrayList<Event> events = getEvents(input);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		XLIFFWriter xliffWriter = new XLIFFWriter();
		xliffWriter.setOptions(LocaleId.FRENCH, null);
		xliffWriter.getParameters().setPlaceholderMode(true);
		xliffWriter.getParameters().setIncludeCodeAttrs(true);
		xliffWriter.setOutput(outputStream);
		for (Event event : events) {
			xliffWriter.handleEvent(event);
		}
		xliffWriter.close();
		outputStream.close();

		return new String(outputStream.toByteArray(), "UTF-8");
	}

	private ArrayList<Event> getEvents(String snippet) {
		return getEvents(snippet, filter);
	}

	private ArrayList<Event> getEvents (String snippet,
			XLIFFFilter filterToUse) {
		return FilterTestDriver.getEvents(filterToUse, snippet, locEN, locFR);
	}

}
