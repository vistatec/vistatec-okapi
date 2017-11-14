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

package net.sf.okapi.connectors.simpletm;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.PathInputPart;

public class ParametersUI implements IEditorDescriptionProvider {

	public EditorDescription createEditorDescription(ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("SimpleTM Connector Settings", true, false);

		PathInputPart part = desc.addPathInputPart(paramDesc.get(Parameters.DBPATH), "Database File", false);
		part.setBrowseFilters(String.format("Database Files (*%s)\tAll Files (*.*)", Parameters.DB_EXTENSION),
			String.format("*%s\t*.*", Parameters.DB_EXTENSION));
		
		desc.addCheckboxPart(paramDesc.get(Parameters.PENALIZESOURCEWITHDIFFERENTCODES));
		desc.addCheckboxPart(paramDesc.get(Parameters.PENALIZETARGETWITHDIFFERENTCODES));
		
		return desc;
	}

}
