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

package net.sf.okapi.steps.tokenization.ui;

import net.sf.okapi.common.BaseContext;
import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ui.abstracteditor.AbstractParametersEditor;
import net.sf.okapi.steps.tokenization.Parameters;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Widget;

@EditorFor(Parameters.class)
public class ParametersEditor extends AbstractParametersEditor {

	public static void main(String[] args) {
		
		ParametersEditor editor = new ParametersEditor();
		editor.edit(editor.createParameters(), false, new BaseContext());
	}
	
	@Override
	protected void createPages(TabFolder pageContainer) {
				
		addPage("Options", OptionsTab.class);
//		addPage("Languages", LanguagesTab.class);
//		addPage("Tokens", TokenTypesTab.class);		
	}

	@Override
	public IParameters createParameters() {
		
		return new Parameters();
	}

	@Override
	protected String getCaption() {

		return "Tokenization";
	}

	@Override
	protected void interop(Widget speaker) {
		// TODO Auto-generated method stub

	}

}
