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

package net.sf.okapi.filters.transtable;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String ALLOWSEGMENTS = "allowSegments";

	public Parameters () {
		super();
	}
	
	public boolean getAllowSegments () {
		return getBoolean(ALLOWSEGMENTS);
	}
	
	public void setAllowSegments (boolean allowSegments) {
		setBoolean(ALLOWSEGMENTS, allowSegments);
	}

	public void reset () {
		super.reset();
		setAllowSegments(true);
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Translation Table Parameters", true, false);
		desc.addCheckboxPart(paramDesc.get(ALLOWSEGMENTS));
		return desc;
	}
	
	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(ALLOWSEGMENTS, "Allow segmentation (one row per segment)", null);
		return desc;
	}

}
