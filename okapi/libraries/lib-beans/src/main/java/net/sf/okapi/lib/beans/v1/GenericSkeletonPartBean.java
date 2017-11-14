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

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.ReferenceBean;

public class GenericSkeletonPartBean extends PersistenceBean<GenericSkeletonPart> {

	private String data;
	private ReferenceBean parent = new ReferenceBean();
	//private FactoryBean parent = new FactoryBean();
	private String locId;

	@Override
	protected GenericSkeletonPart createObject(IPersistenceSession session) {
		LocaleId localeId;
		if (locId == null) {
			localeId = null; 
		} else if ("".equals(locId)) {
			localeId = LocaleId.EMPTY;
		}
		else {
			localeId = LocaleId.fromString(locId);
		}			
		
		return new GenericSkeletonPart(data, parent.get(IResource.class, session), localeId);
	}

	@Override
	protected void fromObject(GenericSkeletonPart obj, IPersistenceSession session) {
		data = obj.toString();
		parent.set(obj.getParent(), session);
		LocaleId loc = obj.getLocale();
		if (loc != null)
			locId = loc.toString();			
	}

	@Override
	protected void setObject(GenericSkeletonPart obj, IPersistenceSession session) {
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setLocId(String locId) {
		this.locId = locId;
	}

	public String getLocId() {
		return locId;
	}

//	public FactoryBean getParent() {
//		return parent;
//	}
//
//	public void setParent(FactoryBean parent) {
//		this.parent = parent;
//	}

	public ReferenceBean getParent() {
		return parent;
	}

	public void setParent(ReferenceBean parent) {
		this.parent = parent;
	}
}
