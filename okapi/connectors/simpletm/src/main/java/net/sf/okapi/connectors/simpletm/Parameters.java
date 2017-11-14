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

package net.sf.okapi.connectors.simpletm;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {

	static final String DB_EXTENSION = ".h2.db";
	 /** 
	  * The full path of the database name to open.
	  * The path can have the extension {@link #DB_EXTENSION} or not extension.
	  */
	static final String DBPATH = "dbPath";
	static final String PENALIZETARGETWITHDIFFERENTCODES = "penalizeTargetWithDifferentCodes";
	static final String PENALIZESOURCEWITHDIFFERENTCODES = "penalizeSourceWithDifferentCodes";
	
	public Parameters () {
		super();
	}
	
	public Parameters (String initialData) {
		super(initialData);
	}
	
	public String getDbPath () {
		return getString(DBPATH);
	}

	public void setDbPath (String dbPath) {
		setString(DBPATH, dbPath);
	}
	
	public boolean getPenalizeTargetWithDifferentCodes () {
		return getBoolean(PENALIZETARGETWITHDIFFERENTCODES);
	}
	
	public void setPenalizeTargetWithDifferentCodes (boolean penalizeTargetWithDifferentCodes) {
		setBoolean(PENALIZETARGETWITHDIFFERENTCODES, penalizeTargetWithDifferentCodes);
	}
	
	public boolean getPenalizeSourceWithDifferentCodes () {
		return getBoolean(PENALIZESOURCEWITHDIFFERENTCODES);
	}
	
	public void setPenalizeSourceWithDifferentCodes (boolean penalizeSourceWithDifferentCodes) {
		setBoolean(PENALIZESOURCEWITHDIFFERENTCODES, penalizeSourceWithDifferentCodes);
	}
	
	@Override
	public void reset () {
		super.reset();
		setDbPath("");
		setPenalizeSourceWithDifferentCodes(true);
		setPenalizeTargetWithDifferentCodes(true);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(DBPATH,
			"Path of the Database file",
			String.format("Full path of the database file (%s)", DB_EXTENSION));
		desc.add(PENALIZESOURCEWITHDIFFERENTCODES,
			"Penalize exact matches when the source has different codes than the query", null);
		desc.add(PENALIZETARGETWITHDIFFERENTCODES,
			"Penalize exact matches when the target has different codes than the query", null);
		return desc;
	}

}
