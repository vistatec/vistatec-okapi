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

import net.sf.okapi.common.ui.abstracteditor.IDialogPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Widget;

public class TokenNamesRuleTab extends Composite implements IDialogPage {
	private Button btnAllTokens;
	private Button btnOnlyTheseTokens;
	private List list;
	private Button btnAdd;
	private Button btnRemove;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public TokenNamesRuleTab(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(3, false));
		
		btnAllTokens = new Button(this, SWT.RADIO);
		btnAllTokens.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnAllTokens.setData("name", "btnAllTokens");
		btnAllTokens.setText("All tokens");
		new Label(this, SWT.NONE);
		
		btnOnlyTheseTokens = new Button(this, SWT.RADIO);
		btnOnlyTheseTokens.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnOnlyTheseTokens.setData("name", "btnOnlyTheseTokens");
		btnOnlyTheseTokens.setText("Only these tokens:");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		list = new List(this, SWT.BORDER);
		{
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
			gridData.heightHint = 300;
			gridData.widthHint = 500;
			list.setLayoutData(gridData);
		}
		list.setData("name", "list");
		
		btnAdd = new Button(this, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 90;
		btnAdd.setLayoutData(gridData);
		btnAdd.setData("name", "btnAdd");
		btnAdd.setText("Add...");
		new Label(this, SWT.NONE);
		
		btnRemove = new Button(this, SWT.NONE);
		btnRemove.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnRemove.setData("name", "btnRemove");
		btnRemove.setText("Remove");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public boolean canClose(boolean isOK) {
		
		return true;
	}

	public void interop(Widget speaker) {
		
	}

	public boolean load(Object data) {
		
		return true;
	}

	public boolean save(Object data) {

		return true;
	}

}
