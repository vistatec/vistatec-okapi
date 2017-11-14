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

package net.sf.okapi.steps.common.createtarget;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String COPYCONTENT = "copyContent";
	private static final String COPYPROPERTIES = "copyProperties";
	private static final String OVERWRITEEXISTING = "overwriteExisting";
	private static final String CREATEONNONTRANSLATABLE = "createOnNonTranslatable";
	
	public Parameters() {
		super();
	}

	public void reset() {
		super.reset();
		setBoolean(COPYPROPERTIES, true);
		setBoolean(COPYCONTENT, true);
		setBoolean(OVERWRITEEXISTING, false);
		setBoolean(CREATEONNONTRANSLATABLE, true);
	}

	public boolean isCopyProperties() {
		return getBoolean(COPYPROPERTIES);
	}

	public void setCopyProperties(boolean copyProperties) {
		setBoolean(COPYPROPERTIES, copyProperties);
	}

	public boolean isCopyContent() {
		return getBoolean(COPYCONTENT);
	}

	public void setCopyContent(boolean copyContent) {
		setBoolean(COPYCONTENT, copyContent);
	}

	public boolean isOverwriteExisting() {
		return getBoolean(OVERWRITEEXISTING);
	}

	public void setOverwriteExisting(boolean overwriteExisting) {
		setBoolean(OVERWRITEEXISTING, overwriteExisting);
	}

	public boolean isCreateOnNonTranslatable() {
		return getBoolean(CREATEONNONTRANSLATABLE);
	}

	public void setCreateOnNonTranslatable(boolean createOnNonTranslatable) {
		setBoolean(CREATEONNONTRANSLATABLE, createOnNonTranslatable);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(COPYCONTENT, "Copy the source content to the target", null);
		desc.add(COPYPROPERTIES, "Copy the source properties to the target", null);
		desc.add(OVERWRITEEXISTING, "Overwrite the current target content", null);
		desc.add(CREATEONNONTRANSLATABLE, "Creates target for non-translatable text units", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Create Target", true, false);		
		desc.addCheckboxPart(paramsDesc.get(COPYCONTENT));
		desc.addCheckboxPart(paramsDesc.get(COPYPROPERTIES));
		desc.addSeparatorPart();
		desc.addCheckboxPart(paramsDesc.get(OVERWRITEEXISTING));
		desc.addSeparatorPart();
		desc.addCheckboxPart(paramsDesc.get(CREATEONNONTRANSLATABLE));
		return desc;
	}
}
