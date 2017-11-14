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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class XLIFFFilterLengthConstraintsTest {

    private static final Logger log = LoggerFactory.getLogger(XLIFFFilterLengthConstraintsTest.class);

    private XLIFFFilter filter;

    private LocaleId locEN = LocaleId.fromString("en");

    private LocaleId locFR = LocaleId.fromString("fr");

    @Before
    public void setUp() {
        filter = new XLIFFFilter();
    }

    @Test
    public void testTransUnit() throws Exception {
        test("<trans-unit id=\"1\" maxwidth=\"100\" size-unit=\"char\">"
                        + "<source>hello</source></trans-unit>",
                "maxwidth=\"100\"", "size-unit=\"char\"");
    }

    @Test
    public void testSizeUnitDefault() throws Exception {
        test("<trans-unit id=\"1\" maxwidth=\"100\">"
                        + "<source>hello</source></trans-unit>",
                "maxwidth=\"100\"", "size-unit=\"pixel\"");
    }

    @Test
    public void testGroup() throws Exception {
        test("<group maxwidth=\"100\" size-unit=\"char\">"
                        + "<trans-unit id=\"1\"><source>hello</source></trans-unit>"
                        + "<trans-unit id=\"2\"><source>world</source></trans-unit>"
                        + "</group>",
                "maxwidth=\"100\"", "size-unit=\"char\"");
    }

    private void test(String content, String... expectedContent) throws IOException {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r<xliff version=\"1.2\">\r"
                + "<file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
                + "<body>"
                + content
                + "</body>"
                + "</file></xliff>";

        String actual = filterAndWrite(input);
        log.debug("Input: {}", input);
        log.debug("Actual: {}", actual);

        assertThat(actual).contains(expectedContent);
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

    private ArrayList<Event> getEvents(String snippet,
            XLIFFFilter filterToUse) {
        return FilterTestDriver.getEvents(filterToUse, snippet, locEN, locFR);
    }

}
