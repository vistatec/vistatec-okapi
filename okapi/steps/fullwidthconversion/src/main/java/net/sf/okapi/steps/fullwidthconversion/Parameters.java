/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.steps.fullwidthconversion;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	
	private static final String TOHALFWIDTH = "toHalfWidth";
	private static final String ASCIIONLY = "asciiOnly";
	private static final String KATAKANAONLY = "katakanaOnly";
	private static final String INCLUDESLA = "includeSLA";
	private static final String INCLUDELLS = "includeLLS";
	private static final String INCLUDEKATAKANA = "includeKatakana";
	private static final String NORMALIZEOUTPUT = "normalizeOutput";
	
	public Parameters () {
		super();
	}
	
	public boolean getToHalfWidth() {
		return getBoolean(TOHALFWIDTH);
	}

	public void setToHalfWidth(boolean toHalfWidth) {
		setBoolean(TOHALFWIDTH, toHalfWidth);
	}

	public boolean getAsciiOnly() {
		return getBoolean(ASCIIONLY);
	}

	public void setAsciiOnly(boolean asciiOnly) {
		setBoolean(ASCIIONLY, asciiOnly);
		if ( asciiOnly ) {
			setBoolean(KATAKANAONLY, false);
		}
	}

	public boolean getKatakanaOnly() {
		return getBoolean(KATAKANAONLY);
	}

	public void setKatakanaOnly(boolean katakanaOnly) {
		setBoolean(KATAKANAONLY, katakanaOnly);
		if ( katakanaOnly ) {
			setBoolean(ASCIIONLY, false);
		}
	}

	public boolean getIncludeSLA() {
		return getBoolean(INCLUDESLA);
	}

	public void setIncludeSLA(boolean includeSLA) {
		setBoolean(INCLUDESLA, includeSLA);
	}

	public boolean getIncludeLLS() {
		return getBoolean(INCLUDELLS);
	}

	public void setIncludeLLS(boolean includeLLS) {
		setBoolean(INCLUDELLS, includeLLS);
	}

	public boolean getIncludeKatakana() {
		return getBoolean(INCLUDEKATAKANA);
	}

	public void setIncludeKatakana(boolean includeKatakana) {
		setBoolean(INCLUDEKATAKANA, includeKatakana);
	}
	
	public boolean getNormalizeOutput() {
		return getBoolean(NORMALIZEOUTPUT);
	}

	public void setNormalizeOutput(boolean normalizeOutput) {
		setBoolean(NORMALIZEOUTPUT, normalizeOutput);
	}

	public void reset() {
		super.reset();
		setToHalfWidth(true);
		setAsciiOnly(false);
		setKatakanaOnly(false);
		setIncludeSLA(false);
		setIncludeLLS(false);
		setIncludeKatakana(false);
		setNormalizeOutput(true);
	}
}
