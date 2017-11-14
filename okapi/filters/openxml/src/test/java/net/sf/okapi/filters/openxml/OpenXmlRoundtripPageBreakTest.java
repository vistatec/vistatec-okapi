package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.LocaleId;

/**
 * Tests roundtrip of a page break in a word document.
 */
@RunWith(JUnit4.class)
public class OpenXmlRoundtripPageBreakTest extends AbstractOpenXMLRoundtripTest {

    @Test
    public void testPageBreakWithLineSeparatorOption() throws Exception {
        ConditionalParameters params = new ConditionalParameters();
        params.setAddTabAsCharacter(true);
        params.setAddLineSeparatorCharacter(true);

        runOneTest("PageBreak.docx", true, false, params, "pageBreak/", LocaleId.US_ENGLISH);
        assertTrue(this.allGood);
    }

    @Test
    public void testPageBreakWithoutLineSeparatorOption() throws Exception {
        ConditionalParameters params = new ConditionalParameters();
        params.setAddTabAsCharacter(true);
        params.setAddLineSeparatorCharacter(false);

        runOneTest("PageBreak.docx", true, false, params, "pageBreak/", LocaleId.US_ENGLISH);
        assertTrue(this.allGood);
    }

}