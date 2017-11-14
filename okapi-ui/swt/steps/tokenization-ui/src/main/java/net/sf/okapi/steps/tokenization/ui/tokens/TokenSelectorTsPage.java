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

package net.sf.okapi.steps.tokenization.ui.tokens;

import java.util.List;

import net.sf.okapi.common.ui.abstracteditor.SWTUtil;
import net.sf.okapi.common.ui.abstracteditor.TableAdapter;
import net.sf.okapi.steps.tokenization.tokens.Parameters;
import net.sf.okapi.steps.tokenization.tokens.TokenItem;

import org.eclipse.swt.widgets.Composite;

public class TokenSelectorTsPage extends TokenSelectorPage {

	public TokenSelectorTsPage(Composite parent, int style) {
		
		super(parent, style);
	
		SWTUtil.setText(listDescr, "This program lets you configure the global set of tokens.");
	}

	@Override
	public boolean save(Object data) {
		
		if ( super.save(data) ) {
			Parameters params = new Parameters();
			TableAdapter adapter = getAdapter();
			List<TokenItem> items = params.getItems();
			
			for (int i = 0; i < adapter.getNumRows(); i++)			
				items.add(new TokenItem(adapter.getValue(i + 1, 1), adapter.getValue(i + 1, 2)));
			
			params.saveItems();
		}			
		
		return true;
	}

}
