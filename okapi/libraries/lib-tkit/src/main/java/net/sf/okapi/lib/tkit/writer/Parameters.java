/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.tkit.writer;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	static final String REMOVETARGET = "removeTarget"; //$NON-NLS-1$
	static final String MESSAGE = "message"; //$NON-NLS-1$
	
	public Parameters () {
		super();
	}
	
	public void reset() {
		super.reset();
		setMessage("");
		setRemoveTarget(true);
	}

	public void fromString (String data) {
		super.fromString(data);
	}

	public String getMessage() {
		return getString(MESSAGE);
	}

	public void setMessage(String message) {
		setString(MESSAGE, message);
	}

	public boolean isRemoveTarget() {
		return getBoolean(REMOVETARGET);
	}

	public void setRemoveTarget(boolean removeTarget) {
		setBoolean(REMOVETARGET, removeTarget);
	}
}
