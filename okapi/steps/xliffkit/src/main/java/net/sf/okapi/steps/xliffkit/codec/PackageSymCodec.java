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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.RegexUtil;

public class PackageSymCodec extends BasePackageCodec {

	private static final String[] LOOKUP = new String[] {
		"NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL", "BS", "HT", "LF", "VT",
		"FF", "CR", "SO", "SI", "DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
		"CAN", "EM", "SUB", "ESC", "FS", "GS", "RS", "US", "DEL"
	};

	private static final String MASK = "[%s]";
	
	private static Pattern[] patterns;
	private String mask;

	public PackageSymCodec() {
		this(MASK);		
	}
	
	public PackageSymCodec(String mask) {
		super();
		this.mask = mask;
		patterns = new Pattern[LOOKUP.length];
		for (int i = 0; i < LOOKUP.length; i++) {
			String st = String.format(mask, LOOKUP[i]);
			patterns[i] = Pattern.compile(RegexUtil.escape(st));
		}		
	}
	
	@Override
	protected String doEncode(int codePoint) {
		if (codePoint == 0x7F) codePoint = 0x20;		
		if (codePoint <= 0x20) {
			String st = String.format(mask, LOOKUP[codePoint]);
			return st;
		}
		else
			return new String(Character.toChars(codePoint));
	}

	@Override
	protected String doDecode(String text) {
		for (int i = 0; i < patterns.length; i++) {
			Pattern pattern = patterns[i];
			Matcher matcher = pattern.matcher(text);
			if (!matcher.find()) continue;
			
			String replacement = getReplacement(i);
			text = pattern.matcher(text).replaceAll(replacement);
		}
		return text;
	}

	private String getReplacement(int index) {
		if (index == 0x20) index = 0x7F;
		return new String(Character.toChars(index));
	}
}
