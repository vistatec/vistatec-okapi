/*===========================================================================
  Copyright (C) 2009-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.json.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.filters.InlineCodeFinderPanel;
import net.sf.okapi.filters.json.Parameters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@EditorFor(Parameters.class)
public class Editor implements IParametersEditor {
	
	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private Button chkExtractStandalone;
	private Button rdExtractAllPairs;
	private Button rdDontExtractPairs;
	private Text edExceptions;
	private Button chkUseKeyAsName;
	private Button rdUseSubfilter;
	private Text edSubfilterId;
	private Button rdUseCodeFinder;
	private InlineCodeFinderPanel pnlCodeFinder;
	private IHelp help;
	private Button chkUseFullKeyPath;
	private Button chkUseLeadingSlashOnKeyPath;
	private Button chkEscapeForwardSlashes;


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

		Group grpTmp = new Group(cmpTmp, SWT.NONE);
		layTmp = new GridLayout();
		grpTmp.setLayout(layTmp);
		grpTmp.setText(Res.getString("grpStandaloneStrings"));
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);

		chkExtractStandalone = new Button(grpTmp, SWT.CHECK);
		chkExtractStandalone.setText(Res.getString("chkExtractStandalone"));

		grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setLayout(new GridLayout());
		grpTmp.setText(Res.getString("grpKeyValuePairs"));
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);
		
		rdExtractAllPairs = new Button(grpTmp, SWT.RADIO);
		rdExtractAllPairs.setText(Res.getString("rdExtractAllPairs"));
		rdDontExtractPairs = new Button(grpTmp, SWT.RADIO);
		rdDontExtractPairs.setText(Res.getString("rdDontExtractPairs"));
		
		Label label = new Label(grpTmp, SWT.NONE);
		label.setText(Res.getString("stExceptions"));
		
		edExceptions = new Text(grpTmp, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		edExceptions.setLayoutData(gdTmp);
		
		chkUseKeyAsName = new Button(grpTmp, SWT.CHECK);
		chkUseKeyAsName.setText(Res.getString("chkKeyAsResname"));
		
		chkUseFullKeyPath = new Button(grpTmp, SWT.CHECK);
		chkUseFullKeyPath.setText(Res.getString("chkUseFullKeyPath"));
		
		chkUseLeadingSlashOnKeyPath = new Button(grpTmp, SWT.CHECK);
		chkUseLeadingSlashOnKeyPath.setText(Res.getString("chkUseLeadingSlashOnKeyPath"));

	    chkUseFullKeyPath.addSelectionListener(new SelectionAdapter() {
			@Override
		    public void widgetSelected(SelectionEvent e) {
	    		chkUseLeadingSlashOnKeyPath.setEnabled(chkUseFullKeyPath.getSelection());
		    }
		});
		
		TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText(Res.getString("tabOptions"));
		tiTmp.setControl(cmpTmp);
		
		//--- Inline tab

		cmpTmp = new Composite(tfTmp, SWT.NONE);
		layTmp = new GridLayout();
		cmpTmp.setLayout(layTmp);

		grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setLayout(layTmp);
		grpTmp.setText(Res.getString("grpEncodingSettings"));
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);

		chkEscapeForwardSlashes = new Button(grpTmp, SWT.CHECK);
		chkEscapeForwardSlashes.setText(Res.getString("chkEscapeForwardSlashes"));

		grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setLayout(layTmp);
		grpTmp.setText(Res.getString("grpContentSettings"));
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);

		rdUseSubfilter = new Button(grpTmp, SWT.RADIO);
		rdUseSubfilter.setText(Res.getString("rdSubfilter"));
		
		edSubfilterId = new Text(grpTmp, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		edSubfilterId.setLayoutData(gdTmp);
		
		rdUseCodeFinder = new Button(grpTmp, SWT.RADIO);
		rdUseCodeFinder.setText(Res.getString("rdInlineCodeFinder"));
		SelectionAdapter listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateInlineCodes();
			};
		};
		rdUseCodeFinder.addSelectionListener(listener);
		rdUseSubfilter.addSelectionListener(listener);
		
		pnlCodeFinder = new InlineCodeFinderPanel(grpTmp, SWT.NONE);
		pnlCodeFinder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText(Res.getString("tabContentProcessing"));
		tiTmp.setControl(cmpTmp);
			

		//--- Output tab
		
		/*cmpTmp = new Composite(tfTmp, SWT.NONE);
		layTmp = new GridLayout();
		cmpTmp.setLayout(layTmp);
		
		grpTmp = new Group(cmpTmp, SWT.NONE);
		layTmp = new GridLayout();
		grpTmp.setLayout(layTmp);
		grpTmp.setText(Res.getString("grpExtendedChars"));
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);

		chkEscapeExtendedChars = new Button(grpTmp, SWT.CHECK);
		chkEscapeExtendedChars.setText(Res.getString("chkEscapeExtendedChars"));
		
		tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText(Res.getString("tabOutput"));
		tiTmp.setControl(cmpTmp);*/
		
		
		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("JSON Filter");
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
		chkExtractStandalone.setSelection(params.getExtractStandalone());
		rdExtractAllPairs.setSelection(params.getExtractAllPairs());
		rdDontExtractPairs.setSelection(!params.getExtractAllPairs());
		edExceptions.setText(params.getExceptions()==null ? "" : params.getExceptions());
		chkUseKeyAsName.setSelection(params.getUseKeyAsName());
		chkUseFullKeyPath.setSelection(params.getUseFullKeyPath());
		chkUseLeadingSlashOnKeyPath.setSelection(params.getUseLeadingSlashOnKeyPath());
		rdUseSubfilter.setSelection(!params.getUseCodeFinder());
		chkEscapeForwardSlashes.setSelection(params.getEscapeForwardSlashes());
		
		edSubfilterId.setText(params.getSubfilter()==null? "" : params.getSubfilter());
		rdUseCodeFinder.setSelection(params.getUseCodeFinder());
		pnlCodeFinder.setRules(params.getCodeFinderData());

		// Leading slash option enabled only if full key path option is set
		chkUseLeadingSlashOnKeyPath.setEnabled(chkUseFullKeyPath.getSelection());
		
		updateInlineCodes();
		pnlCodeFinder.updateDisplay();
	}

	// Returns null if expression is OK, a message if it is not.
	private String checkExceptionsSyntax () {
		try {
			String tmp = edExceptions.getText();
			if ( Util.isEmpty(tmp) ) return null;
			Pattern.compile(tmp);
			params.setExceptions(tmp);
		}
		catch ( PatternSyntaxException e ) {
			return e.getMessage();
		}
		return null;
	}
	
	private boolean saveData () {
		if ( rdUseCodeFinder.getSelection() ) {
			if ( pnlCodeFinder.getRules() == null ) {
				return false;
			}
			else {
				params.setCodeFinderData(pnlCodeFinder.getRules());
			}
		}
		String tmp = checkExceptionsSyntax();
		if ( tmp != null ) {
			edExceptions.selectAll();
			edExceptions.setFocus();
			Dialogs.showError(shell, tmp, null);
			return false;
		}

		params.setUseCodeFinder(rdUseCodeFinder.getSelection());
		params.setExceptions(edExceptions.getText());
		params.setExtractStandalone(chkExtractStandalone.getSelection());
		params.setExtractAllPairs(rdExtractAllPairs.getSelection());
		params.setUseKeyAsName(chkUseKeyAsName.getSelection());
		params.setUseFullKeyPath(chkUseFullKeyPath.getSelection());
		params.setUseLeadingSlashOnKeyPath(chkUseLeadingSlashOnKeyPath.getSelection());
		params.setSubfilter(rdUseSubfilter.getSelection() ? edSubfilterId.getText() : "");
		params.setEscapeForwardSlashes(chkEscapeForwardSlashes.getSelection());
		return true;
	}
	
	private void updateInlineCodes () {
		pnlCodeFinder.setEnabled(rdUseCodeFinder.getSelection());
		edSubfilterId.setEnabled(!rdUseCodeFinder.getSelection());
	}

}
