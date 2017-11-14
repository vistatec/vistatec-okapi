/*===========================================================================
  Copyright (C) 2016 by the Okapi Framework contributors
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

package net.sf.okapi.filters.pdf;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider, ISimplifierRulesParameters {
	public static final String USECODEFINDER = "useCodeFinder";
	public static final String INDENT_THRESHOLD = "indentThreshold";
	public static final String SPACING_TOLERANCE = "spacingTolerance";
	public static final String PRESERVE_WHITESPACE = "preserveWhitespace";
	public static final String LINE_SEPARATOR = "lineSeparator";
	public static final String PARAGRAPH_SEPARATOR = "paragraphSeparator";

	public InlineCodeFinder codeFinder;

	public Parameters () {
		super();
	}
	
	public boolean getUseCodeFinder() {
		return getBoolean(USECODEFINDER);
	}
	
	public void setUseCodeFinder(boolean useCodeFinder) {
		setBoolean(USECODEFINDER, useCodeFinder);
	}
	
	public void setIndentThreshold(String value) {
		setString(INDENT_THRESHOLD, value);
	}
	
	public String getIndentThreshold() {
		return getString(INDENT_THRESHOLD);
	}

	public void setSpacingTolerance(String value) {
		setString(SPACING_TOLERANCE, value);
	}
	
	public String getSpacingTolerance() {
		return getString(SPACING_TOLERANCE);
	}
	
	public void setPreserveWhitespace(boolean preserve) {
		setBoolean(PRESERVE_WHITESPACE, preserve);
	}
	
	public boolean getPreserveWhitespace() {
		return getBoolean(PRESERVE_WHITESPACE);		
	}
	
	public String getLineSeparator() {
		return getString(LINE_SEPARATOR);
	}

	public void setLineSeparator(String value) {
		setString(LINE_SEPARATOR, value);
	}
	
	public String getParagraphSeparator() {
		return getString(PARAGRAPH_SEPARATOR);
	}

	public void setParagraphSeparator(String value) {
		setString(PARAGRAPH_SEPARATOR, value);
	}
	
	public void reset () {
		super.reset();
		codeFinder = new InlineCodeFinder();
		setUseCodeFinder(true);
		setIndentThreshold("2.0");
		setSpacingTolerance("0.5");
		setLineSeparator("\n");
		setParagraphSeparator("\n");
		setPreserveWhitespace(false);
		codeFinder.setSample("&name; <tag></at><tag/> <tag attr='val'> </tag=\"val\">");
		codeFinder.setUseAllRulesWhenTesting(true);
		codeFinder.addRule("</?([A-Z0-9a-z]*)\\b[^>]*>");
		setSimplifierRules(null);
	}

	@Override
	public String toString () {
		super.toString();
		buffer.setGroup("codeFinderRules", codeFinder.toString());		
		return buffer.toString();
	}
	
	public void fromString (String data) {
		super.fromString(data);
		codeFinder.fromString(buffer.getGroup("codeFinderRules", ""));
	}
	
	@Override
	public String getSimplifierRules() {
		return getString(SIMPLIFIERRULES);
	}

	@Override
	public void setSimplifierRules(String rules) {
		setString(SIMPLIFIERRULES, rules);		
	}

	@Override
	public void validateSimplifierRules() throws ParseException {
		SimplifierRules r = new SimplifierRules(getSimplifierRules(), new Code());
		r.parse();
	}
	
	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(LINE_SEPARATOR, "Character to use as line separator", null);
		desc.add(PARAGRAPH_SEPARATOR, "Character to use as paragraph separator", null);
		desc.add(INDENT_THRESHOLD, "Amount of indent needed to define a new paragraph (Default=2.0)", null);
		desc.add(SPACING_TOLERANCE, "Amount of spacing needed to define a white space character (Default=0.5)", null);
		desc.add(PRESERVE_WHITESPACE, "Preserve whitespace", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("PDF Filter", true, false);
		desc.addTextInputPart(paramsDesc.get(LINE_SEPARATOR));		
		desc.addTextInputPart(paramsDesc.get(PARAGRAPH_SEPARATOR));		
		desc.addTextInputPart(paramsDesc.get(INDENT_THRESHOLD));		
		desc.addTextInputPart(paramsDesc.get(SPACING_TOLERANCE));		
		desc.addSeparatorPart();		
		desc.addCheckboxPart(paramsDesc.get(PRESERVE_WHITESPACE));		
		return desc;
	}
}
