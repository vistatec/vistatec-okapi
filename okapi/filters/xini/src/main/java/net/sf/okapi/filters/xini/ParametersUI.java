package net.sf.okapi.filters.xini;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class ParametersUI implements IEditorDescriptionProvider {

	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("XINI Filter Parameters", true, false);
		desc.addCheckboxPart(paramDesc.get(Parameters.USE_OKAPI_SEGMENTATION));
		return desc;
	}

}
