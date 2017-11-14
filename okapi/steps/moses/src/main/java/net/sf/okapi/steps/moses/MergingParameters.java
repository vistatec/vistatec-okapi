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

package net.sf.okapi.steps.moses;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(MergingParameters.class)
public class MergingParameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String COPYTOTARGET = "copyToTarget";
	private static final String OVERWRITEEXISINGTARGET = "overwriteExistingTarget";
	private static final String FORCEALTTRANSOUTPUT = "forceAltTransOutput";
	private static final String USEGMODEINALTTRANS = "useGModeInAltTrans";
	
	public MergingParameters () {
		super();
	}
	
	public boolean getCopyToTarget () {
		return getBoolean(COPYTOTARGET);
	}

	public void setCopyToTarget (boolean copyToTarget) {
		setBoolean(COPYTOTARGET, copyToTarget);
	}

	public boolean getOverwriteExistingTarget () {
		return getBoolean(OVERWRITEEXISINGTARGET);
	}

	public void setOverwriteExistingTarget (boolean overwriteExistingTarget) {
		setBoolean(OVERWRITEEXISINGTARGET, overwriteExistingTarget);
	}

	public boolean getForceAltTransOutput () {
		return getBoolean(FORCEALTTRANSOUTPUT);
	}

	public void setForceAltTransOutput (boolean forceAltTransOutput) {
		setBoolean(FORCEALTTRANSOUTPUT, forceAltTransOutput);
	}

	public boolean getUseGModeInAltTrans () {
		return getBoolean(USEGMODEINALTTRANS);
	}

	public void setUseGModeInAltTrans (boolean useGModeInAltTrans) {
		setBoolean(USEGMODEINALTTRANS, useGModeInAltTrans);
	}

	@Override
	public void reset() {
		super.reset();
		setCopyToTarget(false);
		setOverwriteExistingTarget(false);
		setForceAltTransOutput(true);
		setUseGModeInAltTrans(true);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(COPYTOTARGET, "Copy the leveraged translation into the target", null);
		desc.add(OVERWRITEEXISINGTARGET, "Overwrite any existing target text", null);
		desc.add(FORCEALTTRANSOUTPUT, "In XLIFF, force the new <alt-trans> in the output", null);
		desc.add(USEGMODEINALTTRANS, "Use the <g> notation in new <alt-trans> elements", null);
		return desc;
	}

	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription(MergingStep.NAME, true, false);

		CheckboxPart cbp1 = desc.addCheckboxPart(paramDesc.get(COPYTOTARGET));
		CheckboxPart cbp2 = desc.addCheckboxPart(paramDesc.get(OVERWRITEEXISINGTARGET));
		cbp2.setMasterPart(cbp1, true);
		
		cbp1 = desc.addCheckboxPart(paramDesc.get(FORCEALTTRANSOUTPUT));
		desc.addCheckboxPart(paramDesc.get(USEGMODEINALTTRANS)).setMasterPart(cbp1, true);
		
		return desc;
	}

}
