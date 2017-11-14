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

package net.sf.okapi.steps.tokenization.tokens;

import net.sf.okapi.common.ParametersString;
import net.sf.okapi.lib.extra.AbstractParameters;

public class TokenItem extends AbstractParameters {

	private String name;
	private String description;
	
	public TokenItem() {
		
		super();
	}

	public TokenItem(String name, String description) {
		
		super();
		
		this.name = name;
		this.description = description;
	}

	@Override
	protected void parameters_load(ParametersString buffer) {
		
		name = buffer.getString("name");
		description = buffer.getString("description");
	}
	
	@Override
	protected void parameters_save(ParametersString buffer) {
		
		buffer.setString("name", name);
		buffer.setString("description", description);
	}

	@Override
	protected void parameters_reset() {
		
		name = "";
		description = "";
	}

	public String getName() {
		
		return name;
	}

	public String getDescription() {
		
		return description;
	}

}