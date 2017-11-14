/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.tm.pensieve.common;

import net.sf.okapi.common.Util;

/**
 * Represents a Unit of Translation.
 */
public class TranslationUnit {
	private TranslationUnitVariant source;
	private TranslationUnitVariant target;
	private Metadata metadata;

	/**
	 * Creates a TU w/o an source or target defined
	 */
	public TranslationUnit() {
		metadata = new Metadata();
	}

	/**
	 * Creates a TU with the provided source and targets
	 * 
	 * @param source
	 *            The source of the TU
	 * @param target
	 *            The target of the TU
	 */
	public TranslationUnit(TranslationUnitVariant source,
			TranslationUnitVariant target) {
		this();
		this.source = source;
		this.target = target;
	}

	/**
	 * Gets the metadata or attributes for this TU
	 * 
	 * @return The Metadata of this TU
	 */
	public Metadata getMetadata() {
		return metadata;
	}

	// TODO: get rid of me
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public TranslationUnitVariant getSource() {
		return source;
	}

	public TranslationUnitVariant getTarget() {
		return target;
	}

	public void setSource(TranslationUnitVariant source) {
		this.source = source;
	}

	public void setTarget(TranslationUnitVariant target) {
		this.target = target;
	}

	/**
	 * Checks to see if the the source is empty
	 * 
	 * @return true if the source is empty
	 */
	public boolean isSourceEmpty() {
		return isFragmentEmpty(source);
	}

	/**
	 * Sets the value for a give metadata value field
	 * 
	 * @param key
	 *            the key for the data we want set
	 * @param value
	 *            the vlaue to set the metadata to
	 */
	public void setMetadataValue(MetadataType key, String value) {
		if (Util.isEmpty(value)) {
			metadata.remove(key);
		} else {
			metadata.put(key, value);
		}
	}

	/**
	 * Checks to see if the the target is empty
	 * 
	 * @return true if the target is empty
	 */
	public boolean isTargetEmpty() {
		return isFragmentEmpty(target);
	}

	/**
	 * Gets the value for a give metadata value field
	 * 
	 * @param key
	 *            the key for the data we want
	 * @return the value for a give metadata value field
	 */
	public String getMetadataValue(MetadataType key) {
		return metadata.get(key);
	}

	private static boolean isFragmentEmpty(TranslationUnitVariant frag) {
		return (frag == null || frag.getContent() == null || frag.getContent().isEmpty());
	}

	@Override
	public String toString() {
		return "Source: " + getSource().getContent().toText() + 
			   "\nTarget: " + getTarget().getContent().toText();

	}
}
