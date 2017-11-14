/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

import java.util.concurrent.Callable;

import net.sf.okapi.common.Event;

/**
 * Step that implements the Callable interface and can be run concurrently.
 *
 * @param <T> - return type of call method, i.e. Event, SortableEvent
 */
public interface ICallableStep<T> extends IPipelineStep, Callable<T> {
	
	/**
	 * Gets the main step wrapped by ICallableStep
	 * @return the main step
	 */
	public IPipelineStep getMainStep();
	
	/**
	 * process the event now without threading. Normally used for initialization 
	 * events such as START_BATCH etc.
	 * @param event - event to be processed
	 * @return the resulting event
	 */
	public Event processNow(Event event);
}
