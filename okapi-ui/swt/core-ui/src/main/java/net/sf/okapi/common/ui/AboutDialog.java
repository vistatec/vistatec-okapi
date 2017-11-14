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

package net.sf.okapi.common.ui;

import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Default implementation of an About dialog box.
 */
public class AboutDialog {

	private Shell shell;

	/**
	 * Creates a new AboutDialog object. The icon displayed in this dialog box
	 * is provided by the parent shell. It should be either a unique image 
	 * (Shell.getImage()), or the second image of a list of (Shell.getImages()[1]).
	 * @param parent The parent shell (also carry the icon to display).
	 * @param caption Caption text.
	 * @param description Text for the application description line.
	 * @param version Text for the application version line.
	 */
	public AboutDialog (Shell parent,
		String caption,
		String description,
		String version)
	{
		// Take the opportunity to do some clean up if possible
		Runtime rt = Runtime.getRuntime();
		rt.runFinalization();
		rt.gc();

		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText(caption);
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());
			
		Composite cmpTmp = new Composite(shell, SWT.BORDER);
		cmpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layTmp = new GridLayout(2, false);
		cmpTmp.setLayout(layTmp);

		// Application icon
		Label appIcon = new Label(cmpTmp, SWT.NONE);
		GridData gdTmp = new GridData();
		gdTmp.verticalSpan = 2;
		appIcon.setLayoutData(gdTmp);
		Image[] list = parent.getImages();
		// Gets the single icon
		if (( list == null ) || ( list.length < 2 )) {
			appIcon.setImage(parent.getImage());
		}
		else { // Or the second one if there are more than one.
			appIcon.setImage(list[1]);
		}

		Label label = new Label(cmpTmp, SWT.NONE);
		label.setText(description==null ? "TBD" : description); //$NON-NLS-1$
		gdTmp = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL
			| GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_VERTICAL);
		label.setLayoutData(gdTmp);
		
		label = new Label(cmpTmp, SWT.NONE);
		label.setText(String.format(Res.getString("AboutDialog.versionLabel"),
			version==null ? "TBD" : version)); //$NON-NLS-1$
		gdTmp = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL
			| GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_VERTICAL);
		label.setLayoutData(gdTmp);

		// Info
		
		cmpTmp = new Composite(shell, SWT.BORDER);
		cmpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		layTmp = new GridLayout(2, false);
		cmpTmp.setLayout(layTmp);
		
		label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("AboutDialog.jvmVersion")); //$NON-NLS-1$
		label = new Label(cmpTmp, SWT.NONE);
		label.setText(System.getProperty("java.version")); //$NON-NLS-1$
		label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("AboutDialog.platform")); //$NON-NLS-1$
		label = new Label(cmpTmp, SWT.NONE);
		label.setText(String.format("%s, %s, %s", //$NON-NLS-1$ 
			System.getProperty("os.name"), //$NON-NLS-1$ 
			System.getProperty("os.arch"), //$NON-NLS-1$
			System.getProperty("os.version"))); //$NON-NLS-1$
		label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("AboutDialog.memoryLabel")); //$NON-NLS-1$
		label = new Label(cmpTmp, SWT.NONE);
		NumberFormat nf = NumberFormat.getInstance();
		label.setText(String.format(Res.getString("AboutDialog.memory"), //$NON-NLS-1$
			nf.format(rt.freeMemory()/1024),
			nf.format(rt.totalMemory()/1024)));
		
		//--- Dialog-level buttons

		SelectionAdapter CloseActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			};
		};
		ClosePanel pnlActions = new ClosePanel(shell, SWT.NONE, CloseActions, false);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shell.setDefaultButton(pnlActions.btClose);
		
		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 350 ) startSize.x = 350;
		shell.setSize(startSize);
		Dialogs.centerWindow(shell, parent);
	}

	public void showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
	}

}
