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

package net.sf.okapi.common.ui.filters;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * Implements a panel for editing rules to capture in-line codes with
 * the {@link net.sf.okapi.common.filters.InlineCodeFinder} class.
 */
public class InlineCodeFinderPanel extends Composite {

	private static final String ACCEPT_LABEL = Res.getString("InlineCodeFinderPanel.accept"); //$NON-NLS-1$
	
	private InlineCodeFinder codeFinder;
	private List lbRules;
	private Text edExpression;
	private Text edSample;
	private Text edResults;
	private Button btMoveUp;
	private Button btModify;
	private Button btDiscard;
	private Button btInsertPattern;
	private Button btAdd;
	private Button btRemove;
	private Button btMoveDown;
	private Button chkTestAllRules;
	private boolean editMode;
	private boolean wasNew;
	private TextFragment textFrag;
	private GenericContent genericCont;
	private boolean canUpdateTest = true;

	public InlineCodeFinderPanel (Composite parent,
		int flags)
	{
		super(parent, flags);
		codeFinder = new InlineCodeFinder();
		textFrag = new TextFragment();
		genericCont = new GenericContent();
		createContent();
	}
	
	private void createContent () {
		GridLayout layTmp = new GridLayout(5, false);
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		setLayout(layTmp);
		
		Composite cmpRules = new Composite(this, SWT.NONE);
		layTmp = new GridLayout(2, false);
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		cmpRules.setLayout(layTmp);
		GridData gdTmp = new GridData(GridData.FILL_VERTICAL);
		gdTmp.verticalSpan = 4;
		cmpRules.setLayoutData(gdTmp);
		
		lbRules = new List(cmpRules, SWT.BORDER | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		gdTmp.grabExcessVerticalSpace = true;
		lbRules.setLayoutData(gdTmp);
		lbRules.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateDisplay();
			};
		});
		
		int buttonSet1Width = 90;
		int buttonSet2Width = 90;

		//--- Buttons for the rules list

		Composite cmpTmp = new Composite(cmpRules, SWT.NONE);
		layTmp = new GridLayout(2, true);
		layTmp.marginHeight = layTmp.marginWidth = 0;
		cmpTmp.setLayout(layTmp);
		
		btAdd = new Button(cmpTmp, SWT.PUSH);
		btAdd.setText(Res.getString("InlineCodeFinderPanel.add")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		btAdd.setLayoutData(gdTmp);
		UIUtil.ensureWidth(btAdd, buttonSet1Width);
		btAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				startEditMode(true);
			};
		});
	
		btMoveUp = new Button(cmpTmp, SWT.PUSH);
		btMoveUp.setText(Res.getString("InlineCodeFinderPanel.moveUp")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		btMoveUp.setLayoutData(gdTmp);
		btMoveUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveUp();
			};
		});
		
		btRemove = new Button(cmpTmp, SWT.PUSH);
		btRemove.setText(Res.getString("InlineCodeFinderPanel.remove")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		btRemove.setLayoutData(gdTmp);
		btRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeExpression();
			};
		});

		btMoveDown = new Button(cmpTmp, SWT.PUSH);
		btMoveDown.setText(Res.getString("InlineCodeFinderPanel.moveDown")); //$NON-NLS-1$
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		btMoveDown.setLayoutData(gdTmp);
		btMoveDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveDown();
			};
		});

		// Expression sides
		
		edExpression = new Text(this, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 4;
		gdTmp.heightHint = 60;
		edExpression.setLayoutData(gdTmp);
		edExpression.setEditable(false);
		edExpression.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateTest();
			}
		});

		btModify = new Button(this, SWT.PUSH);
		gdTmp = new GridData();
		btModify.setLayoutData(gdTmp);
		// This button has two labels: use the maximum width
		int max = buttonSet2Width;
		btModify.setText(ACCEPT_LABEL);
		btModify.pack();
		Rectangle rect = btModify.getBounds();
		if ( rect.width > max ) max = rect.width;
		btModify.setText(Res.getString("InlineCodeFinderPanel.modify")); //$NON-NLS-1$
		UIUtil.ensureWidth(btModify, max);
		btModify.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( editMode ) endEditMode(true);
				else startEditMode(false);
			};
		});
		
		btDiscard = new Button(this, SWT.PUSH);
		btDiscard.setText(Res.getString("InlineCodeFinderPanel.discard")); //$NON-NLS-1$
		gdTmp = new GridData();
		btDiscard.setLayoutData(gdTmp);
		UIUtil.ensureWidth(btDiscard, buttonSet2Width);
		btDiscard.setEnabled(false);
		btDiscard.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				endEditMode(false);
			};
		});
		
		btInsertPattern = new Button(this, SWT.PUSH);
		btInsertPattern.setText(Res.getString("InlineCodeFinderPanel.patterns")); //$NON-NLS-1$
		gdTmp = new GridData();
		btInsertPattern.setLayoutData(gdTmp);
		UIUtil.ensureWidth(btInsertPattern, buttonSet2Width);
		btInsertPattern.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//TODO: Implement real insert. For now just open the help on Java regex patterns
				Util.openURL("http://download.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html"); //$NON-NLS-1$
			};
		});
		
		chkTestAllRules = new Button(this, SWT.CHECK);
		chkTestAllRules.setText(Res.getString("InlineCodeFinderPanel.useAllrules")); //$NON-NLS-1$
		gdTmp = new GridData();
		chkTestAllRules.setLayoutData(gdTmp);
		chkTestAllRules.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTest();
			};
		});
		
		edSample = new Text(this, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 4;
		gdTmp.heightHint = 60;
		edSample.setLayoutData(gdTmp);
		edSample.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateTest();
			}
		});

		edResults = new Text(this, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 4;
		gdTmp.heightHint = 60;
		edResults.setLayoutData(gdTmp);
		edResults.setEditable(false);

		updateDisplay();
	}
	
	private void moveUp () {
		int n = lbRules.getSelectionIndex();
		if ( n < 1 ) return;
		// Move in the rules array
		String tmp = lbRules.getItem(n);
		lbRules.setItem(n, lbRules.getItem(n-1));
		lbRules.setItem(n-1, tmp);
		lbRules.select(n-1);
		updateDisplay();
	}
	
	private void moveDown () {
		int n = lbRules.getSelectionIndex();
		if ( n == -1 ) return;
		String tmp = lbRules.getItem(n);
		lbRules.setItem(n, lbRules.getItem(n+1));
		lbRules.setItem(n+1, tmp);
		lbRules.select(n+1);
		updateDisplay();
	}
	
	private void startEditMode (boolean createNew) {
		try {
			wasNew = createNew;
			if ( createNew ) {
				lbRules.add(""); //$NON-NLS-1$
				lbRules.setSelection(lbRules.getItemCount()-1);
				canUpdateTest = false;
				updateDisplay();
				canUpdateTest = true;
			}
			int n = lbRules.getSelectionIndex();
			if ( n == -1 ) return;
			toggleMode(true);
		}
		catch ( Throwable e) {
			Dialogs.showError(getShell(), e.getMessage(), null);
		}
	}
	
	public boolean endEditMode (boolean accept) {
		if ( accept ) {
			if ( edExpression.getText().length() == 0 ) {
				Dialogs.showError(getShell(), Res.getString("InlineCodeFinderPanel.enterExpression"), null); //$NON-NLS-1$
				edExpression.setFocus();
				return false;
			}
			lbRules.setItem(lbRules.getSelectionIndex(), edExpression.getText());
		}
		else {
			if ( wasNew ) removeExpression();
		}
		toggleMode(false);
		return true;
	}
	
	private void toggleMode (boolean editMode) {
		this.editMode = editMode;
		lbRules.setEnabled(!editMode);
		edExpression.setEditable(editMode);
		btDiscard.setEnabled(editMode);
		//TODO: To comment out when implements real insert btInsertPattern.setEnabled(editMode);
		btAdd.setEnabled(!editMode);
		
		if ( editMode ) {
			btRemove.setEnabled(false);
			btMoveUp.setEnabled(false);
			btMoveDown.setEnabled(false);
			btModify.setEnabled(true);
			btModify.setText(ACCEPT_LABEL);
			edExpression.setFocus();
		}
		else {
			btModify.setText(Res.getString("InlineCodeFinderPanel.modify")); //$NON-NLS-1$
			updateDisplay();
		}
	}
	
	public void removeExpression () {
		int n = lbRules.getSelectionIndex();
		if ( n == -1 ) return;
		lbRules.remove(n);
		if ( n >= lbRules.getItemCount() ) n = lbRules.getItemCount()-1;
		if ( n > -1 ) lbRules.setSelection(n);
		updateDisplay();
	}
	
	public void updateDisplay () {
		int n = lbRules.getSelectionIndex();
		btRemove.setEnabled(n>-1);
		btMoveUp.setEnabled(n>0);
		btMoveDown.setEnabled(n<lbRules.getItemCount()-1);
		btModify.setEnabled(n>-1);
		if ( n == -1 ) edExpression.setText(""); //$NON-NLS-1$
		else edExpression.setText(lbRules.getItem(n));
	}
	
	private void updateTest () {
		if ( !canUpdateTest ) return; // No updates in some contexts
		try {
			int n = lbRules.getSelectionIndex();
			if ( n == -1 ) return;
			codeFinder.getRules().clear();
			if ( chkTestAllRules.getSelection() ) {
				for ( String pattern : lbRules.getItems() ) {
					codeFinder.addRule(pattern);
				}
				codeFinder.getRules().set(n, edExpression.getText());
			}
			else {
				codeFinder.addRule(edExpression.getText());
			}
			codeFinder.compile();
			textFrag.clear();
			textFrag.setCodedText(getSampleText());
			codeFinder.process(textFrag);
			genericCont.setContent(textFrag);
			edResults.setText(genericCont.toString());
		}
		catch ( Throwable e ) {
			edResults.setText(e.getMessage());
		}
	}
	
	private String getSampleText () {
		// Change different line breaks type into \n cases
		String tmp = edSample.getText();
		tmp = tmp.replaceAll("\r\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		tmp = tmp.replace("\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return tmp.replace("\r", "\n");  //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void setRules (String codeFinderRules) {
		codeFinder.fromString(codeFinderRules);
		lbRules.removeAll();
		for ( String pattern : codeFinder.getRules() ) {
			lbRules.add(pattern);
		}
		edSample.setText(codeFinder.getSample());
		chkTestAllRules.setSelection(codeFinder.useAllRulesWhenTesting());
		if ( lbRules.getItemCount() > 0 ) {
			lbRules.setSelection(0);
			updateDisplay();
		}
	}

	public String getRules () {
		if ( editMode ) {
			if ( !endEditMode(true) ) return null;
		}
		codeFinder.getRules().clear();
		for ( String pattern : lbRules.getItems() ) {
			codeFinder.addRule(pattern);
		}
		codeFinder.setSample(getSampleText());
		codeFinder.setUseAllRulesWhenTesting(chkTestAllRules.getSelection());
		return codeFinder.toString();
	}
	
	public boolean inEditMode () {
		return editMode;
	}
	
	@Override
	public void setEnabled (boolean enabled) {
		super.setEnabled(enabled);
		btAdd.setEnabled(enabled);
		chkTestAllRules.setEnabled(enabled);
		lbRules.setEnabled(enabled);
		edExpression.setEnabled(enabled);
		edResults.setEnabled(enabled);
		edSample.setEnabled(enabled);
		btInsertPattern.setEnabled(enabled);
		
		if ( enabled ) {
			updateDisplay();
		}
		else {
			if ( inEditMode() ) endEditMode(false);
			btDiscard.setEnabled(false);
			btRemove.setEnabled(false);
			btMoveUp.setEnabled(false);
			btMoveDown.setEnabled(false);
			btModify.setEnabled(false);
		}
	}

}
