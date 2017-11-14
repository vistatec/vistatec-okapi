/*
 * =============================================================================
 *   Copyright (C) 2010-2017 by the Okapi Framework contributors
 * -----------------------------------------------------------------------------
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =============================================================================
 */

package net.sf.okapi.filters.idml;

import net.sf.okapi.common.resource.CodeTypeBuilder;
import net.sf.okapi.common.resource.ExtendedCodeType;
import net.sf.okapi.filters.idml.SpecialCharacter.Instruction.InstructionType;
import net.sf.okapi.filters.idml.SpecialCharacter.SpecialCharacterType;

import javax.xml.stream.events.Attribute;

import static net.sf.okapi.common.resource.Code.TYPE_BOLD;
import static net.sf.okapi.common.resource.Code.TYPE_ITALIC;
import static net.sf.okapi.common.resource.Code.TYPE_LINK;
import static net.sf.okapi.common.resource.ExtendedCodeType.FOOTNOTE_REFERENCE;
import static net.sf.okapi.common.resource.ExtendedCodeType.NOTE_REFERENCE;
import static net.sf.okapi.common.resource.ExtendedCodeType.STRIKE_THROUGH;
import static net.sf.okapi.common.resource.ExtendedCodeType.SUBSCRIPT;
import static net.sf.okapi.common.resource.ExtendedCodeType.SUPERSCRIPT;
import static net.sf.okapi.common.resource.ExtendedCodeType.TABLE_REFERENCE;
import static net.sf.okapi.common.resource.ExtendedCodeType.UNDERLINE;

class CodeTypes {

    private static final String SKEW_ATTRIBUTE_NAME = "Skew";
    private static final String STRIKE_THROUGH_ATTRIBUTE_NAME = "StrikeThru";
    private static final String UNDERLINE_ATTRIBUTE_NAME = "Underline";
    private static final String FONT_STYLE_ATTRIBUTE_NAME = "FontStyle";
    private static final String POSITION_ATTRIBUTE_NAME = "Position";

    private static final String FONT_STYLE_BOLD_VALUE = "Bold";
    private static final String POSITION_SUPERSCRIPT_VALUE = "Superscript";
    private static final String POSITION_SUBSCRIPT_VALUE = "Subscript";

    private static final String EMPTY_VALUE = "";

    private static final boolean ADD_EXTENDED_CODE_TYPE_PREFIX = true;

    static String createCodeType(MarkupRange markupRange) {
        return createCodeType(markupRange, ADD_EXTENDED_CODE_TYPE_PREFIX);
    }

    private static String createCodeType(MarkupRange markupRange, boolean addExtendedCodeTypePrefix) {
        CodeTypeBuilder codeTypeBuilder = new CodeTypeBuilder(addExtendedCodeTypePrefix);

        if (markupRange instanceof StoryChildElement.StyledTextReferenceElement.HyperlinkTextSource) {
            codeTypeBuilder.addType(TYPE_LINK);

        } else if (markupRange instanceof StoryChildElement.StyledTextReferenceElement.Table) {
            codeTypeBuilder.addType(TABLE_REFERENCE.getValue());

        } else if (markupRange instanceof StoryChildElement.StyledTextReferenceElement.Footnote) {
            codeTypeBuilder.addType(FOOTNOTE_REFERENCE.getValue());

        } else if (markupRange instanceof StoryChildElement.StyledTextReferenceElement.Note) {
            codeTypeBuilder.addType(NOTE_REFERENCE.getValue());

        } else if (markupRange instanceof SpecialCharacter) {
            codeTypeBuilder.addType(fromSpecialCharacter((SpecialCharacter) markupRange));
        }

        return codeTypeBuilder.build();
    }

    private static String fromSpecialCharacter(SpecialCharacter specialCharacter) {

        if (specialCharacter instanceof SpecialCharacter.Instruction) {
            return fromSpecialCharacterInstruction((SpecialCharacter.Instruction) specialCharacter);
        }

        SpecialCharacterType specialCharacterType = SpecialCharacterType.fromSpecialCharacter(specialCharacter);

        switch (specialCharacterType) {
            case FIXED_WIDTH_NON_BREAKING_SPACE:
                return ExtendedCodeType.FIXED_WIDTH_NON_BREAKING_SPACE.getValue();
            case HAIR_SPACE:
                return ExtendedCodeType.HAIR_SPACE.getValue();
            case THIN_SPACE:
                return ExtendedCodeType.THIN_SPACE.getValue();
            case PUNCTUATION_SPACE:
                return ExtendedCodeType.PUNCTUATION_SPACE.getValue();
            case FIGURE_SPACE:
                return ExtendedCodeType.FIGURE_SPACE.getValue();
            case SIXTH_SPACE:
                return ExtendedCodeType.SIXTH_SPACE.getValue();
            case QUARTER_SPACE:
                return ExtendedCodeType.QUARTER_SPACE.getValue();
            case THIRD_SPACE:
                return ExtendedCodeType.THIRD_SPACE.getValue();
            case FLUSH_SPACE:
                return ExtendedCodeType.FLUSH_SPACE.getValue();
            case FORCED_LINE_BREAK:
                return ExtendedCodeType.FORCED_LINE_BREAK.getValue();
            case DISCRETIONARY_LINE_BRAKE:
                return ExtendedCodeType.DISCRETIONARY_LINE_BRAKE.getValue();
            case ZERO_WIDTH_NON_JOINER:
                return ExtendedCodeType.ZERO_WIDTH_NON_JOINER.getValue();
            case DISCRETIONARY_HYPHEN:
                return ExtendedCodeType.DISCRETIONARY_HYPHEN.getValue();
            case NON_BREAKING_HYPHEN:
                return ExtendedCodeType.NON_BREAKING_HYPHEN.getValue();
            case ZERO_WIDTH_NO_BREAK_SPACE:
                return ExtendedCodeType.ZERO_WIDTH_NO_BREAK_SPACE.getValue();
        }

        return EMPTY_VALUE;
    }

    private static String fromSpecialCharacterInstruction(SpecialCharacter.Instruction instruction) {
        InstructionType instructionType = InstructionType.fromInstruction(instruction);

        switch (instructionType) {
            case ALIGNMENT:
                return ExtendedCodeType.ALIGNMENT.getValue();
            case END_NESTED_STYLE:
                return ExtendedCodeType.END_NESTED_STYLE.getValue();
            case FOOTNOTE_MARKER:
                return ExtendedCodeType.FOOTNOTE_MARKER.getValue();
            case INDENT_HERE_TAB:
                return ExtendedCodeType.INDENT_HERE_TAB.getValue();
            case RIGHT_INDENT_TAB:
                return ExtendedCodeType.RIGHT_INDENT_TAB.getValue();
            case AUTO_PAGE_NUMBER:
                return ExtendedCodeType.AUTO_PAGE_NUMBER.getValue();
            case SECTION_MARKER:
                return ExtendedCodeType.SECTION_MARKER.getValue();
        }

        return EMPTY_VALUE;
    }

    static String createCodeType(StyleDefinitions styleDefinitions) {
        return createCodeType(styleDefinitions, ADD_EXTENDED_CODE_TYPE_PREFIX);
    }

    // TODO: rewrite when full styles hierarchy is available
    private static String createCodeType(StyleDefinitions styleDefinitions, boolean addExtendedCodeTypePrefix) {

        CodeTypeBuilder codeTypeBuilder = new CodeTypeBuilder(addExtendedCodeTypePrefix);

        StyleRange combinedStyleRange = styleDefinitions.getCombinedStyleRange();

        for (Attribute attribute : combinedStyleRange.getAttributes()) {

            if (SKEW_ATTRIBUTE_NAME.equals(attribute.getName().getLocalPart())) {
                codeTypeBuilder.addType(TYPE_ITALIC);
                continue;
            }

            if (STRIKE_THROUGH_ATTRIBUTE_NAME.equals(attribute.getName().getLocalPart())) {
                codeTypeBuilder.addType(STRIKE_THROUGH.getValue());
                continue;
            }

            if (UNDERLINE_ATTRIBUTE_NAME.equals(attribute.getName().getLocalPart())) {
                codeTypeBuilder.addType(UNDERLINE.getValue());
                continue;
            }

            if (FONT_STYLE_ATTRIBUTE_NAME.equals(attribute.getName().getLocalPart())) {

                if (FONT_STYLE_BOLD_VALUE.equals(attribute.getValue())) {
                    codeTypeBuilder.addType(TYPE_BOLD);
                    continue;
                }
            }

            if (POSITION_ATTRIBUTE_NAME.equals(attribute.getName().getLocalPart())) {

                switch (attribute.getValue()) {
                    case POSITION_SUPERSCRIPT_VALUE:
                        codeTypeBuilder.addType(SUPERSCRIPT.getValue());
                        continue;
                    case POSITION_SUBSCRIPT_VALUE:
                        codeTypeBuilder.addType(SUBSCRIPT.getValue());
                        continue;
                    default:
                        // baseline is not supported
                }
            }
        }

        return codeTypeBuilder.build();
    }
}

