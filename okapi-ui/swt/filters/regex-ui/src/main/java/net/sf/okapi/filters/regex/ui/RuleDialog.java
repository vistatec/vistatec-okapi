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

package net.sf.okapi.filters.regex.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.filters.regex.Rule;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RuleDialog {
	
	private Shell shell;
	private Text edExpression;
	private Text edSample;
	private Text edResult;
	private Text edSource;
	private boolean useSource;
	private Text edTarget;
	private boolean useTarget;
	private Text edName;
	private boolean useName;
	private Text edNote;
	private boolean useNote;
	private Combo cbRuleType;
	private boolean result = false;
	private Pattern fullPattern;
	private Rule rule = null;
	private int regexOptions;
	private IHelp help;
	private Font largerFont;

	@Override
	protected void finalize () {
		dispose();
	}

	public RuleDialog (Shell parent,
		IHelp helpParam,
		Rule rule,
		int regexOptions)
	{
		this.rule = rule;
		help = helpParam;
		this.regexOptions = regexOptions;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText(Res.getString("RuleDialog.caption")); //$NON-NLS-1$
		shell.setImage(parent.getImage());
		shell.setLayout(new GridLayout());
		
		Group grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText("Regular expression"); //$NON-NLS-1$
		grpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layTmp = new GridLayout();
		grpTmp.setLayout(layTmp);

		edExpression = new Text(grpTmp, SWT.BORDER | SWT.SINGLE);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		edExpression.setLayoutData(gdTmp);
		edExpression.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateResults();
			}
		});
		
		Font font = edExpression.getFont();
		FontData[] fontData = font.getFontData();
		fontData[0].setHeight(fontData[0].getHeight()+3);
		largerFont = new Font(font.getDevice(), fontData[0]);
		edExpression.setFont(largerFont);
		
		Label label = new Label(grpTmp, SWT.NONE);
		label.setText("Sample:");
		
		edSample = new Text(grpTmp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 64;
		edSample.setLayoutData(gdTmp);
		edSample.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateResults();
			}
		});

		edResult = new Text(grpTmp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 88;
		edResult.setLayoutData(gdTmp);
		edResult.setEditable(false);
		
		grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText("Action and groups");
		grpTmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layTmp = new GridLayout(4, false);
		grpTmp.setLayout(layTmp);
		
		Composite cmpTmp = new Composite(grpTmp, SWT.None);
		layTmp = new GridLayout(2, false);
		layTmp.marginWidth = 0;
		cmpTmp.setLayout(layTmp);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		cmpTmp.setLayoutData(gdTmp);
		
		label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("Editor.action")); //$NON-NLS-1$
		
		cbRuleType = new Combo(cmpTmp, SWT.DROP_DOWN | SWT.READ_ONLY);
		cbRuleType.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateGroups();
			}
		});
		cbRuleType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cbRuleType.add(Res.getString("Editor.extractStringsInside")); //$NON-NLS-1$
		cbRuleType.add(Res.getString("Editor.extractContent")); //$NON-NLS-1$
		cbRuleType.add(Res.getString("Editor.treatAsComment")); //$NON-NLS-1$
		cbRuleType.add(Res.getString("Editor.doNotExtract")); //$NON-NLS-1$
		cbRuleType.add(Res.getString("Editor.startGroup")); //$NON-NLS-1$
		cbRuleType.add(Res.getString("Editor.endGroup")); //$NON-NLS-1$

		label = new Label(grpTmp, SWT.NONE);
		label.setText("&Source group number:");
		
		int fieldWidth = 50;
		edSource = new Text(grpTmp, SWT.BORDER | SWT.SINGLE);
		gdTmp = new GridData();
		gdTmp.widthHint = fieldWidth;
		edSource.setLayoutData(gdTmp);
		edSource.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateResults();
			}
		});
		
		int indent = 10;
		label = new Label(grpTmp, SWT.NONE);
		label.setText("&Target group number:");
		gdTmp = new GridData();
		gdTmp.horizontalIndent = indent;
		label.setLayoutData(gdTmp);
		
		edTarget = new Text(grpTmp, SWT.BORDER | SWT.SINGLE);
		gdTmp = new GridData();
		gdTmp.widthHint = fieldWidth;
		edTarget.setLayoutData(gdTmp);
		edTarget.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateResults();
			}
		});
		
		label = new Label(grpTmp, SWT.NONE);
		label.setText("&Identifier group number:");
		
		edName = new Text(grpTmp, SWT.BORDER | SWT.SINGLE);
		gdTmp = new GridData();
		gdTmp.widthHint = fieldWidth;
		edName.setLayoutData(gdTmp);
		edName.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateResults();
			}
		});
		
		label = new Label(grpTmp, SWT.NONE);
		label.setText("&Note group number:");
		gdTmp = new GridData();
		gdTmp.horizontalIndent = indent;
		label.setLayoutData(gdTmp);
		
		edNote = new Text(grpTmp, SWT.BORDER | SWT.SINGLE);
		gdTmp = new GridData();
		gdTmp.widthHint = fieldWidth;
		edNote.setLayoutData(gdTmp);
		edNote.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateResults();
			}
		});
		
		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					if ( help != null ) help.showTopic(this, "editRule"); //$NON-NLS-1$
					return;
				}
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					if ( !saveData() ) return;
				}
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 600 ) startSize.x = 600; 
		if ( startSize.y < 550 ) startSize.y = 550; 
		shell.setSize(startSize);
		setData();
		Dialogs.centerWindow(shell, parent);
	}

	public void dispose () {
		if ( largerFont != null ) {
			largerFont.dispose();
			largerFont = null;
		}
	}

	public boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		dispose();
		return result;
	}

	public void updateGroups () {
		int type = cbRuleType.getSelectionIndex();
		switch ( type ) {
		case Rule.RULETYPE_STRING:
			useSource = true;
			useTarget = false;
			useName = true;
			useNote = true;
			break;
		case Rule.RULETYPE_CONTENT:
			useSource = true;
			useTarget = true;
			useName = true;
			useNote = true;
			break;
		case Rule.RULETYPE_COMMENT:
			useSource = true;
			useTarget = false;
			useName = false;
			useNote = false;
			break;
		case Rule.RULETYPE_NOTRANS:
		case Rule.RULETYPE_CLOSEGROUP:
			useSource = false;
			useTarget = false;
			useName = false;
			useNote = false;
			break;
		case Rule.RULETYPE_OPENGROUP:
			useSource = false;
			useTarget = false;
			useName = true;
			useNote = true;
			break;
		}
		edSource.setEnabled(useSource);
		edTarget.setEnabled(useTarget);
		edName.setEnabled(useName);
		edNote.setEnabled(useNote);
		updateResults();
	}
	
	private boolean updateResults () {
		boolean result = true;
		try {
			// Get the values
			fullPattern = Pattern.compile(edExpression.getText(), regexOptions);
			int source = Integer.valueOf(edSource.getText());
			int target = Integer.valueOf(edTarget.getText());
			int name = Integer.valueOf(edName.getText());
			int note = Integer.valueOf(edNote.getText());
			
			Matcher m1 = fullPattern.matcher(getSampleText());
			StringBuilder tmp = new StringBuilder();
			int startSearch = 0;
			while ( m1.find(startSearch) ) {
				if ( m1.start() == m1.end() ) break;
				boolean hasGroup = false;
				if ( tmp.length() > 0 ) tmp.append("-----\n");
				if ( useSource && ( source != -1 )) {
					tmp.append("Source=[" + m1.group(source) + "]\n");
					hasGroup = true;
				}
				if ( useTarget && ( target != -1 )) {
					tmp.append("Target=[" + m1.group(target) + "]\n");
					hasGroup = true;
				}
				if ( useName && ( name != -1 )) {
					tmp.append("Identifier=[" + m1.group(name) + "]\n");
					hasGroup = true;
				}
				if ( useNote && ( note != -1 )) {
					tmp.append("Note=[" + m1.group(note) + "]\n");
					hasGroup = true;
				}
				if ( !hasGroup ) tmp.append("Expression=[" + m1.group() + "]\n");
				startSearch = m1.end();
			}
			// If there is no match: tell it
			if ( tmp.length() == 0 ) {
				tmp.append(Res.getString("RuleDialog.noMatch")); //$NON-NLS-1$
			}
			// Display the results
			edResult.setText(tmp.toString());
		}
		catch ( Throwable e ) {
			edResult.setText(Res.getString("RuleDialog.error")+e.getMessage()); //$NON-NLS-1$
			result = false;
		}
		return result;
	}

	private String getSampleText() {
		// Change different line breaks type into \n cases
		String tmp = edSample.getText();
		tmp = tmp.replaceAll("\r\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		tmp = tmp.replace("\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return tmp.replace("\r", "\n");  //$NON-NLS-1$ //$NON-NLS-2$
	}

	private boolean saveData () {
		try {
			if ( edExpression.getText().length() == 0 ) {
				edExpression.selectAll();
				edExpression.setFocus();
				return false;
			}
			// Check if values are valid before setting the rule
			Integer.valueOf(edSource.getText());
			Integer.valueOf(edTarget.getText());
			Integer.valueOf(edName.getText());
			Integer.valueOf(edNote.getText());
			if ( !updateResults() ) throw new OkapiException("Error in expression or in group numbers.");
					
			rule.setExpression(edExpression.getText());
			rule.setSourceGroup(Integer.valueOf(edSource.getText()));
			rule.setTargetGroup(Integer.valueOf(edTarget.getText()));
			rule.setNameGroup(Integer.valueOf(edName.getText()));
			rule.setNoteGroup(Integer.valueOf(edNote.getText()));
			rule.setSample(getSampleText());
			rule.setRuleType(cbRuleType.getSelectionIndex());
			result = true;
			return result;
		}
		catch ( Exception e) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
			return false;
		}
	}

	public void setData () {
		edSource.setText(String.valueOf(rule.getSourceGroup()));
		edTarget.setText(String.valueOf(rule.getTargetGroup()));
		edName.setText(String.valueOf(rule.getNameGroup()));
		edNote.setText(String.valueOf(rule.getNoteGroup()));
		cbRuleType.select(rule.getRuleType());
		edExpression.setText(rule.getExpression());
		edSample.setText(rule.getSample());
	}

	public Rule getRule () {
		return rule;
	}

}
