/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.lib;

import net.sf.okapi.common.IHelp;

public interface ILog {

	public boolean beginProcess (String p_sText);

	boolean beginTask (String p_sText);

	void cancel (boolean p_bAskConfirmation);

	void clear ();

	boolean canContinue ();

	void endProcess (String p_sText);

	void endTask (String p_sText);

	boolean error (String p_sText);

	public long getCallerData ();

	int getErrorAndWarningCount ();

	int getErrorCount ();

	int getWarningCount ();

	public void hide ();

	public boolean inProgress ();

	public boolean isVisible ();

	boolean message (String p_sText);

	boolean newLine ();

	void save (String p_sPath);

	public void setCallerData (long p_lData);

	public void setHelp (IHelp helpParam,
		String helpPath);

	boolean setLog (int p_nType,
		int p_nValue,
		String p_sValue);

	public void setMainProgressMode (int p_nValue);

	public boolean setOnTop (boolean p_bValue);

	void setSubProgressMode (int p_nValue);

	void setTitle (String p_sValue);

	public void show ();

	boolean warning (String p_sText);
}
