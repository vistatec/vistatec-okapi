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

package net.sf.okapi.lib.beans.v1;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class CodeBean extends PersistenceBean<Code> {

	private String data;

	@Override
	protected Code createObject(IPersistenceSession session) {
		List<Code> codes = Code.stringToCodes(data);
		if (Util.isEmpty(codes)) return null;
		
		return codes.get(0);
	}

	@Override
	protected void fromObject(Code obj, IPersistenceSession session) {
		List<Code> codes = new ArrayList<Code>();
		codes.add(obj);
		data = Code.codesToString(codes);
	}

	@Override
	protected void setObject(Code destObj, IPersistenceSession session) {
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
