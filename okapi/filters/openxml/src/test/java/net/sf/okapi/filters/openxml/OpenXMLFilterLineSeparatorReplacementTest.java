package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Before;
import org.junit.Test;

/**
 * @author ccudennec
 * @since 23.05.2017
 */
public class OpenXMLFilterLineSeparatorReplacementTest {

    private OpenXMLFilter openXMLFilter = new OpenXMLFilter();

    @Before
    public void before() throws Exception {
        openXMLFilter.getParameters().setTranslateDocProperties(false);
        openXMLFilter.getParameters().setTranslatePowerpointMasters(false);
        openXMLFilter.getParameters().setAddLineSeparatorCharacter(true);
        openXMLFilter.getParameters().setLineSeparatorReplacement('\u2028');
    }

    @Test
    public void testSimple() throws Exception {
        try (InputStream inputStream = getClass()
                .getResourceAsStream("/Document-with-soft-linebreaks.docx")) {
            openXMLFilter.open(inputStream);
            boolean textUnitFound = false;
            while (openXMLFilter.hasNext()) {
                Event event = openXMLFilter.next();
                if (event.isTextUnit()) {
                    textUnitFound = true;
                    ITextUnit textUnit = event.getTextUnit();
                    assertEquals("First line\u2028second line.",
                            textUnit.getSource().getCodedText());
                    break;
                }
            }

            assertTrue(textUnitFound);
        }
        finally {
            openXMLFilter.close();
        }
    }

    @Test
    public void testSimple2() throws Exception {
        try (InputStream inputStream = getClass()
                .getResourceAsStream("/leading_line_breaks.pptx")) {
            RawDocument rd = new RawDocument(inputStream, "UTF-8", LocaleId.ENGLISH);
            List<ITextUnit> tus =
                    FilterTestDriver.filterTextUnits(FilterTestDriver.getEvents(openXMLFilter, rd, null));
            assertEquals(1, tus.size());
            assertEquals("<run1>Leading line breaks</run1>", tus.get(0).getSource().toString());
        }
        finally {
            openXMLFilter.close();
        }
    }
}
