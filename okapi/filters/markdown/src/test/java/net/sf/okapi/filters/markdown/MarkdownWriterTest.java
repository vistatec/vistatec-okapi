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

package net.sf.okapi.filters.markdown;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

public class MarkdownWriterTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void writeDocumentParts() throws Exception {
        MarkdownFilter filter = new MarkdownFilter();
        IFilterWriter writer = filter.createFilterWriter();

        Path path = tempFolder.newFile().toPath();
        OutputStream os = Files.newOutputStream(path);
        writer.setOutput(os);
        writer.setOptions(LocaleId.FRENCH, StandardCharsets.UTF_8.name());

        filter.open(new RawDocument("[Link](<https://www.google.com>)\n\n", null, null));

        while (filter.hasNext()) {
            writer.handleEvent(filter.next());
        }

        filter.close();
        writer.close();

        String outputData = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertEquals("[Link](<https://www.google.com>)\n\n", outputData);
    }

    @Test
    public void writeTextUnitsAndDocumentPartsText() throws Exception {
        MarkdownFilter filter = new MarkdownFilter();
        IFilterWriter writer = filter.createFilterWriter();

        Path path = tempFolder.newFile().toPath();
        OutputStream os = Files.newOutputStream(path);
        writer.setOutput(os);
        writer.setOptions(LocaleId.FRENCH, StandardCharsets.UTF_8.name());

        filter.open(new RawDocument("First text unit\n\nSecond text unit", null, null));

        while (filter.hasNext()) {
            Event event = filter.next();
            if (event.isTextUnit()) {
                ITextUnit tu = event.getTextUnit();
                TextContainer tc = tu.createTarget(LocaleId.FRENCH, false, IResource.COPY_ALL);
                TextFragment tf = tc.getFirstContent();
                tf.setCodedText(tf.getCodedText().toUpperCase());
            }
            writer.handleEvent(event);
        }

        filter.close();
        writer.close();

        String outputData = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertEquals("FIRST TEXT UNIT\n\nSECOND TEXT UNIT\n\n", outputData);
    }

    @Test
    public void writeTextUnitsAndDocumentPartsHtml() throws Exception {
        MarkdownFilter filter = new MarkdownFilter();
        IFilterWriter writer = filter.createFilterWriter();

        Path path = tempFolder.newFile().toPath();
        OutputStream os = Files.newOutputStream(path);
        writer.setOutput(os);
        writer.setOptions(LocaleId.FRENCH, StandardCharsets.UTF_8.name());

        filter.open(new RawDocument("This contains <span>some inline</span> HTML\n\n", null, null));

        while (filter.hasNext()) {
            Event event = filter.next();
            if (event.isTextUnit()) {
                ITextUnit tu = event.getTextUnit();
                TextContainer tc = tu.createTarget(LocaleId.FRENCH, false, IResource.COPY_ALL);
                TextFragment tf = tc.getFirstContent();
                tf.setCodedText(tf.getCodedText().toUpperCase());
            }
            writer.handleEvent(event);
        }

        filter.close();
        writer.close();

        String outputData = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertEquals("THIS CONTAINS <span>SOME INLINE</span> HTML\n\n", outputData);
    }

    @Test
    public void writeTextUnitsAndDocumentPartsList() throws Exception {
        MarkdownFilter filter = new MarkdownFilter();
        IFilterWriter writer = filter.createFilterWriter();

        Path path = tempFolder.newFile().toPath();
        OutputStream os = Files.newOutputStream(path);
        writer.setOutput(os);
        writer.setOptions(LocaleId.FRENCH, StandardCharsets.UTF_8.name());

        filter.open(new RawDocument("This is a list:\n\n" + "* First\nelement\n\n"
                + "* Second element\n\n" + "End of the list", null, null));

        while (filter.hasNext()) {
            Event event = filter.next();
            if (event.isTextUnit()) {
                ITextUnit tu = event.getTextUnit();
                TextContainer tc = tu.createTarget(LocaleId.FRENCH, false, IResource.COPY_ALL);
                TextFragment tf = tc.getFirstContent();
                tf.setCodedText(tf.getCodedText().toUpperCase());
            }
            writer.handleEvent(event);
        }

        filter.close();
        writer.close();

        String outputData = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertEquals("THIS IS A LIST:\n\n* FIRST\nELEMENT\n\n"
                + "* SECOND ELEMENT\n\nEND OF THE LIST\n\n", outputData);
    }

    @Test
    public void testCommonMarkRoundTrip() throws Exception {
        testRoundTrip("commonmark_original.md");
    }

    @Test
    public void testCommonMarkChangedOutput() throws Exception {
        testChangedOutput("commonmark_original.md", "commonmark_changed.md");
    }

    @Test
    public void testListsRoundTrip() throws Exception {
        testRoundTrip("lists_original.md");
    }

    @Test
    public void testListChangedOutput() throws Exception {
       testChangedOutput("lists_original.md", "lists_changed.md");
    }

    @Test
    public void testTable1RoundTrip() throws Exception {
        testRoundTrip("table1_original.md");
    }

    @Test
    public void testTable1ChangedOutput() throws Exception {
        testChangedOutput("table1_original.md", "table1_changed.md");
    }

    @Test
    public void testTable2RoundTrip() throws Exception {
        testRoundTrip("table2_original.md");
    }

    @Test
    public void testTable2ChangedOutput() throws Exception {
        testChangedOutput("table2_original.md", "table2_changed.md");
    }

    private void testRoundTrip(String originalFile) throws Exception {
        MarkdownFilter filter = new MarkdownFilter();
        String contents = getFileContents(originalFile);

        List<Event> events = FilterTestDriver.getEvents(filter, contents, null, LocaleId.FRENCH);

        EncoderManager em = new EncoderManager();
        em.setAllKnownMappings();
        assertEquals(contents, FilterTestDriver.generateOutput(events, em, LocaleId.FRENCH));
    }

    private void testChangedOutput(String originalFile, String changedFile) throws Exception {
        MarkdownFilter filter = new MarkdownFilter();

        List<Event> events = FilterTestDriver.getEvents(filter,
                getFileContents(originalFile), null, LocaleId.FRENCH);

        EncoderManager em = new EncoderManager();
        em.setAllKnownMappings();
        assertEquals(getFileContents(changedFile),
                FilterTestDriver.generateChangedOutput(events, em, LocaleId.FRENCH));
    }

    private String getFileContents(String filename) throws Exception {
        try (InputStream is = MarkdownWriterTest.class.getResourceAsStream(filename);
                Scanner scanner = new Scanner(is)) {
            return scanner.useDelimiter("\\A").next();
        }
    }

}
