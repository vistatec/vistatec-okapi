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

package net.sf.okapi.connectors.globalsight;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String SERVERURL = "serverURL";
	private static final String TMPROFILE = "tmProfile";
	
	public Parameters () {
		super();
	}
	
	public Parameters (String initialData) {
		super(initialData);
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

	public String getServerURL () {
		return getString(SERVERURL);
	}

	public void setServerURL (String serverURL) {
		setString(SERVERURL, serverURL);
	}

	public String getTmProfile () {
		return getString(TMPROFILE);
	}

	public void setTmProfile (String tmProfile) {
		setString(TMPROFILE, tmProfile);
	}

	@Override
	public void reset () {
		super.reset();
		setUsername("");
		setPassword("");
		setServerURL("http://HOST:PORT/globalsight/services/AmbassadorWebService?wsdl");
		setTmProfile("default");
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(Parameters.SERVERURL,
			"Server URL", "The full URL of the TM server (e.g. http://xyz:8080/globalsight/services/AmbassadorWebService?wsdl");
		desc.add(Parameters.USERNAME,
			"User name", "The login name to use");
		desc.add(Parameters.PASSWORD,
			"Password", "The password for the given user name");
		desc.add(Parameters.TMPROFILE,
			"TM profile", "The name of the TM profile to use");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("GlobalSight TM Connector Settings");
		desc.addTextInputPart(paramsDesc.get(Parameters.SERVERURL));
		desc.addTextInputPart(paramsDesc.get(Parameters.USERNAME));
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(Parameters.PASSWORD));
		tip.setPassword(true);
		desc.addTextInputPart(paramsDesc.get(Parameters.TMPROFILE));
		return desc;
	}

}
