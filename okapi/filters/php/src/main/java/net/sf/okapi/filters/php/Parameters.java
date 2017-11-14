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

package net.sf.okapi.filters.php;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.CodeFinderPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider, ISimplifierRulesParameters {
	
	static final String USECODEFINDER = "useCodeFinder";
	static final String CODEFINDERRULES = "codeFinderRules";
	static final String USEDIRECTIVES = "useDirectives";
	static final String EXTRACTOUTSIDEDIRECTIVES = "extractOutsideDirectives";

	private InlineCodeFinder codeFinder;

	public Parameters () {
		super();
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

	public String getCodeFinderRules () {
		return codeFinder.toString();
	}

	public void setCodeFinderRules (String codeFinderRules) {
		codeFinder.fromString(codeFinderRules);
	}

	public boolean getUseDirectives () {
		return getBoolean(USEDIRECTIVES);
	}
	
	public void setUseDirectives (boolean useDirectives) {
		setBoolean(USEDIRECTIVES, useDirectives);
	}
	
	public boolean getExtractOutsideDirectives () {
		return getBoolean(EXTRACTOUTSIDEDIRECTIVES);
	}
	
	public void setExtractOutsideDirectives (boolean extractOutsideDirectives) {
		setBoolean(EXTRACTOUTSIDEDIRECTIVES, extractOutsideDirectives);
	}

	@Override
	public void reset () {
		super.reset();
		setUseDirectives(true);
		setExtractOutsideDirectives(true);
		setUseCodeFinder(true);
		codeFinder = new InlineCodeFinder();
		codeFinder.setSample("text <br/> text \\n text <a att='val'> text [VAR1] text\n{VAR2} text");
		codeFinder.setUseAllRulesWhenTesting(true);

		// HTML-like tags
		codeFinder.addRule("<[\\w!?/].*?>");
		// Basic escaped characters
		codeFinder.addRule("\\\\a|\\\\b|\\\\f|\\\\n|\\\\r|\\\\t|\\\\v");
		// Email address
		codeFinder.addRule("(\\w[-._\\w]*\\w@\\w[-._\\w]*\\w\\.\\w{2,3})");
		// [var] and {var} variables
		codeFinder.addRule("[\\[{][\\w_$]+?[}\\]]");
		setSimplifierRules(null);
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
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(USEDIRECTIVES, "Use localization directives", null);
		desc.add(EXTRACTOUTSIDEDIRECTIVES, "Extract outside the scope of the directives", null);
		desc.add(USECODEFINDER, "Has inline codes as defined below:", null);
		desc.add(CODEFINDERRULES, null, "Rules for inline codes");
		return desc;
	}

	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("PHP Filter Parameters", true, false);

		CheckboxPart cbp1 = desc.addCheckboxPart(paramDesc.get(USEDIRECTIVES));
		CheckboxPart cbp2 = desc.addCheckboxPart(paramDesc.get(EXTRACTOUTSIDEDIRECTIVES));
		cbp2.setMasterPart(cbp1, true);

		cbp1 = desc.addCheckboxPart(paramDesc.get(Parameters.USECODEFINDER));
		CodeFinderPart cfp = desc.addCodeFinderPart(paramDesc.get(Parameters.CODEFINDERRULES));
		cfp.setMasterPart(cbp1, true);
		
		return desc;
	}
	
}
