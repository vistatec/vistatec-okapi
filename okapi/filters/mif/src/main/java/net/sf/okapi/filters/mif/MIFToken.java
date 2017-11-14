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
===========================================================================*/

package net.sf.okapi.filters.mif;

import net.sf.okapi.common.resource.Code;

class MIFToken {

	public static final int TYPE_NULL = 0;
	public static final int TYPE_STRING = 1;
	public static final int TYPE_CODE = 2;
	
	private int type;
	private String stringValue;
	private Code codeValue;
	private boolean isLast;

	public MIFToken () {
		type = TYPE_NULL;
	}
	
	public MIFToken (String value) {
		type = TYPE_STRING;
		stringValue = value;
	}
	
	public MIFToken (Code value) {
		type = TYPE_CODE;
		codeValue = value;
	}
	
	@Override
	public String toString () {
		if ( type == TYPE_STRING ) return stringValue;
		else return "";
	}
	
	public void setString (String value) {
		type = TYPE_STRING;
		stringValue = value;
	}
	
	public String getString () {
		return stringValue;
	}
	
	public Code getCode () {
		return codeValue;
	}
	
	public void setLast (boolean value) {
		isLast = value;
	}
	
	public boolean isLast () {
		return isLast;
	}
	
	public void setType (int value) {
		type = value;
	}
	
	public int getType () {
		return type;
	}
	
}
