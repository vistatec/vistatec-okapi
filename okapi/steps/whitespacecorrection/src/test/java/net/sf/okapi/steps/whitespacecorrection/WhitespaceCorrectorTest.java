package net.sf.okapi.steps.whitespacecorrection;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnit;
import static net.sf.okapi.steps.whitespacecorrection.WhitespaceCorrector.Punctuation;

@RunWith(JUnit4.class)
public class WhitespaceCorrectorTest {

    @Test
    public void testSpaceDelimitedLanguage() {
        assertTrue(WhitespaceCorrector.isSpaceDelimitedLanguage(LocaleId.ENGLISH));
        assertTrue(WhitespaceCorrector.isSpaceDelimitedLanguage(LocaleId.FRENCH));
        assertFalse(WhitespaceCorrector.isSpaceDelimitedLanguage(LocaleId.JAPANESE));
        assertFalse(WhitespaceCorrector.isSpaceDelimitedLanguage(LocaleId.CHINA_CHINESE));
    }

    @Test
    public void testEnglishToJapanese() {
        WhitespaceCorrector corrector = new WhitespaceCorrector(LocaleId.ENGLISH, LocaleId.JAPANESE,
                                                Collections.singleton(Punctuation.FULL_STOP));

        ITextUnit tu = makeTuTarget(makeTu("Sentence 1.  ", "Sentence 2. ", "Sentence 3.  Sentence 4."),
                        LocaleId.JAPANESE, new PunctuationConversionStep.En2JaTranslator());
        TextContainer tgtTc = tu.getTarget(LocaleId.JAPANESE);
        List<TextPart> parts = tgtTc.getParts();
        assertEquals("Sentence 1。  ", parts.get(0).toString());
        assertEquals("Sentence 2。 ", parts.get(1).toString());
        assertEquals("Sentence 3。  Sentence 4。", parts.get(2).toString());

        tu = corrector.correctWhitespace(tu);
        assertEquals("Sentence 1。Sentence 2。Sentence 3。Sentence 4。", tu.getTarget(LocaleId.JAPANESE).toString());
    }

    @Test
    public void testJapaneseToEnglish() {
        WhitespaceCorrector corrector = new WhitespaceCorrector(LocaleId.JAPANESE, LocaleId.ENGLISH,
                                                Collections.singleton(Punctuation.FULL_STOP));

        ITextUnit tu = makeTuTarget(makeTu("Sentence 1。", "Sentence 2\uFF0E", "Sentence 3。Sentence 4。"),
                        LocaleId.ENGLISH, new PunctuationConversionStep.Ja2EnTranslator());
        TextContainer tgtTc = tu.getTarget(LocaleId.ENGLISH);
        List<TextPart> parts = tgtTc.getParts();
        assertEquals("Sentence 1.", parts.get(0).toString());
        assertEquals("Sentence 2.", parts.get(1).toString());
        assertEquals("Sentence 3.Sentence 4.", parts.get(2).toString());

        tu = corrector.correctWhitespace(tu);
        assertEquals("Sentence 1. Sentence 2. Sentence 3.Sentence 4. ", tu.getTarget(LocaleId.ENGLISH).toString());
    }

    @Test
    public void testAdditionalPunctuationToJapanese() {
        WhitespaceCorrector corrector = new WhitespaceCorrector(LocaleId.ENGLISH, LocaleId.JAPANESE,
                            new HashSet<>(Arrays.asList(Punctuation.values())));

        ITextUnit tu = makeTuTarget(makeTu("Sentence 1?  ", "Sentence 2!\n", "Sentence 3,\t"),
                        LocaleId.JAPANESE, new PunctuationConversionStep.En2JaTranslator());
        tu = corrector.correctWhitespace(tu);
        assertEquals("Sentence 1？Sentence 2！Sentence 3、", tu.getTarget(LocaleId.JAPANESE).toString());
    }

    @Test
    public void testAdditionalPunctuationToEnglish() {
        WhitespaceCorrector corrector = new WhitespaceCorrector(LocaleId.JAPANESE, LocaleId.ENGLISH,
                            new HashSet<>(Arrays.asList(Punctuation.values())));

        ITextUnit tu = makeTuTarget(makeTu("Sentence 1？", "Sentence 2！", "Sentence 3、", "Sentence 4\uFF0C"),
                        LocaleId.ENGLISH, new PunctuationConversionStep.Ja2EnTranslator());
        tu = corrector.correctWhitespace(tu);
        assertEquals("Sentence 1? Sentence 2! Sentence 3, Sentence 4, ", tu.getTarget(LocaleId.ENGLISH).toString());
    }

    @Test
    public void doNothingEnglishToFrench() {
        // Even though the translation mysteriously converts the punctuation, the target is still
        // a space-delimited language, so no correction should be done
        WhitespaceCorrector corrector = new WhitespaceCorrector(LocaleId.ENGLISH, LocaleId.FRENCH,
                new HashSet<>(Arrays.asList(Punctuation.values())));

        ITextUnit tu = makeTuTarget(makeTu("Sentence 1?  ", "Sentence 2! ", "Sentence 3, "),
                    LocaleId.FRENCH, new PunctuationConversionStep.En2JaTranslator());
        tu = corrector.correctWhitespace(tu);
        assertEquals("Sentence 1？  Sentence 2！ Sentence 3、 ", tu.getTarget(LocaleId.FRENCH).toString());
    }

    private TextUnit makeTu(String...parts) {
        TextUnit tu = new TextUnit("id1");
        for (String s : parts) {
            tu.getSource().append(new TextPart(s));
        }
        return tu;
    }

    private TextUnit makeTuTarget(TextUnit tu, LocaleId tgtLocale, PunctuationConversionStep.PunctuationTranslator trans) {
        tu.createTarget(tgtLocale, true, 0);
        for (TextPart tp : tu.getSource().getParts()) {
            String converted = trans.translate(tp.getContent().getCodedText());
            tu.getTarget(tgtLocale).append(new TextPart(converted));
        }
        return tu;
    }
}
