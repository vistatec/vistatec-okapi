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

import static net.sf.okapi.filters.idml.CodeTypes.createCodeType;

class ContentCode {

    private final int codeId;
    private final String codeType;
    private final StyleDefinitions styleDefinitions;
    private final StoryChildElement.StyledTextReferenceElement styledTextReferenceElement;

    ContentCode(int codeId, StyleDefinitions styleDefinitions, StoryChildElement.StyledTextReferenceElement styledTextReferenceElement) {
        this.codeId = codeId;

        codeType = null == styledTextReferenceElement
                ? createCodeType(styleDefinitions)
                : createCodeType(styledTextReferenceElement);

        this.styleDefinitions = styleDefinitions;
        this.styledTextReferenceElement = styledTextReferenceElement;
    }

    int getCodeId() {
        return codeId;
    }

    String getCodeType() {
        return codeType;
    }

    StyleDefinitions getStyleDefinitions() {
        return styleDefinitions;
    }

    StoryChildElement.StyledTextReferenceElement getStyledTextReferenceElement() {
        return styledTextReferenceElement;
    }
}
