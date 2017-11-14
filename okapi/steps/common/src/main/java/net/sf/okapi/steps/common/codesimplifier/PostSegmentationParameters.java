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

package net.sf.okapi.steps.common.codesimplifier;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(PostSegmentationParameters.class)
public class PostSegmentationParameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String REMOVE_LEADING_TRAILING_CODES = "removeLeadingTrailingCodes";
	private static final String MERGE_CODES = "mergeCodes";

	public PostSegmentationParameters() {
		super();
	}
	
	@Override
	public void reset() {
		super.reset();
		setRemoveLeadingTrailingCodes(true);
		setMergeCodes(true);
	}

	public void setRemoveLeadingTrailingCodes(boolean removeLeadingTrailingCodes) {
		setBoolean(REMOVE_LEADING_TRAILING_CODES, removeLeadingTrailingCodes);
	}

	public boolean getRemoveLeadingTrailingCodes() {
		return getBoolean(REMOVE_LEADING_TRAILING_CODES);
	}
	
	public void setMergeCodes(boolean mergeCodes) {
		setBoolean(MERGE_CODES, mergeCodes);
	}

	public boolean getMergeCodes() {
		return getBoolean(MERGE_CODES);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(REMOVE_LEADING_TRAILING_CODES,
			"Remove leading and trailing codes",
			"Removes leading and trailing codes from the segment and place them in the inter-segment part.");
		desc.add(MERGE_CODES,
				"Merge codes",
				"Merges adjacent codes (leading/trailing codes are removed first (if option is enabled)).");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Post-segmentation Inline Codes Simplifier", true, false);		
		desc.addCheckboxPart(paramsDesc.get(REMOVE_LEADING_TRAILING_CODES));
		desc.addCheckboxPart(paramsDesc.get(MERGE_CODES));
		return desc;
	}
}
