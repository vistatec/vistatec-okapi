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

package net.sf.okapi.connectors.promt;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	protected static final String HOST = "host";
	protected static final String USERNAME = "username";
	protected static final String PASSWORD = "password";
	
	public String getHost () {
		return getString(HOST);
	}

	public void setHost (String host) {
		setString(HOST, host);
	}

	public String getUsername () {
		return getString(USERNAME);
	}

	public void setUsername (String username) {
		setString(USERNAME, username);
	}

	public String getPassword () {
		return getString(PASSWORD);
	}

	public void setPassword (String password) {
		setString(PASSWORD, password);
	}

	public Parameters () {
		super();
	}
	
	public Parameters (String initialData) {
		fromString(initialData);
	}
	
	public void reset () {
		super.reset();
		setHost("ptsdemo.promt.ru/");
		setUsername("");
		setPassword("");
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(HOST, "Host server", "The root URL of the host server (e.g. http://ptsdemo.promt.ru/");
		desc.add(USERNAME, "User name (optional)", "The login name to use");
		desc.add(PASSWORD, "Password (if needed)", "The password for the given user name");
		return desc;
	}

	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("ProMT Connector Settings");
		desc.addTextInputPart(paramsDesc.get(HOST));
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(USERNAME));
		tip.setAllowEmpty(true); // Username is optional
		tip = desc.addTextInputPart(paramsDesc.get(PASSWORD));
		tip.setPassword(true);
		tip.setAllowEmpty(true); // Password is optional
		return desc;
	}

}
