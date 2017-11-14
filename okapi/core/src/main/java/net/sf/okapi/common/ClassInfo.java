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

package net.sf.okapi.common;

/**
 * Stores a class name and its class-loader, for dynamic loading.
 */
public class ClassInfo {

	/**
	 * Full name of the class.
	 */
	public String name;
	/**
	 * Class loader for this class, or null if the default
	 * class loader should be used.
	 */
	public ClassLoader loader;

	/**
	 * Creates a new ClassInfo object for a given class name and loader.
	 * @param name the full name of the class.
	 * @param loader the class loader for this class, or null to use the
	 * default class loader.
	 */
	public ClassInfo (String name,
		ClassLoader loader)
	{
		this.name = name;
		this.loader = loader;
	}
	
	/**
	 * Convenience method to create a new ClassInfo object for a given
	 * class name. This is the same as calling {@link #ClassInfo(String, ClassLoader)}
	 * with the class loader set to null.
	 * @param name the full name of the class.
	 */
	public ClassInfo (String name) {
		this.name = name;
	}

}
