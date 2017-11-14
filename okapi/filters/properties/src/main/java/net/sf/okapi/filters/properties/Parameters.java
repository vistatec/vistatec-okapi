/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.filters.properties;

import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.filters.LocalizationDirectives;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

public class Parameters extends StringParameters implements ISimplifierRulesParameters {

	private static final String USELD = "useLd";
	private static final String LOCALIZEOUTSIDE = "localizeOutside";
	private static final String USECODEFINDER = "useCodeFinder";
	private static final String CODEFINDERRULES = "codeFinderRules";

	private static final String CONVERTLFANDTAB = "convertLFandTab";
	private static final String USEKEYCONDITION = "useKeyCondition";
	private static final String EXTRACTONLYMATCHINGKEY = "extractOnlyMatchingKey";
	private static final String KEYCONDITION = "keyCondition";
	private static final String EXTRACOMMENTS = "extraComments";
	private static final String COMMENTSARENOTES = "commentsAreNotes";
	private static final String ESCAPEEXTENDEDCHARS = "escapeExtendedChars";
	private static final String SUBFILTER = "subfilter";
	private static final String IDLIKERESNAME = "idLikeResname";
	
	public InlineCodeFinder codeFinder;
	public LocalizationDirectives locDir;
	
	public Parameters () {
		super();
	}
	
	public void reset () {
		super.reset();
		locDir = new LocalizationDirectives();

		setEscapeExtendedChars(true);
		setConvertLFandTab(true);
		setUseKeyCondition(false);;
		setExtractOnlyMatchingKey(true);
		setKeyCondition(".*text.*");
		setExtraComments(false);
		setCommentsAreNotes(true);
		setSubfilter(null);
		setIdLikeResname(false);

		setUseCodeFinder(true);
		codeFinder = new InlineCodeFinder();	
		codeFinder.setSample("%s, %d, {1}, \\n, \\r, \\t, etc.");
		codeFinder.setUseAllRulesWhenTesting(true);
		// Default in-line codes: special escaped-chars and printf-style variable
		codeFinder.addRule("%(([-0+#]?)[-0+#]?)((\\d\\$)?)(([\\d\\*]*)(\\.[\\d\\*]*)?)[dioxXucsfeEgGpn]");
		codeFinder.addRule("(\\\\r\\\\n)|\\\\a|\\\\b|\\\\f|\\\\n|\\\\r|\\\\t|\\\\v");
		//TODO: Add Java-style variables. this is too basic
		codeFinder.addRule("\\{\\d[^\\\\]*?\\}");		
		// Basic HTML/XML
		codeFinder.addRule("\\<(/?)\\w+[^>]*?>");
		setSimplifierRules(null);
	}

	@Override
	public String toString () {
		buffer.setBoolean(USELD, locDir.useLD());
		buffer.setBoolean(LOCALIZEOUTSIDE, locDir.localizeOutside());
		buffer.setGroup(CODEFINDERRULES, codeFinder.toString());

		return super.toString();
	}
	
	public void fromString (String data) {
		super.fromString(data);

		boolean tmpBool1 = buffer.getBoolean(USELD, locDir.useLD());
		boolean tmpBool2 = buffer.getBoolean(LOCALIZEOUTSIDE, locDir.localizeOutside());
		locDir.setOptions(tmpBool1, tmpBool2);
		codeFinder.fromString(buffer.getGroup(CODEFINDERRULES, ""));
	}

	public boolean isUseCodeFinder() {
		return getBoolean(USECODEFINDER);
	}

	public void setUseCodeFinder(boolean useCodeFinder) {
		setBoolean(USECODEFINDER, useCodeFinder);
	}

	public InlineCodeFinder getCodeFinder() {
		return codeFinder;
	}

	public void setCodeFinder(InlineCodeFinder codeFinder) {
		this.codeFinder = codeFinder;
	}

	public boolean isEscapeExtendedChars() {
		return getBoolean(ESCAPEEXTENDEDCHARS);
	}

	public void setEscapeExtendedChars(boolean escapeExtendedChars) {
		setBoolean(ESCAPEEXTENDEDCHARS, escapeExtendedChars);
	}

	public boolean isUseKeyCondition() {
		return getBoolean(USEKEYCONDITION);
	}

	public void setUseKeyCondition(boolean useKeyCondition) {
		setBoolean(USEKEYCONDITION, useKeyCondition);
	}

	public boolean isExtractOnlyMatchingKey() {
		return getBoolean(EXTRACTONLYMATCHINGKEY);
	}

	public void setExtractOnlyMatchingKey(boolean extractOnlyMatchingKey) {
		setBoolean(EXTRACTONLYMATCHINGKEY, extractOnlyMatchingKey);
	}

	public String getKeyCondition() {
		return getString(KEYCONDITION);
	}

	public void setKeyCondition(String keyCondition) {
		setString(KEYCONDITION, keyCondition);
	}

	public boolean isExtraComments() {
		return getBoolean(EXTRACOMMENTS);
	}

	public void setExtraComments(boolean extraComments) {
		setBoolean(EXTRACOMMENTS, extraComments);
	}

	public boolean isCommentsAreNotes() {
		return getBoolean(COMMENTSARENOTES);
	}

	public void setCommentsAreNotes(boolean commentsAreNotes) {
		setBoolean(COMMENTSARENOTES, commentsAreNotes);
	}

	public LocalizationDirectives getLocDir() {
		return locDir;
	}

	public void setLocDir(LocalizationDirectives locDir) {
		this.locDir = locDir;
	}

	public boolean isConvertLFandTab() {
		return getBoolean(CONVERTLFANDTAB);
	}

	public void setConvertLFandTab(boolean convertLFandTab) {
		setBoolean(CONVERTLFANDTAB, convertLFandTab);
	}

	public boolean isIdLikeResname () {
		return getBoolean(IDLIKERESNAME);
	}

	public void setIdLikeResname (boolean idLikeResname) {
		setBoolean(IDLIKERESNAME, idLikeResname);
	}

	public String getSubfilter() {
		return getString(SUBFILTER);
	}

	public void setSubfilter(String subfilter) {
		setString(SUBFILTER, subfilter);
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
