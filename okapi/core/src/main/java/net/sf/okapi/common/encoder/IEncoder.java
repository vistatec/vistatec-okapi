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

import java.nio.charset.CharsetEncoder;

import net.sf.okapi.common.IParameters;

/**
 * Provides common methods to encode/escape text to a specific format.
 * <p>Important: Each class implementing this interface must have a nullary constructor, so the object 
 * can be instantiated using the Class.fromName() methods by the EncoderManager.
 */
public interface IEncoder {

	/**
	 * Sets the options for this encoder.
	 * @param params the parameters object with all the configuration information 
	 * specific to this encoder.
	 * @param encoding the name of the charset encoding to use.
	 * @param lineBreak the type of line break to use.
	 */
	public void setOptions (IParameters params,
		String encoding,
		String lineBreak);
	
	/**
	 * Encodes a given text with this encoder.
	 * @param text the text to encode.
	 * @param context the context of the text: 0=text, 1=skeleton, 2=inline.
	 * @return the encoded text.
	 */
	public String encode (String text, EncoderContext context);
	
	/**
	 * Encodes a given code-point with this encoding. If this method is called from
	 * a loop it is assumed that the code point is tested by the caller to know 
	 * if it is a supplemental one or not and and any index update to skip the
	 * low surrogate part of the pair is done on the caller side.
	 * @param codePoint the code-point to encode.
	 * @param context the context of the character: 0=text, 1=skeleton, 2=inline.
	 * @return the encoded character (as a string since it can be now made up of
	 * more than one character).
	 */
	public String encode (int codePoint, EncoderContext context);
	
	/**
	 * Encodes a given character with this encoding.
	 * @param value the character to encode.
	 * @param context the context of the character: 0=text, 1=skeleton, 2=inline.
	 * @return the encoded character 9as a string since it can be now made up of
	 * more than one character).
	 */
	public String encode (char value, EncoderContext context);
	
	/**
	 * Converts any property values from its standard representation to
	 * the native representation for this encoder.
	 * @param propertyName the name of the property.
	 * @param value the standard value to convert.
	 * @return the native representation of the given value.
	 */
	public String toNative (String propertyName,
		String value);

	/**
	 * Gets the line-break to use for this encoder.
	 * @return the line-break used for this encoder.
	 */
	public String getLineBreak ();

	/**
	 * Gets the name of the charset encoding to use.
	 * @return the charset encoding used for this encoder.
	 */
	public String getEncoding();
	
	/**
	 * Gets the character set encoder used for this encoder.
	 * @return the character set encoder used for this encoder. This can be null.
	 */
	public CharsetEncoder getCharsetEncoder ();
	
	/**
	 * Gets the parameters object with all the configuration information 
	 * specific to this encoder. 
	 * @return the parameters object used for this encoder. This can be null.
	 */
	public IParameters getParameters ();
	
}
