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

package net.sf.okapi.common.ui;

import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IParameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Provides a very basic implementation {@link ISWTEmbeddableParametersEditor}.
 * This implementation offers only a plain text edit box where the string
 * representation of the parameters can be manually edited. No validation
 * is provided, except checking that the string is not empty.
 */
public class DefaultEmbeddableEditor implements ISWTEmbeddableParametersEditor {

	private Composite mainComposite;
	private IParameters params;
	private Text text;
	
	@Override
	public Composite getComposite () {
		return mainComposite;
	}
	
	@Override
	public void initializeEmbeddableEditor(Composite parent,
		IParameters paramsObject,
		IContext context)
	{
		this.params = paramsObject;
		createPanel(parent);
	}
	
	@Override
	public String validateAndSaveParameters () {
		try {
			if ( text.getText().length() == 0 ) {
				text.setFocus();
				return null;
			}
			params.fromString(text.getText());
			return params.toString();
		}
		catch ( Throwable e) {
			Dialogs.showError(mainComposite.getShell(), e.getMessage(), null);
			return null;
		}
	}

	private void createPanel (Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new GridLayout());
		text = new Text(mainComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.setText(params.toString());
	}
	
}
