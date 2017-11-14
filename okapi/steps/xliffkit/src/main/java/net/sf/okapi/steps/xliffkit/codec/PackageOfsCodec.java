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

public class PackageOfsCodec extends BasePackageCodec {

	private static final int PUA_START = 0xF000; // Not 0xE000 to not interfere with inline code markers
	private static final int PUA_END = 0xF8FF;
	
	@Override
	protected String doEncode(int codePoint) {
		return new String(Character.toChars(codePoint + PUA_START));
	}

	@Override
	protected String doDecode(int codePoint) {
	   if (codePoint >= PUA_START && codePoint <= PUA_END) 
		   codePoint -= PUA_START;
		return new String(Character.toChars(codePoint));
	}
}
