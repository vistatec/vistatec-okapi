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

import javax.xml.namespace.QName;
import javax.xml.stream.events.XMLEvent;

/**
 * A block consists of a sequence of content chunks, each
 * of which is either BlockMarkup or a Run.
 */
class Block implements XMLEvents, Textual {
	private QName runName, textName;
	private List<Chunk> chunks;
	private boolean isHidden;

	Block(List<Chunk> chunks, QName runName, QName textName, boolean isHidden) {
		this.chunks = new ArrayList<>(chunks);
		this.runName = runName;
		this.textName = textName;
		this.isHidden = isHidden;
	}

	/**
	 * Return the QName of the element that contains run data in this block.
	 */
	QName getRunName() {
		return runName;
	}

	/**
	 * Return the QName of the element that contains text data in this block.
	 */
	QName getTextName() {
		return textName;
	}

	@Override
	public List<XMLEvent> getEvents() {
		List<XMLEvent> events = new ArrayList<>();
		for (XMLEvents chunk : chunks) {
			events.addAll(chunk.getEvents());
		}
		return events;
	}

	public List<Chunk> getChunks() {
		return chunks;
	}

	boolean isHidden() {
		return isHidden;
	}

	boolean hasVisibleRunContent() {
		for (Chunk chunk : chunks) {
			if (chunk instanceof Run) {
				if (((Run)chunk).containsVisibleText()) {
					return true;
				}
			}
			else if (chunk instanceof RunContainer) {
				if (((RunContainer)chunk).containsVisibleText()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Block [" + chunks + "]";
	}

	/**
	 * Marker interface to distinguish XMLEvents implementation that
	 * can be added to a Block.
	 */
	public interface BlockChunk extends Chunk { }

	static class BlockMarkup extends Markup implements BlockChunk { }
}
