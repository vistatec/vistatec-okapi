/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.filters.plaintext.regex;

import java.util.regex.Pattern;

import net.sf.okapi.common.ParametersString;

/**
 * Parameters of the Regex Plain Text Filter
 * 
 * @version 0.1, 09.06.2009  
 */

public class Parameters extends net.sf.okapi.filters.plaintext.base.Parameters {
			
	public static final String	DEF_RULE = "(^(?=.+))(.*?)$";
	public static final int		DEF_GROUP = 2;
	public static final String	DEF_SAMPLE = "\nThis is the first sentence. And this is the second one.\n" +	
		"Second paragraph. Each one ends at the line-break.\n\nThird paragraph.\nAnd the last paragraph may have no line-break.";
	public static final int		DEF_OPTIONS = Pattern.MULTILINE;
	
	/**
	 * Java regex rule used to extract lines of text.<p>Default: "(^(?=.+))(.*?)$". 
	 */
	public String rule;
	
	/**
	 * Java regex capturing group denoting text to be extracted.<p>Default: 2.
	 */
	public int sourceGroup;
	
	/**
	 * Java regex options.<p>Default: Pattern.MULTILINE.
	 */
	public int regexOptions;
	
	/**
	 * Sample text for the rule.
	 */
	public String sample;
							
//----------------------------------------------------------------------------------------------------------------------------	
	
	@Override
	protected void parameters_load(ParametersString buffer) {

		super.parameters_load(buffer);
		
		rule =  buffer.getString("rule", DEF_RULE);
		sourceGroup = buffer.getInteger("sourceGroup", DEF_GROUP);		
		regexOptions = buffer.getInteger("regexOptions", DEF_OPTIONS);
		sample =  buffer.getString("sample", "");
	}

	@Override
	protected void parameters_reset() {
		
		super.parameters_reset();
		
		rule = DEF_RULE;
		sourceGroup = DEF_GROUP;		
		regexOptions = DEF_OPTIONS;
		sample = DEF_SAMPLE;
	}

	@Override
	protected void parameters_save(ParametersString buffer) {

		super.parameters_save(buffer);
		
		buffer.setString("rule", rule);
		buffer.setInteger("sourceGroup", sourceGroup);		
		buffer.setInteger("regexOptions", regexOptions);
		buffer.setString("sample", sample);
	}
	
}
