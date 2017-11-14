/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

import net.sf.okapi.common.IUserPrompt;
import net.sf.okapi.common.exceptions.OkapiUserCanceledException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * A GUI implementation of {@link IUserPrompt}.
 */
public class UserPrompt implements IUserPrompt {

	private Shell shell;
	private String title;

	@Override
	public void initialize(Object uiParent, String title) {
		if (uiParent != null && uiParent instanceof Shell) {
			shell = (Shell)uiParent;
		} else {
			shell = new Shell();
		}
		this.title = title == null ? "Okapi" : title;
	}

	public boolean promptYesNoCancel(String message) throws OkapiUserCanceledException {
		
		MessageBox dlg = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);
		dlg.setMessage(message);
		dlg.setText(title);
		switch  ( dlg.open() ) {
		case SWT.YES:
			return true;
		case SWT.NO:
			return false;
		}
		
		throw new OkapiUserCanceledException("Operation was canceled by user.");
	}

	public boolean promptOKCancel(String message) throws OkapiUserCanceledException {
		
		MessageBox dlg = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
		dlg.setMessage(message); 
		dlg.setText(title);
		if  ( dlg.open() == SWT.OK ) {
			return true;
		}
		
		throw new OkapiUserCanceledException("Operation was canceled by user.");
	}
}
