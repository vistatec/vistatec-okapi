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

class StyleExclusionChecker {

    static boolean isStyleExcluded(String style, ConditionalParameters params) {
        // If style is null or list of excluded styles is empty, nothing should be excluded:
        if (null == style || null == params.tsExcludeWordStyles || params.tsExcludeWordStyles.isEmpty()) {
            return false;
        }

        return params.tsExcludeWordStyles.contains(style);
    }
}