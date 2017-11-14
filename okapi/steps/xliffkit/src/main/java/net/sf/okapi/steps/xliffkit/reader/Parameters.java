/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffkit.reader;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	static final String GENERATE_TARGETS = "generateTargets"; //$NON-NLS-1$
	static final String USE_APPROVED_ONLY = "useApprovedOnly"; //$NON-NLS-1$
	static final String UPDATE_APPROVED_FLAG = "updateApprovedFlag"; //$NON-NLS-1$
	static final String GROUP_BY_PACKAGE_PATH = "groupByPackagePath"; //$NON-NLS-1$
	
	public Parameters () {
		super();
	}
		
	@Override
	public void reset() {
		super.reset();
		setGenerateTargets(true);
		setUseApprovedOnly(false);
		setUpdateApprovedFlag(true);
		setGroupByPackagePath(true);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);		
		desc.add(GENERATE_TARGETS, "Generate target files in the output directory", "Generate targets");		
		desc.add(USE_APPROVED_ONLY, "Update target only if translation was approved", "Use only approved translation");
		desc.add(UPDATE_APPROVED_FLAG, "Update the approved flag if translation was approved", "Update approved flag");
		desc.add(GROUP_BY_PACKAGE_PATH, "Group target files by their paths in the package", "Group targets");
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription(
			ParametersDescription parametersDescription) {
		EditorDescription desc = new EditorDescription("XLIFF Kit Reader Options", true, false);		
		desc.addCheckboxPart(parametersDescription.get(GENERATE_TARGETS));		
		desc.addCheckboxPart(parametersDescription.get(USE_APPROVED_ONLY));
		desc.addCheckboxPart(parametersDescription.get(UPDATE_APPROVED_FLAG));
		desc.addCheckboxPart(parametersDescription.get(GROUP_BY_PACKAGE_PATH));
		return desc;
	}

	public void setGenerateTargets(boolean generateTargets) {
		setBoolean(GENERATE_TARGETS, generateTargets);
	}

	public boolean isGenerateTargets() {
		return getBoolean(GENERATE_TARGETS);
	}

	public void setUseApprovedOnly(boolean useApprovedOnly) {
		setBoolean(USE_APPROVED_ONLY, useApprovedOnly);
	}

	public boolean isUseApprovedOnly() {
		return getBoolean(USE_APPROVED_ONLY);
	}

	public void setUpdateApprovedFlag(boolean updateApprovedFlag) {
		setBoolean(UPDATE_APPROVED_FLAG, updateApprovedFlag);
	}

	public boolean isUpdateApprovedFlag() {
		return getBoolean(UPDATE_APPROVED_FLAG);
	}

	public boolean isGroupByPackagePath() {
		return getBoolean(GROUP_BY_PACKAGE_PATH);
	}

	public void setGroupByPackagePath(boolean groupByPackagePath) {
		setBoolean(GROUP_BY_PACKAGE_PATH, groupByPackagePath);
	}

}
