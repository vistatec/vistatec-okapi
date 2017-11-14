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

package net.sf.okapi.common;

/**
 * Common way to call in an editor to modify the parameters of 
 * a component. The parameters are implemented
 * through the {@link net.sf.okapi.common.IParameters} interface. 
 */
public interface IParametersEditor {

	/**
	 * Edits the values for the given parameters. If the edit succeeds
	 * (returns true), the parameters have been updated in p_Parameters to
	 * reflect the changes. 
	 * @param paramsObject the parameters to edit.
	 * @param readOnly indicates if the editor is used just to view the parameters.
	 * If true, the editor must return false.
	 * @param context an implementation of the {@link IContext} interface that
	 * holds caller-specific information.
	 * @return true if the edit was successful, false if the user canceled or if 
	 * an error occurred, or if the read-only mode is set. 
	 */
	public boolean edit (IParameters paramsObject,
		boolean readOnly,
		IContext context);
	
	/**
	 * Creates an instance of the parameters object the editor can edit (with
	 * the default values). This allows the user to create new parameters object 
	 * from the interface, without knowing what exact type of object is created. 
	 * @return an instance of the parameters object for the editor.
	 */
	public IParameters createParameters ();
}
