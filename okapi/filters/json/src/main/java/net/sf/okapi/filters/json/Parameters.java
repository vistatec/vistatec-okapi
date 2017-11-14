/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.json;

import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

public class Parameters extends StringParameters implements ISimplifierRulesParameters {

	private static final String EXTRACTISOLATEDSTRINGS = "extractIsolatedStrings";
	private static final String EXTRACTALLPAIRS = "extractAllPairs";
	private static final String EXCEPTIONS = "exceptions";
	private static final String USEKEYASNAME = "useKeyAsName";
	private static final String USECODEFINDER = "useCodeFinder";
	private static final String USEFULLKEYPATH = "useFullKeyPath";
	private static final String USELEADINGSLASHONKEYPATH = "useLeadingSlashOnKeyPath";
	private static final String CODEFINDERRULES = "codeFinderRules";
	private static final String SUBFILTER = "subfilter";
	private static final String ESCAPEFORWARDSLASHES = "escapeForwardSlashes";

	private InlineCodeFinder codeFinder; // Initialized in reset()

	public Parameters () {
		super();
	}

	public boolean getExtractStandalone () {
		return getBoolean(EXTRACTISOLATEDSTRINGS);
	}

	public void setExtractStandalone (boolean extractStandalone) {
		setBoolean(EXTRACTISOLATEDSTRINGS, extractStandalone);
	}

	public boolean getExtractAllPairs () {
		return getBoolean(EXTRACTALLPAIRS);
	}

	public void setExtractAllPairs (boolean extractAllPairs) {
		setBoolean(EXTRACTALLPAIRS, extractAllPairs);
	}

	public String getExceptions () {
		return getString(EXCEPTIONS);
	}

	public void setExceptions (String exceptions) {
		setString(EXCEPTIONS, exceptions);
	}

	public boolean getUseKeyAsName () {
		return getBoolean(USEKEYASNAME);
	}

	public void setUseKeyAsName (boolean useKeyAsName) {
		setBoolean(USEKEYASNAME, useKeyAsName);
	}

	public boolean getUseFullKeyPath () {
		return getBoolean(USEFULLKEYPATH);
	}

	public void setUseFullKeyPath (boolean useFullKeyPath) {
		setBoolean(USEFULLKEYPATH, useFullKeyPath);
	}

	public boolean getUseLeadingSlashOnKeyPath () {
		return getBoolean(USELEADINGSLASHONKEYPATH);
	}

	public void setUseLeadingSlashOnKeyPath (boolean useLeadingSlashOnKeyPath) {
		setBoolean(USELEADINGSLASHONKEYPATH, useLeadingSlashOnKeyPath);
	}

	public boolean getEscapeForwardSlashes () {
		return getBoolean(ESCAPEFORWARDSLASHES);
	}

	public void setEscapeForwardSlashes (boolean setEscapeForwardSlashes) {
		setBoolean(ESCAPEFORWARDSLASHES, setEscapeForwardSlashes);
	}

	public boolean getUseCodeFinder () {
		return getBoolean(USECODEFINDER);
	}

	public void setUseCodeFinder (boolean useCodeFinder) {
		setBoolean(USECODEFINDER, useCodeFinder);
		if (getUseCodeFinder()) {
			setSubfilter("");
		}
	}

	public InlineCodeFinder getCodeFinder () {
		return codeFinder;
	}

	public String getSubfilter() {
		return getString(SUBFILTER);
	}

	public void setSubfilter(String subfilter) {
		setString(SUBFILTER, subfilter);
		if (!"".equals(getSubfilter())) {
			setUseCodeFinder(false);
		}
	}

	public String getCodeFinderData () {
		return codeFinder.toString();
	}

	public void setCodeFinderData (String data) {
		codeFinder.fromString(data);
	}

	public void reset () {
		super.reset();
		setExtractStandalone(false);
		setExtractAllPairs(true);
		setExceptions("");
		setUseKeyAsName(true);
		setUseFullKeyPath(false);
		setUseLeadingSlashOnKeyPath(true);
		setEscapeForwardSlashes(true);
		setUseCodeFinder(false);
		setSubfilter(null);
		codeFinder = new InlineCodeFinder();
		codeFinder.setSample("&name; <tag></at><tag/> <tag attr='val'> </tag=\"val\">");
		codeFinder.setUseAllRulesWhenTesting(true);
		codeFinder.addRule("</?([A-Z0-9a-z]*)\\b[^>]*>");
		setSimplifierRules(null);
	}

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
}
