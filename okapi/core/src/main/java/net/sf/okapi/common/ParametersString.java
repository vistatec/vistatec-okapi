/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

import java.util.Iterator;
import java.util.LinkedHashMap;

import net.sf.okapi.common.exceptions.OkapiException;

/**
 * String-based representation of a set of parameters.
 * <ul>
 * <li>The string or file should start with "#v1\n"
 * <li>Each parameter is saved in a format: key=value
 * <li>Keys and values are case-sensitive.
 * <li>Keys should not contain periods (.) as this character is reserved for group handling.
 * <li>The character \r should be escaped as $0d$
 * <li>The character \n should be escaped as $0a$
 * <li>The suffix .i and .b should be used for integer and boolean entries.
 * <li>Commented lines are denoted by a character '#' as the first character of the line.
 * <li>White-spaces are significant after '=' for string entries.
 * </ul>
 * Example:
 * <pre>#v1
 *paramKey1.b=true
 *paramKey2.i = 123
 *paramStr  =value for paramStr
 * </pre>
 */
public class ParametersString {

	private LinkedHashMap<String, Object> list;
	
	public ParametersString () {
		list = new LinkedHashMap<String, Object>();
	}

	public ParametersString (String data) {
		list = new LinkedHashMap<String, Object>();
		buildList(null, data);
	}

	@Override
	public String toString () {
		return buildString(null);
	}
	
	public void fromString (String data) {
		fromString(data, true);
	}

	/**
	 * Deserialize the specified data into this object. Optionally
	 * reset the internal parameter set before doing so.
	 * @param data data to deserialize
	 * @param clearBuffer if true, the internal parameters list will be 
	 *        cleared before loading the data.  if false, any existing
	 *        parameters will be left intact unless they are overridden
	 *        by the data.
	 */
	public void fromString (String data, boolean clearBuffer) {
		if (clearBuffer) {
			list.clear();
		}
		buildList(null, data);
	}
	
	public void reset () {
		list.clear();
	}
	
	public void remove (String name) {
		if ( list.containsKey(name) ) {
			list.remove(name);
		}
	}
	
	public void removeGroup (String groupName) {
		groupName += ".";
		Iterator<String> iter = list.keySet().iterator();
		String key;
		while ( iter.hasNext() ) {
			key = iter.next();
			if ( key.startsWith(groupName) ) {
				list.remove(key);
			}
		}
	}
	
	private String escape (String value) {
		if ( value == null ) return value;
		value = value.replace("\r", "$0d$");
		return value.replace("\n", "$0a$");
	}
	
	private String unescape (String value) {
		if ( value == null ) return value;
		value = value.replace("$0d$", "\r");
		return value.replace("$0a$", "\n");
	}
	
	private String buildString (String prefix) {
		StringBuilder tmp = new StringBuilder("#v1");
		Object value;
		if ( prefix != null ) prefix += ".";
		for ( String key : list.keySet() ) {
			// Check the group prefix if required
			if ( prefix != null ) {
				if ( !key.startsWith(prefix) ) continue;
				tmp.append("\n"+key.substring(prefix.length()));
			}
			else tmp.append("\n"+key);
			// Add to the string
			value = list.get(key);
			if ( value instanceof String ) {
				tmp.append("="+escape((String)value));
			}
			else if ( value instanceof Integer ) {
				tmp.append(".i="+String.valueOf(value));
			}
			else if ( value instanceof Boolean ) {
				tmp.append(".b="+((Boolean)value ? "true" : "false"));
			}
			else {
				throw new OkapiException("Invalide type: "+key);
			}
		}
		// If we have only the start marker, it's really empty
		if ( tmp.length() == 3 ) tmp.setLength(0);
		return tmp.toString();
	}
	
	private void buildList (String prefix,
		String data)
	{
		if ( prefix == null ) prefix = "";
		else prefix += ".";
		if ( data == null ) data = "";
		
		String[] lines = data.split("\n", 0);
		int n;
		String qualifiedName;
		String key;
		String trimmedValue;
		
		for ( String line : lines ) {
			if ( line.trim().length() == 0 ) continue;
			if ( line.charAt(0) == '#' ) continue;
			if (( n = line.indexOf('=')) == -1 ) continue;
			
			qualifiedName = line.substring(0, n).trim();
			trimmedValue = line.substring(n+1).trim();
			
			if ( qualifiedName.endsWith(".b") ) {
				key = prefix + qualifiedName.substring(0, qualifiedName.lastIndexOf("."));				
				list.put(key, "true".equals(trimmedValue));
			}
			else if ( qualifiedName.endsWith(".i") ) {
				key = prefix + qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
				list.put(key, (int)Integer.valueOf(trimmedValue));
			}
			else {
				key = prefix + qualifiedName;
				// Does not use trimmed value because white-spaces are significant in string entries 
				list.put(key, unescape(line.substring(n+1)));
			}
		}
	}
	
	public String getGroup (String name,
		String defaultValue)
	{
		String tmp = buildString(name);
		if ( tmp.length() > 0 ) return tmp;
		// Else: return default value
		return defaultValue;
	}
	
	public String getGroup (String name) {
		return buildString(name);
	}
	
	public void setGroup (String name,
		String data)
	{
		buildList(name, data);
	}
	
	public void setGroup (String name,
		ParametersString params)
	{
		if ( name == null ) name = "";
		else name += ".";
		for ( String key : params.list.keySet() ) {
			list.put(name+key, params.list.get(key));
		}
	}
	
	public String getString (String name,
		String defaultValue)
	{
		if ( list.containsKey(name) )
			return (String)list.get(name);
		// Else: return default value.
		return defaultValue;
	}

	public String getString (String name) {
		return getString(name, "");
	}
	
	public void setString (String name,
		String value)
	{
		if ( value == null ) list.remove(name);
		else list.put(name, value);
	}
	
	public boolean getBoolean (String name,
		boolean defaultValue)
	{
		if ( list.containsKey(name) )
			return (Boolean)list.get(name);
		// Else: return false by default.
		return defaultValue;
	}
	
	public boolean getBoolean (String name) {
		return getBoolean(name, false);
	}
	
	public void setBoolean (String name,
		boolean value)
	{
		list.put(name, value);
	}
	
	public int getInteger (String name,
		int defaultValue)
	{
		if ( list.containsKey(name) )
			return (Integer)list.get(name);
		// Else: return zero by default.
		return defaultValue;
	}
	
	public int getInteger (String name) {
		return getInteger(name, 0);
	}
	
	public void setInteger (String name,
		int value)
	{
		list.put(name, value);
	}

	public void setParameter (String name,
		String value)
	{
		setString(name, value);
	}

	public void setParameter (String name,
		boolean value)
	{
		setBoolean(name, value);
	}

	public void setParameter (String name,
		int value)
	{
		setInteger(name, value);
	}

	/**
	 * Gets the string value for a given parameter after decoding it if necessary. 
	 * @param name the name of the parameter.
	 * @param defaultValue the default (not encoded) value to use.
	 * @return the string value of the given parameter, decoded if necessary.
	 */
	public String getEncodedString (String name,
		String defaultValue)
	{
		return Base64.decodePassword(getString(name, defaultValue));
	}
	
	/**
	 * Sets the string value of a given parameter and encode it.
	 * The encoding applied is only a basic first level of security protecting
	 * the value from being casually read by a human, it is not an encryption.
	 * @param name the name of the parameter.
	 * @param value the value to set and encode.
	 */
	public void setEncodedString (String name,
		String value)
	{
		if ( value == null ) {
			list.remove(name);
		}
		else {
			list.put(name, Base64.encodePassword(value));
		}
	}
	
}
