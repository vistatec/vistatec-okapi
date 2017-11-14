/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.steps.textmodification;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	
	public static final int TYPE_KEEPORIGINAL = 0;
	public static final int TYPE_XNREPLACE = 1;
	public static final int TYPE_KEEPINLINE = 2;
	public static final int TYPE_EXTREPLACE = 3;
	
	private static final String APPLYTOBLANKENTRIES = "applyToBlankEntries";
	private static final String EXPAND = "expand";
	private static final String SCRIPT = "script";
	private static final String TYPE = "type";
	private static final String ADDPREFIX = "addPrefix";
	private static final String PREFIX = "prefix";
	private static final String ADDSUFFIX = "addSuffix";
	private static final String SUFFIX = "suffix";
	private static final String APPLYTOEXISTINGTARGET = "applyToExistingTarget";
	private static final String ADDNAME = "addName";
	private static final String ADDID = "addID";
	private static final String MARKSEGMENTS = "markSegments";

	public Parameters () {
		super();
	}
	
	public int getType() {
		return getInteger(TYPE);
	}

	public void setType(int type) {
		setInteger(TYPE, type);
	}

	public boolean getAddPrefix() {
		return getBoolean(ADDPREFIX);
	}

	public void setAddPrefix(boolean addPrefix) {
		setBoolean(ADDPREFIX, addPrefix);
	}

	public String getPrefix() {
		return getString(PREFIX);
	}

	public void setPrefix(String prefix) {
		setString(PREFIX, prefix);
	}

	public boolean getAddSuffix() {
		return getBoolean(ADDSUFFIX);
	}

	public void setAddSuffix(boolean addSuffix) {
		setBoolean(ADDSUFFIX, addSuffix);
	}

	public String getSuffix() {
		return getString(SUFFIX);
	}

	public void setSuffix(String suffix) {
		setString(SUFFIX, suffix);
	}

	public boolean getApplyToExistingTarget() {
		return getBoolean(APPLYTOEXISTINGTARGET);
	}

	public void setApplyToExistingTarget(boolean applyToExistingTarget) {
		setBoolean(APPLYTOEXISTINGTARGET, applyToExistingTarget);
	}

	public boolean getAddName() {
		return getBoolean(ADDNAME);
	}

	public void setAddName(boolean addName) {
		setBoolean(ADDNAME, addName);
	}

	public boolean getAddID() {
		return getBoolean(ADDID);
	}

	public void setAddID(boolean addID) {
		setBoolean(ADDID, addID);
	}

	public boolean getMarkSegments() {
		return getBoolean(MARKSEGMENTS);
	}

	public void setMarkSegments(boolean markSegments) {
		setBoolean(MARKSEGMENTS, markSegments);
	}

	public boolean getApplyToBlankEntries() {
		return getBoolean(APPLYTOBLANKENTRIES);
	}

	public void setApplyToBlankEntries(boolean applyToBlankEntries) {
		setBoolean(APPLYTOBLANKENTRIES, applyToBlankEntries);
	}

	public boolean getExpand() {
		return getBoolean(EXPAND);
	}

	public void setExpand(boolean expand) {
		setBoolean(EXPAND, expand);
	}

	public int getScript() {
		return getInteger(SCRIPT);
	}

	public void setScript(int script) {
		setInteger(SCRIPT, script);
	}
	
	public void reset() {
		super.reset();
		setType(0);
		setAddPrefix(false);
		setPrefix("{START_");
		setAddSuffix(false);
		setSuffix("_END}");
		setApplyToExistingTarget(false);
		setAddName(false);
		setAddID(false);
		setMarkSegments(false);
		setApplyToBlankEntries(true); // For backward compatibility
		setExpand(false);
		setScript(0);
	}
}
