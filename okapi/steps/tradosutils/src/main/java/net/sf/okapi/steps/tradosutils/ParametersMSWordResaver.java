/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tradosutils;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;

@EditorFor(ParametersMSWordResaver.class)
public class ParametersMSWordResaver extends StringParameters implements IEditorDescriptionProvider {

	private static final String FORMAT = "format";
	private static final String SENDNEW = "sendNew";
	private static final String wdFormatDocumentDefault = "16";
	private static final String wdFormatRTF = "6";
	private static final String wdFormatDocument = "0";
	private static final String wdFormatFilteredHTML = "10";
	private static final String wdFormatHTML = "8";

	public int getFormat() {
		return getInteger(FORMAT);
	}

	public void setFormat (int format) {
		setInteger(FORMAT, format);
	}
	
	public boolean getSendNew () {
		return getBoolean(SENDNEW);
	}
	
	public void setSendNew (boolean sendNew) {
		setBoolean(SENDNEW, sendNew);
	}
	
	public ParametersMSWordResaver () {
		super();
	}
	
	public void reset() {
		super.reset();
		setFormat(6);
		setSendNew(true);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(FORMAT, "Format to save as:", null);
		desc.add(SENDNEW, "Send resaved document to the next step", null);
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("MS Word Resaver", true, false);

		String[] labels = {
				"Rich Text Format (RTF)",
				"Microsoft Office Word Format (DOC)",
				"Word Default Document Format (DOCX for Word 2007)",
				"Filtered HTML Format",
				"Standard HTML Format"
			};
		
		String[] values = {
				wdFormatRTF,
				wdFormatDocument,
				wdFormatDocumentDefault,
				wdFormatFilteredHTML,
				wdFormatHTML
			};
		
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(FORMAT), values);
		lsp.setChoicesLabels(labels);
		lsp.setListType(ListSelectionPart.LISTTYPE_DROPDOWN);
		lsp.setVertical(false);
		
		CheckboxPart cbp = desc.addCheckboxPart(paramDesc.get(SENDNEW));
		cbp.setVertical(true);
		
		return desc;
	}
	
}
