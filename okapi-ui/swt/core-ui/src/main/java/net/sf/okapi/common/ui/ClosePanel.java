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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Default panel for Help/Close buttons
 */
public class ClosePanel extends Composite {

	public Button btClose;
	public Button btHelp;

	/**
	 * Creates a new panel for Help/OK/Cancel buttons.
	 * @param parent Parent control.
	 * @param flags Style flags.
	 * @param action Action to execute when any of the buttons is clicked.
	 * The receiving event, the widget's data is marked: 'c' for the Close
	 * button, and 'h' for help.
	 * @param showHelp True to display the Help button.
	 */
	public ClosePanel (Composite parent,
		int flags,
		SelectionAdapter action,
		boolean showHelp)
	{
		super(parent, SWT.NONE);
		createContent(action, showHelp);
	}
	
	private void createContent (SelectionAdapter action,
		boolean showHelp)
	{
		GridLayout layTmp = new GridLayout(2, true);
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		setLayout(layTmp);
		int nWidth = UIUtil.BUTTON_DEFAULT_WIDTH;

		btHelp = new Button(this, SWT.PUSH);
		btHelp.setText(Res.getString("ClosePanel.btHelp")); //$NON-NLS-1$
		btHelp.setData("h"); //$NON-NLS-1$
		btHelp.addSelectionListener(action);
		btHelp.setLayoutData(new GridData());
		UIUtil.ensureWidth(btHelp, nWidth);
		btHelp.setVisible(showHelp);
		
		Composite cmpTmp = new Composite(this, SWT.NONE);
		layTmp = new GridLayout();
		layTmp.marginWidth = 0;
		layTmp.marginHeight = 0;
		cmpTmp.setLayout(layTmp);
		GridData gdTmp = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gdTmp.grabExcessHorizontalSpace = true;
		cmpTmp.setLayoutData(gdTmp);

		btClose = new Button(cmpTmp, SWT.PUSH);
		btClose.setText(Res.getString("ClosePanel.btClose")); //$NON-NLS-1$
		btClose.setData("c"); //$NON-NLS-1$
		btClose.addSelectionListener(action);
		btClose.setLayoutData(new GridData());
		UIUtil.ensureWidth(btClose, nWidth);
	}
	
}
