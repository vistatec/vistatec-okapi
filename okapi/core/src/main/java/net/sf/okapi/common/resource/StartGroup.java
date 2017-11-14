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
 * Resource associated with the filter event START_GROUP.
 */
public class StartGroup extends BaseReferenceable {
	
	public StartGroup() {
	}

	/**
	 * Creates a new StartGroup object.
	 * @param parentId The identifier of the parent resource for this group.
	 */
	public StartGroup (String parentId) {
		this(parentId, null, false);
	}

	/**
	 * Creates a new startGroup object with the identifier of the group's parent
	 * and the group's identifier.
	 * @param parentId the identifier of the parent resource for this group.
	 * @param id the identifier of this group.
	 */
	public StartGroup (String parentId,
		String id)
	{
		this(parentId, id, false);
	}

	/**
	 * Creates a new startGroup object with the identifier of the group's parent,
	 * the group's identifier, and an indicator of wether this group is a referent or not.
	 * @param parentId the identifier of the parent resource for this group.
	 * @param id the identifier of this group.
	 * @param isReference true if this group is referred by another resource.
	 */
	public StartGroup (String parentId,
		String id,
		boolean isReference)
	{
		super();
		this.parentId = parentId;
		this.id = id;
		this.refCount = (isReference ? 1 : 0);
	}

}
