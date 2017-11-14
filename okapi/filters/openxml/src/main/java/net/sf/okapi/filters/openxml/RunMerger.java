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

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.filters.openxml.RunFonts.ContentCategory;
import net.sf.okapi.filters.openxml.RunProperty.FontsRunProperty;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static java.util.Arrays.asList;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.addChunksToList;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isWhitespace;

class RunMerger {

    /**
     * All latin character ranges according to
     * <a href="https://blogs.msdn.microsoft.com/officeinteroperability/2013/04/22/office-open-xml-themes-schemes-and-fonts/">
     * Office Open XML Themes, Schemes and Fonts</a>.
     */
    private static final String LATIN_CHARACTER_SELECTOR = ".*["
            + "\u0000-\u007F\u0080-\u00A6\u00A9-\u00AF\u00B2-\u00B3\u00B5-\u00D6\u00D8-\u00F6"
            + "\u00F8-\u058F\u10A0-\u10FF\u1200-\u137F\u13A0-\u177F\u1D00-\u1D7F\u1E00-\u1FFF"
            + "\u2000-\u202B\u2030-\u2046\u204A-\u245F\u27C0-\u2BFF\uFB00-\uFB17\uFE50-\uFE6F"
            + "\uD835].*";

    /**
     * All complex script character ranges according to
     * <a href="https://blogs.msdn.microsoft.com/officeinteroperability/2013/04/22/office-open-xml-themes-schemes-and-fonts/">
     * Office Open XML Themes, Schemes and Fonts</a>.
     */
    private static final String COMPLEX_SCRIPT_CHARACTER_SELECTOR = ".*["
            + "\u0590-\u074F\u0780-\u07BF\u0900-\u109F\u1780-\u18AF"
            + "\u200F-\u200F\u202A-\u202F\u2670-\u2671\uFB1D-\uFB4F"
            + "].*";

    /**
     * Simple east asia character selector according to
     * <a href="https://blogs.msdn.microsoft.com/officeinteroperability/2013/04/22/office-open-xml-themes-schemes-and-fonts/">
     * Office Open XML Themes, Schemes and Fonts</a>.
     * <p>
     * Note that also any character not matching a certain selector may be considered as east asia,
     * too.
     */
    private static final String EAST_ASIA_CHARACTER_SELECTOR = ".*[\u3099-\u309A].*";

    /**
     * In dependence of the {@code w:lang} property, the characters selected by this selector may be
     * considered as east asian character. Further information can be found under
     * <a href="https://blogs.msdn.microsoft.com/officeinteroperability/2013/04/22/office-open-xml-themes-schemes-and-fonts/">
     * Office Open XML Themes, Schemes and Fonts</a>.
     */
    private static final String QUOTATION_CHARACTERS_EAST_ASIA = ".*[\u2010-\u2029].*";

    /**
     * Contains the {@link LocaleId locales} of the east asian languages that influence the
     * effective content categories for
     * {@link #QUOTATION_CHARACTERS_EAST_ASIA quotation characters}.
     */
    private static final List<LocaleId> EAST_ASIAN_REGIONS_BY_LANGUAGES = asList(
            LocaleId.fromBCP47("ii-CN"),
            LocaleId.fromBCP47("ja-JP"),
            LocaleId.fromBCP47("ko-KR"),
            LocaleId.fromBCP47("zh-CN"),
            LocaleId.fromBCP47("zh-HK"),
            LocaleId.fromBCP47("zh-MO"),
            LocaleId.fromBCP47("zh-SG"),
            LocaleId.fromBCP47("zh-TW")
    );

    private String paragraphStyle;
    private RunBuilder previousRunBuilder;
    private List<Block.BlockChunk> completedRuns = new ArrayList<>();

    void setParagraphStyle(String paragraphStyle) {
        this.paragraphStyle = paragraphStyle;
    }

    boolean isEmpty() {
        return previousRunBuilder == null;
    }

    List<Block.BlockChunk> getRuns() throws XMLStreamException {
        if (previousRunBuilder != null) {
            completedRuns.add(previousRunBuilder.build());
            previousRunBuilder = null;
        }

        return completedRuns;
    }

    void add(RunBuilder runBuilder) throws XMLStreamException {
        if (null == previousRunBuilder) {
            runBuilder.resetCombinedRunProperties(paragraphStyle); // to be consistent with other run builders
            previousRunBuilder = runBuilder;

            return;
        }

        if (canMerge(previousRunBuilder, runBuilder)) {
            merge(previousRunBuilder, runBuilder);
        } else {
            completedRuns.add(previousRunBuilder.build());
            previousRunBuilder = runBuilder;
        }
    }

    private boolean canMerge(RunBuilder runBuilder, RunBuilder otherRunBuilder) {
        RunProperties combinedRunProperties = runBuilder.getCombinedRunProperties(paragraphStyle);
        RunProperties combinedOtherRunProperties = otherRunBuilder.getCombinedRunProperties(paragraphStyle);

        if (runBuilder.isHidden() || otherRunBuilder.isHidden()){
            return false;
        }

        // Merging runs in the math namespace can sometimes corrupt formulas,
        // so don't do it.
        if (Namespaces.Math.containsName(runBuilder.getStartElementContext().getStartElement().getName())) {
            return false;
        }
        // XXX Don't merge runs that have nested blocks, to avoid having to go
        // back and renumber the references in the skeleton. I should probably
        // fix this at some point.  Note that we check for the existence of -any-
        // nested block, not just ones with text.
        // The reason for this pre-caution is because when we merge runs, we
        // re-parse the xml eventReader.  However, it doesn't cover all the cases.
        // We might be able to remove this restriction if we could clean up the
        // way the run body eventReader are parsed during merging.
        if (runBuilder.containsNestedItems() || otherRunBuilder.containsNestedItems()) {
            return false;
        }
        // Don't merge stuff involving complex codes
        if (runBuilder.hasComplexCodes() || otherRunBuilder.hasComplexCodes()) {
            return false;
        }

        detectRunFontsContentCategories(runBuilder, combinedRunProperties);
        detectRunFontsContentCategories(otherRunBuilder, combinedOtherRunProperties);

        return canRunPropertiesBeMerged(combinedRunProperties, combinedOtherRunProperties);
    }

    /**
     * Analyzes run texts {@code runText} and detects run fonts {@link ContentCategory}s.
     *
     * @param runBuilder the run builder
     * @param combinedRunProperties the combined run properties
     */
    private void detectRunFontsContentCategories(RunBuilder runBuilder, RunProperties combinedRunProperties) {
        LocaleId sourceLanguage = runBuilder.getStartElementContext().getSourceLanguage();
        String runText = runBuilder.getRunText();

        if (runText == null) {
            return;
        }

        for (RunProperty runProperty : combinedRunProperties.getProperties()) {

            if (runProperty instanceof FontsRunProperty) {
                FontsRunProperty fontsRunProperty = (FontsRunProperty) runProperty;
                RunFonts runFonts = fontsRunProperty.getRunFonts();

                List<ContentCategory> contentCategories = detectAndGetRunFontsContentCategories(sourceLanguage, runText, runFonts);
                runFonts.setDetectedContentCategories(contentCategories);
            }
        }
    }

    private List<ContentCategory> detectAndGetRunFontsContentCategories(LocaleId sourceLanguage, String runText,
                                                                        RunFonts runFonts) {
        List<ContentCategory> detectedContentCategories = new ArrayList<>();

        if (runText.matches(".*\\p{ASCII}.*")) {
            detectedContentCategories.add(runFonts.getContentCategory(ContentCategory.ASCII_THEME, ContentCategory.ASCII));
        }

        if (runText.matches(LATIN_CHARACTER_SELECTOR)
                && runText.matches(".*[^\\p{ASCII}].*")) {
            detectedContentCategories.add(runFonts.getContentCategory(ContentCategory.HIGH_ANSI_THEME, ContentCategory.HIGH_ANSI));
        }

        if (runText.matches(EAST_ASIA_CHARACTER_SELECTOR)
                || containsQuotationsInEastAsianLanguage(runText, sourceLanguage)
                || containsOtherCharacters(runText)) {
            detectedContentCategories.add(runFonts.getContentCategory(ContentCategory.EAST_ASIAN_THEME, ContentCategory.EAST_ASIAN));
        }

        if (runText.matches(COMPLEX_SCRIPT_CHARACTER_SELECTOR)) {
            detectedContentCategories.add(runFonts.getContentCategory(ContentCategory.COMPLEX_SCRIPT_THEME, ContentCategory.COMPLEX_SCRIPT));
        }

        return detectedContentCategories;
    }

    private boolean containsQuotationsInEastAsianLanguage(String runText, LocaleId sourceLanguage) {
        return EAST_ASIAN_REGIONS_BY_LANGUAGES.contains(sourceLanguage)
                && runText.matches(QUOTATION_CHARACTERS_EAST_ASIA);
    }

    private boolean containsOtherCharacters(String runText) {
        return !runText.matches(LATIN_CHARACTER_SELECTOR)
                && !runText.matches(COMPLEX_SCRIPT_CHARACTER_SELECTOR)
                && !runText.matches(QUOTATION_CHARACTERS_EAST_ASIA)
                && !runText.matches(EAST_ASIA_CHARACTER_SELECTOR);
    }

    private boolean canRunPropertiesBeMerged(RunProperties currentProperties, RunProperties otherProperties) {
        if (currentProperties.count() != otherProperties.count()) {
            return false;
        }

        int numberOfMatchedProperties = 0;

        for (RunProperty currentProperty : currentProperties.getProperties()) {
            QName currentPropertyStartElementName = currentProperty.getName();

            for (RunProperty otherProperty : otherProperties.getProperties()) {
                QName otherPropertyStartElementName = otherProperty.getName();

                if (!currentPropertyStartElementName.equals(otherPropertyStartElementName)) {
                    continue;
                }

                if (currentProperty instanceof MergeableRunProperty && otherProperty instanceof MergeableRunProperty) {
                    if (!((MergeableRunProperty) currentProperty).canBeMerged((MergeableRunProperty) otherProperty)) {
                        return false;
                    }
                } else {
                    if (!currentProperty.canBeReplaced(otherProperty)) {
                        return false;
                    }
                }
                numberOfMatchedProperties++;
                break;
            }
        }

        if (numberOfMatchedProperties < currentProperties.count()) {
            return false;
        }

        return true;
    }

    /**
     * Merge the property body.  This is something of a mess, since
     * this may cause tetris-style collapsing of consecutive text across
     * the two runs. To handle this correctly, we concatenate the two run
     * bodies, then re-process them.
     */
    private void merge(RunBuilder runBuilder, RunBuilder otherRunBuilder) throws XMLStreamException {

        runBuilder.setRunProperties(mergeRunProperties(runBuilder.getRunProperties(), otherRunBuilder.getRunProperties(),
                runBuilder.getCombinedRunProperties(paragraphStyle), otherRunBuilder.getCombinedRunProperties(paragraphStyle)));

        runBuilder.resetCombinedRunProperties(paragraphStyle);

        List<XMLEvent> newBodyEvents = new ArrayList<>();
        addChunksToList(newBodyEvents, runBuilder.getRunBodyChunks());
        runBuilder.setTextPreservingWhitespace(runBuilder.isTextPreservingWhitespace() || otherRunBuilder.isTextPreservingWhitespace());
        addChunksToList(newBodyEvents, otherRunBuilder.getRunBodyChunks());
        runBuilder.getRunBodyChunks().clear();
        XMLEventReader events = new XMLListEventReader(newBodyEvents);
        while (events.hasNext()) {
            runBuilder.addRunBody(events.nextEvent(), events);
        }
        // Flush any outstanding buffers
        runBuilder.flushText();
        runBuilder.flushMarkupChunk();
    }

    private RunProperties mergeRunProperties(RunProperties runProperties, RunProperties otherRunProperties,
                                             RunProperties combinedRunProperties, RunProperties otherCombinedRunProperties) {
        // try to reduce the set of properties
        List<RunProperty> mergeableRunProperties = runProperties.getMergeableRunProperties();
        List<RunProperty> otherMergeableRunProperties = otherRunProperties.getMergeableRunProperties();

        if (mergeableRunProperties.isEmpty() && otherMergeableRunProperties.isEmpty()) {
            return runProperties.count() <= otherRunProperties.count()
                    ? runProperties
                    : otherRunProperties;
        }

        if (mergeableRunProperties.size() >= otherMergeableRunProperties.size()) {
            List<RunProperty> remainedOtherMergeableRunProperties = mergeMergeableRunProperties(mergeableRunProperties, otherMergeableRunProperties);
            runProperties.getProperties().addAll(remainedOtherMergeableRunProperties);

            clarifyFontsRunProperties(runProperties, mergeCombinedRunProperties(combinedRunProperties, otherCombinedRunProperties));

            return runProperties;
        }

        List<RunProperty> remainedMergeableRunProperties = mergeMergeableRunProperties(otherMergeableRunProperties, mergeableRunProperties);
        otherRunProperties.getProperties().addAll(remainedMergeableRunProperties);

        clarifyFontsRunProperties(otherRunProperties, mergeCombinedRunProperties(combinedRunProperties, otherCombinedRunProperties));

        return otherRunProperties;
    }

    private RunProperties mergeCombinedRunProperties(RunProperties combinedRunProperties, RunProperties otherCombinedRunProperties) {
        List<RunProperty> mergeableCombinedRunProperties = combinedRunProperties.getMergeableRunProperties();
        List<RunProperty> otherMergeableCombinedRunProperties = otherCombinedRunProperties.getMergeableRunProperties();

        if (mergeableCombinedRunProperties.size() >= otherMergeableCombinedRunProperties.size()) {
            List<RunProperty> remainedOtherMergeableCombinedRunProperties = mergeMergeableRunProperties(mergeableCombinedRunProperties, otherMergeableCombinedRunProperties);
            combinedRunProperties.getProperties().addAll(remainedOtherMergeableCombinedRunProperties);

            return combinedRunProperties;
        }

        List<RunProperty> remainedMergeableCombinedRunProperties = mergeMergeableRunProperties(otherMergeableCombinedRunProperties, mergeableCombinedRunProperties);
        otherCombinedRunProperties.getProperties().addAll(remainedMergeableCombinedRunProperties);

        return otherCombinedRunProperties;
    }

    private List<RunProperty> mergeMergeableRunProperties(List<RunProperty> mergeableRunProperties, List<RunProperty> otherMergeableRunProperties) {
        List<RunProperty> remainedOtherMergeableRunProperties = new ArrayList<>(otherMergeableRunProperties);

        for (RunProperty runProperty : mergeableRunProperties) {
            QName currentPropertyStartElementName = runProperty.getName();

            Iterator<RunProperty> remainedOtherMergeableRunPropertyIterator = remainedOtherMergeableRunProperties.iterator();

            while (remainedOtherMergeableRunPropertyIterator.hasNext()) {
                RunProperty otherRunProperty = remainedOtherMergeableRunPropertyIterator.next();
                QName otherPropertyStartElementName = otherRunProperty.getName();

                if (!currentPropertyStartElementName.equals(otherPropertyStartElementName)) {
                    continue;
                }

                ((MergeableRunProperty) runProperty).merge((MergeableRunProperty) otherRunProperty);
                remainedOtherMergeableRunPropertyIterator.remove();
                break;
            }
        }

        return remainedOtherMergeableRunProperties;
    }

    private void clarifyFontsRunProperties(RunProperties runProperties, RunProperties combinedRunProperties) {
        for (RunProperty combinedRunProperty : combinedRunProperties.getProperties()) {

            if (combinedRunProperty instanceof FontsRunProperty) {
                ListIterator<RunProperty> runPropertyIterator = runProperties.getProperties().listIterator();

                while (runPropertyIterator.hasNext()) {
                    RunProperty runProperty = runPropertyIterator.next();

                    if (runProperty instanceof FontsRunProperty) {
                        runPropertyIterator.set(combinedRunProperty);

                        return;
                    }
                }

                runPropertyIterator.add(combinedRunProperty);

                return;
            }
        }
    }

    void reset() {
        completedRuns.clear();
        previousRunBuilder = null;
    }

    public void append(char c) throws XMLStreamException{
        List<Chunk> bodyChunks = previousRunBuilder.getRunBodyChunks();
        for (Chunk chunk : bodyChunks) {
            if (chunk instanceof StyledText) {
                StyledText styledText = (StyledText) chunk;
                String text = styledText.getText();
                styledText.setText(previousRunBuilder.getStartElementContext().getEventFactory().createCharacters(text+ String.valueOf(c)));
            }
        }
    }

    /**
     * Wrap an event iterator as a true reader; this lets us replay strings
     * of eventReader during merge.
     */
    static class XMLListEventReader implements XMLEventReader {
        private Iterator<XMLEvent> events;

        XMLListEventReader(Iterable<XMLEvent> events) {
            this.events = events.iterator();
        }

        @Override
        public boolean hasNext() {
            return events.hasNext();
        }

        @Override
        public XMLEvent nextEvent() {
            return events.next();
        }

        @Override
        public void remove() {
            events.remove();
        }

        @Override
        public XMLEvent nextTag() throws XMLStreamException {
            for (XMLEvent e = nextEvent(); e != null; e = nextEvent()) {
                if (e.isStartElement() || e.isEndElement()) {
                    return e;
                } else if (!isWhitespace(e)) {
                    throw new IllegalStateException("Unexpected event: " + e);
                }
            }
            return null;
        }

        @Override
        public XMLEvent peek() throws XMLStreamException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getElementText() throws XMLStreamException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getProperty(String name) throws IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws XMLStreamException {
        }

        @Override
        public Object next() {
            return events.next();
        }
    }
}
