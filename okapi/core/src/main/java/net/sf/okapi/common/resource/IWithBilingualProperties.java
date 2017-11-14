/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource;

import java.util.Set;

import net.sf.okapi.common.LocaleId;

public interface IWithBilingualProperties {

	/**
	 * Gets the source property for a given name.
	 * @param name The name of the source property to retrieve.
	 * @return The property or null if it does not exist.
	 */
	public Property getSourceProperty (String name);

	/**
	 * Gets the names of all the source properties for this resource.
	 * @return All the names of the source properties for this resource.
	 */
	public Set<String> getSourcePropertyNames ();

	/**
	 * Gets all the target locales for this resource.
	 * @return all the target locales for this resource.
	 */
	public Set<LocaleId> getTargetLocales ();

	/**
	 * Gets the target property for a given name and target locale.
	 * @param locId the locale of the property to retrieve.
	 * @param name The name of the property to retrieve. This name is case-sensitive.
	 * @return The property or null if it does not exist.
	 */
	public Property getTargetProperty (LocaleId locId,
		String name);

	/**
	 * Gets the names of all the properties for a given target locale in this resource.
	 * @param locId the target locale to query.
	 * @return all the names of the target properties for the given locale in this resource.
	 */
	public Set<String> getTargetPropertyNames (LocaleId locId);

	/**
	 * Indicates if a source property exists for a given name.
	 * @param name The name of the source property to query.
	 * @return True if a source property exists, false otherwise.
	 */
	public boolean hasSourceProperty (String name);

	/**
	 * Indicates if a property exists for a given name and target locale.
	 * @param locId the target locale to query.
	 * @param name the name of the property to query.
	 * @return true if a property exists, false otherwise.
	 */
	public boolean hasTargetProperty (LocaleId locId,
		String name);

	/**
	 * Removes a source property of a given name. If the property does not exists
	 * nothing happens.
	 * @param name The name of the property to remove.
	 */
	public void removeSourceProperty (String name);

	/**
	 * Removes a target property of a given name. If the property does not exists
	 * nothing happens.
	 * @param locId The target locale for which this property should be set.
	 * @param name The name of the property to remove.
	 */
	public void removeTargetProperty (LocaleId locId,
		String name);

	/**
	 * Sets a source property. If a property already exists it is overwritten. 
	 * @param property The new property to set.
	 * @return The property that has been set.
	 */
	public Property setSourceProperty (Property property);

	/**
	 * Sets a target property. If a property already exists it is overwritten.
	 * @param locId The target locale for which this property should be set.
	 * @param property The new property to set. This name is case-sensitive.
	 * @return The property that has been set.
	 */
	public Property setTargetProperty (LocaleId locId,
		Property property);

}
