/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.common.tufiltering;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String TUFILTERCLASSNAME = "tuFilterClassName";
	
	public Parameters() {
		super();
	}
	
	@Override
	public void reset() {
		super.reset();
		setTuFilterClassName(null);
	}

	public void setTuFilterClassName(String tuFilterClassName) {
		setString(TUFILTERCLASSNAME, tuFilterClassName);
	}

	public String getTuFilterClassName() {
		return getString(TUFILTERCLASSNAME);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add("tuFilterClassName", "Class name for the text unit filter", null);
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription(
			ParametersDescription parametersDescription) {
		EditorDescription desc = new EditorDescription("Text Unit Filtering", true, false);
		desc.addTextInputPart(parametersDescription.get("tuFilterClassName"));
		return desc;
	}
}
