/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.common.encoder;

import net.sf.okapi.common.resource.Property;

/**
 * Implements {@link IEncoder} for TS file format.
 */
public class TSEncoder extends XMLEncoder {
	
	@Override
	public String encode (String text,
			EncoderContext context)
	{
		StringBuilder escaped = new StringBuilder();
		char ch;
		for ( int i=0; i<text.length(); i++ ) {
			ch = text.charAt(i);
			if ((ch==0x9) || (ch==0xA)|| (ch==0xD) || ((ch >= 0x20) && (ch<=0xD7FF)) || ((ch >= 0xE000) && (ch<=0xFFFD)) || ((ch >= 0x10000) && (ch<=0x10FFFF))) {
				escaped.append(super.encode(ch, context));
			}
			else {
				escaped.append("<byte value=\"x"+Integer.toHexString(ch)+"\">");
			}
		}
		return escaped.toString();
	}

	@Override
	public String toNative (String propertyName,
		String value)
	{
		// PROP_LANGUAGE: Not applicable
		// PROP_ENCODING: No change
		
		// Approve property is resolved with tyep attribute
		if ( Property.APPROVED.equals(propertyName) ) {
			if (( value != null ) && ( value.equals("yes") )) {
				return "";
			}else{
				return " type=\"unfinished\"";
			}
		}
		return value;		
	}

}
