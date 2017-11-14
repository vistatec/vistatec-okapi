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

package net.sf.okapi.steps.fullwidthconversion.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.ISWTEmbeddableParametersEditor;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.steps.fullwidthconversion.Parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

@EditorFor(Parameters.class)
public class ParametersEditor implements IParametersEditor, ISWTEmbeddableParametersEditor {
	
	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private IHelp help;
	private Button rdToFullWidth;
	private Button rdToHalfWidth;
	private Button chkAsciiOnly;
	private Button chkKatakanaOnly;
	private Button chkIncludeSLA;
	private Button chkIncludeLLS;
	private Button chkIncludeKatakana;
	private Button chkNormalizeOutput;
	private Composite mainComposite;
	
	public boolean edit (IParameters params,
		boolean readOnly,
		IContext context)
	{
		boolean bRes = false;
		try {
			shell = null;
			help = (IHelp)context.getObject("help");
			this.params = (Parameters)params;
			shell = new Shell((Shell)context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
			create((Shell)context.getObject("shell"), readOnly);
			return showDialog();
		}
		catch ( Exception e ) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
			bRes = false;
		}
		finally {
			// Dispose of the shell, but not of the display
			if ( shell != null ) shell.dispose();
		}
		return bRes;
	}
	
	public IParameters createParameters () {
		return new Parameters();
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
		params = (Parameters)paramsObject; 
		shell = (Shell)context.getObject("shell");
		createComposite(parent);
		setData();
	}

	@Override
	public String validateAndSaveParameters () {
		if ( !saveData() ) return null;
		return params.toString();
	}
	
	private void create (Shell parent,
		boolean readOnly)
	{
		shell.setText("Full-Width Conversion");
		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		createComposite(shell);

		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("Full-Width Conversion Step");
					return;
				}
				if ( e.widget.getData().equals("o") ) saveData();
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pnlActions.btOK.setEnabled(!readOnly);
		if ( !readOnly ) {
			shell.setDefaultButton(pnlActions.btOK);
		}

		setData();
		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Dialogs.centerWindow(shell, parent);
	}
	
	private void createComposite (Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new GridLayout());
		int indent = 16;
		
		rdToHalfWidth = new Button(mainComposite, SWT.RADIO);
		rdToHalfWidth.setText("Convert full-width characters to half-width or ASCII equivalents");
		
		chkIncludeSLA = new Button(mainComposite, SWT.CHECK);
		chkIncludeSLA.setText("Include Squared Latin Abbreviations of the CJK Compatibility block");
		GridData gdTmp = new GridData();
		gdTmp.horizontalIndent = indent;
		chkIncludeSLA.setLayoutData(gdTmp);
		
		chkIncludeLLS = new Button(mainComposite, SWT.CHECK);
		chkIncludeLLS.setText("Include special characters of the Letter-Like Symbols block");
		gdTmp = new GridData();
		gdTmp.horizontalIndent = indent;
		chkIncludeLLS.setLayoutData(gdTmp);
		
		chkIncludeKatakana = new Button(mainComposite, SWT.CHECK);
		chkIncludeKatakana.setText("Include Japanese Katakana and associated punctuation");
		gdTmp = new GridData();
		gdTmp.horizontalIndent = indent;
		chkIncludeKatakana.setLayoutData(gdTmp);

		rdToFullWidth = new Button(mainComposite, SWT.RADIO);
		rdToFullWidth.setText("Convert half-width and ASCII characters to full-width equivalents");
		
		chkAsciiOnly = new Button(mainComposite, SWT.CHECK);
		chkAsciiOnly.setText("Convert only the ASCII characters");
		gdTmp = new GridData();
		gdTmp.horizontalIndent = indent;
		chkAsciiOnly.setLayoutData(gdTmp);
		chkAsciiOnly.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (chkAsciiOnly.getSelection()) {
					chkKatakanaOnly.setSelection(false);
				}
			}
		});

		chkKatakanaOnly = new Button(mainComposite, SWT.CHECK);
		chkKatakanaOnly.setText("Convert only Japanese Katakana and associated punctuation");
		gdTmp = new GridData();
		gdTmp.horizontalIndent = indent;
		chkKatakanaOnly.setLayoutData(gdTmp);
		chkKatakanaOnly.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (chkKatakanaOnly.getSelection()) {
					chkAsciiOnly.setSelection(false);
				}
			}
		});

		rdToFullWidth.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateChkBox();
			}
		});
		
		chkNormalizeOutput = new Button(mainComposite, SWT.CHECK);
		chkNormalizeOutput.setText("Normalize output");
	}
	
	private void updateChkBox() {
		chkIncludeSLA.setEnabled(rdToHalfWidth.getSelection());
		chkIncludeLLS.setEnabled(rdToHalfWidth.getSelection());
		chkIncludeKatakana.setEnabled(rdToHalfWidth.getSelection());
		chkAsciiOnly.setEnabled(!rdToHalfWidth.getSelection());
		chkKatakanaOnly.setEnabled(!rdToHalfWidth.getSelection());
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
		rdToHalfWidth.setSelection(params.getToHalfWidth());
		chkIncludeSLA.setSelection(params.getIncludeSLA());
		chkIncludeLLS.setSelection(params.getIncludeLLS());
		chkIncludeKatakana.setSelection(params.getIncludeKatakana());
		rdToFullWidth.setSelection(!params.getToHalfWidth());
		chkAsciiOnly.setSelection(params.getAsciiOnly());
		chkKatakanaOnly.setSelection(params.getKatakanaOnly());
		chkNormalizeOutput.setSelection(params.getNormalizeOutput());
		updateChkBox();
	}

	private boolean saveData () {
		params.setToHalfWidth(rdToHalfWidth.getSelection());
		params.setIncludeSLA(chkIncludeSLA.getSelection());
		params.setIncludeLLS(chkIncludeLLS.getSelection());
		params.setIncludeKatakana(chkIncludeKatakana.getSelection());
		params.setAsciiOnly(chkAsciiOnly.getSelection());
		params.setKatakanaOnly(chkKatakanaOnly.getSelection());
		params.setNormalizeOutput(chkNormalizeOutput.getSelection());
		result = true;
		return result;
	}
	
}
