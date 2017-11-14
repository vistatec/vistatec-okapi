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
import net.sf.okapi.common.filters.IFilterConfigurationMapper;

import org.eclipse.swt.widgets.Shell;

/**
 * Dialog box to edit the information for a given configuration.
 * This interface is used by the {@link FilterConfigurationsPanel} class to
 * have an application-specific way to define the information for a given
 * configuration, for example when creating a new one.
 */
public interface IFilterConfigurationInfoEditor {

	/**
	 * Creates the dialog box.
	 * @param parent the parent shell of this dialog.
	 */
	public void create (Shell parent);
	
	/**
	 * Calls the dialog box.
	 * @param config the configuration to edit.
	 * @param mapper the filter configuration mapper where this
	 * configuration will be set. Having access to this mapper allows
	 * for example to check for identifier duplication.
	 * @return true if the edit was successful, false if an error
	 * occurred or if the user canceled the operation.
	 */
	public boolean showDialog (FilterConfiguration config,
		IFilterConfigurationMapper mapper);

}
