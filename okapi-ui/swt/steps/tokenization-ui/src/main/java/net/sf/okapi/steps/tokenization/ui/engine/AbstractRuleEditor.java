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

package net.sf.okapi.steps.tokenization.ui.engine;

import net.sf.okapi.common.ui.abstracteditor.AbstractParametersEditor;
import net.sf.okapi.steps.tokenization.ui.common.NameDescriptionTab;
import net.sf.okapi.steps.tokenization.ui.locale.LanguagesRuleTab;
import net.sf.okapi.steps.tokenization.ui.tokens.TokenNamesRuleTab;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

public abstract class AbstractRuleEditor extends AbstractParametersEditor {

	protected abstract Class<? extends Composite> getRuleClass();
	
	@Override
	protected void createPages(TabFolder pageContainer) {
				
		addPage("Rule", getRuleClass());
		addPage("Languages", LanguagesRuleTab.class);
		addPage("Tokens", TokenNamesRuleTab.class);
		addPage("Info", NameDescriptionTab.class);
	}


}
