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
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;

public class MarkdownFilterTest {

    @Test
    public void testCloseWithoutInput() throws Exception {
        MarkdownFilter filter = new MarkdownFilter();
        filter.close();
    }

    @Test
    public void testEventsFromEmptyInput() {
        String snippet = "";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(2, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            assertEquals(EventType.END_DOCUMENT, events.get(1).getEventType());
        }
    }

    @Test
    public void testAutoLink() {
        String snippet = "<https://www.google.com>";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(5, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            assertDocumentPart(events.get(1), "<https://www.google.com>");
            assertDocumentPart(events.get(2), System.lineSeparator());
            assertDocumentPart(events.get(3), System.lineSeparator());
            assertEquals(EventType.END_DOCUMENT, events.get(4).getEventType());
        }
    }

    @Test
    public void testBlockQuoteEvents() {
        String snippet = "> Blockquote";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(6, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            assertDocumentPart(events.get(1), "> ");
            assertTextUnit(events.get(2), "Blockquote");
            assertDocumentPart(events.get(3), System.lineSeparator());
            assertDocumentPart(events.get(4), System.lineSeparator());
            assertEquals(EventType.END_DOCUMENT, events.get(5).getEventType());
        }
    }

    @Test
    public void testBulletList() {
        String snippet = "* First\nelement\n\n" + "* Second element\n\n" + "* Third element\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(14, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());

            assertDocumentPart(events.get(1), "* ");
            assertTextUnit(events.get(2), "First\nelement");
            assertDocumentPart(events.get(3), "\n");
            assertDocumentPart(events.get(4), "\n");

            assertDocumentPart(events.get(5), "* ");
            assertTextUnit(events.get(6), "Second element");
            assertDocumentPart(events.get(7), "\n");
            assertDocumentPart(events.get(8), "\n");

            assertDocumentPart(events.get(9), "* ");
            assertTextUnit(events.get(10), "Third element");
            assertDocumentPart(events.get(11), "\n");
            assertDocumentPart(events.get(12), "\n");

            assertEquals(EventType.END_DOCUMENT, events.get(13).getEventType());
        }
    }

    @Test
    public void testCode() {
        String snippet = "`Text`";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(6, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            assertDocumentPart(events.get(1), "`");
            assertTextUnit(events.get(2), "Text`", "`");
            assertDocumentPart(events.get(3), System.lineSeparator());
            assertDocumentPart(events.get(4), System.lineSeparator());
            assertEquals(EventType.END_DOCUMENT, events.get(5).getEventType());
        }
    }

    @Test
    public void testEmphasis() {
        String snippet = "_Text_";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(6, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            assertDocumentPart(events.get(1), "_");
            assertTextUnit(events.get(2), "Text_", "_");
            assertDocumentPart(events.get(3), System.lineSeparator());
            assertDocumentPart(events.get(4), System.lineSeparator());
            assertEquals(EventType.END_DOCUMENT, events.get(5).getEventType());
        }
    }

    @Test
    public void testFencedCodeBlock() {
        String snippet = "```{java}\n"
                + "Content in a fenced code block\n"
                + "```\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(10, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());

            assertDocumentPart(events.get(1), "```");
            assertDocumentPart(events.get(2), "{java}");
            assertDocumentPart(events.get(3), "\n");
            assertTextUnit(events.get(4), "Content in a fenced code block");
            assertDocumentPart(events.get(5), "\n");
            assertDocumentPart(events.get(6), "```");
            assertDocumentPart(events.get(7), "\n");
            assertDocumentPart(events.get(8), "\n");

            assertEquals(EventType.END_DOCUMENT, events.get(9).getEventType());
        }
    }

    @Test
    public void testHeadingPrefix() {
        String snippet = "# Heading";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(6, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            assertDocumentPart(events.get(1), "# ");
            assertTextUnit(events.get(2), "Heading");
            assertDocumentPart(events.get(3), System.lineSeparator());
            assertDocumentPart(events.get(4), System.lineSeparator());
            assertEquals(EventType.END_DOCUMENT, events.get(5).getEventType());
        }
    }

    @Test
    public void testHeadingPrefixWithoutSpace() {
        // The tokens are of type TEXT but are meant to be headers, so have the filter convert the '#'s to code
        String snippet = "#Heading 1\n\n" + "##Heading 2\n\n" + "###Heading 3";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);

            assertEquals(11, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());

            assertTextUnit(events.get(1), "#Heading 1", "#");
            assertDocumentPart(events.get(2), "\n");
            assertDocumentPart(events.get(3), "\n");

            assertTextUnit(events.get(4), "##Heading 2", "##");
            assertDocumentPart(events.get(5), "\n");
            assertDocumentPart(events.get(6), "\n");

            assertTextUnit(events.get(7), "###Heading 3", "###");
            assertDocumentPart(events.get(8), "\n");
            assertDocumentPart(events.get(9), "\n");

            assertEquals(EventType.END_DOCUMENT, events.get(10).getEventType());
        }
    }

    @Test
    public void testHeadingUnderline() {
        String snippet = "Heading\n=======\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(7, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());

            assertTextUnit(events.get(1), "Heading");
            assertDocumentPart(events.get(2), "\n");
            assertDocumentPart(events.get(3), "=======");
            assertDocumentPart(events.get(4), "\n");
            assertDocumentPart(events.get(5), "\n");

            assertEquals(EventType.END_DOCUMENT, events.get(6).getEventType());
        }
    }

    @Test
    public void testHtmlBlock() {
        String snippet = "<table><tr><td>Test</td></tr></table>\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(5, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());

            assertTextUnit(events.get(1), "<table><tr><td>Test</td></tr></table>",
                    "<table>", "<tr>", "<td>", "</td>", "</tr>", "</table>");
            assertDocumentPart(events.get(2), "\n");
            assertDocumentPart(events.get(3), "\n");

            assertEquals(EventType.END_DOCUMENT, events.get(4).getEventType());
        }
    }

    @Test
    public void testHtmlBlockWithMarkdown() {
        String snippet = "<table><tr><td>\n\n**Bold**\n\n*Italic*\n\n</td></tr></table>\n\n";;

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(16, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());

            assertDocumentPart(events.get(1), "<table><tr><td>");
            assertDocumentPart(events.get(2), "\n");
            assertDocumentPart(events.get(3), "\n");
            assertDocumentPart(events.get(4), "**");
            assertTextUnit(events.get(5), "Bold**", "**");
            assertDocumentPart(events.get(6), "\n");
            assertDocumentPart(events.get(7), "\n");
            assertDocumentPart(events.get(8), "*");
            assertTextUnit(events.get(9), "Italic*", "*");
            assertDocumentPart(events.get(10), "\n");
            assertDocumentPart(events.get(11), "\n");
            assertDocumentPart(events.get(12), "</td></tr></table>");
            assertDocumentPart(events.get(13), "\n");
            assertDocumentPart(events.get(14), "\n");

            assertEquals(EventType.END_DOCUMENT, events.get(15).getEventType());
        }
    }

    @Test
    public void testHtmlInline() {
        String snippet = "This contains <span>some inline</span> HTML\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(5, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());

            assertTextUnit(events.get(1), "This contains <span>some inline</span> HTML",
                    "<span>", "</span>");
            assertDocumentPart(events.get(2), "\n");
            assertDocumentPart(events.get(3), "\n");

            assertEquals(EventType.END_DOCUMENT, events.get(4).getEventType());
        }
    }

    @Test
    public void testHtmlInlineWithAttributes() {
        String snippet = "Sentence 1. <span class=\"foo\">Sentence 2.</span> Sentence 3.";
        try (MarkdownFilter filter = new MarkdownFilter()) {
            List<ITextUnit> tus =
                    FilterTestDriver.filterTextUnits(FilterTestDriver.getEvents(filter, snippet, null));
            assertEquals(1, tus.size());
            assertTextUnit(tus.get(0), "Sentence 1. <span class=\"foo\">Sentence 2.</span> Sentence 3.",
                           "<span class=\"foo\">", "</span>");
        }
    }

    @Test
    public void testHtmlBreakElement() {
        String snippet = "<p>Sentence 1.<br/>Sentence 2.</p>";
        try (MarkdownFilter filter = new MarkdownFilter()) {
            List<ITextUnit> tus =
                    FilterTestDriver.filterTextUnits(FilterTestDriver.getEvents(filter, snippet, null));
            assertEquals(1, tus.size());
            assertTextUnit(tus.get(0), "<p>Sentence 1.<br/>Sentence 2.</p>", "<p>", "<br/>", "</p>");
        }
    }

    @Test
    public void testImage() {
        String snippet = "Here is an ![Image](https://www.google.com)\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(5, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            assertEquals(EventType.TEXT_UNIT, events.get(1).getEventType());
            assertEquals("Here is an Image",
                         events.get(1).getTextUnit().getSource().getCodedText());
            assertEquals(EventType.DOCUMENT_PART, events.get(2).getEventType());
            assertEquals(EventType.DOCUMENT_PART, events.get(3).getEventType());
            assertEquals(EventType.END_DOCUMENT, events.get(4).getEventType());
        }
    }

    @Test
    public void testExtractImageTitleAndAltText() {
        String snippet = "![alt text](https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png"
                         + " \"Logo Title Text 1\")";
        try (MarkdownFilter filter = new MarkdownFilter()) {
            List<ITextUnit> tus = FilterTestDriver.filterTextUnits(FilterTestDriver.getEvents(filter, snippet, null));
            assertEquals(1, tus.size());
            assertEquals("alt textLogo Title Text 1", tus.get(0).getSource().getCodedText());
        }
    }

    @Test
    public void testExtractImageTitleButNotAltText() {
        String snippet = "![alt text](https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png"
                         + " \"Logo Title Text 1\")";
        try (MarkdownFilter filter = new MarkdownFilter()) {
            Parameters params = filter.getParameters();
            params.setTranslateImageAltText(false);
            List<ITextUnit> tus = FilterTestDriver.filterTextUnits(FilterTestDriver.getEvents(filter, snippet, null));
            assertEquals(1, tus.size());
            assertEquals("Logo Title Text 1", tus.get(0).getSource().getCodedText());
        }
    }

    @Test
    public void testImageWithTranslatableUrl() {
        String snippet = "Here is an ![Image](https://www.google.com)\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            filter.getParameters().setTranslateUrls(true);
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            List<ITextUnit> tus = FilterTestDriver.filterTextUnits(events);
            assertEquals(1, tus.size());
            assertEquals("Here is an Imagehttps://www.google.com",
                         tus.get(0).getSource().getCodedText());
            assertEquals(EventType.DOCUMENT_PART, events.get(2).getEventType());
            assertEquals(EventType.DOCUMENT_PART, events.get(3).getEventType());
            assertEquals(EventType.END_DOCUMENT, events.get(4).getEventType());
        }
    }

    @Test
    public void testImageRef() {
        String snippet = "![Image][A]\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(10, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            for (int i = 1; i < 8; i++) {
                assertEquals(EventType.DOCUMENT_PART, events.get(i).getEventType());
            }
            assertEquals(EventType.END_DOCUMENT, events.get(9).getEventType());
        }
    }

    @Test
    public void testIndentedCodeBlock() {
        String snippet = "    This is text\n"
                + "    in an indented\n"
                + "    code block\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(4, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());

            assertTextUnit(events.get(1),
                      "    This is text\n"
                    + "    in an indented\n"
                    + "    code block\n",
                    "    ", "\n", "    ", "\n", "    ", "\n");
            assertDocumentPart(events.get(2), "\n");

            assertEquals(EventType.END_DOCUMENT, events.get(3).getEventType());
        }
    }

    @Test
    public void testLink() {
        String snippet = "This is a [Link](<https://www.google.com>)\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            List<ITextUnit> tus = FilterTestDriver.filterTextUnits(events);
            assertEquals(1, tus.size());
            assertEquals("This is a Link", tus.get(0).getSource().getCodedText());
        }
    }

    @Test
    public void testLinkWithTranslatableUrl() {
        String snippet = "This is a [Link](<https://www.google.com>)\n\n";
        try (MarkdownFilter filter = new MarkdownFilter()) {
            Parameters params = filter.getParameters();
            params.setTranslateUrls(true);
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            List<ITextUnit> tus = FilterTestDriver.filterTextUnits(events);
            assertEquals(1, tus.size());
            assertEquals("This is a Linkhttps://www.google.com", tus.get(0).getSource().getCodedText());
        }
    }

    @Test
    public void testLinkRef() {
        String snippet = "[Link][A]\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(10, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            for (int i = 1; i < 8; i++) {
                assertEquals(EventType.DOCUMENT_PART, events.get(i).getEventType());
            }
            assertEquals(EventType.END_DOCUMENT, events.get(9).getEventType());
        }
    }

    @Test
    public void testReferenceDefinition() {
        String snippet = "[1]: https://www.google.com 'Google'\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(11, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            for (int i = 1; i < 9; i++) {
                assertEquals(EventType.DOCUMENT_PART, events.get(i).getEventType());
            }
            assertEquals(EventType.END_DOCUMENT, events.get(10).getEventType());
        }
    }

    @Test
    public void testStrikethroughSubscript() {
        String snippet = "Some ~~strikethrough~~ ~subscript~ text";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(5, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            assertTextUnit(events.get(1), "Some ~~strikethrough~~ ~subscript~ text",
                    "~~", "~~", "~", "~");
            assertDocumentPart(events.get(2), System.lineSeparator());
            assertDocumentPart(events.get(3), System.lineSeparator());
            assertEquals(EventType.END_DOCUMENT, events.get(4).getEventType());
        }
    }

    @Test
    public void testThematicBreak() {
        String snippet = "---\n\n";

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getEvents(filter, snippet, null);
            assertEquals(5, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            assertDocumentPart(events.get(1), "---");
            assertDocumentPart(events.get(2), "\n");
            assertDocumentPart(events.get(3), "\n");
            assertEquals(EventType.END_DOCUMENT, events.get(4).getEventType());
        }
    }

    @Test
    public void testTable1TextUnits() throws Exception {
        RawDocument rd =  new RawDocument(getFileContents("table1_original.md"), LocaleId.ENGLISH);

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, rd);
            assertEquals(4, events.size());
            assertTextUnit(events.get(0), "Command");
            assertTextUnit(events.get(1), "Description");
            assertTextUnit(events.get(2), "git status`", "`"); // Leading "`" is a document part
            assertTextUnit(events.get(3), "List all **new** or _modified_ files", "**", "**", "_", "_");
        }
    }

    @Test
    public void testTable2TextUnits() throws Exception {
        RawDocument rd =  new RawDocument(getFileContents("table2_original.md"), LocaleId.ENGLISH);

        try (MarkdownFilter filter = new MarkdownFilter()) {
            ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, rd);
            assertEquals(10, events.size());
            assertTextUnit(events.get(0), "Left-aligned");
            assertTextUnit(events.get(1), "Center-aligned");
            assertTextUnit(events.get(2), "Right-aligned");
            assertTextUnit(events.get(3), "git status");
            assertTextUnit(events.get(4), "git status");
            assertTextUnit(events.get(5), "git status");
            assertTextUnit(events.get(6), "git diff");
            assertTextUnit(events.get(7), "git diff");
            assertTextUnit(events.get(8), "git diff");
            assertTextUnit(events.get(9), "GitHub](http://github.com)", "](http://github.com)");
        }
    }

    @Test
    public void testDontTranslateFencedCodeBlocks() throws Exception {
        RawDocument rd = new RawDocument(getFileContents("code_and_codeblock_tests.md"), LocaleId.ENGLISH);
        try (MarkdownFilter filter = new MarkdownFilter()) {
            filter.getParameters().setTranslateCodeBlocks(false);
            List<ITextUnit> tus = FilterTestDriver.filterTextUnits(FilterTestDriver.getTextUnitEvents(filter, rd));
            assertTextUnit(tus.get(0), "Code and Codeblock tests");
            assertTextUnit(tus.get(1), "Code blocks");
            assertTextUnit(tus.get(2), "There are two ways to specify code blocks. One may delimit via four  tildas, like this:");
            assertTextUnit(tus.get(3), "Another is to delimit with three ticks like this:");
            assertTextUnit(tus.get(4), "One may also specify that the code is to be treated with syntax coloring like this:");
            assertTextUnit(tus.get(5), "Inline code blocks");
            assertTextUnit(tus.get(6), "Inline code contain things like `variable names` that we may want to protect.", "`variable names`");
        }
    }

    @Test
    public void testTranslateFencedCodeBlocks() throws Exception {
        RawDocument rd = new RawDocument(getFileContents("code_and_codeblock_tests.md"), LocaleId.ENGLISH);
        try (MarkdownFilter filter = new MarkdownFilter()) {
            List<ITextUnit> tus = FilterTestDriver.filterTextUnits(FilterTestDriver.getTextUnitEvents(filter, rd));
            assertTextUnit(tus.get(0), "Code and Codeblock tests");
            assertTextUnit(tus.get(1), "Code blocks");
            assertTextUnit(tus.get(2), "There are two ways to specify code blocks. One may delimit via four  tildas, like this:");
            assertTextUnit(tus.get(3), "This text is within a code block and should remain in English.");
            assertTextUnit(tus.get(4), "Another is to delimit with three ticks like this:");
            assertTextUnit(tus.get(5), "This text is within a code block and should remain in English.");
            assertTextUnit(tus.get(6), "One may also specify that the code is to be treated with syntax coloring like this:");
            assertTextUnit(tus.get(7), "This text is within a code block and should remain in English.");
            assertTextUnit(tus.get(8), "Inline code blocks");
            assertTextUnit(tus.get(9), "Inline code contain things like `variable names` that we may want to protect.", "`", "`");
        }
    }

    @Test
    public void testDontTranslateMetadataHeader() throws Exception {
        RawDocument rd = new RawDocument(getFileContents("metadata_header.md"), LocaleId.ENGLISH);
        try (MarkdownFilter filter = new MarkdownFilter()) {
            // This is the default behavior
            List<ITextUnit> tus = FilterTestDriver.filterTextUnits(FilterTestDriver.getTextUnitEvents(filter, rd));
            assertTextUnit(tus.get(0), "This should be the only translatable segment.");
        }
    }
    @Test
    public void testTranslateMetadataHeader() throws Exception {
        RawDocument rd = new RawDocument(getFileContents("metadata_header.md"), LocaleId.ENGLISH);
        try (MarkdownFilter filter = new MarkdownFilter()) {
            filter.getParameters().setTranslateHeaderMetadata(true);
            List<ITextUnit> tus = FilterTestDriver.filterTextUnits(FilterTestDriver.getTextUnitEvents(filter, rd));
            assertTextUnit(tus.get(0), "value");
            assertTextUnit(tus.get(1), "value1, value2");
            assertTextUnit(tus.get(2), "This should be the only translatable segment.");
        }
    }

    /** Assert the event is a text unit with the given content and given codes. */
    private void assertTextUnit(Event event, String content, String... codes) {
        assertEquals(EventType.TEXT_UNIT, event.getEventType());
        assertTrue(event.getTextUnit().isTranslatable());
        assertTextUnit(event.getTextUnit(), content, codes);
    }
    private void assertTextUnit(ITextUnit tu, String content, String... codes) {
        assertEquals(content, tu.toString());
        assertEquals(codes.length, getNumCodes(tu));
        for (int i = 0; i < codes.length; i++) {
            assertEquals(codes[i], getCodeString(tu, i));
        }
    }

    private int getNumCodes(ITextUnit tu) {
        return tu.getSource().getFirstContent().getCodes().size();
    }
    private String getCodeString(ITextUnit tu, int index) {
        return tu.getSource().getFirstContent().getCodes().get(index).toString();
    }

    /** Assert the event is a document part with the given content. */
    private void assertDocumentPart(Event event, String content) {
        assertEquals(EventType.DOCUMENT_PART, event.getEventType());
        assertEquals(content, event.getDocumentPart().toString());
    }

    private String getFileContents(String filename) throws Exception {
        try (InputStream is = MarkdownWriterTest.class.getResourceAsStream(filename);
                Scanner scanner = new Scanner(is)) {
            return scanner.useDelimiter("\\A").next();
        }
    }

}
