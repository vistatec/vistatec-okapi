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

import net.sf.okapi.common.pipeline.BasePipelineStep;

/**
 * Pipeline as step delegate. Helper class implementing a pipeline's functionality as a pipeline step.
 * This class allows a pipeline to be inserted in another pipeline as a step.
 * Pipeline step method calls are delegated to an instance of this class instantiated inside the pipeline.
 */
public class XPipelineAsStepImpl extends BasePipelineStep {

	private String description;
	
	public String getName() {
		return "Pipeline as step delegate.";
	}
	public String getDescription() {

		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}
