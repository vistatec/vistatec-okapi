package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class OpenXMLDefaultConfigRoundTripTest extends AbstractOpenXMLRoundtripTest {
    private String filename;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{ "BoldWorld.docx" },
                { "Deli.docx" },
                { "DocProperties.docx" }, { "Escapades.docx" }, { "Addcomments.docx" },
                { "styles.docx" }, { "sample.pptx" }, { "sample.xlsx" }, { "sampleMore.xlsx" },
                { "sampleMore.pptx" }, { "OpenXML_text_reference_v1_2.docx" }, { "Mauris.docx" },
                { "HiddenExcluded.docx" }, { "ExcelColors.xlsx" }, { "UTF8.docx" },
                { "commentTable.xlsx" }, { "InsertText.pptx" }, { "Endpara.pptx" },
                { "MissingPara.docx" }, { "EndGroup.docx" }, { "Practice2.docx" }, { "Hangs.docx" },
                { "TestLTinsideBoxFails.docx" }, { "watermark.docx" },
                { "StartsWithLineSeparator.docx"}, { "FontThemeOverFont.docx" }
                // tests translation of watermark
                , { "word art.docx" } // tests translation of text in word art
                , { "table of contents - automatic.docx" } // test exclusion of hyperlinks
                , { "equation.docx" }, { "docxtest.docx" } // extract from mc:Choice and mc:Fallback
                , { "docxsegtest.docx" } // extract from mc:Choice and mc:Fallback
                , { "SmartArt.pptx" } // extract from mc:Choice and mc:Fallback
                , { "neverendingloop.docx" } // issue 350 #3
                , { "DrawingML_Test.docx" } // issue
                , { "sample.docx" }, { "TextBoxes.docx" }, { "OutOfTheTextBox.docx" },
                { "apissue.docx" }, { "GraphicInTextBox.docx" } // issue 350 #1 doesn't work yet
                , { "TestDako2.docx" }, { "shape with text.docx" }
                // test translation of text in he shape
                , { "AlternateContentTest.docx" } // extract from mc:Choice and mc:Fallback
                , { "AlternateContent.docx" }, { "columns.xlsx" }, { "NoStylesXml.docx" },
                { "AltContentEscaping.docx" }, { "graphicdata.docx" }, { "br.docx" },
                { "multiple_tabs.docx" }, { "smartquotes.docx" }, { "br2.docx" },
                { "table_truncation.docx" }, { "delTextAmp.docx" }, { "hyperlink.docx" },
                { "diagrams.pptx" }, { "gettysburg_en.docx" }, { "textarea.docx" },
                { "picture.docx" }, { "spelling.docx" }, { "lang.docx" }, { "slideLayouts.pptx" },
                { "formulas.pptx" }, { "spacing.docx" }, { "vertAlign.docx" },
                { "bookmarkgoback.docx" }, { "hidden_cells.xlsx" },
                { "document-with-run-fonts-variations.docx" },
                { "document-style-definitions.docx" }, { "sharedstring_entities.xlsx" },
                { "hidden_stuff.xlsx" }, { "PageBreak.docx" }, { "TextboxNumber.docx" },
                { "Hidden_Textbox.docx" }, { "large-attribute.docx" }, { "smart_art.docx" },
                { "document-revision-information-stripping.docx" }, { "hidden_table.xlsx" },
                { "chartAmpersand.docx" }, { "issue536.docx" },
                { "missing_fillId.xlsx" },
                {"simple_chart.xlsx"}, {"chart_content.pptx"}, {"simple_chart.docx"},
                {"content_category_test.docx"}
        });
    }

    public OpenXMLDefaultConfigRoundTripTest(String filename) {
        this.filename = filename;
    }

    @Test
    public void testWitthDefaultConfig() throws Exception {
        runOneTest(filename, true, false, new ConditionalParameters());  // PigLatin
        assertTrue("Some Roundtrip files failed.", allGood);
    }

}