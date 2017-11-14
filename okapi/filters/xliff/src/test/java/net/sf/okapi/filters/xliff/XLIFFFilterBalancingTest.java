package net.sf.okapi.filters.xliff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextPart;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jpmaas
 * @since 11.05.2017
 */
public class XLIFFFilterBalancingTest {

    private XLIFFFilter filter;

    private LocaleId locEN = LocaleId.fromString("en");

    private LocaleId locFR = LocaleId.fromString("fr");

    @Before
    public void setUp() throws Exception {
        filter = new XLIFFFilter();
    }

    @Test
    public void testValidBalancingWithCTypesAfterJoinAll() throws Exception {
        String given = IOUtils
                .toString(getClass().getResourceAsStream("/Balancing/WithCTypes.xlf"), "UTF-8");

        ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(given), 1);
        assertNotNull(tu);

        // get target and joinAll
        TextContainer target = tu.getTarget(LocaleId.ITALIAN);
        target.joinAll();

        List<TextPart> actualParts = target.getParts();
        assertEquals(1, actualParts.size());
        assertEquals(2, actualParts.get(0).text.getCodes().size());

        // check balancing
        Code actualCodeOpening = actualParts.get(0).text.getCode(0);
        assertEquals(TagType.OPENING, actualCodeOpening.getTagType());
        assertEquals(1, actualCodeOpening.getId());

        Code actualCodeClosing = actualParts.get(0).text.getCode(1);
        assertEquals(TagType.CLOSING, actualCodeClosing.getTagType());
        assertEquals(1, actualCodeOpening.getId());
    }

    @Test
    public void testValidBalancingOverMultipleSegmentsAfterJoinAll() throws Exception {
        String given = IOUtils
                .toString(getClass().getResourceAsStream("/Balancing/MultipleSegments.xlf"), "UTF-8");

        ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(given), 1);
        assertNotNull(tu);

        // get target and joinAll
        TextContainer target = tu.getTarget(LocaleId.ITALIAN);
        target.joinAll();

        List<TextPart> actualParts = target.getParts();
        assertEquals(1, actualParts.size());
        assertEquals(2, actualParts.get(0).text.getCodes().size());

        // check balancing
        Code actualCodeOpening = actualParts.get(0).text.getCode(0);
        assertEquals(TagType.OPENING, actualCodeOpening.getTagType());
        assertEquals(1, actualCodeOpening.getId());

        Code actualCodeClosing = actualParts.get(0).text.getCode(1);
        assertEquals(TagType.CLOSING, actualCodeClosing.getTagType());
        assertEquals(1, actualCodeClosing.getId());
    }

    @Test
    public void testValidBalancingWithNestedGTagsAfterJoinAll() throws Exception {
        String given = IOUtils
                .toString(getClass().getResourceAsStream("/Balancing/2LevelGTags.xlf"), "UTF-8");

        ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(given), 1);
        assertNotNull(tu);

        // get target and joinAll
        TextContainer target = tu.getTarget(LocaleId.ITALIAN);
        target.joinAll();

        List<TextPart> actualParts = target.getParts();
        assertEquals(actualParts.size(), 1);
        assertEquals(4, actualParts.get(0).text.getCodes().size());

        // check balancing
        Code actualCodeOpening1 = actualParts.get(0).text.getCode(0);
        assertEquals(TagType.OPENING, actualCodeOpening1.getTagType());
        assertEquals(1, actualCodeOpening1.getId());

        Code actualCodeOpening2 = actualParts.get(0).text.getCode(1);
        assertEquals(TagType.OPENING, actualCodeOpening2.getTagType());
        assertEquals(2, actualCodeOpening2.getId());

        Code actualCodeClosing1 = actualParts.get(0).text.getCode(2);
        assertEquals(TagType.CLOSING, actualCodeClosing1.getTagType());
        assertEquals(2, actualCodeClosing1.getId());

        Code actualCodeClosing2 = actualParts.get(0).text.getCode(3);
        assertEquals(TagType.CLOSING, actualCodeClosing2.getTagType());
        assertEquals(1, actualCodeClosing2.getId());
    }

    @Test
    public void testValidBalancingBetweenSegmentsAfterJoinAll() throws Exception {
        String given = IOUtils
                .toString(getClass().getResourceAsStream("/Balancing/BetweenSegments.xlf"),
                        "UTF-8");

        ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(given), 1);
        assertNotNull(tu);

        // get target and joinAll
        TextContainer target = tu.getTarget(LocaleId.ITALIAN);
        target.joinAll();

        List<TextPart> actualParts = target.getParts();
        assertEquals(actualParts.size(), 1);
        assertEquals(4, actualParts.get(0).text.getCodes().size());

        // check balancing
        Code actualCodeOpening1 = actualParts.get(0).text.getCode(0);
        assertEquals(TagType.OPENING, actualCodeOpening1.getTagType());
        assertEquals(1, actualCodeOpening1.getId());

        Code actualCodeClosing1 = actualParts.get(0).text.getCode(1);
        assertEquals(TagType.CLOSING, actualCodeClosing1.getTagType());
        assertEquals(1, actualCodeClosing1.getId());

        Code actualCodeOpening2 = actualParts.get(0).text.getCode(2);
        assertEquals(TagType.OPENING, actualCodeOpening2.getTagType());
        assertEquals(2, actualCodeOpening2.getId());

        Code actualCodeClosing2 = actualParts.get(0).text.getCode(3);
        assertEquals(TagType.CLOSING, actualCodeClosing2.getTagType());
        assertEquals(2, actualCodeClosing2.getId());
    }

    @Test
    public void testValidBalancingWithBxAndGTagsAfterJoinAll() throws Exception {
        String given = IOUtils
                .toString(getClass().getResourceAsStream("/Balancing/DifferentTags.xlf"), "UTF-8");

        ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(given), 1);
        assertNotNull(tu);

        // get target and joinAll
        TextContainer target = tu.getTarget(LocaleId.ITALIAN);
        target.joinAll();

        List<TextPart> actualParts = target.getParts();
        assertEquals(actualParts.size(), 1);
        assertEquals(6, actualParts.get(0).text.getCodes().size());

        // check balancing
        Code actualCodeOpening1 = actualParts.get(0).text.getCode(0);
        assertEquals(TagType.OPENING, actualCodeOpening1.getTagType());
        assertEquals(1, actualCodeOpening1.getId());

        Code actualCodeOpening2 = actualParts.get(0).text.getCode(2);
        assertEquals(TagType.OPENING, actualCodeOpening2.getTagType());
        assertEquals(2, actualCodeOpening2.getId());

        Code actualCodeClosing1 = actualParts.get(0).text.getCode(3);
        assertEquals(TagType.CLOSING, actualCodeClosing1.getTagType());
        assertEquals(2, actualCodeClosing1.getId());

        Code actualCodeClosing2 = actualParts.get(0).text.getCode(5);
        assertEquals(TagType.CLOSING, actualCodeClosing2.getTagType());
        assertEquals(1, actualCodeClosing2.getId());
    }

    @Test
    public void testValidBalancingWithNestedGTagsOnThreeLevelsAfterJoinAll() throws Exception {
        String given = IOUtils
                .toString(getClass().getResourceAsStream("/Balancing/3LevelGTags.xlf"), "UTF-8");

        ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(given), 1);
        assertNotNull(tu);

        // get target and joinAll
        TextContainer target = tu.getTarget(LocaleId.ITALIAN);
        target.joinAll();

        List<TextPart> actualParts = target.getParts();
        assertEquals(actualParts.size(), 1);
        assertEquals(6, actualParts.get(0).text.getCodes().size());

        // check balancing
        Code actualCodeOpening1 = actualParts.get(0).text.getCode(0);
        assertEquals(TagType.OPENING, actualCodeOpening1.getTagType());
        assertEquals(1, actualCodeOpening1.getId());

        Code actualCodeOpening2 = actualParts.get(0).text.getCode(1);
        assertEquals(TagType.OPENING, actualCodeOpening2.getTagType());
        assertEquals(2, actualCodeOpening2.getId());

        Code actualCodeOpening3 = actualParts.get(0).text.getCode(2);
        assertEquals(TagType.OPENING, actualCodeOpening3.getTagType());
        assertEquals(3, actualCodeOpening3.getId());

        Code actualCodeClosing1 = actualParts.get(0).text.getCode(3);
        assertEquals(TagType.CLOSING, actualCodeClosing1.getTagType());
        assertEquals(3, actualCodeClosing1.getId());

        Code actualCodeClosing2 = actualParts.get(0).text.getCode(4);
        assertEquals(TagType.CLOSING, actualCodeClosing2.getTagType());
        assertEquals(2, actualCodeClosing2.getId());

        Code actualCodeClosing3 = actualParts.get(0).text.getCode(5);
        assertEquals(TagType.CLOSING, actualCodeClosing3.getTagType());
        assertEquals(1, actualCodeClosing3.getId());
    }
    @Test
    public void testValidBalancingWithNestedGTagsOnThreeLevelsAfterJoinAllWithNamespaces() throws Exception {
        String given = IOUtils
                .toString(getClass().getResourceAsStream("/Balancing/3LevelGTagsWithNamespaces.xlf"), "UTF-8");

        ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(given), 1);
        assertNotNull(tu);

        // get target and joinAll
        TextContainer target = tu.getTarget(LocaleId.ITALIAN);
        target.joinAll();

        List<TextPart> actualParts = target.getParts();
        assertEquals(actualParts.size(), 1);
        assertEquals(6, actualParts.get(0).text.getCodes().size());

        // check balancing
        Code actualCodeOpening1 = actualParts.get(0).text.getCode(0);
        assertEquals(TagType.OPENING, actualCodeOpening1.getTagType());
        assertEquals(1, actualCodeOpening1.getId());

        Code actualCodeOpening2 = actualParts.get(0).text.getCode(1);
        assertEquals(TagType.OPENING, actualCodeOpening2.getTagType());
        assertEquals(2, actualCodeOpening2.getId());

        Code actualCodeOpening3 = actualParts.get(0).text.getCode(2);
        assertEquals(TagType.OPENING, actualCodeOpening3.getTagType());
        assertEquals(3, actualCodeOpening3.getId());

        Code actualCodeClosing1 = actualParts.get(0).text.getCode(3);
        assertEquals(TagType.CLOSING, actualCodeClosing1.getTagType());
        assertEquals(3, actualCodeClosing1.getId());

        Code actualCodeClosing2 = actualParts.get(0).text.getCode(4);
        assertEquals(TagType.CLOSING, actualCodeClosing2.getTagType());
        assertEquals(2, actualCodeClosing2.getId());

        Code actualCodeClosing3 = actualParts.get(0).text.getCode(5);
        assertEquals(TagType.CLOSING, actualCodeClosing3.getTagType());
        assertEquals(1, actualCodeClosing3.getId());
    }

    @Test
    public void testDifferentCTypes() throws Exception {
        String given = IOUtils
                .toString(getClass().getResourceAsStream("/Balancing/DifferentCTypes.xlf"), "UTF-8");

        ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(given), 1);
        assertNotNull(tu);

        // get target and joinAll
        TextContainer target = tu.getTarget(LocaleId.ENGLISH);
        target.joinAll();

        List<TextPart> actualParts = target.getParts();
        assertEquals(1, actualParts.size());
        assertEquals(4, actualParts.get(0).text.getCodes().size());

        // check balancing
        Code actualCodeOpening1 = actualParts.get(0).text.getCode(0);
        assertEquals(TagType.OPENING, actualCodeOpening1.getTagType());
        assertEquals(1, actualCodeOpening1.getId());

        Code actualCodeOpening2 = actualParts.get(0).text.getCode(1);
        assertEquals(TagType.OPENING, actualCodeOpening2.getTagType());
        assertEquals(2, actualCodeOpening2.getId());

        Code actualCodeClosing1 = actualParts.get(0).text.getCode(2);
        assertEquals(TagType.CLOSING, actualCodeClosing1.getTagType());
        assertEquals(2, actualCodeClosing1.getId());

        Code actualCodeClosing2 = actualParts.get(0).text.getCode(3);
        assertEquals(TagType.CLOSING, actualCodeClosing2.getTagType());
        assertEquals(1, actualCodeClosing2.getId());
    }

    @Test
    public void testDifferentCTypesWithBreakingMrk() throws Exception {
        String given = IOUtils
                .toString(getClass().getResourceAsStream("/Balancing/DifferentCTypesWithBreakingMrk.xlf"), "UTF-8");

        ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(given), 1);
        assertNotNull(tu);

        // get target and joinAll
        TextContainer target = tu.getTarget(LocaleId.ENGLISH);
        target.joinAll();

        List<TextPart> actualParts = target.getParts();
        assertEquals(1, actualParts.size());
        assertEquals(4, actualParts.get(0).text.getCodes().size());

        // check balancing
        Code actualCodeOpening1 = actualParts.get(0).text.getCode(0);
        assertEquals(TagType.OPENING, actualCodeOpening1.getTagType());
        assertEquals(1, actualCodeOpening1.getId());

        Code actualCodeOpening2 = actualParts.get(0).text.getCode(1);
        assertEquals(TagType.OPENING, actualCodeOpening2.getTagType());
        assertEquals(2, actualCodeOpening2.getId());

        Code actualCodeClosing1 = actualParts.get(0).text.getCode(2);
        assertEquals(TagType.CLOSING, actualCodeClosing1.getTagType());
        assertEquals(2, actualCodeClosing1.getId());

        Code actualCodeClosing2 = actualParts.get(0).text.getCode(3);
        assertEquals(TagType.CLOSING, actualCodeClosing2.getTagType());
        assertEquals(1, actualCodeClosing2.getId());
    }

    private ArrayList<Event> getEvents(String snippet) {
        return getEvents(snippet, filter);
    }

    private ArrayList<Event> getEvents(String snippet,
            XLIFFFilter filterToUse) {
        return FilterTestDriver.getEvents(filterToUse, snippet, locEN, locFR);
    }

}
