/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

public class Options extends StringParameters implements IEditorDescriptionProvider {

	private static final String PLACEHOLDERMODE = "placeholderMode"; //$NON-NLS-1$
	private static final String INCLUDENOTRANSLATE = "includeNoTranslate"; //$NON-NLS-1$ 
	private static final String SETAPPROVEDASNOTRANSLATE = "setApprovedAsNoTranslate"; //$NON-NLS-1$
	private static final String COPYSOURCE = "copySource"; //$NON-NLS-1$
	private static final String INCLUDEALTTRANS = "includeAltTrans"; //$NON-NLS-1$
	private static final String INCLUDECODEATTRS = "includeCodeAttrs"; //$NON-NLS-1$
	private static final String INCLUDEITS = "includeIts"; //$NON-NLS-1$
	private static final String USESKELETON = "useSkeleton"; //$NON-NLS-1$

	public Options () {
		super();
	}
	
	@Override
	public void reset() {
		super.reset();
		setPlaceholderMode(true);
		setIncludeNoTranslate(true);
		setSetApprovedAsNoTranslate(false);
		setCopySource(true);
		setIncludeAltTrans(true);
		setIncludeCodeAttrs(false);
		setIncludeIts(true);
		setUseSkeleton(false);
	}

	@Override
	public void fromString (String data) {
		super.fromString(data);
		
		// Make sure the we can merge later
		if ( !getIncludeNoTranslate()) {
			setSetApprovedAsNoTranslate(false);
		}
	}

	/**
	 * Sets the flag indicating if we use the JSON skeleton
	 * @param useSkeleton true to use the skeleton, false to use the original as the base for merging.
	 */
	public void setUseSkeleton (boolean useSkeleton) {
		setBoolean(USESKELETON, useSkeleton);
	}

	public boolean getUseSkeleton () {
		return getBoolean(USESKELETON);
	}

	public boolean getPlaceholderMode () {
		return getBoolean(PLACEHOLDERMODE);
	}

	public void setPlaceholderMode (boolean placeholderMode) {
		setBoolean(PLACEHOLDERMODE, placeholderMode);
	}
	
	public boolean getIncludeNoTranslate () {
		return getBoolean(INCLUDENOTRANSLATE);
	}

	public void setIncludeNoTranslate (boolean includeNoTranslate) {
		setBoolean(INCLUDENOTRANSLATE, includeNoTranslate);
	}
	
	public boolean getSetApprovedAsNoTranslate () {
		return getBoolean(SETAPPROVEDASNOTRANSLATE);
	}

	public void setSetApprovedAsNoTranslate (boolean setApprovedAsNoTranslate) {
		setBoolean(SETAPPROVEDASNOTRANSLATE, setApprovedAsNoTranslate);
	}

	public boolean getCopySource () {
		return getBoolean(COPYSOURCE);
	}
	
	public void setCopySource (boolean copySource) {
		setBoolean(COPYSOURCE, copySource);
	}

	public boolean getIncludeAltTrans () {
		return getBoolean(INCLUDEALTTRANS);
	}

	public void setIncludeAltTrans (boolean includeAltTrans) {
		setBoolean(INCLUDEALTTRANS, includeAltTrans);
	}

	public boolean getIncludeCodeAttrs () {
		return getBoolean(INCLUDECODEATTRS);
	}

	public void setIncludeCodeAttrs (boolean includeCodeAttrs) {
		setBoolean(INCLUDECODEATTRS, includeCodeAttrs);
	}

	public boolean getIncludeIts () {
		return getBoolean(INCLUDEITS);
	}

	public void setIncludeIts (boolean includeIts) {
		setBoolean(INCLUDEITS, includeIts);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(PLACEHOLDERMODE, "Use <g></g> and <x/> notation", null);
		desc.add(INCLUDENOTRANSLATE, "Include non-translatable text units", null);
		desc.add(SETAPPROVEDASNOTRANSLATE, "Set approved entries as non-translatable", null);
		desc.add(COPYSOURCE, "Copy source text in target if no target is available", null);
		desc.add(INCLUDEALTTRANS, "Include <alt-trans> elements", null);
		desc.add(INCLUDECODEATTRS, "Include extended code attributes when available", null);
		desc.add(INCLUDEITS, "Include ITS markup when available", null);
		desc.add(USESKELETON, "Use the JSON skeleton for merging back", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Generic XLIFF Package", true, false);
		CheckboxPart cbp1 = desc.addCheckboxPart(paramsDesc.get(INCLUDENOTRANSLATE));
		CheckboxPart cbp2 = desc.addCheckboxPart(paramsDesc.get(SETAPPROVEDASNOTRANSLATE));
		cbp2.setMasterPart(cbp1, true);
		desc.addCheckboxPart(paramsDesc.get(PLACEHOLDERMODE));
		desc.addCheckboxPart(paramsDesc.get(COPYSOURCE));
		desc.addCheckboxPart(paramsDesc.get(INCLUDEALTTRANS));
		desc.addCheckboxPart(paramsDesc.get(INCLUDECODEATTRS));
		desc.addCheckboxPart(paramsDesc.get(INCLUDEITS));
		desc.addCheckboxPart(paramsDesc.get(USESKELETON));
		return desc;
	}

}
