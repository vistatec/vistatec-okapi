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

package net.sf.okapi.steps.generatesimpletm;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	private static final String TMPATH = "tmPath";
	
	public Parameters () {
		super();
	}
	
	public String getTmPath () {
		return getString(TMPATH);
	}

	public void setTmPath (String tmPath) {
		setString(TMPATH, tmPath);
	}

	@Override
	public void reset() {
		super.reset();
		setTmPath("");
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add("tmPath", "Path of the TM", "Full path of the TM to generate.");
		return desc;
	}
	
}
