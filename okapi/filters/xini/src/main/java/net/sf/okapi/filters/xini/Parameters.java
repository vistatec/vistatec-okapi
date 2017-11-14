/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xini;

import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

public class Parameters extends StringParameters implements ISimplifierRulesParameters {
	/**
	 * Name of the parameter that enables/disables output segmentation
	 */
	public static final String USE_OKAPI_SEGMENTATION = "useOkapiSegmentation";
	
	public Parameters () {
		super();
	}
	
	public void reset () {
		super.reset();
		setUseOkapiSegmentation(true);
		setSimplifierRules(null);
	}

	public boolean isUseOkapiSegmentation() {
		return getBoolean(USE_OKAPI_SEGMENTATION);
	}

	public void setUseOkapiSegmentation(boolean useOkapiSegmentation) {
		setBoolean(USE_OKAPI_SEGMENTATION, useOkapiSegmentation);
	}

	@Override
	public String getSimplifierRules() {
		return getString(SIMPLIFIERRULES);
	}

	@Override
	public void setSimplifierRules(String rules) {
		setString(SIMPLIFIERRULES, rules);		
	}

	@Override
	public void validateSimplifierRules() throws ParseException {
		SimplifierRules r = new SimplifierRules(getSimplifierRules(), new Code());
		r.parse();
	}
	
	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(
				USE_OKAPI_SEGMENTATION, 
				"Use Okapi segmentation for output", 
				"If disabled, all XINI segments with the same value\n" +
				"of the attribute 'SegmentIDBeforeSegmentation' will be merged.\n" +
				"If the XINI was not segmented, it will remain unsegmented.\n" +
				"If this option is enabled, new segmentation\n" +
				"(i.e. from segmentation step) will be used for the output.");
		return desc;
	}
}
