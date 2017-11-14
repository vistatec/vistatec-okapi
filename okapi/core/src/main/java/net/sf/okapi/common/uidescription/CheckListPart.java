/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.common.uidescription;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import net.sf.okapi.common.ParameterDescriptor;

/**
 * UI part descriptor for a list of boolean options.
 */
public class CheckListPart extends AbstractPart {
	
	private Map<String, ParameterDescriptor> entries;
	private int heightHint;
	
	/**
	 * Creates a new ListSelectionPart object with a given parameter descriptor.
	 * @param label the text to set at the top of the list (or null).
	 * @param heightHint the suggested height of the list.
	 */
	public CheckListPart (String label,
		int heightHint)
	{
		super(new ParameterDescriptor(UUID.randomUUID().toString(), null, label, null));
		entries = new LinkedHashMap<String, ParameterDescriptor>();
		this.heightHint = heightHint;
	}

	@Override
	protected void checkType () {
		// Nothing to check
	}
	
	/**
	 * Gets the suggested height for the list.
	 * @return the suggested height for the list.
	 */
	public int getHeightHint () {
		return heightHint;
	}

	/**
	 * Gets the map of the the entries.
	 * @return the map of the entries.
	 */
	public Map<String, ParameterDescriptor> getEntries () {
		return entries;
	}

	/**
	 * Clears the map of the entries.
	 */
	public void clearEntries () {
		entries.clear();
	}
	
	/**
	 * Adds one entry in the map.
	 * @param desc the description of the parameter.
	 */
	public void addEntry (ParameterDescriptor desc) {
		entries.put(desc.getName(), desc);
	}
	
}
