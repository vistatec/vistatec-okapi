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

package net.sf.okapi.steps.tests;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.ITextUnit;

/**
 * Generic step to capture the last text unit for a set of filter events.
 */
public class CaptureStep extends BasePipelineStep {

	private ITextUnit tu;
	
	public String getDescription() {
		return "Capturing step for testing.";
	}

	public String getName() {
		return "Capture";
	}

	@Override
	protected Event handleTextUnit (Event event) {
		tu = event.getTextUnit();
		return event;
	}
	
	public ITextUnit getLastTextUnit () {
		return tu;
	}

	public void reset () {
		tu = null;
	}

}
