/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.filters.tmx;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.encoder.XMLEncoder;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;

@EditorFor(Parameters.class)
public class ParametersUI implements IEditorDescriptionProvider {

	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("TMX Filter Parameters", true, false);
		desc.addCheckboxPart(paramDesc.get(Parameters.PROCESSALLTARGETS));
		desc.addCheckboxPart(paramDesc.get(Parameters.CONSOLIDATEDPSKELETON));
		desc.addCheckboxPart(paramDesc.get(Parameters.EXITONINVALID));
		
		String[] values = {String.valueOf(TmxFilter.SEGTYPE_SENTENCE),
			String.valueOf(TmxFilter.SEGTYPE_PARA),
			String.valueOf(TmxFilter.SEGTYPE_OR_SENTENCE),
			String.valueOf(TmxFilter.SEGTYPE_OR_PARA)};
		String[] labels = {
			"Always creates the segment (ignore segtype attribute)",
			"Never creates the segment (ignore segtype attribute)",
			"Creates the segment if segtype is 'sentence' or is undefined",
			"Creates the segment only if segtype is 'sentence'"};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(Parameters.SEGTYPE), values);
		lsp.setChoicesLabels(labels);
		
		desc.addSeparatorPart();	
		desc.addCheckboxPart(paramDesc.get(XMLEncoder.ESCAPEGT));

		desc.addSeparatorPart();
		desc.addTextInputPart(paramDesc.get(Parameters.PROPVALUESEP));

		return desc;
	}

}
