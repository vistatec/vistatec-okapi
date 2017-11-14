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

public class StructureParametersItem extends AbstractParameters {

	private boolean enabled;
	private String description;
	private String lexerClass;
	private String rulesLocation;
		
	@Override
	protected void parameters_reset() {
		
		enabled = true;
		description = "";
		lexerClass = "";
		rulesLocation = "";
	}

	@Override
	protected void parameters_load(ParametersString buffer) {

		enabled = buffer.getBoolean("enabled", true);
		description = buffer.getString("description");
		lexerClass = buffer.getString("lexerClass");
		rulesLocation = buffer.getString("rulesLocation");
	}

	@Override
	protected void parameters_save(ParametersString buffer) {
		
		buffer.setBoolean("enabled", enabled);
		buffer.setString("description", description);
		buffer.setString("lexerClass", lexerClass);
		buffer.setString("rulesLocation", rulesLocation);
	}

	public String getLexerClass() {
		
		return lexerClass;
	}

	public String getRulesLocation() {
		
		return rulesLocation;
	}

	public boolean isEnabled() {
		
		return enabled;
	}
		
}
