package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import net.sf.okapi.common.LocaleId;

/**
 * Tests the combination of enabled {@link ConditionalParameters#ADDLINESEPARATORASCHARACTER} and
 * excluded styles from translation.
 */
@RunWith(Parameterized.class)
public class OpenXmlRoundtripSoftLineBreaksDoNotTranslateTest extends AbstractOpenXMLRoundtripTest {

    private String filename;

    public OpenXmlRoundtripSoftLineBreaksDoNotTranslateTest(String filename) {
        this.filename = filename;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "OpenXmlRoundtripSoftLineBreaksDoNotTranslateTestParagraphStyle.docx" },
                { "OpenXmlRoundtripSoftLineBreaksDoNotTranslateTestCharacterStyle.docx" }
        });
    }

    @Test
    public void test() throws Exception {
        ConditionalParameters params = new ConditionalParameters();
        params.setAddLineSeparatorCharacter(true);
        params.tsExcludeWordStyles.add("tw4winExternal");

        this.allGood = true;
        runOneTest(filename, true, false, params, "softLineBreakDoNotTranslate/", LocaleId.US_ENGLISH);
        assertTrue(this.allGood);
    }
}
