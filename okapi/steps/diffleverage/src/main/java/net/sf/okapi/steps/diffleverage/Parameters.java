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

package net.sf.okapi.steps.diffleverage;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.SpinInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {
	
	private static final String FUZZYTHRESHOLD = "fuzzyThreshold";
	private static final String CODESENSITIVE = "codesensitive";
	private static final String DIFFONLY = "diffOnly";
	private static final String COPYTOTARGET = "copyToTarget";
	
	public Parameters() {
		super();
	}
	
	@Override
	public void reset() {
		super.reset();
		// default is exact match
		setFuzzyThreshold(100);
		setCodesensitive(true);
		setDiffOnly(false);
		setCopyToTarget(false);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add("fuzzyThreshold", "Leverage only if the match is equal or above this score", "Fuzzy Thresholds are between 1 and 100. A score of 100 emans exact match (codes and text) only");		
		desc.add("codesensitive", "Include inline codes in the comparison", "Use codes to compare contents");
		desc.add("diffOnly", "Diff only and mark the TextUnit as matched", "Diff only and do not copy the match or create a leverage annotation");
		desc.add("copyToTarget", 
				"Copy to/over the target? (WARNING: Copied target will not be segmented!)", "Copy to/over the target (a leverage annotation " +
				"will still be created). WARNING: Copied target will not be segmented and any exisiting target will be lost.");
		//desc.add("diffOnSentences", "Diff on sentences or paragraphs (if sentences then source and target must be aligned)?", "Diff On Sentences?");
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Diff Leverage", true, false);	
		SpinInputPart sip = desc.addSpinInputPart(paramsDesc.get("fuzzyThreshold"));
		sip.setRange(1, 100);
		sip.setVertical(false);
		desc.addCheckboxPart(paramsDesc.get("codesensitive"));
		desc.addSeparatorPart();
		desc.addCheckboxPart(paramsDesc.get("diffOnly"));
		desc.addCheckboxPart(paramsDesc.get("copyToTarget"));
		//desc.addCheckboxPart(paramsDesc.get("diffOnSentences"));
		return desc;
	}
	
	public int getFuzzyThreshold() {
		return getInteger(FUZZYTHRESHOLD);
	}
	
	public void setFuzzyThreshold(int fuzzyThreshold) {
		setInteger(FUZZYTHRESHOLD, fuzzyThreshold);
	}

	public boolean isCodesensitive() {
		return getBoolean(CODESENSITIVE);
	}

	public void setCodesensitive(boolean codesensitive) {
		setBoolean(CODESENSITIVE, codesensitive);
	}

	public boolean isDiffOnly() {
		return getBoolean(DIFFONLY);
	}

	public void setDiffOnly(boolean diffOnly) {
		setBoolean(DIFFONLY, diffOnly);
	}

	public void setCopyToTarget(boolean copyToTarget) {
		setBoolean(COPYTOTARGET, copyToTarget);
	}

	public boolean isCopyToTarget() {
		return getBoolean(COPYTOTARGET);
	}

//	public boolean isDiffOnSentences() {
//		return diffOnSentences;
//	}
//
//	public void setDiffOnSentences(boolean diffOnSentences) {
//		this.diffOnSentences = diffOnSentences;
//	}
}
