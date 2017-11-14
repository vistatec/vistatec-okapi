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

package net.sf.okapi.connectors.apertium;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.SpinInputPart;
import net.sf.okapi.common.uidescription.TextInputPart;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String SERVER = "server";
	private static final String APIKEY = "apiKey";
	private static final String TIMEOUT = "timeout";
	
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
		setServer("http://api.apertium.org/json/translate");
		setApiKey("");
		setTimeout(0);
	}

	public String getServer () {
		return getString(SERVER);
	}

	public void setServer (String server) {
		setString(SERVER, server);
	}

	public String getApiKey () {
		return getString(APIKEY);
	}

	public void setApiKey (String apiKey) {
		setString(APIKEY, apiKey);
	}
	
	public int getTimeout () {
		return getInteger(TIMEOUT);
	}
	
	public void setTimeout (int timeout) {
		setInteger(TIMEOUT, timeout);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(SERVER, "Server URL:", "Full URL of the server");
		desc.add(APIKEY, "API Key:", "Recommended key (See http://api.apertium.org/register.jsp)");
		desc.add(TIMEOUT, "Timeout", "Timeout in second after which to give up (use 0 for system timeout)");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Apertium MT Connector Settings");
		
		desc.addTextInputPart(paramsDesc.get(SERVER));
		
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(APIKEY));
		tip.setPassword(true);
		tip.setAllowEmpty(true); // API key is optional
		
		SpinInputPart sip = desc.addSpinInputPart(paramsDesc.get(TIMEOUT));
		sip.setRange(0, 60);
		return desc;
	}

}
