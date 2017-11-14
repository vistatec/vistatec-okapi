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

package net.sf.okapi.filters.mosestext;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(FilterWriterParameters.class)
public class FilterWriterParameters extends StringParameters implements IEditorDescriptionProvider {

	public static final String NAME = "Moses InlineText Extraction";
	
	private static final String SOURCEANDTARGET = "sourceAndTarget";
	
	public FilterWriterParameters () {
		super();
	}
	
	public boolean getSourceAndTarget () {
		return getBoolean(SOURCEANDTARGET);
	}

	public void setSourceAndTarget (boolean sourceAndTarget) {
		setBoolean(SOURCEANDTARGET, sourceAndTarget);
	}

	public void reset() {
		super.reset();
		setSourceAndTarget(false);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(SOURCEANDTARGET, "Create two outputs: one for the source and one for the target", null);
		return desc;
	}

	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription(NAME, true, false);

		desc.addCheckboxPart(paramDesc.get(SOURCEANDTARGET));
		
		return desc;
	}

}
