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

package net.sf.okapi.steps.rainbowkit.omegat;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

public class Options extends StringParameters implements IEditorDescriptionProvider {

	private static final String PLACEHOLDERMODE = "placeholderMode"; //$NON-NLS-1$
	private static final String ALLOWSEGMENTATION = "allowSegmentation"; //$NON-NLS-1$
	private static final String INCLUDEPOSTPROCESSINGHOOK = "includePostProcessingHook"; //$NON-NLS-1$
	private static final String CUSTOMPOSTPROCESSINGHOOK = "customPostProcessingHook"; //$NON-NLS-1$
	
	public Options () {
		super();
	}
	
	@Override
	public void reset() {
		super.reset();
		setPlaceholderMode(true);
		setAllowSegmentation(true);
		setIncludePostProcessingHook(true);
		setCustomPostProcessingHook("");
	}
	
	public boolean getPlaceholderMode () {
		return getBoolean(PLACEHOLDERMODE);
	}

	public void setPlaceholderMode (boolean placeholderMode) {
		setBoolean(PLACEHOLDERMODE, placeholderMode);
	}

	public boolean getAllowSegmentation () {
		return getBoolean(ALLOWSEGMENTATION);
	}
	
	public boolean getIncludePostProcessingHook () {
		return getBoolean(INCLUDEPOSTPROCESSINGHOOK);
	}
	
	public String getCustomPostProcessingHook () {
		return getString(CUSTOMPOSTPROCESSINGHOOK);
	}

	public void setAllowSegmentation (boolean allowSegmentation) {
		setBoolean(ALLOWSEGMENTATION, allowSegmentation);
	}
	
	public void setIncludePostProcessingHook (boolean includePostProcessingHook) {
		setBoolean(INCLUDEPOSTPROCESSINGHOOK, includePostProcessingHook);
	}
	
	public void setCustomPostProcessingHook (String customPostProcessingHook) {
		setString(CUSTOMPOSTPROCESSINGHOOK, customPostProcessingHook);
	}
	
	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(PLACEHOLDERMODE, "Use <g></g> and <x/> notation", null);
		desc.add(ALLOWSEGMENTATION, "Allow segmentation in the OmegaT project",
			"Allow or not segmentation in the project. Ignored if there is a segmentation step.");
		desc.add(INCLUDEPOSTPROCESSINGHOOK, "Include post-processing hook",
			"Set up the project so that OmegaT's \"Create Translated Documents\" command will "
		  + "automatically trigger Okapi's Translation Kit Post-Processing pipeline.");
		desc.add(CUSTOMPOSTPROCESSINGHOOK, "Custom hook", "A custom CLI command to be used as the "
			+ "post-processing hook (leave blank to use default hook).");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("OmegaT Project", true, false);
		desc.addCheckboxPart(paramsDesc.get(PLACEHOLDERMODE));
		desc.addCheckboxPart(paramsDesc.get(ALLOWSEGMENTATION));
		desc.addCheckboxPart(paramsDesc.get(INCLUDEPOSTPROCESSINGHOOK));
		desc.addTextInputPart(paramsDesc.get(CUSTOMPOSTPROCESSINGHOOK)).setAllowEmpty(true);
		return desc;
	}

}
