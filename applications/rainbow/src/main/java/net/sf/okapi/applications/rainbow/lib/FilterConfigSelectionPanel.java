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

import net.sf.okapi.common.BaseContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.FilterInfo;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.common.ui.filters.FilterConfigurationEditor;
import net.sf.okapi.common.ui.filters.FilterConfigurationsDialog;
import net.sf.okapi.common.ui.filters.IFilterConfigurationInfoEditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

/**
 * Implements a common UI to select a filter configuration.
 */
public class FilterConfigSelectionPanel extends Composite {

	private FilterConfigurationMapper mapper;
	private Combo cbFilters;
	private Text edDescription;
	private List lbConfigs;
	private Button btEdit;
	private Button btCreate;
	private Button btDelete;
	private Button btMore;
	private BaseContext context;
	private java.util.List<FilterInfo> filters;
	private IFilter cachedFilter;
	private IHelp help;
	
	public FilterConfigSelectionPanel (Composite p_Parent,
		IHelp helpParam,
		int p_nFlags,
		FilterConfigurationMapper mapper,
		String projectDir)
	{
		super(p_Parent, SWT.NONE);
		context = new BaseContext();
		help = helpParam;
		context.setObject("help", helpParam); //$NON-NLS-1$
		context.setString("projDir", projectDir); //$NON-NLS-1$
		context.setObject("shell", getShell()); //$NON-NLS-1$
		this.mapper = mapper;
		createContent();
	}
	
	private void createContent () {
		GridLayout layTmp = new GridLayout(4, false);
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		setLayout(layTmp);

		cbFilters = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		gdTmp.widthHint = 340;
		cbFilters.setLayoutData(gdTmp);
		cbFilters.setVisibleItemCount(15);
		cbFilters.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e1) {
				fillConfigurations(0, null);
			}
			public void widgetDefaultSelected(SelectionEvent e2) {}
		});

		lbConfigs = new List(this, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 4;
		gdTmp.heightHint = 60;
		lbConfigs.setLayoutData(gdTmp);
		lbConfigs.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				updateConfigurationInfo();
            }
		});
		lbConfigs.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				editParameters();
			}
			public void mouseDown(MouseEvent e) {}
			public void mouseUp(MouseEvent e) {}
		});
		

		edDescription = new Text(this, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		gdTmp.heightHint = 60;
		edDescription.setLayoutData(gdTmp);
		edDescription.setEditable(false);
		
		int nWidth = 80;
		
		btEdit = new Button(this, SWT.PUSH);
		btEdit.setText(Res.getString("FilterConfigSelectionPanel.edit")); //$NON-NLS-1$
		btEdit.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				editParameters();
			}
		});

		btCreate = new Button(this, SWT.PUSH);
		btCreate.setText(Res.getString("FilterConfigSelectionPanel.create")); //$NON-NLS-1$
		btCreate.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				createConfiguration();
			}
		});

		btDelete = new Button(this, SWT.PUSH);
		btDelete.setText(Res.getString("FilterConfigSelectionPanel.delete")); //$NON-NLS-1$
		btDelete.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				deleteConfiguration();
			}
		});

		btMore = new Button(this, SWT.PUSH);
		btMore.setText(Res.getString("FilterConfigSelectionPanel.more")); //$NON-NLS-1$
		btMore.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				editAllConfigurations();
			}
		});

		nWidth = UIUtil.getMinimumWidth(nWidth, btEdit, Res.getString("FilterConfigSelectionPanel.view")); //$NON-NLS-1$
		UIUtil.setSameWidth(nWidth, btEdit, btCreate, btDelete, btMore);
	}
	
	public String getConfigurationId () {
		int n = lbConfigs.getSelectionIndex();
		if ( n < 0 ) return ""; // No configuration //$NON-NLS-1$
		else return lbConfigs.getItem(n);
	}

	public void setConfigurationId (String configId) {
		// Fill the list of available filters
		// Rely on order (index+1) because we cannot attach object to the items
		cbFilters.removeAll();
		cbFilters.add(Res.getString("FilterConfigSelectionPanel.noFilter")); //$NON-NLS-1$

		filters = mapper.getFiltersInfo();
		for ( FilterInfo item : filters ) {
			cbFilters.add(item.toString());
		}
		
		// Set the current filter
		FilterConfiguration config = mapper.getConfiguration(configId);
		if ( config == null ) {
			// Warn no configuration was found (if we were expecting one)
			if (( configId != null ) && ( configId.length()!=0 )) {
				Dialogs.showError(getShell(),
					String.format(Res.getString("FilterConfigSelectionPanel.configNotFound"), configId), null); //$NON-NLS-1$
			}
		}
		setConfiguration(config);
	}

	private void setConfiguration (FilterConfiguration config) {
		int n = -1;
		if ( config != null ) {
			for ( int i=0; i<filters.size(); i++ ) {
				if ( filters.get(i).className.equals(config.filterClass) ) {
					n = i; // Found it 
					break;
				}
			}
			if ( n == -1 ) {
				// Warn that the configuration or filter was not found
				Dialogs.showError(getShell(), String.format(
					Res.getString("FilterConfigSelectionPanel.configOrFilterNotFound"), //$NON-NLS-1$
					config.configId), null);
			}
		}
		
		cbFilters.select((n>-1) ? n+1 : 0); // n+1 to correct for <None> at 0
		fillConfigurations(0, (config==null) ? null : config.configId);
	}
	
	private void editAllConfigurations () {
		try {
			int n = lbConfigs.getSelectionIndex();
			String configId = null;
			if ( n > -1 ) {
				configId = lbConfigs.getItem(n);
			}
			String oldConfigId = configId;
			FilterConfigurationsDialog dlg = new FilterConfigurationsDialog(getShell(), true, mapper, help); 
			configId = dlg.showDialog(configId);
			if ( configId == null ) { // Close without selection
				configId = oldConfigId;
			}
			// Update the list of configuration with the new or old selection
			setConfigurationId(configId);
		}
		catch ( Throwable e ) {
			Dialogs.showError(getShell(), e.getMessage(), null);
		}
	}
	
	private void updateConfigurationInfo () {
		int n = lbConfigs.getSelectionIndex();
		String configId = null;
		if ( n > -1 ) configId = lbConfigs.getItem(n);
		if (( configId == null ) || ( configId.length() == 0 )) {
			edDescription.setText(""); //$NON-NLS-1$
			btEdit.setEnabled(false);
			btCreate.setEnabled(false);
			btDelete.setEnabled(false);
		}
		else {
			FilterConfiguration config = mapper.getConfiguration(configId);
			edDescription.setText(config.name + "\n" + config.description); //$NON-NLS-1$
			if ( config.custom ) btEdit.setText(Res.getString("FilterConfigSelectionPanel.edit")); //$NON-NLS-1$
			else btEdit.setText(Res.getString("FilterConfigSelectionPanel.view")); //$NON-NLS-1$
			btEdit.setEnabled(true);
			btCreate.setEnabled(true);
			btDelete.setEnabled(config.custom);
		}
	}

	private void fillConfigurations (int index,
		String selectedConfigId)
	{
		// Set default index if we don't find the selected configuration
		if ( selectedConfigId != null ) index = 0;
		
		lbConfigs.removeAll();
		// We should always have at least one configuration,
		// otherwise there would be no filter
		int n = cbFilters.getSelectionIndex();
		if ( n < 1 ) {
			updateConfigurationInfo();
			return; // First is <None>
		}
		n--; // Real index in filters list
		java.util.List<FilterConfiguration> list = mapper.getFilterConfigurations(filters.get(n).className);

		// Fill the list, and detect selected configuration if needed
		int i = 0;
		for ( FilterConfiguration item : list ) {
			lbConfigs.add(item.configId);
			if ( selectedConfigId != null ) {
				if ( selectedConfigId.equals(item.configId) ) index = i;
				i++;
			}
		}

		lbConfigs.setSelection(index);
		updateConfigurationInfo();
	}
	
	private void editParameters () {
		try {
			String configId = getConfigurationId();
			if ( configId == null ) return;
			FilterConfiguration config = mapper.getConfiguration(configId);
			if ( config == null ) return;
			cachedFilter = mapper.createFilter(config.configId, cachedFilter);
			IFilterConfigurationEditor editor = new FilterConfigurationEditor();
			editor.editConfiguration(configId, mapper, cachedFilter, getShell(), context);
		}
		catch ( Throwable e ) {
			Dialogs.showError(getShell(), e.getMessage(), null);
		}
	}

	private void createConfiguration () {
		try {
			String baseConfigId = getConfigurationId();
			if ( baseConfigId == null ) return;
			FilterConfiguration baseConfig = mapper.getConfiguration(baseConfigId);
			if ( baseConfig == null ) return;
			FilterConfiguration newConfig = mapper.createCustomConfiguration(baseConfig);
			if ( newConfig == null ) {
				MessageBox dlg = new MessageBox(getShell(), SWT.ICON_INFORMATION);
				dlg.setMessage("This filter has no parameters.");
				dlg.setText("Information");
				dlg.open();
				return;
			}
			
			// Edit the configuration info
			if ( !editConfigurationInfo(newConfig) ) return; // Canceled
			
			// Set the new parameters with the base ones
			IParameters newParams = mapper.getParameters(baseConfig);
			// Save the new configuration
			mapper.saveCustomParameters(newConfig, newParams);
			
			// Add the new configuration
			mapper.addConfiguration(newConfig);
			// Update the list and the selection
			// Refresh the list of parameters
			fillConfigurations(0, newConfig.configId);

			// And continue by editing the parameters for that configuration
			editParameters();
		}
		catch ( Throwable e ) {
			Dialogs.showError(getShell(), "Error while creating or editing the new configuration. " + e.getMessage(), null);
		}
	}

	private void deleteConfiguration () {
		try {
			String configId = getConfigurationId();
			if ( configId == null ) return;
			FilterConfiguration config = mapper.getConfiguration(configId);
			if ( !config.custom ) return; // Cannot delete pre-defined configurations

			// Ask confirmation
			MessageBox dlg = new MessageBox(getParent().getShell(),
				SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			dlg.setMessage(String.format(
				Res.getString("FilterConfigSelectionPanel.confirmDeletion"), configId)); //$NON-NLS-1$
			dlg.setText("Rainbow"); //$NON-NLS-1$
			switch  ( dlg.open() ) {
			case SWT.NO:
			case SWT.CANCEL:
				return;
			}
			// Else: delete the configuration
			mapper.deleteCustomParameters(config);
			mapper.removeConfiguration(configId);
			// Refresh the list of parameters
			fillConfigurations(0, null);
		}
		catch ( Throwable e ) {
			Dialogs.showError(getShell(), e.getMessage(), null);
		}
	}

	private boolean editConfigurationInfo (FilterConfiguration config) {
		// Create the configuration info editor
		IFilterConfigurationInfoEditor editor = new FilterConfigInfoEditor(); //new FilterConfigurationInfoEditor();
		// Create and call the dialog
		editor.create(getShell());
		return editor.showDialog(config, mapper);
	}

}
