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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Provides the different information common to all types of parameters used
 * to configure steps, filters, and other okapi components.
 */
public interface IParameterDescriptor {

	/**
	 * Gets the programming name of this parameter. the name must follow the 
	 * JavaBean naming conventions. For example, a parameter accessible by
	 * <code>getMyText</code> and <code>setMyText</code> must be named <code>myText</code>
	 * @return the programming name of this parameter.
	 */
	public String getName ();

	/**
	 * Gets the localizable name of this parameter.
	 * @return the localizable name of this parameter.
	 */
	public String getDisplayName ();

	/**
	 * Sets the localizable name of this parameter.
	 * @param displayName the new localizable name of this parameter.
	 */
	public void setDisplayName (String displayName);

	/**
	 * Gets the short localizable description of this parameter.
	 * @return the short localizable description of this parameter.
	 */
	public String getShortDescription ();
	
	/**
	 * Gets the short localizable description of this parameter.
	 * @param shortDescription the new short localizable description of this parameter.
	 */
	public void setShortDescription (String shortDescription);

	/**
	 * Gets the type of this parameter.
	 * @return the type of this parameter.
	 */
	public Type getType ();
	
	/**
	 * Gets the method to obtain the current value of this parameter.
	 * @return the method to get the current value of this parameter.
	 */
	public Method getReadMethod ();
	
	/**
	 * Gets the method to set a new value for this this parameter.
	 * @return the method to set a new value of this parameter.
	 */
	public Method getWriteMethod ();
	
	/**
	 * Gets the object where this parameter is instantiated.
	 * @return the object where this parameter is instantiated.
	 */
	public Object getParent ();

}
