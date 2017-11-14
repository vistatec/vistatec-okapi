/*===========================================================================
  Copyright (C) 2010-2012 by the Okapi Framework contributors
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

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class InputDocumentDialog {
	
	private Shell shell;
	private Object[] result;
	private InputDocumentPanel pnlMain;
	private Button chkAcceptAll;
	private OKCancelPanel pnlActions;

	public InputDocumentDialog (Shell parent,
		String captionText,
		IFilterConfigurationMapper fcMapper,
		boolean batchMode)
	{
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		if ( captionText != null ) shell.setText(captionText);
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());

		Composite cmpTmp = new Composite(shell, SWT.BORDER);
		cmpTmp.setLayout(new GridLayout());
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		cmpTmp.setLayoutData(gdTmp);

		// Panel
		pnlMain = new InputDocumentPanel(cmpTmp, SWT.NONE, 1, "Input document:", null, fcMapper);

		// Optional accept-all check box
		if ( batchMode ) {
			chkAcceptAll = new Button(cmpTmp, SWT.CHECK);
			chkAcceptAll.setText("Accept all next documents with their defaults.");
			gdTmp = new GridData(GridData.FILL_HORIZONTAL);
			gdTmp.verticalIndent = 8;
			chkAcceptAll.setLayoutData(gdTmp);
		}
		
		//--- Dialog-level buttons
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = null;
				if ( e.widget.getData().equals("h") ) {
					Util.openWikiTopic("Input Document");
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					if ( !saveData() ) return;
				}
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		Point size = shell.getSize();
		shell.setMinimumSize(size);
		if ( size.x < 600 ) size.x = 600;
		shell.setSize(size);
		Dialogs.centerWindow(shell, parent);
	}

	/**
	 * Opens the dialog box.
	 * @return Null if the user cancel the operation.
	 * Or an array of objects: 0=path of the document, 1=filter configuration id,
	 * 2=encoding, 3=source locale, 4=first target locale, 5=true to accept all next open or false to prompt.
	 */
	public Object[] showDialog () {
		// Open the dialog box
		shell.open();
		// If accept-all is active, checks its value
		if ( chkAcceptAll != null ) {
			if ( chkAcceptAll.getSelection() ) {
				// And auto-save and auto-OK if we can
				if ( saveData() ) {
					shell.close();
					return result;
				}
			}
		}
		
		// Else: start the event loop
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	public void setAcceptAll (boolean acceptAll) {
		if ( chkAcceptAll != null ) {
			chkAcceptAll.setSelection(acceptAll);
		}
	}
	
	public void setData (String path,
		String configId,
		String encoding,
		LocaleId srcLoc,
		LocaleId trgLoc)
	{
		pnlMain.setDocumentPath(path==null ? "" : path);
		pnlMain.setFilterConfigurationId(configId);
		pnlMain.setEncoding(encoding);
		pnlMain.setSourceLocale(srcLoc);
		pnlMain.setTargetLocale(trgLoc);
		// If no configuration is given, but we have a path: try to guess the configuration
		if ( pnlMain.getFilterConfigurationId().isEmpty() && !pnlMain.getDocumentPath().isEmpty() ) {
			pnlMain.guessConfiguration();
		}
		if ( !pnlMain.getDocumentPath().isEmpty() ) {
			pnlMain.guessLocales();
		}
	}
	
	public void setLocalesEditable (boolean editable) {
		pnlMain.setLocalesEditable(editable);
	}
	
	private boolean saveData () {
		result = null;
		if ( !pnlMain.validate(true) ) return false;
		result = new Object[6];
		result[0] = pnlMain.getDocumentPath();
		result[1] = pnlMain.getFilterConfigurationId();
		result[2] = pnlMain.getEncoding();
		result[3] = pnlMain.getSourceLocale();
		result[4] = pnlMain.getTargetLocale();
		result[5] = chkAcceptAll==null ? false : chkAcceptAll.getSelection(); 
		return true;
	}

}
