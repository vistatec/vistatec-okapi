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

@EditorFor(XliffJoinerParameters.class)
public class XliffJoinerParameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String INPUTFILEMARKER = "inputFileMarker";
	private static final String OUTPUTFILEMARKER = "outputFileMarker";

	public XliffJoinerParameters() {
		super();
	}

	public void reset() {
		super.reset();
		setInputFileMarker("_PART");
		setOutputFileMarker("_CONCAT");
	}

	public String getInputFileMarker() {
		return getString(INPUTFILEMARKER);
	}
	
	public void setInputFileMarker (String inputFileMarker) {
		setString(INPUTFILEMARKER, inputFileMarker);
	}
	
	public String getOutputFileMarker() {
		return getString(OUTPUTFILEMARKER);
	}
	
	public void setOutputFileMarker (String outputFileMarker) {
		setString(OUTPUTFILEMARKER, outputFileMarker);
	}
	

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(INPUTFILEMARKER, "Input file marker", null);
		desc.add(OUTPUTFILEMARKER, "Output file marker", null);
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("XLIFF Joiner", true, false);
		
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(INPUTFILEMARKER));
		tip.setVertical(false);
		tip = desc.addTextInputPart(paramsDesc.get(OUTPUTFILEMARKER));
		tip.setVertical(false);
		tip.setAllowEmpty(true);

		return desc;
	}

}