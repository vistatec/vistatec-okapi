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

package net.sf.okapi.common;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;

/**
 * Basic implementation of the {@link IContext} interface.
 */
public class BaseContext implements IContext {

	private Map<String, Object> properties;
	private Annotations annotations;

	/**
	 * Creates an empty context.
	 */
	public BaseContext () {
	}

	/**
	 * Creates a BaseContext object and copy a map of properties.
	 * @param properties the map of properties to copy.
	 */
	public BaseContext (Map<String, Object> properties) {
		this.properties = new LinkedHashMap<String, Object>(properties);
	}
	
	public String getString (String name) {
		if ( properties == null ) return null;
		return (String)properties.get(name);
	}
	
	public void setString (String name,
		String value)
	{
		if ( properties == null ) {
			properties = new LinkedHashMap<String, Object>();
		}
		properties.put(name, value);
	}

	public boolean getBoolean (String name) {
		if ( properties == null ) return false;
		Object result = properties.get(name);
		return result == null ? false : (Boolean) result;
	}
	
	public void setBoolean (String name,
		boolean value)
	{
		if ( properties == null ) {
			properties = new LinkedHashMap<String, Object>();
		}
		properties.put(name, value);
	}
	
	public int getInteger (String name) {
		if ( properties == null ) return 0;
		return (Integer)properties.get(name);
	}
	
	public void setInteger (String name,
		int value)
	{
		if ( properties == null ) {
			properties = new LinkedHashMap<String, Object>();
		}
		properties.put(name, value);
	}
	
	public Object getObject (String name) {
		if ( properties == null ) return null;
		return properties.get(name);
	}
	
	public void setObject (String name,
		Object value)
	{
		if ( properties == null ) {
			properties = new LinkedHashMap<String, Object>();
		}
		properties.put(name, value);
	}
	
	public void removeProperty (String name) {
		if ( properties != null ) {
			properties.remove(name);
		}
	}
	
	public Map<String, Object> getProperties () {
		if ( properties == null ) {
			properties = new LinkedHashMap<String, Object>();
		}
		return properties;
	}
	public void clearProperties () {
		if ( properties != null ) {
			properties.clear();
		}
	}
		
	public <A extends IAnnotation> A getAnnotation (Class<A> type) {
		if ( annotations == null ) return null;
		return annotations.get(type);
	}

	public void setAnnotation (IAnnotation annotation) {
		if ( annotations == null ) {
			annotations = new Annotations();
		}
		annotations.set(annotation);
	}

	public void clearAnnotations () {
		if ( annotations != null ) {
			annotations.clear();
		}
	}

}
