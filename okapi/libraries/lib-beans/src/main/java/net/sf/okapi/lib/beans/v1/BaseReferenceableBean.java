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

package net.sf.okapi.lib.beans.v1;

import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.BaseReferenceable;
import net.sf.okapi.lib.persistence.IPersistenceSession;

public class BaseReferenceableBean extends BaseNameableBean {

	private int refCount;
	private String parentId;
	private boolean isReferent;

	@Override
	protected BaseNameable createObject(IPersistenceSession session) {
		return new BaseReferenceable();
	}

	@Override
	protected void fromObject(BaseNameable obj, IPersistenceSession session) {
		super.fromObject(obj, session);
		
		if (obj instanceof BaseReferenceable) {
			BaseReferenceable br = (BaseReferenceable) obj;
			
			refCount = br.getReferenceCount();
			parentId = br.getParentId();
			isReferent = br.isReferent();
		}
	}

	@Override
	protected void setObject(BaseNameable obj, IPersistenceSession session) {
		super.setObject(obj, session);
		
		if (obj instanceof BaseReferenceable) {						
			
			BaseReferenceable br = (BaseReferenceable) obj;
			br.setReferenceCount(refCount);
			br.setParentId(parentId);
			br.setIsReferent(isReferent);
		}
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

	public void setReferent(boolean isReferent) {
		this.isReferent = isReferent;
	}

	public boolean isReferent() {
		return isReferent;
	}
}
