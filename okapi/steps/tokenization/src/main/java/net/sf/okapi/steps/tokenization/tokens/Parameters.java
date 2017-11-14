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

package net.sf.okapi.steps.tokenization.tokens;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.ParametersString;
import net.sf.okapi.lib.extra.AbstractParameters;

public class Parameters extends AbstractParameters {

	private List<TokenItem> items;
	
	@Override
	protected void parameters_init() {
	}

	@Override
	protected void parameters_load(ParametersString buffer) {
		
		loadGroup(buffer, items, TokenItem.class);
	}

	@Override
	protected void parameters_reset() {
		items = new ArrayList<TokenItem>();
	}

	@Override
	protected void parameters_save(ParametersString buffer) {
		
		//items.parameters_save(buffer);
		saveGroup(buffer, items, TokenItem.class);
	}

	public boolean loadItems() {
		
		return loadFromResource("tokens.tprm");
	}
	
	public void saveItems() {
		
		saveToResource("tokens.tprm");
	}

//	protected int generateId() {
//		// Slow, as used only from UI  
//		
//		int max = 0;
//		for (TokenItem item : items) {
//			
//			if (item == null) continue;
//			if (max < item.getId())
//				max = item.getId();
//		}
//		
//		//return (max > 0) ? max + 1: 0;
//		return max + 1;
//	}
	
	public void addTokenItem(String name, String description) {
		
		items.add(new TokenItem(name, description));
	}

	public List<TokenItem> getItems() {
		
		return items;
	}
	
}
