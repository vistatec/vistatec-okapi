/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.searchandreplace;

import java.util.ArrayList;

import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.Util;

public class Parameters extends StringParameters {

	private static final String REGEX = "regEx";
	private static final String DOTALL = "dotAll";
	private static final String IGNORECASE = "ignoreCase";
	private static final String MULTILINE = "multiLine";
	private static final String TARGET = "target";
	private static final String SOURCE = "source";
	private static final String REPLACEALL = "replaceALL";
	private static final String COUNT = "count";
	private static final String REPLACEMENTSPATH = "replacementsPath";
	private static final String LOGPATH = "logPath";
	private static final String SAVELOG = "saveLog";
	
	public boolean getRegEx() {
		return getBoolean(REGEX);
	}

	public void setRegEx(boolean regEx) {
		setBoolean(REGEX, regEx);
	}

	public boolean getDotAll() {
		return getBoolean(DOTALL);
	}

	public void setDotAll(boolean dotAll) {
		setBoolean(DOTALL, dotAll);
	}

	public boolean getIgnoreCase() {
		return getBoolean(IGNORECASE);
	}

	public void setIgnoreCase(boolean ignoreCase) {
		setBoolean(IGNORECASE, ignoreCase);
	}

	public boolean getMultiLine() {
		return getBoolean(MULTILINE);
	}

	public void setMultiLine(boolean multiLine) {
		setBoolean(MULTILINE, multiLine);
	}

	public boolean getTarget() {
		return getBoolean(TARGET);
	}

	public void setTarget(boolean target) {
		setBoolean(TARGET, target);
	}

	public boolean getSource() {
		return getBoolean(SOURCE);
	}

	public void setSource(boolean source) {
		setBoolean(SOURCE, source);
	}

	public boolean getReplaceAll() {
		return getBoolean(REPLACEALL);
	}

	public void setReplaceAll(boolean replaceAll) {
		setBoolean(REPLACEALL, replaceAll);
	}

	public String getReplacementsPath() {
		return getString(REPLACEMENTSPATH);
	}

	public void setReplacementsPath(String replacementsPath) {
		setString(REPLACEMENTSPATH, replacementsPath);
	}

	public String getLogPath() {
		return getString(LOGPATH);
	}

	public void setLogPath(String logPath) {
		setString(LOGPATH, logPath);
	}

	public boolean getSaveLog() {
		return getBoolean(SAVELOG);
	}

	public void setSaveLog(boolean saveLog) {
		setBoolean(SAVELOG, saveLog);
	}

	public void setRules(ArrayList<String[]> rules) {
		this.rules = rules;
	}

	public ArrayList<String[]> rules;

	public Parameters () {
		super();
	}

	public void reset () {
		super.reset();
		setRegEx(false);
		setDotAll(false);
		setIgnoreCase(false);
		setMultiLine(false);
		setTarget(true);
		setSource(false);
		setReplaceAll(true);
		setReplacementsPath("");
		setLogPath(Util.ROOT_DIRECTORY_VAR+"/replacementsLog.txt");
		setSaveLog(false);
		
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
		
		int count = buffer.getInteger(COUNT, 0);
		for ( int i=0; i<count; i++ ) {
			String []s = new String[3];
			s[0] = buffer.getString(String.format("use%d", i), "");
			s[1] = buffer.getString(String.format("search%d", i), "");
			s[2] = buffer.getString(String.format("replace%d", i), "");
			rules.add(s);
		}
	}

	public String toString() {
		buffer.setInteger(COUNT, rules.size());
		int i = 0;

		for ( String[] temp : rules ) {
			buffer.setString(String.format("use%d", i), temp[0]);
			buffer.setString(String.format("search%d", i), temp[1]);
			buffer.setString(String.format("replace%d", i), temp[2]);
			i++;
		}		
		return buffer.toString();
	}
}
