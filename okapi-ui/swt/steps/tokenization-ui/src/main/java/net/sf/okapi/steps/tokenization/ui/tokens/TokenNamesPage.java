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

import net.sf.okapi.common.ui.abstracteditor.AbstractListAddRemoveTab;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

import org.eclipse.swt.widgets.Composite;

public class TokenNamesPage extends AbstractListAddRemoveTab {

	public TokenNamesPage(Composite parent, int style) {
		
		super(parent, style);
	}

	@Override
	protected void actionAdd(int afterIndex) {
		
		String[] res = TokenSelector.select(getShell(), TokenSelectorPePage.class, "WORD,NUMBER");
		
		for (int i = 0; i < res.length; i++) {
			
			if (list.indexOf(res[i]) == -1)
				list.add(res[i]);
		}
	}

	@Override
	protected String getItemDescription(int index) {

		return Tokens.getTokenDescription(list.getItem(index));
	}

	@Override
	protected boolean getDisplayListDescr() {

		return false;
	}

	public boolean canClose(boolean isOK) {

		return true;
	}

	public boolean load(Object data) {

//		if (!(data instanceof LanguageAndTokenParameters)) return false;
//		
//		LanguageAndTokenParameters params = (LanguageAndTokenParameters) data;		
//		if (params.getTokenMode() == LanguageAndTokenParameters.TOKENS_ALL) return false;		
//		
//		list.setItems(ListUtil.listAsArray(params.getTokenNames()));		
//		selectListItem(0);			
		
		return true;
	}

	public boolean save(Object data) {
		
//		if (!(data instanceof LanguageAndTokenParameters)) return false;
//		
//		LanguageAndTokenParameters params = (LanguageAndTokenParameters) data;
//		if (params.getTokenMode() == LanguageAndTokenParameters.TOKENS_ALL) return false;
//		
//		params.setTokenNames(ListUtil.arrayAsList(list.getItems()));
		
		return true;
	}

}