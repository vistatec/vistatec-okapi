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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.sf.okapi.filters.openxml.ElementSkipperFactory.createGeneralElementSkipper;
import static net.sf.okapi.filters.openxml.Namespaces.WordProcessingML;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.getAttributeValue;

/**
 * Representation of the data in an <w:rFonts> tag, which can be
 * merged with the font information from other runs in some cases.
 */
public class RunFonts {
    static final QName RUN_FONTS = Namespaces.WordProcessingML.getQName("rFonts");

    /**
     * A maximum number of events per rFonts tag.
     */
    private static final int MAX_NUMBER_OF_EVENTS = 2;

    /**
     * Within a single run, there can be up to four types of content present
     * which shall each be allowed to use a unique font.
     */
    private static final int MAX_NUMBER_OF_CONTENT_CATEGORIES = 4;

    private static final EnumSet<ContentCategory> FONT_CONTENT_CATEGORIES = EnumSet.of(
            ContentCategory.ASCII,
            ContentCategory.HIGH_ANSI,
            ContentCategory.COMPLEX_SCRIPT,
            ContentCategory.EAST_ASIAN,
            ContentCategory.HINT);

    private static final EnumMap<ContentCategory, ContentCategory> fontThemeContentCategories = new EnumMap<ContentCategory, ContentCategory>(ContentCategory.class) {{
        put(ContentCategory.ASCII, ContentCategory.ASCII_THEME);
        put(ContentCategory.HIGH_ANSI, ContentCategory.HIGH_ANSI_THEME);
        put(ContentCategory.COMPLEX_SCRIPT, ContentCategory.COMPLEX_SCRIPT_THEME);
        put(ContentCategory.EAST_ASIAN, ContentCategory.EAST_ASIAN_THEME);
    }};

    private XMLEventFactory eventFactory;
    private StartElement startElement;
    private EnumMap<ContentCategory, String> fonts;

    /**
     * The content categories that were detected during parse.
     *
     * E.g. if the text contains characters from the ASCII range this {@link Set} will have {@link ContentCategory#ASCII} or
     * {@link ContentCategory#ASCII_THEME}.
     */
    private Set<ContentCategory> detectedContentCategories = EnumSet.noneOf(ContentCategory.class);

    private RunFonts(XMLEventFactory eventFactory, StartElement startElement,
            EnumMap<ContentCategory, String> fonts) {
        this.eventFactory = eventFactory;
        this.startElement = startElement;
        this.fonts = fonts;
    }

    static RunFonts createRunFonts(StartElementContext startElementContext) throws XMLStreamException {
        EnumMap<ContentCategory, String> fonts = new EnumMap<>(ContentCategory.class);

        for (ContentCategory contentCategory : ContentCategory.values()) {
            fonts.put(contentCategory, getAttributeValue(startElementContext.getStartElement(), contentCategory.getValue()));
        }

        ElementSkipper elementSkipper = createGeneralElementSkipper(startElementContext.getConditionalParameters());
        elementSkipper.skip(startElementContext);

        return new RunFonts(startElementContext.getEventFactory(), startElementContext.getStartElement(), fonts);
    }


    private EnumMap<ContentCategory, String> getFonts() {
        return fonts;
    }

    private boolean isDetectedContentCategory(ContentCategory category) {
        return detectedContentCategories.contains(category);
    }

    void setDetectedContentCategories(List<ContentCategory> categories) {
        detectedContentCategories.clear();
        detectedContentCategories.addAll(categories);
    }

    ContentCategory getContentCategory(ContentCategory contentCategory, ContentCategory defaultContentCategory) {
        return fonts.get(contentCategory) == null ? defaultContentCategory : contentCategory;
    }

    /**
     * Fonts can be merged if they contain no contradictory font information. This means
     * all content categories either have the same value, or else be specified for at most one
     * of the two font objects.
     *
     * @param runFonts The run fonts to check against
     *
     * @return {@code true} if current run fonts can be merged with another
     */
    boolean canBeMerged(RunFonts runFonts) {
        for (ContentCategory fontContentCategory : FONT_CONTENT_CATEGORIES) {

            if (!canContentCategoriesBeMerged(fontContentCategory, runFonts)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns {@code false} if the given {@code category} is effectively used by this and the other
     * {@link RunFonts} and they have different values. If one of the {@link RunFonts} does
     * not need the category because it does not contain text that is within the category's
     * character range, the value can be discarded and the categories can be merged.
     *
     * @return {@code false} if the given {@code category} is effectively used by this and the other
     *      {@link RunFonts} and they have different values
     */
    @SuppressWarnings("RedundantIfStatement")
    private boolean canContentCategoriesBeMerged(ContentCategory fontCategory, RunFonts runFonts) {

        if (ContentCategory.HINT == fontCategory) {
            return null == fonts.get(fontCategory) && null != runFonts.getFonts().get(fontCategory)
                    || null != fonts.get(fontCategory) && null == runFonts.getFonts().get(fontCategory)
                    || Objects.equals(fonts.get(fontCategory), runFonts.getFonts().get(fontCategory));
        }

        ContentCategory fontThemeCategory = fontThemeContentCategories.get(fontCategory);

        if (isDetectedContentCategory(fontThemeCategory) && runFonts.isDetectedContentCategory(fontCategory)) {
            return Objects.equals(fonts.get(fontThemeCategory), runFonts.fonts.get(fontCategory));
        }

        if (isDetectedContentCategory(fontCategory) && runFonts.isDetectedContentCategory(fontThemeCategory)) {
            return Objects.equals(fonts.get(fontCategory), runFonts.fonts.get(fontThemeCategory));
        }

        if (isDetectedContentCategory(fontCategory) && runFonts.isDetectedContentCategory(fontCategory)) {
            return Objects.equals(fonts.get(fontCategory), runFonts.fonts.get(fontCategory));
        }

        if (isDetectedContentCategory(fontThemeCategory) && runFonts.isDetectedContentCategory(fontThemeCategory)) {
            return Objects.equals(fonts.get(fontThemeCategory), runFonts.fonts.get(fontThemeCategory));
        }

        return true;
    }

    /**
     * Merges another run fonts object into this one. Returns the merged instance,
     * which may not be the same as this.
     *
     * @param runFonts The run fonts to merge with
     *
     * @return Merged current run fonts
     */
    RunFonts merge(RunFonts runFonts) {
        EnumMap<ContentCategory, String> newFonts = new EnumMap<>(ContentCategory.class);

        for (ContentCategory category : ContentCategory.values()) {
            newFonts.put(category, mergeContentCategories(category, runFonts));
        }

        fonts = newFonts;

        return this;
    }

    /**
     * Returns the merged category value. Considers the effective categories of both
     * {@link RunFonts}.
     *
     * @param contentCategory the content category
     * @param runFonts the run fonts to merge with
     * @return the merged category value
     *         or {@code null} if the content category does not belong to any of effective content categories
     */
    private String mergeContentCategories(ContentCategory contentCategory, RunFonts runFonts) {

        if (ContentCategory.HINT == contentCategory) {
            return null == fonts.get(ContentCategory.HINT)
                    ? runFonts.getFonts().get(ContentCategory.HINT)
                    : fonts.get(ContentCategory.HINT);
        }

        if (isDetectedContentCategory(contentCategory)) {
            return fonts.get(contentCategory);
        }

        if (runFonts.isDetectedContentCategory(contentCategory)) {
            return runFonts.fonts.get(contentCategory);
        }

        if (Objects.equals(fonts.get(contentCategory), runFonts.getFonts().get(contentCategory))) {
            return fonts.get(contentCategory);
        }

        return null;
    }

    List<XMLEvent> getEvents() {
        List<XMLEvent> events = new ArrayList<>(MAX_NUMBER_OF_EVENTS);

        events.add(eventFactory.createStartElement(startElement.getName(), getAttributes(), startElement.getNamespaces()));
        events.add(eventFactory.createEndElement(startElement.getName(), startElement.getNamespaces()));

        return events;
    }

    private Iterator<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<>(MAX_NUMBER_OF_CONTENT_CATEGORIES);

        for (ContentCategory category : ContentCategory.values()) {
            String value = fonts.get(category);

            if (value != null) {
                attributes.add(eventFactory.createAttribute("w", Namespaces.WordProcessingML.getURI(), category.getValue().getLocalPart(), value));
            }
        }

        return attributes.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RunFonts runFonts = (RunFonts) o;
        return Objects.equals(fonts, runFonts.fonts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fonts);
    }

    enum ContentCategory {
        ASCII("ascii"),
        ASCII_THEME("asciiTheme"),
        HIGH_ANSI("hAnsi"),
        HIGH_ANSI_THEME("hAnsiTheme"),
        COMPLEX_SCRIPT("cs"),
        COMPLEX_SCRIPT_THEME("cstheme"),
        EAST_ASIAN("eastAsia"),
        EAST_ASIAN_THEME("eastAsiaTheme"),
        HINT("hint");

        private QName value;

        ContentCategory(String value) {
            this.value = WordProcessingML.getQName(value);
        }

        public QName getValue() {
            return value;
        }
    }
}
