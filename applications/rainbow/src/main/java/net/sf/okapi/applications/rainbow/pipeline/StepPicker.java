/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.pipeline;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class StepPicker {

	private Shell shell;
	private List lbUtilities;
	private Text edDescription;
	private String result;
	private ArrayList<StepInfo> availableSteps;
	private IHelp help;
	
	public StepPicker (Shell parent,
		Map<String, StepInfo> steps,
		IHelp helpParam) 
	{
		result = null;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Add Step");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());
		
		help = helpParam;
		
		Label label = new Label(shell, SWT.None);
		label.setText("Available steps:");
		
		lbUtilities = new List(shell, SWT.BORDER | SWT.V_SCROLL);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 350;
		lbUtilities.setLayoutData(gdTmp);
		lbUtilities.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				if ( !saveData() ) return;
				shell.close();
			}
			public void mouseDown(MouseEvent e) {}
			public void mouseUp(MouseEvent e) {}
		});

		StepInfo step;
		availableSteps = new ArrayList<StepInfo>(); 
		for ( String id : steps.keySet() ) {
			step = steps.get(id);
			lbUtilities.add(step.name);
			lbUtilities.setData(step.name, step.stepClass);
			availableSteps.add(step);
		}
		lbUtilities.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateStepDisplay();
			}
		});
		
		edDescription = new Text(shell, SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		gdTmp.heightHint = 45;
		gdTmp.horizontalSpan = 2;
		edDescription.setLayoutData(gdTmp);
		edDescription.setEditable(false);
		
		if ( lbUtilities.getItemCount() > 0 ) {
			lbUtilities.select(0);
			updateStepDisplay();
		}

		// Dialog-level buttons
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = null;
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					showStepHelp();
					return;
				}
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					if ( !saveData() ) return;
				}
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE,
			OKCancelActions, true);
		pnlActions.btHelp.setText("Step Help");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		//gdTmp.horizontalSpan = 2;
		pnlActions.setLayoutData(gdTmp);
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		Dialogs.centerWindow(shell, parent);
	}

	private void showStepHelp () {
		try {
			int n = lbUtilities.getSelectionIndex();
			if ( n == -1 ) return;
			StepInfo si = availableSteps.get(n);
			IPipelineStep step;
			if ( si.loader == null ) {
				step = (IPipelineStep)Class.forName(si.stepClass).newInstance();
			}
			else {
				step = (IPipelineStep)Class.forName(si.stepClass, true, si.loader).newInstance();
			}
			
			String stepHelp = step.getHelpLocation();
			if ( Util.isEmpty(stepHelp) ) return;
			if ( stepHelp.startsWith(".") ) {
				// Use old method: Local help
				String path = Util.getClassLocation(step.getClass());
				if ( Util.isEmpty(path) ) return; // No help available
				path += File.separator + stepHelp + File.separator;
				n = si.stepClass.lastIndexOf('.');
				Util.openURL(path+si.stepClass.substring(n+1).toLowerCase()+".html");
			}
			else if ( stepHelp.endsWith(".html") ) {
				// Third-party step
				Util.openURL(stepHelp);
			}
			else {
				// Go to OkapiWiki
				help.showWiki(stepHelp);
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}	
	}
	
	private void updateStepDisplay () {
		int n = lbUtilities.getSelectionIndex();
		if ( n < 0 ) {
			edDescription.setText("");
			return; 
		}
		StepInfo step = availableSteps.get(n);
		edDescription.setText(step.description);
	}
	
	public String showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	private boolean saveData () {
		int n = lbUtilities.getSelectionIndex();
		if ( n == -1 ) return false;
		result = (String)lbUtilities.getData(lbUtilities.getItem(n));
		return true;
	}

}
