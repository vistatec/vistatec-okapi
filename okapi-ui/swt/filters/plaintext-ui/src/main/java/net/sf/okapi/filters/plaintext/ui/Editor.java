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

package net.sf.okapi.filters.plaintext.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.filters.plaintext.ui.common.FilterParametersEditor;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Widget;


/**
 * Plain text filter parameters editor.
 * 
 * @version 0.1, 12.06.2009
 */
@EditorFor(net.sf.okapi.filters.plaintext.Parameters.class)
public class Editor extends FilterParametersEditor {
	
	@Override
	public IParameters createParameters() {
		
		return new net.sf.okapi.filters.plaintext.Parameters();
	}

	@Override
	protected String getCaption() {
		
		return "Plain Text Filter Parameters";
	}

	@Override
	protected void createPages(TabFolder pageContainer) {
	
		addPage("General", GeneralTab.class);
		addPage("Options", OptionsTab.class);
	}

	@Override
	protected void interop(Widget speaker) {
		
	}

	@Override
	protected String getWikiPage () {
		return "Plain Text Filter";
	}

}
