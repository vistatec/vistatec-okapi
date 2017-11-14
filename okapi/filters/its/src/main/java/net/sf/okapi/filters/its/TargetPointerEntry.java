/*===========================================================================
  Copyright (C) 2012-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.its;

import org.w3c.dom.Node;

/**
 * Holds the information for the Target Pointer data category on a given node.
 */
class TargetPointerEntry {

	static final String SRC_TRGPTRFLAGNAME = "\u10ff"; // Name of the user-data property that holds the target pointer flag in the source
	static final String TRG_TRGPTRFLAGNAME = "\u20ff"; // Name of the user-data property that holds the target pointer flag in the target

	static final int BEFORE = 0;
	static final int AFTER = 1;
		
	private boolean translate;
	private Node srcNode;
	private Node trgNode;
	private boolean hasExistingTargetContent;

	/**
	 * Creates a {@link #TargetPointerEntry} object.
	 * @param srcNode the source node.
	 * @param trgNode the target node.
	 */
	TargetPointerEntry (Node srcNode,
		Node trgNode)
	{
		this.srcNode = srcNode;
		this.trgNode = trgNode;
	}

	/**
	 * Gets the source node for this entry.
	 * @return the source node for this entry.
	 */
	Node getSourceNode () {
		return srcNode;
	}
	
	/**
	 * Gets the target node for this entry.
	 * @return the target node for this entry.
	 */
	Node getTargetNode () {
		return trgNode;
	}

	/**
	 * Sets the flag indicating if the source node of this pair is translatable. 
	 * @param translate true if it is to be translated, false otherwise.
	 */
	void setTranslate (boolean translate) {
		this.translate = translate;
	}
	
	/**
	 * Gets the flag indicating if the source node of this pair is translatable.
	 * @return true if it to be translated, false otherwise.
	 */
	boolean getTranslate () {
		return this.translate;
	}
	
	/**
	 * sets the flag indicating if there is an initial target content for the pair.
	 * @param hasExistingTargetContent true if there is an initial target content, false otherwise.
	 */
	void setHasExistingTargetContent (boolean hasExistingTargetContent) {
		this.hasExistingTargetContent = hasExistingTargetContent;
	}
}
