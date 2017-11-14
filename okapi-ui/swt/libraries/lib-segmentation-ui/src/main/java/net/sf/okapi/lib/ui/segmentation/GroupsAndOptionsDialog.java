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

import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.ui.ClosePanel;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.InputDialog;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.segmentation.LanguageMap;
import net.sf.okapi.lib.segmentation.Rule;
import net.sf.okapi.lib.segmentation.SRXDocument;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GroupsAndOptionsDialog {

	private Shell shell;
	private List lbLangRules;
	private List lbLangMaps;
	private SRXDocument srxDoc;
	private Button btAddRules;
	private Button btRenameRules;
	private Button btRemoveRules;
	private Button btAddMap;
	private Button btEditMap;
	private Button btRemoveMap;
	private Button btMoveUpMap;
	private Button btMoveDownMap;
	private Button chkSegmentSubFlows;
	private Button chkCascade;
	private Button chkUseIcu4jBreak;
	private Button chkIncludeOpeningCodes;
	private Button chkIncludeClosingCodes;
	private Button chkIncludeIsolatedCodes;
	private Button chkOneSegmentIncludesAll;	
	private Button chkTrimLeadingWS;
	private Button chkTrimTrailingWS;
	private Button chkTreatIsolatedCodesAsWhitespace;
	private ClosePanel pnlActions;
	private IHelp help;
	private Text edHeaderComments;
	private Text edDocComments;

	public GroupsAndOptionsDialog (Shell parent,
		SRXDocument srxDoc,
		IHelp helpParam)
	{
		help = helpParam;
		this.srxDoc = srxDoc;
		
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText(Res.getString("options.caption")); //$NON-NLS-1$
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(2, true));
		
		Group grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText(Res.getString("options.grpOptions")); //$NON-NLS-1$
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		grpTmp.setLayoutData(gdTmp);
		GridLayout layTmp = new GridLayout(2, false);
		grpTmp.setLayout(layTmp);

		chkCascade = new Button(grpTmp, SWT.CHECK);
		chkCascade.setText(Res.getString("options.cascade")); //$NON-NLS-1$
		
		chkIncludeOpeningCodes = new Button(grpTmp, SWT.CHECK);
		chkIncludeOpeningCodes.setText(Res.getString("options.includeStartCodes")); //$NON-NLS-1$
		
		chkSegmentSubFlows = new Button(grpTmp, SWT.CHECK);
		chkSegmentSubFlows.setText(Res.getString("options.segmentSubFlow")); //$NON-NLS-1$
		
		chkIncludeClosingCodes = new Button(grpTmp, SWT.CHECK);
		chkIncludeClosingCodes.setText(Res.getString("options.includeEndCodes")); //$NON-NLS-1$

		new Label(grpTmp, SWT.NONE);
		
		chkIncludeIsolatedCodes = new Button(grpTmp, SWT.CHECK);
		chkIncludeIsolatedCodes.setText(Res.getString("options.includeIsolatedCodes")); //$NON-NLS-1$
		
		grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText(Res.getString("options.grpExtensions")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		grpTmp.setLayoutData(gdTmp);
		layTmp = new GridLayout(2, false);
		grpTmp.setLayout(layTmp);

		chkUseIcu4jBreak = new Button(grpTmp, SWT.CHECK);
		chkUseIcu4jBreak.setText(Res.getString("options.useIcu4jBreak")); //$NON-NLS-1$
		
	
		chkTrimLeadingWS = new Button(grpTmp, SWT.CHECK);
		chkTrimLeadingWS.setText(Res.getString("options.trimLeadingWS")); //$NON-NLS-1$
		
		chkOneSegmentIncludesAll = new Button(grpTmp, SWT.CHECK);
		chkOneSegmentIncludesAll.setText(Res.getString("options.includeAllInOne")); //$NON-NLS-1$

		chkTrimTrailingWS = new Button(grpTmp, SWT.CHECK);
		chkTrimTrailingWS.setText(Res.getString("options.trimtrailingWS")); //$NON-NLS-1$

		chkTreatIsolatedCodesAsWhitespace = new Button(grpTmp, SWT.CHECK);
		chkTreatIsolatedCodesAsWhitespace.setText(Res.getString("options.treatIsolatedCodesAsWhitespace"));
		
		Label label = new Label(grpTmp, SWT.NONE);
		label.setText(Res.getString("GroupsAndOptionsDialog.headerComments")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		label.setLayoutData(gdTmp);
		
		edHeaderComments = new Text(grpTmp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		gdTmp.heightHint = 40;
		edHeaderComments.setLayoutData(gdTmp);
		
		//=== Language Rules
		
		grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText(Res.getString("options.grpLangRules")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.FILL_BOTH);
		grpTmp.setLayoutData(gdTmp);
		layTmp = new GridLayout(2, false);
		grpTmp.setLayout(layTmp);
		
		int listWidthHint = 150;
		lbLangRules = new List(grpTmp, SWT.BORDER | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.verticalSpan = 1;
		gdTmp.widthHint = listWidthHint;
		lbLangRules.setLayoutData(gdTmp);
		lbLangRules.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateRulesButtons();
			};
		});
		
		Composite cmpTmp = new Composite(grpTmp, SWT.NONE);
		layTmp = new GridLayout(1, true);
		layTmp.marginWidth = 0;
		layTmp.marginHeight = 0;
		cmpTmp.setLayout(layTmp);
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		cmpTmp.setLayoutData(gdTmp);
		
		int buttonWidth = UIUtil.BUTTON_DEFAULT_WIDTH;
		btAddRules = new Button(cmpTmp, SWT.PUSH);
		btAddRules.setText(Res.getString("options.addRules")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		btAddRules.setLayoutData(gdTmp);
		// Make sure one button is at least buttonWidth wide
		UIUtil.ensureWidth(btAddRules, buttonWidth);
		btAddRules.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editRules(true);
			}
		});
		
		btRenameRules = new Button(cmpTmp, SWT.PUSH);
		btRenameRules.setText(Res.getString("options.renameRules")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		btRenameRules.setLayoutData(gdTmp);
		btRenameRules.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editRules(false);
			}
		});
		
		btRemoveRules = new Button(cmpTmp, SWT.PUSH);
		btRemoveRules.setText(Res.getString("options.removeRules")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		btRemoveRules.setLayoutData(gdTmp);
		btRemoveRules.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeRules();
			}
		});
		
		//=== Language Maps
		
		grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText(Res.getString("options.grpLangMaps")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.FILL_BOTH);
		grpTmp.setLayoutData(gdTmp);
		layTmp = new GridLayout(2, false);
		grpTmp.setLayout(layTmp);
		
		lbLangMaps = new List(grpTmp, SWT.BORDER | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.verticalSpan = 1;
		gdTmp.widthHint = listWidthHint;		
		lbLangMaps.setLayoutData(gdTmp);
		lbLangMaps.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateMapsButtons();
			};
		});

		cmpTmp = new Composite(grpTmp, SWT.NONE);
		layTmp = new GridLayout(1, true);
		layTmp.marginWidth = 0;
		layTmp.marginHeight = 0;
		cmpTmp.setLayout(layTmp);
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		cmpTmp.setLayoutData(gdTmp);
		
		btAddMap = new Button(cmpTmp, SWT.PUSH);
		btAddMap.setText(Res.getString("options.addMap")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		btAddMap.setLayoutData(gdTmp);
		// Make sure one button is at least buttonWidth wide
		UIUtil.ensureWidth(btAddMap, buttonWidth);
		btAddMap.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editMap(true);
			}
		});

		btEditMap = new Button(cmpTmp, SWT.PUSH);
		btEditMap.setText(Res.getString("options.editMap")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		btEditMap.setLayoutData(gdTmp);
		btEditMap.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editMap(false);
			}
		});
		
		btRemoveMap = new Button(cmpTmp, SWT.PUSH);
		btRemoveMap.setText(Res.getString("options.removeMap")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		btRemoveMap.setLayoutData(gdTmp);
		btRemoveMap.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeMap();
			}
		});
		
		btMoveUpMap = new Button(cmpTmp, SWT.PUSH);
		btMoveUpMap.setText(Res.getString("options.moveUpMap")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		btMoveUpMap.setLayoutData(gdTmp);
		btMoveUpMap.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveUpMap();
			}
		});
		
		btMoveDownMap = new Button(cmpTmp, SWT.PUSH);
		btMoveDownMap.setText(Res.getString("options.moveDownMap")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		btMoveDownMap.setLayoutData(gdTmp);
		btMoveDownMap.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveDownMap();
			}
		});

		// === Document comment
		
		grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText(Res.getString("GroupsAndOptionsDialog.docComments")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		grpTmp.setLayoutData(gdTmp);
		grpTmp.setLayout(new GridLayout());

		edDocComments = new Text(grpTmp, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 60;
		edDocComments.setLayoutData(gdTmp);
		
		// Handling of the closing event
		shell.addShellListener(new ShellListener() {
			public void shellActivated(ShellEvent event) {}
			public void shellClosed(ShellEvent event) {
				if ( !validate() ) event.doit = false;
				else getOptions();
			}
			public void shellDeactivated(ShellEvent event) {}
			public void shellDeiconified(ShellEvent event) {}
			public void shellIconified(ShellEvent event) {}
		});

		//--- Dialog-level buttons
		
		SelectionAdapter CloseActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					if ( help != null ) help.showWiki("Ratel - Groups and Options");
					return;
				}
				if ( e.widget.getData().equals("c") ) { //$NON-NLS-1$
					shell.close();
				}
			};
		};
		pnlActions = new ClosePanel(shell, SWT.NONE, CloseActions, true);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		pnlActions.setLayoutData(gdTmp);
		shell.setDefaultButton(pnlActions.btClose);
		
		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.y < 400 ) startSize.y = 400;
		shell.setSize(startSize);
		Dialogs.centerWindow(shell, parent);

		setOptions();
		updateLanguageRules(null);
		updateLanguageMaps(0);
	}
	
	public void showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
	}

	private void setOptions () {
		chkSegmentSubFlows.setSelection(srxDoc.segmentSubFlows());
		chkCascade.setSelection(srxDoc.cascade());
		chkIncludeOpeningCodes.setSelection(srxDoc.includeStartCodes());
		chkIncludeClosingCodes.setSelection(srxDoc.includeEndCodes());
		chkIncludeIsolatedCodes.setSelection(srxDoc.includeIsolatedCodes());
		chkOneSegmentIncludesAll.setSelection(srxDoc.oneSegmentIncludesAll());
		chkUseIcu4jBreak.setSelection(srxDoc.useIcu4JBreakRules());
		chkTrimLeadingWS.setSelection(srxDoc.trimLeadingWhitespaces());
		chkTrimTrailingWS.setSelection(srxDoc.trimTrailingWhitespaces());
		chkTreatIsolatedCodesAsWhitespace.setSelection(srxDoc.treatIsolatedCodesAsWhitespace());
		String tmp = srxDoc.getHeaderComments();
		edHeaderComments.setText(tmp==null ? "" : tmp); //$NON-NLS-1$
		tmp = srxDoc.getComments();
		edDocComments.setText(tmp==null ? "" : tmp); //$NON-NLS-1$
	}
	
	private void getOptions () {
		srxDoc.setSegmentSubFlows(chkSegmentSubFlows.getSelection());
		srxDoc.setCascade(chkCascade.getSelection());
		srxDoc.setUseICU4JBreakRules(chkUseIcu4jBreak.getSelection());
		srxDoc.setIncludeStartCodes(chkIncludeOpeningCodes.getSelection());
		srxDoc.setIncludeEndCodes(chkIncludeClosingCodes.getSelection());
		srxDoc.setIncludeIsolatedCodes(chkIncludeIsolatedCodes.getSelection());
		srxDoc.setOneSegmentIncludesAll(chkOneSegmentIncludesAll.getSelection());
		srxDoc.setTrimLeadingWhitespaces(chkTrimLeadingWS.getSelection());
		srxDoc.setTrimTrailingWhitespaces(chkTrimTrailingWS.getSelection());
		srxDoc.setTreatIsolatedCodesAsWhitespace(chkTreatIsolatedCodesAsWhitespace.getSelection());
		String tmp = edHeaderComments.getText();
		srxDoc.setHeaderComments(tmp.replace("\r\n", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
		tmp = edDocComments.getText();
		srxDoc.setComments(tmp.replace("\r\n", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private void updateRulesButtons () {
		boolean enabled = (lbLangRules.getSelectionIndex()!=-1);
		btRenameRules.setEnabled(enabled);
		btRemoveRules.setEnabled(enabled);
	}
	
	private void updateLanguageRules (String selection) {
		lbLangRules.removeAll();
		LinkedHashMap<String, ArrayList<Rule>> list = srxDoc.getAllLanguageRules();
		
		if (( selection != null ) && !list.containsKey(selection) ) {
			selection = null;
		}
		for ( String ruleName : list.keySet() ) {
			lbLangRules.add(ruleName);
			if ( selection == null ) selection = ruleName;
		}
		if ( lbLangRules.getItemCount() > 0 ) {
			if ( selection != null ) {
				lbLangRules.select(lbLangRules.indexOf(selection));
			}
		}
		updateRulesButtons();
	}
	
	private void updateMapsButtons () {
		int n = lbLangMaps.getSelectionIndex();
		boolean enabled = (n!=-1);
		btEditMap.setEnabled(enabled);
		btRemoveMap.setEnabled(enabled);
		btMoveUpMap.setEnabled(n>0);
		btMoveDownMap.setEnabled(n<lbLangMaps.getItemCount()-1);
	}
	
	private void updateLanguageMaps (int selection) {
		lbLangMaps.removeAll();
		ArrayList<LanguageMap> list = srxDoc.getAllLanguagesMaps();
		for ( LanguageMap langMap : list ) {
			lbLangMaps.add(langMap.getPattern() + " --> " + langMap.getRuleName()); //$NON-NLS-1$
		}
		if (( selection < 0 ) || ( selection >= lbLangMaps.getItemCount() )) {
			selection = 0;
		}
		if ( lbLangMaps.getItemCount() > 0 ) {
			lbLangMaps.select(selection);
		}
		updateMapsButtons();
	}
	
	private void editMap (boolean createNewMap) {
		LanguageMap langMap;
		int n = -1;
		if ( createNewMap ) {
			langMap = new LanguageMap("", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			n = lbLangMaps.getSelectionIndex();
			if ( n == -1 ) return;
			langMap = srxDoc.getAllLanguagesMaps().get(n);
		}
		
		LanguageMapDialog dlg = new LanguageMapDialog(shell, langMap, help);
		if ( (langMap = dlg.showDialog()) == null ) return; // Cancel
		
		if ( createNewMap ) {
			srxDoc.addLanguageMap(langMap);
			n = srxDoc.getAllLanguagesMaps().size()+1;
		}
		else {
			srxDoc.getAllLanguagesMaps().set(n, langMap);
		}
		srxDoc.setModified(true);
		updateLanguageMaps(n);
	}
	
	private void removeMap () {
		int n = lbLangMaps.getSelectionIndex();
		if ( n == -1 ) return;
		srxDoc.getAllLanguagesMaps().remove(n);
		srxDoc.setModified(true);
		updateLanguageMaps(n);
	}
	
	private void moveUpMap () {
		int n = lbLangMaps.getSelectionIndex();
		if ( n < 1 ) return;
		LanguageMap tmp = srxDoc.getAllLanguagesMaps().get(n-1);
		srxDoc.getAllLanguagesMaps().set(n-1,
			srxDoc.getAllLanguagesMaps().get(n));
		srxDoc.getAllLanguagesMaps().set(n, tmp);
		srxDoc.setModified(true);
		updateLanguageMaps(--n);
	}
	
	private void moveDownMap () {
		int n = lbLangMaps.getSelectionIndex();
		if ( n > lbLangMaps.getItemCount()-2 ) return;
		LanguageMap tmp = srxDoc.getAllLanguagesMaps().get(n+1);
		srxDoc.getAllLanguagesMaps().set(n+1,
			srxDoc.getAllLanguagesMaps().get(n));
		srxDoc.getAllLanguagesMaps().set(n, tmp);
		srxDoc.setModified(true);
		updateLanguageMaps(++n);
	}
	
	private void editRules (boolean createNewRules) {
		String name;
		String oldName = null;
		String caption;
		if ( createNewRules ) {
			name = String.format(Res.getString("options.defaultGroupName"), //$NON-NLS-1$
				srxDoc.getAllLanguageRules().size()+1);
			caption = Res.getString("options.newGroupCaption"); //$NON-NLS-1$
		}
		else {
			int n = lbLangRules.getSelectionIndex();
			if ( n == -1 ) return;
			oldName = name = lbLangRules.getItem(n);
			caption = Res.getString("options.renameGroupCaption"); //$NON-NLS-1$
		}
		
		while ( true ) {
			// Edit the name
			InputDialog dlg = new InputDialog(shell, caption,
				Res.getString("options.groupNameLabel"), name, null, 0, -1, -1); //$NON-NLS-1$
			if ( (name = dlg.showDialog()) == null ) return; // Cancel
		
			// Else:
			if ( createNewRules ) {
				if ( srxDoc.getAllLanguageRules().containsKey(name) ) {
					Dialogs.showError(shell,
						String.format(Res.getString("options.sameNameError"), name), //$NON-NLS-1$
						null);
				}
				else {
					srxDoc.addLanguageRule(name, new ArrayList<Rule>());
					break;
				}
			}
			else {
				ArrayList<Rule> list = srxDoc.getLanguageRules(oldName);
				srxDoc.getAllLanguageRules().remove(oldName);
				srxDoc.addLanguageRule(name, list);
				break;
			}
		}
		updateLanguageRules(name);
	}
	
	private void removeRules () {
		int n = lbLangRules.getSelectionIndex();
		if ( n == -1 ) return;
		String ruleName = lbLangRules.getItem(n);
		// Ask confirmation
		MessageBox dlg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
		dlg.setText(shell.getText());
		dlg.setMessage(String.format(Res.getString("options.confirmRemoveRules"), ruleName)); //$NON-NLS-1$
		switch ( dlg.open() ) {
		case SWT.CANCEL:
		case SWT.NO:
			return;
		}
		// Remove
		srxDoc.getAllLanguageRules().remove(ruleName);
		srxDoc.setModified(true);
		updateLanguageRules(null);
	}
	
	private boolean validate () {
		try {
			int nonexistingRules = 0;
			StringBuilder notMapped = new StringBuilder();
			LinkedHashMap<String, ArrayList<Rule>> list = srxDoc.getAllLanguageRules();
			for ( LanguageMap langRule : srxDoc.getAllLanguagesMaps() ) {
				if ( !list.containsKey(langRule.getRuleName()) ) {
					if ( nonexistingRules > 0 ) notMapped.append(", "); //$NON-NLS-1$
					notMapped.append(langRule.getRuleName());
					nonexistingRules++;
				}
			}
			
			if ( nonexistingRules == 0 ) return true;
			// Else: Error.
			MessageBox dlg = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);
			dlg.setText(shell.getText());
			dlg.setMessage(String.format(Res.getString("options.badNamesError"), //$NON-NLS-1$
				nonexistingRules, notMapped.toString()));
			switch ( dlg.open() ) {
			case SWT.CANCEL:
			case SWT.NO:
				return false;
			case SWT.YES:
				return true;
			}
		}
		catch ( Exception e) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
			return false;
		}
		return true;
	}

}
