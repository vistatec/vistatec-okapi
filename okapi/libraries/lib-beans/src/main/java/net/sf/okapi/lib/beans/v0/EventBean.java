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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IResource;

@Deprecated
public class EventBean implements IPersistenceBean {

	private EventType type;
	private FactoryBean resource = new FactoryBean();
	
	@Override
	public void init(IPersistenceSession session) {
	}

	@Override
	public <T> T get(Class<T> classRef) {
		Event event = new Event(type);		
		event.setResource(resource.get(IResource.class));
		return classRef.cast(event);
	}

	@Override
	public IPersistenceBean set(Object obj) {
		if (obj instanceof Event) {
			Event e = (Event) obj;
			
			type = e.getEventType();
			resource.set(e.getResource());
		}		
		return this;
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
