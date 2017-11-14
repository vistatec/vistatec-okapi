/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.filters.IFilter;

/**
 * Common set of methods for a step within a {@link IPipeline} pipeline.
 */
public interface IPipelineStep {

	/**
	 * Gets the current parameters for this step.
	 * 
	 * @return the current parameters for this step or null if there are no
	 *         parameters.
	 */
	public IParameters getParameters();

	/**
	 * Sets new parameters for this step.
	 * 
	 * @param params
	 *            the new parameters to use.
	 */
	public void setParameters(IParameters params);

	/**
	 * Gets the localizable name of this step.
	 * 
	 * @return the localizable name of this step.
	 */
	public String getName();

	/**
	 * Gets a short localizable description of what this step does.
	 * 
	 * @return the text of a short description of what this step does.
	 */
	public String getDescription();

	/**
	 * Gets the relative directory location for the help of this step. The main help file
	 * for the step must be at that location and its name must be the name of the class
	 * implementing the step in lowercase with a .html extension.
	 * @return the relative directory location for the help of this step.
	 */
	public String getHelpLocation();

	/**
	 * Processes each event sent though the pipeline.
	 * 
	 * @param event
	 *            the event to process.
	 * @return the event to pass down the pipeline.
	 */
	public Event handleEvent(Event event);

	/**
	 * Steps that can generate {@link Event}s such as {@link IFilter}s return
	 * false until no more events can be created.
	 * Steps which do not create {@link Event}s always return true.
	 * 
	 * @return false if can generate more events, true otherwise.
	 */
	public boolean isDone();

	/**
	 * Executes any cleanup code for this step. Called once at the end of the
	 * pipeline lifecycle.
	 */
	public void destroy();
	
	/**
	 * Cancel processing on this pipeline. Each {@link IPipelineStep} is responsible 
	 * to implement a cancel method that will interrupt long running operations
	 */
	public void cancel();

	/**
	 * Is this step the last step with output in the pipeline?
	 * 
	 * @return true if last step with output, false otherwise.
	 */
	public boolean isLastOutputStep();

	/**
	 * Tell the step if it is the last one on the pipeline with output.
	 * 
	 * @param isLastStep true if last step with output, false otherwise.
	 */
	public void setLastOutputStep(boolean isLastStep);

}
