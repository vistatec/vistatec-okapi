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

package net.sf.okapi.lib.extra;

import net.sf.okapi.common.Util;

/**
 * 
 * 
 * @version 0.1 08.07.2009
 */

public class Component implements IComponent, INotifiable {

	private String name;
	private String description;	
	
//	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public Component() {
		super();		
	}

	public Component(String name, String description) {
		super();
		
		this.name = name;
		this.description = description;
	}

	protected void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	protected void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		//return this.getClass().getName();
		return description;
	}

	@Override
	public String toString() {
		if (!Util.isEmpty(name) && !Util.isEmpty(description))
			return String.format("%s [%s]", name, description);
		else if (!Util.isEmpty(name))
			return name;
		else
			return super.toString();
	}

	public boolean exec(Object sender, String command, Object info) {
		return false;
	}

	
}
