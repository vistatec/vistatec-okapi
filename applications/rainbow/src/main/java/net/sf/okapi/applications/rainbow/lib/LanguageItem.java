/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.lib;

import net.sf.okapi.common.Util.SUPPORTED_OS;

public class LanguageItem {

	public String name;
	public String code;
	public String encodingW;
	public String encodingM;
	public String encodingU;
	public int lcid;
	
	public String toString () {
		return name;
	}
	
	public String getEncoding (SUPPORTED_OS osType) {
		String sTmp;
		switch ( osType ) {
		case MAC:
			sTmp = encodingM;
			break;
		case LINUX:
			sTmp = encodingU;
			break;
		default:
			sTmp = encodingW;
			break;
		}
		if ( sTmp == null ) return encodingW;
		else return sTmp;
	}
	
	public void setEncoding (String value,
		SUPPORTED_OS osType)
	{
		switch ( osType ) {
		case MAC:
			encodingM = value;
			break;
		case LINUX:
			encodingU = value;
			break;
		default:
			encodingW = value;
			break;
		}
	}
	
}
