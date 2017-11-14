/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.filters.mif.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.filters.InlineCodeFinderPanel;
import net.sf.okapi.filters.mif.Parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

@EditorFor(Parameters.class)
public class Editor implements IParametersEditor {
	
	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private Button chkUseCodeFinder;
	private InlineCodeFinderPanel pnlCodeFinder;
	private IHelp help;
	private Button chkExtractBodyPages;
	private Button chkExtractHiddenPages;
	private Button chkExtractMasterPages;
	private Button chkExtractReferencePages;
	private Button chkExtractVariables;
	private Button chkExtractIndexMarkers;
	private Button chkExtractLinks;

	public boolean edit (IParameters params,
		boolean readOnly,
		IContext context)
	{
		help = (IHelp)context.getObject("help");
		boolean bRes = false;
		shell = null;
		this.params = (Parameters)params;
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
		return new Parameters();
	}
	
	private void create (Shell parent,
		boolean readOnly)
	{
		shell.setText(Res.getString("EditorCaption"));
		if ( parent != null ) shell.setImage(parent.getImage());
		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		TabFolder tfTmp = new TabFolder(shell, SWT.NONE);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		tfTmp.setLayoutData(gdTmp);

		//--- Options tab
		
		Composite cmpTmp = new Composite(tfTmp, SWT.NONE);
		layTmp = new GridLayout();
		cmpTmp.setLayout(layTmp);
		
		chkExtractVariables = new Button(cmpTmp, SWT.CHECK);
		chkExtractVariables.setText(Res.getString("extractVariables"));
		
		chkExtractIndexMarkers = new Button(cmpTmp, SWT.CHECK);
		chkExtractIndexMarkers.setText(Res.getString("extractIndexMarkers"));
		
		chkExtractLinks = new Button(cmpTmp, SWT.CHECK);
		chkExtractLinks.setText(Res.getString("extractLinks"));
		
		Group grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setText(Res.getString("pageTypesExtraction"));
		grpTmp.setLayout(new GridLayout());
		gdTmp = new GridData(); gdTmp.verticalIndent = 8;
		grpTmp.setLayoutData(gdTmp);
		
		chkExtractBodyPages = new Button(grpTmp, SWT.CHECK);
		chkExtractBodyPages.setText(Res.getString("extractBodyPages"));

		chkExtractHiddenPages = new Button(grpTmp, SWT.CHECK);
		chkExtractHiddenPages.setText(Res.getString("extractHiddenPages"));

		chkExtractMasterPages = new Button(grpTmp, SWT.CHECK);
		chkExtractMasterPages.setText(Res.getString("extractMasterPages"));

		chkExtractReferencePages = new Button(grpTmp, SWT.CHECK);
		chkExtractReferencePages.setText(Res.getString("extractReferencePages"));

		TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText(Res.getString("tabOptions"));
		tiTmp.setControl(cmpTmp);
		
		//--- Inline tab
		
		cmpTmp = new Composite(tfTmp, SWT.NONE);
		layTmp = new GridLayout();
		cmpTmp.setLayout(layTmp);
		
		chkUseCodeFinder = new Button(cmpTmp, SWT.CHECK);
		chkUseCodeFinder.setText(Res.getString("hasInlineCodes"));
		chkUseCodeFinder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateInlineCodes();
			};
		});
		
		pnlCodeFinder = new InlineCodeFinderPanel(cmpTmp, SWT.NONE);
		pnlCodeFinder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText(Res.getString("tabInlineCodes"));
		tiTmp.setControl(cmpTmp);
			

		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("MIF Filter"); // Wiki page
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					if ( !saveData() ) return;
					result = true;
				}
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		pnlActions.setLayoutData(gdTmp);
		pnlActions.btOK.setEnabled(!readOnly);
		if ( !readOnly ) {
			shell.setDefaultButton(pnlActions.btOK);
		}

		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
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
		chkExtractBodyPages.setSelection(params.getExtractBodyPages());
		chkExtractHiddenPages.setSelection(params.getExtractHiddenPages());
		chkExtractMasterPages.setSelection(params.getExtractMasterPages());
		chkExtractReferencePages.setSelection(params.getExtractReferencePages());
		chkExtractVariables.setSelection(params.getExtractVariables());
		chkExtractIndexMarkers.setSelection(params.getExtractIndexMarkers());
		chkExtractLinks.setSelection(params.getExtractLinks());
		chkUseCodeFinder.setSelection(params.getUseCodeFinder());
		pnlCodeFinder.setRules(params.getCodeFinderData());
		updateInlineCodes();
		pnlCodeFinder.updateDisplay();
	}

	private boolean saveData () {
		if ( chkUseCodeFinder.getSelection() ) {
			if ( pnlCodeFinder.getRules() == null ) {
				return false;
			}
			else {
				params.setCodeFinderData(pnlCodeFinder.getRules());
			}
		}

		params.setExtractBodyPages(chkExtractBodyPages.getSelection());
		params.setExtractHiddenPages(chkExtractHiddenPages.getSelection());
		params.setExtractMasterPages(chkExtractMasterPages.getSelection());
		params.setExtractReferencePages(chkExtractReferencePages.getSelection());
		params.setExtractVariables(chkExtractVariables.getSelection());
		params.setExtractIndexMarkers(chkExtractIndexMarkers.getSelection());
		params.setExtractLinks(chkExtractLinks.getSelection());
		params.setUseCodeFinder(chkUseCodeFinder.getSelection());
		return true;
	}
	
	private void updateInlineCodes () {
		pnlCodeFinder.setEnabled(chkUseCodeFinder.getSelection());
	}

}
