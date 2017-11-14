/*===========================================================================
  Copyright (C) 2008 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffkit.writer;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(XLIFFKitWriterStep.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	static final String PLACEHOLDERMODE = "placeholderMode"; //$NON-NLS-1$
	static final String COPYSOURCE = "copySource"; //$NON-NLS-1$
	static final String MESSAGE = "message"; //$NON-NLS-1$
	
	static final String INCLUDE_NO_TRANSLATE = "includeNoTranslate"; //$NON-NLS-1$
	static final String SET_APPROVED_AS_NO_TRANSLATE = "setApprovedAsNoTranslate"; //$NON-NLS-1$
	
	static final String OUTPUT_URI = "outputURI"; //$NON-NLS-1$
	static final String INCLUDE_SOURCE = "includeSource"; //$NON-NLS-1$
	static final String INCLUDE_ORIGINAL = "includeOriginal"; //$NON-NLS-1$
	static final String INCLUDE_CODE_ATTRS = "includeCodeAttrs"; //$NON-NLS-1$
	
	public Parameters () {
		super();
	}
	
	public void reset() {
		super.reset();
		setPlaceholderMode(false);
		setCopySource(true);
		setIncludeNoTranslate(true);
		setSetApprovedAsNoTranslate(true);
		setMessage("");
		setOutputURI("");
		setIncludeSource(true);
		setIncludeOriginal(false);
		setIncludeCodeAttrs(false);
	}

	public void fromString (String data) {
		super.fromString(data);
		
		// Make sure the we can merge later
		if ( !isIncludeNoTranslate()) {
			setSetApprovedAsNoTranslate(false);
		}
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		
		desc.add(PLACEHOLDERMODE, "Use the <g></g> and <x/> notation", null);
		desc.add(COPYSOURCE, "Copy source text in target if no target is available", null);
		desc.add(INCLUDE_NO_TRANSLATE, "Include non-translatable text units", "Include non-translatables");
		desc.add(SET_APPROVED_AS_NO_TRANSLATE, "Set approved entries as non-translatable", "Approved as non-translatable");
		desc.add(MESSAGE, "Description of the XLIFF file", "Description");
		desc.add(OUTPUT_URI, "Directory of the T-kit file", "T-kit Path");
		desc.add(INCLUDE_SOURCE, "Include source files in the T-kit file", "Include source");
		desc.add(INCLUDE_ORIGINAL, "Include original files in the T-kit file", "Include originals");
		desc.add(INCLUDE_CODE_ATTRS, "Include original files in the T-kit file", "Include originals");
		
		return desc;
	}
	
	public EditorDescription createEditorDescription(
			ParametersDescription parametersDescription) {
		EditorDescription desc = new EditorDescription("XLIFF Kit Writer Options", true, false);
		
		desc.addCheckboxPart(parametersDescription.get(PLACEHOLDERMODE));
		desc.addCheckboxPart(parametersDescription.get(COPYSOURCE));
		desc.addCheckboxPart(parametersDescription.get(INCLUDE_NO_TRANSLATE));
		desc.addCheckboxPart(parametersDescription.get(SET_APPROVED_AS_NO_TRANSLATE));
		desc.addTextInputPart(parametersDescription.get(MESSAGE));
		desc.addTextInputPart(parametersDescription.get(OUTPUT_URI));
		desc.addCheckboxPart(parametersDescription.get(INCLUDE_SOURCE));
		desc.addCheckboxPart(parametersDescription.get(INCLUDE_ORIGINAL));
		desc.addCheckboxPart(parametersDescription.get(INCLUDE_CODE_ATTRS));
		
		return desc;
	}

	public boolean isPlaceholderMode() {
		return getBoolean(PLACEHOLDERMODE);
	}

	public void setPlaceholderMode(boolean placeholderMode) {
		setBoolean(PLACEHOLDERMODE, placeholderMode);
	}

	public boolean isIncludeNoTranslate() {
		return getBoolean(INCLUDE_NO_TRANSLATE);
	}

	public void setIncludeNoTranslate(boolean includeNoTranslate) {
		setBoolean(INCLUDE_NO_TRANSLATE, includeNoTranslate);
	}

	public boolean isSetApprovedAsNoTranslate() {
		return getBoolean(SET_APPROVED_AS_NO_TRANSLATE);
	}

	public void setSetApprovedAsNoTranslate(boolean setApprovedAsNoTranslate) {
		setBoolean(SET_APPROVED_AS_NO_TRANSLATE, setApprovedAsNoTranslate);
	}

	public String getMessage() {
		return getString(MESSAGE);
	}

	public void setMessage(String message) {
		setString(MESSAGE, message);
	}

	public String getOutputURI() {
		return getString(OUTPUT_URI);
	}

	public void setOutputURI(String outputURI) {
		setString(OUTPUT_URI, outputURI);
	}

	public boolean isIncludeSource() {
		return getBoolean(INCLUDE_SOURCE);
	}

	public void setIncludeSource(boolean includeSource) {
		setBoolean(INCLUDE_SOURCE, includeSource);
	}

	public boolean isIncludeOriginal() {
		return getBoolean(INCLUDE_ORIGINAL);
	}

	public void setIncludeOriginal(boolean includeOriginal) {
		setBoolean(INCLUDE_ORIGINAL, includeOriginal);
	}

	public void setCopySource(boolean copySource) {
		setBoolean(COPYSOURCE, copySource);
	}

	public boolean isCopySource() {
		return getBoolean(COPYSOURCE);
	}

	public void setIncludeCodeAttrs(boolean includeCodeAttrs) {
		setBoolean(INCLUDE_CODE_ATTRS, includeCodeAttrs);
	}

	public boolean isIncludeCodeAttrs() {
		return getBoolean(INCLUDE_CODE_ATTRS);
	}
}
