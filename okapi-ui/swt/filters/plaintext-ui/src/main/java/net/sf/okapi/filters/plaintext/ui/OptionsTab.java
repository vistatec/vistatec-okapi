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

package net.sf.okapi.filters.plaintext.ui;

import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.abstracteditor.IDialogPage;
import net.sf.okapi.common.ui.abstracteditor.SWTUtil;
import net.sf.okapi.common.ui.filters.InlineCodeFinderPanel;
import net.sf.okapi.lib.extra.filters.WrapMode;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

/**
 * Options tab for plain text and table filters 
 * 
 * @version 0.1, 13.06.2009
 */

public class OptionsTab extends Composite implements IDialogPage {
	private Button lead;
	private Button trail;
	private Group grpTextUnitProcessing;
	private Group grpInlineCodes;
	private Button inlines;
	private Group multi;
	private Button separate;
	private Button unwrap;
	private Button codes;
	private Button convert;
	private InlineCodeFinderPanel panel;
	private Button allow;
	private Label label;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public OptionsTab(Composite parent, int style) {
		
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		FormData formData_4 = new FormData();
		formData_4.right = new FormAttachment(100, -169);
		
		grpTextUnitProcessing = new Group(this, SWT.NONE);
		grpTextUnitProcessing.setLayout(new GridLayout(2, false));
		grpTextUnitProcessing.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpTextUnitProcessing.setText("Text unit processing");
		
		allow = new Button(grpTextUnitProcessing, SWT.CHECK);
		allow.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		allow.setData("name", "allow");
		allow.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				interop(e.widget);
			}
		});
		allow.setText("Allow trimming");
		
		label = new Label(grpTextUnitProcessing, SWT.NONE);
		label.setData("name", "label");
		label.setText("    ");
		
		lead = new Button(grpTextUnitProcessing, SWT.CHECK);
		lead.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lead.setData("name", "lead");
		lead.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			}
		});
		lead.setText("Trim leading spaces and tabs");
		new Label(grpTextUnitProcessing, SWT.NONE);
		
		trail = new Button(grpTextUnitProcessing, SWT.CHECK);
		trail.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		trail.setData("name", "trail");
		trail.setText("Trim trailing spaces and tabs");
		
		convert = new Button(grpTextUnitProcessing, SWT.CHECK);
		convert.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		convert.setText("Convert \\t, \\n, \\\\, \\uXXXX into characters            ");
		
		multi = new Group(this, SWT.NONE);
		multi.setData("name", "multi");
		multi.setLayout(new GridLayout(1, false));
		multi.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		multi.setText("Multi-line text units");
		
		separate = new Button(multi, SWT.RADIO);
		separate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		separate.setText("Separate lines with line-feeds (\\n)");
		
		unwrap = new Button(multi, SWT.RADIO);
		unwrap.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		unwrap.setText("Unwrap lines (replace line breaks with spaces)         ");
		
		codes = new Button(multi, SWT.RADIO);
		codes.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		codes.setText("Create inline codes for line breaks");

		grpInlineCodes = new Group(this, SWT.NONE);
		grpInlineCodes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		grpInlineCodes.setLayout(new GridLayout(1, false));
		grpInlineCodes.setText("Inline codes");
		
		inlines = new Button(grpInlineCodes, SWT.CHECK);
		inlines.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		inlines.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				interop(e.widget);
			}
		});
		inlines.setText("Has inline codes as defined below:");
		
		panel = new InlineCodeFinderPanel(grpInlineCodes, SWT.NONE);
		//panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	public void interop(Widget speaker) {

		if (allow.getSelection()) {
		
			lead.setEnabled(true);
			trail.setEnabled(true);
		}
		else {
			
			lead.setSelection(false);
			trail.setSelection(false);
			
			lead.setEnabled(false);
			trail.setEnabled(false);
		}
		
//		if (inlines.getSelection())
//			panel.enable(true);
//			//SWTUtil.setAllEnabled(panel, true);
//		else
//			SWTUtil.setAllEnabled(panel, false);
//		//	panel.enable(false);

		panel.setEnabled(inlines.getSelection());
		//This re-enable some buttons: panel.updateDisplay();
		
//		if (btnProcessInlineCodes.getSelection())
//			pnlCodeFinder.enable(true);
//		else
//			SWTUtil.setAllEnabled(pnlCodeFinder, false);
		
//		if (inlines.getSelection())
//			SWTUtil.setAllEnabled(grpInlineCodes, true);
//		else {
//			if (panel.inEditMode()) {
//
//				Dialogs.showError(getShell(), "Cannot exit the mode while the rules for inline codes are being edited." +
//						"\nPlease accept or discard changes first.", null);
//				inlines.setSelection(true);
//			}
//			else
//				SWTUtil.setAllEnabled(grpInlineCodes, false);
//		}
						
//		SWTUtil.setAllEnabled(grpInlineCodes, btnProcessInlineCodes.getSelection());
		
//		if (btnPreserveWhiteSpaces.getSelection()) {
//			
//			btnTrimLeft.setSelection(false);
//			btnTrimLeft.setEnabled(false);
//			
//			btnTrimRight.setSelection(false);
//			btnTrimRight.setEnabled(false);
//		}
//		else {
//			
//			btnTrimLeft.setEnabled(true);
//			btnTrimRight.setEnabled(true);
//		}		
	}

	public boolean load(Object data) {

		if (data instanceof net.sf.okapi.filters.plaintext.base.Parameters) {
			
			net.sf.okapi.filters.plaintext.base.Parameters params = 
				(net.sf.okapi.filters.plaintext.base.Parameters) data;
						
			allow.setSelection(!params.preserveWS);
			lead.setSelection(params.trimLeading);
			trail.setSelection(params.trimTrailing);
			convert.setSelection(params.unescapeSource);
						
			inlines.setSelection(params.useCodeFinder);
			panel.setRules(params.codeFinderRules);
			
			SWTUtil.unselectAll(multi);
			
			switch (params.wrapMode) {
			
				case NONE:
					separate.setSelection(true);
					break;
					
				case SPACES:
					unwrap.setSelection(true);
					break;
					
				case PLACEHOLDERS:
					codes.setSelection(true);
					break;					
			}
		}

		return true;		
	}

	public boolean save(Object data) {
		
		if (data instanceof net.sf.okapi.filters.plaintext.base.Parameters) {
		
			net.sf.okapi.filters.plaintext.base.Parameters params = 
				(net.sf.okapi.filters.plaintext.base.Parameters) data; 
				
			params.preserveWS = !allow.getSelection();
			params.trimLeading = lead.getSelection();
			params.trimTrailing = trail.getSelection();
			params.unescapeSource = convert.getSelection();			
			
			params.useCodeFinder = inlines.getSelection();
			params.codeFinderRules = panel.getRules();
			
			if (separate.getSelection())
				params.wrapMode = WrapMode.NONE;
			
			else if (unwrap.getSelection())
				params.wrapMode = WrapMode.SPACES;
			
			else if (codes.getSelection())
				params.wrapMode = WrapMode.PLACEHOLDERS;
		}
						
		return true;
	}

	public boolean canClose(boolean isOK) {
		
		if (panel.inEditMode() && isOK) {

			Dialogs.showWarning(getShell(), "Cannot close the window while the rules for inline codes are being edited." +
					"\nPlease accept or discard changes first.", null);
			return false;
		}
		return true;
	}
}

