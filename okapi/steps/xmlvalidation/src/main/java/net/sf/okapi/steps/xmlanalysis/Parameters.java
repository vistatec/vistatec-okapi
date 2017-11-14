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
============================================================================*/

package net.sf.okapi.steps.xmlanalysis;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.PathInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static String OUTPUTPATH = "outputPath";
	private static String AUTOOPEN = "autoOpen";
	
	public Parameters () {
		super();
	}
	
	public String getOutputPath () {
		return getString(OUTPUTPATH);
	}

	public void setOutputPath (String outputPath) {
		setString(OUTPUTPATH, outputPath);
	}

	public boolean getAutoOpen () {
		return getBoolean(AUTOOPEN);
	}

	public void setAutoOpen (boolean autoOpen) {
		setBoolean(AUTOOPEN, autoOpen);
	}

	@Override
	public void reset() {
		super.reset();
		setOutputPath("analysis.html");
		setAutoOpen(true);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(OUTPUTPATH,
			"Path of the result file", "Full path of the result file.");
		desc.add(AUTOOPEN,
			"Open the result file after completion", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("XML Analysis", true, false);

		PathInputPart part = desc.addPathInputPart(paramDesc.get(OUTPUTPATH), "Result File", true);
		part.setBrowseFilters("Text Files (*.txt)\tAll Files (*.*)", "*.txt\t*.*");
		
		desc.addCheckboxPart(paramDesc.get(AUTOOPEN));
		
		return desc;
	}
	
}
