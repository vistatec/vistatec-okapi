/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.mmt;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

/**
 * Parameters for the {@link MMTConnector}.
 */
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String URL = "url";
	private static final String CONTEXT = "context";

	public Parameters() {
	}

	public String getUrl () {
		return getString(URL);
	}

	public void setUrl(String url) {
		setString(URL, url);
	}

	public String getContext () {
		return getString(CONTEXT);
	}

	public void setContext(String context) {
		setString(CONTEXT, context);
	}

	@Override
	public void reset () {
		super.reset();
        setUrl("");
		setContext("");
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(URL,
			"URL for ModernMT Engine",
			"The ModernMT Engine's API URL - format http://<servername>:<port>");
		desc.add(CONTEXT,
			"Context for the ModernMT Engine calls",
			"Optional context description for the ModernMT engine calls");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("ModernMT Engine Connector Settings", true, false);
		desc.addTextInputPart(paramsDesc.get(URL));
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(CONTEXT));
		tip.setAllowEmpty(true);
		return desc;
	}

}
