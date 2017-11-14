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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_BIDIRECTIONAL;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_RTL;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_RUN_PROPERTIES;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_PROPERTY_LANGUAGE;

/**
 * Provides a run properties clarifier.
 */
class RunPropertiesClarifier {

    private static final int DEFAULT_ATTRIBUTES_SIZE = 1;

    private CreationalParameters creationalParameters;
    private ClarificationParameters clarificationParameters;

    RunPropertiesClarifier(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
        this.creationalParameters = creationalParameters;
        this.clarificationParameters = clarificationParameters;
    }

    /**
     * Clarifies run properties.
     *
     * @param runProperties Run properties
     *
     * @return Clarified run properties
     *         {@code null} if the resulted run properties are empty
     */
    RunProperties clarify(RunProperties runProperties) {
        if (null == runProperties) {
            if (!clarificationParameters.shouldBeBidirectional()) {
                return null;
            }

            return addBidirectionalAndLangRunProperties(null);
        }

        Iterator<RunProperty> iterator = runProperties.getProperties().iterator();

        while (iterator.hasNext()) {
            RunProperty runProperty = iterator.next();

            if (LOCAL_RTL.equals(runProperty.getName().getLocalPart())) {
                // the lang property is always stripped on the parsing stage

                if (!clarificationParameters.shouldBeBidirectional()) {
                    return removeRunProperty(runProperties, iterator);
                }

                return addLangRunProperty(runProperties);
            }
        }

        if (clarificationParameters.shouldBeBidirectional()) {
            return addBidirectionalAndLangRunProperties(runProperties);
        }

        return runProperties;
    }

    private RunProperties addBidirectionalAndLangRunProperties(RunProperties runProperties) {
        runProperties = addRunProperty(runProperties, LOCAL_RTL, Collections.<String, String>emptyMap());

        return addLangRunProperty(runProperties);
    }

    private RunProperties addLangRunProperty(RunProperties runProperties) {
        if (!clarificationParameters.shouldEntailBidirectionalLang()) {
            return runProperties;
        }

        Map<String, String> attributes = new HashMap<>(DEFAULT_ATTRIBUTES_SIZE);
        attributes.put(LOCAL_BIDIRECTIONAL, clarificationParameters.getBidirectionalLang());

        return addRunProperty(runProperties, LOCAL_PROPERTY_LANGUAGE, attributes);
    }

    private RunProperties addRunProperty(RunProperties runProperties, String localName, Map<String, String> attributes) {
        if (null == runProperties) {
            runProperties = RunProperties.defaultRunProperties(
                    creationalParameters.getEventFactory().createStartElement(
                            creationalParameters.getPrefix(), creationalParameters.getNamespaceUri(), LOCAL_RUN_PROPERTIES),
                    creationalParameters.getEventFactory().createEndElement(
                            creationalParameters.getPrefix(), creationalParameters.getNamespaceUri(), LOCAL_RUN_PROPERTIES));
        }
        runProperties.getProperties().add(RunPropertyFactory.createRunProperty(creationalParameters, localName, attributes));

        return runProperties;
    }

    private RunProperties removeRunProperty(RunProperties runProperties, Iterator<RunProperty> iterator) {
        iterator.remove();

        if (runProperties.getProperties().isEmpty()) {
            return null;
        }

        return runProperties;
    }
}
