/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.utilities;

import java.io.File;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.pipeline.IPipelineStep;

public class UtilityStep implements IPipelineStep {

	private IFilterDrivenUtility utility;

	public UtilityStep (IFilterDrivenUtility utility) {
		this.utility = utility;
	}

	public Event handleEvent (Event event) {
		utility.handleEvent(event);
		return event;
	}
	
	public String getHelpLocation () {
		return ".." + File.separator + "help" + File.separator + "steps";
	}

	public void cancel () {
		// Cancel needed here
	}

	public String getName () {
		return utility.getName();
	}

	public String getDescription () {
		// TODO: Implement real descriptions
		return utility.getName();
	}
	
	public void pause () {
	}

	public void postprocess () {
		utility.postprocess();
	}

	public void preprocess () {
		utility.preprocess();
	}

	public void destroy () {
		utility.destroy(); 
	}

	public boolean hasNext () {
		return false;
	}

	
	//======================= Just temporary until we move all utilities to steps
	
	public IParameters getParameters() {
		//x TODO Auto-generated method stub
		return null;
	}

	public void setParameters(IParameters params) {
		//x TODO Auto-generated method stub		
	}

	public boolean isDone() {
		//x TODO Auto-generated method stub
		return false;
	}

	public boolean isLastOutputStep() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setLastOutputStep(boolean isLastStep) {
		// TODO Auto-generated method stub		
	}
}
