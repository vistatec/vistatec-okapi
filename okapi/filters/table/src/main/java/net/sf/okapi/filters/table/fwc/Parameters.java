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

package net.sf.okapi.filters.table.fwc;

import net.sf.okapi.common.ParametersString;

/**
 * Fixed-Width Columns Filter parameters
 * 
 * @version 0.1, 09.06.2009 
 */

public class Parameters extends net.sf.okapi.filters.table.base.Parameters {

	/** 
	 * Specifies start positions of fixed-width table columns. The positions are x-coordinates, like the position of a char in a string.
	 * The difference is that columnStartPositions are 1-based.  
	 * Can be represented by one of the following string types:
	 *<li>"1" - position (1-based) where the column starts
	 *<li>"1,2,5" - comma-delimited list (1-based) of starting positions of the table columns<p>
	 * Default: Empty
	 */
	public String columnStartPositions;
	
	/** 
	 * Specifies end positions of fixed-width table columns. The positions are x-coordinates, like the position of a char in a string.
	 * The difference is that columnEndPositions are 1-based.  
	 * Can be represented by one of the following string types:
	 *<li>"1" - position (1-based) where the column starts
	 *<li>"1,2,5" - comma-delimited list (1-based) of starting positions of the table columns<p>
	 * Default: Empty
	 */
	public String columnEndPositions;
			
//----------------------------------------------------------------------------------------------------------------------------	
	
	@Override
	protected void parameters_load(ParametersString buffer) {

		super.parameters_load(buffer);
		
		columnStartPositions = buffer.getString("columnStartPositions", "").trim(); // null is impossible, default is ""
		columnEndPositions = buffer.getString("columnEndPositions", "").trim(); // null is impossible, default is ""
	}

	@Override
	protected void parameters_reset() {
		
		super.parameters_reset();
		
		columnStartPositions = "";
		columnEndPositions = "";
		trimMode = TRIM_ALL;  // To get rid of white-space padding in-between columns
	}

	@Override
	protected void parameters_save(ParametersString buffer) {

		super.parameters_save(buffer);
		
		buffer.setString("columnStartPositions", columnStartPositions);
		buffer.setString("columnEndPositions", columnEndPositions);
	}


}
 