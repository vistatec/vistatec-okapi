/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.extra.pipelinebuilder;

import java.net.URL;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ParametersString;
import net.sf.okapi.common.pipeline.IPipelineStep;

public class XPipelineStep implements IPipelineStep{

	private IPipelineStep step;
	private ParametersString parametersString = new ParametersString(); 
	
	public XPipelineStep(IPipelineStep step, IParameters parameters) {
		this(step);
		step.setParameters(parameters);
	}

	@Deprecated
	public XPipelineStep(IPipelineStep step) {
		this.step = step;
	}
	
	public XPipelineStep(IPipelineStep step, XParameter... parameters) {
		this(step);
		
		if (step == null) return;
		IParameters params = step.getParameters();
		if (params != null)
			parametersString.fromString(params.toString());
		for (XParameter parameter : parameters) {
			if (parameter.getType() == null) {
				Object value = parameter.getValue();
				
				if (value instanceof Integer)
					parametersString.setParameter(parameter.getName(), Integer.class.cast(value));
				
				else if (value instanceof Boolean)
					parametersString.setParameter(parameter.getName(), Boolean.class.cast(value));
				
				else if (value instanceof String) {
					if (parameter.isAsGroup())
						parametersString.setGroup(parameter.getName(), String.class.cast(value));
					else
						parametersString.setParameter(parameter.getName(), String.class.cast(value));
				}					
			}
			else
				switch (parameter.getType()) {
				case OUTPUT_URI:
					
				}
			
		}
		
		if (params != null)
			params.fromString(parametersString.toString());
	}
	
	public XPipelineStep(Class<? extends IPipelineStep> stepClass, IParameters parameters) {
		step = instantiateStep(stepClass);
		step.setParameters(parameters);
	}
	
	public XPipelineStep(Class<? extends IPipelineStep> stepClass, XParameter... parameters) {
		this(instantiateStep(stepClass), parameters);
	}
	
	private static IPipelineStep instantiateStep(Class<? extends IPipelineStep> stepClass) {
		IPipelineStep res = null;
		
		try {
			res = ClassUtil.instantiateClass(stepClass);
			
		} catch (InstantiationException e) {
			// TODO Handle exception

		} catch (IllegalAccessException e) {			
			// TODO Handle exception
		}	
		return res;
	}

	public XPipelineStep(IPipelineStep step, URL parametersURL, boolean ignoreErrors) {
		this.step = step;
		IParameters params = step.getParameters();
		params.load(parametersURL, ignoreErrors);
	}
	
	public XPipelineStep(Class<? extends IPipelineStep> stepClass, URL parametersURL, boolean ignoreErrors) {
		this.step = instantiateStep(stepClass);
		IParameters params = step.getParameters();
		params.load(parametersURL, ignoreErrors);
	}

	public String getDescription() {
		return step.getDescription();
	}

	public String getName() {
		return step.getName();
	}

	public void destroy() {
		step.destroy();
	}

	public String getHelpLocation() {
		return step.getHelpLocation();
	}

	public IParameters getParameters() {
		return step.getParameters();
	}

	public Event handleEvent(Event event) {
		return step.handleEvent(event);
	}

	public boolean isDone() {
		return step.isDone();
	}

	public boolean isLastOutputStep() {	
		return step.isLastOutputStep();
	}

	public void setLastOutputStep(boolean isLastStep) {		
		step.setLastOutputStep(isLastStep);
	}

	public void setParameters(IParameters params) {		
		step.setParameters(params);
	}

	public IPipelineStep getStep() {
		return step;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		// implement cancel
	}	
}
