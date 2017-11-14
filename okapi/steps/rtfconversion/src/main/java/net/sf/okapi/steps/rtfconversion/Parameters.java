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
============================================================================*/

package net.sf.okapi.steps.rtfconversion;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	public static final int LBTYPE_PLATFORM = 0;
	public static final int LBTYPE_DOS = 1;
	public static final int LBTYPE_UNIX = 2;
	public static final int LBTYPE_MAC = 3;
	
	private static final String LINEBREAK = "lineBreak";
	private static final String BOMONUTF8 = "bomOnUTF8";
	private static final String UPDATEENCODING = "updateEncoding";
	
	public Parameters () {
		super();
	}
	
	public void reset() {
		super.reset();
		setBomOnUTF8(true);
		setLineBreak(System.getProperty("line.separator"));
		if ( getLineBreak() == null ) {
			setLineBreak(Util.LINEBREAK_DOS);
		}
		setUpdateEncoding(true);
	}
	
	public String getLineBreak () {
		return getString(LINEBREAK);
	}
	
	public void setLineBreak (String lineBreak) {
		setString(LINEBREAK, lineBreak);
	}
	
	public boolean getBomOnUTF8 () {
		return getBoolean(BOMONUTF8);
	}
	
	public void setUpdateEncoding (boolean updateEncoding) {
		setBoolean(UPDATEENCODING, updateEncoding);
	}

	public boolean getUpdateEncoding () {
		return getBoolean(UPDATEENCODING);
	}
	
	public void setBomOnUTF8 (boolean bomOnUTF8) {
		setBoolean(BOMONUTF8, bomOnUTF8);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(LINEBREAK, "Type of line-break to use", "Select the type of line-break to use in the output.");		
		desc.add(BOMONUTF8, "Use Byte-Order-Mark for UTF-8 output", null);
		desc.add(UPDATEENCODING, "Try to update the encoding declarations (when detected)", null);
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("RTF Conversion", true, false);	

		desc.addCheckboxPart(paramsDesc.get(BOMONUTF8));
		desc.addCheckboxPart(paramsDesc.get(UPDATEENCODING));

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
		ListSelectionPart lsp = desc.addListSelectionPart(paramsDesc.get(LINEBREAK), values);
		lsp.setChoicesLabels(labels);
		lsp.setListType(ListSelectionPart.LISTTYPE_SIMPLE);

		return desc;
	}

}
