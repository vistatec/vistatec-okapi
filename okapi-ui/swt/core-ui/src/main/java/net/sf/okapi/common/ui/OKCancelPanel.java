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

package net.sf.okapi.common.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Default panel for Help/OK/Cancel buttons
 */
public class OKCancelPanel extends Composite {

	public Button btOK;
	public Button btCancel;
	public Button btHelp;
	public Button btExtra;

	/**
	 * Creates a new panel for Help/OK/Cancel buttons.
	 * @param parent Parent control.
	 * @param flags Style flags.
	 */
	public OKCancelPanel (Composite parent,	int style)
	{
		super(parent, SWT.NONE);
		createContent(null, false, Res.getString("OKCancelPanel.btOK"), null);
	}

	/**
	 * Creates a new panel for Help/OK/Cancel buttons.
	 * @param parent Parent control.
	 * @param flags Style flags.
	 * @param action Action to execute when any of the buttons is clicked.
	 * The receiving event, the widget's data is marked: 'c' for the Cancel
	 * button, 'o' for OK, and 'h' for help.
	 * @param showHelp True to display the Help button.
	 */
	public OKCancelPanel (Composite parent,
		int flags,
		SelectionAdapter action,
		boolean showHelp)
	{
		super(parent, SWT.NONE);
		createContent(action, showHelp, Res.getString("OKCancelPanel.btOK"), null);
	}
	
	/**
	 * Creates a new panel for Help/OK/Cancel buttons.
	 * @param parent the parent control.
	 * @param flags the style flags.
	 * @param action the action to execute when any of the buttons is clicked.
	 * The receiving event, the widget's data is marked: 'c' for the Cancel
	 * button, 'o' for OK, and 'h' for help.
	 * @param showHelp true to display the Help button.
	 * @param okLabel the label for the 'o' button.
	 */
	public OKCancelPanel (Composite parent,
		int flags,
		SelectionAdapter action,
		boolean showHelp,
		String okLabel)
	{
		super(parent, SWT.NONE);
		createContent(action, showHelp, okLabel, null);
	}
	
	/**
	 * Creates a new panel for Help/OK/Cancel buttons.
	 * @param parent the parent control.
	 * @param flags the style flags.
	 * @param action the action to execute when any of the buttons is clicked.
	 * The receiving event, the widget's data is marked: 'c' for the Cancel
	 * button, 'o' for OK, and 'h' for help, and 'x' for the extra button.
	 * @param showHelp true to display the Help button.
	 * @param okLabel the label for the 'o' button.
	 * @param extraLabel the label for the 'x' button (can be null)
	 */
	public OKCancelPanel (Composite parent,
		int flags,
		SelectionAdapter action,
		boolean showHelp,
		String okLabel,
		String extraLabel)
	{
		super(parent, SWT.NONE);
		createContent(action, showHelp, okLabel, extraLabel);
	}
	
	private void createContent (SelectionAdapter action,
		boolean showHelp,
		String okLabel,
		String extraLabel)
	{
		GridLayout layTmp = new GridLayout(2, true);
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		setLayout(layTmp);
		int nWidth = UIUtil.BUTTON_DEFAULT_WIDTH;

		btHelp = new Button(this, SWT.PUSH);
		btHelp.setText(Res.getString("OKCancelPanel.btHelp"));
		btHelp.setData("h");
		if (action != null) {
			btHelp.addSelectionListener(action);
		}
		GridData gdTmp = new GridData();
		btHelp.setLayoutData(gdTmp);
		UIUtil.ensureWidth(btHelp, nWidth);
		btHelp.setVisible(showHelp);
		
		Composite cmpTmp = new Composite(this, SWT.NONE);
		RowLayout layRow = new RowLayout(SWT.HORIZONTAL);
		layRow.marginWidth = 0;
		layRow.marginHeight = 0;
		cmpTmp.setLayout(layRow);
		gdTmp = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gdTmp.grabExcessHorizontalSpace = true;
		cmpTmp.setLayoutData(gdTmp);

		// Create the buttons in a platform-specific order
		if ( UIUtil.getPlatformType() == UIUtil.PFTYPE_WIN ) {
			if ( extraLabel != null ) btExtra = new Button(cmpTmp, SWT.PUSH);
			btOK = new Button(cmpTmp, SWT.PUSH);
			btCancel = new Button(cmpTmp, SWT.PUSH);
		}
		else { // UIUtil.PFTYPE_UNIX, UIUtil.PFTYPE_MAC
			btCancel = new Button(cmpTmp, SWT.PUSH);
			btOK = new Button(cmpTmp, SWT.PUSH);
			if ( extraLabel != null ) btExtra = new Button(cmpTmp, SWT.PUSH);
		}

		btOK.setText(okLabel);
		btOK.setData("o");
		if (action != null) {
			btOK.addSelectionListener(action);
		}
		RowData rdTmp = new RowData();
		btOK.setLayoutData(rdTmp);
		btOK.pack();
		Rectangle rect1 = btOK.getBounds();
		
		btCancel.setText(Res.getString("OKCancelPanel.btCancel"));
		btCancel.setData("c");
		if (action != null) {
			btCancel.addSelectionListener(action);
		}
		rdTmp = new RowData();
		btCancel.setLayoutData(rdTmp);
		btCancel.pack();
		Rectangle rect2 = btCancel.getBounds();
		
		Rectangle rect3 = null;
		if ( btExtra != null ) {
			btExtra.setText(extraLabel);
			btExtra.setData("x");
			if (action != null) {
				btExtra.addSelectionListener(action);
			}
			rdTmp = new RowData();
			btExtra.setLayoutData(rdTmp);
			btExtra.pack();
			rect3 = btExtra.getBounds();
		}
		
		int max = rect1.width;
		if ( max < rect2.width ) max = rect2.width;
		if ( rect3 != null ) {
			if ( max < rect3.width ) max = rect3.width;
		}
		if ( max < nWidth ) max = nWidth;
		((RowData)btOK.getLayoutData()).width = max;
		((RowData)btCancel.getLayoutData()).width = max;
		if ( btExtra != null ) {
			((RowData)btExtra.getLayoutData()).width = max;
		}
	}
	
	public void setOKText (String text) {
		btOK.setText(text);
	}

}
