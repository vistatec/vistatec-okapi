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

package net.sf.okapi.common;

/**
 * Common way to embed in a dialog box an editor to modify the 
 * parameters of a component. The parameters are implemented
 * through the {@link net.sf.okapi.common.IParameters} interface. 
 */
public interface IEmbeddableParametersEditor {

	/**
	 * Validates and save the current parameters for this editor.
	 * @return the string storage of the saved parameter, the same 
	 * string as the one returned by IParameters.toString() 
	 */
	public String validateAndSaveParameters ();

}
