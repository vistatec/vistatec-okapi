/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

import net.sf.okapi.common.exceptions.OkapiUserCanceledException;

/**
 * An interface for prompting the user for confirmation before continuing.
 */
public interface IUserPrompt {

	/**
	 * Initialize the prompt.
	 * @param uiParent The UI parent (used in GUI mode only; can be null otherwise)
	 * @param title The title of the dialog (used in GUI mode only; can be null)
	 */
	public void initialize (Object uiParent, String title);

	/**
	 * Prompt the user to decide between "Yes", "No", and "Cancel".
	 * @param message The text message to display
	 * @return true if yes, false if no
	 * @throws OkapiUserCanceledException If user cancels
	 */
	public boolean promptYesNoCancel (String message)
			throws OkapiUserCanceledException;

	/**
	 * Prompt the user to decide between "OK" and "Cancel".
	 * @param message The text message to display
	 * @return true if OK
	 * @throws OkapiUserCanceledException If user cancels
	 */
	public boolean promptOKCancel (String message)
			throws OkapiUserCanceledException;
}
