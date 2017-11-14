/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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

package net.sf.okapi.steps.common;

import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.simplifier.ResourceSimplifier;

/**
 * Converts events, i.e. splits the generic skeleton of a given event resource into parts to contain no references.
 * The skeleton parts are attached to newly created DOCUMENT_PART events.
 * Original references are converted either to skeleton parts or TEXT_UNIT events.
 * The sequence of DOCUMENT_PART and TEXT_UNIT events is packed into a single MULTI_EVENT event.
 * <p>
 * For text units, the step removes the skeleton of a text unit, creating document parts for the skeleton parts before and after
 * the content placeholder, and removes the remaining tu skeleton as holding the content placeholder as its only part.  
 */
@UsingParameters() // No parameters
public class ResourceSimplifierStep extends BasePipelineStep {
	
	private ResourceSimplifier simplifier;
	private LocaleId targetLocale;
	private String outputEncoding;
	
	@Override
	public String getDescription() {
		return "Simplify resources. Simplification algorythm is format-specific. " +
				"Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName() {
		return "Resource Simplifier";
	}
	
	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALES)
	public void setTargetLocales (List<LocaleId> targetLocales) {
		if (!Util.isEmpty(targetLocales)) this.targetLocale = targetLocales.get(0);
	}

	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_ENCODING)
	public void setOutputEncoding (String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}
	
	@Override
	public Event handleEvent(Event event) {
		switch (event.getEventType()) {
		case START_DOCUMENT:			
			simplifier = new ResourceSimplifier(targetLocale);
			simplifier.setOutputEncoding(outputEncoding);
			if (event.getStartDocument() == null) {
				throw new OkapiException("StartDocument resource not set.");
			}
			simplifier.setMultilingual(event.getStartDocument().isMultilingual());
			break;
			
		default:
			break;
		}
		
		if (simplifier != null)
			return simplifier.convert(event);
					
		// Original event
		return event;
	}
}
