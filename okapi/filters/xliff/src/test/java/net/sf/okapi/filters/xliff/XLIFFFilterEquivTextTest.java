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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filterwriter.XLIFFWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This test makes sure that inline code attribute "equiv-text" is restored when filtering and
 * re-writing XLIFF content.
 *
 * @see <a href="http://docs.oasis-open.org/xliff/v1.2/os/xliff-core.html#equiv-text">Xliff 1.2 specification</a>
 */
@RunWith(JUnit4.class)
public class XLIFFFilterEquivTextTest {

    private XLIFFFilter filter;

    private LocaleId locEN = LocaleId.fromString("en");

    private LocaleId locFR = LocaleId.fromString("fr");

    @Before
    public void setUp() {
        filter = new XLIFFFilter();
    }

    @Test
    public void testKeepEquivTextGHello() throws Exception {
        ensureResultContainsEquivText("<g id=\"1\" equiv-text=\"hello\">foo</g>", "hello");
    }

    @Test
    public void testKeepEquivTextGCustom() throws Exception {
        ensureResultContainsEquivText("<g id=\"1\" equiv-text=\"x-custom\">foo</g>", "x-custom");
    }

    @Test
    public void testKeepEquivTextX() throws Exception {
        ensureResultContainsEquivText("<x id=\"1\" equiv-text=\"hello\"/>foo", "hello");
    }

    @Test
    public void testKeepEquivTextXWithEscapedContent() throws Exception {
        ensureResultContainsEquivText("<x id=\"1\" equiv-text=\"{&quot;hello&quot;}\"/>foo", "{&quot;hello&quot;}");
    }

    @Test
    public void testKeepEquivTextBx() throws Exception {
        ensureResultContainsEquivText("<bx id=\"1\" equiv-text=\"hello\"/>foo", "hello");
    }

    @Test
    public void testKeepEquivTextEx() throws Exception {
        ensureResultContainsEquivText("<ex id=\"1\" equiv-text=\"hello\"/>foo", "hello");
    }

    @Test
    public void testKeepEquivTextBxEx() throws Exception {
        ensureResultContainsEquivText("<bx id=\"1\" equiv-text=\"hello\"/>foo<ex id=\"1\" equiv-text=\"hello\"/>", "hello");
    }

    @Test
    public void testKeepEquivTextBpt() throws Exception {
        ensureResultContainsEquivText("<bpt id=\"1\" equiv-text=\"hello\">data</bpt>foo", "hello");
    }

    @Test
    public void testKeepEquivTextEpt() throws Exception {
        ensureResultContainsEquivText("<ept id=\"1\" equiv-text=\"hello\">data</ept>foo", "hello");
    }

    @Test
    public void testKeepEquivTextPh() throws Exception {
        ensureResultContainsEquivText("<ph id=\"1\" equiv-text=\"hello\">data</ph>foo", "hello");
    }

    @Test
    public void testKeepEquivTextIt() throws Exception {
        ensureResultContainsEquivText("<it id=\"1\" equiv-text=\"hello\" pos=\"open\">data</it>foo","hello");
    }

    private void ensureResultContainsEquivText(String content, String expectedEquivText)
            throws IOException, XMLStreamException {
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

        // check if xml is well-formed
        StringReader reader = new StringReader(actual);
        XMLEventReader actualXmlReader = XMLInputFactory.newFactory().createXMLEventReader(reader);
        while(actualXmlReader.hasNext()) {
            actualXmlReader.nextEvent();
        }

        assertTrue("Result does not contain equiv-text " + expectedEquivText + ": " +
                actual, actual.contains("equiv-text=\"" + expectedEquivText + "\""));
    }

    private String filterAndWrite(String input) throws IOException {
        ArrayList<Event> events = getEvents(input);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XLIFFWriter xliffWriter = new XLIFFWriter();
        xliffWriter.setOptions(LocaleId.FRENCH, null);
        xliffWriter.getParameters().setPlaceholderMode(true);
        xliffWriter.getParameters().setIncludeCodeAttrs(true);
        xliffWriter.getParameters().setIncludeIts(true);
        xliffWriter.setOutput(outputStream);
        for (Event event : events) {
            xliffWriter.handleEvent(event);
        }
        xliffWriter.close();
        outputStream.close();

        return new String(outputStream.toByteArray(), "UTF-8");
    }

    private ArrayList<Event> getEvents(String snippet) {
        return FilterTestDriver.getEvents(filter, snippet, locEN, locFR);
    }
}
