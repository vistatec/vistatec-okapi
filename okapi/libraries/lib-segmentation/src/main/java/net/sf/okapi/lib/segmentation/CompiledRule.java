/*===========================================================================
  Copyright (C) 2008 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.lib.segmentation;

import java.util.regex.Pattern;

class CompiledRule {
	protected Pattern pattern;
	protected boolean isBreak;

	CompiledRule(String pattern, boolean isBreak) {
		this.pattern = Pattern.compile(pattern, Pattern.UNICODE_CHARACTER_CLASS);
		this.isBreak = isBreak;
	}
	

	@Override
	public String toString() {
		return "CompiledRule [pattern=" + pattern + ", isBreak=" + isBreak + "]";
	}
}
