/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.common.filters;

import net.sf.okapi.common.IContext;

/**
 * Interface to edit the parameters of a filter configuration.
 * <p>There are different ways the parameters for a filter configuration can be edited
 * depending on what editors are available. This interface provides a single call access
 * to those different types of editors.
 */
public interface IFilterConfigurationEditor {

	/**
	 * Edits a given filter configuration.
	 * @param configId the filter configuration identifier.
	 * @param fcMapper the {@link IFilterConfigurationMapper} to use.
	 * @param cachedFilter an optional cached filter (can be null). If not null
	 * the call will try to re-use it to load the parameters and the appropriate editor.
	 * @param parent optional parent object used to place the dialog box (can be null).
	 * @param context optional context from the caller (help, etc.)
	 * The type of the object can be different depending on the implementations. 
	 * @return true if the configuration was done, false if it could not be done or was canceled.
	 * @throws RuntimeException if the configuration cannot be found, or if the parameters 
	 * cannot be loaded or another error occurs.
	 */
	public boolean editConfiguration (String configId,
		IFilterConfigurationMapper fcMapper,
		IFilter cachedFilter,
		Object parent,
		IContext context);

	/**
	 * Edits a given filter configuration.
	 * @param configId the filter configuration identifier.
	 * @param fcMapper the {@link IFilterConfigurationMapper} to use.
	 * @return true if the configuration was done, false if it could not be done or was canceled.
	 * @throws RuntimeException if the configuration cannot be found, or if the parameters 
	 * cannot be loaded or another error occurs.
	 */
	public boolean editConfiguration (String configId,
		IFilterConfigurationMapper fcMapper);

}
