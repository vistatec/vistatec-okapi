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

package net.sf.okapi.lib.persistence.beans;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.lib.persistence.IPersistenceBean;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class ReferenceBean extends PersistenceBean<Object> {

	private long reference;
	private String className;

	@Override
	protected Object createObject(IPersistenceSession session) {
		Object obj = session.getObject(reference);
		if (obj == null) {
			IPersistenceBean<?> proxy = session.getProxy(className);
			if (proxy != null) {
				// Create an object and put to cache so getObject() can find it from PersistenceBean.get()
				obj = proxy.get(session.getClass(className), session);				
			}		
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fromObject(Object obj, IPersistenceSession session) {
		if (obj == null) return;
		
		this.className = ClassUtil.getQualifiedClassName(obj);
		session.setRefIdForObject(this, this.getRefId()); // To find the ref parent's root
		
		long rid = session.getRefIdForObject(obj);
		if (rid != 0) {
			this.reference = rid;
			//session.setRefIdForObject(this, this.getRefId()); // To find the ref parent's root
			session.setReference(this.getRefId(), rid);
			return;
		}
		
		IPersistenceBean<Object> bean =
				(obj instanceof IPersistenceBean) ?
						(IPersistenceBean<Object>) obj :	
						(IPersistenceBean<Object>) session.createBean(ClassUtil.getClass(obj));
		session.cacheBean(obj, bean); // for a FactoryBean or PersistenceSession.serialize() to hook up later
		reference = bean.getRefId();
		session.setRefIdForObject(obj, reference);
		session.setReference(this.getRefId(), reference);
	}

	@Override
	protected void setObject(Object obj, IPersistenceSession session) {
		if (obj != null)
			session.setRefIdForObject(obj, reference);
	}

	public void setReference(long reference) {
		this.reference = reference;
	}

	public long getReference() {
		return reference;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}
}
