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

package net.sf.okapi.applications.rainbow.pipeline;

import java.util.List;
import java.util.Map;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipeline.IPipeline;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.pipeline.Pipeline;

public abstract class PredefinedPipeline extends Pipeline implements IPredefinedPipeline {
	
	private String id;
	private String title;
	private String paramData;
	private int initialStepIndex = -1;

	public PredefinedPipeline (String id,
		String title)
	{
		this.id = id;
		this.title = title;
	}
	
	@Override
	public String getId () {
		return id;
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public int getInitialStepIndex () {
		return initialStepIndex;
	}
	
	public void setInitialStepIndex (int index) {
		initialStepIndex = index;
	}

	@Override
	public String getParameters () {
		// Save the data from pipeline into a string
		// OK to pass null to availableSteps because that is not used for writing
		PipelineStorage store = new PipelineStorage(null);
		store.write(this);
		paramData = store.getStringOutput();
		return paramData;
	}

	@Override
	public void setParameters (Map<String, StepInfo> availableSteps,
		String data)
	{
		// Create a temporary pipeline from the storage
		if ( Util.isEmpty(data) ) return; // Nothing to read
		PipelineStorage store = new PipelineStorage(availableSteps, (CharSequence)data);
		IPipeline tmp = store.read();
		
		// Else: Fill the pre-defined steps with the saved data
		List<IPipelineStep> steps = getSteps();
		List<IPipelineStep> tmpSteps = tmp.getSteps();
		// Loop through the steps and assign the parameters of the steps
		// of the temporary pipeline (the stored data) to their equivalent.
		// Allows for the rare case where the pipeline may be different (e.g. changes in pre-defined pipelines)
		for ( int i=0; i<steps.size(); i++ ) {
			// Search for the corresponding step
			int found = -1;
			for ( int j=0; j<tmpSteps.size(); j++ ) {
				if ( steps.get(i).getName().equals(tmpSteps.get(j).getName()) ) {
					found = j;
					break;
				}
			}
			// If found: Use the data, then remove it
			if ( found != -1 ) {
				steps.get(i).setParameters(tmpSteps.get(found).getParameters());
				tmpSteps.remove(found);
			}
		}
	}

}
