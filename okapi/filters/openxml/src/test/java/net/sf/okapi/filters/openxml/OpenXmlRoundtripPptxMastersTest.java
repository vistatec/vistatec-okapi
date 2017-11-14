package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author ccudennec
 * @since 17.11.2016
 */
@RunWith(Parameterized.class)
public class OpenXmlRoundtripPptxMastersTest extends AbstractOpenXMLRoundtripTest {

    private String filename;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"textbox-on-master.pptx"}
        });
    }

    public OpenXmlRoundtripPptxMastersTest(String filename) {
        this.filename = filename;
    }

    @Test
    public void test() throws Exception {
        ConditionalParameters params = new ConditionalParameters();
        params.setTranslatePowerpointNotes(false);
        params.setTranslatePowerpointMasters(true);
        params.setIgnorePlaceholdersInPowerpointMasters(true);

        this.allGood = true;
        runOneTest(filename, true, false, params, "pptxmasters/");
        assertTrue(this.allGood);
    }


}
