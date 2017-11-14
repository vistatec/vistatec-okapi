/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.spacecheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.lib.verification.SpaceChecker;

public class SpaceCheckStep extends BasePipelineStep {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private LocaleId targetLocale;
	private SpaceChecker checker;
	private long changes;

	public SpaceCheckStep () {
		this.checker = new SpaceChecker();
	}
	
	@Override
	public String getName () {
		return "Space Quality Check";
	}

	@Override
	public String getDescription () {
		return "Compare and fix spaces around inline codes in target based on the source. "
			+ "Expects: filter events. Sends back: filter events.";
	}

	@Override
	public IParameters getParameters () {
		return null;
	}

	@Override
	public void setParameters (IParameters params) {
		// None for now
	}

	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
//	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
//	public void setSourceLocale (LocaleId sourceLocale) {
//		this.sourceLocale = sourceLocale;
//	}
	
	@Override
	protected Event handleStartDocument (Event event) {
		changes = 0;
		return event;
	}
	
	@Override
	protected Event handleTextUnit (Event event) {		
		changes += checker.checkUnitSpacing(event.getTextUnit(), targetLocale);
		return event;
	}
	
	@Override
	protected Event handleEndDocument (Event event) {
		LOGGER.info("Space checker changes: {}", changes);
		return event;
	}
	
}
