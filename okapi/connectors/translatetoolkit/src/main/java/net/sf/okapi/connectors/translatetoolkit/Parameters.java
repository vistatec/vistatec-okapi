/*===========================================================================
  Copyright (C) 2009-2014 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.translatetoolkit;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String HOST = "host"; // Obsolete
	private static final String PORT = "port"; // Obsolete
	private static final String SUPPORTCODES = "supportCodes";
	private static final String URL = "url";
	
	public Parameters () {
		super();
	}
	
	public Parameters (String initialData) {
		super(initialData);
	}
	
	@Override
	public void reset () {
		super.reset();
		setUrl("https://amagama-live.translatehouse.org/api/v1/");
		setSupportCodes(false);
		
		// Obsolete: Use get/setUrl() instead
		setHost("localhost");
		setPort(8080);
	}

	@Deprecated
	public String getHost () {
		return getString(HOST);
	}

	@Deprecated
	public void setHost (String host) {
		setString(HOST, host);
	}

	@Deprecated
	public int getPort () {
		return getInteger(PORT);
	}

	@Deprecated
	public void setPort (int port) {
		setInteger(PORT, port);
	}

	public String getUrl () {
		return getString(URL);
	}

	public void setUrl (String url) {
		setString(URL, url);
	}

	public boolean getSupportCodes () {
		return getBoolean(SUPPORTCODES);
	}

	public void setSupportCodes (boolean supportCodes) {
		setBoolean(SUPPORTCODES, supportCodes);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(URL, "URL", "The Base part of URL of the TM server");
		// Obsolete desc.add(HOST, "Host", "The host name of the TM server (e.g. localhost)");
		// Obsolete desc.add(PORT, "Port", "The port number of the TM server (e.g. 8080)");
		desc.add(SUPPORTCODES, "Inline codes are letter-coded (e.g. <x1/><g2></g2>)", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Translation Toolkit TM Connector Settings");
		desc.addTextInputPart(paramsDesc.get(URL));
		// Obsolete desc.addTextInputPart(paramsDesc.get(HOST));
		// Obsolete desc.addTextInputPart(paramsDesc.get(PORT));
		CheckboxPart cbp = desc.addCheckboxPart(paramsDesc.get(SUPPORTCODES));
		cbp.setVertical(true);
		return desc;
	}

}
