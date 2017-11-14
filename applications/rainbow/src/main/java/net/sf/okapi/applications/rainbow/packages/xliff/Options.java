/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.packages.xliff;

import net.sf.okapi.common.StringParameters;

public class Options extends StringParameters{

	private static final String GMODE = "gMode";
	private static final String INCLUDENOTRANSLATE = "includeNoTranslate";
	private static final String SETAPPROVEDASNOTRANSLATE = "setApprovedAsNoTranslate";
	private static final String MESSAGE = "message";
	private static final String COPYSOURCE = "copySource";
	private static final String INCLUDEALTTRANS = "includeAltTrans";
	
	public boolean getIncludeNoTranslate () {
		return getBoolean(INCLUDENOTRANSLATE);
	}

	public void setIncludeNoTranslate (boolean includeNoTranslate) {
		setBoolean(INCLUDENOTRANSLATE, includeNoTranslate);
	}
	
	public boolean getSetApprovedAsNoTranslate () {
		return getBoolean(SETAPPROVEDASNOTRANSLATE);
	}

	public void setSetApprovedAsNoTranslate (boolean setApprovedAsNoTranslate) {
		setBoolean(SETAPPROVEDASNOTRANSLATE, setApprovedAsNoTranslate);
	}

	public boolean getCopySource () {
		return getBoolean(COPYSOURCE);
	}
	
	public void setCopySource (boolean copySource) {
		setBoolean(COPYSOURCE, copySource);
	}

	
	public boolean getGMode() {
		return getBoolean(GMODE);
	}

	public void setGMode(boolean gMode) {
		setBoolean(GMODE, gMode);
	}

	public String getMessage() {
		return getString(MESSAGE);
	}

	public void setMessage(String message) {
		setString(MESSAGE, message);
	}
	
	public boolean getIncludeAltTrans () {
		return getBoolean(INCLUDEALTTRANS);
	}

	public void setIncludeAltTrans (boolean includeAltTrans) {
		setBoolean(INCLUDEALTTRANS, includeAltTrans);
	}

	public Options () {
		super();
	}
	
	public void reset() {
		super.reset();
		setGMode(false);
		setIncludeNoTranslate(true);
		setSetApprovedAsNoTranslate(false);
		setMessage("");
		setCopySource(true);
		setIncludeAltTrans(true);
	}

	public void fromString (String data) {
		super.fromString(data);
		
		// Make sure the we can merge later
		if ( !getIncludeNoTranslate()) {
			setSetApprovedAsNoTranslate(false);
		}
	}
}
