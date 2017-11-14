/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.pipeline;

import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.ISWTEmbeddableParametersEditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class EmbeddableEditorButton implements ISWTEmbeddableParametersEditor {

	private Composite mainComposite;
	private Button btEdit;
	private IParameters params;
	private IParametersEditor editor;
	private IContext context;
	
	public EmbeddableEditorButton (IParametersEditor editor) {
		this.editor = editor;
	}
	
	@Override
	public Composite getComposite () {
		return mainComposite;
	}
	
	@Override
	public void initializeEmbeddableEditor (Composite parent,
		IParameters paramsObject,
		IContext context)
	{
		this.params = paramsObject;
		this.context = context;
		createPanel(parent);
	}
	
	@Override
	public String validateAndSaveParameters () {
		// Nothing to do as this is done via the edit dialog box
		return params.toString();
	}

	private void createPanel (Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new GridLayout());
		btEdit = new Button(mainComposite, SWT.PUSH);
		btEdit.setText("Edit Step Parameters...");
		btEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editParameters();
			}
		});
	}
	
	private void editParameters () {
		try {
			editor.edit(params, false, context);
		}
		catch ( Throwable e) {
			Dialogs.showError(mainComposite.getShell(), e.getMessage(), null);
		}
	}

}
