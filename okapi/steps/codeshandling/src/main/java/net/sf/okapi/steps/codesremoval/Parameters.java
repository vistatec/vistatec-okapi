/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.codesremoval;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {
	
	public static final int REMOVECODE_KEEPCONTENT = 0;
	public static final int KEEPCODE_REMOVECONTENT = 1;
	public static final int REMOVECODE_REMOVECONTENT = 2;
	
	private static final String STRIPSOURCE = "stripSource";
	private static final String STRIPTARGET = "stripTarget";
	private static final String MODE = "mode";
	private static final String INCLUDENONTRANSLATABLE = "includeNonTranslatable";
	private static final String REPLACEWITHSPACE = "replaceWithSpace";
	
	public Parameters () {
		super();
	}
	
	public boolean getStripSource () {
		return getBoolean(STRIPSOURCE);
	}
	
	public void setStripSource (boolean stripSource) {
		setBoolean(STRIPSOURCE, stripSource);
	}

	public boolean getStripTarget () {
		return getBoolean(STRIPTARGET);
	}
	
	public void setStripTarget (boolean stripTarget) {
		setBoolean(STRIPTARGET, stripTarget);
	}

	public int getMode () {
		return getInteger(MODE);
	}
	
	public void setMode (int mode) {
		setInteger(MODE, mode);
	}

	public boolean getIncludeNonTranslatable () {
		return getBoolean(INCLUDENONTRANSLATABLE);
	}

	public void setIncludeNonTranslatable (boolean includeNonTranslatable) {
		setBoolean(INCLUDENONTRANSLATABLE, includeNonTranslatable);
	}
	
	public boolean getReplaceWithSpace() {
		return getBoolean(REPLACEWITHSPACE);
	}

	public void setReplaceWithSpace (boolean replaceWithSpace) {
		setBoolean(REPLACEWITHSPACE, replaceWithSpace);
	}

	public void reset() {
		super.reset();
		setStripSource(true);
		setStripTarget(true);
		setMode(REMOVECODE_REMOVECONTENT);
		setIncludeNonTranslatable(true);
		setReplaceWithSpace(false);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(MODE, "What to remove", "Select what parts of the inline codes to remove");
		desc.add(STRIPSOURCE, "Strip codes in the source text", null);
		desc.add(STRIPTARGET, "Strip codes in the target text", null);
		desc.add(INCLUDENONTRANSLATABLE, "Apply to non-translatable text units", null);
		desc.add(REPLACEWITHSPACE, "Replace line break codes with spaces (When removing content AND marker)", null);		
		return desc;
	}
	
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Codes Removal", true, false);

		String[] values = {"0", "1", "2"};
		String[] labels = {
			"Remove code marker, but keep code content  (\"<ph x='1'>[X]</ph>\" ==> \"[X]\")",
			"Remove code content, but keep code marker  (\"<ph x='1'>[X]</ph>\" ==> \"<ph x='1'/>\")",
			"Remove code marker and code content  (\"<ph x='1'>[X]</ph>\" ==> \"\")",
		};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(MODE), values);
		lsp.setChoicesLabels(labels);

		desc.addCheckboxPart(paramDesc.get(REPLACEWITHSPACE));
		desc.addCheckboxPart(paramDesc.get(STRIPSOURCE));
		desc.addCheckboxPart(paramDesc.get(STRIPTARGET));
		desc.addCheckboxPart(paramDesc.get(INCLUDENONTRANSLATABLE));
		
		return desc;
	}

}
