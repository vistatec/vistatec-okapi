/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.xliff;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

public class XLIFF2Options extends StringParameters implements IEditorDescriptionProvider {

	private static final String WITHORIGINALDATA = "withOriginalData"; //$NON-NLS-1$
	private static final String CREATETIPPACKAGE = "createTipPackage"; //$NON-NLS-1$
	private static final String ELIMINATEEMPTYTARGETSWITHNONEMPTYSOURCE = "eliminateEmptyTargetsWithNonEmptySource"; //$NON-NLS-1$
	
	public XLIFF2Options () {
		super();
	}
	
	@Override
	public void reset() {
		super.reset();
		setWithOriginalData(true);
		setCreateTipPackage(false);
	}

	public boolean getwithOriginalData () {
		return getBoolean(WITHORIGINALDATA);
	}

	public void setWithOriginalData (boolean withOriginalData) {
		setBoolean(WITHORIGINALDATA, withOriginalData);
	}
	
	public boolean getCreateTipPackage () {
		return getBoolean(CREATETIPPACKAGE);
	}
	
	public void setCreateTipPackage (boolean createTipPackage) {
		setBoolean(CREATETIPPACKAGE, createTipPackage);
	}
	
	public boolean getEliminateEmptyTargetsWithNonEmptySource () {
		return getBoolean(ELIMINATEEMPTYTARGETSWITHNONEMPTYSOURCE);
	}
	
	public void setEliminateEmptyTargetsWithNonEmptySource (boolean eliminateEmptyTargetsWithNonEmptySource) {
		setBoolean(ELIMINATEEMPTYTARGETSWITHNONEMPTYSOURCE, eliminateEmptyTargetsWithNonEmptySource);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(WITHORIGINALDATA, "Output includes original data when available", null);
		desc.add(CREATETIPPACKAGE, "Create a TIPP file", null);
		desc.add(ELIMINATEEMPTYTARGETSWITHNONEMPTYSOURCE, "Remove segments with empty target and non-empty source", null);
		
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Experimental XLIFF 2.0", true, false);
		desc.addCheckboxPart(paramsDesc.get(WITHORIGINALDATA));
		desc.addCheckboxPart(paramsDesc.get(ELIMINATEEMPTYTARGETSWITHNONEMPTYSOURCE));
		//TODO maybe desc.addCheckboxPart(paramsDesc.get(CREATETIPPACKAGE));
		return desc;
	}

}
