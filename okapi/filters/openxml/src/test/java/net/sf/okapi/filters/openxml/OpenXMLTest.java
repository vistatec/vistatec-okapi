package net.sf.okapi.filters.openxml;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiEncryptedDataException;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Miscellaneous OOXML tests.
 */
@RunWith(DataProviderRunner.class)
public class OpenXMLTest {
    private LocaleId locENUS = LocaleId.fromString("en-us");
    private final FileLocation root = FileLocation.fromClass(getClass());

    /**
     * Test to ensure the filter can handle an OOXML package in
     * which the [Content Types].xml document does not appear
     * as the first entry in the ZIP archive.
     * @throws Exception
     */
    @Test
    public void testReorderedZipPackage() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        URL url = root.in("/reordered-zip.docx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(),"UTF-8", locENUS);
        ArrayList<Event> events = getEvents(filter, doc);
        ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
        assertNotNull(tu);
        assertEquals("This is a test.", tu.getSource().getCodedText());
        tu = FilterTestDriver.getTextUnit(events, 2);
        assertEquals("Untitled document.docx", tu.getSource().toString());
    }

    /**
     * Test to ensure that the filter parses the file metadata
     * in order to present PPTX slides for translation in the order
     * they are viewed by the user.
     * @throws Exception
     */
    @Test
    public void testSlideReordering() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        URL url = root.in("/Okapi-325.pptx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        checkTu(events, 1, "<run1>Sample Presentation</run1>");
        checkTu(events, 2, "<run1>This is slide 1</run1>");
        checkTu(events, 3, "<run1>This is slide 2</run1>");
        checkTu(events, 4, "<run1>This is slide 3</run1>");
    }

    /**
     * Test that we expose document properties for PowerPoint files.
     */
    @Test
    public void testPPTXDocProperties() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setTranslateDocProperties(true);
        params.setTranslateComments(false);
        params.setTranslatePowerpointMasters(false);
        params.setTranslatePowerpointNotes(false);
        URL url = root.in("/DocProperties.pptx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(10, events.size());
        // The first 4 are body that we don't care about
        checkTu(events, 5, "Test of OOXML filter");
        checkTu(events, 6, "Okapi OOXML Filter");
        checkTu(events, 7, "Chase Tingley");
        checkTu(events, 8, "Okapi, filtering, OOXML, PPTX");
        checkTu(events, 9, "This is document property comment.");
        checkTu(events, 10, "Filters");
    }

    /**
     * Verify that disabling the option also works.
     */
    @Test
    public void testPPTXIgnoreDocProperties() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setTranslateDocProperties(false);
        params.setTranslateComments(false);
        params.setTranslatePowerpointMasters(false);
        params.setTranslatePowerpointNotes(false);
        URL url = root.in("/DocProperties.pptx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        // Only the 4 body segments are still there
        assertEquals(4, events.size());
    }

    /**
     * Test that PPTX comments are extracted.
     */
    @Test
    public void testPPTXComments() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setTranslateComments(true);
        params.setTranslateDocProperties(false);
        params.setTranslatePowerpointMasters(false);
        params.setTranslatePowerpointNotes(false);
        URL url = root.in("/Comments.pptx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(2, events.size());
        assertEquals("Comment on the title slide", events.get(0).getTextUnit().getSource().getCodedText());
        assertEquals("This is a comment on a slide body.", events.get(1).getTextUnit().getSource().getCodedText());
    }

    /**
     * Verify that disabling the option also works.
     */
    @Test
    public void testPPTXIgnoreComments() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setTranslateComments(false);
        params.setTranslateDocProperties(false);
        params.setTranslatePowerpointMasters(false);
        params.setTranslatePowerpointNotes(false);
        URL url = root.in("/Comments.pptx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(0, events.size());
    }

    private void dump(List<Event> events) {
        for (Event e : events) {
            if (!(e.getResource() instanceof ITextUnit)) continue;
            System.out.println(e.getTextUnit().getSource().getCodedText());
            System.out.println(e.getTextUnit().getSource().toString());
        }
    }

    @Test
    public void testXLSXOnlyExtractStringsNotNumbers() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setTranslateDocProperties(false);
        URL url = root.in("/sample.xlsx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(10, events.size());
        checkTu(events, 1, "Lorem");
        checkTu(events, 2, "ipsum");
        checkTu(events, 3, "dolor");
        checkTu(events, 4, "sit");
        checkTu(events, 5, "amet");
        checkTu(events, 6, "consectetuer");
        checkTu(events, 7, "adipiscing");
        checkTu(events, 8, "elit");
        checkTu(events, 9, "Nunc");
        checkTu(events, 10, "at");
    }

    /**
     * This test now captures the intended ordering behavior of the
     * string table, which is to expose strings in the order they appear
     * to the user, not the order in which they appear in the original
     * string table.
     */
    @Test
    public void testXLSXOrdering() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setTranslateDocProperties(false);
        URL url = root.in("/ordering.xlsx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(7, events.size());
        checkTu(events, 1, "Cell A2");
        checkTu(events, 2, "Cell B2");
        checkTu(events, 3, "Cell C3");
        checkTu(events, 4, "Sheet 2, Cell A1");
        checkTu(events, 5, "Sheet2, Cell B2");
        checkTu(events, 6, "Sheet2, Cell A3");
        checkTu(events, 7, "Sheet 3, Cell A1");
    }

    /**
     * Test for Excel column excludes.
     */
    @Test
    public void testXLSXExcludeAllColumns() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setTranslateDocProperties(false);
        params.setTranslateExcelExcludeColumns(true);
        params.tsExcelExcludedColumns = new TreeSet<String>();
        params.tsExcelExcludedColumns.add("1A");
        params.tsExcelExcludedColumns.add("1B");
        URL url = root.in("/columns.xlsx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        // Current behavior seems to be exposing them as placeholders
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(0, events.size());
        // Make sure it also works on styled text
        RawDocument rd2 = new RawDocument(root.in("/cell_styling.xlsx").asUri(), "UTF-8", locENUS);
        events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(0, events.size());
        rd2.close();
    }

    /**
     * Test for Excel Sheet Name Translation.
     */
    @Test
    public void testXLSXTranslateSheetNames() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        URL url = root.in("/sheet_names.xlsx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);

        // Test default no translation of sheet names
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(2, events.size());
        checkTu(events, 1, "Text in sheet 1");
        checkTu(events, 2, "Text in sheet 2");

        // Now with set to true to translate sheet names
        params.setTranslateExcelSheetNames(true);
        events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(4, events.size());
        checkTu(events, 3, "Sheet One");
        checkTu(events, 4, "Sheet Two");
    }

    /**
     * Test the case where the same string occurs in both excluded and non-excluded
     * contexts.
     */
    @Test
    public void testPartialExclusionFromColumns() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setTranslateDocProperties(false);

        // Parse once with default params, we should get both cells
        URL url = root.in("/shared_string_in_two_columns.xlsx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(2, events.size());
        assertEquals("Danger", events.get(0).getTextUnit().getSource().toString());
        assertEquals("Danger", events.get(1).getTextUnit().getSource().toString());

        // Now with excludes set, we only get one
        params.setTranslateExcelExcludeColumns(true);
        params.tsExcelExcludedColumns = new TreeSet<String>();
        params.tsExcelExcludedColumns.add("1A");
        doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(1, events.size());
        assertEquals("Danger", events.get(0).getTextUnit().getSource().toString());
    }

    @Test
    public void testSmartQuotes() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setTranslateDocProperties(false);
        URL url = root.in("/smartquotes.docx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(1, events.size());
        assertEquals("“Smart quotes”", events.get(0).getTextUnit().getSource().toString());
    }

    @Test
    public void testTabAsCharacter() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setAddTabAsCharacter(true);
        params.setTranslateDocProperties(false);
        URL url = root.in("/Document-with-tabs.docx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(1, events.size());
        assertEquals("Before\tafter.", events.get(0).getTextUnit().getSource().getCodedText());
    }

    @Test
    public void testTabAsCharacter2() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setAddTabAsCharacter(true);
        params.setTranslateDocProperties(false);
        URL url = root.in("/Document-with-tabs-2.docx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(1, events.size());
        assertEquals("Before\tafter.", events.get(0).getTextUnit().getSource().getCodedText());
    }

    @Test
    public void testTabAsTag() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setAddTabAsCharacter(false);
        params.setTranslateDocProperties(false);
        URL url = root.in("/Document-with-tabs.docx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(1, events.size());
        assertEquals("Beforeafter.", events.get(0).getTextUnit().getSource().getCodedText());
    }

    @Test
    public void testLineBreakAsCharacter() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setAddLineSeparatorCharacter(true);
        params.setTranslateDocProperties(false);
        URL url = root.in("/Document-with-soft-linebreaks.docx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(1, events.size());
        assertEquals("First line\nsecond line.", events.get(0).getTextUnit().getSource().getCodedText());
    }

    @Test
    public void testLineBreakAsTag() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setAddLineSeparatorCharacter(false);
        params.setTranslateDocProperties(false);
        URL url = root.in("/Document-with-soft-linebreaks.docx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(1, events.size());
        assertEquals("First linesecond line.", events.get(0).getTextUnit().getSource().getCodedText());
    }

    @Test
    public void testExcludeAllColors() throws Exception {
        OpenXMLFilter filter = new OpenXMLFilter();
        ConditionalParameters params = (ConditionalParameters)filter.getParameters();
        params.setTranslateDocProperties(false);
        params.setTranslateExcelExcludeColors(true);
        params.tsExcelExcludedColors = new TreeSet<String>();
        params.tsExcelExcludedColors.add("FF800000"); // dark red
        params.tsExcelExcludedColors.add("FFFF0000"); // red
        params.tsExcelExcludedColors.add("FFFF6600"); // orange
        params.tsExcelExcludedColors.add("FFFFFF00"); // yellow
        params.tsExcelExcludedColors.add("FFCCFFCC"); // light green
        params.tsExcelExcludedColors.add("FF008000"); // green
        params.tsExcelExcludedColors.add("FF3366FF"); // light blue
        params.tsExcelExcludedColors.add("FF0000FF"); // blue
        params.tsExcelExcludedColors.add("FF000090"); // dark blue
        params.tsExcelExcludedColors.add("FF660066"); // purple
        URL url = root.in("/standardcolors.xlsx").asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = FilterTestDriver.getTextUnitEvents(filter, doc);
        assertEquals(0, events.size());
    }

    @DataProvider
    public static Object[][] testHiddenTextExtractionProvider() {
        return new Object[][] {
                { //0
                        new String[] {
                                "Here is the [visible] <run1/>message [visible] <run2/>of Jeremiah [visible].",
                                "Here is the 5th message with direct vanish prop in pPr.",
                                "<run1>Here is the 6th message with direct false vanish prop overriding vanish prop in pPr.</run1>",
                                "<run1>Here is the 7th message with RunStyle1.</run1>",
                                "Here is the 8th message with ParagraphStyle1.",
                                "<run1>Here is the 9th message with RunStyle2.</run1>",
                                "Here is the 10th message with ParagraphStyle2.",
                                "<run1>Here is the 11th message with ParagraphStyle2 and RunStyle2.</run1>",
                                "<run1>Here is the 12th message with RunStyleB.</run1>",
                                "Here is the 13th message with ParagraphStyleB.",
                                "<run1>Here is the 14th message with RunStyleC.</run1>",
                                "Here is the 15th message with ParagraphStyleC.",
                                "<run1>Here is the 16th message with ParagraphStyleC and RunStyleC.</run1>",
                                "<run1>Here is the 17th message with direct false vanish prop overriding vanish prop in Haydn rStyle.</run1>",
                                "<run1>Here is the 18th message with direct false vanish prop overriding vanish prop in FranzJosef pStyle.</run1>",
                                "<run1>Here is the 19th message with direct false vanish prop overriding vanish prop in Haydn rStyle in pPr.</run1>",
                                "Here is the 20th message with Haydn rStyle in pPr.",
                        },
                        false,
                        new String[] {},
                },
                { //1
                        new String[] {
                                "Here is the [visible] <run1>hidden [direct vanish] </run1>message [visible] <run2>written by the hand [rStyle Haydn] </run2>of Jeremiah [visible].",
                                "Here is the message of Isaiah (with hidden pStyle FranzJosef).",
                                "<run1>Here is the message of Daniel (with both direct vanish props).</run1>",
                                "<run1>Here is the message of Peter, James & John (with simple direct vanish prop).</run1>",
                                "Here is the 5th message with direct vanish prop in pPr.",
                                "<run1>Here is the 6th message with direct false vanish prop overriding vanish prop in pPr.</run1>",
                                "<run1>Here is the 7th message with RunStyle1.</run1>",
                                "Here is the 8th message with ParagraphStyle1.",
                                "<run1>Here is the 9th message with RunStyle2.</run1>",
                                "Here is the 10th message with ParagraphStyle2.",
                                "<run1>Here is the 11th message with ParagraphStyle2 and RunStyle2.</run1>",
                                "<run1>Here is the 12th message with RunStyleB.</run1>",
                                "Here is the 13th message with ParagraphStyleB.",
                                "<run1>Here is the 14th message with RunStyleC.</run1>",
                                "Here is the 15th message with ParagraphStyleC.",
                                "<run1>Here is the 16th message with ParagraphStyleC and RunStyleC.</run1>",
                                "<run1>Here is the 17th message with direct false vanish prop overriding vanish prop in Haydn rStyle.</run1>",
                                "<run1>Here is the 18th message with direct false vanish prop overriding vanish prop in FranzJosef pStyle.</run1>",
                                "<run1>Here is the 19th message with direct false vanish prop overriding vanish prop in Haydn rStyle in pPr.</run1>",
                                "Here is the 20th message with Haydn rStyle in pPr.",
                        },
                        true,
                        new String[] {},
                },
                { //2
                        new String[] {
                                "Here is the [visible] <run1/>message [visible] <run2/>of Jeremiah [visible].",
                                "Here is the 5th message with direct vanish prop in pPr.",
                                "<run1>Here is the 6th message with direct false vanish prop overriding vanish prop in pPr.</run1>",
                                "Here is the 8th message with ParagraphStyle1.",
                                "<run1>Here is the 9th message with RunStyle2.</run1>",
                                "Here is the 10th message with ParagraphStyle2.",
                                "<run1>Here is the 11th message with ParagraphStyle2 and RunStyle2.</run1>",
                                "<run1>Here is the 12th message with RunStyleB.</run1>",
                                "Here is the 13th message with ParagraphStyleB.",
                                "<run1>Here is the 14th message with RunStyleC.</run1>",
                                "Here is the 15th message with ParagraphStyleC.",
                                "<run1>Here is the 16th message with ParagraphStyleC and RunStyleC.</run1>",
                                "<run1>Here is the 17th message with direct false vanish prop overriding vanish prop in Haydn rStyle.</run1>",
                                "<run1>Here is the 18th message with direct false vanish prop overriding vanish prop in FranzJosef pStyle.</run1>",
                                "<run1>Here is the 19th message with direct false vanish prop overriding vanish prop in Haydn rStyle in pPr.</run1>",
                                "Here is the 20th message with Haydn rStyle in pPr.",
                        },
                        false,
                        new String[] {
                                "RunStyle1",
                        },
                },
                { //3
                        new String[] {
                                "Here is the [visible] <run1/>message [visible] <run2/>of Jeremiah [visible].",
                                "Here is the 5th message with direct vanish prop in pPr.",
                                "<run1>Here is the 6th message with direct false vanish prop overriding vanish prop in pPr.</run1>",
                                "<run1>Here is the 7th message with RunStyle1.</run1>",
                                "<run1>Here is the 9th message with RunStyle2.</run1>",
                                "Here is the 10th message with ParagraphStyle2.",
                                "<run1>Here is the 11th message with ParagraphStyle2 and RunStyle2.</run1>",
                                "<run1>Here is the 12th message with RunStyleB.</run1>",
                                "Here is the 13th message with ParagraphStyleB.",
                                "<run1>Here is the 14th message with RunStyleC.</run1>",
                                "Here is the 15th message with ParagraphStyleC.",
                                "<run1>Here is the 16th message with ParagraphStyleC and RunStyleC.</run1>",
                                "<run1>Here is the 17th message with direct false vanish prop overriding vanish prop in Haydn rStyle.</run1>",
                                "<run1>Here is the 18th message with direct false vanish prop overriding vanish prop in FranzJosef pStyle.</run1>",
                                "<run1>Here is the 19th message with direct false vanish prop overriding vanish prop in Haydn rStyle in pPr.</run1>",
                                "Here is the 20th message with Haydn rStyle in pPr.",
                        },
                        false,
                        new String[] {
                                "ParagraphStyle1",
                        },
                },
                { //4
                        new String[] {
                                "Here is the [visible] <run1/>message [visible] <run2/>of Jeremiah [visible].",
                                "Here is the 5th message with direct vanish prop in pPr.",
                                "<run1>Here is the 6th message with direct false vanish prop overriding vanish prop in pPr.</run1>",
                                "<run1>Here is the 7th message with RunStyle1.</run1>",
                                "Here is the 8th message with ParagraphStyle1.",
                                "<run1>Here is the 9th message with RunStyle2.</run1>",
                                "Here is the 10th message with ParagraphStyle2.",
                                "<run1>Here is the 11th message with ParagraphStyle2 and RunStyle2.</run1>",
                                "<run1>Here is the 14th message with RunStyleC.</run1>",
                                "Here is the 15th message with ParagraphStyleC.",
                                "<run1>Here is the 16th message with ParagraphStyleC and RunStyleC.</run1>",
                                "<run1>Here is the 17th message with direct false vanish prop overriding vanish prop in Haydn rStyle.</run1>",
                                "<run1>Here is the 18th message with direct false vanish prop overriding vanish prop in FranzJosef pStyle.</run1>",
                                "<run1>Here is the 19th message with direct false vanish prop overriding vanish prop in Haydn rStyle in pPr.</run1>",
                                "Here is the 20th message with Haydn rStyle in pPr.",
                        },
                        false,
                        new String[] {
                                "RunStyleB",
                                "ParagraphStyleB",
                        },
                },
                { //5
                        new String[] {
                                "Here is the [visible] <run1/>message [visible] <run2/>of Jeremiah [visible].",
                                "Here is the 5th message with direct vanish prop in pPr.",
                                "<run1>Here is the 6th message with direct false vanish prop overriding vanish prop in pPr.</run1>",
                                "<run1>Here is the 7th message with RunStyle1.</run1>",
                                "Here is the 8th message with ParagraphStyle1.",
                                "<run1>Here is the 9th message with RunStyle2.</run1>",
                                "Here is the 10th message with ParagraphStyle2.",
                                "<run1>Here is the 11th message with ParagraphStyle2 and RunStyle2.</run1>",
                                "<run1>Here is the 12th message with RunStyleB.</run1>",
                                "Here is the 13th message with ParagraphStyleB.",
                                "<run1>Here is the 14th message with RunStyleC.</run1>",
                                "Here is the 15th message with ParagraphStyleC.",
                                "<run1>Here is the 16th message with ParagraphStyleC and RunStyleC.</run1>",
                                "<run1>Here is the 17th message with direct false vanish prop overriding vanish prop in Haydn rStyle.</run1>",
                                "<run1>Here is the 18th message with direct false vanish prop overriding vanish prop in FranzJosef pStyle.</run1>",
                                "<run1>Here is the 19th message with direct false vanish prop overriding vanish prop in Haydn rStyle in pPr.</run1>",
                                "Here is the 20th message with Haydn rStyle in pPr.",
                        },
                        false,
                        new String[] {
                                "Normal",
                        },
                },
                { //6
                        new String[] {
                                "Here is the [visible] <run1/>message [visible] <run2/>of Jeremiah [visible].",
                                "Here is the 5th message with direct vanish prop in pPr.",
                                "<run1>Here is the 6th message with direct false vanish prop overriding vanish prop in pPr.</run1>",
                                "Here is the 8th message with ParagraphStyle1.",
                                "Here is the 13th message with ParagraphStyleB.",
                                "<run1>Here is the 14th message with RunStyleC.</run1>",
                                "<run1>Here is the 17th message with direct false vanish prop overriding vanish prop in Haydn rStyle.</run1>",
                                "<run1>Here is the 18th message with direct false vanish prop overriding vanish prop in FranzJosef pStyle.</run1>",
                                "<run1>Here is the 19th message with direct false vanish prop overriding vanish prop in Haydn rStyle in pPr.</run1>",
                                "Here is the 20th message with Haydn rStyle in pPr.",
                        },
                        false,
                        new String[] {
                                "RunStyle1",
                                "RunStyle2",
                                "RunStyleB",
                                "ParagraphStyle2",
                                "ParagraphStyleA",
                                "ParagraphStyleC",
                        },
                },
        };
    }

    @Test
    @UseDataProvider("testHiddenTextExtractionProvider")
    public void testHiddenTextExtraction(String[] expectedTexts, boolean translateWordHiddenParameter, String[] excludedStyles) throws Exception {
        ConditionalParameters params = new ConditionalParametersBuilder()
                .translateDocProperties(false)
                .translateWordHidden(translateWordHiddenParameter)
                .build();
        params.tsExcludeWordStyles.addAll(Collections.newHashSet(excludedStyles));
        ArrayList<Event> textUnitEvents = getTextUnitEventsFromFile("/HiddenExcluded.docx", params);
        assertEquals(expectedTexts.length, textUnitEvents.size());
        for (int i = 0; i < textUnitEvents.size(); i++) {
            Assert.assertThat(getStringFromTextUnitEvent(textUnitEvents.get(i)), equalTo(expectedTexts[i]));
        }
    }

    private String getStringFromTextUnitEvent(Event textUnitEvent) {
        return textUnitEvent.getTextUnit().getSource().toString();
    }

    @Test(expected = OkapiEncryptedDataException.class)
    public void testOkapiEncryptedDataException() throws Exception {
        getTextUnitEventsFromFile("/encrypted/encrypted.docx", new ConditionalParameters());
    }

    private ArrayList<Event> getTextUnitEventsFromFile(String path, ConditionalParameters params) throws Exception{
        OpenXMLFilter filter = new OpenXMLFilter();
        filter.setParameters(params);
        URL url = root.in(path).asUrl();
        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        return FilterTestDriver.getTextUnitEvents(filter, doc);
    }

    private void checkTu(ArrayList<Event> events, int i, String gold) {
        ITextUnit tu = FilterTestDriver.getTextUnit(events, i);
        assertNotNull(tu);
        assertEquals(gold, tu.getSource().toString());
    }

    private ArrayList<Event> getEvents(OpenXMLFilter filter, RawDocument doc) {
        ArrayList<Event> list = new ArrayList<Event>();
        filter.open(doc, false);
        while (filter.hasNext()) {
            Event event = filter.next();
            list.add(event);
        }
        filter.close();
        return list;
    }
}
