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

package net.sf.okapi.applications.rainbow.packages.xliff;

import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class OptionsEditor implements IParametersEditor {
	
	private Shell shell;
	private boolean result = false;
	private Button chkGMode;
	private Button chkIncludeNoTranslate;
	private Button chkSetApprovedAsNoTranslate;
	private Button chkCopySource;
	private Button chkIncludeAltTrans;
	private Options params;
	private IHelp help;

	public boolean edit (IParameters params,
		boolean readOnly,
		IContext context)
	{
		help = (IHelp)context.getObject("help");
		boolean bRes = false;
		shell = null;
		this.params = (Options)params;
		try {
			shell = new Shell((Shell)context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
			create((Shell)context.getObject("shell"), readOnly);
			return showDialog();
		}
		catch ( Exception E ) {
			Dialogs.showError(shell, E.getLocalizedMessage(), null);
			bRes = false;
		}
		finally {
			// Dispose of the shell, but not of the display
			if ( shell != null ) shell.dispose();
		}
		return bRes;
	}
	
	public IParameters createParameters () {
		return new Options();
	}
	
	private void create (Shell parent,
		boolean readOnly)
	{
		shell.setText("XLIFF Package Options");
		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		Composite cmpTmp = new Composite(shell, SWT.BORDER);
		cmpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		cmpTmp.setLayout(new GridLayout());
		
		chkIncludeNoTranslate = new Button(cmpTmp, SWT.CHECK);
		chkIncludeNoTranslate.setText("Include non-translatable text units");
		chkIncludeNoTranslate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateNoTranslateCases();
			};
		});
		
		chkSetApprovedAsNoTranslate = new Button(cmpTmp, SWT.CHECK);
		chkSetApprovedAsNoTranslate.setText("Set approved entries as non-translatable");
		GridData gdTmp = new GridData();
		gdTmp.horizontalIndent = 16;
		chkSetApprovedAsNoTranslate.setLayoutData(gdTmp);
		chkSetApprovedAsNoTranslate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateNoTranslateCases();
			};
		});
		
		chkGMode = new Button(cmpTmp, SWT.CHECK);
		chkGMode.setText("Use <g></g> and <x/> notation");

		chkCopySource = new Button(cmpTmp, SWT.CHECK);
		chkCopySource.setText("Copy source text in target if no target is available");
		
		chkIncludeAltTrans = new Button(cmpTmp, SWT.CHECK);
		chkIncludeAltTrans.setText("Include <alt-trans> elements");
		
		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("Rainbow - XLIFF Package Options");
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					if ( !saveData() ) return;
					result = true;
				}
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		pnlActions.setLayoutData(gdTmp);
		pnlActions.btOK.setEnabled(!readOnly);
		if ( !readOnly ) {
			shell.setDefaultButton(pnlActions.btOK);
		}

		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 300 ) startSize.x = 300; 
		if ( startSize.y < 200 ) startSize.y = 200; 
		shell.setSize(startSize);
		Dialogs.centerWindow(shell, parent);
		setData();
	}
	
	private boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}
	
	private void setData () {
		chkGMode.setSelection(params.getGMode());
		chkIncludeNoTranslate.setSelection(params.getIncludeNoTranslate());
		chkSetApprovedAsNoTranslate.setSelection(params.getSetApprovedAsNoTranslate());
		chkCopySource.setSelection(params.getCopySource());
		chkIncludeAltTrans.setSelection(params.getIncludeAltTrans());
		updateNoTranslateCases();
	}
	
	private boolean saveData () {
		params.setGMode(chkGMode.getSelection());
		params.setIncludeNoTranslate(chkIncludeNoTranslate.getSelection());
		params.setSetApprovedAsNoTranslate(chkSetApprovedAsNoTranslate.getSelection());
		params.setCopySource(chkCopySource.getSelection());
		params.setIncludeAltTrans(chkIncludeAltTrans.getSelection());
		return true;
	}
	
	private void updateNoTranslateCases () {
		if ( !chkIncludeNoTranslate.getSelection() ) {
			chkSetApprovedAsNoTranslate.setSelection(false);
		}
		chkSetApprovedAsNoTranslate.setEnabled(chkIncludeNoTranslate.getSelection());
	}
}
