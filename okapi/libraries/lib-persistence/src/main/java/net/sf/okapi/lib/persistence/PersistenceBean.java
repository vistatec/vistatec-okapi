/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.persistence;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.lib.persistence.beans.FactoryBean;
import net.sf.okapi.lib.persistence.beans.ReferenceBean;

public abstract class PersistenceBean<PutCoreClassHere> implements IPersistenceBean<PutCoreClassHere> {
	
	private long refId = 0;
	private boolean busy = false;

	protected abstract PutCoreClassHere createObject(IPersistenceSession session);
	protected abstract void setObject(PutCoreClassHere obj, IPersistenceSession session);
	protected abstract void fromObject(PutCoreClassHere obj, IPersistenceSession session);
	
	protected PersistenceBean() {
		super();
		refId = ReferenceResolver.generateRefId();
	}
	
	@Override
	public long getRefId() {
		return refId;
	}

	@Override
	public void setRefId(long refId) {
		this.refId = refId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Class<T> classRef, IPersistenceSession session) {
		if (busy) {
			throw new OkapiException(String.format("PersistenceBean: recursive get() in %s", 
					ClassUtil.getQualifiedClassName(this.getClass())));
		}
		
		// Try to get one created by ReferenceBean.createObject()
		PutCoreClassHere obj = (PutCoreClassHere) session.getObject(refId); 
		if (obj == null) {			
			busy = true; // recursion protection
			try {
				obj = createObject(session);
			}
			finally {
				busy = false;
			}			
		}					
		if (obj != null && refId != 0) { // not for proxies
			session.setRefIdForObject(obj, refId);		
			setObject(obj, session);
		}		
		return classRef.cast(obj);
	}
	
	@Override
	public IPersistenceBean<PutCoreClassHere> set(PutCoreClassHere obj, IPersistenceSession session) {
		if (obj == null) return this;
		
		boolean isRefBean = this instanceof ReferenceBean ||
				this instanceof FactoryBean;
		
		if (!isRefBean) {
			long rid = session.getRefIdForObject(obj);
			if (rid != 0 && rid != getRefId()) {
				setRefId(rid);
				// ? TODO Update bean caches for the new refId
			}
			else {
				session.cacheBean(obj, this);
				session.setRefIdForObject(obj, refId);
			}			
		}
		
		fromObject(obj, session);
		return this;
	}		
}
