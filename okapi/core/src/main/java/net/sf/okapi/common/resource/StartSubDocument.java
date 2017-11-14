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

import net.sf.okapi.common.IParameters;

/**
 * Resource associated with the filter event START_SUBDOCUMENT.
 */
public class StartSubDocument extends BaseNameable {

	private String parentId;
	protected IParameters params;
	
	public StartSubDocument() {
	}
	
	public StartSubDocument (String parentId) {
		super();
		this.parentId = parentId;
	}

	public StartSubDocument (String parentId,
		String id)
	{
		super();
		this.parentId = parentId;
		this.id = id;
	}

	public String getParentId () {
		return parentId;
	}
	
	public void setParentId (String parentId) {
		this.parentId = parentId;
	}
	
	/**
	 * Gets the current parameters for this sub-document.
	 * @return the object containing the parameters for this document.
	 */
	public IParameters getFilterParameters () {
		return params;
	}
	
	/**
	 * Sets the parameters for this sub-document.
	 * @param params the object containing the parameters for this document.
	 */
	public void setFilterParameters (IParameters params) {
		this.params = params;
	}

}
