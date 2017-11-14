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

import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.XMLEvent;

import static java.util.Collections.singletonList;

class SpecialCharacter extends MarkupRange {

    SpecialCharacter(XMLEvent event) {
        super(singletonList(event));
    }

    static SpecialCharacter fromXmlEvent(XMLEvent event) {
        return new SpecialCharacter(event);
    }

    XMLEvent getEvent() {
        return events.get(0);
    }

    enum SpecialCharacterType {

        FIXED_WIDTH_NON_BREAKING_SPACE('\u202F'),

        HAIR_SPACE('\u200A'),
        THIN_SPACE('\u2009'),
        PUNCTUATION_SPACE('\u2008'),
        FIGURE_SPACE('\u2007'),
        SIXTH_SPACE('\u2006'),
        QUARTER_SPACE('\u2005'),
        THIRD_SPACE('\u2004'),
        FLUSH_SPACE('\u2001'),

        FORCED_LINE_BREAK('\u2028'),
        DISCRETIONARY_LINE_BRAKE('\u200B'),
        ZERO_WIDTH_NON_JOINER('\u200C'),

        DISCRETIONARY_HYPHEN('\u00AD'),
        NON_BREAKING_HYPHEN('\u2011'),

        ZERO_WIDTH_NO_BREAK_SPACE('\uFEFF'),

        UNSUPPORTED(' ');

        char value;

        SpecialCharacterType(char value) {
            this.value = value;
        }

        static SpecialCharacterType fromChar(char character) {

            for (SpecialCharacterType specialCharacterType : values()) {
                if (specialCharacterType.getValue() == character) {
                    return specialCharacterType;
                }
            }

            return UNSUPPORTED;
        }

        static SpecialCharacterType fromSpecialCharacter(SpecialCharacter specialCharacter) {

            char characterValue = specialCharacter.getEvent().asCharacters().getData().toCharArray()[0];

            return fromChar(characterValue);
        }

        char getValue() {
            return value;
        }
    }

    static class Instruction extends SpecialCharacter {

        Instruction(XMLEvent event) {
            super(event);
        }

        static Instruction fromXmlEvent(XMLEvent event) {
            return new Instruction(event);
        }

        enum InstructionType {

            ALIGNMENT("0"),
            END_NESTED_STYLE("3"),
            FOOTNOTE_MARKER("4"),
            INDENT_HERE_TAB("7"),
            RIGHT_INDENT_TAB("8"),
            AUTO_PAGE_NUMBER("18"),
            SECTION_MARKER("19"),

            UNSUPPORTED("");

            String value;

            InstructionType(String value) {
                this.value = value;
            }

            static InstructionType fromInstruction(Instruction instruction) {

                for (InstructionType instructionType : values()) {
                    if (instructionType.getValue().equals(((ProcessingInstruction) instruction.getEvent()).getData())) {
                        return instructionType;
                    }
                }

                return UNSUPPORTED;
            }

            String getValue() {
                return value;
            }
        }
    }
}
