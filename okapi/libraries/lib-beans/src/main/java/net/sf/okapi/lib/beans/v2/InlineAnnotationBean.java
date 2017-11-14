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

package net.sf.okapi.lib.beans.v2;

import net.sf.okapi.common.resource.InlineAnnotation;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.beans.TypeInfoBean;

public class InlineAnnotationBean extends TypeInfoBean {

	private String data;

	@Override
	protected Object createObject(IPersistenceSession session) {
		return super.createObject(session);
	}

	@Override
	protected void fromObject(Object o, IPersistenceSession session) {
		super.fromObject(o, session);
		
		if (o instanceof InlineAnnotation) {
			InlineAnnotation obj = (InlineAnnotation) o;
			data = obj.getData();
		}		
	}

	@Override
	protected void setObject(Object o, IPersistenceSession session) {
		super.setObject(o, session);
		
		if (o instanceof InlineAnnotation) {
			InlineAnnotation obj = (InlineAnnotation) o;
			obj.setData(data);
		}
	}
	
	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}
}
