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
============================================================================*/

package net.sf.okapi.filters.txml;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider, ISimplifierRulesParameters {

	public static final String ALLOWEMPTYOUTPUTTARGET = "allowEmptyOutputTarget";

	public Parameters () {
		super();
	}
	
	public boolean getAllowEmptyOutputTarget () {
		return getBoolean(ALLOWEMPTYOUTPUTTARGET);
	}

	public void setAllowEmptyOutputTarget (boolean allowEmptyOutputTarget) {
		setBoolean(ALLOWEMPTYOUTPUTTARGET, allowEmptyOutputTarget);
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

	public void reset () {
		super.reset();
		setAllowEmptyOutputTarget(true);
		setSimplifierRules(null);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(ALLOWEMPTYOUTPUTTARGET, "Allow empty target segments in output", null);
		return desc;
	}

	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("TXML Filter Parameters", true, false);
		desc.addCheckboxPart(paramDesc.get(ALLOWEMPTYOUTPUTTARGET));
		return desc;
	}

}
