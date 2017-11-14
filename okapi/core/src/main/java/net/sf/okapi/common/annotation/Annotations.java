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

package net.sf.okapi.common.annotation;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides annotation mechanism to the resources.
 */
public class Annotations implements Iterable<IAnnotation> {
	
	private ConcurrentHashMap<Class<? extends IAnnotation>, IAnnotation> annotations;

	/**
	 * Creates a new Annotations object.
	 */
	public Annotations () {
		annotations = new ConcurrentHashMap<Class<? extends IAnnotation>, IAnnotation>();
	}
	
	/**
	 * Sets an annotation.
	 * @param <T> the annotation type.
	 * @param annotation The annotation object to set.
	 */
	public <T extends IAnnotation> void set (T annotation) {
		if (annotation != null)
			annotations.put(annotation.getClass(), annotation);
	}
	
	/**
	 * Gets the annotation for a given type.
	 * @param <A> the annotation type
	 * @param annotationType Type of the annotation to retrieve.
	 * @return The found annotation, or null if no annotation of the given type was found. 
	 */
	public <A extends IAnnotation> A get (Class<A> annotationType) {
		return annotationType.cast(annotations.get(annotationType) );
	}

	/**
	 * Removes all the annotations in this object.
	 */
	public void clear () {
		annotations.clear();
	}
	
	/**
	 * Removes the annotation of a given type.
	 * @param <A> the annotation type
	 * @param annotationType Type of the annotation to remove.
	 * @return The removed annotation, or null if no annotation of the given type was found. 
	 */
	public <A extends IAnnotation> A remove (Class<A> annotationType) {
		
		return annotationType.cast(annotations.remove(annotationType));
	}
	
	/**
	 * Clones this Annotations object.
	 * <p>Important: the annotations themselves are not cloned, only the map holding them is new.
	 * @return the cloned object.
	 */
	@Override
	public Annotations clone() {
		Annotations anns = new Annotations();
		for ( IAnnotation ann : annotations.values() ) {
			anns.set(ann);
		}
		return anns;
	}
	
//	/**
//	 * Used by clone method to copy over all annotations at once. 
//	 * @param annotations
//	 */
//	protected void setAnnotations(ConcurrentHashMap<Class<? extends IAnnotation>, IAnnotation> annotations) {
//		this.annotations = annotations;
//	}

	@Override
	public Iterator<IAnnotation> iterator() {
		return annotations.values().iterator();
	}
	
}
