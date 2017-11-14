package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.LocaleId;

/**
 * Tests roundtrip of a line break replaced by a character.
 */
@RunWith(JUnit4.class)
public class OpenXMLRoundtripLineSeparatorReplacementTest extends AbstractOpenXMLRoundtripTest {

    @Test
    public void testPageBreakWithLineSeparatorOption() throws Exception {
        ConditionalParameters params = new ConditionalParameters();
        params.setAddLineSeparatorCharacter(true);
        params.setTranslateDocProperties(false);
        params.setTranslatePowerpointMasters(false);

        runOneTest("leading_line_breaks.pptx", true, false, params, "lineseparator/", LocaleId.US_ENGLISH);
        assertTrue(this.allGood);
    }
}
