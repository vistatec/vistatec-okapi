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

package net.sf.okapi.steps.tokenization.ui.mapping;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.abstracteditor.IDialogPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class MappingItemPage extends Composite implements IDialogPage {
	private Label lblEditor;
	private Label lblParameters;
	private Text eclass;
	private Text pclass;
	private Label label_1;
	private Label label;
	private Label label_2;
	private Label label_3;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public MappingItemPage(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(4, false));
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		label_3 = new Label(this, SWT.NONE);
		label_3.setData("name", "label_3");
		new Label(this, SWT.NONE);
		
		label = new Label(this, SWT.NONE);
		label.setData("name", "label");
		label.setText("    ");
		
		lblEditor = new Label(this, SWT.NONE);
		lblEditor.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEditor.setData("name", "lblEditor");
		lblEditor.setText("Editor class:");
		
		eclass = new Text(this, SWT.BORDER);
		eclass.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				
				detectParamsClass(eclass.getText());
			}
		});
		eclass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		eclass.setData("name", "eclass");
		
		label_2 = new Label(this, SWT.NONE);
		label_2.setData("name", "label_2");
		label_2.setText("    ");
		new Label(this, SWT.NONE);
		
		lblParameters = new Label(this, SWT.NONE);
		lblParameters.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblParameters.setData("name", "lblParameters");
		lblParameters.setText("Parameters class:");
		
		pclass = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		pclass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		pclass.setData("name", "pclass");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		label_1 = new Label(this, SWT.NONE);
		label_1.setData("name", "label_1");
		label_1.setText("                                                                                                     ");
		new Label(this, SWT.NONE);

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
						
		eclass.setText(colDef[0]);
		pclass.setText(colDef[1]);
		
		return true;
	}

	public boolean save(Object data) {

		if (!(data instanceof String[])) return false;
		
		String[] colDef = (String[]) data;
		if (colDef.length != 2) return false;
		
		colDef[0] = eclass.getText();
		colDef[1] = pclass.getText();
		
		return true;
	}

	private void detectParamsClass(String editorClass) {
	
		IParametersEditor editor = null;
		
		try {
			editor = (IParametersEditor)Class.forName(editorClass).newInstance();
		}
		catch ( Exception e ) {
			pclass.setText("");
			return;
		}
		
		if (editor == null) return;
		
		IParameters params = editor.createParameters();
		if (params == null) return;
		
		pclass.setText(params.getClass().getName());
	}
	
// TODO Check if the new class not already there
	
}
