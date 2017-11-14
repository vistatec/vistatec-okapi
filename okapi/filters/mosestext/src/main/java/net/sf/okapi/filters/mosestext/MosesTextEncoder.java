/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.filters.mosestext;

import java.nio.charset.CharsetEncoder;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.IEncoder;

public class MosesTextEncoder implements IEncoder {

	private static final String LINEBREAK = "<lb/>";
	
	@Override
	public String encode (String text,
			EncoderContext context)
	{
		if ( text == null ) return "";
		StringBuffer sbTmp = new StringBuffer(text.length());
		char ch;
		for ( int i=0; i<text.length(); i++ ) {
			ch = text.charAt(i);
			switch ( ch ) {
			case '<':
				sbTmp.append("&lt;");
				continue;
			case '&':
				sbTmp.append("&amp;");
				continue;
			case '\r': // In XML this is a literal not a line-break
				sbTmp.append("&#13;");
				break;
			case '\n':
				sbTmp.append(LINEBREAK);
				break;
			default:
				if ( ch > 127 ) { // Extended chars
					if ( Character.isHighSurrogate(ch) ) {
						int cp = text.codePointAt(i++);
						String tmp = new String(Character.toChars(cp));
						sbTmp.append(tmp);
					}
					else {
						sbTmp.append(ch);
					}
				}
				else { // ASCII chars
					sbTmp.append(ch);
				}
			}
		}
		return sbTmp.toString();
	}

	@Override
	public String encode (int value,
			EncoderContext context)
	{
		switch ( value ) {
		case '<':
			return "&lt;";
		case '&':
			return "&amp;";
		case '\r': // In XML this is a literal not a line-break
			return "&#13;";
		case '\n':
			return LINEBREAK;
		default:
			if ( Character.isSupplementaryCodePoint(value) ) {
				return new String(Character.toChars(value)).replace("\n", LINEBREAK);
			}
			return String.valueOf((char)value).replace("\n", LINEBREAK); 
		}
	}

	@Override
	public String encode (char value,
			EncoderContext context)
	{
		switch ( value ) {
		case '<':
			return "&lt;";
		case '&':
			return "&amp;";
		case '\r': // In XML this is a literal not a line-break
			return "&#13;";
		case '\n':
			return LINEBREAK;
		default:
			return String.valueOf(value);
		}
	}

	@Override
	public CharsetEncoder getCharsetEncoder () {
		return null;
	}

	@Override
	public String getLineBreak () {
		return LINEBREAK;
	}

	@Override
	public void setOptions (IParameters params,
		String encoding,
		String lineBreak)
	{
		// Line-break is LINEBREAK
		// Encoding is always UTF-8
	}

	@Override
	public String toNative (String propertyName,
		String value)
	{
		return value;
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
