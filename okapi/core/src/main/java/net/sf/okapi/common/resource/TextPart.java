/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;

/**
 * Implements the base object for the parts that make up a content.
 */
public class TextPart implements IWithProperties, IWithAnnotations {
	protected LinkedHashMap<String, Property> properties;
	protected Annotations annotations;

	/**
	 * Text fragment of this part.
	 */
	public TextFragment text;

	/**
	 * Creates an empty part.
	 */
	public TextPart() {
		text = new TextFragment();
	}

	/**
	 * Creates a new TextPart with a given {@link TextFragment}.
	 * 
	 * @param text
	 *            the {@link TextFragment} for this new part.
	 */
	public TextPart(TextFragment text) {
		if (text == null) {
			text = new TextFragment();
		}
		this.text = text;
	}

	/**
	 * Creates a new TextPart with a given text string.
	 * 
	 * @param text
	 *            the text for this new part.
	 */
	public TextPart(String text) {
		this.text = new TextFragment(text);
	}

	@Override
	public TextPart clone() {
		TextPart tp = new TextPart(text.clone());
		// Clone the properties
		if ( this.properties != null ) {
			tp.properties = new LinkedHashMap<String, Property>();
			for ( Property prop : this.properties.values() ) {
				tp.properties.put(prop.getName(), prop.clone()); 
			}
		}
		// clone annotations
		if (annotations != null) {
			tp.annotations = annotations.clone();
		}
		return tp;
	}

	@Override
	public String toString() {
		if (text == null)
			return "";
		return text.toText();
	}

	/**
	 * Gets the text fragment for this part.
	 * 
	 * @return the text fragment for this part.
	 */
	public TextFragment getContent() {
		return text;
	}

	/**
	 * Sets the {@link TextFragment} for this part.
	 * 
	 * @param fragment
	 *            the {@link TextFragment} to assign to this part. It must not
	 *            be null.
	 */
	public void setContent(TextFragment fragment) {
		this.text = fragment;
	}

	/**
	 * Indicates if this part is a {@link Segment}.
	 * 
	 * @return true if the part is a {@link Segment}, false if it is not.
	 */
	public boolean isSegment() {
		return false;
	}

	@Override
	public Iterable<IAnnotation> getAnnotations() {
		if (annotations == null) {
			return Collections.emptyList();
		}
		return annotations;
	}

	@Override
	public <A extends IAnnotation> A getAnnotation(Class<A> annotationType) {
		if (annotations == null)
			return null;
		return annotationType.cast(annotations.get(annotationType));
	}

	@Override
	public void setAnnotation(IAnnotation annotation) {
		if (annotations == null)
			annotations = new Annotations();
		annotations.set(annotation);
	}

	@Override
	public Property getProperty(String name) {
		if (properties == null)
			return null;
		return properties.get(name);
	}

	@Override
	public Property setProperty(Property property) {
		if (properties == null)
			properties = new LinkedHashMap<String, Property>();
		properties.put(property.getName(), property);
		return property;
	}

	@Override
	public void removeProperty(String name) {
		if (properties != null) {
			properties.remove(name);
		}
	}

	@Override
	public Set<String> getPropertyNames() {
		if (properties == null)
			properties = new LinkedHashMap<String, Property>();
		return properties.keySet();
	}

	@Override
	public boolean hasProperty(String name) {
		if (properties == null)
			return false;
		return properties.containsKey(name);
	}
}
