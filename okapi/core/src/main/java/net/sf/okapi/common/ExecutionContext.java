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

/**
 * A class to encapsulate information about execution details such as
 * the name of the application, the current UI parent, etc.
 */
public class ExecutionContext extends BaseContext {

	private static String IS_NO_PROMPT = "isNoPrompt";
	private static String UI_PARENT = "uiParent";
	private static String APPLICATION_NAME = "applicationName";

	public boolean getIsGui() {
		return getUiParent() != null;
	}

	public void setIsNoPrompt(boolean bool) {
		setBoolean(IS_NO_PROMPT, bool);
	}

	public boolean getIsNoPrompt() {
		return getBoolean(IS_NO_PROMPT);
	}

	public void setUiParent(Object uiParent) {
		setObject(UI_PARENT, uiParent);
	}

	public Object getUiParent() {
		return getObject(UI_PARENT);
	}

	public void setApplicationName(String name) {
		setString(APPLICATION_NAME, name);
	}

	public String getApplicationName() {
		return getString(APPLICATION_NAME);
	}
}
