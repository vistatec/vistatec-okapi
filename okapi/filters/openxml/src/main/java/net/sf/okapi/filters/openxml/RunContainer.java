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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static net.sf.okapi.filters.openxml.RunProperties.emptyRunProperties;

/**
 * A markup structure, such as a hyperlink and smartTag , that can contain
 * multiple child runs and nested containers.
 *
 * Runs within this container can be simplified and consolidated, but
 * can't be consolidated with runs outside the container.  When exposed
 * as ITextUnit content, the container boundaries should appear as a single
 * set of paired codes.
 */
class RunContainer implements Block.BlockChunk, ChunkContainer {
	final static EnumSet<Type> RUN_CONTAINER_TYPES = EnumSet.of(Type.HYPERLINK, Type.SMARTTAG);

	private StartElement startElement;
	private EndElement endElement;
	private Type type;

	private List<Block.BlockChunk> chunks = new ArrayList<>();
	private MarkupComponent properties;

	RunContainer() {
	}

	void addChunks(RunMerger runMerger) throws XMLStreamException {
		this.chunks.addAll(runMerger.getRuns());
		runMerger.reset();
	}

	public void addChunk(Block.BlockChunk chunk) throws XMLStreamException {
		this.chunks.add(chunk);
	}

	List<Block.BlockChunk> getChunks() {
		return chunks;
	}

	StartElement getStartElement() {
		return startElement;
	}

	EndElement getEndElement() {
		return endElement;
	}

	Type getType() {
		return type;
	}

	MarkupComponent getProperties() {
		return properties;
	}

	void setProperties(MarkupComponent properties) {
		this.properties = properties;
	}

	void setStartElement(StartElement startElement) {
		this.startElement = startElement;
	}

	void setEndElement(EndElement endElement) {
		this.endElement = endElement;
	}

	void setType(Type type) {
		this.type = type;
	}

	boolean containsVisibleText() {
		for (Chunk chunk : getChunks()) {
			if (chunk instanceof Run) {
				if (((Run) chunk).containsVisibleText()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * The container assumes the properties of the first run are its "default",
	 * so that no additional code is needed to write them.
	 */
	RunProperties getDefaultRunProperties() {
		return (chunks.size() > 0 && chunks.get(0) instanceof Run)
				? ((Run) chunks.get(0)).getProperties()
				: emptyRunProperties();
	}

	RunProperties getDefaultCombinedRunProperties() {
		return (chunks.size() > 0 && chunks.get(0) instanceof Run)
				? ((Run) chunks.get(0)).getCombinedProperties()
				: emptyRunProperties();
	}

	@Override
	public List<XMLEvent> getEvents() {
		List<XMLEvent> events = new ArrayList<>();
		events.add(startElement);
		for (Block.BlockChunk chunk: chunks) {
			events.addAll(chunk.getEvents());
		}
		events.add(endElement);
		return events;
	}

	@Override
	public String toString() {
		return "RunContainer(" + XMLEventSerializer.serialize(startElement) + ", "+ chunks.size() +
				")[" + chunks  + "]";
	}

	/**
	 * Provides run container types.
	 */
	enum Type {
		HYPERLINK("hyperlink"),
		SMARTTAG("smartTag"),
		UNSUPPORTED("");

		String value;

		Type(String value) {
			this.value = value;
		}

		String getValue() {
			return value;
		}

		static Type fromValue(String tagName) {
			if (tagName == null) {
				return UNSUPPORTED;
			}

			for (Type type: values()) {
				if (type.getValue().equals(tagName)) {
					return type;
				}
			}
			return Type.UNSUPPORTED;
		}
	}
}
