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

package net.sf.okapi.common.pipelinedriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.pipeline.ICallableStep;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.pipeline.IWorkQueueStep;
import net.sf.okapi.common.pipeline.annotations.ConfigurationParameter;
import net.sf.okapi.common.pipeline.annotations.StepIntrospector;
import net.sf.okapi.common.resource.RawDocument;

/**
 * Share code between the different {@link IPipelineDriver} implementations
 */
public class PipelineDriverUtils {

	/**
	 * Called from {@link PipelineDriver}
	 * @param driver - {@link IPipelineDriver} implementation
	 * @param paramList - list of {@link ConfigurationParameter} for each step in the pipeline
	 * @param item - {@link IBatchItemContext} that has attributes
	 */
	public static void assignRuntimeParameters(final PipelineDriver driver, 
		final LinkedList<List<ConfigurationParameter>> paramList,
		final IBatchItemContext item)
	{
		for ( List<ConfigurationParameter> pList : paramList ) {
			assignRuntimeParameters(driver, null, item, pList);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void assignSingleRuntimeParameter(final PipelineDriver driver, final IPipelineStep currentStep,
			final IBatchItemContext item, final ConfigurationParameter p) 
					throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		RawDocument input = item.getRawDocument(0);
		Method method = p.getMethod();
		if (method == null)
			return;
		switch (p.getParameterType()) {
		case OUTPUT_URI:
			if (item.getOutputURI(0) != null)
				method.invoke(currentStep, item.getOutputURI(0));
			break;
		case TARGET_LOCALE:
			method.invoke(currentStep, input.getTargetLocale());
			break;
		case TARGET_LOCALES:
			method.invoke(currentStep, input.getTargetLocales());
			break;
		case SOURCE_LOCALE:
			method.invoke(currentStep, input.getSourceLocale());
			break;
		case OUTPUT_ENCODING:
			method.invoke(currentStep, item.getOutputEncoding(0));
			break;
		case INPUT_URI:
			method.invoke(currentStep, input.getInputURI());
			break;
		case FILTER_CONFIGURATION_ID:
			method.invoke(currentStep, input.getFilterConfigId());
			break;
		case FILTER_CONFIGURATION_MAPPER:
			method.invoke(currentStep, driver.getFcMapper());
			break;
		case INPUT_RAWDOC:
			method.invoke(currentStep, input);
			break;
		case SECOND_INPUT_RAWDOC:
			method.invoke(currentStep, item.getRawDocument(1));
			break;
		case THIRD_INPUT_RAWDOC:
			method.invoke(currentStep, item.getRawDocument(2));
			break;
		case ROOT_DIRECTORY:
			method.invoke(currentStep, (driver.getRootDir() == null) ? "" : driver.getRootDir());
			break;
		case INPUT_ROOT_DIRECTORY:
			method.invoke(currentStep, (driver.getInputRootDir() == null) ? "" : driver.getInputRootDir());
			break;
		case OUTPUT_DIRECTORY:
			method.invoke(currentStep, (driver.getOutputDir() == null) ? "" : driver.getOutputDir());
			break;
		case UI_PARENT:
			method.invoke(currentStep, driver.getUiParent());
			break;
		case BATCH_INPUT_COUNT:
			method.invoke(currentStep, driver.getBatchItems().size());
			break;
		case EXECUTION_CONTEXT:
			method.invoke(currentStep, driver.getContext());
			break;
		default:
			throw new OkapiBadStepInputException(
					String.format(
							"The step '%s' is using a runtime parameter not supported by this driver.",
							currentStep.getName()));
		}
	}
	
	/**
	 * Called from WorkQueuePipelineDriver
	 * @param driver - {@link IPipelineDriver} implementation
	 * @param step - {@link IPipelineStep} to initialize
	 * @param item - {@link IBatchItemContext} that has attributes
	 * @param pList - list of {@link ConfigurationParameter}
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void assignRuntimeParameters(final PipelineDriver driver, final IPipelineStep step,
			final IBatchItemContext item, final List<ConfigurationParameter> pList) {
		try {
			// Set the runtime parameters using the method annotations for each
			// exposed parameter
			List<ConfigurationParameter> stepParams = pList;
			IPipelineStep currentStep = step;
			if (step != null) {
				if (currentStep instanceof IWorkQueueStep) {				
					stepParams = StepIntrospector.getStepParameters(((IWorkQueueStep)currentStep).getMainStep());
				} else {
					stepParams = StepIntrospector.getStepParameters(step);
				}
				currentStep = step;
			} else {
				if (pList.size() > 0) {
					currentStep = pList.get(0).getStep();
				}
			}
			
			for (ConfigurationParameter p : stepParams) {		
				if (currentStep instanceof IWorkQueueStep) {					
					IPipelineStep s = ((IWorkQueueStep)currentStep).getMainStep();	
					assignSingleRuntimeParameter(driver, s, item, p);
					List<ICallableStep> cSteps = ((IWorkQueueStep)currentStep).getCallableSteps();
					for (ICallableStep cs : cSteps) {
						assignSingleRuntimeParameter(driver, cs.getMainStep(), item, p);
					}
					continue;
				}
				assignSingleRuntimeParameter(driver, currentStep, item, p);
			}
		} catch (IllegalArgumentException e) {
			throw new OkapiException("Error when assigning runtime parameters.", e);
		} catch (IllegalAccessException e) {
			throw new OkapiException("Error when assigning runtime parameters.", e);
		} catch (InvocationTargetException e) {
			throw new OkapiException("Error when assigning runtime parameters.", e);
		}
	}
}
