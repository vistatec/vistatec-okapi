/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.tda;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;
import net.sf.okapi.common.uidescription.TextInputPart;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String SERVER = "server";
	private static final String APPKEY = "appKey";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String INDUSTRY = "industry";
	private static final String CONTENTTYPE = "contentType";
	
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
		setServer("http://www.tausdata.org/api");
		setAppKey("");
		setUsername("");
		setPassword("");
		setIndustry(0);
		setContentType(0);
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

	public String getServer () {
		return getString(SERVER);
	}

	public void setServer (String server) {
		setString(SERVER, server);
	}

	public String getAppKey () {
		return getString(APPKEY);
	}

	public void setAppKey (String appKey) {
		setString(APPKEY, appKey);
	}

	public int getIndustry () {
		return getInteger(INDUSTRY);
	}

	public void setIndustry (int industry) {
		setInteger(INDUSTRY, industry);
	}

	public int getContentType () {
		return getInteger(CONTENTTYPE);
	}

	public void setContentType (int contentType) {
		setInteger(CONTENTTYPE, contentType);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(USERNAME, "User name", "The login name to use");
		desc.add(PASSWORD, "Password", "The password for the given user name");
		desc.add(SERVER, "Server URL", "URL of the server");
		desc.add(APPKEY, "Application key", "Application key");
		desc.add(INDUSTRY, "Industry", "Keyword for the industry");
		desc.add(CONTENTTYPE, "Content type", "Keyword for the type of content");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("TDA Search Connector Settings");
		desc.addTextInputPart(paramsDesc.get(USERNAME));
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(PASSWORD));
		tip.setPassword(true);

		desc.addTextInputPart(paramsDesc.get(SERVER));

		tip = desc.addTextInputPart(paramsDesc.get(APPKEY));
		tip.setPassword(true);

		// List of industries
		//TODO: Get list dynamically from the API
		String[] values1 = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
			"10", "11", "12", "13", "14", "15", "16", "17", "18"};
		String[] labels1 = {
			"Any",
			"Automotive Manufacturing",
			"Consumer Electronics", 
			"Computer Software", 
			"Computer Hardware", 
			"Industrial Manufacturing", 
			"Telecommunications", 
			"Professional and Business Services", 
			"Stores and Retail Distribution", 
			"Industrial Electronics", 
			"Legal Services", 
			"Energy, Water and Utilities", 
			"Financials", 
			"Medical Equipment and Supplies", 
			"Healthcare", 
			"Pharmaceuticals and Biotechnology", 
			"Chemicals", 
			"Undefined Sector", 
			"Leisure, Tourism, and Arts" 
		};
		ListSelectionPart lsp = desc.addListSelectionPart(paramsDesc.get(INDUSTRY), values1);
		lsp.setChoicesLabels(labels1);

		// List of content types
		//TODO: Get list dynamically from the API
		String[] values2 = {"0", "1", "2", "4", "5", "6", "7", "8", "9", "10", "12"};
		String[] labels2 = {
			"Any",
			"Instructions for Use", 
			"Sales and Marketing Material", 
			"Policies, Process and Procedures", 
			"Software Strings and Documentation", 
			"Undefined Content Type", 
			"News Announcements, Reports and Research", 
			"Patents", 
			"Standards, Statutes and Regulations", 
			"Financial Documentation", 
			"Support Content" 
		};
		lsp = desc.addListSelectionPart(paramsDesc.get(CONTENTTYPE), values2);
		lsp.setChoicesLabels(labels2);

		return desc;
	}

}
