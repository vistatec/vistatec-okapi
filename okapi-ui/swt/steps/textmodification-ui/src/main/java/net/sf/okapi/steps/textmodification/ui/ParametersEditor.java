/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.textmodification.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.ISWTEmbeddableParametersEditor;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.steps.textmodification.Parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

@EditorFor(Parameters.class)
public class ParametersEditor implements IParametersEditor, ISWTEmbeddableParametersEditor {

	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private List lbTypes;
	private Button chkAddPrefix;
	private Text edPrefix;
	private Button chkAddSuffix;
	private Text edSuffix;
	private boolean inInit = true;
	private Button chkApplyToExistingTarget;
	private Button chkAddID;
	private Button chkAddName;
	private Button chkMarkSegments;
	private Button chkExpand;
	private Button chkApplyToBlankEntries;
	private IHelp help;
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
		inInit = false;
	}

	@Override
	public String validateAndSaveParameters () {
		if ( !saveData() ) return null;
		return params.toString();
	}
	
	private void create (Shell parent,
		boolean readOnly)
	{
		shell.setText("Text Rewriting");
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
					if ( help != null ) help.showWiki("Text Modification Step");
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

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 600 ) startSize.x = 600;
		shell.setSize(startSize);
		
		setData();
		inInit = false;
		Dialogs.centerWindow(shell, parent);
	}

	private void createComposite (Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new GridLayout());
		
		Label stTmp = new Label(mainComposite, SWT.NONE);
		stTmp.setText("Type of change to perform:");
		GridData gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		stTmp.setLayoutData(gdTmp);

		lbTypes = new List(mainComposite, SWT.BORDER | SWT.V_SCROLL);
		lbTypes.add("Keep the original text");
		lbTypes.add("Replace letters with Xs and digits with Ns");
		lbTypes.add("Remove text but keep inline codes");
		lbTypes.add("Replace selected ASCII characters with Extended Latin characters");
		lbTypes.add("Replace selected ASCII characters with Cyrillic characters");
		lbTypes.add("Replace selected ASCII characters with Arabic characters");
		lbTypes.add("Replace selected ASCII characters with Chinese characters");
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 70;
		gdTmp.horizontalSpan = 2;
		lbTypes.setLayoutData(gdTmp);

		chkAddPrefix = new Button(mainComposite, SWT.CHECK);
		chkAddPrefix.setText("Add the following prefix:");
		chkAddPrefix.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				edPrefix.setEnabled(chkAddPrefix.getSelection());
			}
		});
		
		edPrefix = new Text(mainComposite, SWT.BORDER);
		edPrefix.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		chkAddSuffix = new Button(mainComposite, SWT.CHECK);
		chkAddSuffix.setText("Add the following suffix:");
		chkAddSuffix.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				edSuffix.setEnabled(chkAddSuffix.getSelection());
			}
		});
		
		edSuffix = new Text(mainComposite, SWT.BORDER);
		edSuffix.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		chkAddName = new Button(mainComposite, SWT.CHECK);
		chkAddName.setText("Append the name of the item.");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkAddName.setLayoutData(gdTmp);

		chkAddID = new Button(mainComposite, SWT.CHECK);
		chkAddID.setText("Append the extraction ID of the item.");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkAddID.setLayoutData(gdTmp);

		chkMarkSegments = new Button(mainComposite, SWT.CHECK);
		chkMarkSegments.setText("Mark segments with '[' and ']' delimiters");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkMarkSegments.setLayoutData(gdTmp);
		
		chkExpand = new Button(mainComposite, SWT.CHECK);
		chkExpand.setText("Expand the text");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkExpand.setLayoutData(gdTmp);
		
		Label separator = new Label(mainComposite, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.heightHint = 1;
		gdTmp.horizontalSpan = 2;
		separator.setLayoutData(gdTmp);

		chkApplyToBlankEntries = new Button(mainComposite, SWT.CHECK);
		chkApplyToBlankEntries.setText("Modify also the items without text");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkApplyToBlankEntries.setLayoutData(gdTmp);
		
		chkApplyToExistingTarget = new Button(mainComposite, SWT.CHECK);
		chkApplyToExistingTarget.setText("Modify also the items with an existing translation");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkApplyToExistingTarget.setLayoutData(gdTmp);
		
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
		int n = params.getType();
		if ( n == Parameters.TYPE_EXTREPLACE ) {
			n += params.getScript();
		}
		lbTypes.setSelection(n);
		
		chkAddPrefix.setSelection(params.getAddPrefix());
		edPrefix.setText(params.getPrefix());
		chkAddSuffix.setSelection(params.getAddSuffix());
		edSuffix.setText(params.getSuffix());
		chkApplyToBlankEntries.setSelection(params.getApplyToBlankEntries());
		chkApplyToExistingTarget.setSelection(params.getApplyToExistingTarget());
		chkAddName.setSelection(params.getAddName());
		chkAddID.setSelection(params.getAddID());
		chkMarkSegments.setSelection(params.getMarkSegments());
		chkExpand.setSelection(params.getExpand());

		edPrefix.setEnabled(chkAddPrefix.getSelection());
		edSuffix.setEnabled(chkAddSuffix.getSelection());
	}

	private boolean saveData () {
		if ( inInit ) return true;
		
		int n = lbTypes.getSelectionIndex();
		if ( n >= Parameters.TYPE_EXTREPLACE ) {
			params.setScript(n-Parameters.TYPE_EXTREPLACE);
			n = Parameters.TYPE_EXTREPLACE;
		}
		params.setType(n);
		
		params.setAddPrefix(chkAddPrefix.getSelection());
		params.setPrefix(edPrefix.getText());
		params.setAddSuffix(chkAddSuffix.getSelection());
		params.setSuffix(edSuffix.getText());
		params.setApplyToBlankEntries(chkApplyToBlankEntries.getSelection());
		params.setApplyToExistingTarget(chkApplyToExistingTarget.getSelection());
		params.setAddName(chkAddName.getSelection());
		params.setAddID(chkAddID.getSelection());
		params.setMarkSegments(chkMarkSegments.getSelection());
		params.setExpand(chkExpand.getSelection());
		result = true;
		return true;
	}
	
}
