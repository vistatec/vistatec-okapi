/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

import java.nio.charset.CharsetEncoder;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.resource.Property;

/**
 * Implements {@link IEncoder} for PO file format.
 */
public class POEncoder implements IEncoder {
	
	@Override
	public void setOptions (IParameters params,
		String encoding,
		String lineBreak)
	{
		//this.lineBreak = lineBreak;
	}

	@Override
	public String encode (String text,
			EncoderContext context)
	{
		return text;
	}

	@Override
	public String encode (char value,
			EncoderContext context)
	{
		return String.valueOf(value);
	}

	@Override
	public String encode (int value,
			EncoderContext context)
	{
		if ( Character.isSupplementaryCodePoint(value) ) {
			return new String(Character.toChars(value));
		}
		return String.valueOf((char)value); 
	}

	@Override
	public String toNative (String propertyName,
		String value)
	{
		if ( Property.APPROVED.equals(propertyName) ) {
			if (( value != null ) && ( value.equals("no") )) {
				return "fuzzy";
			}
			else { // Don't set the fuzzy flag
				return "";
			}
		}

		// No changes for the other values
		return value;
	}


	@Override
	public String getLineBreak () {
		return "\n";
	}

	@Override
	public CharsetEncoder getCharsetEncoder () {
		return null;
	}

	@Override
	public IParameters getParameters() {
		return null;
	}

	@Override
	public String getEncoding() {
		return "";
	}

}
