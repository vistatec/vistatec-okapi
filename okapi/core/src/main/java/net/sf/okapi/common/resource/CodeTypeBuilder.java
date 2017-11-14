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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static net.sf.okapi.common.resource.Code.EXTENDED_CODE_TYPE_DELIMITER;
import static net.sf.okapi.common.resource.Code.EXTENDED_CODE_TYPE_PREFIX;
import static net.sf.okapi.common.resource.Code.EXTENDED_CODE_TYPE_VALUE_DELIMITER;

/**
 * Provides a code type builder.
 */
public class CodeTypeBuilder {
    private static final String EMPTY_VALUE = "";

    private boolean addExtendedCodeTypePrefix;

    private Set<String> codeTypes = new LinkedHashSet<>();
    private Map<String, String> codeTypesAndValues = new LinkedHashMap<>();

    public CodeTypeBuilder(boolean addExtendedCodeTypePrefix) {
        this.addExtendedCodeTypePrefix = addExtendedCodeTypePrefix;
    }

    public void addType(String type) {
        codeTypes.add(type);
    }

    public void addType(String type, String value) {
        codeTypesAndValues.put(type, value);
    }

    public String build() {
        if (codeTypes.isEmpty() && codeTypesAndValues.isEmpty()) {
            return EMPTY_VALUE;
        }

        StringBuilder codeTypeBuilder = new StringBuilder(addExtendedCodeTypePrefix ? EXTENDED_CODE_TYPE_PREFIX : EMPTY_VALUE);

        for (String codeType : codeTypes) {
            codeTypeBuilder.append(codeType).append(EXTENDED_CODE_TYPE_DELIMITER);
        }

        for (Map.Entry<String, String> codeTypeAndValue : codeTypesAndValues.entrySet()) {
            codeTypeBuilder.append(codeTypeAndValue.getKey()).append(EXTENDED_CODE_TYPE_VALUE_DELIMITER)
                    .append(codeTypeAndValue.getValue()).append(EXTENDED_CODE_TYPE_DELIMITER);
        }

        return codeTypeBuilder.toString();
    }
}
