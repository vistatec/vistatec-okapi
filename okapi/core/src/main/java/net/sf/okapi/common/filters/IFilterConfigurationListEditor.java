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

/**
 * Interface to edit a list of filter configurations at once.
 */
public interface IFilterConfigurationListEditor {

	/**
	 * Displays a list of all available configurations in a given {@link FilterConfigurationMapper} and allow to edit them.
	 * @param fcMapper the {@link IFilterConfigurationMapper} to use.
	 */
	public void editConfigurations (IFilterConfigurationMapper fcMapper);

	/**
	 * Displays a list of all available configurations in a given {@link FilterConfigurationMapper}, allow to edit them
	 * and to select one.
	 * @param fcMapper the {@link IFilterConfigurationMapper} to use.
	 * @param configId the configuration id to start with (can be null or empty).
	 * @return the configuration ID selected or null if none was selected. If the dialog
	 * is terminated with a Cancel or Close rather than a Select action, the return is null.
	 */
	public String editConfigurations (IFilterConfigurationMapper fcMapper, String configId);

}
