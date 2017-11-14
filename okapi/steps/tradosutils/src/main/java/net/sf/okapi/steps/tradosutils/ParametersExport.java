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
import net.sf.okapi.common.uidescription.PathInputPart;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(ParametersExport.class)
public class ParametersExport extends StringParameters implements IEditorDescriptionProvider {

	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final String CONSTRAINTSFILE = "constraintsFile";
	private static final String FORMAT = "format";
	private static final String SENDEXPORTEDFILE = "sendExportedFile";

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

	public int getFormat() {
		return getInteger(FORMAT);
	}

	public void setFormat (int format) {
		setInteger(FORMAT, format);
	}
	
	public String getConstraintsFile () {
		return getString(CONSTRAINTSFILE);
	}

	public void setConstraintsFile (String constraintsFile) {
		setString(CONSTRAINTSFILE, constraintsFile);
	}

	public boolean getSendExportedFile () {
		return getBoolean(SENDEXPORTEDFILE);
	}
	
	public void setSendExportedFile (boolean sendExportedFile) {
		setBoolean(SENDEXPORTEDFILE, sendExportedFile);
	}
	
	public ParametersExport () {
		super();
	}
	
	public void reset() {
		super.reset();
		String tmp = System.getProperty("user.name");
		setUser(tmp != null ? tmp : "");
		setPass("");
		setFormat(9);
		setConstraintsFile("");
		setSendExportedFile(false);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(CONSTRAINTSFILE, "Select filter constraints file", null);
		desc.add(USER, "User ID", null);
		desc.add(PASS, "TM password", "Password of the TM (leave blank if not needed)");

		desc.add(FORMAT, "Export format:", null);
		desc.add(SENDEXPORTEDFILE, "Send exported document to the next step", null);
	
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Trados Export", true, false);

		TextInputPart tip = desc.addTextInputPart(paramDesc.get(USER));
		tip.setVertical(false);
		
		tip = desc.addTextInputPart(paramDesc.get(PASS));
		tip.setAllowEmpty(true);
		tip.setPassword(true);
		tip.setVertical(false);
		
		desc.addSeparatorPart();

		String[] labels = {
				"Translator's Workbench (*.txt)",
				"Tmx 1.1 (*.tmx)",
				"Tmx 1.4 (*.tmx)",
				"Tmx 1.4b (*.tmx)",
				"Systran (*.rtf)",
				"Logos (*.sgm)"
			};
		String[] values = {
				"10",
				"6",
				"8",
				"9",
				"2",
				"1"
			};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(FORMAT), values);
		lsp.setChoicesLabels(labels);
		lsp.setListType(ListSelectionPart.LISTTYPE_DROPDOWN);
		lsp.setVertical(false);
		
		PathInputPart pip = desc.addPathInputPart(paramDesc.get(CONSTRAINTSFILE), "Select filter constraints file", false);
		pip.setBrowseFilters("Constraint Settings File (*.wcs)\tAll Files (*.*)", "*.wcs\t*.*");
		pip.setAllowEmpty(true);
		
		CheckboxPart cbp = desc.addCheckboxPart(paramDesc.get(SENDEXPORTEDFILE));
		cbp.setVertical(true);
		
		return desc;
	}

	/**
	 * Gets the Okapi filter configuration for a given Trados export format.
	 * @param formatCode the code of the export format.
	 * @return the configuration identifier, or null if none is found.
	 */
	public String getFilterConfigurationForExportFormat (int formatCode) {
		switch ( formatCode ) {
		case 6:
		case 8:
		case 9:
			return "okf_tmx";
		default:
			return null;
		}
	}

}
