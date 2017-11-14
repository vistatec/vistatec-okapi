/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.packages;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;

/**
 * Provides a common way to read a translation package generated with an 
 * implementation of IWriter. 
 */
public interface IReader {

	/**
	 * Opens the translated document.
	 * @param path The full path of the document to post-process.
	 * @param sourceLanguage The code of the source language.
	 * @param targetLanguage The code of the target language.
	 */
	public void openDocument (String path,
		LocaleId sourceLanguage,
		LocaleId targetLanguage);
	
	/**
	 * Closes the document.
	 */
	public void closeDocument ();

	/**
	 * Reads the next item to post-process.
	 * @return True if an item is available.
	 */
	public boolean readItem ();
	
	/**
	 * Gets the last TextUnit object read.
	 * @return The last TextUnit object read.
	 */
	public ITextUnit getItem ();
	
}
