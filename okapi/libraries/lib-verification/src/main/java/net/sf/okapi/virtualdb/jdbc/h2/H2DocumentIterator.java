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
import net.sf.okapi.virtualdb.IVDocument;

public class H2DocumentIterator implements Iterator<IVDocument> {

	private H2Access db;
	private List<Long> list;
	private int current = -1;
	
	public H2DocumentIterator (H2Access db) {
		this.db = db;
		list = db.getDocumentsKeys();
	}

	@Override
	public boolean hasNext () {
		if ( Util.isEmpty(list) ) return false;
		return current < list.size()-1;
	}

	@Override
	public IVDocument next () {
		return db.getDocument(list.get(++current));
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("The method remove() is not supported.");
	}

//	public boolean hasPrevious () {
//		if ( Util.isEmpty(list) ) return false;
//		return current > 0;
//	}
//
//	public IVDocument previous () {
//		return db.getDocument(list.get(--current));
//	}

}
