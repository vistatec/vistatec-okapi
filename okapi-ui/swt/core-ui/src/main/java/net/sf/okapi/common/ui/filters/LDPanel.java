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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Implements a panel for selecting localization directives options.
 */
public class LDPanel extends Composite {

	private Button      chkUseLD;
	private Button      chkLocalizeOutside;
	
	public LDPanel (Composite parent,
		int flags)
	{
		super(parent, flags);
		createContent();
	}
	
	private void createContent () {
		GridLayout layTmp = new GridLayout();
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		setLayout(layTmp);

		chkUseLD = new Button(this, SWT.CHECK);
		chkUseLD.setText(Res.getString("LDPanel.useIfPresent")); //$NON-NLS-1$
		chkUseLD.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateDisplay();
			};
		});
		
		chkLocalizeOutside = new Button(this, SWT.CHECK);
		chkLocalizeOutside.setText(Res.getString("LDPanel.extractOutside")); //$NON-NLS-1$
		GridData gdTmp = new GridData();
		gdTmp.horizontalIndent = 16;
		chkLocalizeOutside.setLayoutData(gdTmp);
		
		updateDisplay();
	}
	
	public void updateDisplay () {
		chkLocalizeOutside.setEnabled(chkUseLD.getSelection());
	}
	
	public void setOptions (boolean useLD,
		boolean localizeOutside)
	{
		chkUseLD.setSelection(useLD);
		chkLocalizeOutside.setSelection(localizeOutside);
	}
	
	public boolean getUseLD () {
		return chkUseLD.getSelection();
	}
	
	public boolean getLocalizeOutside () {
		return chkLocalizeOutside.getSelection();
	}
}
