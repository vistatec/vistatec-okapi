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

package net.sf.okapi.lib.ui.verification;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

class StatusBar extends Composite {

	private CLabel      counterLabel;
	private CLabel      infoLabel;
	
	StatusBar (Composite parent,
		int flags)
	{
		super(parent, flags);
		createContent();
	}
	
	private void createContent () {
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 3;
		setLayoutData(gdTmp);
		
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 2;
		setLayout(layout);
		
		counterLabel = new CLabel(this, SWT.BORDER | SWT.CENTER);
		gdTmp = new GridData();
		gdTmp.widthHint = 180;
		counterLabel.setLayoutData(gdTmp);

		infoLabel = new CLabel(this, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		infoLabel.setLayoutData(gdTmp);
	}

	void setInfo (String p_sText) {
		infoLabel.setText((p_sText == null) ? "" : p_sText); //$NON-NLS-1$
	}
	
	void clearInfo () {
		infoLabel.setText(""); //$NON-NLS-1$
	}
	
	void setCounter (int current,
		int outOf,
		int total)
	{
		if ( current < 0 ) {
			counterLabel.setText(String.format("0 / 0 / T=%d", total)); //$NON-NLS-1$
		}
		else {
			counterLabel.setText(String.format("%d / %d / T=%d", current+1, outOf, total)); //$NON-NLS-1$
		}
	}

}
