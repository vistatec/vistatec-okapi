/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.terminology;

import java.util.LinkedHashMap;

import net.sf.okapi.common.resource.Property;

public class BaseEntry {
	
	protected LinkedHashMap<String, Property> props;

	public void setProperty (Property property) {
		if ( props == null ) props = new LinkedHashMap<String, Property>();
		props.put(property.getName(), property);
	}
	
	public Property getProperty (String name) {
		if ( props == null ) return null;
		return props.get(name);
	}

	public void setProperties (LinkedHashMap<String, Property> props) {
		this.props = props;
	}

	public Iterable<String> propertiesKeys () {
		return props.keySet();
	}

}
