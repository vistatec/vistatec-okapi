/*===========================================================================
  Copyright (C) 2008 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.steps.linebreakconversion;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String LINEBREAK = "lineBreak";

	public String getLineBreak () {
		return getString(LINEBREAK);
	}

	public void setLineBreak (String lineBreak) {
		setString(LINEBREAK, lineBreak);
	}

	public Parameters () {
		super();
	}
	
	public void reset() {
		super.reset();
		setLineBreak(System.getProperty("line.separator"));
		if ( getLineBreak() == null ) {
			setLineBreak(Util.LINEBREAK_DOS);
		}
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(LINEBREAK, "Convert line-breaks to the following type", "Select the new type of line-break for the output.");
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Line-Break Conversion", true, false);

		String[] values = {
			Util.LINEBREAK_DOS,
			Util.LINEBREAK_UNIX,
			Util.LINEBREAK_MAC
		};
		String[] labels = {
			"DOS/Windows (Carriage-Return + Line-Feed, \\r\\n, 0x0D+0x0A)",
			"Unix/Linux (Line-Feed, \\n, 0x0A)",
			"Macintosh (Carriage-Return, \\r, 0x0D)"
		};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(LINEBREAK), values);
		lsp.setChoicesLabels(labels);
		lsp.setListType(ListSelectionPart.LISTTYPE_SIMPLE);

		return desc;
	}
	
}
