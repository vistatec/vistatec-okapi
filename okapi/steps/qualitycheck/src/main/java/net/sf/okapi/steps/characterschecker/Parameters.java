/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.characterschecker;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {

	private static final String CHECKCHARACTERS = "checkCharacters";
	private static final String CHECKALLOWEDCHARACTERS = "checkAllowedCharacters";
	private static final String CHARSET = "charset";
	private static final String EXTRACHARSALLOWED = "extraCharsAllowed";
	private static final String CORRUPTEDCHARACTERS = "corruptedCharacters";

	public Parameters() {
		super();
	}

	public boolean getCorruptedCharacters() {
		return getBoolean(CORRUPTEDCHARACTERS);
	}

	public void setCorruptedCharacters(boolean corruptedCharacters) {
		setBoolean(CORRUPTEDCHARACTERS, corruptedCharacters);
	}

	public boolean getCheckAllowedCharacters() {
		return getBoolean(CHECKALLOWEDCHARACTERS);
	}

	public void setCheckAllowedCharacters(boolean checkAllowedCharacters) {
		setBoolean(CHECKALLOWEDCHARACTERS, checkAllowedCharacters);
	}

	public boolean getCheckCharacters() {
		return getBoolean(CHECKCHARACTERS);
	}

	public void setCheckCharacters(boolean checkCharacters) {
		setBoolean(CHECKCHARACTERS, checkCharacters);
	}

	public String getCharset() {
		return getString(CHARSET);
	}

	public void setCharset(String charset) {
		setString(CHARSET, charset);
	}

	public String getExtraCharsAllowed() {
		return getString(EXTRACHARSALLOWED);
	}

	public void setExtraCharsAllowed(String extraCharsAllowed) {
		setString(EXTRACHARSALLOWED, extraCharsAllowed);
	}

	@Override
	public void reset() {
		super.reset();
		setCorruptedCharacters(true);
		setCheckAllowedCharacters(true);
		setCheckCharacters(false);
		setCharset("ISO-8859-1");
		setExtraCharsAllowed("");
	}

	@Override
	public void fromString(String data) {
		super.fromString(data);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
