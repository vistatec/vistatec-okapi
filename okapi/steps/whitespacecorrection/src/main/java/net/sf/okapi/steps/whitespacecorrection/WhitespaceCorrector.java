package net.sf.okapi.steps.whitespacecorrection;

import static net.sf.okapi.common.LocaleId.CHINA_CHINESE;
import static net.sf.okapi.common.LocaleId.JAPANESE;

import java.util.Iterator;
import java.util.Set;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;

public class WhitespaceCorrector {
    public enum Punctuation {
        // U+3002 IDEOGRAPHIC FULL STOP, U+FF0E FULLWIDTH FULL STOP
        FULL_STOP('.', '\u3002', '\uFF0E'),
        // U+3001 IDEOGRAPHIC COMMA, U+FF0C FULLWIDTH COMMA
        COMMA(',', '\u3001', '\uFF0C'), 
        // U+FF01 FULLWIDTH EXCLAMATION MARK
        EXCLAMATION_MARK('!', '\uff01'),
        // U+FF1F FULLWIDTH QUESTION MARK
        QUESTION_MARK ('?', '\uff1f');

        private char[] whitespaceNonAcceptingForm;
        private char whitespaceAcceptingForm;
        private Punctuation(char whitespaceAcceptingForm, char... whitespaceNonAcceptingForms) {
            this.whitespaceAcceptingForm = whitespaceAcceptingForm;
            this.whitespaceNonAcceptingForm = whitespaceNonAcceptingForms;
        }

        public char getWhitespaceAcceptingForm() {
            return whitespaceAcceptingForm;
        }

        public char[] getWhitespaceNonAcceptingForm() {
            return whitespaceNonAcceptingForm;
        }
    }
    protected static final char WHITESPACE = ' ';

    protected LocaleId sourceLocale;
    protected LocaleId targetLocale;
    protected Set<Punctuation> punctuation;

    public WhitespaceCorrector(LocaleId sourceLocale, LocaleId targetLocale, Set<Punctuation> punctuation) {
        this.sourceLocale = sourceLocale;
        this.targetLocale = targetLocale;
        this.punctuation = punctuation;
    }

    static boolean isSpaceDelimitedLanguage(LocaleId localeId) {
        return !JAPANESE.sameLanguageAs(localeId) && !CHINA_CHINESE.sameLanguageAs(localeId);
    }

    public ITextUnit correctWhitespace(ITextUnit tu) {
        if (isSpaceDelimitedLanguage(sourceLocale) && !isSpaceDelimitedLanguage(targetLocale)) {
            removeTrailingWhitespace(tu);
        }
        else if (!isSpaceDelimitedLanguage(sourceLocale) && isSpaceDelimitedLanguage(targetLocale)) {
            addTrailingWhitespace(tu);
        }
        return tu;
    }

    protected void removeTrailingWhitespace(ITextUnit textUnit) {
        TextContainer targetTextContainer = textUnit.getTarget(targetLocale);

        /**
         * If whitespace trimming was enabled during segmentation, the
         * whitespace will be trapped in non-Segment TextParts.  So
         * we need to check everything in the container, not just the
         * results of tu.getTargetSegments();
         */
        for (TextPart targetTextPart : targetTextContainer.getParts()) {
            TextFragment textFragment = findAndRemoveWhitespacesAfterPunctuation(targetTextPart.getContent());
            targetTextPart.setContent(textFragment);
        }
    }

    protected void addTrailingWhitespace(ITextUnit textUnit) {
        TextContainer sourceTextContainer = textUnit.getSource();
        TextContainer targetTextContainer = textUnit.getTarget(targetLocale);

        Iterator<TextPart> sourceTextPartsIterator = sourceTextContainer.getParts().iterator();
        Iterator<TextPart> targetTextPartsIterator = targetTextContainer.getParts().iterator();

        while (sourceTextPartsIterator.hasNext() && targetTextPartsIterator.hasNext()) {
            TextPart sourceTextPart = sourceTextPartsIterator.next();
            TextPart targetTextPart = targetTextPartsIterator.next();

            String sourceText = sourceTextPart.getContent().getText();
            if (sourceText.isEmpty() || !isNonSpaceDelimitedPunctuation(lastChar(sourceText))) {
                // the text does not end with punctuation requiring conversion
                continue;
            }

            if (isWhitespace(lastChar(targetTextPart.getContent().getText()))) {
                // the whitespace is present at the end
                continue;
            }

            targetTextPart.getContent().append(WHITESPACE);
        }
    }

    protected boolean isWhitespace(char c) {
        // Match any space (\s) plus U+00A0, non-breaking space
        return Character.isWhitespace(c) || c == '\u00A0';
    }

    private char lastChar(String s) {
        return s.charAt(s.length() - 1);
    }

    protected boolean isSpaceDelimitedPunctuation(char c) {
        for (Punctuation p : punctuation) {
            if (c == p.whitespaceAcceptingForm) {
                return true;
            }
        }
        return false;
    }

    protected boolean isNonSpaceDelimitedPunctuation(char c) {
        for (Punctuation p : punctuation) {
            for (char form : p.whitespaceNonAcceptingForm) {
                if (form == c) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private TextFragment findAndRemoveWhitespacesAfterPunctuation(TextFragment textFragment) {        
        TextFragment newTextFragment = new TextFragment();
        char[] chars = textFragment.getCodedText().toCharArray();

        for (int i = 0; i < chars.length; i++) {

            if (TextFragment.isMarker(chars[i])) {
                int codeIndex = TextFragment.toIndex(chars[++i]);
                newTextFragment.append(textFragment.getCode(codeIndex));

                continue;
            }

            newTextFragment.append(chars[i]);

            if (isNonSpaceDelimitedPunctuation(chars[i]) &&
                    i + 1 < chars.length &&
                    isWhitespace(chars[i + 1])) {
                i = getLastWhitespacePosition(chars, i + 1);                
            }
        }

        return newTextFragment;
    }

    private int getLastWhitespacePosition(char[] chars, int position) {
        do {
            position++;
        } while (position < chars.length && isWhitespace(chars[position]));

        return --position;
    }
}
