/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.v0;

import net.sf.okapi.common.resource.BaseReferenceable;

@Deprecated
public class BaseReferenceableBean extends BaseNameableBean {

	private int refCount;
	private String parentId;
	private IPersistenceSession session;
	
	@Override
	public <T> T get(Class<T> classRef) {		
		BaseReferenceable br = null;
		
		if (session == null)
			br = new BaseReferenceable();
		else
			br = session.convert(this, BaseReferenceable.class); // Get an object with superclass fields set
		
		// TODO Check if convert() sets br with this class fields
		
		return classRef.cast(br);
	}

	@Override
	public void init(IPersistenceSession session) {
		this.session = session;
	}

	@Override
	public IPersistenceBean set(Object obj) {
		super.set(obj);
		
		if (obj instanceof BaseReferenceable) {
			BaseReferenceable br = (BaseReferenceable) obj;
			
			refCount = br.getReferenceCount();
			parentId = br.getParentId();
		}
		
		return this;
	}

	public int getRefCount() {
		return refCount;
	}

	public void setRefCount(int refCount) {
		this.refCount = refCount;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

}
