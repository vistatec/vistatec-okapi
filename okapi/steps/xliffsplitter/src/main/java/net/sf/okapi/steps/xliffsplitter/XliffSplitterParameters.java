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

package net.sf.okapi.steps.xliffsplitter;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(XliffSplitterParameters.class)
public class XliffSplitterParameters extends StringParameters implements IEditorDescriptionProvider {

	public static final String TRANSLATIONTYPE = "translation_type";
	public static final String TRANSLATIONSTATUS = "translation_status";

	private static final String BIGFILE = "bigFile";
	private static final String FILEMARKER = "fileMarker";

	private static final String UPDATESDLTRANSLATIONSTATUS = "updateSDLTranslationStatus";
	private static final String TRANSLATIONTYPEVALUE = "translationTypeValue";
	private static final String TRANSLATIONSTATUSVALUE = "translationStatusValue";

	public XliffSplitterParameters() {
		super();
	}

	public void reset() {
		super.reset();
		setBigFile(false);
		setFileMarker("_PART");
		setUpdateSDLTranslationStatus(false);
		setTranslationTypeValue("manual_translation");
		setTranslationStatusValue("finished");
	}

	public boolean isBigFile() {
		return getBoolean(BIGFILE);
	}

	public void setBigFile(boolean bigFile) {
		setBoolean(BIGFILE, bigFile);
	}

	public String getFileMarker() {
		return getString(FILEMARKER);
	}
	
	public void setFileMarker (String fileMarker) {
		setString(FILEMARKER, fileMarker);
	}
	
	public boolean isUpdateSDLTranslationStatus() {
		return getBoolean(UPDATESDLTRANSLATIONSTATUS);
	}

	public void setUpdateSDLTranslationStatus(boolean updateSDLTranslationStatus) {
		setBoolean(UPDATESDLTRANSLATIONSTATUS, updateSDLTranslationStatus);
	}
	
	public String getTranslationTypeValue () {
		return getString(TRANSLATIONTYPEVALUE);
	}
	
	public void setTranslationTypeValue (String translationTypeValue) {
		setString(TRANSLATIONTYPEVALUE, translationTypeValue);
	}

	public String getTranslationStatusValue () {
		return getString(TRANSLATIONSTATUSVALUE);
	}
	
	public void setTranslationStatusValue (String translationStatusValue) {
		setString(TRANSLATIONSTATUSVALUE, translationStatusValue);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(BIGFILE, "Process big file", null);
		desc.add(FILEMARKER, "File marker", null);
		desc.add(UPDATESDLTRANSLATIONSTATUS, "Update the <iws:status> translation status (WorldServer-specific)", null);
		desc.add(TRANSLATIONTYPEVALUE, String.format("Value for '%s'", TRANSLATIONTYPE),
			String.format("Value to set for the %s attribute.", TRANSLATIONTYPE));
		desc.add(TRANSLATIONSTATUSVALUE, String.format("Value for '%s'", TRANSLATIONSTATUS),
			String.format("Value to set for the %s attribute.", TRANSLATIONSTATUS));
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("XLIFF Splitter", true, false);
		
		CheckboxPart cbp = desc.addCheckboxPart(paramsDesc.get(BIGFILE));
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(FILEMARKER));
		tip.setVertical(false);
		tip.setMasterPart(cbp, true);

		desc.addSeparatorPart();
		
		cbp = desc.addCheckboxPart(paramsDesc.get(UPDATESDLTRANSLATIONSTATUS));
		
		// translation_type
		tip = desc.addTextInputPart(paramsDesc.get(TRANSLATIONTYPEVALUE));
		tip.setVertical(false);
		tip.setMasterPart(cbp, true);
		
		// translation_status
		tip = desc.addTextInputPart(paramsDesc.get(TRANSLATIONSTATUSVALUE));
		tip.setVertical(false);
		tip.setMasterPart(cbp, true);
		
		return desc;
	}

}