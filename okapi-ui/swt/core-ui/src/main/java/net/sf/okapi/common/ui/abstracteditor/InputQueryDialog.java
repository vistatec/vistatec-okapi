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

package net.sf.okapi.common.ui.abstracteditor;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.ui.OKCancelPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * 
 * @version 0.1, 23.06.2009
 */

public class InputQueryDialog extends AbstractBaseDialog  {
	
	//	private OKCancelPanel pnlActions;
//	private String caption;
	private String prompt;	
//	private Object data = null;
//	private IHelp help;
//	private Class<?> pageClass;
//	private IInputQueryPage page; 

	public InputQueryDialog() {
		super(false);
	}
	
	public InputQueryDialog(boolean sizeable) {
		super(sizeable);		
	}
	
	public boolean run(Shell parent, Class<? extends Composite> pageClass, String caption, String prompt, Object initialData, IHelp help) {
		
//		if (!IDialogPage.class.isAssignableFrom(pageClass)) return false;
//		if (!Composite.class.isAssignableFrom(pageClass)) return false;
		
		this.prompt = prompt;
//		this.data = initialData;

		return super.run(parent, pageClass, caption, initialData, help);
		
//		try {
////			this.pageClass = pageClass;
////			this.caption = caption;
////			this.prompt = prompt;
////			this.data = initialData;
////			this.help = help;
//			
//			create(parent);			
//			if (!result) return  false;
//			
//			showDialog();			
//			if (!result) return  false;
//		}
//		finally {
//			
//			if (shell != null) shell.dispose();
//		}
//		
//		return result;				
	}
	
	
	
//	private void showDialog () {
//		
//		if (!result) return;
//		
//		result = false; // To react to OK only
//		shell.open();
//		while ( !shell.isDisposed() ) {
//			
//			try {
//				if ( !shell.getDisplay().readAndDispatch() )
//					shell.getDisplay().sleep();
//			}
//			catch ( Exception E ) {
//				Dialogs.showError(shell, E.getLocalizedMessage(), null);
//			}
//		}				
//	}
	

//	@Override
//	protected void setActionButtonsPanel(Shell shell, SelectionAdapter listener) {
//		
//		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, listener, true);
//		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
//		pnlActions.setLayoutData(gdTmp);
//		shell.setDefaultButton(pnlActions.btOK);
//		
//	}
	
	@Override
	protected void setActionButtonsPanel(Shell shell, SelectionAdapter listener, boolean showHelp) {
		
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, listener, showHelp);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		pnlActions.setLayoutData(gdTmp);
		shell.setDefaultButton(pnlActions.btOK);
		
	}
	
	@Override
	protected void done() {
		
	}



	@Override
	protected void init() {
		
		if (page instanceof IInputQueryPage)
			((IInputQueryPage) page).setPrompt(prompt);
	}
	
}

