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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Consumer extends BasePipelineStep {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	public String getName() {
		return "Consumer";
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
	protected Event handleTextUnit(Event event) {
		LOGGER.trace("EventType: " + event.getEventType().name());
		return event;
	}
	
	@Override
	protected Event handleRawDocument(Event event) {		
		LOGGER.trace("EventType: " + event.getEventType().name());
		return event;
	}

}
