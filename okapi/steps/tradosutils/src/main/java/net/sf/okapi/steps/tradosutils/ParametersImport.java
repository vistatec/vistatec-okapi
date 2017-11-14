/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.steps.tradosutils;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(ParametersImport.class)
public class ParametersImport extends StringParameters implements IEditorDescriptionProvider {

	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final String EXISTINGTM = "existingTm";
	private static final String MODE = "mode";
	private static final String FORMAT = "format";
	private static final String REORGANIZE = "reorganize";
	private static final String IGNORENEWFIELDS = "ignoreNewFields";
	private static final String CHECKLANG = "checkLang";
	private static final String OVERWRITE = "overwrite";
	private static final String SENDTM = "sendTm";

	public String getUser() {
		return getString(USER);
	}

	public void setUser (String user) {
		setString(USER, user);
	}

	public String getPass () {
		return getString(PASS);
	}

	public void setPass (String pass) {
		setString(PASS, pass);
	}

	public int getMode() {
		return getInteger(MODE);
	}

	public void setMode (int mode) {
		setInteger(MODE, mode);
	}
	
	public int getFormat() {
		return getInteger(FORMAT);
	}

	public void setFormat (int format) {
		setInteger(FORMAT, format);
	}
	
	public String getExistingTm () {
		return getString(EXISTINGTM);
	}

	public void setExistingTm (String existingTm) {
		setString(EXISTINGTM, existingTm);
	}

	public boolean getCheckLang() {
		return getBoolean(CHECKLANG);
	}

	public void setCheckLang (boolean checkLang) {
		setBoolean(CHECKLANG, checkLang);
	}

	public boolean getReorganize () {
		return getBoolean(REORGANIZE);
	}
	
	public void setReorganize (boolean reorganize) {
		setBoolean(REORGANIZE, reorganize);
	}
	
	public boolean getIgnoreNewFields () {
		return getBoolean(IGNORENEWFIELDS);
	}
	
	public void setIgnoreNewFields (boolean ignoreNewFields) {
		setBoolean(IGNORENEWFIELDS, ignoreNewFields);
	}
	
	public boolean getOverwrite () {
		return getBoolean(OVERWRITE);
	}

	public void setOverwrite (boolean overwrite) {
		setBoolean(OVERWRITE, overwrite);
	}

	public boolean getSendTm () {
		return getBoolean(SENDTM);
	}
	
	public void setSendTm (boolean sendTm) {
		setBoolean(SENDTM, sendTm);
	}
	
	public ParametersImport () {
		super();
	}
	
	public void reset() {
		super.reset();
		String tmp = System.getProperty("user.name");
		setUser(tmp != null ? tmp : "");
		setPass("");
		setMode(2);
		setFormat(9);
		setExistingTm("");
		setReorganize(false);
		setIgnoreNewFields(false);
		setCheckLang(false);
		setOverwrite(false);
		setSendTm(false);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(EXISTINGTM, "Use or create the TM you want to import into", null);
		desc.add(OVERWRITE, "Overwrite if it exists", null);
		desc.add(SENDTM, "Send the TM to the next step", null);
		
		desc.add(USER, "User ID", null);
		desc.add(PASS, "TM password", "Password of the TM (leave blank if not needed)");

		desc.add(MODE, "Existing translation units:", null);
		desc.add(FORMAT, "Import format:", null);
		desc.add(REORGANIZE, "Large import file (with reorganization) ", null);
		desc.add(IGNORENEWFIELDS, "Ignore new fields", null);
		desc.add(CHECKLANG, "Check matching sub-languages", null);
		
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Trados Import", true, false);

		desc.addPathInputPart(paramDesc.get(EXISTINGTM), "TM File", false);
		desc.addCheckboxPart(paramDesc.get(OVERWRITE));
		desc.addCheckboxPart(paramDesc.get(SENDTM));
		
		TextInputPart tip = desc.addTextInputPart(paramDesc.get(USER));
		tip.setVertical(false);
		
		tip = desc.addTextInputPart(paramDesc.get(PASS));
		tip.setAllowEmpty(true);
		tip.setPassword(true);
		tip.setVertical(false);
		
		desc.addSeparatorPart();

		String[] labels = {
			"Leave unchanged",
			"Keep most recent",
			"Keep oldest",
			"Merge",
			"Overwrite"
		};
		String[] values = {
			"0",
			"3",
			"4",
			"1",
			"2"
		};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(MODE), values);
		lsp.setChoicesLabels(labels);
		lsp.setListType(ListSelectionPart.LISTTYPE_DROPDOWN);
		lsp.setVertical(false);
		
		String[] labels2 = {
				"Translator's Workbench (*.txt)",
				"TMX 1.1 (*.tmx)",
				"TMX 1.4 (*.tmx)",
				"TMX 1.4b (*.tmx)",
				"Systran (*.rtf)",
				"Logos (*.sgm)"
			};
		String[] values2 = {
				"10",
				"6",
				"8",
				"9",
				"2",
				"1"
			};
		lsp = desc.addListSelectionPart(paramDesc.get(FORMAT), values2);
		lsp.setChoicesLabels(labels2);
		lsp.setListType(ListSelectionPart.LISTTYPE_DROPDOWN);
		lsp.setVertical(false);
		
		CheckboxPart cbp = desc.addCheckboxPart(paramDesc.get(REORGANIZE));
		cbp = desc.addCheckboxPart(paramDesc.get(IGNORENEWFIELDS));
		cbp = desc.addCheckboxPart(paramDesc.get(CHECKLANG));
		cbp.setVertical(true);
		
		return desc;
	}
	
}
