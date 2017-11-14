/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.filters.xliff;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that manages ctype attributes for paired tags ({@code <bx>}, {@code <ex>},
 * {@code <bpt>}, {@code <ept>}). The tags use either their {@code id} or {@code rid} attribute
 * for linking the other tag. The value of the {@code rid} attribute should be preferred if present.
 *
 * @author ccudennec
 * @since 26.01.2017
 */
class CodeTypeForPairedTagsHelper {

    public static final String DEFAULT_CODE_TYPE = "Xpt";

    private Map<String, String> codeTypesById = new HashMap<>();
    private Map<String, String> codeTypesByRid = new HashMap<>();

    /**
     * Stores the given {@code ctype} or {@link #DEFAULT_CODE_TYPE} under the given rid or id.
     *
     * @return the ctype or {@link #DEFAULT_CODE_TYPE}
     */
    public String store(String rid, String id, String ctype) {
        String codeType = getNonEmptyCodeType(ctype);
        if (rid != null) {
            codeTypesByRid.put(rid, codeType);
        }
        if (id != null) {
            codeTypesById.put(id, codeType);
        }
        return codeType;
    }

    /**
     * @return the code type stored under the given rid (or id) or {@link #DEFAULT_CODE_TYPE}
     */
    public String find(String rid, String id) {
        String ctype = null;
        if (rid != null && codeTypesByRid.containsKey(rid)) {
            ctype = codeTypesByRid.get(rid);
        }
        else if (id != null && codeTypesById.containsKey(id)) {
            ctype = codeTypesById.get(id);
        }
        return getNonEmptyCodeType(ctype);
    }

    private String getNonEmptyCodeType(String ctype) {
        return ctype != null && ctype.trim().length() > 0 ? ctype : DEFAULT_CODE_TYPE;
    }

}
