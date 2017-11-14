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

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.ParametersString;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

public class LanguageAndTokenParameters extends LanguageParameters {
	
	private List<String> tokenNames;
	private List<Integer> tokenIds;

	@Override
	protected void parameters_init() {

		super.parameters_init();
	}
	
	@Override
	protected void parameters_load(ParametersString buffer) {
		
		super.parameters_load(buffer);
		
//		ListUtil.stringAsList(tokenNames, buffer.getString("tokens"));
//		tokenIds = Tokens.getTokenIDs(tokenNames);
		setTokenNames(buffer.getString("tokens"));
	}

	@Override
	protected void parameters_reset() {
		
		super.parameters_reset();
		
		tokenNames = new ArrayList<String>();
		tokenIds = new ArrayList<Integer>();
	}

	@Override
	protected void parameters_save(ParametersString buffer) {
		
		super.parameters_save(buffer);
		
		buffer.setString("tokens", ListUtil.listAsString(tokenNames));
	}

	public boolean supportsToken(String tokenName) {
		
		return (tokenNames != null) && (tokenNames.contains(tokenName) || (tokenNames.size() == 0));
	}
	
	public boolean supportsToken(int tokenId) {
		
		return (tokenIds != null) && (tokenIds.contains(tokenId) || (tokenIds.size() == 0));
	}

//	public void setTokenNames(List<String> tokenNames) {
//		
//		this.tokenNames = tokenNames;
//		tokenIds = Tokens.getTokenIDs(this.tokenNames);
//	}
	
	public void setTokenNames(String... tokenNames) {
		
		//setTokenNames(ListUtil.arrayAsList(tokenNames));
		if (tokenNames != null)
			this.tokenNames = ListUtil.arrayAsList(tokenNames);
		
		tokenIds = Tokens.getTokenIDs(this.tokenNames);
	}

	public List<String> getTokenNames() {
		
		return tokenNames;
	}
	
}
