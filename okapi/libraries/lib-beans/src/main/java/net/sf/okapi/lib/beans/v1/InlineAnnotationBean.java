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

import net.sf.okapi.common.resource.InlineAnnotation;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class InlineAnnotationBean extends PersistenceBean<InlineAnnotation> {

	private String data;

	@Override
	protected InlineAnnotation createObject(IPersistenceSession session) {
		return new InlineAnnotation(data);
	}

	@Override
	protected void fromObject(InlineAnnotation obj, IPersistenceSession session) {
		data = obj.getData();
	}

	@Override
	protected void setObject(InlineAnnotation obj, IPersistenceSession session) {
		obj.setData(data);
	}
	
	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}
}
