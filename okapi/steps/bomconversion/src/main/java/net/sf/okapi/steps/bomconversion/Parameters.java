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

package net.sf.okapi.steps.bomconversion;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	private static final String REMOVEBOM = "removeBOM";
	private static final String ALSONONUTF8 = "alsoNonUTF8";
	
	public Parameters () {
		super();
	}
	
	public boolean getRemoveBOM() {
		return getBoolean(REMOVEBOM);
	}
	
	public void setRemoveBOM(boolean removeBOM) {
		setBoolean(REMOVEBOM, removeBOM);
	}
	
	public boolean getAlsoNonUTF8() {
		return getBoolean(ALSONONUTF8);
	}
	
	public void setAlsoNonUTF8(boolean alsoNonUTF8) {
		setBoolean(ALSONONUTF8, alsoNonUTF8);
	}

	public void reset() {
		super.reset();
		setRemoveBOM(false);;
		setAlsoNonUTF8(false);
	}
}
