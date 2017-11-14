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

package net.sf.okapi.lib.beans.v0;

import net.sf.okapi.common.EventType;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.ReferenceBean;

@Deprecated
public class TestEventBean extends PersistenceBean<TestEvent> {
	private String id;
	private EventType type;
	private ReferenceBean parent = new ReferenceBean();
	
	@Override
	protected TestEvent createObject(IPersistenceSession session) {
		return new TestEvent(id);
	}

	@Override
	protected void fromObject(TestEvent obj, IPersistenceSession session) {		
		id = obj.getId();
		parent.set(obj.getParent(), session);
	}

	@Override
	protected void setObject(TestEvent obj, IPersistenceSession session) {
		obj.setId(id);
		obj.setParent(parent.get(TestEvent.class, session));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ReferenceBean getParent() {
		return parent;
	}

	public void setParent(ReferenceBean parent) {
		this.parent = parent;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public EventType getType() {
		return type;
	}

}
