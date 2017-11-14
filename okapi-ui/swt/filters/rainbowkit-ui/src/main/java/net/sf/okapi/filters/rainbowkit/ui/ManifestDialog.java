/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.rainbowkit.ui;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.filters.rainbowkit.IManifestEditor;
import net.sf.okapi.filters.rainbowkit.Manifest;

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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class ManifestDialog implements IManifestEditor {
	
	private Shell shell;
	private Manifest manifest;
	private ManifestTableModel tableMod;
	private SelectionAdapter CloseActions;
	private boolean result;
	private Text edProjectId;
	private Text edPkgID;
	private Text edSource;
	private Text edTarget;
	private Text edDate;
	private Text edTargetDirectory;
	private Button chkUseApprovedOnly;
	private Button chkUpdateApprovedFlag;

	public ManifestDialog () {
		// Needed to be able to instantiate this class with Class.forName().
	}
	
	private void create (Shell parent,
		boolean inProcess)
	{
		result = false;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Translation Kit Manifest");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());
		
		TabFolder tfTmp = new TabFolder(shell, SWT.NONE);
		tfTmp.setLayoutData(new GridData(GridData.FILL_BOTH));

		//--- Documents tab
		
		Composite cmpTmp = new Composite(tfTmp, SWT.NONE);
		cmpTmp.setLayout(new GridLayout());
		TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Documents");
		tiTmp.setControl(cmpTmp);
		
		final Table tableDocs = new Table(cmpTmp, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.minimumHeight = 300;
		gdTmp.minimumWidth = 550;
		tableDocs.setLayoutData(gdTmp);
		tableDocs.setHeaderVisible(true);
		tableDocs.setLinesVisible(true);
		tableMod = new ManifestTableModel();
		tableMod.linkTable(tableDocs);

		edTargetDirectory = new Text(cmpTmp, SWT.BORDER);
		edTargetDirectory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edTargetDirectory.setEditable(false);
		
		//--- Options tab
		
		cmpTmp = new Composite(tfTmp, SWT.NONE);
		cmpTmp.setLayout(new GridLayout());
		tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Options");
		tiTmp.setControl(cmpTmp);
		
		chkUseApprovedOnly = new Button(cmpTmp, SWT.CHECK);
		chkUseApprovedOnly.setText("Merge the translation only if it is approved");
		
		chkUpdateApprovedFlag = new Button(cmpTmp, SWT.CHECK);
		chkUpdateApprovedFlag.setText("Update the 'approved' flag on the merged translations (for applicable formats)");

		//--- Information tab
		
		cmpTmp = new Composite(tfTmp, SWT.NONE);
		cmpTmp.setLayout(new GridLayout(2, false));
		tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Information");
		tiTmp.setControl(cmpTmp);

		Label stTmp = new Label(cmpTmp, SWT.NONE);
		stTmp.setText("Project ID:");
		stTmp.setLayoutData(new GridData());
		edProjectId = new Text(cmpTmp, SWT.BORDER);
		edProjectId.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edProjectId.setEditable(false);
		
		stTmp = new Label(cmpTmp, SWT.NONE);
		stTmp.setText("Package ID:");
		stTmp.setLayoutData(new GridData());
		edPkgID = new Text(cmpTmp, SWT.BORDER);
		edPkgID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edPkgID.setEditable(false);
		
		stTmp = new Label(cmpTmp, SWT.NONE);
		stTmp.setText("Source locale:");
		stTmp.setLayoutData(new GridData());
		edSource = new Text(cmpTmp, SWT.BORDER);
		edSource.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edSource.setEditable(false);
		
		stTmp = new Label(cmpTmp, SWT.NONE);
		stTmp.setText("Target locale:");
		stTmp.setLayoutData(new GridData());
		edTarget = new Text(cmpTmp, SWT.BORDER);
		edTarget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edTarget.setEditable(false);
		
		stTmp = new Label(cmpTmp, SWT.NONE);
		stTmp.setText("Creation date:");
		stTmp.setLayoutData(new GridData());
		edDate = new Text(cmpTmp, SWT.BORDER);
		edDate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edDate.setEditable(false);
				
		//--- Dialog-level buttons

		CloseActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					// Don't use context because this dialog box may be used from anywhere
					Util.openWikiTopic("Rainbow Translation Kit Manifest");
					return;
				}
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					tableMod.saveData();
					manifest.setUseApprovedOnly(chkUseApprovedOnly.getSelection());
					manifest.setUpdateApprovedFlag(chkUpdateApprovedFlag.getSelection());
					manifest.save(null);
					result = true;
				}
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, CloseActions, true,
			(inProcess ? "Continue" : "OK"));
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 450 ) startSize.x = 450;
		shell.setSize(startSize);

    	Rectangle rect = tableDocs.getClientArea();
		int nPart = (int)(rect.width / 100);
		tableDocs.getColumn(0).setWidth(45*nPart);
		tableDocs.getColumn(1).setWidth(15*nPart);
		tableDocs.getColumn(2).setWidth(rect.width-(60*nPart));
		
		Dialogs.centerWindow(shell, parent);
	}
	
	private void setData (Manifest manifest) {
		this.manifest = manifest;
		edTargetDirectory.setText(manifest.getTempTargetDirectory());
		edProjectId.setText(manifest.getProjectId());
		edPkgID.setText(manifest.getPackageId());
		edSource.setText(manifest.getSourceLocale().toString());
		edTarget.setText(manifest.getTargetLocale().toString());
		edDate.setText(manifest.getDate());
		chkUseApprovedOnly.setSelection(manifest.getUseApprovedOnly());
		chkUpdateApprovedFlag.setSelection(manifest.getUpdateApprovedFlag());
	}
	
	private boolean showDialog (Manifest manifest) {
		tableMod.setManifest(manifest);
		setData(manifest);
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	@Override
	public boolean edit (Object parent,
		Manifest manifest,
		boolean inProcess)
	{
		Shell shell = null;
		if (( parent != null ) && ( parent instanceof Shell )) {
			shell = (Shell)parent;
		}
		create(shell, inProcess);
		boolean res = showDialog(manifest);
		
		return res;
	}

}
