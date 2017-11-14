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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;

/**
 * UNUSED class.
 */
public class Document implements IResource, IWithAnnotations, Iterable<IResource> {

	private Annotations annotations;
	private String id;
	private List<IResource> documentResources;
	
	public Document() {
		documentResources = new ArrayList<IResource>(100);
	}

	public void addResource(IResource resource) {
		documentResources.add(resource);
	}
	
	@Override
	public <A extends IAnnotation> A getAnnotation(Class<A> annotationType) {
		if (annotations == null)
			return null;
		return annotationType.cast(annotations.get(annotationType));
	}

	@Override
	public String getId() {
		return id;
	}
	
	/**
	 * Always throws an exception as there is never a skeleton associated to a RawDocument. 
	 * @return never returns.
	 * @throws OkapiNotImplementedException this method is not implemented.
	 */
	@Override
	public ISkeleton getSkeleton() {
		throw new OkapiNotImplementedException("The Document resource does not have skeketon");
	}

	@Override
	public void setAnnotation(IAnnotation annotation) {
		if (annotations == null) {
			annotations = new Annotations();
		}
		annotations.set(annotation);
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * This method has no effect as there is never a skeleton for a Document.
	 * @param skeleton the skeleton.
	 * @throws OkapiNotImplementedException this method is not implemented.
	 */
	@Override	
	public void setSkeleton(ISkeleton skeleton) {
		throw new OkapiNotImplementedException("Document has no skeleton");
	}

	@Override
	public Iterator<IResource> iterator() {
		return documentResources.iterator();
	}

	public Annotations getAnnotations() {
		return (annotations == null) ? new Annotations() : annotations;
	}	
}
