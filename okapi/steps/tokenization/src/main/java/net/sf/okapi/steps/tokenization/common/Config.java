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

package net.sf.okapi.steps.tokenization.common;

import net.sf.okapi.common.ParametersString;
import net.sf.okapi.lib.extra.AbstractParameters;

public class Config extends AbstractParameters {

	private String engineConfig;
	
	@Override
	protected void parameters_load(ParametersString buffer) {
		
		engineConfig = buffer.getString("engineConfig");
	}

	@Override
	protected void parameters_reset() {
		
		engineConfig = "";
	}

	@Override
	protected void parameters_save(ParametersString buffer) {
		
		buffer.setString("engineConfig", engineConfig);
	}

	public String getEngineConfig() {
		
		return engineConfig;
	}

}
