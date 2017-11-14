/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffkit.codec;


public interface ICodec {

	/**
	 * Checks if a given code point can be encoded by this codec. 
	 * @param codePoint the given code point.
	 * @return true if this codec handles the given code point.
	 */
	boolean canEncode(int codePoint);

	/**
	 * Encodes a given text with this codec.
	 * @param text the text to encode.
	 * @return the encoded text.
	 */
	String encode (String text);

	/**
	 * Decodes a given text with this codec.
	 * @param text the text to decode.
	 * @return the decoded text.
	 */
	String decode (String text);

}
