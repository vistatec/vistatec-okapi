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
import net.sf.okapi.common.Util;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;
import net.sf.okapi.common.uidescription.PathInputPart;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(ParametersCleanup.class)
public class ParametersCleanup extends StringParameters implements IEditorDescriptionProvider {

	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final String EXISTINGTM = "existingTm";
	private static final String LOGPATH = "logPath";
	private static final String WHENCHANGED = "whenChanged";
	private static final String USEEXISTING = "useExisting";
	private static final String OVERWRITE = "overwrite";
	private static final String AUTOOPENLOG = "autoOpenLog";
	private static final String APPENDTOLOG = "appendToLog";
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
	
	public int getWhenChanged() {
		return getInteger(WHENCHANGED);
	}

	public void setWhenChanged (int whenChanged) {
		setInteger(WHENCHANGED, whenChanged);
	}
	
	public String getLogPath() {
		return getString(LOGPATH);
	}

	public void setLogPath (String logPath) {
		setString(LOGPATH, logPath);
	}
	
	public String getExistingTm () {
		return getString(EXISTINGTM);
	}

	public void setExistingTm (String existingTm) {
		setString(EXISTINGTM, existingTm);
	}

	public boolean getUseExisting () {
		return getBoolean(USEEXISTING);
	}

	public void setUseExisting (boolean useExisting) {
		setBoolean(USEEXISTING, useExisting);
	}

	public boolean getAutoOpenLog () {
		return getBoolean(AUTOOPENLOG);
	}
	
	public void setAutoOpenLog (boolean autoOpenLog) {
		setBoolean(AUTOOPENLOG, autoOpenLog);
	}
	
	public boolean getAppendToLog () {
		return getBoolean(APPENDTOLOG);
	}
	
	public void setAppendToLog (boolean appendToLog) {
		setBoolean(APPENDTOLOG, appendToLog);
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
	
	public ParametersCleanup () {
		super();
	}
	
	public void reset() {
		super.reset();
		String tmp = System.getProperty("user.name");
		setUser(tmp != null ? tmp : "");
		setPass("");
		setWhenChanged(0);
		setLogPath(Util.INPUT_ROOT_DIRECTORY_VAR+"/log.txt");
		setExistingTm("");
		setUseExisting(false);
		setOverwrite(false);
		setAutoOpenLog(false);
		setAppendToLog(true);
		setSendTm(false);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(USEEXISTING, "Use or create the following TM:", null);
		desc.add(EXISTINGTM, null, null);
		desc.add(OVERWRITE, "Overwrite if it exists", null);
		desc.add(USER, "User ID", null);
		desc.add(PASS, "TM password", "Password of the TM (leave blank if not needed)");

		desc.add(LOGPATH, "Full path of the log file", null);
		desc.add(APPENDTOLOG, "Append to the log file if one exists already", null);
		desc.add(AUTOOPENLOG, "Open the log file after completion", null);

		desc.add(WHENCHANGED, "Changed translations", null);
		desc.add(SENDTM, "Send the TM to the next step", null);
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Trados Clean-up", true, false);

		CheckboxPart cbp1 = desc.addCheckboxPart(paramDesc.get(USEEXISTING));
		
		CheckboxPart cbp3 = desc.addCheckboxPart(paramDesc.get(OVERWRITE));
		cbp3.setMasterPart(cbp1, true);
		
		CheckboxPart cbp2 = desc.addCheckboxPart(paramDesc.get(SENDTM));
		cbp2.setMasterPart(cbp1, true);
		
		PathInputPart pip = desc.addPathInputPart(paramDesc.get(EXISTINGTM), "TM File", false);
		pip.setWithLabel(false);
		pip.setMasterPart(cbp1, true);

		// User ID is always needed
		TextInputPart tip = desc.addTextInputPart(paramDesc.get(USER));
		tip.setVertical(false);
		
		tip = desc.addTextInputPart(paramDesc.get(PASS));
		tip.setAllowEmpty(true);
		tip.setPassword(true);
		tip.setVertical(false);
		tip.setMasterPart(cbp1, true);
		
		desc.addSeparatorPart();

		pip = desc.addPathInputPart(paramDesc.get(LOGPATH), "Log Path", true);
		pip.setWithLabel(true);
		
		desc.addCheckboxPart(paramDesc.get(APPENDTOLOG));
		desc.addCheckboxPart(paramDesc.get(AUTOOPENLOG));
		
		desc.addSeparatorPart();

		String[] labels = {
			"No update",
			"Update TM",
			"Update document"
		};
		String[] values = {
			"0",
			"2",
			"3"
		};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(WHENCHANGED), values);
		lsp.setMasterPart(cbp1, true);
		lsp.setChoicesLabels(labels);
		lsp.setListType(ListSelectionPart.LISTTYPE_DROPDOWN);

		return desc;
	}
	
}
