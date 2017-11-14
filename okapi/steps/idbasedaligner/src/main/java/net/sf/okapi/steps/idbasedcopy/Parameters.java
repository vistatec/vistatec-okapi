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

package net.sf.okapi.steps.idbasedcopy;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String MARKASTRANSLATENO = "markAsTranslateNo";
	private static final String MARKASAPPROVED = "markAsApproved";
	
	public Parameters () {
		super();
	}
	
	public Parameters (String initialData) {
		super(initialData);
	}

	@Override
	public void reset () {
		super.reset();
		// Default
		setMarkAsTranslateNo(false);
		setMarkAsApproved(true);
	}

	public boolean getMarkAsTranslateNo () {
		return getBoolean(MARKASTRANSLATENO);
	}

	public void setMarkAsTranslateNo (boolean markAsTranslateNo) {
		setBoolean(MARKASTRANSLATENO, markAsTranslateNo);
	}

	public boolean getMarkAsApproved () {
		return getBoolean(MARKASAPPROVED);
	}

	public void setMarkAsApproved (boolean markAsApproved) {
		setBoolean(MARKASAPPROVED, markAsApproved);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(MARKASTRANSLATENO, "Set the text unit as non-translatable", null);
		desc.add(MARKASAPPROVED, "Set the target property 'approved' to 'yes'", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Id-Based Copy Settings");
		desc.addTextLabelPart("If the text unit has a match:");
		desc.addCheckboxPart(paramsDesc.get(MARKASTRANSLATENO));
		desc.addCheckboxPart(paramsDesc.get(MARKASAPPROVED));
		return desc;
	}

}
