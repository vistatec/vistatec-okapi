/*===========================================================================
  Copyright (C) 2010-2012 by the Okapi Framework contributors
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

import net.sf.okapi.common.BaseContext;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationEditor;
import net.sf.okapi.common.filters.IFilterConfigurationListEditor;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.ui.InputDialog;
import net.sf.okapi.common.ui.genericeditor.GenericEditor;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Implements {@link IFilterConfigurationEditor} for the SWT-based UI.
 */
public class FilterConfigurationEditor implements IFilterConfigurationEditor, IFilterConfigurationListEditor  {

	@Override
	public boolean editConfiguration (String configId,
		IFilterConfigurationMapper fcMapper)
	{
		return editConfiguration(configId, fcMapper, null, null, new BaseContext());
	}
	
	@Override
	public boolean editConfiguration (String configId,
		IFilterConfigurationMapper fcMapper,
		IFilter cachedFilter,
		Object parent,
		IContext context)
	{
		FilterConfiguration config = fcMapper.getConfiguration(configId);
		if ( config == null ) {
			throw new OkapiException(String.format(
				"Cannot find the configuration for '%s'.", configId));
		}
		IParameters params = fcMapper.getParameters(config, cachedFilter);
		if ( params == null ) { // No parameter for this filter
			Shell shell = null;
			if (( parent != null ) && ( parent instanceof Shell )) {
				shell = (Shell)parent;
			}
			MessageBox dlg = new MessageBox(shell, SWT.ICON_INFORMATION);
			dlg.setMessage("This filter has no parameters to edit.");
			dlg.setText("Information");
			dlg.open();
			return false;
		}

		IParametersEditor editor = fcMapper.createConfigurationEditor(configId, cachedFilter);
		if ( editor != null ) {
			if ( !editor.edit(params, !config.custom, context) ) {
				return false; // Cancel
			}
		}
		else {
			// Try to see if we can edit with the generic editor
			IEditorDescriptionProvider descProv = fcMapper.getDescriptionProvider(params.getClass().getName());
			if ( descProv != null ) {
				// Edit the data
				GenericEditor genEditor = new GenericEditor();
				if ( !genEditor.edit(params, descProv, !config.custom, context) ) {
					return false; // Cancel
				}
				// The params object gets updated if edit not canceled.
			}
			else { // Else: fall back to the plain text editor
				Shell shell = null;
				if (( parent != null ) && ( parent instanceof Shell )) {
					shell = (Shell)parent;
				}
				InputDialog dlg  = new InputDialog(shell,
					String.format("Filter Parameters (%s)", config.configId), "Parameters:",
					params.toString(), null, 0, 300, 800);
				dlg.setReadOnly(!config.custom); // Pre-defined configurations should be read-only
				dlg.changeFontSize(+2);
				String data = dlg.showDialog();
				if ( data == null ) return false; // Cancel
				if ( !config.custom ) return true; // Don't save pre-defined parameters
				data = data.replace("\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				params.fromString(data.replace("\r", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		// If not canceled and if custom configuration: save the changes
		if ( config.custom ) {
			// Save the configuration filefcMapper
			fcMapper.saveCustomParameters(config, params);
		}
		return true;
	}

	@Override
	public void editConfigurations (IFilterConfigurationMapper fcMapper) {
		FilterConfigurationsDialog dlg = new FilterConfigurationsDialog(null, false,
			(FilterConfigurationMapper)fcMapper, null);
		dlg.showDialog(null);
	}

	public String editConfigurations (IFilterConfigurationMapper fcMapper,
		String configId)
	{
		FilterConfigurationsDialog dlg = new FilterConfigurationsDialog(null, true,
			(FilterConfigurationMapper)fcMapper, null);
		return dlg.showDialog(configId);
	}

}
