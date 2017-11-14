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

import static net.sf.okapi.filters.openxml.CodeTypeFactory.createCodeType;

class RunCode {

    private int codeId;
    private String codeType;
    private RunProperties runProperties;

    RunCode(int nextCodeId, RunProperties runProperties, RunProperties combinedRunProperties) {
        this.codeId = nextCodeId;
        this.codeType = createCodeType(combinedRunProperties);
        this.runProperties = runProperties;
    }

    int getCodeId() {
        return codeId;
    }

    String getCodeType() {
        return codeType;
    }

    RunProperties getRunProperties() {
        return runProperties;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + codeId + ", " + codeType + ", " + runProperties + ")";
    }
}