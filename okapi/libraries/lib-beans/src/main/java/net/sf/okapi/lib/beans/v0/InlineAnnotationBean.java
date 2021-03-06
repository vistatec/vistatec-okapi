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

import net.sf.okapi.common.resource.InlineAnnotation;

@Deprecated
public class InlineAnnotationBean implements IPersistenceBean {

	private String data;
	
	@Override
	public void init(IPersistenceSession session) {
	}

	@Override
	public <T> T get(Class<T> classRef) {
		InlineAnnotation ann = new InlineAnnotation();
		ann.setData(data);
		return classRef.cast(ann);
	}

	@Override
	public IPersistenceBean set(Object obj) {
		if (obj instanceof InlineAnnotation) {
			InlineAnnotation ann = (InlineAnnotation) obj;
			data = ann.getData();
		}
		return this;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}

}
