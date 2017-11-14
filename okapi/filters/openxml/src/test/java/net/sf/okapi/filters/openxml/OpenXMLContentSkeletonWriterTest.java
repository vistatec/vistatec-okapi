package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Christopher Cudennec
 * @version $Rev: 74201 $ $Date: 2015-02-11 16:32:38 +0100 (Mi, 11 Feb 2015) $
 * @since 03.06.2015
 */
@RunWith(JUnit4.class)
public class OpenXMLContentSkeletonWriterTest {

    private OpenXMLContentSkeletonWriter writer = new OpenXMLContentSkeletonWriter(
            ParseType.MSWORD);

    @Test
    public void testAddSpacePreserveToLastT() throws Exception {
        StringBuilder tmp = new StringBuilder("<w:t>text");
        writer.addSpacePreserveToLastT(tmp);
        assertEquals("<w:t xml:space=\"preserve\">text", tmp.toString());
    }

    @Test
    public void testAddSpacePreserveToLastT2() throws Exception {
        StringBuilder tmp = new StringBuilder("<w:t>");
        writer.addSpacePreserveToLastT(tmp);
        assertEquals("<w:t xml:space=\"preserve\">", tmp.toString());
    }

    @Test
    public void testAddSpacePreserveToLastTAlreadyThere() throws Exception {
        StringBuilder tmp = new StringBuilder("<w:t xml:space=\"preserve\">text");
        writer.addSpacePreserveToLastT(tmp);
        assertEquals("<w:t xml:space=\"preserve\">text", tmp.toString());
    }

    @Test
    public void testAddSpacePreserveToLastTMoreThanOneT() throws Exception {
        StringBuilder tmp = new StringBuilder("<w:t>foo</w:t><w:t>text");
        writer.addSpacePreserveToLastT(tmp);
        assertEquals("<w:t>foo</w:t><w:t xml:space=\"preserve\">text", tmp.toString());
    }

    @Test
    public void testAddSpacePreserveToLastTDoNothing() throws Exception {
        StringBuilder tmp = new StringBuilder("text");
        writer.addSpacePreserveToLastT(tmp);
        assertEquals("text", tmp.toString());
    }
}