/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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

package net.sf.okapi.steps.ttxsplitter;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(TTXJoinerParameters.class)
public class TTXJoinerParameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String SUFFIX = "suffix";

	public TTXJoinerParameters() {
		super();
	}

	public void reset() {
		super.reset();
		setSuffix("_joined");
	}

	public String getSuffix () {
		return getString(SUFFIX);
	}
	
	public void setSuffix (String suffix) {
		setString(SUFFIX, suffix);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(SUFFIX, "Optional suffix to place at the end of the result files", null);
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("TTX Joiner", true, false);
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(SUFFIX));
		tip.setAllowEmpty(true);
		return desc;
	}

}