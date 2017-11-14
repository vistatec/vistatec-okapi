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
import java.util.List;

import net.sf.okapi.common.Util;

public class H2ItemIterator<T> implements Iterator<T> {

	private H2Access db;
	private H2Document doc;
	private List<Long> list;
	private int current = -1;
	
	public H2ItemIterator (H2Access db,
		H2Document doc,
		boolean tuOnly)
	{
		this.db = db;
		this.doc = doc;
		list = db.getItemsKeys(doc.key, tuOnly);
	}

	@Override
	public boolean hasNext () {
		if ( Util.isEmpty(list) ) return false;
		return current < list.size()-1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next () {
		return (T)db.getItemFromItemKey(doc, list.get(++current));
	}

	@Override
	public void remove () {
		throw new UnsupportedOperationException("The method remove() is not supported.");
	}
	
//	public boolean hasPrevious () {
//		if ( Util.isEmpty(list) ) return false;
//		return current > 0;
//	}
//
//	@SuppressWarnings("unchecked")
//	public T previous () {
//		return (T)db.getItemFromKey(doc, list.get(--current));
//	}

}
