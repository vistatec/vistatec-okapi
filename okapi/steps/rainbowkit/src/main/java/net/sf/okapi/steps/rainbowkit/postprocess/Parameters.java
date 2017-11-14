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

package net.sf.okapi.steps.rainbowkit.postprocess;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	static final String PRESERVESEGMENTATION = "preserveSegmentation"; //$NON-NLS-1$
	static final String RETURNRAWDOCUMENT = "returnRawDocument"; //$NON-NLS-1$
	static final String FORCETARGETLOCALE = "forceTargetLocale"; //$NON-NLS-1$
	static final String OVERRIDEOUTPUTPATH = "overrideOutputPath"; //$NON-NLS-1$

	public Parameters () {
		super();
	}
	
	@Override
	public void reset () {
		super.reset();
		// Most of the times, this is the last step of the pipeline
		// so preserving the segmentation is not needed
		setPreserveSegmentation(false);
		setReturnRawDocument(false);
		setForceTargetLocale(false);
		setOverrideOutputPath("");
	}

	public boolean getPreserveSegmentation () {
		return getBoolean(PRESERVESEGMENTATION);
	}

	public void setPreserveSegmentation (boolean preserveSegmentation) {
		setBoolean(PRESERVESEGMENTATION, preserveSegmentation);
	}

	public boolean getReturnRawDocument () {
		return getBoolean(RETURNRAWDOCUMENT);
	}

	public void setReturnRawDocument (boolean returnRawDocument) {
		setBoolean(RETURNRAWDOCUMENT, returnRawDocument);
	}

	public boolean getForceTargetLocale () {
		return getBoolean(FORCETARGETLOCALE);
	}
	
	public void setForceTargetLocale (boolean forceTargetLocale) {
		setBoolean(FORCETARGETLOCALE, forceTargetLocale);
	}

	public String getOverrideOutputPath() {
		return getString(OVERRIDEOUTPUTPATH);
	}

	public void setOverrideOutputPath(String overrideOutputPath) {
		setString(OVERRIDEOUTPUTPATH, overrideOutputPath);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(PRESERVESEGMENTATION, "Preserve the segmentation for the next steps", null);
		desc.add(RETURNRAWDOCUMENT, "Return raw documents instead of filter events", null);
		desc.add(FORCETARGETLOCALE, "Specify the target locale from the tool instead of the manifest", null);
		desc.add(OVERRIDEOUTPUTPATH, "Override the output path (leave empty to use the path from the manifest)", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription(MergingStep.NAME, true, false);
		desc.addCheckboxPart(paramDesc.get(PRESERVESEGMENTATION));
		desc.addCheckboxPart(paramDesc.get(RETURNRAWDOCUMENT));
		desc.addCheckboxPart(paramDesc.get(FORCETARGETLOCALE));
		TextInputPart tip = desc.addTextInputPart(paramDesc.get(OVERRIDEOUTPUTPATH));
		tip.setAllowEmpty(true);
		return desc;
	}

}
