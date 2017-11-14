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

import net.sf.okapi.common.ui.abstracteditor.IDialogPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Widget;

public class LanguagesRuleTab extends Composite implements IDialogPage {
	private Button all;
	private Button except;
	private List listW;
	private Button addB;
	private Button removeB;
	private Button only;
	private List listB;
	private Button addW;
	private Button removeW;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public LanguagesRuleTab(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(3, false));
		
		all = new Button(this, SWT.RADIO);
		all.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		all.setData("name", "all");
		all.setText("All languages");
		new Label(this, SWT.NONE);
		
		only = new Button(this, SWT.RADIO);
		only.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		only.setData("name", "only");
		only.setText("Only these languages:");
		new Label(this, SWT.NONE);
		
		listW = new List(this, SWT.BORDER);
		{
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
			gridData.heightHint = 150;
			gridData.widthHint = 500;
			listW.setLayoutData(gridData);
		}
		listW.setData("name", "listB");
		
		addB = new Button(this, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 90;
		addB.setLayoutData(gridData);
		addB.setData("name", "addB");
		addB.setText("Add...");
		new Label(this, SWT.NONE);
		
		removeB = new Button(this, SWT.NONE);
		removeB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		removeB.setData("name", "removeB");
		removeB.setText("Remove");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		except = new Button(this, SWT.RADIO);
		except.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		except.setData("name", "except");
		except.setText("All languages except these:");
		new Label(this, SWT.NONE);
		
		listB = new List(this, SWT.BORDER);
		{
			GridData gridData_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
			gridData_1.widthHint = 500;
			gridData_1.heightHint = 150;
			listB.setLayoutData(gridData_1);
		}
		listB.setData("name", "listW");
		
		addW = new Button(this, SWT.NONE);
		addW.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		addW.setData("name", "addW");
		addW.setText("Add...");
		new Label(this, SWT.NONE);
		
		removeW = new Button(this, SWT.NONE);
		removeW.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		removeW.setData("name", "removeW");
		removeW.setText("Remove");
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
		// TODO Auto-generated method stub
		
	}

	public boolean load(Object data) {

		return true;
	}

	public boolean save(Object data) {

		return true;
	}

}
