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

package net.sf.okapi.virtualdb;

/**
 * Stores an item key and an optional segment id.
 * If the segment id is null, the object refers to the whole item.
 */
public class KeyAndSegId {
	
	/**
	 * Creates a new KeyAndSegId object with a given key and segment id.
	 * @param key the key value.
	 * @param segId the segment id (can be null).
	 */
	public KeyAndSegId (long key,
		String segId)
	{
		this.key = key;
		this.segId = segId;
	}
	
	public long key;
	public String segId;

}
