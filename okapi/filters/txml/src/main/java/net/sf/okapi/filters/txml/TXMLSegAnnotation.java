/*===========================================================================
  Copyright (C) 2011-2012 by the Okapi Framework contributors
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

package net.sf.okapi.filters.txml;

import net.sf.okapi.common.annotation.IAnnotation;

public class TXMLSegAnnotation implements IAnnotation {

	private String value;

	/**
	 * Create a new TXMLSegAnnotation object.
	 * @param value the value: 'b'=ws before, 'a'=ws after, 'ba'=ws before and after
	 */
	public TXMLSegAnnotation (String value) {
		this.value = (value==null ? "" : value);
	}
	
	public boolean hasWSBefore () {
		return (value.indexOf('b') > -1);
	}
	
	public boolean hasWSAfter () {
		return (value.indexOf('a') > -1);
	}
	
	@Override
	public String toString () {
		return value;
	}

}
