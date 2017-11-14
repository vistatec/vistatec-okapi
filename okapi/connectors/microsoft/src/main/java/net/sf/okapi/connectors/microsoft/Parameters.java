/*===========================================================================
  Copyright (C) 2010-2016 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.microsoft;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String AZUREKEY = "azureKey";
	private static final String CATEGORY = "category";
	
	public Parameters () {
		super();
	}

	public String getAzureKey() {
		return getString(AZUREKEY);
	}

	public void setAzureKey(String azureKey) {
		setString(AZUREKEY, azureKey);
	}

	public String getCategory () {
		return getString(CATEGORY);
	}

	public void setCategory (String category) {
		setString(CATEGORY, category == null ? "" : category);
	}

	@Override
	public void reset () {
		super.reset();
		setAzureKey("");
		setCategory("");
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(AZUREKEY,
			"Azure Key (See https://translatorbusiness.uservoice.com/knowledgebase/articles/1078534-microsoft-translator-on-azure#signup)",
			"Microsoft Azure subscription key");
		desc.add(CATEGORY,
			"Category (See http://hub.microsofttranslator.com", "A category code for an MT system trained by user data, if any");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Microsoft MT Connector Settings", true, false);
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(AZUREKEY));
		tip.setPassword(true);
		tip = desc.addTextInputPart(paramsDesc.get(CATEGORY));
		tip.setAllowEmpty(true);
		return desc;
	}

}
