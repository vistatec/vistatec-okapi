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

package net.sf.okapi.steps.msbatchtranslation;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.PathInputPart;
import net.sf.okapi.common.uidescription.SeparatorPart;
import net.sf.okapi.common.uidescription.SpinInputPart;
import net.sf.okapi.common.uidescription.TextInputPart;
import net.sf.okapi.common.uidescription.TextLabelPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String AZUREKEY = "azureKey";
	private static final String CATEGORY = "category";
	private static final String CONFIGPATH = "configPath";
	private static final String ANNOTATE = "annotate";
	private static final String MAKETMX = "makeTmx";
	private static final String TMXPATH = "tmxPath";
	private static final String MARKASMT = "markAsMT";
	private static final String MAXEVENTS = "maxEvents";
	private static final String MAXMATCHES = "maxMatches";
	private static final String THRESHOLD = "threshold";
	private static final String FILLTARGET = "fillTarget";
	private static final String FILLTARGETTHRESHOLD = "fillTargetThreshold";
	private static final String ONLYWHENWITHOUTCANDIDATE = "onlyWhenWithoutCandidate";
	private static final String SENDTMX = "sendTmx";
	
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
		setAzureKey("");
		setCategory("");
		setTmxPath("${rootDir}/tmFromMS.tmx");
		setConfigPath("");
		setMarkAsMT(true);
		setMaxEvents(20);
		setMaxMatches(1);
		setThreshold(80);
		setMakeTmx(false);
		setAnnotate(true);
		setFillTarget(true);
		setFillTargetThreshold(95);
		setOnlyWhenWithoutCandidate(true);
		setSendTmx(false);
	}

	public boolean getFillTarget () {
		return getBoolean(FILLTARGET);
	}

	public void setFillTarget (boolean fillTarget) {
		setBoolean(FILLTARGET, fillTarget);
	}

	public int getFillTargetThreshold () {
		return getInteger(FILLTARGETTHRESHOLD);
	}

	public void setFillTargetThreshold (int fillTargetThreshold) {
		setInteger(FILLTARGETTHRESHOLD, fillTargetThreshold);
	}

	public boolean getMarkAsMT () {
		return getBoolean(MARKASMT);
	}

	public void setMarkAsMT (boolean markAsMT) {
		setBoolean(MARKASMT, markAsMT);
	}

	public int getMaxEvents () {
		return getInteger(MAXEVENTS);
	}
	
	public void setMaxEvents (int maxEvents) {
		setInteger(MAXEVENTS, maxEvents);
	}
	
	public int getThreshold () {
		return getInteger(THRESHOLD);
	}
	
	public void setThreshold (int threshold) {
		setInteger(THRESHOLD, threshold);
	}
	
	public int getMaxMatches () {
		return getInteger(MAXMATCHES);
	}
	
	public void setMaxMatches (int maxMatches) {
		setInteger(MAXMATCHES, maxMatches);
	}
	
	public String getTmxPath () {
		return getString(TMXPATH);
	}

	public void setTmxPath (String tmxPath) {
		setString(TMXPATH, tmxPath);
	}
	
	public String getConfigPath () {
		return getString(CONFIGPATH);
	}

	public void setConfigPath (String configPath) {
		setString(CONFIGPATH, configPath);
	}

	public String getAzureKey () {
		return getString(AZUREKEY);
	}

	public void setAzureKey (String azureKey) {
		setString(AZUREKEY, azureKey);
	}

	public String getCategory () {
		return getString(CATEGORY);
	}

	public void setCategory (String category) {
		setString(CATEGORY, category);
	}
	
	public boolean getMakeTmx () {
		return getBoolean(MAKETMX);
	}
	
	public void setMakeTmx (boolean makeTmx) {
		setBoolean(MAKETMX, makeTmx);
	}
	
	public boolean getAnnotate () {
		return getBoolean(ANNOTATE);
	}
	
	public void setAnnotate (boolean annotate) {
		setBoolean(ANNOTATE, annotate);
	}
	
	public boolean getOnlyWhenWithoutCandidate () {
		return getBoolean(ONLYWHENWITHOUTCANDIDATE);
	}
	
	public void setOnlyWhenWithoutCandidate (boolean onlyWhenWithoutCandidate) {
		setBoolean(ONLYWHENWITHOUTCANDIDATE, onlyWhenWithoutCandidate);
	}
	
	public boolean getSendTmx () {
		return getBoolean(SENDTMX);
	}
	
	public void setSendTmx (boolean sendTmx) {
		setBoolean(SENDTMX, sendTmx);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(AZUREKEY, "Azure Key", "Microsoft Azure subscription key");
		desc.add(CATEGORY, "Category", "Category code if accessing a trained system");
		desc.add(CONFIGPATH, "Engine mapping", "Full path of the properties file listing the engines");		
		desc.add(MAXEVENTS, "Events buffer", "Number of events to store before sending a query");
		desc.add(MAXMATCHES, "Maximum matches", "Maximum number of matches allowed");
		desc.add(THRESHOLD, "Threshold", "Score below which matches are not retained");
		desc.add(ONLYWHENWITHOUTCANDIDATE, "Query only entries without existing candidate", null);
		desc.add(TMXPATH, null, "Full path of the new TMX document to create");
		desc.add(MARKASMT, "Mark the generated translation as machine translation results", null);
		desc.add(MAKETMX, "Generate a TMX document", null);
		desc.add(SENDTMX, "Send the TMX document to the next step", null);
		desc.add(ANNOTATE, "Annotate the text units with the translations", null);
		desc.add(FILLTARGET, "Fill the target with the best translation candidate", null);
		desc.add(FILLTARGETTHRESHOLD, "Fill threshold", "Fill the target when the best candidate is equal or above this score");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Microsoft Batch Translation Settings");
		
		TextLabelPart tlp = desc.addTextLabelPart("Powered by Microsoft\u00AE Translator"); // Required by TOS
		tlp.setVertical(true);
		SeparatorPart sp = desc.addSeparatorPart();
		sp.setVertical(true);

		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(AZUREKEY));
		tip.setPassword(true);

		tip = desc.addTextInputPart(paramsDesc.get(CATEGORY));
		tip.setAllowEmpty(true);
		tip.setPassword(false);

		PathInputPart pip1 = desc.addPathInputPart(paramsDesc.get(CONFIGPATH), "Config Path", false);
		pip1.setBrowseFilters("MS HUB Config (*.properties)\tAll Files (*.*)", "*.properties\t*.*");
		pip1.setVertical(false);
		pip1.setWithLabel(true);
		pip1.setAllowEmpty(true);
		
		sp = desc.addSeparatorPart();
		sp.setVertical(true);
		
		SpinInputPart sip = desc.addSpinInputPart(paramsDesc.get(MAXEVENTS));
		sip.setRange(1, 999);
		
		sip = desc.addSpinInputPart(paramsDesc.get(MAXMATCHES));
		sip.setRange(1, 100);
		
		sip = desc.addSpinInputPart(paramsDesc.get(THRESHOLD));
		sip.setRange(1, 100);
		
		CheckboxPart cbp = desc.addCheckboxPart(paramsDesc.get(ONLYWHENWITHOUTCANDIDATE));
		cbp.setVertical(true);
		
		sp = desc.addSeparatorPart();
		sp.setVertical(true);

		cbp = desc.addCheckboxPart(paramsDesc.get(ANNOTATE));
		cbp.setVertical(true);
		
		CheckboxPart master = desc.addCheckboxPart(paramsDesc.get(MAKETMX));
		master.setVertical(true);
		
		PathInputPart pip = desc.addPathInputPart(paramsDesc.get(TMXPATH), "TMX Path", true);
		pip.setBrowseFilters("TMX Documents (*.tmx)\tAll Files (*.*)", "*.tmx\t*.*");
		pip.setVertical(true);
		pip.setWithLabel(false);
		pip.setMasterPart(master, true);
		
		cbp = desc.addCheckboxPart(paramsDesc.get(SENDTMX));
		cbp.setVertical(true);
		cbp.setMasterPart(master, true);
		
		cbp = desc.addCheckboxPart(paramsDesc.get(MARKASMT));
		cbp.setVertical(true);
		cbp.setMasterPart(master, true);

		sp = desc.addSeparatorPart();
		sp.setVertical(true);

		cbp = desc.addCheckboxPart(paramsDesc.get(FILLTARGET));
		cbp.setVertical(true);

		sip = desc.addSpinInputPart(paramsDesc.get(FILLTARGETTHRESHOLD));
		sip.setRange(1, 100);
		sip.setMasterPart(cbp, true);
		
		return desc;
	}

}
