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
============================================================================*/

package net.sf.okapi.common.ui.plugins;

public class PluginInfo {

	private String name;
	private String provider;
	private String description;
	private String helpURL;

	/**
	 * Creates a new PluginInfo object.
	 * @param name the name of the plugin.
	 * @param provider the provider name (can be null).
	 * @param description a description of what the plugin does (can be null).
	 * @param helpURL a URL to an help page (can be null).
	 */
	public PluginInfo (String name,
		String provider,
		String description,
		String helpURL)
	{
		this.name = name;
		this.provider = provider;
		this.description = description;
		this.helpURL = helpURL;
	}
	
	public String getName () {
		return name;
	}

	public String getProvider () {
		return provider;
	}

	public String getDescription () {
		return description;
	}

	public String getHelpURL () {
		return helpURL;
	}

}
