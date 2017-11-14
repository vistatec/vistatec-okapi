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

import javax.xml.stream.events.Attribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static net.sf.okapi.filters.openxml.BlockPropertiesFactory.createParagraphProperties;
import static net.sf.okapi.filters.openxml.BlockPropertiesFactory.createTableProperties;
import static net.sf.okapi.filters.openxml.BlockPropertiesFactory.createTextBodyProperties;
import static net.sf.okapi.filters.openxml.BlockPropertyFactory.createBlockProperty;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_BIDIRECTIONAL;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_BIDI_VISUAL;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_PARAGRAPH_PROPERTIES;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_TABLE_PROPERTIES;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_TEXT_BODY_PROPERTIES;

/**
 * Provides a block properties clarifier strategy.
 */
abstract class BlockPropertiesClarifierStrategy extends MarkupComponentClarifierStrategy {

    protected String blockPropertiesName;

    BlockPropertiesClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters,
                                     ClarifiableAttribute clarifiableAttribute, String blockPropertiesName) {

        super(creationalParameters, clarificationParameters, clarifiableAttribute);
        this.blockPropertiesName = blockPropertiesName;
    }

    /**
     * Clarifies block properties.
     *
     * @param markupComponentIterator A markup component iterator.
     */
    abstract void clarifyBlockProperties(ListIterator<MarkupComponent> markupComponentIterator);

    protected MarkupComponent getMarkupComponent(ListIterator<MarkupComponent> markupComponentIterator) {
        if (!markupComponentIterator.hasNext()) {
            // the block is the very last component
            if (!clarificationParameters.shouldBeBidirectional()) {
                return null;
            }
            addBlockProperties(markupComponentIterator);

            return null;
        }

        MarkupComponent markupComponent = markupComponentIterator.next();

        if (!(markupComponent instanceof BlockProperties)
                || !blockPropertiesName.equals(((BlockProperties) markupComponent).getName().getLocalPart())) {
            // block properties must be the very first after the start of a block
            if (!clarificationParameters.shouldBeBidirectional()) {
                return null;
            }
            markupComponentIterator.previous();
            addBlockProperties(markupComponentIterator);

            return null;
        }

        return markupComponent;
    }

    protected abstract void addBlockProperties(ListIterator<MarkupComponent> markupComponentIterator);

    /**
     * Provides an attribute clarifier strategy.
     */
    abstract static class AttributesClarifierStrategy extends BlockPropertiesClarifierStrategy {

        AttributesClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters,
                                    ClarifiableAttribute clarifiableAttribute, String blockPropertiesName) {
            super(creationalParameters, clarificationParameters, clarifiableAttribute, blockPropertiesName);
        }

        @Override
        void clarifyBlockProperties(ListIterator<MarkupComponent> markupComponentIterator) {
            MarkupComponent markupComponent = getMarkupComponent(markupComponentIterator);

            if (null == markupComponent) {
                return;
            }

            super.clarifyMarkupComponent(markupComponent);
        }

        /**
         * Provides a table properties clarifier strategy.
         */
        static class TablePropertiesClarifierStrategy extends AttributesClarifierStrategy {

            TablePropertiesClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters, ClarifiableAttribute clarifiableAttribute) {
                super(creationalParameters, clarificationParameters, clarifiableAttribute, LOCAL_TABLE_PROPERTIES);
            }

            @Override
            protected void addBlockProperties(ListIterator<MarkupComponent> markupComponentIterator) {
                markupComponentIterator.add(createTableProperties(creationalParameters, Collections.singletonList(createRequiredAttribute()), Collections.<BlockProperty>emptyList()));
            }
        }

        /**
         * Provides a text body properties clarifier strategy.
         */
        static class TextBodyPropertiesClarifierStrategy extends AttributesClarifierStrategy {

            TextBodyPropertiesClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters, ClarifiableAttribute clarifiableAttribute) {
                super(creationalParameters, clarificationParameters, clarifiableAttribute, LOCAL_TEXT_BODY_PROPERTIES);
            }

            @Override
            protected void addBlockProperties(ListIterator<MarkupComponent> markupComponentIterator) {
                markupComponentIterator.add(createTextBodyProperties(creationalParameters, Collections.singletonList(createRequiredAttribute())));
            }
        }
    }

    /**
     * Provides a properties clarifier strategy.
     */
    abstract static class PropertiesClarifierStrategy extends BlockPropertiesClarifierStrategy {

        private static final int DEFAULT_BLOCK_PROPERTIES_SIZE = 1;

        private static final String DEFAULT_CLARIFIABLE_ATTRIBUTE_PREFIX = "";
        private static final String DEFAULT_CLARIFIABLE_ATTRIBUTE_NAME = "";

        private static final ClarifiableAttribute DEFAULT_CLARIFIABLE_ATTRIBUTE = new ClarifiableAttribute(
                DEFAULT_CLARIFIABLE_ATTRIBUTE_PREFIX,
                DEFAULT_CLARIFIABLE_ATTRIBUTE_NAME,
                Collections.<String>emptyList());

        private String clarifiablePropertyName;

        PropertiesClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters,
                                    String blockPropertiesName, String clarifiablePropertyName) {
            super(creationalParameters, clarificationParameters, DEFAULT_CLARIFIABLE_ATTRIBUTE, blockPropertiesName);
            this.clarifiablePropertyName = clarifiablePropertyName;
        }

        @Override
        void clarifyBlockProperties(ListIterator<MarkupComponent> markupComponentIterator) {
            MarkupComponent markupComponent = getMarkupComponent(markupComponentIterator);

            if (null == markupComponent) {
                return;
            }

            ListIterator<BlockProperty> blockPropertyIterator = ((BlockProperties) markupComponent).getProperties().listIterator();

            while (blockPropertyIterator.hasNext()) {
                BlockProperty blockProperty = blockPropertyIterator.next();

                if (clarifiablePropertyName.equals(blockProperty.getName().getLocalPart())) {
                    if (!clarificationParameters.shouldBeBidirectional()) {
                        removeBlockProperty(markupComponentIterator, (BlockProperties) markupComponent, blockPropertyIterator);
                        return;
                    }
                    // it is already bidirectional
                    return;
                }
            }

            if (clarificationParameters.shouldBeBidirectional()) {
                addBlockProperty(blockPropertyIterator);
            }
        }

        List<BlockProperty> createRequiredBlockProperties() {
            List<BlockProperty> blockProperties = new ArrayList<>(DEFAULT_BLOCK_PROPERTIES_SIZE);
            blockProperties.add(createBlockProperty(creationalParameters, clarifiablePropertyName, Collections.<String, String>emptyMap()));

            return blockProperties;
        }

        private void addBlockProperty(ListIterator<BlockProperty> blockPropertyIterator) {
            blockPropertyIterator.add(createBlockProperty(creationalParameters, clarifiablePropertyName, Collections.<String, String>emptyMap()));
        }

        private void removeBlockProperty(ListIterator<MarkupComponent> markupComponentIterator, BlockProperties blockProperties, ListIterator<BlockProperty> blockPropertyIterator) {
            blockPropertyIterator.remove();

            if (blockProperties.getAttributes().isEmpty() && blockProperties.getProperties().isEmpty()) {
                markupComponentIterator.remove();
            }
        }

        /**
         * Provides a paragraph properties clarifier strategy.
         */
        static class ParagraphPropertiesClarifierStrategy extends BlockPropertiesClarifierStrategy.PropertiesClarifierStrategy {

            ParagraphPropertiesClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
                super(creationalParameters, clarificationParameters, LOCAL_PARAGRAPH_PROPERTIES, LOCAL_BIDIRECTIONAL);
            }

            @Override
            protected void addBlockProperties(ListIterator<MarkupComponent> markupComponentIterator) {
                markupComponentIterator.add(createParagraphProperties(creationalParameters, createRequiredBlockProperties()));
            }
        }

        /**
         * Provides a table properties clarifier strategy.
         */
        static class TablePropertiesClarifierStrategy extends BlockPropertiesClarifierStrategy.PropertiesClarifierStrategy {

            TablePropertiesClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
                super(creationalParameters, clarificationParameters, LOCAL_TABLE_PROPERTIES, LOCAL_BIDI_VISUAL);
            }

            @Override
            protected void addBlockProperties(ListIterator<MarkupComponent> markupComponentIterator) {
                markupComponentIterator.add(createTableProperties(creationalParameters, Collections.<Attribute>emptyList(), createRequiredBlockProperties()));
            }
        }
    }
}
