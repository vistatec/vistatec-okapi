/*===========================================================================
  Copyright (C) 2008 by the Okapi Framework contributors
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
 * Common way to get access to the parameters of a given component.
 */
public interface IParametersProvider {

	/**
	 * Loads a parameters object from a given location. 
	 * @param location the string that encodes the source location. The value depends
	 * on each implementation. It can be a path, a filter setting string, etc.
	 * @return the loaded parameters object or null if an error occurred.
	 * @throws Exception if we encounter any problem loading the parameters.
	 */
	public IParameters load (String location)
		throws Exception;
	
	/**
	 * Gets the default parameters for a given provider.
	 * @param location the string that encodes the source location. The value depends
	 * on each implementation. It can be a path, a filter setting string, etc.
	 * @return the defaults parameters object or null if an error occurred.
	 * @throws Exception if we encounter any problem creating the parameters.
	 */
	public IParameters createParameters (String location)
		throws Exception;

	/**
	 * Saves a parameters object to a given location.
	 * @param location the string that encodes the target location. The value depends
	 * on each implementation. It can be a path, a filter setting string, etc.
	 * @param paramsObject the parameters object to save.
	 * @throws Exception if we encounter any problem saving the parameters.
	 */
	public void save (String location,
		IParameters paramsObject)
		throws Exception;
	
	/**
	 * Deletes a parameters object at a given location. 
	 * @param location the string that encodes the target location. The value depends
	 * on each implementation. It can be a path, a filter setting string, etc.
	 * @return true if the parameters object was delete, false if it was not.
	 */
	public boolean deleteParameters (String location);
	
	/**
	 * Split a given location into its components.
	 * @param location the string that encodes the location. The value depends
	 * on each implementation. It can be a path, a filter setting string, etc.
	 * @return an array of string corresponding to each component of the location.
	 * The values depend on each implementation.
	 */
	public String[] splitLocation (String location);
	
	/**
	 * Gets the list of available sets of parameters (for example, the list
	 * of all filter settings). 
	 * @return an array of string, each string being the string you
	 * would pass to load the give set of parameters. 
	 */
	public String[] getParametersList ();
}
