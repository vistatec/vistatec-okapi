/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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
 * Implements {@link IEncoder} for XML format.
 */
public class XMLEncoder implements IEncoder {

	/**
	 * Parameter flag for escaping the greater-than characters.
	 */
	public static final String ESCAPEGT = "escapeGT";
	/**
	 * Parameter flag for escaping the non-breaking space characters.
	 */
	public static final String ESCAPENBSP = "escapeNbsp";
	/**
	 * Parameter flag for escaping the line-breaks.
	 */
	public static final String ESCAPELINEBREAK = "escapeLineBreak";
	
	/**
	 * Parameter flag for indicating that the {@link #QUOTEMODE} is defined. 
	 */
	public static final String QUOTEMODEDEFINED = "quoteModeDefined";
	
	/**
	 * Parameter flag for defining the quote mode.
	 */
	public static final String QUOTEMODE = "quoteMode";
	
	private CharsetEncoder chsEnc;
	private String lineBreak;
	private String encoding;
	private boolean escapeGT = false;
	private boolean escapeNbsp = false;
	private boolean escapeLineBreak = false;
	private QuoteMode quoteMode = QuoteMode.ALL;
	private IParameters params;

	public XMLEncoder(String encoding, String lineBreak,
			boolean escapeGT, boolean escapeNbsp, boolean escapeLineBreak,
			QuoteMode quoteMode) {
		super();
		this.escapeGT = escapeGT;
		this.escapeNbsp = escapeNbsp;
		this.escapeLineBreak = escapeLineBreak;
		this.quoteMode = quoteMode;
		setOptions(null, encoding, lineBreak);
	}

	public XMLEncoder() {
		super();
	}

	/**
	 * Sets the options for this encoder. This encoder supports the following
	 * parameters:
	 * <ul><li>escapeGT=true to converts '&gt;' characters to to <code>&amp;gt;</code>.</li>
	 * <li>escapeNbsp=true to converts non-breaking space to <code>&amp;#x00a0;</code>.</li>
	 * <li>escapeLineBreak=true to converts line-breaks to <code>&amp;#10;</code>.</li>
	 * </ul>
	 * @param params the parameters object with all the configuration information 
	 * specific to this encoder.
	 * @param encoding the name of the character set encoding to use.
	 * @param lineBreak the type of line break to use.
	 */
	@Override
	public void setOptions (IParameters params,
		String encoding,
		String lineBreak)
	{
		this.lineBreak = lineBreak;
		this.encoding = encoding; 
		// Use an encoder only if the output is not UTF-8/16
		// since those support all characters
		String enclc = encoding.toLowerCase();
		if ( enclc.equals("utf-8") || enclc.startsWith("utf-16") ) {
			chsEnc = null;
		}
		else {
			chsEnc = Charset.forName(encoding).newEncoder();
		}
		
		this.params = params;		
		if ( params != null ) {
			// Retrieve the options
			escapeGT = params.getBoolean(ESCAPEGT);
			escapeNbsp = params.getBoolean(ESCAPENBSP);
			escapeLineBreak = params.getBoolean(ESCAPELINEBREAK);
			if ( params.getBoolean(QUOTEMODEDEFINED) ) {
				quoteMode = QuoteMode.fromValue(params.getInteger(QUOTEMODE));
			}
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

	private char last = Character.MAX_VALUE;

	@Override
	public String encode (char value,
			EncoderContext context)
	{
		String rv = _encode(value, context);
		this.last = value;
		return rv;
	}

	private String _encode (char value,
			EncoderContext context)
	{
		switch ( value ) {
		case '<':
			return "&lt;";
		case '"':
			if ( quoteMode != QuoteMode.UNESCAPED ) return "&quot;";
			else return "\"";
		case '\'':
			switch ( quoteMode ) {
			case ALL:
				return "&apos;";
			case NUMERIC_SINGLE_QUOTES:
				return "&#39;";
			case UNESCAPED:
			case DOUBLE_QUOTES_ONLY:
				return "'";
			}
		case '&':
			return "&amp;";
		case '>':
			if ( escapeGT || last == ']' ) return "&gt;";
			else return ">";
		case '\r': // In XML this is a literal not a line-break
			return "&#13;";
		case '\n':
			if ( escapeLineBreak ) return "&#10;";
			else return lineBreak;
		case '\u00A0':
			if ( escapeNbsp ) {
				return "&#x00a0;";
			}
			// Else: fall through
		default:
			if ( value > 127 ) { // Extended chars
				// Store high surrogate for future use
				if ( Character.isHighSurrogate(value) ) {
					return "";
				}
				// Combine stored surrogate with current char to make a single codepoint
				if ( Character.isHighSurrogate(last) ) {
					int cp = Character.toCodePoint(last, value);
					String tmp = new String(Character.toChars(cp));
					if (( chsEnc != null ) && !chsEnc.canEncode(tmp) ) {
						return String.format("&#x%x;", cp);
					}
					else {
						return tmp;
					}
				}
				if (( chsEnc != null ) && ( !chsEnc.canEncode(value) )) {
					return String.format("&#x%04x;", (int)value);
				}
				else { // No encoder or char is supported
					return String.valueOf(value);
				}
			}
			else { // ASCII chars
				return String.valueOf(value);
			}
		}
	}

	@Override
	public String encode (int value,
			EncoderContext context)
	{
		if (Character.isSupplementaryCodePoint(value) ) {
			String tmp = new String(Character.toChars(value));
			if (( chsEnc != null ) && !chsEnc.canEncode(tmp) ) {
				return String.format("&#x%x;", value);
			}
			return tmp;
		}
		return encode((char)value, context);
	}

	@Override
	public String toNative (String propertyName,
		String value)
	{
		// PROP_ENCODING: Same value in native
		// PROP_LANGUGE: Same value in native
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
