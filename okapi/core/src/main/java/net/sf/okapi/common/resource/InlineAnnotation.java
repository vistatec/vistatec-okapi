/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

import net.sf.okapi.common.annotation.IAnnotation;

/**
 * Base implementation of an annotation that can be used on inline codes.
 * Inline annotations must have a {@link #toString()} and {@link #fromString(String)} 
 * methods to write and read themselves to and from a string.
 * <p>This basic annotation has only a string data. Its usage depends of the type 
 * of the annotation.
 */
public class InlineAnnotation implements IAnnotation {

	protected String data;

	/**
	 * Creates an empty annotation object.
	 */
	public InlineAnnotation () {
	}
	
	/**
	 * Creates a new annotation object with some initial data.
	 * @param data The data to set.
	 */
	public InlineAnnotation (String data) {
		this.data = data;
	}
	
	/**
	 * Clones this annotation.
	 * @return A new InlineAnnotation object that is a copy of this one.
	 */
	@Override
	public InlineAnnotation clone () {
		return new InlineAnnotation(this.data);
	}
	
	/**
	 * Gets a storage string representation of the whole annotation that can
	 * be used for serialization.
	 * @return The storage string representation of this annotation.
	 */
	@Override
	public String toString () {
		// this annotation has just one string.
		return data;
	}
	
	/**
	 * Initializes this annotation from a storage string originally obtained
	 * from {@link #toString()}.
	 * @param storage The storage string to use for the initialization.
	 */
	public void fromString (String storage) {
		// This annotation has just one string.
		this.data = storage;
	}

	/**
	 * Gets the data for this annotation.
	 * @return The data of this annotation.
	 */
	public String getData () {
		return data;
	}
	
	/**
	 * Sets the data for this annotation.
	 * @param data The data to set.
	 */
	public void setData (String data) {
		this.data = data;
	}

}
