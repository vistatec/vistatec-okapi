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

package net.sf.okapi.filters.plaintext.paragraphs;

import net.sf.okapi.common.ParametersString;

/**
 * Paragraph Plain Text Filter parameters 
 * 
 * @version 0.1, 09.06.2009
 */

public class Parameters extends net.sf.okapi.filters.plaintext.base.Parameters {
	
	/**
	 * This parameter specifies whether input plain text contains paragraphs (groups of lines separated by one or more empty lines).<p>
	 * When false, each line will be extracted separately. When true, adjacent lines will be extracted together as a paragraph.<p> 
	 * Default: false (extract single lines)
	 */
	public boolean extractParagraphs;
		
//----------------------------------------------------------------------------------------------------------------------------	
	
	@Override
	protected void parameters_load(ParametersString buffer) {

		super.parameters_load(buffer);
		
		extractParagraphs = buffer.getBoolean("extractParagraphs", true);
	}

	@Override
	protected void parameters_reset() {

		super.parameters_reset();
		
		extractParagraphs = true;
	}

	@Override
	protected void parameters_save(ParametersString buffer) {

		super.parameters_save(buffer);
		
		buffer.setBoolean("extractParagraphs", extractParagraphs);
	}
	
}
