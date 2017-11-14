package net.sf.okapi.steps.whitespacecorrection;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.EnumSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.steps.whitespacecorrection.WhitespaceCorrector.Punctuation;

@RunWith(JUnit4.class)
public class WhitespaceCorrectionStepParametersTest {
    private static final String ALL_PUNCTUATION = "#v1\npunctuation=FULL_STOP,COMMA,EXCLAMATION_MARK,QUESTION_MARK";

    @Test
    public void testSave() {
        WhitespaceCorrectionStepParameters params = new WhitespaceCorrectionStepParameters();
        params.setPunctuation(Arrays.asList(Punctuation.values()));
        assertEquals(ALL_PUNCTUATION, params.toString());
    }

    @Test
    public void testLoad() {
        WhitespaceCorrectionStepParameters params = new WhitespaceCorrectionStepParameters();
        params.fromString(ALL_PUNCTUATION);
        EnumSet<Punctuation> punctuation = EnumSet.allOf(Punctuation.class);
        assertEquals(punctuation, params.getPunctuation());
    }
}
