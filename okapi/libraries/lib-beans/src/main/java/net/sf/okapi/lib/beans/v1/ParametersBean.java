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

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class ParametersBean extends PersistenceBean<IParameters> {

	private String className;
	private String data;

	@Override
	protected IParameters createObject(IPersistenceSession session) {
		if (Util.isEmpty(className)) return null;
			
		IParameters obj = null;
		try {
			obj = (IParameters) ClassUtil.instantiateClass(className);			
		} catch (Exception e) {
			throw new OkapiException(String.format("ParametersBean: cannot instantiate %s", className), e);
		}
		return obj;
	}

	@Override
	protected void fromObject(IParameters obj, IPersistenceSession session) {
		className = ClassUtil.getQualifiedClassName(obj);
		data = obj.toString();
	}

	@Override
	protected void setObject(IParameters obj, IPersistenceSession session) {
		obj.fromString(data);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
