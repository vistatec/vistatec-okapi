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

package net.sf.okapi.lib.beans.v1;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.filters.openxml.ConditionalParameters;
import net.sf.okapi.filters.openxml.ParseType;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class ConditionalParametersBean extends PersistenceBean<IParameters> {
	
	private String data;
	private ParseType fileType = ParseType.MSWORD;

	@Override
	protected ConditionalParameters createObject(IPersistenceSession session) {
		return new ConditionalParameters();
	}

	@Override
	protected void fromObject(IParameters obj, IPersistenceSession session) {
		if (obj instanceof ConditionalParameters) {
			ConditionalParameters params = (ConditionalParameters) obj;
			data = params.toString();
			fileType = params.nFileType;
		}
	}

	@Override
	protected void setObject(IParameters obj, IPersistenceSession session) {
		if (obj instanceof ConditionalParameters) {
			ConditionalParameters params = (ConditionalParameters) obj;
			params.fromString(data);
			params.nFileType = fileType;
		}
	}

	public ParseType getFileType() {
		return fileType;
	}

	public void setFileType(ParseType fileType) {
		this.fileType = fileType;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}

}
