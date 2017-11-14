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
============================================================================*/

package net.sf.okapi.filters.xliff2;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	public static final String MAXVALIDATION = "maxValidation";
	public static final String MERGE_AS_PARAGRAPH = "mergeAsParagraph";
	private static final String USECODEFINDER = "useCodeFinder";
	private static final String CODEFINDERRULES = "codeFinderRules";
	private static final String SIMPLIFY_TAGS = "simplifyTags";
	private static final String NEEDS_SEGMENTATION = "needsSegmentation";

	private InlineCodeFinder codeFinder; // Initialized in reset()

	public Parameters () {
		super();
	}

	public boolean getMaxValidation () {
		return getBoolean(MAXVALIDATION);
	}

	public void setMaxValidation (boolean maxValidation) {
		setBoolean(MAXVALIDATION, maxValidation);
	}
	
	public boolean getMergeAsParagraph() {
		return getBoolean(MERGE_AS_PARAGRAPH);
	}

	public void setMergeAsParagraph (boolean mergeAsParagraph) {
		setBoolean(MERGE_AS_PARAGRAPH, mergeAsParagraph);
	}
	
	public boolean getUseCodeFinder () {
		return getBoolean(USECODEFINDER);
	}

	public void setUseCodeFinder (boolean useCodeFinder) {
		setBoolean(USECODEFINDER, useCodeFinder);
	}

	public InlineCodeFinder getCodeFinder () {
		return codeFinder;
	}
	
	
	public String getCodeFinderData () {
		return codeFinder.toString();
	}

	public void setCodeFinderData (String data) {
		codeFinder.fromString(data);
	}

	public boolean getSimplifyTags () {
		return getBoolean(SIMPLIFY_TAGS);

	}
	
	public void setSimplifyTags (boolean simplifyTags) {
		setBoolean(SIMPLIFY_TAGS, simplifyTags);
	}
	
	public boolean getNeedsSegmentation() {
		return getBoolean(NEEDS_SEGMENTATION);

	}
	
	public void setNeedsSegmentation(boolean needsSegmentation) {
		setBoolean(NEEDS_SEGMENTATION, needsSegmentation);
	}
	
	@Override
	public void reset () {
		super.reset();
		setMaxValidation(true);
		setMergeAsParagraph(false);
		setUseCodeFinder(false);
		codeFinder = new InlineCodeFinder();
		codeFinder.setSample("&name; <tag></at><tag/> <tag attr='val'> </tag=\"val\">");
		codeFinder.setUseAllRulesWhenTesting(true);
		codeFinder.addRule("</?([A-Z0-9a-z]*)\\b[^>]*>");
		setSimplifyTags(true);		
		setNeedsSegmentation(false);
	}

	@Override
	public void fromString (String data) {
		super.fromString(data);
		codeFinder.fromString(buffer.getGroup(CODEFINDERRULES, ""));
	}

	@Override
	public String toString () {
		buffer.setGroup(CODEFINDERRULES, codeFinder.toString());
		return super.toString();
	}
	
	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(MAXVALIDATION, "Perform maximum validation when parsing", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("XLIFF-2 Filter Parameters", true, false);
		
		desc.addCheckboxPart(paramDesc.get(MAXVALIDATION));

		return desc;
	}

}
