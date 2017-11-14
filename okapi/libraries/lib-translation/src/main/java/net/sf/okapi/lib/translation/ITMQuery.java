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

package net.sf.okapi.lib.translation;

import net.sf.okapi.common.query.IQuery;

/**
 * Provides the methods common to all query engines of translation resources
 * that are translation memory systems. 
 */
public interface ITMQuery extends IQuery {

	/**
	 * Sets the maximum number of hits to retrieve.
	 * @param max The maximum number of hits to retrieve.
	 */
	public void setMaximumHits (int max);
	
	/**
	 * Gets the current maximum number of hits to retrieve.
	 * @return the current maximum number of hits to retrieve.
	 */
	public int getMaximumHits ();
	
	/**
	 * Sets the threshold value to use for the query.
	 * @param threshold The threshold value (between 0 and 100).
	 */
	public void setThreshold (int threshold);
	
	/**
	 * Gets the current threshold value to use for the query.
	 * @return The current threshold value to use for the query.
	 */
	public int getThreshold ();

}
