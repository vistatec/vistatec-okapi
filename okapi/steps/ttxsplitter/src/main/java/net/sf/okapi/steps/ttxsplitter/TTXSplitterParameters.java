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
import net.sf.okapi.common.uidescription.SpinInputPart;

@EditorFor(TTXSplitterParameters.class)
public class TTXSplitterParameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String PARTCOUNT = "partCount";

	public TTXSplitterParameters() {
		super();
	}

	public void reset() {
		super.reset();
		setPartCount(2);
	}

	public int getPartCount () {
		return getInteger(PARTCOUNT);
	}
	
	public void setPartCount (int partCount) {
		setInteger(PARTCOUNT, partCount);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(PARTCOUNT, "Number of output files", null);
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("TTX Splitter", true, false);
		SpinInputPart sip = desc.addSpinInputPart(paramsDesc.get(PARTCOUNT));
		sip.setRange(2, 999);
		return desc;
	}

}