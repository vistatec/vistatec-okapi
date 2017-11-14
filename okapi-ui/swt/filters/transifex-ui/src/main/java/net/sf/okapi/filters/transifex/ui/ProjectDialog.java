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
===========================================================================*/

package net.sf.okapi.filters.transifex.ui;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.filters.transifex.IProjectEditor;
import net.sf.okapi.filters.transifex.Project;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class ProjectDialog implements IProjectEditor {
	
	private Shell shell;
	private Project project;
	private ProjectTableModel tableMod;
	private SelectionAdapter CloseActions;
	private boolean result;
	private Text edHost;
	private Text edProjectId;
	private Text edUser;
	private Text edPassword;
	private Text edSource;
	private Text edTarget;
	private Button chkProtectApproved;

	public ProjectDialog () {
		// Needed to be able to instantiate this class with Class.forName().
	}
	
	private void create (Shell parent,
		boolean inProcess)
	{
		result = false;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Transifex Project");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());
		
		Composite cmpTmp = new Composite(shell, SWT.BORDER);
		cmpTmp.setLayout(new GridLayout(4, false));
		cmpTmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(cmpTmp, SWT.NONE);
		label.setText("Project Id:");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		edProjectId = new Text(cmpTmp, SWT.BORDER);
		edProjectId.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		label = new Label(cmpTmp, SWT.NONE);
		label.setText("Host:");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		edHost = new Text(cmpTmp, SWT.BORDER);
		edHost.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		label = new Label(cmpTmp, SWT.NONE);
		label.setText("Source:");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		edSource = new Text(cmpTmp, SWT.BORDER);
		edSource.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		edSource.setEditable(false);
		
		label = new Label(cmpTmp, SWT.NONE);
		label.setText("User name:");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		edUser = new Text(cmpTmp, SWT.BORDER);
		edUser.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		
		label = new Label(cmpTmp, SWT.NONE);
		label.setText("Target:");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		edTarget = new Text(cmpTmp, SWT.BORDER);
		edTarget.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		edTarget.setEditable(false);
		
		label = new Label(cmpTmp, SWT.NONE);
		label.setText("Password:");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		edPassword = new Text(cmpTmp, SWT.BORDER);
		edPassword.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		edPassword.setEchoChar('*');
		
		chkProtectApproved = new Button(cmpTmp, SWT.CHECK);
		chkProtectApproved.setText("Protect approved entries (entries not empty and not fuzzy)");
		GridData gdTmp = new GridData();
		gdTmp.horizontalSpan = 4;
		chkProtectApproved.setLayoutData(gdTmp);
		
		Button btRefresh = UIUtil.createGridButton(shell, SWT.PUSH, "Refresh Resources List", UIUtil.BUTTON_DEFAULT_WIDTH*2, 1);
		btRefresh.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refresh();
			};
		});

		cmpTmp = new Composite(shell, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		cmpTmp.setLayout(layout);
		cmpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Table tableDocs = new Table(cmpTmp, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.minimumHeight = 300;
		gdTmp.minimumWidth = 550;
		tableDocs.setLayoutData(gdTmp);
		tableDocs.setHeaderVisible(true);
		tableDocs.setLinesVisible(true);
		tableMod = new ProjectTableModel();
		tableMod.linkTable(tableDocs);

		//--- Dialog-level buttons

		CloseActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					// Don't use context because this dialog box may be used from anywhere
					Util.openWikiTopic("Transifex Filter");
					return;
				}
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					saveData();
					project.save();
					result = true;
				}
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, CloseActions, true,
			(inProcess ? "Continue" : "OK"));
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shell.setDefaultButton(btRefresh);

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 450 ) startSize.x = 450;
		shell.setSize(startSize);

    	Rectangle rect = tableDocs.getClientArea();
		int nPart = (int)(rect.width / 100);
		tableDocs.getColumn(0).setWidth(55*nPart);
		tableDocs.getColumn(1).setWidth(rect.width-(55*nPart));
		
		Dialogs.centerWindow(shell, parent);
	}
	
	private void setData (Project project) {
		this.project = project;
		edHost.setText(project.getHost());
		edProjectId.setText(project.getProjectId());
		edUser.setText(project.getUser());
		edPassword.setText(project.getPassword());
		edSource.setText(project.getSourceLocale().toString());
		edTarget.setText(project.getTargetLocale().toString());
		chkProtectApproved.setSelection(project.getProtectApproved());
	}
	
	private boolean saveData () {
		String tmp = edHost.getText().trim();
		if ( tmp.isEmpty() ) {
			return false;
		}
		project.setHost(tmp);

		tmp = edUser.getText().trim();
		if ( tmp.isEmpty() ) {
			return false;
		}
		project.setUser(tmp);
		
		tmp = edPassword.getText().trim();
		if ( tmp.isEmpty() ) {
			return false;
		}
		project.setPassword(tmp);
		
		tmp = edProjectId.getText().trim();
		if ( tmp.isEmpty() ) {
			return false;
		}
		project.setProjectId(tmp);
		
		tmp = edSource.getText().trim();
		if ( tmp.isEmpty() ) {
			return false;
		}
		project.setSourceLocale(LocaleId.fromString(tmp));
		
		tmp = edTarget.getText().trim();
		if ( tmp.isEmpty() ) {
			return false;
		}
		project.setTargetLocale(LocaleId.fromString(tmp));
		
		project.setProtectApproved(chkProtectApproved.getSelection());
		tableMod.saveData();
		return true;
	}
	
	private boolean showDialog (Project project) {
		tableMod.setProject(project);
		setData(project);
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	@Override
	public boolean edit (Object parent,
		Project project,
		boolean inProcess)
	{
		Shell shell = null;
		if (( parent != null ) && ( parent instanceof Shell )) {
			shell = (Shell)parent;
		}
		create(shell, inProcess);
		boolean res = showDialog(project);
		
		return res;
	}

	private void refresh () {
		try {
			if ( !saveData() ) return;
			project.refreshResources(false);
			tableMod.setProject(project);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, "Error during the refresh:\n"+e.getMessage(), null);
		}
	}

}
