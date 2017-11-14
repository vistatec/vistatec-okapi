/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.common.createtarget;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;

@UsingParameters(Parameters.class)
public class CreateTargetStep extends BasePipelineStep {

	private Parameters params;
	private LocaleId targetLocale;

	public CreateTargetStep() {
		params = new Parameters();
	}

	public String getDescription() {
		return "Create target segment container. Optionally copy source content to the target."
			+ " Expects: filter events. Sends back: filter events.";
	}

	public String getName() {
		return "Create Target";
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale(LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	public LocaleId getTargetLocale() {
		return targetLocale;
	}
	
	@Override
	public Parameters getParameters() {
		return params;
	}

	@Override
	public Event handleTextUnit(Event event) {
		ITextUnit tu = event.getTextUnit();
		if (tu.isTranslatable() || params.isCreateOnNonTranslatable()) {
			// Initialize the copy options
			int copyOptions = IResource.CREATE_EMPTY;
	
			if (params.isCopyContent()) {
				copyOptions |= IResource.COPY_SEGMENTED_CONTENT;
			}
			if (params.isCopyProperties()) {
				copyOptions |= IResource.COPY_PROPERTIES;
			}
	
			tu.createTarget(targetLocale, params.isOverwriteExisting(), copyOptions);
		}
		return event;
	}

}
