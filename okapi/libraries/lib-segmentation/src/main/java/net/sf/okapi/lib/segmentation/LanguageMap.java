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
============================================================================*/

package net.sf.okapi.lib.segmentation;

/**
 * Stores the data for an SRX &lt;languagemap&gt; map element
 */
public class LanguageMap {

	/**
	 * The pattern of this language map.
	 */
	protected String pattern;
	
	/**
	 * The name of this language map.
	 */
	protected String ruleName;

	/**
	 * Creates an empty LanguageMap object. 
	 */
	public LanguageMap () {
	}
	
	/**
	 * Creates a LanguageMap object with a given pattern and a given name.
	 * @param pattern the pattern for the new language map.
	 * @param ruleName the name of the new language map.
	 */
	public LanguageMap (String pattern,
		String ruleName)
	{
		this.pattern = pattern;
		this.ruleName = ruleName;
	}
	
	/**
	 * Gets the pattern associated to this language map.
	 * @return the pattern associated to this language map.
	 */
	public String getPattern () {
		return pattern;
	}
	
	/**
	 * Gets the name of this language map.
	 * @return the name of this languag emap.
	 */
	public String getRuleName () {
		return ruleName;
	}

}
