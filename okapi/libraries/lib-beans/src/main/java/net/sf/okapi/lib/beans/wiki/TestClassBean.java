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

package net.sf.okapi.lib.beans.wiki;

import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class TestClassBean extends PersistenceBean<TestClass> {

	private int data;
	private boolean ready;
	
	@Override
	protected TestClass createObject(IPersistenceSession session) {
		return new TestClass(data);
	}

	@Override
	protected void fromObject(TestClass obj, IPersistenceSession session) {
		data = obj.getData();
		ready = obj.ready;
	}

	@Override
	protected void setObject(TestClass obj, IPersistenceSession session) {
		obj.ready = ready;
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}
}
