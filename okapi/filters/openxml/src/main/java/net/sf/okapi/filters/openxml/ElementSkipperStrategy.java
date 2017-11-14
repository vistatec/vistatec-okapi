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

import net.sf.okapi.common.exceptions.OkapiUnexpectedRevisionException;
import net.sf.okapi.filters.openxml.ElementSkipper.InlineSkippableElement;
import net.sf.okapi.filters.openxml.ElementSkipper.RevisionInlineSkippableElement;
import net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement;
import net.sf.okapi.filters.openxml.ElementSkipper.RunPropertySkippableElement;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static net.sf.okapi.filters.openxml.ElementSkipper.GeneralCrossStructureSkippableElement.BOOKMARK_END;
import static net.sf.okapi.filters.openxml.ElementSkipper.GeneralCrossStructureSkippableElement.BOOKMARK_START;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionInlineSkippableElement.RUN_INSERTED_CONTENT;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_RUN_PROPERTIES;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_TABLE_GRID;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_TABLE_PROPERTIES;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.WPML_ID;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.WPML_NAME;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.getAttributeValue;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isEndElement;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isStartElement;

/**
 * Provides an element skipper strategy.
 */
abstract class ElementSkipperStrategy {

    protected Set<String> skippableElementValues;

    protected ElementSkipperStrategy(Set<String> skippableElementValues) {
        this.skippableElementValues = skippableElementValues;
    }

    abstract boolean canSkip(StartElement startElement, StartElement parentStartElement);

    void skip(StartElementContext startElementContext) throws XMLStreamException {

        while (startElementContext.getEventReader().hasNext()) {
            XMLEvent e = startElementContext.getEventReader().nextEvent();

            if (isEndElement(e, startElementContext.getStartElement())) {
                return;
            }
        }

        throw new IllegalStateException(ExceptionMessages.UNEXPECTED_STRUCTURE);
    }

    /**
     * Provides a general element skipper strategy.
     */
    static class GeneralElementSkipperStrategy extends ElementSkipperStrategy {

        private static final String LOCAL_NUMBERING_PROPERTIES = "numPr";

        private static final Map<String, String> CONTEXT_AWARE_GENERAL_SKIPPABLE_ELEMENTS = new HashMap<>();
        private static final Map<String, String> CONTEXT_AWARE_REVISION_SKIPPABLE_ELEMENTS = new HashMap<>();

        private static final Set<String> REVISION_SKIPPABLE_ELEMENTS = new HashSet<>();

        static {
            CONTEXT_AWARE_GENERAL_SKIPPABLE_ELEMENTS.put(LOCAL_RUN_PROPERTIES, RunPropertySkippableElement.RUN_PROPERTY_CHARACTER_SPACING.getValue());

            CONTEXT_AWARE_REVISION_SKIPPABLE_ELEMENTS.put(LOCAL_NUMBERING_PROPERTIES, RevisionPropertySkippableElement.RUN_PROPERTY_INSERTED_PARAGRAPH_MARK.getValue());
            CONTEXT_AWARE_REVISION_SKIPPABLE_ELEMENTS.put(LOCAL_TABLE_PROPERTIES, RevisionPropertySkippableElement.TABLE_PROPERTIES_CHANGE.getValue());
            CONTEXT_AWARE_REVISION_SKIPPABLE_ELEMENTS.put(LOCAL_TABLE_GRID, RevisionPropertySkippableElement.TABLE_GRID_CHANGE.getValue());

            REVISION_SKIPPABLE_ELEMENTS.addAll(RevisionInlineSkippableElement.getValues());
            REVISION_SKIPPABLE_ELEMENTS.addAll(RevisionPropertySkippableElement.getValues());
        }

        private ConditionalParameters conditionalParameters;

        GeneralElementSkipperStrategy(Set<String> skippableElementValues, ConditionalParameters conditionalParameters) {
            super(skippableElementValues);
            this.conditionalParameters = conditionalParameters;
        }

        @Override
        boolean canSkip(StartElement startElement, StartElement parentStartElement) {

            if (!skippableElementValues.contains(startElement.getName().getLocalPart())) {
                return false;
            }

            if (!conditionalParameters.getAutomaticallyAcceptRevisions()
                    && isEntryPresentInContextAwareRevisionSkippableElements(startElement, parentStartElement)) {
                return false;
            }

            if (isValuePresentInContextAwareGeneralSkippableElements(startElement)
                    && !isEntryPresentInContextAwareGeneralSkippableElements(startElement, parentStartElement)) {
                return false;
            }

            return true;
        }

        private static boolean isValuePresentInContextAwareGeneralSkippableElements(StartElement startElement) {
            return CONTEXT_AWARE_GENERAL_SKIPPABLE_ELEMENTS.containsValue(startElement.getName().getLocalPart());
        }

        private static boolean isEntryPresentInContextAwareGeneralSkippableElements(StartElement startElement, StartElement parentStartElement) {
            return isEntryPresentInContextAwareSkippableElements(startElement, parentStartElement, CONTEXT_AWARE_GENERAL_SKIPPABLE_ELEMENTS);
        }

        private static boolean isEntryPresentInContextAwareRevisionSkippableElements(StartElement startElement, StartElement parentStartElement) {
            return isEntryPresentInContextAwareSkippableElements(startElement, parentStartElement, CONTEXT_AWARE_REVISION_SKIPPABLE_ELEMENTS);
        }

        private static boolean isEntryPresentInContextAwareSkippableElements(StartElement startElement, StartElement parentStartElement, Map<String, String> contextAwareSkippableElements) {
            if (null == parentStartElement) {
                return false;
            }

            for (Map.Entry<String, String> entry : contextAwareSkippableElements.entrySet()) {
                if (parentStartElement.getName().getLocalPart().equals(entry.getKey())
                        && startElement.getName().getLocalPart().equals(entry.getValue())) {
                    return true;
                }
            }

            return false;
        }

        @Override
        void skip(StartElementContext startElementContext) throws XMLStreamException {

            if (!startElementContext.getConditionalParameters().getAutomaticallyAcceptRevisions()
                    && REVISION_SKIPPABLE_ELEMENTS.contains(startElementContext.getStartElement().getName().getLocalPart())) {
                throw new OkapiUnexpectedRevisionException();
            }

            if (InlineSkippableElement.class == startElementContext.getSkippableElementType()
                    && RUN_INSERTED_CONTENT.getValue().equals(startElementContext.getStartElement().getName().getLocalPart())) {
                return;
            }

            super.skip(startElementContext);
        }
    }

    /**
     * Provides a bookmark element skipper strategy.
     */
    static class BookmarkElementSkipperStrategy extends ElementSkipperStrategy {

        private String bookmarkName;
        private String bookmarkId;

        BookmarkElementSkipperStrategy(Set<String> skippableElementValues, String bookmarkName) {
            super(skippableElementValues);
            this.bookmarkName = bookmarkName;
        }

        @Override
        boolean canSkip(StartElement startElement, StartElement parentStartElement) {

            return isBookmarkStartElement(startElement) && bookmarkName.equals(getAttributeValue(startElement, WPML_NAME))
                    || isBookmarkEndElement(startElement) && Objects.equals(bookmarkId, getAttributeValue(startElement, WPML_ID));
        }

        @Override
        void skip(StartElementContext startElementContext) throws XMLStreamException {
            super.skip(startElementContext);

            if (isBookmarkStartElement(startElementContext.getStartElement())) {
                bookmarkId = getAttributeValue(startElementContext.getStartElement(), WPML_ID);
                return;
            }

            bookmarkId = null;
        }

        private static boolean isBookmarkStartElement(XMLEvent event) {
            return isStartElement(event, BOOKMARK_START.getValue());
        }

        private static boolean isBookmarkEndElement(XMLEvent event) {
            return isStartElement(event, BOOKMARK_END.getValue());
        }
    }
}
