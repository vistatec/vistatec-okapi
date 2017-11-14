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

package net.sf.okapi.lib.ui.verification;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.TextOptions;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.ui.editor.TextOptionsPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

class PreferencesDialog {
	
	private Shell dialog;
	private Object[] result = null;
	private OKCancelPanel pnlActions;
	private IHelp help;
	private TextOptionsPanel pnlSourceOptions;
	private TextOptionsPanel pnlTargetOptions;
	
	public PreferencesDialog (Shell parent, IHelp paramHelp) {

		help = paramHelp;
		dialog = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		dialog.setText("User Preferences");
		dialog.setLayout(new GridLayout());
		dialog.setLayoutData(new GridData(GridData.FILL_BOTH));
		UIUtil.inheritIcon(dialog, parent);

		Group grpTmp = new Group(dialog, SWT.NONE);
		grpTmp.setText("Edit Fields");
		grpTmp.setLayout(new GridLayout(1, false));
		grpTmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		pnlSourceOptions = new TextOptionsPanel(grpTmp, SWT.NONE, "Source edit field:", null);
		pnlSourceOptions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		pnlTargetOptions = new TextOptionsPanel(grpTmp, SWT.NONE, "Target edit field:", null);
		pnlTargetOptions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = null;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("CheckMate - User Preferences");
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					result = new Object[2];
					result[0] = pnlSourceOptions.getOptions();
					result[1] = pnlTargetOptions.getOptions();
				}
				dialog.close();
			};
		};
		pnlActions = new OKCancelPanel(dialog, SWT.NONE, OKCancelActions, true);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		pnlActions.setLayoutData(gdTmp);

		dialog.pack();
		Point size = dialog.getSize();
		dialog.setMinimumSize(size);
		Dialogs.centerWindow(dialog, parent);
	}

	public void setData (TextOptions srcOptions,
		TextOptions trgOptions)
	{
		pnlSourceOptions.setOptions(srcOptions);
		pnlTargetOptions.setOptions(trgOptions);
	}
	
	public Object[] showDialog () {
		dialog.open();
		while ( !dialog.isDisposed() ) {
			if ( !dialog.getDisplay().readAndDispatch() )
				dialog.getDisplay().sleep();
		}
		return result;
	}

	public TextOptions getSourceTextOptions () {
		return pnlSourceOptions.getOptions();
	}

	public TextOptions getTargetTextOptions () {
		return pnlTargetOptions.getOptions();
	}

}
