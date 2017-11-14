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

import net.sf.okapi.common.ui.abstracteditor.AbstractListTab;

import org.eclipse.swt.widgets.Composite;

public class RulesTab extends AbstractListTab {

	public RulesTab(Composite parent, int style) {
		
		super(parent, style);
	}

	public boolean canClose(boolean isOK) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean load(Object data) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean save(Object data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void actionAdd(int afterIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void actionDown(int itemIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void actionModify(int itemIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void actionUp(int itemIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean getDisplayListDescr() {

		return true;
	}

	@Override
	protected String getItemDescription(int index) {
		// TODO Auto-generated method stub
		return null;
	}

}
