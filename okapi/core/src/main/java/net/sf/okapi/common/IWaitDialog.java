/*===========================================================================
  Copyright (C) 2011-2013 by the Okapi Framework contributors
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
 * Provides a generic way to open a dialog box and wait for a simple
 * input from the user.
 */
public interface IWaitDialog {

	/**
	 * Opens a dialog or prompt and waits for the user input.
	 * @param message the message to display.
	 * @param okLabel the label to display for the OK button.
	 * @return 0 if the user cancels, a positive value otherwise.
	 */
	public int waitForUserInput (String message,
		String okLabel);

}
