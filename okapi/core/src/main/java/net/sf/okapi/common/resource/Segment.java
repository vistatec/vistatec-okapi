/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource;

/**
 * Implement a special content part that is a segment.
 * A segment is a {@link TextPart} with an identifier.
 */
public class Segment extends TextPart {
	
	public static final String REF_MARKER = "$segment$";

	/**
	 * Identifier of this segment.
	 */
	// FIXME - why is this public?
	public String id;
	
	/**
	 * Creates an empty Segment object with a null identifier.
	 */
	public Segment () {
		super(new TextFragment());
	}
	
	/**
	 * Creates an empty Segment object with a given identifier.
	 * @param id identifier for the new segment (Can be null).
	 */
	public Segment (String id) {
		super(new TextFragment());
		this.id = id;
	}
	
	/**
	 * Creates a Segment object with a given identifier and a given
	 * text fragment.
	 * @param id identifier for the new segment (Can be null).
	 * @param text text fragment for the new segment.
	 */
	public Segment (String id,
		TextFragment text)
	{
		super(text);
		this.id = id;
	}

	@Override
	public Segment clone () {
		TextPart part = super.clone();
		Segment newSeg = new Segment(id, part.getContent());
		newSeg.properties = part.properties;
		newSeg.annotations = part.annotations;
		return newSeg;
	}
	
	@Override
	public boolean isSegment () {
		return true;
	}

	/**
	 * Gets the identifier for this segment.
	 * @return the identifier for this segment.
	 */
	public String getId () {
		return id;
	}
	
	/**
	 * Forces the id of this segment to a specific value.
	 * No check is made to validate this ID value. It is the caller's responsability
	 * to avoid duplicates, null value, and other wrong values.
	 * @param id the new value of the segment.
	 */
	public void forceId (String id) {
		this.id = id;
	}
	
	public static String makeRefMarker(String segId) {
		return TextFragment.makeRefMarker(segId, REF_MARKER);
	}
}
