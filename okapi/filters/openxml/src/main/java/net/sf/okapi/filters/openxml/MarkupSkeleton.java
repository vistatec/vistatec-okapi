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

/**
 * Provides a markup skeleton.
 */
class MarkupSkeleton implements ISkeleton {
    private Markup markup;
    private IResource parent;

    public MarkupSkeleton(Markup markup) {
        this.markup = markup;
    }

    public Markup getMarkup() {
        return markup;
    }

    @Override
    public ISkeleton clone() {
        return new MarkupSkeleton(markup);
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
