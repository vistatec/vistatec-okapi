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

package net.sf.okapi.filters.table.ui;

import java.util.Arrays;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.abstracteditor.IInputQueryPage;
import net.sf.okapi.common.ui.abstracteditor.SWTUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * 
 * 
 * @version 0.1, 24.06.2009
 */

public class AddModifyColumnDefPage extends Composite implements IInputQueryPage {
	
	public static final String TYPE_SOURCE		 = "Source"; 
	public static final String TYPE_SOURCE_ID	 = "Source ID";
	public static final String TYPE_TARGET		 = "Target";
	public static final String TYPE_COMMENT 	 = "Comment";
	public static final String TYPE_RECORD_ID 	 = "Record ID";
	
	private Composite composite;
	private Label lblColumnNumber;
	private Spinner colNum;
	private Group typeGroup;
	private Composite composite_1;
	private Button typeSource;
	private Button typeSourceId;
	private Button typeTarget;
	private Button typeComment;
	private Button typeRecordId;
	private Label lblSourceColumn;
	private Label lblIdSuffix;
	private Label label_2;
	private Label lstart;
	private Label lend;
	private Spinner srcIndex;
	private Spinner start;
	private Spinner end;
	private Text suffix;
	private Text language;
	private String[] colDef;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public AddModifyColumnDefPage(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1));
		
		lblColumnNumber = new Label(composite, SWT.NONE);
		lblColumnNumber.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblColumnNumber.setText("Column #:");
		
		colNum = new Spinner(composite, SWT.BORDER);
		colNum.setMinimum(1);
		
		typeGroup = new Group(composite, SWT.NONE);
		typeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		typeGroup.setLayout(new GridLayout(1, false));
		typeGroup.setText("Type");
		
		composite_1 = new Composite(typeGroup, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));
		
		typeSource = new Button(composite_1, SWT.RADIO);
		typeSource.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				interop(e.widget);
			}
		});
		typeSource.setText(TYPE_SOURCE);
		
		typeSourceId = new Button(composite_1, SWT.RADIO);
		typeSourceId.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				interop(e.widget);
			}
		});
		typeSourceId.setText(TYPE_SOURCE_ID);
		
		typeTarget = new Button(composite_1, SWT.RADIO);
		typeTarget.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				interop(e.widget);
			}
		});
		typeTarget.setText(TYPE_TARGET);
		
		typeComment = new Button(composite_1, SWT.RADIO);
		typeComment.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				interop(e.widget);
			}
		});
		typeComment.setText(TYPE_COMMENT);
		
		typeRecordId = new Button(composite_1, SWT.RADIO);
		typeRecordId.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				interop(e.widget);
			}
		});
		typeRecordId.setText(TYPE_RECORD_ID);
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));
		
		lblSourceColumn = new Label(composite, SWT.NONE);
		lblSourceColumn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSourceColumn.setText("Source column:");
		
		srcIndex = new Spinner(composite, SWT.BORDER);
		
		label_2 = new Label(composite, SWT.NONE);
		label_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_2.setText("Language:");
		
		language = new Text(composite, SWT.BORDER);
		language.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		lblIdSuffix = new Label(composite, SWT.NONE);
		lblIdSuffix.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblIdSuffix.setAlignment(SWT.RIGHT);
		lblIdSuffix.setText("ID suffix:");
		
		suffix = new Text(composite, SWT.BORDER);
		suffix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		lstart = new Label(composite, SWT.NONE);
		lstart.setData("name", "lstart");
		lstart.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lstart.setText("Start:");
		
		start = new Spinner(composite, SWT.BORDER);
		start.setData("name", "start");
		start.setMaximum(1000);
		start.setMinimum(1);
		
		lend = new Label(composite, SWT.NONE);
		lend.setData("name", "lend");
		lend.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lend.setText("End:");
		
		end = new Spinner(composite, SWT.BORDER);
		end.setData("name", "end");
		end.setMaximum(1000);
		end.setMinimum(1);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public boolean load(Object data) {

		if (!(data instanceof String[])) return false;
		
		colDef = (String[]) data;
		if (colDef.length != 7) return false; 
		
		colNum.setSelection(Util.strToInt(colDef[0], 1));
		SWTUtil.setRadioGroupSelection(typeGroup, colDef[1]);
		srcIndex.setSelection(Util.strToInt(colDef[2], 0));		
		language.setText(colDef[3]);
		suffix.setText(colDef[4]);
		start.setSelection(Util.strToInt(colDef[5], 0));
		end.setSelection(Util.strToInt(colDef[6], 0));
		
		return true;
	}

	public boolean save(Object data) {

		if (!(data instanceof String[])) return false;
		
		String[] colDef = (String[]) data;
		if (colDef.length != 7) return false;
		
		Arrays.fill(colDef, "");
		
		colDef[0] = Util.intToStr(colNum.getSelection());
		
		Button btn = SWTUtil.getRadioGroupSelection(typeGroup);
		if (btn == null) 
			colDef[1] = "";		
		else
			colDef[1] = btn.getText();
		
		if (srcIndex.isEnabled()) 
			colDef[2] = Util.intToStr(srcIndex.getSelection());
		
		if (language.isEnabled())
			colDef[3] = language.getText();
		
		if (suffix.isEnabled())
			colDef[4] = suffix.getText();
		
		if (start.isEnabled())
			colDef[5] = Util.intToStr(start.getSelection());
		
		if (end.isEnabled())
			colDef[6] = Util.intToStr(end.getSelection());
		
		return true;
	}

	public void setPrompt(String prompt) {
		
	}

	public void interop(Widget speaker) {
		
		if (typeSource.getSelection()) {
		
			srcIndex.setMinimum(0);
			srcIndex.setSelection(0);
			srcIndex.setEnabled(false);
			
			language.setText("");
			language.setEnabled(false);
			
			//suffix.setText("");
			suffix.setText(colDef[4]);
			suffix.setEnabled(true);
		} 
		else if (typeSourceId.getSelection()) {
			
			srcIndex.setMinimum(1);
			//srcIndex.setSelection(1);
			srcIndex.setSelection(Util.strToInt(colDef[2], 0));
			srcIndex.setEnabled(true);
			
			language.setText("");
			language.setEnabled(false);
			
			suffix.setText("");
			suffix.setEnabled(false);
		}
		else if (typeTarget.getSelection()) {
		
			srcIndex.setMinimum(1);
			//srcIndex.setSelection(1);
			srcIndex.setSelection(Util.strToInt(colDef[2], 0));
			srcIndex.setEnabled(true);
			
			//language.setText("");
			language.setText(colDef[3]);
			language.setEnabled(true);
			
			suffix.setText("");
			suffix.setEnabled(false);
		}
		else if (typeComment.getSelection()) {
		
			srcIndex.setMinimum(1);
			//srcIndex.setSelection(1);
			srcIndex.setSelection(Util.strToInt(colDef[2], 0));
			srcIndex.setEnabled(true);
			
			language.setText("");
			language.setEnabled(false);
			
			suffix.setText("");
			suffix.setEnabled(false);
		}
		else if (typeRecordId.getSelection()) {
		
			srcIndex.setMinimum(0);
			srcIndex.setSelection(0);
			srcIndex.setEnabled(false);
			
			language.setText("");
			language.setEnabled(false);
			
			suffix.setText("");
			suffix.setEnabled(false);
		}

		//load(data); // to restore initial fields
	}

	public boolean canClose(boolean isOK) {

		return true;
	}
}

