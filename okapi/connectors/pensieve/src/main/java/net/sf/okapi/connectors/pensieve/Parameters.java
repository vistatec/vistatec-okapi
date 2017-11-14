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

package net.sf.okapi.connectors.pensieve;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.FolderInputPart;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String USESERVER = "useServer";
	private static final String HOST = "host";
	private static final String DBDIRECTORY = "dbDirectory";

	public Parameters () {
		super();
	}
	
	public Parameters (String initialData) {
		super(initialData);
	}
	
	public boolean getUseServer () {
		return getBoolean(USESERVER);
	}

	public void setUseServer (boolean useServer) {
		setBoolean(USESERVER, useServer);
	}

	public String getHost () {
		return getString(HOST);
	}

	public void setHost (String host) {
		setString(HOST, host);
	}

	public String getDbDirectory () {
		return getString(DBDIRECTORY);
	}

	public void setDbDirectory(String dbDirectory) {
		setString(DBDIRECTORY, dbDirectory);
	}
	
	@Override
	public void reset () {
		super.reset();
		setDbDirectory("");
		setHost("http://localhost:8080/");
		setUseServer(false);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(USESERVER, "Use a server (instead of a local TM)", null);
		desc.add(HOST, "Server URL", "URL of the server to use (e.g. http://localhost:8080/");
		desc.add(DBDIRECTORY, "TM Directory", "Directory of the TM database");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Pensieve TM Connector Settings", true, false);
		CheckboxPart cbp = desc.addCheckboxPart(paramsDesc.get(Parameters.USESERVER));
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(Parameters.HOST));
		tip.setMasterPart(cbp, true);
		FolderInputPart fip = desc.addFolderInputPart(paramsDesc.get(Parameters.DBDIRECTORY), "TM Directory");
		fip.setMasterPart(cbp, false);
		return desc;
	}

}
