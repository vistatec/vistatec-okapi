/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.steps.paraaligner;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {
	private static final String OUTPUT_ONE_TO_ONE_MATCHES_ONLY = "outputOneToOneMatchesOnly";
	private static final String USE_SKELETON_ALIGNMENT = "useSkeletonAlignment";
	
	public Parameters() {
		super();
	}

	public boolean isOutputOneToOneMatchesOnly() {
		return getBoolean(OUTPUT_ONE_TO_ONE_MATCHES_ONLY);
	}

	public void setOutputOneToOneMatchesOnly(boolean outputOneToOneMatchesOnly) {
		setBoolean(OUTPUT_ONE_TO_ONE_MATCHES_ONLY, outputOneToOneMatchesOnly);
	}
	
	public boolean isUseSkeletonAlignment() {
		return getBoolean(USE_SKELETON_ALIGNMENT);
	}

	public void setUseSkeletonAlignment(boolean useSkeletonAlignment) {
		setBoolean(USE_SKELETON_ALIGNMENT, useSkeletonAlignment);
	}

	@Override
	public void reset() {
		super.reset();
		setOutputOneToOneMatchesOnly(true);
		setUseSkeletonAlignment(false);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(OUTPUT_ONE_TO_ONE_MATCHES_ONLY, "Output 1-1 Matches Only?", 
			"Ouput only 1-1 aligned paragraphs?");		
		desc.add(USE_SKELETON_ALIGNMENT, "Use Skeleton Alignment? (Experimental)", 
				"Use Skeleton alignment? (Experimental)");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Paragraph Aligner", true, false);		
		desc.addCheckboxPart(paramsDesc.get(OUTPUT_ONE_TO_ONE_MATCHES_ONLY));
		desc.addCheckboxPart(paramsDesc.get(USE_SKELETON_ALIGNMENT));
		return desc;
	}
}
