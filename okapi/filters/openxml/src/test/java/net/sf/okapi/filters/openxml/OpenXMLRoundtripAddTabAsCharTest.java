package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class OpenXMLRoundtripAddTabAsCharTest extends AbstractOpenXMLRoundtripTest {

    private String filename;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"Document-with-tabs.docx"},
                // <w:r><w:t>Before</w:t></w:r><w:r><w:tab/><w:t>after.</w:t></w:r>
                {"Document-with-tabs-2.docx"},
                // runs with tab should not be combined
                {"Document-with-tabs-3.docx"},
                // runs with tab should not be combined
                {"Document-with-tabs-4.docx"},
                {"Document-with-tabs-5.docx"},
                // <w:tabs><w:tab w:val="left" w:pos="6999"/></w:tabs>
                {"Document-with-custom-tabs.docx"},
                {"Document-with-tabs-at-EOL.docx"},
                {"Document-with-tab-09.docx"},
                {"tabstyles.docx"},
                {"Document-with-formula-and-tabs.docx"},
                {"N_001_Auswertung_Part3.docx"},
                {"N_001_Auswertung_Part4.docx"},
                {"N_001_Auswertung_Part2.docx"},
                {"TabAtEnd.docx"},
                {"TabAtEndAfterNewRun.docx"},
                //
                //                // PPTX
                {"Document-with-tabs.pptx"}
        });
    }

    public OpenXMLRoundtripAddTabAsCharTest(String filename) {
        this.filename = filename;
    }

    @Test
    public void test() throws Exception {
        ConditionalParameters params = new ConditionalParameters();
        params.setAddTabAsCharacter(true);

        this.allGood = true;
        runOneTest(filename, true, false, params, "tabsaschar/");
        assertTrue(this.allGood);
    }
}