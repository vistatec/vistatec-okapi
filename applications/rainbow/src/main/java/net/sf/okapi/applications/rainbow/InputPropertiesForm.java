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

package net.sf.okapi.applications.rainbow;

import net.sf.okapi.applications.rainbow.lib.FilterConfigSelectionPanel;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class InputPropertiesForm {
	
	private Shell shell;
	private Text edSrcEncoding;
	private Text edTrgEncoding;
	private String[] results;
	private OKCancelPanel pnlActions;
	private FilterConfigSelectionPanel pnlFilterConfigSelection;
	private IHelp help;
	private String oldData;

	InputPropertiesForm (Shell parent,
		IHelp helpParam,
		FilterConfigurationMapper fcMapper,
		Project project,
		String projectDir)
	{
		help = helpParam;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText(Res.getString("INPROP_CAPTION")); //$NON-NLS-1$
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());
		
		Group grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		grpTmp.setLayout(new GridLayout(1, false));
		grpTmp.setText(Res.getString("INPROP_GRPPARAMS")); //$NON-NLS-1$

		pnlFilterConfigSelection = new FilterConfigSelectionPanel(grpTmp, help,
			SWT.NONE, fcMapper, projectDir);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		pnlFilterConfigSelection.setLayoutData(gdTmp);
		
		grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpTmp.setLayout(new GridLayout(2, false));
		grpTmp.setText(Res.getString("INPROP_GRPENCODINGS")); //$NON-NLS-1$
		
		new Label(grpTmp, SWT.NONE); // Place-holder
		
		Label label = new Label(grpTmp, SWT.NONE);
		label.setText(Res.getString("INPROP_ENCNOTE")); //$NON-NLS-1$

		label = new Label(grpTmp, SWT.NONE);
		label.setText(Res.getString("INPROP_SRCENCODING")); //$NON-NLS-1$
		
		edSrcEncoding = new Text(grpTmp, SWT.BORDER | SWT.SINGLE);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		edSrcEncoding.setLayoutData(gdTmp);
		
		label = new Label(grpTmp, SWT.NONE);
		label.setText(Res.getString("INPROP_TRGENCODING")); //$NON-NLS-1$
		edTrgEncoding = new Text(grpTmp, SWT.BORDER | SWT.SINGLE);
		edTrgEncoding.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Dialog-level buttons
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				results = null;
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					if ( help != null ) help.showWiki("Rainbow - Input Document Properties");
					return;
				}
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					if ( !saveData() ) return;
				}
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		pnlActions.setLayoutData(gdTmp);
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		if ( Rect.width < 500 ) Rect.width = 500;
		if ( Rect.height < 450 ) Rect.height = 450;
		shell.setSize(Rect.width, Rect.height);
		Dialogs.centerWindow(shell, parent);
	}
	
	String[] showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return results;
	}

	void setData (String filterSettings,
		String sourceEncoding,
		String targetEncoding,
		FilterConfigurationMapper fcMapper)
	{
		oldData = filterSettings + sourceEncoding + targetEncoding;
		pnlFilterConfigSelection.setConfigurationId(filterSettings);
		edSrcEncoding.setText(sourceEncoding);
		edTrgEncoding.setText(targetEncoding);
	}

	private boolean saveData () {
		try {
			results = new String[4];
			results[0] = pnlFilterConfigSelection.getConfigurationId();
			//TODO: Check if the parameters are still OK.
			results[1] = edSrcEncoding.getText();
			results[2] = edTrgEncoding.getText();
			if ( !oldData.equals(results[0]+results[1]+results[2]) ) {
				results[3] = "!"; //$NON-NLS-1$
			} // Else: null means no changes
		}
		catch ( Exception E ) {
			results = null;
			return false;
		}
		return true;
	}
}
