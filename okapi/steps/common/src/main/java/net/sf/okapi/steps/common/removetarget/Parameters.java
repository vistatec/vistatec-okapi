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

package net.sf.okapi.steps.common.removetarget;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {
	private static final String TUS_FOR_TARGET_REMOVAL = "tusForTargetRemoval";
	private static final String TARGET_LOCALES_TO_KEEP = "targetLocalesToKeep";
	private static final String FILTER_BASED_ON_IDS = "filterBasedOnIds";
	private static final String REMOVE_TU_IF_NO_TARGET = "removeTUIfNoTarget";
	
	public Parameters() {
		super();
	}
	
	public void reset() {
		super.reset();
		setTusForTargetRemoval("");
		setTargetLocalesToKeep("");
		setFilterBasedOnIds(true);
		setRemoveTUIfNoTarget(false);
	}

	public void setTusForTargetRemoval(String tusForTargetRemoval) {
		setString(TUS_FOR_TARGET_REMOVAL, tusForTargetRemoval);
	}

	public String getTusForTargetRemoval() {
		return getString(TUS_FOR_TARGET_REMOVAL);
	}

	public void setTargetLocalesToKeep(String targetLocalesToKeep) {
		setString(TARGET_LOCALES_TO_KEEP, targetLocalesToKeep);
	}

	public String getTargetLocalesToKeep() {
		return getString(TARGET_LOCALES_TO_KEEP);
	}

	public boolean isFilterBasedOnIds() {
		return getBoolean(FILTER_BASED_ON_IDS);
	}
	
	public void setFilterBasedOnIds(boolean filterBasedOnIds) {
		setBoolean(FILTER_BASED_ON_IDS, filterBasedOnIds);
	}

	public boolean isRemoveTUIfNoTarget() {
		return getBoolean(REMOVE_TU_IF_NO_TARGET);
	}
	
	public void setRemoveTUIfNoTarget(boolean removeTUIfNoTarget) {
		setBoolean(REMOVE_TU_IF_NO_TARGET, removeTUIfNoTarget);
	}
	
	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(TUS_FOR_TARGET_REMOVAL, 
				"Comma-delimited list of ids of the text units where targets are to be removed (empty - remove all targets)", 
				null);
		desc.add(TARGET_LOCALES_TO_KEEP, 
				"Comma-delimited list of locales of the text units of targets that should be kept (empty - keep all targets)", 
				null);
		desc.add(FILTER_BASED_ON_IDS, 
				"If true filter on ID's, if false filter on locales (you cannot filter on both)", 
				null);
		desc.add(REMOVE_TU_IF_NO_TARGET, 
				"If true remove the Text Unit if it has no remaining targets, if false do nothing", 
				null);

		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Remove Target Options", true, false);
		desc.addCheckboxPart(paramsDesc.get(FILTER_BASED_ON_IDS));
		desc.addCheckboxPart(paramsDesc.get(REMOVE_TU_IF_NO_TARGET));
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(TUS_FOR_TARGET_REMOVAL));
		tip.setAllowEmpty(true);
		tip = desc.addTextInputPart(paramsDesc.get(TARGET_LOCALES_TO_KEEP));
		tip.setAllowEmpty(true);
		return desc;
	}

}
