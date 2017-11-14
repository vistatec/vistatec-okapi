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

import java.io.InputStream;

import net.sf.okapi.common.resource.RawDocument;

public interface IVRepository {

	public enum OpeningMode {
		MUST_EXIST,
		CREATE_IF_NEEDED,
		OVERWRITE
	}

	public void open (String name,
		OpeningMode mode);
	
	/**
	 * Creates a new physical repository.
	 * @param name the name of the repository.
	 */
	public void create (String name);
	
	/**
	 * Opens an existing physical repository.
	 * @param name the name of the repository.
	 */
	public void open (String name);
	
	/** 
	 * Closes the repository.
	 */
	public void close ();
	
//	/**
//	 * Gets the document associated with a given id.
//	 * @param docId the id of the document to retrieve.
//	 * @return the document associated with the given id.
//	 */
//	//TODO: fix this type of access: docId is not useable in a transparent way
//	public IVDocument getDocument (String docId);
	
	/**
	 * Creates an iterable object for all the documents contained into this repository.
	 * @return an new iterable object for all the items contained into this repository.
	 */
	public Iterable<IVDocument> documents ();

//	/**
//	 * Creates an iterable object for all the items contained into this repository.
//	 * @return an new iterable object for all the items contained into this repository.
//	 */
//	public Iterable<IVItem> items ();

//	/**
//	 * Creates an iterable object for all the virtual text units contained into this document.
//	 * @return an new iterable object for all the virtual text units contained into this document.
//	 */
//	public Iterable<IVTextUnit> textUnits ();

	/**
	 * Imports a document into this repository.
	 * @param rawDoc the document to import (must be URI based).
	 * @return the document id of the imported document.
	 */
	public String importDocument (RawDocument rawDoc);

	public long importDocumentReturnKey (RawDocument rawDoc);

	/**
	 * Removes a given document from this repository.
	 * <p>Calling this method may invalidate any current iterator created by {@link #documents()},
	 * as well as the sibling relationships for any current {@link IVDocument} corresponding to 
	 * the previous and next documents of this one.
	 * <p>To remove all documents from a repository it may be simpler to re-create the repository.
	 * @param doc the virtual document to remove.
	 */
	public void removeDocument (IVDocument doc);
	
	/**
	 * Gets the first virtual document in this repository. 
	 * @return the first virtual document in this repository or null.
	 */
	public IVDocument getFirstDocument ();

	/**
	 * Gets the virtual document for a given document key.
	 * @param docKey the key of the document to retrieve.
	 * @return the virtual document for the given key, or null if no document could be retrieve.
	 */
	IVDocument getDocument (long docKey);

	/**
	 * Saves a block of extra data into the repository.
	 * @param inputStream the input stream of the object to save.
	 */
	public void saveExtraData1 (InputStream inputStream);

	public InputStream loadExtraData1 ();

}
