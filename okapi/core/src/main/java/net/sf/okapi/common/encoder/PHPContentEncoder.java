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

/**
 * Implements {@link IEncoder} for JSON format.
 */
public class PHPContentEncoder implements IEncoder {
	
//	private CharsetEncoder chsEnc;
	
	/**
	 * Creates a new PHPContentEncoder object, with US-ASCII as the encoding.
	 */
	public PHPContentEncoder () {
//		chsEnc = Charset.forName("us-ascii").newEncoder();
	}
	
	@Override
	public void setOptions (IParameters params,
		String encoding,
		String lineBreak)
	{
//		chsEnc = Charset.forName(encoding).newEncoder();
	}

	@Override
	public String encode (String text,
			EncoderContext context)
	{
		StringBuilder escaped = new StringBuilder();
		char ch;
		for ( int i=0; i<text.length(); i++ ) {
			ch = text.charAt(i);
			
			if ( ch > 127 ) { // Extended chars
				if ( Character.isHighSurrogate(ch) ) {
					int cp = text.codePointAt(i++);
					String tmp = new String(Character.toChars(cp));
					escaped.append(tmp);
				}
				else { // Should be able to fold to char, supplementary case will be treated
					escaped.append(String.valueOf(ch));
				}
			}
			else { // ASCII chars
				switch ( ch ) {
//				case '\n':
//					escaped.append("\\n");
//					break;
//				case '\t':
//					escaped.append("\\t");
//					break;
				default:
					escaped.append(ch);
					break;
				}
			}
		}
		return escaped.toString();
	}

	@Override
	public String encode (char value,
			EncoderContext context)
	{
		if ( value > 127 ) {
			return String.valueOf(value);
		}
		else {
			switch ( value ) {
//			case '\n':
//				return "\\n";
//			case '\t':
//				return "\\t";
			default:
				return String.valueOf(value);
			}
		}
	}

	@Override
	public String encode (int value,
			EncoderContext context)
	{
		if ( value > 127 ) {
			if ( Character.isSupplementaryCodePoint(value) ) {
				String tmp = new String(Character.toChars(value));
				return tmp;
			}
			else { // Extended not supplemental
				return String.valueOf((char)value);
			}
		}			
		else { // Non-extended
			switch ( (char)value ) {
//			case '\n':
//				return "\\n";
//			case '\t':
//				return "\\t";
			default:
				return String.valueOf((char)value);
			}
		}
	}

	@Override
	public String toNative (String propertyName,
		String value)
	{
		// No changes
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
