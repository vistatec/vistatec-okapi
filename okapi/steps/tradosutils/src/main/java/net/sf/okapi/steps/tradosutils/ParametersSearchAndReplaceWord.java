/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tradosutils;

import java.util.ArrayList;

import net.sf.okapi.common.StringParameters;

public class ParametersSearchAndReplaceWord extends StringParameters {
	private static final String REGEX = "regEx";
	private static final String WHOLEWORD = "wholeWord";
	private static final String MATCHCASE = "matchCase";
	private static final String REPLACEALL = "replaceAll";
	
	public ArrayList<String[]> rules;
	
	public ParametersSearchAndReplaceWord () {
		super();
	}

	public boolean getRegEx() {
		return getBoolean(REGEX);
	}

	public void setRegEx(boolean regEx) {
		setBoolean(REGEX, regEx);
	}

	public boolean getWholeWord() {
		return getBoolean(WHOLEWORD);
	}

	public void setWholeWord(boolean wholeWord) {
		setBoolean(WHOLEWORD, wholeWord);
	}

	public boolean getMatchCase() {
		return getBoolean(MATCHCASE);
	}

	public void setMatchCase(boolean matchCase) {
		setBoolean(MATCHCASE, matchCase);
	}

	public boolean getReplaceAll() {
		return getBoolean(REPLACEALL);
	}

	public void setReplaceAll(boolean replaceALL) {
		setBoolean(REPLACEALL, replaceALL);
	}

	public void reset () {
		super.reset();
		setRegEx(false);
		setWholeWord(false);
		setMatchCase(false);
		setReplaceAll(true);
		rules = new ArrayList<String[]>();
	}

	public void addRule (String pattern[]) {
		rules.add(pattern);
	}	
	
	public ArrayList<String[]> getRules () {
		return rules;
	}	

	public void fromString (String data) {
		super.fromString(data);

		int count = buffer.getInteger("count", 0);
		for ( int i=0; i<count; i++ ) {
			String []s = new String[5];
			s[0] = buffer.getString(String.format("use%d", i), "").replace("\r", "");
			s[1] = buffer.getString(String.format("search%d", i), "").replace("\r", "");
			s[2] = buffer.getString(String.format("replace%d", i), "").replace("\r", "");
			s[3] = buffer.getString(String.format("searchFormat%d", i), "").replace("\r", "");			
			s[4] = buffer.getString(String.format("replaceFormat%d", i), "").replace("\r", "");
			rules.add(s);
		}
	}

	public String toString() {
		buffer.setInteger("count", rules.size());
		int i = 0;
		for ( String[] temp : rules ) {
			buffer.setString(String.format("use%d", i), temp[0]);
			buffer.setString(String.format("search%d", i), temp[1]);
			buffer.setString(String.format("replace%d", i), temp[2]);
			buffer.setString(String.format("searchFormat%d", i), temp[3]);
			buffer.setString(String.format("replaceFormat%d", i), temp[4]);
			i++;
		}
		return super.toString();
	}

/* SAMPLE CONFIG
#v1
regEx.b=false
wholeWord.b=false
matchCase.b=false
count.i=0
replaceALL.b=true
use0=true
search0=Hello
replace0=Bonjour
searchFormat0=Normal
replaceFormat0=Heading 1
use1=true
search1=world
replace1=cosmos
searchFormat1=Heading 1
replaceFormat1=Heading 2
*/
}
