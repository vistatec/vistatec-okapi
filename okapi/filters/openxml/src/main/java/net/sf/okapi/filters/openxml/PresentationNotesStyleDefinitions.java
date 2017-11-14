/*
 * =============================================================================
 *   Copyright (C) 2010-2017 by the Okapi Framework contributors
 * -----------------------------------------------------------------------------
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =============================================================================
 */

package net.sf.okapi.filters.openxml;

import java.util.Map;

import static net.sf.okapi.filters.openxml.PresentationNotesStylesParser.DEFAULT_PARAGRAPH_LEVEL_ID;
import static net.sf.okapi.filters.openxml.RunProperties.copiedRunProperties;

/**
 * Provides presentation notes style definitions.
 *
 * @author ccudennec
 * @since 06.09.2017
 */
class PresentationNotesStyleDefinitions implements StyleDefinitions {

    private final RunProperties extraDocumentDefaultRunProperties;
    private final Map<String, RunProperties> runPropertiesByParagraphLevelIds;

    PresentationNotesStyleDefinitions(RunProperties extraDocumentDefaultRunProperties, Map<String, RunProperties> runPropertiesByParagraphLevelIdss) {
        this.extraDocumentDefaultRunProperties = extraDocumentDefaultRunProperties;
        this.runPropertiesByParagraphLevelIds = runPropertiesByParagraphLevelIdss;
    }

    /**
     * Gets the combined run properties by applying the styles in the following order:
     *
     * <ul>
     * <li>extra document default styles (see {@link #extraDocumentDefaultRunProperties}</li>
     * <li>paragraph level styles from master</li>
     * <li>direct styles {@link RunProperties}</li>
     * </ul>
     *
     * @param paragraphLevelId The paragraph level ID
     * @param runStyle         <i>Unused</i>
     * @param runProperties    The run properties
     *
     * @return The combined run properties
     */
    @Override
    public RunProperties getCombinedRunProperties(String paragraphLevelId, String runStyle, RunProperties runProperties) {

        RunProperties combinedRunProperties = copiedRunProperties(extraDocumentDefaultRunProperties);

        combinedRunProperties.combineDistinct(copiedRunProperties(getParagraphLevelProperties(paragraphLevelId)), TraversalStage.DIRECT);

        combinedRunProperties = combinedRunProperties.combineDistinct(copiedRunProperties(runProperties), TraversalStage.DIRECT);

        return combinedRunProperties;
    }

    private RunProperties getParagraphLevelProperties(String paragraphLevelId) {
        return runPropertiesByParagraphLevelIds.get(null != paragraphLevelId ? paragraphLevelId : DEFAULT_PARAGRAPH_LEVEL_ID);
    }
}
