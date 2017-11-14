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

package net.sf.okapi.filters.openxml;

import net.sf.okapi.common.resource.CodeTypeBuilder;
import net.sf.okapi.filters.openxml.RunProperty.HyperlinkRunProperty;
import net.sf.okapi.filters.openxml.RunProperty.SmlRunProperty;
import net.sf.okapi.filters.openxml.RunProperty.WpmlToggleRunProperty;
import net.sf.okapi.filters.openxml.RunPropertyFactory.SmlPropertyName;
import net.sf.okapi.filters.openxml.RunPropertyFactory.WpmlTogglePropertyName;

import static net.sf.okapi.common.resource.Code.TYPE_BOLD;
import static net.sf.okapi.common.resource.Code.TYPE_ITALIC;
import static net.sf.okapi.common.resource.Code.TYPE_LINK;
import static net.sf.okapi.common.resource.ExtendedCodeType.COLOR;
import static net.sf.okapi.common.resource.ExtendedCodeType.HIGHLIGHT;
import static net.sf.okapi.common.resource.ExtendedCodeType.SHADE;
import static net.sf.okapi.common.resource.ExtendedCodeType.SHADOW;
import static net.sf.okapi.common.resource.ExtendedCodeType.STRIKE_THROUGH;
import static net.sf.okapi.common.resource.ExtendedCodeType.SUBSCRIPT;
import static net.sf.okapi.common.resource.ExtendedCodeType.SUPERSCRIPT;
import static net.sf.okapi.common.resource.ExtendedCodeType.UNDERLINE;
import static net.sf.okapi.filters.openxml.RunContainer.Type.HYPERLINK;

/**
 * Provides a code type factory.
 */
class CodeTypeFactory {

    private static final String EMPHASIS_MARK_PROPERTY_NAME = "em";
    private static final String UNDERLINE_PROPERTY_NAME = "u";
    private static final String BOLD_PROPERTY_NAME = "b";
    private static final String ITALIC_PROPERTY_NAME = "i";
    private static final String HIGHLIGHT_COLOR_PROPERTY_NAME = "highlight";
    private static final String SHADE_COLOR_PROPERTY_NAME = "shd";
    private static final String COLOR_PROPERTY_NAME = "color";
    private static final String VERTICAL_ALIGNMENT_PROPERTY_NAME = "vertAlign";
    private static final String HLINK_CLICK_PROPERTY_NAME = "hlinkClick";
    private static final String HLINK_MOUSEOVER_PROPERTY_NAME = "hlinkMouseOver";

    private static final String VERTICAL_ALIGNMENT_SUPERSCRIPT_VALUE = "superscript";
    private static final String VERTICAL_ALIGNMENT_SUBSCRIPT_VALUE = "subscript";

    private static final String NONE_VALUE = "none";

    private static final boolean ADD_EXTENDED_CODE_TYPE_PREFIX = true;

    static String createCodeType(RunContainer runContainer) {

        CodeTypeBuilder codeTypeBuilder = new CodeTypeBuilder(ADD_EXTENDED_CODE_TYPE_PREFIX);

        if (HYPERLINK == runContainer.getType()) {
            codeTypeBuilder.addType(TYPE_LINK);
        } else {
            codeTypeBuilder.addType(runContainer.getType().getValue());
        }

        return codeTypeBuilder.build() + createCodeType(runContainer.getDefaultCombinedRunProperties(), !ADD_EXTENDED_CODE_TYPE_PREFIX);
    }

    static String createCodeType(RunProperties runProperties) {
        return createCodeType(runProperties, ADD_EXTENDED_CODE_TYPE_PREFIX);
    }

    private static String createCodeType(RunProperties runProperties, boolean addExtendedCodeTypePrefix) {

        CodeTypeBuilder codeTypeBuilder = new CodeTypeBuilder(addExtendedCodeTypePrefix);

        for (RunProperty runProperty : runProperties.getProperties()) {
            if (runProperty instanceof WpmlToggleRunProperty) {
                handleWpmlToggleRunProperty(codeTypeBuilder, (WpmlToggleRunProperty) runProperty);
                continue;
            }
            else if (runProperty instanceof SmlRunProperty) {
                handleSmlRunProperty(codeTypeBuilder, (SmlRunProperty) runProperty);
                continue;
            }
            if (runProperty instanceof HyperlinkRunProperty) {
                handleHyperlinkRunProperty(codeTypeBuilder, (HyperlinkRunProperty) runProperty);
                continue;
            }

            handleRunProperty(codeTypeBuilder, runProperty);
        }

        return codeTypeBuilder.build();
    }

    private static void handleWpmlToggleRunProperty(CodeTypeBuilder codeTypeBuilder, WpmlToggleRunProperty toggleRunProperty) {
        if (!toggleRunProperty.getToggleValue()) {
            // get rid of "false" values
            return;
        }

        switch (WpmlTogglePropertyName.fromValue(toggleRunProperty.getName())) {
            case BOLD:
            case COMPLEX_SCRIPT_BOLD:
                codeTypeBuilder.addType(TYPE_BOLD);
                break;

            case ITALICS:
            case COMPLEX_SCRIPT_ITALICS:
                codeTypeBuilder.addType(TYPE_ITALIC);
                break;

            case STRIKE_THROUGH:
                codeTypeBuilder.addType(STRIKE_THROUGH.getValue());
                break;

            case SHADOW:
                codeTypeBuilder.addType(SHADOW.getValue());
                break;
        }
    }

    private static void handleSmlRunProperty(CodeTypeBuilder codeTypeBuilder, SmlRunProperty runProperty) {
        String value = runProperty.getValue();
        if (value == null) {
            value = runProperty.getDefaultValue();
        }

        if ("false".equals(value)) {
            // get rid of "false" values
            return;
        }

        switch (SmlPropertyName.fromValue(runProperty.getName())) {
            case BOLD:
                codeTypeBuilder.addType(TYPE_BOLD);
                break;

            case ITALICS:
                codeTypeBuilder.addType(TYPE_ITALIC);
                break;

            case STRIKE_THROUGH:
                codeTypeBuilder.addType(STRIKE_THROUGH.getValue());
                break;

            case SHADOW:
                codeTypeBuilder.addType(SHADOW.getValue());
                break;

            case UNDERLINE:
                codeTypeBuilder.addType(UNDERLINE.getValue(), value);
                break;
        }
    }

    private static void handleHyperlinkRunProperty(CodeTypeBuilder codeTypeBuilder,
            HyperlinkRunProperty runProperty) {
        if (null == runProperty.getValue() || NONE_VALUE.equals(runProperty.getValue())) {
            // get rid of "null" and "none" values
            return;
        }
        codeTypeBuilder.addType(TYPE_LINK);
    }

    private static void handleRunProperty(CodeTypeBuilder codeTypeBuilder, RunProperty runProperty) {
        if (null == runProperty.getValue() || NONE_VALUE.equals(runProperty.getValue())) {
            // get rid of "null" and "none" values
            return;
        }

        String runPropertyName = runProperty.getName().getLocalPart();
        if (BOLD_PROPERTY_NAME.equals(runPropertyName)) {
            addCodeTypeIfNecessary(runProperty.getValue(), TYPE_BOLD, codeTypeBuilder);
            return;
        }
        if (ITALIC_PROPERTY_NAME.equals(runPropertyName)) {
            addCodeTypeIfNecessary(runProperty.getValue(), TYPE_ITALIC, codeTypeBuilder);
            return;
        }
        if (UNDERLINE_PROPERTY_NAME.equals(runPropertyName)) {
            codeTypeBuilder.addType(UNDERLINE.getValue(), runProperty.getValue());
            return;
        }
        if (EMPHASIS_MARK_PROPERTY_NAME.equals(runPropertyName)) {
            codeTypeBuilder.addType(TYPE_ITALIC);
            return;
        }
        if (HIGHLIGHT_COLOR_PROPERTY_NAME.equals(runPropertyName)) {
            codeTypeBuilder.addType(HIGHLIGHT.getValue(), runProperty.getValue());
            return;
        }
        if (SHADE_COLOR_PROPERTY_NAME.equals(runPropertyName)) {
            codeTypeBuilder.addType(SHADE.getValue(), runProperty.getValue());
            return;
        }
        if (COLOR_PROPERTY_NAME.equals(runPropertyName)) {
            codeTypeBuilder.addType(COLOR.getValue(), runProperty.getValue());
            return;
        }

        if (VERTICAL_ALIGNMENT_PROPERTY_NAME.equals(runPropertyName)) {

            switch (runProperty.getValue()) {
                case VERTICAL_ALIGNMENT_SUPERSCRIPT_VALUE:
                    codeTypeBuilder.addType(SUPERSCRIPT.getValue());
                    return;
                case VERTICAL_ALIGNMENT_SUBSCRIPT_VALUE:
                    codeTypeBuilder.addType(SUBSCRIPT.getValue());
                    return;
                default:
                    // baseline is not supported
            }
        }
    }

    /**
     * Adds the code type according to the toggled attribute value.
     *
     * @param runPropertyValue the value of the attribute
     * @param codeType the code type to add
     * @param codeTypeBuilder the code type builder
     */
    private static void addCodeTypeIfNecessary(
            String runPropertyValue, String codeType, CodeTypeBuilder codeTypeBuilder) {
        switch (codeType) {
            case TYPE_BOLD:
            case TYPE_ITALIC:
                if ("1".equals(runPropertyValue)) {
                    codeTypeBuilder.addType(codeType);
                }
                break;
            default:
                // no other code types should be handled here
        }

    }
}
