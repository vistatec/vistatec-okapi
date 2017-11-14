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

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;

import java.util.Map;

class StringItemSkeleton implements ISkeleton {
    private StringItem stringItem;
    private IResource parent;
    private Map<Integer, XMLEvents> codeMap;

    public StringItemSkeleton(StringItem stringItem, Map<Integer, XMLEvents> codeMap) {
        this.stringItem = stringItem;
        this.codeMap = codeMap;
    }

    @Override
    public ISkeleton clone() {
        StringItemSkeleton stringItemSkeleton = new StringItemSkeleton(stringItem, codeMap);
        stringItemSkeleton.setParent(getParent());
        return stringItemSkeleton;
    }

    public StringItem getStringItem() {
        return stringItem;
    }

    @Override
    public void setParent(IResource parent) {
        this.parent = parent;
    }

    @Override
    public IResource getParent() {
        return parent;
    }
}
