/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.mif;

import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

public class Parameters extends StringParameters implements ISimplifierRulesParameters {

	private static final String EXTRACTBODYPAGES = "extractBodyPages";
	private static final String EXTRACTREFERENCEPAGES = "extractReferencePages";
	private static final String EXTRACTMASTERPAGES = "extractMasterPages";
	private static final String EXTRACTHIDDENPAGES = "extractHiddenPages";
	private static final String USECODEFINDER = "useCodeFinder";
	private static final String CODEFINDERRULES = "codeFinderRules";
	private static final String EXTRACTVARIABLES = "extractVariables";
	private static final String EXTRACTINDEXMARKERS = "extractIndexMarkers";
	private static final String EXTRACTLINKS = "extractLinks";
	
	private InlineCodeFinder codeFinder; // Initialized in reset()

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

	public String getCodeFinderData () {
		return codeFinder.toString();
	}

	public void setCodeFinderData (String data) {
		codeFinder.fromString(data);
	}
	
	public boolean getExtractReferencePages () {
		return getBoolean(EXTRACTREFERENCEPAGES);
	}
	
	public void setExtractReferencePages (boolean extractReferencePages) {
		setBoolean(EXTRACTREFERENCEPAGES, extractReferencePages);
	}

	public boolean getExtractMasterPages () {
		return getBoolean(EXTRACTMASTERPAGES);
	}
	
	public void setExtractMasterPages (boolean extractMasterPages) {
		setBoolean(EXTRACTMASTERPAGES, extractMasterPages);
	}
	
	public boolean getExtractHiddenPages () {
		return getBoolean(EXTRACTHIDDENPAGES);
	}

	public void setExtractHiddenPages (boolean extractHiddenPages) {
		setBoolean(EXTRACTHIDDENPAGES, extractHiddenPages);
	}

	public boolean getExtractBodyPages () {
		return getBoolean(EXTRACTBODYPAGES);
	}

	public void setExtractBodyPages (boolean extractBodyPages) {
		setBoolean(EXTRACTBODYPAGES, extractBodyPages);
	}

	public boolean getExtractVariables () {
		return getBoolean(EXTRACTVARIABLES);
	}
	
	public void setExtractVariables (boolean extractVariables) {
		setBoolean(EXTRACTVARIABLES, extractVariables);
	}
	
	public boolean getExtractIndexMarkers () {
		return getBoolean(EXTRACTINDEXMARKERS);
	}
	
	public void setExtractIndexMarkers (boolean extractIndexMarkers) {
		setBoolean(EXTRACTINDEXMARKERS, extractIndexMarkers);
	}
	
	public boolean getExtractLinks () {
		return getBoolean(EXTRACTLINKS);
	}
	
	public void setExtractLinks (boolean extractLinks) {
		setBoolean(EXTRACTLINKS, extractLinks);
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
	public void reset () {
		super.reset();
		setExtractBodyPages(true);
		setExtractMasterPages(true);
		setExtractReferencePages(true);
		setExtractHiddenPages(true);
		setExtractVariables(true);
		setExtractIndexMarkers(true);
		setExtractLinks(false);
		setUseCodeFinder(true);
		
		codeFinder = new InlineCodeFinder();
		codeFinder.setSample("text <$varName> text");
		codeFinder.setUseAllRulesWhenTesting(true);
		codeFinder.addRule("<\\$.*?>");
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

}
