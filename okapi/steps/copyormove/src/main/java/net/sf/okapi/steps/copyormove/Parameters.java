/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.copyormove;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String COPYOPTION = "copyOption";
	private static final String MOVE = "move";
	
	public static final String COPYOPTION_OVERWRITE = "overwrite";
	public static final String COPYOPTION_BACKUP = "backup";
	public static final String COPYOPTION_SKIP = "skip";
	
	public Parameters() {
		super();
	}

	@Override
	public void reset() {
		super.reset();
		setCopyOption(COPYOPTION_OVERWRITE);
		setMove(false);
	}

	public boolean isMove() {
		return getBoolean(MOVE);
	}

	public void setMove(boolean move) {
		setBoolean(MOVE, move);
	}

	public String getCopyOption() {
		return getString(COPYOPTION);
	}
	
	public void setCopyOption(String copyOption) {
		setString(COPYOPTION, copyOption);			
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(COPYOPTION, "Choose copy protection method:", null);
		desc.add(MOVE, "Move files?", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Copy Or Move", true, false);
		String[] values = {COPYOPTION_OVERWRITE, COPYOPTION_BACKUP, COPYOPTION_SKIP};
		String[] labels = {"Overwrite existing files", "Backup existing files", "Skip copy/move"};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(COPYOPTION), values);
		lsp.setChoicesLabels(labels);
		lsp.setListType(ListSelectionPart.LISTTYPE_DROPDOWN);
		desc.addCheckboxPart(paramDesc.get(MOVE));
		return desc;
	}
}
