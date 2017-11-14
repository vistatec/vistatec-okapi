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

import java.util.Iterator;

import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.virtualdb.IVDocument;
import net.sf.okapi.virtualdb.IVGroup;
import net.sf.okapi.virtualdb.IVItem;
import net.sf.okapi.virtualdb.IVTextUnit;

public class H2Group extends H2Navigator implements IVGroup {

	private H2Document doc;
	private String id;
	private String name;
	private String type;
	
	public H2Group (long itemKey,
		H2Document doc,
		String id,
		String name,
		String type)
	{
		this.id = id;
		this.name = name;
		this.type = type;
		this.doc = doc;
		itemType = ItemType.GROUP;
	}
	
	@Override
	public String getId () {
		return id;
	}

	@Override
	public Ending getEndGroup () {
		throw new UnsupportedOperationException("getEndGroup");
	}

	@Override
	public StartGroup getStartGroup () {
		throw new UnsupportedOperationException("getStartGroup");
	}

	@Override
	public IVItem getFirstChild () {
		return doc.db.getItemFromItemKey(doc, firstChild);
	}

	@Override
	public Iterable<IVItem> items () {
		return new Iterable<IVItem>() {
			@Override
			public Iterator<IVItem> iterator() {
				return new H2ItemIterator<IVItem>(doc.db, doc, false);
			}
		}; 
	}
	
	@Override
	public Iterable<IVTextUnit> textUnits () {
		return new Iterable<IVTextUnit>() {
			@Override
			public Iterator<IVTextUnit> iterator() {
				return new H2ItemIterator<IVTextUnit>(doc.db, doc, true);
			}
		}; 
	}

	@Override
	public IVDocument getDocument () {
		return doc;
	}

	@Override
	public String getName () {
		return name;
	}

	@Override
	public IVItem getNextSibling () {
		return doc.db.getItemFromItemKey(doc, next);
	}

	@Override
	public IVItem getParent () {
		return doc.db.getItemFromItemKey(doc, parent);
	}

	@Override
	public IVItem getPreviousSibling () {
		return doc.db.getItemFromItemKey(doc, previous);
	}

	@Override
	public String getType () {
		return type;
	}

	@Override
	public void save () {
		// No modifiable data to save
	}

}
