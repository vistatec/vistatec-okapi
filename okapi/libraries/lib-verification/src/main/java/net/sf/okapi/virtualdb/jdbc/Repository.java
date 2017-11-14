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

package net.sf.okapi.virtualdb.jdbc;

import java.io.InputStream;

import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.virtualdb.IVDocument;
import net.sf.okapi.virtualdb.IVRepository;

public class Repository implements IVRepository {

	private IDBAccess db;
	
	public Repository (IDBAccess engine) {
		db = engine;
	}
	
	@Override
	protected void finalize ()
		throws Throwable
	{
		close();
		super.finalize();
	}
	
	@Override
	public void open (String name,
		OpeningMode mode)
	{
		db.open(name, mode);
	}

	@Override
	public void create (String name) {
		db.create(name);
	}
	
	@Override
	public void open (String name) {
		db.open(name);
	}
	
	@Override
	public void close () {
		if ( db != null ) {
			db.close();
			db = null;
		}
	}
	
	@Override
	public Iterable<IVDocument> documents () {
		return db.documents();
	}

	@Override
	public IVDocument getDocument (long docKey) {
		return db.getDocument(docKey);
	}

//	@Override
//	public Iterable<IVItem> items () {
//		return null;
//	}

//	@Override
//	public Iterable<IVTextUnit> textUnits () {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public String importDocument (RawDocument rawDoc) {
		return db.importDocument(rawDoc);
	}
	
	@Override
	public long importDocumentReturnKey (RawDocument rawDoc) {
		return db.importDocumentReturnKey(rawDoc);
	}

	@Override
	public void removeDocument (IVDocument doc) {
		db.removeDocument(doc);
	}
	
	@Override
	public IVDocument getFirstDocument () {
		return db.getFirstDocument();
	}

	@Override
	public void saveExtraData1 (InputStream inputStream) {
		db.saveExtraData1(inputStream);
	}

	@Override
	public InputStream loadExtraData1 () {
		return db.loadExtraData1();
	}

}
