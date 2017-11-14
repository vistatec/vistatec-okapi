/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tmimport;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String TMDIRECTORY = "tmDirectory";
	private static final String OVERWRITESAMESOURCE = "overwriteSameSource";
	
	public Parameters () {
		super();
	}

	public String getTmDirectory () {
		return getString(TMDIRECTORY);
	}

	public void setTmDirectory (String tmDirectory) {
		setString(TMDIRECTORY, tmDirectory);
	}

	public boolean getOverwriteSameSource () {
		return getBoolean(OVERWRITESAMESOURCE);
	}
	
	public void setOverwriteSameSource (boolean overwriteSameSource) {
		setBoolean(OVERWRITESAMESOURCE, overwriteSameSource);
	}
	
	@Override
	public void reset () {
		super.reset();
		setTmDirectory("");
		setOverwriteSameSource(false);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(TMDIRECTORY, "Directory of the TM where to import",
			"Full path of directory of the TM where to import");
		desc.add(OVERWRITESAMESOURCE, "Overwrite if source is the same", null);
		return desc;
	}

	public EditorDescription createEditorDescription(ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("TM Import", true, false);
		desc.addFolderInputPart(paramDesc.get(TMDIRECTORY), "TM Directory");
		desc.addCheckboxPart(paramDesc.get(OVERWRITESAMESOURCE));
		return desc;
	}

}
