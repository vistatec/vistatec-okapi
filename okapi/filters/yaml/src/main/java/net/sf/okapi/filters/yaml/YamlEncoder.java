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

package net.sf.okapi.filters.yaml;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.StreamReader;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.StringUtil;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.filters.yaml.parser.YamlScalarTypes;

/**
 * Implements {@link IEncoder} for YAMLformat.
 * encoder logic comes from snakeyaml (http://www.apache.org/licenses/LICENSE-2.0)
 */
public class YamlEncoder implements IEncoder {		
	private static final Map<Character, String> ESCAPE_REPLACEMENTS = new HashMap<Character, String>();
    
    static {
        ESCAPE_REPLACEMENTS.put('\0', "0");
        ESCAPE_REPLACEMENTS.put('\u0007', "a");
        ESCAPE_REPLACEMENTS.put('\u0008', "b");
        ESCAPE_REPLACEMENTS.put('\u0009', "t");
        ESCAPE_REPLACEMENTS.put('\n', "n");
        ESCAPE_REPLACEMENTS.put('\u000B', "v");
        ESCAPE_REPLACEMENTS.put('\u000C', "f");
        ESCAPE_REPLACEMENTS.put('\r', "r");
        ESCAPE_REPLACEMENTS.put('\u001B', "e");
        ESCAPE_REPLACEMENTS.put('"', "\"");
        ESCAPE_REPLACEMENTS.put('\\', "\\");
        ESCAPE_REPLACEMENTS.put('\u0085', "N");
        ESCAPE_REPLACEMENTS.put('\u00A0', "_");
        ESCAPE_REPLACEMENTS.put('\u2028', "L");
        ESCAPE_REPLACEMENTS.put('\u2029', "P");
    }
    
	private boolean escapeNonAscii;
	private String lineBreak = "\n";
	private String encoding;
	private Parameters params;
	private Yaml yaml;
	private CharsetEncoder chsEnc;
	private YamlScalarTypes scalarType;
	private boolean illegalCharWarning;
	
	/**
	 * Creates a new YamlEncoder that does basic string decoding.
	 * More extensive support like adding quotes to string isn't possible
	 * as we don't have the full TextUnit here. If a PLAIN or SINGLE type string 
	 * needs quotes because of a new escape char then we can't do it here but
	 * must wait until all TextUnit's are returned for the scalar block. This
	 * is the normal case when a subfilter is called.
	 * 
	 * SnakeYaml should handle these cases: Escape codes: Numeric : { "\x12":
	 * 8-bit, "\u1234": 16-bit, "\U00102030": 32-bit } Protective: {
	 * "\\": '\', "\"": '"', "\ ": ' ', "\<TAB>": TAB } C : { "\0": NUL, "\a":
	 * BEL, "\b": BS, "\f": FF, "\n": LF, "\r": CR, "\t": TAB, "\v": VTAB }
	 * Additional: { "\e": ESC, "\_": NBSP, "\N": NEL, "\L": LS, "\P": PS }
	 */
	public YamlEncoder () {
		chsEnc = Charset.forName("UTF-8").newEncoder();
		escapeNonAscii = false;
		yaml = new Yaml(); 
		scalarType = YamlScalarTypes.PLAIN;
		illegalCharWarning = false;
	}
	
	@Override
	public void setOptions (IParameters params,
		String encoding,
		String lineBreak)
	{
		chsEnc = Charset.forName(encoding).newEncoder();
		this.lineBreak = lineBreak;
		this.encoding = encoding;
		this.params = (Parameters) params;
		// Get the output options
		if ( params != null ) {
			escapeNonAscii = this.params.getEscapeNonAscii();
		}
	}

	@Override
	public String encode(String text, EncoderContext context)
	{
		switch(context) {
			case INLINE:
				return text;
			case SKELETON:
				return text;
			case TEXT:									
				if (scalarType == YamlScalarTypes.DOUBLE || scalarType == YamlScalarTypes.SINGLE) {		
					return encodeString(text);
				}
		}
		return text;
	}
	
	private String encodeString(String s) {
		StringBuilder t = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			int cp = s.codePointAt(i);
			t.append(encode(cp, EncoderContext.TEXT));
		}
		return t.toString();
	}

	@Override
	public String encode(char ch, EncoderContext context)
	{	
		String data;
		if (scalarType == YamlScalarTypes.LITERAL || 
				scalarType == YamlScalarTypes.FOLDED || 
				scalarType == YamlScalarTypes.PLAIN) {
			return String.valueOf(ch);
		}
		
		if (scalarType == YamlScalarTypes.SINGLE) {
			 if (ch == '\'') {
				 return "''";
	         }
			 return String.valueOf(ch);
		}
		
		if (scalarType == YamlScalarTypes.DOUBLE) {		        
			if (ESCAPE_REPLACEMENTS.containsKey(ch)) {
	            data = "\\" + ESCAPE_REPLACEMENTS.get(ch);
	        } else if (this.escapeNonAscii || !isPrintable(ch)) {
	            // if !allowUnicode or the character is not printable,
	            // we must encode it
	            if (ch <= '\u00FF') {
	                String s = "0" + Integer.toString(ch, 16);
	                data = "\\x" + s.substring(s.length() - 2);
	            } else if (ch >= '\uD800' && ch <= '\uDBFF') {
	                 String s = "000" + Integer.toString(ch, 16);
	                 data = "\\u" + s.substring(s.length() - 4);
	            } else {
	                String s = "000" + Integer.toString(ch, 16);
	                data = "\\u" + s.substring(s.length() - 4);
	            }
	        } else {
	            data = String.valueOf(ch);
	        }		
			return data;
		}
		
		// shouldn't happen
		return String.valueOf(ch);
	}

	private boolean isPrintable(final char c) {
		return (c >= '\u0020' && c <= '\u007E') || c == '\n' || c == '\r' || c == '\t' || c == '\u0085'
				|| (c >= '\u00A0' && c <= '\uD7FF') || (c >= '\uE000' && c <= '\uFFFD');
	}

	@Override
	public String encode (int value,
			EncoderContext context)
	{
		if (value > 127) {
			if (Character.isSupplementaryCodePoint(value)) {
				return new String(Character.toChars(value));
			} else {
				return encode((char)value, context);
			}
		} else {
			return encode((char)value, context);
		}
	}

	@Override
	public String toNative (String propertyName,String value) {
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

	public boolean isIllegalCharWarning() {
		return illegalCharWarning;
	}

	public void setIllegalCharWarning(boolean illegalCharWarning) {
		this.illegalCharWarning = illegalCharWarning;
	}

	public YamlScalarTypes getScalarType() {
		return scalarType;
	}

	public void setScalarType(YamlScalarTypes scalarType) {
		this.scalarType = scalarType;
	}
}
