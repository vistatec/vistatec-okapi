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

import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.filters.openxml.ElementSkipper.InlineSkippableElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

import static net.sf.okapi.filters.openxml.AttributeStripper.RevisionAttributeStripper.stripParagraphRevisionAttributes;
import static net.sf.okapi.filters.openxml.ElementSkipper.GeneralCrossStructureSkippableElement.BOOKMARK_END;
import static net.sf.okapi.filters.openxml.ElementSkipper.GeneralCrossStructureSkippableElement.BOOKMARK_START;
import static net.sf.okapi.filters.openxml.ElementSkipper.GeneralElementSkipper.isInsertedRunContentEndElement;
import static net.sf.okapi.filters.openxml.ElementSkipper.GeneralInlineSkippableElement.PROOFING_ERROR_ANCHOR;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionInlineSkippableElement.RUN_DELETED_CONTENT;
import static net.sf.okapi.filters.openxml.ElementSkipper.RunPropertySkippableElement.RUN_PROPERTY_LANGUAGE;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement.PARAGRAPH_PROPERTIES_CHANGE;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement.RUN_PROPERTIES_CHANGE;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement.RUN_PROPERTY_DELETED_PARAGRAPH_MARK;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement.RUN_PROPERTY_INSERTED_PARAGRAPH_MARK;
import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createEndMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createGeneralMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createStartMarkupComponent;
import static net.sf.okapi.filters.openxml.StartElementContextFactory.createStartElementContext;
import static net.sf.okapi.filters.openxml.StyleExclusionChecker.isStyleExcluded;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isLineBreakStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isRunStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isRunContainerStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isParagraphPropertiesStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isRunContainerPropertiesStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isSimpleFieldStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.gatherEvents;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isWhitespace;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.getAttributeValue;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.WPML_VAL;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.WPML_PROPERTY_VANISH;

/**
 * Given an event stream and a block start element, this will parse and return
 * the block object.
 */
class BlockParser extends ChunkParser<Block> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BlockParser.class);

	private ElementSkipper defaultElementSkipper;
	private ElementSkipper blockPropertiesElementSkipper;
	private ElementSkipper insertedRunContentElementSkipper;
	private ElementSkipper deletedRunContentAndProofingErrorElementSkipper;
	private ElementSkipper bookmarkElementSkipper;
	private RunBuilderSkipper runBuilderSkipper;
	private BlockBuilder builder;
	private String paragraphStyle;

	BlockParser(StartElementContext startElementContext, IdGenerator nestedBlockIdGenerator, StyleDefinitions styleDefinitions) {
		super(startElementContext, nestedBlockIdGenerator, styleDefinitions);

		defaultElementSkipper = ElementSkipperFactory.createGeneralElementSkipper(startElementContext.getConditionalParameters());

		blockPropertiesElementSkipper = ElementSkipperFactory.createGeneralElementSkipper(
				startElementContext.getConditionalParameters(),
				RUN_PROPERTY_LANGUAGE,
				RUN_PROPERTY_INSERTED_PARAGRAPH_MARK,
				RUN_PROPERTY_DELETED_PARAGRAPH_MARK,
				PARAGRAPH_PROPERTIES_CHANGE,
				RUN_PROPERTIES_CHANGE);

		insertedRunContentElementSkipper = ElementSkipperFactory.createGeneralElementSkipper(
				startElementContext.getConditionalParameters(),
				ElementSkipper.RevisionInlineSkippableElement.RUN_INSERTED_CONTENT);

		deletedRunContentAndProofingErrorElementSkipper = ElementSkipperFactory.createGeneralElementSkipper(
				startElementContext.getConditionalParameters(),
				RUN_DELETED_CONTENT,
				PROOFING_ERROR_ANCHOR);

		bookmarkElementSkipper = ElementSkipperFactory.createBookmarkElementSkipper(
				BOOKMARK_START,
				BOOKMARK_END);

		runBuilderSkipper = new RunBuilderSkipper();

		builder = new BlockBuilder();
	}

	private void addRunsToBuilder(BlockBuilder builder, RunMerger runMerger) throws XMLStreamException {
		for (Block.BlockChunk chunk : runMerger.getRuns()) {
				builder.addChunk(chunk);
		}
		runMerger.reset();
	}

	private void parseRunContainer(ChunkContainer chunkContainer, StartElement runContainerStart) throws XMLStreamException {
		RunContainer rc = new RunContainer();
		RunMerger runMerger = new RunMerger();
		while (startElementContext.getEventReader().hasNext()) {
			XMLEvent e = startElementContext.getEventReader().nextEvent();
			// Check for end of container
			if (e.isEndElement() && runContainerStart.getName().equals(e.asEndElement().getName())) {
				rc.setStartElement(runContainerStart);
				rc.setEndElement(e.asEndElement());
				rc.setType(RunContainer.Type.fromValue(runContainerStart.getName().getLocalPart()));
				rc.addChunks(runMerger);
				chunkContainer.addChunk(rc);
				return;
			} else if (isRunStartEvent(e)) {
				processRun(builder, runMerger, e.asStartElement());
			} else if (isRunContainerStartEvent(e)) {
				rc.addChunks(runMerger);
				parseRunContainer(rc, e.asStartElement());
			} else if (isRunContainerPropertiesStartEvent(e)) {
				StartElementContext rcPropertiesElementContext = createStartElementContext(e.asStartElement(), startElementContext);
				MarkupComponent blockProperties = MarkupComponentParser.parseBlockProperties(rcPropertiesElementContext,
						ElementSkipperFactory.createGeneralElementSkipper(startElementContext.getConditionalParameters()));
				rc.setProperties(blockProperties);
			}
		}
		throw new IllegalStateException("Invalid content? Unterminated run container");
	}

	public Block parse() throws XMLStreamException {
		log("startBlock: " + startElementContext.getStartElement());
		builder.addMarkupComponent(createStartMarkupComponent(startElementContext.getEventFactory(), stripParagraphRevisionAttributes(startElementContext)));
		RunMerger runMerger = new RunMerger();
		while (startElementContext.getEventReader().hasNext()) {
			XMLEvent e = startElementContext.getEventReader().nextEvent();
			if (isParagraphPropertiesStartEvent(e)) {
				StartElementContext blockPropertiesElementContext = createStartElementContext(e.asStartElement(), startElementContext);
				BlockProperties blockProperties = MarkupComponentParser.parseBlockProperties(blockPropertiesElementContext, blockPropertiesElementSkipper);

				if (!blockProperties.getProperties().isEmpty() || !blockProperties.getAttributes().isEmpty()) {
					builder.addMarkupComponent(blockProperties);
				}

				paragraphStyle = getParagraphStyle(blockProperties);

				runMerger.setParagraphStyle(paragraphStyle);
				builder.setIsHidden(isStyleExcluded(paragraphStyle, startElementContext.getConditionalParameters()));
			}
			else if (isRunStartEvent(e)) {
				processRun(builder, runMerger, e.asStartElement());
			}
			else if (startElementContext.getConditionalParameters().getAddLineSeparatorCharacter() &&
					 isLineBreakStartEvent(e) && !runMerger.isEmpty()) {
				runMerger.append(startElementContext.getConditionalParameters().getLineSeparatorReplacement());
				defaultElementSkipper.skip(createStartElementContext(e.asStartElement(), startElementContext));
			}
			else if (isRunContainerStartEvent(e)) {
				StartElement runContainerStart = e.asStartElement();
				// Flush previous run, if any
				addRunsToBuilder(builder, runMerger);
				// Build the run container and add it as a single chunk
				parseRunContainer(builder, runContainerStart);
			}
			else if (isSimpleFieldStartEvent(e)) {
				addRunsToBuilder(builder, runMerger);
				StartElementContext simpleFieldElementContext = createStartElementContext(e.asStartElement(), startElementContext);
				for (XMLEvent fldEvent : gatherEvents(simpleFieldElementContext)) {
					builder.addEvent(fldEvent);
				}
				// Flush it so it will all end up as a single code with nothing else
				builder.flushMarkup();
			}
			else {
				if (processSkippableElements(e)) {
					continue;
				}

				// Trim non-essential whitespace
				if (!isWhitespace(e)) {
					// Flush any outstanding run if there's any markup
					addRunsToBuilder(builder, runMerger);

					// Check for end of block
					if (e.isEndElement() && startElementContext.getStartElement().getName().equals(e.asEndElement().getName())) {
						builder.addMarkupComponent(createEndMarkupComponent(e.asEndElement()));
						log("End block: " + e);
						return builder.build();
					} else {
						builder.addEvent(e);
					}
				}
			}
		}
		throw new IllegalStateException("Invalid content? Unterminated paragraph");
	}

	private String getParagraphStyle(BlockProperties blockProperties) {
		Attribute paragraphLevelAttribute = blockProperties.getParagraphLevelAttribute();

		if (null != paragraphLevelAttribute) {
			return paragraphLevelAttribute.getValue();
		}

		BlockProperty paragraphStyleProperty = blockProperties.getParagraphStyleProperty();

		if (null != paragraphStyleProperty) {
			return getAttributeValue(paragraphStyleProperty.getEvents().get(0).asStartElement(), WPML_VAL);
		}

		return null;
	}

	/**
	 * Processes skippable elements and skips found.
	 *
	 * @param event An XML event
	 *
	 * @return {@code true}  - if an element has been skipped
	 *         {@code false} - otherwise
	 *
	 * @throws XMLStreamException
     */
	private boolean processSkippableElements(XMLEvent event) throws XMLStreamException {
		if (event.isStartElement() && insertedRunContentElementSkipper.canSkip(event.asStartElement(), startElementContext.getStartElement())) {
			insertedRunContentElementSkipper.skip(createStartElementContext(event.asStartElement(), startElementContext, InlineSkippableElement.class));
			return true;
		}
		if (isInsertedRunContentEndElement(event)) {
			return true;
		}
		if (event.isStartElement() && deletedRunContentAndProofingErrorElementSkipper.canSkip(event.asStartElement(), startElementContext.getStartElement())) {
			deletedRunContentAndProofingErrorElementSkipper.skip(createStartElementContext(event.asStartElement(), startElementContext));
			return true;
		}
		if (event.isStartElement() && bookmarkElementSkipper.canSkip(event.asStartElement(), null)) {
			bookmarkElementSkipper.skip(createStartElementContext(event.asStartElement(), startElementContext));
			return true;
		}

		return false;
	}

	private void processRun(BlockBuilder blockBuilder, RunMerger runMerger, StartElement startEl) throws XMLStreamException {
		StartElementContext runElementContext = createStartElementContext(startEl, startElementContext);
		RunBuilder runBuilder = new RunParser(runElementContext, nestedBlockIdGenerator, styleDefinitions, blockBuilder.isHidden()).parse();

		if (runBuilderSkipper.canSkip(runBuilder)) {
			return;
		}

		clarifyVisibility(runBuilder);

		blockBuilder.setRunName(startEl.getName());
		blockBuilder.setTextName(runBuilder.getTextName());

		runMerger.add(runBuilder);
	}

	private void clarifyVisibility(RunBuilder runBuilder) {
		// If translateWordHidden parameter is turned on, no runs should be hidden:
		if (startElementContext.getConditionalParameters().getTranslateWordHidden()){
			return;
		}

		List<RunProperty> combinedRunProperties = styleDefinitions.getCombinedRunProperties(paragraphStyle, runBuilder.getRunStyle(), runBuilder.getRunProperties()).getProperties();
		for (RunProperty property : combinedRunProperties) {
			// Skip all unrelated properties:
			if (!WPML_PROPERTY_VANISH.getLocalPart().equals(property.getName().getLocalPart())) {
				continue;
			}
			// If vanish property is present but the value is false, run shouldn't be hidden:
			if (property instanceof RunProperty.WpmlToggleRunProperty
                    && !((RunProperty.WpmlToggleRunProperty) property).getToggleValue()) {
				return;
			}
			// If vanish property is present and the value is not false, run should be hidden:
			runBuilder.setHidden(true);
			return;
		}
	}

	private static class BlockBuilder implements ChunkContainer {
		private List<Chunk> chunks = new ArrayList<>();
		private List<XMLEvent> currentMarkupComponentEvents = new ArrayList<>();
		private Markup markup = new Block.BlockMarkup();
		private boolean isHidden = false;
		private QName runName, textName;

		BlockBuilder() { }

		boolean isHidden() {
			return isHidden;
		}

		void setIsHidden(boolean isHidden) {
			this.isHidden = isHidden;
		}

		void setRunName(QName runName) {
			if (this.runName == null) {
				this.runName = runName;
			}
		}

		void setTextName(QName textName) {
			if (this.textName == null) {
				this.textName = textName;
			}
		}

		private void flushMarkup() {
			if (!currentMarkupComponentEvents.isEmpty()) {
				markup.addComponent(createGeneralMarkupComponent(currentMarkupComponentEvents));
				currentMarkupComponentEvents = new ArrayList<>();
			}
			if (!markup.getComponents().isEmpty()) {
				chunks.add(markup);
				markup = new Block.BlockMarkup();
			}
		}

		void addEvent(XMLEvent event) {
			currentMarkupComponentEvents.add(event);
		}

		@Override
		public void addChunk(Block.BlockChunk chunk) {
			flushMarkup();
			chunks.add(chunk);
		}

		void addMarkupComponent(MarkupComponent markupComponent) {
			if (!currentMarkupComponentEvents.isEmpty()) {
				markup.addComponent(createGeneralMarkupComponent(currentMarkupComponentEvents));
				currentMarkupComponentEvents = new ArrayList<>();
			}
			markup.addComponent(markupComponent);
		}

		Block build() {
			flushMarkup();
			return new Block(chunks, runName, textName, isHidden);
		}
	}

	private void log(String s) {
		LOGGER.debug(s);
	}
}
