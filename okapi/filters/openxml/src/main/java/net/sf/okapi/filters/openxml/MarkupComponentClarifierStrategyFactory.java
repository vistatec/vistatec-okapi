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

import java.util.Collections;

import static net.sf.okapi.filters.openxml.ClarifierStrategyFactoryValues.EMPTY_ATTRIBUTE_PREFIX;
import static net.sf.okapi.filters.openxml.ClarifierStrategyFactoryValues.RTL_BOOLEAN_VALUES;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_READING_ORDER;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_RIGHT_TO_LEFT;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_RTL;

/**
 * Provides a markup component clarifier strategy factory.
 */
class MarkupComponentClarifierStrategyFactory {

    private static final String READING_ORDER_RTL_VALUE = "2";

    /**
     * Creates a presentation markup component clarifier strategy.
     *
     * @param creationalParameters    Creational parameters
     * @param clarificationParameters Clarification parameters
     *
     * @return A presentation markup component clarifier strategy
     */
    static MarkupComponentClarifierStrategy createPresentationMarkupComponentClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
        return new MarkupComponentClarifierStrategy(creationalParameters, clarificationParameters,
                new ClarifiableAttribute(EMPTY_ATTRIBUTE_PREFIX, LOCAL_RTL, RTL_BOOLEAN_VALUES));
    }

    /**
     * Creates a sheet view markup component clarifier strategy.
     *
     * @param creationalParameters    Creational parameters
     * @param clarificationParameters Clarification parameters
     *
     * @return A sheet view markup component clarifier strategy
     */
    static MarkupComponentClarifierStrategy createSheetViewMarkupComponentClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
        return new MarkupComponentClarifierStrategy(creationalParameters, clarificationParameters,
                new ClarifiableAttribute(EMPTY_ATTRIBUTE_PREFIX, LOCAL_RIGHT_TO_LEFT, RTL_BOOLEAN_VALUES));
    }

    /**
     * Creates an alignment markup component clarifier strategy.
     *
     * @param creationalParameters    Creational parameters
     * @param clarificationParameters Clarification parameters
     *
     * @return An alignment markup component clarifier strategy
     */
    static MarkupComponentClarifierStrategy createAlignmentMarkupComponentClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
        return new MarkupComponentClarifierStrategy(creationalParameters, clarificationParameters,
                new ClarifiableAttribute(EMPTY_ATTRIBUTE_PREFIX, LOCAL_READING_ORDER, Collections.singletonList(READING_ORDER_RTL_VALUE)));
    }
}
