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

package net.sf.okapi.lib.extra;

import net.sf.okapi.common.IParameters;

/**
 * 
 * 
 * @version 0.1 13.07.2009
 */

public interface IConfigurable {

	/**
	 * Sets new parameters for this component.
	 * @param params The new parameters to use.
	 */
	public void setParameters (IParameters params);

	/**
	 * Gets the current parameters for this component.
	 * @return The current parameters for this component.
	 */
	public IParameters getParameters ();

}
