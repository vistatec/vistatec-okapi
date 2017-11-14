/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.tm.pensieve.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Field;

/**
 * The types of metadata that is supported. Currently all properties use the same store and indexTypes
 * @author HaslamJD
 */
public enum MetadataType {
    //TODO move ID which should be required to the TranslationUnitField enum
    ID("tuid"),
    TYPE("datatype"),
    GROUP_NAME("Txt::GroupName"),
    FILE_NAME("Txt::FileName");

    private String fieldName;

    private static Map<String, MetadataType> mapping = new HashMap<String, MetadataType>() {
        private static final long serialVersionUID = 2838020991609251445L;

		{
            put("Txt::GroupName", GROUP_NAME);
            put("Txt::FileName", FILE_NAME);
            put("datatype", TYPE);
            put("tuid", ID);
        }
    };

    private MetadataType(String fieldName) {
        this.fieldName = fieldName;
    }

    public String fieldName() {
        return fieldName;
    }

    public Field.Store store() {
        return Field.Store.YES;
    }

    public Field.Index indexType() {
        return Field.Index.NOT_ANALYZED;
    }

    public static MetadataType findMetadataType(String keyword) {
        return mapping.get(keyword);
    }
}


