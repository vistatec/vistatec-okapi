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

package net.sf.okapi.steps.xmlcharfixing;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static String REPLACEMENT = "replacement";
	
	public Parameters () {
		super();
	}
	
	public String getReplacement () {
		return getString(REPLACEMENT);
	}

	public void setReplacement (String replacement) {
		setString(REPLACEMENT, replacement);
	}

	public void reset () {
		super.reset();
		setReplacement("_#x%X;");
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(REPLACEMENT, "Replacement string", "Enter a Java-formatted replacement string.");
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("XML Characters Fixing", true, false);
		TextInputPart tip = desc.addTextInputPart(paramDesc.get(REPLACEMENT));
		tip.setAllowEmpty(true);
		return desc;
	}

}
