/*===========================================================================
Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.common.pipeline.annotations;

import java.lang.reflect.Method;

import net.sf.okapi.common.pipeline.IPipelineStep;

/**
 * Java annotation item for runtime configuration parameters used in {@link IPipelineStep}.
 */
public class ConfigurationParameter {
	private Method method;
	private StepParameterType parameterType;
	private IPipelineStep step;

	/**
	 * Creates a new ConfigurationParameter object.
	 * @param method method for this parameter.
	 * @param parameterType type of parameter.
	 * @param step step where the parameter is set.
	 */
	ConfigurationParameter (Method method,
		StepParameterType parameterType,
		IPipelineStep step)
	{
		this.setMethod(method);
		this.setParameterType(parameterType);
		this.setStep(step);
	}

	/**
	 * Sets the method for this parameter.
	 * @param method the method for this parameter.
	 */
	public void setMethod (Method method) {
		this.method = method;
	}

	/**
	 * Gets the method for this parameter.
	 * @return the method for this parameter.
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * Sets the type of parameter for this parameter.
	 * @param parameterType the type of parameter for this parameter.
	 */
	public void setParameterType (StepParameterType parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * Gets the type of parameter for this parameter.
	 * @return the type of parameter for this parameter.
	 */
	public StepParameterType getParameterType () {
		return parameterType;
	}

	/**
	 * Sets the step for this parameter.
	 * @param step the step for this parameter.
	 */
	public void setStep (IPipelineStep step) {
		this.step = step;
	}

	/**
	 * Gets the step for this parameter.
	 * @return the step for this parameter.
	 */
	public IPipelineStep getStep() {
		return step;
	}
}
