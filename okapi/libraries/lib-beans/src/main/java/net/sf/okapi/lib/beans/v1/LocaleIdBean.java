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

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class LocaleIdBean extends PersistenceBean<LocaleId> {

	private String language;
	private String region;
	private String userPart;
	
	@Override
	protected LocaleId createObject(IPersistenceSession session) {
		return Util.isEmpty(language) ?
				LocaleId.EMPTY :
				new LocaleId(language, region, userPart);
	}

	@Override
	protected void fromObject(LocaleId obj, IPersistenceSession session) {
		language = obj.getLanguage();
		region = obj.getRegion();
		userPart = obj.getUserPart();
	}

	@Override
	protected void setObject(LocaleId obj, IPersistenceSession session) {
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getUserPart() {
		return userPart;
	}

	public void setUserPart(String userPart) {
		this.userPart = userPart;
	}
	
}
