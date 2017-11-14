/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.bifile;

import java.nio.charset.Charset;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String BIFILE = "biFile";
	private static final String INPUTENCODING = "inputEncoding";

	public Parameters () {
		super();
	}

	public String getBiFile () {
		return getString(BIFILE);
	}
	
	public void setBiFile(String biFile) {
		setString(BIFILE, biFile);
	}
	
	public String getInputEncoding () {
		return getString(INPUTENCODING);
	}
	
	public void setInputEncoding(String inputEncoding) {
		setString(INPUTENCODING, inputEncoding);
	}	
	
	@Override
	public void reset () {
		super.reset();
		setBiFile("");
		setInputEncoding(Charset.defaultCharset().name());
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(BIFILE, "Bilingual file", "The bilingual file from which to leverage");
		desc.add(INPUTENCODING, "Input encoding", "The encoding of the bilingual file");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Bilingual File Connector Settings", true, false);
		desc.addPathInputPart(paramsDesc.get(BIFILE), "Bilingual file", false);
		desc.addTextInputPart(paramsDesc.get(INPUTENCODING));
		return desc;
	}

}
