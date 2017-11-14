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

package net.sf.okapi.common.ui.filters;

import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.InputDialog;

import org.eclipse.swt.widgets.Shell;

public class FilterConfigurationIdentifierEditor implements IFilterConfigurationInfoEditor {

	private Shell parent;

	public void create (Shell parent) {
		this.parent = parent;
	}

	public boolean showDialog (FilterConfiguration config,
		IFilterConfigurationMapper mapper)
	{
		int n = config.configId.indexOf(FilterConfigurationMapper.CONFIGFILE_SEPARATOR);
		String prefix = config.configId.substring(0, n);
		String part = config.configId.substring(n+1);

		while ( true ) {
			InputDialog dlg = new InputDialog(parent, Res.getString("FilterConfigurationIdentifierEditor.caption"), //$NON-NLS-1$
				String.format(Res.getString("FilterConfigurationIdentifierEditor.enterConfigId"), //$NON-NLS-1$
					prefix+FilterConfigurationMapper.CONFIGFILE_SEPARATOR),
				part, null, 0, -1, 500);
			String newPart = dlg.showDialog();
			if ( newPart == null ) return false;
		
			// Else: Update the configuration
			config.configId = config.configId.replace(part, newPart);
			config.name = config.name.replace(part, newPart);
			config.parametersLocation = config.parametersLocation.replace(part, newPart);
			
			// check if it exists already
			if ( mapper.getConfiguration(config.configId) != null ) {
				Dialogs.showError(parent, String.format(Res.getString("FilterConfigurationIdentifierEditor.configIdExitsAlready"), //$NON-NLS-1$
					config.configId), null);
			}
			else break; // Done
		}
		
		return true;
	}

}
