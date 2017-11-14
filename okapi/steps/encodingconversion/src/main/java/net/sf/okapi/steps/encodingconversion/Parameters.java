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
============================================================================*/

package net.sf.okapi.steps.encodingconversion;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	
	public static final int ESCAPE_NCRHEXAU      = 0;
	public static final int ESCAPE_NCRHEXAL      = 1;
	public static final int ESCAPE_NCRDECI       = 2;
	public static final int ESCAPE_CER           = 3;
	public static final int ESCAPE_JAVAU         = 4;
	public static final int ESCAPE_JAVAL         = 5;
	public static final int ESCAPE_USERFORMAT    = 6;
	
	private static final String UNESCAPENCR = "unescapeNCR";
	private static final String UNESCAPECER = "unescapeCER";
	private static final String UNESCAPEJAVA = "unescapeJava";
	private static final String ESCAPEALL = "escapeAll";
	private static final String ESCAPENOTATION = "escapeNotation";
	private static final String USERFORMAT = "userFormat";
	private static final String USEBYTES = "useBytes";
	private static final String BOMONUTF8 = "BOMonUTF8";
	private static final String REPORTUNSUPPORTED = "reportUnsupported";

	public Parameters () {
		super();
	}
	
	public boolean getUnescapeNCR() {
		return getBoolean(UNESCAPENCR);
	}

	public void setUnescapeNCR(boolean unescapeNCR) {
		setBoolean(UNESCAPENCR, unescapeNCR);
	}

	public boolean getUnescapeCER() {
		return getBoolean(UNESCAPECER);
	}

	public void setUnescapeCER(boolean unescapeCER) {
		setBoolean(UNESCAPECER, unescapeCER);
	}

	public boolean getUnescapeJava() {
		return getBoolean(UNESCAPEJAVA);
	}

	public void setUnescapeJava(boolean unescapeJava) {
		setBoolean(UNESCAPEJAVA, unescapeJava);
	}

	public boolean getEscapeAll() {
		return getBoolean(ESCAPEALL);
	}

	public void setEscapeAll(boolean escapeAll) {
		setBoolean(ESCAPEALL, escapeAll);
	}

	public int getEscapeNotation() {
		return getInteger(ESCAPENOTATION);
	}

	public void setEscapeNotation(int escapeNotation) {
		setInteger(ESCAPENOTATION, escapeNotation);
	}

	public String getUserFormat() {
		return getString(USERFORMAT);
	}

	public void setUserFormat(String userFormat) {
		setString(USERFORMAT, userFormat);
	}

	public boolean getUseBytes() {
		return getBoolean(USEBYTES);
	}

	public void setUseBytes(boolean useBytes) {
		setBoolean(USEBYTES, useBytes);
	}

	public boolean getBOMonUTF8() {
		return getBoolean(BOMONUTF8);
	}

	public void setBOMonUTF8(boolean bOMonUTF8) {
		setBoolean(BOMONUTF8, bOMonUTF8);
	}

	public boolean getReportUnsupported() {
		return getBoolean(REPORTUNSUPPORTED);
	}

	public void setReportUnsupported(boolean reportUnsupported) {
		setBoolean(REPORTUNSUPPORTED, reportUnsupported);
	}


	public void reset() {
		super.reset();
		setUnescapeNCR(true);
		setUnescapeCER(true);
		setUnescapeJava(true);
		setEscapeAll(false);
		setEscapeNotation(0);
		setUserFormat("%d");
		setUseBytes(false);
		setBOMonUTF8(true);
		setReportUnsupported(true);
	}
}
