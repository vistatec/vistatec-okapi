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

package net.sf.okapi.filters.plaintext.spliced;

import net.sf.okapi.common.ParametersString;
import net.sf.okapi.common.Util;

/**
 * Spliced Lines Filter parameters
 * @version 0.1, 09.06.2009
 */

public class Parameters extends net.sf.okapi.filters.plaintext.base.Parameters {
	
	/**
	 * Char at the end of a line, signifying the line is continued on the next line (normally "\" or "_"). Can be a custom string too.<p>
	 * Default: \ (backslash)
	 */
	public String splicer; 
	
	/**
	 * If in-line codes should be created for the dropped splicers and linebreaks of spliced lines
	 * Default: true (create in-line codes)
	 */
	public boolean createPlaceholders;
	
//----------------------------------------------------------------------------------------------------------------------------	
	
	@Override
	protected void parameters_load(ParametersString buffer) {

		super.parameters_load(buffer);
		
		String st = buffer.getString("splicer", "\\");
		if (Util.isEmpty(st))
			splicer = "";
		else
			splicer = st.trim(); 
				
		createPlaceholders = buffer.getBoolean("createPlaceholders", false);
	}

	@Override
	protected void parameters_reset() {

		super.parameters_reset();
		
		splicer = "\\";
		createPlaceholders = true;
	}

	@Override
	protected void parameters_save(ParametersString buffer) {

		super.parameters_save(buffer);
		
		buffer.setString("splicer", splicer);
		buffer.setBoolean("createPlaceholders", createPlaceholders);
	}

}
