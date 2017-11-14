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

package net.sf.okapi.applications.rainbow.lib;

import net.sf.okapi.applications.rainbow.Project;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.InputDialog;
import net.sf.okapi.common.ui.filters.FilterConfigurationsDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Implements a panel for selecting the folder where to get the custom
 * filter configurations. 
 */
public class CustomConfigurationFolderPanel extends Composite {

	private Text edParamsFolder;
	private Button btGetParamsFolder;
	private Project project;
	private FilterConfigurationsDialog dialog;
	
	/**
	 * Creates a CustomConfigurationFolderPanel panel.
	 * @param parent the parent composite.
	 * @param style the style flags.
	 * @param project the current Rainbow project.
	 * @param dialog the containing FilterConfigMapperDialog caller, or null if
	 * the containing caller is not FilterConfigMapperDialog.
	 */
	public CustomConfigurationFolderPanel (Composite parent,
		int style,
		Project project,
		FilterConfigurationsDialog dialog)
	{
		super(parent, style);
		this.dialog = dialog;
		this.project = project;
		createContent();
	}
	
	private void createContent () {
		GridLayout layTmp = new GridLayout(1, false);
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		setLayout(layTmp);

		Group group = new Group(this, SWT.NONE);
		group.setLayout(new GridLayout(3, false));
		group.setText(Res.getString("CustomConfigurationFolderPanel.caption")); //$NON-NLS-1$
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NONE);
		label.setText(Res.getString("CustomConfigurationFolderPanel.folder")); //$NON-NLS-1$

		edParamsFolder = new Text(group, SWT.BORDER);
		edParamsFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edParamsFolder.setEditable(false);
		
		btGetParamsFolder = new Button(group, SWT.PUSH);
		btGetParamsFolder.setText("..."); //$NON-NLS-1$
		btGetParamsFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				editParamsFolder();
            }
		});
		
		edParamsFolder.setText(project.getParametersFolder(true));
	}

	private void editParamsFolder () {
		try {
			InputDialog dlg = new InputDialog(getShell(),
				Res.getString("CustomConfigurationFolderPanel.customConfigCaption"), //$NON-NLS-1$
				Res.getString("CustomConfigurationFolderPanel.customConfigCaptionLabel"), //$NON-NLS-1$
				project.getParametersFolder(true, false),
				null, 1, -1, -1);
			dlg.setAllowEmptyValue(true);
			String newDir = dlg.showDialog();
			if ( newDir == null ) return; // Canceled
			if ( newDir.length() < 2 ) newDir = ""; // Use project's //$NON-NLS-1$
			// Set the project
			project.setCustomParametersFolder(newDir);
			project.setUseCustomParametersFolder(newDir.length()!=0);
			// Update the display
			edParamsFolder.setText(project.getParametersFolder(true));
			// Reload the custom configurations
			if ( dialog != null ) {
				dialog.updateCustomConfigurations();
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(getShell(), e.getMessage(), null);
		}
	}

}
