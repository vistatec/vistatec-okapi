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

package net.sf.okapi.steps.tokenization.ui.locale;

import net.sf.okapi.common.ui.abstracteditor.AbstractListAddRemoveTab;
import net.sf.okapi.steps.tokenization.common.LanguageAndTokenParameters;
import net.sf.okapi.steps.tokenization.locale.LanguageList;

import org.eclipse.swt.widgets.Composite;

public class LanguagesPage extends AbstractListAddRemoveTab {

	public LanguagesPage(Composite parent, int style) {
		
		super(parent, style);
	}

	@Override
	protected boolean getDisplayListDescr() {

		return false;
	}

	@Override
	protected void actionAdd(int afterIndex) {
		
		String[] res = LanguageSelector.select(getShell(), LanguageSelectorPePage.class, "EN-US,RU-RU");
		
		for (int i = 0; i < res.length; i++) {
			
			if (list.indexOf(res[i]) == -1)
				list.add(res[i]);
		}
	}

	public boolean canClose(boolean isOK) {

		return true;
	}

	public boolean load(Object data) {
		
		if (!(data instanceof LanguageAndTokenParameters)) return false;
		
//		LanguageAndTokenParameters params = (LanguageAndTokenParameters) data;
//		
//		if (params.getLanguageMode() == LanguageAndTokenParameters.LANGUAGES_ALL) return false;
//		
//		if (params.getLanguageMode() == LanguageAndTokenParameters.LANGUAGES_ONLY_WHITE_LIST)
//			list.setItems(ListUtil.listAsArray(params.getLanguageWhiteList()));
//		
//		if (params.getLanguageMode() == LanguageAndTokenParameters.LANGUAGES_ALL_EXCEPT_BLACK_LIST)
//			list.setItems(ListUtil.listAsArray(params.getLanguageBlackList()));
//			
//		selectListItem(0);			
		
		return true;
	}

	public boolean save(Object data) {
		
		if (!(data instanceof LanguageAndTokenParameters)) return false;
		
//		LanguageAndTokenParameters params = (LanguageAndTokenParameters) data;
//		if (params.getLanguageMode() == LanguageAndTokenParameters.LANGUAGES_ALL) return false;
//		
//		if (params.getLanguageMode() == LanguageAndTokenParameters.LANGUAGES_ONLY_WHITE_LIST)			
//			params.setLanguageWhiteList(ListUtil.arrayAsList(list.getItems()));
//		
//		if (params.getLanguageMode() == LanguageAndTokenParameters.LANGUAGES_ALL_EXCEPT_BLACK_LIST)
//			params.setLanguageBlackList(ListUtil.arrayAsList(list.getItems()));
		
		return true;
	}

	@Override
	protected String getItemDescription(int index) {
		return LanguageList.getDisplayName(list.getItem(index));
	}
	
}
