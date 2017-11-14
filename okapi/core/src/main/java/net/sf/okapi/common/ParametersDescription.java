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

package net.sf.okapi.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Groups in a single objects all the parameter descriptors associated with
 * a given object such as a step or a filter.
 */
public class ParametersDescription {

	private Object originalObject;
	private LinkedHashMap<String, ParameterDescriptor> descriptors;
	
	/**
	 * Creates a new ParametersDescription object for a given parent object.
	 * @param originalObject the object described. 
	 */
	public ParametersDescription (Object originalObject) {
		descriptors = new LinkedHashMap<String, ParameterDescriptor>();
		this.originalObject = originalObject;
	}
	
	/**
	 * Gets a map of all the parameter descriptors for this description. 
	 * @return a map of all parameter descriptors.
	 */
	public Map<String, ParameterDescriptor> getDescriptors () {
		return descriptors;
	}
	
	/**
	 * Gets the descriptor for a given parameter.
	 * @param name the name of the parameter to lookup.
	 * @return the descriptor for the given parameter.
	 */
	public ParameterDescriptor get (String name) {
		return descriptors.get(name);
	}
	
	/**
	 * Adds a descriptor to this description.
	 * @param name the name of the parameter.
	 * @param displayName the localizable display name for this parameter.
	 * @param shortDescription a short localizable description for this parameter. 
	 * @return the parameter descriptor created by the call.
	 */
	public ParameterDescriptor add (String name,
		String displayName,
		String shortDescription)
	{
		ParameterDescriptor desc = new ParameterDescriptor(name, originalObject,
			displayName, shortDescription);
		descriptors.put(name, desc);
		return desc;
	}

}
