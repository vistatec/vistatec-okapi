package net.sf.okapi.steps.whitespacecorrection;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.steps.whitespacecorrection.WhitespaceCorrector.Punctuation;

public class PunctuationConversionStep extends BasePipelineStep {
    private PunctuationTranslator converter;

    public PunctuationConversionStep(PunctuationTranslator converter) {
        this.converter = converter;
    }

    @Override
    protected Event handleTextUnit(Event event) {
        ITextUnit tu = event.getTextUnit();
        TextContainer target = tu.getTarget(targetLocale);
        for (TextPart tp : target.getParts()) {
            TextFragment tf = tp.getContent();
            tf.setCodedText(converter.translate(tf.getCodedText()));
        }
        return event;
    }

    protected LocaleId targetLocale;

    @StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
    public void setTargetLocale(LocaleId targetLocale) {
        this.targetLocale = targetLocale;
    }
    

    interface PunctuationTranslator {
        String translate(String s);
    }

    static class Ja2EnTranslator implements PunctuationTranslator {
        public String translate(String s) {
            for (Punctuation p : Punctuation.values()) {
                for (char form : p.getWhitespaceNonAcceptingForm()) {
                    s = s.replace(form, p.getWhitespaceAcceptingForm());
                }
            }
            return s;
        }
    }

    static class En2JaTranslator implements PunctuationTranslator {
        public String translate(String s) {
            for (Punctuation p : Punctuation.values()) {
                s = s.replace(p.getWhitespaceAcceptingForm(), p.getWhitespaceNonAcceptingForm()[0]);
            }
            return s;
        }
    }

    @Override
    public String getName() {
        return "Convert Punctuation";
    }

    @Override
    public String getDescription() {
        return "";
    }
}
