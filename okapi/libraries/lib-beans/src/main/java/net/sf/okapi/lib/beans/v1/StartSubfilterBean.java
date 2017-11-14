/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class StartSubfilterBean extends StartGroupBean {

	private StartDocumentBean startDoc = new StartDocumentBean();
	private FactoryBean parentEncoder = new FactoryBean();
	
	@Override
	protected StartSubfilter createObject(IPersistenceSession session) {
		return new StartSubfilter(getId(), startDoc.get(StartDocument.class, session),
				parentEncoder.get(IEncoder.class, session));
	}

	@Override
	protected void setObject(BaseNameable obj, IPersistenceSession session) {
		super.setObject(obj, session);
	}

	@Override
	protected void fromObject(BaseNameable obj, IPersistenceSession session) {
		super.fromObject(obj, session);
		
		if (obj instanceof StartSubfilter) {
			StartSubfilter ssf = (StartSubfilter) obj;			
			startDoc.set(ssf.getStartDoc(), session);
			parentEncoder.set(((StartSubfilter) obj).getParentEncoder(), session);
		}
	}

	public StartDocumentBean getStartDoc() {
		return startDoc;
	}

	public void setStartDoc(StartDocumentBean startDoc) {
		this.startDoc = startDoc;
	}

	public FactoryBean getParentEncoder() {
		return parentEncoder;
	}

	public void setParentEncoder(FactoryBean parentEncoder) {
		this.parentEncoder = parentEncoder;
	}
}
