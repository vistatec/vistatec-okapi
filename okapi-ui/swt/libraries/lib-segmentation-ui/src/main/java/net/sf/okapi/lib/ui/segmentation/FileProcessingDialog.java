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

package net.sf.okapi.lib.ui.segmentation;

import java.io.File;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FileProcessingDialog {
	
	private Shell shell;
	private Text edInput;
	private Text edOutput;
	private Button chkHtmlOutput;
	private String[] result = null;
	private IHelp help;

	public FileProcessingDialog (Shell parent,
		IHelp helpParam)
	{
		help = helpParam;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText(Res.getString("testFileDlg.caption")); //$NON-NLS-1$
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());
		
		Composite cmpTmp = new Composite(shell, SWT.BORDER);
		cmpTmp.setLayout(new GridLayout(2, false));
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		cmpTmp.setLayoutData(gdTmp);

		Label label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("testFileDlg.inputPath")); //$NON-NLS-1$
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		label.setLayoutData(gdTmp);
		
		edInput = new Text(cmpTmp, SWT.BORDER | SWT.SINGLE);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		edInput.setLayoutData(gdTmp);
		
		Button btGetInput = new Button(cmpTmp, SWT.PUSH);
		btGetInput.setText("..."); //$NON-NLS-1$
		btGetInput.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String[] paths = Dialogs.browseFilenames(shell, Res.getString("testFileDlg.getInputCaption"), false, //$NON-NLS-1$
					Util.getDirectoryName(edInput.getText()), Res.getString("testFileDlg.getInputFileTypes"), Res.getString("testFileDlg.getInputFilter")); //$NON-NLS-1$ //$NON-NLS-2$
				if ( paths == null ) return;
				edInput.setText(paths[0]);
				edInput.selectAll();
				edInput.setFocus();
				updateOutputPath();
			}
		});

		label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("testFileDlg.outputPath")); //$NON-NLS-1$
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		label.setLayoutData(gdTmp);
		
		edOutput = new Text(cmpTmp, SWT.BORDER | SWT.SINGLE);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		edOutput.setLayoutData(gdTmp);

		Button btGetOutput = new Button(cmpTmp, SWT.PUSH);
		btGetOutput.setText("..."); //$NON-NLS-1$
		btGetOutput.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String path = Dialogs.browseFilenamesForSave(shell, Res.getString("testFileDlg.getOutputCaption"), //$NON-NLS-1$
					edInput.getText(), null, 
					Res.getString("testFileDlg.getOutputFileTypes"), Res.getString("testFileDlg.getOutputFilter")); //$NON-NLS-1$ //$NON-NLS-2$
				if ( path == null ) return;
				edOutput.setText(path);
				edOutput.selectAll();
				edOutput.setFocus();
			}
		});

		chkHtmlOutput = new Button(cmpTmp, SWT.CHECK);
		chkHtmlOutput.setText(Res.getString("testFileDlg.createHTML")); //$NON-NLS-1$
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkHtmlOutput.setLayoutData(gdTmp);
		chkHtmlOutput.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateOutputPath();
			}
		});
		
		//--- Dialog-level buttons

		SelectionAdapter okCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = null;
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					if ( help != null ) help.showWiki("Ratel - Test Segmentation on a File");
					return;
				}
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					if ( !saveData() ) return;
				}
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, okCancelActions, true);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 600 ) startSize.x = 600;
		shell.setSize(startSize);
		Dialogs.centerWindow(shell, parent);
	}
	
	public String[] showDialog (String inputPath,
		String outputPath,
		boolean htmlOutput)
	{
		shell.open();
		if ( inputPath != null ) edInput.setText(inputPath);
		if ( outputPath != null ) edOutput.setText(outputPath);
		chkHtmlOutput.setSelection(htmlOutput);
		updateOutputPath();
		
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	private boolean saveData () {
		try {
			result = null;
			if ( edInput.getText().length() == 0 ) {
				edInput.selectAll();
				edInput.setFocus();
				return false;
			}
			if ( edOutput.getText().length() == 0 ) {
				edOutput.selectAll();
				edOutput.setFocus();
				return false;
			}
			result = new String[3];
			result[0] = edInput.getText();
			result[1] = edOutput.getText();
			result[2] = (chkHtmlOutput.getSelection() ? "html" : null); //$NON-NLS-1$
			return true;
		}
		catch ( Exception e) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
			return false;
		}
	}

	private String makeHtmlOutputPath (String inputPath) {
		if ( inputPath.length() == 0 ) return ""; //$NON-NLS-1$
		return inputPath + ".html"; //$NON-NLS-1$
	}
	
	private String makeNonHtmlOutputPath (String inputPath) {
		if ( inputPath.length() == 0 ) return ""; //$NON-NLS-1$
		String ext = Util.getExtension(inputPath);
		String filename = Util.getFilename(inputPath, false);
		return Util.getDirectoryName(inputPath) + File.separator +
			filename + Res.getString("testFileDlg.outputExtension") + ext; //$NON-NLS-1$
	}
	
	private void updateOutputPath () {
		if ( chkHtmlOutput.getSelection() ) {
			edOutput.setText(makeHtmlOutputPath(edInput.getText()));
		}
		else {
			edOutput.setText(makeNonHtmlOutputPath(edInput.getText()));
		}
	}
}
