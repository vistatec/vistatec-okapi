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
import net.sf.okapi.common.uidescription.PathInputPart;
import net.sf.okapi.common.uidescription.SpinInputPart;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(ParametersAnalysis.class)
public class ParametersAnalysis extends StringParameters implements IEditorDescriptionProvider {

	private static final String USEEXISTING = "useExisting";
	private static final String EXISTINGTM = "existingTm";
	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final String LOGPATH = "logPath";
	private static final String AUTOOPENLOG = "autoOpenLog";
	private static final String APPENDTOLOG = "appendToLog";
	private static final String CREATEPRJTM = "createPrjTm";
	private static final String PRJTMPATH = "prjTmPath";
	private static final String EXPORTUNKNOWN = "exportUnknown";
	private static final String TMXPATH = "tmxPath";	
	private static final String MAXMATCH = "maxMatch";
	private static final String SENDTMX = "sendTmx";

	public String getPrjTmPath() {
		return getString(PRJTMPATH);
	}

	public void setPrjTmPath(String prjTmPath) {
		setString(PRJTMPATH, prjTmPath);
	}

	public boolean isCreatePrjTm() {
		return getBoolean(CREATEPRJTM);
	}

	public void setCreatePrjTm(boolean createPrjTm) {
		setBoolean(CREATEPRJTM, createPrjTm);
	}

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

	public int getMaxMatch () {
		return getInteger(MAXMATCH);
	}

	public void setMaxMatch (int maxMatch) {
		setInteger(MAXMATCH, maxMatch);
	}
	
	public String getLogPath() {
		return getString(LOGPATH);
	}

	public void setLogPath (String logPath) {
		setString(LOGPATH, logPath);
	}
	
	public String getTmxPath () {
		return getString(TMXPATH);
	}

	public void setTmxPath (String tmxPath) {
		setString(TMXPATH, tmxPath);
	}
	
	public String getExistingTm () {
		return getString(EXISTINGTM);
	}

	public void setExistingTm (String existingTm) {
		setString(EXISTINGTM, existingTm);
	}

	public boolean getExportUnknown () {
		return getBoolean(EXPORTUNKNOWN);
	}

	public void setExportUnknown (boolean exportUnknown) {
		setBoolean(EXPORTUNKNOWN, exportUnknown);
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
	
	public boolean getSendTmx () {
		return getBoolean(SENDTMX);
	}
	
	public void setSendTmx (boolean sendTmx) {
		setBoolean(SENDTMX, sendTmx);
	}
	
	public ParametersAnalysis () {
		super();
	}
	
	public void reset() {
		super.reset();
		setUseExisting(false);
		setExistingTm("");
		String tmp = System.getProperty("user.name");
		setUser(tmp != null ? tmp : "");
		setPass("");
		setLogPath(Util.INPUT_ROOT_DIRECTORY_VAR+"/log.txt");
		setAutoOpenLog(false);
		setAppendToLog(true);
		setCreatePrjTm(false);
		setPrjTmPath(Util.INPUT_ROOT_DIRECTORY_VAR+"/project.tmw");
		setExportUnknown(false);
		setTmxPath(Util.INPUT_ROOT_DIRECTORY_VAR+"/unknownSegments.tmx");
		setMaxMatch(90);
		setSendTmx(true);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(USEEXISTING, "Use this existing TM:", null);
		desc.add(EXISTINGTM, null, null);
		desc.add(USER, "User ID", null);
		desc.add(PASS, "TM password", "Password of the TM (leave blank if not needed)");

		desc.add(LOGPATH, "Full path of the log file", null);
		desc.add(APPENDTOLOG, "Append to the log file if one exists already", null);
		desc.add(AUTOOPENLOG, "Open the log file after completion", null);

		desc.add(CREATEPRJTM, "Create project TM:", null);
		desc.add(PRJTMPATH, "Full path of the new Trados project TM to create", "Full path of the new Trados project TM to create");
		desc.add(EXPORTUNKNOWN, "Export unknown segments:", null);
		desc.add(TMXPATH, "Full path of the new TMX document to create", "Full path of the new TMX document to create");
		desc.add(MAXMATCH, "Export threshold", "Export segments with no match above this threshold");
		desc.add(SENDTMX, "Send the TMX document to the next step", null);
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Trados Analysis", true, false);

		CheckboxPart cbp = desc.addCheckboxPart(paramDesc.get(USEEXISTING));
		
		PathInputPart pip = desc.addPathInputPart(paramDesc.get(EXISTINGTM), "TM File", false);
		pip.setWithLabel(false);
		pip.setMasterPart(cbp, true);
		
		// User ID is always required (even for temporary TM)
		TextInputPart tip = desc.addTextInputPart(paramDesc.get(USER));
		tip.setVertical(false);
		
		tip = desc.addTextInputPart(paramDesc.get(PASS));
		tip.setAllowEmpty(true);
		tip.setPassword(true);
		tip.setVertical(false);
		tip.setMasterPart(cbp, true);
		
		desc.addSeparatorPart();
		
		pip = desc.addPathInputPart(paramDesc.get(LOGPATH), "Log Path", true);
		pip.setWithLabel(true);
		
		cbp = desc.addCheckboxPart(paramDesc.get(APPENDTOLOG));

		cbp = desc.addCheckboxPart(paramDesc.get(AUTOOPENLOG));

		desc.addSeparatorPart();

		cbp = desc.addCheckboxPart(paramDesc.get(CREATEPRJTM));
		pip = desc.addPathInputPart(paramDesc.get(PRJTMPATH), "TM Path", true);
		pip.setMasterPart(cbp, true);
		pip.setWithLabel(false);
		
		desc.addSeparatorPart();
		
		cbp = desc.addCheckboxPart(paramDesc.get(EXPORTUNKNOWN));
		pip = desc.addPathInputPart(paramDesc.get(TMXPATH), "TMX Path", true);
		pip.setMasterPart(cbp, true);
		pip.setWithLabel(false);
		SpinInputPart sip = desc.addSpinInputPart(paramDesc.get(MAXMATCH));
		sip.setVertical(false);
		sip.setRange(0, 100);
		sip.setMasterPart(cbp, true);
		CheckboxPart cbp2 = desc.addCheckboxPart(paramDesc.get(SENDTMX));
		cbp2.setMasterPart(cbp, true);
		
		return desc;
	}
	
}
