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

public interface IVSet extends IVItem {

	/**
	 * Gets the first child item of this set.
	 * @return the first child item of this set, or null if there is no child.
	 */
	public IVItem getFirstChild ();
	
	/**
	 * Creates an iterable object for all the items in this set.
	 * @return a new iterable object for all the items in this set.
	 */
	public Iterable<IVItem> items ();
	
	/**
	 * Creates an iterable object for all the virtual text units in this set.
	 * @return a new iterable object for all the virtual text units in this set.
	 */
	public 	Iterable<IVTextUnit> textUnits ();
	
}
