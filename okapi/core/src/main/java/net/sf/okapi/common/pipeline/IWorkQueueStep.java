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

import java.util.LinkedList;

/**
 * Step that can process multiple events concurrently.
 * @param <T> - return type of {@link ICallableStep}, i.e. Event, SortableEvent
 */
public interface IWorkQueueStep<T> extends IPipelineStep {

	/**
	 * Gets the main step wrapped by IWorkQueueStep
	 * @return the main step used as template by {@link ICallableStep} steps
	 */
	public IPipelineStep getMainStep();	
	
	/**
	 * Gets the list of {@link ICallableStep} based on the main or template step.
	 * @return the {@link ICallableStep} steps
	 */
	public LinkedList<ICallableStep<T>> getCallableSteps();
	
	/** 
	 * Get the number of work queues defined for this step
	 * @return the number of work queue
	 */
	public int getWorkQueueCount();
	
	/**
	 * Used when the empty constructor is called
	 * @throws InstantiationException when we fail to create an instance..
	 * @throws IllegalAccessException when trying to create an instance but don't have the right.
	 */
	public void init() throws InstantiationException, IllegalAccessException;
	
	/**
	 * Sets the main (template for all callabale steps) pipeline step
	 * <b>MUST BE CALLED BEFORE init()</b>
	 * @param step - the main step
	 */
	public void setMainStep(IPipelineStep step);
	
	/**
	 * Set the work queue count
	 * <b>MUST BE CALLED BEFORE init()</b>
	 * @param workQueueCount - the number of step work queues
	 */
	public void setWorkQueueCount(int workQueueCount);
}
