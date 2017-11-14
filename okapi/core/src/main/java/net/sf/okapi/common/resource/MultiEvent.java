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

package net.sf.okapi.common.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.pipeline.Pipeline;

/**
 * Special resource that holds one or more events.
 */
public class MultiEvent implements IResource, IWithAnnotations, Iterable<Event> {

	private Annotations annotations;
	private String id;
	private boolean propagateAsSingleEvent = false;
	private List<Event> events;

	/**
	 * Creates a new empty MultiEvent object.
	 */
	public MultiEvent () {
		propagateAsSingleEvent = false;
		events = new ArrayList<Event>(100);
	}
	
	/**
	 * Creates a new MultiEvent object with a list of given events.
	 * @param events the list of initial events.
	 */
	public MultiEvent (List<Event> events) {
		propagateAsSingleEvent = false;
		this.events = events;
	}

	/**
	 * Adds an event to this object.
	 * @param event the event to add.
	 */
	public void addEvent (Event event) {
		if (!event.isNoop())
			events.add(event);
	}
	
	/**
	 * Inserts an event in this object at the specified position.
	 * @param event the event to insert.
	 * @param index index at which the event is to be inserted.
	 */
	public void addEvent (Event event, int index) {
		if (!event.isNoop())
			events.add(index, event);
	}

	@Override
	public <A extends IAnnotation> A getAnnotation(Class<A> annotationType) {
		if (annotations == null)
			return null;
		return annotationType.cast(annotations.get(annotationType));
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * Always throws an exception as there is never a skeleton associated to a RawDocument.
	 * 
	 * @return never returns.
	 * @throws OkapiNotImplementedException this method is not implemented.
	 */
	@Override
	public ISkeleton getSkeleton() {
		throw new OkapiNotImplementedException("MultiResource does not have a skeketon");
	}

	@Override
	public void setAnnotation(IAnnotation annotation) {
		if (annotations == null) {
			annotations = new Annotations();
		}
		annotations.set(annotation);
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * This method has no effect as there is never a skeleton for a Document.
	 * 
	 * @param skeleton the skeleton.
	 * @throws OkapiNotImplementedException this method is not implemented.
	 */
	@Override
	public void setSkeleton(ISkeleton skeleton) {
		throw new OkapiNotImplementedException("MultiResource does not have a skeketon");
	}

	/**
	 * Creates an iterator for the events in this resource.
	 */
	@Override
	public Iterator<Event> iterator() {
		return events.iterator();
	}

	/**
	 * Set Propagate As Single Event flag.
	 * @param propagateAsSingleEvent true if we want to propagate as single event.
	 */
	public void setPropagateAsSingleEvent(boolean propagateAsSingleEvent) {
		this.propagateAsSingleEvent = propagateAsSingleEvent;
	}

	/**
	 * Do we send this {@link Event} by itself or does the {@link Pipeline} break the individual Events and end them
	 * singly. Default is false - we send each Event singly.
	 * 
	 * @return true if we send the Event as-is, false to send the individual Events continued here.
	 */
	public boolean isPropagateAsSingleEvent () {
		return propagateAsSingleEvent;
	}

	public Iterable<IAnnotation> getAnnotations () {
		if ( annotations == null ) {
			return Collections.emptyList();
		}
		return annotations;
	}

	/**
	 * Returns the number of events in this object.
	 * @return number of events
	 */
	public int size() {
		return events.size();
	}
}
