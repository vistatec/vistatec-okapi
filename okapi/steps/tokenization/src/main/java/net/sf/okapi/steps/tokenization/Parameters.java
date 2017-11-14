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

package net.sf.okapi.steps.tokenization;

import net.sf.okapi.common.ParametersString;
import net.sf.okapi.steps.tokenization.common.LanguageAndTokenParameters;

/**
 * Tokenization step parameters
 * 
 * @version 0.1 06.07.2009
 */

public class Parameters extends LanguageAndTokenParameters {

	public boolean tokenizeSource; 
	public boolean tokenizeTargets;
		
	@Override
	protected void parameters_reset() {

		super.parameters_reset();
		
		tokenizeSource = true;
		tokenizeTargets = false;
	}

	@Override
	protected void parameters_load(ParametersString buffer) {

		super.parameters_load(buffer);
		
		tokenizeSource = buffer.getBoolean("tokenizeSource", true);
		tokenizeTargets = buffer.getBoolean("tokenizeTargets", false);
	}
	
	@Override
	protected void parameters_save(ParametersString buffer) {

		super.parameters_save(buffer);
		
		buffer.setBoolean("tokenizeSource", tokenizeSource);
		buffer.setBoolean("tokenizeTargets", tokenizeTargets);
	}	
}
