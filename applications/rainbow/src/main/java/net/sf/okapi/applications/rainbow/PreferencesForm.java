/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow;

import java.io.File;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.UserConfiguration;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.TextAndBrowsePanel;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

class PreferencesForm {
	
	private Shell shell;
	private IHelp help;
	private Button rdStartPrjDoNotLoad;
	private Button rdStartPrjAsk;
	private Button rdStartPrjLoad;
	private Button chkAlwaysOpenLog;
	private Button chkAllowDuplicateInputs;
	private Button chkUseUserDefaults;
	private Combo cbLogLevel;
	private UserConfiguration config;
	private TextAndBrowsePanel pnlDropinsDir;
	private TextAndBrowsePanel pnlParamsDir;

	PreferencesForm (Shell p_Parent,
		IHelp helpParam)
	{
		help = helpParam;
		shell = new Shell(p_Parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText(Res.getString("PreferencesForm.caption")); //$NON-NLS-1$
		UIUtil.inheritIcon(shell, p_Parent);
		shell.setLayout(new GridLayout());
		
		Group grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText(Res.getString("PreferencesForm.initialProjectGroup")); //$NON-NLS-1$
		grpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		grpTmp.setLayout(new GridLayout());

		rdStartPrjDoNotLoad = new Button(grpTmp, SWT.RADIO);
		rdStartPrjDoNotLoad.setText(Res.getString("PreferencesForm.neverLoad")); //$NON-NLS-1$
		
		rdStartPrjAsk = new Button(grpTmp, SWT.RADIO);
		rdStartPrjAsk.setText(Res.getString("PreferencesForm.askUser")); //$NON-NLS-1$
		
		rdStartPrjLoad = new Button(grpTmp, SWT.RADIO);
		rdStartPrjLoad.setText(Res.getString("PreferencesForm.autoLoad")); //$NON-NLS-1$
		
		grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText(Res.getString("PreferencesForm.miscGroup")); //$NON-NLS-1$
		grpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		grpTmp.setLayout(new GridLayout(2, false));
		
		chkAlwaysOpenLog = new Button(grpTmp, SWT.CHECK);
		chkAlwaysOpenLog.setText(Res.getString("PreferencesForm.alwaysOpenLog")); //$NON-NLS-1$
		GridData gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkAlwaysOpenLog.setLayoutData(gdTmp);
		
		chkAllowDuplicateInputs = new Button(grpTmp, SWT.CHECK);
		chkAllowDuplicateInputs.setText(Res.getString("PreferencesForm.allowDuplicated")); //$NON-NLS-1$
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkAllowDuplicateInputs.setLayoutData(gdTmp);
		
		chkUseUserDefaults = new Button(grpTmp, SWT.CHECK);
		chkUseUserDefaults.setText(Res.getString("PreferencesForm.useUserDefaults")); //$NON-NLS-1$
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkUseUserDefaults.setLayoutData(gdTmp);
		
		Label label = new Label(grpTmp, SWT.NONE);
		label.setText(Res.getString("PreferencesForm.logLevel")); //$NON-NLS-1$
		
		cbLogLevel = new Combo(grpTmp, SWT.DROP_DOWN | SWT.READ_ONLY);
		cbLogLevel.add(Res.getString("PreferencesForm.logNormal")); //$NON-NLS-1$
		cbLogLevel.add(Res.getString("PreferencesForm.logDebug")); //$NON-NLS-1$
		cbLogLevel.add(Res.getString("PreferencesForm.logTrace")); //$NON-NLS-1$
		
		grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText("Plugins Location");
		grpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		grpTmp.setLayout(new GridLayout(1, false));

		label = new Label(grpTmp, SWT.NONE);
		label.setText("Enter the directory for the plugins (leave empty to use the default)");
		pnlDropinsDir = new TextAndBrowsePanel(grpTmp, SWT.NONE, true);
		pnlDropinsDir.setTitle("Select the Plugins Location");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.minimumWidth = 500;
		pnlDropinsDir.setLayoutData(gdTmp);

		grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText("Default Custom Parameters Folder");
		grpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		grpTmp.setLayout(new GridLayout(1, false));
		
		label = new Label(grpTmp, SWT.NONE);
		label.setText("Enter the directory for the parameters (leave empty to use the default)");
		pnlParamsDir = new TextAndBrowsePanel(grpTmp, SWT.NONE, true);
		pnlParamsDir.setTitle("Select the Parameters Location");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.minimumWidth = 500;
		pnlParamsDir.setLayoutData(gdTmp);
		
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					if ( help != null ) help.showWiki("Rainbow - User Preferences");
					return;
				}
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					if ( !saveData() ) return;
				}
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		pnlActions.setLayoutData(gdTmp);
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		Dialogs.centerWindow(shell, p_Parent);
	}
	
	void showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
	}

	void setData (UserConfiguration config) {
		this.config = config;
		chkAlwaysOpenLog.setSelection(config.getBoolean(MainForm.OPT_ALWAYSOPENLOG));
		chkAllowDuplicateInputs.setSelection(config.getBoolean(MainForm.OPT_ALLOWDUPINPUT));
		chkUseUserDefaults.setSelection(config.getBoolean(MainForm.OPT_USEUSERDEFAULTS));
		
		int n = config.getInteger(MainForm.OPT_LOADMRU);
		if ( n == 1 ) rdStartPrjAsk.setSelection(true);
		else if ( n == 2 ) rdStartPrjLoad.setSelection(true);
		else rdStartPrjDoNotLoad.setSelection(true);
		n = config.getInteger(MainForm.OPT_LOGLEVEL);
		if (( n < 0 ) || ( n > 2)) n = 0;
		cbLogLevel.select(n);
		
		String tmp = config.getProperty(MainForm.OPT_DROPINSDIR, "");
		if ( tmp.endsWith("/") || tmp.endsWith("\\") ) {
			tmp = tmp.substring(0, tmp.length()-1);
		}
		pnlDropinsDir.setText(tmp);
		
		tmp = config.getProperty(MainForm.OPT_PARAMSDIR, "");
		if ( tmp.endsWith("/") || tmp.endsWith("\\") ) {
			tmp = tmp.substring(0, tmp.length()-1);
		}
		pnlParamsDir.setText(tmp);
	}

	private boolean saveData () {
		try {
			String tmp = pnlDropinsDir.getText().trim();
			if ( tmp.length() > 0 ) {
				if ( tmp.endsWith("/") || tmp.endsWith("\\") ) {
					tmp = tmp.substring(0, tmp.length()-1);
				}
				File file = new File(tmp);
				if ( !file.exists() ) {
					Dialogs.showError(shell, "The directory for the plugins does not exists.", null);
					pnlDropinsDir.setFocus();
					return false;
				}
				if ( !file.isDirectory() ) {
					Dialogs.showError(shell, "The path for the plugins location is not a directory.", null);
					pnlDropinsDir.setFocus();
					return false;
				}
			}
			config.setProperty(MainForm.OPT_DROPINSDIR, tmp);
			
			tmp = pnlParamsDir.getText().trim();
			if ( tmp.length() > 0 ) {
				if ( tmp.endsWith("/") || tmp.endsWith("\\") ) {
					tmp = tmp.substring(0, tmp.length()-1);
				}
				File file = new File(tmp);
				if ( !file.exists() ) {
					Dialogs.showError(shell, "The directory for the parameters does not exists.", null);
					pnlParamsDir.setFocus();
					return false;
				}
				if ( !file.isDirectory() ) {
					Dialogs.showError(shell, "The path for the parameters location is not a directory.", null);
					pnlParamsDir.setFocus();
					return false;
				}
			}
			config.setProperty(MainForm.OPT_PARAMSDIR, tmp);
			
			config.setProperty(MainForm.OPT_ALWAYSOPENLOG, chkAlwaysOpenLog.getSelection());
			config.setProperty(MainForm.OPT_ALLOWDUPINPUT, chkAllowDuplicateInputs.getSelection());
			config.setProperty(MainForm.OPT_USEUSERDEFAULTS, chkUseUserDefaults.getSelection());
			
			if ( rdStartPrjAsk.getSelection() ) config.setProperty(MainForm.OPT_LOADMRU, 1);
			else if ( rdStartPrjLoad.getSelection() ) config.setProperty(MainForm.OPT_LOADMRU, 2);
			else config.setProperty(MainForm.OPT_LOADMRU, 0);
			config.setProperty(MainForm.OPT_LOGLEVEL, cbLogLevel.getSelectionIndex());
		}
		catch ( Exception E ) {
			return false;
		}
		return true;
	}
}
