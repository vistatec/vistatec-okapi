/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.verification;

public class BlackTerm {
	public BlackTerm() {}

	public BlackTerm(String text, String suggestion) {
		this(text, suggestion, ""); // No comment
	}
	
	public BlackTerm(String text, String suggestion, String comment) {
		this();
		this.text = text;
		this.suggestion = suggestion;
		this.comment = comment;
	}

	public String text;
	public String searchTerm;
	public String suggestion;
	public String comment;
	public boolean doCaseSensitiveMatch = false;
}
