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

public abstract class H2Item extends H2Navigator implements IVItem {

	protected H2Document doc;
	protected String id;
	protected String name;
	protected String type;

	@Override
	public IVDocument getDocument () {
		return doc;
	}

	@Override
	public String getId () {
		return id;
	}

	@Override
	public String getName () {
		return name;
	}

	@Override
	public IVItem getParent () {
		return doc.db.getItemFromItemKey(doc, parent);
	}

	@Override
	public IVItem getNextSibling () {
		return doc.db.getItemFromItemKey(doc, next);
	}

	@Override
	public IVItem getPreviousSibling () {
		return doc.db.getItemFromItemKey(doc, previous);
	}
	
	public IVItem getFirstChild () {
		return doc.db.getItemFromItemKey(doc, firstChild);
	}

	@Override
	public String getType () {
		return type;
	}

}
