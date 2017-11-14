/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.lingo24;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

/**
 * Parameters for the {@link Lingo24Connector} connector.
 */
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String APIKEY = "userKey";
	
	public Parameters() {
	}
	
	public String getUserKey () {
		return getString(APIKEY);
	}

	public void setUserKey (String userKey) {
		setString(APIKEY, userKey);
	}

	@Override
	public void reset () {
		super.reset();
        setUserKey("");
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(APIKEY,
			"Lingo24 Premium MT API key",
			"The Lingo24 Premium MT API key to identify the application/user");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Lingo24 Premium MT Connector Settings", true, false);
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(APIKEY));
		tip.setPassword(true);
		return desc;
	}

}
