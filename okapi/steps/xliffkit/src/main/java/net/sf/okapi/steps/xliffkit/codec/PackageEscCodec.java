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

public class PackageEscCodec extends BasePackageCodec {

	private static final String[] LOOKUP = new String[] {
		"\\0", "^A", "^B", "^C", "^D", "^E", "^F", "\\a", "\\b", "\\t", "\\n", "\\v",
		"\\f", "\\r", "^N", "^O", "^P", "^Q", "^R", "^S", "^T", "^U", "^V", "^W",
		"^X", "^Y", "^Z", "\\e", "^\\", "^]", "^^", "^_", "^?"
	};

	private static final String PREFIX = new String(Character.toChars(0xFFF0)); // PUA
	
	private static Pattern[] patterns;
//	private static Pattern[] protectPatterns;
	
	public PackageEscCodec() {
		super();
		patterns = new Pattern[LOOKUP.length];
		for (int i = 0; i < LOOKUP.length; i++) {
			String st = PREFIX + LOOKUP[i];
			patterns[i] = Pattern.compile(RegexUtil.escape(st));
		}
		
//		protectPatterns = new Pattern[LOOKUP.length];
//		for (int i = 0; i < LOOKUP.length; i++) {
//			String st = LOOKUP[i];
//			String escaper = st.substring(0, 1);
//			st = String.format("(%s+)*", RegexUtil.escape(escaper)) + 
//					RegexUtil.escape(st);
//			protectPatterns[i] = Pattern.compile(st);
//		}
	}
	
	@Override
	protected String doEncode(int codePoint) {
		if (codePoint == 0x7F) codePoint = 0x20;		
		if (codePoint <= 0x20)
			return PREFIX + LOOKUP[codePoint]; 
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
//		for (int i = 0; i < protectPatterns.length; i++) {
//			Pattern pattern = protectPatterns[i];
//			Matcher matcher = pattern.matcher(text);
//			if (!matcher.find()) continue;
//			
//			String match = matcher.group(1); // leading escapes
//			// If we got a
//			String replacement = getReplacement(i);
//			text = pattern.matcher(text).replaceAll(replacement);
//		}
		return text;
	}

	private String getReplacement(int index) {
		if (index == 0x20) index = 0x7F;
		return new String(Character.toChars(index));
	}
	
//	private String getProtectReplacement(int index) {
//		String st = LOOKUP[index];
//		String escaper = st.substring(0, 1);
//		return RegexUtil.escape(new String(escaper + st));
//	}

//	@Override
//	protected String doEncodePreprocess(String text) {
////		// Duplicate \ and ^ before control representations found in original text		
////		// We go backwards because ^^ (for 0x1E) gets in the way
////		for (int i = patterns.length-1; i > 0; i--) {
////			Pattern pattern = patterns[i];					
////			Matcher matcher = pattern.matcher(text);
////			if (!matcher.find()) continue;
////			
////			String replacement = getProtectReplacement(i);
////			text = matcher.replaceAll(replacement);
////		}
//		
//		// Duplicate all \ and ^ found in original text
//		text = text.replaceAll("\\\\", "\\\\\\\\");
//		text = text.replaceAll("\\^", "\\^\\^");
//		
//		return text;
//	}
	
}
