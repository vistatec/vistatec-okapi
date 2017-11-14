package net.sf.okapi.steps.segmentation;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author ccudennec
 * @since 25.10.2016
 */
@RunWith(JUnit4.class)
public class SegmentationStepTreatIsolatedCodesTest {

    private SegmentationStep segStep;
    private Parameters params;

    @Before
    public void startUp() throws Exception {
        segStep = new SegmentationStep();
        segStep.setSourceLocale(LocaleId.ENGLISH);
        segStep.setTargetLocales(Collections.singletonList(LocaleId.FRENCH));

        String srxFile = FileLocation.fromClass(this.getClass()).in("/Test01.srx").toString();
        params = (Parameters) segStep.getParameters();
        params.setSourceSrxPath(srxFile);
        params.setTargetSrxPath(srxFile);
    }

    @Test
    public void testTreatIsolatedCodesAsWhitespaceTrue() {
        ITextUnit tu1 = createTextUnitWithIsolatedCode();

        params.setTreatIsolatedCodesAsWhitespace(true);
        segStep.handleStartBatchItem(new Event(EventType.START_BATCH_ITEM));

        segStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu1));
        assertEquals(2, tu1.getSourceSegments().count());
    }

    @Test
    public void testTreatIsolatedCodesAsWhitespaceFalse() {
        ITextUnit tu1 = createTextUnitWithIsolatedCode();

        params.setTreatIsolatedCodesAsWhitespace(false);
        segStep.handleStartBatchItem(new Event(EventType.START_BATCH_ITEM));

        segStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu1));
        assertEquals(1, tu1.getSourceSegments().count());
    }

    private ITextUnit createTextUnitWithIsolatedCode() {
        ITextUnit tu1 = new TextUnit("tu1");
        TextContainer source = tu1.getSource();
        TextFragment tf = new TextFragment();
        tf.append("Sentence one.");
        tf.append(TextFragment.TagType.PLACEHOLDER, "break", "<br/>");
        tf.append("Sentence two.");
        source.append(tf);
        return tu1;
    }
}
