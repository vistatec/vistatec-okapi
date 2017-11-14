/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.extra.steps;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.pipeline.annotations.ConfigurationParameter;
import net.sf.okapi.common.pipeline.annotations.StepIntrospector;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CompoundStep extends AbstractPipelineStep {

	protected LinkedList<IPipelineStep> steps = new LinkedList<IPipelineStep>();
	private LinkedList<List<ConfigurationParameter>> paramList;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected abstract void addStepsToList(List<IPipelineStep> list);
	
	public CompoundStep() {
		super();
		paramList = new LinkedList<List<ConfigurationParameter>>();
		addStepsToList(steps);
		for (IPipelineStep step : steps) {
			if (step == null) {
				logger.error("Attempt to add null for an internal step.");
				continue;
			}
			List<ConfigurationParameter> pList = null;
			if (step instanceof XPipelineStep)
				pList = StepIntrospector.getStepParameters(((XPipelineStep)step).getStep());
			else
				pList = StepIntrospector.getStepParameters(step);
			paramList.add(pList);
		}		
	}

	@Override
	protected void component_init() {
		// Stub not to implement in subclasses as would've been required otherwise
	}

	private Event expandEvent(Event event, IPipelineStep currentStep) {
		if (event.getEventType() == EventType.MULTI_EVENT
				&& !(((MultiEvent) event.getResource()).isPropagateAsSingleEvent())) {

			// add the remaining steps to a temp list - these are the steps that will receive the expanded
			// MULT_EVENTS
			List<IPipelineStep> remainingSteps = steps.subList(steps.indexOf(currentStep) + 1,
					steps.size());
			if (remainingSteps.size() == 0) return event;
				
			for (Event e : ((MultiEvent)event.getResource())) {
				// send the current event from MULTI_EVENT down the remaining steps in the pipeline
				for (IPipelineStep remainingStep : remainingSteps) {
					e = remainingStep.handleEvent(e);
					e = expandEvent(e, remainingStep);
				}					
			}
		}
		
		return event;
	}
	
	@Override
	public Event handleEvent(Event event) {		
		for (IPipelineStep step : steps) {
			event = step.handleEvent(event);
			// Recursively expand the event if needed
			event = expandEvent(event, step);
		}
		
		return super.handleEvent(event);
	}
	
	private void invokeParameterMethods(StepParameterType type, Object value) {
		for ( List<ConfigurationParameter> pList : paramList ) {
			// For each exposed parameter
			for ( ConfigurationParameter p : pList ) {
				Method method = p.getMethod();
				if ( method == null ) continue;
				if ( p.getParameterType() == type) {
					try {
						method.invoke(p.getStep(), value);
					} 
					catch ( IllegalArgumentException e ) {
						throw new OkapiException("Error when assigning runtime parameters.", e);
					}
					catch ( IllegalAccessException e ) {
						throw new OkapiException("Error when assigning runtime parameters.", e);
					}
					catch ( InvocationTargetException e ) {
						throw new OkapiException("Error when assigning runtime parameters.", e);
					}
				}
			}
		}
	}
	
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	@Override
	public void setTargetLocale (LocaleId targetLocale) {
		super.setTargetLocale(targetLocale);
		invokeParameterMethods(StepParameterType.TARGET_LOCALE, targetLocale);		
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_ENCODING)
	public void setOutputEncoding (String outputEncoding) {
		invokeParameterMethods(StepParameterType.OUTPUT_ENCODING, outputEncoding);
	}
	
	@StepParameterMapping(parameterType = StepParameterType.INPUT_URI)
	public void setInputUri (URI inputURI) {
		invokeParameterMethods(StepParameterType.INPUT_URI, inputURI);
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputUri (URI outputURI) {
		invokeParameterMethods(StepParameterType.OUTPUT_URI, outputURI);
	}
}
