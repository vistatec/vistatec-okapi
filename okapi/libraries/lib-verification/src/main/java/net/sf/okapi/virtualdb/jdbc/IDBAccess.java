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
import net.sf.okapi.virtualdb.IVRepository.OpeningMode;

public interface IDBAccess {

	public enum RepositoryType {
		INMEMORY,
		LOCAL,
		REMOTE
	}
	
	public void open (String name,
		OpeningMode mode);

	public void open (String name);
	
	public void create (String name);
	
	public void close () ;
	
	public void delete ();

	public String importDocument (RawDocument rawDoc);
	
	public long importDocumentReturnKey (RawDocument rawDoc);
	
	public void removeDocument (IVDocument doc);
	
//	public IVDocument getDocument (String docId);

	public IVDocument getDocument (long key);

	public Iterable<IVDocument> documents ();

	public IVDocument getFirstDocument();

	public void saveExtraData1 (InputStream inputStream);

	public InputStream loadExtraData1 ();
}
