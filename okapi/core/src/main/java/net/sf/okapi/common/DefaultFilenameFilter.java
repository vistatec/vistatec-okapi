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
============================================================================*/

package net.sf.okapi.common;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * Implements a default filename filter that supports filtering by wild-cards like ("myFile*.*").
 */
public class DefaultFilenameFilter implements FilenameFilter {

	private Pattern pattern;
	
	/**
	 * Creates a new DefaultFilenameFilter object for a given pattern.
	 * @param pattern to filter on.
	 * @param caseSensitive true to use case-sensitive pattern, false otherwise.
	 * You can use ? to match any single character and * to match any multiple characters.
	 * The pattern is not case-sensitive ("test.*" and "TeSt.*" give the same results)
	 * except if specified otherwise.
	 */
	public DefaultFilenameFilter (String pattern,
		boolean caseSensitive)
	{
		if ( pattern == null ) throw new NullPointerException("Mask of the filename cannot be null.");
		pattern = pattern.replace('.', '\b');
		pattern = pattern.replace("*", ".*");
		pattern = pattern.replace('?', '.');
		pattern = pattern.replace("\b", "\\.");
		if ( caseSensitive ) this.pattern = Pattern.compile(pattern);
		else this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Creates a new DefaultFilenameFilter object with a given extension value.
	 * This constructor is for backward compatibility and is equivalent to calling
	 * <code>DefaultFilenameFilter("*"+extension, false)</code> 
	 * @param extension the extension to filter on.
	 */
	public DefaultFilenameFilter (String extension) {
		this("*"+extension, false);
	}
	
	/**
	 * Accept or reject a given filename.
	 * @return true if the filename is accepted. If the value is null, the method returns false. 
	 */
	public boolean accept (File directory,
		String fileName)
	{
		if ( fileName == null ) return false;
		return pattern.matcher(fileName).matches();
	}

}

