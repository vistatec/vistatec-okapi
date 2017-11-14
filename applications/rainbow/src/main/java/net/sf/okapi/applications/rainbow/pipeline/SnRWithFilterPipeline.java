/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.pipeline;

import net.sf.okapi.steps.common.FilterEventsToRawDocumentStep;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.common.createtarget.CreateTargetStep;
import net.sf.okapi.steps.searchandreplace.SearchAndReplaceStep;

public class SnRWithFilterPipeline extends PredefinedPipeline {

	public SnRWithFilterPipeline () {
		super("SnRWithFilterPipeline",
			"Search and Replace - With Filter");
		addStep(new RawDocumentToFilterEventsStep());
		addStep(new CreateTargetStep());
		addStep(new SearchAndReplaceStep());
		addStep(new FilterEventsToRawDocumentStep());
		setInitialStepIndex(2);
	}
	
}
