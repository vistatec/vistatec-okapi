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

package net.sf.okapi.common.resource;

import net.sf.okapi.common.annotation.IAnnotation;

public interface IWithAnnotations {
	/**
	 * Gets the annotation object for a given class for this resource.
	 * @param <A> the type of the class.
	 * @param annotationType the class of the annotation object to retrieve.
	 * @return the annotation for the given class for this resource. 
	 */
	public <A extends IAnnotation> A getAnnotation(Class<A> annotationType);

	/**
	 * Sets an annotation object for this resource.
	 * @param annotation the annotation object to set.
	 */
	public void setAnnotation (IAnnotation annotation);

	/**
	 * Gets the iterable list of the annotations for this resource.
	 * @return the iterable list of the annotations for this resource.
	 */
	public Iterable<IAnnotation> getAnnotations ();
}
