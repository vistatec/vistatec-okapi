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

import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static net.sf.okapi.filters.idml.ParsingIdioms.StyledStoryChildElement.CONTENT;
import static net.sf.okapi.filters.idml.ParsingIdioms.UNEXPECTED_STRUCTURE;
import static net.sf.okapi.filters.idml.StyleDefinitions.getDefaultStyleDefinitions;

class ReferenceableEventsMerger {

    private final static String UNEXPECTED_CODE = "Unexpected code";

    private static final QName CONTENT_SPACE_ATTRIBUTE_NAME = Namespaces.getXmlNamespace().getQName("space");
    private static final String CONTENT_SPACE_ATTRIBUTE_VALUE = "preserve";

    private final XMLEventFactory eventFactory;
    private final LocaleId targetLocale;
    private final StyleDefinitions defaultStyleDefinitions;

    private List<StoryChildElement> storyChildElements;
    private Map<Integer, Object> codeMap;

    private Deque<List<StoryChildElement>> savedStoryChildElements;

    private StoryChildElement savedBreak;

    private Deque<StyleDefinitions> currentStyleDefinitions;
    private StringBuilder contentTextBuilder;
    private List<XMLEvent> contentEvents;

    ReferenceableEventsMerger(XMLEventFactory eventFactory, LocaleId targetLocale) {
        this.eventFactory = eventFactory;
        this.targetLocale = targetLocale;

        defaultStyleDefinitions = getDefaultStyleDefinitions(eventFactory);
    }

    void merge(ReferenceableEvent referenceableEvent) {
        for (ReferenceableEvent referentEvent : referenceableEvent.getReferentEvents()) {
            new ReferenceableEventsMerger(eventFactory, targetLocale).merge(referentEvent);
        }

        if (referenceableEvent.getEvent().isStartGroup()) {
            mergeStartGroup(referenceableEvent);
            return;
        }

        if (referenceableEvent.getEvent().isDocumentPart()) {
            return;
        }

        mergeTextUnit(referenceableEvent);
    }

    private void mergeStartGroup(ReferenceableEvent referenceableEvent) {
        StyledTextReferenceSkeleton styledTextReferenceSkeleton = (StyledTextReferenceSkeleton) referenceableEvent.getEvent().getStartGroup().getSkeleton();
        StoryChildElement.StyledTextReferenceElement styledTextReferenceElement = styledTextReferenceSkeleton.getStyledTextReferenceElement();

        if (styledTextReferenceElement instanceof StoryChildElement.StyledTextReferenceElement.Table) {
            return;
        }

        storyChildElements = styledTextReferenceElement.getStoryChildElements();
        storyChildElements.clear();

        for (ReferenceableEvent referentEvent : referenceableEvent.getReferentEvents()) {

            if (referentEvent.getEvent().isStartGroup()) {
                StoryChildElement.StyledTextReferenceElement nestedStyledTextReferenceElement =
                        ((StyledTextReferenceSkeleton) referenceableEvent.getEvent().getStartGroup().getSkeleton()).getStyledTextReferenceElement();

                if (!(nestedStyledTextReferenceElement instanceof StoryChildElement.StyledTextReferenceElement.Table)) {
                    continue;
                }

                storyChildElements.add(nestedStyledTextReferenceElement);
            }

            if (referentEvent.getEvent().isDocumentPart()) {
                ISkeleton skeleton = referentEvent.getEvent().getDocumentPart().getSkeleton();

                if (skeleton instanceof MarkupSkeleton) {
                    addStoryChildElementsFromMarkupSkeleton((MarkupSkeleton) skeleton);

                } else if (skeleton instanceof StyledTextSkeleton) {
                    addStoryChildElementsFromStyledTextSkeleton((StyledTextSkeleton) skeleton);

                } else {
                    throw new IllegalStateException(UNEXPECTED_STRUCTURE);
                }
                continue;
            }

            addStoryChildElementsFromStyledTextSkeleton((StyledTextSkeleton) referentEvent.getEvent().getTextUnit().getSkeleton());
        }
    }

    private void addStoryChildElementsFromMarkupSkeleton(MarkupSkeleton markupSkeleton) {
        for (MarkupRange markupRange : markupSkeleton.getMarkup().getMarkupRanges()) {

            if (!(markupRange instanceof StoryChildElement)) {
                throw new IllegalStateException(UNEXPECTED_STRUCTURE);
            }

            this.storyChildElements.add((StoryChildElement) markupRange);
        }
    }

    private void addStoryChildElementsFromStyledTextSkeleton(StyledTextSkeleton styledTextSkeleton) {
        for (StoryChildElement storyChildElement : styledTextSkeleton.getStoryChildElements()) {
            this.storyChildElements.add(storyChildElement);
        }
    }

    private void mergeTextUnit(ReferenceableEvent referenceableEvent) {
        ITextUnit textUnit = referenceableEvent.getEvent().getTextUnit();

        StyledTextSkeleton styledTextSkeleton = (StyledTextSkeleton) textUnit.getSkeleton();
        storyChildElements = styledTextSkeleton.getStoryChildElements();
        codeMap = styledTextSkeleton.getCodeMap();

        savedStoryChildElements = new ArrayDeque<>();
        savedBreak = saveBreak(storyChildElements);
        storyChildElements.clear();

        currentStyleDefinitions = new ArrayDeque<>();
        contentTextBuilder = new StringBuilder();
        contentEvents = new ArrayList<>();

        mergeTextContainer(null == textUnit.getTarget(targetLocale)
                ? textUnit.getSource()
                : textUnit.getTarget(targetLocale));
    }

    private StoryChildElement saveBreak(List<StoryChildElement> textUnitElements) {
        for (StoryChildElement storyChildElement : textUnitElements) {

            if (storyChildElement instanceof StoryChildElement.StyledTextElement.Break) {
                return storyChildElement;
            }
        }

        return null;
    }

    private void mergeTextContainer(TextContainer textContainer) {
        for (Segment segment : textContainer.getSegments()) {
            mergeSegment(segment);
        }

        addContent();
        addBreak();
    }

    private void mergeSegment(Segment segment) {
        TextFragment textFragment = segment.getContent();

        String codedText = textFragment.getCodedText();
        List<Code> codes = textFragment.getCodes();

        for (int i = 0; i < codedText.length(); i++) {
            char c = codedText.charAt(i);

            if (!TextFragment.isMarker(c)) {
                addChar(c);
                continue;
            }

            int codeIndex = TextFragment.toIndex(codedText.charAt(++i));
            addCode(codes.get(codeIndex));
        }
    }

    private void addChar(char c) {
        contentTextBuilder.append(c);
    }

    private void addCode(Code code) {
        Object codeObject = codeMap.get(code.getId());

        switch (code.getTagType()) {

            case OPENING:
                addContent();

                if (codeObject instanceof StyleDefinitions) {
                    currentStyleDefinitions.push((StyleDefinitions) codeObject);

                } else if (codeObject instanceof StoryChildElement.StyledTextReferenceElement) {
                    currentStyleDefinitions.push(((StoryChildElement.StyledTextReferenceElement) codeObject).getStyleDefinitions());
                    storyChildElements.add((StoryChildElement.StyledTextReferenceElement) codeObject);

                    savedStoryChildElements.push(storyChildElements);
                    storyChildElements = ((StoryChildElement.StyledTextReferenceElement) codeObject).getStoryChildElements();
                    storyChildElements.clear();

                } else {
                    throw new IllegalStateException(UNEXPECTED_CODE + codeObject);
                }
                break;

            case PLACEHOLDER:
                if (codeObject instanceof SpecialCharacter) {
                    addSpecialCharacter((SpecialCharacter) codeObject);

                } else {
                    if (codeObject instanceof StoryChildElement) {
                        addStoryChildElement((StoryChildElement) codeObject);
                    } else {
                        throw new IllegalStateException(UNEXPECTED_CODE + codeObject);
                    }
                }
                break;

            case CLOSING:
                addContent();

                if (codeObject instanceof StyleDefinitions) {
                    currentStyleDefinitions.pop();

                } else if (codeObject instanceof StoryChildElement.StyledTextReferenceElement) {
                    currentStyleDefinitions.pop();
                    storyChildElements = savedStoryChildElements.pop();

                } else {
                    throw new IllegalStateException(UNEXPECTED_CODE + codeObject);
                }
                break;
        }
    }

    private void addSpecialCharacter(SpecialCharacter specialCharacter) {

        if (!(specialCharacter instanceof SpecialCharacter.Instruction)) {
            // trying to minify the number of Characters events
            contentTextBuilder.append(specialCharacter.getEvent().asCharacters().getData());

            return;
        }

        if (0 < contentTextBuilder.length()) {
            contentEvents.add(eventFactory.createCharacters(contentTextBuilder.toString()));
            contentTextBuilder = new StringBuilder();
        }

        contentEvents.addAll(specialCharacter.getEvents());
    }

    private void addStoryChildElement(StoryChildElement storyChildElement) {
        addContent();
        storyChildElements.add(storyChildElement);
    }

    private void addContent() {
        if (0 == contentEvents.size() && 0 == contentTextBuilder.length()) {
            return;
        }

        List<Attribute> contentAttributes = new ArrayList<>();
        contentAttributes.add(eventFactory.createAttribute(CONTENT_SPACE_ATTRIBUTE_NAME, CONTENT_SPACE_ATTRIBUTE_VALUE));

        StoryChildElement.StyledTextElement.Content.ContentBuilder contentBuilder = new StoryChildElement.StyledTextElement.Content.ContentBuilder();
        contentBuilder.setStartElement(eventFactory.createStartElement(CONTENT.getName(), contentAttributes.iterator(), null));

        if (0 < contentEvents.size()) {
            for (XMLEvent event : contentEvents) {
                contentBuilder.addInnerEvent(event);
            }
            contentEvents = new ArrayList<>();
        }

        if (0 < contentTextBuilder.length()) {
            contentBuilder.addInnerEvent(eventFactory.createCharacters(contentTextBuilder.toString()));
            contentTextBuilder = new StringBuilder();
        }

        contentBuilder.setEndElement(eventFactory.createEndElement(CONTENT.getName(), null));
        contentBuilder.setStyleDefinitions(getStyleDefinitions());

        storyChildElements.add(contentBuilder.build());
    }

    private void addBreak() {
        if (null == savedBreak) {
            return;
        }

        storyChildElements.add(savedBreak);
    }

    private StyleDefinitions getStyleDefinitions() {
        StyleDefinitions styleDefinitions = currentStyleDefinitions.peek();

        if (null == styleDefinitions) {
            return defaultStyleDefinitions;
        }

        return styleDefinitions;
    }
}
