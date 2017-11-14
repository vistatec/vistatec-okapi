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

import net.sf.okapi.common.ISkeleton;

/**
 * Resource associated with the filter events DOCUMENT_PART,
 */
public class DocumentPart extends BaseReferenceable {
	
	public DocumentPart() {
	}

	/**
	 * Creates a new DocumentPart object.
	 * @param id The ID of this resource.
	 * @param isReferent Indicates if this resource is a referent (i.e. is referred to
	 * by another resource) or not.
	 */
	public DocumentPart (String id,
		boolean isReferent)
	{
		this.id = id;
		this.refCount = (isReferent ? 1 : 0);
	}

	/**
	 * Creates a new DocumentPart object.
	 * @param id The ID of this resource.
	 * @param isReferent Indicates if this resource is a referent (i.e. is referred to
	 * by another resource) or not.
	 * @param skeleton The skeleton associated with this resource.
	 */
	public DocumentPart (String id,
		boolean isReferent,
		ISkeleton skeleton)
	{
		this.id = id;
		this.refCount = (isReferent ? 1 : 0);
		setSkeleton(skeleton);
	}

	@Override
	public String toString() {
		return getSkeleton().toString();
	}	
}
