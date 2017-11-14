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
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createGeneralMarkupComponent;
import static net.sf.okapi.filters.openxml.StartElementContextFactory.createStartElementContext;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.createQName;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.hasPreserveWhitespace;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isEndElement;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isLineBreakStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isNoBreakHyphenStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isPageBreak;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isTabStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isTextStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isWhitespace;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_REGULAR_HYPHEN_VALUE;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.CACHED_PAGE_BREAK;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_TEXT;

import net.sf.okapi.filters.openxml.Run.RunText;

class RunBuilder {
    private StartElementContext startElementContext;
    private EndElement endEvent;
    private RunProperties runProperties = RunProperties.emptyRunProperties();
    private RunProperties combinedRunProperties;
    private List<Chunk> runBodyChunks = new ArrayList<>();
    private List<Textual> nestedTextualItems = new ArrayList<>();
    private boolean isHidden = false;

    private QName textName;
    private String runStyle;
    private boolean containsNestedItems = false;
    private boolean hasComplexCodes;
    private StyleDefinitions styleDefinitions;
    private boolean isTextPreservingWhitespace = false;
    private List<XMLEvent> currentMarkupChunk = new ArrayList<>();
    private boolean hasAnyText = false;
    private boolean hasNonWhitespaceText = false;
    private StringBuilder textContent = new StringBuilder();

    private ElementSkipper elementSkipper;
    private ElementSkipper softHyphenElementSkipper;
    private ElementSkipper alternateContentFallbackElementSkipper;

    RunBuilder(StartElementContext startElementContext, StyleDefinitions styleDefinitions) {
        this.startElementContext = startElementContext;
        this.styleDefinitions = styleDefinitions;

        elementSkipper = ElementSkipperFactory.createGeneralElementSkipper(startElementContext.getConditionalParameters());

        softHyphenElementSkipper = ElementSkipperFactory.createGeneralElementSkipper(
                startElementContext.getConditionalParameters(),
                ElementSkipper.GeneralInlineSkippableElement.SOFT_HYPHEN);

        alternateContentFallbackElementSkipper = ElementSkipperFactory.createGeneralElementSkipper(
                startElementContext.getConditionalParameters(),
                ElementSkipper.GeneralInlineSkippableElement.ALTERNATE_CONTENT_FALLBACK);
    }

    StartElementContext getStartElementContext() {
        return startElementContext;
    }

    void setEndEvent(EndElement endEvent) {
        this.endEvent = endEvent;
    }

    List<Chunk> getRunBodyChunks() {
        return runBodyChunks;
    }

    RunProperties getRunProperties() {
        return runProperties;
    }

    void setRunProperties(RunProperties runProperties) {
        this.runProperties = runProperties;
    }

    RunProperties getCombinedRunProperties(String paragraphStyle) {
        if (null == combinedRunProperties) {
            resetCombinedRunProperties(paragraphStyle);
        }

        return combinedRunProperties;
    }

    void resetCombinedRunProperties(String paragraphStyle) {
        combinedRunProperties = styleDefinitions.getCombinedRunProperties(paragraphStyle, runStyle, runProperties);
    }

    List<Textual> getNestedTextualItems() {
        return nestedTextualItems;
    }

    void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public boolean isHidden() {
        return isHidden;
    }

    QName getTextName() {
        return textName;
    }

    String getRunStyle() {
        return runStyle;
    }

    void setRunStyle(String runStyle) {
        this.runStyle = runStyle;
    }

    boolean containsNestedItems() {
        return containsNestedItems;
    }

    void setContainsNestedItems(boolean containsNestedItems) {
        this.containsNestedItems = containsNestedItems;
    }

    boolean hasComplexCodes() {
        return hasComplexCodes;
    }

    void setHasComplexCodes(boolean hasComplexCodes) {
        this.hasComplexCodes = hasComplexCodes;
    }

    StyleDefinitions getStyleDefinitions() {
        return styleDefinitions;
    }

    boolean isTextPreservingWhitespace() {
        return isTextPreservingWhitespace;
    }

    void setTextPreservingWhitespace(boolean textPreservingWhitespace) {
        isTextPreservingWhitespace = textPreservingWhitespace;
    }

    boolean hasNonWhitespaceText() {
        return hasNonWhitespaceText;
    }

    Block.BlockChunk build() throws XMLStreamException {
        return new Run(startElementContext.getStartElement(), endEvent,
                runProperties, combinedRunProperties,
                runBodyChunks, nestedTextualItems, isHidden);
    }

    void addRunBody(XMLEvent e, XMLEventReader eventReader) throws XMLStreamException {

        if (isTextStartEvent(e)) {

            flushMarkupChunk();
            processText(e.asStartElement(), eventReader);

        } else if (startElementContext.getConditionalParameters().getAddTabAsCharacter() && isTabStartEvent(e) && !isHidden) {

            flushMarkupChunk();
            addRawText("\t", e.asStartElement());
            elementSkipper.skip(createStartElementContext(e.asStartElement(), eventReader, null, startElementContext.getConditionalParameters()));

        } else if (startElementContext.getConditionalParameters().getAddLineSeparatorCharacter() && isLineBreakStartEvent(e) && !isPageBreak(e.asStartElement())  && !isHidden) {

            flushMarkupChunk();
            char replacement = startElementContext.getConditionalParameters().getLineSeparatorReplacement();
            addRawText(String.valueOf(replacement), e.asStartElement());
            elementSkipper.skip(createStartElementContext(e.asStartElement(), eventReader, null, startElementContext.getConditionalParameters()));

        } else if (e.isStartElement() && isStrippableRunBodyElement(e.asStartElement())) {

            // Consume to the end of the element
            elementSkipper.skip(createStartElementContext(e.asStartElement(), eventReader, null, startElementContext.getConditionalParameters()));

        } else if (startElementContext.getConditionalParameters().getReplaceNoBreakHyphenTag() && isNoBreakHyphenStartEvent(e)) {

            flushMarkupChunk();
            addRawText(LOCAL_REGULAR_HYPHEN_VALUE, e.asStartElement());
            elementSkipper.skip(createStartElementContext(e.asStartElement(), eventReader, null, startElementContext.getConditionalParameters()));

        } else if (startElementContext.getConditionalParameters().getIgnoreSoftHyphenTag()
                && e.isStartElement() && softHyphenElementSkipper.canSkip(e.asStartElement(), startElementContext.getStartElement())) {

            // Ignore soft hyphens
            softHyphenElementSkipper.skip(createStartElementContext(e.asStartElement(), eventReader, null, startElementContext.getConditionalParameters()));

        } else if (e.isStartElement() && alternateContentFallbackElementSkipper.canSkip(e.asStartElement(), startElementContext.getStartElement())) {

            alternateContentFallbackElementSkipper.skip(createStartElementContext(e.asStartElement(), eventReader, null, startElementContext.getConditionalParameters()));

        } else if (!isWhitespace(e) || inPreservingWhitespaceElement()) {

            // Real text should have been handled above.  Most whitespace is ignorable,
            // but if we're in a preserve-whitespace section, we need to save it (eg
            // for w:instrText, which isn't translatable but needs to be preserved).
            flushText();
            isTextPreservingWhitespace = false;
            addToMarkupChunk(e);
        }
    }

    /**
     * @return the run's text
     */
    String getRunText() {
        for (Chunk chunk : getRunBodyChunks()) {
            if (chunk instanceof RunText) {
                return ((RunText) chunk).getText();
            }
        }
        return null;
    }

    /**
     * Handle cases like <w:instrText> -- non-text elements that we may need
     * to obey xml:space="preserve" on.
     */
    private boolean inPreservingWhitespaceElement() {
        // Look only at the most recent element on the stack
        if (currentMarkupChunk.size() > 0) {
            XMLEvent e = currentMarkupChunk.get(currentMarkupChunk.size() - 1);
            if (e instanceof StartElement && hasPreserveWhitespace(e.asStartElement())) {
                return true;
            }
        }
        return false;
    }

    private void processText(StartElement startEvent, XMLEventReader events) throws XMLStreamException {
        hasAnyText = true;
        // Merge the preserve whitespace flag
        isTextPreservingWhitespace = isTextPreservingWhitespace || hasPreserveWhitespace(startEvent);

        if (textName == null) {
            textName = startEvent.getName();
        }
        while (events.hasNext()) {
            XMLEvent e = events.nextEvent();
            if (isEndElement(e, startEvent)) {
                return;
            } else if (e.isCharacters()) {
                String text = e.asCharacters().getData();
                if (text.trim().length() > 0) {
                    hasNonWhitespaceText = true;
                }
                textContent.append(text);
            }
        }
    }

    void flushText() {
        // It seems like there may be a bug where presml runs need to have
        // an empty <a:t/> at a minimum.
        if (hasAnyText) {
            runBodyChunks.add(new Run.RunText(createTextStart(),
                    startElementContext.getEventFactory().createCharacters(textContent.toString()),
                    startElementContext.getEventFactory().createEndElement(textName, null)));
            textContent.setLength(0);
            hasAnyText = false;
        }
    }

    private StartElement createTextStart() {
        return startElementContext.getEventFactory().createStartElement(textName,
                // DrawingML <a:t> does not use the xml:space="preserve" attribute
                isTextPreservingWhitespace && !Namespaces.DrawingML.containsName(textName) ?
                        java.util.Collections.singleton(
                                startElementContext.getEventFactory().createAttribute("xml", Namespaces.XML.getURI(), "space", "preserve"))
                                .iterator() : null,
                null);
    }

    private boolean isStrippableRunBodyElement(StartElement el) {
        return el.getName().equals(CACHED_PAGE_BREAK);
    }

    private void addRawText(String text, StartElement startElement) throws XMLStreamException {
        hasAnyText = true;
        textContent.append(text);

        if (textName == null) {
            textName = createQName(LOCAL_TEXT, startElement.getName());
        }
    }

    void addToMarkupChunk(XMLEvent event) {
        currentMarkupChunk.add(event);
    }

    void flushMarkupChunk() {
        if (currentMarkupChunk.size() > 0) {
            runBodyChunks.add(new Run.RunMarkup().addComponent(createGeneralMarkupComponent(currentMarkupChunk)));
            currentMarkupChunk = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        try {
            return "RunBuilder for " + build().toString();
        } catch (XMLStreamException e) {
            return "RunBuilder (" + e.getMessage() + ")";
        }
    }
}
