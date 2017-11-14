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

package net.sf.okapi.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * String-based representation of a configuration file.
 */
public class ConfigurationString {

	private static final String STARTGROUP  = "beginGroup";
	private static final String ENDGROUP    = "endGroup";
	
	private StringBuilder  data;


	public ConfigurationString () {
		reset();
	}
	
	public ConfigurationString (String data) {
		fromString(data);
	}
	
	public void fromString (String data) {
		if ( data != null ) {
			// Make sure all types of line-breaks are reduced to one or more \n
			this.data = new StringBuilder(prependMarker(data.replace('\r', '\n')));
		}
		else reset();
	}

	public void reset () {
		data = new StringBuilder();
		data.append("\n");
	}

	public String toString () {
		String tmp = data.toString().replaceAll("\n\n", "\n");
		return tmp.trim();
	}

	public void set (String name,
		String value)
	{
		// Create the full name+value string
		if ( value != null ) {
			value = String.format("%s=%s\n", name, escape(value));
		}

		// Search if the the field exists
		String tmpName = "\n" + name + "=";
		int pos1 = data.toString().indexOf(tmpName);
		if ( pos1 < 0 ) { // Not found: add it
			if ( value != null ) {
				data.append(value);
			}
			return;
		}

		// Else: It exists, replace it
		// Search for the value
		int pos2 = data.toString().indexOf("\n", pos1+1);
		if ( pos2 < 0 ) pos2 = pos1; // No end marker, no value;

		// Replace the value
		data.delete(pos1+1, pos2);
		if ( value != null )
			data.insert(pos1+1, value);
	}

	public void add (String name,
		String value)
	{
		if ( value == null ) {
			data.append(String.format("%s=%s\n", name, ""));
		}
		else {
			data.append(String.format("%s=%s\n", name, escape(value)));
		}
	}

	public void add (String name,
		boolean value)
	{
		data.append(String.format("%s=%d\n", name, (value ? 1 : 0)));
	}

	public void add (String name,
		int value)
	{
		data.append(String.format("%s=%d\n", name, value));
	}

	public void add (String name,
		char value)
	{
		data.append(String.format("%s=%c\n", name, value));
	}
	
	public void addGroup (String name,
		String value)
	{
		// Make sure all types of line-breaks are reduced to one or more \n
		data.append(String.format("%s=%s\n%s\n%s\n", STARTGROUP, 
			name, prependMarker(value.replace('\r', '\n')), ENDGROUP));
	}

	public String get (String name,
		String defaultValue)
	{
		// Search for the field name
		String tmpName = "\n" + name + "=";
		int pos1 = data.toString().indexOf(tmpName);
		if ( pos1 < 0 ) return defaultValue; // Field name not found

		// Search for the value
		pos1 += tmpName.length();
		int pos2 = data.toString().indexOf("\n", pos1);
		if ( pos2 < 0 ) return defaultValue; // No value found

		// Get the value
		return unescape(data.toString().substring(pos1, pos2));
	}

	public boolean get (String name,
		boolean defaultValue)
	{
		String sTmp = get(name, null);
		if ( sTmp == null ) return defaultValue;
		return sTmp.equals("1");
	}

	public int get (String name,
		int defaultValue)
	{
		String tmp = get(name, null);
		if (( tmp == null ) || ( tmp.length() == 0 )) return defaultValue;
		return Integer.parseInt(tmp);
	}

	public char get (String name,
		char defaultValue)
	{
		String tmp = get(name, null);
		if (( tmp == null ) || ( tmp.length() == 0 )) return defaultValue;
		return tmp.charAt(0);
	}

	public String getGroup (String name,
		String defaultValue)
	{
		// Search for the field name
		String tmpName = "\n" + STARTGROUP + name + "=";
		int pos1 = data.toString().indexOf(tmpName);
		if ( pos1 < 0 ) return defaultValue; // Field name not found

		// Search for the value
		pos1 += tmpName.length();
		int pos2 = data.toString().indexOf("\n", pos1);
		if ( pos2 < 0 ) return defaultValue; // No value found

		// Get the value
		return data.toString().substring(pos1, pos2);
	}

	public Map<String, String> toMap () {
		LinkedHashMap<String, String> table = new LinkedHashMap<String, String>();
		String tmp = toString();
		if ( Util.isEmpty(tmp) ) return table;

		String[] pairs = tmp.split("\n", 0);
		for ( String pair : pairs ) {
			String[] keyvalue = pair.split("=");
			table.put(keyvalue[0], // Handle empty parameters
				(( keyvalue.length > 1 ) ? keyvalue[1] : ""));
		}
		return table;
	}

	private String prependMarker (String data) {
		if ( data == null ) return data;
		if ( data.length() == 0 ) return "\n";
		if ( data.charAt(0) != '\n' ) return "\n"+data;
		else return data;
	}
	
	private String escape (String text) {
		String escapedText = text.replace("\r", "{0xD}");
		return escapedText.replace("\n", "{0xA}");
	}
	
	private String unescape (String text) {
		String escapedText = text.replace("{0xD}", "\r");
		return escapedText.replace("{0xA}", "\n");
	}
	
}
