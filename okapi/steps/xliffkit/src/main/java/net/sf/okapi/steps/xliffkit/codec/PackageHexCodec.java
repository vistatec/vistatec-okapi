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

public class PackageHexCodec extends BasePackageCodec {

	private static final String MASK = "_#x%04X;";
	private static final Pattern PATTERN = Pattern.compile("_#x(\\p{XDigit}+?);");
	private static final int GROUP = 1;
	
	@Override
	protected String doEncode(int codePoint) {		
		return String.format(MASK, codePoint);
	}

	@Override
	protected String doDecode(String text) {
		Matcher matcher = PATTERN.matcher(text);
	    StringBuilder buf = new StringBuilder();
	    
	    int start = 0;
	    int end = 0;
	    
	    while (matcher.find()) {
	        start = matcher.start();
	        if (start != -1) {
	        	buf.append(text.substring(end, start));
	        	int codePoint = Integer.parseInt(matcher.group(GROUP), 16);
	        	char[] replacement = Character.toChars(codePoint);
	        	buf.append(replacement);
		        end = matcher.end();
	        }
	    }
	    
	    buf.append(text.substring(end));
	    return buf.toString();
	}
}
