/*===========================================================================
  Copyright (C) 2011-2013 by the Okapi Framework contributors
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

import net.sf.okapi.common.IWaitDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Default implementation of the IWaitDialog interface.
 */
public class WaitDialog implements IWaitDialog {

	private Shell shell;
	private int result;

	public WaitDialog () {
		// Need for runtime creation
	}
	
	private void createDialog (String caption,
		String text,
		String okLabel)
	{
		// Take the opportunity to do some clean up if possible
		Runtime rt = Runtime.getRuntime();
		rt.runFinalization();
		rt.gc();

		Shell parent = null;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText(caption);
		shell.setLayout(new GridLayout());
			
		Composite cmpTmp = new Composite(shell, SWT.BORDER);
		cmpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layTmp = new GridLayout(2, false);
		cmpTmp.setLayout(layTmp);

		Label label = new Label(cmpTmp, SWT.NONE);
		label.setText(text);
		GridData gdTmp = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL
			| GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_VERTICAL);
		label.setLayoutData(gdTmp);

		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = 0;
				if ( e.widget.getData().equals("h") ) {
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					result = 1;
				}
				shell.close();
			};
		};

		OKCancelPanel pnlActionsDialog = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, false);
		pnlActionsDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pnlActionsDialog.setOKText(okLabel);
		shell.setDefaultButton(pnlActionsDialog.btOK);
		
		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 350 ) startSize.x = 350;
		shell.setSize(startSize);
		Dialogs.centerWindow(shell, parent);
	}

	@Override
	public int waitForUserInput (String message,
		String okLabel)
	{
		result = 0;
		createDialog("Waiting User Input", message, okLabel);
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

}
