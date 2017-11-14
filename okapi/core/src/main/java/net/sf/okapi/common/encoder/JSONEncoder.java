/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import net.sf.okapi.common.IParameters;

/**
 * Implements {@link IEncoder} for JSON format.
 */
public class JSONEncoder implements IEncoder {

	private CharsetEncoder chsEnc;
	private boolean escapeExtendedChars;
	private boolean escapeForwardSlashes;

	private String lineBreak = "\n";
	private String encoding;
	private IParameters params;
	private char last = Character.MAX_VALUE;

	/**
	 * Creates a new PropertiesEncoder object, with US-ASCII as the encoding.
	 */
	public JSONEncoder () {
		escapeExtendedChars = false;
		escapeForwardSlashes = true;
		chsEnc = Charset.forName("UTF-8").newEncoder();
	}

	public void setOptions (IParameters params,
		String encoding,
		String lineBreak)
	{
		chsEnc = Charset.forName(encoding).newEncoder();
		this.lineBreak = lineBreak;
		this.encoding = encoding;
		this.params = params;
		// Get the output options
		if ( params != null ) {
			escapeExtendedChars = params.getBoolean("escapeExtendedChars");
			escapeForwardSlashes = params.getBoolean("escapeForwardSlashes");
		}
	}

	@Override
	public String encode (String text,
			EncoderContext context)
	{
		if ( text == null ) return "";

		StringBuffer sbTmp = new StringBuffer(text.length());
		for ( int i=0; i<text.length(); i++ ) {
			sbTmp.append(encode(text.charAt(i), context));
		}
		return sbTmp.toString();
	}

	@Override
	public String encode (char value, EncoderContext context)
	{
		String rv = _encode(value, context);
		this.last = value;
		return rv;
	}

	protected String _encode (char value,
			EncoderContext context)
	{
		if ( value > 127 ) {
			// Store high surrogate for future use
			if ( Character.isHighSurrogate(value) ) {
				return "";
			}
			// Combine stored surrogate with current char to make a single codepoint
			if ( Character.isHighSurrogate(last) ) {
				int cp = Character.toCodePoint(last, value);
				String tmp = new String(Character.toChars(cp));
				if (escapeExtendedChars || !chsEnc.canEncode(tmp) ) {
					return String.format("\\u%04x\\u%04x",
							(int)tmp.charAt(0), (int)tmp.charAt(1));
				}
				else {
					return tmp;
				}
			}
			if ( escapeExtendedChars || !chsEnc.canEncode(value) ) {
				return String.format("\\u%04x", (int)value);
			}
			else {
				return String.valueOf(value);
			}
		}
		else {
			switch ( value ) {
			case '\b':
				return "\\b";
			case '\f':
				return "\\f";
			case '\n':
				return "\\n";
			case '\r':
				return "\\r";
			case '\t':
				return "\\t";
			case '/':
				return escapeForwardSlashes ? "\\" + value : String.valueOf(value);
			case '"':
			case '\\':
				return "\\" + value;
			default:
				return String.valueOf(value);
			}
		}
	}

	@Override
	public String encode (int value,
			EncoderContext context)
	{
		if ( Character.isSupplementaryCodePoint(value) ) {
			String tmp = new String(Character.toChars(value));
			if ( escapeExtendedChars || !chsEnc.canEncode(tmp) ) {
				return String.format("\\u%04x\\u%04x",
					(int)tmp.charAt(0), (int)tmp.charAt(1));
			}
			return tmp;
		}
		return encode((char)value, context);
	}

	public String toNative (String propertyName,
		String value)
	{
		// No changes for the other values
		return value;
	}

	@Override
	public String getLineBreak () {
		return lineBreak;
	}

	@Override
	public CharsetEncoder getCharsetEncoder () {
		return chsEnc;
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

}
