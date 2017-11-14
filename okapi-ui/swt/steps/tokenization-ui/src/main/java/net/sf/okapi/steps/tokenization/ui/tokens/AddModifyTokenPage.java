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

import net.sf.okapi.common.ui.abstracteditor.IDialogPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class AddModifyTokenPage extends Composite implements IDialogPage {
	private Label lblToken;
	private Text name;
	private Label lblDescription;
	private Text descr;
	private Label label;
	private Label label_1;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public AddModifyTokenPage(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(5, false));
		
		label = new Label(this, SWT.NONE);
		label.setData("name", "label");
		label.setText("    ");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		lblToken = new Label(this, SWT.NONE);
		lblToken.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblToken.setData("name", "lblToken");
		lblToken.setText("Token name:");
		
		name = new Text(this, SWT.BORDER);
		name.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				
				e.text = e.text.toUpperCase();
			}
		});
		
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		name.setData("name", "name");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		lblDescription = new Label(this, SWT.NONE);
		lblDescription.setData("name", "lblDescription");
		lblDescription.setText("Token description:");
		
		descr = new Text(this, SWT.BORDER);
		descr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		descr.setData("name", "descr");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		label_1 = new Label(this, SWT.NONE);
		label_1.setData("name", "label_1");
		label_1.setText("    ");

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

		if (!(data instanceof String[])) return false;
		
		String[] colDef = (String[]) data;
		if (colDef.length != 2) return false; 
						
		name.setText(colDef[0]);
		descr.setText(colDef[1]);
		
		return true;
	}

	public boolean save(Object data) {

		if (!(data instanceof String[])) return false;
		
		String[] colDef = (String[]) data;
		if (colDef.length != 2) return false;
		
		colDef[0] = name.getText();
		colDef[1] = descr.getText();
		
		return true;
	}
}
