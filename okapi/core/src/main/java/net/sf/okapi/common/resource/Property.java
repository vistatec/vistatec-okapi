/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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

import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.annotation.XLIFFNoteAnnotation;

/**
 * Represents a read-only or a modifiable property associated with a resource.
 * For example the HREF attribute of the element A in HTML would be a property.
 * Note that translatable data (such as the text of an attribute ALT of an IMG element in HTML)
 * must be stored in {@link TextUnit} rather that Property.
 */
public class Property {
	
	public static final String ENCODING = "encoding";
	public static final String LANGUAGE = "language";
	public static final String APPROVED = "approved"; 
	
	/**
	 * The XLIFFilter now uses {@link XLIFFNoteAnnotation} to store notes and their attributes.
	 * <p>
	 * Property.NOTE is still used in other filters, but may be removed in the future and
	 * unified into a standard Note Annotation.
	 */
	@Deprecated
	public static final String NOTE = "note"; 
	/**
	 * @see #NOTE
	 */
	@Deprecated
	public static final String TRANSNOTE = "transNote";
	
	
	public static final String COORDINATES = "coordinates";
	public static final String STATE_QUALIFIER = "state-qualifier";
	public static final String STATE = "state";
	
	/**
	 * Helper property used only for placing back some ITS and other annotations.
	 */
	public static final String ITS_LQI = "locQualityIssuesRef";
	public static final String ITS_PROV = "provenanceRecordsRef";
	public static final String ITS_MTCONFIDENCE = "mtConfidence";
	public static final String XLIFF_TOOL = "tool";
	public static final String XLIFF_PHASE = "phase";

	/**
	 * Sizing properties, can be applied to StartGroup or ITextUnit objects.
	 */
	public static final String MAX_WIDTH = "maxwidth";
	public static final String MAX_HEIGHT = "maxheight";
	public static final String SIZE_UNIT = "size-unit";

	private String name;
	private String value;
	private boolean isReadOnly;
	
	@Deprecated
	protected Annotations annotations;
	
	public Property() {		
	}

	/**
	 * Creates a new property object with a name, a vale and its read-only flag.
	 * @param name the name of the property (case-sensitive).
	 * @param value the value of the property.
	 * @param isReadOnly true if the property cannot be modified using the filter, false if you
	 * can modify the value in the output document.
	 */
	public Property (String name, String value, boolean isReadOnly) {
		this.name = name;
		this.value = value;
		this.isReadOnly = isReadOnly;
	}
	
	/**
	 * Creates a new read-only property object with a name and a value.
	 * @param name the name of the property (case-sensitive)
	 * @param value the value of the property.
	 */
	public Property (String name, String value) {
		this(name, value, true);
	}
	
	/**
	 * Gets the string representation of this property. This is the same as its value.
	 * @return the value of the property.
	 */
	@Override
	public String toString () {
		return value;
	}
	
	/**
	 * Clones this property.
	 * @return a new property object that is a copy of this one.
	 */
	public Property clone () {
		Property prop = new Property(name, value, isReadOnly);
		//TODO: copy annotations?? prop.annotations = annotations.clone();
		return prop;
	}

	/**
	 * Gets the name of this property.
	 * @return the name of this property.
	 */
	public String getName () {
		return name;
	}
	
	/**
	 * Gets the value of this property.
	 * @return the value of this property.
	 */
	public String getValue () {
		return value;
	}

	/**
	 * Gets the boolean value of this property. Use this helper method to get a boolean from the 
	 * value of this property. The values "true" and "yes" (in any case) returns true, any other 
	 * value returns false. No verification is done to see if the value is really boolean or not.
	 * @return true is the property value is "true", "yes" (case-insensitive), false otherwise.
	 */
	public boolean getBoolean () {
		if ( value == null ) return false;
		return (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"));
	}
	
	/**
	 * Sets a new value for this property.
	 * @param value the new value to set.
	 */
	public void setValue (String value) {
		this.value = value;
	}
	
	/**
	 * Indicates if this property is read-only.
	 * <p>All property can be changed, but the ones flagged as read-only
	 * cannot be modified when re-writing the document from where they have been extracted.
	 * @return true if the property cannot be modified in the filter output, false if it can be modified.
	 */
	public boolean isReadOnly () {
		return isReadOnly;
	}
	
	/**
	 * Gets the annotation of a given type for this property.
	 * @param <A> the class type.
	 * @param type the type of annotation to retrieve.
	 * @return the annotation, or null if this property has no annotation of the requested type.
	 */
	@Deprecated
	public <A extends IAnnotation> A getAnnotation (Class<A> type) {
		if ( annotations == null ) return null;
		return annotations.get(type);
	}

	/**
	 * Sets an annotation for this property. 
	 * @param annotation the annotation object to set.
	 */
	@Deprecated
	public void setAnnotation (IAnnotation annotation) {
		if ( annotations == null ) annotations = new Annotations();
		annotations.set(annotation);
	}

	@Deprecated
	public Annotations getAnnotations() {
		return (annotations == null) ? new Annotations() : annotations;
	}

	// For serialization only
	protected void setName(String name) {
		this.name = name;
	}

	protected void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}
}
