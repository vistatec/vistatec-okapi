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

package net.sf.okapi.lib.beans.sessions;

import net.sf.okapi.common.Event;
import net.sf.okapi.lib.beans.v1.OkapiBeans;
import net.sf.okapi.lib.persistence.VersionMapper;
import net.sf.okapi.persistence.xml.java.beans.JavaBeansPersistenceSession;

public class OkapiXmlSession extends JavaBeansPersistenceSession {

	@Override
	protected Class<?> getDefItemClass() {
		return Event.class;
	}

	@Override
	protected String getDefItemLabel() {
		return "event";
	}

	@Override
	protected String getDefVersionId() {
		return OkapiBeans.VERSION;
	}

	@Override
	public void registerVersions() {
		VersionMapper.registerVersion(OkapiBeans.class);		// v1
	}

}
