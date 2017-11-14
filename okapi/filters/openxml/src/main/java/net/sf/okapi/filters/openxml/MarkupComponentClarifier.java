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

import static net.sf.okapi.filters.openxml.MarkupComponentClarifierStrategyFactory.createAlignmentMarkupComponentClarifierStrategy;
import static net.sf.okapi.filters.openxml.MarkupComponentClarifierStrategyFactory.createPresentationMarkupComponentClarifierStrategy;
import static net.sf.okapi.filters.openxml.MarkupComponentClarifierStrategyFactory.createSheetViewMarkupComponentClarifierStrategy;

/**
 * Provides a markup component clarifier.
 */
class MarkupComponentClarifier {

    private MarkupComponentClarifierStrategy strategy;

    MarkupComponentClarifier(MarkupComponentClarifierStrategy strategy) {
        this.strategy = strategy;
    }

    void clarify(MarkupComponent markupComponent) {
        strategy.clarifyMarkupComponent(markupComponent);
    }

    /**
     * Provides a presentation clarifier.
     */
    static class PresentationClarifier extends MarkupComponentClarifier {

        PresentationClarifier(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
            super(createPresentationMarkupComponentClarifierStrategy(creationalParameters, clarificationParameters));
        }
    }

    /**
     * Provides a sheet view clarifier.
     */
    static class SheetViewClarifier extends MarkupComponentClarifier {

        SheetViewClarifier(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
            super(createSheetViewMarkupComponentClarifierStrategy(creationalParameters, clarificationParameters));
        }
    }

    /**
     * Provides an alignment clarifier.
     */
    static class AlignmentClarifier extends MarkupComponentClarifier {

        AlignmentClarifier(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
            super(createAlignmentMarkupComponentClarifierStrategy(creationalParameters, clarificationParameters));
        }
    }
}
