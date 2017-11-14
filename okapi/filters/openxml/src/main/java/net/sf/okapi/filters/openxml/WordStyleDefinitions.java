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

import java.util.Map;

/**
 * Provides the style definitions.
 */
class WordStyleDefinitions implements StyleDefinitions {

    /**
     * Document default run properties.
     */
    private RunProperties documentDefaultRunProperties;

    /**
     * Default styles by style types.
     */
    private Map<StyleType, String> defaultStylesByStyleTypes;

    /**
     * Style types by style IDs.
     */
    private Map<String, StyleType> styleTypesByStyleIds;

    /**
     * Parent styles by style IDs.
     */
    private Map<String, String> parentStylesByStyleIds;

    /**
     * Linked styles by style IDs.
     */
    private Map<String, String> linkedStylesByStyleIds;

    /**
     * Run properties by style IDs.
     */
    private Map<String, RunProperties> runPropertiesByStyleIds;

    /**
     * Construct the style definitions.
     *
     * @param documentDefaultRunProperties Document default run properties
     * @param defaultStylesByStyleTypes    Default styles by style types
     * @param styleTypesByStyleIds         Style types by style IDs
     * @param parentStylesByStyleIds       Parent styles by style IDs
     * @param linkedStylesByStyleIds       Linked styles by style IDs
     * @param runPropertiesByStyleIds      Run properties by style IDs
     */
    WordStyleDefinitions(RunProperties documentDefaultRunProperties,
                            Map<StyleType, String> defaultStylesByStyleTypes,
                            Map<String, StyleType> styleTypesByStyleIds,
                            Map<String, String> parentStylesByStyleIds,
                            Map<String, String> linkedStylesByStyleIds,
                            Map<String, RunProperties> runPropertiesByStyleIds) {

        this.documentDefaultRunProperties = documentDefaultRunProperties;
        this.defaultStylesByStyleTypes = defaultStylesByStyleTypes;
        this.styleTypesByStyleIds = styleTypesByStyleIds;
        this.parentStylesByStyleIds = parentStylesByStyleIds;
        this.linkedStylesByStyleIds = linkedStylesByStyleIds;
        this.runPropertiesByStyleIds = runPropertiesByStyleIds;
    }

    /**
     * Gets run properties combined through a semi-full style hierarchy.
     *
     * Here "semi-full" means that there is no table style involved.
     *
     * Firstly, the document defaults are applied to all runs and paragraphs in the document. Next, the table style
     * properties are applied to each table in the document, following the conditional formatting inclusions and
     * exclusions specified per table. Next, numbered item and paragraph properties are applied to each paragraph
     * formatted with a numbering style (we do not need this currently). Next, paragraph and run properties are applied
     * to each paragraph as defined by the paragraph style (pStyle). Next, run properties are applied to each run with a
     * specific character style applied (rStyle). Finally, we apply direct formatting (paragraph or run properties not
     * from styles).
     *
     * In oder to build up the resulting style, a consumer must trace the hierarchy (following each basedOn value) back
     * to a style which has no basedOn element (is not based on another style). The resulting style is then constructed
     * by following each level in the tree, applying the specified paragraph and/or character properties as appropriate.
     * When properties conflict, they are overridden by each subsequent level (this includes turning OFF a property set
     * at an earlier level). Properties which are not specified simply do not change those specified at earlier levels.
     *
     * As for the default attribute of the style element, it specifies that this style is the default for this style
     * type and is applied to objects of the same type that do not explicitly declare the style.
     *
     * In addition, the linked styles, which are groupings of paragraph and character styles, are merged into one if and
     * only if a paragraph and a character styles have been specified in the scope of one paragraph. If the current
     * style type is paragraph, then only all parent style run properties are combined. If the current style type is
     * character, then all linked style run properties are combined first (they are of paragraph type now) and then
     * merged with (overridden by) the current style run properties (which are also combined throughout the whole
     * hierarchy).
     *
     * And the last but not least, the toggle properties are combined in their own way. Firstly, if multiple instances
     * of a toggle property appear at the horizontal traversal stage (i.e paragraph or character) in the style hierarchy,
     * then the first closest to the root value encountered is used. Then, at the vertical traversal stage the already
     * gathered toggle properties are joined by XORing their values. After all that proceeded, at the document default
     * traversal stage, the document default toggle properties are joined by ORing their values. The absence of a toggle
     * property is corresponding to the "false" value. And finally, the directly specified toggle properties substitute
     * any other fond throughout the hierarchy.
     *
     * @param paragraphStyle A paragraph style
     * @param runStyle       A run style
     * @param runProperties  Run properties
     *
     * @return Run properties which are combined through the whole style hierarchy
     */
    @Override
    public RunProperties getCombinedRunProperties(String paragraphStyle, String runStyle, RunProperties runProperties) {
        // get all but strip toggle properties to apply them later
        RunProperties combinedRunProperties = RunProperties.copiedRunProperties(documentDefaultRunProperties, false, false, true);

        combinedRunProperties = combinedRunProperties.combineDistinct(getParagraphStyleProperties(paragraphStyle), TraversalStage.VERTICAL);
        combinedRunProperties = combinedRunProperties.combineDistinct(getRunStyleProperties(runStyle, paragraphStyle), TraversalStage.VERTICAL);

        // apply previously stripped toggle properties
        combinedRunProperties = combinedRunProperties.combineDistinct(RunProperties.copiedToggleRunProperties(documentDefaultRunProperties), TraversalStage.DOCUMENT_DEFAULT);

        // combine with the exclusion of the RunStyleProperty
        combinedRunProperties = combinedRunProperties.combineDistinct(RunProperties.copiedRunProperties(runProperties, false, true, false), TraversalStage.DIRECT);

        return combinedRunProperties;
    }

    private RunProperties getParagraphStyleProperties(String paragraphStyle) {
        return getPropertiesByTypeAndStyle(StyleType.PARAGRAPH, paragraphStyle, null);
    }

    private RunProperties getRunStyleProperties(String runStyle, String linkedStyle) {
        return getPropertiesByTypeAndStyle(StyleType.CHARACTER, runStyle, linkedStyle);
    }

    private RunProperties getPropertiesByTypeAndStyle(StyleType styleType, String styleId, String linkedStyleId) {
        if (null == styleId
                || !styleTypesByStyleIds.containsKey(styleId)
                || !styleTypesByStyleIds.get(styleId).equals(styleType)) {
            // if there is no style specified
            // or the style does not exist
            // or the style types do not match
            // return default properties for the specified style type

            String defaultStyleId = defaultStylesByStyleTypes.get(styleType);

            return combineParentStyleProperties(styleType, defaultStyleId, RunProperties.emptyRunProperties());
        }

        return combineLinkedAndParentStyleProperties(styleType, styleId, linkedStyleId, RunProperties.emptyRunProperties());
    }

    private RunProperties combineLinkedAndParentStyleProperties(StyleType styleType, String styleId, String linkedStyleId, RunProperties runProperties) {
        if (null == linkedStyleId
                || StyleType.CHARACTER != styleType
                || null == linkedStylesByStyleIds.get(linkedStyleId)
                || StyleType.PARAGRAPH != styleTypesByStyleIds.get(linkedStyleId)) {
            // if the linked style is not specified
            // or the style type is not of the linkable style type (character)
            // or the linked style is not present
            // or the linked style type is not a paragraph

            return combineParentStyleProperties(styleType, styleId, runProperties);
        }

        RunProperties paragraphProperties = combineParentStyleProperties(StyleType.PARAGRAPH, linkedStylesByStyleIds.get(linkedStyleId), RunProperties.emptyRunProperties());
        RunProperties characterProperties = combineParentStyleProperties(StyleType.CHARACTER, styleId, RunProperties.emptyRunProperties());

        return paragraphProperties.combineDistinct(characterProperties, TraversalStage.HORIZONTAL);
    }

    private RunProperties combineParentStyleProperties(StyleType styleType, String styleId, RunProperties runProperties) {
        if (null == runPropertiesByStyleIds.get(styleId)) {
            return runProperties;
        }

        if (null == parentStylesByStyleIds.get(styleId)
                || styleTypesByStyleIds.get(parentStylesByStyleIds.get(styleId)) != styleType) {
            // if there is no parent style
            // or the style types do not match

            return RunProperties.copiedRunProperties(runPropertiesByStyleIds.get(styleId)).combineDistinct(runProperties, TraversalStage.HORIZONTAL);
        }

        return combineParentStyleProperties(styleType, parentStylesByStyleIds.get(styleId),  RunProperties.copiedRunProperties(runPropertiesByStyleIds.get(styleId)))
                .combineDistinct(runProperties, TraversalStage.HORIZONTAL);
    }

}
