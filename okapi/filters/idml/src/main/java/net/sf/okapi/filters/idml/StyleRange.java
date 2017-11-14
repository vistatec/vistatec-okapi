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

package net.sf.okapi.filters.idml;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static net.sf.okapi.filters.idml.Properties.getEmptyProperties;

class StyleRange {

    static final String APPLIED_PARAGRAPH_STYLE = "AppliedParagraphStyle";
    static final String APPLIED_PARAGRAPH_STYLE_DEFAULT_VALUE = "ParagraphStyle/$ID/NormalParagraphStyle";

    static final String APPLIED_CHARACTER_STYLE = "AppliedCharacterStyle";
    static final String APPLIED_CHARACTER_STYLE_DEFAULT_VALUE = "CharacterStyle/$ID/[No character style]";

    private final List<Attribute> attributes;
    private final Properties properties;

    StyleRange(List<Attribute> attributes, Properties properties) {
        this.attributes = attributes;
        this.properties = properties;
    }

    static StyleRange getDefaultParagraphStyleRange(XMLEventFactory eventFactory) {
        List<Attribute> attributes = singletonList(eventFactory.createAttribute(APPLIED_PARAGRAPH_STYLE, APPLIED_PARAGRAPH_STYLE_DEFAULT_VALUE));
        Properties properties = getEmptyProperties();

        return new StyleRange(attributes, properties);
    }

    static StyleRange getDefaultCharacterStyleRange(XMLEventFactory eventFactory) {
        List<Attribute> attributes = singletonList(eventFactory.createAttribute(APPLIED_CHARACTER_STYLE, APPLIED_CHARACTER_STYLE_DEFAULT_VALUE));
        Properties properties = getEmptyProperties();

        return new StyleRange(attributes, properties);
    }

    List<Attribute> getAttributes() {
        return attributes;
    }

    Properties getProperties() {
        return properties;
    }

    boolean isSubsetOf(StyleRange other) {
        if (getAttributes().isEmpty() && !other.getAttributes().isEmpty()
                || getProperties().isEmpty() && !other.getProperties().isEmpty()) {
            return false;
        }

        outerAttributes:	for (Attribute attribute : getAttributes()) {
            for (Attribute otherAttribute : other.getAttributes()) {
                if (otherAttribute.equals(attribute)) {
                    continue outerAttributes;
                }
            }
            return false;
        }

        outerProperties:	for (Property property : getProperties().getProperties()) {
            for (Property otherProperty : other.getProperties().getProperties()) {
                if (otherProperty.equals(property)) {
                    continue outerProperties;
                }
            }
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        StyleRange that = (StyleRange) o;

        return areAttributesEqual(getAttributes(), that.getAttributes())
                && Objects.equals(getProperties(), that.getProperties());
    }

    /**
     * Checks whether provided attributes are equal.
     *
     * Replaces the conservative org.codehaus.stax2.ri.evt.AttributeEventImpl#equals(java.lang.Object) implementation,
     * which also checks for the internal mWasSpecified flag.
     *
     * @param thisAttributes This attributes
     * @param thatAttributes That attributes
     *
     * @return {@code true}  If attributes are equal
     *         {@code false} Otherwise
     */
    private boolean areAttributesEqual(List<Attribute> thisAttributes, List<Attribute> thatAttributes) {
        if (thisAttributes == thatAttributes) return true;
        if (null == thisAttributes || null == thatAttributes) return false;

        if (thisAttributes.size() != thatAttributes.size()) return false;

        Iterator<Attribute> thisAttributesIterator = thisAttributes.iterator();
        Iterator<Attribute> thatAttributesIterator = thatAttributes.iterator();

        while (thisAttributesIterator.hasNext() && thatAttributesIterator.hasNext()) {
            Attribute thisAttribute = thisAttributesIterator.next();
            Attribute thatAttribute = thatAttributesIterator.next();

            if (!thisAttribute.getName().equals(thatAttribute.getName())
                    || !thisAttribute.getValue().equals(thatAttribute.getValue())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAttributes(), getProperties());
    }

    static class StyleRangeBuilder implements Builder<StyleRange> {

        private List<Attribute> attributes;
        private Properties properties;

        StyleRangeBuilder setAttributes(List<Attribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        StyleRangeBuilder setProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        @Override
        public StyleRange build() {
            return new StyleRange(attributes, properties);
        }
    }
}
