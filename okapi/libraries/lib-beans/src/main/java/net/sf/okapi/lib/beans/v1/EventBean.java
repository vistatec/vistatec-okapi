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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IResource;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class EventBean extends PersistenceBean<Event> {

	private EventType type;
	private FactoryBean resource = new FactoryBean();

	@Override
	protected Event createObject(IPersistenceSession session) {
		return new Event(type);
	}

	@Override
	protected void fromObject(Event obj, IPersistenceSession session) {
		type = obj.getEventType();
		resource.set(obj.getResource(), session);
	}

	@Override
	protected void setObject(Event obj, IPersistenceSession session) {
		obj.setResource(resource.get(IResource.class, session));		
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public EventType getType() {
		return type;
	}

	public void setResource(FactoryBean resource) {
		this.resource = resource;
	}

	public FactoryBean getResource() {
		return resource;
	}
}
