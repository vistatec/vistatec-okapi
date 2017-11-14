/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tokenization.ui.common;

import net.sf.okapi.common.ui.abstracteditor.IDialogPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class NameDescriptionTab extends Composite implements IDialogPage {
	private Text text;
	private Text text_1;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public NameDescriptionTab(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		{
			Group grpGeneral = new Group(this, SWT.NONE);
			grpGeneral.setText("General");
			grpGeneral.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			grpGeneral.setLayout(new GridLayout(1, false));
			{
				Label lblName = new Label(grpGeneral, SWT.NONE);
				lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				lblName.setText("Name:");
			}
			{
				text = new Text(grpGeneral, SWT.BORDER);
				text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				Label lblDescription = new Label(grpGeneral, SWT.NONE);
				lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
				lblDescription.setText("Description:");
			}
			{
				text_1 = new Text(grpGeneral, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
				GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
				gridData.widthHint = 400;
				gridData.heightHint = 100;
				text_1.setLayoutData(gridData);
			}
		}
		{
			Group grpSummary = new Group(this, SWT.NONE);
			grpSummary.setText("Summary");
			grpSummary.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		}

	}

	public boolean canClose(boolean isOK) {
		
		return true;
	}

	public void interop(Widget speaker) {
		
		
	}

	public boolean load(Object data) {
		
		return true;
	}

	public boolean save(Object data) {

		return true;
	}
}
