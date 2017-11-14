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

package net.sf.okapi.filters.icml;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.SpinInputPart;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider, ISimplifierRulesParameters {

	private static final String EXTRACTNOTES = "extractNotes";
	private static final String SIMPLIFYCODES = "simplifyCodes";
	private static final String EXTRACTMASTERSPREADS = "extractMasterSpreads";
	private static final String SKIPTHRESHOLD = "skipThreshold";
	private static final String NEWTUONBR = "newTuOnBr";

	private boolean extractNotes;
	private boolean simplifyCodes;
	private boolean extractMasterSpreads;
	private int skipThreshold;
	private boolean newTuOnBr;

	public Parameters () {
		reset();
		toString(); // fill the list
	}
	
	public void reset () {
		extractNotes = false;
		simplifyCodes = true;
		extractMasterSpreads = true;
		skipThreshold = 1000;
		newTuOnBr = false; //true;
		setSimplifierRules(null);
	}

	public void fromString (String data) {
		reset();
		buffer.fromString(data);
		extractNotes = buffer.getBoolean(EXTRACTNOTES, extractNotes);
		simplifyCodes = buffer.getBoolean(SIMPLIFYCODES, simplifyCodes);
		extractMasterSpreads = buffer.getBoolean(EXTRACTMASTERSPREADS, extractMasterSpreads);
		skipThreshold = buffer.getInteger(SKIPTHRESHOLD, skipThreshold);
		newTuOnBr = buffer.getBoolean(NEWTUONBR, newTuOnBr);
	}
	
	@Override
	public String toString () {
		buffer.reset();
		buffer.setBoolean(EXTRACTNOTES, extractNotes);
		buffer.setBoolean(SIMPLIFYCODES, simplifyCodes);
		buffer.setBoolean(EXTRACTMASTERSPREADS, extractMasterSpreads);
		buffer.setInteger(SKIPTHRESHOLD, skipThreshold);
		buffer.setBoolean(NEWTUONBR, newTuOnBr);
		return buffer.toString();
	}

	public boolean getExtractNotes () {
		return extractNotes;
	}
	
	public void setExtractNotes (boolean extractNotes) {
		this.extractNotes = extractNotes;
	}

	public boolean getSimplifyCodes () {
		return simplifyCodes;
	}
	
	public void setSimplifyCodes (boolean simplifyCodes) {
		this.simplifyCodes = simplifyCodes;
	}

	public boolean getExtractMasterSpreads () {
		return extractMasterSpreads;
	}
	
	public void setExtractMasterSpreads (boolean extractMasterSpreads) {
		this.extractMasterSpreads = extractMasterSpreads;
	}
	
	public int getSkipThreshold () {
		return skipThreshold;
	}
	
	public void setSkipThreshold (int skipThreshold) {
		this.skipThreshold = skipThreshold;
	}

	public boolean getNewTuOnBr () {
		return newTuOnBr;
	}
	
	public void setNewTuOnBr (boolean newTuOnBr) {
		this.newTuOnBr = newTuOnBr;
	}
	
	@Override
	public String getSimplifierRules() {
		return getString(SIMPLIFIERRULES);
	}

	@Override
	public void setSimplifierRules(String rules) {
		setString(SIMPLIFIERRULES, rules);		
	}

	@Override
	public void validateSimplifierRules() throws ParseException {
		SimplifierRules r = new SimplifierRules(getSimplifierRules(), new Code());
		r.parse();
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(EXTRACTNOTES, "Extract notes", null);
		desc.add(EXTRACTMASTERSPREADS, "Extract master spreads", null);
		desc.add(SIMPLIFYCODES, "Simplify inline codes when possible", null);
		desc.add(NEWTUONBR, "Create new paragraphs on hard returns (<Br/> elements) [BETA]", null);
		desc.add(SKIPTHRESHOLD, "Maximum spread size", "Skip any spread larger than the given value (in Kbytes)");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("ICML Filter", true, false);
		
		desc.addCheckboxPart(paramsDesc.get(EXTRACTNOTES));
		desc.addCheckboxPart(paramsDesc.get(EXTRACTMASTERSPREADS));
		desc.addSeparatorPart();
		
		desc.addCheckboxPart(paramsDesc.get(SIMPLIFYCODES));
//TODO: NOT READY YET		desc.addCheckboxPart(paramsDesc.get(NEWTUONBR));
		desc.addSeparatorPart();
		
		SpinInputPart sip = desc.addSpinInputPart(paramsDesc.get(SKIPTHRESHOLD));
		sip.setRange(1, 32000);
		
		return desc;
	}

}
