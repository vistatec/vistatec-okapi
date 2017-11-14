/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.translation;

import net.sf.okapi.common.query.IQuery;

/**
 * Set of fields common to all translation resources.
 */
public class ResourceItem {
	
	/**
	 * The query engine for this resource.
	 */
	public IQuery query;
	
	/**
	 * A flags indicating if this resource is to be used or not.
	 */
	public boolean enabled;
	
	/**
	 * the name of this resource.
	 */
	public String name;

}
