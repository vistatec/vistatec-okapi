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

package net.sf.okapi.steps.xliffsplitter;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(XliffWCSplitterParameters.class)
public class XliffWCSplitterParameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String THRESHOLD = "threshold";

	public XliffWCSplitterParameters() {
		super();
		reset();
	}

	public void reset() {
		super.reset();
		setThreshold(200000);
	}

	public int getThreshold () {
		return getInteger(THRESHOLD);
	}
	
	public void setThreshold (int threshold) {
		setInteger(THRESHOLD, threshold);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(THRESHOLD, "Maximum word-count per part:", null);
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("XLIFF Word-Count Splitter", true, false);
		TextInputPart ip = desc.addTextInputPart(paramsDesc.get(THRESHOLD));
		ip.setAllowEmpty(false);
		ip.setRange(1, 1000000);
		ip.setVertical(false);
		return desc;
	}

}