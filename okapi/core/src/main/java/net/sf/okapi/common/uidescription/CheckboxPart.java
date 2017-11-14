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

package net.sf.okapi.common.uidescription;

import net.sf.okapi.common.ParameterDescriptor;

/**
 * UI part descriptor for a check box. This UI part supports the following types:
 * Boolean, Integer (0: false, non-0: true), and String ("0": false, not-"0": true).  
 */
public class CheckboxPart extends AbstractPart {

	/**
	 * Creates a new CheckboxPart object with a given parameter descriptor.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 */
	public CheckboxPart (ParameterDescriptor paramDescriptor) {
		super(paramDescriptor);
	}

	@Override
	protected void checkType () {
		// Check type support
		if ( getType().equals(boolean.class) ) return;
		if ( getType().equals(int.class) ) return;
		if ( getType().equals(String.class) ) return;
		// Otherwise: call the base method.
		super.checkType();
	}

}
