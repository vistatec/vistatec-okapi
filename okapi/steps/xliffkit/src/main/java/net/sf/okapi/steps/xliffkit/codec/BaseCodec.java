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

/**
 * Base class for text encoders/decoders. Subclasses are expected to override methods of this class
 * based on the subclass implementation logic.   
 */
public abstract class BaseCodec implements ICodec {

	@Override
	public final String encode(String text) {
		text = doEncodePreprocess(text);
		text = doEncode(text);		
		return doEncodePostprocess(text);
	}
	
	@Override
	public final String decode(String text) {
		text = doDecodePreprocess(text);
		text = doDecode(text);
		return doDecodePostprocess(text);
	}
	
	protected String doEncode(String text) {
		StringBuilder sb = new StringBuilder();
		
		final int length = text.length();
		
		for (int offset = 0; offset < length; ) {
		   final int codePoint = text.codePointAt(offset);

		   if (canEncode(codePoint)) {
			   sb.append(doEncode(codePoint));
		   }
		   else {
			   sb.append(Character.toChars(codePoint));
		   }

		   offset += Character.charCount(codePoint);
		}
		return sb.toString();
	}
	
	protected String doEncode(int codePoint) {
		return new String(Character.toChars(codePoint));
	}
	
	protected String doDecode(String text) {
		StringBuilder sb = new StringBuilder();
		
		final int length = text.length();
		
		for (int offset = 0; offset < length; ) {
		   int codePoint = text.codePointAt(offset);
		   sb.append(doDecode(codePoint));
		   
		   offset += Character.charCount(codePoint);
		}
		return sb.toString();
	}
	
	protected String doDecode(int codePoint) {
		return new String(Character.toChars(codePoint));
	}
	
	protected String doEncodePreprocess(String text) {
		return text;
	}
	
	protected String doEncodePostprocess(String text) {
		return text;
	}
	
	protected String doDecodePreprocess(String text) {
		return text;
	}
	
	protected String doDecodePostprocess(String text) {
		return text;
	}
}
