/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.resource.RawDocument;

/**
 * Common set of methods to drive an event-driven process. A pipeline is made of a chain of {@link IPipelineStep}
 * objects through which documents are processed.
 */
public interface IPipeline {

	/**
	 * Starts {@link IPipeline} processing with a {@link RawDocument} as input. This is a convenience method that calls
	 * {@link #process(Event)}.
	 * 
	 * @param input
	 *            the RawDocument to process.
	 */
	public void process(RawDocument input);

	/**
	 * Starts {@link IPipeline} processing with a {@link Event} as input.
	 * 
	 * @param input
	 *            event that primes the {@link IPipeline}
	 */
	public void  process(Event input);

	/**
	 * Gets the current pipeline state.
	 * 
	 * @return the current state of the pipeline.
	 */
	public PipelineReturnValue getState();

	/**
	 * Cancels processing on this pipeline.
	 */
	public void cancel();

	/**
	 * Adds a step to this pipeline. Steps are executed in the order they are added.
	 * 
	 * @param step
	 *            the step to add.
	 */
	public void addStep(IPipelineStep step);

	/**
	 * Gets the list of all steps in this pipeline.
	 * 
	 * @return a list of all steps in this pipeline, the list may be empty.
	 */
	public List<IPipelineStep> getSteps();

	/**
	 * Starts a batch of inputs.
	 */
	public void startBatch();

	/**
	 * Finishes a batch of inputs.
	 * 
	 */
	public void endBatch();

	/**
	 * Frees all resources from all steps in this pipeline.
	 */
	public void destroy();

	/**
	 * Remove all the {@link IPipelineStep}s from the pipeline. Also calls the destroy() method on each step.
	 */
	public void clearSteps();

	/**
	 * Set the pipelines identifier.
	 * @param id the new id of the pipeline.
	 */
	public void setId(String id);

	/**
	 * Get the Pipelines identifier.
	 * 
	 * @return String identifier
	 */
	public String getId();
}
