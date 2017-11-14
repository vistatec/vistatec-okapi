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

package net.sf.okapi.filters.abstractmarkup.ui;

import java.util.regex.Pattern;

import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Simple multi-purpose input dialog box.
 */
public class ConditionsDialog {

	private static final String LABEL_MODIFY = "Modify";
	private static final String LABEL_ACCEPT = "Accept";
	private static final String LABEL_REMOVE = "Remove";
	private static final String LABEL_DISCARD = "Discard";
	
	private Shell shell;
	private boolean result;
	private String help;
	private OKCancelPanel pnlActions;
	private java.util.List<Condition> oriConditions;
	private List lbConditions;
	private Text edPart1;
	private Combo cbOperator;
	private Text edPart2;
	private Button btAdd;
	private Button btRemoveOrDiscard;
	private Button btModifyOrAccept;
	private boolean editMode;

	public ConditionsDialog (Shell parent,
		String helpFile,
		java.util.List<Condition> oriConditions)
	{
		// Make a copy of the conditions
		this.oriConditions = oriConditions;
		
		help = helpFile;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Edit Conditions");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());
		
		Composite cmpTmp = new Composite(shell, SWT.BORDER);
		cmpTmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layTmp = new GridLayout(3, false);
		cmpTmp.setLayout(layTmp);
		
		edPart1 = new Text(cmpTmp, SWT.BORDER);
		edPart1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		cbOperator = new Combo(cmpTmp, SWT.DROP_DOWN | SWT.READ_ONLY);
		cbOperator.add(TaggedFilterConfiguration.EQUALS);
		cbOperator.add(TaggedFilterConfiguration.NOT_EQUALS);
		cbOperator.add(TaggedFilterConfiguration.MATCHES);
		
		edPart2 = new Text(cmpTmp, SWT.BORDER);
		edPart2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Buttons
		
		Composite cmpButtons = new Composite(shell, SWT.NONE);
		cmpButtons.setLayoutData(new GridData());
		layTmp = new GridLayout(3, false);
		cmpButtons.setLayout(layTmp);

		btAdd = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Add", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				addCondition();
            }
		});
		
		btRemoveOrDiscard = UIUtil.createGridButton(cmpButtons, SWT.PUSH, LABEL_REMOVE, UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btRemoveOrDiscard.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if ( editMode ) toggleEditMode(false);
				else removeCondition();
            }
		});

		btModifyOrAccept = UIUtil.createGridButton(cmpButtons, SWT.PUSH, LABEL_MODIFY, UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btModifyOrAccept.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if ( editMode ) acceptChanges();
				else toggleEditMode(true);
            }
		});

		// List of the conditions
		
		lbConditions = new List(shell, SWT.BORDER | SWT.H_SCROLL);
		lbConditions.setLayoutData(new GridData(GridData.FILL_BOTH));
		lbConditions.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				updateCondition();
            }
		});
		lbConditions.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				toggleEditMode(true);
			}
			public void mouseDown(MouseEvent e) {}
			public void mouseUp(MouseEvent e) {}
		});
		
		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					UIUtil.start(help);
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					if ( editMode ) {
						if ( !acceptChanges() ) return;
					}
					if ( !saveData() ) return;
					result = true;
				}
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, (helpFile != null));
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		Point size = shell.getSize();
		shell.setMinimumSize(size);
		if ( size.x < 550 ) size.x = 550;
		if ( size.y < 300 ) size.y = 300;
		shell.setSize(size);
		Dialogs.centerWindow(shell, parent);
		setData();
	}
	
	private void addCondition () {
		// Add a new condition
		Condition cond = new Condition();
		cond.part1 = "Enter Name";
		cond.part2 = "Enter Value(s)";
		lbConditions.add(cond.part1);
		lbConditions.setData(cond.part1, cond);
		lbConditions.select(lbConditions.getItemCount()-1);
		updateButtons();
		updateCondition();
		// And switch to edit mode
		toggleEditMode(true);
	}

	private boolean acceptChanges () {
		int n = lbConditions.getSelectionIndex();
		Condition cond = (Condition)lbConditions.getData(lbConditions.getItem(n));

		// Validate first
		String part1 = edPart1.getText().trim();
		if ( part1.isEmpty() ) {
			Dialogs.showError(shell, "You must enter an attribute name.", null);
			edPart1.setFocus();
			return false;
		}
		String part2 = edPart2.getText().trim();
		if ( part2.isEmpty() ) {
			Dialogs.showError(shell, "You must enter a value or a list of values.", null);
			edPart2.setFocus();
			return false;
		}
		// Check the regular expression
		if ( cbOperator.getItem(cbOperator.getSelectionIndex()).equals(TaggedFilterConfiguration.MATCHES)  ) {
			try {
				Pattern.compile(part2);
			}
			catch ( Throwable e ) {
				Dialogs.showError(shell, "Error in regular expression:\n"+e.getMessage(), null);
				edPart2.setFocus();
				return false;
			}
		}
		
		// Then, update the entry
		cond.part1 = part1;
		cond.part2 = part2;
		cond.operator = cbOperator.getItem(cbOperator.getSelectionIndex());
		String str = cond.toString();
		lbConditions.setItem(n, str); // Set the new string displayed
		lbConditions.setData(str, cond); // And don't forget to reset the corresponding data
		
		// Change the UI back to non-edit mode
		toggleEditMode(false);
		return true;
	}
	
	private void toggleEditMode (boolean editMode) {
		this.editMode = editMode;
		btAdd.setVisible(!editMode);
		edPart1.setEditable(editMode);
		cbOperator.setEnabled(editMode);
		edPart2.setEditable(editMode);
		if ( editMode ) {
			btRemoveOrDiscard.setText(LABEL_DISCARD);
			btModifyOrAccept.setText(LABEL_ACCEPT);
			edPart1.selectAll();
			edPart1.setFocus();
		}
		else {
			btRemoveOrDiscard.setText(LABEL_REMOVE);
			btModifyOrAccept.setText(LABEL_MODIFY);
			updateCondition();
		}
	}
	
	private void removeCondition () {
		int n = lbConditions.getSelectionIndex();
		if ( n < 0 ) return;
		lbConditions.remove(n);
		if ( n >= lbConditions.getItemCount() ) {
			n = lbConditions.getItemCount()-1;
		}
		if ( n > -1 ) {
			lbConditions.select(n);
		}
		updateButtons();
		updateCondition();
	}
	
	private void updateCondition () {
		int n = lbConditions.getSelectionIndex();
		if ( n < 0 ) {
			edPart1.setText("");
			cbOperator.select(0);
			edPart2.setText("");
		}
		else {
			Condition cond = (Condition)lbConditions.getData(lbConditions.getItem(n));
			edPart1.setText(cond.part1);
			int i = 0;
			n = 0;
			for ( String str : cbOperator.getItems() ) {
				if ( str.equals(cond.operator) ) {
					n = i;
					break;
				}
				i++;
			}
			cbOperator.select(n);
			edPart2.setText(cond.part2);
		}
	}

	private void updateButtons () {
		boolean enabled = (lbConditions.getItemCount()>0);
		btRemoveOrDiscard.setEnabled(enabled);
		btModifyOrAccept.setEnabled(enabled);
	}
	
	public boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	private void setData () {
		for ( Condition cond : oriConditions ) {
			String str = cond.toString();
			lbConditions.add(str);
			lbConditions.setData(str, cond.clone()); // Make a copy!
		}
		if ( lbConditions.getItemCount() > 0 ) {
			lbConditions.select(0);
			lbConditions.setFocus();
		}
		else {
			btAdd.setFocus();
		}
		toggleEditMode(false);
		updateButtons();
	}
	
	private boolean saveData () {
		// Reset the original list with the conditions from the UI list
		oriConditions.clear();
		for ( int i=0; i<lbConditions.getItemCount(); i++ ) {
			oriConditions.add((Condition)lbConditions.getData(lbConditions.getItem(i)));
		}
		return true;
	}

}
