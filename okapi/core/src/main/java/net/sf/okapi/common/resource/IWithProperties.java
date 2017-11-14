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

public interface IWithProperties {

	/**
	 * Gets the resource-level property for a given name.
	 * @param name Name of the property to retrieve.
	 * @return The property or null if it does not exist.
	 */
	public Property getProperty (String name);

	/**
	 * Gets the names of all the resource-level properties for this resource.
	 * @return All the names of the resource-level properties for this resource.
	 */
	public Set<String> getPropertyNames ();

	/**
	 * Indicates if a resource-level property exists for a given name.
	 * @param name The name of the resource-level property to query.
	 * @return True if a resource-level property exists, false otherwise.
	 */
	public boolean hasProperty (String name);

	/**
	 * Removes a resource-level property of a given name. If the property does not exists
	 * nothing happens.
	 * @param name The name of the property to remove.
	 */
	public void removeProperty (String name);

	/**
	 * Sets a resource-level property. If a property already exists it is overwritten.
	 * @param property The new property to set.
	 * @return The property that has been set.
	 */
	public Property setProperty (Property property);

}
