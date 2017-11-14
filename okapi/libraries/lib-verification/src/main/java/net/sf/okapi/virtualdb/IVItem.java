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

public interface IVItem {

	public enum ItemType {
		DOCUMENT,
		SUB_DOCUMENT,
		GROUP,
		TEXT_UNIT
	};

	/**
	 * Gets the extraction id for this item. The extraction id is unique per document.
	 * @return the extraction id for this item.
	 */
	public String getId ();
	
	/**
	 * Gets the storage key for this item. This key is unique for each virtual repository.
	 * @return the key for this item.
	 */
	public long getKey ();
	
	/**
	 * Gets the resource name of this item. This name depends on the type of item and can be null.
	 * @return the resource name of the item (can be null).
	 */
	public String getName ();
	
	/**
	 * Gets the resource type of this item. This type depends on the type of item and can be null.
	 * @return the resource type of this item.
	 */
	public String getType ();
	
	/**
	 * Gets the parent item of this item.
	 * @return the parent item of this item, or null if it has no parent (e.g. for a IVDocument).
	 */
	public IVItem getParent ();
	
	/**
	 * Gets the next item (on the same level) of this item.
	 * @return the next item (on the same level) of this item, or null.
	 */
	public IVItem getNextSibling ();
	
	/**
	 * Gets the previous item (on the same level) of this item.
	 * @return the previous item (on the same level) of this item, or null.
	 */
	public IVItem getPreviousSibling ();
	
	/**
	 * Gets the first child item of this item.
	 * @return the first child item of this item, or null.
	 */
	public IVItem getFirstChild ();
	
	/**
	 * Gets the document containing this item.
	 * @return the document containing this item.
	 */
	public IVDocument getDocument ();
	
	/**
	 * Gets the type of item this item is (document, group, text unit, etc.)
	 * @return one of the ItemType values.
	 */
	public ItemType getItemType ();

	/**
	 * Saves into the repository the modifiable data associated with this virtual item.
	 */
	public void save ();

}
