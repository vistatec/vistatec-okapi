package net.sf.okapi.steps.whitespacecorrection;

import java.util.Collection;
import java.util.EnumSet;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

import static net.sf.okapi.steps.whitespacecorrection.WhitespaceCorrector.Punctuation;

@EditorFor(WhitespaceCorrectionStepParameters.class)
public class WhitespaceCorrectionStepParameters extends StringParameters implements IEditorDescriptionProvider {
    private static final String PUNCTUATION = "punctuation";

    private EnumSet<Punctuation> punctuation = EnumSet.allOf(Punctuation.class);

    @Override
    public void reset() {
        super.reset();
        punctuation = EnumSet.allOf(Punctuation.class);
    }

    public void setPunctuation(Collection<Punctuation> punctuation) {
        this.punctuation.clear();
        for (Punctuation p : punctuation) {
            this.punctuation.add(p);
        }
    }

    public EnumSet<Punctuation> getPunctuation() {
        return punctuation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Punctuation p : punctuation) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(p.toString());
        }
        buffer.setString(PUNCTUATION, sb.toString());
        return super.toString();
    }

    @Override
    public void fromString(String data) {
        super.fromString(data);
        loadPunctuation(buffer.getString(PUNCTUATION));
    }

    private void loadPunctuation(String s) {
        punctuation.clear();
        for (String ps : s.split(",")) {
            Punctuation p = Punctuation.valueOf(ps);
            if (ps != null) {
                punctuation.add(p);
            }
        }
    }

    public boolean getFullStop() {
        return punctuation.contains(Punctuation.FULL_STOP);
    }

    public void setFullStop(boolean value) {
        set(Punctuation.FULL_STOP, value);
    }

    public boolean getComma() {
        return punctuation.contains(Punctuation.COMMA);
    }

    public void setComma(boolean value) {
        set(Punctuation.COMMA, value);
    }

    public boolean getExclamationPoint() {
        return punctuation.contains(Punctuation.EXCLAMATION_MARK);
    }

    public void setExclamationPoint(boolean value) {
        set(Punctuation.EXCLAMATION_MARK, value);
    }

    public boolean getQuestionMark() {
        return punctuation.contains(Punctuation.QUESTION_MARK);
    }

    public void setQuestionMark(boolean value) {
        set(Punctuation.QUESTION_MARK, value);
    }

    private void set(Punctuation p, boolean value) {
        if (value) {
            punctuation.add(p);
        }
        else {
            punctuation.remove(p);
        }
    }

    @Override
    public ParametersDescription getParametersDescription () {
        ParametersDescription desc = new ParametersDescription(this);
        desc.add("fullStop", "Full Stop", null);
        desc.add("comma", "Comma", null);
        desc.add("exclamationPoint", "Exclamation Point", null);
        desc.add("questionMark", "Question Mark", null);
        return desc;
    }

    @Override
    public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
        EditorDescription desc = new EditorDescription("Correct whitespace following", true, false);
        desc.addCheckboxPart(paramDesc.get("fullStop"));
        desc.addCheckboxPart(paramDesc.get("comma"));
        desc.addCheckboxPart(paramDesc.get("exclamationPoint"));
        desc.addCheckboxPart(paramDesc.get("questionMark"));
        return desc;
    }
}
