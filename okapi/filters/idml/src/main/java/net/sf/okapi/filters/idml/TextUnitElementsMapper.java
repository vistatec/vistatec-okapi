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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static net.sf.okapi.filters.idml.CodeTypes.createCodeType;
import static net.sf.okapi.filters.idml.ParsingIdioms.UNEXPECTED_STRUCTURE;
import static net.sf.okapi.filters.idml.StyleDefinitions.getDefaultStyleDefinitions;

class TextUnitElementsMapper {

    private static final String REFERENT_GROUP = "rg";

    private static final String CONTENT = "content";
    private static final String DASH = "-";
    private static final String TAG_OPENING = "<";
    private static final String TAG_CLOSING = ">";
    private static final String TAG_CLOSING_SINGLE = "/>";
    private static final String CONTENT_TAG_OPENING = TAG_OPENING + CONTENT;
    private static final String CONTENT_TAG_CLOSING = TAG_OPENING + "/" + CONTENT;

    private final Parameters parameters;
    private final XMLEventFactory eventFactory;
    private final IdGenerator documentPartIdGenerator;
    private final String textUnitId;
    private final StyleDefinitions defaultStyleDefinitions;

    private IdGenerator referentIdGenerator;

    private List<Event> referentEvents;
    private Map<Integer, Object> codeMap;
    private Deque<ContentCode> contentCodes;

    private int nextCodeId;

    TextUnitElementsMapper(Parameters parameters, XMLEventFactory eventFactory, IdGenerator documentPartIdGenerator,
                           IdGenerator textUnitIdGenerator) {
        this.parameters = parameters;
        this.eventFactory = eventFactory;
        this.documentPartIdGenerator = documentPartIdGenerator;
        textUnitId = textUnitIdGenerator.createId();
        defaultStyleDefinitions = getDefaultStyleDefinitions(eventFactory);

        referentEvents = new ArrayList<>();
        codeMap = new HashMap<>();
        contentCodes = new ArrayDeque<>();

        nextCodeId = 1;
    }

    List<Event> map(List<StoryChildElement> textUnitElements) throws XMLStreamException {

        TextFragment textFragment = new TextFragment();

        boolean translatable = assembleTextFragment(textUnitElements, textFragment, contentCodes.size());

        if (!translatable && 0 == referentEvents.size()) {
            return singletonList(createDocumentPartEvent(textUnitElements));
        }

        List<Event> events = new ArrayList<>(1 + referentEvents.size());

        events.add(createTextUnitEvent(textUnitElements, textFragment, textUnitId));
        events.addAll(referentEvents);

        return events;
    }

    private boolean assembleTextFragment(List<StoryChildElement> textUnitElements, TextFragment textFragment, int inlineReferenceOpeningDepth) throws XMLStreamException {
        boolean translatable = false;

        for (StoryChildElement storyChildElement : textUnitElements) {

            if (storyChildElement instanceof StoryChildElement.StyledTextElement.Break) {
                if (0 < inlineReferenceOpeningDepth) {
                    addIsolatedCode(textFragment, storyChildElement);
                    continue;
                }

                break;
            }

            if (storyChildElement instanceof StoryChildElement.StyledTextElement.StyledTextReferenceElement) {
                translatable |= addReferenceableContent(textFragment, (StoryChildElement.StyledTextElement.StyledTextReferenceElement) storyChildElement, textUnitId, inlineReferenceOpeningDepth);
                continue;
            }

            if (storyChildElement instanceof StoryChildElement.StyledTextElement.Content) {
                translatable |= addContent(textFragment, (StoryChildElement.StyledTextElement.Content) storyChildElement, inlineReferenceOpeningDepth);
                continue;
            }

            addIsolatedCode(textFragment, storyChildElement);
        }

        popContentCodesToDepth(textFragment, inlineReferenceOpeningDepth);

        return translatable;
    }

    private boolean addReferenceableContent(TextFragment textFragment, StoryChildElement.StyledTextElement.StyledTextReferenceElement styledTextReferenceElement,
                                            String parentId, int inlineReferenceOpeningDepth) throws XMLStreamException {

        if (styledTextReferenceElement instanceof StoryChildElement.StyledTextReferenceElement.HyperlinkTextSource) {
            return addHyperlinkTextSource(textFragment, styledTextReferenceElement, inlineReferenceOpeningDepth);
        }

        if (styledTextReferenceElement instanceof StoryChildElement.StyledTextElement.StyledTextReferenceElement.Note && !parameters.getExtractNotes()) {
            addIsolatedCode(textFragment, styledTextReferenceElement);

            return false;
        }

        referentIdGenerator = getReferentIdGenerator(parentId, REFERENT_GROUP);
        List<Event> events = new ReferentEventsAccumulator(parameters, eventFactory, styledTextReferenceElement, parentId, referentIdGenerator).accumulate();

        // TODO: substitute with a better check
        if (2 == events.size()) {
            addIsolatedCode(textFragment, styledTextReferenceElement);

            return false;
        }

        if (!events.get(0).isStartGroup()) {
            throw new IllegalStateException(UNEXPECTED_STRUCTURE);
        }

        addIsolatedCode(textFragment, styledTextReferenceElement, events.get(0).getStartGroup().getId());
        referentEvents.addAll(events);

        return false;
    }

    private IdGenerator getReferentIdGenerator(String parentId, String prefix) {
        if (null != referentIdGenerator) {
            return referentIdGenerator;
        }

        return new IdGenerator(parentId, prefix);
    }

    private boolean addHyperlinkTextSource(TextFragment textFragment, StoryChildElement.StyledTextReferenceElement styledTextReferenceElement, int inlineReferenceOpeningDepth) throws XMLStreamException {

        StyleDefinitions styleDefinitions = styledTextReferenceElement.getStyleDefinitions();

        // the hyperlink text source is a styled element, so aligning with the styles
        addClosingCodes(textFragment, styleDefinitions, inlineReferenceOpeningDepth);

        ContentCode contentCode = new ContentCode(nextCodeId++, styledTextReferenceElement.getStyleDefinitions(), styledTextReferenceElement);
        contentCodes.push(contentCode);

        addOpeningCode(textFragment, contentCode);
        boolean translatable = assembleTextFragment(styledTextReferenceElement.getStoryChildElements(), textFragment, contentCodes.size());
        addClosingCode(textFragment, contentCodes.pop());

        return translatable;
    }

    /**
     * Adds content.
     *
     * An isolated code is produced for inner events if a content does not have translatable text.
     *
     * If a new content is a superset of the old content, then this is a nested tag (this is equivalent to asking if the
     * old content is a subset of the new one) eg, bold; --> bold;underline;
     *
     * If a new content has the same styles as the old content, no new tag is added.
     *
     * @param textFragment                A text fragment
     * @param content                     A content
     * @param inlineReferenceOpeningDepth An inline reference opening depth
     *
     * @return {@code true} - if the run content has been added
     *         {@code false} - otherwise
     */
    protected boolean addContent(TextFragment textFragment, StoryChildElement.StyledTextElement.Content content, int inlineReferenceOpeningDepth) {

        StyleDefinitions styleDefinitions = content.getStyleDefinitions();

        addClosingCodes(textFragment, styleDefinitions, inlineReferenceOpeningDepth);

        if (!defaultStyleDefinitions.equals(styleDefinitions)
                && (contentCodes.size() == inlineReferenceOpeningDepth || !contentCodes.peekFirst().getStyleDefinitions().equals(styleDefinitions))) {
            // if the specified style definitions are not equal to the default ones
            // and (the code stack is greater than the inline reference opening depth
            // or the top of the code stack is not equal to the specified style definitions)

            ContentCode contentCode = new ContentCode(nextCodeId++, styleDefinitions, null);
            contentCodes.push(contentCode);
            addOpeningCode(textFragment, contentCode);
        }

        addTranslatableContent(textFragment, content);

        return true;
    }

    private void addClosingCodes(TextFragment textFragment, StyleDefinitions styleDefinitions, int inlineReferenceOpeningDepth) {
        while (contentCodes.size() > inlineReferenceOpeningDepth
                && !contentCodes.peekFirst().getStyleDefinitions().isSubsetOf(styleDefinitions)) {
            // if the code stack size is greater than the inline reference opening depth
            // and the top of the code stack is not a subset of the specified style definitions

            addClosingCode(textFragment, contentCodes.pop());
        }
    }

    protected void addTranslatableContent(TextFragment textFragment, StoryChildElement.StyledTextElement.Content content) {
        for (XMLEvent event : content.getInnerEvents()) {

            if (event.isProcessingInstruction()) {
                addIsolatedCode(textFragment, SpecialCharacter.Instruction.fromXmlEvent(event));
                continue;
            }

            if (!event.isCharacters()) {
                // only processing instructions and characters are allowed
                throw new IllegalStateException(UNEXPECTED_STRUCTURE);
            }

            isolateSpecialCharacters(textFragment, event.asCharacters().getData());
        }
    }

    private void isolateSpecialCharacters(TextFragment textFragment, String characters) {
        for (int i = 0; i < characters.length(); i++) {
            char character = characters.charAt(i);

            SpecialCharacter.SpecialCharacterType specialCharacterType = SpecialCharacter.SpecialCharacterType.fromChar(character);

            if (parameters.getSkipDiscretionaryHyphens()
                    && SpecialCharacter.SpecialCharacterType.DISCRETIONARY_HYPHEN == specialCharacterType) {
                continue;
            }

            if (SpecialCharacter.SpecialCharacterType.UNSUPPORTED != specialCharacterType) {
                SpecialCharacter specialCharacter = SpecialCharacter.fromXmlEvent(eventFactory.createCharacters(String.valueOf(character)));

                addIsolatedCode(textFragment, specialCharacter, null);
                continue;
            }

            textFragment.append(character);
        }
    }

    private void popContentCodesToDepth(TextFragment textFragment, int contentCodesDepth) {
        while (contentCodesDepth < contentCodes.size()) {
            addClosingCode(textFragment, contentCodes.pop());
        }
    }

    private void addOpeningCode(TextFragment textFragment, ContentCode contentCode) {
        Code code = new Code(TextFragment.TagType.OPENING, contentCode.getCodeType());
        code.setId(contentCode.getCodeId());
        code.setData(CONTENT_TAG_OPENING + DASH + code.getId() + TAG_CLOSING);
        textFragment.append(code);

        codeMap.put(contentCode.getCodeId(),
                null == contentCode.getStyledTextReferenceElement()
                    ? contentCode.getStyleDefinitions()
                    : contentCode.getStyledTextReferenceElement());
    }

    private void addClosingCode(TextFragment textFragment, ContentCode contentCode) {
        Code code = new Code(TextFragment.TagType.CLOSING, contentCode.getCodeType());
        code.setId(contentCode.getCodeId());
        code.setData(CONTENT_TAG_CLOSING + DASH + code.getId() + TAG_CLOSING);
        textFragment.append(code);
    }

    private void addIsolatedCode(TextFragment textFragment, MarkupRange markupRange) {
        addIsolatedCode(textFragment, markupRange, null);
    }

    private void addIsolatedCode(TextFragment textFragment, MarkupRange markupRange, String referentId) {
        int codeId = nextCodeId++;

        String codeType = createCodeType(markupRange);
        String codeData = getCodeData(referentId, codeId);
        Code code = new Code(TextFragment.TagType.PLACEHOLDER, codeType, codeData);
        code.setId(codeId);

        if (null != referentId) {
            code.setReferenceFlag(true);
        }

        textFragment.append(code);

        codeMap.put(codeId, markupRange);
    }

    private String getCodeData(String referentId, int codeId) {
        if (null == referentId) {
            return CONTENT_TAG_OPENING + DASH + codeId + TAG_CLOSING_SINGLE;
        }

        return CONTENT_TAG_OPENING + DASH + referentId + DASH + codeId + TAG_CLOSING_SINGLE;
    }

    private Event createTextUnitEvent(List<StoryChildElement> textUnitElements, TextFragment textFragment, String id) {
        ITextUnit textUnit = new TextUnit(id);
        textUnit.setPreserveWhitespaces(true);
        textUnit.setSource(new TextContainer(textFragment));

        ISkeleton skeleton = new StyledTextSkeleton(textUnitElements, codeMap);
        skeleton.setParent(textUnit);
        textUnit.setSkeleton(skeleton);

        return new Event(EventType.TEXT_UNIT, textUnit);
    }

    private Event createDocumentPartEvent(List<StoryChildElement> textUnitElements) {
        DocumentPart documentPart = new DocumentPart(documentPartIdGenerator.createId(), false);
        documentPart.setSkeleton(new StyledTextSkeleton(textUnitElements, Collections.<Integer, Object>emptyMap()));

        return new Event(EventType.DOCUMENT_PART, documentPart);
    }
}
