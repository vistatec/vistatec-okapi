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

package net.sf.okapi.steps.tokenization.ui.tokens;

import java.util.List;

import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.ui.abstracteditor.InputQueryDialog;

import org.eclipse.swt.widgets.Shell;

public class TokenSelector {

	/**
	 * For creation of new tokens and storing them to the globally accessible net.sf.okapi.steps.tokenization.tokens/tokens.tprm
	 */
	public static void main(String[] args) {
		
		select();
	}

	public static String[] select() {
		
		return select(null, TokenSelectorTsPage.class, null); 
	}

	/**
	 * 
	 * @param parent
	 * @param classRef
	 * @param initialData a list of comma-separated token names
	 * @return
	 */
	public static String[] select(Shell parent, Class<? extends TokenSelectorPage> classRef, String initialData) {
				
		InputQueryDialog dlg = new InputQueryDialog();
		List<String> list = ListUtil.stringAsList(initialData);
				
		dlg.run(parent, classRef, "Tokens", "", list, null);
				
		return list.toArray(new String[] {});
	}
}
