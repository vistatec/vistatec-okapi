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

package net.sf.okapi.virtualdb.jdbc.h2;

import net.sf.okapi.virtualdb.IVItem.ItemType;

class H2Navigator {

	protected long key;
	protected long docKey;
	protected ItemType itemType;
	protected int level;
	protected long parent;
	protected long firstChild;
	protected long next;
	protected long previous;
	
	public H2Navigator () {
		// Default constructor
	}
	
	public H2Navigator (ItemType type,
		long key,
		long docKey,
		int level)
	{
		this.itemType = type;
		this.key = key;
		this.docKey = docKey;
		this.level = level;
		fillPointers(-1, -1, -1, -1);
	}

	public void fillPointers(long parent,
		long firstChild,
		long previous,
		long next)
	{
		this.parent = parent;
		this.firstChild = firstChild;
		this.previous = previous;
		this.next = next;
	}

	public ItemType getItemType () {
		return itemType;
	}
	
	public long getKey () {
		return key;
	}

}
