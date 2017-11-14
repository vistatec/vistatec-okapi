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

import net.sf.okapi.virtualdb.IVDocument;
import net.sf.okapi.virtualdb.IVItem;
import net.sf.okapi.virtualdb.IVSet;
import net.sf.okapi.virtualdb.IVTextUnit;

public class H2Set implements IVSet {

//	private H2Access db;
	private String id;
	private String name;
	private String type;
	
	public H2Set (H2Access access,
		String id,
		String name,
		String type)
	{
		//db = access;
		this.id = id;
		this.name = name;
		this.type = type;
	}

	@Override
	public String getId () {
		return id;
	}
	
	@Override
	public IVItem getFirstChild() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemType getItemType() {
		return null;
	}

	@Override
	public Iterable<IVItem> items() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<IVTextUnit> textUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVDocument getDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName () {
		return name;
	}

	@Override
	public IVItem getNextSibling() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVItem getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVItem getPreviousSibling() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType () {
		return type;
	}

	@Override
	public void save () {
		// No modifiable data to save
	}

	@Override
	public long getKey () {
		return -1L; // TODO
	}

}
