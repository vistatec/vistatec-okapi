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

import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.StartDocument;

public interface IVDocument extends IVSet {

	/**
	 * Gets the StartDocument resource associated with this document.
	 * @return the StartDocument resource associated with this document.
	 */
	public StartDocument getStartDocument ();
	
	/**
	 * Gets the Ending resource associated with this document.
	 * @return the Ending resource associated with this document.
	 */
	public Ending getEndDocument ();
	
	/**
	 * Gets the item for a given extraction id.
	 * @param extractionId the extraction id for the item to retrieve.
	 * @return the item for the given extraction id.
	 */
	public IVItem getItem (String extractionId);
	
	/**
	 * Gets the item for a given key.
	 * @param key the key for the item to retrieve.
	 * @return the item retrieved for the given key.
	 */
	public IVItem getItem (long key);
	
	/**
	 * Gets the virtual text unit for a given extraction id.
	 * @param extractionId the extraction id of the text unit to retrieve.
	 * @return the virtual text unit for a given extraction id.
	 */
	public IVTextUnit getTextUnit (String extractionId);
	
}
