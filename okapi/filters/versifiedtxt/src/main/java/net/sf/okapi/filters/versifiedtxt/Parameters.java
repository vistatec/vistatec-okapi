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

package net.sf.okapi.filters.versifiedtxt;

import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

public class Parameters extends StringParameters {

	private static final String FORCETARGETOUTPUT = "forceTargetOutput";

	public Parameters () {	
		super();
	}
	
	public boolean isAllowEmptyOutputTarget () {
		return getBoolean(GenericSkeletonWriter.ALLOWEMPTYOUTPUTTARGET);
	}
	
	public void setAllowEmptyOutputTarget (boolean allowEmptyOutputTarget) {
		setBoolean(GenericSkeletonWriter.ALLOWEMPTYOUTPUTTARGET, allowEmptyOutputTarget);
	}

	/**
	 * @return the forceTargetOutput
	 */
	public boolean isForceTargetOutput() {
		return getBoolean(FORCETARGETOUTPUT);
	}

	/**
	 * @param forceTargetOutput the forceTargetOutput to set
	 */
	public void setForceTargetOutput(boolean forceTargetOutput) {
		setBoolean(FORCETARGETOUTPUT, forceTargetOutput);
	}

	public void reset () {
		super.reset();
		setAllowEmptyOutputTarget(true);
		setForceTargetOutput(true);
	}

}
