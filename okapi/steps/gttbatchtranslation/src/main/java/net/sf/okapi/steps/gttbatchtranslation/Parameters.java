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

package net.sf.okapi.steps.gttbatchtranslation;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.PathInputPart;
import net.sf.okapi.common.uidescription.SeparatorPart;
import net.sf.okapi.common.uidescription.TextInputPart;
import net.sf.okapi.common.uidescription.TextLabelPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String EMAIL = "email";
	private static final String PASSWORD = "password";
	private static final String TMXPATH = "tmxPath";
	private static final String WAITCLASS = "waitClass";
	private static final String MARKASMT = "markAsMT";
	private static final String OPENGTTPAGES = "openGttPages";
	
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
		setEmail("");
		setPassword("");
		setTmxPath("${rootDir}/tmFromGTT.tmx");
		setMarkAsMT(true);
		setOpenGttPages(true);
		setWaitClass("net.sf.okapi.common.ui.WaitDialog");
	}

	public boolean getMarkAsMT () {
		return getBoolean(MARKASMT);
	}

	public void setMarkAsMT (boolean markAsMT) {
		setBoolean(MARKASMT, markAsMT);
	}

	public boolean getOpenGttPages () {
		return getBoolean(OPENGTTPAGES);
	}

	public void setOpenGttPages (boolean openGttPages) {
		setBoolean(OPENGTTPAGES, openGttPages);
	}

	public String getTmxPath () {
		return getString(TMXPATH);
	}

	public void setTmxPath (String tmxPath) {
		setString(TMXPATH, tmxPath);
	}

	public String getEmail () {
		return getString(EMAIL);
	}

	public void setEmail (String email) {
		setString(EMAIL, email);
	}

	public String getPassword () {
		return getString(PASSWORD);
	}

	public void setPassword (String password) {
		setString(PASSWORD, password);
	}

	public String getWaitClass () {
		return getString(WAITCLASS);
	}

	public void setWaitClass (String waitClass) {
		setString(WAITCLASS, waitClass);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(EMAIL, "Email address", "Email address of the Google Translator Toolkit account");
		desc.add(PASSWORD, "Password", "Password of the account");
		desc.add(TMXPATH, "TMX document to create", "Full path of the new TMX document to create");
		desc.add(MARKASMT, "Mark the generated translation as machine translation results", null);
		desc.add(OPENGTTPAGES, "Open the GTT edit pages automatically after upload", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Google Translator Toolkit Settings");
		TextLabelPart tlp = desc.addTextLabelPart("Powered by Google\u00AE Translator Toolkit"); // Required by TOS
		tlp.setVertical(true);
		SeparatorPart sp = desc.addSeparatorPart();
		sp.setVertical(true);

		desc.addTextInputPart(paramsDesc.get(EMAIL));
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(PASSWORD));
		tip.setPassword(true);

		sp = desc.addSeparatorPart();
		sp.setVertical(true);
		
		CheckboxPart cbp = desc.addCheckboxPart(paramsDesc.get(MARKASMT));
		cbp.setVertical(true);

		cbp = desc.addCheckboxPart(paramsDesc.get(OPENGTTPAGES));
		cbp.setVertical(true);

		sp = desc.addSeparatorPart();
		sp.setVertical(true);

		PathInputPart pip = desc.addPathInputPart(paramsDesc.get(TMXPATH), "TMX Path", true);
		pip.setBrowseFilters("TMX Documents (*.tmx)\tAll Files (*.*)", "*.tmx\t*.*");
		pip.setVertical(true);
		pip.setLabelFlushed(false);
		
		return desc;
	}

}
