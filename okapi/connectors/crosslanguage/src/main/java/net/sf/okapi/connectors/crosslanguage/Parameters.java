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

package net.sf.okapi.connectors.crosslanguage;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	protected static final String SERVERURL = "serverURL";
	protected static final String USER = "user";
	protected static final String APIKEY = "apiKey";
	protected static final String PASSWORD = "password";
	
	public Parameters () {
		super();
	}
	
	public Parameters (String initialData) {
		super(initialData);
	}
	
	public String getUser () {
		return getString(USER);
	}

	public void setUser (String user) {
		setString(USER, user);
	}

	public String getApiKey () {
		return getString(APIKEY);
	}

	public void setApiKey (String apiKey) {
		setString(APIKEY, apiKey);
	}

	public String getServerURL () {
		return getString(SERVERURL);
	}

	public void setServerURL (String serverURL) {
		setString(SERVERURL, serverURL);
	}

	public String getPassword () {
		return getString(PASSWORD);
	}

	public void setPassword (String password) {
		setString(PASSWORD, password);
	}

	public void reset () {
		super.reset();
		setUser("myUsername");
		setApiKey("myApiKey");
		setServerURL("http://gateway.crosslang.com:8080/services/clGateway?wsdl");
		setPassword("");
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(Parameters.SERVERURL,
			"Server URL", "The full URL of the server (e.g. http://gateway.crosslang.com:8080/services/clGateway?wsdl");
		desc.add(Parameters.USER,
			"User name", "The login name to use");
		desc.add(Parameters.APIKEY,
			"API key", "The API key for the given user, engine and language pair");
		desc.add(Parameters.PASSWORD,
			"Password", "The login passowrd");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("CrossLanguage MT Connector Settings");
		
		desc.addTextInputPart(paramsDesc.get(SERVERURL));
		
		desc.addTextInputPart(paramsDesc.get(USER));
		
		desc.addTextInputPart(paramsDesc.get(APIKEY));
		
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(PASSWORD));
		tip.setPassword(true);
		
		return desc;
	}

}
