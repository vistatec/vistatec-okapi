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

import java.util.ListIterator;

import static net.sf.okapi.filters.openxml.BlockPropertiesClarifierStrategyFactory.createParagraphPropertiesClarifierStrategy;
import static net.sf.okapi.filters.openxml.BlockPropertiesClarifierStrategyFactory.createTablePropertiesClarifierStrategy;
import static net.sf.okapi.filters.openxml.BlockPropertiesClarifierStrategyFactory.createTextBodyPropertiesClarifierStrategy;

/**
 * Provides a block properties clarifier.
 */
class BlockPropertiesClarifier {

    private BlockPropertiesClarifierStrategy strategy;

    BlockPropertiesClarifier(BlockPropertiesClarifierStrategy strategy) {
        this.strategy = strategy;
    }

    void clarify(ListIterator<MarkupComponent> markupComponentIterator) {
        strategy.clarifyBlockProperties(markupComponentIterator);
    }

    /**
     * Provides a paragraph properties clarifier.
     */
    static class ParagraphPropertiesClarifier extends BlockPropertiesClarifier {

        ParagraphPropertiesClarifier(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
            super(createParagraphPropertiesClarifierStrategy(creationalParameters, clarificationParameters));
        }
    }

    /**
     * Provides a text body properties clarifier.
     */
    static class TextBodyPropertiesClarifier extends BlockPropertiesClarifier {

        TextBodyPropertiesClarifier(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
            super(createTextBodyPropertiesClarifierStrategy(creationalParameters, clarificationParameters));
        }
    }

    /**
     * Provides a table properties clarifier.
     */
    static class TablePropertiesClarifier extends BlockPropertiesClarifier {

        TablePropertiesClarifier(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
            super(createTablePropertiesClarifierStrategy(creationalParameters, clarificationParameters));
        }
    }
}
