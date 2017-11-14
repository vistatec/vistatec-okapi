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
===========================================================================*/

package net.sf.okapi.steps.rainbowkit.transifex;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.uidescription.EditorDescription;

public class Parameters extends net.sf.okapi.lib.transifex.Parameters {

	private static final String PROJECTNAME = "projectName";
	
	public Parameters () {
		super();
	}
	
	public Parameters (String initialData) {
		super(initialData);
	}

	@Override
	public void reset () {
		super.reset();
		// Additional fields
		setProjectName("");
	}

	public String getProjectName () {
		return getString(PROJECTNAME);
	}

	public void setProjectName (String projectName) {
		setString(PROJECTNAME, projectName);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = super.getParametersDescription();
		desc.add(PROJECTNAME, "Project name", "Name of the project");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = super.createEditorDescription(paramsDesc);
		desc.addTextInputPart(paramsDesc.get(PROJECTNAME));
		return desc;
	}

}
