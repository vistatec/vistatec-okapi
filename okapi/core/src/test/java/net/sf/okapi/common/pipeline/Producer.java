/*===========================================================================
  Copyright (C) 2008 Jim Hargrave
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

package net.sf.okapi.common.pipeline;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Producer extends BasePipelineStep {	
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private int eventCount = -1;

	public String getName() {
		return "Producer";
	}

	public String getDescription() {
		return "Description";
	}

	@Override
	protected Event handleEndBatchItem (Event event) {		
		LOGGER.trace(getName() + " end-batch-item");
		return event;
	}

	@Override
	protected Event handleStartBatchItem (Event event) {		
		LOGGER.trace(getName() + " start-batch-item");
		return event;
	}
	
	@Override
	public Event handleEvent(Event event) {			
		event = new Event(EventType.TEXT_UNIT, null);		
		return event;
	}

	public boolean isDone () {
		eventCount++;
		if (eventCount >= 10) {					
			return true;
		}
		return false;
	}
}
