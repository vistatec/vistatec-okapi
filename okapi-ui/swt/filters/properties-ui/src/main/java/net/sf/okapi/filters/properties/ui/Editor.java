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

package net.sf.okapi.filters.properties.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.filters.InlineCodeFinderPanel;
import net.sf.okapi.common.ui.filters.LDPanel;
import net.sf.okapi.filters.properties.Parameters;

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

@EditorFor(Parameters.class)
public class Editor implements IParametersEditor {
	
	private Shell shell;
	private boolean result = false;
	private Button chkUseKeyFilter;
	private Button rdExtractOnlyMatchingKey;
	private Button rdExcludeMatchingKey;
	private Text edKeyCondition;
	private Button chkExtraComments;
	private Button chkCommentsAreNotes;
	private Button chkIdLikeResname;
	private LDPanel pnlLD;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private Button chkEscapeExtendedChars;
	private Button chkUseCodeFinder;
	private InlineCodeFinderPanel pnlCodeFinder;
	private IHelp help;
	private Button chkConvertLFAndTab;
	private Text edSubFilter;

	public boolean edit (IParameters p_Options,
		boolean readOnly,
		IContext context)
	{
		help = (IHelp)context.getObject("help");
		boolean bRes = false;
		shell = null;
		params = (Parameters)p_Options;
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
	
	private void create (Shell p_Parent,
		boolean readOnly)
	{
		shell.setText(Res.getString("EditorCaption"));
		if ( p_Parent != null ) shell.setImage(p_Parent.getImage());
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
		grpTmp.setText(Res.getString("LodDirTitle"));
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);
		pnlLD = new LDPanel(grpTmp, SWT.NONE);
		
		grpTmp = new Group(cmpTmp, SWT.NONE);
		layTmp = new GridLayout();
		grpTmp.setLayout(layTmp);
		grpTmp.setText(Res.getString("KeyCondTitle"));
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);
		
		chkUseKeyFilter = new Button(grpTmp, SWT.CHECK);
		chkUseKeyFilter.setText(Res.getString("chkUseKeyFilter"));
		chkUseKeyFilter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateKeyFilter();
			};
		});

		rdExtractOnlyMatchingKey = new Button(grpTmp, SWT.RADIO);
		rdExtractOnlyMatchingKey.setText(Res.getString("rdExtractOnlyMatchingKey"));
		gdTmp = new GridData();
		gdTmp.horizontalIndent = 16;
		rdExtractOnlyMatchingKey.setLayoutData(gdTmp);

		rdExcludeMatchingKey = new Button(grpTmp, SWT.RADIO);
		rdExcludeMatchingKey.setText(Res.getString("rdExcludeMatchingKey"));
		rdExcludeMatchingKey.setLayoutData(gdTmp);

		edKeyCondition = new Text(grpTmp, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalIndent = 16;
		edKeyCondition.setLayoutData(gdTmp);
		
		Label label = new Label(grpTmp, SWT.WRAP);
		label.setText(Res.getString("KeyCondNote"));
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalIndent = 16;
		gdTmp.widthHint = 300;
		label.setLayoutData(gdTmp);
		
		Label stSubFilter = new Label(cmpTmp, SWT.NONE);
		stSubFilter.setText("Configuration identifier of the sub-filter to use on the content (empty for none):");
		
		edSubFilter = new Text(cmpTmp, SWT.BORDER);
		edSubFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		chkExtraComments = new Button(cmpTmp, SWT.CHECK);
		chkExtraComments.setText(Res.getString("chkExtraComments"));

		chkCommentsAreNotes = new Button(cmpTmp, SWT.CHECK);
		chkCommentsAreNotes.setText(Res.getString("chkCommentsAreNotes"));
		
		chkConvertLFAndTab = new Button(cmpTmp, SWT.CHECK);
		chkConvertLFAndTab.setText(Res.getString("chkConvertLFAndTab"));
		
		chkIdLikeResname = new Button(cmpTmp, SWT.CHECK);
		chkIdLikeResname.setText(Res.getString("chkIdLikeResname"));
		
		TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText(Res.getString("tabOptions"));
		tiTmp.setControl(cmpTmp);
		
		//--- Inline tab
		
		cmpTmp = new Composite(tfTmp, SWT.NONE);
		layTmp = new GridLayout();
		cmpTmp.setLayout(layTmp);
		
		chkUseCodeFinder = new Button(cmpTmp, SWT.CHECK);
		chkUseCodeFinder.setText("Has inline codes as defined below:");
		chkUseCodeFinder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateInlineCodes();
			};
		});
		
		pnlCodeFinder = new InlineCodeFinderPanel(cmpTmp, SWT.NONE);
		pnlCodeFinder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Inline Codes");
		tiTmp.setControl(cmpTmp);
			

		//--- Output tab
		
		cmpTmp = new Composite(tfTmp, SWT.NONE);
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
		tiTmp.setControl(cmpTmp);
		
		
		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("Properties Filter");
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
		Dialogs.centerWindow(shell, p_Parent);
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
		pnlLD.setOptions(params.locDir.useLD(), params.locDir.localizeOutside());
		edKeyCondition.setText(params.getKeyCondition());
		rdExtractOnlyMatchingKey.setSelection(params.isExtractOnlyMatchingKey());
		rdExcludeMatchingKey.setSelection(!params.isExtractOnlyMatchingKey());
		chkUseKeyFilter.setSelection(params.isUseKeyCondition());
		chkExtraComments.setSelection(params.isExtraComments());
		chkCommentsAreNotes.setSelection(params.isCommentsAreNotes());
		chkEscapeExtendedChars.setSelection(params.isEscapeExtendedChars());
		chkIdLikeResname.setSelection(params.isIdLikeResname());
		chkUseCodeFinder.setSelection(params.isUseCodeFinder());
		pnlCodeFinder.setRules(params.codeFinder.toString());
		chkConvertLFAndTab.setSelection(params.isConvertLFandTab());
		String tmp = params.getSubfilter();
		edSubFilter.setText(Util.isEmpty(tmp) ? "" : tmp);
		
		updateInlineCodes();
		pnlCodeFinder.updateDisplay();
		pnlLD.updateDisplay();
		updateKeyFilter();
	}
	
	private boolean saveData () {
		String tmp = pnlCodeFinder.getRules();
		if ( tmp == null ) {
			return false;
		}
		else {
			params.codeFinder.fromString(tmp);
		}
		params.locDir.setOptions(pnlLD.getUseLD(), pnlLD.getLocalizeOutside());
		params.setUseKeyCondition(chkUseKeyFilter.getSelection());
		params.setKeyCondition(edKeyCondition.getText());
		params.setExtractOnlyMatchingKey(rdExtractOnlyMatchingKey.getSelection());
		params.setExtraComments(chkExtraComments.getSelection());
		params.setCommentsAreNotes(chkCommentsAreNotes.getSelection());
		params.setEscapeExtendedChars(chkEscapeExtendedChars.getSelection());
		params.setIdLikeResname(chkIdLikeResname.getSelection());
		params.setUseCodeFinder(chkUseCodeFinder.getSelection());
		params.setConvertLFandTab(chkConvertLFAndTab.getSelection());
		params.setSubfilter(edSubFilter.getText().trim());
		return true;
	}
	
	private void updateKeyFilter () {
		edKeyCondition.setEnabled(chkUseKeyFilter.getSelection());
		rdExtractOnlyMatchingKey.setEnabled(chkUseKeyFilter.getSelection());
		rdExcludeMatchingKey.setEnabled(chkUseKeyFilter.getSelection());
	}
	
	private void updateInlineCodes () {
		pnlCodeFinder.setEnabled(chkUseCodeFinder.getSelection());
	}

}
