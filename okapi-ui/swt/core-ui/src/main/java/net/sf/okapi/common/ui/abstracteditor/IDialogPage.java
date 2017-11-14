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

package net.sf.okapi.common.ui.abstracteditor;

import org.eclipse.swt.widgets.Widget;

/**
 * 
 * 
 * @version 0.1, 27.06.2009
 */

public interface IDialogPage {

	/**
	 * Called by the parent dialog when the page is attached. Implementation is expected to configure GUI controls after the value(s) of data.
	 * @param data provided by the editor.
	 * @return true if GUI controls were configured successfully 
	 */
	public boolean load(Object data);
	
	/**
	 * Provides a means for synchronization of the page's GUI controls. All integrity checks are performed in this method.
	 * Event handlers of the controls affecting other controls should call this method.
	 * @param speaker the widget firing the event. 
	 */
	public void interop(Widget speaker);
	
	/**
	 * Called by the parent dialog when closed with OK. Implementation is expected to set the given data according to
	 * the state of corresponding GUI controls.   
	 * @param data
	 * @return
	 */
	public boolean save(Object data);
	
	/**
	 * Checks if the page can be closed. Called by the editor when OK or Cancel were pressed.
	 * @param isOK the editor is being closed with OK 
	 * @return true if the page can be closed.
	 */
	public boolean canClose(boolean isOK);
}
