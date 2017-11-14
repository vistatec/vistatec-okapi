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
===========================================================================*/

package net.sf.okapi.steps.enrycher;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.SpinInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {
	
	private static final String BASEURL = "baseUrl";
	private static final String MAXEVENTS = "maxEvents";
	
	public Parameters() {
		super();
	}
	
	@Override
	public void reset() {
		super.reset();
		setBaseUrl("http://aidemo.ijs.si/mlw");
		setMaxEvents(20);
	}
	
	public String getBaseUrl () {
		return getString(BASEURL);
	}
	
	public void setBaseUrl (String baseUrl) {
		setString(BASEURL, Util.ensureSeparator(baseUrl, true));
	}

	public int getMaxEvents () {
		return getInteger(MAXEVENTS);
	}
	
	public void setMaxEvents (int maxEvents) {
		setInteger(MAXEVENTS, maxEvents);
	}
	
	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(BASEURL, "URL of the Enrycher Web service", null);
		desc.add(MAXEVENTS, "Events buffer", "Number of events to store before sending a query");
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Enrycher", true, false);
		desc.addTextInputPart(paramsDesc.get(BASEURL));
		SpinInputPart sip = desc.addSpinInputPart(paramsDesc.get(MAXEVENTS));
		sip.setRange(1, 999);
		return desc;
	}
	
}
