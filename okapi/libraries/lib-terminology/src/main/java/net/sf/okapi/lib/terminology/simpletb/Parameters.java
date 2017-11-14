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
===========================================================================*/

package net.sf.okapi.lib.terminology.simpletb;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String GLOSSARYPATH = "glossaryPath";
	private static final String SOURCELOCALE = "sourceLocale";
	private static final String TARGETLOCALE = "targetLocale";

	public Parameters () {
		super();
	}
	
	public String getGlossaryPath () {
		return getString(GLOSSARYPATH);
	}

	public void setGlossaryPath (String glossaryPath) {
		setString(GLOSSARYPATH, glossaryPath);
	}

	public LocaleId getSourceLocale () {
		return LocaleId.fromString(getString(SOURCELOCALE));
	}
	
	public void setSourceLocale (LocaleId locId) {
		setString(SOURCELOCALE, locId.toString());
	}
	
	public LocaleId getTargetLocale () {
		return LocaleId.fromString(getString(TARGETLOCALE));
	}
	
	public void setTargetLocale (LocaleId locId) {
		setString(TARGETLOCALE, locId.toString());
	}

	@Override
	public void reset () {
		super.reset();
		setGlossaryPath("");
		setSourceLocale(LocaleId.ENGLISH);
		setTargetLocale(LocaleId.FRENCH);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(GLOSSARYPATH, "TBX document", "Full path of the TBX document");
		desc.add(SOURCELOCALE, "Source locale", "Locale identifier for the source");
		desc.add(TARGETLOCALE, "Target locale", "Locale identifier for the target");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("SimpleTB Connector Settings", true, false);
		desc.addPathInputPart(paramsDesc.get(Parameters.GLOSSARYPATH), "TBX File", false);
		desc.addTextInputPart(paramsDesc.get(Parameters.SOURCELOCALE));
		desc.addTextInputPart(paramsDesc.get(Parameters.TARGETLOCALE));
		return desc;
	}

}
