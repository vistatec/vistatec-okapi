/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.desegmentation;

import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.RenumberingUtil;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;

@UsingParameters(Parameters.class)
public class DesegmentationStep extends BasePipelineStep {

	private Parameters params;
	private List<LocaleId>  targetLocales;

	public DesegmentationStep () {
		params = new Parameters();
	}
	
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALES)
	public void setTargetLocales (List<LocaleId> targetLocales) {
		this.targetLocales = targetLocales;
	}
	
	public List<LocaleId> getTargetLocales() {
		return targetLocales;
	}
	
	@Override
	public String getName () {
		return "Desegmentation";
	}

	@Override
	public String getDescription () {
		return "Joins all segments into a single content. "
			+ "Expects: filter events. Sends back: filter events.";
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	protected Event handleTextUnit (Event event) {
		ITextUnit tu = event.getTextUnit();

		// Skip non-translatable
		if ( !tu.isTranslatable() ) return event;
				
		// Desegment source if needed
		if ( params.getDesegmentSource() && tu.getSource().hasBeenSegmented() ) {
			if (params.getRenumberCodes()) {
				RenumberingUtil.renumberCodesForDesegmentation(tu.getSource());
			}
			tu.getSource().getSegments().joinAll();
		}
		
		// Desegment target if needed
		if ( params.getDesegmentTarget() && targetLocales != null ) {
			for(LocaleId targetLocale : targetLocales) {
				TextContainer cont = tu.getTarget(targetLocale);
				if ( cont != null ) {
					if ( cont.hasBeenSegmented() ) {
						if (params.getRenumberCodes()) {
							RenumberingUtil.renumberCodesForDesegmentation(cont);
						}
						cont.getSegments().joinAll();
					}
				}
			}
		}
		
		return event;
	}


}
