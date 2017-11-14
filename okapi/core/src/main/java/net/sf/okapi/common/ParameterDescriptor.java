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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import net.sf.okapi.common.exceptions.OkapiException;

/**
 * Default implementation of the {@link IParameterDescriptor} interface.
 */
public class ParameterDescriptor implements IParameterDescriptor {

	private String name;
	private String shortDescription;
	private String displayName;
	private Type type;
	private Object parent;
	private Method readMethod;
	private Method writeMethod;

	/**
	 * Creates a new ParameterDescriptor object with a given name and type,
	 * associated with a given parent object, and with a given display name
	 * and short description.
	 * @param name the name of this parameter. The name must follow the JavaBean naming
	 * conventions.
	 * @param parent the object where this parameter is instantiated (or null for container-only).
	 * @param displayName the localizable name of this parameter.
	 * @param shortDescription a short localizable description of this parameter.
	 */
	public ParameterDescriptor (String name,
		Object parent,
		String displayName,
		String shortDescription)
	{
		this.name = name;
		this.parent = parent;
		this.displayName = displayName;
		this.shortDescription = shortDescription;
		
		// Case of UI-only parts: no parent to update
		if ( parent == null ) return;
			
		try {
			BeanInfo info;
			info = Introspector.getBeanInfo(parent.getClass());
			for ( PropertyDescriptor pd : info.getPropertyDescriptors() ) {
				if ( pd.getName().equals(name) ) {
					readMethod = pd.getReadMethod();
					writeMethod = pd.getWriteMethod();
				}
			}
		}
		catch ( IntrospectionException e ) {
			throw new OkapiException(String.format(
				"Introspection error when creating descriptor for '%s'", name), e);
		}
		
		if ( readMethod == null ) {
			throw new NullPointerException(String.format(
				"The readMethod for '%s' is null.\n(Getter maybe not declared or not following Java Bean naming convention?)", name));
		}
		// Get the type of the parameter from the return type
		this.type = readMethod.getGenericReturnType();
	}
	
	public String getName () {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getShortDescription() {
		return shortDescription;
	}
	
	public void setShortDescription (String shortDescription) {
		this.shortDescription = shortDescription;
	}
	
	public Type getType () {
		return type;
	}
	
	public Method getReadMethod () {
		return readMethod;
	}
	
	public Method getWriteMethod () {
		return writeMethod;
	}
	
	public Object getParent () {
		return parent;
	}

}
