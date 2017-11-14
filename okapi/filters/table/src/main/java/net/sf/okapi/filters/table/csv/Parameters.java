/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package net.sf.okapi.filters.table.csv;

import net.sf.okapi.common.ParametersString;

/**
 * CSV Filter parameters
 * 
 * @version 0.1, 09.06.2009  
 */

public class Parameters extends net.sf.okapi.filters.table.base.Parameters {

	final public static int ESCAPING_MODE_DUPLICATION = 1;
	final public static int ESCAPING_MODE_BACKSLASH = 2;
	
	/**
	 * Character separating fields in a row. <p>
	 * Default: , (comma)
	 */
	public String fieldDelimiter;
	
	/** 
	 * Character before and after field value to allow field delimiters inside the field. 
	 * For instance, this field will not be broken into parts: "Field, containing comma, \", "" and \n".
	 * The qualifiers are not included in translation units.<p>  
	 * Default: " (quotation mark)
	 */ 
	public String textQualifier;
	
	/**
	 * True if qualifiers should be dropped, and shouldn't go into the text units
	 */
	public boolean removeQualifiers = true;
	
	/**
	 * The way qualifiers in the original text are escaped.
	 */
	public int escapingMode = ESCAPING_MODE_DUPLICATION;

	public boolean addQualifiers = false;
	
	@Override
	protected void parameters_load(ParametersString buffer) {

		super.parameters_load(buffer);
		
		fieldDelimiter = buffer.getString("fieldDelimiter", ""); 
		textQualifier = buffer.getString("textQualifier", "");
		removeQualifiers = buffer.getBoolean("removeQualifiers", true);
		escapingMode = buffer.getInteger("escapingMode", ESCAPING_MODE_DUPLICATION);
		addQualifiers = buffer.getBoolean("addQualifiers", false);
	}

	@Override
	protected void parameters_reset() {

		super.parameters_reset();
		
		fieldDelimiter = ",";
		textQualifier = "\"";
		removeQualifiers = true;
		escapingMode = ESCAPING_MODE_DUPLICATION;
		addQualifiers = false;
	}

	@Override
	protected void parameters_save(ParametersString buffer) {

		super.parameters_save(buffer);
		
		buffer.setString("fieldDelimiter", fieldDelimiter);
		buffer.setString("textQualifier", textQualifier);
		buffer.setBoolean("removeQualifiers", removeQualifiers);
		buffer.setInteger("escapingMode", escapingMode);
		buffer.setBoolean("addQualifiers", addQualifiers);
	}
	
}
