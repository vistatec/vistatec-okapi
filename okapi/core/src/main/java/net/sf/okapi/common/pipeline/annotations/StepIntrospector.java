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
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.pipeline.IPipelineStep;

/**
 * Scan a class for retrieving the different {@link ConfigurationParameter} it uses.
 */
public final class StepIntrospector {

	/**
	 * Gets the list of the {@link ConfigurationParameter} in a given step.
	 * @param step the step to introspect.
	 * @return the list of the {@link ConfigurationParameter}.
	 */
	public static List<ConfigurationParameter> getStepParameters (
		IPipelineStep step)
	{
		List<ConfigurationParameter> parameters = new LinkedList<ConfigurationParameter>();

		// get all the public member methods of the class or interface represented by this step object,
		// including those declared by the IPipelineStep interface and its implementation classes 
		Method[] methods = step.getClass().getMethods();

		// iterate over the fields and pull out the ones that have
		// StepConfigurationParameter or StepExternalParameter annotations
		for (Method m : methods) {
			if (Modifier.isPublic(m.getModifiers()) && m.isAnnotationPresent(StepParameterMapping.class)) {
				StepParameterMapping a = m
						.getAnnotation(StepParameterMapping.class);
				parameters.add(new ConfigurationParameter(m, a.parameterType(),
						step));
			}
		}
		return parameters;
	}

	/**
	 * Gets the list of the event handlers for a given step.
	 * @param step the step to introspect.
	 * @return the list of the event handlers.
	 */
	public static List<String> getStepEventHandlers (IPipelineStep step) {
		List<String> eventHandlers = new LinkedList<String>();
		Method[] methods = step.getClass().getDeclaredMethods();
		for (Method m : methods) {
			// only look at methods with a single Event
			// parameter and return type
			if (m.getName().startsWith("handle") //$NON-NLS-1$
					&& m.getParameterTypes().length == 1
					&& Arrays.asList(m.getParameterTypes()).contains(
							Event.class)) {
				eventHandlers.add(m.getName());
			}
		}

		if (eventHandlers.size() <= 0) {
			throw new OkapiBadStepInputException(
				"Steps must implement at least one handler method"); //$NON-NLS-1$
		}

		return eventHandlers;
	}
}
