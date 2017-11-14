/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.markdown;

import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.BULLET_LIST_ITEM;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.FENCED_CODE_BLOCK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.FENCED_CODE_BLOCK_INFO;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HARD_LINE_BREAK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HEADING_PREFIX;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HEADING_UNDERLINE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HTML_BLOCK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HTML_COMMENT_BLOCK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HTML_INNER_BLOCK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HTML_INNER_BLOCK_COMMENT;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.INDENTED_CODE_BLOCK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.ORDERED_LIST_ITEM;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.SOFT_LINE_BREAK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.TABLE_PIPE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.TEXT;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.THEMATIC_BREAK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.WHITE_SPACE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.YAML_METADATA_HEADER;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.filters.AbstractFilter;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.filters.markdown.parser.MarkdownParser;
import net.sf.okapi.filters.markdown.parser.MarkdownToken;

@UsingParameters(Parameters.class)
public class MarkdownFilter extends AbstractFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownFilter.class);

    private MarkdownParser parser;
    private Parameters params = new Parameters();
    private RawDocument currentRawDocument;
    private BOMNewlineEncodingDetector detector;
    private MarkdownEventBuilder eventBuilder;

    public MarkdownFilter() {
        super();
        this.parser = new MarkdownParser(params);
        setMimeType(MimeTypeMapper.MARKDOWN_MIME_TYPE);
        setMultilingual(false);
        setName("okf_markdown");
        setDisplayName("Markdown Filter");
        // must be called *after* parameters is initialized
        setFilterWriter(createFilterWriter());
        addConfiguration(new FilterConfiguration(getName(), MimeTypeMapper.MARKDOWN_MIME_TYPE,
                getClass().getName(), "Markdown",
                "Markdown files", null, ".md"));
    }

    @Override
    public void close() {
        if (currentRawDocument != null) {
            currentRawDocument.close();
            detector = null;
            eventBuilder = null;
        }
    }

    @Override
    public IFilterWriter createFilterWriter() {
        return super.createFilterWriter();
    }

    @Override
    public Parameters getParameters() {
        return params;
    }

    @Override
    public boolean hasNext() {
        return eventBuilder.hasQueuedEvents();
    }

    @Override
    protected boolean isUtf8Bom() {
        return detector != null && detector.hasUtf8Bom();
    }

    @Override
    protected boolean isUtf8Encoding() {
        return detector != null && detector.hasUtf8Encoding();
    }

    @Override
    public Event next() {
        if (hasNext()) {
            return eventBuilder.next();
        }
        throw new IllegalStateException("No events available");
    }

    @Override
    public void open(RawDocument input) {
        open(input, true);
    }

    @Override
    public void open(RawDocument input, boolean generateSkeleton) {
        currentRawDocument = input;
        if (input.getInputURI() != null) {
            setDocumentName(input.getInputURI().getPath());
        }

        detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
        detector.detectAndRemoveBom();
        setNewlineType(detector.getNewlineType().toString());

        String detectedEncoding = getDetectedEncoding();
        input.setEncoding(detectedEncoding);
        setEncoding(detectedEncoding);
        setOptions(input.getSourceLocale(), input.getTargetLocale(), detectedEncoding, generateSkeleton);

        // Make sure the parser is using the latest params
        this.parser = new MarkdownParser(params);

        generateTokens();

        // Create EventBuilder with document name as rootId
        if (eventBuilder == null) {
            eventBuilder = new MarkdownEventBuilder(getParentId(), this);
        } else {
            eventBuilder.reset(getParentId(), this);
        }
        eventBuilder.setPreserveWhitespace(true);

        // Compile code finder rules
        if (params.getUseCodeFinder()) {
            params.getCodeFinder().compile();
            eventBuilder.setCodeFinder(params.getCodeFinder());
        }
        generateEvents();
    }

    @Override
    public void setParameters(IParameters params) {
        this.params = (Parameters) params;
        // may be new parameter options for skeleton writer and encoder
        createSkeletonWriter();
        getEncoderManager();
    }

    private String getDetectedEncoding() {
        String detectedEncoding = getEncoding();
        if (detector.isDefinitive()) {
            detectedEncoding = detector.getEncoding();
            LOGGER.debug("Overridding user set encoding (if any). Setting auto-detected encoding {}.",
                    detectedEncoding);

        } else if (!detector.isDefinitive() && getEncoding().equals(RawDocument.UNKOWN_ENCODING)) {
            detectedEncoding = detector.getEncoding();
            LOGGER.debug("Default encoding and detected encoding not found. Using best guess encoding {}",
                    detectedEncoding);
        }
        return detectedEncoding;
    }

    private void generateTokens() {
        parser.setNewline(getNewlineType());
        try (Scanner scanner = new Scanner(currentRawDocument.getReader())) {
            scanner.useDelimiter("\\A");
            if (scanner.hasNext()) {
                parser.parse(scanner.next());
            }
        }
    }

    private void generateEvents() {
        eventBuilder.addFilterEvent(createStartFilterEvent());

        while (parser.hasNextToken()) {
            MarkdownToken token = parser.getNextToken();

            // In the case of indented code block, make the entire block a text unit
            if (token.getType().equals(INDENTED_CODE_BLOCK)) {
                boolean lastTokenNewline = false;

                if (!eventBuilder.isCurrentTextUnit()) {
                    eventBuilder.startTextUnit();
                }
                while (parser.hasNextToken()) {
                    // If there is no whitespace token after a newline, we have exited the code block
                    if (lastTokenNewline && !isIndented(token)) {
                        eventBuilder.endTextUnit();
                        break;
                    }
                    if (isIndented(token) || isNewline(token)) {
                        eventBuilder.addToTextUnit(
                                new Code(TagType.PLACEHOLDER, token.getType().name(), token.getContent()));
                    } else {
                        eventBuilder.addToTextUnit(token.getContent());
                    }
                    lastTokenNewline = isNewline(token);
                    token = parser.getNextToken();
                }
            }

            if (isDocumentPart(token)) {
                if (eventBuilder.isCurrentTextUnit()) {
                    eventBuilder.endTextUnit();
                }
                eventBuilder.startDocumentPart(token.getContent());
                eventBuilder.endDocumentPart();

            } else if (isCode(token)) {
                if (eventBuilder.isCurrentTextUnit()) {
                    // Add to the already-existing text unit
                    eventBuilder.addToTextUnit(
                            new Code(TagType.PLACEHOLDER, token.getType().name(), token.getContent()));
                } else {
                    // No need to create a text unit starting with a code, so create document part instead
                    eventBuilder.startDocumentPart(token.getContent());
                    eventBuilder.endDocumentPart();
                }

            } else if (token.isTranslatable()) {
                if (token.getContent().replaceAll(Parameters.HTML_RULE, "").isEmpty() && isHtmlBlock(token)) {
                    // Special case - token contains only HTML content, so add it as document part
                    eventBuilder.startDocumentPart(token.getContent());
                    eventBuilder.endDocumentPart();

                } else {
                    // Normal case - add token as text unit
                    if (!eventBuilder.isCurrentTextUnit()) {
                        eventBuilder.startTextUnit();
                    }
                    eventBuilder.addToTextUnit(token.getContent());

                }

            } else {
                eventBuilder.startDocumentPart(token.getContent());
                eventBuilder.endDocumentPart();
            }
        }

        if (eventBuilder.isCurrentTextUnit()) {
            eventBuilder.endTextUnit();
        }
        eventBuilder.flushRemainingTempEvents();
        eventBuilder.addFilterEvent(createEndFilterEvent());
    }

    private boolean isCode(MarkdownToken token) {
        return token != null && !isNewline(token) && !isHtmlBlock(token) && !token.getType().equals(TEXT);
    }

    private boolean isHtmlBlock(MarkdownToken token) {
        return token.getType().equals(HTML_BLOCK)
                || token.getType().equals(HTML_COMMENT_BLOCK)
                || token.getType().equals(HTML_INNER_BLOCK)
                || token.getType().equals(HTML_INNER_BLOCK_COMMENT);
    }

    private boolean isNewline(MarkdownToken token) {
        return token != null && (token.getType().equals(SOFT_LINE_BREAK)
                || token.getType().equals(HARD_LINE_BREAK));
    }

    private boolean isDocumentPart(MarkdownToken token) {
        return token != null && (isNewline(token)
                || token.getType().equals(BULLET_LIST_ITEM)
                || token.getType().equals(ORDERED_LIST_ITEM)
                || token.getType().equals(FENCED_CODE_BLOCK)
                || token.getType().equals(FENCED_CODE_BLOCK_INFO)
                || token.getType().equals(HEADING_PREFIX)
                || token.getType().equals(HEADING_UNDERLINE)
                || token.getType().equals(THEMATIC_BREAK)
                || token.getType().equals(WHITE_SPACE)
                || token.getType().equals(TABLE_PIPE)
                || token.getType().equals(YAML_METADATA_HEADER));
    }

    private boolean isIndented(MarkdownToken token) {
        return token.getContent().contains("    ");
    }

}
