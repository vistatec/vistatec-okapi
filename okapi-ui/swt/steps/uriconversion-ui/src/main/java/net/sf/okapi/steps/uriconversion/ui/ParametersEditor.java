/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.steps.uriconversion.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.ISWTEmbeddableParametersEditor;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.steps.uriconversion.Parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

@EditorFor(Parameters.class)
public class ParametersEditor implements IParametersEditor, ISWTEmbeddableParametersEditor {

	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private Table  table;
	private Button chkUpdateAll;
	private Button chkUnescape;
	private Button chkEscape;
	private Button btnFirstOption;
	private Button btnSecondOption;
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
	}

	@Override
	public String validateAndSaveParameters () {
		if ( !saveData() ) return null;
		return params.toString();
	}
	
	private void create (Shell parent,
		boolean readOnly)
	{
		shell.setText("URI Conversion");
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
					if ( help != null ) help.showWiki("URI Conversion Step");
					return;
				}
				if ( e.widget.getData().equals("o") ){
					if(!saveData()){
						return;
					}
				}
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
		shell.setSize(shell.getSize().x,shell.getSize().y+50);
		Dialogs.centerWindow(shell, parent);
		
		setData();
	}
	
	private void createComposite (Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new GridLayout(2, false));
		
		chkUnescape = new Button (mainComposite, SWT.RADIO);
		chkUnescape.setText("Un-escape the URI escape sequences");
		chkUnescape.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		chkUnescape.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(chkUnescape.getSelection()){
					table.setEnabled(false);
					chkUpdateAll.setEnabled(false);
					btnFirstOption.setEnabled(false);
					btnSecondOption.setEnabled(false);
				}
			}
		});		
	
		chkEscape = new Button (mainComposite, SWT.RADIO);
		chkEscape.setText("Escape content to URI escape sequences");
		chkEscape.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		chkEscape.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(chkEscape.getSelection()){
					table.setEnabled(true);
					chkUpdateAll.setEnabled(true);
					btnFirstOption.setEnabled(true);
					btnSecondOption.setEnabled(true);					
				}
			}
		});		
		
		Label lblList = new Label (mainComposite,SWT.LEFT);
		lblList.setText ("List of the characters to escape:");
		GridData gdTmp = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		int indent = 16;
		gdTmp.horizontalIndent = indent;
		lblList.setLayoutData(gdTmp);

		table = new Table (mainComposite, SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gdTmp = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gdTmp.horizontalIndent = indent;
		gdTmp.heightHint = 250; // To avoid filling down the screen
		table.setLayoutData(gdTmp);

		//--click updates button states--
		table.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event event) {
				if ( event.detail!=SWT.CHECK ) {
					
				}
			}
		});		
		
		chkUpdateAll = new Button(mainComposite, SWT.CHECK);
		chkUpdateAll.setText("Escape all extended characters");
		gdTmp = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		gdTmp.horizontalIndent = indent;
		chkUpdateAll.setLayoutData(gdTmp);

		int buttonWidth = 170;
		btnFirstOption = new Button(mainComposite, SWT.PUSH);
		btnFirstOption.setText("All But Marks");
		gdTmp = new GridData();
		gdTmp.horizontalIndent = indent;
		btnFirstOption.setLayoutData(gdTmp);
		UIUtil.ensureWidth(btnFirstOption, buttonWidth);
		btnFirstOption.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String selectList = " `@#$^&+={}|[]\\:\";<>,?/";
				for ( int i=0; i<table.getItemCount(); i++ ) {
					TableItem ti = table.getItem(i);
					if(selectList.contains(ti.getText(0))){
						ti.setChecked(true);
					}else{
						ti.setChecked(false);
					}
				};
			}
		});		

		btnSecondOption = new Button(mainComposite, SWT.PUSH);
		btnSecondOption.setText("All But Marks And Reserved");
		gdTmp = new GridData();
		btnSecondOption.setLayoutData(gdTmp);
		UIUtil.ensureWidth(btnSecondOption, buttonWidth);
		btnSecondOption.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String selectList = " `#^{}|[]\\\"<>";
				for ( int i=0; i<table.getItemCount(); i++ ) {
					TableItem ti = table.getItem(i);
					if(selectList.contains(ti.getText(0))){
						ti.setChecked(true);
					}else{
						ti.setChecked(false);
					}
				};
			}
		});	

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
		if(params.getConversionType()==0){
			chkUnescape.setSelection(true);
			chkEscape.setSelection(false);
			
			table.setEnabled(false);
			chkUpdateAll.setEnabled(false);
			btnFirstOption.setEnabled(false);
			btnSecondOption.setEnabled(false);
			
		}else{
			chkUnescape.setSelection(false);
			chkEscape.setSelection(true);
			
			table.setEnabled(true);
			chkUpdateAll.setEnabled(true);
			btnFirstOption.setEnabled(true);
			btnSecondOption.setEnabled(true);			
		}
		chkUpdateAll.setSelection(params.getUpdateAll());
		
		String allItems = " ~`!@#$^&*() +-={}|[]\\:\";'<>,.?/";
		String selList = params.getEscapeList();
		
		int len = allItems.length();
        for (int i = 0; i < len; i++) {
        	TableItem ti = new TableItem (table, SWT.NONE);
			ti.setText (""+allItems.charAt(i));
			if(selList.contains(""+allItems.charAt(i))){
				ti.setChecked(true);
			}
        } 		
	}

	private boolean saveData () {
		params.reset();
		
		if(chkUnescape.getSelection()){
			params.setConversionType(0);
		}else{
			params.setConversionType(1);
		}
		params.setUpdateAll(chkUpdateAll.getSelection());
		StringBuilder selectedItems = new StringBuilder();
		selectedItems.append("%");
		for ( int i=0; i<table.getItemCount(); i++ ) {
			TableItem ti = table.getItem(i);
			if(ti.getChecked()){
				selectedItems.append(ti.getText(0));
			}
		};
		params.setEscapeList(selectedItems.toString());
		result = true;
		return result;
	}
}
