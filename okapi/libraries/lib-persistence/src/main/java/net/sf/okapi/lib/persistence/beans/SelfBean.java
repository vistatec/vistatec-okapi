/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.persistence.beans;

import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

/**
 * Used as the base class for the objects that can be serialized as are (all getters/setters access only simple types and structures of simple types and no full-blown bean is needed)
 * Extend this class for the classes that contain only serializable fields.
 * !!! Important: in the subclasses provide getters/setters for the internal fields to be serialized. 
 */
public class SelfBean extends PersistenceBean<Object> {

	@Override
	protected Object createObject(IPersistenceSession session) {
		return this;
	}
	
	@Override
	protected void setObject(Object obj,
			IPersistenceSession session) {		
	}

	@Override
	protected void fromObject(Object obj,
			IPersistenceSession session) {
	}
}
