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
===========================================================================*/

package net.sf.okapi.filters.markdown;

import net.sf.okapi.common.filters.EventBuilder;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;

public class MarkdownEventBuilder extends EventBuilder {
    private InlineCodeFinder codeFinder;

    public MarkdownEventBuilder(String rootId, IFilter subFilter) {
        super(rootId, subFilter);
        codeFinder = null;
    }

    @Override
    protected ITextUnit postProcessTextUnit(ITextUnit textUnit) {
        TextFragment text = textUnit.getSource().getFirstContent();
        if (codeFinder != null) {
            codeFinder.process(text);
        }
        return textUnit;
    }

    public void setCodeFinder(InlineCodeFinder codeFinder) {
        this.codeFinder = codeFinder;
    }
}
