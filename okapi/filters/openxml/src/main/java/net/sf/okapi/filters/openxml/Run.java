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

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Representation of a parsed text run.
 */
class Run implements Block.BlockChunk {
	private StartElement startEvent;
	private EndElement endEvent;
	private RunProperties runProperties;
	private RunProperties combinedProperties;
	private List<Chunk> bodyChunks;
	private List<Textual> nestedTextualItems;
	private boolean isHidden;

	Run(StartElement startEvent, EndElement endEvent, RunProperties runProperties, RunProperties combinedProperties,
		List<Chunk> bodyChunks, List<Textual> nestedTextualItems, boolean isHidden) {

		this.startEvent = startEvent;
		this.endEvent = endEvent;
		this.runProperties = runProperties;
		this.combinedProperties = combinedProperties;
		this.bodyChunks = bodyChunks;
		this.nestedTextualItems = nestedTextualItems;
		this.isHidden = isHidden;
	}

	RunProperties getProperties() {
		return runProperties;
	}

	RunProperties getCombinedProperties() {
		return combinedProperties;
	}

	List<Chunk> getBodyChunks() {
		return bodyChunks;
	}

	List<Textual> getNestedTextualItems() {
		return nestedTextualItems;
	}

	/**
	 * Return true if this run contains visible text.
	 */
	boolean containsVisibleText() {
		if (isHidden) {
			return false;
		}
		for (Chunk c : bodyChunks) {
			if (c instanceof RunText) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<XMLEvent> getEvents() {
		List<XMLEvent> events = new ArrayList<>();
		events.add(startEvent);
		events.addAll(runProperties.getEvents());
		for (XMLEvents chunk : bodyChunks) {
			events.addAll(chunk.getEvents());
		}
		events.add(endEvent);
		return events;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + XMLEventSerializer.serialize(getEvents()) + "]";
	}

	static class RunText extends StyledText implements RunChunk {
		RunText(StartElement startElement, Characters text, EndElement endElement) {
			super(startElement, text, endElement);
		}
	}

	/**
	 * Marker interface to distinguish XMLEvents implementation that
	 * can be added to a Run as body content.
	 */
	public interface RunChunk extends Chunk { }

	static class RunMarkup extends Markup implements RunChunk { }
}
