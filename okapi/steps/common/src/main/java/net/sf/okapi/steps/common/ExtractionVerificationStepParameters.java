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

package net.sf.okapi.steps.common;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(ExtractionVerificationStepParameters.class)
public class ExtractionVerificationStepParameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String COMPARESKELETON = "compareSkeleton";
	private static final String STEPENABLED = "stepEnabled";
	private static final String ALLEVENTS = "allEvents";
	private static final String LIMIT = "limit";
	private static final String INTERRUPT = "interrupt";
	
	public boolean monolingual = false;
	
	public ExtractionVerificationStepParameters() {
		super();
	}

	public void reset() {
		super.reset();
		setCompareSkeleton(true);
		setStepEnabled(true);
		setAllEvents(true);
		setLimit(10);
		setInterrupt(false);
	}

	public boolean getStepEnabled () {
		return getBoolean(STEPENABLED);
	}

	public void setStepEnabled (boolean stepEnabled) {
		setBoolean(STEPENABLED, stepEnabled);
	}
	
	public boolean getCompareSkeleton () {
		return getBoolean(COMPARESKELETON);
	}

	public void setCompareSkeleton (boolean compareSkeleton) {
		setBoolean(COMPARESKELETON, compareSkeleton);
	}

	public boolean getAllEvents () {
		return getBoolean(ALLEVENTS);
	}

	public void setAllEvents (boolean allEvents) {
		setBoolean(ALLEVENTS, allEvents);
	}
	
	public int getLimit () {
		return getInteger(LIMIT);
	}
	
	public void setLimit (int limit) {
		setInteger(LIMIT, limit);
	}
	
	public boolean getInterrupt () {
		return getBoolean(INTERRUPT);
	}

	public void setInterrupt (boolean interrupt) {
		setBoolean(INTERRUPT, interrupt);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(STEPENABLED, "Perform the extraction verification", null);
		desc.add(COMPARESKELETON, "Compare skeleton", null);
		desc.add(ALLEVENTS, "Verify all events (otherwise only text units are verified)", null);
		desc.add(LIMIT, "Maximum number of warnings per document", null);
		desc.add(INTERRUPT, "Interrupt after reaching the maximum number of warnings", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Extraction Verification", true, false);

		CheckboxPart cbp = desc.addCheckboxPart(paramsDesc.get(STEPENABLED));
		desc.addSeparatorPart();
		
		CheckboxPart cbp2 = desc.addCheckboxPart(paramsDesc.get(ALLEVENTS));
		cbp2.setMasterPart(cbp, true);
		
		cbp2 = desc.addCheckboxPart(paramsDesc.get(COMPARESKELETON));
		cbp2.setMasterPart(cbp, true);
		
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(LIMIT));
		tip.setMasterPart(cbp, true);
		
		cbp2 = desc.addCheckboxPart(paramsDesc.get(INTERRUPT));
		cbp2.setMasterPart(cbp, true);
		
		return desc;
	}
}
