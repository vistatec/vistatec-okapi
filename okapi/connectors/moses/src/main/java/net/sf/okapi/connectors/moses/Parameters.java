/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.moses;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	protected static String SERVERURL = "serverURL";
	
	public Parameters() {
		super();
	}

	public Parameters(String initialData) {
		fromString(initialData);
	}

	public String getServerURL() {
		return getString(SERVERURL);
	}

	public void setServerURL(String serverURL) {
		setString(SERVERURL, serverURL);
	}

	public void reset() {
		super.reset();
		// serverURL = "http://HOST:PORT/RPC2";
		setServerURL("http://localhost:8080/RPC2");
	}
}
