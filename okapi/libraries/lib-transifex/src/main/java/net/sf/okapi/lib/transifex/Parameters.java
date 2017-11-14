/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.lib.transifex;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String SERVER = "server";
	private static final String PROJECTID = "projectId";
	private static final String USER = "user";
	private static final String PASSWORD = "password";
	private static final String PROJECTURL = "projectUrl";
	private static final String OPENSOURCE = "openSource";
	
	public Parameters () {
		super();
	}
	
	public Parameters (String initialData) {
		super(initialData);
	}

	@Override
	public void reset () {
		super.reset();
		// Default
		setServer("http://www.transifex.net/api/2/");
		setProjectId("");
		setUser("");
		setPassword("");
		setProjectUrl("");
		setOpenSource(true);
	}

	public String getServer () {
		return getString(SERVER);
	}

	public String getServerWithoutAPI () {
		String host = getString(SERVER);
		if ( host.endsWith("api/2/") ) {
			return host.substring(0, host.length()-6);
		}
		return host;
	}

	public void setServer (String server) {
		if ( server.endsWith("\\") ) {
			server = server.substring(0, server.length()-1) + "/";
		}
		else if ( !server.endsWith("/") ) {
			server += "/";
		}
		setString(SERVER, server);
	}

	public boolean getOpenSource () {
		return getBoolean(OPENSOURCE);
	}

	public void setOpenSource (boolean openSource) {
		setBoolean(OPENSOURCE, openSource);
	}

	public String getProjectUrl () {
		return getString(PROJECTURL);
	}

	public void setProjectUrl (String projectUrl) {
		setString(PROJECTURL, projectUrl);
	}

	public String getProjectId () {
		return getString(PROJECTID);
	}

	public void setProjectId (String projectId) {
		setString(PROJECTID, projectId);
	}

	public String getUser () {
		return getString(USER);
	}

	public void setUser (String user) {
		setString(USER, user);
	}

	public String getPassword () {
		return getString(PASSWORD);
	}

	public void setPassword (String password) {
		setString(PASSWORD, password);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(SERVER, "Server URL", "Full URL of the server (e.g. http://www.transifex.net/)");
		desc.add(USER, "User name", "User name to login");
		desc.add(PASSWORD, "Password", "Password to login");
		desc.add(PROJECTID, "Project ID", "Identifier of the project (case sensitive)");
		desc.add(OPENSOURCE, "Is an open-source project", "True for open-source projects");
		desc.add(PROJECTURL, "Project Repository URL", "URL of your project repository");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Transifex Settings");
		desc.addTextInputPart(paramsDesc.get(SERVER));
		desc.addTextInputPart(paramsDesc.get(USER));
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(PASSWORD));
		tip.setPassword(true);
		desc.addTextInputPart(paramsDesc.get(PROJECTID));
		desc.addTextInputPart(paramsDesc.get(PROJECTURL));
		desc.addCheckboxPart(paramsDesc.get(OPENSOURCE));
		return desc;
	}

}
