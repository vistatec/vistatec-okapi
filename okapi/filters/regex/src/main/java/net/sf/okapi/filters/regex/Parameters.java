/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.regex;

import java.util.ArrayList;
import java.util.regex.Pattern;

import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.filters.LocalizationDirectives;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

public class Parameters extends StringParameters implements ISimplifierRulesParameters {

	private static final String EXTRACTOUTERSTRINGS = "extractOuterStrings";
	private static final String STARTSTRING = "startString";
	private static final String ENDSTRING = "endString";
	private static final String USEBSLASHESCAPE = "useBSlashEscape";
	private static final String USEDOUBLECHARESCAPE = "useDoubleCharEscape";
	private static final String ONELEVELGROUP = "oneLevelGroups";
	private static final String USELD = "useLd";
	private static final String LOCALIZEOUTSIDE = "localizeOutside";
	private static final String REGEXOPTIONS = "regexOptions";
	private static final String MIMETYPE = "mimeType";

	private LocalizationDirectives localizationDirectives;
	private ArrayList<Rule> rules;

	public Parameters () {
		super();
	}
	
	public void reset () {
		super.reset();
		setRegexOptions(Pattern.DOTALL | Pattern.MULTILINE);
		setStartString("\"");
		setEndString("\"");
		setExtractOuterStrings(false);
		setUseBSlashEscape(true);
		setUseDoubleCharEscape(false);
		setMimeType("text/plain");
		setOneLevelGroups(false);
		
		rules = new ArrayList<Rule>();
		localizationDirectives = new LocalizationDirectives();
		setSimplifierRules(null);
	}

	public boolean getExtractOuterStrings () {
		return getBoolean(EXTRACTOUTERSTRINGS);
	}

	public void setExtractOuterStrings (boolean extractOuterStrings) {
		setBoolean(EXTRACTOUTERSTRINGS, extractOuterStrings);
	}

	public String getStartString () {
		return getString(STARTSTRING);
	}

	public void setStartString (String startString) {
		setString(STARTSTRING, startString);
	}

	public String getEndString () {
		return getString(ENDSTRING);
	}

	public void setEndString (String endString) {
		setString(ENDSTRING, endString);
	}

	public boolean getUseBSlashEscape () {
		return getBoolean(USEBSLASHESCAPE);
	}

	public void setUseBSlashEscape (boolean useBSlashEscape) {
		setBoolean(USEBSLASHESCAPE, useBSlashEscape);
	}
	
	public boolean getUseDoubleCharEscape () {
		return getBoolean(USEDOUBLECHARESCAPE);
	}
	
	public void setUseDoubleCharEscape (boolean useDoubleCharEscape) {
		setBoolean(USEDOUBLECHARESCAPE, useDoubleCharEscape);
	}
	
	public int getRegexOptions () {
		return getInteger(REGEXOPTIONS);
	}

	public void setRegexOptions (int regexOptions) {
		setInteger(REGEXOPTIONS, regexOptions);
	}
	
	public LocalizationDirectives getLocalizationDirectives () {
		return localizationDirectives;
	}
	
	public void setLocalizationDirectives (
		LocalizationDirectives localizationDirectives) {
		this.localizationDirectives = localizationDirectives;
	}
	
	public String getMimeType () {
		return getString(MIMETYPE);
	}
	
	public void setMimeType (String mimeType) {
		setString(MIMETYPE, mimeType);
	}
	
	public boolean getOneLevelGroups () {
		return getBoolean(ONELEVELGROUP);
	}
	
	public void setOneLevelGroups (boolean oneLevelGroups) {
		setBoolean(ONELEVELGROUP, oneLevelGroups);
	}

	public void fromString (String data) {
		super.fromString(data);

		boolean tmpBool1 = buffer.getBoolean(USELD, localizationDirectives.useLD());
		boolean tmpBool2 = buffer.getBoolean(LOCALIZEOUTSIDE, localizationDirectives.localizeOutside());
		localizationDirectives.setOptions(tmpBool1, tmpBool2);

		Rule rule;
		int count = buffer.getInteger("ruleCount", 0);
		for ( int i=0; i<count; i++ ) {
			rule = new Rule();
			rule.fromString(buffer.getGroup(String.format("rule%d", i), null));
			rules.add(rule);
		}
	}
	
	@Override
	public String toString () {
		buffer.setBoolean(USELD, localizationDirectives.useLD());
		buffer.setBoolean(LOCALIZEOUTSIDE, localizationDirectives.localizeOutside());
		buffer.setInteger("ruleCount", rules.size());
		for ( int i=0; i<rules.size(); i++ ) {
			buffer.setGroup(String.format("rule%d", i), rules.get(i).toString());
		}
		return super.toString();
	}
	
	public void compileRules () {
		for ( Rule rule : rules ) {
			// Compile the full pattern
			rule.pattern = Pattern.compile(rule.expr, getRegexOptions());
			// Compile any used in-line code rules for this rule
			if ( rule.useCodeFinder ) {
				rule.codeFinder.compile();
			}
		}
	}
	
	public ArrayList<Rule> getRules () {
		return rules;
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
}
