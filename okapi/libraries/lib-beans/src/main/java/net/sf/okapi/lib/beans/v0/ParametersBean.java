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

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.IParameters;

@Deprecated
public class ParametersBean implements IPersistenceBean {

	private String data;
	
	@Override
	public <T> T get(Class<T> classRef) {
		IParameters params = null;
		try {
			params = (IParameters) ClassUtil.instantiateClass(classRef);
			params.fromString(data);
		} catch (InstantiationException e) {
			// TODO Handle exception
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Handle exception
			e.printStackTrace();
		}
		
		return classRef.cast(params);
	}

	@Override
	public void init(IPersistenceSession session) {
	}

	@Override
	public IPersistenceBean set(Object obj) {
		if (obj instanceof IParameters) {
			IParameters params = (IParameters) obj;
			data = params.toString();
		}
		return this;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
