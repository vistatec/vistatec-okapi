/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.po;

import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

public class Parameters extends StringParameters implements ISimplifierRulesParameters {

	public static final String PROTECTAPPROVED = "protectApproved";
    private static final String ALLOWEMPTYTARGET = GenericSkeletonWriter.ALLOWEMPTYOUTPUTTARGET;
	private static final String BILINGUALMODE = "bilingualMode";
	private static final String MAKEID = "makeID";
	private static final String USECODEFINDER = "useCodeFinder";
	private static final String CODEFINDERRULES = "codeFinderRules";
	private static final String INCLUDECONTEXTINNOTE = "includeMsgContextInNote";

	private InlineCodeFinder codeFinder;

	// POFilterWriter or filter-driven options, not persisted 
	private boolean wrapContent = true;
	private boolean outputGeneric = false;

	public Parameters () {
		super();
	}
	
	public boolean getBilingualMode () {
		return getBoolean(BILINGUALMODE);
	}

	public void setBilingualMode (boolean bilingualMode) {
		setBoolean(BILINGUALMODE, bilingualMode);
	}

	public boolean getProtectApproved () {
		return getBoolean(PROTECTAPPROVED);
	}

	public void setProtectApproved (boolean protectApproved) {
		setBoolean(PROTECTAPPROVED, protectApproved);
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

	public void setCodeFinder (InlineCodeFinder codeFinder) {
		this.codeFinder = codeFinder;
	}

	public boolean getMakeID () {
		return getBoolean(MAKEID);
	}
	
	public void setMakeID (boolean makeID) {
		setBoolean(MAKEID, makeID);
	}

	public boolean getWrapContent () {
		return wrapContent;
	}
	
	public void setWrapContent (boolean wrapContent) {
		this.wrapContent = wrapContent;
	}

	public boolean getOutputGeneric () {
		return outputGeneric;
	}
	
	public void setOutputGeneric (boolean outputGeneric) {
		this.outputGeneric = outputGeneric;
	}

	public boolean getAllowEmptyOutputTarget () {
		return getBoolean(ALLOWEMPTYTARGET);
	}
	
	public void setAllowEmptyOutputTarget (boolean allowEmptyOutputTarget) {
		setBoolean(ALLOWEMPTYTARGET, allowEmptyOutputTarget);
	}

	public boolean getIncludeMsgContextInNote() {
	    return getBoolean(INCLUDECONTEXTINNOTE);
	}

	public void setIncludeMsgContextInNote(boolean includeMsgContextInNote) {
	    setBoolean(INCLUDECONTEXTINNOTE, includeMsgContextInNote);
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

	public void reset () {
		super.reset();
		setBilingualMode(true);
		setMakeID(true);
		setProtectApproved(false);
		setUseCodeFinder(true);
		setAllowEmptyOutputTarget(false);

		codeFinder = new InlineCodeFinder();
		codeFinder.setSample("%s, %d, {1}, \\n, \\r, \\t, etc.");
		codeFinder.setUseAllRulesWhenTesting(true);
		// Default in-line codes: special escaped-chars and printf-style variable
		codeFinder.addRule("%(([-0+#]?)[-0+#]?)((\\d\\$)?)(([\\d\\*]*)(\\.[\\d\\*]*)?)[dioxXucsfeEgGpn]");
		codeFinder.addRule("(\\\\r\\\\n)|\\\\a|\\\\b|\\\\f|\\\\n|\\\\r|\\\\t|\\\\v");
		//TODO: Add Java-style variables. this is too basic
		codeFinder.addRule("\\{\\d[^\\\\]*?\\}");
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
