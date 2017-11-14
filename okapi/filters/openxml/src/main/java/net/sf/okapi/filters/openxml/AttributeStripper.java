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
import javax.xml.stream.events.StartElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static net.sf.okapi.filters.openxml.Collections.newHashSet;

/**
 * Provides an attributes stripper.
 */
class AttributeStripper {

    /**
     * Strips start element attribute values.
     *
     * @param startElementContext       A start element context
     * @param strippableAttributeValues Strippable attributes
     *
     * @return A new start element with strippable attributes removed
     */
    protected static StartElement stripStartElementAttributeValues(StartElementContext startElementContext, Set<String> strippableAttributeValues) {
        List<Attribute> newAttributes = new ArrayList<>();

        Iterator currentAttributesIterator = startElementContext.getStartElement().getAttributes();

        while (currentAttributesIterator.hasNext()) {
            Attribute attribute = (Attribute) currentAttributesIterator.next();

            if (!strippableAttributeValues.contains(attribute.getName().getLocalPart())) {
                newAttributes.add(attribute);
            }
        }

        return startElementContext.getEventFactory().createStartElement(startElementContext.getStartElement().getName(),
                newAttributes.iterator(),
                startElementContext.getStartElement().getNamespaces());
    }

    /**
     * Provides a strippable attribute.
     */
    interface StrippableAttribute {
        String getValue();
    }

    /**
     * Provides a general attributes stripper.
     */
    static class GeneralAttributeStripper extends AttributeStripper {
        
        private static final Set<String> GENERAL_ATTRIBUTES = GeneralStrippableAttribute.getValues();

        static StartElement stripGeneralAttributes(StartElementContext startElementContext) {
            return stripStartElementAttributeValues(startElementContext, GENERAL_ATTRIBUTES);
        }

        /**
         * Provides a general strippable attribute enumeration.
         */
        private enum GeneralStrippableAttribute implements StrippableAttribute {

            SPELLING_ERROR("err"),
            NO_PROOFING("noProof"),
            DIRTY("dirty"),
            SMART_TAG_CLEAN("smtClean");

            private String value;

            GeneralStrippableAttribute(String value) {
                this.value = value;
            }

            @Override
            public String getValue() {
                return value;
            }

            static Set<String> getValues() {
                Set<String> values = new HashSet<>(values().length);

                for (GeneralStrippableAttribute generalStrippableAttribute : values()) {
                    values.add(generalStrippableAttribute.getValue());
                }

                return values;
            }
        }
    }

    /**
     * Provides a revision attributes stripper.
     */
    static class RevisionAttributeStripper extends AttributeStripper {

        private static final Set<String> PARAGRAPH_REVISION_ATTRIBUTES = newHashSet(
                RevisionStrippableAttribute.RPR.getValue(),
                RevisionStrippableAttribute.DEL.getValue(),
                RevisionStrippableAttribute.R.getValue(),
                RevisionStrippableAttribute.P.getValue(),
                RevisionStrippableAttribute.R_DEFAULT.getValue());

        private static final Set<String> RUN_REVISION_ATTRIBUTES = newHashSet(
                RevisionStrippableAttribute.RPR.getValue(),
                RevisionStrippableAttribute.DEL.getValue(),
                RevisionStrippableAttribute.R.getValue());

        private static final Set<String> TABLE_ROW_REVISION_ATTRIBUTES = newHashSet(
                RevisionStrippableAttribute.RPR.getValue(),
                RevisionStrippableAttribute.DEL.getValue(),
                RevisionStrippableAttribute.R.getValue(),
                RevisionStrippableAttribute.TR.getValue());

        private static final Set<String> SECTION_PROPERTIES_REVISION_ATTRIBUTES = newHashSet(
                RevisionStrippableAttribute.RPR.getValue(),
                RevisionStrippableAttribute.DEL.getValue(),
                RevisionStrippableAttribute.R.getValue(),
                RevisionStrippableAttribute.SECT.getValue());

        static StartElement stripParagraphRevisionAttributes(StartElementContext startElementContext) {
            return stripStartElementAttributeValues(startElementContext, PARAGRAPH_REVISION_ATTRIBUTES);
        }

        static StartElement stripRunRevisionAttributes(StartElementContext startElementContext) {
            return stripStartElementAttributeValues(startElementContext, RUN_REVISION_ATTRIBUTES);
        }

        static StartElement stripTableRowRevisionAttributes(StartElementContext startElementContext) {
            return stripStartElementAttributeValues(startElementContext, TABLE_ROW_REVISION_ATTRIBUTES);
        }

        static StartElement stripSectionPropertiesRevisionAttributes(StartElementContext startElementContext) {
            return stripStartElementAttributeValues(startElementContext, SECTION_PROPERTIES_REVISION_ATTRIBUTES);
        }

        /**
         * Provides a revision attribute enumeration.
         */
        private enum RevisionStrippableAttribute implements StrippableAttribute {

            RPR("rsidRPr"),
            DEL("rsidDel"),
            R("rsidR"),
            SECT("rsidSect"),
            P("rsidP"),
            R_DEFAULT("rsidRDefault"),
            TR("rsidTr");

            private String value;

            RevisionStrippableAttribute(String value) {
                this.value = value;
            }

            @Override
            public String getValue() {
                return value;
            }
        }
    }
}
