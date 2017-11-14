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

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class StartSubDocumentBean extends BaseNameableBean {

	private String parentId;
	private FactoryBean filterParams = new FactoryBean();

	@Override
	protected BaseNameable createObject(IPersistenceSession session) {
		return new StartSubDocument(parentId);
	}

	@Override
	protected void fromObject(BaseNameable obj, IPersistenceSession session) {
		super.fromObject(obj, session);
		
		if (obj instanceof StartSubDocument) {
			StartSubDocument ssd = (StartSubDocument) obj;
			
			parentId = ssd.getParentId();
			filterParams.set(ssd.getFilterParameters(), session);
		}
	}

	@Override
	protected void setObject(BaseNameable obj, IPersistenceSession session) {
		super.setObject(obj, session);
		
		if (obj instanceof StartSubDocument) {
			StartSubDocument ssd = (StartSubDocument) obj;
			
			ssd.setParentId(parentId);
			ssd.setFilterParameters(filterParams.get(IParameters.class, session));
		}
	}
	
	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public FactoryBean getFilterParams() {
		return filterParams;
	}

	public void setFilterParams(FactoryBean filterParams) {
		this.filterParams = filterParams;
	}
}
