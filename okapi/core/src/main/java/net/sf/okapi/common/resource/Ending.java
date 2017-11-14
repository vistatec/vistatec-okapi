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

package net.sf.okapi.common.resource;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;

/**
 * Resource associated with the filter events END_DOCUMENT,
 * END_SUBDOCUMENT, and END_GROUP.
 */
public class Ending implements IResource, IWithAnnotations {

	protected String id;
	protected ISkeleton skeleton;
	protected Annotations annotations;

	public Ending() {
	}
	
	/**
	 * Creates a new Ending object.
	 * @param id The ID of this resource (It should NOT be the same ID as the one set of 
	 * the corresponding starting resource: each resource has a ID are unique).
	 */
	public Ending (String id) {
		annotations = new Annotations();
		this.id = id;
	}

	@Override
	public String getId () {
		return id;
	}
	
	@Override
	public void setId (String id) {
		this.id = id;
	}

	@Override
	public ISkeleton getSkeleton () {
		return skeleton;
	}
	
	@Override
	public void setSkeleton (ISkeleton skeleton) {
		this.skeleton = skeleton;
		if (skeleton != null) skeleton.setParent(this);
	}

	@Override
	public <A extends IAnnotation> A getAnnotation (Class<A> annotationType) {
		if ( annotations == null ) return null;
		return annotationType.cast(annotations.get(annotationType) );
	}
	
	@Override
	public void setAnnotation (IAnnotation annotation) {
		annotations.set(annotation);
	}

	@Override
	public Annotations getAnnotations() {
		return (annotations == null) ? new Annotations() : annotations;
	}

	protected void setAnnotations(Annotations annotations) {
		this.annotations = annotations;
	}

	@Override
	public String toString() {
		return skeleton == null ? super.toString() : skeleton.toString();
	}
}
