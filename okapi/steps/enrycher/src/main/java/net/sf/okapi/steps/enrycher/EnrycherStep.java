/*===========================================================================
  Copyright (C) 2012-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.enrycher;

import java.util.LinkedList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.MultiEvent;

@UsingParameters(Parameters.class)
public class EnrycherStep extends BasePipelineStep {

	private LinkedList<Event> events;
	private int maxEvents;
	private boolean needReset;
	private EnrycherClient client;

	public EnrycherStep () {
		client = new EnrycherClient();
	}
	
	private void closeAndClean () {
		if ( events != null ) {
			events.clear();
			events = null;
		}
	}
	
	@Override
	public String getName () {
		return "Enrycher";
	}

	@Override
	public String getDescription () {
		return "Applies Enrycher ITS annotations to the source content. "
			+ "Expects: filter events. Sends back: filter events.";
	}

	@Override
	public IParameters getParameters () {
		return client.getParameters();
	}

	@Override
	public void setParameters (IParameters params) {
		client.setParameters((Parameters)params);
	}

	@Override
	protected Event handleStartBatch (Event event) {
		events = new LinkedList<Event>();
		maxEvents = ((Parameters)client.getParameters()).getMaxEvents();
		if (( maxEvents < 1 ) || ( maxEvents > 1000 )) maxEvents = 20;
		return event;
	}
	
	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_BATCH:
			return handleStartBatch(event);
		// Events to store until the next trigger
		case TEXT_UNIT:
			// Store and possibly trigger
			return storeAndPossiblyProcess(event, false);			
		case DOCUMENT_PART:
		case START_GROUP:
		case END_GROUP:
		case START_SUBFILTER:
		case END_SUBFILTER:
		case PIPELINE_PARAMETERS:
			// Store and possibly trigger
			return storeAndPossiblyProcess(event, false);
		// Events that force the trigger if needed
		case CUSTOM:
		case MULTI_EVENT:
		case START_SUBDOCUMENT: // Could have text units between start document and sub-document
		case END_DOCUMENT:
		case END_SUBDOCUMENT:
			return storeAndPossiblyProcess(event, true);
		// Events that should clean up
		case CANCELED:
		case END_BATCH:
			closeAndClean();
			break;
			// Events before any storing or after triggers	
		case START_BATCH_ITEM:
		case END_BATCH_ITEM:
		case RAW_DOCUMENT:
		case START_DOCUMENT:
			break; // Do nothing special
		case NO_OP:
			break;
		default:
			break;
		}
		return event;
	}
	
	private Event processEvents () {
		// Call the service on the stored events
		callService();
		// Process is done
		// Now we sent all the stored events down the pipeline
		needReset = true; // To reset the list next time around
		return new Event(EventType.MULTI_EVENT, new MultiEvent(events));
	}
	
	private Event storeAndPossiblyProcess (Event event,
		boolean mustProcess)
	{
		// Reset if needed
		if ( needReset ) {
			needReset = false;
			events.clear();
		}
		// Add the event
		events.add(event);
		// And trigger the process if needed
		if ( mustProcess || ( events.size() >= maxEvents )) {
			return processEvents();
		}
		// Else, if we just store this event, we pass a no-operation event down for now
		return Event.NOOP_EVENT;
	}
	
	private void callService () {
		if ( events.isEmpty() ) return; // Nothing to do
		// Gather the text units to process
		LinkedList<ITextUnit> list = new LinkedList<ITextUnit>();
		for ( Event event : events ) {
			if ( !event.isTextUnit() ) continue;
			list.add(event.getTextUnit());
		}
		// And process them
		client.processList(list);
	}

}
