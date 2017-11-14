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

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class MultiEventBean extends PersistenceBean<MultiEvent> {
	private AnnotationsBean annotations = new AnnotationsBean();
	private String id;
	private boolean propagateAsSingleEvent = false;
	private List<EventBean> events = new ArrayList<EventBean>();

	@Override
	protected MultiEvent createObject(IPersistenceSession session) {
		return new MultiEvent();
	}

	@Override
	protected void fromObject(MultiEvent obj, IPersistenceSession session) {
		annotations.set(obj.getAnnotations(), session);
		
		id = obj.getId();
		propagateAsSingleEvent = obj.isPropagateAsSingleEvent();
		
		for (Event event : obj) {
			EventBean eventBean = new EventBean();
			events.add(eventBean);
			eventBean.set(event, session);
		}
	}

	@Override
	protected void setObject(MultiEvent obj, IPersistenceSession session) {
		for (FactoryBean annotationBean : annotations.getItems())
			obj.setAnnotation(annotationBean.get(IAnnotation.class, session));

		obj.setId(id);
		obj.setPropagateAsSingleEvent(propagateAsSingleEvent);
		
		for (EventBean eventBean : events)
			obj.addEvent(eventBean.get(Event.class, session));
	}

	public AnnotationsBean getAnnotations() {
		return annotations;
	}

	public void setAnnotations(AnnotationsBean annotations) {
		this.annotations = annotations;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isPropagateAsSingleEvent() {
		return propagateAsSingleEvent;
	}

	public void setPropagateAsSingleEvent(boolean propagateAsSingleEvent) {
		this.propagateAsSingleEvent = propagateAsSingleEvent;
	}

	public List<EventBean> getEvents() {
		return events;
	}

	public void setEvents(List<EventBean> events) {
		this.events = events;
	}
}
