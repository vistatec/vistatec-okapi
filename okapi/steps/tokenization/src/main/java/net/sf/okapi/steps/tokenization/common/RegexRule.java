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

public class RegexRule extends LexerRule {

	private int regexOptions;
	private int regexGroup;
	
	@Override
	protected void parameters_load(ParametersString buffer) {

		super.parameters_load(buffer);
		
		regexOptions = buffer.getInteger("regexOptions");
		regexGroup = buffer.getInteger("regexGroup");
	}

	@Override
	protected void parameters_reset() {
		
		super.parameters_reset();
		
		regexOptions = 0;
		regexGroup = 0;		
	}

	@Override
	protected void parameters_save(ParametersString buffer) {
		
		super.parameters_save(buffer);
		
		buffer.setInteger("regexOptions", regexOptions);
		buffer.setInteger("regexGroup", regexGroup);
	}

	public int getRegexOptions() {
		
		return regexOptions;
	}

	public int getRegexGroup() {
		
		return regexGroup;
	}
}
