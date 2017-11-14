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

package net.sf.okapi.steps.uriconversion;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	
	public final static int UNESCAPE = 0;
	
	private static final String CONVERSIONTYPE = "conversionType";
	private static final String UPDATEALL = "updateAll";
	private static final String ESCAPELIST = "escapeList";

	public Parameters () {
		super();
	}
	
	public int getConversionType() {
		return getInteger(CONVERSIONTYPE);
	}

	public void setConversionType(int conversionType) {
		setInteger(CONVERSIONTYPE, conversionType);
	}

	public boolean getUpdateAll() {
		return getBoolean(UPDATEALL);
	}

	public void setUpdateAll(boolean updateAll) {
		setBoolean(UPDATEALL, updateAll);
	}

	public String getEscapeList() {
		return getString(ESCAPELIST);
	}

	public void setEscapeList(String escapeList) {
		setString(ESCAPELIST, escapeList);
	}

	public void reset () {
		super.reset();
		setConversionType(UNESCAPE);
		setUpdateAll(false);
		setEscapeList("%{}[]()&");
	}
}
