/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Simple class to store user-specific properties of an application in the user folder.
 */
public class UserConfiguration extends Properties {
	
	private static final long serialVersionUID = 1L;
	
	public UserConfiguration () {
		super();
		defaults = new Properties();
	}

	/**
	 * Load the user-specific application properties.
	 * @param appName the application name.
	 */
	public void load (String appName) {
		try {
			InputStream input = new FileInputStream(
				new File(System.getProperty("user.home") + File.separator + "."+appName));
			load(input);
		}
		catch ( IOException e ) {
			// Don't care about not reading this file
		}
	}
	
	/**
	 * Save the user-specific application properties.
	 * @param appName the application name.
	 * @param version the application version, will be used in a comment.
	 */
	public void save (String appName,
		String version)
	{
		try {
			OutputStream output = new FileOutputStream(
				new File(System.getProperty("user.home") + File.separator + "."+appName));
			store(output, appName + " - " + version);
		}
		catch ( IOException e ) {
			// Don't care about not writing this file
		}
	}
	
	@Override
	public Object setProperty (String key,
		String value)
	{
		if ( value == null ) return remove(key);
		else return super.setProperty(key, value);
	}
	
	/**
	 * Gets a boolean property value.
	 * @param key The name of the property.
	 * @return True if the property exists and is set to "true", false if it
	 * does not exists or if it is set to "false".
	 */
	public boolean getBoolean (String key) {
		return "true".equals(getProperty(key, "false"));
	}

	/**
	 * Gets a boolean property value possibly set to a default value.
	 * @param key the name of the property.
	 * @param defaultValue the default value.
	 * @return the value of the property if it exists, 
	 * or the default value if it does not exist.
	 */
	public boolean getBoolean (String key,
		boolean defaultValue)
	{
		return "true".equals(getProperty(key, (defaultValue ? "true" : "false")));
	}
	
	/**
	 * Sets a boolean property.
	 * @param key The name of the property.
	 * @param value The new value for the property.
	 * @return The previous value of the property, or null if the
	 * property did not exists yet.
	 */
	public Object setProperty (String key,
		boolean value)
	{
		return super.setProperty(key, (value ? "true" : "false"));
	}

	/**
	 * Gets an integer property.
	 * @param key The name of the property.
	 * @return The integer value of the property if it exists, 0 if it
	 * does not exists.
	 */
	public int getInteger (String key) {
		return Integer.valueOf(getProperty(key, "0"));
	}
	
	/**
	 * Sets an integer property.
	 * @param key The name of the property.
	 * @param value The new value for the property.
	 * @return The previous value of the property, or null if the
	 * property did not exist.
	 */
	public Object setProperty (String key,
		int value)
	{
		return super.setProperty(key, String.valueOf(value));
	}
	
}
