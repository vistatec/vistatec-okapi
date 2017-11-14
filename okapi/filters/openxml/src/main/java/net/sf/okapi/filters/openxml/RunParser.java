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
import net.sf.okapi.common.resource.TextFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Deque;
import java.util.LinkedList;

import static net.sf.okapi.filters.openxml.AttributeStripper.RevisionAttributeStripper.stripRunRevisionAttributes;
import static net.sf.okapi.filters.openxml.StartElementContextFactory.createStartElementContext;
import static net.sf.okapi.filters.openxml.StyleExclusionChecker.isStyleExcluded;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isComplexCodeEnd;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isComplexCodeStart;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isComplexCodeSeparate;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isFieldCodeStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isFieldCodeEndEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isEndElement;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isGraphicsProperty;

import static net.sf.okapi.filters.openxml.XMLEventHelpers.isParagraphStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isRunPropsStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isTextPath;

class RunParser implements Parser<RunBuilder> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RunParser.class);

	private RunBuilder runBuilder;

	private IdGenerator nestedTextualIds;
	private Deque<ComplexCodeProcessingState> nestedComplexCodeProcessingStates = new LinkedList<>();

    private ConditionalParameters conditionalParameters;

    RunParser(StartElementContext startElementContext, IdGenerator nestedTextualIds, StyleDefinitions styleDefinitions, boolean hidden) {
        runBuilder = new RunBuilder(createStartElementContext(stripRunRevisionAttributes(startElementContext), startElementContext),
				styleDefinitions);

        this.conditionalParameters = startElementContext.getConditionalParameters();
		runBuilder.setHidden(hidden);

		this.nestedTextualIds = nestedTextualIds;
	}

	public RunBuilder parse() throws XMLStreamException {
		log("startRun: " + runBuilder.getStartElementContext().getStartElement());

		// rPr is either the first child, or not present (section 17.3.2)
		XMLEvent firstChild = runBuilder.getStartElementContext().getEventReader().nextTag();
		if (isRunPropsStartEvent(firstChild)) {
			processRunProperties(firstChild.asStartElement());
		}
		else if (isEndElement(firstChild, runBuilder.getStartElementContext().getStartElement())) {
			// Empty run!
			return endRun(firstChild.asEndElement());
		}
		else {
			// No properties section
			processRunBody(firstChild, runBuilder.getStartElementContext().getEventReader());
		}
		while (runBuilder.getStartElementContext().getEventReader().hasNext()) {
			XMLEvent e = runBuilder.getStartElementContext().getEventReader().nextEvent();
			log("processRun: " + e);
			if (isEndElement(e, runBuilder.getStartElementContext().getStartElement())) {
				return endRun(e.asEndElement());
			}
			else {
				// Handle non-properties (run body) content
				processRunBody(e, runBuilder.getStartElementContext().getEventReader());
			}
		}
		throw new IllegalStateException("Invalid content? Unterminated run");
	}

	private void processRunProperties(StartElement startElement) throws XMLStreamException {
		StartElementContext runPropertiesElementContext = createStartElementContext(startElement, runBuilder.getStartElementContext());
		runBuilder.setRunProperties(new RunPropertiesParser(runPropertiesElementContext).parse());

		RunProperty.RunStyleProperty runStyleProperty = runBuilder.getRunProperties().getRunStyleProperty();

		if (null == runStyleProperty) {
			return;
		}

		runBuilder.setRunStyle(runStyleProperty.getValue());
		runBuilder.setHidden(isStyleExcluded(runStyleProperty.getValue(), conditionalParameters));
	}

	private RunBuilder endRun(EndElement e) throws XMLStreamException {
		runBuilder.flushText();
		runBuilder.flushMarkupChunk();
		this.runBuilder.setEndEvent(e);
		// XXX This is pretty hacky.
		// Recalculate the properties now that consolidation has already happened.
		// This is required in order to properly handle the aggressive-mode trimming
		// of the vertAlign property, which is only done if there's no text in the
		// run.  Whether or not text is present can only be correctly calculated
		// -after- other run merging has already taken place.
		if (!runBuilder.hasNonWhitespaceText() && runBuilder.getStartElementContext().getConditionalParameters().getCleanupAggressively()) {
			runBuilder.setRunProperties(RunProperties.copiedRunProperties(runBuilder.getRunProperties(), true, false, false));
		}
		return this.runBuilder;
	}

	private void processRunBody(XMLEvent e, XMLEventReader events) throws XMLStreamException {
		if (isParagraphStartEvent(e)) {
			log("Nested block start event: " + e);
			runBuilder.flushText();
			StartElementContext blockElementContext = createStartElementContext(e.asStartElement(), runBuilder.getStartElementContext());

			BlockParser nestedBlockParser = new BlockParser(blockElementContext, nestedTextualIds, runBuilder.getStyleDefinitions());
			Block nested = nestedBlockParser.parse();
			runBuilder.setContainsNestedItems(true);
			if (nested.hasVisibleRunContent()) {
				// Create a reference to mark the location of the nested block
				runBuilder.addToMarkupChunk(runBuilder.getStartElementContext().getEventFactory().createCharacters(
						TextFragment.makeRefMarker(nestedTextualIds.createId())));
				runBuilder.getNestedTextualItems().add(nested);
			}
			else {
				// Empty block, we don't need to expose it after all
				for (XMLEvent nestedEvent : nested.getEvents()) {
					runBuilder.addToMarkupChunk(nestedEvent);
				}
				// However, we do need to preserve anything it references that's translatable
				for (Chunk chunk : nested.getChunks()) {
					if (chunk instanceof Run) {
						runBuilder.getNestedTextualItems().addAll(((Run) chunk).getNestedTextualItems());
					}
					else if (chunk instanceof RunContainer) {
						for (Block.BlockChunk nestedChunk : ((RunContainer)chunk).getChunks()) {
							if (nestedChunk instanceof Run) {
								runBuilder.getNestedTextualItems().addAll(((Run) nestedChunk).getNestedTextualItems());
							}
						}
					}
				}
			}
		}
		// XXX I need to make sure I don't try to merge this thing
		else if (isComplexCodeStart(e)) {
			processComplexCodes(e, events);
		}
		else {
			runBuilder.addRunBody(processTranslatableAttributes(e), events);
		}
	}

	private void processComplexCodes(XMLEvent e, XMLEventReader events) throws XMLStreamException {
		boolean isFieldCodeValue = false;
		nestedComplexCodeProcessingStates.add(new ComplexCodeProcessingState());
		runBuilder.setHasComplexCodes(true);
		runBuilder.addToMarkupChunk(e);
		while (events.hasNext()) {
            e = (XMLEvent)events.next();
            if (nestedComplexCodeProcessingStates.peek().isAfterSeparate()
					&& nestedComplexCodeProcessingStates.peek().containsPersistentContent()
					&& !isComplexCodeEnd(e)
					&& !isComplexCodeStart(e)) {
                runBuilder.addRunBody(processTranslatableAttributes(e), events);
            } else {
                runBuilder.addToMarkupChunk(e);

				if (isComplexCodeStart(e)) {
					nestedComplexCodeProcessingStates.add(new ComplexCodeProcessingState());
				} else if (isComplexCodeSeparate(e)) {
					nestedComplexCodeProcessingStates.peekLast().setAfterSeparate(true);
				} else if (isComplexCodeEnd(e)) {
					nestedComplexCodeProcessingStates.pollLast();
					if (nestedComplexCodeProcessingStates.peekLast() == null) {
						break;
					}
				} else if(!nestedComplexCodeProcessingStates.peek().isAfterSeparate()) {
					if (isFieldCodeStartEvent(e)) {
						isFieldCodeValue = true;
					} else if (isFieldCodeValue && isFieldCodeWithPersistentContent(e)) {
						nestedComplexCodeProcessingStates.peek().setContainsPersistentContent(true);
					} else if(isFieldCodeEndEvent(e)) {
						isFieldCodeValue = false;
					}
				}
            }
        }
	}

	private boolean isFieldCodeWithPersistentContent(XMLEvent e) {
		if (e.isCharacters()) {
			//get the field definition out of the field code string
			String data = e.asCharacters().getData().trim();
			int fieldCodeNameLength = data.indexOf(" ");

			String fieldCodeName;
			if (fieldCodeNameLength > 0) {
				fieldCodeName = data.substring(0, fieldCodeNameLength);
			} else {
				fieldCodeName = data;
			}

			return conditionalParameters.tsComplexFieldDefinitionsToExtract.contains(fieldCodeName);
		} else {
			return false;
		}
	}

	// translatable attributes:
	// wp:docPr/@name  if that option isn't set
	// v:textpath/@string
	private XMLEvent processTranslatableAttributes(XMLEvent e) {
		if (!e.isStartElement()) return e;
		StartElement startEl = e.asStartElement();
		// I will need to
		// - extract translatable attribute
		// - create a new start event with all the attributes except for that one, which is replaced
		if (isGraphicsProperty(startEl) && !runBuilder.getStartElementContext().getConditionalParameters().getTranslateWordExcludeGraphicMetaData()) {
			startEl = processTranslatableAttribute(startEl, "name");
		}
		else if (isTextPath(startEl)) {
			startEl = processTranslatableAttribute(startEl, "string");
		}
		return startEl;
	}

	private StartElement processTranslatableAttribute(StartElement startEl, String attrName) {
		List<Attribute> newAttrs = new ArrayList<>();
		Iterator<?> it = startEl.getAttributes();
		boolean dirty = false;
		while (it.hasNext()) {
			Attribute a = (Attribute)it.next();
			if (a.getName().getLocalPart().equals(attrName)) {
				runBuilder.setContainsNestedItems(true);
				runBuilder.getNestedTextualItems().add(new UnstyledText(a.getValue()));
				newAttrs.add(runBuilder.getStartElementContext().getEventFactory().createAttribute(a.getName(),
						TextFragment.makeRefMarker(nestedTextualIds.createId())));
				dirty = true;
			}
			else {
				newAttrs.add(a);
			}
		}
		return dirty ?
			runBuilder.getStartElementContext().getEventFactory().createStartElement(startEl.getName(), newAttrs.iterator(), startEl.getNamespaces()) :
			startEl;
	}

	private void log(String s) {
		LOGGER.debug(s);
	}

	private static class ComplexCodeProcessingState {
		private Boolean containsPersistentContent = false;
		private Boolean afterSeparate = false;

		Boolean containsPersistentContent() {
			return containsPersistentContent;
		}

		void setContainsPersistentContent(Boolean containsPersistentContent) {
			this.containsPersistentContent = containsPersistentContent;
		}

		Boolean isAfterSeparate() {
			return afterSeparate;
		}

		void setAfterSeparate(Boolean afterSeparate) {
			this.afterSeparate = afterSeparate;
		}
	}

}
