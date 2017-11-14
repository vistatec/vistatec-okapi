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

package net.sf.okapi.common.ui.abstracteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Widget;

/**
 * 
 * 
 * @version 0.1, 23.06.2009
 */

public class InputQueryPageInt extends Composite implements IInputQueryPage {
	private Composite composite;
	private Label lblPrompt;
	private Spinner spinner;
	@SuppressWarnings("unused")
	private Label label;
	@SuppressWarnings("unused")
	private Label label_1;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public InputQueryPageInt(Composite parent, int style) {
		super(parent, SWT.BORDER);
		setLayout(new GridLayout(1, false));
		
		composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		new Label(composite, SWT.NONE);
		
		label = new Label(composite, SWT.NONE);
		
		lblPrompt = new Label(composite, SWT.NONE);
		lblPrompt.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPrompt.setText("Prompt:");
		
		spinner = new Spinner(composite, SWT.BORDER);
		new Label(composite, SWT.NONE);
		
		label_1 = new Label(composite, SWT.NONE);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public boolean load(Object data) {
		
		if (!(data instanceof Integer[])) return false;
		
		Integer[] intArr = (Integer[]) data;		
		spinner.setMinimum(intArr[1]);
		spinner.setMaximum(intArr[2]);
		spinner.setSelection(intArr[0]);
		return true;
	}

	public boolean save(Object data) {

		if (!(data instanceof Integer[])) return false;
		Integer[] intArr = (Integer[]) data; 
		intArr[0] = spinner.getSelection();
		
		return true;
	}

	public void setPrompt(String prompt) {
		
		lblPrompt.setText(prompt);
	}

	public void interop(Widget speaker) {
		
		
	}

	public boolean canClose(boolean isOK) {
		
		return true;
	}

}
