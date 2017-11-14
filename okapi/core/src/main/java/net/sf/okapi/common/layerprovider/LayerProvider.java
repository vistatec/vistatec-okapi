/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common.layerprovider;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderContext;

public class LayerProvider implements ILayerProvider {

	private CharsetEncoder outputEncoder;
	private String lineBreak;
	private String encoding;

	public LayerProvider() {
	}
	
	public LayerProvider(CharsetEncoder outputEncoder, String lineBreak) {
		super();
		this.outputEncoder = outputEncoder;
		this.lineBreak = lineBreak;
	}

	@Override
	public String endCode () {
		return "}";
	}

	@Override
	public String endInline () {
		return "}";
	}

	@Override
	public String startCode () {
		return "{\\cs5\\f1\\cf15\\lang1024 ";
	}

	@Override
	public String startInline () {
		return "{\\cs6\\f1\\cf6\\lang1024 ";
	}
	
	@Override
	public String startSegment () {
		return "{\\cs15\\v\\cf12\\sub\\f2 \\{0>}{\\v\\f1 ";
	}
	
	@Override
	public String endSegment () {
		return "{\\cs15\\v\\cf12\\sub\\f2 <0\\}}";
	}
	
	@Override
	public String midSegment (int leverage) {
		return String.format("%s%d%s", "}{\\cs15\\v\\cf12\\sub\\f2 <\\}", leverage, "\\{>}");
	}
	
	// Context: 0=in text, 1=in skeleton, 2=in inline
	@Override
	public String encode (String text,
			EncoderContext context)
	{
		// Context here can be used for lineBreak type
		return Util.escapeToRTF(text, true, context.ordinal(), outputEncoder);
	}

	@Override
	public String encode (char value,
			EncoderContext context)
	{
		// Context here can be used for lineBreak type
		return Util.escapeToRTF(String.valueOf(value), true, context.ordinal(), outputEncoder);
	}

	@Override
	public String encode (int value,
			EncoderContext context)
	{
		// Context here can be used for lineBreak type
		if ( Character.isSupplementaryCodePoint(value) ) {
			return Util.escapeToRTF(new String(Character.toChars(value)),
				true, context.ordinal(), outputEncoder);
		}
		return Util.escapeToRTF(String.valueOf((char)value),
			true, context.ordinal(), outputEncoder);
	}

	@Override
	public void setOptions (IParameters params,
		String encoding,
		String lineBreak)
	{
		outputEncoder = Charset.forName(encoding).newEncoder();
		this.lineBreak = lineBreak;
		this.encoding = encoding;
	}

	@Override
	public String toNative(String propertyName,
		String value)
	{
		// No modification: The layer provider does not change the value
		return value;
	}

	@Override
	public String getLineBreak () {
		return lineBreak;
	}

	@Override
	public CharsetEncoder getCharsetEncoder () {
		return outputEncoder;
	}

	@Override
	public IParameters getParameters() {
		return null;
	}

	@Override
	public String getEncoding() {
		// TODO Auto-generated method stub
		return encoding;
	}

}
