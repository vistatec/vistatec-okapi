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

package net.sf.okapi.steps.tokenization.ui.common;

import net.sf.okapi.common.ui.abstracteditor.IDialogPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class CompoundStepItemPage extends Composite implements IDialogPage {
	private Label label;
	private Label lblStepClass;
	private Label lblResourceName;
	private Text ctext;
	private Text ptext;
	private Label label_1;
	private Label label_2;
	private Label label_3;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public CompoundStepItemPage(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(4, false));
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		label_2 = new Label(this, SWT.NONE);
		label_2.setData("name", "label_2");
		new Label(this, SWT.NONE);
		
		label = new Label(this, SWT.NONE);
		label.setData("name", "label");
		label.setText("    ");
		
		lblStepClass = new Label(this, SWT.NONE);
		lblStepClass.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStepClass.setData("name", "lblStepClass");
		lblStepClass.setText("Step class (fully qualified):");
		
		ctext = new Text(this, SWT.BORDER);
		{
			GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gridData.widthHint = 400;
			ctext.setLayoutData(gridData);
		}
		ctext.setData("name", "ctext");
		
		label_1 = new Label(this, SWT.NONE);
		label_1.setData("name", "label_1");
		label_1.setText("    ");
		new Label(this, SWT.NONE);
		
		lblResourceName = new Label(this, SWT.NONE);
		lblResourceName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblResourceName.setData("name", "lblResourceName");
		lblResourceName.setText("Configuration (short filename):");
		
		ptext = new Text(this, SWT.BORDER);
		ptext.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		ptext.setData("name", "ptext");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		label_3 = new Label(this, SWT.NONE);
		label_3.setData("name", "label_3");
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
