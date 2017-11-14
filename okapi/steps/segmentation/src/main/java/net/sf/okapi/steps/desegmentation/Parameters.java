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

package net.sf.okapi.steps.desegmentation;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {
	
	private static final String DESEGMENTSOURCE = "desegmentSource";
	private static final String DESEGMENTTARGET = "desegmentTarget";
	private static final String RENUMBERCODES = "renumberCodes";
	
	public Parameters () {
		super();
	}
	
	public boolean getDesegmentSource () {
		return getBoolean(DESEGMENTSOURCE);
	}

	public void setDesegmentSource (boolean desegmentSource) {
		setBoolean(DESEGMENTSOURCE, desegmentSource);
	}

	public boolean getDesegmentTarget () {
		return getBoolean(DESEGMENTTARGET);
	}

	public void setDesegmentTarget (boolean desegmentTarget) {
		setBoolean(DESEGMENTTARGET, desegmentTarget);
	}
	
	public boolean getRenumberCodes() {
		return getBoolean(RENUMBERCODES);
	}
	
	public void setRenumberCodes(boolean renumberCodes) {
		setBoolean(RENUMBERCODES, renumberCodes);
	}

	@Override
	public void reset () {
		super.reset();
		setDesegmentSource(true);
		setDesegmentTarget(true);
		setRenumberCodes(false);
	}
	
	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(DESEGMENTSOURCE, "Join all segments of the source text", null);
		desc.add(DESEGMENTTARGET, "Join all segments of the target text", null);
		desc.add(RENUMBERCODES, "Restore original IDs to renumbered codes", null);
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Desegmentation", true, false);
		desc.addCheckboxPart(paramDesc.get(DESEGMENTSOURCE));
		desc.addCheckboxPart(paramDesc.get(DESEGMENTTARGET));
		desc.addCheckboxPart(paramDesc.get(RENUMBERCODES));
		return desc;
	}

}
