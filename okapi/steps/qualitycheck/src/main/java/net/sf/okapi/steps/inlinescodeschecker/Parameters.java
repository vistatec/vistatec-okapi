/*===========================================================================
  Copyright (C) 2016 by the Okapi Framework contributors
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

package net.sf.okapi.steps.inlinescodeschecker;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {

	private static final String CODEDIFFERENCE = "codeDifference";
	private static final String GUESSOPENCLOSE = "guessOpenClose";
	private static final String EXTRACODESALLOWED = "extraCodesAllowed";
	private static final String MISSINGCODESALLOWED = "missingCodesAllowed";
	private static final String TYPESTOIGNORE = "typesToIgnore";

	List<String> extraCodesAllowed;
	List<String> missingCodesAllowed;

	public Parameters() {
		super();
	}

	public String getTypesToIgnore() {
		return getString(TYPESTOIGNORE);
	}

	public void setTypesToIgnore(String typesToIgnore) {
		setString(TYPESTOIGNORE, typesToIgnore);
	}

	public boolean getCodeDifference() {
		return getBoolean(CODEDIFFERENCE);
	}

	public void setCodeDifference(boolean codeDifference) {
		setBoolean(CODEDIFFERENCE, codeDifference);
	}

	public boolean getGuessOpenClose() {
		return getBoolean(GUESSOPENCLOSE);
	}

	public void setGuessOpenClose(boolean guessOpenClose) {
		setBoolean(GUESSOPENCLOSE, guessOpenClose);
	}

	@Override
	public void reset() {
		super.reset();

		setCodeDifference(true);
		setGuessOpenClose(true);

		extraCodesAllowed = new ArrayList<String>();
		missingCodesAllowed = new ArrayList<String>();

		setTypesToIgnore("mrk;x-df-s;");
	}

	@Override
	public void fromString(String data) {
		super.fromString(data);

		// Allowed extra codes
		int count = buffer.getInteger(EXTRACODESALLOWED, 0);
		if (count > 0) {
			extraCodesAllowed.clear();
		}
		for (int i = 0; i < count; i++) {
			extraCodesAllowed.add(buffer.getString(String.format("%s%d", EXTRACODESALLOWED, i), ""));
		}
		// Allowed missing codes
		count = buffer.getInteger(MISSINGCODESALLOWED, 0);
		if (count > 0) {
			missingCodesAllowed.clear();
		}
		for (int i = 0; i < count; i++) {
			missingCodesAllowed.add(buffer.getString(String.format("%s%d", MISSINGCODESALLOWED, i), ""));
		}

	}

	@Override
	public String toString() {
		buffer.setInteger(EXTRACODESALLOWED, extraCodesAllowed.size());
		for (int i = 0; i < extraCodesAllowed.size(); i++) {
			buffer.setString(String.format("%s%d", EXTRACODESALLOWED, i), extraCodesAllowed.get(i));
		}
		buffer.setInteger(MISSINGCODESALLOWED, missingCodesAllowed.size());
		for (int i = 0; i < missingCodesAllowed.size(); i++) {
			buffer.setString(String.format("%s%d", MISSINGCODESALLOWED, i), missingCodesAllowed.get(i));
		}

		return super.toString();
	}
}
