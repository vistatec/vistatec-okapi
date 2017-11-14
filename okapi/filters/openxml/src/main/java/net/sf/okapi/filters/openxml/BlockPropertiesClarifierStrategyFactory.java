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

import net.sf.okapi.filters.openxml.BlockPropertiesClarifierStrategy.PropertiesClarifierStrategy.ParagraphPropertiesClarifierStrategy;

import static net.sf.okapi.filters.openxml.ClarifierStrategyFactoryValues.EMPTY_ATTRIBUTE_PREFIX;
import static net.sf.okapi.filters.openxml.ClarifierStrategyFactoryValues.RTL_BOOLEAN_VALUES;
import static net.sf.okapi.filters.openxml.Namespaces.DrawingML;
import static net.sf.okapi.filters.openxml.Namespaces.fromNamespaceURI;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_RTL;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_RTL_COL;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.PREFIX_A;

/**
 * Provides a block properties clarifier strategy factory.
 */
class BlockPropertiesClarifierStrategyFactory {

    /**
     * Creates a paragraph properties clarifier strategy.
     *
     * @param creationalParameters    Creational parameters
     * @param clarificationParameters Clarification parameters
     *
     * @return A paragraph properties clarifier strategy
     */
    static BlockPropertiesClarifierStrategy createParagraphPropertiesClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
        return new ParagraphPropertiesClarifierStrategy(creationalParameters, clarificationParameters);
    }

    /**
     * Creates a text body properties clarifier strategy.
     *
     * @param creationalParameters    Creational parameters
     * @param clarificationParameters Clarification parameters
     *
     * @return A text body properties clarifier strategy
     */
    static BlockPropertiesClarifierStrategy createTextBodyPropertiesClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
        CreationalParameters newCreationalParameters = (DrawingML != fromNamespaceURI(creationalParameters.getNamespaceUri()))
                ? new CreationalParameters(
                    creationalParameters.getEventFactory(),
                    PREFIX_A,
                    DrawingML.getURI())
                : creationalParameters;

        return new BlockPropertiesClarifierStrategy.AttributesClarifierStrategy.TextBodyPropertiesClarifierStrategy(newCreationalParameters, clarificationParameters,
                new ClarifiableAttribute(EMPTY_ATTRIBUTE_PREFIX, LOCAL_RTL_COL, RTL_BOOLEAN_VALUES));
    }

    /**
     * Creates a table properties clarifier strategy.
     *
     * @param creationalParameters    Creational parameters
     * @param clarificationParameters Clarification parameters
     *
     * @return A table properties clarifier strategy
     */
    static BlockPropertiesClarifierStrategy createTablePropertiesClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
        if (DrawingML == fromNamespaceURI(creationalParameters.getNamespaceUri())) {
            return new BlockPropertiesClarifierStrategy.AttributesClarifierStrategy.TablePropertiesClarifierStrategy(creationalParameters, clarificationParameters,
                    new ClarifiableAttribute(EMPTY_ATTRIBUTE_PREFIX, LOCAL_RTL, RTL_BOOLEAN_VALUES));
        }

        return new BlockPropertiesClarifierStrategy.PropertiesClarifierStrategy.TablePropertiesClarifierStrategy(creationalParameters, clarificationParameters);
    }
}
