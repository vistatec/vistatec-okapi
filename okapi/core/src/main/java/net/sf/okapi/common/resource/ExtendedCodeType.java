/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.common.resource;

/**
 * Provides extended code types.
 */
public enum ExtendedCodeType {

    COLOR("color"),
    HIGHLIGHT("highlight"),
    SHADE("shade"),
    SHADOW("shadow"),
    STRIKE_THROUGH("strikethrough"),
    UNDERLINE("underline"),
    SUPERSCRIPT("sup"),
    SUBSCRIPT("sub"),

    TABLE_REFERENCE("table-ref"),
    FOOTNOTE_REFERENCE("footnote-ref"),
    NOTE_REFERENCE("note-ref"),

    ALIGNMENT("alignment"),
    END_NESTED_STYLE("end-nested-style"),
    FOOTNOTE_MARKER("footnote-marker"),
    INDENT_HERE_TAB("indent-here-tab"),
    RIGHT_INDENT_TAB("right-indent-tab"),
    AUTO_PAGE_NUMBER("auto-page-number"),
    SECTION_MARKER("section-marker"),

    FIXED_WIDTH_NON_BREAKING_SPACE("nbsp-fixed-width"),

    HAIR_SPACE("sp-hair"),
    THIN_SPACE("sp-thin"),
    PUNCTUATION_SPACE("sp-punctuation"),
    FIGURE_SPACE("sp-figure"),
    SIXTH_SPACE("sp-sixth"),
    QUARTER_SPACE("sp-quarter"),
    THIRD_SPACE("sp-third"),
    FLUSH_SPACE("sp-flush"),

    FORCED_LINE_BREAK("lb-forced"),
    DISCRETIONARY_LINE_BRAKE("lb-discretionary"),

    ZERO_WIDTH_NON_JOINER("nj-zero-width"),

    DISCRETIONARY_HYPHEN("hyphen-discretionary"),
    NON_BREAKING_HYPHEN("hyphen-nb"),

    ZERO_WIDTH_NO_BREAK_SPACE("nbsp-zero-width");

    String value;

    ExtendedCodeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
