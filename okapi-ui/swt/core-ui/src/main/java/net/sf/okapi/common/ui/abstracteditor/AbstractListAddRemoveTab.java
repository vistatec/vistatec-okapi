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

package net.sf.okapi.common.ui.abstracteditor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public abstract class AbstractListAddRemoveTab extends AbstractListTab {

	public AbstractListAddRemoveTab(Composite parent, int style) {
		
		super(parent, style);
		
		SWTUtil.setVisible(modify, false);
		SWTUtil.setVisible(up, false);
		SWTUtil.setVisible(down, false);
	}

	@Override
	protected boolean getDisplayModify() {
		
		return false;
	}

	@Override
	public void interop(Widget speaker) {
		
		super.interop(speaker);
		
		// If the list is empty, display a selection dialog
		if (speaker instanceof Shell && list.getItemCount() == 0) {// speaker is Shell only when called from showDialog()
		
			actionAdd(0);
			selectListItem(list.getItemCount() - 1);
			interop(null);  // To update the Remove button status 
		}			
	}
}
