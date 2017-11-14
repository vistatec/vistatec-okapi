/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.searchandreplace.ui;

import java.io.File;
import java.util.regex.Pattern;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.ISWTEmbeddableParametersEditor;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.TextAndBrowsePanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.steps.searchandreplace.Parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

@EditorFor(Parameters.class)
public class ParametersEditor implements IParametersEditor, ISWTEmbeddableParametersEditor {

	public static final int ADD_ITEM    = 1;
	public static final int EDIT_ITEM   = 2;

	private Shell dialog;	
	private OKCancelPanel pnlActionsDialog;	
	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private Table table;
	private Text searchText;
	private Text replacementText;
	private Button btnImport;
	private Button btnExport;
	private Button btMoveUp;
	private Button btMoveDown;
	private Button chkSource;
	private Button chkTarget;
	private Button chkRegEx;
	private Button chkDotAll;
	private Button chkIgnoreCase;
	private Button chkReplaceAll;
	private Button chkMultiLine;
	private int updateType;
	private IHelp help;
	private Composite mainComposite;
	TextAndBrowsePanel pnlReplPath;
	private Button chkSaveLog;
	TextAndBrowsePanel pnlLogPath;
	
	public boolean edit (IParameters params,
		boolean readOnly,
		IContext context)
	{
		boolean bRes = false;
		try {
			shell = null;
			help = (IHelp)context.getObject("help");
			this.params = (Parameters)params;
			shell = new Shell((Shell)context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
			create((Shell)context.getObject("shell"), readOnly);
			return showDialog();
		}
		catch ( Exception e ) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
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
	
	@Override
	public Composite getComposite () {
		return mainComposite;
	}

	@Override
	public void initializeEmbeddableEditor (Composite parent,
		IParameters paramsObject,
		IContext context)
	{
		params = (Parameters)paramsObject; 
		shell = (Shell)context.getObject("shell");
		createComposite(parent);
		setData(params);
	}

	@Override
	public String validateAndSaveParameters () {
		if ( !saveData(params) ) return null;
		return params.toString();
	}
	
	private void updateUpDownBtnState(){
		int index = table.getSelectionIndex();
		int items = table.getItemCount();
		
        if ( items > 1 ) {
        	if ( index == -1 ) {
	        	btMoveDown.setEnabled(false);
	        	btMoveUp.setEnabled(false);
        	}
        	else if ( index == 0 ) {
	        	btMoveUp.setEnabled(false);
	        	btMoveDown.setEnabled(true);
	        }
        	else if(( index+1 ) == items ) {
	        	btMoveDown.setEnabled(false);
	        	btMoveUp.setEnabled(true);
	        }
        	else {
	        	btMoveDown.setEnabled(true);
	        	btMoveUp.setEnabled(true);
	        }
        }
        else {
        	btMoveDown.setEnabled(false);
        	btMoveUp.setEnabled(false);
        }
	}

	private void createComposite (Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new GridLayout(2, false));
		
		// Search and replace grid items
		table = new Table (mainComposite, SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible (true);
		table.setLinesVisible (true);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		gdTmp.heightHint = 150;
		table.setLayoutData(gdTmp);

		// Click updates button states
		table.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event event) {
				if ( event.detail!=SWT.CHECK ) {
					updateUpDownBtnState();
				}
			}
		});		

		// Double-click opens editor
		table.addListener (SWT.MouseDoubleClick, new Listener () {
			public void handleEvent (Event event) {
				if(table.getSelectionIndex()!=-1){
					updateType=EDIT_ITEM;
					showAddItemsDialog();
					updateUpDownBtnState();
				}				
			}
		});		
		
		// Resizing the columns
		table.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				int tableWidth = table.getBounds().width;
				int remaining = tableWidth - table.getColumn(0).getWidth();
				table.getColumn(1).setWidth(remaining/2-2);
				table.getColumn(2).setWidth(remaining/2-2);
			}
		});
		
		// Table headers
		String[] titles = {"Use", "Search For", "Replace By"};
		for (int i=0; i<titles.length; i++) {
			TableColumn column = new TableColumn (table, SWT.LEFT);
			column.setText (titles [i]);
			column.pack();
		}

		// Buttons
		int standardWidth = 80;
		// Add, edit, delete, move-up, move-down
		Composite cmpTmp = new Composite(mainComposite, SWT.NONE);
		GridLayout layTmp = new GridLayout(5, true);
		layTmp.marginHeight = layTmp.marginWidth = 0;
		cmpTmp.setLayout(layTmp);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		cmpTmp.setLayoutData(gdTmp);
		
		Button btAdd = new Button(cmpTmp, SWT.PUSH);
		btAdd.setText("Add...");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		btAdd.setLayoutData(gdTmp);
		UIUtil.ensureWidth(btAdd, standardWidth);
		btAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateType = ADD_ITEM;
				showAddItemsDialog();
			}
		});		
		
		Button btEdit = new Button(cmpTmp, SWT.PUSH);
		btEdit.setText("Edit...");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		btEdit.setLayoutData(gdTmp);
		btEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(table.getSelectionIndex()!=-1){
					updateType = EDIT_ITEM; 
					showAddItemsDialog();
				}				
			}
		});		
		
		Button btRemove = new Button(cmpTmp, SWT.PUSH);
		btRemove.setText("Remove");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		btRemove.setLayoutData(gdTmp);
		btRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(table.getSelectionIndex()!=-1){
					int index = table.getSelectionIndex();
					table.remove(index);
					if(index == table.getItemCount())
						table.setSelection(index-1);
					else
						table.setSelection(index);
					updateUpDownBtnState();
				}
			}
		});	
		

		btMoveUp = new Button(cmpTmp, SWT.PUSH);
		btMoveUp.setText("Move Up");
		btMoveUp.setEnabled(false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		btMoveUp.setLayoutData(gdTmp);
		btMoveUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( table.getSelectionIndex()!=-1 ) {

			        int index = table.getSelectionIndex();
			        boolean isChecked=false;
			        
			        TableItem ti = table.getItem(index);
			        isChecked = ti.getChecked();
			        String[] values = {ti.getText(0), ti.getText(1), ti.getText(2)};
			        ti.dispose();

					ti = new TableItem (table, SWT.NONE,index-1);
					ti.setChecked(isChecked);
					String [] strs =values;
					ti.setText(strs);
					table.select(index-1);
					
					updateUpDownBtnState();
				}
			}
		});	
		
		btMoveDown = new Button(cmpTmp, SWT.PUSH);
		btMoveDown.setText("Move Down");
		btMoveDown.setEnabled(false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		btMoveDown.setLayoutData(gdTmp);
		btMoveDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				if ( table.getSelectionIndex()!=-1 ) {
					
			        int index = table.getSelectionIndex();
			        boolean isChecked=false;
			        
			        TableItem ti = table.getItem(index);
			        isChecked = ti.getChecked();
			        String[] values = {ti.getText(0), ti.getText(1), ti.getText(2)};
			        ti.dispose();

					ti = new TableItem (table, SWT.NONE,index+1);
					ti.setChecked(isChecked);
					String [] strs =values;
					ti.setText(strs);
					table.select(index+1);
					
					updateUpDownBtnState();
				}
			}
		});			
		
		btnImport = new Button(cmpTmp, SWT.PUSH);
		btnImport.setText("Import...");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		btnImport.setLayoutData(gdTmp);
		btnImport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Import Search and Replace Options");
				String selected = fd.open();
				if ( selected != null ) {
					try {
						Parameters tmpParams = new Parameters(); 
						tmpParams.load(Util.toURL(selected), false);
						setData(tmpParams);
					}
					catch ( Throwable err ) {
						Dialogs.showError(shell, err.getMessage(), null);
					}
				}
			}
		});			
		
		btnExport = new Button(cmpTmp, SWT.PUSH);
		btnExport.setText("Export...");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		btnExport.setLayoutData(gdTmp);
		btnExport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setText("Export Search and Replace Options");
				fd.setOverwrite(true);
				String selected = fd.open();
				if ( selected != null ) {
					Parameters tmpParams = new Parameters();
					if ( saveData(tmpParams) ) {
						tmpParams.save(selected);
					}
				}
			}
		});			

		// Regular expression option flag
		chkRegEx = new Button(mainComposite, SWT.CHECK);
		chkRegEx.setText("Use regular expressions");
		chkRegEx.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chkDotAll.setEnabled(chkRegEx.getSelection());
				chkMultiLine.setEnabled(chkRegEx.getSelection());
				chkIgnoreCase.setEnabled(chkRegEx.getSelection());
				chkReplaceAll.setEnabled(chkRegEx.getSelection());
			}
		});
		
		// Placeholder
		new Label(mainComposite, SWT.NONE);
		
		//--- Regular expression options group
		Group group = new Group(mainComposite, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setText("Regular expression options");

		chkDotAll = new Button(group, SWT.CHECK);
		chkDotAll.setText("Dot also matches line-feed");
		
		chkMultiLine = new Button(group, SWT.CHECK);
		chkMultiLine.setText("Multi-line");
		
		chkIgnoreCase = new Button(group, SWT.CHECK);
		chkIgnoreCase.setText("Ignore case differences");

		chkReplaceAll = new Button(group, SWT.CHECK);
		chkReplaceAll.setText("Replace all instances of the pattern");
		chkReplaceAll.setToolTipText("If true replace all instances, otherwise replace only the first");
		
		//--- Filter event options group
		group = new Group(mainComposite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText("When processing text units (i.e. using a filter)");

		chkSource = new Button(group, SWT.CHECK);
		chkSource.setText("Search and replace the source content");
		
		chkTarget = new Button(group, SWT.CHECK);
		chkTarget.setText("Search and replace the target content");
		
		// Replacements file
		Label label = new Label(mainComposite, SWT.NONE);
		label.setText("Path of file with replacements (Leave empty if not used):");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		label.setLayoutData(gdTmp);
		
		pnlReplPath = new TextAndBrowsePanel(mainComposite, SWT.NONE, false);
		pnlReplPath.setSaveAs(false);
		pnlReplPath.setBrowseFilters("Tab-Delimited Files (*.txt)\tAll Files (*.*)", "*.txt\t*.*");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		pnlReplPath.setLayoutData(gdTmp);
		
		// Log section
		chkSaveLog = new Button(mainComposite, SWT.CHECK);
		chkSaveLog.setText("Save in the following file a log of the replacements performed:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkSaveLog.setLayoutData(gdTmp);
		chkSaveLog.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pnlLogPath.setEnabled(chkSaveLog.getSelection());
			}
		});
		
		pnlLogPath = new TextAndBrowsePanel(mainComposite, SWT.NONE, false);
		pnlLogPath.setSaveAs(true);
		pnlLogPath.setBrowseFilters("Log Files (*.txt)\tAll Files (*.*)", "*.txt\t*.*");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		pnlLogPath.setLayoutData(gdTmp);
	}
	
	private void create (Shell parent,
		boolean readOnly)
	{
		shell.setText("Search and Replace");
		if ( parent != null ) UIUtil.inheritIcon(shell, parent);

		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		createComposite(shell);
		
		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("Search and Replace Step");
					return;
				}
				if ( e.widget.getData().equals("o") ){
					if ( !saveData(params) ) {
						return;
					}
				}
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pnlActions.btOK.setEnabled(!readOnly);
		if ( !readOnly ) {
			shell.setDefaultButton(pnlActions.btOK);
		}

		setData(params);
		shell.pack();
		shell.setMinimumSize(shell.getSize());
		shell.setSize(600, 400);
		Dialogs.centerWindow(shell, parent);
	}
	
	private boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	private boolean showAddItemsDialog () {
		dialog = new Shell (mainComposite.getShell(), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		dialog.setText ("Search And Replace Item");

		dialog.setLayout(new GridLayout());

		// start - content
		Label label = new Label(dialog, SWT.NONE);
		label.setText("Search expression:");
		
		searchText = new Text(dialog, SWT.BORDER);
		searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		
		label = new Label(dialog, SWT.NONE);
		label.setText("Replacement expression:");

		replacementText = new Text(dialog, SWT.BORDER);
		replacementText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// end - content		

		// start - dialog level buttons
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					//--validating empty search string--
					if(searchText.getText().trim().length()<1){
						Dialogs.showError(shell, "You need to provide a search expression", null);
						return;
					}					
					//--validating regEx--
					if( chkRegEx.getSelection() ){
						try{
							Pattern.compile(searchText.getText());
							Pattern.compile(replacementText.getText());

						}catch(Exception ex){
							Dialogs.showError(shell, ex.getLocalizedMessage(), null);
							return;
						}
					}
					if(updateType==EDIT_ITEM){
						int index = table.getSelectionIndex();
				        TableItem ti = table.getItem(index);
				        String [] s ={"",searchText.getText(),replacementText.getText()};
				        ti.setText(s);
					}else{
						TableItem item = new TableItem (table, SWT.NONE);
						String [] strs ={"",searchText.getText(),replacementText.getText()};
						item.setText(strs);
						item.setChecked(true);
						table.setSelection(table.getItemCount()-1);
						updateUpDownBtnState();
					}
				}
				dialog.close();
			};
		};

		pnlActionsDialog = new OKCancelPanel(dialog, SWT.NONE, OKCancelActions, false);
		pnlActionsDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dialog.setDefaultButton(pnlActionsDialog.btOK);
		// end - dialog level buttons
		
		// begin - initialize edit fields
		if ( updateType==EDIT_ITEM ) {
			int index = table.getSelectionIndex();
	        TableItem ti = table.getItem(index);
	        searchText.setText(ti.getText(1));
	        replacementText.setText(ti.getText(2));
		}
		// end - initialize edit fields
		
		dialog.pack();
		dialog.setMinimumSize(dialog.getSize());
		Dialogs.centerWindow(dialog, shell);
		dialog.open ();
		while ( !dialog.isDisposed() ) {
			if ( !dialog.getDisplay().readAndDispatch() )
				dialog.getDisplay().sleep();
		}
		return result;
	}
	
	private void setData (Parameters fromParams) {
		pnlReplPath.setText(fromParams.getReplacementsPath());
		pnlLogPath.setText(fromParams.getLogPath());
		chkSaveLog.setSelection(fromParams.getSaveLog());
		
		chkRegEx.setSelection(fromParams.getRegEx());
		chkDotAll.setSelection(fromParams.getDotAll());
		chkIgnoreCase.setSelection(fromParams.getIgnoreCase());
		chkReplaceAll.setSelection(fromParams.getReplaceAll());
		chkMultiLine.setSelection(fromParams.getMultiLine());
		chkTarget.setSelection(fromParams.getTarget());
		chkSource.setSelection(fromParams.getSource());

		chkDotAll.setEnabled(chkRegEx.getSelection());
		chkMultiLine.setEnabled(chkRegEx.getSelection());
		chkIgnoreCase.setEnabled(chkRegEx.getSelection());
		chkReplaceAll.setEnabled(chkRegEx.getSelection());	
		
		table.removeAll();
        for ( String[] s : fromParams.rules ) {
        	TableItem item = new TableItem (table, SWT.NONE);
			String [] strs ={"",s[1],s[2]};
			item.setText(strs);
			if ( s[0].equals("true") ) {
				item.setChecked(true);				
			}
        }
        table.setSelection(0);
        updateUpDownBtnState();
        pnlLogPath.setEnabled(chkSaveLog.getSelection());
	}

	private boolean saveData (Parameters destParams) {
		// validate regular expressions
		if(chkRegEx.getSelection() && !validRegEx()) return false;
		
		// Make sure the list is not empty or the replacements path is not empty
		if (( table.getItemCount()==0 ) && pnlReplPath.getText().isEmpty() ) {
			Dialogs.showError(shell, "You need to provide a search expression in the table or to select a replacements file.", null);
			return false;
		}
		
		destParams.reset();
		for ( int i=0; i<table.getItemCount(); i++ ) {
			TableItem ti = table.getItem(i);
			String s[]=new String[3];
			s[0]=Boolean.toString(ti.getChecked());
			s[1]=ti.getText(1);
			s[2]=ti.getText(2);
			destParams.addRule(s);
		};
	
		destParams.setReplacementsPath(pnlReplPath.getText());
		destParams.setRegEx(chkRegEx.getSelection());
		destParams.setDotAll(chkDotAll.getSelection());		
		destParams.setIgnoreCase(chkIgnoreCase.getSelection());
		destParams.setReplaceAll(chkReplaceAll.getSelection());
		destParams.setMultiLine(chkMultiLine.getSelection());
		destParams.setTarget(chkTarget.getSelection());
		destParams.setSource(chkSource.getSelection());
		destParams.setLogPath(pnlLogPath.getText());
		destParams.setSaveLog(chkSaveLog.getSelection());

		result = true;
		return result;
	}

	private boolean validRegEx(){

		for ( int i=0; i<table.getItemCount(); i++ ) {
			TableItem ti = table.getItem(i);
			try{
				Pattern.compile(ti.getText(1));
				Pattern.compile(ti.getText(2));

			}catch(Exception ex){
				Dialogs.showError(shell, ex.getLocalizedMessage(), null);
				return false;
			}			
		};
		return true;
	}
}
