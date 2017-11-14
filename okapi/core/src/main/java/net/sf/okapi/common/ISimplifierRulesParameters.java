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

package net.sf.okapi.common;

import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

/**
 * Implemented by {@link IFilter} {@link IParameters} to provide code simplifier rule support.
 * @author jimh
 */
public interface ISimplifierRulesParameters {	
	public static final String SIMPLIFIERRULES = "simplifierRules";	
	public static final String SIMPLIFIERRULES_SHORT_DESC = "Simplifier Rules as defined in the Okapi Code Simplifier Rule Format";	
	public static final String SIMPLIFIERRULES_DISPLAY_NAME = "Simplifier Rules";	
	
	/**
	 * Get the code simplifier rules as defined by {@link SimplifierRules} 
	 * (JavaCC file: core/simplifierrules/SimplifierRules.jj).
	 * @return rules as a string.
	 */
	public String getSimplifierRules();
	
	/**
	 * Set the simplifier rules.
	 * @param rules new simplifier rules
	 */
	public void setSimplifierRules(String rules);
	
	/**
	 * Validate the current code simplifier rules.
	 * @throws ParseException if the rule cannot be parsed
	 */
	public void validateSimplifierRules() throws ParseException;
}
