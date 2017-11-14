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

/**
 * Implements a nameable resource that can be a referent. 
 */
public class BaseReferenceable extends BaseNameable implements IReferenceable {

	protected int refCount;
	protected String parentId;
	
	public boolean isReferent () {
		return (refCount > 0);
	}

	public void setIsReferent (boolean value) {
		refCount = (value ? 1 : 0);
	}

	public int getReferenceCount () {
		return refCount;
	}
	
	public void setReferenceCount (int value) {
		refCount = value;
	}
	
	/**
	 * Gets the identifier of the parent resource of this resource.
	 * @return the identifier of this resource's parent, or null if there is none.
	 */
	public String getParentId () {
		return parentId;
	}
	
	/**
	 * Sets the identifier of the parent resource of this resource.
	 * @param id the identifier to set.
	 */
	public void setParentId (String id) {
		parentId = id;
	}

}
