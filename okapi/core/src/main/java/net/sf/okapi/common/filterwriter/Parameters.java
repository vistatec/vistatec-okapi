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

package net.sf.okapi.common.filterwriter;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {	
	private static final String WRITE_ALL_PROPERTIES_AS_ATTRIBUTES = "writeAllPropertiesAsAttributes";
	private static final String ENABLE_DUPLICATE_PROPS = "enableDuplicateProps";
	private static final String PROP_VALUE_SEP = "PROP_VALUE_SEP";

	private static final String GENERATE_TU_UUID = "GENERATE_TU_UUID";
	private static final String NORMALIZE_INLINE_IDS = "NORMALIZE_INLINE_IDS";
	
	public Parameters() {
		super();
	}
	
	public void reset() {
		super.reset();
		setWriteAllPropertiesAsAttributes(false);
		setEnableDuplicateProps(false);
		setPropValueSep(", ");	
		setGenerateUUID(false);
		setNormalizeInlineIDs(false);
	}

	public boolean isWriteAllPropertiesAsAttributes() {
		return getBoolean(WRITE_ALL_PROPERTIES_AS_ATTRIBUTES);
	}
	
	public void setEnableDuplicateProps(boolean duplicateProps) {
		setBoolean(ENABLE_DUPLICATE_PROPS, duplicateProps);
	}
	
	public boolean isEnableDuplicateProps() {
		return getBoolean(ENABLE_DUPLICATE_PROPS);
	}
	
	public void setWriteAllPropertiesAsAttributes(boolean writeAllPropertiesAsAttributes) {
		setBoolean(WRITE_ALL_PROPERTIES_AS_ATTRIBUTES, writeAllPropertiesAsAttributes);
	}
	
	public String getPropValueSep () {
		return getString(PROP_VALUE_SEP);
	}
	
	public void setPropValueSep (String sep) {
		setString(PROP_VALUE_SEP, sep);
	}
	
	public boolean isGenerateUUID() {
		return getBoolean(GENERATE_TU_UUID);
	}
	
	public void setGenerateUUID(boolean generateUUID) {
		setBoolean(GENERATE_TU_UUID, generateUUID);
	}
	
	public boolean isNormalizeInlineIDs() {
		return getBoolean(NORMALIZE_INLINE_IDS);
	}
	
	public void setNormalizeInlineIDs(boolean normalizeInlineIDs) {
		setBoolean(NORMALIZE_INLINE_IDS, normalizeInlineIDs);
	}
	
	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(WRITE_ALL_PROPERTIES_AS_ATTRIBUTES, 
				"Write all text unit level properties as TMX attributes", 
				null);
		desc.add(ENABLE_DUPLICATE_PROPS, 
				"Expand out duplicate property values into individual elements", 
				null);
		desc.add(PROP_VALUE_SEP, 
				"Seperator string used to delimit duplicate property values", 
				null);
		desc.add(GENERATE_TU_UUID, 
				"Generate a UUID instead of an auto generated id or existing name", 
				null);
		desc.add(NORMALIZE_INLINE_IDS, 
				"Start inline code ids at 1 for each segment", 
				null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("TMX Filter Writer Options", true, false);
		desc.addCheckboxPart(paramsDesc.get(WRITE_ALL_PROPERTIES_AS_ATTRIBUTES));
		desc.addCheckboxPart(paramsDesc.get(ENABLE_DUPLICATE_PROPS));
		desc.addTextInputPart(paramsDesc.get(PROP_VALUE_SEP));
		desc.addCheckboxPart(paramsDesc.get(GENERATE_TU_UUID));
		desc.addCheckboxPart(paramsDesc.get(NORMALIZE_INLINE_IDS));
		return desc;
	}
}
