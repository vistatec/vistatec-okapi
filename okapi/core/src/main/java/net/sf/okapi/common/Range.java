/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.common;

/**
 * Represents a range: a start and end position.
 */
public class Range {

	/**
	 * Starting position of this range.
	 */
	public int start;
	
	/**
	 * Ending position of this range.
	 */
	public int end;
	
	/**
	 * Optional id for this range.
	 */
	public String id;

	/**
	 * Creates a new range with given starting and ending values.
	 * @param start the start value of the new range.
	 * @param end the end value of the new range.
	 */
	public Range (int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Creates a new range with given starting and ending values and an id.
	 * @param start the start value of the new range.
	 * @param end the end value of the new range.
	 * @param id the identifier value of the new range.
	 */
	public Range (int start, int end, String id) {
		this.start = start;
		this.end = end;
		this.id = id;
	}
	
	/**
	 * Returns true if the range contains a given position within its bounds. 
	 * @param pos the given position.
	 * @return true if the given position is inside the range.
	 */
	public boolean contains(int pos) {
		return pos >= start && pos <= end;
	}
	
	/**
	 * Returns true if the range contains a given range within its bounds.
	 * @param range the given range.
	 * @return true if the given range is inside the range.
	 */
	public boolean contains(Range range) {
		return contains(range.start) && contains(range.end);
	}
	
	/**
	 * Returns true if the range intersects with a given range.
	 * @param range the given range.
	 * @return true if the given and this range have at least one common position.
	 */
	public boolean intersectsWith(Range range) {
		return contains(range.start) || contains(range.end);
	}
	
//	/**
//	 * Gets the string representation of the range without id 
//	 * (its start and end position between parenthesis, the id is not represented).
//	 * @return the string representation of the range.
//	 * @see #toString()
//	 */
//	public String toStringWithoutId () {
//		return String.format("(%d,%d)", start, end);
//	}
	
	/**
	 * Gets the string representation of the range with its id (its start and end position, and the id
	 * between parenthesis).
	 * @return the string representation of the range and its id.
	 */
	public String toString () {
		return String.format("(%d,%d,%s)", start, end, id);
	}

}
