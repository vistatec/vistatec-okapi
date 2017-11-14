/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.ttx;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.encoder.XMLEncoder;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider, ISimplifierRulesParameters {

	public static final int MODE_AUTO = 0;
	public static final int MODE_EXISTINGSEGMENTS = 1;
	public static final int MODE_ALL = 2;
	
	private final static String SEGMENTMODE = "segmentMode";
	
	public Parameters () {
		super();
	}
	
	public boolean getEscapeGT () {
		return getBoolean(XMLEncoder.ESCAPEGT);
	}

	public void setEscapeGT (boolean escapeGT) {
		setBoolean(XMLEncoder.ESCAPEGT, escapeGT);
	}
	
	public int getSegmentMode () {
		return getInteger(SEGMENTMODE);
	}
	
	public void setSegmentMode (int segmentMode) {
		setInteger(SEGMENTMODE, segmentMode);
	}
	
	@Override
	public String getSimplifierRules() {
		return getString(SIMPLIFIERRULES);
	}

	@Override
	public void setSimplifierRules(String rules) {
		setString(SIMPLIFIERRULES, rules);		
	}

	@Override
	public void validateSimplifierRules() throws ParseException {
		SimplifierRules r = new SimplifierRules(getSimplifierRules(), new Code());
		r.parse();
	}

	public void reset () {
		super.reset();
		setEscapeGT(false);
		setSegmentMode(0);
		setSimplifierRules(null);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(XMLEncoder.ESCAPEGT, "Escape the greater-than characters in output", null);
		desc.add(SEGMENTMODE, "Extraction mode", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("TTX Filter Parameters", true, false);
		
		String[] values = {String.valueOf(MODE_AUTO),
			String.valueOf(MODE_EXISTINGSEGMENTS),
			String.valueOf(MODE_ALL)};
		String[] labels = {
			"Auto-detect existing segments (If found: extract only those, otherwise extract all)",
			"Extract only existing segments",
			"Extract all (existing segments and un-segmented text parts)",
		};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(SEGMENTMODE), values);
		lsp.setChoicesLabels(labels);
		
		desc.addCheckboxPart(paramDesc.get(XMLEncoder.ESCAPEGT));
		return desc;
	}

}
