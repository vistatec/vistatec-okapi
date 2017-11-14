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

import net.sf.okapi.common.HTMLCharacterEntities;
import net.sf.okapi.common.IParameters;

/**
 * Implements {@link IEncoder} for HTML format.
 */
public class HtmlEncoder implements IEncoder {
	/**
	 * Parameter flag for indicating that the {@link #QUOTEMODE} is defined. 
	 */
	public static final String QUOTEMODEDEFINED = "quoteModeDefined";
	
	/**
	 * Parameter flag for defining the quote mode.
	 */
	public static final String QUOTEMODE = "quoteMode";
			
	/** HTML content attribute */
	public static final String CONTENT = "content";
	
	/** HTML charset identifier */
	public static final String CHARSET = "charset";

	private CharsetEncoder chsEnc;
	private String lineBreak;
	private String encoding;
	private QuoteMode quoteMode = QuoteMode.NUMERIC_SINGLE_QUOTES;
	private String charsToCER = null;
	private HTMLCharacterEntities entities;
	private IParameters params;
	
	public HtmlEncoder(String encoding, String lineBreak, QuoteMode quoteMode) {
		super();
		this.quoteMode = quoteMode;
		setOptions(null, encoding, lineBreak);
	}

	public HtmlEncoder() {
		super();
	}

	@Override
	public void setOptions (IParameters params,
		String encoding,
		String lineBreak)
	{
		this.lineBreak = lineBreak;
		this.encoding = encoding;
		// Use an encoder only if the output is not UTF-8/16
		// since those support all characters
		if ( "utf-8".equals(encoding) || "utf-16".equals(encoding) ) {
			chsEnc = null;
		}
		else {
			chsEnc = Charset.forName(encoding).newEncoder();
		}

		this.params = params;
		// Get options from the filter's configuration		
		if ( params != null ) {
			
			// Retrieve the options
			charsToCER = params.getString("escapeCharacters");
			
			if (params.getBoolean(QUOTEMODEDEFINED)) {
				int q = params.getInteger(QUOTEMODE);
				// -1 value from AbstractMarkupParameters means no value (we use the default)
				if (q >= 0 && q <= 3) {
					quoteMode = QuoteMode.fromValue(params.getInteger(QUOTEMODE));
				}
			}		
		}
	}

	@Override
	public String encode (String text,
			EncoderContext context)
	{
		if ( text == null ) return "";
		boolean escapeGT = false;
		
		StringBuffer sbTmp = new StringBuffer(text.length());
		char ch;
		for ( int i=0; i<text.length(); i++ ) {
			ch = text.charAt(i);
			switch ( ch ) {
			case '<':
				sbTmp.append("&lt;");
				continue;
			case '>':
				if ( escapeGT ) sbTmp.append("&gt;");
				else {
					if (( i > 0 ) && ( text.charAt(i-1) == ']' )) sbTmp.append("&gt;");
					else sbTmp.append('>');
				}
				continue;
			case '&':
				sbTmp.append("&amp;");
				continue;
			case '"':
				if (quoteMode != QuoteMode.UNESCAPED) sbTmp.append("&quot;");
				else sbTmp.append('"');
				continue;
			case '\'':
				switch ( quoteMode ) {
				case ALL:				
					sbTmp.append("&apos;");
					break;
				case NUMERIC_SINGLE_QUOTES:
					sbTmp.append("&#39;");
					break;
				case UNESCAPED:
				case DOUBLE_QUOTES_ONLY:
					sbTmp.append(ch);
					break;
				}
				continue;
			case '\n':
				sbTmp.append(lineBreak);
				break;
			default:
				if ( ch > 127 ) { // Extended chars
					if ( Character.isHighSurrogate(ch) ) {
						int cp = text.codePointAt(i++);
						String tmp = new String(Character.toChars(cp));
						if (( chsEnc != null ) && !chsEnc.canEncode(tmp) ) {
							sbTmp.append(String.format("&#x%x;", cp));
						}
						else {
							sbTmp.append(tmp);
						}
					}
					else { // Should be able to fold to char, supplementary case will be treated
						String cer = checkCER(ch);
						if ( cer != null ) {
							sbTmp.append(cer);
							continue;
						}
						// Else: fall back to normal character process
						if (( chsEnc != null ) && !chsEnc.canEncode(ch) ) {
							sbTmp.append(String.format("&#x%04x;", (int)ch));
						}
						else { // No encoder or char is supported
							sbTmp.append(String.valueOf(ch));
						}
					}
				}
				else { // ASCII chars
					sbTmp.append(ch);
				}
				continue;
			}
		}
		return sbTmp.toString();
	}

	/**
	 * Checks if the character needs/can be represented as a CER.
	 * @param ch the character to process.
	 * @return the string representing the CER or null if the character needs to be processed as usual.
	 */
	private String checkCER (char ch) {
		if (( charsToCER != null ) && ( charsToCER.indexOf(ch) > -1 )) {
			if ( entities == null ) {
				entities = new HTMLCharacterEntities();
				entities.ensureInitialization(false);
			}
			String name = entities.getName(ch);
			if ( name == null ) return null;
			else return "&"+name+";";
		}
		return null;
	}
	
	@Override
	public String encode (char value,
			EncoderContext context)
	{
		switch ( value ) {
		case '<':
			return "&lt;";
		case '\"':
			if (quoteMode != QuoteMode.UNESCAPED) return "&quot;";
			else  return "\"";
		case '\'':
			switch (quoteMode) {
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
		case '\n':
			return lineBreak;
		default:
			if ( value > 127 ) { // Extended chars
				String cer = checkCER(value);
				if ( cer != null ) {
					return cer;
				}
				// Else: fall back to normal character process
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
		switch ( value ) {
		case '<':
			return "&lt;";
		case '\"':
			if (quoteMode != QuoteMode.UNESCAPED) return "&quot;";
			else  return "\"";
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
		case '\n':
			return lineBreak;
		default:
			if ( value > 127 ) { // Extended chars
				if ( Character.isSupplementaryCodePoint(value) ) {
					String tmp = new String(Character.toChars(value));
					if (( chsEnc != null ) && !chsEnc.canEncode(tmp) ) {
						return String.format("&#x%x;", value);
					}
					return tmp;
				}
				String cer = checkCER((char)value);
				if ( cer != null ) {
					return cer;
				}
				// Else: fall back to normal character process
				// Should be able to fold to char, supplementary case will be treated
				if (( chsEnc != null ) && !chsEnc.canEncode((char)value) ) {
					return String.format("&#x%04x;", value);
				}
				else { // No encoder or char is supported
					return String.valueOf((char)value);
				}
			}
			else { // ASCII chars
				return String.valueOf((char)value);
			}
		}
	}

	@Override
	public String toNative (String propertyName,
		String value)
	{
		return value;
	}

	@Override
	public String getLineBreak () {
		return this.lineBreak;
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
