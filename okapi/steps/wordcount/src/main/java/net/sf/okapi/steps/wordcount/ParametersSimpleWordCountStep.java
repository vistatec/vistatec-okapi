/*===========================================================================
  Copyright (C) 2011-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.wordcount;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

// For now we don't use the parameters
// @EditorFor(ParametersSimpleWordCountStep.class)
public class ParametersSimpleWordCountStep extends StringParameters implements IEditorDescriptionProvider {	

	public static final String COUNTTARGETS = "countTargets";
	
	public ParametersSimpleWordCountStep () {
		super();
	}

	@Override
	public void reset () {
		super.reset();
		setCountTargets(false);
	}
	
	public boolean getCountTargets () {
		return getBoolean(COUNTTARGETS);
	}
	
	public void setCountTargets(boolean countTargets) {
		setBoolean(COUNTTARGETS, countTargets);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(COUNTTARGETS, "Count also the target entries", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Translation Comparison", true, false);
		desc.addCheckboxPart(paramsDesc.get(COUNTTARGETS));
		return desc;
	}

}
