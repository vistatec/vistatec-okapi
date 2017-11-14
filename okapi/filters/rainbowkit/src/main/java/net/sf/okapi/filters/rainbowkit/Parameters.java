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

package net.sf.okapi.filters.rainbowkit;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String OPENMANIFEST = "openManifest";
	
	public Parameters () {
		super();
	}
	
	@Override
	public void reset () {
		super.reset();
		setOpenManifest(true);
	}
	
	public void setOpenManifest (boolean openManifest) {
		setBoolean(OPENMANIFEST, openManifest);
	}
	
	public boolean getOpenManifest () {
		return getBoolean(OPENMANIFEST);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(OPENMANIFEST, "Open the manifest file before processing", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Rainbow Translation Kit Parameters", true, false);
		desc.addCheckboxPart(paramDesc.get(OPENMANIFEST));
		return desc;
	}
}
