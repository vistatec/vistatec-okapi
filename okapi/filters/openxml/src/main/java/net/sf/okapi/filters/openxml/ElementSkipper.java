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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import java.util.HashSet;
import java.util.Set;

import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_PROPERTY_LANGUAGE;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isEndElement;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isStartElement;

/**
 * Provides an element skipper.
 */
class ElementSkipper {

    private ElementSkipperStrategy elementSkipperStrategy;

    protected ElementSkipper(ElementSkipperStrategy elementSkipperStrategy) {
        this.elementSkipperStrategy = elementSkipperStrategy;
    }

    boolean canSkip(StartElement startElement, StartElement parentStartElement) {
        return elementSkipperStrategy.canSkip(startElement, parentStartElement);
    }

    void skip(StartElementContext startElementContext) throws XMLStreamException {
        elementSkipperStrategy.skip(startElementContext);
    }

    /**
     * Provides a general element skipper.
     */
    static class GeneralElementSkipper extends ElementSkipper {

        GeneralElementSkipper(ElementSkipperStrategy elementSkipperStrategy) {
            super(elementSkipperStrategy);
        }

        static boolean isInsertedRunContentEndElement(XMLEvent event) {
            return isEndElement(event, RevisionInlineSkippableElement.RUN_INSERTED_CONTENT.getValue());
        }
    }

    /**
     * Provides a bookmark element skipper.
     */
    static class BookmarkElementSkipper extends ElementSkipper {

        BookmarkElementSkipper(ElementSkipperStrategy elementSkipperStrategy) {
            super(elementSkipperStrategy);
        }
    }

    /**
     * Provides a skippable element interface.
     */
    interface SkippableElement {
        String getValue();
    }

    /**
     * Provides an inline skippable element interface.
     */
    interface InlineSkippableElement extends SkippableElement {}

    /**
     * Provides a general inline skippable element enumeration.
     */
    enum GeneralInlineSkippableElement implements InlineSkippableElement {

        PROOFING_ERROR_ANCHOR("proofErr"),
        SOFT_HYPHEN("softHyphen"),
        ALTERNATE_CONTENT_FALLBACK("Fallback");

        private String value;

        GeneralInlineSkippableElement(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     *  Provides a phonetic run and phonetic property skippable element enumeration.
     */
    enum PhoneticInlineSkippableElement implements InlineSkippableElement {

        PHONETIC_RUN("rPh"),
        PHONETIC_PROPERTY("phoneticPr");

        private String value;

        PhoneticInlineSkippableElement(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * Provides a revision inline skippable element enumeration.
     */
    enum RevisionInlineSkippableElement implements InlineSkippableElement {

        RUN_INSERTED_CONTENT("ins"),
        RUN_DELETED_CONTENT("del");

        private String value;

        RevisionInlineSkippableElement(String value) {
            this.value = value;
        }

        static Set<String> getValues() {
            Set<String> values = new HashSet<>(values().length);

            for (RevisionInlineSkippableElement revisionInlineSkippableElement : values()) {
                values.add(revisionInlineSkippableElement.getValue());
            }

            return values;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * Provides a cross-structure skippable element interface.
     */
    interface CrossStructureSkippableElement extends SkippableElement {}

    /**
     * Provides a general cross-structure skippable element enumeration.
     */
    enum GeneralCrossStructureSkippableElement implements CrossStructureSkippableElement {

        BOOKMARK_START("bookmarkStart"),
        BOOKMARK_END("bookmarkEnd");

        private String value;

        GeneralCrossStructureSkippableElement(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * Provides a property skippable element interface.
     */
    interface PropertySkippableElement extends SkippableElement {}

    /**
     * Provides a run property skippable element enumeration.
     */
    enum RunPropertySkippableElement implements PropertySkippableElement {

        RUN_PROPERTY_COMPLEX_SCRIPT_BOLD("bCs"),
        RUN_PROPERTY_LANGUAGE(LOCAL_PROPERTY_LANGUAGE),
        RUN_PROPERTY_NO_SPELLING_OR_GRAMMAR("noProof"),
        RUN_PROPERTY_CHARACTER_SPACING("spacing"),
        RUN_PROPERTY_COMPLEX_SCRIPT_FONT_SIZE("szCs"),
        RUN_PROPERTY_CHARACTER_WIDTH("w"),
        RUN_PROPERTY_VERTICAL_ALIGNMENT("vertAlign");

        private String value;

        RunPropertySkippableElement(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * Provides a revision property skippable element enumeration.
     */
    enum RevisionPropertySkippableElement implements PropertySkippableElement {

        RUN_PROPERTY_INSERTED_PARAGRAPH_MARK("ins"),
        RUN_PROPERTY_DELETED_PARAGRAPH_MARK("del"),

        PARAGRAPH_PROPERTIES_CHANGE("pPrChange"),
        RUN_PROPERTIES_CHANGE("rPrChange"),
        SECTION_PROPERTIES_CHANGE("sectPrChange"),
        TABLE_GRID_CHANGE("tblGridChange"),
        TABLE_PROPERTIES_CHANGE("tblPrChange"),
        TABLE_PROPERTIES_EXCEPTIONS_CHANGE("tblPrExChange"),
        TABLE_CELL_PROPERTIES_CHANGE("tcPrChange"),
        TABLE_ROW_PROPERTIES_CHANGE("trPrChange");

        private String value;

        RevisionPropertySkippableElement(String value) {
            this.value = value;
        }

        static Set<String> getValues() {
            Set<String> values = new HashSet<>(values().length);

            for (RevisionPropertySkippableElement revisionPropertySkippableElement : values()) {
                values.add(revisionPropertySkippableElement.getValue());
            }

            return values;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
}
