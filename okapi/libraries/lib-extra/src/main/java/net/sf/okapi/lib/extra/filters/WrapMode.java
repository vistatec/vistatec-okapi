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

package net.sf.okapi.lib.extra.filters;

/**
 * 
 * 
 * @version 0.1, 09.06.2009
 */

public enum WrapMode {
	/**
	 * Lines of a multi-line text fragment (filter-specific) will be separated with the LF character (line feed, \n).
	 * @see wrapMode
	 */
	NONE,
	
	/**
	 * Replace line terminators in multi-line text fragments (filter-specific) with spaces,
	 * thus merging lines together (or unwrapping the lines).
	 * @see wrapMode 
	 */
	SPACES,
	
	/**
	 * Replace line terminators in multi-line text fragments (filter-specific) with in-line codes.
	 * @see wrapMode
	 */
	PLACEHOLDERS
}
